/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.dialogs;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

import com.perforce.p4java.core.IStreamSummary;
import com.perforce.p4java.option.Options;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4FileIntegration;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.ui.IErrorProvider;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.SWTUtils;
import com.perforce.team.ui.changelists.PendingCombo;
import com.perforce.team.ui.dialogs.P4StatusDialog;
import com.perforce.team.ui.preferences.IPreferenceConstants;

/**
 * Base class for Stream copy/merge dialog.
 * 
 * @author ali
 */
public abstract class IntegrateToStreamDialog extends P4StatusDialog{
    
    public interface ISourceTargetWidget<T>{
        T getSourcePath();
        T getTargetPath();
        IStatus validate();
        Composite getControl();
        boolean isShowAllSource();
        void setEnabled(boolean enabled);
    }
    public interface IRevisionRangeWidget{
        String getStart();
        String getEnd();
        IStatus validate();
        Composite getControl();
    }
    public interface IDepotFileChooser{
        String getDepotPath();
        Control getControl();
        IStatus validate();
    }
    public interface IProgressProvider{
        void startProgress(String title);
        void stopProgress();
    }
    public interface ILongtimeTask{
        Runnable getNonUIJob();
        Runnable getUIJob();
    }
    
    abstract protected IRevisionRangeWidget createRevisionRangeWidget(Composite parent);
    abstract protected List<IStreamSummary> getPreferredStreams(IP4Resource resource);
    abstract protected SourceTargetStreamWidget createSourceTargetWidget(Composite parent, IP4Resource resource);
    abstract protected IP4Resource[] doIntegrate(IP4Connection connection, P4FileIntegration integration, String description, Options options);
    abstract protected Options updatePreviewOptions();
    abstract protected Options updateNonPreviewOptions();
    abstract protected String getOkButtonText();

    protected Point getPreferredSize() {
        return new Point(700,400);
    }

    protected IDepotFileChooser createFolderWidget(Composite parent,
            IP4Resource resource){
        return null;
    }
    
    @Override
    public void create() {
        super.create();
        getShell().setSize(getPreferredSize());
        validate(); // show error message
        postInit();
    }
    
    private void postInit() {
        startProgress(Messages.IntegrateToStreamDialog_InitializeDialog);
        P4Runner.schedule(new P4Runnable() {

            @Override
            public String getTitle() {
                return Messages.IntegrateToStreamDialog_InitializeDialog;
            }

            @Override
            public void run(IProgressMonitor monitor) {
                pathWidget.getNonUIJob().run();
                PerforceUIPlugin.syncExec(new Runnable() {

                    public void run() {
                        if (!displayArea.isDisposed()) {
                            pathWidget.getUIJob().run();
                            stopProgress();
                        }
                    }
                });
            }

        });
    }

    /**
     * TRY_AUTO_RESOLVE
     */
    public static final String TRY_AUTO_RESOLVE = IPreferenceConstants.PREFIX
            + "TRY_AUTO_RESOLVE"; //$NON-NLS-1$

    private Composite displayArea;

    private PendingCombo changelistCombo;
    private Label progressLabel;
    private ProgressBar progressBar;

    private Button preview;
    private Button integrate;

    private IP4Resource resource = null;

    protected SourceTargetStreamWidget pathWidget;
    protected IRevisionRangeWidget rangeWidget;

    protected IDepotFileChooser folderWidget;
    
    // model
    private String changeListDescription;
    private Options options; // either CopyFileOptions or MergeFileOptions
    private P4FileIntegration fileIntegration;
    
    public String getChangeListDescription() {
        return changeListDescription;
    }
    
    public Options getOptions() {
        return options;
    }
    
    public P4FileIntegration getFileIntegration() {
        return fileIntegration;
    }
    
    /**
     * Creates an integrate dialog opened against a specified connection with
     * initial selected resource.
     * 
     * @param parent
     * @param connection
     * @param title dialog title
     */
    public IntegrateToStreamDialog(Shell parent, IP4Resource resource,
            String title) {
        super(parent, title);
        setStatusLineAboveButtons(true);
        setModalResizeStyle();
        this.resource = resource;
    }
    
    /**
     * 
     * @see org.eclipse.jface.dialogs.StatusDialog#updateButtonsEnableState(org.eclipse.core.runtime.IStatus)
     */
    @Override
    protected void updateButtonsEnableState(IStatus status) {
        if (integrate != null && !integrate.isDisposed()) {
            integrate.setEnabled(!status.matches(IStatus.ERROR));
            preview.setEnabled(!status.matches(IStatus.ERROR));
        }
    }

    private void validate() {

        if(changelistCombo.getErrorMessage()!=null){
            return;
        }
            
        IStatus status=pathWidget.validate();
        if(!status.isOK()){
            setErrorMessage(status.getMessage());
            return;
        }

        if(folderWidget!=null){
            status=folderWidget.validate();
            if(!status.isOK()){
                setErrorMessage(status.getMessage());
                return;
            }
        }
        
        status=rangeWidget.validate();
        if(!status.isOK()){
            setErrorMessage(status.getMessage());
            return;
        }
        
        setErrorMessage(null);
    }
    
    @Override
    public void setErrorMessage(String message, IErrorProvider provider) {
        if(provider==changelistCombo){
            if(message==null){
                validate();
                return;
            }
        }
        super.setErrorMessage(message, provider);
    }
    
    private void createChangelistArea(Composite parent) {
        changelistCombo = new PendingCombo(this.resource);
        changelistCombo.createControl(parent,
                IP4Connection.INTEGRATE_DEFAULT_DESCRIPTION);
        changelistCombo.setErrorDisplay(this);
        
        progressLabel = new Label(displayArea, SWT.LEFT);
        progressLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                false));

        progressBar = new ProgressBar(displayArea, SWT.INDETERMINATE
                | SWT.SMOOTH | SWT.HORIZONTAL);
        progressBar
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        progressBar.setEnabled(false);
        progressBar.setVisible(false);
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite c = (Composite) super.createDialogArea(parent);

        displayArea = new Composite(c, SWT.NONE);
        GridLayout daLayout = new GridLayout(1, true);
        displayArea.setLayout(daLayout);
        displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        pathWidget=createSourceTargetWidget(displayArea, resource);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).applyTo(pathWidget.getControl());
        
        folderWidget=createFolderWidget(displayArea, resource);
        if(folderWidget!=null){
            GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).applyTo(folderWidget.getControl());
        }
        
        rangeWidget=createRevisionRangeWidget(displayArea);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).applyTo(rangeWidget.getControl());

        createChangelistArea(displayArea);

        addListeners();

        return c;
    }

    private void addListeners() {
        SWTUtils.addContentListener(new Control[]{pathWidget.getControl(),rangeWidget.getControl()}, new Runnable() {
            public void run() {
                validate();
            }
        });
        if(folderWidget!=null){
            SWTUtils.addContentListener(new Control[]{folderWidget.getControl()}, new Runnable() {
                public void run() {
                    validate();
                }
            });
        }
        
        // changelistCombo is not necessary, since it registered itself already.
    }

    private void updateHistory() {}

    /**
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        updateModel();
        super.okPressed();
    }

    private void updateModel() {
        changeListDescription = updateChangeListDescription();
        options = updateNonPreviewOptions();
        fileIntegration = updateIntegration();
    }

    public void startProgress(String title) {
        progressBar.setEnabled(true);
        progressBar.setVisible(true);
        progressLabel.setText(title);
        pathWidget.setEnabled(false);
    }

    public void stopProgress() {
        progressBar.setEnabled(false);
        progressBar.setVisible(false);
        progressLabel.setText(""); //$NON-NLS-1$
        pathWidget.setEnabled(true);
        validate();
    }

    private void runPreview() {
        final IP4Connection connection = resource.getConnection();
        
        IStatus status=validateTaskStreamOp();
        if(!status.isOK()){
        	MessageDialog.openError(null, com.perforce.team.ui.Messages.P4TeamUtils_Error, status.getMessage());
            return;
        }

        startProgress(Messages.IntegrateDialog_GeneratingIntegPreview);
        preview.setEnabled(false);
        integrate.setEnabled(false);

        P4Runner.schedule(new P4Runnable() {

            @Override
            public String getTitle() {
                return Messages.IntegrateDialog_GeneratingIntegPreview;
            }

            @Override
            public void run(IProgressMonitor monitor) {
                updatePreviewModel();
                final IP4Resource[] previewed = doIntegrate(connection, fileIntegration,changeListDescription,options);
                PerforceUIPlugin.syncExec(new Runnable() {

                    public void run() {
                        if (!displayArea.isDisposed()) {
                            stopProgress();
                            preview.setEnabled(true);
                            integrate.setEnabled(true);
                            if(previewed!=null && previewed.length>0){
	                            new IntegrationPreviewDialog(getShell(), previewed)
	                                    .open();
                            }
                        }
                    }
                });
            }

        });

    }

    protected IStatus validateTaskStreamOp() {
		return ValidationStatus.ok();
	}
    
	private void updatePreviewModel() {
        changeListDescription = updateChangeListDescription();
        options = updatePreviewOptions();
        fileIntegration = updateIntegration();
    }
    /**
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        preview = createButton(parent, IDialogConstants.DETAILS_ID,
                Messages.IntegrateDialog_Preview, false);
        preview.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateHistory();
                runPreview();
            }

        });
        integrate = createButton(parent, IDialogConstants.OK_ID,
                getOkButtonText(), true);
        createButton(parent, IDialogConstants.CANCEL_ID,
                Messages.IntegrateDialog_Cancel, false);
    }

    /**
     * Get current file integration info
     * 
     * @return - file integration info
     */
    public P4FileIntegration updateIntegration() {
        P4FileIntegration integration = new P4FileIntegration();
        integration.setStart(rangeWidget.getStart());
        integration.setEnd(rangeWidget.getEnd());
        if(folderWidget!=null){
            String path = folderWidget.getDepotPath().trim();
            if(!StringUtils.isEmpty(path))
                integration.setTarget(path);
        }
        return integration;
    }

    /**
     * Get the pending changelist id
     * 
     * @return - pending changelist id
     */
    protected int getChangelist() {
        return this.changelistCombo.getSelected();
    }

    /**
     * Get the changelist description
     * 
     * @return - description
     */
    private String updateChangeListDescription() {
        return this.changelistCombo.getDescription();
    }

    protected IP4Connection getConnection(){
        return resource.getConnection();
    }
}
