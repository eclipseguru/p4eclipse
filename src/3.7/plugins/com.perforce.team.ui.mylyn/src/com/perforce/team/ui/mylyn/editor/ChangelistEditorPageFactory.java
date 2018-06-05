/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.editor;

import com.perforce.team.core.mylyn.IP4MylynConstants;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;

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
public class ChangelistEditorPageFactory extends AbstractTaskEditorPageFactory {

    private Image image = null;

    /**
     * @see org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory#canCreatePageFor(org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput)
     */
    @Override
    public boolean canCreatePageFor(TaskEditorInput input) {
        return IP4MylynConstants.KIND.equals(input.getTaskRepository()
                .getConnectorKind())
                && !TasksUiUtil.isOutgoingNewTask(input.getTask(),
                        IP4MylynConstants.KIND);
    }

    /**
     * @see org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory#createPage(org.eclipse.mylyn.tasks.ui.editors.TaskEditor)
     */
    @Override
    public IFormPage createPage(TaskEditor parentEditor) {
        return new FixFormPage(parentEditor);
    }

    /**
     * @see org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory#getPageImage()
     */
    @Override
    public Image getPageImage() {
        if (image == null || image.isDisposed()) {
            image = PerforceUIPlugin.getDescriptor(
                    IPerforceUIConstants.IMG_CHG_SUBMITTED).createImage();
        }
        return image;
    }

    /**
     * @see org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory#getPriority()
     */
    @Override
    public int getPriority() {
        return PRIORITY_ADDITIONS + 1;
    }

    /**
     * @see org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory#getPageText()
     */
    @Override
    public String getPageText() {
        return Messages.ChangelistEditorPageFactory_Fixes;
    }

}
