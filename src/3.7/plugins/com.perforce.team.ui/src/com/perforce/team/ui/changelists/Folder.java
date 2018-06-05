/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.changelists;

import com.perforce.p4java.core.IDepot.DepotType;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Depot;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.WorkbenchAdapter;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Folder extends WorkbenchAdapter {

    /**
     * Folder builder
     */
    public static class FolderBuilder {

        private Object[] files;
        private Type type;

        /**
         * Create a folder builder
         * 
         * @param files
         * @param type
         */
        public FolderBuilder(Object[] files, Type type) {
            this.files = files;
            this.type = type;
        }

        /**
         * Create a folder
         * 
         * @param parent
         * @param path
         * @param type
         * @return folder
         */
        protected Folder createFolder(Folder parent, String path, Type type) {
            return new Folder(parent, path, type);
        }

        private Folder getTreeParent(String path, Map<String, Folder> folders,
                Map<String, Folder> roots, Type type) {
            Folder folder = null;
            if (path.lastIndexOf('/') != 1) {
                String parentPath = path.substring(0, path.lastIndexOf('/'));
                Folder parent = null;
                if (folders.containsKey(parentPath)) {
                    parent = folders.get(parentPath);
                } else {
                    parent = getTreeParent(parentPath, folders, roots, type);
                    folders.put(parentPath, parent);
                }
                folder = createFolder(parent, path, type);
                parent.add(folder);
            } else {
                folder = createFolder(null, path, type);
                roots.put(path, folder);
            }

            return folder;
        }

        /**
         * Build folders for current resources
         * 
         * @return non-null but possibly empty array of folders
         */
        public Folder[] build() {
            return build(false);
        }

        /**
         * Build folders for current resources
         * 
         * @param compress
         * 
         * @return non-null but possibly empty array of folders
         */
        public Folder[] build(boolean compress) {
            Map<String, Folder> roots = new HashMap<String, Folder>();
            Map<String, Folder> folders = new HashMap<String, Folder>();
            IP4Connection connection = null;
            for (Object element : files) {
                IP4Resource file = P4CoreUtils.convert(element,
                        IP4Resource.class);
                if (file != null) {
                    if (connection == null) {
                        connection = file.getConnection();
                    }
                    String path = file.getRemotePath();
                    if (path != null) {
                        String parentPath = path.substring(0,
                                path.lastIndexOf('/'));
                        Folder parent = null;
                        if (folders.containsKey(parentPath)) {
                            parent = folders.get(parentPath);
                        } else {
                            parent = getTreeParent(parentPath, folders, roots,
                                    type);
                            folders.put(parentPath, parent);
                        }
                        parent.add(element);
                    }
                }
            }
            Folder[] treeFolders = roots.values().toArray(
                    new Folder[roots.size()]);
            if (connection != null) {
                for (Folder folder : treeFolders) {
                    folder.setConnection(connection);
                }
            }
            if (compress) {
                compressFolders(treeFolders);
            }
            return treeFolders;
        }
    }

    /**
     * Display type
     */
    public static enum Type {

        /**
         * FLAT
         */
        FLAT,

        /**
         * TREE
         */
        TREE,

        /**
         * COMPRESSED
         */
        COMPRESSED,

    }

    /**
     * Build a tree folder model using the specified resources
     * 
     * @param files
     * @param type
     * @return non-null but possibly empty array of folders
     */
    public static Folder[] buildTree(Object[] files, Type type) {
        FolderBuilder builder = new FolderBuilder(files, type);
        return builder.build();
    }

    /**
     * Set connection for folder
     * 
     * @param connection
     */
    public void setConnection(IP4Connection connection) {
        this.connection = connection;
    }

    /**
     * Compress the folders specified
     * 
     * @param folders
     */
    public static void compressFolders(Folder[] folders) {
        if (folders != null) {
            for (Folder folder : folders) {
                folder.compress();
            }
        }
    }

    /**
     * Compress the child folders of the specified folder
     * 
     * @param folder
     */
    public static void compressFolders(Folder folder) {
        if (folder == null) {
            return;
        }
        folder.compress();
    }

    private Folder parent;
    private String path;
    private String name = ""; //$NON-NLS-1$

    /**
     * Connection
     */
    protected IP4Connection connection = null;

    /**
     * Children
     */
    protected List<Object> children = new ArrayList<Object>();

    /**
     * Type
     */
    protected Type type;

    /**
     * Folder count
     */
    protected int folderCount = 0;

    /**
     * Child count
     */
    protected int childCount = 0;

    /**
     * Create a folder with specified parent and path
     * 
     * @param parent
     * @param path
     * @param type
     */
    public Folder(Folder parent, String path, Type type) {
        this.parent = parent;
        this.path = path;
        if (this.path == null) {
            this.path = ""; //$NON-NLS-1$
        }
        this.type = type;
        this.name = generateName();
    }

    /**
     * Does this folder only contain other folders?
     * 
     * @return - true if folder can be compressed
     */
    public boolean canCompress() {
        return this.folderCount == 0
                || (this.folderCount == 1 && this.childCount == 0);
    }

    /**
     * Does this folder contain sub-folders?
     * 
     * @return - true if contains subfolders
     */
    public boolean containsFolders() {
        return this.children.size() > this.childCount;
    }

    /**
     * Does this folder contains files
     * 
     * @return - true if this folder contains files
     */
    public boolean containsFiles() {
        return this.childCount > 0;
    }

    private String generateName() {
        if (this.type == Type.TREE) {
            int lastSlash = this.path.lastIndexOf('/');
            if (lastSlash + 1 < this.path.length()) {
                if (lastSlash > 1) {
                    return this.path.substring(lastSlash + 1);
                } else {
                    return this.path;
                }
            }
        } else if (this.type == Type.COMPRESSED) {
            if (this.parent != null
                    && this.path.length() > this.parent.path.length() + 1) {
                return this.path.substring(this.parent.path.length() + 1);
            }
        }
        return this.path;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof Folder) {
            Folder folder = (Folder) obj;
            if (path.equals(folder.path)) {
                return parent != null ? parent.equals(folder.parent) : true;
            }
        }
        return false;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return path.hashCode();
    }

    /**
     * Get path
     * 
     * @return - folder path
     */
    public String getPath() {
        return this.path;
    }

    /**
     * Set the path of this folder
     * 
     * @param path
     */
    public void setPath(String path) {
        if (path != null) {
            this.path = path;
            this.name = generateName();
        }
    }

    /**
     * Add a child object to this folder
     * 
     * @param child
     */
    public void add(Object child) {
        if (child != null) {
            if (!(child instanceof Folder)) {
                this.childCount++;
            } else {
                this.folderCount++;
                ((Folder) child).parent = this;
            }
            this.children.add(child);
        }
    }

    /**
     * Remove a child object from this folder
     * 
     * @param child
     */
    public void remove(Object child) {
        if (child != null && this.children.remove(child)) {
            if (child instanceof Folder) {
                this.folderCount--;
            } else {
                this.childCount--;
            }
        }
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
     */
    @Override
    public Object[] getChildren(Object o) {
        return this.children.toArray();
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
     */
    @Override
    public ImageDescriptor getImageDescriptor(Object object) {
        if (isDepot()) {
            ImageDescriptor depotIcon = null;
            if (connection != null) {
                P4Depot depot = connection.getDepot(getLabel(this));
                if (depot != null) {
                    DepotType type = depot.getType();
                    if (type != null) {
                        switch (type) {
                        case REMOTE:
                            depotIcon = PerforceUIPlugin
                                    .getDescriptor(IPerforceUIConstants.IMG_DEPOT_REMOTE);
                            break;
                        case SPEC:
                            depotIcon = PerforceUIPlugin
                                    .getDescriptor(IPerforceUIConstants.IMG_DEPOT_SPEC);
                            break;
                        default:
                            break;
                        }
                    }
                }
            }
            if (depotIcon == null) {
                depotIcon = PerforceUIPlugin
                        .getDescriptor(IPerforceUIConstants.IMG_DEPOT_DEPOT);
            }
            return depotIcon;
        } else {
            return PerforceUIPlugin
                    .getDescriptor(IPerforceUIConstants.IMG_DEPOT_FOLDER);
        }
    }

    private boolean isDepot() {
        boolean depot = false;
        if (getParent(this) == null) {
            if (type == Type.TREE) {
                depot = true;
            } else if (type == Type.COMPRESSED) {
                depot = this.path.lastIndexOf('/') == 1;
            }
        }
        return depot;
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
     */
    @Override
    public String getLabel(Object o) {
        if (isDepot() && this.name.startsWith("//")) { //$NON-NLS-1$
            return this.name.substring(2);
        } else {
            return this.name;
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.name;
    }

    /**
     * Compress the folder and all sub-folders
     */
    public void compress() {
        if (this.canCompress()) {
            Object[] children = this.getChildren(this);
            for (Object child : children) {
                if (child instanceof Folder) {
                    Folder subFolder = (Folder) child;
                    this.remove(subFolder);
                    this.setPath(subFolder.getPath());
                    for (Object fChild : subFolder.getChildren(subFolder)) {
                        this.add(fChild);
                    }
                    this.compress();
                    break;
                }
            }
        }
        for (Object child : this.getChildren(this)) {
            if (child instanceof Folder) {
                ((Folder) child).compress();
            }
        }
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
     */
    @Override
    public Object getParent(Object o) {
        return this.parent;
    }

}
