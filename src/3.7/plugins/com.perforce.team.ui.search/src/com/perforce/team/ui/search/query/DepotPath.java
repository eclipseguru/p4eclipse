/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.search.query;

import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.WorkbenchAdapter;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class DepotPath extends WorkbenchAdapter {

    private String path;

    /**
     * Create depot path
     * 
     * @param path
     */
    public DepotPath(String path) {
        this.path = path;
    }

    /**
     * Get string path
     * 
     * @return path
     */
    public String getPath() {
        return this.path;
    }

    /**
     * @see org.eclipse.ui.model.WorkbenchAdapter#getImageDescriptor(java.lang.Object)
     */
    @Override
    public ImageDescriptor getImageDescriptor(Object object) {
        return PerforceUIPlugin
                .getDescriptor(IPerforceUIConstants.IMG_DEPOT_FOLDER);
    }

    /**
     * @see org.eclipse.ui.model.WorkbenchAdapter#getLabel(java.lang.Object)
     */
    @Override
    public String getLabel(Object object) {
        return this.path;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof DepotPath) {
            return this.path.equals(((DepotPath) obj).path);
        }
        return false;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.path.hashCode();
    }

}
