/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.ruby.timelapse;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.internal.ui.editor.ISourceModuleDocumentProvider;
import org.eclipse.dltk.ruby.core.RubyLanguageToolkit;
import org.eclipse.dltk.ruby.internal.ui.RubyUI;
import org.eclipse.dltk.ruby.internal.ui.editor.RubyOutlineLabelDecorator;
import org.eclipse.dltk.ruby.internal.ui.text.RubyTextTools;
import org.eclipse.dltk.ui.DLTKUIPlugin;
import org.eclipse.dltk.ui.EclipsePreferencesAdapter;
import org.eclipse.dltk.ui.PreferencesAdapter;
import org.eclipse.dltk.ui.ScriptElementImageProvider;
import org.eclipse.dltk.ui.ScriptElementLabels;
import org.eclipse.dltk.ui.viewsupport.AppearanceAwareLabelProvider;
import org.eclipse.dltk.ui.viewsupport.DecoratingModelLabelProvider;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public final class RubyUtils {

    /**
     * Create a ruby label provider with decorations
     * 
     * @return - label provider
     */
    public static ILabelProvider createLabelProvider() {
        AppearanceAwareLabelProvider base = new AppearanceAwareLabelProvider(
                AppearanceAwareLabelProvider.DEFAULT_TEXTFLAGS
                        | ScriptElementLabels.F_APP_TYPE_SIGNATURE
                        | ScriptElementLabels.ALL_CATEGORY,
                AppearanceAwareLabelProvider.DEFAULT_IMAGEFLAGS
                        | ScriptElementImageProvider.SMALL_ICONS, RubyUI
                        .getDefault().getPreferenceStore());
        base.addLabelDecorator(new RubyOutlineLabelDecorator());
        return new DecoratingModelLabelProvider(base);
    }

    /**
     * Creates and returns the preference store for this provider.
     * 
     * @return the preference store for this provider
     */
    public static IPreferenceStore createCombinedPreferenceStore() {
        List<IPreferenceStore> stores = new ArrayList<IPreferenceStore>();
        String preferenceQualifier = RubyLanguageToolkit.getDefault()
                .getPreferenceQualifier();
        stores.add(getPreferences());
        if (preferenceQualifier != null) {
            stores.add(new EclipsePreferencesAdapter(new InstanceScope(),
                    preferenceQualifier));
            stores.add(new EclipsePreferencesAdapter(new DefaultScope(),
                    preferenceQualifier));
        }
        stores.add(new PreferencesAdapter(DLTKCore.getDefault()
                .getPluginPreferences()));
        stores.add(EditorsUI.getPreferenceStore());
        stores.add(PlatformUI.getPreferenceStore());
        return new ChainedPreferenceStore(
                stores.toArray(new IPreferenceStore[stores.size()]));
    }

    /**
     * Get text tools to use
     * 
     * @return - java text tools
     */
    public static RubyTextTools getTools() {
        return RubyUI.getDefault().getTextTools();
    }

    /**
     * Get document provider to use
     * 
     * @return - compilation unit document provider
     */
    public static ISourceModuleDocumentProvider getProvider() {
        return DLTKUIPlugin.getDefault().getSourceModuleDocumentProvider();
    }

    /**
     * Get java plugin combined preference store
     * 
     * @return - preference store
     */
    public static IPreferenceStore getPreferences() {
        return RubyUI.getDefault().getPreferenceStore();
    }

    /**
     * Get compilation unit from an editor input class
     * 
     * @param input
     * @return - compilation unit
     */
    public static ISourceModule getWorkingCopy(IEditorInput input) {
        return DLTKUIPlugin.getDefault().getWorkingCopyManager()
                .getWorkingCopy(input, false);
    }
}
