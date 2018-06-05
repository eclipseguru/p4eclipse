/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.text;

import com.perforce.team.ui.text.timelapse.AgingPainter;
import com.perforce.team.ui.text.timelapse.NodeModelTimeLapseEditor;
import com.perforce.team.ui.text.timelapse.NodeTickFormatter;
import com.perforce.team.ui.text.timelapse.NodeTimeLapseEditor;
import com.perforce.team.ui.text.timelapse.TextTimeLapseEditor;
import com.perforce.team.ui.text.timelapse.form.FormTimeLapseEditor;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.StringConverter;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class TextPreferenceInitializer extends AbstractPreferenceInitializer {

    /**
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
     */
    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = PerforceUiTextPlugin.getDefault()
                .getPreferenceStore();
        store.setDefault(TextTimeLapseEditor.LEAST_RECENT_COLOR,
                StringConverter.asString(AgingPainter.DEFAULT_LEAST_RECENT));
        store.setDefault(TextTimeLapseEditor.MOST_RECENT_COLOR,
                StringConverter.asString(AgingPainter.DEFAULT_MOST_RECENT));
        store.setDefault(NodeTimeLapseEditor.SHOW_FOLDING, true);
        store.setDefault(NodeModelTimeLapseEditor.TICK_CHANGE_COLOR,
                StringConverter.asString(NodeTickFormatter.TICK_BG));
        store.setDefault(NodeTimeLapseEditor.LINK_EDITOR, true);
        store.setDefault(TextTimeLapseEditor.WHITESPACE_TYPE, ""); //$NON-NLS-1$
        store.setDefault(FormTimeLapseEditor.HIDE_COMMENTS, true);
    }
}
