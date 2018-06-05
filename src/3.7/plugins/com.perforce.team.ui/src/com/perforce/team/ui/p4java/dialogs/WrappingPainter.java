/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.dialogs;

import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.preferences.IPreferenceConstants;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.MarginPainter;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class WrappingPainter {

    private TextViewer viewer = null;
    private MarginPainter painter = null;
    private Color color = null;
    private IPropertyChangeListener listener = new IPropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent event) {
            String property = event.getProperty();
            if (IPreferenceConstants.DESCRIPTION_RULER.equals(property)
                    || IPreferenceConstants.DESCRIPTION_RULER_COLOR
                            .equals(property)
                    || IPreferenceConstants.DESCRIPTION_RULER_COLUMN
                            .equals(property)
                    || IPreferenceConstants.DESCRIPTION_RULER_STYLE
                            .equals(property)) {
                refresh();
            }
        }
    };

    /**
     * Creates a new wrapping guide painter
     * 
     * @param viewer
     */
    public WrappingPainter(TextViewer viewer) {
        this.viewer = viewer;
        PerforceUIPlugin.getPlugin().getPreferenceStore()
                .addPropertyChangeListener(listener);
        this.viewer.getTextWidget().addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                try {
                    if (color != null && !color.isDisposed()) {
                        color.dispose();
                    }
                } finally {
                    PerforceUIPlugin.getPlugin().getPreferenceStore()
                            .removePropertyChangeListener(listener);
                }
            }
        });
        refresh();
    }

    /**
     * Show the wrapping painter
     */
    private void show() {
        Color oldColor = this.color;
        boolean add = false;
        if (this.painter == null) {
            this.painter = new MarginPainter(this.viewer);
            this.painter.setMarginRulerWidth(1);
            add = true;
        }
        IPreferenceStore store = PerforceUIPlugin.getPlugin()
                .getPreferenceStore();

        int column = store
                .getInt(IPreferenceConstants.DESCRIPTION_RULER_COLUMN);
        this.painter.setMarginRulerColumn(column);

        int style = store.getInt(IPreferenceConstants.DESCRIPTION_RULER_STYLE);
        this.painter.setMarginRulerStyle(style);

        RGB rgb = PreferenceConverter.getColor(store,
                IPreferenceConstants.DESCRIPTION_RULER_COLOR);
        this.color = new Color(this.viewer.getTextWidget().getDisplay(), rgb);
        // Once new color is set, dispose of old color
        this.painter.setMarginRulerColor(this.color);
        if (oldColor != null && !oldColor.isDisposed()) {
            oldColor.dispose();
        }

        if (add) {
            this.viewer.addPainter(this.painter);
        }
        this.painter.initialize();
    }

    /**
     * Hide the wrapping painter
     */
    private void hide() {
        if (this.painter != null) {
            this.viewer.removePainter(this.painter);
            this.painter.deactivate(true);
            this.painter.dispose();
            this.painter = null;
        }
    }

    /**
     * Refresh painter state
     */
    public void refresh() {
        if (!this.viewer.getTextWidget().isDisposed()) {
            IPreferenceStore store = PerforceUIPlugin.getPlugin()
                    .getPreferenceStore();
            boolean enabled = store
                    .getBoolean(IPreferenceConstants.DESCRIPTION_RULER);
            if (enabled) {
                int column = store
                        .getInt(IPreferenceConstants.DESCRIPTION_RULER_COLUMN);
                enabled = column > 0;
            }
            if (enabled) {
                show();
            } else {
                hide();
            }
        }
    }
}
