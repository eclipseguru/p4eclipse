/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.policies;

import com.perforce.team.core.mergequest.model.Mapping;
import com.perforce.team.ui.mergequest.figures.MappingFigure;
import com.perforce.team.ui.mergequest.parts.MappingEditPart;
import com.perforce.team.ui.mergequest.parts.MappingEndpointHandle;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionLocator;
import org.eclipse.gef.Handle;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class MappingSelectionEditPolicy extends ConnectionEndpointEditPolicy {

    /**
     * Get edit part
     * 
     * @return connection edit part
     */
    protected MappingEditPart getEditPart() {
        return (MappingEditPart) getHost();
    }

    /**
     * Get mapping figure
     * 
     * @return mapping figure
     */
    protected MappingFigure getMappingFigure() {
        return (MappingFigure) getEditPart().getFigure();
    }

    /**
     * @see org.eclipse.gef.editpolicies.SelectionHandlesEditPolicy#addSelectionHandles()
     */
    @Override
    protected void addSelectionHandles() {
        super.addSelectionHandles();
        getMappingFigure().select();
    }

    /**
     * @see org.eclipse.gef.editpolicies.SelectionHandlesEditPolicy#removeSelectionHandles()
     */
    @Override
    protected void removeSelectionHandles() {
        super.removeSelectionHandles();
        getMappingFigure().deselect();
    }

    /**
     * 
     * @see org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy#createSelectionHandles()
     */
    @Override
    protected List<?> createSelectionHandles() {
        List<Handle> handles = new ArrayList<Handle>();
        MappingEditPart owner = getEditPart();
        Mapping mapping = owner.getMapping();
        MappingEndpointHandle source = new MappingEndpointHandle(owner,
                ConnectionLocator.SOURCE, mapping.getSourceAnchor());
        source.setForegroundColor(ColorConstants.gray);
        handles.add(source);
        MappingEndpointHandle target = new MappingEndpointHandle(owner,
                ConnectionLocator.TARGET, mapping.getTargetAnchor());
        target.setForegroundColor(ColorConstants.gray);
        handles.add(target);
        return handles;
    }
}
