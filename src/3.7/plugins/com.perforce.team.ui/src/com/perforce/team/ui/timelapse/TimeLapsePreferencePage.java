/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.timelapse;

import com.perforce.team.ui.LabelProviderAdapter;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.preferences.IPreferenceConstants;
import com.perforce.team.ui.preferences.IPreferenceHandler;
import com.perforce.team.ui.views.SessionManager;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class TimeLapsePreferencePage extends PreferencePage implements
        IWorkbenchPreferencePage {

    /**
     * ID
     */
    public static final String ID = "com.perforce.team.ui.timelapse.TimeLapsePreferencePage"; //$NON-NLS-1$

    private Button p4vButton;
    private Button textButton;
    private BooleanFieldEditor showChangelistButton;
    private BooleanFieldEditor showBranchesButton;
    private BooleanFieldEditor showActionsButton;
    private List<IContentType> contentTypes;
    private CheckboxTableViewer typeViewer;
    private IPreferenceHandler[] handlers = TimeLapseRegistry.getRegistry()
            .getPreferenceHandlers();

    /**
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        Composite displayArea = new Composite(parent, SWT.NONE);
        GridLayout daLayout = new GridLayout(1, true);
        displayArea.setLayout(daLayout);
        displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        boolean internal = getPreferenceStore().getBoolean(
                IPreferenceConstants.USE_INTERNAL_TIMELAPSE);

        p4vButton = new Button(displayArea, SWT.RADIO);
        p4vButton.setText(Messages.TimeLapsePreferencePage_UseP4VTimelapse);
        p4vButton.setSelection(!internal);

        textButton = new Button(displayArea, SWT.RADIO);
        textButton
                .setText(Messages.TimeLapsePreferencePage_UseInternalTimelapse);
        textButton.setSelection(internal);

        createContentTable(displayArea);

        showChangelistButton = new BooleanFieldEditor(
                TimeLapseEditor.SHOW_CHANGELIST,
                Messages.TimeLapsePreferencePage_ShowChangelistDetails,
                displayArea);
        showChangelistButton.setPreferenceStore(getPreferenceStore());
        showChangelistButton.load();

        showBranchesButton = new BooleanFieldEditor(
                TimeLapseEditor.SHOW_BRANCH_HISTORY,
                Messages.TimeLapsePreferencePage_ShowBranchingHistory,
                displayArea);
        showBranchesButton.setPreferenceStore(getPreferenceStore());
        showBranchesButton.load();

        showActionsButton = new BooleanFieldEditor(
                TimeLapseEditor.SHOW_FILE_ACTIONS,
                Messages.TimeLapsePreferencePage_ShowFileActionsInSlider,
                displayArea);
        showActionsButton.setPreferenceStore(getPreferenceStore());
        showActionsButton.load();

        for (IPreferenceHandler handler : handlers) {
            handler.addControls(displayArea);
        }

        return displayArea;
    }

    private void createContentTable(Composite parent) {
        Group languageGroup = new Group(parent, SWT.NONE);
        languageGroup
                .setText(Messages.TimeLapsePreferencePage_AvailableContentTypeSpecificTimelapseViews);
        GridLayout lgLayout = new GridLayout(1, true);
        lgLayout.marginWidth = 0;
        lgLayout.marginHeight = 0;
        languageGroup.setLayout(lgLayout);
        languageGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                false));

        typeViewer = CheckboxTableViewer.newCheckList(languageGroup,
                SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.SINGLE);
        typeViewer.getTable().setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, true, true));

        ILabelProvider labelProvider = new LabelProviderAdapter() {

            public String getText(Object element) {
                if (element instanceof IContentType) {
                    return ((IContentType) element).getName();
                }
                return ""; //$NON-NLS-1$
            }

        };
        typeViewer.setLabelProvider(labelProvider);
        typeViewer.setContentProvider(new ArrayContentProvider());
        typeViewer.setSorter(new ViewerSorter());
        IContentTypeManager manager = Platform.getContentTypeManager();
        this.contentTypes = new ArrayList<IContentType>();
        for (String type : TimeLapseRegistry.getRegistry().getContentTypes()) {
            IContentType cType = manager.getContentType(type);
            if (cType != null) {
                contentTypes.add(cType);
            }
        }
        typeViewer.setInput(contentTypes);
        typeViewer.setAllChecked(true);
        String[] disabled = SessionManager
                .getEntries(IPreferenceConstants.DISABLED_TIMELAPSE_CONTENT_TYPES);
        for (String disable : disabled) {
            IContentType cType = manager.getContentType(disable);
            if (cType != null) {
                typeViewer.setChecked(cType, false);
            }
        }
    }

    /**
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        boolean internal = getPreferenceStore().getDefaultBoolean(
                IPreferenceConstants.USE_INTERNAL_TIMELAPSE);
        textButton.setSelection(internal);
        p4vButton.setSelection(!internal);
        typeViewer.setAllChecked(true);
        showBranchesButton.loadDefault();
        showChangelistButton.loadDefault();
        showActionsButton.loadDefault();
        for (IPreferenceHandler handler : handlers) {
            handler.defaults();
        }
    }

    /**
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        boolean ok = super.performOk();
        getPreferenceStore().setValue(
                IPreferenceConstants.USE_INTERNAL_TIMELAPSE,
                textButton.getSelection());
        List<String> disabled = new ArrayList<String>();
        for (IContentType type : contentTypes) {
            if (!typeViewer.getChecked(type)) {
                disabled.add(type.getId());
            }
        }
        showBranchesButton.store();
        showChangelistButton.store();
        showActionsButton.store();
        SessionManager.saveEntries(disabled,
                IPreferenceConstants.DISABLED_TIMELAPSE_CONTENT_TYPES);
        for (IPreferenceHandler handler : handlers) {
            handler.save();
        }
        return ok;
    }

    /**
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench) {
        setPreferenceStore(PerforceUIPlugin.getPlugin().getPreferenceStore());
    }

}
