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

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Label;
import com.perforce.team.ui.dialogs.P4StatusDialog;
import com.perforce.team.ui.labels.LabelsViewControl;
import com.perforce.team.ui.views.IPerforceView;

/**
 * @author ali
 * 
 */
public class BrowseLabelDialog extends P4StatusDialog {

    private LabelsViewControl control; 
    private IP4Connection connection;
    
    private IP4Label label;

    /**
     * @param parent
     * @param connection 
     */
    public BrowseLabelDialog(Shell parent, IP4Connection connection) {
        super(parent,Messages.BrowseLabelDialog_Title);
        this.connection = connection;
        setShellStyle(SWT.RESIZE);
    }

    /**
     * Get selected label
     * 
     * @return - IP4Label
     */
    public IP4Label getSelectedLabel() {
        return label;
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
                return BrowseLabelDialog.this.getShell();
            }
            
            public IP4Connection getConnection() {
                return connection;
            }
        };
        control=new LabelsViewControl(view){
            protected void setViewerInput(IP4Connection con) {
                super.setViewerInput(con);
                getTableViewer().addSelectionChangedListener(new ISelectionChangedListener() {
                    public void selectionChanged(SelectionChangedEvent event) {
                        validate();
                    }
                });
                control.showDetails(true);
            }
        };
        control.createViewControl(dialogArea);
        
        return dialogArea;
    }

    private void validate() {
        String errorMessage = null;

        label = control.getLabelSelection();
        if(label==null)
            errorMessage=Messages.BrowseLabelDialog_SelectLabelErrorPrompt;
        
        if (errorMessage != null) {
            setErrorMessage(errorMessage);
        } else {
            setInfoMessage(Messages.BrowseLabelDialog_InformationText);
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
        label = control.getLabelSelection();
        super.okPressed();
    }

}
