/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.diff;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.Policy;
import com.perforce.team.core.Tracing;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceContentProvider;
import com.perforce.team.ui.PerforceUIPlugin;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 *
 */
public abstract class DiffContentProvider extends PerforceContentProvider {

    private Map<String, IFileDiffer> typeDiffers = new HashMap<String, IFileDiffer>();
    private Map<String, IFileDiffer> extDiffers = new HashMap<String, IFileDiffer>();

    private ISchedulingRule diffRule = P4Runner.createRule();

    /**
     * Creates a new content provider for a structured viewer
     *
     * @param viewer
     */
    public DiffContentProvider(StructuredViewer viewer) {
        super(viewer);
    }

    /**
     * Creates a new content provider for a structured view
     *
     * @param viewer
     * @param async
     */
    public DiffContentProvider(StructuredViewer viewer, boolean async) {
        super(viewer, async);
    }

    /**
     * Creates a new content provider for a structured view
     *
     * @param viewer
     * @param context
     */
    public DiffContentProvider(StructuredViewer viewer, Object context) {
        super(viewer, context);
    }

    /**
     * Creates a new content provider for a structured view
     *
     * @param viewer
     * @param async
     * @param context
     */
    public DiffContentProvider(StructuredViewer viewer, boolean async,
            Object context) {
        super(viewer, async, context);
    }

    /**
     * Can this content provider generate child nodes for the specified file
     *
     * @param file
     * @return - true if can diff
     */
    public abstract boolean canDiff(IP4Resource file);

    /**
     * Get left diff side storage
     *
     * @param resource
     * @param file
     * @return - storage
     */
    public abstract IStorage getLeftStorage(IP4Resource resource, IP4File file);

    /**
     * Get right diff side storage
     *
     * @param resource
     * @param file
     * @return - storage
     */
    public abstract IStorage getRightStorage(IP4Resource resource, IP4File file);

    /**
     * Generate the diff using the differ specified
     *
     * @param differ
     * @param resource
     * @param file
     */
    protected void generateDiff(final IFileDiffer differ,
            final IP4Resource resource, final IP4File file) {
        P4Runner.schedule(new P4Runnable() {

            @Override
            public String getTitle() {
                return Messages.DiffContentProvider_GettingFileDifferences;
            }

            @Override
            public void run(IProgressMonitor monitor) {
                IStorage storage1 = getLeftStorage(resource, file);
                if (monitor.isCanceled()) {
                    return;
                }
                IStorage storage2 = getRightStorage(resource, file);
                if (monitor.isCanceled()) {
                    return;
                }
                differ.generateDiff(resource, file, storage1, storage2);
                if (monitor.isCanceled()) {
                    return;
                }
                PerforceUIPlugin.syncExec(new Runnable() {

                    public void run() {
                        if (P4UIUtils.okToUse(viewer)) {
                            updateResource(resource);
                        }
                    }
                });
            }
        }, diffRule);
    }

    /**
     * Update a resource after the diff elements load for it
     *
     * @param resource
     */
    protected void updateResource(IP4Resource resource) {
        viewer.refresh(resource);
        if (viewer instanceof TreeViewer) {
            ((TreeViewer) viewer)
                    .expandToLevel(resource, TreeViewer.ALL_LEVELS);
        }
    }

    /**
     * Get differ for specified resource
     *
     * @param resource
     * @return - file differ or null if none found
     */
    public IFileDiffer getDiffer(IP4Resource resource) {
        IFileDiffer differ = null;
        if (resource != null) {
            String name = resource.getName();
            if (name != null) {
                IContentType type = Platform.getContentTypeManager()
                        .findContentTypeFor(name);
                if (type != null) {
                    if (this.typeDiffers.containsKey(type.getId())) {
                        differ = this.typeDiffers.get(type.getId());
                    } else {
                        differ = DiffRegistry.getRegistry().getDiffer(type,
                                true);
                        this.typeDiffers.put(type.getId(), differ);
                    }
                } else {
                    String ext = new Path(name).getFileExtension();
                    if (ext != null && ext.length() > 0) {
                        if (extDiffers.containsKey(ext)) {
                            differ = extDiffers.get(ext);
                        } else {
                            differ = DiffRegistry.getRegistry()
                                    .getDifferByExtension(ext);
                            this.extDiffers.put(ext, differ);
                        }
                    }
                }
            }
        }
        return differ;
    }

    /**
     * Get diffs from differ. Sub-classes may override.
     *
     * @param differ
     * @param resource
     * @return non-null but possibly empty array
     */
    protected Object[] getDiffs(IFileDiffer differ, IP4Resource resource) {
        return differ.getDiff(resource);
    }

    /**
     * @see com.perforce.team.ui.PerforceContentProvider#getChildren(java.lang.Object)
     */
    @Override
    public Object[] getChildren(Object parentElement) {
    	Tracing.printTrace(Policy.DEBUG,"SUBMIT", getClass().getSimpleName()+":getChildren() parentElement={0}", parentElement);//$NON-NLS-1$,$NON-NLS-2$
        if (parentElement instanceof IP4Resource) {
            IP4Resource resource = (IP4Resource) parentElement;
            if (resource.isFile() && canDiff(resource)) {
                IP4File file = P4CoreUtils.convert(resource, IP4File.class);
                if (file != null) {
                    IFileDiffer differ = getDiffer(resource);
                    if (differ != null) {
                        if (differ.diffGenerated(resource)) {
                            return getDiffs(differ, resource);
                        } else {
                            generateDiff(differ, resource, file);
                            return new Object[] { new Loading() };
                        }
                    }
                }
            }
        } else if (parentElement instanceof IDiffContainer) {
            return ((IDiffContainer) parentElement).getChildren();
        }
        return super.getChildren(parentElement);
    }

    /**
     * @see com.perforce.team.ui.PerforceContentProvider#getParent(java.lang.Object)
     */
    @Override
    public Object getParent(Object element) {
        if (element instanceof IDiffElement) {
            return ((IDiffElement) element).getParent();
        }
        return super.getParent(element);
    }

    /**
     * @see com.perforce.team.ui.PerforceContentProvider#hasChildren(java.lang.Object)
     */
    @Override
    public boolean hasChildren(Object element) {
        if (element instanceof IP4Resource) {
            IP4Resource resource = (IP4Resource) element;
            if (resource.isFile() && canDiff(resource)) {
                return getDiffer(resource) != null;
            }
        } else if (element instanceof IDiffContainer) {
            return ((IDiffContainer) element).hasChildren();
        }
        return super.hasChildren(element);
    }

    /**
     * Dispose all current differs and clear references to differs
     */
    protected void clearDiffers() {
        for (IFileDiffer differ : typeDiffers.values()) {
            if (differ != null) {
                differ.dispose();
            }
        }
        typeDiffers.clear();
        for (IFileDiffer differ : extDiffers.values()) {
            if (differ != null) {
                differ.dispose();
            }
        }
        extDiffers.clear();
    }

    /**
     * @see com.perforce.team.ui.PerforceContentProvider#dispose()
     */
    @Override
    public void dispose() {
        clearDiffers();
        super.dispose();
    }

}
