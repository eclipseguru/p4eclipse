/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.changelists;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.ui.StyledLabelProvider;

import java.text.MessageFormat;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StyledString;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class StyledChangelistLabelProvider extends StyledLabelProvider {

    /**
     * @param labelProvider
     */
    public StyledChangelistLabelProvider(ILabelProvider labelProvider) {
        super(labelProvider);
    }

    /**
     * @see com.perforce.team.ui.LabelProviderAdapter#getStyledText(java.lang.Object)
     */
    @Override
    public StyledString getStyledText(Object element) {
        StyledString styled = super.getStyledText(element);
        IP4Changelist list = P4CoreUtils.convert(element, IP4Changelist.class);
        if (list != null) {
            addCounter(list, styled);
            addQualifier(list, styled);

        }
        return styled;
    }

    /**
     * Add counter text
     * 
     * @param list
     * @param styled
     */
    protected void addCounter(IP4Changelist list, StyledString styled) {
        if (!list.needsRefresh()) {
            styled.append(
                    MessageFormat.format(" ({0})", list.getFiles().length), //$NON-NLS-1$
                    StyledString.COUNTER_STYLER);
        }
    }

    /**
     * Add qualifier text
     * 
     * @param list
     * @param styled
     */
    protected void addQualifier(IP4Changelist list, StyledString styled) {
        if (list instanceof IP4PendingChangelist
                || list instanceof IP4SubmittedChangelist) {
            String user = list.getUserName();
            String client = list.getClientName();
            if (user != null && client != null) {
                styled.append(MessageFormat.format(" {0}@{1}", user, client), //$NON-NLS-1$
                        StyledString.QUALIFIER_STYLER);
            }
        }
    }
}
