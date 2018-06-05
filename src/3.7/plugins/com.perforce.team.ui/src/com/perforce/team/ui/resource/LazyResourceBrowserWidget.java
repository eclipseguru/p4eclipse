/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.resource;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.model.IWorkbenchAdapter;

import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.ui.BaseErrorProvider;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceContentProvider;
import com.perforce.team.ui.PerforceContentProvider.Loading;
import com.perforce.team.ui.PerforceLabelProvider;
import com.perforce.team.ui.PerforceUIPlugin;

/**
 * @author ali
 * 
 */
public class LazyResourceBrowserWidget extends BaseErrorProvider {

    private IWorkbenchAdapter adapter = null;
    private TreeViewer viewer = null;

    /**
     * Create resource browser widget
     */
    public LazyResourceBrowserWidget(IWorkbenchAdapter adapter) {
        this.adapter=adapter;
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
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                validate();
            }
        });
        validate();
        
        initControl();
    }

    private void initControl() {
        final Loading loading = new PerforceContentProvider.Loading();
        viewer.setInput(loading);
        P4Runner.schedule(new P4Runnable() {

            @Override
            public String getTitle() {
                return "Loading resources...";
            }

            @Override
            public void run(IProgressMonitor monitor) {
                final Object[] resources = adapter.getChildren(loading);
                
                PerforceUIPlugin.syncExec(new Runnable() {

                    public void run() {
                        if (!viewer.getControl().isDisposed()) {
                            viewer.remove(loading);
                            viewer.setInput(resources);
                            validate();
                        }
                    }
                });
            }

        });

    }

}
