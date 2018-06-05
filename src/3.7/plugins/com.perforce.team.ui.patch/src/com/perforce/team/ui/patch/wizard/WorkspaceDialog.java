/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.patch.wizard;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;

import com.perforce.team.ui.decorator.PerforceDecorator;
import com.perforce.team.ui.patch.P4PatchUiPlugin;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class WorkspaceDialog extends TitleAreaDialog {

    /**
     * DIALOG_SETTINGS
     */
    public static final String DIALOG_SETTINGS = "WorkspaceDialog"; //$NON-NLS-1$

    private TreeViewer viewer;
    private Text nameText;

    private IContainer parent;
    private String name;

    private String initialMessage = null;
    private String initialTitle = null;

    /**
     * @param parentShell
     */
    public WorkspaceDialog(Shell parentShell) {
        super(parentShell);
        setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#getDialogBoundsSettings()
     */
    @Override
    protected IDialogSettings getDialogBoundsSettings() {
        IDialogSettings wizardSection = null;
        IDialogSettings settings = P4PatchUiPlugin.getDefault()
                .getDialogSettings();
        if (settings != null) {
            wizardSection = settings.getSection(DIALOG_SETTINGS);
            if (wizardSection == null) {
                wizardSection = settings.addNewSection(DIALOG_SETTINGS);
            }
        }
        return wizardSection;
    }

    /**
     * Set initial message to display
     * 
     * @param message
     */
    public void setInitialMessage(String message) {
        this.initialMessage = message;
    }

    /**
     * Set initial title to display
     * 
     * @param title
     */
    public void setInitialTitle(String title) {
        this.initialTitle = title;
    }

    /**
     * Get selected file
     * 
     * @return file
     */
    public IFile getFile() {
        if (parent != null && name != null) {
            return parent.getFile(new Path(name));
        } else {
            return null;
        }
    }

    /**
     * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite c = (Composite) super.createDialogArea(parent);

        Composite displayArea = new Composite(c, SWT.NONE);
        displayArea.setLayout(GridLayoutFactory.fillDefaults().create());
        displayArea.setLayoutData(GridDataFactory.fillDefaults()
                .grab(true, true).create());

        viewer = new TreeViewer(displayArea, SWT.V_SCROLL | SWT.H_SCROLL
                | SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
        viewer.getTree().setLayoutData(
                GridDataFactory.fillDefaults().grab(true, true).create());

        final List<IProject> projects = new ArrayList<IProject>();
        for (IProject project : ResourcesPlugin.getWorkspace().getRoot()
                .getProjects()) {
            if (project.isAccessible()) {
                projects.add(project);
            }
        }
        viewer.setContentProvider(new WorkbenchContentProvider() {

            @Override
            public Object[] getElements(Object element) {
                return projects.toArray();
            }

        });
        viewer.setLabelProvider(new DecoratingLabelProvider(
                new WorkbenchLabelProvider(), new PerforceDecorator(true){
                    public String getName() {
                    	return WorkspaceDialog.class.getSimpleName()+":"+super.getName();
                    }
                }));
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                Object selection = ((IStructuredSelection) viewer
                        .getSelection()).getFirstElement();
                if (selection instanceof IFile) {
                    nameText.setText(((IFile) selection).getName());
                }
                validate();
            }
        });
        viewer.addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {
                Object selection = ((IStructuredSelection) viewer
                        .getSelection()).getFirstElement();
                if (selection instanceof IFile) {
                    okPressed();
                }
            }
        });
        viewer.setComparator(new ResourceComparator(ResourceComparator.NAME));
        viewer.setInput(projects);

        Composite nameArea = new Composite(displayArea, SWT.NONE);
        nameArea.setLayout(GridLayoutFactory.swtDefaults().numColumns(2)
                .equalWidth(false).create());
        nameArea.setLayoutData(GridDataFactory.fillDefaults().grab(true, false)
                .create());

        new Label(nameArea, SWT.NONE)
                .setText(Messages.WorkspaceDialog_FileName);

        nameText = new Text(nameArea, SWT.SINGLE | SWT.BORDER);
        nameText.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.FILL, SWT.CENTER).grab(true, false).create());
        nameText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                validate();
            }
        });

        validate();

        if (this.initialMessage != null) {
            setMessage(this.initialMessage);
        }
        if (this.initialTitle != null) {
            setTitle(this.initialTitle);
        }

        return c;
    }

    /**
     * Validate the dialog
     */
    private void validate() {
        Object selection = ((IStructuredSelection) viewer.getSelection())
                .getFirstElement();
        if (selection instanceof IFile) {
            parent = ((IFile) selection).getParent();
        } else if (selection instanceof IContainer) {
            parent = (IContainer) selection;
        } else {
            parent = null;
        }
        name = nameText.getText();

        String message = null;

        if (message == null && name.length() == 0) {
            message = Messages.WorkspaceDialog_MustSpecifyFileName;
        }

        if (message == null && !Path.EMPTY.isValidSegment(name)) {
            message = Messages.WorkspaceDialog_FileNameInvalid;
        }

        if (message == null && parent == null) {
            message = Messages.WorkspaceDialog_SelectFolder;
        }
    }
}
