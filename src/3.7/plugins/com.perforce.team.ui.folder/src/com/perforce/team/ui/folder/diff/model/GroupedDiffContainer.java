/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.model;

import com.perforce.p4java.core.IFileDiff.Status;
import com.perforce.team.core.folder.IP4DiffFile;
import com.perforce.team.core.p4java.P4Resource;
import com.perforce.team.ui.changelists.Folder;
import com.perforce.team.ui.changelists.Folder.FolderBuilder;
import com.perforce.team.ui.changelists.Folder.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class GroupedDiffContainer {

    /**
     * Sorter
     */
    public static final Comparator<Object> SORTER = new Comparator<Object>() {

        public int compare(Object e1, Object e2) {
            if (e1 instanceof Folder && e2 instanceof IP4DiffFile) {
                return -1;
            } else if (e1 instanceof IP4DiffFile && e2 instanceof Folder) {
                return 1;
            } else if (e1 instanceof Folder && e2 instanceof Folder) {
                return ((Folder) e1).getLabel(e1).compareTo(
                        ((Folder) e2).getLabel(e2));
            } else if (e1 instanceof IP4DiffFile && e2 instanceof IP4DiffFile) {
                return ((IP4DiffFile) e1).getRemotePath().compareToIgnoreCase(
                        ((IP4DiffFile) e2).getRemotePath());
            }
            return 0;
        }
    };

    private class DiffGroup implements IGroupProvider {

        private Object treeParent = null;
        private Object compressedParent = null;

        private Object treePair = null;
        private Object compressedPair = null;

        private Object flatPair = null;

        /**
         * @see com.perforce.team.ui.folder.diff.model.IGroupProvider#getParent(com.perforce.team.ui.folder.diff.model.FileEntry)
         */
        public Object getParent(FileEntry entry) {
            Type parentType = getType();
            if (parentType != null) {
                switch (parentType) {
                case TREE:
                    return this.treeParent;
                case COMPRESSED:
                    return this.compressedParent;
                }
            }
            return null;
        }

        /**
         * @see com.perforce.team.ui.folder.diff.model.IGroupProvider#getType()
         */
        public Type getType() {
            return type;
        }

        /**
         * @see com.perforce.team.ui.folder.diff.model.IGroupProvider#getUniquePair(com.perforce.team.ui.folder.diff.model.FileEntry)
         */
        public Object getUniquePair(FileEntry entry) {
            Type parentType = getType();
            if (parentType != null) {
                switch (parentType) {
                case TREE:
                    return this.treePair;
                case COMPRESSED:
                    return this.compressedPair;
                case FLAT:
                    return this.flatPair;
                }
            }
            return null;
        }

    }

    private class GroupFolderBuilder extends FolderBuilder {

        /**
         * @param files
         * @param type
         */
        public GroupFolderBuilder(Object[] files, Type type) {
            super(files, type);
        }

        /**
         * @see com.perforce.team.ui.changelists.Folder.FolderBuilder#createFolder(com.perforce.team.ui.changelists.Folder,
         *      java.lang.String, com.perforce.team.ui.changelists.Folder.Type)
         */
        @Override
        protected Folder createFolder(Folder parent, String path, Type type) {
            return new GroupFolder(parent, path, type);
        }

        /**
         * @see com.perforce.team.ui.changelists.Folder.FolderBuilder#build(boolean)
         */
        @Override
        public Folder[] build(boolean compress) {
            Folder[] folders = super.build(compress);
            for (Folder folder : folders) {
                ((GroupFolder) folder).replace();
            }
            return folders;
        }
    }

    /**
     * Group folder
     */
    public static class GroupFolder extends Folder {

        private boolean finished = false;
        private boolean project = false;

        /**
         * @param parent
         * @param path
         * @param type
         */
        public GroupFolder(Folder parent, String path, Type type) {
            super(parent, path, type);
        }


        @Override
        public boolean equals(Object obj) {
        	// override to prevent coverity from complaining.
        	return obj instanceof GroupFolder && super.equals(obj);
        }
        
        @Override
        public int hashCode() {
        	// override to prevent coverity from complaining: FB.EQ_COMPARETO_USE_OBJECT_EQUALS
        	return super.hashCode();
        }

        /**
         * @see com.perforce.team.ui.changelists.Folder#remove(java.lang.Object)
         */
        @Override
        public void remove(Object child) {
            if (!finished) {
                super.remove(child);
            }
        }

        private void replace() {
            if (!containsFiles() || !containsFolders()) {
                GroupFolder parent = (GroupFolder) getParent(this);
                if (parent != null) {
                    parent.remove(this);
                }
            }
            List<Object> validChildren = new ArrayList<Object>();
            for (Object child : this.getChildren(this)) {
                if (child instanceof FileEntry) {
                    validChildren.add(((FileEntry) child).getFile());
                } else if (child instanceof GroupFolder) {
                    GroupFolder sub = (GroupFolder) child;
                    sub.replace();
                    if (sub.containsFiles() || sub.containsFolders()) {
                        validChildren.add(sub);
                    }
                }
            }
            this.finished = true;
            this.children.clear();
            Collections.sort(validChildren, SORTER);
            this.children = validChildren;
        }

        /**
         * @see com.perforce.team.ui.changelists.Folder#add(java.lang.Object)
         */
        @Override
        public void add(Object child) {
            if (!finished) {
                if (child instanceof FileEntry) {
                    FileEntry entry = (FileEntry) child;
                    if (!project && ".project".equals(entry.getLabel(entry))) { //$NON-NLS-1$
                        project = true;
                    }
                    if (entry.getFile().getDiff().getStatus() != Status.IDENTICAL) {
                        switch (this.type) {
                        case TREE:
                            ((DiffGroup) entry.getProvider()).treeParent = this;
                            break;
                        case COMPRESSED:
                            ((DiffGroup) entry.getProvider()).compressedParent = this;
                            break;
                        }
                        super.add(child);
                    }
                } else {
                    super.add(child);
                }
            }
        }

        /**
         * @see com.perforce.team.ui.changelists.Folder#canCompress()
         */
        @Override
        public boolean canCompress() {
            if (isProject()) {
                return false;
            }
            if (folderCount == 1 && childCount == 0) {
                return !((GroupFolder) children.get(0)).isProject();
            } else {
                return super.canCompress();
            }
        }

        /**
         * @see com.perforce.team.ui.changelists.Folder#getImageDescriptor(java.lang.Object)
         */
        @Override
        public ImageDescriptor getImageDescriptor(Object object) {
            if (isProject()) {
                return PlatformUI.getWorkbench().getSharedImages()
                        .getImageDescriptor(IDE.SharedImages.IMG_OBJ_PROJECT);
            }
            return super.getImageDescriptor(object);
        }

        /**
         * Is this folder a project?
         * 
         * @return true if project, false otherwise
         */
        public boolean isProject() {
            return this.project;
        }
    }

    private Type type = null;
    private Folder[] tree;
    private Folder[] compressed;
    private IP4DiffFile[] flat;

    private Set<FileEntry> workingEntries;

    /**
     * Create a grouped diff container
     */
    public GroupedDiffContainer() {
        this.workingEntries = new LinkedHashSet<FileEntry>();
    }

    /**
     * Builds the multiple groups for this container. Must be called after all
     * diff files have been added via {@link #add(IP4DiffFile)}
     */
    public void finish() {
        FileEntry[] entries = this.workingEntries
                .toArray(new FileEntry[workingEntries.size()]);
        Set<IP4DiffFile> flatList = new TreeSet<IP4DiffFile>(SORTER);
        for (FileEntry entry : this.workingEntries) {
            IP4DiffFile diff = entry.getFile();
            if (diff.getDiff().getStatus() != Status.IDENTICAL) {
                flatList.add(diff);
            }
        }
        this.flat = flatList.toArray(new IP4DiffFile[flatList.size()]);
        this.tree = new GroupFolderBuilder(entries, Type.TREE).build();
        this.compressed = new GroupFolderBuilder(entries, Type.COMPRESSED)
                .build(true);
    }

    /**
     * Dispose of working entries
     */
    public void disposeWorkingEntries() {
        this.workingEntries.clear();
        this.workingEntries = null;
    }

    /**
     * Complete unique pairs
     * 
     * @param pair
     */
    public void completeUniquePairs(GroupedDiffContainer pair) {
        Object[] compressed = pair.getElements(Type.COMPRESSED);
        Object[] tree = pair.getElements(Type.TREE);
        Object[] flat = pair.getElements(Type.FLAT);
        for (FileEntry entry : this.workingEntries) {
            if (entry.getFile().getDiff().getStatus() != Status.IDENTICAL) {
                String path = entry.getVirtualPairPath();
                if (path != null) {
                    DiffGroup group = (DiffGroup) entry.getProvider();
                    Object uniquePair = findFolderPair(entry, path, compressed);
                    if (uniquePair != null) {
                        group.compressedPair = uniquePair;
                    }
                    uniquePair = findFolderPair(entry, path, tree);
                    if (uniquePair != null) {
                        group.treePair = uniquePair;
                    }
                    uniquePair = findFilePair(flat, path, true);
                    if (uniquePair != null) {
                        group.flatPair = uniquePair;
                    }
                }
            }
        }
    }

    private Object findFolderPair(FileEntry entry, String path, Object[] roots) {
        Object uniquePair = null;
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash > 0 && lastSlash + 1 < path.length()) {
            String folderPath = path.substring(0, lastSlash);
            String name = path.substring(lastSlash + 1);
            for (Object root : roots) {
                if (root instanceof Folder) {
                    uniquePair = findFolderPair((Folder) root, name, folderPath);
                    if (uniquePair != null) {
                        break;
                    }
                }
            }
        }
        return uniquePair;
    }

    private Object getLastChild(Folder folder) {
        Object last = null;
        Object[] children = folder.getChildren(folder);
        if (children.length > 0) {
            last = children[children.length - 1];
        }
        if (last instanceof Folder) {
            last = getLastChild((Folder) last);
        }
        if (last == null) {
            last = folder;
        }
        return last;
    }

    private Object findFilePair(Object[] children, String name,
            boolean comparePath) {
        Object pair = null;
        for (Object child : children) {
            if (child instanceof IP4DiffFile) {
                String compare = comparePath ? ((IP4DiffFile) child)
                        .getRemotePath() : ((IP4DiffFile) child).getName();
                if (name.compareToIgnoreCase(compare) > 0) {
                    pair = child;
                }
            } else if (child instanceof Folder) {
                pair = getLastChild((Folder) child);
            }
        }
        return pair;
    }

    private Object findFolderPair(Folder folder, String name, String path) {
        Object pair = null;
        String folderPath = folder.getPath();
        if (path.equals(folderPath)) {
            pair = folder;
            Object betterMatch = findFilePair(folder.getChildren(folder), name,
                    false);
            if (betterMatch != null) {
                pair = betterMatch;
            }
        } else if (folderPath.startsWith(path)) {
            pair = folder;
        } else {
            if (path.startsWith(folderPath + "/")) { //$NON-NLS-1$
                pair = folder;
                Object betterMatch = null;
                String folderName = path.substring(folderPath.length() + 1);
                int lastSlash = folderName.indexOf('/');
                if (lastSlash != -1) {
                    folderName = folderName.substring(0, lastSlash);
                }
                Folder currentFolder = null;
                for (Object child : folder.getChildren(folder)) {
                    if (child instanceof Folder) {
                        currentFolder = (Folder) child;
                        betterMatch = findFolderPair(currentFolder, name, path);
                        if (betterMatch != null) {
                            pair = betterMatch;
                            break;
                        } else if (folderName.compareToIgnoreCase(currentFolder
                                .getLabel(child)) > 0) {
                            pair = getLastChild(currentFolder);
                        }
                    }
                }
            }
        }
        return pair;
    }

    /**
     * Set group type
     * 
     * @param type
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * Get elements for currently set type
     * 
     * @param type
     * @return non-null but possibly empty array
     */
    public Object[] getElements(Type type) {
        if (type != null) {
            switch (type) {
            case FLAT:
                return this.flat;
            case TREE:
                return this.tree;
            case COMPRESSED:
                return this.compressed;
            }
        }
        return P4Resource.EMPTY;
    }

    /**
     * Get elements for currently set type
     * 
     * @return non-null but possibly empty array
     */
    public Object[] getElements() {
        return getElements(this.type);
    }

    /**
     * Add diff file to this group container
     * 
     * @param entry
     * @return non-null created file entry
     */
    public FileEntry add(IP4DiffFile entry) {
        FileEntry fileEntry = new FileEntry(entry, new DiffGroup());
        this.workingEntries.add(fileEntry);
        return fileEntry;
    }
}
