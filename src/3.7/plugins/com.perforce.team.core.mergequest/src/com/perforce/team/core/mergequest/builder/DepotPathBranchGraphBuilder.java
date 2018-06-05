/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mergequest.builder;

import com.perforce.team.core.mergequest.builder.xml.XmlBranchGraphBuilder;
import com.perforce.team.core.mergequest.model.IBranchGraphContainer;
import com.perforce.team.core.mergequest.model.factory.IContainerFactory;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.Assert;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class DepotPathBranchGraphBuilder extends XmlBranchGraphBuilder {

    private IP4Connection connection;
    private String path;

    /**
     * Create depot path based branch graph builder
     * 
     * 
     * @param connection
     * @param path
     */
    public DepotPathBranchGraphBuilder(IP4Connection connection, String path) {
        super();
        Assert.isNotNull(connection, "Connection cannot be null"); //$NON-NLS-1$
        Assert.isNotNull(path, "Path cannot be null"); //$NON-NLS-1$
        this.connection = connection;
        this.path = path;
    }

    /**
     * Create depot path based branch graph builder
     * 
     * @param factory
     * @param connection
     * @param path
     */
    public DepotPathBranchGraphBuilder(IContainerFactory factory,
            IP4Connection connection, String path) {
        super(factory);
        Assert.isNotNull(connection, "Connection cannot be null"); //$NON-NLS-1$
        Assert.isNotNull(path, "Path cannot be null"); //$NON-NLS-1$
        this.connection = connection;
        this.path = path;
    }

    /**
     * @see com.perforce.team.core.mergequest.builder.IBranchGraphBuilder#load()
     */
    public IBranchGraphContainer load() throws IOException {
        IBranchGraphContainer container = null;
        InputStream stream = null;
        IP4File file = this.connection.getFile(this.path);
        if (file != null) {
            stream = file.getRemoteContents();
        }
        IOException ioException = null;
        try {
            if (stream != null) {
                container = unformat(new InputSource(stream));
            }
            if (container == null) {
                container = unformat(null);
            }
        } catch (SAXException e) {
            ioException = new IOException(e);
            throw ioException;
        } catch (IOException e) {
            ioException = e;
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
