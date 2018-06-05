/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.resource;

import com.perforce.team.core.p4java.IP4Container;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.ui.BaseErrorProvider;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceContentProvider;
import com.perforce.team.ui.PerforceLabelProvider;
import com.perforce.team.ui.PerforceUIPlugin;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class ResourceBrowserWidget extends BaseErrorProvider {

    private IP4Resource[] resources = null;
    private TreeViewer viewer = null;

    /**
     * Create resource browser widget
     * 
     * @param resources
     */
    public ResourceBrowserWidget(IP4Resource[] resources) {
        this.resources = resources;
    }

    /**
     * Get underlying tree view
     * 
     * @return - tree viewer
     */
    public TreeViewer getViewer() {
        return this.viewer;
    }

    /**
     * Get selected resource from current selection in widget.
     * 
     * @return - selected p4 resource of null
     */
    public IP4Resource getSelectedResource() {
        IP4Resource selected = null;
        IStructuredSelection selection = (IStructuredSelection) viewer
                .getSelection();
        if (selection.size() == 1) {
            Object first = selection.getFirstElement();
            if (first instanceof IP4Resource) {
                selected = (IP4Resource) first;
            }
        }
        return selected;
    }

    /**
     * @see com.perforce.team.ui.BaseErrorProvider#validate()
     */
    @Override
    public void validate() {
        IP4Resource selected = getSelectedResource();
        if (selected == null) {
            errorMessage = Messages.ResourceBrowserWidget_SelectAResource;
        } else {
            errorMessage = null;
        }
        super.validate();
    }

    /**
     * Creates the controls of this widget
     * 
     * @param parent
     */
    public void createControl(Composite parent) {
        viewer = new TreeViewer(parent, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL
                | SWT.V_SCROLL);
        GridData vData = new GridData(SWT.FILL, SWT.FILL, true, true);
        vData.heightHint = P4UIUtils.computePixelHeight(viewer.getTree()
                .getFont(), 15);
        vData.widthHint = 300;
        viewer.getTree().setLayoutData(vData);
        viewer.setContentProvider(new PerforceContentProvider(viewer, true));
        viewer.setLabelProvider(new PerforceLabelProvider(false));
        viewer.setInput(this.resources);
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                validate();
            }
        });
        
        addContextMenu(this);
        validate();
    }

	private void addContextMenu(final ResourceBrowserWidget widget) {
        // Create refresh menu
        final Action refreshAction = new Action(
                Messages.ResourceBrowserDialog_Refresh,
                PerforceUIPlugin
                        .getDescriptor(IPerforceUIConstants.IMG_REFRESH)) {

            @Override
            public void run() {
                IStructuredSelection selection = (IStructuredSelection) widget
                        .getViewer().getSelection();
                for (Object refresh : selection.toArray()) {
                    if (refresh instanceof IP4Container) {
                        ((IP4Container) refresh).markForRefresh();
                    }
                    widget.getViewer().refresh(refresh, true);
                }
            }
        };
        refreshAction.setToolTipText(Messages.ResourceBrowserDialog_Refresh);
        MenuManager manager = new MenuManager();
        Tree tree = widget.getViewer().getTree();
        Menu menu = manager.createContextMenu(tree);
        manager.addMenuListener(new IMenuListener() {

            public void menuAboutToShow(IMenuManager manager) {
                IStructuredSelection selection = (IStructuredSelection) widget
                        .getViewer().getSelection();
                boolean containersOnly = true;
                for (Object refresh : selection.toArray()) {
                    if (!(refresh instanceof IP4Container)) {
                        containersOnly = false;
                        break;
                    }
                }
                if (containersOnly) {
                    manager.add(refreshAction);
                }
            }
        });
        manager.setRemoveAllWhenShown(true);
        tree.setMenu(menu);
		
	}

}
