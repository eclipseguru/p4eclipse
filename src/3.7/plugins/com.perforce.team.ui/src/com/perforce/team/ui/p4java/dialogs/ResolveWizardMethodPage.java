package com.perforce.team.ui.p4java.dialogs;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;

public class ResolveWizardMethodPage extends WizardPage {

    private Button autoButton;
    private Button interactiveButton;
	private Button rememberChoiceButton;

    public ResolveWizardMethodPage(String pageName) {
        super(pageName);
        setTitle(Messages.ResolveWizardMethodPage_PageTitle);
        setDescription(Messages.ResolveWizardMethodPage_PageDescription);
    }

    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));
        setControl(composite);

        Group group = new Group(composite, SWT.NONE);
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        group.setLayout(new GridLayout(1, true));

        autoButton = new Button(group, SWT.RADIO);
        autoButton.setText(Messages.ResolveWizardMethodPage_AutoResolve);
        interactiveButton = new Button(group, SWT.RADIO);
        interactiveButton.setText(Messages.ResolveWizardMethodPage_InteractiveResolve);

        rememberChoiceButton = new Button(composite, SWT.CHECK);
        rememberChoiceButton.setText(Messages.ResolveWizardMethodPage_RememberChoice);
        
        // Set initial selection from prefs
        String mode = PerforceUIPlugin.getPlugin().getPreferenceStore()
                .getString(IPerforceUIConstants.PREF_RESOLVE_DEFAULT_MODE);
        autoButton.setSelection(IPerforceUIConstants.RESOLVE_AUTO.equals(mode));
        interactiveButton.setSelection(IPerforceUIConstants.RESOLVE_INTERACTIVE.equals(mode));

       	SelectionListener listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				getWizard().getContainer().updateButtons();
			}
		};
       	autoButton.addSelectionListener(listener);
       	interactiveButton.addSelectionListener(listener);
    }

    public boolean autoSelected() {
        return autoButton.getSelection();
    }

    public boolean interactiveSelected() {
        return interactiveButton.getSelection();
    }

    public boolean rememberDefaultSelected() {
        return rememberChoiceButton.getSelection();
    }
    
    @Override
    public boolean isPageComplete() {
    	return (autoButton.getSelection() || interactiveButton.getSelection());
    }
    
    @Override
    public boolean canFlipToNextPage() {
    	return (autoButton.getSelection() || interactiveButton.getSelection());
    }
    
}
