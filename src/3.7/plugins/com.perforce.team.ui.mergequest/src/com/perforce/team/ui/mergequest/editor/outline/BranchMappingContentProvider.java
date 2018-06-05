/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.editor.outline;

import com.perforce.p4java.core.IBranchMapping;
import com.perforce.p4java.core.IMapEntry.EntryType;
import com.perforce.p4java.core.ViewMap;
import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.BranchSpecMapping;
import com.perforce.team.core.mergequest.model.Mapping;
import com.perforce.team.core.p4java.IP4Branch;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceContentProvider;
import com.perforce.team.ui.PerforceUIPlugin;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.model.WorkbenchAdapter;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchMappingContentProvider extends PerforceContentProvider {

    /**
     * View line
     */
    public static class ViewLine extends WorkbenchAdapter {

        private IBranchMapping line;

        /**
         * 
         * @param line
         */
        public ViewLine(IBranchMapping line) {
            this.line = line;
        }

        /**
         * Get line
         * 
         * @return - branch mapping line
         */
        public IBranchMapping getLine() {
            return this.line;
        }

        /**
         * @see org.eclipse.ui.model.WorkbenchAdapter#getImageDescriptor(java.lang.Object)
         */
        @Override
        public ImageDescriptor getImageDescriptor(Object object) {
            EntryType type = this.line.getType();
            if (type != null) {
                switch (type) {
                case EXCLUDE:
                    return PerforceUIPlugin
                            .getDescriptor(IPerforceUIConstants.IMG_VIEW_LINE_EXCLUDE);
                case OVERLAY:
                    return PerforceUIPlugin
                            .getDescriptor(IPerforceUIConstants.IMG_VIEW_LINE_OVERLAY);
				default:
					break;
                }
            }
            return PerforceUIPlugin
                    .getDescriptor(IPerforceUIConstants.IMG_VIEW_LINE);
        }

        /**
         * @see org.eclipse.ui.model.WorkbenchAdapter#getLabel(java.lang.Object)
         */
        @Override
        public String getLabel(Object object) {
            return this.line.toString("   ", true); //$NON-NLS-1$
        }

    }

    private ISchedulingRule viewRule = P4Runner.createRule();
    private IP4Connection connection;

    private Map<Mapping, ViewLine[]> mappings = new HashMap<Mapping, ViewLine[]>();

    /**
     * @param connection
     * @param viewer
     */
    public BranchMappingContentProvider(IP4Connection connection,
            StructuredViewer viewer) {
        super(viewer, true);
        this.connection = connection;
    }

    private void loadBranch(final MappingProxy proxy) {
        if (proxy.getMapping() instanceof BranchSpecMapping) {
            final BranchSpecMapping mapping = (BranchSpecMapping) proxy
                    .getMapping();
            final IP4Branch branch = mapping.generateBranch(connection);
            P4Runner.schedule(new P4Runnable() {

                @Override
                public String getTitle() {
                    return MessageFormat.format(
                            Messages.BranchMappingContentProvider_LoadingSpec,
                            mapping.getName());
                }

                @Override
                public void run(IProgressMonitor monitor) {
                    branch.refresh();
                    ViewMap<IBranchMapping> entries = branch.getView();
                    List<ViewLine> view = new ArrayList<ViewLine>();
                    if (entries != null) {
                        for (IBranchMapping entry : entries) {
                            view.add(new ViewLine(entry));
                        }
                    }
                    mappings.put(mapping,
                            view.toArray(new ViewLine[view.size()]));
                    PerforceUIPlugin.syncExec(new Runnable() {

                        public void run() {
                            if (P4UIUtils.okToUse(viewer)) {
                                viewer.refresh(proxy);
                            }
                        }
                    });
                }
            }, viewRule);
        }
    }

    private Object[] getProxyChildren(MappingProxy proxy) {
        if (proxy.getMapping() instanceof BranchSpecMapping) {
            if (!mappings.containsKey(proxy.getMapping())) {
                loadBranch(proxy);
                return new Object[] { new Loading() };
            } else {
                return mappings.get(proxy.getMapping());
            }
        } else {
            return proxy.getChildren(proxy);
        }
    }

    /**
     * @see com.perforce.team.ui.PerforceContentProvider#getChildren(java.lang.Object)
     */
    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof MappingProxy) {
            return getProxyChildren((MappingProxy) parentElement);
        }
        if (parentElement instanceof Branch) {
            Object[] children = super.getChildren(parentElement);
            Branch branch = (Branch) parentElement;
            Object[] converted = new Object[children.length];
            for (int i = 0; i < children.length; i++) {
                if (children[i] instanceof Mapping) {
                    converted[i] = new MappingProxy(branch,
                            (Mapping) children[i]);
                } else {
                    converted[i] = children[i];
                }
            }
            return converted;
        }
        return super.getChildren(parentElement);
    }

    /**
     * @see com.perforce.team.ui.PerforceContentProvider#hasChildren(java.lang.Object)
     */
    @Override
    public boolean hasChildren(Object element) {
        if (element instanceof MappingProxy
                && ((MappingProxy) element).getMapping() instanceof BranchSpecMapping) {
            return true;
        }
        return super.hasChildren(element);
    }

    /**
     * @see com.perforce.team.ui.PerforceContentProvider#dispose()
     */
    @Override
    public void dispose() {
        mappings.clear();
        super.dispose();
    }

}
