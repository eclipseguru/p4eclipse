/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.changelists;

import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.preferences.IPreferenceConstants;
import com.perforce.team.ui.views.SessionManager;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class DescriptionTemplate implements IWorkbenchAdapter, IAdaptable {

    /**
     * Get current templates from the pref store
     * 
     * @return - array of templates
     */
    public static List<DescriptionTemplate> getTemplates() {
        List<DescriptionTemplate> templates = new ArrayList<DescriptionTemplate>();
        for (String content : SessionManager.getEntries(
                IPreferenceConstants.CHANGELIST_TEMPLATES, false)) {
            templates.add(new DescriptionTemplate(content));
        }
        return templates;
    }

    /**
     * Get previously entered changelist description
     * 
     * @return - previously entered changelist descriptions
     */
    public static List<DescriptionTemplate> getHistory() {
        List<DescriptionTemplate> templates = new ArrayList<DescriptionTemplate>();
        for (String content : SessionManager.getEntries(
                IPreferenceConstants.CHANGELIST_DESCRIPTIONS, false)) {
            templates.add(new DescriptionTemplate(content));
        }
        return templates;
    }

    /**
     * Save changelist description history
     * 
     * @param latest
     */
    public static void saveHistory(String latest) {
        if (latest != null) {

            boolean shouldAdd = true;

            // Also check latest is not a direct template match
            String[] templates = SessionManager.getEntries(
                    IPreferenceConstants.CHANGELIST_TEMPLATES, false);
            for (String entry : templates) {
                if (latest.equals(entry)) {
                    shouldAdd = false;
                    break;
                }
            }

            if (shouldAdd) {
                // Only save new if latest is not in history already
                String[] current = SessionManager.getEntries(
                        IPreferenceConstants.CHANGELIST_DESCRIPTIONS, false);
                int max = PerforceUIPlugin.getPlugin().getPreferenceStore()
                        .getInt(IPreferenceConstants.CHANGELISTS_SAVED);
                int count = Math.min(max, current.length + 1);
                List<String> entries = new ArrayList<String>();
                entries.add(latest);
                int index = 0;
                while (index < current.length && entries.size() < count) {
                    String entry = current[index];
                    if (!entries.contains(entry)) {
                        entries.add(entry);
                    }
                    index++;
                }
                SessionManager.saveHistory(entries,
                        IPreferenceConstants.CHANGELIST_DESCRIPTIONS);
            }
        }
    }

    /**
     * Save specified templates
     * 
     * @param templates
     */
    public static void saveTemplates(List<DescriptionTemplate> templates) {
        if (templates != null) {
            List<String> entries = new ArrayList<String>();
            for (DescriptionTemplate tpl : templates) {
                String content = tpl.getContent();
                if (content != null) {
                    entries.add(content);
                }
            }
            SessionManager.saveEntries(entries,
                    IPreferenceConstants.CHANGELIST_TEMPLATES);
        }
    }

    private String content = null;
    private String formatted = null;

    /**
     * Create a new description template with content
     * 
     * @param content
     */
    public DescriptionTemplate(String content) {
        this.content = content;
        format();
    }

    private void format() {
        if (this.content != null) {
            this.formatted = this.content.replace("\n", "\\n").replace("\r", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    "\\r").replace("\t", "\\t"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        } else {
            this.formatted = ""; //$NON-NLS-1$
        }
    }

    /**
     * @return the content
     */
    public String getContent() {
        return this.content;
    }

    /**
     * @param content
     *            the content to set
     */
    public void setContent(String content) {
        this.content = content;
        format();
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object o) {
        return null;
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
     */
    public ImageDescriptor getImageDescriptor(Object object) {
        return null;
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
     */
    public String getLabel(Object o) {
        return this.formatted;
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
     */
    public Object getParent(Object o) {
        return null;
    }

    /**
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
        if (IWorkbenchAdapter.class == adapter) {
            return this;
        }
        return null;
    }

}
