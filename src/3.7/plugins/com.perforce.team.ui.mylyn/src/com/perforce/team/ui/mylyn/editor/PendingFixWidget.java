/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.editor;

import com.perforce.team.core.p4java.IP4Container;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4ShelveFile;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.ui.PerforceContentProvider;
import com.perforce.team.ui.actions.OpenAction;
import com.perforce.team.ui.pending.PendingListener;
import com.perforce.team.ui.pending.PendingSorter;
import com.perforce.team.ui.shelve.OpenEditorAction;

import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class PendingFixWidget extends ChangelistFixWidget {

    private ISchedulingRule rule = P4Runner.createRule();

    /**
     * Pending fix widget
     * 
     * @param enableEdit
     * @param id
     */
    public PendingFixWidget(boolean enableEdit, String id) {
        super(enableEdit, id);
    }

    /**
     * Create content provider to use
     * 
     * @param viewer
     * @return - content provider
     */
    @Override
    protected ITreeContentProvider createContentProvider(TreeViewer viewer) {
        return new PerforceContentProvider(viewer, true) {

            @Override
            protected ISchedulingRule generateRule(IP4Container container) {
                return PendingFixWidget.this.rule;
            }

            @Override
            public Object[] getElements(Object inputElement) {
                if (inputElement == lists) {
                    return lists.toArray();
                } else {
                    return super.getElements(inputElement);
                }
            }

            @Override
            protected IP4Resource[] getMembers(IP4Container container) {
                if (container instanceof IP4PendingChangelist) {
                    return ((IP4PendingChangelist) container).getAllMembers();
                }
                return super.getMembers(container);
            }

        };
    }

    /**
     * @see com.perforce.team.ui.mylyn.editor.ChangelistFixWidget#configureViewer(org.eclipse.jface.viewers.TreeViewer)
     */
    @Override
    protected void configureViewer(TreeViewer viewer) {
        super.configureViewer(viewer);
        viewer.setSorter(new PendingSorter());
        new PendingListener(viewer) {

            @Override
            protected void addLists(IP4Resource[] resources) {
                // Don't allow changelists to be added
            }

            @Override
            protected void handleExpand(IP4Resource resource) {
                // Don't auto-expand any resources
            }

            @Override
            public String getName() {
            	return PendingFixWidget.class.getSimpleName();
            }
        };
    }

    /**
     * @see com.perforce.team.ui.mylyn.editor.ChangelistFixWidget#isValid(com.perforce.team.core.p4java.IP4Resource)
     */
    @Override
    protected boolean isValid(IP4Resource resource) {
        return resource instanceof IP4PendingChangelist;
    }

    /**
     * @see com.perforce.team.ui.mylyn.editor.ChangelistFixWidget#openEditor(org.eclipse.jface.viewers.ISelection)
     */
    @Override
    protected void openEditor(ISelection selection) {
        IStructuredSelection structured = (IStructuredSelection) selection;

        OpenEditorAction open = new OpenEditorAction();

        for (Object element : structured.toArray()) {
            if (element instanceof IP4File) {
                OpenAction.openFile((IP4File) element);
            } else if (element instanceof IP4ShelveFile) {
                open.selectionChanged(null, new StructuredSelection(element));
                open.run(null);
            }
        }

    }

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}
}
