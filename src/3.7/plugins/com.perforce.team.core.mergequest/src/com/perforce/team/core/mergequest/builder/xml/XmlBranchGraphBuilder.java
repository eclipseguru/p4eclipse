/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mergequest.builder.xml;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.mergequest.P4BranchGraphCorePlugin;
import com.perforce.team.core.mergequest.builder.BranchGraphBuilder;
import com.perforce.team.core.mergequest.builder.Messages;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.core.mergequest.model.IBranchGraphContainer;
import com.perforce.team.core.mergequest.model.IBranchGraphElement;
import com.perforce.team.core.mergequest.model.factory.IContainerFactory;
import com.perforce.team.core.p4java.P4Workspace;

import java.io.IOException;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class XmlBranchGraphBuilder extends BranchGraphBuilder {

    /**
     * Default extension point id for builder elements to and from xml
     */
    public static final String EXTENSION_POINT_ID = P4BranchGraphCorePlugin.PLUGIN_ID
            + ".builders"; //$NON-NLS-1$

    private static final String GRAPHS_ELEMENT = "graphs"; //$NON-NLS-1$
    private static final String GRAPH_ELEMENT = "graph"; //$NON-NLS-1$

    private DocumentBuilderFactory factory;
    private DocumentBuilder builder;
    private Transformer transformer;
    private BuilderRegistry registry = null;

    /**
     * @param factory
     */
    public XmlBranchGraphBuilder(IContainerFactory factory) {
        super(factory);
    }

    /**
     */
    public XmlBranchGraphBuilder() {
        super();
    }

    private BuilderRegistry getRegistry() {
        if (registry == null) {
            registry = new BuilderRegistry(EXTENSION_POINT_ID);
        }
        return registry;
    }

    /**
     * Log load exception
     * 
     * @param t
     */
    protected void logLoadException(Throwable t) {
        if (t == null) {
            return;
        }
        if (t.getCause() != null) {
            t = t.getCause();
        }
        IOException ioException = new IOException(MessageFormat.format(
                Messages.XmlBranchGraphBuilder_ErrorLoading,
                t.getLocalizedMessage()), t);
        PerforceProviderPlugin.logError(ioException);
    }

    /**
     * Log persist exception
     * 
     * @param t
     */
    protected void logPersistException(Throwable t) {
        if (t == null) {
            return;
        }
        if (t.getCause() != null) {
            t = t.getCause();
        }
        IOException ioException = new IOException(MessageFormat.format(
                Messages.XmlBranchGraphBuilder_ErrorSaving,
                t.getLocalizedMessage()), t);
        PerforceProviderPlugin.logError(ioException);
    }

    private String createString(Document document) throws TransformerException {
        String value = null;
        DOMSource source = new DOMSource(document);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        if (transformer == null) {
            this.transformer = TransformerFactory.newInstance()
                    .newTransformer();
            this.transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
        }
        transformer.transform(source, result);
        value = writer.toString();
        return value;
    }

    private DocumentBuilder getBuilder() {
        if (builder == null) {
            if (factory == null) {
                factory = DocumentBuilderFactory.newInstance();
            }
            try {
                builder = factory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                // Ignore
            }
        }
        return this.builder;
    }

    private Document createNewDocument() {
        Document document = null;
        DocumentBuilder builder = getBuilder();
        if (builder != null) {
            document = builder.newDocument();
        }
        return document;
    }

    private IBranchGraph createGraph(Element graphElement,
            IBranchGraphContainer container) {
        String id = graphElement.getAttribute(IElementConstants.ID_ATTRIBUTE);
        IBranchGraph graph = container.createGraph(id);
        String name = graphElement
                .getAttribute(IElementConstants.NAME_ATTRIBUTE);
        if (name.length() > 0) {
            graph.setName(name);
        }
        return graph;
    }

    /**
     * Format the container to an xml string
     * 
     * @param container
     * @return xml
     * @throws TransformerException
     */
    protected String format(IBranchGraphContainer container)
            throws TransformerException {
        String value = null;
        if (container != null) {
            Document document = createNewDocument();
            if (document != null) {
                Element root = document.createElement(GRAPHS_ELEMENT);
                root.setAttribute(IElementConstants.VERSION, P4Workspace
                        .getWorkspace().getVersion());
                document.appendChild(root);
                for (IBranchGraph graph : container.getGraphs()) {
                    Element graphNode = document.createElement(GRAPH_ELEMENT);
                    fillGraph(graph, graphNode);
                    root.appendChild(graphNode);
                }
                value = createString(document);
            }
        }
        if (value == null) {
            value = ""; //$NON-NLS-1$
        }
        return value;
    }

    private void fillGraph(IBranchGraph graph, Element root) {
        root.setAttribute(IElementConstants.ID_ATTRIBUTE, graph.getId());
        String name = graph.getName();
        if (name != null) {
            root.setAttribute(IElementConstants.NAME_ATTRIBUTE, name);
        }
        IElementBuilder builder = null;
        BuilderRegistry registry = getRegistry();
        for (IBranchGraphElement element : graph.getElements()) {
            builder = registry.getBuilder(element);
            if (builder == null) {
                builder = P4CoreUtils.convert(element, IElementBuilder.class);
            }
            if (builder != null) {
                builder.save(root, element);
            }
        }
    }

    /**
     * Unformat the specified input source
     * 
     * @param source
     * @return branch graph container
     * @throws SAXException
     * @throws IOException
     */
    protected IBranchGraphContainer unformat(InputSource source)
            throws SAXException, IOException {
        IBranchGraphContainer container = this.containerFactory.create();
        if (source != null) {
            DocumentBuilder builder = getBuilder();
            if (builder != null) {
                Document document = builder.parse(source);
                Element containerNode = document.getDocumentElement();
                // Only support single container per file
                if (GRAPHS_ELEMENT.equals(containerNode.getTagName())) {
                    BuilderRegistry registry = getRegistry();
                    NodeList graphs = containerNode
                            .getElementsByTagName(GRAPH_ELEMENT);
                    for (int h = 0; h < graphs.getLength(); h++) {
                        Element graphNode = (Element) graphs.item(h);
                        IBranchGraph graph = createGraph(graphNode, container);
                        if (graph != null && container.add(graph)) {
                            NodeList elements = graphNode.getChildNodes();
                            Map<IBranchGraphElement, IElementBuilder> completions = new HashMap<IBranchGraphElement, IElementBuilder>();
                            for (int e = 0; e < elements.getLength(); e++) {
                                Node node = elements.item(e);
                                if (node.getNodeType() == Node.ELEMENT_NODE) {
                                    Element element = (Element) node;
                                    IElementBuilder elementBuilder = registry
                                            .getBuilder(element.getTagName());
                                    if (elementBuilder != null) {
                                        IBranchGraphElement created = elementBuilder
                                                .initialize(element, graph);
                                        completions
                                                .put(created, elementBuilder);
                                    }
                                }
                            }
                            for (Map.Entry<IBranchGraphElement, IElementBuilder> entry: completions
                                    .entrySet()) {
                            	IElementBuilder eb = entry.getValue();
                            	if(eb!=null)
                            		eb.complete(entry.getKey(),graph);
                            }
                        }
                    }
                }
            }
        }
        return container;
    }
}
