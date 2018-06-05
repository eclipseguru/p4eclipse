/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.branchgraph.builder;

import com.perforce.team.core.mergequest.builder.xml.IElementBuilder;
import com.perforce.team.core.mergequest.model.IBranchGraphElement;
import com.perforce.team.tests.P4TestCase;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class BaseElementBuilderTest extends P4TestCase {

    /**
     * Get builder
     * 
     * @return document builder
     * @throws ParserConfigurationException
     */
    protected DocumentBuilder getBuilder() throws ParserConfigurationException {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }

    /**
     * Create document
     * 
     * @return new document
     * @throws ParserConfigurationException
     */
    protected Document createNewDocument() throws ParserConfigurationException {
        return getBuilder().newDocument();
    }

    /**
     * Save the element to a node
     * 
     * @param builder
     * @param graphElement
     * @return element
     * @throws ParserConfigurationException
     */
    protected Element save(IElementBuilder builder,
            IBranchGraphElement graphElement)
            throws ParserConfigurationException {
        Document doc = createNewDocument();
        assertNotNull(doc);
        Element element = doc.createElement("test");
        assertNotNull(element);
        builder.save(element, graphElement);
        return (Element) element.getFirstChild();
    }

}
