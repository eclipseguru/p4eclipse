/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.submitted;

import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.ui.changelists.ChangelistFileWidget;
import com.perforce.team.ui.changelists.ChangelistFormPage;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class SubmittedFormPage extends ChangelistFormPage {

    /**
     * @param editor
     * @param changelist
     */
    public SubmittedFormPage(FormEditor editor, IP4Changelist changelist) {
        super(editor, changelist);
    }

    /**
     * @see com.perforce.team.ui.changelists.ChangelistFormPage#createFileViewer(org.eclipse.ui.forms.widgets.FormToolkit,
     *      org.eclipse.ui.forms.widgets.Section,
     *      org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected ChangelistFileWidget createFileViewer(final FormToolkit toolkit,
            final Section section, final Composite parent) {
        ChangelistFileWidget widget = new SubmittedChangelistFileWidget(true) {

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
     * @see com.perforce.team.ui.changelists.ChangelistFormPage#getDateLabelText()
     */
    @Override
    protected String getDateLabelText() {
        return Messages.SubmittedFormPage_DateSubmitted;
    }

    /**
     * @see com.perforce.team.ui.changelists.ChangelistFormPage#getUserLabelText()
     */
    @Override
    protected String getUserLabelText() {
        return Messages.SubmittedFormPage_SubmittedBy;
    }

	public String getName() {
		return getClass().getSimpleName();
	}

}
