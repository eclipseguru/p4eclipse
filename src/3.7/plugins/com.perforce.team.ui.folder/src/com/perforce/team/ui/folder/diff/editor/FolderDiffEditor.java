/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.editor;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.editor.PageFactoryRegistry;
import com.perforce.team.ui.folder.diff.editor.input.IDiffConfiguration;
import com.perforce.team.ui.folder.diff.editor.input.IFolderDiffInput;
import com.perforce.team.ui.folder.diff.model.IFileDiffContainerProvider;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.SharedHeaderFormEditor;
import org.eclipse.ui.forms.widgets.Form;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class FolderDiffEditor extends SharedHeaderFormEditor {

    /**
     * ID
     */
    public static final String ID = "com.perforce.team.ui.editors.folderdiff"; //$NON-NLS-1$

    private FolderDiffPage diffPage;
    private Image headerImage = null;
    private Image inputImage = null;

    /**
     * @see org.eclipse.ui.forms.editor.FormEditor#addPages()
     */
    @Override
    protected void addPages() {
        try {
            this.diffPage = new FolderDiffPage(this);
            this.addPage(this.diffPage);
        } catch (PartInitException e) {
            PerforceProviderPlugin.logError(e);
        }
        setPartName(getEditorInput().getName());
        ImageDescriptor desc = getEditorInput().getImageDescriptor();
        if (desc != null) {
            this.inputImage = desc.createImage();
            setTitleImage(inputImage);
        }
        PageFactoryRegistry.getRegistry().addPages(this, ID);

        IToolBarManager manager = getHeaderForm().getForm().getToolBarManager();
        this.diffPage.fillToolbar(manager);
        manager.update(true);
    }

    /**
     * @see org.eclipse.ui.part.MultiPageEditorPart#getAdapter(java.lang.Class)
     */
    @Override
    public Object getAdapter(Class adapter) {
        if (FolderDiffPage.class == adapter
                || IFileDiffContainerProvider.class == adapter) {
            return this.diffPage;
        }
        return super.getAdapter(adapter);
    }

    /**
     * @see org.eclipse.ui.forms.editor.FormEditor#init(org.eclipse.ui.IEditorSite,
     *      org.eclipse.ui.IEditorInput)
     */
    @Override
    public void init(IEditorSite site, IEditorInput input)
            throws PartInitException {
        if (input instanceof IFolderDiffInput) {
            super.init(site, input);
        } else {
            throw new PartInitException("Input must be an IFolderDiffInput"); //$NON-NLS-1$
        }
    }

    /**
     * @see org.eclipse.ui.forms.editor.SharedHeaderFormEditor#dispose()
     */
    @Override
    public void dispose() {
        super.dispose();
        if (this.inputImage != null) {
            inputImage.dispose();
        }
        if (headerImage != null) {
            headerImage.dispose();
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
        IFolderDiffInput input = (IFolderDiffInput) getEditorInput();
        IP4Connection connection = P4CoreUtils.convert(getEditorInput(),
                IP4Connection.class);
        IDiffConfiguration headerConfig = input.getHeaderConfiguration();
        String prefix = headerConfig.getLabel(headerConfig);
        Form form = headerForm.getForm().getForm();
        String message = MessageFormat.format(Messages.FolderDiffEditor_Header,
                prefix, connection.getParameters().getPort());
        form.setText(message);

        ImageDescriptor descriptor = headerConfig
                .getImageDescriptor(headerConfig);
        if (descriptor != null) {
            this.headerImage = descriptor.createImage();
            form.setImage(this.headerImage);
        }
        getToolkit().decorateFormHeading(form);
    }
}
