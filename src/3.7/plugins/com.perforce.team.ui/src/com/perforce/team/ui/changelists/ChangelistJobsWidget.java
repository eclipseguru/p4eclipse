/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.changelists;

import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.jobs.JobsWidget;
import com.perforce.team.ui.p4java.actions.FixJobAction;
import com.perforce.team.ui.p4java.actions.UnfixJobAction;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ChangelistJobsWidget extends JobsWidget {

    private IP4Changelist changelist;

    /**
     * @param changelist
     * @param enableEdit
     */
    public ChangelistJobsWidget(IP4Changelist changelist, boolean enableEdit) {
        super(enableEdit);
        this.changelist = changelist;
    }

    private IP4Job[] getSelectedJobs() {
        List<IP4Job> jobs = new ArrayList<IP4Job>();
        IStructuredSelection selection = (IStructuredSelection) getViewer()
                .getSelection();
        for (Object element : selection.toArray()) {
            if (element instanceof IP4Job) {
                jobs.add((IP4Job) element);
            }
        }
        return jobs.toArray(new IP4Job[jobs.size()]);
    }

    /**
     * @see com.perforce.team.ui.jobs.JobsWidget#fillToolbar(org.eclipse.swt.widgets.ToolBar)
     */
    @Override
    protected void fillToolbar(ToolBar toolbar) {
        super.fillToolbar(toolbar);

        ToolItem addFixItem = new ToolItem(toolbar, SWT.PUSH);
        addFixItem.setToolTipText(Messages.ChangelistJobsWidget_AddJob);
        Image addImage = PerforceUIPlugin.getDescriptor(
                IPerforceUIConstants.IMG_ADD).createImage();
        P4UIUtils.registerDisposal(addFixItem, addImage);
        addFixItem.setImage(addImage);
        addFixItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                FixJobAction fix = new FixJobAction();
                fix.selectionChanged(null, new StructuredSelection(changelist));
                fix.run(null);
            }

        });

        ToolItem removeFixItem = new ToolItem(toolbar, SWT.PUSH);
        removeFixItem
                .setToolTipText(Messages.ChangelistJobsWidget_RemoveSelectedJobs);
        Image removeImage = PerforceUIPlugin.getDescriptor(
                IPerforceUIConstants.IMG_DELETE).createImage();
        P4UIUtils.registerDisposal(removeFixItem, removeImage);
        removeFixItem.setImage(removeImage);
        removeFixItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                IP4Job[] jobs = getSelectedJobs();
                if (jobs.length > 0) {
                    StringBuilder title = new StringBuilder(
                            Messages.ChangelistJobsWidget_ConfirmJobRemoval);
                    String message = MessageFormat
                            .format(Messages.ChangelistJobsWidget_RemoveJobsFromChangelist,
                                    jobs.length, changelist.getId());
                    if (P4ConnectionManager.getManager().openConfirm(
                            title.toString(), message.toString())) {
                        UnfixJobAction unfix = new UnfixJobAction();
                        unfix.unfix(changelist, jobs);
                    }
                }
            }

        });
    }
}
