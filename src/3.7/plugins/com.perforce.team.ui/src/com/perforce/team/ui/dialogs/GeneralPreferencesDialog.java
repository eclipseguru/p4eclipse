package com.perforce.team.ui.dialogs;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.preferences.IPreferenceConstants;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.PlatformUI;

/**
 * General Perforce preferences page.
 */
public class GeneralPreferencesDialog extends PerforcePreferencesDialog {

    /**
     * ID
     */
    public static final String ID = "com.perforce.team.ui.dialogs.GeneralPreferencesDialog"; //$NON-NLS-1$

    // Preference check boxes
    private Button showMarkers;
    private Button openForAdd;
    private Button openDefault;
    private Button openRefactor;
    private Button noAction;
    private Button refactorSupport;
    private Button saveRefactorSupport;
    private Button useMoveCommand;
    private Button deleteProject;
    private Button logCommand;
    private Button persistOfflineStateButton;
    private Button saveExpandedDepotsButton;
    private Button deleteLinkedResources;
    private Button enableChangesets;
    private Button enablePromptOnRemove;
	private Button enableTeamRefresh;
	private Button disableMarkerDecoration;

    /**
     * Constructor.
     */
    public GeneralPreferencesDialog() {
        super(1);
    }

    /**
     * OK button clicked. Save all preferences and exit.
     * 
     * @return - true if no exceptions thrown
     */
    @Override
    public boolean performOk() {
        setPrefBoolean(IPerforceUIConstants.PREF_SHOW_MARKERS,
                showMarkers.getSelection());
        setPrefBoolean(IPerforceUIConstants.PREF_NEW_OPEN_ADD,
                openForAdd.getSelection());
        setPrefBoolean(IPerforceUIConstants.PREF_OPEN_DEFAULT,
                openDefault.getSelection());
        setPrefBoolean(IPreferenceConstants.REFACTOR_DIALOG,
                openRefactor.getSelection());
        setPrefBoolean(IPerforceUIConstants.PREF_REFACTOR_SUPPORT,
                refactorSupport.getSelection());
        setPrefBoolean(IPerforceUIConstants.PREF_REFACTOR_SAVE_SUPPORT,
                saveRefactorSupport.getSelection());
        setPrefBoolean(IPerforceUIConstants.PREF_REFACTOR_USE_MOVE,
                useMoveCommand.getSelection());
        setPrefBoolean(IPerforceUIConstants.PREF_DELETE_PROJECT_FILES,
                deleteProject.getSelection());
        setPrefBoolean(IPerforceUIConstants.PREF_DELETE_LINKED_RESOURCES,
                deleteLinkedResources.getSelection());
        setPrefBoolean(IPreferenceConstants.SHOW_CHANGE_SETS,
                enableChangesets.getSelection());
        setPrefBoolean(IPerforceUIConstants.PREF_LOG_COMMAND,
                logCommand.getSelection());
        setPrefBoolean(IPerforceUIConstants.PREF_DISABLE_MARKER_DECORATION,
        		disableMarkerDecoration.getSelection());

        setPrefBoolean(IPreferenceConstants.PERSIST_OFFINE,
                persistOfflineStateButton.getSelection());

        setPrefBoolean(IPreferenceConstants.SAVE_EXPANDED_DEPOTS,
                saveExpandedDepotsButton.getSelection());

        setPrefBoolean(
				IPerforceUIConstants.PREF_PROMPT_ON_DELETING_MANAGED_FOLDERS,
				enablePromptOnRemove.getSelection());

        setPrefBoolean(
				IPerforceUIConstants.PREF_REFRESH_REVISION_ON_REFRESH,
				enableTeamRefresh.getSelection());
        return true;
    }

    /**
     * Restore defaults
     */
    @Override
    protected void performDefaults() {
        showMarkers.setSelection(false);
        openForAdd.setSelection(false);
        noAction.setSelection(false);
        disableMarkerDecoration.setSelection(false);
        

        if (getPrefDefBoolean(IPerforceUIConstants.PREF_SHOW_MARKERS)) {
            showMarkers.setSelection(true);
        } else if (getPrefDefBoolean(IPerforceUIConstants.PREF_NEW_OPEN_ADD)) {
            openForAdd.setSelection(true);
        } else {
            noAction.setSelection(true);
        }
        openDefault
                .setSelection(getPrefDefBoolean(IPerforceUIConstants.PREF_OPEN_DEFAULT));
        openRefactor
                .setSelection(getPrefDefBoolean(IPreferenceConstants.REFACTOR_DIALOG));
        refactorSupport
                .setSelection(getPrefDefBoolean(IPerforceUIConstants.PREF_REFACTOR_SUPPORT));
        deleteProject
                .setSelection(getPrefDefBoolean(IPerforceUIConstants.PREF_DELETE_PROJECT_FILES));
        deleteLinkedResources
                .setSelection(getPrefDefBoolean(IPerforceUIConstants.PREF_DELETE_LINKED_RESOURCES));
        enableChangesets
                .setSelection(getPrefDefBoolean(IPreferenceConstants.SHOW_CHANGE_SETS));
        disableMarkerDecoration
                .setSelection(getPrefDefBoolean(IPerforceUIConstants.PREF_DISABLE_MARKER_DECORATION));
        logCommand
                .setSelection(getPrefDefBoolean(IPerforceUIConstants.PREF_LOG_COMMAND));

        persistOfflineStateButton
                .setSelection(getPrefDefBoolean(IPreferenceConstants.PERSIST_OFFINE));

        saveExpandedDepotsButton
                .setSelection(getPrefDefBoolean(IPreferenceConstants.SAVE_EXPANDED_DEPOTS));

        useMoveCommand
                .setSelection(getPrefDefBoolean(IPerforceUIConstants.PREF_REFACTOR_USE_MOVE));

        saveRefactorSupport
                .setSelection(getPrefDefBoolean(IPerforceUIConstants.PREF_REFACTOR_SAVE_SUPPORT));
        
		enablePromptOnRemove
		.setSelection(getPrefDefBoolean(IPerforceUIConstants.PREF_PROMPT_ON_DELETING_MANAGED_FOLDERS));

		enableTeamRefresh
		.setSelection(getPrefDefBoolean(IPerforceUIConstants.PREF_REFRESH_REVISION_ON_REFRESH));

    }

    /**
     * Create dialog controls
     * 
     * @param parent
     * @return - main control
     */
    @Override
    protected Control createContents(Composite parent) {
        Composite composite = DialogUtils.createComposite(parent);

        DialogUtils.createLabel(composite,
                Messages.GeneralPreferencesDialog_GeneralSettings);
        createAddingFileGroup(composite);

        DialogUtils.createBlank(composite);
        openDefault = DialogUtils
                .createCheck(
                        composite,
                        Messages.GeneralPreferencesDialog_ShowChangelistSelectionWhenMarkingFiles);
        openRefactor = DialogUtils
                .createCheck(
                        composite,
                        Messages.GeneralPreferencesDialog_ShowChangelistSelectionWhenRefactoring);
        refactorSupport = DialogUtils
                .createCheck(
                        composite,
                        Messages.GeneralPreferencesDialog_EnableEditDeleteRenameRefactorOps);
        saveRefactorSupport = DialogUtils.createCheck(composite,
                Messages.GeneralPreferencesDialog_EnableSaveOps);
        useMoveCommand = DialogUtils.createCheck(composite,
                Messages.GeneralPreferencesDialog_UseMoveForRefactoring);
        deleteProject = DialogUtils
                .createCheck(
                        composite,
                        Messages.GeneralPreferencesDialog_MarkForDeleteWhenDeletingProject);
        deleteLinkedResources = DialogUtils
                .createCheck(
                        composite,
                        Messages.GeneralPreferencesDialog_MarkForDeleteWhenDeletingLinkedResources);
        disableMarkerDecoration = DialogUtils.createCheck(composite,
                Messages.GeneralPreferencesDialog_DisableMarkerDecoration);
        logCommand = DialogUtils.createCheck(composite,
                Messages.GeneralPreferencesDialog_LogAllCommands);

        persistOfflineStateButton = DialogUtils.createCheck(composite,
                Messages.GeneralPreferencesDialog_RetainOfflineState);

        saveExpandedDepotsButton = DialogUtils.createCheck(composite,
                Messages.GeneralPreferencesDialog_RetainExpandedFolders);

        enableChangesets = DialogUtils.createCheck(composite,
                Messages.GeneralPreferencesDialog_GroupSyncsByChangelist);

        enablePromptOnRemove = DialogUtils.createCheck(composite,
                Messages.GeneralPreferencesDialog_PromptOnRemove);
        
        enableTeamRefresh = DialogUtils.createCheck(composite,
                Messages.GeneralPreferencesDialog_RefreshRevisionWhenRefresh);

        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(openDefault, IHelpContextIds.PREF_OPEN_DEFAULT);
        PlatformUI
                .getWorkbench()
                .getHelpSystem()
                .setHelp(refactorSupport, IHelpContextIds.PREF_REFACTOR_SUPPORT);
        PlatformUI
                .getWorkbench()
                .getHelpSystem()
                .setHelp(deleteProject,
                        IHelpContextIds.PREF_DELETE_PROJECT_FILES);

        initialiseValues();

        return composite;
    }

    private void initialiseValues() {
        if (getPrefBoolean(IPerforceUIConstants.PREF_SHOW_MARKERS)) {
            showMarkers.setSelection(true);
        } else if (getPrefBoolean(IPerforceUIConstants.PREF_NEW_OPEN_ADD)) {
            openForAdd.setSelection(true);
        } else {
            noAction.setSelection(true);
        }
        openDefault
                .setSelection(getPrefBoolean(IPerforceUIConstants.PREF_OPEN_DEFAULT));
        openRefactor
                .setSelection(getPrefBoolean(IPreferenceConstants.REFACTOR_DIALOG));
        refactorSupport
                .setSelection(getPrefBoolean(IPerforceUIConstants.PREF_REFACTOR_SUPPORT));
        saveRefactorSupport
                .setSelection(getPrefBoolean(IPerforceUIConstants.PREF_REFACTOR_SAVE_SUPPORT));
        useMoveCommand
                .setSelection(getPrefBoolean(IPerforceUIConstants.PREF_REFACTOR_USE_MOVE));
        deleteProject
                .setSelection(getPrefBoolean(IPerforceUIConstants.PREF_DELETE_PROJECT_FILES));
        deleteLinkedResources
                .setSelection(getPrefBoolean(IPerforceUIConstants.PREF_DELETE_LINKED_RESOURCES));
        enableChangesets
                .setSelection(getPrefBoolean(IPreferenceConstants.SHOW_CHANGE_SETS));
        disableMarkerDecoration
                .setSelection(getPrefBoolean(IPerforceUIConstants.PREF_DISABLE_MARKER_DECORATION));
        logCommand
                .setSelection(getPrefBoolean(IPerforceUIConstants.PREF_LOG_COMMAND));
        
        persistOfflineStateButton
                .setSelection(getPrefBoolean(IPreferenceConstants.PERSIST_OFFINE));

        saveExpandedDepotsButton
                .setSelection(getPrefBoolean(IPreferenceConstants.SAVE_EXPANDED_DEPOTS));
        
		enablePromptOnRemove
		.setSelection(getPrefBoolean(IPerforceUIConstants.PREF_PROMPT_ON_DELETING_MANAGED_FOLDERS));

		enableTeamRefresh
		.setSelection(getPrefBoolean(IPerforceUIConstants.PREF_REFRESH_REVISION_ON_REFRESH));

    }

    private void createAddingFileGroup(Composite parent) {
        Group group = DialogUtils.createGroup(parent,
                Messages.GeneralPreferencesDialog_WhenAddingNewFile, 1);

        openForAdd = DialogUtils.createRadio(group,
                Messages.GeneralPreferencesDialog_MarkForAdd);
        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(openForAdd, IHelpContextIds.PREF_NEW_OPEN_ADD);

        showMarkers = DialogUtils.createRadio(group,
                Messages.GeneralPreferencesDialog_CreateMarker);
        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(showMarkers, IHelpContextIds.PREF_MARKERS_ENABLED);

        noAction = DialogUtils.createRadio(group,
                Messages.GeneralPreferencesDialog_TakeNoAction);

    }

}
