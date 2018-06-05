package com.perforce.team.ui.dialogs;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import com.perforce.team.core.IConstants;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;

/**
 * Preference page for client workspace settings. 
 */
public class ClientPreferencesDialog extends FieldEditorPreferencePage implements IWorkbenchPreferencePage{

    /**
     * ID
     */
    public static final String ID = "com.perforce.team.ui.dialogs.ClientPreferencesDialog"; //$NON-NLS-1$

    /**
     * Constructor.
     */
    public ClientPreferencesDialog() {
        super();
    	// Set the preference store for the preference page.
    	IPreferenceStore store =
    		PerforceUIPlugin.getPlugin().getPreferenceStore();
    	setPreferenceStore(store);

    	if(StringUtils.isEmpty(store.getString(IPerforceUIConstants.PREF_CLIENT_SWITCH_OPTION)))
    			store.setDefault(IPerforceUIConstants.PREF_CLIENT_SWITCH_OPTION, IPerforceUIConstants.NEVER);
    	
    	if(StringUtils.isEmpty(store.getString(IPerforceUIConstants.PREF_CLIENT_ROOT_PARENT_DEFAULT)))
    		store.setDefault(IPerforceUIConstants.PREF_CLIENT_ROOT_PARENT_DEFAULT, System.getProperty("user.home"));//$NON-NLS-1$

    }
    
    public String getTitle() {
        return Messages.ClientPreferencesDialog_Title;
    }
    
    @Override
    public void createControl(Composite parent) {
    	super.createControl(parent);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IHelpContextIds.PREF_CLIENT_SWITCH_RADIO);
    }

	@Override
	protected void createFieldEditors() {
		final int SPAN = 3;

        Composite composite = getFieldEditorParent();
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        Label switchLabel = new Label(composite, SWT.WRAP);
        switchLabel.setText(Messages.ClientPreferencesDialog_SyncChoices);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan=SPAN;
        switchLabel.setLayoutData(gd);

		RadioGroupFieldEditor switchOption = new RadioGroupFieldEditor(
                IPerforceUIConstants.PREF_CLIENT_SWITCH_OPTION,
                IConstants.EMPTY_STRING,
                1,
                new String[][] {
                        { Messages.ClientPreferencesDialog_Always,
                        	IPerforceUIConstants.ALWAYS },//$NON-NLS-1$
                        { Messages.ClientPreferencesDialog_Never,
                        		IPerforceUIConstants.NEVER },//$NON-NLS-1$
                        { Messages.ClientPreferencesDialog_Prompt,
                        			IPerforceUIConstants.PROMPT },//$NON-NLS-1$
                }, 
                composite,
                true);
		addField(switchOption);
        
        Label defLabel = new Label(composite, SWT.WRAP);
        defLabel.setText(Messages.ClientPreferencesDialog_NewConnectDef);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan=SPAN;
        defLabel.setLayoutData(gd);
        
        Composite clientComp=DialogUtils.createGroup(composite, IConstants.EMPTY_STRING, 3);
        clientComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        DirectoryFieldEditor clientLocation = new DirectoryFieldEditor(
        		IPerforceUIConstants.PREF_CLIENT_ROOT_PARENT_DEFAULT,
        		Messages.ClientPreferencesDialog_Location,
        		clientComp){ // change default validation strategy
        	@Override
        	protected void createControl(Composite parent) {
        		setValidateStrategy(VALIDATE_ON_KEY_STROKE);
        		super.createControl(parent);
        	}
        	
        };
		addField(clientLocation);		
		clientComp.setLayoutData(new GridData(SWT.FILL,SWT.TOP,true,false));
	}

	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
		
	}
}
