/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mergequest.builder.xml;

import com.perforce.team.core.mergequest.model.DepotPathMapping;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.core.mergequest.model.IBranchGraphElement;

import org.w3c.dom.Element;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class DepotPathMappingBuilder extends MappingBuilder implements
        IElementBuilder {

    /**
     * DEPOT_PATH_MAPPING_ELEMENT
     */
    public static final String DEPOT_PATH_MAPPING_ELEMENT = "depotPathMapping"; //$NON-NLS-1$

    /**
     * TARGET_PATH_ATTRIBUTE
     */
    public static final String TARGET_PATH_ATTRIBUTE = "targetPath"; //$NON-NLS-1$

    /**
     * SOURCE_PATH_ATTRIBUTE
     */
    public static final String SOURCE_PATH_ATTRIBUTE = "sourcePath"; //$NON-NLS-1$

    /**
     * @see com.perforce.team.core.mergequest.builder.xml.IElementBuilder#initialize(org.w3c.dom.Element,
     *      com.perforce.team.core.mergequest.model.IBranchGraph)
     */
    public IBranchGraphElement initialize(Element element, IBranchGraph graph) {
        DepotPathMapping mapping = null;
        String id = element.getAttribute(IElementConstants.ID_ATTRIBUTE);
        if (id.length() > 0) {
            mapping = graph.createDepotPathMapping(id);
            loadMappingFields(element, mapping);
            mapping.setSourcePath(element.getAttribute(SOURCE_PATH_ATTRIBUTE));
            mapping.setTargetPath(element.getAttribute(TARGET_PATH_ATTRIBUTE));
            graph.add(mapping);
        }
        return mapping;
    }

    /**
     * @see com.perforce.team.core.mergequest.builder.xml.IElementBuilder#save(org.w3c.dom.Element,
     *      com.perforce.team.core.mergequest.model.IBranchGraphElement)
     */
    public void save(Element parent, IBranchGraphElement element) {
        if (element instanceof DepotPathMapping) {
            DepotPathMapping mapping = (DepotPathMapping) element;
            Element mappingElement = parent.getOwnerDocument().createElement(
                    DEPOT_PATH_MAPPING_ELEMENT);
            saveMappingFields(mappingElement, mapping);
            mappingElement.setAttribute(SOURCE_PATH_ATTRIBUTE,
                    mapping.getSourcePath());
            mappingElement.setAttribute(TARGET_PATH_ATTRIBUTE,
                    mapping.getTargetPath());
            parent.appendChild(mappingElement);
        }
    }

}
