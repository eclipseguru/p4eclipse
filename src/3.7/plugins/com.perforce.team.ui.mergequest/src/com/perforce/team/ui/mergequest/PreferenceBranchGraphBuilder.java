/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest;

import com.perforce.team.core.mergequest.builder.xml.XmlBranchGraphBuilder;
import com.perforce.team.core.mergequest.model.IBranchGraphContainer;
import com.perforce.team.core.p4java.IP4Connection;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.transform.TransformerException;

import org.eclipse.jface.preference.IPreferenceStore;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class PreferenceBranchGraphBuilder extends XmlBranchGraphBuilder {

    /**
     * BASE_PREF
     */
    public static final String BASE_PREF = "GRAPH_SETTINGS."; //$NON-NLS-1$

    private IPreferenceStore store;
    private String prefName;

    /**
     * Create a new preference based graph builder
     * 
     * @param store
     * @param connection
     */
    public PreferenceBranchGraphBuilder(IPreferenceStore store,
            IP4Connection connection) {
        this.store = store;
        this.prefName = BASE_PREF + connection.getParameters().getPort();
    }

    /**
     * @see com.perforce.team.core.mergequest.builder.IBranchGraphBuilder#load()
     */
    public IBranchGraphContainer load() throws IOException {
        IBranchGraphContainer container = null;
        String prefValue = store.getString(this.prefName);
        IOException ioException = null;
        try {
            if (prefValue.length() > 0) {
                InputSource source = new InputSource(
                        new StringReader(prefValue));
                container = unformat(source);
            } else {
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
    public void persist(IBranchGraphContainer container) throws IOException {
        if (container != null) {
            IOException ioException = null;
            try {
                String value = format(container);
                store.setValue(this.prefName, value);
            } catch (TransformerException t) {
                ioException = new IOException(t);
                throw ioException;
            } finally {
                logPersistException(ioException);
            }
        }
    }

}
