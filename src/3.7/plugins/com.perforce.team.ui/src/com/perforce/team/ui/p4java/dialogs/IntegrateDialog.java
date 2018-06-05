/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.dialogs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import com.perforce.p4java.server.IServer;
import com.perforce.p4java.server.IServerInfo;
import com.perforce.team.core.IP4ServerConstants;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Branch;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Resource.Type;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.core.p4java.P4FileIntegration;
import com.perforce.team.core.p4java.P4IntegrationOptions;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.branches.SelectBranchDialog;
import com.perforce.team.ui.changelists.PendingCombo;
import com.perforce.team.ui.dialogs.P4StatusDialog;
import com.perforce.team.ui.preferences.IPreferenceConstants;
import com.perforce.team.ui.resource.ResourceBrowserDialog;
import com.perforce.team.ui.views.SessionManager;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class IntegrateDialog extends P4StatusDialog {

    /**
     * TRY_AUTO_RESOLVE
     */
    public static final String TRY_AUTO_RESOLVE = IPreferenceConstants.PREFIX
            + "TRY_AUTO_RESOLVE"; //$NON-NLS-1$

    private Composite displayArea;
    private TabFolder specTabs;

    private TabItem fileTab;
    private Combo sourceText;
    private Combo targetText;

    private Button attemptSafeResolveButton;

    private TabItem branchTab;
    private Combo nameText;
    private Button limitButton;
    private Text limitText;
    private Button sourceButton;
    private Button targetButton;

    private Group limitArea;
    private Button startButton;
    private Button endButton;
    private Combo startCombo;
    private Combo endCombo;
    private Text startText;
    private Text endText;
    private Label startHelp;
    private Label endHelp;

    private PendingCombo changelistCombo;
    private Label progressLabel;
    private ProgressBar progressBar;

    private Button preview;
    private Button integrate;

    private boolean fileIntegration = true;
    private IP4Resource resource = null;
    private String sourcePath = null;
    private String targetPath = null;
    private String startOption = null;
    private String branchName = null;
    private String endOption = null;

    private ModifyListener validateListener = new ModifyListener() {

        public void modifyText(ModifyEvent e) {
            validate();
        }
    };

    private ModifyListener pathListener = new ModifyListener() {

        public void modifyText(ModifyEvent e) {
            updatePaths();
        }
    };

    private IP4SubmittedChangelist defaultLimit = null;
    private P4IntegrationOptions defaultOptions;
    private P4IntegrationOptions integrationOptions;

    private IntegOptionWidget optionWidget=IntegOptionWidget.INTEG3; // default integ 3
    
    /**
     * Creates an integrate dialog opened against a specified connection with
     * initial source and target path values
     * 
     * @param parent
     * @param connection
     * @param sourcePath
     * @param targetPath
     */
    public IntegrateDialog(Shell parent, IP4Connection connection,
            String sourcePath, String targetPath) {
        super(parent, Messages.IntegrateDialog_Integrate);
        setStatusLineAboveButtons(true);
        setModalResizeStyle();
        this.resource = connection;
        this.sourcePath = sourcePath;
        this.targetPath = targetPath;
        
        initOptionWidget(resource);
    }

    /**
     * Set default enabled options
     * 
     * @param options
     */
    public void setDefaultOptions(P4IntegrationOptions options) {
        this.defaultOptions = options;
    }

    public P4IntegrationOptions getDefaultOptions() {
        return this.defaultOptions;
    }

    /**
     * Set default limiter on integration
     * 
     * @param changelist
     */
    public void setDefaultLimit(IP4SubmittedChangelist changelist) {
        this.defaultLimit = changelist;
    }

    /**
     * Creates an integration dialog
     * 
     * @param parent
     * @param resource
     */
    public IntegrateDialog(Shell parent, IP4Resource resource) {
        super(parent, Messages.IntegrateDialog_Integrate);
        setStatusLineAboveButtons(true);
        setModalResizeStyle();
        this.resource = resource;
        if (this.resource instanceof IP4Branch) {
            this.branchName = this.resource.getName();
        } else {
            this.sourcePath = this.resource.getActionPath();
            this.targetPath = this.resource.getActionPath();
        }
        initOptionWidget(resource);
    }

    private String findFirstError() {
        TabItem[] tabs = specTabs.getSelection();
        if (tabs.length == 1) {
            if (tabs[0] == fileTab) {
                String source = sourceText.getText().trim();
                if (source.length() == 0) {
                    return Messages.IntegrateDialog_MustEnterSourcePath;
                }
                String target = targetText.getText().trim();
                if (target.length() == 0) {
                    return Messages.IntegrateDialog_MustEnterTargetPath;
                }
            } else if (tabs[0] == branchTab) {
                String name = nameText.getText().trim();
                if (name.length() == 0) {
                    return Messages.IntegrateDialog_MustEnterBranchName;
                }
            }
        }
        return null;
    }

    /**
     * 
     * @see org.eclipse.jface.dialogs.StatusDialog#updateButtonsEnableState(org.eclipse.core.runtime.IStatus)
     */
    @Override
    protected void updateButtonsEnableState(IStatus status) {
        if (integrate != null && !integrate.isDisposed()) {
            integrate.setEnabled(!status.matches(IStatus.ERROR));
        }
    }

    private void validate() {
        setErrorMessage(findFirstError(), null);
    }

    private void addComboListener(final Combo combo, final Label help) {
        combo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                String text = combo.getText();
                if (text.equals(Messages.IntegrateDialog_Revision)) {
                    help.setText(Messages.IntegrateDialog_EnterRevisionNumber);
                } else if (text.equals(Messages.IntegrateDialog_Changelist)) {
                    help.setText(Messages.IntegrateDialog_EnterChangelistNumber);
                } else if (text.equals(Messages.IntegrateDialog_Label)) {
                    help.setText(Messages.IntegrateDialog_EnterLabel);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }

        });
    }

    private String getPrefix(int index) {
        String prefix = null;
        switch (index) {
        case 0:
            prefix = "#"; //$NON-NLS-1$
            break;
        case 1:
        case 2:
            prefix = "@"; //$NON-NLS-1$
            break;
        default:
            prefix = ""; //$NON-NLS-1$
            break;
        }
        return prefix;
    }

    private void createBrowseForFileButton(Composite parent, final Object update) {
        final Button browseSource = new Button(parent, SWT.PUSH);
        browseSource.setText(Messages.IntegrateDialog_Browse);
        browseSource.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
            	IP4Connection conn = resource.getConnection();
            	// save showClientOnly
            	boolean showClientOnly=conn.showClientOnly();

            	try{
            		// set the showClientOnly to false, so that we can show all resources.
            		conn.setShowClientOnly(false);

            		ResourceBrowserDialog dialog = new ResourceBrowserDialog(
	                        browseSource.getShell(), conn
	                                .members());
	                if (dialog.open() == ResourceBrowserDialog.OK) {
	                    IP4Resource resource = dialog.getSelectedResource();
	                    if (resource != null) {
	                        String actionPath = resource.getActionPath(Type.REMOTE);
	                        if (actionPath != null) {
	                            if (update instanceof Combo) {
	                                ((Combo) update).setText(actionPath);
	                            } else if (update instanceof Text) {
	                                ((Text) update).setText(actionPath);
	                            }
	                        }
	                    }
	                }
            	}finally{
	                // restore the showClientOnly
	                conn.setShowClientOnly(showClientOnly);
            	}
            }

        });
    }

    private void createFileArea(final TabFolder folder) {
        fileTab = new TabItem(folder, SWT.NONE);
        fileTab.setText(Messages.IntegrateDialog_FileSpec);
        Composite filePathArea = new Composite(folder, SWT.NONE);
        fileTab.setControl(filePathArea);
        GridLayout fpaLayout = new GridLayout(3, false);
        filePathArea.setLayout(fpaLayout);
        filePathArea
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        Label sourceLabel = new Label(filePathArea, SWT.LEFT);
        sourceLabel.setText(Messages.IntegrateDialog_SourceLabel);

        sourceText = new Combo(filePathArea, SWT.DROP_DOWN);
        SessionManager.loadComboHistory(sourceText,
                IPreferenceConstants.SOURCE_FILE_HISTORY);
        sourceText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        if (this.sourcePath != null) {
            sourceText.setText(this.sourcePath);
        }
        sourceText.addModifyListener(pathListener);
        sourceText.addModifyListener(validateListener);

        createBrowseForFileButton(filePathArea, sourceText);

        Label targetLabel = new Label(filePathArea, SWT.LEFT);
        targetLabel.setText(Messages.IntegrateDialog_TargetLabel);

        targetText = new Combo(filePathArea, SWT.SINGLE | SWT.BORDER);
        SessionManager.loadComboHistory(targetText,
                IPreferenceConstants.TARGET_FILE_HISTORY);
        targetText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        if (this.targetPath != null) {
            targetText.setText(targetPath);
        }
        targetText.addModifyListener(pathListener);
        targetText.addModifyListener(validateListener);
        createBrowseForFileButton(filePathArea, targetText);
    }

    private void createBranchArea(final TabFolder folder) {
        branchTab = new TabItem(folder, SWT.NONE);
        branchTab.setText(Messages.IntegrateDialog_BranchSpec);
        Composite branchArea = new Composite(folder, SWT.NONE);
        branchTab.setControl(branchArea);
        GridLayout baLayout = new GridLayout(3, false);
        branchArea.setLayout(baLayout);
        branchArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Label nameLabel = new Label(branchArea, SWT.LEFT);
        nameLabel.setText(Messages.IntegrateDialog_BranchName);
        nameText = new Combo(branchArea, SWT.DROP_DOWN);
        SessionManager.loadComboHistory(nameText,
                IPreferenceConstants.BRANCH_HISTORY);
        nameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        if (this.branchName != null) {
            nameText.setText(branchName);
        }
        nameText.addModifyListener(pathListener);
        nameText.addModifyListener(validateListener);

        final Button browseButton = new Button(branchArea, SWT.PUSH);
        browseButton.setText(Messages.IntegrateDialog_Browse);
        browseButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                SelectBranchDialog dialog = new SelectBranchDialog(browseButton
                        .getShell(), resource.getConnection());
                if (SelectBranchDialog.OK == dialog.open()) {
                    IP4Branch selected = dialog.getSelected();
                    if (selected != null && selected.getName() != null) {
                        nameText.setText(selected.getName());
                        validate();
                    }
                }
            }

        });

        Composite limitArea = new Composite(branchArea, SWT.NONE);
        GridData laData = new GridData(SWT.FILL, SWT.FILL, true, false);
        laData.horizontalSpan = 3;
        limitArea.setLayoutData(laData);
        GridLayout laLayout = new GridLayout(4, false);
        limitArea.setLayout(laLayout);

        limitButton = new Button(limitArea, SWT.CHECK);
        limitButton.setText(Messages.IntegrateDialog_LimitIntegTo);
        limitButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean enabled = limitButton.getSelection();
                limitText.setEnabled(enabled);
                targetButton.setEnabled(enabled);
                sourceButton.setEnabled(enabled);
                optionWidget.enableReverseButton(!enabled);
            }

        });
        limitText = new Text(limitArea, SWT.SINGLE | SWT.BORDER);
        limitText.setEnabled(false);
        GridData ltData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        ltData.horizontalSpan = 2;
        limitText.setLayoutData(ltData);

        createBrowseForFileButton(limitArea, limitText);

        Label usePathLabel = new Label(limitArea, SWT.LEFT);
        usePathLabel.setLayoutData(new GridData(SWT.END, SWT.CENTER, false,
                false));
        usePathLabel.setText(Messages.IntegrateDialog_UsePathAs);

        SelectionListener limitListener = new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updatePaths();
            }

        };

        limitText.addModifyListener(pathListener);
        limitButton.addSelectionListener(limitListener);

        sourceButton = new Button(limitArea, SWT.RADIO);
        sourceButton.setText(Messages.IntegrateDialog_Source);
        sourceButton.setEnabled(false);
        sourceButton.setSelection(true);
        sourceButton.addSelectionListener(limitListener);

        targetButton = new Button(limitArea, SWT.RADIO);
        targetButton.setText(Messages.IntegrateDialog_Target);
        targetButton.setEnabled(false);
        targetButton.addSelectionListener(limitListener);
    }

    private void createLimitArea(Composite parent) {
        limitArea = new Group(parent, SWT.NONE);
        limitArea.setText(Messages.IntegrateDialog_LimitRevRange);
        limitArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        GridLayout laLayout = new GridLayout(4, false);
        limitArea.setLayout(laLayout);

        startButton = new Button(limitArea, SWT.CHECK);
        startButton.setText(Messages.IntegrateDialog_Start);
        startButton.setEnabled(false);
        startButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean enabled = startButton.getSelection();
                startCombo.setEnabled(enabled);
                startText.setEnabled(enabled);
                startHelp.setVisible(enabled);
                if (!enabled) {
                    startOption = null;
                }
            }

        });

        startCombo = new Combo(limitArea, SWT.DROP_DOWN | SWT.READ_ONLY);
        startCombo.add(Messages.IntegrateDialog_Revision);
        startCombo.add(Messages.IntegrateDialog_Changelist);
        startCombo.add(Messages.IntegrateDialog_Label);
        startCombo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                String prefix = getPrefix(startCombo.getSelectionIndex());
                startOption = prefix + startText.getText();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }

        });
        startCombo.setEnabled(false);

        startText = new Text(limitArea, SWT.SINGLE | SWT.BORDER);
        startText.setEnabled(false);
        startText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                String prefix = getPrefix(startCombo.getSelectionIndex());
                startOption = prefix + startText.getText();
            }

        });
        startHelp = new Label(limitArea, SWT.LEFT);
        startHelp
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        addComboListener(startCombo, startHelp);

        endButton = new Button(limitArea, SWT.CHECK);
        endButton.setText(Messages.IntegrateDialog_End);
        endButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean enabled = endButton.getSelection();
                endCombo.setEnabled(enabled);
                endText.setEnabled(enabled);
                startButton.setEnabled(enabled);
                startCombo.setEnabled(enabled);
                startText.setEnabled(enabled);
                if (!enabled) {
                    startButton.setSelection(false);
                    endOption = null;
                    startOption = null;
                }
            }

        });

        endCombo = new Combo(limitArea, SWT.DROP_DOWN | SWT.READ_ONLY);
        endCombo.add(Messages.IntegrateDialog_Revision);
        endCombo.add(Messages.IntegrateDialog_Changelist);
        endCombo.add(Messages.IntegrateDialog_Label);
        endCombo.setEnabled(false);
        endCombo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                String prefix = getPrefix(endCombo.getSelectionIndex());
                endOption = prefix + endText.getText();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }

        });

        endText = new Text(limitArea, SWT.SINGLE | SWT.BORDER);
        endText.setEnabled(false);
        endText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                String prefix = getPrefix(endCombo.getSelectionIndex());
                endOption = prefix + endText.getText();
            }

        });

        endHelp = new Label(limitArea, SWT.LEFT);
        endHelp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        addComboListener(endCombo, endHelp);

        if (this.defaultLimit != null) {
            startCombo.select(1);
            endCombo.select(1);
            startHelp.setText(Messages.IntegrateDialog_EnterChangelistNumber);
            endHelp.setText(Messages.IntegrateDialog_EnterChangelistNumber);
            startButton.setEnabled(true);
            startButton.setSelection(true);
            endButton.setSelection(true);
            startCombo.setEnabled(true);
            endCombo.setEnabled(true);
            startText.setEnabled(true);
            endText.setEnabled(true);
            int id = this.defaultLimit.getId();
            startText.setText(Integer.toString(id));
            endText.setText(Integer.toString(id));
        } else {
            startCombo.select(0);
            endCombo.select(0);
            startHelp.setText(Messages.IntegrateDialog_EnterRevisionNumber);
            endHelp.setText(Messages.IntegrateDialog_EnterRevisionNumber);
        }
    }

    private void createOptionsArea(Composite parent) {
    	optionWidget.createOptionsArea(parent);

        specTabs.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                TabItem[] tabs = specTabs.getSelection();
                optionWidget.enableReverseButton(tabs.length == 1
                        && tabs[0] == branchTab);
            }

        });

        attemptSafeResolveButton = new Button(parent, SWT.CHECK);
        attemptSafeResolveButton
                .setText(Messages.IntegrateDialog_PerformSafeAutoresolve);
        attemptSafeResolveButton.setSelection(PerforceUIPlugin.getPlugin()
                .getPreferenceStore().getBoolean(TRY_AUTO_RESOLVE));

    	optionWidget.initControl(defaultOptions);
    }

    private void createChangelistArea(Composite parent) {
        changelistCombo = new PendingCombo(this.resource);
        changelistCombo.createControl(parent,
                IP4Connection.INTEGRATE_DEFAULT_DESCRIPTION);

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

        specTabs = new TabFolder(displayArea, SWT.TOP);
        specTabs.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updatePaths();
                validate();
            }

        });
        specTabs.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        if (resource instanceof IP4SubmittedChangelist) {
            setDefaultLimit((IP4SubmittedChangelist) resource);
        }

        createFileArea(specTabs);
        createBranchArea(specTabs);
        createLimitArea(displayArea);

        createOptionsArea(displayArea);
        createChangelistArea(displayArea);

        if (resource instanceof IP4Branch) {
            specTabs.setSelection(branchTab);
        } else {
            if ((sourcePath != null && sourcePath.length() > 0)
                    && (targetPath != null && targetPath.length() > 0)) {
                specTabs.setSelection(fileTab);
            } else {
                specTabs.setSelection(branchTab);
            }
        }

        specTabs.notifyListeners(SWT.Selection, null);

        validate();

        return c;
    }

    /**
     * Update the source, target, and branch values
     */
    public void updatePaths() {
        TabItem[] tabs = specTabs.getSelection();
        if (tabs.length == 1) {
            if (tabs[0] == branchTab) {
                branchName = nameText.getText();
                if (limitButton.getSelection()) {
                    if (sourceButton.getSelection()) {
                        String limitSource = limitText.getText();
                        if (limitSource.length() > 0) {
                            sourcePath = limitSource;
                        } else {
                            sourcePath = null;
                        }
                        targetPath = null;
                    } else {
                        sourcePath = IP4Connection.ROOT;
                        String limitTarget = limitText.getText();
                        if (limitTarget.length() > 0) {
                            targetPath = limitTarget;
                        } else {
                            targetPath = null;
                        }
                    }
                } else {
                    targetPath = null;
                    sourcePath = null;
                }
            } else if (tabs[0] == fileTab) {
                branchName = null;
                sourcePath = sourceText.getText();
                targetPath = targetText.getText();
            }
        }
    }

    /**
     * Update the options object with the latest state of the option buttons
     */
    public void updateOptions() {
    	optionWidget.updateOptions(integrationOptions);
    	integrationOptions.setTrySafeResolve(attemptSafeResolveButton
                .getSelection());
    }

    private void updateHistory() {
        String branch = nameText.getText().trim();
        if (branch.length() > 0) {
            SessionManager.saveComboHistory(nameText, 10,
                    IPreferenceConstants.BRANCH_HISTORY);
        }
        String source = sourceText.getText().trim();
        if (source.length() > 0) {
            SessionManager.saveComboHistory(sourceText, 10,
                    IPreferenceConstants.SOURCE_FILE_HISTORY);
        }

        String target = targetText.getText().trim();
        if (target.length() > 0) {
            SessionManager.saveComboHistory(targetText, 10,
                    IPreferenceConstants.TARGET_FILE_HISTORY);
        }
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        updatePaths();
        updateOptions();
        updateHistory();
        updatePreferences();
        super.okPressed();
    }

    private void updatePreferences() {
        boolean tryResolve = attemptSafeResolveButton.getSelection();
        PerforceUIPlugin.getPlugin().getPreferenceStore()
                .setValue(TRY_AUTO_RESOLVE, tryResolve);
    }

    private void startPreview() {
        progressBar.setEnabled(true);
        progressBar.setVisible(true);
        progressLabel.setText(Messages.IntegrateDialog_GeneratingIntegPreview);
        preview.setEnabled(false);
    }

    private void stopPreview() {
        progressBar.setEnabled(false);
        progressBar.setVisible(false);
        progressLabel.setText(""); //$NON-NLS-1$
        preview.setEnabled(true);
    }

    private void runPreview() {
        final int changelist = getChangelist();
        final IP4Connection connection = resource.getConnection();
        final String name = branchName;
        updateOptions();
        final P4IntegrationOptions options = integrationOptions;
        final P4FileIntegration integration = getIntegration();
        startPreview();
        P4Runner.schedule(new P4Runnable() {

            @Override
            public String getTitle() {
                return Messages.IntegrateDialog_GeneratingIntegPreview;
            }

            @Override
            public void run(IProgressMonitor monitor) {
                final IP4Resource[] previewed = connection.integrate(
                        integration, name, changelist, true, true, options);
                PerforceUIPlugin.syncExec(new Runnable() {

                    public void run() {
                        if (!displayArea.isDisposed()) {
                            stopPreview();
                            new IntegrationPreviewDialog(getShell(), previewed)
                                    .open();
                        }
                    }
                });
            }

        });

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
                Messages.IntegrateDialog_Integrate, true);
        createButton(parent, IDialogConstants.CANCEL_ID,
                Messages.IntegrateDialog_Cancel, false);
    }

    /**
     * Get current file integration info
     * 
     * @return - file integration info
     */
    public P4FileIntegration getIntegration() {
        P4FileIntegration integration = new P4FileIntegration();
        integration.setEnd(this.endOption);
        integration.setStart(this.startOption);
        integration.setSource(this.sourcePath);
        integration.setTarget(this.targetPath);
        return integration;
    }

    /**
     * Set reverse mappings button
     * 
     * @param reverse
     */
    public void setReverseMappings(boolean reverse) {
        optionWidget.setReverseMappings(reverse);
    }

    /**
     * Set do not copy button
     * 
     * @param doNotCopy
     */
    public void setDoNotCopy(boolean doNotCopy) {
    	optionWidget.setDoNotCopy(doNotCopy);
    }

    /**
     * Set baseless merge button
     * 
     * @param merge
     */
    public void setBaselessMerge(boolean merge) {
        optionWidget.setBaselessMerge(merge);
    }

    /**
     * Set disregard history button
     * 
     * @param disregard
     */
    public void setDisregardHistory(boolean disregard) {
    	optionWidget.setDisregardHistory(disregard);
    }

    /**
     * Set integrate around deleted button
     * 
     * @param integrate
     */
    public void setIntegrateAroundDeleted(boolean integrate) {
        optionWidget.setIntegrateAroundDeleted(integrate);
    }

    /**
     * Set do not get latest revision button
     * 
     * @param latest
     */
    public void setDoNotGetLatest(boolean latest) {
    	optionWidget.setDoNotGetLatest(latest);
    }

    /**
     * Set propogate filetypes button
     * 
     * @param propogate
     */
    public void setPropogateFiletypes(boolean propogate) {
        optionWidget.setPropogateFiletypes(propogate);
    }

    /**
     * Get the source field
     * 
     * @return - souce path
     */
    public String getSource() {
        return this.sourcePath;
    }

    /**
     * Get the target field
     * 
     * @return - target path
     */
    public String getTarget() {
        return this.targetPath;
    }

    /**
     * Get the start revision option
     * 
     * @return - start revision
     */
    public String getStart() {
        return this.startOption;
    }

    /**
     * Get the end revision option
     * 
     * @return - end revision
     */
    public String getEnd() {
        return this.endOption;
    }

    /**
     * Get the pending changelist id
     * 
     * @return - pending changelist id
     */
    public int getChangelist() {
        return this.changelistCombo.getSelected();
    }

    /**
     * Get the changelist description
     * 
     * @return - description
     */
    public String getDescription() {
        return this.changelistCombo.getDescription();
    }

    /**
     * Get the branch name entered
     * 
     * @return - branch name
     */
    public String getBranch() {
        return this.branchName;
    }

    /**
     * Is a file integration selected?
     * 
     * @return - true if file, false if branch
     */
    public boolean isFileIntegration() {
        return this.fileIntegration;
    }

    /**
     * Get the selection integration options
     * 
     * @return - p4 integration options
     */
    public P4IntegrationOptions getSelectedOptions() {
        return integrationOptions;
    }

    private void initOptionWidget(IP4Resource resource){
    	optionWidget=createWidget(resource);
    	integrationOptions=optionWidget.getDefaultIntegrationOptions();
    }

	private IntegOptionWidget createWidget(IP4Resource resource) {
		IntegOptionWidget widget=null;
    	IP4Connection connection = resource.getConnection();
    	IServer server = connection.getServer();
    	String stream=connection.getClient().getStream();
    	if(stream!=null && !stream.trim().isEmpty()){ // stream client always using integ 3, server will take care of this.
    		widget = IntegOptionWidget.INTEG3;
    	}else{
	    	int serverVer = server.getServerVersionNumber();
	    	String level=null;
			try {
				IServerInfo info = server.getServerInfo();
				level = info.getIntegEngine();
			} catch (Exception e) {
				PerforceProviderPlugin.logError(e.getLocalizedMessage());
				PerforceUIPlugin.syncExec(new Runnable() {
					
					public void run() {
						MessageDialog.openError(null, "Error", "Cannot connecto retrieve integEngine information, please check your connection and retry.");
					}
				});
			}
	    	if(level==null){
	    		if(serverVer<IP4ServerConstants.INTEG3_SERVERID_VERSION){ 
	    			widget=IntegOptionWidget.INTEG2; // older server default to integEngine 2
	    		}else{
	    			widget=IntegOptionWidget.INTEG3; // newer server default to integEngine 3
	    		}
	    	}else{
	    		widget=IntegOptionWidget.valueOf("INTEG"+level.trim());
	    	}
    	}
		return widget;
	}
    
}
