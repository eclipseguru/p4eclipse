/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.server;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.client.IClientSummary.ClientLineEnd;
import com.perforce.p4java.client.IClientSummary.IClientOptions;
import com.perforce.p4java.client.IClientSummary.IClientSubmitOptions;
import com.perforce.p4java.client.IClientViewMapping;
import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.IMapEntry;
import com.perforce.p4java.core.IStream;
import com.perforce.p4java.core.IStreamSummary;
import com.perforce.p4java.core.ViewMap;
import com.perforce.p4java.impl.generic.client.ClientOptions;
import com.perforce.p4java.impl.generic.client.ClientSubmitOptions;
import com.perforce.p4java.impl.generic.client.ClientView;
import com.perforce.p4java.impl.generic.client.ClientView.ClientViewMapping;
import com.perforce.p4java.impl.mapbased.client.Client;
import com.perforce.p4java.option.server.GetStreamOptions;
import com.perforce.p4java.server.IOptionsServer;
import com.perforce.team.core.IConstants;
import com.perforce.team.core.IP4ServerConstants;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Stream;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.ui.IErrorDisplay;
import com.perforce.team.ui.IErrorProvider;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.SWTUtils;
import com.perforce.team.ui.streams.StreamsSuggestProvider;
import com.perforce.team.ui.streams.SuggestBox;
import com.perforce.team.ui.streams.SuggestBox.SelectionModel;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * @author ali
 */
public class ClientWidget implements IErrorProvider{

    private Composite displayArea;
    private Label workspaceLabel;
    private Text workspaceText;
    private Label updateLabel;
    private Text updateText;
    private Label accessLabel;
    private Text accessText;
    private Label ownerLabel;
    private Text ownerText;
    private Label hostLabel;
    private Text hostText;
    private Label submitLabel;
    private Combo submitCombo;
    private Label lineLabel;
    private Combo lineCombo;
    private Label descriptionLabel;
    private Text descriptionText;
    private Label streamLabel;
    private SuggestBox streamCombo;
    private Label rootLabel;
    private Text rootText;
    private Label altRootsLabel;
    private Text altRootsText;
    private Label optionsLabel;
    private Composite optionsArea;
    private Button allwriteButton;
    private Button clobberButton;
    private Button compressButton;
    private Button lockedButton;
    private Button modtimeButton;
    private Button rmdirButton;
    private Label viewLabel;
    private Text viewText;
    private Composite serverIdArea;
    private Button restrictToServerId;
    private Text serverIdText;
    private Text streamAtChangeText;

    private IP4Connection connection;
    private IClient spec;
    private IClient currentSpec;

    private StreamsSuggestProvider provider;
    private IErrorDisplay errorDisplay;
    
    // if the client is not created yet
	private boolean clientNameEditable;

    /**
     * Create client widget around an initial client spec
     * 
     * @param connection
     * @param spec
     */
    public ClientWidget(IP4Connection connection, IClient spec) {
    	this(connection, spec, false);
    }

    public ClientWidget(IP4Connection connection, IClient spec, boolean newClient) {
        this.connection = connection;
        this.spec = spec;
        this.provider = new StreamsSuggestProvider(connection);
        this.clientNameEditable = newClient;
    }

    private void fillSubmitCombo(IClient spec) {
        submitCombo.add(Messages.ClientWidget_NoneSelected);
        submitCombo.add("submitunchanged"); //$NON-NLS-1$
        submitCombo.add("submitunchanged+reopen"); //$NON-NLS-1$
        submitCombo.add("revertunchanged"); //$NON-NLS-1$
        submitCombo.add("revertunchanged+reopen"); //$NON-NLS-1$
        submitCombo.add("leaveunchanged"); //$NON-NLS-1$
        submitCombo.add("leaveunchanged+reopen"); //$NON-NLS-1$

        IClientSubmitOptions options = spec.getSubmitOptions();
        if (options != null) {
            if (options.isSubmitunchanged()) {
                submitCombo.select(1);
            } else if (options.isSubmitunchangedReopen()) {
                submitCombo.select(2);
            } else if (options.isRevertunchanged()) {
                submitCombo.select(3);
            } else if (options.isRevertunchangedReopen()) {
                submitCombo.select(4);
            } else if (options.isLeaveunchanged()) {
                submitCombo.select(5);
            } else if (options.isLeaveunchangedReopen()) {
                submitCombo.select(6);
            } else {
                submitCombo.select(0);
            }
        } else {
            submitCombo.select(0);
        }

    }

    private void fillLineEndCombo(IClient spec) {
        lineCombo.add(Messages.ClientWidget_NoneSelected);
        lineCombo.add("local"); //$NON-NLS-1$
        lineCombo.add("unix"); //$NON-NLS-1$
        lineCombo.add("mac"); //$NON-NLS-1$
        lineCombo.add("win"); //$NON-NLS-1$
        lineCombo.add("share"); //$NON-NLS-1$

        ClientLineEnd endings = spec.getLineEnd();
        if (ClientLineEnd.LOCAL == endings) {
            lineCombo.select(1);
        } else if (ClientLineEnd.UNIX == endings) {
            lineCombo.select(2);
        } else if (ClientLineEnd.MAC == endings) {
            lineCombo.select(3);
        } else if (ClientLineEnd.WIN == endings) {
            lineCombo.select(4);
        } else if (ClientLineEnd.SHARE == endings) {
            lineCombo.select(5);
        } else {
            lineCombo.select(0);
        }

    }

    private String getSelectedStream() {
        if (streamCombo != null) {
            SelectionModel model = (SelectionModel) streamCombo.getModel()
                    .getValue();
            if (model != null) {
                Object stream = model.getSelection();
                if (stream instanceof IStreamSummary) {
                    return ((IStreamSummary) stream).getStream();
                }
            }
        }
        return null;
    }

    private ClientLineEnd getSelectedLineEnding() {
        switch (lineCombo.getSelectionIndex()) {
        case 1:
            return ClientLineEnd.LOCAL;
        case 2:
            return ClientLineEnd.UNIX;
        case 3:
            return ClientLineEnd.MAC;
        case 4:
            return ClientLineEnd.WIN;
        case 5:
            return ClientLineEnd.SHARE;
        default:
            return null;
        }
    }

    private IClientSubmitOptions getSelectedSubmitOptions() {
        ClientSubmitOptions options = null;
        int index = submitCombo.getSelectionIndex();
        if (index > 0) {
            options = new ClientSubmitOptions();
            switch (index) {
            case 1:
                options.setSubmitunchanged(true);
                break;
            case 2:
                options.setSubmitunchangedReopen(true);
                break;
            case 3:
                options.setRevertunchanged(true);
                break;
            case 4:
                options.setRevertunchangedReopen(true);
                break;
            case 5:
                options.setLeaveunchanged(true);
                break;
            case 6:
                options.setLeaveunchangedReopen(true);
                break;
            }
        }
        return options;
    }

    private IClientOptions getSelectedOptions() {
        ClientOptions options = new ClientOptions();
        options.setAllWrite(allwriteButton.getSelection());
        options.setClobber(clobberButton.getSelection());
        options.setCompress(compressButton.getSelection());
        options.setLocked(lockedButton.getSelection());
        options.setModtime(modtimeButton.getSelection());
        options.setRmdir(rmdirButton.getSelection());
        return options;
    }

    private ClientView getClientView() {
        ClientView view = new ClientView();
        StringTokenizer tokenizer = new StringTokenizer(viewText.getText(),
                "\r\n"); //$NON-NLS-1$
        int count = 0;
        while (tokenizer.hasMoreElements()) {
            view.addEntry(new ClientViewMapping(count, tokenizer.nextToken()));
            count++;
        }
        return view;
    }

    private void createTopControls(Composite parent, IClient spec) {
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yy hh:mm aaa"); //$NON-NLS-1$
        GridData textData = new GridData(SWT.FILL, SWT.FILL, true, false);

        workspaceLabel = new Label(parent, SWT.LEFT);
        workspaceLabel.setText(Messages.ClientWidget_Workspace);
        workspaceText = new Text(parent, SWT.SINGLE | SWT.BORDER
                | (clientNameEditable?SWT.NONE:SWT.READ_ONLY));
        workspaceText.setLayoutData(textData);
        if (spec.getName() != null) {
            workspaceText.setText(spec.getName());
        }

        hostLabel = new Label(parent, SWT.LEFT);
        hostLabel.setText(Messages.ClientWidget_Host);
        hostText = new Text(parent, SWT.SINGLE | SWT.BORDER);
        hostText.setLayoutData(textData);
        if (spec.getHostName() != null) {
            hostText.setText(spec.getHostName());
        }

        updateLabel = new Label(parent, SWT.LEFT);
        updateLabel.setText(Messages.ClientWidget_Update);
        updateText = new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
        updateText.setLayoutData(textData);
        if (spec.getUpdated() != null) {
            updateText.setText(format.format(spec.getUpdated()));
        }

        submitLabel = new Label(parent, SWT.LEFT);
        submitLabel.setText(Messages.ClientWidget_SubmitOptions);
        submitCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
        submitCombo.setLayoutData(textData);
        fillSubmitCombo(spec);

        accessLabel = new Label(parent, SWT.LEFT);
        accessLabel.setText(Messages.ClientWidget_Access);
        accessText = new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
        accessText.setLayoutData(textData);
        if (spec.getAccessed() != null) {
            accessText.setText(format.format(spec.getAccessed()));
        }

        lineLabel = new Label(parent, SWT.LEFT);
        lineLabel.setText(Messages.ClientWidget_LineEnd);
        lineCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
        lineCombo.setLayoutData(textData);
        fillLineEndCombo(spec);

        ownerLabel = new Label(parent, SWT.LEFT);
        ownerLabel.setText(Messages.ClientWidget_Owner);
        ownerText = new Text(parent, SWT.SINGLE | SWT.BORDER);
        if (spec.getOwnerName() != null) {
            ownerText.setText(spec.getOwnerName());
        }
        ownerText.setLayoutData(textData);

        if (connection.getIntVersion() >= IP4ServerConstants.SUBMIT_OPTIONS_VERSION) {
            // Create spacers
            new Label(parent, SWT.LEFT);
            new Label(parent, SWT.LEFT);
        } else {
            // Hide submit options
            GridData hiddenData = new GridData(SWT.FILL, SWT.FILL, true, false);
            hiddenData.exclude = true;
            submitCombo.setLayoutData(hiddenData);
            submitLabel.setLayoutData(hiddenData);
        }
    }

    private void createBottomControls(Composite parent, IClient spec) {
        boolean enableView=true;
        
        GridData labelData = new GridData(SWT.FILL, SWT.TOP, false, false);

        GridData largeTextData = new GridData(SWT.FILL, SWT.FILL, true, true);
        largeTextData.heightHint = 100;
        largeTextData.horizontalSpan = 3;

        descriptionLabel = new Label(parent, SWT.LEFT);
        descriptionLabel.setText(Messages.ClientWidget_Description);
        descriptionLabel.setLayoutData(labelData);
        descriptionText = new Text(parent, SWT.MULTI | SWT.BORDER);
        descriptionText.setLayoutData(largeTextData);
        if (spec.getDescription() != null) {
            descriptionText.setText(spec.getDescription());
        }

        if (connection.getIntVersion() >= IP4ServerConstants.STREAM_VERSION) {
            streamLabel = new Label(parent, SWT.LEFT);
            streamLabel.setText(Messages.ClientWidget_Stream);
            streamCombo = new SuggestBox(parent, SWT.None, provider);
            streamCombo.setLayoutData(GridDataFactory.swtDefaults()
                    .align(SWT.FILL, SWT.CENTER).grab(true, false).span(3, 1)
                    .create());
            String current = spec.getStream();
            if (!StringUtils.isEmpty(current)) {
                IP4Stream stream = this.connection.getStream(current);
                enableView=(stream==null);
                streamCombo.getModel().setValue(
                        new SelectionModel(null, stream == null ? null : stream
                                .getStreamSummary()));
                streamCombo.refresh();
            }

            if (connection.getServer().getServerVersionNumber() >= IP4ServerConstants.SERVERID_VERSION) {
                Label streamAtChangeLabel = new Label(parent, SWT.LEFT);
                streamAtChangeLabel.setText(Messages.ClientWidget_StreamAtChange);
                streamAtChangeText = new Text(parent, SWT.BORDER);
                if(IChangelist.UNKNOWN!=spec.getStreamAtChange()){
                    streamAtChangeText.setText(spec.getStreamAtChange()+""); //$NON-NLS-1$
                }
                SWTUtils.decorate(streamAtChangeText, SWT.LEFT | SWT.TOP);
                SWTUtils.updateDecoration(streamAtChangeText, ValidationStatus.info(Messages.ClientWidget_InputChangeListNumberMsg));
                GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
                .grab(true, false).span(3, 1).applyTo(streamAtChangeText);
            }
        }

        rootLabel = new Label(parent, SWT.LEFT);
        rootLabel.setText(Messages.ClientWidget_Root);
        rootText = new Text(parent, SWT.SINGLE | SWT.BORDER);
        rootText.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.FILL, SWT.CENTER).grab(true, false).span(3, 1)
                .create());
        if (spec.getRoot() != null) {
            rootText.setText(spec.getRoot());
        }

        altRootsLabel = new Label(parent, SWT.LEFT);
        altRootsLabel.setLayoutData(labelData);
        altRootsLabel.setText(Messages.ClientWidget_AltRoots);
        altRootsText = new Text(parent, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL
                | SWT.H_SCROLL);
        GridData artData = new GridData(SWT.FILL, SWT.FILL, true, false);
        artData.horizontalSpan = 3;
        artData.heightHint = P4UIUtils.computePixelHeight(
                altRootsText.getFont(), 3);
        altRootsText.setLayoutData(artData);

        if (spec.getAlternateRoots() != null) {
            StringBuilder roots = new StringBuilder();
            for (String alt : spec.getAlternateRoots()) {
                if (alt != null) {
                    roots.append(alt);
                    roots.append('\n');
                }
            }
            altRootsText.setText(roots.toString());
        }

        optionsLabel = new Label(parent, SWT.LEFT);
        optionsLabel.setText(Messages.ClientWidget_Options);
        optionsArea = new Composite(parent, SWT.BORDER | SWT.SHADOW_IN);
        optionsArea.setLayout(new GridLayout(6, false));
        if (spec.getOptions() != null) {
            allwriteButton = new Button(optionsArea, SWT.CHECK);
            allwriteButton.setText("allwrite"); //$NON-NLS-1$
            allwriteButton.setSelection(spec.getOptions().isAllWrite());
            clobberButton = new Button(optionsArea, SWT.CHECK);
            clobberButton.setText("clobber"); //$NON-NLS-1$
            clobberButton.setSelection(spec.getOptions().isClobber());
            compressButton = new Button(optionsArea, SWT.CHECK);
            compressButton.setText("compress"); //$NON-NLS-1$
            compressButton.setSelection(spec.getOptions().isCompress());
            lockedButton = new Button(optionsArea, SWT.CHECK);
            lockedButton.setText("locked"); //$NON-NLS-1$
            lockedButton.setSelection(spec.getOptions().isLocked());
            modtimeButton = new Button(optionsArea, SWT.CHECK);
            modtimeButton.setText("modtime"); //$NON-NLS-1$
            modtimeButton.setSelection(spec.getOptions().isModtime());
            rmdirButton = new Button(optionsArea, SWT.CHECK);
            rmdirButton.setText("rmdir"); //$NON-NLS-1$
            rmdirButton.setSelection(spec.getOptions().isRmdir());
        }
        GridData fixedTextData = new GridData(SWT.FILL, SWT.FILL, true, false);
        fixedTextData.horizontalSpan = 3;
        fixedTextData.widthHint=optionsArea.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
        optionsArea.setLayoutData(fixedTextData);

        if (connection.getServer().getServerVersionNumber() >= IP4ServerConstants.SERVERID_VERSION) {
            new Label(parent, SWT.LEFT);
            serverIdArea = new Composite(parent, 0);
            GridLayout layout = new GridLayout(2, false);
            serverIdArea.setLayout(layout);
            serverIdArea.setLayoutData(fixedTextData);
            restrictToServerId = new Button(serverIdArea, SWT.CHECK);
            restrictToServerId
                    .setText(Messages.ClientWidget_RestrictToServerId);
            restrictToServerId.setSelection(StringUtils.isNotEmpty(spec
                    .getServerId()));
            restrictToServerId.setLayoutData(GridDataFactory.swtDefaults()
                    .align(SWT.LEFT, SWT.TOP).create());
            serverIdText = new Text(serverIdArea, SWT.SINGLE | SWT.BORDER);
            serverIdText.setLayoutData(GridDataFactory.swtDefaults()
                    .align(SWT.FILL, SWT.TOP).grab(true, false).indent(5, -1).create());
            SWTUtils.decorate(serverIdText, SWT.LEFT | SWT.TOP);
            SWTUtils.updateDecoration(serverIdText, ValidationStatus.ok());

            if (StringUtils.isNotEmpty(spec.getServerId())) {
                restrictToServerId.setSelection(true);
                serverIdText.setText(spec.getServerId());
            } else {
                restrictToServerId.setSelection(false);
                serverIdText.setText(lookupServerId());
                serverIdText.setEnabled(false);
            }
            restrictToServerId.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    serverIdText.setEnabled(restrictToServerId.getSelection());
                    // give modification listener a chance to disable its error
                    // when serverId Text field is disabled
                    serverIdText.setText(serverIdText.getText());
                }
            });
        }

        viewLabel = new Label(parent, SWT.LEFT);
        viewLabel.setText(Messages.ClientWidget_View);
        viewLabel.setLayoutData(labelData);
        viewText = new Text(parent, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL
                | SWT.H_SCROLL);
        viewText.setLayoutData(largeTextData);
        viewText.setEditable(enableView);
        
        if (connection.getIntVersion() >= IP4ServerConstants.STREAM_VERSION && !StringUtils.isEmpty(spec.getStream())) {
        	updateClientView(spec.getStream());
        }else{
        	if(spec.getClientView() != null) {
	        	String mappings=getViewMapping(spec.getClientView().getEntryList());
	            viewText.setText(mappings);
        	}
        }

    }

    private String getViewMapping(List<? extends IMapEntry> list) {
    	StringBuilder builder = new StringBuilder();
    	if (list != null) {
	        Collections.sort(list,
	                new Comparator<IMapEntry>() {
	
	                    public int compare(IMapEntry o1,
	                            IMapEntry o2) {
	                        return o1.getOrder() - o2.getOrder();
	                    }
	                });
	        for (IMapEntry mapping : list) {
	            builder.append(mapping.toString(" ", true)); //$NON-NLS-1$
	            builder.append("\n"); //$NON-NLS-1$
	        }
    	}
		return builder.toString();
	}

	private void createClientControls(Composite parent, IClient spec) {
        createTopControls(parent, spec);
        createBottomControls(parent, spec);
    }

    /**
     * Create the UI controls in this widget
     * 
     * @param parent
     */
    public void createControl(Composite parent) {
        displayArea = new Composite(parent, SWT.NONE);
        GridLayout daLayout = new GridLayout(4, false);
        displayArea.setLayout(daLayout);
        displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        createClientControls(displayArea, this.spec);
        this.descriptionText.forceFocus();
        
        addListeners();
    }

    /**
     * Update the current client spec from the values currently entered in the
     * fields. Should be called before the widget is disposed.
     */
    public void updateCurrentSpec() {
        IClient latestSpec = new Client();
        latestSpec.setName(this.workspaceText.getText());
        latestSpec.setDescription(this.descriptionText.getText());
        latestSpec.setHostName(this.hostText.getText());
        latestSpec.setRoot(this.rootText.getText());
        String[] altRoots = null; //$NON-NLS-1$
        String altRootText = this.altRootsText.getText().trim();
        if(!StringUtils.isEmpty(altRootText)){
        	altRoots=altRootText.split("\n"); //$NON-NLS-1$
        	P4UIUtils.addIndentToAltRoots(altRoots);
        	latestSpec
        	.setAlternateRoots(new Vector<String>(Arrays.asList(altRoots)));
        }else
        	latestSpec.setAlternateRoots(null);

        latestSpec.setOwnerName(this.ownerText.getText());
        latestSpec.setLineEnd(getSelectedLineEnding());
        latestSpec.setSubmitOptions(getSelectedSubmitOptions());
        latestSpec.setOptions(getSelectedOptions());
        if (connection.getIntVersion() >= IP4ServerConstants.STREAM_VERSION && !StringUtils.isEmpty(getSelectedStream())) {
            latestSpec.setStream(getSelectedStream());
        } else {
            latestSpec.setClientView(getClientView());
        }
        if (connection.getServer().getServerVersionNumber() >= IP4ServerConstants.SERVERID_VERSION){
            if(!StringUtils.isEmpty(streamAtChangeText.getText().trim()))
                latestSpec.setStreamAtChange(Integer.parseInt(streamAtChangeText.getText().trim()));
            latestSpec.setServerId(getServerIdText());
        }
        this.currentSpec = latestSpec;
    }

    /**
     * Get spec created from the last call to {@link #updateCurrentSpec()}
     * 
     * @return - current client spec
     */
    public IClient getCurrentSpec() {
        return this.currentSpec;
    }

    /**
     * Get workspace text
     * 
     * @return - workspace text
     */
    public String getWorkspaceText() {
        return this.workspaceText.getText();
    }

    /**
     * Get description text
     * 
     * @return - description text
     */
    public String getDescriptionText() {
        return this.descriptionText.getText();
    }

    /**
     * Get host text
     * 
     * @return - host text
     */
    public String getHostText() {
        return this.hostText.getText();
    }

    /**
     * Get root text
     * 
     * @return - root text
     */
    public String getRootText() {
        return this.rootText.getText();
    }

    /**
     * Get owner text
     * 
     * @return - owner text
     */
    public String getOwnerText() {
        return this.ownerText.getText();
    }

    /**
     * Get line endings text
     * 
     * @return - line endings text
     */
    public String getLineEndingText() {
        return this.lineCombo.getText();
    }

    /**
     * Get submit options text
     * 
     * @return - submit options text
     */
    public String getSubmitOptionText() {
        return this.submitCombo.getText();
    }

    /**
     * Get currently entered client view
     * 
     * @return - client view text
     */
    public String getViewText() {
        return this.viewText.getText();
    }

    public String getServerIdText() {
        if (connection.getServer().getServerVersionNumber() >= IP4ServerConstants.SERVERID_VERSION){
            if (restrictToServerId == null)
                return null;
            if (!restrictToServerId.getSelection())
                return null;
            return serverIdText.getText();
            }
        return null;
    }

    private String lookupServerId() {
        String serverId = null;
        try {
            serverId = spec.getServer().getServerInfo().getServerId();

        } catch (Exception e) {
        }
        if (serverId == null) {
            serverId = ""; //$NON-NLS-1$
        }
        return serverId;
    }

    private void addListeners() {
        if (connection.getIntVersion() >= IP4ServerConstants.STREAM_VERSION) {
            streamCombo.getModel().addValueChangeListener(new IValueChangeListener() {
                public void handleValueChange(ValueChangeEvent event) {
                    SelectionModel model=(SelectionModel) streamCombo.getModel().getValue();
                    if(model.getSelection()!=null){
                        viewText.setText(Messages.ClientWidget_Refreshing);
                        viewText.setEditable(false);
                        if(model.getSelection() instanceof IStreamSummary){
                        	String sum=((IStreamSummary) model.getSelection()).getStream();
                        	updateClientView(sum);
                        }
                    }else{
                    	viewText.setText(IConstants.EMPTY_STRING);
                        viewText.setEditable(true);
                    }
                }
            });
        }

        if (connection.getServer().getServerVersionNumber() >= IP4ServerConstants.SERVERID_VERSION){
            this.streamAtChangeText.addModifyListener(new ModifyListener() {
                public void modifyText(ModifyEvent e) {
                    validate();
                }
            });
            if (serverIdText != null) {
                serverIdText.addModifyListener(new ModifyListener() {
                    public void modifyText(ModifyEvent e) {
                        validate();
                    }
                });
            }
        }
    }

    protected void updateClientView(final String sum) {
    	P4Runner.schedule(new P4Runnable() {
    		
    		@Override
    		public String getTitle() {
    			return Messages.ClientWidget_LoadingStreamView;
    		}
    		
    		@Override
    		public void run(IProgressMonitor monitor) {
    			try {
    				IOptionsServer server=(IOptionsServer) connection.getServer();
    				GetStreamOptions opt = new GetStreamOptions(true);
    				opt.setOptions("-o"); //$NON-NLS-1$
    				IStream stream = server.getStream(sum, opt);
    				
    				ViewMap<IClientViewMapping> view = stream.getClientView();
    				if (view != null) {
    					final String mappings=getViewMapping(view.getEntryList());
    					PerforceUIPlugin.asyncExec(new Runnable() {
    						public void run() {
    							viewText.setText(mappings);
    						}
    					});
    				}
    			} catch (Exception e) {
    				e.printStackTrace();
    				viewText.setText(IConstants.EMPTY_STRING);
    			}
    		}
    		
    	});
	}

	public void validate() {
        if (connection.getServer().getServerVersionNumber() >= IP4ServerConstants.SERVERID_VERSION){
            IStatus status=ValidationStatus.ok();
            if (serverIdText.getEnabled() == true) {
                String name = serverIdText.getText();
                String error = P4UIUtils.validateName(name, "Server"); //$NON-NLS-1$
    
                if (error != null) {
                    SWTUtils.updateDecoration(serverIdText, ValidationStatus.error(error));
                    errorDisplay.setErrorMessage(Messages.ClientWidget_IllegalServerName, ClientWidget.this);
                    return;
                }
            }
            SWTUtils.updateDecoration(serverIdText, status);

            try {
                if(!StringUtils.isEmpty(streamAtChangeText.getText().trim())){
                    int num=Integer.parseInt(streamAtChangeText.getText().trim());
                    if(num<0){
                        errorDisplay.setErrorMessage(Messages.ClientWidget_AtChangeMustBeInteger, this);
                        return;
                    }
                }
            } catch (Throwable t) {
                errorDisplay.setErrorMessage(Messages.ClientWidget_AtChangeMustBeInteger, this);
                return;
            }
        }

        errorDisplay.setErrorMessage(null,this);

    }

    public void setErrorDisplay(IErrorDisplay display) {
        this.errorDisplay=display;
    }

    public String getErrorMessage() {
        return null;
    }


}
