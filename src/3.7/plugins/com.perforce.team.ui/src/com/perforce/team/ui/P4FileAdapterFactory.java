/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui;

import com.perforce.p4java.core.file.FileAction;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.ui.history.P4HistoryPageSource;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.ui.history.IHistoryPageSource;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4FileAdapterFactory implements IAdapterFactory {

    private IWorkbenchAdapter wbAdapter = new IWorkbenchAdapter() {

        public Object getParent(Object o) {
            return null;
        }

        public String getLabel(Object o) {
            String label = null;
            if (o instanceof IP4File) {
                label = ((IP4File) o).getRemotePath();
                if (label == null) {
                    label = ((IP4File) o).getName();
                }
                if (label != null) {
                    String type = ((IP4File) o).getOpenedType();
                    if (type != null) {
                        label += " <" + type + ">"; //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    FileAction action = ((IP4File) o).getAction();
                    if (action != null) {
                        label += " <" + action.toString().toLowerCase() + ">"; //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
            }
            if (label == null) {
                label = ""; //$NON-NLS-1$
            }
            return label;
        }

        public ImageDescriptor getImageDescriptor(Object object) {
            return null;
        }

        public Object[] getChildren(Object o) {
            return new Object[0];
        }

    };

    /**
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object,
     *      java.lang.Class)
     */
    public Object getAdapter(Object adaptableObject, @SuppressWarnings("rawtypes") Class adapterType) {
        if (adaptableObject instanceof IP4File) {
            if (adapterType == IWorkbenchAdapter.class) {
                return wbAdapter;
            } else if (adapterType == IPropertySource.class) {
                return new P4FilePropertySource((IP4File) adaptableObject);
            }
        } else if (adaptableObject instanceof RepositoryProvider) {
            if (adapterType == IHistoryPageSource.class) {
                return new P4HistoryPageSource();
            }
        }
        return null;
    }

    /**
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
     */
    @SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
        return new Class[] { IWorkbenchAdapter.class, IPropertySource.class,
                RepositoryProvider.class, };
    }

}
