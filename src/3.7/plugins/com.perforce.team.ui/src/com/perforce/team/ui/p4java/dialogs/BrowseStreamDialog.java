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

import com.perforce.p4java.core.IStreamSummary;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Stream;
import com.perforce.team.ui.dialogs.P4StatusDialog;
import com.perforce.team.ui.streams.StreamsViewControl;
import com.perforce.team.ui.views.IPerforceView;

/**
 * @author ali
 * 
 */
public class BrowseStreamDialog extends P4StatusDialog {

    private StreamsViewControl control; 
    private IP4Connection connection;
    
    private IStreamSummary stream;
    private String info;

    /**
     * @param parent
     * @param connection 
     */
    public BrowseStreamDialog(Shell parent, IP4Connection connection, String info) {
        super(parent,Messages.BrowseStreamDialog_Title);
        this.connection = connection;
        this.info=info;
        setShellStyle(SWT.RESIZE);
    }

    /**
     * Get selected stream
     * 
     * @return - IStreamSummary
     */
    public IStreamSummary getSelectedStream() {
        return stream;
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
                return BrowseStreamDialog.this.getShell();
            }
            
            public IP4Connection getConnection() {
                return BrowseStreamDialog.this.connection;
            }
        };
        control=new StreamsViewControl(view);
        control.createViewControl(dialogArea);
        control.enableAutoUpdate(true);
        control.enableDoubleClick(false);
        control.setVisible(true);
        control.showFilters(true);
        control.showDisplayDetails(true);
        
        control.getTreeViewer().addSelectionChangedListener(new ISelectionChangedListener() {
            
            public void selectionChanged(SelectionChangedEvent event) {
                validate();
            }
        });
        
        return dialogArea;
    }

    private void validate() {
        String errorMessage = null;

        IP4Stream stream=control.getSelectedStream();
        if(stream==null)
            errorMessage=Messages.BrowseStreamDialog_NoStreamSelectedError;
        
        if (errorMessage != null) {
            setErrorMessage(errorMessage);
        } else {
            setInfoMessage(this.info);
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
        IP4Stream s = control.getSelectedStream();
        if(s!=null)
            stream=s.getStreamSummary();
        else
            stream=null;
        super.okPressed();
    }

}
