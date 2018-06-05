package com.perforce.team.ui.p4java.dialogs;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class ResolveWizardInteractivePage extends WizardPage implements IResolveControlContainer{

	private ResolveInteractiveControl control;

	protected ResolveWizardInteractivePage(String pageName) {
        super(pageName);
        setTitle(Messages.ResolveWizardInteractivePage_PageTitle);
        setDescription(Messages.ResolveWizardInteractivePage_PageDescription);
    }

    public void createControl(Composite parent) {
        control = new ResolveInteractiveControl(parent, SWT.NONE, this);
        setControl(control);
    }

	public ResolveWizard getResolveWizard() {
		return (ResolveWizard) getWizard();
	}


    @Override
    public void setVisible(boolean b) {
    	control.init();
        super.setVisible(b);
    }
}
