/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.views.tasks;

import java.text.MessageFormat;

import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.model.IWorkbenchAdapter;

import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.changelists.ChangelistDecorator;
import com.perforce.team.ui.changelists.ChangelistLabelProvider;
import com.perforce.team.ui.changelists.StyledChangelistLabelProvider;
import com.perforce.team.ui.mergequest.views.tasks.IIntegrateTaskContainer.Mode;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class IntegrateTaskLabelProvider {

    private IIntegrateTaskContainer container;
    private Font bold;

    private ILabelProvider viewerLabelProvider = new ChangelistLabelProvider(
            true);

    private IStyledLabelProvider styleProvider = new StyledChangelistLabelProvider(
            viewerLabelProvider) {

        Styler boldStyler = new Styler() {

            @Override
            public void applyStyles(TextStyle textStyle) {
                textStyle.font = bold;
            }
        };

        private void addCount(StyledString styled, IWorkbenchAdapter adapter) {
            int count = adapter.getChildren(adapter).length;
            String counter = MessageFormat.format(
                    Messages.IntegrateTaskLabelProvider_Count, count);
            styled.append(counter, StyledString.COUNTER_STYLER);
        }

        @Override
        public StyledString getStyledText(Object element) {
            StyledString styled = new StyledString();
            if (element instanceof UserTaskGroup
                    && ((UserTaskGroup) element).isCurrentUser()) {
                styled.append(viewerLabelProvider.getText(element), boldStyler);
            } else if (element instanceof JobTaskGroup
                    && ((JobTaskGroup) element).getJob() == null) {
                styled.append(viewerLabelProvider.getText(element), boldStyler);
            } else {
                styled.append(super.getStyledText(element));
            }
            if (element instanceof ITaskGroup) {
                addCount(styled, (ITaskGroup) element);
            }
            return styled;
        }

        /**
         * @see com.perforce.team.ui.changelists.StyledChangelistLabelProvider#addQualifier(com.perforce.team.core.p4java.IP4Changelist,
         *      org.eclipse.jface.viewers.StyledString)
         */
        @Override
        protected void addQualifier(IP4Changelist list, StyledString styled) {
            if (list instanceof IP4SubmittedChangelist) {
                String client = list.getClientName();
                if (client != null && Mode.USER == container.getMode()) {
                    styled.append(
                            MessageFormat
                                    .format(Messages.IntegrateTaskLabelProvider_ChangelistClient,
                                            client),
                            StyledString.QUALIFIER_STYLER);
                    return;
                }
            }
            super.addQualifier(list, styled);
        }

        /**
         * @see com.perforce.team.ui.StyledLabelProvider#dispose()
         */
        @Override
        public void dispose() {
            super.dispose();
            bold.dispose();
        }

    };

    private ILabelDecorator decorator = new ChangelistDecorator(true) {

        @Override
        public String decorateText(String text, Object element) {
            if (element instanceof JobTaskGroup) {
                StringBuilder decorated = new StringBuilder(text);
                IP4Job job = ((JobTaskGroup) element).getJob();
                if (job != null) {
                    decorated.append(" : "); //$NON-NLS-1$
                    decorated.append(job.getShortDescription());
                }
                return decorated.toString();
            } else {
                return super.decorateText(text, element);
            }
        }

        public String getName() {
        	return IntegrateTaskLabelProvider.class.getSimpleName();
        };
    };

    /**
     * Create integrate task label provider
     * 
     * @param container
     */
    public IntegrateTaskLabelProvider(IIntegrateTaskContainer container) {
        this.container = container;
        IBaseLabelProvider labelProvider = new DecoratingStyledCellLabelProvider(
                styleProvider, decorator, null);
        container.getViewer().setLabelProvider(labelProvider);
        Control control = container.getViewer().getControl();
        bold = P4UIUtils.generateBoldFont(control.getDisplay(),
                control.getFont());
    }

    /**
     * Get standard label provider
     * 
     * @return label provider
     */
    public ILabelProvider getLabelProvider() {
        return this.viewerLabelProvider;
    }

}
