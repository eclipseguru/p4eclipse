/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.job;

import com.perforce.team.core.mylyn.IP4MylynConstants;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Job;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class JobAdapterFactory implements IAdapterFactory {

    /**
     * Adapter classes
     */
    public static final Class[] CLASSES = new Class[] { IJobProxy.class,
            JobProxyContainer.class };

    private JobProxyContainer getContainer(Object adaptable) {
        JobProxyContainer container = null;
        if (adaptable instanceof IRepositoryQuery) {
            IRepositoryQuery query = (IRepositoryQuery) adaptable;
            if (IP4MylynConstants.KIND.equals(query.getConnectorKind())) {
                container = new JobProxyContainer(query);
            }
        } else if (adaptable instanceof IP4Connection) {
            container = new JobProxyContainer((IP4Connection) adaptable);
        }
        return container;
    }

    private IJobProxy getProxy(Object adaptable) {
        IJobProxy proxy = null;
        if (adaptable instanceof ITask) {
            if (IP4MylynConstants.KIND.equals(((ITask) adaptable)
                    .getConnectorKind())) {
                proxy = new TaskProxy((ITask) adaptable);
            }
        } else if (adaptable instanceof IP4Job) {
            proxy = new JobProxy((IP4Job) adaptable);
        }
        return proxy;
    }

    /**
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object,
     *      java.lang.Class)
     */
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (JobProxyContainer.class == adapterType) {
            return getContainer(adaptableObject);
        }
        if (IJobProxy.class == adapterType) {
            return getProxy(adaptableObject);
        }
        return null;
    }

    /**
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
     */
    public Class[] getAdapterList() {
        return CLASSES;
    }

}
