/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.project;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.BaseErrorProvider;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class ShareProjectsWidget extends BaseErrorProvider {

    private IP4Connection connection = null;
    private IProject[] projects = null;

    private IProject[] unmanaged = null;
    private Composite displayArea;
    private CheckboxTableViewer projectViewer;
    private Button selectAll;
    private Button deselectAll;

    /**
     * Share projects widget
     * 
     * @param connection
     */
    public ShareProjectsWidget(IP4Connection connection) {
        this.connection = connection;
    }

    /**
     * Get checked projects
     * 
     * @return - checked projects
     */
    public IProject[] getProjects() {
        return this.projects;
    }

    /**
     * Validate and show any error messages that should be shown when the dialog
     * is created.
     */
    public void validateCreate() {
        if (this.unmanaged.length == 0) {
            this.errorMessage = Messages.ShareProjectsWidget_NoUnsharedProjectsInWorkspace;
            super.validate();
        }
    }

    /**
     * @see com.perforce.team.ui.BaseErrorProvider#validate()
     */
    @Override
    public void validate() {
        Object[] checked = projectViewer.getCheckedElements();
        if (checked.length == 0) {
            this.errorMessage = Messages.ShareProjectsWidget_MustSelectAtLeastOneProject;
        } else {
            this.errorMessage = null;
        }
        super.validate();
    }

    /**
     * Create share project widget
     * 
     * @param parent
     */
    public void createControl(Composite parent) {
        displayArea = new Composite(parent, SWT.NONE);
        GridLayout daLayout = new GridLayout(1, true);
        displayArea.setLayout(daLayout);
        displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Label connectionLabel = new Label(displayArea, SWT.LEFT);
        connectionLabel.setText(MessageFormat.format(
                Messages.ShareProjectsWidget_Connection,
                this.connection.getName()));

        projectViewer = CheckboxTableViewer.newCheckList(displayArea,
                SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER
                        | SWT.SINGLE);
        GridData pvData = new GridData(SWT.FILL, SWT.FILL, true, true);
        pvData.heightHint = 100;
        pvData.widthHint = 400;
        projectViewer.getTable().setLayoutData(pvData);
        projectViewer.getTable().addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                Object[] checked = projectViewer.getCheckedElements();
                projects = new IProject[checked.length];
                System.arraycopy(checked, 0, projects, 0, checked.length);
            }
        });
        projectViewer.setContentProvider(new ArrayContentProvider());
        projectViewer.setLabelProvider(new WorkbenchLabelProvider());
        List<IProject> unmanagedProject = new ArrayList<IProject>();
        for (IProject project : ResourcesPlugin.getWorkspace().getRoot()
                .getProjects()) {
            if (project.isAccessible() && !RepositoryProvider.isShared(project)) {
                unmanagedProject.add(project);
            }
        }
        this.unmanaged = unmanagedProject.toArray(new IProject[unmanagedProject
                .size()]);
        projectViewer.setInput(this.unmanaged);

        Composite buttons = new Composite(displayArea, SWT.NONE);
        GridLayout bLayout = new GridLayout(2, false);
        buttons.setLayout(bLayout);
        buttons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        selectAll = new Button(buttons, SWT.PUSH);
        selectAll.setLayoutData(new GridData(SWT.END, SWT.FILL, true, false));
        selectAll.setText(Messages.ShareProjectsWidget_SelectAll);
        selectAll.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                projectViewer.setAllChecked(true);
                validate();
            }

        });

        deselectAll = new Button(buttons, SWT.PUSH);
        deselectAll.setText(Messages.ShareProjectsWidget_DeselectAll);
        deselectAll.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                projectViewer.setAllChecked(false);
                validate();
            }

        });

        if (this.unmanaged.length == 0) {
            selectAll.setEnabled(false);
            deselectAll.setEnabled(false);
        }

        projectViewer.addCheckStateListener(new ICheckStateListener() {

            public void checkStateChanged(CheckStateChangedEvent event) {
                validate();
            }
        });
    }
}
