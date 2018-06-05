/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mergequest.builder.xml;

import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.core.mergequest.model.IBranchGraphElement;

import org.w3c.dom.Element;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class ElementBuilder implements IElementBuilder {

    /**
     * Get integer
     * 
     * @param element
     * @param attribute
     * @return int
     */
    protected int getInteger(Element element, String attribute) {
        int intValue = -1;
        String value = element.getAttribute(attribute);
        if (value.length() > 0) {
            try {
                intValue = Integer.parseInt(value);
            } catch (NumberFormatException nfe) {
                intValue = -1;
            }
        }
        return intValue;
    }

    /**
     * @see com.perforce.team.core.mergequest.builder.xml.IElementBuilder#complete(com.perforce.team.core.mergequest.model.IBranchGraphElement,
     *      com.perforce.team.core.mergequest.model.IBranchGraph)
     */
    public void complete(IBranchGraphElement element, IBranchGraph graph) {
        // Do nothing by default, sub-classes should override
    }

}
