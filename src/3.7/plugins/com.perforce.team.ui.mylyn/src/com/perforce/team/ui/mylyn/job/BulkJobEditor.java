/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.job;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.editor.PageFactoryRegistry;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.SharedHeaderFormEditor;
import org.eclipse.ui.forms.widgets.Form;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BulkJobEditor extends SharedHeaderFormEditor {

    /**
     * ID
     */
    public static final String ID = "com.perforce.team.ui.mylyn.job.BulkJobEditor"; //$NON-NLS-1$

    private IP4Connection connection;
    private IJobProxy[] jobs;

    private BulkJobPage jobPage = null;

    /**
     * @see org.eclipse.ui.forms.editor.FormEditor#addPages()
     */
    @Override
    protected void addPages() {
        try {
            jobPage = new BulkJobPage(this, this.connection, this.jobs);
            addPage(jobPage);
            IToolBarManager manager = getHeaderForm().getForm()
                    .getToolBarManager();
            jobPage.fillHeaderToolbar(manager);
            manager.update(true);
        } catch (PartInitException e) {
            PerforceProviderPlugin.logError(e);
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
        if (input instanceof BulkJobInput) {
            BulkJobInput jobInput = (BulkJobInput) input;
            this.jobs = jobInput.getJobs();
            this.connection = jobInput.getConnection();
            super.init(site, input);
        } else {
            throw new PartInitException(
                    Messages.BulkJobEditor_InputMustBeBulkJobInput);
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
        Form form = headerForm.getForm().getForm();
        form.setText(MessageFormat.format(Messages.BulkJobEditor_EditJobsOn,
                this.connection.getParameters().getPort()));
        getToolkit().decorateFormHeading(form);
    }
}
