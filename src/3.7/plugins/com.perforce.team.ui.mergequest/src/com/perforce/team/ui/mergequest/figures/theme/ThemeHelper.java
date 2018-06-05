/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.figures.theme;

import com.perforce.team.ui.mergequest.P4BranchGraphPlugin;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ThemeHelper implements IPropertyChangeListener {

    /**
     * BG_PREFIX
     */
    public static final String BG_PREFIX = P4BranchGraphPlugin.PLUGIN_ID
            + ".background."; //$NON-NLS-1$

    /**
     * FG_PREFIX
     */
    public static final String FG_PREFIX = P4BranchGraphPlugin.PLUGIN_ID
            + ".foreground."; //$NON-NLS-1$

    /**
     * FONT_PREFIX
     */
    public static final String FONT_PREFIX = P4BranchGraphPlugin.PLUGIN_ID
            + ".font."; //$NON-NLS-1$

    /**
     * FONT_PREFIX
     */
    public static final String OUTLINE_PREFIX = P4BranchGraphPlugin.PLUGIN_ID
            + ".outline."; //$NON-NLS-1$

    private IThemeListener listener;

    private Color defaultForeground;
    private Color defaultBackground;
    private Color defaultOutline;
    private Font defaultFont;

    private String foreground;
    private String background;
    private String font;
    private String outline;
    private Runnable changeCallback = null;

    /**
     * Create a theme helper
     * 
     */
    public ThemeHelper() {
    }

    /**
     * Set listener callback
     * 
     * @param listener
     */
    public void setListener(IThemeListener listener) {
        this.listener = listener;
    }

    /**
     * Set foreground key
     * 
     * @param foreground
     */
    public void setForegroundKey(String foreground) {
        this.foreground = foreground;
    }

    /**
     * Set font key
     * 
     * @param font
     */
    public void setFontKey(String font) {
        this.font = font;
    }

    /**
     * Set background key
     * 
     * @param background
     */
    public void setBackgroundKey(String background) {
        this.background = background;
    }

    /**
     * Set outline key
     * 
     * @param outline
     */
    public void setOutlineKey(String outline) {
        this.outline = outline;
    }

    private IThemeManager getThemeManager() {
        return PlatformUI.getWorkbench().getThemeManager();
    }

    /**
     * Synchronize the figure with the current theme
     */
    public void synchronize() {
        ITheme current = getThemeManager().getCurrentTheme();
        ColorRegistry registry = current.getColorRegistry();
        if (this.listener != null) {
            if (this.background != null) {
                Color newBackground = registry.get(this.background);
                if (newBackground == null) {
                    newBackground = this.defaultBackground;
                }
                this.listener.setBackgroundColor(newBackground);
            }
            if (this.foreground != null) {
                Color newForeground = registry.get(this.foreground);
                if (newForeground == null) {
                    newForeground = this.defaultForeground;
                }
                this.listener.setForegroundColor(newForeground);
            }
            if (this.font != null) {
                Font newFont = current.getFontRegistry().get(this.font);
                if (newFont == null) {
                    newFont = this.defaultFont;
                }
                this.listener.setFont(newFont);
            }
            if (this.outline != null) {
                Color newOutline = registry.get(this.outline);
                if (newOutline == null) {
                    newOutline = this.defaultOutline;
                }
                this.listener.setOutlineColor(newOutline);
            }
            if (changeCallback != null) {
                changeCallback.run();
            }
        }
    }

    /**
     * Active the theme helper and synchronize the figure with the current theme
     */
    public void activate() {
        activate(null);
    }

    /**
     * Active the theme helper and synchronize the figure with the current theme
     * 
     * @param callback
     */
    public void activate(Runnable callback) {
        this.changeCallback = callback;
        getThemeManager().addPropertyChangeListener(this);
        synchronize();
    }

    /**
     * Deactivate the theme helper
     */
    public void deactivate() {
        getThemeManager().removePropertyChangeListener(this);
    }

    /**
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event) {
        String property = event.getProperty();
        if (property == null) {
            return;
        }
        if (IThemeManager.CHANGE_CURRENT_THEME.equals(property)
                || property.equals(this.foreground)
                || property.equals(this.background)
                || property.equals(this.font) || property.equals(this.outline)) {
            synchronize();
        }
    }

    /**
     * @return the defaultForeground
     */
    public Color getDefaultForeground() {
        return this.defaultForeground;
    }

    /**
     * @param defaultForeground
     *            the defaultForeground to set
     */
    public void setDefaultForeground(Color defaultForeground) {
        this.defaultForeground = defaultForeground;
    }

    /**
     * @return the defaultBackground
     */
    public Color getDefaultBackground() {
        return this.defaultBackground;
    }

    /**
     * @param defaultBackground
     *            the defaultBackground to set
     */
    public void setDefaultBackground(Color defaultBackground) {
        this.defaultBackground = defaultBackground;
    }

    /**
     * @return the defaultOutline
     */
    public Color getDefaultOutline() {
        return this.defaultOutline;
    }

    /**
     * @param defaultOutline
     *            the defaultOutline to set
     */
    public void setDefaultOutline(Color defaultOutline) {
        this.defaultOutline = defaultOutline;
    }

    /**
     * @return the defaultFont
     */
    public Font getDefaultFont() {
        return this.defaultFont;
    }

    /**
     * @param defaultFont
     *            the defaultFont to set
     */
    public void setDefaultFont(Font defaultFont) {
        this.defaultFont = defaultFont;
    }

}
