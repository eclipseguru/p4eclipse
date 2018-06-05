/*
 * Copyright (c) 2006 Perforce Software.  All rights reserved.
 *
 */
package com.perforce.team.ui;

import com.perforce.team.core.PerforceTeamProvider;
import com.perforce.team.ui.history.P4HistoryPageSource;

import org.eclipse.team.ui.history.IHistoryPageSource;

/**
 * This class only use so that we can kick off ui plug-in
 * 
 * @author Sehyo Chang
 * 
 */
public class UITeamProvider extends PerforceTeamProvider {

    /**
     * @see org.eclipse.team.core.RepositoryProvider#getAdapter(java.lang.Class)
     */
    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
        if (IHistoryPageSource.class == adapter) {
            return new P4HistoryPageSource();
        }
        return super.getAdapter(adapter);
    }

}
