/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.changelists;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4ShelvedChangelist;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.ui.diff.DiffLabelProvider;

import java.text.MessageFormat;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ChangelistLabelProvider extends DiffLabelProvider {

    /**
     * Create a changelist label provider
     * 
     * @param decorateResources
     */
    public ChangelistLabelProvider(boolean decorateResources) {
        super(decorateResources, false, true);
    }

    /**
     * @see com.perforce.team.ui.PerforceLabelProvider#getColumnText(java.lang.Object,
     *      int)
     */
    @Override
    public String getColumnText(Object element, int columnIndex) {
        IP4File file = P4CoreUtils.convert(element, IP4File.class);
        if (file != null) {
            return file.getRemotePath();
        } else {
            IP4Changelist list = P4CoreUtils.convert(element,
                    IP4Changelist.class);
            if (list instanceof IP4PendingChangelist) {
                if (list.isDefault()) {
                    return Messages.ChangelistLabelProvider_DefaultChange;
                } else {
                    int id = list.getId();
                    if (id > 0) {
                        return MessageFormat.format(
                                Messages.ChangelistLabelProvider_ChangeNumber,
                                id);
                    } else {
                        return Messages.ChangelistLabelProvider_Change;
                    }
                }
            } else if (list instanceof IP4SubmittedChangelist) {
                int id = list.getId();
                if (id > 0) {
                    return MessageFormat.format(
                            Messages.ChangelistLabelProvider_ChangeNumber, id);
                } else {
                    return Messages.ChangelistLabelProvider_Change;
                }
            } else if (list instanceof IP4ShelvedChangelist) {
                return Messages.ChangelistLabelProvider_ShelvedFiles;
            }
        }
        return super.getColumnText(element, columnIndex);
    }

}
