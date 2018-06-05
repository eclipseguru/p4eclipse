/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.submitted;

import com.perforce.team.core.p4java.IP4SubmittedFile;
import com.perforce.team.ui.changelists.ChangelistFileWidget;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class SubmittedChangelistFileWidget extends ChangelistFileWidget {

    /**
     * DISPLAY_TYPE
     */
    public static final String DISPLAY_TYPE = "com.perforce.team.ui.submitted.DISPLAY_TYPE"; //$NON-NLS-1$

    // Configuration options
    private boolean enableEdit;

    /**
     * Submitted changelist file widget
     * 
     * @param enableEdit
     */
    public SubmittedChangelistFileWidget(boolean enableEdit) {
        this.enableEdit = enableEdit;
    }

    /**
     * @see com.perforce.team.ui.changelists.ChangelistFileWidget#configureViewer(org.eclipse.jface.viewers.TreeViewer)
     */
    @Override
    protected void configureViewer(TreeViewer viewer) {
        super.configureViewer(viewer);

        if (this.enableEdit) {
            viewer.addDoubleClickListener(new IDoubleClickListener() {

                public void doubleClick(DoubleClickEvent event) {
                    IStructuredSelection select = (IStructuredSelection) getViewer()
                            .getSelection();
                    if (select.size() == 1
                            && select.getFirstElement() instanceof IP4SubmittedFile) {
                        OpenEditorAction open = new OpenEditorAction();
                        open.selectionChanged(null, select);
                        open.run(null);
                    }
                }
            });
        }
    }

    /**
     * @see com.perforce.team.ui.changelists.ChangelistFileWidget#createContentProvider(org.eclipse.jface.viewers.TreeViewer)
     */
    @Override
    protected ITreeContentProvider createContentProvider(TreeViewer viewer) {
        return new SubmittedFileContentProvider(viewer, true, this);
    }

    /**
     * @see com.perforce.team.ui.changelists.ChangelistFileWidget#getTypePreferenceKey()
     */
    @Override
    protected String getTypePreferenceKey() {
        return DISPLAY_TYPE;
    }

}
