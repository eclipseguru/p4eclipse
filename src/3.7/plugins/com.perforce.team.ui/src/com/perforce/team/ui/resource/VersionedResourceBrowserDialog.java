/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.resource;

import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Container;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.ui.dialogs.P4StatusDialog;
import com.perforce.team.ui.labels.VersionWidget;
import com.perforce.team.ui.p4java.dialogs.PopulateDialog;
import com.perforce.team.ui.p4java.dialogs.PopulateDialog.FileVersion;

public class VersionedResourceBrowserDialog extends P4StatusDialog {

	private String initialPath;
	/**
	 * selected version
	 * <ul>
	 * Annotations:
	 * <li><em>label:</em> @release1</li>
	 * <li><em>changelist:</em>@123456</li>
	 * <li><em>revision:</em>#2</li>
	 * <li><em>null:</em>the head revision</li>
	 * </ul>
	 */
	private String version; 
	
    private IP4Resource[] resources = null;
    private IP4Resource selected = null;

    private ResourceBrowserWidget treeWidget = null;
    private VersionWidget versionWidget = null;

    /**
     * @param parent
     * @param resources
     */
    public VersionedResourceBrowserDialog(Shell parent, IP4Resource[] resources, String initial) {
        super(parent);
        setTitle(Messages.ResourceBrowserDialog_SelectAResource);
        setModalResizeStyle();
        this.resources = resources;
        this.initialPath=initial;
    }

    /**
     * Get selected resource
     * 
     * @return - selected resource
     */
    public IP4Resource getSelectedResource() {
        return this.selected;
    }
    
    public String getVersion(){
    	return version;
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
    	IP4Resource selection = this.treeWidget.getSelectedResource();
    	if(selection==null || StringUtils.isEmpty(selection.getActionPath())){
    		setErrorMessage("You must select a folder or file!");
    		return;
    	}
    	this.selected = selection;
        this.version = this.versionWidget.getVersion();
        super.okPressed();
    }

    /**
     * Get the viewer
     * 
     * @return - viewer
     */
    public TreeViewer getViewer() {
        return this.treeWidget != null ? this.treeWidget.getViewer() : null;
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite c = (Composite) super.createDialogArea(parent);
        treeWidget = new ResourceBrowserWidget(this.resources);
        treeWidget.createControl(c);
        treeWidget.getViewer().addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {
                if (treeWidget.getSelectedResource() != null) {
                    okPressed();
                }
            }
        });
        treeWidget.setErrorDisplay(this);
        
        versionWidget = new VersionWidget();
        versionWidget.createControl(c).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));  
        versionWidget.setErrorDisplay(this);

        init();
        return c;
    }

	private void init() {
		FileVersion v = new PopulateDialog.FileVersion(initialPath);
		if(!StringUtils.isEmpty(initialPath)){
			expandElement(treeWidget.getViewer(), resources[0].getConnection(), v.getPath());
		}
		
		if(!StringUtils.isEmpty(v.getRevision())){
			versionWidget.setVersion(v.getRevision());
		}
	}
	
    private Object expandElement(TreeViewer viewer, IP4Connection connection, String path) {
        if (path == null) {
            return null;
        }
        IP4Container curr = connection;
        if (curr == null || curr.getConnection().isOffline()) {
            return null;
        }
        StringTokenizer tokens = new StringTokenizer(path, "/"); //$NON-NLS-1$
        
        Object targetFile=null;
        while (tokens.hasMoreTokens()) {
            String name = tokens.nextToken();
            System.out.println("expand  "+name);
            viewer.setExpandedState(curr, true);
            Object[] children = curr.members();
            boolean found = false;
            for (int i = 0; i < children.length; i++) {
                IP4Resource child = (IP4Resource) children[i];
                if (name.equals(child.getName())){
                	if(child instanceof IP4Container) {
	                    curr = (IP4Container) child;
	                    found = true;
	                    break;
	                }
                	targetFile = child;
                }
            }
            if (!found) {
                break;
            }
        }
        viewer.setExpandedState(curr, true);
        if(targetFile!=null)
        	viewer.setSelection(new StructuredSelection(targetFile),true);
        else
        	viewer.setSelection(new StructuredSelection(curr),true);
        return curr;
    }
}
