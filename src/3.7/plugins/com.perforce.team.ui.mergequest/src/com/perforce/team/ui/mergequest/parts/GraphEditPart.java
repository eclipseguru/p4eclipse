/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.parts;

import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.BranchGraph;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.ui.mergequest.policies.GraphXYLayoutEditPolicy;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.draw2d.BendpointConnectionRouter;
import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.gef.CompoundSnapToHelper;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.SnapToGeometry;
import org.eclipse.gef.SnapToGrid;
import org.eclipse.gef.SnapToGuides;
import org.eclipse.gef.SnapToHelper;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;
import org.eclipse.gef.rulers.RulerProvider;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class GraphEditPart extends AbstractGraphicalEditPart implements
        PropertyChangeListener {

    private IBranchGraph graph;

    /**
     * Graph edit part
     * 
     * @param graph
     */
    public GraphEditPart(IBranchGraph graph) {
        this.graph = graph;
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#activate()
     */
    @Override
    public void activate() {
        if (!isActive()) {
            super.activate();
            graph.addPropertyListener(this);
        }
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#deactivate()
     */
    @Override
    public void deactivate() {
        if (isActive()) {
            graph.removePropertyListener(this);
            super.deactivate();
        }
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
     */
    @Override
    protected IFigure createFigure() {
        IFigure figure = new FreeformLayer();
        figure.setBorder(new MarginBorder(10));
        figure.setLayoutManager(new FreeformLayout());

        ConnectionLayer connLayer = (ConnectionLayer) getLayer(LayerConstants.CONNECTION_LAYER);
        BendpointConnectionRouter router = new BendpointConnectionRouter();
        connLayer.setConnectionRouter(router);

        return figure;
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getAdapter(java.lang.Class)
     */
    @Override
    public Object getAdapter(Class key) {
        if (key == SnapToHelper.class) {
            List<SnapToHelper> snapStrategies = new ArrayList<SnapToHelper>();
            Boolean value = (Boolean) getViewer().getProperty(
                    RulerProvider.PROPERTY_RULER_VISIBILITY);
            if (value != null && value.booleanValue()) {
                snapStrategies.add(new SnapToGuides(this));
            }
            value = (Boolean) getViewer().getProperty(
                    SnapToGeometry.PROPERTY_SNAP_ENABLED);
            if (value != null && value.booleanValue()) {
                snapStrategies.add(new SnapToGeometry(this));
            }
            value = (Boolean) getViewer().getProperty(
                    SnapToGrid.PROPERTY_GRID_ENABLED);
            if (value != null && value.booleanValue()) {
                snapStrategies.add(new SnapToGrid(this));
            }
            if (snapStrategies.size() > 0) {
                return new CompoundSnapToHelper(
                        snapStrategies.toArray(new SnapToHelper[snapStrategies
                                .size()]));
            } else if (snapStrategies.size() == 1) {
                return snapStrategies.get(0);
            }
        }
        return super.getAdapter(key);
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractEditPart#getModelChildren()
     */
    @Override
    protected List<Branch> getModelChildren() {
        BranchGraph graph = (BranchGraph) getModel();
        return Arrays.asList(graph.getBranches());
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
     */
    @Override
    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.COMPONENT_ROLE,
                new RootComponentEditPolicy());
        installEditPolicy(EditPolicy.LAYOUT_ROLE, new GraphXYLayoutEditPolicy(
                graph));
    }

    /**
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        String property = evt.getPropertyName();
        if (IBranchGraph.ELEMENT_ADDED.equals(property)
                || IBranchGraph.ELEMENT_REMOVED.equals(property)) {
            refreshChildren();
        }
    }

}
