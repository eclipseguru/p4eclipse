package com.perforce.team.ui.p4java.dialogs;


public interface IResolveControlContainer {
	ResolveWizard getResolveWizard();
	void setMessage(String msg);
	void setErrorMessage(String msg);
}
