/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.connection;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.fieldassist.ContentAssistCommandAdapter;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.client.IClientSummary;
import com.perforce.p4java.core.IStreamSummary;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.team.core.IConstants;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.ErrorHandler;
import com.perforce.team.core.p4java.IErrorHandler;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.P4BrowsableConnection;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.streams.StreamsSuggestProvider;
import com.perforce.team.ui.streams.SuggestBox;
import com.perforce.team.ui.streams.SuggestBox.SelectionModel;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ClientWizardPage extends BaseConnectionWizardPage {

    private Composite displayArea;

    private Button existingWorkspaceButton;
    private Button hostOnlyButton;
    private Table workspaceTable;
    private Label workspaceLabel;
    private ToolBar workspaceBar;
    private ToolItem refreshItem;
    private Image refreshImage;
    private Text workspaceText;
    private TextContentAdapter contentAdapter;
    private SimpleContentProposalProvider provider;
    private ContentAssistCommandAdapter contentAssist;

    private Button newWorkspaceButton;
    private Label nameLabel;
    private Text nameText;
    private Label nameInstructionsLabel;
    private Label locationLabel;
    private Text locationText;
    private Button locationButton;

    private boolean create = false;
    private String client = null;
    private String location = null;
    private boolean errorShown = false;
    
    // UI related flags only
    private boolean updateName=false; // is name being changed?
    private boolean needMatch=true; // match name in workspace
    
    private IErrorHandler handler = new ErrorHandler() {

        @Override
        public boolean shouldRetry(IP4Connection connection,
                P4JavaException exception) {
            boolean retry = false;
            if (!errorShown) {
                if (P4ConnectionManager.isClientNonExistentError(exception
                        .getMessage())) {
                    showClientError(connection);
                } else {
                    retry = P4ConnectionManager.getManager().displayException(
                            connection, exception, true, false);
                }
                // Mark error shown if not retrying
                errorShown = !retry;
            }
            return retry;
        }

    };

    private ModifyListener modify = new ModifyListener() {

        public void modifyText(ModifyEvent e) {
            if(e.getSource()==nameText){
                updateName=true;
                if(needMatch){
                    locationText.setText(getDefaultLocation()+File.separatorChar+nameText.getText());
                }
                updateName=false;
            }else if(e.getSource()==locationText){
                if(!updateName){
                    needMatch=false;
                }
            }
            validatePage();
        }
    };

    private SuggestBox streamCombo;

    private Button launchImportButton;
    private boolean showLaunchImportOption=false;    
    
    /**
     * @param pageName
     */
    public ClientWizardPage(String pageName) {
        super(pageName);
        setImageDescriptor(PerforceUIPlugin.getPlugin().getImageDescriptor(
                IPerforceUIConstants.IMG_SHARE_WIZARD));
        setTitle(Messages.ClientWizardPage_ChooseClientTitle);
        setDescription(Messages.ClientWizardPage_ChooseClientMessage);
    }

    public ClientWizardPage(String pageName, boolean launchImport) {
    	this(pageName);
    	this.showLaunchImportOption=launchImport;
    }

    private void createWorkspaceTable(Composite parent) {

        Composite workspaceArea = new Composite(parent, SWT.NONE);
        GridLayout waLayout = new GridLayout(3, false);
        waLayout.marginLeft = 15;
        waLayout.marginHeight = 0;
        waLayout.marginWidth = 0;
        workspaceArea.setLayout(waLayout);
        workspaceArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                false));

        hostOnlyButton=new Button(workspaceArea, SWT.CHECK);
        hostOnlyButton.setText(Messages.ClientWizardPage_HostOnly+" ("+getHostname()+")"); //$NON-NLS-1$
        hostOnlyButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 3, 1));
        hostOnlyButton.setSelection(true);
        hostOnlyButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                loadClients();
                validatePage();
            }
		});

        workspaceLabel = new Label(workspaceArea, SWT.LEFT);
        workspaceLabel.setText(Messages.ClientWizardPage_ExistingWorkspaceName);

        workspaceText = new Text(workspaceArea, SWT.SINGLE | SWT.BORDER);
        workspaceText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                false));
        workspaceText.addModifyListener(modify);
        contentAdapter = new TextContentAdapter();

        // This is to provide Eclipse 3.2 compatibility
        provider = new SimpleContentProposalProvider(new String[0]) {

            @Override
            public IContentProposal[] getProposals(String contents, int position) {
                List<IContentProposal> matching = new ArrayList<IContentProposal>();
                for (IContentProposal proposal : super.getProposals(contents,
                        position)) {
                    String propContent = proposal.getContent();
                    if (propContent != null
                            && propContent.length() >= contents.length()
                            && propContent.substring(0, contents.length())
                                    .equalsIgnoreCase(contents)) {
                        matching.add(proposal);
                    }
                }
                return matching.toArray(new IContentProposal[0]);
            }

        };
        // On Eclipse 3.3+ remove the overriden getProposals and use:
        // provider.setFiltering(true);

        contentAssist = new ContentAssistCommandAdapter(workspaceText,
                contentAdapter, provider, null, null);

        // This work on Eclipse 3.2.2+ with null specified
        try {
            contentAssist.setAutoActivationCharacters(null);
        } catch (Throwable t) {
            // Suppress this due to fix in Eclipse 3.2.2 that accepts null but
            // still needs to work against 3.2 and 3.2.1
        }
        contentAssist
                .setProposalAcceptanceStyle(ContentAssistCommandAdapter.PROPOSAL_REPLACE);

        workspaceBar = new ToolBar(workspaceArea, SWT.FLAT);
        refreshItem = new ToolItem(workspaceBar, SWT.PUSH);
        refreshItem.setToolTipText(Messages.ClientWizardPage_RefreshWorkspaces);
        refreshImage = PerforceUIPlugin.getPlugin()
                .getImageDescriptor(IPerforceUIConstants.IMG_REFRESH)
                .createImage();
        refreshItem.setImage(refreshImage);
        refreshItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                loadClients();
                validatePage();
            }

        });

        workspaceTable = new Table(parent, SWT.SINGLE | SWT.FULL_SELECTION
                | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        GridData wtData = new GridData(SWT.FILL, SWT.FILL, true, true);
        wtData.horizontalIndent = 15;
        wtData.heightHint = 100;
        workspaceTable.setLayoutData(wtData);

        workspaceTable.setHeaderVisible(true);
        workspaceTable.setLinesVisible(true);

        TableLayout layout = new TableLayout();
        layout.addColumnData(new ColumnWeightData(30, true));
        layout.addColumnData(new ColumnWeightData(30, true));
        layout.addColumnData(new ColumnWeightData(20, true));
        layout.addColumnData(new ColumnWeightData(20, true));
        workspaceTable.setLayout(layout);

        TableColumn clientColumn = new TableColumn(workspaceTable, SWT.LEFT);
        clientColumn.setText(Messages.ClientWizardPage_Client);
        TableColumn rootColumn = new TableColumn(workspaceTable, SWT.LEFT);
        rootColumn.setText(Messages.ClientWizardPage_Root);
        TableColumn streamColumn = new TableColumn(workspaceTable, SWT.LEFT);
        streamColumn.setText(Messages.ClientWizardPage_Stream);
        TableColumn hostColumn = new TableColumn(workspaceTable, SWT.LEFT);
        hostColumn.setText(Messages.ClientWizardPage_Host);

        workspaceTable.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (workspaceTable.getSelectionCount() == 1) {
                    TableItem selected = workspaceTable.getSelection()[0];
                    workspaceText.setText(selected.getText(0));
                    validatePage();
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                if (workspaceTable.getSelectionCount() == 1) {
                    TableItem selected = workspaceTable.getSelection()[0];
                    workspaceText.setText(selected.getText(0));
                    validatePage();
                    getWizard().getContainer().showPage(getNextPage());
                }
            }

        });

    }

    /**
     * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            loadClients();
            streamCombo.setSuggestProvider(new StreamsSuggestProvider(createConnection(true,true)));
            validatePage();
        }
        super.setVisible(visible);
        if (visible && isExistingClientSelected()) {
            workspaceText.setFocus();
        }
    }

    private void createNewWorkspaceArea(Composite parent) {
        Composite newArea = new Composite(parent, SWT.NONE);
        newArea.setLayout(new GridLayout(3, false));
        GridData naData = new GridData(SWT.FILL, SWT.FILL, true, true);
        naData.horizontalIndent = 15;
        newArea.setLayoutData(naData);

        nameLabel = new Label(newArea, SWT.LEFT);
        nameLabel.setText(Messages.ClientWizardPage_WorkspaceName);
        nameLabel.setLayoutData(new GridData(SWT.END, SWT.CENTER, false,
                false));
        nameText = new Text(newArea, SWT.SINGLE | SWT.BORDER);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).indent(10,-1).applyTo(nameText);
        nameText.addModifyListener(modify);
        nameText.setEnabled(false);
        new Label(newArea, SWT.NONE);

        new Label(newArea, SWT.NONE);
        nameInstructionsLabel = new Label(newArea, SWT.LEFT);
        nameInstructionsLabel
                .setText(Messages.ClientWizardPage_NoSpacesAllowed);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).indent(10,-1).applyTo(nameInstructionsLabel);
        new Label(newArea, SWT.NONE);

        locationLabel = new Label(newArea, SWT.LEFT);
        locationLabel.setText(Messages.ClientWizardPage_Location);
        locationLabel.setLayoutData(new GridData(SWT.END, SWT.CENTER, false,
                false));
        locationText = new Text(newArea, SWT.SINGLE | SWT.BORDER);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).indent(10,-1).applyTo(locationText);
        String defaultLocation = getDefaultLocation();
        locationText.setText(defaultLocation);
        locationText.addModifyListener(modify);
        locationText.setEnabled(false);
        locationButton = new Button(newArea, SWT.PUSH);
        locationButton.setText(Messages.ClientWizardPage_Browse);
        locationButton.setEnabled(false);
        locationButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                DirectoryDialog dialog = new DirectoryDialog(locationButton
                        .getShell(), SWT.NONE);
                String dir = dialog.open();
                if (dir != null) {
                    locationText.setText(dir);
                }
            }

        });

        Label streamLabel = new Label(newArea, SWT.LEFT);
        streamLabel.setText(Messages.ClientWizardPage_StreamLabel);
        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).grab(false, false).applyTo(streamLabel);
        streamCombo = new SuggestBox(newArea, SWT.None, new StreamsSuggestProvider(createConnection(true,true)));
        GridDataFactory.swtDefaults().indent(10,-1).align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(streamCombo);
        streamCombo.setEnabled(false);
        streamCombo.getModel().addValueChangeListener(new IValueChangeListener() {
            public void handleValueChange(ValueChangeEvent event) {
                validatePage();
            }
        });

    }

    private String getDefaultLocation() {
    	IPreferenceStore store = PerforceUIPlugin.getPlugin().getPreferenceStore();
    	String defaultLocation=store.getString(IPerforceUIConstants.PREF_CLIENT_ROOT_PARENT_DEFAULT);
    	if(defaultLocation==null){
	        defaultLocation=System.getProperty("user.home"); //$NON-NLS-1$
	        if (defaultLocation != null) {
	            if (!defaultLocation.endsWith(Character
	                    .toString(File.separatorChar))) {
	                defaultLocation += File.separatorChar;
	            }
	            defaultLocation += "Perforce"; //$NON-NLS-1$
	        }else
	            defaultLocation=""; //$NON-NLS-1$
    	}
        return defaultLocation;
    }

    /**
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        displayArea = new Composite(parent, SWT.NONE);
        displayArea.setLayout(new GridLayout(1, true));
        displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        existingWorkspaceButton = new Button(displayArea, SWT.RADIO);
        existingWorkspaceButton
                .setText(Messages.ClientWizardPage_SelectExistingWorkspace);
        existingWorkspaceButton.setSelection(true);
        existingWorkspaceButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean selected = existingWorkspaceButton.getSelection();
                workspaceText.setEnabled(selected);
                workspaceTable.setEnabled(selected);
                refreshItem.setEnabled(selected);
                validatePage();
            }
        });

        createWorkspaceTable(displayArea);

        newWorkspaceButton = new Button(displayArea, SWT.RADIO);
        newWorkspaceButton
                .setText(Messages.ClientWizardPage_CreateNewWorkspace);
        newWorkspaceButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean selected = newWorkspaceButton.getSelection();
                nameText.setEnabled(selected);
                locationText.setEnabled(selected);
                locationButton.setEnabled(selected);
                streamCombo.setEnabled(selected);
                validatePage();
            }
        });

        createNewWorkspaceArea(displayArea);
        
        createImportOptionArea(displayArea);

        setPageComplete(false);
        setControl(displayArea);
    }

    private void createImportOptionArea(Composite parent) {
    	if(this.showLaunchImportOption){
	    	Composite optArea = new Composite(parent, SWT.NONE);
	        optArea.setLayout(new GridLayout(1, false));
	        GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
	        optArea.setLayoutData(data);
	
	        launchImportButton = new Button(optArea, SWT.CHECK);
	        launchImportButton.setText(Messages.ClientWizardPage_LaunchPerforceProjectImportWizard);
	        launchImportButton.setSelection(true); // default is launch import project wizard after creation.
    	}
	}

	private void loadClients() {
    	final String workspace=Messages.ClientWizardPage_Loading.equals(workspaceText.getText())?null:workspaceText.getText();

        errorShown = false;
        workspaceTable.removeAll();

        TableItem loading = new TableItem(workspaceTable, SWT.NONE);
        loading.setText(Messages.ClientWizardPage_Loading);
        
        try {
			getContainer().run(true, true, new IRunnableWithProgress() {
				
				public void run(IProgressMonitor monitor) throws InvocationTargetException,
						InterruptedException {
					
					monitor.beginTask(MessageFormat.format(Messages.ClientWizardPage_FetchingClientsFor, getUser()), 4);
					
	                IClientSummary[] loadedClients = null;

	                IP4Connection connection = getWizardConnection();
	                if(connection==null){
		                // Ignore client since it may be non-existent and it shouldn't
		                // affect client lookup
	                	connection = createConnection(true, true);

		                monitor.worked(1);
		                
		                // This allows an auto-login attempt to be made based on
		                // password entered on previous wizard pages
		                connection.getParameters().setSavePassword(true);
	
		                connection.setErrorHandler(handler);
	
		                // Call connect to ensure a p4 info has been run so we know the
		                // client host name before filtering the client list based on
		                // the Host field
		                connection.connect();
		                
		                setWizardConnection(connection);
	                }
	                monitor.worked(1);

	                // Get only the owned clients 
	                loadedClients = connection.getOwnedClients();

	                monitor.worked(1);
	                
	                updateAuthTicket(connection);
	                if (loadedClients == null) {
	                    loadedClients = new IClient[0];
	                }
	                final IClientSummary[] clients = loadedClients;
	                
	                PerforceUIPlugin.asyncExec(new Runnable() {
						
						public void run() {
	                        if (workspaceTable != null
	                                && !workspaceTable.isDisposed()) {
	                            workspaceTable.removeAll();
	                            Arrays.sort(clients,
	                                    new Comparator<IClientSummary>() {

	                                        public int compare(IClientSummary o1,
	                                                IClientSummary o2) {
	                                            if (o1 != null
	                                                    && o1.getName() != null
	                                                    && o2 != null
	                                                    && o2.getName() != null) {
	                                                return o1.getName().compareTo(
	                                                        o2.getName());
	                                            } else {
	                                                return 0;
	                                            }
	                                        }
	                                    });
	                            List<String> clientProposals = new ArrayList<String>();

	                            String hostname=getHostname();
	                            for (IClientSummary spec : clients) {
	                            	if(hostOnlyButton.getSelection() && !StringUtils.isEmpty(spec.getHostName()) && !spec.getHostName().equals(hostname)){
	                            		continue;
	                            	}
	                                clientProposals.add(spec.getName());
	                                TableItem item = new TableItem(workspaceTable,
	                                        SWT.NONE);
	                                item.setText(new String[] { spec.getName(),
	                                        spec.getRoot(), spec.getStream(), spec.getHostName()});
	                                if(spec.getName().equals(workspace))
	                                	workspaceTable.setSelection(item);
	                            }
	                            workspaceText.setText(IConstants.EMPTY_STRING);
	                            if(workspace!=null){
                            		workspaceText.setText(workspace);
	                            }
	                            provider.setProposals(clientProposals
	                                    .toArray(new String[0]));
	                        }
						}

					});
	            }
			});
		} catch (Exception e) {
			PerforceProviderPlugin.logError(e);
			MessageDialog.openError(getShell(), Messages.ClientWizardPage_Error, e.getLocalizedMessage());
		}
	}

	private String getHostname() {
        String hostName=IConstants.EMPTY_STRING;
        try {
        	hostName=P4Workspace.getP4HOST();
        	if(StringUtils.isEmpty(hostName))
        		hostName=InetAddress.getLocalHost().getHostName();
		} catch (Exception e) {
		}
        return hostName;
	}

    public void validatePage() {
        String message = null;
        if (existingWorkspaceButton.getSelection()) {
            this.create = false;
            this.location = null;
            String client = workspaceText.getText().trim();
            if (client.length() == 0) {
                message = Messages.ClientWizardPage_MustEnterClientName;
            } else {
        		message = Messages.ClientWizardPage_ClientNameInvalid;
            	IContentProposal[] matched = provider.getProposals(client, client.length());
            	if(matched!=null && matched.length>0){
            		for(IContentProposal prop:matched){
            			if(client.equals(prop.getContent())){
            				this.client = client;
            				message=null;
            				break;
            			}
            		}
            	}
            }
        } else if (newWorkspaceButton.getSelection()) {
            this.create = true;
            String client = nameText.getText().trim();
            String location = locationText.getText().trim();

            if (client.length() == 0) {
                message = Messages.ClientWizardPage_MustEnterClientName;
            } else {
                message =P4UIUtils.validateName(client, Messages.ClientWizardPage_Workspace);
                if(message==null)
                    this.client = client;
            }

            if (message == null) {
                if (location.length() == 0) {
                    message = Messages.ClientWizardPage_MustEnterValidDirectory;
                } else {
                    this.location = location;
                }
            }
            
            if(message==null){
                message = validateStream();
            }
        }

        setPageComplete(message == null);
        setErrorMessage(message);
    }

    private void showClientError(final IP4Connection connection) {
        if (connection != null) {
            PerforceUIPlugin.syncExec(new Runnable() {

                public void run() {
                    String client = connection.getParameters()
                            .getClientNoNull();
                    P4ConnectionManager
                            .getManager()
                            .openError(
                                    getShell(),
                                    Messages.ClientWizardPage_ClientDoesNotExistTitle,
                                    NLS.bind(
                                            Messages.ClientWizardPage_ClientDoesNotExistMessage,
                                            client));
                }
            });
        }
    }

    private void showClientExistsError(final IP4Connection connection) {
        if (connection != null) {
            PerforceUIPlugin.syncExec(new Runnable() {

                public void run() {
                    String client = connection.getParameters()
                            .getClientNoNull();
                    P4ConnectionManager
                            .getManager()
                            .openError(
                                    getShell(),
                                    Messages.ClientWizardPage_ClientExistsTitle,
                                    NLS.bind(
                                            Messages.ClientWizardPage_ClientExistsMessage,
                                            client));
                }
            });
        }
    }

    private boolean validateClient(String client) {
        this.errorShown = false;
        boolean validated = false;
        if (isExistingClientSelected()) {
            IP4Connection connection = createConnection();
            connection.getParameters().setSavePassword(true);
            connection.setErrorHandler(handler);
            validated = connection.refreshClient();
        } else {
            P4BrowsableConnection connection = createBrowseConnection();
            connection.getParameters().setSavePassword(true);
            connection.setErrorHandler(handler);
            validated = connection.refreshClient();
            if (validated) {
                if (connection.clientExists()) {
                    showClientExistsError(connection);
                    validated = false;
                }
            }
        }

        return validated;
    }

    /**
     * @see org.eclipse.jface.wizard.WizardPage#getNextPage()
     */
    @Override
    public IWizardPage getNextPage() {
        IWizardPage next = null;
        String client = getClient();
        if (client!=null && validateClient(client)) {
            next = super.getNextPage();
        }
        return next;
    }
    
    @Override
    public boolean canFlipToNextPage() {
        return isPageComplete() && (super.getNextPage()!=null);
    }

    /**
     * @see org.eclipse.jface.dialogs.DialogPage#dispose()
     */
    @Override
    public void dispose() {
        super.dispose();
        if (refreshImage != null) {
            refreshImage.dispose();
        }
    }

    /**
     * Get location of client
     * 
     * @return - directory location
     */
    public String getLocation() {
        return this.location;
    }

    /**
     * Get client name
     * 
     * @return - perforce client workspace name
     */
    @Override
    public String getClient() {
        return this.client;
    }

    /**
     * Set the existing client field
     * 
     * @param client
     */
    public void setClient(String client) {
        if (client != null) {
            this.workspaceText.setText(client);
        }
    }

    /**
     * Is existing client option selected
     * 
     * @return - true if selected
     */
    public boolean isExistingClientSelected() {
        return this.existingWorkspaceButton.getSelection();
    }

    /**
     * Is new client option selected
     * 
     * @return - true if selected
     */
    public boolean isNewClientSelected() {
        return this.newWorkspaceButton.getSelection();
    }

    /**
     * True if the client should be created
     * 
     * @return - true to create
     */
    public boolean shouldCreate() {
        return this.create;
    }

    public String getStream() {
        SelectionModel model=(SelectionModel) streamCombo.getModel().getValue();
        if(model!=null){
            if(model.getSelection()!=null){
                return ((IStreamSummary)model.getSelection()).getStream();
            }
        }
        return ""; //$NON-NLS-1$
    }

    private String validateStream() {
        SelectionModel model=(SelectionModel) streamCombo.getModel().getValue();
        if(model!=null){
            if(model.getSelection()==null){
                if(!streamCombo.getText().isEmpty()){
                   return MessageFormat.format(Messages.ClientWizardPage_StreamInvalid,streamCombo.getText());
                }
            }
        }
        return null;
    }

    public boolean isLaunchImportWizard(){
    	return launchImportButton.getSelection();
    }
}
