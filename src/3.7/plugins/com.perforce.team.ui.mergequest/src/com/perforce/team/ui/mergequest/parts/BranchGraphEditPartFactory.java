/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.parts;

import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.BranchGraph;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.core.mergequest.model.Mapping;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchGraphEditPartFactory implements EditPartFactory {

    private IBranchGraph graph;
    private SharedResources images;

    /**
     * Factory
     * 
     * @param graph
     * @param images
     */
    public BranchGraphEditPartFactory(IBranchGraph graph, SharedResources images) {
        this.graph = graph;
        this.images = images;
    }

    /**
     * @see org.eclipse.gef.EditPartFactory#createEditPart(org.eclipse.gef.EditPart,
     *      java.lang.Object)
     */
    public EditPart createEditPart(EditPart context, Object model) {
        EditPart part = null;
        if (model instanceof Branch) {
            part = new BranchEditPart(images);
        } else if (model instanceof Mapping) {
            part = new MappingEditPart(images);
        } else if (model instanceof BranchGraph) {
            part = new GraphEditPart(this.graph);
        }
        if (part != null) {
            part.setModel(model);
        }
        return part;
    }
}
