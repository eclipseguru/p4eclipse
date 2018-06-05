package com.perforce.team.ui.dialogs;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.perforce.team.ui.PerforceUIPlugin;

/**
 * Superclasses for Perforce perforce pages
 */
public abstract class PerforcePreferencesDialog extends PreferencePage
        implements IWorkbenchPreferencePage {

    // Number of columns in GridLayout
    private int numColumns;

    // List of field editors
    private FieldEditor[] fieldEditors;

    /**
     * Perforce colour editor. Supports multiple columns and getting the help
     * control.
     */
    protected class PerforceColorFieldEditor extends ColorFieldEditor {

        private Composite parent;

        public PerforceColorFieldEditor(String name, String labelText,
                Composite parent) {
            super(name, labelText, parent);
            adjustForNumColumns(numColumns);
            this.parent = parent;
        }

        public Control getHelpControl() {
            return getChangeControl(parent);
        }
    }

    /**
     * Perforce boolean editor. Supports multiple columns and getting the help
     * control.
     */
    protected class PerforceBooleanFieldEditor extends BooleanFieldEditor {

        private Composite parent;

        public PerforceBooleanFieldEditor(String name, String labelText,
                Composite parent) {
            super(name, labelText, parent);
            adjustForNumColumns(numColumns);
            this.parent = parent;
        }

        public Control getHelpControl() {
            return getChangeControl(parent);
        }
    }

    /**
     * Perforce string editor. Supports multiple columns and getting the help
     * control.
     */
    protected class PerforceStringFieldEditor extends StringFieldEditor {

        private Composite parent;

        public PerforceStringFieldEditor(String name, String labelText,
                Composite parent) {
            super(name, labelText, parent);
            adjustForNumColumns(numColumns);
            this.parent = parent;
        }

        public Control getHelpControl() {
            return getTextControl(parent);
        }
    }

    /**
     * Perforce font editor. Supports multiple columns and getting the help
     * control.
     */
    protected class PerforceFontFieldEditor extends FontFieldEditor {

        private Composite parent;

        public PerforceFontFieldEditor(String name, String labelText,
                Composite parent) {
            super(name, labelText, parent);
            adjustForNumColumns(numColumns);
            this.parent = parent;
        }

        public Control getHelpControl() {
            return getChangeControl(parent);
        }
    }

    /**
     * Perforce font editor. Supports multiple columns and getting the help
     * control.
     */
    protected class PerforceRadioGroupFieldEditor extends RadioGroupFieldEditor {

        private Composite parent;

        public PerforceRadioGroupFieldEditor(String name, String labelText,
                int numColumns, String[][] labelAndValues, Composite parent,
                boolean useGroup) {
            super(name, labelText, numColumns, labelAndValues, parent, useGroup);
            adjustForNumColumns(PerforcePreferencesDialog.this.numColumns);
            this.parent = parent;
        }

        public Control getHelpControl() {
            return getRadioBoxControl(parent);
        }
    }

    /**
     * Constructor.
     * 
     * @param numColumns
     *            the number of columns in the GridLayout.
     */
    public PerforcePreferencesDialog(int numColumns) {
        this.numColumns = numColumns;
        ;
    }

    /**
     * OK button pressed so save all values.
     */
    @Override
    public boolean performOk() {
        for (int i = 0; i < fieldEditors.length; i++) {
            fieldEditors[i].store();
        }
        return super.performOk();
    }

    public void init(IWorkbench workbench) {
    }

    /**
     * Set all field editors for this dialog.
     * 
     * @param fieldEditor
     *            the array of field editors.
     */
    protected void setFieldEditors(FieldEditor[] fieldEditors) {
        this.fieldEditors = fieldEditors;
    }

    /**
     * Initialise all editors with values from the preference store.
     */
    protected void initializeValues() {
        for (int i = 0; i < fieldEditors.length; i++) {
            fieldEditors[i].load();
        }
    }

    /**
     * Defaults button pressed so restore default values.
     */
    @Override
    protected void performDefaults() {
        super.performDefaults();
        for (int i = 0; i < fieldEditors.length; i++) {
            fieldEditors[i].loadDefault();
        }
    }

    /**
     * Create new color field editor
     * 
     * @param preferenceName
     *            the name of the preference property
     * @param label
     *            the label for the editor
     * @param parent
     *            the parent for the control
     * @return the field editor
     */
    protected PerforceColorFieldEditor createColorFieldEditor(
            String preferenceName, String label, Composite parent) {
        PerforceColorFieldEditor editor = new PerforceColorFieldEditor(
                preferenceName, label, parent);

        editor.setPage(this);
        editor.setPreferenceStore(getPreferenceStore());
        return editor;
    }

    /**
     * Create new boolean field editor
     * 
     * @param preferenceName
     *            the name of the preference property
     * @param label
     *            the label for the editor
     * @param parent
     *            the parent for the control
     * @return the field editor
     */
    protected PerforceBooleanFieldEditor createBooleanFieldEditor(
            String preferenceName, String label, Composite parent) {
        PerforceBooleanFieldEditor editor = new PerforceBooleanFieldEditor(
                preferenceName, label, parent);
        editor.setPage(this);
        editor.setPreferenceStore(getPreferenceStore());
        return editor;
    }

    /**
     * Create new string field editor
     * 
     * @param preferenceName
     *            the name of the preference property
     * @param label
     *            the label for the editor
     * @param parent
     *            the parent for the control
     * @return the field editor
     */
    protected PerforceStringFieldEditor createStringFieldEditor(
            String preferenceName, String label, Composite parent) {
        PerforceStringFieldEditor editor = new PerforceStringFieldEditor(
                preferenceName, label, parent);
        editor.setPage(this);
        editor.setPreferenceStore(getPreferenceStore());
        return editor;
    }

    /**
     * Create new font field editor
     * 
     * @param preferenceName
     *            the name of the preference property
     * @param label
     *            the label for the editor
     * @param parent
     *            the parent for the control
     * @return the field editor
     */
    protected PerforceFontFieldEditor createFontFieldEditor(
            String preferenceName, String label, Composite parent) {
        PerforceFontFieldEditor editor = new PerforceFontFieldEditor(
                preferenceName, label, parent);
        editor.setPage(this);
        editor.setPreferenceStore(getPreferenceStore());
        return editor;
    }

    /**
     * Create new radio group field editor
     * 
     * @param preferenceName
     *            the name of the preference property
     * @param label
     *            the label for the editor
     * @param parent
     *            the parent for the control
     * @return the field editor
     */
    protected PerforceRadioGroupFieldEditor createRadioGroupFieldEditor(
            String preferenceName, String label, int numColumns,
            String[][] labelAndValues, Composite parent, boolean useGroup) {
        PerforceRadioGroupFieldEditor editor = new PerforceRadioGroupFieldEditor(
                preferenceName, label, numColumns, labelAndValues, parent,
                useGroup);
        editor.setPage(this);
        editor.setPreferenceStore(getPreferenceStore());
        return editor;
    }

    /**
     * Get the preference store for this page
     * 
     * @return the preference store
     */
    @Override
    protected IPreferenceStore doGetPreferenceStore() {
        return PerforceUIPlugin.getPlugin().getPreferenceStore();
    }

    /**
     * Set integer preference
     * 
     * @param key
     *            the preference key
     * @param value
     *            the preference value
     */
    protected void setPrefInt(String key, int value) {
        getPreferenceStore().setValue(key, value);
    }

    /**
     * Get integer preference
     * 
     * @param key
     *            the preference key
     * @return the preference value
     */
    protected int getPrefInt(String key) {
        return getPreferenceStore().getInt(key);
    }

    /**
     * Get default integer preference
     * 
     * @param key
     *            the preference key
     * @return the default preference value
     */
    protected int getPrefDefInt(String key) {
        return getPreferenceStore().getDefaultInt(key);
    }

    /**
     * Set boolean preference
     * 
     * @param key
     *            the preference key
     * @param value
     *            the preference value
     */
    protected void setPrefBoolean(String key, boolean value) {
        getPreferenceStore().setValue(key, value);
    }

    /**
     * Get boolean preference
     * 
     * @param key
     *            the preference key
     * @return the preference value
     */
    protected boolean getPrefBoolean(String key) {
        return getPreferenceStore().getBoolean(key);
    }

    /**
     * Get default boolean preference
     * 
     * @param key
     *            the preference key
     * @return the default preference value
     */
    protected boolean getPrefDefBoolean(String key) {
        return getPreferenceStore().getDefaultBoolean(key);
    }

    /**
     * Set string preference
     * 
     * @param key
     *            the preference key
     * @param value
     *            the preference value
     */
    protected void setPrefString(String key, String value) {
        getPreferenceStore().setValue(key, value);
    }

    /**
     * Get string preference
     * 
     * @param key
     *            the preference key
     * @return the preference value
     */
    protected String getPrefString(String key) {
        return getPreferenceStore().getString(key);
    }

    /**
     * Get default string preference
     * 
     * @param key
     *            the preference key
     * @return the default preference value
     */
    protected String getPrefDefString(String key) {
        return getPreferenceStore().getDefaultString(key);
    }
}
