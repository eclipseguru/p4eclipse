/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.preferences;

import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.ui.LabelProviderAdapter;
import com.perforce.team.ui.mylyn.IP4MylynUiConstants;
import com.perforce.team.ui.mylyn.PerforceUiMylynPlugin;
import com.perforce.team.ui.mylyn.editor.JobField;
import com.perforce.team.ui.mylyn.editor.JobFieldGroup;

import org.eclipse.swt.graphics.Image;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class JobSpecLabelProvider extends LabelProviderAdapter {

    private Image dateImage = PerforceUiMylynPlugin
            .getImage(IP4MylynUiConstants.IMG_DATE);
    private Image tabImage = PerforceUiMylynPlugin
            .getImage(IP4MylynUiConstants.IMG_TAB);
    private Image textImage = PerforceUiMylynPlugin
            .getImage(IP4MylynUiConstants.IMG_TEXT);
    private Image selectImage = PerforceUiMylynPlugin
            .getImage(IP4MylynUiConstants.IMG_SELECT);
    private Image wordImage = PerforceUiMylynPlugin
            .getImage(IP4MylynUiConstants.IMG_WORD);

    /**
     * @see com.perforce.team.ui.LabelProviderAdapter#getImage(java.lang.Object)
     */
    @Override
    public Image getImage(Object element) {
        if (element instanceof JobField) {
            JobField field = (JobField) element;
            String type = field.getField().getDataType();
            if (IP4Job.DATE_DATA_TYPE.equals(type)) {
                return dateImage;
            } else if (IP4Job.SELECT_DATA_TYPE.equals(type)) {
                return selectImage;
            } else if (IP4Job.WORD_DATA_TYPE.equals(type)) {
                return wordImage;
            } else {
                return textImage;
            }
        } else if (element instanceof JobFieldGroup) {
            return tabImage;
        }
        return null;
    }

    /**
     * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
     */
    public String getText(Object element) {
        if (element instanceof JobField) {
            return ((JobField) element).getField().getName();
        } else if (element instanceof JobFieldGroup) {
            return ((JobFieldGroup) element).getTitle();
        }
        return ""; //$NON-NLS-1$
    }

}
