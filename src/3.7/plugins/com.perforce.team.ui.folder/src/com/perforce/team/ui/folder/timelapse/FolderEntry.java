/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.timelapse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

import com.perforce.p4java.core.file.FileAction;
import com.perforce.p4java.core.file.IFileRevisionData;
import com.perforce.p4java.core.file.IRevisionIntegrationData;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class FolderEntry implements IWorkbenchAdapter,
        Comparable<IWorkbenchAdapter>, IEntry {

    private RootEntry root;
    private String name;
    private FolderEntry parent;
    private int first = Integer.MAX_VALUE;
    private Map<Integer, String> moveAdds = new HashMap<Integer, String>();
    private Set<IEntry> children = new TreeSet<IEntry>();

    /**
     * Create a new folder entry
     * 
     * @param name
     * @param parent
     * @param root
     */
    public FolderEntry(String name, FolderEntry parent) {
        this.name = name;
        this.parent = parent;
        if (this.parent != null) {
            this.parent.add(this);
        }
    }

    public void setRoot(RootEntry root){
    	this.root = root;
        if (root != null) {
            for (IP4Revision revision : getRevisions()) {
                moveAdds.put(revision.getChangelist(), null);
            }
        }
    }
    
    /**
     * Complete the model meaning all revision data has been added to file
     * entries
     */
    public void complete() {
        for (IEntry child : children) {
            child.complete();
        }
        for (IEntry child : children) {
            first = Math.min(first, child.getFirst());
        }
    }

    /**
     * @see com.perforce.team.ui.folder.timelapse.IEntry#getFirst()
     */
    public int getFirst() {
        return this.first;
    }

    /**
     * Is move add at specified changelist
     * 
     * @param changelist
     * @return - true if move add
     */
    public boolean isMoveAdd(int changelist) {
        return this.moveAdds.containsKey(changelist);

    }

    /**
     * Get move from path at specified changelist
     * 
     * @param changelist
     * @return - move from path
     */
    public String getMoveFrom(int changelist) {
        return this.moveAdds.get(changelist);
    }

    /**
     * Update move status of this folder
     * 
     * @param entry
     * @param data
     */
    public void updateMoveStatus(FileEntry entry, IFileRevisionData data) {
        int id = data.getChangelistId();
        if (moveAdds.containsKey(id)) {
            FileAction action = data.getAction();
            if (!FileAction.MOVE_ADD.equals(action)) {
                moveAdds.remove(id);
            } else {
                List<IRevisionIntegrationData> integs = data
                        .getRevisionIntegrationData();
                if (integs != null) {
                    for (IRevisionIntegrationData integ : integs) {
                        if (integ.getHowFrom().equals("moved from")) {
                            String from = integ.getFromFile();
                            int lastSlash = from.lastIndexOf('/');
                            if (lastSlash >= 0 && lastSlash < from.length()) {
                                from = from.substring(0, lastSlash);
                                lastSlash = from.lastIndexOf('/');
                                if (lastSlash >= 0
                                        && lastSlash + 1 < from.length()) {
                                    String moveFrom = from
                                            .substring(lastSlash + 1);
                                    String currMove = moveAdds.get(id);
                                    if (currMove == null) {
                                        moveAdds.put(id, moveFrom);
                                    } else if (!currMove.equals(moveFrom)) {
                                        moveAdds.remove(id);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Add child entry to this folder
     * 
     * @param child
     */
    public void add(IEntry child) {
        if (child != null && !children.contains(child)) {
            children.add(child);
        }
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object o) {
        return this.children.toArray();
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
     */
    public ImageDescriptor getImageDescriptor(Object object) {
        return PerforceUIPlugin
                .getDescriptor(IPerforceUIConstants.IMG_DEPOT_FOLDER);
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
     */
    public String getLabel(Object o) {
        return this.name;
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
     */
    public Object getParent(Object o) {
        return this.parent;
    }

    /**
     * @param o
     * @return - comparison
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(IWorkbenchAdapter o) {
        return getLabel(this).compareToIgnoreCase(o.getLabel(o));
    }

    /**
     * @see com.perforce.team.ui.folder.timelapse.IEntry#getRevisions()
     */
    public IP4Revision[] getRevisions() {
        return this.root.getRevisions();
    }

    @Override
    public boolean equals(Object obj) {
    	// override to prevent coverity from complaining.
		return obj instanceof FolderEntry
				&& P4CoreUtils.equals(this.parent,
						((FolderEntry) obj).getParent(obj))
				&& P4CoreUtils.equals(this.name,
						((FolderEntry) obj).getLabel(obj)) && super.equals(obj);
    }
    
    @Override
    public int hashCode() {
    	// override to prevent coverity from complaining: FB.EQ_COMPARETO_USE_OBJECT_EQUALS
    	return super.hashCode()+P4CoreUtils.hashCode(this.name)*31+P4CoreUtils.hashCode(this.parent)*11;
    }

}
