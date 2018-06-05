/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.timelapse;

import com.perforce.p4java.core.file.FileAction;
import com.perforce.p4java.core.file.IFileRevisionData;
import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.decorator.P4Decoration;
import com.perforce.team.ui.decorator.PerforceDecorator;
import com.perforce.team.ui.preferences.IPreferenceConstants;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class FolderLabelDecorator implements ILabelDecorator {

    private IP4Revision revision;
    private String fileDecoration = null;

    /**
     * Folder label decorator
     */
    public FolderLabelDecorator() {
        this.fileDecoration = PerforceUIPlugin.getPlugin().getPreferenceStore()
                .getString(IPreferenceConstants.FILE_DECORATION_TEXT);
    }

    /**
     * Set revision to base decorations on
     * 
     * @param revision
     */
    public void setRevision(IP4Revision revision) {
        this.revision = revision;
    }

    /**
     * @see org.eclipse.jface.viewers.ILabelDecorator#decorateImage(org.eclipse.swt.graphics.Image,
     *      java.lang.Object)
     */
    public Image decorateImage(Image image, Object element) {
        return null;
    }

    /**
     * @see org.eclipse.jface.viewers.ILabelDecorator#decorateText(java.lang.String,
     *      java.lang.Object)
     */
    public String decorateText(String text, Object element) {
        if (revision != null & element instanceof FileEntry) {
            FileEntry file = (FileEntry) element;
            int id = revision.getChangelist();
            IFileRevisionData data = file.getData(id);
            Map<String, String> bindings = new HashMap<String, String>();
            if (data.getRevision() > 0) {
                bindings.put(P4Decoration.HAVE_VARIABLE,
                        Integer.toString(data.getRevision()));
            }

            FileAction action = data.getAction();
            String actionText = action != null ? action.toString()
                    .toLowerCase() : null;

            if (actionText != null) {
                bindings.put(P4Decoration.ACTION_VARIABLE, actionText);
            }

            bindings.put(P4Decoration.NAME_VARIABLE, text);

            StringBuilder decorated = P4Decoration.decorate(fileDecoration,
                    bindings);

            decorated.append(MessageFormat.format(
                    Messages.FolderLabelDecorator_ChangelistSuffix, file
                            .getData(id).getChangelistId()));
            return PerforceDecorator.removeTrailingWhitespace(decorated);
        }
        return text;
    }

    /**
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    public void addListener(ILabelProviderListener listener) {

    }

    /**
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
     */
    public void dispose() {

    }

    /**
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object,
     *      java.lang.String)
     */
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    /**
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    public void removeListener(ILabelProviderListener listener) {

    }

}
