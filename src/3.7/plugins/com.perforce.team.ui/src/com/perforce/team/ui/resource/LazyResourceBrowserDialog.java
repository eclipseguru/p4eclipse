/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.resource;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.model.IWorkbenchAdapter;

import com.perforce.team.core.p4java.IP4Container;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.dialogs.P4StatusDialog;

/**
 * @author ali
 * 
 */
public class LazyResourceBrowserDialog extends P4StatusDialog {
    public interface ILazyResourceProvider{
        IP4Resource[] getResources();
    }

    private IWorkbenchAdapter provider = null;
    private IP4Resource selected = null;

    private LazyResourceBrowserWidget widget = null;

    /**
     * @param parent
     * @param resources
     */
    public LazyResourceBrowserDialog(Shell parent, IWorkbenchAdapter provider) {
        super(parent);
        setTitle(Messages.ResourceBrowserDialog_SelectAResource);
        setModalResizeStyle();
        this.provider = provider;
    }

    /**
     * Get selected resource
     * 
     * @return - selected resource
     */
    public IP4Resource getSelectedResource() {
        return this.selected;
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        this.selected = this.widget.getSelectedResource();
        super.okPressed();
    }

    /**
     * Get the viewer
     * 
     * @return - viewer
     */
    public TreeViewer getViewer() {
        return this.widget != null ? this.widget.getViewer() : null;
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite c = (Composite) super.createDialogArea(parent);
        widget = new LazyResourceBrowserWidget(this.provider);
        widget.createControl(c);
        widget.getViewer().addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {
                if (widget.getSelectedResource() != null) {
                    okPressed();
                }
            }
        });
        widget.setErrorDisplay(this);

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
        return c;
    }
}
