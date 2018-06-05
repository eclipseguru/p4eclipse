/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.views;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.preferences.IPreferenceConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class SessionManager {

    /**
     * Gets the split entries from a preference key
     * 
     * @param preference
     * @return - values
     */
    public static String[] getEntries(String preference) {
        return getEntries(preference, true);
    }

    /**
     * Gets the split entries from a preference key
     * 
     * @param preference
     * @param store
     * @return - values
     */
    public static String[] getEntries(String preference, IPreferenceStore store) {
        return getEntries(preference, true, store);
    }

    /**
     * Gets the split entries from a preference key
     * 
     * @param preference
     * @param trim
     * @return - values
     */
    public static String[] getEntries(String preference, boolean trim) {
        return getEntries(preference, trim, PerforceUIPlugin.getPlugin()
                .getPreferenceStore());
    }

    /**
     * Gets the split entries from a preference key
     * 
     * @param preference
     * @param trim
     * @param store
     * @return - values
     */
    public static String[] getEntries(String preference, boolean trim,
            IPreferenceStore store) {
        List<String> splitItems = new ArrayList<String>();
        if (preference != null && store != null) {
            String items = store.getString(preference);
            String[] splitStrings = items
                    .split(IPreferenceConstants.VALUE_DELIMITER);

            if (trim) {
                for (String split : splitStrings) {
                    if (split != null) {
                        split = split.trim();
                        if (split.length() > 0) {
                            splitItems.add(split);
                        }
                    }
                }
            } else {
                for (String split : splitStrings) {
                    if (split != null && split.length() > 0) {
                        splitItems.add(split);
                    }
                }
            }

        }
        return splitItems.toArray(new String[0]);

    }

    /**
     * Save a list of entries to the specified preference
     * 
     * @param entries
     * @param preference
     */
    public static void saveEntries(List<String> entries, String preference) {
        if (entries != null && preference != null) {
            StringBuilder entriesValue = new StringBuilder();
            for (String entry : entries) {
                entriesValue.append(entry);
                entriesValue.append(IPreferenceConstants.VALUE_DELIMITER);
            }
            PerforceUIPlugin.getPlugin().getPreferenceStore()
                    .setValue(preference, entriesValue.toString());
        }
    }

    /**
     * Update combo history by saving the max entries currently in the combo
     * plus the current field text.
     * 
     * @param combo
     * @param max
     * @param preference
     * @param store
     */
    public static void saveComboHistory(Combo combo, int max,
            String preference, IPreferenceStore store) {
        if (combo == null || max < 1 || preference == null || store == null) {
            return;
        }
        String current = combo.getText();
        List<String> entries = new ArrayList<String>();
        if (current.length() > 0) {
            entries.add(current);
        }
        for (String item : combo.getItems()) {
            if (!entries.contains(item)) {
                entries.add(item);
            }
            if (entries.size() == max) {
                break;
            }
        }
        combo.removeAll();
        StringBuilder value = new StringBuilder();
        for (String item : entries) {
            combo.add(item, combo.getItemCount());
            value.append(item).append(IPreferenceConstants.VALUE_DELIMITER);
        }
        store.setValue(preference, value.toString());
        combo.select(0);
    }

    /**
     * Update combo history by saving the max entries currently in the combo
     * plus the current field text.
     * 
     * @param combo
     * @param max
     * @param preference
     */
    public static void saveComboHistory(Combo combo, int max, String preference) {
        saveComboHistory(combo, max, preference, PerforceUIPlugin.getPlugin()
                .getPreferenceStore());
    }

    /**
     * Load combo history
     * 
     * @param combo
     * @param preference
     */
    public static void loadComboHistory(Combo combo, String preference) {
        loadComboHistory(combo, preference, PerforceUIPlugin.getPlugin()
                .getPreferenceStore());
    }

    /**
     * Load combo history
     * 
     * @param combo
     * @param preference
     * @param store
     */
    public static void loadComboHistory(Combo combo, String preference,
            IPreferenceStore store) {
        if (combo == null || preference == null || store == null) {
            return;
        }
        String[] entries = getEntries(preference, store);
        for (String entry : entries) {
            combo.add(entry);
        }
    }

    /**
     * Save the items in the list
     * 
     * @param items
     * @param preference
     */
    public static void saveHistory(List<String> items, String preference) {
        if (items != null && preference != null) {
            StringBuilder usersBuffer = new StringBuilder();
            for (String item : items) {
                usersBuffer.append(item).append(
                        PerforceProjectView.SIZE_DELIMITER);
            }
            PerforceUIPlugin.getPlugin().getPreferenceStore()
                    .setValue(preference, usersBuffer.toString());
        }

    }

    /**
     * Save the column sizes in the tree
     * 
     * @param tree
     * @param preference
     */
    public static void saveColumnPreferences(Tree tree, String preference) {
        if (preference != null && tree != null && !tree.isDisposed()) {
            StringBuilder buffer = new StringBuilder();
            for (TreeColumn column : tree.getColumns()) {
                buffer.append(column.getText() + "=" + column.getWidth() //$NON-NLS-1$
                        + PerforceProjectView.SIZE_DELIMITER);
            }
            PerforceUIPlugin.getPlugin().getPreferenceStore()
                    .setValue(preference, buffer.toString());
        }
    }

    /**
     * Save the column sizes in the table
     * 
     * @param table
     * @param preference
     */
    public static void saveColumnPreferences(Table table, String preference) {
        if (preference != null && table != null && !table.isDisposed()) {
            StringBuffer buffer = new StringBuffer();
            for (TableColumn column : table.getColumns()) {
                buffer.append(column.getText() + "=" + column.getWidth() //$NON-NLS-1$
                        + PerforceProjectView.SIZE_DELIMITER);
            }
            PerforceUIPlugin.getPlugin().getPreferenceStore()
                    .setValue(preference, buffer.toString());
        }
    }

    /**
     * Load column sizes
     * 
     * @param preference
     * @return - map of column sizes
     */
    public static Map<String, Integer> loadColumnSizes(String preference) {
        Map<String, Integer> columnSizes = new HashMap<String, Integer>();
        if (preference != null) {
            String columns = PerforceUIPlugin.getPlugin().getPreferenceStore()
                    .getString(preference);
            if (columns.length() > 0) {
                String[] pairs = columns
                        .split(PerforceProjectView.SIZE_DELIMITER);
                for (String pair : pairs) {
                    String[] nameValue = pair.split("="); //$NON-NLS-1$
                    if (nameValue.length == 2) {
                        try {
                            Integer value = Integer.parseInt(nameValue[1]);
                            columnSizes.put(nameValue[0], value);
                        } catch (NumberFormatException nfe) {
                            PerforceProviderPlugin.logError(nfe);
                        }
                    }
                }
            }
        }
        return columnSizes;
    }

}
