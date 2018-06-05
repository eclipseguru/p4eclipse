/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.synchronize;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.team.internal.ui.synchronize.actions.ChangeSetActionGroup;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.SynchronizePageActionGroup;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class RemoveActionGroup extends SynchronizePageActionGroup {

    /**
     * @see org.eclipse.team.ui.synchronize.SynchronizePageActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
     */
    @Override
    public void fillContextMenu(IMenuManager menu) {
    	super.fillContextMenu(menu);
    	
        // Remove all changeset group items found
        while (menu.remove(ChangeSetActionGroup.CHANGE_SET_GROUP) != null)
            ;

        // Remove sort items found
        menu.remove(ISynchronizePageConfiguration.SORT_GROUP);
    }

}
