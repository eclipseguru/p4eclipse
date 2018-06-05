/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.editor;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.P4Storage;

import java.io.InputStream;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class DepotFileEditorInput extends P4BaseEditorInput {

    private IP4File file = null;
    private String revision = null;
    private String name = null;

    /**
     * Create a new file editor input around the specified revision. Specifying
     * null for a revision will cause the currently known head revision to be
     * used.
     * 
     * @param file
     * @param revision
     */
    public DepotFileEditorInput(IP4File file, String revision) {
        this.file = file;
        this.revision = revision;
    }

    /**
     * Create a new file editor input configured around the currently known head
     * revision number
     * 
     * @param file
     */
    public DepotFileEditorInput(IP4File file) {
        this(file, null);
    }

    /**
     * @see org.eclipse.ui.IStorageEditorInput#getStorage()
     */
    public IStorage getStorage() throws CoreException {
        if (storage == null) {
            storage = new P4Storage() {

                @Override
                public IPath getFullPath() {
                    return getTempStoragePath();
                }

                @Override
                public String getName() {
                    return file.getName();
                }

                public InputStream getContents() throws CoreException {
                    IStorage wrapStorage = new P4Storage() {

                        public InputStream getContents() throws CoreException {
                            if (revision == null) {
                                return file.getHeadContents();
                            } else {
                                return file.getRemoteContents(revision);
                            }
                        }
                    };
                    return getStorageContents(wrapStorage);
                }
            };
        }
        return this.storage;
    }

    /**
     * @see org.eclipse.ui.IEditorInput#getName()
     */
    public String getName() {
        if (name == null) {
            if (revision == null) {
                name = file.getName() + " #" + file.getHeadRevision(); //$NON-NLS-1$
            } else {
                name = file.getName() + " " + revision; //$NON-NLS-1$
            }
        }
        return name;
    }

    /**
     * @see org.eclipse.ui.IEditorInput#getToolTipText()
     */
    public String getToolTipText() {
        String tipRevision = null;
        if (this.revision != null) {
            tipRevision = revision;
        } else {
            tipRevision = "#" + file.getHeadRevision(); //$NON-NLS-1$
        }
        return file.getActionPath() + tipRevision;
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
     */
    @Override
    public String getLabel(Object o) {
        if (storage != null) {
            return storage.getName();
        }
        return null;
    }

    /**
     * @see com.perforce.team.ui.editor.P4BaseEditorInput#getSuffix()
     */
    @Override
    protected String getSuffix() {
        String name = file.getName();
        if (name != null) {
            int lastDot = name.lastIndexOf('.');
            if (lastDot > 0) {
                return name.substring(lastDot);
            }
        }
        return super.getSuffix();
    }

    /**
     * @see com.perforce.team.ui.editor.P4BaseEditorInput#getPrefix()
     */
    @Override
    protected String getPrefix() {
        String name = file.getName();
        if (name != null) {
            int lastDot = name.lastIndexOf('.');
            if (lastDot > 0) {
                return name.substring(0, lastDot);
            }
        }
        return super.getSuffix();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof DepotFileEditorInput) {
            DepotFileEditorInput other = (DepotFileEditorInput) obj;
            if (this.file.equals(other.file)) {
                if (revision != null && other.revision != null) {
                    return revision.equals(other.revision);
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
    	// to prevent coverity complain HE_EQUALS_NO_HASHCODE
    	if(name!=null)
    		return name.hashCode();
    	return super.hashCode();
    }

}
