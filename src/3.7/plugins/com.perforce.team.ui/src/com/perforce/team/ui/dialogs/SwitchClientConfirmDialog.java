package com.perforce.team.ui.dialogs;

import java.text.MessageFormat;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;

public class SwitchClientConfirmDialog extends MessageDialog{

    /**
     * SYNC_SECTION
     */
    public static final String SYNC_CLIENT_SECTION = "SyncClient"; //$NON-NLS-1$

    /**
     * SYNC_KEY
     */
    public static final String SYNC_KEY = "HOW_TO_SYNC"; //$NON-NLS-1$

	private Button noSyncButton;
	private Button syncButton;
	private Button noPromptButton;
	private String syncOpt;

	public SwitchClientConfirmDialog(Shell parentShell) {
		super(parentShell, Messages.SwitchClientDialog_Title, null, 
				MessageFormat.format(Messages.SwitchClientDialog_Description,Messages.SwitchClientDialog_DefaultDescription),
				CONFIRM, new String[] { IDialogConstants.OK_LABEL/*,IDialogConstants.CANCEL_LABEL*/ }, 0);
	}

	public void setDescription(String desc){
		this.message=MessageFormat.format(Messages.SwitchClientDialog_Description,desc);
	}
	
	@Override
	protected Control createCustomArea(Composite parent) {

		Composite displayArea = new Composite(parent, SWT.NONE);
		displayArea.setLayout(new GridLayout());
		displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
        noSyncButton = DialogUtils.createRadio(displayArea, SWT.NONE,Messages.SwitchClientDialog_NoSync);
        syncButton = DialogUtils.createRadio(displayArea, SWT.NONE,Messages.SwitchClientDialog_Sync);

        noPromptButton = DialogUtils.createCheck(displayArea, Messages.SwitchClientDialog_NoPrompot);
        
        Link link=new Link(displayArea, SWT.NONE);
        link.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false));
        link.setText(Messages.SwitchClientDialog_ReactivatePromptTip);
        link.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	PreferenceDialog dlg = PreferencesUtil.createPreferenceDialogOn(getShell(), ClientPreferencesDialog.ID, null, ""); //$NON-NLS-1$
            	dlg.open();
            }
        });
        
        loadDialogSettings();
		return displayArea;
	}

	private void loadDialogSettings() {
		// initialize with dialog settings.
        IDialogSettings settings = PerforceUIPlugin.getPlugin()
                .getDialogSettings();
        String syncOpt=IPerforceUIConstants.ALWAYS;
        if (settings != null) {
            IDialogSettings section = settings.getSection(SYNC_CLIENT_SECTION);
            if (section != null) {
                syncOpt = section.get(SYNC_KEY);
            }
        }
        if(IPerforceUIConstants.ALWAYS.equals(syncOpt)){
        	syncButton.setSelection(true);
        }else{
        	noSyncButton.setSelection(true);
        }
	}

	@Override
	protected boolean customShouldTakeFocus() {
		return false;
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (IDialogConstants.OK_ID == buttonId)
			okPressed();
		else
			super.buttonPressed(buttonId);
	}
	
	@Override
	public void okPressed() {
	      
    	IPreferenceStore store =getPreferenceStore();
    	
    	if(syncButton.getSelection()){
    		syncOpt=IPerforceUIConstants.ALWAYS;
    	}else if(noSyncButton.getSelection()){
    		syncOpt=IPerforceUIConstants.NEVER;
    	}
    	
    	if(noPromptButton.getSelection()){ // won't save unless noPrompt is checked.
    		store.setValue(IPerforceUIConstants.PREF_CLIENT_SWITCH_OPTION, syncOpt);
    	}

    	saveDialogSettings();
		
    	super.okPressed();
	}

	private void saveDialogSettings() {

        IDialogSettings settings = PerforceUIPlugin.getPlugin()
                .getDialogSettings();
        if (settings != null) {
            IDialogSettings section = settings.getSection(SYNC_CLIENT_SECTION);
            if (section == null) {
                section = settings.addNewSection(SYNC_CLIENT_SECTION);
            }
            section.put(SYNC_KEY, syncOpt);
        }
	}

	public IPreferenceStore getPreferenceStore() {
    	IPreferenceStore store =
        		PerforceUIPlugin.getPlugin().getPreferenceStore();
		return store;
	}

	public boolean isAutoSync(){
		return syncOpt.equals(IPerforceUIConstants.ALWAYS);
	}

	////////test only///////////
	public void setAutoSync(boolean b) {
		syncButton.setSelection(b);
	}

	public void setNoPrompt(boolean b) {
		noPromptButton.setSelection(b);
	}
	
}
