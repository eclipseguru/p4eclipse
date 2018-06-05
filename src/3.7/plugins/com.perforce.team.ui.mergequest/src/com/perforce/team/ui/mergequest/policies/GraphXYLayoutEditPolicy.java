/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.policies;

import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.core.mergequest.model.registry.BranchType;
import com.perforce.team.ui.mergequest.commands.BranchConstraintCommand;
import com.perforce.team.ui.mergequest.commands.BranchCreateCommand;
import com.perforce.team.ui.mergequest.figures.ColoredResizeHandle;
import com.perforce.team.ui.mergequest.parts.BranchEditPart;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Handle;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ResizableEditPolicy;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.handles.NonResizableHandleKit;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class GraphXYLayoutEditPolicy extends XYLayoutEditPolicy {

    private IBranchGraph graph;

    /**
     * Layout policy
     * 
     * @param graph
     */
    public GraphXYLayoutEditPolicy(IBranchGraph graph) {
        this.graph = graph;
    }

    /**
     * @see org.eclipse.gef.editpolicies.ConstrainedLayoutEditPolicy#createChangeConstraintCommand(org.eclipse.gef.requests.ChangeBoundsRequest,
     *      org.eclipse.gef.EditPart, java.lang.Object)
     */
    @Override
    protected Command createChangeConstraintCommand(
            ChangeBoundsRequest request, EditPart child, Object constraint) {
        if (child instanceof BranchEditPart && constraint instanceof Rectangle) {
            return new BranchConstraintCommand((Branch) child.getModel(),
                    request, (Rectangle) constraint);
        }
        return super.createChangeConstraintCommand(request, child, constraint);
    }

    /**
     * @see org.eclipse.gef.editpolicies.ConstrainedLayoutEditPolicy#createChildEditPolicy(org.eclipse.gef.EditPart)
     */
    @Override
    protected EditPolicy createChildEditPolicy(EditPart child) {
        EditPolicy policy = null;
        if (child instanceof BranchEditPart) {
            policy = new ResizableEditPolicy() {

                @Override
                protected List<Handle> createSelectionHandles() {
                    List<Handle> handles = new ArrayList<Handle>();
                    NonResizableHandleKit.addMoveHandle(
                            (GraphicalEditPart) getHost(), handles);
                    ColoredResizeHandle.addCornerHandles(
                            (GraphicalEditPart) getHost(), handles);
                    for (Object handle : handles) {
                        ((IFigure) handle)
                                .setForegroundColor(ColorConstants.gray);
                    }
                    return handles;
                }

            };
        }
        return policy;
    }

    /**
     * @see org.eclipse.gef.editpolicies.ConstrainedLayoutEditPolicy#createChangeConstraintCommand(org.eclipse.gef.EditPart,
     *      java.lang.Object)
     */
    @Override
    protected Command createChangeConstraintCommand(EditPart child,
            Object constraint) {
        return null;
    }

    /**
     * @see org.eclipse.gef.editpolicies.LayoutEditPolicy#getCreateCommand(org.eclipse.gef.requests.CreateRequest)
     */
    @Override
    protected Command getCreateCommand(CreateRequest request) {
        Command command = null;
        Object newObject = request.getNewObjectType();
        if (newObject instanceof BranchType) {
            Rectangle location = (Rectangle) getConstraintFor(request);
            command = new BranchCreateCommand((BranchType) newObject,
                    this.graph, location);
        }
        return command;
    }

}
