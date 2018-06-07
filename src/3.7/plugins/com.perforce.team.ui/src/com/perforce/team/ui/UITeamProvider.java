/*
 * Copyright (c) 2006 Perforce Software.  All rights reserved.
 *
 */
package com.perforce.team.ui;

import org.eclipse.team.ui.history.IHistoryPageSource;

import com.perforce.team.core.PerforceTeamProvider;
import com.perforce.team.ui.history.P4HistoryPageSource;

/**
 * This class only use so that we can kick off ui plug-in
 *
 * @author Sehyo Chang
 *
 */
public class UITeamProvider extends PerforceTeamProvider {

	private static P4HistoryPageSource historyPageSourceSingleton = new P4HistoryPageSource();

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        if (IHistoryPageSource.class == adapter) {
        	// use singleton to avoid unnecessary refreshes in History view
            return adapter.cast(historyPageSourceSingleton);
        }
        return super.getAdapter(adapter);
    }


}
