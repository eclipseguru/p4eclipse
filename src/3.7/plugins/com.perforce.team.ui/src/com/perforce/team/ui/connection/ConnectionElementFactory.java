/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.connection;

import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.P4ConnectionManager;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class ConnectionElementFactory implements IElementFactory {

    /**
     * CONNECTION
     */
    public static final String CONNECTION = "connection"; //$NON-NLS-1$

    /**
     * Create element from non-null memento and non-null connection
     * 
     * @param connection
     * @param memento
     * @return adaptable
     */
    protected abstract IAdaptable createElement(IP4Connection connection,
            IMemento memento);

    /**
     * @see org.eclipse.ui.IElementFactory#createElement(org.eclipse.ui.IMemento)
     */
    public IAdaptable createElement(IMemento memento) {
        IAdaptable element = null;
        if (memento != null) {
            String connection = memento.getString(CONNECTION);
            if (connection != null) {
                ConnectionParameters params = new ConnectionParameters(
                        connection);
                if (P4ConnectionManager.getManager().containsConnection(params)) {
                    IP4Connection inputConnection = P4ConnectionManager
                            .getManager().getConnection(params);
                    if (inputConnection != null) {
                        element = createElement(inputConnection, memento);
                    }
                }
            }
        }
        return element;
    }
}
