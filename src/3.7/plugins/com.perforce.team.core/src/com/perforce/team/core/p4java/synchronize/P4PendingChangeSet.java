/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java.synchronize;

import java.text.MessageFormat;

import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.impl.generic.core.Changelist;
import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Workspace;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.internal.core.subscribers.ActiveChangeSet;
import org.eclipse.team.internal.core.subscribers.ActiveChangeSetManager;
import org.osgi.service.prefs.Preferences;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class P4PendingChangeSet extends ActiveChangeSet implements IP4ChangeSet {

    /**
     * PENDING_PRIORITY
     */
    public static final int PENDING_PRIORITY = 2;

    /**
     * CTX_LOCAL_NAME
     */
    public static final String CTX_LOCAL_NAME = "localName"; //$NON-NLS-1$

    /**
     * CTX_CONNECTION
     */
    public static final String CTX_CONNECTION = "connection"; //$NON-NLS-1$

    /**
     * CTX_CHANGELIST
     */
    public static final String CTX_CHANGELIST = "changelist"; //$NON-NLS-1$

    /**
     * CTX_SAVED_COMMENT
     */
    public static final String CTX_SAVED_COMMENT = "savedComment"; //$NON-NLS-1$

    /**
     * Pending changelist
     */
    protected IP4PendingChangelist list;

    /**
     * Changelist id
     */
    protected int id = IChangelist.UNKNOWN;

    /**
     * Valid flag
     */
    protected boolean valid = true;

    /**
     * Create a p4 active change set
     * 
     * @param manager
     * @param name
     */
    public P4PendingChangeSet(ActiveChangeSetManager manager, String name) {
        super(manager, name);
    }

    /**
     * Create a p4 active change set
     * 
     * @param manager
     * @param list
     */
    public P4PendingChangeSet(ActiveChangeSetManager manager,
            IP4PendingChangelist list) {
        this(manager, (String) null);
        this.list = list;
        if (this.list != null) {
            id = this.list.getId();
        }
        updateTitle();
    }

    /**
     * @see org.eclipse.team.internal.core.subscribers.ActiveChangeSet#isUserCreated()
     */
    @Override
    public boolean isUserCreated() {
        return true;
    }

    /**
     * @see com.perforce.team.core.p4java.synchronize.IP4ChangeSet#getConnection()
     */
    public IP4Connection getConnection() {
        return this.list != null ? this.list.getConnection() : null;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof P4PendingChangeSet) {
            return this.list != null
                    && this.list.equals(((P4PendingChangeSet) obj).list);
        }
        return false;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        IP4Connection connection = getConnection();
        if (connection != null && this.id != IChangelist.UNKNOWN) {
            return (connection.getParameters().toString() + this.id).hashCode();
        }
        return super.hashCode();
    }

    private IResource getResource(IDiff diff) {
        IResource resource = null;
        if (diff != null) {
            IPath path = diff.getPath();
            if (path != null) {
                resource = ResourcesPlugin.getWorkspace().getRoot()
                        .getFile(path);
            }
        }
        return resource;
    }

    /**
     * @see org.eclipse.team.internal.core.subscribers.ActiveChangeSet#isValidChange(org.eclipse.team.core.diff.IDiff)
     */
    @Override
    protected boolean isValidChange(IDiff diff) {
        boolean valid = super.isValidChange(diff);
        if (valid) {
            valid = false;
            IResource resource = getResource(diff);
            if (resource instanceof IFile) {
                IP4Connection connection = P4Workspace.getWorkspace()
                        .getConnection(resource.getProject());
                if (connection != null) {
                    IP4Resource p4Resource = connection.getResource(resource);
                    if (p4Resource instanceof IP4File) {
                        IP4File p4File = (IP4File) p4Resource;
                        if (p4File.isOpened() && p4File.openedByOwner()) {
                            if (this.list == null) {
                                this.list = p4File.getChangelist(true, true);
                                if (this.list != null) {
                                    valid = true;
                                    this.id = list.getId();
                                }
                            } else {
                                valid = this.list.equals(p4File.getChangelist(
                                        true, true));
                            }
                        }
                    }
                }
            }
        }
        return valid;
    }

    /**
     * Refresh the active change set from the underlying p4 pending changelist
     */
    public void refresh() {
        updateTitle();
    }

    /**
     * Get underlying pending changelist
     * 
     * @return - p4 pending changelist
     */
    public IP4PendingChangelist getChangelist() {
        return this.list;
    }

    /**
     * @see com.perforce.team.core.p4java.synchronize.IP4ChangeSet#getId()
     */
    public int getId() {
        return this.id;
    }

    /**
     * @see org.eclipse.team.internal.core.subscribers.ActiveChangeSet#save(org.osgi.service.prefs.Preferences)
     */
    @Override
    public void save(Preferences prefs) {
		if (prefs != null) {
			super.save(prefs);
			if (this.list != null) {
				String params = this.list.getConnection().getParameters()
						.toString();
				prefs.put(CTX_CONNECTION, params);
				prefs.putInt(CTX_CHANGELIST, this.id);
			}
		}
    }

    /**
     * @see org.eclipse.team.internal.core.subscribers.ActiveChangeSet#getComment()
     */
    @Override
    public String getComment() {
        if (this.list != null) {
            return this.list.getDescription();
        }
        return super.getComment();
    }

    /**
     * Update title of pending changeset
     */
    protected void updateTitle() {
        if (this.list != null) {
            if (this.list.isDefault()) {
                setTitle(Messages.DefaultChange);
            } else {
                setTitle(MessageFormat.format(Messages.PendingChange, getId()));
            }
        }
    }

    /**
     * @see org.eclipse.team.internal.core.subscribers.ActiveChangeSet#init(org.osgi.service.prefs.Preferences)
     */
    @Override
    public void init(Preferences prefs) {
        if (prefs != null) {
        	super.init(prefs);
            String connectionPref = prefs.get(CTX_CONNECTION, null);
            int id = prefs.getInt(CTX_CHANGELIST, -1);
            if (connectionPref != null && id > Changelist.DEFAULT) {
                ConnectionParameters params = new ConnectionParameters(
                        connectionPref);
                if (P4Workspace.getWorkspace().containsConnection(params)) {
                    IP4Connection connection = P4Workspace.getWorkspace()
                            .getConnection(params);
                    if (connection != null) {
                        // getNames().register(connection, id, getName());
                    }
                }
            }
            String comment = prefs.get(CTX_SAVED_COMMENT, null);
            if (comment != null) {
                setComment(comment);
            }
        }
    }

    /**
     * @see com.perforce.team.core.p4java.synchronize.IP4ChangeSet#isValid()
     */
    public boolean isValid() {
        return this.valid;
    }

    /**
     * @see com.perforce.team.core.p4java.synchronize.IP4ChangeSet#setValid(boolean)
     */
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    /**
     * @see com.perforce.team.core.p4java.synchronize.IP4ChangeSet#getPriority()
     */
    public int getPriority() {
        return PENDING_PRIORITY;
    }

    /**
     * Should the {@link #getComment()} value be used to populate the changelist
     * description on submit?
     * 
     * @return - true to use comment, false to not
     */
    public boolean useCommentOnSubmit() {
        return false;
    }

}
