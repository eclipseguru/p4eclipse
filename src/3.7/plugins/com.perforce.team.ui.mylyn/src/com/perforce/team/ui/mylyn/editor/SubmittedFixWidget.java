/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.editor;

import com.perforce.team.core.p4java.IP4Container;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.ui.diff.DiffSorter;
import com.perforce.team.ui.submitted.OpenEditorAction;
import com.perforce.team.ui.submitted.SubmittedFileContentProvider;

import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class SubmittedFixWidget extends ChangelistFixWidget {

    private ISchedulingRule rule = P4Runner.createRule();

    /**
     * Pending fix widget
     * 
     * @param enableEdit
     * @param id
     */
    public SubmittedFixWidget(boolean enableEdit, String id) {
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
        return new SubmittedFileContentProvider(viewer, true) {

            @Override
            protected ISchedulingRule generateRule(IP4Container container) {
                return SubmittedFixWidget.this.rule;
            }

            @Override
            public Object[] getElements(Object inputElement) {
                if (inputElement == lists) {
                    return lists.toArray();
                } else {
                    return super.getElements(inputElement);
                }
            }

        };
    }

    /**
     * @see com.perforce.team.ui.mylyn.editor.ChangelistFixWidget#configureViewer(org.eclipse.jface.viewers.TreeViewer)
     */
    @Override
    protected void configureViewer(TreeViewer viewer) {
        super.configureViewer(viewer);
        viewer.setSorter(new DiffSorter() {

            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
                if (e1 instanceof IP4SubmittedChangelist
                        && e2 instanceof IP4SubmittedChangelist) {
                    return ((IP4SubmittedChangelist) e2).getId()
                            - ((IP4SubmittedChangelist) e1).getId();
                }
                return super.compare(viewer, e1, e2);
            }

        });
    }

    /**
     * @see com.perforce.team.ui.mylyn.editor.ChangelistFixWidget#isValid(com.perforce.team.core.p4java.IP4Resource)
     */
    @Override
    protected boolean isValid(IP4Resource resource) {
        return resource instanceof IP4SubmittedChangelist;
    }

    /**
     * @see com.perforce.team.ui.mylyn.editor.ChangelistFixWidget#openEditor(org.eclipse.jface.viewers.ISelection)
     */
    @Override
    protected void openEditor(ISelection selection) {
        OpenEditorAction open = new OpenEditorAction();
        open.selectionChanged(null, viewer.getSelection());
        open.run(null);
    }

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

}
