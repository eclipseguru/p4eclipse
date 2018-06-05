/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.text;

import com.perforce.team.core.PerforceProviderPlugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.osgi.framework.Bundle;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class TextUtils {

    /**
     * Get editors plugin
     * 
     * @return - editors plugin
     */
    public static EditorsPlugin getEditorsPlugin() {
        return EditorsPlugin.getDefault();
    }

    private static Method modelLineToWidgetLine;
    private static Method getHiddenTopLinePixels;
    private static Method getVisibleModelLines;
    private static Method isShowingEntireContents;

    static {
        Class<?> jfaceTextUtil = null;
        Bundle bundle = Platform.getBundle("org.eclipse.jface.text"); //$NON-NLS-1$
        if (bundle != null) {
            try {
                jfaceTextUtil = bundle
                        .loadClass("org.eclipse.jface.text.JFaceTextUtil"); //$NON-NLS-1$
            } catch (Throwable e) {
                try {
                    jfaceTextUtil = bundle
                            .loadClass("org.eclipse.jface.internal.text.JFaceTextUtil"); //$NON-NLS-1$
                } catch (Throwable e1) {
                }
            }
        }
        if (jfaceTextUtil != null) {
            try {
                modelLineToWidgetLine = jfaceTextUtil.getMethod(
                        "modelLineToWidgetLine", ITextViewer.class, //$NON-NLS-1$
                        Integer.TYPE);
            } catch (SecurityException e) {
                PerforceProviderPlugin.logError(e);
            } catch (NoSuchMethodException e) {
                PerforceProviderPlugin.logError(e);
            }
            try {
                getHiddenTopLinePixels = jfaceTextUtil.getMethod(
                        "getHiddenTopLinePixels", StyledText.class); //$NON-NLS-1$
            } catch (SecurityException e) {
                PerforceProviderPlugin.logError(e);
            } catch (NoSuchMethodException e) {
                PerforceProviderPlugin.logError(e);
            }
            try {
                getVisibleModelLines = jfaceTextUtil.getMethod(
                        "getVisibleModelLines", ITextViewer.class); //$NON-NLS-1$
            } catch (SecurityException e) {
                PerforceProviderPlugin.logError(e);
            } catch (NoSuchMethodException e) {
                PerforceProviderPlugin.logError(e);
            }
            try {
                isShowingEntireContents = jfaceTextUtil.getMethod(
                        "isShowingEntireContents", StyledText.class); //$NON-NLS-1$
            } catch (SecurityException e) {
                PerforceProviderPlugin.logError(e);
            } catch (NoSuchMethodException e) {
                PerforceProviderPlugin.logError(e);
            }
        }
    }

    /**
     * Forwards to JFaceTextUtil.modelLineToWidgetLine
     * 
     * @param viewer
     * @param modelLine
     * @return - int
     */
    public static int modelLineToWidgetLine(ITextViewer viewer,
            final int modelLine) {
        int line = -1;
        if (modelLineToWidgetLine != null) {
            try {
                line = (Integer) modelLineToWidgetLine.invoke(null, viewer,
                        modelLine);
            } catch (IllegalArgumentException e) {
                PerforceProviderPlugin.logError(e);
            } catch (IllegalAccessException e) {
                PerforceProviderPlugin.logError(e);
            } catch (InvocationTargetException e) {
                PerforceProviderPlugin.logError(e);
            }
        }
        return line;
    }

    /**
     * Forwards to JFaceTextUtil.getHiddenTopLinePixels
     * 
     * @param textWidget
     * @return - int
     */
    public static int getHiddenTopLinePixels(StyledText textWidget) {
        int line = -1;
        if (getHiddenTopLinePixels != null) {
            try {
                line = (Integer) getHiddenTopLinePixels
                        .invoke(null, textWidget);
            } catch (IllegalArgumentException e) {
                PerforceProviderPlugin.logError(e);
            } catch (IllegalAccessException e) {
                PerforceProviderPlugin.logError(e);
            } catch (InvocationTargetException e) {
                PerforceProviderPlugin.logError(e);
            }
        }
        return line;
    }

    /**
     * Forwards to JFaceTextUtil.getVisibleModelLines
     * 
     * @param viewer
     * @return - line range
     */
    public static ILineRange getVisibleModelLines(ITextViewer viewer) {
        ILineRange lineRange = null;
        if (getVisibleModelLines != null && viewer != null
                && viewer.getDocument() != null) {
            try {
                lineRange = (ILineRange) getVisibleModelLines.invoke(null,
                        viewer);
            } catch (IllegalArgumentException e) {
                PerforceProviderPlugin.logError(e);
            } catch (IllegalAccessException e) {
                PerforceProviderPlugin.logError(e);
            } catch (InvocationTargetException e) {
                PerforceProviderPlugin.logError(e);
            }
        }
        return lineRange;
    }

    /**
     * Forwards to JFaceTextUtil.isShowingEntireContents
     * 
     * @param widget
     * @return - boolean
     */
    public static boolean isShowingEntireContents(StyledText widget) {
        boolean showing = false;
        if (isShowingEntireContents != null) {
            try {
                showing = (Boolean) isShowingEntireContents
                        .invoke(null, widget);
            } catch (IllegalArgumentException e) {
                PerforceProviderPlugin.logError(e);
            } catch (IllegalAccessException e) {
                PerforceProviderPlugin.logError(e);
            } catch (InvocationTargetException e) {
                PerforceProviderPlugin.logError(e);
            }
        }
        return showing;
    }

    /**
     * Get rgb value from the editor preference store
     * 
     * @param key
     * @return - rgb
     */
    public static RGB getEditorRgb(String key) {
        return PreferenceConverter.getColor(getEditorStore(), key);
    }

    /**
     * Get editor preference store
     * 
     * @return - preference store
     */
    public static IPreferenceStore getEditorStore() {
        return getEditorsPlugin().getPreferenceStore();
    }

    /**
     * Get editor color from the shared text colors store
     * 
     * @param key
     * @return - color
     */
    public static Color getEditorColor(String key) {
        RGB rgb = getEditorRgb(key);
        return getSharedTextColors().getColor(rgb);
    }

    /**
     * Get editor shared text colors
     * 
     * @return - shared text colors
     */
    public static ISharedTextColors getSharedTextColors() {
        return getEditorsPlugin().getSharedTextColors();
    }

}
