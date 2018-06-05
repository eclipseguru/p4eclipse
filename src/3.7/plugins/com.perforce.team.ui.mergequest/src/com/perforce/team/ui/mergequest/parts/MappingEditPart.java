/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.parts;

import com.perforce.team.core.mergequest.model.IBranchGraphElement;
import com.perforce.team.core.mergequest.model.Mapping;
import com.perforce.team.core.mergequest.model.Mapping.Joint;
import com.perforce.team.core.mergequest.model.MappingDescriptor.ChangeType;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.mergequest.actions.MappingEditAction;
import com.perforce.team.ui.mergequest.figures.MappingFigure;
import com.perforce.team.ui.mergequest.figures.theme.CompositeFigureThemeHelper;
import com.perforce.team.ui.mergequest.figures.theme.FigureThemeHelper;
import com.perforce.team.ui.mergequest.figures.theme.ThemeHelper;
import com.perforce.team.ui.mergequest.figures.theme.ThemeListenerAdapter;
import com.perforce.team.ui.mergequest.policies.MappingBendpointEditPolicy;
import com.perforce.team.ui.mergequest.policies.MappingEditPolicy;
import com.perforce.team.ui.mergequest.policies.MappingSelectionEditPolicy;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.RelativeBendpoint;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.swt.graphics.Color;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class MappingEditPart extends AbstractConnectionEditPart implements
        PropertyChangeListener {

    private class MappingColorProvider implements IColorProvider {

        private ThemeHelper enabledHelper;
        private ThemeHelper disabledHelper;
        private Color enabledForeground = null;
        private Color disabledForeground = null;

        public MappingColorProvider() {
            this.enabledHelper = new ThemeHelper();
            this.enabledHelper.setListener(new ThemeListenerAdapter() {

                @Override
                public void setForegroundColor(Color color) {
                    enabledForeground = color;
                    refreshVisuals();
                }

            });
            this.enabledHelper.setForegroundKey(ThemeHelper.FG_PREFIX
                    + "mapping.enabled"); //$NON-NLS-1$
            this.disabledHelper = new ThemeHelper();
            this.disabledHelper.setForegroundKey(ThemeHelper.FG_PREFIX
                    + "mapping.disabled"); //$NON-NLS-1$
            this.disabledHelper.setListener(new ThemeListenerAdapter() {

                @Override
                public void setForegroundColor(Color color) {
                    disabledForeground = color;
                    refreshVisuals();
                }

            });
        }

        /**
         * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
         */
        public Color getBackground(Object element) {
            return null;
        }

        /**
         * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
         */
        public Color getForeground(Object element) {
            if (element instanceof ChangeType) {
                if (ChangeType.VISIBLE_CHANGES == element) {
                    return enabledForeground;
                } else {
                    return disabledForeground;
                }
            }
            return null;
        }

        /**
         * Activate the helpers
         */
        public void activate() {
            this.enabledHelper.activate();
            this.disabledHelper.activate();
        }

        /**
         * Deactivate the helpers
         */
        public void deactivate() {
            this.enabledHelper.deactivate();
            this.disabledHelper.deactivate();
        }

    }

    private SharedResources images;
    private ToolTipHelper tooltipHelper;
    private MappingColorProvider colorProvider;
    private FigureThemeHelper mappingThemeHelper;
    private CompositeFigureThemeHelper countThemeHelper;

    /**
     * Create a mapping edit part
     * 
     * @param images
     */
    public MappingEditPart(SharedResources images) {
        this.images = images;

        this.tooltipHelper = new ToolTipHelper(this);
        this.mappingThemeHelper = new FigureThemeHelper();
        this.mappingThemeHelper.setFontKey(ThemeHelper.FONT_PREFIX
                + "mapping.font"); //$NON-NLS-1$
        this.mappingThemeHelper.setBackgroundKey(ThemeHelper.BG_PREFIX
                + "mapping"); //$NON-NLS-1$
        this.countThemeHelper = createCountThemeHelper();
        this.colorProvider = new MappingColorProvider();
    }

    private CompositeFigureThemeHelper createCountThemeHelper() {
        String prefix = "mapping.count"; //$NON-NLS-1$
        CompositeFigureThemeHelper helper = new CompositeFigureThemeHelper();
        helper.setBackgroundKey(ThemeHelper.BG_PREFIX + prefix);
        helper.setOutlineKey(ThemeHelper.OUTLINE_PREFIX + prefix);
        helper.setFontKey(ThemeHelper.FONT_PREFIX + prefix);
        return helper;
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#activate()
     */
    @Override
    public void activate() {
        if (!isActive()) {
            super.activate();
            getMapping().addPropertyListener(this);
            this.tooltipHelper.activate();
            this.mappingThemeHelper.activate();
            this.countThemeHelper.activate();
            this.colorProvider.activate();
        }
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#deactivate()
     */
    @Override
    public void deactivate() {
        if (isActive()) {
            getMapping().removePropertyListener(this);
            this.tooltipHelper.deactivate();
            this.mappingThemeHelper.deactivate();
            this.colorProvider.deactivate();
            this.countThemeHelper.deactivate();
            super.deactivate();
        }
    }

    /**
     * Get mapping
     * 
     * @return mapping
     */
    public Mapping getMapping() {
        return (Mapping) getModel();
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getAdapter(java.lang.Class)
     */
    @Override
    public Object getAdapter(Class key) {
        if (key == Mapping.class || key == IBranchGraphElement.class) {
            return getMapping();
        }
        return super.getAdapter(key);
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractConnectionEditPart#createFigure()
     */
    @Override
    protected IFigure createFigure() {
        Mapping mapping = getMapping();
        MappingFigure figure = new MappingFigure(mapping, images);
        figure.setOutlineMappingLabel(true);
        figure.setColorProvider(this.colorProvider);
        this.mappingThemeHelper.setFigure(figure);
        this.countThemeHelper.setFigures(new IFigure[] {
                figure.getSourceCountFigure(), figure.getTargetCountFigure() });
        figure.updateStatus(mapping);
        return figure;
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
     */
    @Override
    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE,
                new MappingSelectionEditPolicy());
        installEditPolicy(EditPolicy.CONNECTION_ROLE, new MappingEditPolicy(
                getMapping()));
        installEditPolicy(EditPolicy.CONNECTION_BENDPOINTS_ROLE,
                new MappingBendpointEditPolicy());
    }

    private MappingFigure getMappingFigure() {
        return (MappingFigure) getFigure();
    }

    /**
     * Refresh bendpoints
     */
    protected void refreshBendpoints() {
        Joint[] joints = getMapping().getJoints();
        List<RelativeBendpoint> figureConstraint = new ArrayList<RelativeBendpoint>();
        int count = 0;
        for (Joint joint : joints) {
            RelativeBendpoint rbp = new RelativeBendpoint(getConnectionFigure());
            rbp.setRelativeDimensions(
                    new Dimension(joint.getX1(), joint.getY1()), new Dimension(
                            joint.getX2(), joint.getY2()));
            rbp.setWeight((count + 1) / ((float) joints.length + 1));
            figureConstraint.add(rbp);
            count++;
        }
        getConnectionFigure().setRoutingConstraint(figureConstraint);
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractEditPart#refreshVisuals()
     */
    @Override
    protected void refreshVisuals() {
        if (isActive()) {
            if (PerforceUIPlugin.isUIThread()) {
                getMappingFigure().updateStatus(getMapping());
                refreshSourceAnchor();
                refreshTargetAnchor();
                refreshBendpoints();
            } else {
                PerforceUIPlugin.asyncExec(new Runnable() {

                    public void run() {
                        refreshVisuals();
                    }
                });
            }
        }
    }

    /**
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        refreshVisuals();
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractEditPart#performRequest(org.eclipse.gef.Request)
     */
    @Override
    public void performRequest(Request req) {
        if (RequestConstants.REQ_OPEN.equals(req.getType())) {
            MappingEditAction edit = new MappingEditAction(getMapping());
            edit.run();
        } else {
            super.performRequest(req);
        }
    }

}
