/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.views;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class MenuFilter implements IMenuListener {

    /**
     * Create a team main filter for com.perforce actions
     * 
     * @return - menu filter
     */
    public static MenuFilter createTeamMainFilter() {
        return new MenuFilter("team.main", "com.perforce"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private String menu = null;
    private String prefix = null;

    /**
     * 
     * @param menu
     * @param prefix
     */
    public MenuFilter(String menu, String prefix) {
        this.menu = menu;
        this.prefix = prefix;
    }

    /**
     * @see org.eclipse.jface.action.IMenuListener#menuAboutToShow(org.eclipse.jface.action.IMenuManager)
     */
    public void menuAboutToShow(IMenuManager manager) {
        if (this.menu != null && this.prefix != null) {
            IMenuManager menuManager = manager.findMenuUsingPath(this.menu);
            if (menuManager != null) {
                for (IContributionItem item : menuManager.getItems()) {
                    String id = item.getId();
                    if (id != null && id.startsWith(this.prefix)) {
                        menuManager.remove(item);
                    }
                }
            }
        }
    }

}
