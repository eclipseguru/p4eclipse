package com.perforce.team.ui.dialogs;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.PerforceTeamProvider;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.connection.BasicConnectionWidget;
import com.perforce.team.ui.views.DepotView;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * Dialog for a Perforce project. Shows the connection information
 */
public class ProjectPropertiesDialog extends PropertyPage implements
        IWorkbenchPropertyPage {

    private Composite displayArea;

    // Contains all connection controls and logic
    private BasicConnectionWidget connectionControl;

    private Link editConnectionNote;

    /**
     * Constructor.
     */
    public ProjectPropertiesDialog() {
        connectionControl = new BasicConnectionWidget(true);
        noDefaultAndApplyButton();
    }

    /**
     * Get the dialog settings
     * 
     * @return the dialog settings
     */
    public IDialogSettings getDialogSettings() {
        return PerforceUIPlugin.getPlugin().getDialogSettings();
    }

    /**
     * Create dialog controls
     * 
     * @param parent
     *            the parent window
     * @return the main dialog control
     */
    @Override
    protected Control createContents(Composite parent) {
        displayArea = new Composite(parent, SWT.NONE);
        GridLayout daLayout = new GridLayout(1, false);
        daLayout.marginHeight = 0;
        daLayout.marginWidth = 0;
        displayArea.setLayout(daLayout);
        displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Composite composite = connectionControl
                .createControl(displayArea, true);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        try {
        	PerforceTeamProvider provider = getProvider();
        	if(provider!=null){
	            final ConnectionParameters parameters = provider
	                    .getProjectProperties(true);
	            connectionControl.setConnectionParameters(parameters);
	
	            editConnectionNote = new Link(displayArea, SWT.WRAP);
	            editConnectionNote.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
	                    true, false));
	            editConnectionNote
	                    .setText(Messages.ProjectPropertiesDialog_EditConnectionNote);
	            editConnectionNote.addSelectionListener(new SelectionAdapter() {
	
	                @Override
	                public void widgetSelected(SelectionEvent e) {
	                    DepotView view = DepotView.showView();
	                    if(view!=null){
		                    IP4Connection connection = P4Workspace.getWorkspace()
		                            .getConnection(parameters);
		                    view.getViewer().setSelection(
		                            new StructuredSelection(connection));
	                    }
	                }
	
	            });
        	}
        } catch (CoreException e) {
        }

        return displayArea;
    }

    /**
     * Get Perforce Team provider for this project
     * 
     * @return the team provider
     */
    private PerforceTeamProvider getProvider() {
        IProject project = (IProject) getElement();
        return PerforceTeamProvider.getPerforceProvider(project);
    }
}
