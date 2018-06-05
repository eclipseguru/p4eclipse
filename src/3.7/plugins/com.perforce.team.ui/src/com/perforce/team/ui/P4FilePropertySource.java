/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui;

import com.perforce.p4java.core.file.FileAction;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class P4FilePropertySource implements IPropertySource {

    private static final List<IPropertyDescriptor> CORE_DESCRIPTORS;

    private static final String DEPOT_PATH = "DEPOT_PATH"; //$NON-NLS-1$

    private static final String HAVE_REV = "HAVE_REV"; //$NON-NLS-1$

    private static final String HEAD_REV = "HEAD_REV"; //$NON-NLS-1$

    private static final String FILE_TYPE = "FILE_TYPE"; //$NON-NLS-1$

    private static final String HEAD_ACTION = "HEAD_ACTION"; //$NON-NLS-1$

    private static final String CLIENT_PATH = "CLIENT_PATH"; //$NON-NLS-1$

    private static final String MOD_TIME = "MOD_TIME"; //$NON-NLS-1$

    private static final String OTHER_USER = "OTHER_USER"; //$NON-NLS-1$

    private static final String OTHER_CHANGELIST = "OTHER_CHANGELIST"; //$NON-NLS-1$

    private static final String OTHER_CLIENT = "OTHER_CLIENT"; //$NON-NLS-1$

    private static final String OTHER_ACTION = "OTHER_ACTION"; //$NON-NLS-1$

    private static final String SERVER = "SERVER"; //$NON-NLS-1$

    static {
        CORE_DESCRIPTORS = new ArrayList<IPropertyDescriptor>();
        CORE_DESCRIPTORS.add(new PropertyDescriptor(CLIENT_PATH,
                Messages.P4FilePropertySource_WorkspaceLocation));
        CORE_DESCRIPTORS.add(new PropertyDescriptor(DEPOT_PATH,
                Messages.P4FilePropertySource_DepotLocation));
        CORE_DESCRIPTORS.add(new PropertyDescriptor(HAVE_REV,
                Messages.P4FilePropertySource_HaveRevision));
        CORE_DESCRIPTORS.add(new PropertyDescriptor(HEAD_REV,
                Messages.P4FilePropertySource_HeadRevision));
        CORE_DESCRIPTORS.add(new PropertyDescriptor(MOD_TIME,
                Messages.P4FilePropertySource_LastChanged));
        CORE_DESCRIPTORS.add(new PropertyDescriptor(FILE_TYPE,
                Messages.P4FilePropertySource_FileType));
        CORE_DESCRIPTORS.add(new PropertyDescriptor(HEAD_ACTION,
                Messages.P4FilePropertySource_HeadAction));
        CORE_DESCRIPTORS.add(new PropertyDescriptor(SERVER,
                Messages.P4FilePropertySource_ServerAddress));
    }

    private IP4File file;
    private IP4File[] openedFiles = null;

    /**
     * Creates a new p4 file property source
     * 
     * @param file
     */
    public P4FilePropertySource(IP4File file) {
        this.file = file;
    }

    /**
     * @see org.eclipse.ui.views.properties.IPropertySource#getEditableValue()
     */
    public Object getEditableValue() {
        return null;
    }

    /**
     * @see org.eclipse.ui.views.properties.IPropertySource#getPropertyDescriptors()
     */
    public IPropertyDescriptor[] getPropertyDescriptors() {
        List<IPropertyDescriptor> fileDescriptors = new ArrayList<IPropertyDescriptor>(
                CORE_DESCRIPTORS);

        if (this.file.openedElsewhere()) {
            IP4Connection connection = file.getConnection();
            if (connection != null) {
                String path = file.getActionPath();
                if (path != null) {
                    this.openedFiles = connection.getOpenedBy(path);
                    int totalOpened=(this.openedFiles==null)?0:this.openedFiles.length;
                    
                    for (int i = 0; i < totalOpened; i++) {
                        int marker = i + 1;
                        String category = Messages.P4FilePropertySource_CheckedOutByUser
                                + marker;

                        PropertyDescriptor clientDescriptor = new PropertyDescriptor(
                                OTHER_CLIENT + i,
                                Messages.P4FilePropertySource_Client + marker);
                        clientDescriptor.setCategory(category);
                        fileDescriptors.add(clientDescriptor);

                        PropertyDescriptor userDescriptor = new PropertyDescriptor(
                                OTHER_USER + i,
                                Messages.P4FilePropertySource_User + marker);
                        userDescriptor.setCategory(category);
                        fileDescriptors.add(userDescriptor);

                        PropertyDescriptor clDescriptor = new PropertyDescriptor(
                                OTHER_CHANGELIST + i,
                                Messages.P4FilePropertySource_PendingChangelist
                                        + marker);
                        clDescriptor.setCategory(category);
                        fileDescriptors.add(clDescriptor);

                        FileAction fileAction = this.openedFiles[i].getAction();
                        if (fileAction != null) {
                            PropertyDescriptor actionDescriptor = new PropertyDescriptor(
                                    OTHER_ACTION + i,
                                    Messages.P4FilePropertySource_Action
                                            + marker);
                            actionDescriptor.setCategory(category);
                            fileDescriptors.add(actionDescriptor);
                        }
                    }
                }
            }
        }

        return fileDescriptors.toArray(new IPropertyDescriptor[0]);
    }

    /**
     * @see org.eclipse.ui.views.properties.IPropertySource#getPropertyValue(java.lang.Object)
     */
    public Object getPropertyValue(Object id) {
        Object value = null;
        if (file != null) {
            if (HAVE_REV.equals(id)) {
                value = Integer.toString(file.getHaveRevision());
            } else if (HEAD_REV.equals(id)) {
                value = Integer.toString(file.getHeadRevision());
            } else if (FILE_TYPE.equals(id)) {
                value = file.getHeadType();
            } else if (DEPOT_PATH.equals(id)) {
                value = file.getRemotePath();
            } else if (HEAD_ACTION.equals(id)) {
                value = file.getHeadAction();
            } else if (MOD_TIME.equals(id)) {
                value = P4UIUtils.formatDate(file.getHeadTime());
            } else if (CLIENT_PATH.equals(id)) {
                value = file.getLocalPath();
            } else if (SERVER.equals(id)) {
                IP4Connection connection = file.getConnection();
                if (connection != null) {
                    value = connection.getAddress();
                }
            } else {
                String otherId = id.toString();
                if (otherId.startsWith(OTHER_USER)) {
                    int index = Integer.parseInt(otherId.substring(OTHER_USER
                            .length()));
                    value = this.openedFiles[index].getUserName();
                } else if (otherId.startsWith(OTHER_CHANGELIST)) {
                    int index = Integer.parseInt(otherId
                            .substring(OTHER_CHANGELIST.length()));
                    int changelist = this.openedFiles[index].getChangelistId();
                    if (changelist == 0) {
                        value = Messages.P4FilePropertySource_DefaultChangelistId;
                    } else {
                        value = Integer.toString(changelist);
                    }
                } else if (otherId.startsWith(OTHER_CLIENT)) {
                    int index = Integer.parseInt(otherId.substring(OTHER_CLIENT
                            .length()));
                    value = this.openedFiles[index].getClientName();
                } else if (otherId.startsWith(OTHER_ACTION)) {
                    int index = Integer.parseInt(otherId.substring(OTHER_ACTION
                            .length()));
                    value = this.openedFiles[index].getAction().toString()
                            .toLowerCase();
                }
            }
        }
        return value;
    }

    /**
     * @see org.eclipse.ui.views.properties.IPropertySource#isPropertySet(java.lang.Object)
     */
    public boolean isPropertySet(Object id) {
        return false;
    }

    /**
     * @see org.eclipse.ui.views.properties.IPropertySource#resetPropertyValue(java.lang.Object)
     */
    public void resetPropertyValue(Object id) {

    }

    /**
     * @see org.eclipse.ui.views.properties.IPropertySource#setPropertyValue(java.lang.Object,
     *      java.lang.Object)
     */
    public void setPropertyValue(Object id, Object value) {

    }

}
