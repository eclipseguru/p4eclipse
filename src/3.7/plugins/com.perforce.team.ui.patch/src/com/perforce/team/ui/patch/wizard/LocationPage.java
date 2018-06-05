/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.patch.wizard;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4JavaSysFileCommandsHelper;
import com.perforce.team.ui.IErrorDisplay;
import com.perforce.team.ui.IErrorProvider;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.patch.P4PatchUiPlugin;
import com.perforce.team.ui.patch.model.ClipboardStream;
import com.perforce.team.ui.patch.model.FileSystemStream;
import com.perforce.team.ui.patch.model.IPatchStream;
import com.perforce.team.ui.patch.model.WorkspaceStream;
import com.perforce.team.ui.patch.preferences.IPreferenceConstants;
import com.perforce.team.ui.views.SessionManager;

import java.io.File;
import java.text.MessageFormat;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class LocationPage extends WizardPage implements IErrorProvider {

    private Composite displayArea;

    private Button clipboardButton;

    private Button fileButton;
    private Combo fileText;
    private ToolBar fileToolbar;

    private Button workspaceButton;
    private Combo workspaceText;
    private ToolBar workspaceToolbar;

    private PendingViewer viewer;
    private P4JavaSysFileCommandsHelper helper = new P4JavaSysFileCommandsHelper();

    private Text p4Text;

    private IResource[] resources;

    private ClipboardStream clipboardStream = new ClipboardStream();
    private FileSystemStream fileStream = new FileSystemStream();
    private WorkspaceStream workspaceStream = new WorkspaceStream();

    private IPatchStream selected = null;
    private P4Collection collection = null;
    private String p4Path = null;

    /**
     * Create a location page
     * 
     * @param collection
     */
    protected LocationPage(P4Collection collection) {
        super("locationPage"); //$NON-NLS-1$
        setTitle(Messages.LocationPage_Title);
        setDescription(Messages.LocationPage_Description);
        this.collection = collection;
        this.p4Path = P4PatchUiPlugin.getDefault().getPreferenceStore()
                .getString(IPreferenceConstants.P4_PATH);
    }

    /**
     * Save history
     */
    public void saveHistory() {
        if (P4UIUtils.okToUse(displayArea)) {
            IPreferenceStore store = P4PatchUiPlugin.getDefault()
                    .getPreferenceStore();

            SessionManager.saveComboHistory(fileText, 10,
                    IPreferenceConstants.FILE_PATHS, store);

            SessionManager.saveComboHistory(workspaceText, 10,
                    IPreferenceConstants.WORKSPACE_PATHS, store);

            if (clipboardButton.getSelection()) {
                store.setValue(IPreferenceConstants.PATCH_TYPE,
                        IPreferenceConstants.CLIPBOARD);
            } else if (workspaceButton.getSelection()) {
                store.setValue(IPreferenceConstants.PATCH_TYPE,
                        IPreferenceConstants.WORKSPACE);
            } else if (fileButton.getSelection()) {
                store.setValue(IPreferenceConstants.PATCH_TYPE,
                        IPreferenceConstants.FILE);
            }

            store.setValue(IPreferenceConstants.P4_PATH, this.p4Path);
        }
    }

    private void createFileArea(Composite parent) {
        fileButton = new Button(parent, SWT.RADIO);
        fileButton.setText(Messages.LocationPage_FilePath);

        fileText = new Combo(parent, SWT.SINGLE | SWT.DROP_DOWN);
        fileText.setLayoutData(GridDataFactory.fillDefaults()
                .align(SWT.FILL, SWT.CENTER).grab(true, false).create());

        final Runnable updateText = new Runnable() {

            public void run() {
                validate();
            }
        };
        fileText.addSelectionListener(P4UIUtils
                .createComboSelectionListener(updateText));
        fileText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                updateText.run();
            }
        });

        SessionManager.loadComboHistory(fileText,
                IPreferenceConstants.FILE_PATHS, P4PatchUiPlugin.getDefault()
                        .getPreferenceStore());

        fileToolbar = new ToolBar(parent, SWT.FLAT);
        ToolItem browseItem = new ToolItem(fileToolbar, SWT.PUSH);
        Image browseImage = PerforceUIPlugin.getDescriptor(
                IPerforceUIConstants.IMG_FIND).createImage();
        P4UIUtils.registerDisposal(browseItem, browseImage);
        browseItem.setImage(browseImage);
        browseItem.setToolTipText(Messages.LocationPage_Browse);
        browseItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = new FileDialog(fileToolbar.getShell(),
                        SWT.SAVE);
                String selected = dialog.open();
                if (selected != null) {
                    fileText.setText(selected);
                    validate();
                }
            }

        });
    }

    private void createWorkspaceArea(Composite parent) {
        workspaceButton = new Button(parent, SWT.RADIO);
        workspaceButton.setText(Messages.LocationPage_WorkspacePath);

        workspaceText = new Combo(parent, SWT.SINGLE | SWT.DROP_DOWN);
        workspaceText.setLayoutData(GridDataFactory.fillDefaults()
                .align(SWT.FILL, SWT.CENTER).grab(true, false).create());
        SessionManager.loadComboHistory(workspaceText,
                IPreferenceConstants.WORKSPACE_PATHS, P4PatchUiPlugin
                        .getDefault().getPreferenceStore());

        final Runnable updateText = new Runnable() {

            public void run() {
                validate();
            }
        };
        workspaceText.addSelectionListener(P4UIUtils
                .createComboSelectionListener(updateText));
        workspaceText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                updateText.run();
            }
        });

        workspaceToolbar = new ToolBar(parent, SWT.FLAT);
        ToolItem browseItem = new ToolItem(workspaceToolbar, SWT.PUSH);
        Image browseImage = PerforceUIPlugin.getDescriptor(
                IPerforceUIConstants.IMG_FIND).createImage();
        P4UIUtils.registerDisposal(browseItem, browseImage);
        browseItem.setImage(browseImage);
        browseItem.setToolTipText(Messages.LocationPage_Browse);
        browseItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                WorkspaceDialog dialog = new WorkspaceDialog(getShell());
                dialog.setTitleImage(getImage());
                dialog.setInitialTitle(Messages.LocationPage_WorkspaceDialogTitle);
                dialog.setInitialMessage(Messages.LocationPage_WorkspaceDialogMessage);
                if (WorkspaceDialog.OK == dialog.open()) {
                    IFile file = dialog.getFile();
                    if(file!=null){
	                    workspaceText.setText(file.getFullPath().toString());
	                    validate();
                    }
                }
            }

        });
    }

    private void createP4Area(Composite parent, int span) {
        Composite p4Area = new Composite(parent, SWT.NONE);
        p4Area.setLayout(GridLayoutFactory.fillDefaults().numColumns(3)
                .create());
        p4Area.setLayoutData(GridDataFactory.fillDefaults().grab(true, false)
                .span(3, 1).create());

        new Label(p4Area, SWT.NONE).setText(Messages.LocationPage_P4Path);

        p4Text = new Text(p4Area, SWT.BORDER | SWT.SINGLE);
        p4Text.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.FILL, SWT.CENTER).grab(true, false).create());
        p4Text.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                validate();
            }
        });

        p4Text.setText(p4Path);

        final ToolBar toolbar = new ToolBar(p4Area, SWT.FLAT);
        ToolItem browseItem = new ToolItem(toolbar, SWT.PUSH);
        Image browseImage = PerforceUIPlugin.getDescriptor(
                IPerforceUIConstants.IMG_FIND).createImage();
        P4UIUtils.registerDisposal(browseItem, browseImage);
        browseItem.setImage(browseImage);
        browseItem.setToolTipText(Messages.LocationPage_Browse);
        browseItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = new FileDialog(toolbar.getShell(), SWT.OPEN);
                String selected = dialog.open();
                if (selected != null) {
                    p4Text.setText(selected);
                    validate();
                }
            }

        });
    }

    private void createSeparator(Composite parent, int span) {
        new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL)
                .setLayoutData(GridDataFactory.fillDefaults().span(3, 1)
                        .grab(true, false).create());
    }

    /**
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        displayArea = new Composite(parent, SWT.NONE);
        displayArea.setLayout(GridLayoutFactory.swtDefaults().numColumns(3)
                .equalWidth(false).create());
        displayArea.setLayoutData(GridDataFactory.fillDefaults()
                .grab(true, true).create());

        Label locationLabel = new Label(displayArea, SWT.NONE);
        locationLabel.setText(Messages.LocationPage_SelectExportLocation);
        locationLabel.setLayoutData(GridDataFactory.fillDefaults().span(3, 1)
                .create());

        clipboardButton = new Button(displayArea, SWT.RADIO);
        clipboardButton.setText(Messages.LocationPage_Clipboard);
        clipboardButton.setLayoutData(GridDataFactory.swtDefaults().span(3, 1)
                .create());

        createFileArea(displayArea);
        createWorkspaceArea(displayArea);

        createSeparator(displayArea, 3);

        Label pendingLabel = new Label(displayArea, SWT.NONE);
        pendingLabel.setLayoutData(GridDataFactory.swtDefaults().span(3, 1)
                .create());
        pendingLabel.setText(Messages.LocationPage_PendingProjectFiles);

        this.viewer = new PendingViewer(this.collection);
        viewer.createControl(displayArea, new Runnable() {

            public void run() {
                validate();
            }
        });
        CheckboxTreeViewer checkTree = this.viewer.getViewer();
        if (checkTree != null) {
            checkTree.addCheckStateListener(new ICheckStateListener() {

                public void checkStateChanged(CheckStateChangedEvent event) {
                    validate();
                }
            });
        }
        ((GridData) checkTree.getTree().getLayoutData()).horizontalSpan = 3;

        createSeparator(displayArea, 3);

        createP4Area(displayArea, 3);

        SelectionListener buttonListener = new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateEnablement();
                updateFocus();
                validate();
            }

        };
        clipboardButton.addSelectionListener(buttonListener);
        fileButton.addSelectionListener(buttonListener);
        workspaceButton.addSelectionListener(buttonListener);

        setInitialType();
        updateEnablement();

        validate();

        setControl(displayArea);
        setErrorMessage(null);
        setPageComplete(false);
    }

    private void setInitialType() {
        String initialType = P4PatchUiPlugin.getDefault().getPreferenceStore()
                .getString(IPreferenceConstants.PATCH_TYPE);
        if (initialType.length() > 0) {
            if (IPreferenceConstants.CLIPBOARD.equals(initialType)) {
                clipboardButton.setSelection(true);
            } else if (IPreferenceConstants.WORKSPACE.equals(initialType)) {
                workspaceButton.setSelection(true);
            } else if (IPreferenceConstants.FILE.equals(initialType)) {
                fileButton.setSelection(true);
            } else {
                clipboardButton.setSelection(true);
            }
        } else {
            clipboardButton.setSelection(true);
        }
        if (fileText.getItemCount() > 0) {
            fileText.select(0);
        }
        if (workspaceText.getItemCount() > 0) {
            workspaceText.select(0);
        }
    }

    private void updateFocus() {
        if (fileButton.getSelection()) {
            fileText.setFocus();
        } else if (workspaceButton.getSelection()) {
            workspaceText.setFocus();
        }
    }

    private void updateEnablement() {
        boolean file = fileButton.getSelection();
        fileText.setEnabled(file);
        fileToolbar.setEnabled(file);
        boolean workspace = workspaceButton.getSelection();
        workspaceText.setEnabled(workspace);
        workspaceToolbar.setEnabled(workspace);
    }

    /**
     * @see com.perforce.team.ui.IErrorProvider#setErrorDisplay(com.perforce.team.ui.IErrorDisplay)
     */
    public void setErrorDisplay(IErrorDisplay display) {

    }

    /**
     * @see com.perforce.team.ui.IErrorProvider#validate()
     */
    public void validate() {
        String message = null;

        resources = viewer.getSelectedResources();

        if (clipboardButton.getSelection()) {
            selected = clipboardStream;
        } else if (fileButton.getSelection()) {
            selected = fileStream;

            String text = fileText.getText().trim();
            if (text.length() > 0) {
                File file = new File(text);
                if (message == null && file.exists() && !file.canWrite()) {
                    message = Messages.LocationPage_FileNotWritable;
                }
                if (message == null && !file.isAbsolute()) {
                    message = Messages.LocationPage_AbsolutePath;
                }
                if (message == null && file.isDirectory()) {
                    message = Messages.LocationPage_DirectoryPath;
                }
            } else {
                if (message == null) {
                    message = Messages.LocationPage_AbsolutePath;
                }
            }
            fileStream.setPath(text);
        } else if (workspaceButton.getSelection()) {
            selected = workspaceStream;
            String text = workspaceText.getText().trim();
            IFile file = null;
            if (text.length() > 0 && Path.EMPTY.isValidPath(text)) {
                try {
                    IPath path = new Path(text);
                    file = ResourcesPlugin.getWorkspace().getRoot()
                            .getFile(path);
                    if (file != null) {
                        if (message == null && !file.getProject().exists()) {
                            message = MessageFormat.format(
                                    Messages.LocationPage_ProjectDoesNotExist,
                                    file.getProject().getName());
                        }
                        if (message == null && !file.getParent().exists()) {
                            message = MessageFormat.format(
                                    Messages.LocationPage_FolderDoesNotExist,
                                    file.getParent().getName());
                        }
                        if (message == null) {
                            File localFile = file.getLocation().toFile();
                            if (localFile != null && localFile.exists()
                                    && !localFile.canWrite()) {
                                message = Messages.LocationPage_FileNotWritable;
                            }
                        }
                    }
                } catch (Exception e) {
                    if (message == null) {
                        message = e.getLocalizedMessage();
                    }
                }
            }
            workspaceStream.setFile(file);
            if (message == null && file == null) {
                message = Messages.LocationPage_ValidWorkspacePath;
            }
        }

        if (message == null && resources.length == 0) {
            message = Messages.LocationPage_SelectResource;
        }

        p4Path = p4Text.getText().trim();
        if (p4Path.length() > 0) {
            File file = new File(p4Path);
            if (message == null && !file.exists()) {
                message = Messages.LocationPage_EnterP4Path;
            }
            if (message == null && !file.isAbsolute()) {
                message = Messages.LocationPage_EnterAbsoluteP4Path;
            }
            if (message == null && file.isDirectory()) {
                message = Messages.LocationPage_DirectoryP4Path;
            }
            if (message == null && !P4CoreUtils.isWindows()
                    && !helper.canExecute(file.getAbsolutePath())) {
                message = Messages.LocationPage_NonExecutableP4Path;
            }
        } else {
            if (message == null) {
                message = Messages.LocationPage_EnterP4Path;
            }
        }

        setErrorMessage(message);
        setPageComplete(message == null);
    }

    /**
     * Get resources to include in patch
     * 
     * @return resources
     */
    public IResource[] getResources() {
        return this.resources;
    }

    /**
     * Get patch stream
     * 
     * @return stream
     */
    public IPatchStream getStream() {
        return this.selected;
    }

    /**
     * Get path to p4 executable
     * 
     * @return path to p4 command-line client executable
     */
    public String getP4Path() {
        return this.p4Path;
    }
}
