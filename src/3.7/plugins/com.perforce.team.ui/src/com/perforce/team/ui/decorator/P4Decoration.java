/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.decorator;

import java.util.Map;

import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.preferences.IPreferenceConstants;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4Decoration {

    public static final String NAME_VARIABLE = "name";
    public static final String CLIENT_VARIABLE = "client";
    public static final String USER_VARIABLE = "user";
    public static final String SERVER_VARIABLE = "server";
    public static final String CHARSET_VARIABLE = "charset";
    public static final String HAVE_VARIABLE = "have";
    public static final String HEAD_VARIABLE = "head";
    public static final String HEAD_CHANGE_VARIABLE = "head_change";
    public static final String OFFLINE_VARIABLE = "offline";
    public static final String TYPE_VARIABLE = "type";
    public static final String ACTION_VARIABLE = "action";
    public static final String OUTGOING_CHANGE_VARIABLE = "outgoing_change_flag";
    public static final String UNADDED_CHANGE_VARIABLE = "not_under_version_control";
    public static final String SANDBOX_VARIABLE = "sandbox";
    public static final String STREAM_NAME_VARIABLE = "stream_name";
    public static final String STREAM_ROOT_VARIABLE = "stream_root";

    /**
     * Create a file decoration string with the variables specified
     * 
     * @param variables
     * @return - decorated string label
     */
    public static String decorateFile(Map<String, String> variables) {
        return decorate(
                PerforceUIPlugin.getPlugin().getPreferenceStore()
                        .getString(IPreferenceConstants.FILE_DECORATION_TEXT),
                variables).toString();
    }

    /**
     * Create a project decoration string with the variables specified
     * 
     * @param variables
     * @return - decorated string label
     */
    public static String decorateProject(Map<String, String> variables) {
        return decorate(
                PerforceUIPlugin
                        .getPlugin()
                        .getPreferenceStore()
                        .getString(IPreferenceConstants.PROJECT_DECORATION_TEXT),
                variables).toString();
    }

    /**
     * Create a decoration string using the format string and variables
     * specified
     * 
     * @param format
     * @param variables
     * @return - decorated string label
     */
    public static StringBuilder decorate(String format,
            Map<String, String> variables) {
        StringBuilder output = new StringBuilder();

        int length = format.length();
        int start = -1;
        int end = length;
        boolean containsName = false;
        while (true) {
            end = format.indexOf('{', start);
            if (end > -1) {
                output.append(format.substring(start + 1, end));
                start = format.indexOf('}', end);
                if (start > -1) {
                    String key = format.substring(end + 1, start);
                    String optionalPrefix = null;
                    String optionalSuffix = null;
                    int optionStart = key.indexOf('[');
                    if (optionStart > -1) {
                        int optionEnd = key.lastIndexOf(']');
                        if (optionEnd > -1) {
                            optionalPrefix = key.substring(0, optionStart);
                            optionalSuffix = key.substring(optionEnd + 1);
                            key = key.substring(optionStart + 1, optionEnd);
                        }
                    }
                    if (!containsName && key.equals(NAME_VARIABLE)) {
                        containsName = true;
                    }
                    String value = variables.get(key);

                    if (value != null) {
                        if (optionalPrefix != null) {
                            output.append(optionalPrefix);
                        }
                        output.append(value);
                        if (optionalSuffix != null) {
                            output.append(optionalSuffix);
                        }
                    }
                } else {
                    output.append(format.substring(end, length));
                    break;
                }
            } else {
                output.append(format.substring(start + 1, length));
                break;
            }
        }

        if (!containsName) {
            String name = variables.get(NAME_VARIABLE);
            if (name != null) {
                output.append(" " + name);
            }
        }

        return output;
    }

	protected static StringBuilder trimComma(StringBuilder output) {
		StringBuilder sb = new StringBuilder();
		boolean stillSP=true;
		for(int i=0;i<output.length();i++){
			char c = output.charAt(i);
			if(stillSP){
				if(c==' ' || ','==c)
					continue;
				else {
					stillSP=false;
				}
			}
			sb.append(c);
		}

		if(sb.length()>0){
			stillSP=true;
			int end=-1;
			for(int i=sb.length()-1;i>0;i--){
				char c = output.charAt(i);
				if(stillSP){
					if(c==' ' || ','==c)
						continue;
					else{
						end=i;
						break;
					}
				}
			}
			if(end>=0 && end+1<sb.length())
				sb=sb.delete(end+1, sb.length());
		}
		
		return sb;
	}

}
