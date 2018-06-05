/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.editor;

import com.perforce.team.core.mylyn.IP4MylynConstants;
import com.perforce.team.ui.mylyn.IP4MylynUiConstants;
import com.perforce.team.ui.mylyn.P4MylynUiUtils;
import com.perforce.team.ui.mylyn.PerforceUiMylynPlugin;

import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.forms.editor.IFormPage;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class P4AdvancedEditorPageFactory extends AbstractTaskEditorPageFactory {

    /**
     * P4 advanced editor page factory
     */
    public P4AdvancedEditorPageFactory() {

    }

    /**
     * @see org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory#canCreatePageFor(org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput)
     */
    @Override
    public boolean canCreatePageFor(TaskEditorInput input) {
        return IP4MylynConstants.KIND.equals(input.getTaskRepository()
                .getConnectorKind())
                || TasksUiUtil.isOutgoingNewTask(input.getTask(),
                        IP4MylynConstants.KIND);
    }

    /**
     * @see org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory#getPageImage()
     */
    @Override
    public Image getPageImage() {
        return PerforceUiMylynPlugin.getImage(IP4MylynUiConstants.IMG_ADVANCED);
    }

    /**
     * @see org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory#getPageText()
     */
    @Override
    public String getPageText() {
        return Messages.JobFieldGroup_Other;
    }

    /**
     * @see org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory#createPage(org.eclipse.mylyn.tasks.ui.editors.TaskEditor)
     */
    @Override
    public IFormPage createPage(TaskEditor parentEditor) {
        TaskRepository repository = P4MylynUiUtils.getRepository(parentEditor);
        return new P4JobEditorPage(parentEditor, JobFieldGroup.ADVANCED_FIELDS,
                getPageText(), JobFieldGroup.loadPage(repository,
                        JobFieldGroup.ADVANCED_FIELDS), false, false);
    }

    /**
     * @see org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory#getPriority()
     */
    @Override
    public int getPriority() {
        return PRIORITY_ADDITIONS;
    }
}