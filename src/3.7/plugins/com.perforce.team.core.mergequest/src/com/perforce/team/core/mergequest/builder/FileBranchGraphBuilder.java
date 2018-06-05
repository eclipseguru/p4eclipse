/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mergequest.builder;

import com.perforce.p4java.CharsetDefs;
import com.perforce.team.core.mergequest.builder.xml.XmlBranchGraphBuilder;
import com.perforce.team.core.mergequest.model.IBranchGraphContainer;
import com.perforce.team.core.mergequest.model.factory.IContainerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.xml.transform.TransformerException;

import org.eclipse.core.runtime.Assert;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class FileBranchGraphBuilder extends XmlBranchGraphBuilder {

    private String path;

    /**
     * Create a file-based branch graph builder
     * 
     * @param factory
     * @param path
     */
    public FileBranchGraphBuilder(IContainerFactory factory, String path) {
        super(factory);
        Assert.isNotNull(path, "Path cannot be null"); //$NON-NLS-1$
        this.path = path;
    }

    /**
     * Create a file-based branch graph builder
     * 
     * @param path
     */
    public FileBranchGraphBuilder(String path) {
        Assert.isNotNull(path, "Path cannot be null"); //$NON-NLS-1$
        this.path = path;
    }

    /**
     * @see com.perforce.team.core.mergequest.builder.IBranchGraphBuilder#load()
     */
    public IBranchGraphContainer load() throws IOException {
        IOException ioException = null;
        try {
            InputSource source = new InputSource(new FileInputStream(path));
            return unformat(source);
        } catch (IOException e) {
            ioException = e;
            throw ioException;
        } catch (SAXException e) {
            ioException = new IOException(e);
            throw ioException;
        } finally {
            logLoadException(ioException);
        }
    }

    /**
     * @see com.perforce.team.core.mergequest.builder.IBranchGraphBuilder#persist(com.perforce.team.core.mergequest.model.IBranchGraphContainer)
     */
    public void persist(IBranchGraphContainer container) throws IOException {
        if (container != null) {
            PrintWriter writer = null;
            IOException ioException = null;
            try {
                writer = new PrintWriter(path,CharsetDefs.DEFAULT_NAME);
                writer.println(format(container));
            } catch (IOException e) {
                ioException = e;
                throw ioException;
            } catch (TransformerException e) {
                ioException = new IOException(e);
                throw ioException;
            } finally {
                logPersistException(ioException);
                if (writer != null) {
                    writer.close();
                }
            }
        }
    }

}
