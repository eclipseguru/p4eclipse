/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.text.timelapse.form;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IStorageEditorInput;

import com.perforce.p4java.CharsetDefs;
import com.perforce.p4java.core.IJobSpec;
import com.perforce.p4java.core.IJobSpec.IJobSpecField;
import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.ui.text.timelapse.form.FormField.Type;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Form {

    private SortedSet<FormField> fields;
    private IP4Connection connection = null;
    private String author = null;

    /**
     * Create a empty form
     * 
     * @param connection
     */
    public Form(IP4Connection connection) {
        this.connection = connection;
        this.fields = new TreeSet<FormField>(new Comparator<FormField>() {

            public int compare(FormField o1, FormField o2) {
                return o1.getOffset() - o2.getOffset();
            }
        });
    }

    /**
     * Get author of form
     * 
     * @return author or null if none
     */
    public String getAuthor() {
        return this.author;
    }

    /**
     * Get first form field
     * 
     * @return - first form field
     */
    public FormField getFirst() {
        return this.fields.first();
    }

    /**
     * Get fields in this form
     * 
     * @return - non-null but possibly empty array of form fields
     */
    public FormField[] getFields() {
        return this.fields.toArray(new FormField[this.fields.size()]);
    }

    /**
     * Build the form from an input object
     * 
     * @param input
     */
    public void build(Object input) {
        try {
            if (input instanceof IStorageEditorInput) {
                parseStream(((IStorageEditorInput) input).getStorage()
                        .getContents());
            } else if (input instanceof IStorage) {
                parseStream(((IStorage) input).getContents());
            } else if (input instanceof IDocument) {
                parseStream(new ByteArrayInputStream(((IDocument) input).get()
                        .getBytes(CharsetDefs.DEFAULT)));
            } else if (input instanceof String) {
                parseStream(new ByteArrayInputStream(
                        ((String) input).getBytes(CharsetDefs.DEFAULT)));
            }
        } catch (CoreException e) {
            fields.clear();
        } catch (IOException e) {
            fields.clear();
        }
    }

    private void addField(String name, StringBuilder value, int offset,
            int length, Type type, IJobSpecField ownerField) {
        if (name != null && value != null && offset >= 0 && length > 0) {
            FormField field = new FormField(name, value.toString(), offset,
                    length, type);
            if (!fields.contains(field)) {
                fields.add(field);
            }
            if (this.author == null && ownerField != null
                    && name.equals(ownerField.getName())) {
                this.author = value.toString();
            }
        }
    }

    private String readLine(BufferedReader reader) throws IOException {
        int read = reader.read();
        if (read == -1) {
            return null;
        }
        StringBuilder line = new StringBuilder();
        while (read != -1) {
            line.append((char) read);
            if (read == '\r') {
                reader.mark(1);
                int ahead = reader.read();
                if (ahead == '\n') {
                    line.append((char) read);
                } else {
                    reader.reset();
                }
                break;
            } else if (read == '\n') {
                break;
            }
            read = reader.read();
        }
        return line.toString();
    }

    private void parseStream(InputStream stream) throws IOException {
    	try {
    		Map<String, Type> typeMap = new HashMap<String, Type>();
    		IJobSpecField ownerField = null;
    		if (this.connection != null) {
    			IJobSpec jobSpec = connection.getJobSpec();
    			if (jobSpec != null) {
    				for (IJobSpecField field : jobSpec.getFields()) {
    					if (IP4Job.DATE_DATA_TYPE.equals(field.getDataType())) {
    						typeMap.put(field.getName(), Type.DATE);
    					} else if (IP4Job.TEXT_DATA_TYPE
    							.equals(field.getDataType())) {
    						typeMap.put(field.getName(), Type.TEXT);
    					} else if (IP4Job.SELECT_DATA_TYPE.equals(field
    							.getDataType())) {
    						typeMap.put(field.getName(), Type.SELECT);
    					} else if (IP4Job.WORD_DATA_TYPE
    							.equals(field.getDataType())) {
    						typeMap.put(field.getName(), Type.WORD);
    					}
    					if (ownerField == null
    							&& IP4Job.ALWAYS_FIELD_TYPE.equals(field
    									.getFieldType())
    									&& IP4Job.USER_PRESET.equals(jobSpec.getPresets()
    											.get(field.getName()))) {
    						ownerField = field;
    					}
    				}
    			}
    		}
    		BufferedReader reader = new BufferedReader(
    				new InputStreamReader(stream, ConnectionParameters.getJavaCharset(connection)));
    		String line = readLine(reader);
    		String name = null;
    		StringBuilder value = null;
    		int offset = 0;
    		int length = 0;
    		int read = 0;
    		while (line != null) {
    			if (line.length() > 0) {
    				char first = line.charAt(0);
    				if (first != '#') {
    					if (first == '\t') {
    						if (name != null) {
    							if (value == null) {
    								value = new StringBuilder(line);
    							} else {
    								value.append(line);
    							}
    						}
    					} else if (!Character.isWhitespace(first)) {
    						int firstColon = line.indexOf(':');
    						if (firstColon > 0) {
    							length = read - offset;
    							addField(name, value, offset, length,
    									typeMap.get(name), ownerField);
    							offset = read;
    							name = line.substring(0, firstColon);
    							if (firstColon + 1 < line.length()) {
    								String valueStart = line
    										.substring(firstColon + 1);
    								value = new StringBuilder(valueStart.trim());
    							} else {
    								value = new StringBuilder();
    							}
    						}
    					}
    				}
    			} else {
    				// Empty line means the close of a field
    				length = read - offset;
    				addField(name, value, offset, length, typeMap.get(name),
    						ownerField);
    			}
    			read += line.length();
    			line = readLine(reader);
    		}
    		length = read - offset;
    		addField(name, value, offset, length, typeMap.get(name), ownerField);
		} finally {
			if(stream!=null)
				stream.close();
		}
    }
}
