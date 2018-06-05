/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mergequest.builder.xml;

import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.core.mergequest.model.IBranchGraphElement;

import org.w3c.dom.Element;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchBuilder extends ElementBuilder {

    /**
     * BRANCH_ELEMENT
     */
    public static final String BRANCH_ELEMENT = "branch"; //$NON-NLS-1$

    /**
     * @see com.perforce.team.core.mergequest.builder.xml.IElementBuilder#initialize(org.w3c.dom.Element,
     *      com.perforce.team.core.mergequest.model.IBranchGraph)
     */
    public IBranchGraphElement initialize(Element element, IBranchGraph graph) {
        Branch branch = null;
        String id = element.getAttribute(IElementConstants.ID_ATTRIBUTE);
        if (id.length() > 0) {
            String branchName = element
                    .getAttribute(IElementConstants.NAME_ATTRIBUTE);
            String type = element
                    .getAttribute(IElementConstants.TYPE_ATTRIBUTE);
            branch = graph.createBranch(id);
            branch.setType(type);
            branch.setName(branchName);
            int x = getInteger(element, IElementConstants.X_ATTRIBUTE);
            int y = getInteger(element, IElementConstants.Y_ATTRIBUTE);
            int width = getInteger(element, IElementConstants.WIDTH_ATTRIBUTE);
            int height = getInteger(element, IElementConstants.HEIGHT_ATTRIBUTE);
            branch.setLocation(x, y);
            branch.setSize(width, height);
            graph.add(branch);
        }
        return branch;
    }

    /**
     * @see com.perforce.team.core.mergequest.builder.xml.IElementBuilder#save(org.w3c.dom.Element,
     *      com.perforce.team.core.mergequest.model.IBranchGraphElement)
     */
    public void save(Element parent, IBranchGraphElement element) {
        if (element instanceof Branch) {
            Branch branch = (Branch) element;
            Element branchElement = parent.getOwnerDocument().createElement(
                    BRANCH_ELEMENT);
            branchElement.setAttribute(IElementConstants.ID_ATTRIBUTE,
                    branch.getId());
            branchElement.setAttribute(IElementConstants.NAME_ATTRIBUTE,
                    branch.getName());
            branchElement.setAttribute(IElementConstants.TYPE_ATTRIBUTE,
                    branch.getType());
            branchElement.setAttribute(IElementConstants.X_ATTRIBUTE,
                    Integer.toString(branch.getX()));
            branchElement.setAttribute(IElementConstants.Y_ATTRIBUTE,
                    Integer.toString(branch.getY()));
            branchElement.setAttribute(IElementConstants.WIDTH_ATTRIBUTE,
                    Integer.toString(branch.getWidth()));
            branchElement.setAttribute(IElementConstants.HEIGHT_ATTRIBUTE,
                    Integer.toString(branch.getHeight()));
            parent.appendChild(branchElement);
        }
    }

}
