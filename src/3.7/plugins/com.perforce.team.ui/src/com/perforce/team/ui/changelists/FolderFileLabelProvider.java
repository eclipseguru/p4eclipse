/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.changelists;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.ui.changelists.Folder.Type;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class FolderFileLabelProvider extends ChangelistLabelProvider {

    /**
     * Type
     */
    protected Type type = null;

    /**
     *
     */
    public FolderFileLabelProvider() {
        this(Type.FLAT);
    }

    /**
     * 
     * @param type
     */
    public FolderFileLabelProvider(Type type) {
        super(true);
        setType(type);
    }

    /**
     * Set display type
     * 
     * @param type
     */
    public void setType(Type type) {
        if (type != null) {
            this.type = type;
        }
    }

    /**
     * @see com.perforce.team.ui.PerforceLabelProvider#getColumnText(java.lang.Object,
     *      int)
     */
    @Override
    public String getColumnText(Object element, int columnIndex) {
        if (element instanceof IP4Resource) {
            // Only process resources that adapt to files
            IP4File file = P4CoreUtils.convert(element, IP4File.class);
            if (file != null) {
                String text = null;
                // Use remote path in flat mode, else use name
                if (type == Type.FLAT) {
                    text = ((IP4Resource) element)
                            .getActionPath(IP4Resource.Type.REMOTE);
                } else {
                    text = ((IP4Resource) element).getName();
                }
                if (text != null) {
                    return getDecoratedText(text, element);
                }
            }
        }
        return super.getColumnText(element, columnIndex);
    }

}
