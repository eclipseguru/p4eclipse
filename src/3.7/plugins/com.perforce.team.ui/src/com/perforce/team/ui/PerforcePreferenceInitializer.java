/**
 *
 */
package com.perforce.team.ui;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.ui.dialogs.StreamsPreferencesDialog;
import com.perforce.team.ui.history.P4HistoryPage;
import com.perforce.team.ui.preferences.IPreferenceConstants;
import com.perforce.team.ui.timelapse.TimeLapseEditor;

/**
 * @author Sehyo Chang
 *
 */
public class PerforcePreferenceInitializer extends
        AbstractPreferenceInitializer {

    /**
     * DEFAULT_RETRIEVE
     */
    public static final int DEFAULT_RETRIEVE = 100;

    private IPreferenceStore store;

    /**
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
     */
    @Override
    public void initializeDefaultPreferences() {

        store = PerforceUIPlugin.getPlugin().getPreferenceStore();
        store.setDefault(IPerforceUIConstants.PREF_SHOW_MARKERS, false);
        store.setDefault(IPerforceUIConstants.PREF_NEW_OPEN_ADD, false);
        store.setDefault(IPerforceUIConstants.PREF_OPEN_DEFAULT, false);
        store.setDefault(IPerforceUIConstants.PREF_REFACTOR_SUPPORT, true);
        store.setDefault(IPerforceUIConstants.PREF_DELETE_PROJECT_FILES, false);
        store.setDefault(IPerforceUIConstants.PREF_MARK_UNMANAGED_FILES, true);
        store.setDefault(IPerforceUIConstants.PREF_DISABLE_MARKER_DECORATION, true);

        store.setDefault(IPerforceUIConstants.PREF_CONSOLE_COMMAND_COLOUR,
                StringConverter.asString(new RGB(0, 0, 0)));
        store.setDefault(IPerforceUIConstants.PREF_CONSOLE_MESSAGE_COLOUR,
                StringConverter.asString(new RGB(0, 0, 255)));
        store.setDefault(IPerforceUIConstants.PREF_CONSOLE_ERROR_COLOUR,
                StringConverter.asString(new RGB(255, 0, 0)));
        store.setDefault(IPerforceUIConstants.PREF_CONSOLE_COMMANDS, 80);
        store.setDefault(IPerforceUIConstants.PREF_CONSOLE_COMMAND_OUPUT_HIDE_LARGE, false);

        // WorkbenchChainedTextFontFieldEditor.startPropagate(store,
        // IPerforceUIConstants.PREF_CONSOLE_FONT);

        store.setDefault(IPerforceUIConstants.PREF_FILE_FORMAT,
                IPerforceUIConstants.SHOW_FILE_REVISION
                        | IPerforceUIConstants.SHOW_FILE_TYPE
                        | IPerforceUIConstants.SHOW_FILE_ACTION);
        store.setDefault(IPerforceUIConstants.PREF_PROJECT_FORMAT,
                IPerforceUIConstants.SHOW_PROJECT_PORT
                        | IPerforceUIConstants.SHOW_PROJECT_USER
                        | IPerforceUIConstants.SHOW_PROJECT_CLIENT);
        store.setDefault(IPerforceUIConstants.PREF_IGNORED_TEXT, false);
        store.setDefault(IPerforceUIConstants.PREF_FILE_OPEN_ICON,
                IPerforceUIConstants.ICON_TOP_LEFT);
        store.setDefault(IPerforceUIConstants.PREF_FILE_SYNC_ICON,
                IPerforceUIConstants.ICON_BOTTOM_RIGHT);
        store.setDefault(IPerforceUIConstants.PREF_FILE_SYNC2_ICON,
                IPerforceUIConstants.ICON_BOTTOM_RIGHT);
        store.setDefault(IPerforceUIConstants.PREF_FILE_UNRESOLVED_ICON,
                IPerforceUIConstants.ICON_TOP_RIGHT);
        store.setDefault(IPerforceUIConstants.PREF_FILE_LOCK_ICON,
                IPerforceUIConstants.ICON_BOTTOM_LEFT);
        store.setDefault(IPerforceUIConstants.PREF_FILE_OTHER_ICON,
                IPerforceUIConstants.ICON_TOP_RIGHT);
        store.setDefault(IPerforceUIConstants.PREF_STREAM_SANDBOX_ICON,
                IPerforceUIConstants.ICON_BOTTOM_LEFT);
        store.setDefault(IPerforceUIConstants.PREF_STREAM_SANDBOX_PROJECT_ICON,
                IPerforceUIConstants.ICON_BOTTOM_LEFT);
        store.setDefault(IPerforceUIConstants.PREF_RESOLVE_DEFAULT_MODE,
                IPerforceUIConstants.RESOLVE_PROMPT);
        store.setDefault(IPerforceUIConstants.PREF_RESOLVE_MIGRATED,
                false);
        store.setDefault(IPerforceUIConstants.PREF_RESOLVE_DEFAULT_ACTION,
                "accept_merge_safe");
        store.setDefault(IPerforceUIConstants.PREF_RESOLVE_MERGE_BINARY_AS_TEXT,
                true);
        store.setDefault(IPerforceUIConstants.PREF_RESOLVE_INTERACTIVE_MERGE_TOOL,
                "p4merge");
        store.setDefault(IPerforceUIConstants.PREF_RETRIEVE_NUM_JOBS,
                DEFAULT_RETRIEVE);
        store.setDefault(IPerforceUIConstants.PREF_RETRIEVE_NUM_STREAMS,
                DEFAULT_RETRIEVE);
        store.setDefault(IPerforceUIConstants.PREF_CLIENT_SWITCH_OPTION, IPerforceUIConstants.PROMPT);
        String perforceHome = System.getProperty("user.home"); //$NON-NLS-1$
        if(!StringUtils.isEmpty(perforceHome)){
        	if(!perforceHome.endsWith(Character.toString(File.separatorChar))){
        		perforceHome=perforceHome+File.separatorChar;
        	}
        	perforceHome=perforceHome+"Perforce"; //$NON-NLS-1$
        	File dir=new File(perforceHome);
        	if(!dir.exists()){
        		try {
        			dir.mkdirs();
				} catch (Throwable t) {
					PerforceProviderPlugin.logError(t.getLocalizedMessage());
				}
        	}
        	store.setDefault(IPerforceUIConstants.PREF_CLIENT_ROOT_PARENT_DEFAULT, perforceHome); //$NON-NLS-1$
        }
        store.setDefault(IPerforceUIConstants.PREF_CLIENT_SWITCH_ON_STREAM_OPERATOIN, IPerforceUIConstants.STREAM_SWITCH_STREAM_WITH_SAME_CLIENT);
        store.setDefault(IPerforceUIConstants.PREF_CLIENT_SWITCH_NO_WARN, false); // warn user when switch clients
        store.setDefault(IPerforceUIConstants.PREF_STREAM_DISPLAY,
                StreamsPreferencesDialog.SHOW_NAME_ROOT);
        store.setDefault(IPerforceUIConstants.PREF_RETRIEVE_NUM_CHANGES,
                DEFAULT_RETRIEVE);
        store.setDefault(IPreferenceConstants.NUM_LABELS_RETRIEVE,
                DEFAULT_RETRIEVE);
        store.setDefault(IPreferenceConstants.NUM_BRANCHES_RETRIEVE,
                DEFAULT_RETRIEVE);
        store.setDefault(IPreferenceConstants.NUM_SHELVES_RETRIEVE,
                DEFAULT_RETRIEVE);
        store.setDefault(IPerforceUIConstants.PREF_PROJECT_ICON,
                IPerforceUIConstants.ICON_BOTTOM_RIGHT);
        store.setDefault(IPreferenceConstants.FILE_DECORATION_TEXT,
                "{name} {#[have]}{/[head]} {<[type]>}{<[action]>}");
        store.setDefault(IPreferenceConstants.PROJECT_DECORATION_TEXT,
                "{name} [{[sandbox], }{[offline], }{server}, {client}, {user}{, [charset]}]");
        store.setDefault(IPreferenceConstants.CONNECTION_DECORATION_TEXT,
        		"{name} [{[offline]}{[stream_name]}{,[stream_root]}{,[sandbox]}]");
        store.setDefault(IPreferenceConstants.IGNORED_DECORATION, "<ignored>");
        store.setDefault(IPreferenceConstants.OUTGOING_CHANGE_DECORATION, ">");
        store.setDefault(IPreferenceConstants.UNADDED_CHANGE_DECORATION,
                "<UNADDED>");
        store.setDefault(IPerforceUIConstants.PREF_REFACTOR_USE_MOVE, true);
        store.setDefault(IPreferenceConstants.CHANGELISTS_SAVED, 10);
        store.setDefault(IPreferenceConstants.DESCRIPTION_RULER, true);
        store.setDefault(IPreferenceConstants.DESCRIPTION_RULER_COLUMN, 80);
        store.setDefault(IPreferenceConstants.DESCRIPTION_RULER_COLOR,
                StringConverter.asString(new RGB(200, 200, 200)));
        store.setDefault(IPreferenceConstants.DESCRIPTION_RULER_STYLE,
                SWT.LINE_DOT);
        store.setDefault(IPreferenceConstants.DESCRIPTION_EDITOR_FONT, true);
        store.setDefault(IPreferenceConstants.USE_INTERNAL_TIMELAPSE, true);
        store.setDefault(TimeLapseEditor.SHOW_FILE_ACTIONS, true);
        store.setDefault(IPreferenceConstants.HISTORY_SHOW_MODE,
                P4HistoryPage.REMOTE_MODE);
        store.setDefault(P4HistoryPage.SHOW_COMMENTS, true);
        store.setDefault(IPerforceUIConstants.PREF_PROMPT_ON_DELETING_MANAGED_FOLDERS, true);
        store.setDefault(IPerforceUIConstants.PREF_REFRESH_REVISION_ON_REFRESH, false);
    }

}
