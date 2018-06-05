/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.shelve;

import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4Listener;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.ui.changelists.ChangelistFileWidget;
import com.perforce.team.ui.changelists.ChangelistFormPage;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.progress.UIJob;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ShelvedFormPage extends ChangelistFormPage {

    private IP4Listener shelveListener = new IP4Listener() {

        public void resoureChanged(P4Event event) {
            final EventType type = event.getType();
            if (type == EventType.UPDATE_SHELVE
                    || type == EventType.DELETE_SHELVE) {
                IP4Changelist localList = changelist;
                for (IP4PendingChangelist list : event.getPending()) {
                    if (localList.getConnection().equals(list.getConnection())
                            && localList.getId() == list.getId()) {
                        UIJob update = new UIJob(
                                Messages.ShelvedFormPage_UpdatingShelvedChangelistEditor) {

                            @Override
                            public IStatus runInUIThread(
                                    IProgressMonitor monitor) {
                                if (type == EventType.DELETE_SHELVE) {
                                    getEditor().close(false);
                                } else if (type == EventType.UPDATE_SHELVE) {
                                    refresh();
                                }
                                return Status.OK_STATUS;
                            }
                        };
                        update.schedule();
                        break;
                    }
                }
            }
        }

		public String getName() {
			return ShelvedFormPage.this.getClass().getSimpleName();
		}
    };

    /**
     * @param editor
     * @param changelist
     */
    public ShelvedFormPage(FormEditor editor, IP4Changelist changelist) {
        super(editor, changelist);
    }

    /**
     * @see com.perforce.team.ui.changelists.ChangelistFormPage#createFormContent(org.eclipse.ui.forms.IManagedForm)
     */
    @Override
    protected void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);
        P4Workspace.getWorkspace().addListener(shelveListener);
    }

    /**
     * @see com.perforce.team.ui.changelists.ChangelistFormPage#dispose()
     */
    @Override
    public void dispose() {
        P4Workspace.getWorkspace().removeListener(shelveListener);
        super.dispose();
    }

    /**
     * @see com.perforce.team.ui.changelists.ChangelistFormPage#createFileViewer(org.eclipse.ui.forms.widgets.FormToolkit,
     *      org.eclipse.ui.forms.widgets.Section,
     *      org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected ChangelistFileWidget createFileViewer(final FormToolkit toolkit,
            final Section section, final Composite parent) {
        ChangelistFileWidget widget = new ShelvedChangelistFileWidget(true) {

            @Override
            protected TreeViewer createViewer(Composite parent) {
                Tree tree = toolkit.createTree(parent, SWT.BORDER | SWT.MULTI
                        | SWT.V_SCROLL | SWT.H_SCROLL);
                return new TreeViewer(tree);
            }

            @Override
            protected void createToolbar(Composite parent) {
                ToolBar toolbar = createSectionToolbar(toolkit, section);
                fillToolbar(toolbar);
            }

        };
        widget.createControl(parent);
        return widget;
    }

    /**
     * @see com.perforce.team.ui.changelists.ChangelistFormPage#createJobsSection(org.eclipse.swt.widgets.Composite,
     *      org.eclipse.ui.forms.widgets.FormToolkit)
     */
    @Override
    protected void createJobsSection(Composite parent, final FormToolkit toolkit) {
        // Do nothing
    }

    /**
     * @see com.perforce.team.ui.changelists.ChangelistFormPage#getUserLabelText()
     */
    @Override
    protected String getUserLabelText() {
        return Messages.ShelvedFormPage_ShelvedBy;
    }

	public String getName() {
		return getClass().getSimpleName();
	}

}
