package com.perforce.team.ui.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;

/**
 * This is similar to the JFace's MessageDialogWithToggle. But with a explict labe-id mapping.
 */
public class SwitchStreamDialog extends MessageDialog {

	private int[] buttonIds;
	protected boolean toggleState;
	private String toggleMessage;
	private IPreferenceStore prefStore;
	private String prefKey;
	
	public int[] getButtonIds() {
		return buttonIds;
	}

    public boolean getToggleState() {
        return toggleState;
    }

    public SwitchStreamDialog(Shell parentShell, String dialogTitle,
			Image dialogTitleImage, String dialogMessage, String toggleMessage, int dialogImageType,
			String[] dialogButtonLabels, int[] buttonIds, int defaultIndex, IPreferenceStore store, String key) {
		super(parentShell, dialogTitle, dialogTitleImage, dialogMessage,
				dialogImageType, dialogButtonLabels, defaultIndex);
		this.buttonIds=buttonIds;
		this.toggleMessage=toggleMessage;
        this.prefStore = store;
        this.prefKey = key;
	}
	
	@Override
    protected void createButtonsForButtonBar(Composite parent) {
    	String[] labels = getButtonLabels();
        Button[] btns = new Button[labels.length];
        for (int i = 0; i < btns.length; i++) {
            String label = labels[i];
            Button button = createButton(parent, buttonIds[i], label,
                    getDefaultButtonIndex() == i);
            btns[i] = button;
        }
        setButtons(btns);
    }

	@Override
	protected Control createCustomArea(Composite parent) {
		if(toggleMessage==null)
			return null;
		
        final Button button = new Button(parent, SWT.CHECK | SWT.LEFT);

        GridData data = new GridData(SWT.NONE);
        data.horizontalSpan = 2;
        button.setLayoutData(data);
        button.setFont(parent.getFont());

        button.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                toggleState = button.getSelection();
                System.out.println(toggleState);
                if(prefStore!=null)
                	prefStore.setValue(prefKey, toggleState);
            }

        });

    	button.setText(toggleMessage);
        	
        return button;
    }
	
	public static int open(int kind, Shell parent, String title,
			String message, String[] buttonLabels, int[] buttonIds, int defaultIndex) {
		return open(kind, parent, title, message, null, buttonLabels, buttonIds, defaultIndex);
	}

	public static int open(int kind, Shell parent, String title,
			String message, String toggleMessage, String[] buttonLabels, int[] buttonIds, int defaultIndex) {
    	IPreferenceStore store = null;
    	try {
    		store=PerforceUIPlugin.getPlugin().getPreferenceStore();
		} catch (Exception e) {
		}
		SwitchStreamDialog dialog = new SwitchStreamDialog(parent, title, null, message, toggleMessage,
				kind, buttonLabels, buttonIds, defaultIndex, store,IPerforceUIConstants.PREF_CLIENT_SWITCH_NO_WARN);
		return dialog.open();
	}

	
	public static void main (String [] args) {
		Display display = new Display ();
		Shell shell = new Shell (display);
		shell.open ();
		
		int code=SwitchStreamDialog.open(MessageDialog.ERROR, shell, "Error", "This is an error", "Do not show again, please", new String[]{
				IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL
		},new int[]{IDialogConstants.YES_ID, IDialogConstants.NO_ID},0);
		
		System.out.println(code);

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch ()) display.sleep ();
		}
		display.dispose ();
	}


}
