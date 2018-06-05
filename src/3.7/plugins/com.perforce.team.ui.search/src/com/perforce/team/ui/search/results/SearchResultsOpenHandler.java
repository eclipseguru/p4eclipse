/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.search.results;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class SearchResultsOpenHandler implements IDoubleClickListener {

    /**
     * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
     */
    public void doubleClick(DoubleClickEvent event) {
        Object[] selected = ((IStructuredSelection) event.getSelection())
                .toArray();
        List<String> processed = new ArrayList<String>();
        for (Object select : selected) {
            if (select instanceof IDepotMatch) {
                IDepotMatch match = (IDepotMatch) select;
                if (!processed.contains(match.getDepotPath())) {
                    processed.add(match.getDepotPath());
                    match.openInEditor();
                }
            }
        }
    }

}
