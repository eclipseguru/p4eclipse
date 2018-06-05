package com.perforce.team.ui.dialogs;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import java.text.MessageFormat;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

import com.perforce.p4java.core.file.FileAction;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4ShelveFile;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4UIUtils;

/**
 * Perforce file properties dialog
 */
public class FilePropertiesDialog extends PropertyPage implements
        IWorkbenchPropertyPage {

    // If the file is opened by more users than this then use
    // a list to display them
    private static final int OPENED_THRESHOLD = 3;

    // P4 file being displayed
    private IP4File mainFile = null;

    private Text depotLabel;
    private Text clientLabel;
    private Text typeLabel;
    private Text headLabel;
    private Text haveLabel;
    private Text headActionLabel;
    private Text headChangeLabel;
    private Text lastModifiedLabel;

    private IP4ShelveFile[] shelved;
    private IP4File[] opened;

    /**
     * Don't show default or apply buttons
     */
    public FilePropertiesDialog() {
        noDefaultAndApplyButton();
    }

    private void load() {
        mainFile = getPerforceFile();
        opened = getOpenedBy(mainFile);
        shelved = getShelvedBy(mainFile);
    }

    private void createFileInfoArea(Composite parent) {
        if (mainFile == null) {
            DialogUtils.createLabel(parent,
                    Messages.FilePropertiesDialog_FileNotManagedByPerforce);
        } else {
            String fileType, headRevision, haveRevision;
            String headAction, headChange, modTime;
            if (mainFile.getHeadRevision() > 0) {
                fileType = mainFile.getHeadType();
                headRevision = Integer.toString(mainFile.getHeadRevision());
                haveRevision = Integer.toString(mainFile.getHaveRevision());
                FileAction headFileAction = mainFile.getHeadAction();
                if (headFileAction != null) {
                    headAction = headFileAction.toString().toLowerCase();
                } else {
                    headAction = ""; //$NON-NLS-1$
                }
                headChange = Integer.toString(mainFile.getHeadChange());
                modTime = P4UIUtils.formatLabelDate(mainFile.getHeadTime());
            } else {
                fileType = mainFile.getOpenedType();
                headRevision = ""; //$NON-NLS-1$
                haveRevision = ""; //$NON-NLS-1$
                headAction = ""; //$NON-NLS-1$
                headChange = ""; //$NON-NLS-1$
                modTime = ""; //$NON-NLS-1$
            }
            String remote = mainFile.getRemotePath();
            if (remote == null) {
                remote = ""; //$NON-NLS-1$
            }
            if (fileType == null) {
                fileType = ""; //$NON-NLS-1$
            }
            String client = mainFile.getClientPath();
            if (client == null) {
                client = ""; //$NON-NLS-1$
            }
            DialogUtils.createLabel(parent,
                    Messages.FilePropertiesDialog_DepotPath);
            depotLabel = DialogUtils.createSelectableLabel(parent, remote);
            DialogUtils.createLabel(parent,
                    Messages.FilePropertiesDialog_ClientPath);
            clientLabel = DialogUtils.createSelectableLabel(parent, client);
            DialogUtils.createLabel(parent,
                    Messages.FilePropertiesDialog_FileType);
            typeLabel = DialogUtils.createSelectableLabel(parent, fileType);
            DialogUtils.createLabel(parent,
                    Messages.FilePropertiesDialog_HeadRevision);
            headLabel = DialogUtils.createSelectableLabel(parent, headRevision);
            DialogUtils.createLabel(parent,
                    Messages.FilePropertiesDialog_HaveRevision);
            haveLabel = DialogUtils.createSelectableLabel(parent, haveRevision);
            DialogUtils.createLabel(parent,
                    Messages.FilePropertiesDialog_HaveAction);
            headActionLabel = DialogUtils.createSelectableLabel(parent,
                    headAction);
            DialogUtils.createLabel(parent,
                    Messages.FilePropertiesDialog_HeadChange);
            headChangeLabel = DialogUtils.createSelectableLabel(parent,
                    headChange);
            DialogUtils.createLabel(parent,
                    Messages.FilePropertiesDialog_LastModified);
            lastModifiedLabel = DialogUtils.createSelectableLabel(parent,
                    modTime);

            DialogUtils.createSeparator(parent, 2);

            Label label = DialogUtils.createLabel(parent,
                    Messages.FilePropertiesDialog_OpenedBy);
            if (opened.length == 0) {
                DialogUtils.createBlank(parent);
            } else if (opened.length > OPENED_THRESHOLD) {
                // Display opened by in a list box
                List list = DialogUtils.createList(parent, OPENED_THRESHOLD,
                        getListHeight(label));
                for (int i = 0; i < opened.length; i++) {
                    if (i > 0 && i < OPENED_THRESHOLD) {
                        DialogUtils.createBlank(parent);
                    }
                    list.add(getOpenedByDesc(opened[i]));
                }
            } else {
                for (int i = 0; i < opened.length; i++) {
                    if (i > 0) {
                        DialogUtils.createBlank(parent);
                    }
                    DialogUtils.createLabel(parent, getOpenedByDesc(opened[i]));
                }
            }

            DialogUtils.createLabel(parent,
                    Messages.FilePropertiesDialog_LockedBy);
            boolean locked = false;
            for (int i = 0; i < opened.length; i++) {
                if (opened[i] != null && opened[i].isLocked()) {
                    DialogUtils.createLabel(parent, opened[i].getUserName()
                            + "@" + opened[i].getClientName()); //$NON-NLS-1$
                    locked = true;
                    break;
                }
            }
            if (!locked) {
                DialogUtils.createLabel(parent, ""); //$NON-NLS-1$
            }

            Label shelveLabel = DialogUtils.createLabel(parent,
                    Messages.FilePropertiesDialog_ShelvedBy);
            if (shelved.length == 0) {
                DialogUtils.createBlank(parent);
            } else if (shelved.length > OPENED_THRESHOLD) {
                // Display opened by in a list box
                List list = DialogUtils.createList(parent, OPENED_THRESHOLD,
                        getListHeight(shelveLabel));
                for (int i = 0; i < shelved.length; i++) {
                    if (i > 0 && i < OPENED_THRESHOLD) {
                        DialogUtils.createBlank(parent);
                    }
                    list.add(getOpenedByDesc(shelved[i]));
                }
            } else {
                for (int i = 0; i < shelved.length; i++) {
                    if (i > 0) {
                        DialogUtils.createBlank(parent);
                    }
                    DialogUtils
                            .createLabel(parent, getOpenedByDesc(shelved[i]));
                }
            }
        }
    }

    /**
     * Create dialog controls
     * 
     * @param parent
     * @return - main control
     */
    @Override
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout cLayout = new GridLayout(2, false);
        cLayout.marginHeight = 0;
        cLayout.marginWidth = 0;
        composite.setLayout(cLayout);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        load();
        createFileInfoArea(composite);
        return composite;
    }

    /**
     * Get opened by text
     */
    private String getOpenedByDesc(IP4ShelveFile file) {
        return MessageFormat.format(Messages.FilePropertiesDialog_OpenedByDesc,
                file.getUser(), file.getWorkspace(), file.getId());
    }

    /**
     * Get opened by text
     */
    private String getOpenedByDesc(IP4File file) {
        FileAction fileAction = file.getAction();
        if (fileAction == null) {
            return MessageFormat.format(
                    Messages.FilePropertiesDialog_OpenedByDesc,
                    file.getUserName(), file.getClientName(),
                    file.getChangelistId());
        } else {
            return MessageFormat
                    .format(Messages.FilePropertiesDialog_OpenedByDescWithAction,
                            file.getUserName(), file.getClientName(), file
                                    .getChangelistId(), fileAction.toString()
                                    .toLowerCase());
        }
    }

    /**
     * Get (and refresh) perforce file info
     */
    private IP4File getPerforceFile() {
        IP4File p4File = null;
        IP4Resource p4Resource = null;
        Object element = getElement();
        if (element instanceof IP4Resource) {
            p4Resource = (IP4Resource) element;
        } else {
            IFile file = (IFile) getElement();
            p4Resource = P4ConnectionManager.getManager().getResource(file);
        }
        if (p4Resource instanceof IP4File) {
            if (p4Resource != null) {
                IP4Connection connection = p4Resource.getConnection();

                // Adapt read only resources by requesting it from the
                // connection's cache
                if (p4Resource.isReadOnly()) {
                    IP4File adapted = connection.getFile(((IP4File) p4Resource)
                            .getP4JFile());
                    if (adapted != null) {
                        p4Resource = adapted;
                    }
                }

                if (connection != null && !connection.isOffline()
                        && connection.isConnected()) {
                    p4Resource.refresh();
                }
            }
            if (((IP4File) p4Resource).getP4JFile() != null) {
                p4File = (IP4File) p4Resource;
            }
        }
        return p4File;
    }

    private IP4ShelveFile[] getShelvedBy(IP4File file) {
        IP4ShelveFile[] files = null;
        if (file != null) {
            files = file.getShelvedVersions();
        } else {
            files = new IP4ShelveFile[0];
        }
        return files;
    }

    /**
     * Get who has this file opened
     */
    private IP4File[] getOpenedBy(IP4File file) {
        IP4File[] files = new IP4File[0];
        if (file != null) {
            IP4Connection connection = file.getConnection();
            if (connection != null) {
                String path = file.getActionPath();
                if (path != null) {
                    files = connection.getOpenedBy(path);
                }
            }
        }
        return files;
    }

    /**
     * Get height of opened by list box
     */
    private int getListHeight(Label label) {
        return label.computeSize(SWT.DEFAULT, SWT.DEFAULT).y * OPENED_THRESHOLD
                * 2;
    }

    /**
     * Get depot label
     * 
     * @return - depot text
     */
    public String getDepotLabel() {
        return this.depotLabel.getText();
    }

    /**
     * Get client label
     * 
     * @return - client text
     */
    public String getClientLabel() {
        return this.clientLabel.getText();
    }

    /**
     * Get type label
     * 
     * @return - type text
     */
    public String getTypeLabel() {
        return this.typeLabel.getText();
    }

    /**
     * Get head label
     * 
     * @return - head text
     */
    public String getHeadLabel() {
        return this.headLabel.getText();
    }

    /**
     * Get head action label
     * 
     * @return - head action text
     */
    public String getHeadActionLabel() {
        return this.headActionLabel.getText();
    }

    /**
     * Get head change label
     * 
     * @return - head change text
     */
    public String getHeadChangeLabel() {
        return this.headChangeLabel.getText();
    }

    /**
     * Get last modified label
     * 
     * @return - last modified text
     */
    public String getLastModifiedLabel() {
        return this.lastModifiedLabel.getText();
    }

    /**
     * Get have label
     * 
     * @return - have label text
     */
    public String getHaveLabel() {
        return this.haveLabel.getText();
    }

}
