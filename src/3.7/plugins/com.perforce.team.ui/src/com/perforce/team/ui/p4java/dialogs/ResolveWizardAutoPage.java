package com.perforce.team.ui.p4java.dialogs;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class ResolveWizardAutoPage extends WizardPage implements IResolveControlContainer{

	private ResolveAutoControl control;

	protected ResolveWizardAutoPage(String pageName) {
        super(pageName);
        setTitle(Messages.ResolveWizardAutoPage_PageTitle);
        setDescription(Messages.ResolveWizardAutoPage_PageDescription);
    }

    public void createControl(Composite parent) {
        control = new ResolveAutoControl(parent, SWT.NONE, this);
        setControl(control);
    }


    @Override
    public void setVisible(boolean b) {
    	control.init();
        super.setVisible(b);
    }

	public ResolveWizard getResolveWizard() {
		return (ResolveWizard) getWizard();
	}
}
