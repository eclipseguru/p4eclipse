/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mergequest.builder.xml;

import com.perforce.team.core.mergequest.model.BranchSpecMapping;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.core.mergequest.model.IBranchGraphElement;

import org.w3c.dom.Element;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchSpecMappingBuilder extends MappingBuilder implements
        IElementBuilder {

    /**
     * BRANCH_SPEC_MAPPING_ELEMENT
     */
    public static final String BRANCH_SPEC_MAPPING_ELEMENT = "branchSpecMapping"; //$NON-NLS-1$

    /**
     * @see com.perforce.team.core.mergequest.builder.xml.IElementBuilder#initialize(org.w3c.dom.Element,
     *      com.perforce.team.core.mergequest.model.IBranchGraph)
     */
    public IBranchGraphElement initialize(Element element, IBranchGraph graph) {
        BranchSpecMapping mapping = null;
        String id = element.getAttribute(IElementConstants.ID_ATTRIBUTE);
        if (id.length() > 0) {
            mapping = graph.createBranchSpecMapping(id);
            loadMappingFields(element, mapping);
            graph.add(mapping);
        }
        return mapping;
    }

    /**
     * @see com.perforce.team.core.mergequest.builder.xml.IElementBuilder#save(org.w3c.dom.Element,
     *      com.perforce.team.core.mergequest.model.IBranchGraphElement)
     */
    public void save(Element parent, IBranchGraphElement element) {
        if (element instanceof BranchSpecMapping) {
            BranchSpecMapping mapping = (BranchSpecMapping) element;
            Element mappingElement = parent.getOwnerDocument().createElement(
                    BRANCH_SPEC_MAPPING_ELEMENT);
            saveMappingFields(mappingElement, mapping);
            parent.appendChild(mappingElement);
        }
    }
}
