/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.editor.outline;

import com.perforce.team.core.mergequest.P4BranchGraphCorePlugin;
import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.Mapping;
import com.perforce.team.core.mergequest.model.registry.BranchRegistry;
import com.perforce.team.core.mergequest.model.registry.BranchType;
import com.perforce.team.ui.mergequest.editor.outline.BranchMappingContentProvider.ViewLine;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchGraphOutlineSorter extends ViewerSorter {

    private BranchRegistry registry;

    /**
     * Create outline sorter
     */
    public BranchGraphOutlineSorter() {
        this.registry = P4BranchGraphCorePlugin.getDefault()
                .getBranchRegistry();
    }

    private int compareFirmness(String type1, String type2) {
        int compare = 0;
        BranchType bt1 = this.registry.getType(type1);
        if (bt1 != null) {
            BranchType bt2 = this.registry.getType(type2);
            if (bt2 != null) {
                compare = bt2.getFirmness() - bt1.getFirmness();
            }
        }
        return compare;
    }

    /**
     * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer,
     *      java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
        if (e1 instanceof Branch && e2 instanceof Branch) {
            Branch c1 = (Branch) e1;
            Branch c2 = (Branch) e2;
            if (!c1.getType().equals(c2.getType())) {
                return compareFirmness(c1.getType(), c2.getType());
            } else {
                return c1.getName().compareToIgnoreCase(c2.getName());
            }
        } else if (e1 instanceof Mapping && e2 instanceof Mapping) {
            return ((Mapping) e1).getName().compareToIgnoreCase(
                    ((Mapping) e2).getName());
        } else if (e1 instanceof ViewLine && e2 instanceof ViewLine) {
            return 0;
        }
        return super.compare(viewer, e1, e2);
    }

}
