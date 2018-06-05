/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mergequest.builder;

import com.perforce.team.core.mergequest.builder.xml.XmlBranchGraphBuilder;
import com.perforce.team.core.mergequest.model.IBranchGraphContainer;
import com.perforce.team.core.mergequest.model.factory.IContainerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.eclipse.core.runtime.Assert;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class HttpBranchGraphBuilder extends XmlBranchGraphBuilder {

    private String url;

    /**
     * Create HTTP URL based branch graph builder
     * 
     * @param factory
     * @param url
     */
    public HttpBranchGraphBuilder(IContainerFactory factory, String url) {
        super(factory);
        Assert.isNotNull(url, "Url cannot be null"); //$NON-NLS-1$
        this.url = url;
    }

    /**
     * Create HTTP URL based branch graph builder
     * 
     * @param url
     */
    public HttpBranchGraphBuilder(String url) {
        Assert.isNotNull(url, "Url cannot be null"); //$NON-NLS-1$
        this.url = url;
    }

    /**
     * @see com.perforce.team.core.mergequest.builder.IBranchGraphBuilder#load()
     */
    public IBranchGraphContainer load() throws IOException {
        IBranchGraphContainer container = null;
        InputStream stream = null;
        IOException ioException = null;
        try {
            URL locator = new URL(url);
            URLConnection connection = locator.openConnection();
            stream = connection.getInputStream();
            if (stream != null) {
                container = unformat(new InputSource(stream));
            }
            if (container == null) {
                container = unformat(null);
            }
        } catch (IOException e) {
            ioException = e;
            throw ioException;
        } catch (SAXException e) {
            ioException = new IOException(e);
            throw ioException;
        } finally {
            logLoadException(ioException);
        }
        return container;
    }

    /**
     * @see com.perforce.team.core.mergequest.builder.IBranchGraphBuilder#persist(com.perforce.team.core.mergequest.model.IBranchGraphContainer)
     */
    public void persist(IBranchGraphContainer container) {
        // Persist not currently supported
    }

}
