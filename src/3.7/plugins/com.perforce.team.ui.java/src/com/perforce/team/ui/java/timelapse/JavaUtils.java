/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.java.timelapse;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.ICompilationUnitDocumentProvider;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IEditorInput;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public final class JavaUtils {

    /**
     * Get text tools to use
     * 
     * @return - java text tools
     */
    public static JavaTextTools getTools() {
        return JavaPlugin.getDefault().getJavaTextTools();
    }

    /**
     * Get document provider to use
     * 
     * @return - compilation unit document provider
     */
    public static ICompilationUnitDocumentProvider getProvider() {
        return JavaPlugin.getDefault().getCompilationUnitDocumentProvider();
    }

    /**
     * Get java plugin combined preference store
     * 
     * @return - preference store
     */
    public static IPreferenceStore getPreferences() {
        return JavaPlugin.getDefault().getCombinedPreferenceStore();
    }

    /**
     * Get compilation unit from an editor input class
     * 
     * @param input
     * @return - compilation unit
     */
    public static ICompilationUnit getWorkingCopy(IEditorInput input) {
        return JavaPlugin.getDefault().getWorkingCopyManager()
                .getWorkingCopy(input, false);
    }
}
