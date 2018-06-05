/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.job;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.p4java.IP4Connection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.TransferData;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BulkChangeDropAdapter extends ViewerDropAdapter {

    private IBulkChange change;

    /**
     * @param viewer
     * @param change
     */
    protected BulkChangeDropAdapter(Viewer viewer, IBulkChange change) {
        super(viewer);
        this.change = change;
    }

    /**
     * @see org.eclipse.jface.viewers.ViewerDropAdapter#performDrop(java.lang.Object)
     */
    @Override
    public boolean performDrop(Object data) {
        if (data instanceof IStructuredSelection) {
            List<IJobProxy> proxies = new ArrayList<IJobProxy>();
            IJobProxy proxy = null;
            IP4Connection connection = this.change.getConnection();
            for (Object element : ((IStructuredSelection) data).toArray()) {
                proxy = P4CoreUtils.convert(element, IJobProxy.class);
                if (proxy != null && connection.equals(proxy.getConnection())) {
                    proxies.add(proxy);
                } else {
                    JobProxyContainer container = P4CoreUtils.convert(element,
                            JobProxyContainer.class);
                    if (container != null
                            && connection.equals(container.getConnection())) {
                        proxies.addAll(Arrays.asList(container.getJobs()));
                    }
                }
            }
            if (proxies.size() > 0) {
                change.add(proxies.toArray(new IJobProxy[proxies.size()]));
            }
            return true;
        }
        return false;
    }

    /**
     * @see org.eclipse.jface.viewers.ViewerDropAdapter#validateDrop(java.lang.Object,
     *      int, org.eclipse.swt.dnd.TransferData)
     */
    @Override
    public boolean validateDrop(Object target, int operation,
            TransferData transferType) {
        return LocalSelectionTransfer.getTransfer().isSupportedType(
                transferType)
                && this.change.getConnection() != null;
    }

}
