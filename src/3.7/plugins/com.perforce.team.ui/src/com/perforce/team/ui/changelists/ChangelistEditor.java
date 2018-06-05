/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.changelists;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.forms.editor.SharedHeaderFormEditor;
import org.eclipse.ui.forms.widgets.Form;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4ShelvedChangelist;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.editor.PageFactoryRegistry;
import com.perforce.team.ui.p4java.actions.IntegrateAction;
import com.perforce.team.ui.shelve.ShelvedFormPage;
import com.perforce.team.ui.shelve.UnshelveAction;
import com.perforce.team.ui.submitted.SubmittedFormPage;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ChangelistEditor extends SharedHeaderFormEditor {

    /**
     * ID
     */
    public static final String ID = "com.perforce.team.ui.changelists.ChangelistEditor"; //$NON-NLS-1$

    private IP4Changelist changelist;

    private ChangelistFormPage changelistPage = null;

    /**
     * @see org.eclipse.ui.forms.editor.FormEditor#addPages()
     */
    @Override
    protected void addPages() {
        if (this.changelist instanceof IP4SubmittedChangelist) {
            this.changelistPage = new SubmittedFormPage(this, this.changelist);
        } else if (this.changelist instanceof IP4ShelvedChangelist) {
            this.changelistPage = new ShelvedFormPage(this, this.changelist);
        }
        if (this.changelistPage != null) {
            try {
                addPage(this.changelistPage);
            } catch (PartInitException e) {
                PerforceProviderPlugin.logError(e);
            }
            String name = getEditorInput().getName();
            if (name != null) {
                setPartName(name);
            }
            ImageDescriptor descriptor = getEditorInput().getImageDescriptor();
            if (descriptor != null) {
                Image editorImage = descriptor.createImage();
                setTitleImage(editorImage);
                P4UIUtils.registerDisposal(getContainer(), editorImage);
            }
        }
        PageFactoryRegistry.getRegistry().addPages(this, ID);
    }

    /**
     * @see org.eclipse.ui.forms.editor.FormEditor#init(org.eclipse.ui.IEditorSite,
     *      org.eclipse.ui.IEditorInput)
     */
    @Override
    public void init(IEditorSite site, IEditorInput input)
            throws PartInitException {
        if (input instanceof IChangelistEditorInput) {
            this.changelist = ((IChangelistEditorInput) input).getChangelist();
            if (this.changelist == null) {
                throw new PartInitException("Input changelist must not be null");
            }
            super.init(site, input);
        } else {
            throw new PartInitException(
                    "Input must be an instance of IChangelistEditorInput");
        }
    }

    /**
     * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void doSave(IProgressMonitor monitor) {

    }

    /**
     * @see org.eclipse.ui.part.EditorPart#doSaveAs()
     */
    @Override
    public void doSaveAs() {

    }

    /**
     * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
     */
    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    /**
     * @see org.eclipse.ui.forms.editor.SharedHeaderFormEditor#createHeaderContents(org.eclipse.ui.forms.IManagedForm)
     */
    @Override
    protected void createHeaderContents(IManagedForm headerForm) {
        String format = "{0} ({1})"; //$NON-NLS-1$
        Image image = null;
        if (this.changelist instanceof IP4SubmittedChangelist) {
            format = Messages.ChangelistEditor_SubmittedChangelist;
            image = PerforceUIPlugin.getDescriptor(
                    IPerforceUIConstants.IMG_CHG_SUBMITTED).createImage();
        } else if (this.changelist instanceof IP4ShelvedChangelist) {
            format = Messages.ChangelistEditor_ShelvedChangelist;
            image = PerforceUIPlugin.getDescriptor(
                    IPerforceUIConstants.IMG_SHELVE).createImage();
        }

        Form form = headerForm.getForm().getForm();

        Action refreshAction = new Action(
                Messages.ChangelistEditor_RefreshChangelist,
                PerforceUIPlugin
                        .getDescriptor(IPerforceUIConstants.IMG_REFRESH)) {

            @Override
            public void run() {
                if (changelistPage != null) {
                    changelistPage.refresh();
                }
            }

        };
        form.getToolBarManager().add(refreshAction);

        if (this.changelist instanceof IP4SubmittedChangelist) {
            Action integAction = new Action(
                    Messages.ChangelistEditor_IntegrateChangelist,
                    PerforceUIPlugin
                            .getDescriptor(IPerforceUIConstants.IMG_INTEGRATE)) {

                @Override
                public void run() {
                    IntegrateAction integ = new IntegrateAction();
                    integ.selectionChanged(null, new StructuredSelection(
                            changelist));
                    integ.run(null);
                }

            };
            form.getToolBarManager().add(integAction);
        } else if (this.changelist instanceof IP4ShelvedChangelist) {
            Action unshelveAction = new Action(
                    Messages.ChangelistEditor_Unshelve,
                    PerforceUIPlugin
                            .getDescriptor(IPerforceUIConstants.IMG_UNSHELVE)) {

                @Override
                public void run() {
                    UnshelveAction unshelve = new UnshelveAction();
                    unshelve.selectionChanged(null, new StructuredSelection(
                            changelist));
                    unshelve.run(null);
                }

            };
            form.getToolBarManager().add(unshelveAction);
        }

        form.getToolBarManager().update(true);
        if (image != null) {
            P4UIUtils.registerDisposal(form, image);
            form.setImage(image);
        }
        form.setText(MessageFormat.format(format, this.changelist.getId(),
                this.changelist.getConnection().getParameters().getPort()));
        getToolkit().decorateFormHeading(form);
    }

    /**
     * @see org.eclipse.ui.forms.editor.SharedHeaderFormEditor#setFocus()
     */
    @Override
    public void setFocus() {
        super.setFocus();
        IFormPage page = getActivePageInstance();
        if (page != null) {
            page.setFocus();
        }
    }

}
