package com.perforce.team.ui.views;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IBundleGroup;
import org.eclipse.core.runtime.IBundleGroupProvider;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.branding.IBundleGroupConstants;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

import com.perforce.team.core.P4CommandCallback;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4CommandListener;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Listener;
import com.perforce.team.core.p4java.P4Command;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.dialogs.ConsolePreferencesDialog;
import com.perforce.team.ui.dialogs.IHelpContextIds;

/**
 * Perforce console view
 */

public class ConsoleView extends ViewPart implements IPropertyChangeListener {
	final static int OUPUT_LIMIT=100;

    /**
     * SHOW_INPUT
     */
    public static final String SHOW_INPUT = "com.perforce.team.ui.views.console.SHOW_INPUT"; //$NON-NLS-1$

    /**
     * SHOW_PROGRESS
     */
    public static final String SHOW_PROGRESS = "com.perforce.team.ui.views.console.SHOW_PROGRESS"; //$NON-NLS-1$

    /**
     * VERSION_COMMAND
     */
    public static final String VERSION_COMMAND = "version"; //$NON-NLS-1$

    /**
     * VERSION_BUNDLE_GROUP_ID
     */
    public static final String VERSION_BUNDLE_GROUP_ID = "com.perforce.team"; //$NON-NLS-1$

    /**
     * VERSION_START
     */
    public static final String VERSION_START = Messages.ConsoleView_Version;

    /**
     * VERSION_UNKNOWN
     */
    public static final String VERSION_UNKNOWN = Messages.ConsoleView_VersionUnknown;

    private Composite displayArea;

    // Input area
    private Action showInput;
    private Composite inputArea;
    private Label connectionLabel;
    private Combo connectionCombo;
    private ToolBar connectionToolbar;
    private ToolItem refreshConnectionAction;
    private Image refreshImage;
    private Label inputLabel;
    private Text inputText;
    private Button executeButton;
    private Button refreshButton;

    // Progress area
    private Action showProgress;
    private Composite progressArea;
    private Label progressLabel;
    private ProgressBar progressBar;
    private Button cancelBtn;

    // Viewer and document
    private TextViewer viewer;
    private ConsoleDocument document;

    // Current colours
    private Color commandColour;
    private Color messageColour;
    private Color errorColour;

    // hide large output
	private boolean hideLargeOutput=getPreferenceStore().getBoolean(IPerforceUIConstants.PREF_CONSOLE_COMMAND_OUPUT_HIDE_LARGE);;

    // Current font
    private Font consoleFont;

    private String version;

    private List<String> previousCommands = new ArrayList<String>();
    private int commandIndex = 0;

    /**
     * ID for this view
     */
    public static final String VIEW_ID = "com.perforce.team.ui.ConsoleView"; //$NON-NLS-1$

    // Support for select and copy
    private ConsoleAction copyAction;
    private ConsoleAction selectAllAction;

    private IP4CommandListener commandListener = new IP4CommandListener() {

        public void info(int id, String line) {
            if (line != null) {
                outputWarning(line);
            }
        }

        public void error(int id, String line) {
            if (line != null) {
                outputError(line);
            }
        }

        public void command(int id, String line) {
            executed(line, new String[0]);
        }

    };

    private IP4Listener connectionListener = new IP4Listener() {

        public void resoureChanged(P4Event event) {
            if (event.getConnections().length > 0) {
                UIJob job = new UIJob(
                        Messages.ConsoleView_RefreshingLogConsoleConnections) {

                    @Override
                    public IStatus runInUIThread(IProgressMonitor monitor) {
                        if (!isDisposed()) {
                            refreshConnections();
                        }
                        return Status.OK_STATUS;
                    }

                };
                job.setSystem(true);
                job.schedule();
            }
        }
		public String getName() {
			return ConsoleView.this.getClass().getSimpleName();
		}
    };

    /**
     * Initialise the view
     * 
     * @param parent
     */
    @Override
    public void createPartControl(Composite parent) {
        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(parent, IHelpContextIds.CONSOLE_VIEW);

        displayArea = new Composite(parent, SWT.NONE);
        GridLayout daLayout = new GridLayout(1, true);
        daLayout.marginWidth = 0;
        daLayout.marginHeight = 0;
        displayArea.setLayout(daLayout);
        displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        // Create viewer and document
        viewer = new TextViewer(displayArea, SWT.H_SCROLL | SWT.V_SCROLL);
        viewer.getTextWidget().setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, true, true));
        viewer.setEditable(false);
        document = new ConsoleDocument();
        viewer.setDocument(document);

        // Keep last line displayed
        document.addDocumentListener(new IDocumentListener() {

            public void documentAboutToBeChanged(DocumentEvent event) {
            }

            public void documentChanged(DocumentEvent event) {
                if (viewer.getControl() != null) {
                    StyledText text = viewer.getTextWidget();
                    text.setTopIndex(text.getLineCount());
                }
            }
        });

        // add a selection listener to control enablement of the copy action
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                copyAction.update();
            }
        });

        // Handle the colouring of the different line types
        viewer.getTextWidget().addLineStyleListener(new LineStyleListener() {

            public void lineGetStyle(LineStyleEvent event) {
                StyleRange style = new StyleRange(event.lineOffset,
                        event.lineText.length(),
                        getLineColour(event.lineOffset), null);
                event.styles = new StyleRange[] { style };
            }
        });

        // Get the current colours, font etc
        updatePreferences(null);

        addContextMenu();

        inputArea = new Composite(displayArea, SWT.NONE);
        inputArea.setLayout(new GridLayout(4, false));
        inputArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        connectionLabel = new Label(inputArea, SWT.LEFT);
        connectionLabel.setText(Messages.ConsoleView_Connection);

        Composite connectionArea = new Composite(inputArea, SWT.NONE);
        GridLayout caLayout = new GridLayout(2, false);
        caLayout.marginWidth = 0;
        caLayout.marginHeight = 0;
        connectionArea.setLayout(caLayout);
        GridData caData = new GridData(SWT.FILL, SWT.FILL, true, false);
        caData.horizontalSpan = 3;
        connectionArea.setLayoutData(caData);
        connectionCombo = new Combo(connectionArea, SWT.READ_ONLY
                | SWT.DROP_DOWN);
        connectionCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                false));

        connectionCombo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                inputText.setEnabled(true);
                executeButton.setEnabled(true);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                super.widgetSelected(e);
            }

        });

        connectionToolbar = new ToolBar(connectionArea, SWT.FLAT);
        refreshConnectionAction = new ToolItem(connectionToolbar, SWT.PUSH);
        refreshConnectionAction.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                refreshConnections();
            }

        });
        refreshConnectionAction
                .setToolTipText(Messages.ConsoleView_RefreshConnections);

        refreshImage = PerforceUIPlugin.getPlugin()
                .getImageDescriptor(IPerforceUIConstants.IMG_REFRESH)
                .createImage();
        refreshConnectionAction.setImage(refreshImage);

        inputLabel = new Label(inputArea, SWT.LEFT);
        inputLabel.setText(Messages.ConsoleView_Command);
        inputText = new Text(inputArea, SWT.BORDER | SWT.SINGLE);
        inputText
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        inputText.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.character == '\n' || e.character == '\r') {
                    execute();
                } else if (e.keyCode == SWT.ARROW_UP) {
                    if (commandIndex < previousCommands.size() - 1) {
                        commandIndex++;
                    }
                    try {
                        String oldCommand = previousCommands.get(commandIndex);
                        inputText.setText(oldCommand);
                    } catch (IndexOutOfBoundsException e1) {
                    }
                } else if (e.keyCode == SWT.ARROW_DOWN) {
                    if (commandIndex > 0) {
                        commandIndex--;
                    }
                    try {
                        String newCommand = previousCommands.get(commandIndex);
                        inputText.setText(newCommand);
                    } catch (IndexOutOfBoundsException e1) {
                    }
                }
            }

        });
        executeButton = new Button(inputArea, SWT.PUSH);
        executeButton.setText(Messages.ConsoleView_Run);
        executeButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                execute();
            }

        });
        refreshButton = new Button(inputArea, SWT.CHECK);
        refreshButton.setText(Messages.ConsoleView_RefreshChangedResources);
        refreshButton.setSelection(true);
        showInputArea(showInput.isChecked());

        progressArea = new Composite(displayArea, SWT.NONE);
        progressArea.setLayout(new GridLayout(4, false));
        progressArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        progressLabel = new Label(progressArea, SWT.LEFT);
	    progressBar=new ProgressBar(progressArea, SWT.FLAT|SWT.INDETERMINATE);//SWT.FLAT|SWT.HORIZONTAL|SWT.SMOOTH);
	    progressBar.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false,2,1));
	    cancelBtn = new Button(progressArea, SWT.PUSH);
	    cancelBtn.setText("Cancel");
        cancelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // TODO : cancel the command.
            }

        });
//        showProgress.setChecked(true);
        showProgressArea(false);//showProgress.isChecked());

        refreshConnections();

        // Add the various listeners
        getPreferenceStore().addPropertyChangeListener(this);
        JFaceResources.getFontRegistry().addListener(this);

        P4ConnectionManager.getManager().addCommandListener(commandListener);
        P4ConnectionManager.getManager().addListener(connectionListener);
    }

    private void execute() {
        String command = inputText.getText();
        previousCommands.add(0, command);
        commandIndex = -1;
        if (previousCommands.size() > 10) {
            previousCommands.remove(10);
        }
        inputText.setText(""); //$NON-NLS-1$
        updateProgressLabel(command);
        executeCommand(command);
    }

    private void updateProgressLabel(String command) {
        progressLabel.setText(command);
        progressArea.layout();
	}

	private String getVersion() {
        if (version == null) {
            String aboutText = null;
            IBundleGroupProvider[] providers = Platform
                    .getBundleGroupProviders();
            for (IBundleGroupProvider provider : providers) {
                for (IBundleGroup group : provider.getBundleGroups()) {
                    if (VERSION_BUNDLE_GROUP_ID.equals(group.getIdentifier())) {
                        aboutText = group
                                .getProperty(IBundleGroupConstants.ABOUT_TEXT);
                        if (aboutText != null) {
                            int start = aboutText.indexOf(VERSION_START);
                            if (start != -1) {
                                int end = aboutText.indexOf('\n', start);
                                if (end != -1) {
                                    aboutText = aboutText.substring(start, end);
                                }
                            }
                        }
                        break;
                    }
                }
            }
            if (aboutText == null) {
                aboutText = VERSION_UNKNOWN;
            }
            version = aboutText;
        }
        return version;
    }

    private void printVersion() {
        String productVersion = getVersion();
        executed(VERSION_COMMAND);
        if (productVersion != null) {
            appendLine(ConsoleDocument.MESSAGE, productVersion);
        }
    }

    private void executeCommand(String command) {
        if (command != null) {
            if (VERSION_COMMAND.equals(command)) {
                printVersion();
                return;
            }
            Object connection = connectionCombo.getData(connectionCombo
                    .getText());
            if (connection instanceof IP4Connection) {
                IP4Connection p4Connection = (IP4Connection) connection;
                P4Command p4Command = new P4Command(p4Connection, command);
                p4Command.run(refreshButton.getSelection(),
                        new P4CommandCallback() {

                            @Override
                            public void callback(List<Map<String, Object>> data) {
                                if (!isDisposed() && data !=null) {
                                	if(!hideLargeOutput){
                                		print(data);
                                	}else if(OUPUT_LIMIT<data.size()){
										List<Map<String, Object>> omit=new ArrayList<Map<String,Object>>();//new HashMap[1];
										omit.add(new HashMap<String, Object>());
										omit.get(0).put("Output","too long, omitted.");
										print(omit);
                                	}else{
                                		print(data);
                                	}
                                }
                            }

                            @Override
                            public void callbackError(List<Map<String, Object>> data) {
                                if (!isDisposed() && data !=null) {
                                    print(data, true);
                                }
                            }

                        });
            }
        }
    }

    private void refreshConnections() {
        connectionCombo.removeAll();
        for (IP4Connection connection : P4ConnectionManager.getManager()
                .getConnections()) {
            connectionCombo.add(connection.toString());
            connectionCombo.setData(connection.toString(), connection);
        }
        if (connectionCombo.getItemCount() > 0) {
            connectionCombo.select(0);
            inputText.setEnabled(true);
            executeButton.setEnabled(true);
        } else {
            inputText.setEnabled(false);
            executeButton.setEnabled(false);
        }
    }

    private void showInputArea(boolean show) {
        GridData data = (GridData) inputArea.getLayoutData();
        data.exclude = !show;
        inputArea.setVisible(show);
        displayArea.layout(true, true);
    }

    private void showProgressArea(boolean show) {
        GridData data = (GridData) progressArea.getLayoutData();
        data.exclude = !show;
        progressArea.setVisible(show);
        displayArea.layout(true, true);
    }

    /**
     * Prints an array of maps line by line as key value pairs. Will be
     * displayed as a warning message type.
     * 
     * @param output
     */
    public void print(List<Map<String, Object>> output) {
        print(output, false);
    }

    /**
     * Prints an array of maps line by line as key value pairs
     * 
     * @param output
     * @param error
     *            - true to colorize as an error, false as warning
     */
    public void print(List<Map<String, Object>> output, boolean error) {
        if (output != null) {
            for (Map<String, Object> entry : output) {
                if (entry != null) {
                    for (Map.Entry<String, Object> mapEntry : entry.entrySet()) {
                    	String key = mapEntry.getKey();
                        Object value = mapEntry.getValue();
                        if (key!=null && value != null) {
                            StringBuffer formatted = new StringBuffer();
                            formatted.append(key);
                            formatted.append(": "); //$NON-NLS-1$
                            formatted.append(value);
                            if (!isDisposed()) {
                                if (error) {
                                    outputError(formatted.toString());
                                } else {
                                    outputWarning(formatted.toString());
                                }
                            } else {
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Shutdown view
     */
    @Override
    public void dispose() {
        if (commandColour != null) {
            commandColour.dispose();
            commandColour = null;
        }
        if (messageColour != null) {
            messageColour.dispose();
            messageColour = null;
        }
        if (errorColour != null) {
            errorColour.dispose();
            errorColour = null;
        }
        if (refreshImage != null && refreshImage.isDisposed()) {
            refreshImage.dispose();
            refreshImage = null;
        }

        getPreferenceStore().removePropertyChangeListener(this);
        JFaceResources.getFontRegistry().removeListener(this);
        P4ConnectionManager.getManager().removeListener(connectionListener);
        P4ConnectionManager.getManager().removeCommandListener(commandListener);
    }

    /**
     * Handle property change event
     * 
     * @param event
     */
    public void propertyChange(PropertyChangeEvent event) {
        String property = event.getProperty();
        // When user changes font we get two signals one from the FontRegistry
        // and one from the PreferenceStore. We need to ignore the one from the
        // PreferenceStore as this causes problems
        if (!property.equals(IPerforceUIConstants.PREF_CONSOLE_FONT)
                || event.getSource() instanceof FontRegistry) {
            updatePreferences(property);
        }
    }

    /**
     * Gets the console view
     * 
     * @return - console view
     */
    public static ConsoleView getView() {
        return (ConsoleView) PerforceUIPlugin.getActivePage().findView(VIEW_ID);
    }

    /**
     * Show this view in the current perspective
     * 
     * @return - console view
     */
    public static ConsoleView openInActivePerspective() {
        try {
            return (ConsoleView) PerforceUIPlugin.getActivePage().showView(
                    VIEW_ID);
        } catch (PartInitException pe) {
            PerforceProviderPlugin.logError(pe);
            return null;
        }
    }

    /**
     * Return the colour for a particular line
     * 
     * @param offset
     * @return - line color
     */
    public Color getLineColour(int offset) {
        int type = document.getLineType(offset);
        if (type == ConsoleDocument.COMMAND) {
            return commandColour;
        } else if (type == ConsoleDocument.ERROR) {
            return errorColour;
        } else {
            return messageColour;
        }
    }

    /**
     * Log a command executed to the console view
     * 
     * @param cmd
     * @param args
     */
    public void executed(String cmd, String[] args) {
        String line = "p4 " + cmd; //$NON-NLS-1$
        for (int i = 0; i < args.length; i++) {
            line = line + " " + args[i]; //$NON-NLS-1$
        }
        executed(line);
    }

    /**
     * Handles execution of a line
     * 
     * @param line
     */
    public void executed(String line) {
        appendLine(ConsoleDocument.COMMAND,
                MessageFormat.format(Messages.ConsoleView_Executing, line),
                true);
    }

    /**
     * Output a warning to the console view
     * 
     * @param warning
     */
    public void outputWarning(String warning) {
        appendLine(ConsoleDocument.MESSAGE, warning.replace('\n', ' '));
    }

    /**
     * Output a warning to the console view
     * 
     * @param info
     */
    public void outputInfo(String msg) {
        appendLine(ConsoleDocument.MESSAGE, msg.replace('\n', ' '));
    }

    /**
     * Output an error to the console view
     * 
     * @param error
     */
    public void outputError(String error) {
        appendLine(ConsoleDocument.ERROR, error.replace('\n', ' '));
    }

    /**
     * Handle set focus event
     */
    @Override
    public void setFocus() {
        viewer.getTextWidget().setFocus();
    }

    /**
     * Append a line to the console
     * 
     * @param type
     * @param line
     */
    public void appendLine(final int type, final String line) {
        appendLine(type, line, false);
    }

    private boolean showTimestamp() {
        return PerforceUIPlugin.getPlugin().getPreferenceStore()
                .getBoolean(IPerforceUIConstants.PREF_CONSOLE_TIMESTAMP);
    }

    /**
     * Append a line to the console
     * 
     * @param type
     * @param line
     * @param stamp
     */
    public void appendLine(final int type, final String line,
            final boolean stamp) {
        Display display = Display.getCurrent();
        if (display == null) {
            display = Display.getDefault();
        }
        display.asyncExec(new Runnable() {

            public void run() {
                if (!isDisposed()) {
                    String timestamp = ""; //$NON-NLS-1$
                    if (stamp && showTimestamp()) {
                        Calendar cal = Calendar.getInstance();
                        timestamp = String
                                .format("%1$tI:%1$tM %1$tp ", cal).toUpperCase(); //$NON-NLS-1$
                    }
                    document.appendLine(type, timestamp + line);
                }
            }
        });
    }

    /**
     * Create a colour from preference string
     */
    private Color createColor(String preference) {
        RGB rgb = PreferenceConverter
                .getColor(getPreferenceStore(), preference);
        Display display = getViewSite().getShell().getDisplay();
        return new Color(display, rgb);
    }

    /**
     * Get the preference store for
     */
    private IPreferenceStore getPreferenceStore() {
        return PerforceUIPlugin.getPlugin().getPreferenceStore();
    }

    /**
     * Get the latest preference settings and update console if neccessary
     */
    private void updatePreferences(String property) {
        // update the console colors
        if (property == null
                || property
                        .equals(IPerforceUIConstants.PREF_CONSOLE_COMMAND_COLOUR)
                || property
                        .equals(IPerforceUIConstants.PREF_CONSOLE_MESSAGE_COLOUR)
                || property
                        .equals(IPerforceUIConstants.PREF_CONSOLE_ERROR_COLOUR)) {
            Color oldCommandColour = commandColour;
            Color oldMessageColour = messageColour;
            Color oldErrorColour = errorColour;
            commandColour = createColor(IPerforceUIConstants.PREF_CONSOLE_COMMAND_COLOUR);
            messageColour = createColor(IPerforceUIConstants.PREF_CONSOLE_MESSAGE_COLOUR);
            errorColour = createColor(IPerforceUIConstants.PREF_CONSOLE_ERROR_COLOUR);

            if (oldCommandColour != null) {
                if (viewer != null && !viewer.getControl().isDisposed()) {
                    viewer.refresh();
                }
                oldCommandColour.dispose();
                oldMessageColour.dispose();
                oldErrorColour.dispose();
            }
        }

        if(IPerforceUIConstants.PREF_CONSOLE_COMMAND_OUPUT_HIDE_LARGE.equals(property)){
        	hideLargeOutput=getPreferenceStore().getBoolean(IPerforceUIConstants.PREF_CONSOLE_COMMAND_OUPUT_HIDE_LARGE);
        }
        
        // update the console font
        if (property == null
                || property.equals(IPerforceUIConstants.PREF_CONSOLE_FONT)) {
            Font oldConsoleFont = consoleFont;
            consoleFont = JFaceResources
                    .getFont(IPerforceUIConstants.PREF_CONSOLE_FONT);
            if (viewer != null && !viewer.getControl().isDisposed()) {
                viewer.getTextWidget().setFont(consoleFont);
            }
            if (oldConsoleFont != null) {
                oldConsoleFont.dispose();
            }
        }
    }

    /**
     * Create context menu and tool bar
     */
    private void addContextMenu() {
        PerforceUIPlugin plugin = PerforceUIPlugin.getPlugin();
        final Action clearAction = new Action(
                Messages.ConsoleView_Clear,
                plugin.getImageDescriptor(IPerforceUIConstants.IMG_CLEAR_ENABLED)) {

            @Override
            public void run() {
                document.clear();
            }
        };
        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(clearAction, IHelpContextIds.CONSOLE_CLEAR);

        final Action openPrefs = new Action(
                Messages.ConsoleView_OpenConsolePreferences,
                plugin.getImageDescriptor(IPerforceUIConstants.IMG_PREFERENCES)) {

            @Override
            public void run() {
                P4UIUtils.openPreferencePage(ConsolePreferencesDialog.ID);
            }
        };

        clearAction.setToolTipText(Messages.ConsoleView_ClearConsole);
        clearAction.setDisabledImageDescriptor(plugin
                .getImageDescriptor(IPerforceUIConstants.IMG_CLEAR_DISABLED));
        clearAction.setHoverImageDescriptor(plugin
                .getImageDescriptor(IPerforceUIConstants.IMG_CLEAR));

        copyAction = new ConsoleAction(viewer, ITextOperationTarget.COPY,
                Messages.ConsoleView_Copy);
        selectAllAction = new ConsoleAction(viewer,
                ITextOperationTarget.SELECT_ALL, Messages.ConsoleView_SelectAll);

        showInput = new Action(Messages.ConsoleView_ShowInputArea,
                Action.AS_CHECK_BOX) {

            @Override
            public void run() {
                PerforceUIPlugin.getPlugin().getPreferenceStore()
                        .setValue(SHOW_INPUT, showInput.isChecked());
                showInputArea(showInput.isChecked());
            }
        };
        showInput.setImageDescriptor(plugin
                .getImageDescriptor(IPerforceUIConstants.IMG_INTERACTIVE));
        showInput
                .setChecked(plugin.getPreferenceStore().getBoolean(SHOW_INPUT));

        showProgress = new Action("Show progress", 
                Action.AS_CHECK_BOX) {

            @Override
            public void run() {
                PerforceUIPlugin.getPlugin().getPreferenceStore()
                        .setValue(SHOW_PROGRESS, showProgress.isChecked());
                showProgressArea(showProgress.isChecked());
            }
        };
        showProgress.setImageDescriptor(plugin
                .getImageDescriptor(IPerforceUIConstants.IMG_INTERACTIVE));
        showProgress
                .setChecked(plugin.getPreferenceStore().getBoolean(SHOW_INPUT));

        MenuManager manager = new MenuManager();
        manager.addMenuListener(new IMenuListener() {

            public void menuAboutToShow(IMenuManager manager) {
                manager.add(copyAction);
                manager.add(selectAllAction);
                manager.add(new Separator());
                manager.add(clearAction);
            }
        });
        manager.setRemoveAllWhenShown(true);
        Menu menu = manager.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);

        // Create the local tool bar
        IToolBarManager tbm = getViewSite().getActionBars().getToolBarManager();
        tbm.add(clearAction);
        tbm.add(showInput);
        tbm.add(openPrefs);
        tbm.update(false);

        // Create actions for the text editor
        IActionBars actionBars = getViewSite().getActionBars();
        actionBars.setGlobalActionHandler(ITextEditorActionConstants.COPY,
                copyAction);
        actionBars.setGlobalActionHandler(
                ITextEditorActionConstants.SELECT_ALL, selectAllAction);

        actionBars.updateActionBars();
    }

    /**
     * Gets the text viewer
     * 
     * @return - text viewer
     */
    public TextViewer getViewer() {
        return this.viewer;
    }

    /**
     * Is the view disposed?
     * 
     * @return - true if disposed
     */
    public boolean isDisposed() {
        return this.displayArea == null || this.displayArea.isDisposed();
    }

}
