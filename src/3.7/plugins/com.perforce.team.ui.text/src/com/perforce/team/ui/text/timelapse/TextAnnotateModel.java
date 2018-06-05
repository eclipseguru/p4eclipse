/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.text.timelapse;

import com.perforce.p4java.CharsetDefs;
import com.perforce.p4java.core.file.IFileAnnotation;
import com.perforce.p4java.server.PerforceCharsets;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.core.p4java.P4Storage;
import com.perforce.team.ui.timelapse.RevisionLapseInput;
import com.perforce.team.ui.timelapse.TimeLapseUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.ui.IStorageEditorInput;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class TextAnnotateModel implements ITextAnnotateModel {

    private IP4File file;
    private int count = 0;
    private String prefix;
    private SortedMap<Integer, Annotation> annotations;
    private ListenerList listeners;
    private boolean useChangelistKey = true;

    /**
     * Create an annotate model
     * 
     * @param file
     */
    public TextAnnotateModel(IP4File file) {
        this.file = file;
        this.annotations = new TreeMap<Integer, Annotation>();
        this.listeners = new ListenerList();
    }

    /**
     * Create an annotate model
     * 
     * @param file
     * @param prefix
     */
    public TextAnnotateModel(IP4File file, String prefix) {
        this(file);
        this.prefix = prefix;
    }

    /**
     * Create an annotate model
     * 
     * @param file
     * @param prefix
     * @param useChangelistKey
     *            - true to key off changelists, false to key off revisions
     */
    public TextAnnotateModel(IP4File file, String prefix,
            boolean useChangelistKey) {
        this(file, prefix);
        this.useChangelistKey = useChangelistKey;
    }

    /**
     * Get number of revisions
     * 
     * @return - revision count
     */
    public int getRevisionCount() {
        return this.annotations.size();
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.ITextAnnotateModel#getLineRanges(com.perforce.team.core.p4java.IP4Revision)
     */
    public ILineRange[] getLineRanges(IP4Revision revision) {
        List<ILineRange> chunks = new ArrayList<ILineRange>();
        Line[] lines = getLines(revision);
        if (lines.length > 1) {
            Line line = lines[0];
            int length = 1;
            int start = 0;
            for (int i = 1; i < lines.length; i++) {
                Line next = lines[i];
                if (next.lower == line.lower && next.upper == line.upper) {
                    length++;
                } else {
                    chunks.add(new LineRange(start, length));
                    length = 1;
                    start = i;
                    line = next;
                }
            }
            chunks.add(new LineRange(start, length));
        } else if (lines.length == 1) {
            chunks.add(new LineRange(0, 1));
        }
        return chunks.toArray(new LineRange[chunks.size()]);
    }

    /**
     * @see com.perforce.team.ui.timelapse.IAnnotateModel#getRevision(int)
     */
    public int getRevision(int changelist) {
        int rev = -1;
        Annotation annotation = this.annotations.get(changelist);
        if (annotation != null) {
            rev = annotation.current.getRevision();
        }
        return rev;
    }

    /**
     * @see com.perforce.team.ui.timelapse.IAnnotateModel#getAuthor(int)
     */
    public String getAuthor(int changelist) {
        String author = null;
        Annotation annotation = this.annotations.get(changelist);
        if (annotation != null) {
            author = annotation.current.getAuthor();
        }
        return author;
    }

    /**
     * @see com.perforce.team.ui.timelapse.IAnnotateModel#getDate(int)
     */
    public String getDate(int changelist) {
        String date = null;
        Annotation annotation = this.annotations.get(changelist);
        if (annotation != null) {
            date = TimeLapseUtils.format(annotation.current.getTimestamp());
        }
        return date;
    }

    /**
     * @see com.perforce.team.ui.timelapse.IAnnotateModel#getNext(com.perforce.team.core.p4java.IP4Revision)
     */
    public IP4Revision getNext(IP4Revision revision) {
        IP4Revision next = null;
        if (revision != null) {
            Annotation annotation = this.annotations
                    .get(getRevisionId(revision));
            if (annotation != null) {
                next = annotation.next;
            }
        }
        return next;
    }

    /**
     * @see com.perforce.team.ui.timelapse.IAnnotateModel#getRevisionId(com.perforce.team.core.p4java.IP4Revision)
     */
    public int getRevisionId(IP4Revision revision) {
        if (useChangelistKey) {
            return revision.getChangelist();
        } else {
            return revision.getRevision();
        }
    }

    /**
     * @see com.perforce.team.ui.timelapse.IAnnotateModel#getLatest()
     */
    public IP4Revision getLatest() {
        return this.annotations.get(this.annotations.lastKey()).current;
    }

    /**
     * @see com.perforce.team.ui.timelapse.IAnnotateModel#getEarliest()
     */
    public IP4Revision getEarliest() {
        return this.annotations.get(this.annotations.firstKey()).current;
    }

    /**
     * 
     * @see com.perforce.team.ui.text.timelapse.ITextAnnotateModel#getLines(com.perforce.team.core.p4java.IP4Revision)
     */
    public Line[] getLines(IP4Revision revision) {
        Line[] lines = new Line[0];
        if (revision != null) {
            Annotation annotation = this.annotations
                    .get(getRevisionId(revision));
            if (annotation != null) {
                lines = annotation.lines;
            }
        }
        return lines;
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.ITextAnnotateModel#getPositionTo(int,
     *      com.perforce.team.core.p4java.IP4Revision)
     */
    public int getPositionTo(int lineNumber, IP4Revision revision) {
        int from = 0;
        if (revision != null && lineNumber >= 0) {
            Line[] lines = getLines(revision);
            if (lineNumber < lines.length) {
                Line line = lines[lineNumber];
                Integer[] changelists = this.annotations.keySet().toArray(
                        new Integer[0]);
                int revChangelist = getRevisionId(revision);
                for (int i = 0; i < changelists.length; i++) {
                    int changelist = changelists[i].intValue();
                    if (changelist >= line.lower) {
                        if (changelist <= revChangelist) {
                            from++;
                        } else {
                            break;
                        }
                    }
                }
            }
        }
        return from;
    }

    /**
     * @see com.perforce.team.ui.timelapse.IAnnotateModel#load(com.perforce.team.core.p4java.IP4Revision[],
     *      boolean, com.perforce.team.core.p4java.IP4File.WhitespaceIgnoreType)
     */
    public void load(IP4Revision[] revisions, boolean includeBranches,
            IP4File.WhitespaceIgnoreType ignoreType) {
        if (!useChangelistKey) {
            includeBranches = false;
        }
        IFileAnnotation[] fas = file.getAnnotations(includeBranches,
                ignoreType, useChangelistKey);
    	Line[] lines = fas==null?new Line[0]:generateLines(fas);
        load(revisions, lines);
        for (Object listener : listeners.getListeners()) {
            ((IModelListener) listener).loaded(this);
        }
    }

    private Line[] generateLines(IFileAnnotation[] annotations) {
        List<Line> lines = new ArrayList<Line>();
        if(annotations!=null){
	        for (int i = 0; i < annotations.length; i++) {
	            IFileAnnotation entry = annotations[i];
	            Line line = new Line();
	            line.lower = entry.getLower();
	            line.upper = entry.getUpper();
	            line.data = entry.getLine(true);
	            lines.add(line);
	        }
        }
        return lines.toArray(new Line[lines.size()]);
    }

    private void load(IP4Revision[] revisions, Line[] lines) {
        for (int i = 0; i < revisions.length; i++) {
            IP4Revision revision = revisions[i];
            List<Line> document = new ArrayList<Line>();
            int list = getRevisionId(revision);
            for (Line line : lines) {
                if (list >= line.lower && list <= line.upper) {
                    document.add(line);
                }
            }
            Annotation annotation = new Annotation();
            annotation.current = revision;
            annotation.lines = document.toArray(new Line[document.size()]);
            if (i + 1 < revisions.length) {
                annotation.next = revisions[i + 1];
            }
            this.annotations.put(list, annotation);
        }
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.ITextAnnotateModel#isLatestDifferent(com.perforce.team.core.p4java.IP4Revision,
     *      com.perforce.team.ui.text.timelapse.ITextAnnotateModel.Line, int)
     */
    public boolean isLatestDifferent(IP4Revision revision, Line line, int number) {
        Line[] latest = getLines(getLatest());
        if (number < latest.length) {
            return !StringUtils.equals(line.data,latest[number].data);
        }
        return false;
    }

    private byte[] getBytes(IP4Revision revision) {
        StringBuilder builder = new StringBuilder();
        Annotation annotation = this.annotations.get(getRevisionId(revision));
        for (Line line : annotation.lines) {
            builder.append(line.data);
        }
        String charsetName = revision.getCharset();
        if (charsetName != null) {
            charsetName = PerforceCharsets.getJavaCharsetName(charsetName);
            if (charsetName != null) {
                try {
                    return builder.toString().getBytes(charsetName);
                } catch (UnsupportedEncodingException e) {
                    PerforceProviderPlugin.logError(e);
                }
            }
        }
        return builder.toString().getBytes(P4CoreUtils.charsetForName(PerforceCharsets.getJavaCharsetName(revision.getCharset())));
    }

    private IStorage createStorage(final IP4Revision revision) {
        byte[] bytes = getBytes(revision);
        final ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        final StringBuilder storageName = new StringBuilder();
        if (prefix != null) {
            storageName.append(prefix);
            storageName.append(count);
            count++;
        }
        storageName.append(revision.getName());
        IStorage storage = new P4Storage() {

            @Override
            public String getName() {
                return storageName.toString();
            }

            public InputStream getContents() throws CoreException {
                return stream;
            }
        };
        return storage;
    }

    /**
     * @see com.perforce.team.ui.timelapse.IAnnotateModel#generateInput(com.perforce.team.core.p4java.IP4Revision)
     */
    public IStorageEditorInput generateInput(final IP4Revision p4Revision) {
        RevisionLapseInput input = new RevisionLapseInput(p4Revision) {

            @Override
            protected IStorage getWrappedStorage() throws CoreException {
                return createStorage(p4Revision);
            }
        };
        return input;
    }

    /**
     * @see com.perforce.team.ui.timelapse.IAnnotateModel#clear()
     */
    public void clear() {
        this.annotations.clear();
    }

    /**
     * @see com.perforce.team.ui.timelapse.IAnnotateModel#addListener(com.perforce.team.ui.timelapse.IAnnotateModel.IModelListener)
     */
    public void addListener(IModelListener listener) {
        if (listener != null) {
            this.listeners.add(listener);
        }
    }

    /**
     * @see com.perforce.team.ui.timelapse.IAnnotateModel#removeListener(com.perforce.team.ui.timelapse.IAnnotateModel.IModelListener)
     */
    public void removeListener(IModelListener listener) {
        if (listener != null) {
            this.listeners.remove(listener);
        }
    }

    /**
     * @see com.perforce.team.ui.timelapse.IAnnotateModel#getRevisionById(int)
     */
    public IP4Revision getRevisionById(int id) {
        Annotation annotation = this.annotations.get(id);
        if (annotation != null) {
            return annotation.current;
        }
        return null;
    }

}
