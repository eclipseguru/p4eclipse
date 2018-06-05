/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.tooltip;

import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.IBranchGraphElement;
import com.perforce.team.core.mergequest.model.Mapping;
import com.perforce.team.ui.mergequest.P4BranchGraphPlugin;
import com.perforce.team.ui.mergequest.parts.IControlProvider;
import com.perforce.team.ui.mergequest.preferences.IPreferenceConstants;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.FreeformLayeredPane;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.ScalableFreeformLayeredPane;
import org.eclipse.gef.LayerConstants;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchGraphToolTipManager implements IToolTipManager,
        IPropertyChangeListener {

    private IBranchGraphElement graphElement = null;
    private ToolTip tip = null;
    private IControlProvider provider;
    private boolean enabled = false;

    /**
     * Create tool tip manager
     * 
     * @param provider
     */
    public BranchGraphToolTipManager(IControlProvider provider) {
        this.provider = provider;
        P4BranchGraphPlugin.getDefault().getPreferenceStore()
                .addPropertyChangeListener(this);
        updateEnablement();
    }

    private double getScale(FigureCanvas canvas) {
        double scale = 1.0;
        IFigure root = canvas.getContents();
        if (root != null) {
            Object layer = ((FreeformLayeredPane) root)
                    .getLayer(LayerConstants.SCALABLE_LAYERS);
            if (layer instanceof ScalableFreeformLayeredPane) {
                scale = ((ScalableFreeformLayeredPane) layer).getScale();
            }
        }
        return scale;
    }

    private ToolTip createTip(IBranchGraphElement element, MouseEvent event) {
        ToolTip newTip = null;
        Control control = this.provider.getCurrentControl();
        if (control != null) {
            if (element instanceof Branch) {
                newTip = new BranchToolTip(control, (Branch) element);
            } else if (element instanceof Mapping) {
                newTip = new MappingToolTip(control, (Mapping) element);
            }

            // Convert based on viewport location if control is a figure canvas
            if (control instanceof FigureCanvas) {
                FigureCanvas canvas = (FigureCanvas) control;
                org.eclipse.draw2d.geometry.Point converted = new org.eclipse.draw2d.geometry.Point(
                        event.x, event.y);
                converted.scale(getScale(canvas));
                converted.translate(canvas.getViewport().getViewLocation()
                        .negate());
                event.x = converted.x;
                event.y = converted.y;
            }
        }
        return newTip;
    }

    /**
     * @see com.perforce.team.ui.mergequest.tooltip.IToolTipManager#showToolTip(com.perforce.team.core.mergequest.model.IBranchGraphElement,
     *      org.eclipse.draw2d.MouseEvent)
     */
    public void showToolTip(IBranchGraphElement element, MouseEvent event) {
        if (graphElement == null || !graphElement.equals(element)) {
            this.graphElement = element;
            if (tip != null) {
                tip.hide();
            }
        }
        if (enabled && this.graphElement != null) {
            this.tip = createTip(this.graphElement, event);
            if (this.tip != null) {
                tip.show(new Point(event.x + 5, event.y + 5));
            }
        }
    }

    /**
     * @see com.perforce.team.ui.mergequest.tooltip.IToolTipManager#hideToolTip(com.perforce.team.core.mergequest.model.IBranchGraphElement)
     */
    public void hideToolTip(IBranchGraphElement element) {
        if (this.graphElement != null && this.graphElement.equals(element)) {
            if (tip != null) {
                tip.hide();
            }
        }
    }

    /**
     * @see com.perforce.team.ui.mergequest.tooltip.IToolTipManager#dispose()
     */
    public void dispose() {
        P4BranchGraphPlugin.getDefault().getPreferenceStore()
                .removePropertyChangeListener(this);
    }

    private void updateEnablement() {
        enabled = P4BranchGraphPlugin.getDefault().getPreferenceStore()
                .getBoolean(IPreferenceConstants.SHOW_TOOLTIPS);
    }

    /**
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event) {
        if (IPreferenceConstants.SHOW_TOOLTIPS.equals(event.getProperty())) {
            updateEnablement();
        }
    }

}
