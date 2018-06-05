/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest;

import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.BranchSpecMapping;
import com.perforce.team.core.mergequest.model.DepotPathMapping;
import com.perforce.team.core.mergequest.model.Mapping;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.WorkbenchAdapter;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class MappingWorkbenchAdapter extends WorkbenchAdapter {

    /**
     * Depot path class
     */
    public static class DepotPath extends WorkbenchAdapter {

        private Mapping parent;
        private String path;
        private String name;

        /**
         * Create new depot path
         * 
         * @param parent
         * @param name
         * @param path
         */
        public DepotPath(Mapping parent, String name, String path) {
            this.parent = parent;
            this.name = name;
            this.path = path;
        }

        /**
         * @see org.eclipse.ui.model.WorkbenchAdapter#getImageDescriptor(java.lang.Object)
         */
        @Override
        public ImageDescriptor getImageDescriptor(Object object) {
            return PerforceUIPlugin
                    .getDescriptor(IPerforceUIConstants.IMG_VIEW_LINE);
        }

        /**
         * @see org.eclipse.ui.model.WorkbenchAdapter#getLabel(java.lang.Object)
         */
        @Override
        public String getLabel(Object object) {
            return this.path;
        }

        /**
         * Get branch name
         * 
         * @return branch name
         */
        public String getName() {
            return this.name;
        }

        /**
         * @see org.eclipse.ui.model.WorkbenchAdapter#getParent(java.lang.Object)
         */
        @Override
        public Object getParent(Object object) {
            return this.parent;
        }

    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
     */
    @Override
    public Object[] getChildren(Object o) {
        if (o instanceof DepotPathMapping) {
            DepotPathMapping mapping = (DepotPathMapping) o;
            Branch sBranch = mapping.getSource();
            Branch tBranch = mapping.getTarget();
            if (sBranch != null && tBranch != null) {
                DepotPath source = new DepotPath(mapping, sBranch.getName(),
                        mapping.getSourcePath());
                DepotPath target = new DepotPath(mapping, tBranch.getName(),
                        mapping.getTargetPath());
                return new DepotPath[] { source, target };
            }
        }
        return super.getChildren(o);
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
     */
    @Override
    public ImageDescriptor getImageDescriptor(Object object) {
        String type = ((Mapping) object).getType();
        if (BranchSpecMapping.TYPE.equals(type)) {
            return PerforceUIPlugin
                    .getDescriptor(IPerforceUIConstants.IMG_BRANCH);
        } else if (DepotPathMapping.TYPE.equals(type)) {
            return P4BranchGraphPlugin
                    .getImageDescriptor(IP4BranchGraphConstants.DEPOT_PATH_MAPPING);
        }
        return super.getImageDescriptor(object);
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
     */
    @Override
    public String getLabel(Object o) {
        Mapping mapping = (Mapping) o;
        return mapping.getName();
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
     */
    @Override
    public Object getParent(Object o) {
        return ((Mapping) o).getGraph();
    }

}
