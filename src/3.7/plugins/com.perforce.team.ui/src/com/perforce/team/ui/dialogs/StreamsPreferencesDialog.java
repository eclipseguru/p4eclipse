package com.perforce.team.ui.dialogs;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.preferences.RetrievePreferencePage;

/**
 * Streams preferences dialog
 * 
 * @author ali
 */
public class StreamsPreferencesDialog extends RetrievePreferencePage {

    public static final int SHOW_NAME_ROOT=0; // default
    public static final int SHOW_NAME_ONLY=1;
    public static final int SHOW_ROOT_ONLY=2;

    /**
     * ID
     */
    public static final String ID = "com.perforce.team.ui.dialogs.StreamsPreferencesDialog"; //$NON-NLS-1$

    // Numbers of columns in GridLayout
    private static final int NUM_COLS = 1;

    private Button nameAndRootRadio;

    private Button nameRadio;

    private Button rootRadio;
	private Button diffWorkspaceRadio;
	private Button sameWorkspaceRadio;
	private Button notPromptSwitch;

    /**
     * Constructor.
     */
    public StreamsPreferencesDialog() {
        super(NUM_COLS);

        IPreferenceStore store = getPreferenceStore();
        if(StringUtils.isEmpty(store.getString(IPerforceUIConstants.PREF_CLIENT_SWITCH_ON_STREAM_OPERATOIN)))
        	store.setDefault(IPerforceUIConstants.PREF_CLIENT_SWITCH_ON_STREAM_OPERATOIN, IPerforceUIConstants.STREAM_SWITCH_STREAM_WITH_SAME_CLIENT);

    }

    /**
     * Create dialog controls
     * 
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        Composite displayArea = new Composite(parent, SWT.NONE);
        GridLayout daLayout = new GridLayout(1, true);
        daLayout.marginHeight = 0;
        daLayout.marginWidth = 0;
        displayArea.setLayout(daLayout);
        displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createSwitchArea(displayArea);
        
        super.createRetrieveArea(displayArea);
        
        createStreamDisplayArea(displayArea);

        PlatformUI
                .getWorkbench()
                .getHelpSystem()
                .setHelp(allElementsRadio,
                        IHelpContextIds.PREF_ALL_STREAMS_RADIO);
        PlatformUI
                .getWorkbench()
                .getHelpSystem()
                .setHelp(maxElementsRadio,
                        IHelpContextIds.PREF_MAX_STREAMS_RADIO);
        PlatformUI
                .getWorkbench()
                .getHelpSystem()
                .setHelp(maxElementsText,
                        IHelpContextIds.PREF_MAX_STREAMS_TEXT);
        

        return displayArea;
    }

    private Composite createSwitchArea(Composite parent) {
        Group group = DialogUtils.createGroup(parent, Messages.StreamsPreferencesDialog_SwitchDesc, 1);
        
        int buttonStyle = P4CoreUtils.isWindows() ? SWT.NO_FOCUS : SWT.NONE;
        
        sameWorkspaceRadio = DialogUtils.createRadio(group, buttonStyle,Messages.StreamsPreferencesDialog_AutoSwitch);
        diffWorkspaceRadio = DialogUtils.createRadio(group, buttonStyle,Messages.StreamsPreferencesDialog_ManualSwitch);
        notPromptSwitch = DialogUtils.createCheck(group, Messages.StreamsPreferencesDialog_NotWarnSwitch);
        GridData gd = new GridData(SWT.BEGINNING, SWT.CENTER, true, false);
        gd.horizontalIndent+=20;
        notPromptSwitch.setLayoutData(gd);
        sameWorkspaceRadio.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				if(sameWorkspaceRadio.getSelection()){
					notPromptSwitch.setSelection(false);
				}
				notPromptSwitch.setEnabled(!sameWorkspaceRadio.getSelection());
			}
        });
        Link link=new Link(group, SWT.NONE);
        link.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false));
        link.setText(Messages.StreamsPreferencesDialog_WorkspaceLink);
        link.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	PreferencesUtil.createPreferenceDialogOn(getShell(), ClientPreferencesDialog.ID, null, ""); //$NON-NLS-1$
            }
        });

        initSwitchDisplayValues(getPrefInt(getStreamDisplayPref()));
        
        return group;
    }

	private void initSwitchDisplayValues(int prefInt) {
		String opt=getPrefString(IPerforceUIConstants.PREF_CLIENT_SWITCH_ON_STREAM_OPERATOIN);
		if(StringUtils.isEmpty(opt)){
			opt=IPerforceUIConstants.STREAM_SWITCH_STREAM_WITH_SAME_CLIENT;
		}
		
		if(IPerforceUIConstants.STREAM_SWITCH_STREAM_WITH_SAME_CLIENT.equals(opt)){
			sameWorkspaceRadio.setSelection(true);
		}else if(IPerforceUIConstants.STREAM_SWITCH_STREAM_WITH_DIFF_CLIENT.equals(opt)){
			diffWorkspaceRadio.setSelection(true);
		}
		
		boolean noWarnSWitch=getPrefBoolean(IPerforceUIConstants.PREF_CLIENT_SWITCH_NO_WARN);
		notPromptSwitch.setSelection(noWarnSWitch);
		notPromptSwitch.setEnabled(diffWorkspaceRadio.getSelection());
	}

	private Composite createStreamDisplayArea(Composite parent) {
        Group group = DialogUtils.createGroup(parent, Messages.StreamsPreferencesDialog_DisplayStreamAs, 1);
        
        int buttonStyle = P4CoreUtils.isWindows() ? SWT.NO_FOCUS : SWT.NONE;
        
        nameAndRootRadio = DialogUtils.createRadio(group, buttonStyle,Messages.StreamsPreferencesDialog_NameAndRoot);
        nameRadio = DialogUtils.createRadio(group, buttonStyle,Messages.StreamsPreferencesDialog_NameOnly);
        rootRadio = DialogUtils.createRadio(group, buttonStyle,Messages.StreamsPreferencesDialog_RootOnly);

        initStreamDisplayValues(getPrefInt(getStreamDisplayPref()));
        
        return group;
        
    }

    private void initStreamDisplayValues(int num) {
        nameRadio.setSelection(false);
        rootRadio.setSelection(false);
        nameAndRootRadio.setSelection(false);
        
        switch (num){
        case SHOW_NAME_ONLY:
            nameRadio.setSelection(true);
            break;
        case SHOW_ROOT_ONLY:
            rootRadio.setSelection(true);
            break;
        case SHOW_NAME_ROOT:
        default:
            nameAndRootRadio.setSelection(true);
        }
    }

    /**
     * @see com.perforce.team.ui.preferences.RetrievePreferencePage#getName()
     */
    @Override
    protected String getName() {
        return Messages.StreamsPreferenceDialog_Streams; //$NON-NLS-1$
    }
    
    /**
     * @see com.perforce.team.ui.preferences.RetrievePreferencePage#getRetrievePref()
     */
    @Override
    protected String getRetrievePref() {
        return IPerforceUIConstants.PREF_RETRIEVE_NUM_STREAMS;
    }
    
    protected String getStreamDisplayPref() {
        return IPerforceUIConstants.PREF_STREAM_DISPLAY;
    }

    @Override
    public boolean performOk() {
        int num;
        if (nameAndRootRadio.getSelection()) {
            num = SHOW_NAME_ROOT;
        } else if(nameRadio.getSelection()){
            num = SHOW_NAME_ONLY;
        } else if (rootRadio.getSelection()){
            num = SHOW_ROOT_ONLY;
        } else {
            num = SHOW_NAME_ROOT;
        }
        setPrefInt(getStreamDisplayPref(), num);

        String switchOpt=IPerforceUIConstants.STREAM_SWITCH_STREAM_WITH_SAME_CLIENT;
        if(diffWorkspaceRadio.getSelection()){
        	switchOpt=IPerforceUIConstants.STREAM_SWITCH_STREAM_WITH_DIFF_CLIENT;
        }
        setPrefString(IPerforceUIConstants.PREF_CLIENT_SWITCH_ON_STREAM_OPERATOIN, switchOpt);
        setPrefBoolean(IPerforceUIConstants.PREF_CLIENT_SWITCH_NO_WARN, notPromptSwitch.getSelection());
        return super.performOk();
    }

    /**
     * Restore defaults
     */
    @Override
    protected void performDefaults() {
        super.performDefaults();
        diffWorkspaceRadio.setSelection(false);
        sameWorkspaceRadio.setSelection(true);
        notPromptSwitch.setEnabled(false);
        notPromptSwitch.setSelection(false);
        initStreamDisplayValues(getPrefDefInt(getStreamDisplayPref()));
    }


}
