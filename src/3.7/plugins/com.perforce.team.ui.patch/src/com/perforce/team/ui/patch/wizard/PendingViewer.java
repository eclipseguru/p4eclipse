/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.patch.wizard;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.PerforceTeamProvider;
import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Folder;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceContentProvider;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.changelists.ChangelistDecorator;
import com.perforce.team.ui.changelists.ChangelistLabelProvider;
import com.perforce.team.ui.changelists.DecoratedChangelistLabelProvider;
import com.perforce.team.ui.changelists.StyledChangelistLabelProvider;
import com.perforce.team.ui.pending.PendingSorter;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class PendingViewer implements ICheckStateListener {

    private P4Collection collection;
    private CheckboxTreeViewer viewer;

    /**
     * Create pending viewer
     * 
     * @param collection
     */
    public PendingViewer(P4Collection collection) {
        this.collection = collection;
    }

    private IP4Connection[] getConnections() {
        return new P4Event(EventType.REFRESHED, collection)
                .getCommonConnections();
    }

    /**
     * Create control
     * 
     * @param parent
     * @param callback
     */
    public void createControl(Composite parent, Runnable callback) {
        viewer = new CheckboxTreeViewer(parent, SWT.FULL_SELECTION | SWT.SINGLE | SWT.VIRTUAL
                | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        viewer.getTree().setLayoutData(
                GridDataFactory.fillDefaults().grab(true, true)
                        .hint(SWT.DEFAULT, P4UIUtils.VIEWER_HEIGHT_HINT)
                        .create());
        viewer.setContentProvider(new PerforceContentProvider(viewer, true));
        ChangelistDecorator decorator = new ChangelistDecorator(true);
        ChangelistLabelProvider labelProvider = new ChangelistLabelProvider(
                true);
        StyledChangelistLabelProvider styledLabelProvider = new StyledChangelistLabelProvider(
                labelProvider) {

            @Override
            protected void addCounter(IP4Changelist list, StyledString styled) {
                // Don't add counter since files are filtered from list if not
                // in eclipse workspace
            }

        };
        DecoratedChangelistLabelProvider decorated = new DecoratedChangelistLabelProvider(
                labelProvider, styledLabelProvider, decorator);
        decorated.setAddDecorations(false);
        viewer.setLabelProvider(decorated);
        viewer.setUseHashlookup(true);
        viewer.setSorter(new PendingSorter());
        viewer.addFilter(new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement,
                    Object element) {
                return !(element instanceof IP4Job);
            }

        });
        viewer.addFilter(new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement,
                    Object element) {
                if (element instanceof IP4File) {
                    return ((IP4File) element).getLocalFileForLocation() != null;
                }
                return true;
            }
        });
        viewer.addCheckStateListener(this);
        viewer.setAutoExpandLevel(2);
        loadChangelists(callback);
    }

    /**
     * Get selected file resources
     * 
     * @return non-null but possibly empty array of files
     */
    public IFile[] getSelectedResources() {
        List<IFile> resources = new ArrayList<IFile>();
        for (Object element : viewer.getCheckedElements()) {
            if (element instanceof IP4File) {
                IFile file = ((IP4File) element).getLocalFileForLocation();
                if (file != null) {
                    resources.add(file);
                }
            }
        }
        return resources.toArray(new IFile[resources.size()]);
    }

    /**
     * Process folders
     * 
     * @param folder
     * @param checked
     */
    private void processFolder(IP4Folder folder,
            final Map<IFile, IP4File> files, final List<IP4File> checked) {
        IPath containerPath = null;
        IPath filePath = null;
        for (IContainer container : folder.getLocalContainers()) {
            containerPath = container.getLocation();
            if (containerPath != null) {
                for (Map.Entry<IFile, IP4File> entry: files.entrySet()) {
                	IFile file=entry.getKey();
                    filePath = file.getLocation();
                    if (filePath != null && containerPath.isPrefixOf(filePath)) {
                        checked.add(entry.getValue());
                    }
                }
            }
        }
    }

    private void loadChangelists(final Runnable callback) {
        viewer.setInput(new PerforceContentProvider.Loading());
        P4Runner.schedule(new P4Runnable() {

            @Override
            public String getTitle() {
                return Messages.PendingViewer_LoadingChangelists;
            }

            @Override
            public void run(IProgressMonitor monitor) {
                final IP4Connection[] connections = getConnections();
                Map<IFile, IP4File> files = new HashMap<IFile, IP4File>();
                final List<IP4PendingChangelist> lists = new ArrayList<IP4PendingChangelist>();
                for (IP4Connection connection : connections) {
                    for (IP4PendingChangelist list : connection
                            .getCachedPendingChangelists()) {
                        if (list.needsRefresh()) {
                            list.refresh();
                        }
                        boolean add = false;
                        for (IP4File file : list.getPendingFiles()) {
                            IFile localFile = file.getLocalFileForLocation();
                            if (localFile != null) {
                                add = true;
                                files.put(localFile, file);
                            }
                        }
                        if (add) {
                            lists.add(list);
                        }
                        if (monitor.isCanceled()) {
                            break;
                        }
                    }
                    if (monitor.isCanceled()) {
                        break;
                    }
                }
                if (monitor.isCanceled()) {
                    return;
                }
                final List<IP4File> checkedFiles = new ArrayList<IP4File>();
                for (IP4Resource resource : collection.members()) {
                    if (monitor.isCanceled()) {
                        break;
                    }
                    if (resource instanceof IP4File) {
                        checkedFiles.add((IP4File) resource);
                    } else if (resource instanceof IP4PendingChangelist) {
                        for (IP4File file : ((IP4PendingChangelist) resource)
                                .getPendingFiles()) {
                            checkedFiles.add(file);
                        }
                    } else if (resource instanceof IP4Folder) {
                        processFolder((IP4Folder) resource, files, checkedFiles);
                    }
                }
                if (monitor.isCanceled()) {
                    return;
                }
                PerforceUIPlugin.asyncExec(new Runnable() {

                    public void run() {
                        if (!P4UIUtils.okToUse(viewer)) {
                            return;
                        }
                        viewer.setInput(lists.toArray());
                        Object[] checked = checkedFiles.toArray();
                        viewer.setCheckedElements(checked);
                        updateCheckState(checked, true);
                        if (callback != null) {
                            callback.run();
                        }
                    }
                });
            }

        });
    }

    /**
     * @return check box tree viewer
     */
    public CheckboxTreeViewer getViewer() {
        return this.viewer;
    }

    private void updateCheckState(Object[] elements, boolean state) {
        for (Object element : elements) {
            updateCheckState(element, state);
        }
    }

    private void updateCheckState(Object element, boolean state) {
        if (element instanceof IP4PendingChangelist) {
            viewer.setSubtreeChecked(element, state);
            viewer.setGrayed(element, false);
        } else if (element instanceof IP4File) {
            IP4PendingChangelist parent = ((IP4File) element).getChangelist();
            if(parent==null){
            	PerforceProviderPlugin.logError(MessageFormat.format("Error: {0} is not in any pending change list.",((IP4File) element).getName()));
            	return;
            }
            
            boolean checked = false;
            boolean all = true;
            for (IP4File file : parent.getPendingFiles()) {
                if (viewer.testFindItem(file) != null) {
                    if (viewer.getChecked(file)) {
                        checked = true;
                    } else {
                        all = false;
                    }
                }
            }
            if (all) {
                viewer.setChecked(parent, true);
                viewer.setGrayed(parent, false);
            } else {
                viewer.setGrayChecked(parent, checked);
            }
        }
    }

    /**
     * @see org.eclipse.jface.viewers.ICheckStateListener#checkStateChanged(org.eclipse.jface.viewers.CheckStateChangedEvent)
     */
    public void checkStateChanged(CheckStateChangedEvent event) {
        updateCheckState(event.getElement(), event.getChecked());
    }
}
