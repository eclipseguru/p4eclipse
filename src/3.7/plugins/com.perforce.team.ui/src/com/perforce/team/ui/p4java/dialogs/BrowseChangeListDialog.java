/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.dialogs;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;

import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.ui.dialogs.P4StatusDialog;
import com.perforce.team.ui.views.IPerforceView;
import com.perforce.team.ui.views.SubmittedViewControl;

/**
 * @author ali
 * 
 */
public class BrowseChangeListDialog extends P4StatusDialog {

    private SubmittedViewControl control; 
    private IP4Connection connection;
    
    private IP4Changelist changelist;

    /**
     * @param parent
     * @param connection 
     */
    public BrowseChangeListDialog(Shell parent, IP4Connection connection) {
        super(parent,Messages.BrowseChangeListDialog_Title);
        this.connection = connection;
        setShellStyle(SWT.RESIZE);
    }

    /**
     * Get selected changelist
     * 
     * @return - IP4Changelist
     */
    public IP4Changelist getSelectedChangeList() {
        return changelist;
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite dialogArea = (Composite) super.createDialogArea(parent);
        IPerforceView view = new IPerforceView() {
            
            public IWorkbenchPartSite getSite() {
                return null;
            }
            
            public Shell getShell() {
                return BrowseChangeListDialog.this.getShell();
            }
            
            public IP4Connection getConnection() {
                return connection;
            }
        };
        control=new SubmittedViewControl(view);
        control.createViewControl(dialogArea);
        control.showDisplayDetails(true);
        control.enableDoubleClick(false);
        
        control.getChangelistTable().getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
            
            public void selectionChanged(SelectionChangedEvent event) {
                validate();
            }
        });
        
        return dialogArea;
    }

    private void validate() {
        String errorMessage = null;

        IP4SubmittedChangelist[] selections = control.getChangelistTable().getSelectedChangelists();
        if(selections==null||selections.length==0)
            errorMessage=Messages.BrowseChangeListDialog_NoChangeListSelectedError;
        
        if (errorMessage != null) {
            setErrorMessage(errorMessage);
        } else {
            setInfoMessage(Messages.BrowseChangeListDialog_InformationText);
        }
    }
    
    @Override
    public void create() {
        super.create();
        getShell().setSize(1000,800);
        validate(); // show error message
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        IP4SubmittedChangelist[] selections = control.getChangelistTable().getSelectedChangelists();
        if(selections!=null && selections.length>0){
            this.changelist=selections[0];
        }
        super.okPressed();
    }

}
