/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class ChangelistResolveAction extends ResolveAction {

    /**
     * @see com.perforce.team.ui.p4java.actions.ResolveAction#getResolveCollection()
     */
    @Override
    protected P4Collection getResolveCollection() {
        P4Collection collection = createCollection();
        for (IP4Resource resource : getResourceSelection().members()) {
            if (resource instanceof IP4PendingChangelist) {
                IP4PendingChangelist list = (IP4PendingChangelist) resource;
                if (list.needsRefresh()) {
                    list.refresh();
                }
                for (IP4Resource member : list.members()) {
                    collection.add(member);
                }
            }
        }
        return collection;
    }

}
