/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.preferences;

import com.perforce.team.core.mylyn.IP4MylynConstants;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.mylyn.P4MylynUiUtils;
import com.perforce.team.ui.mylyn.connection.ConnectionMappingDialog;
import com.perforce.team.ui.mylyn.connection.LinkRepositoryAction;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoryLabelProvider;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ConnectionMappingPreferencePage extends PreferencePage implements
        IWorkbenchPreferencePage {

    private static class Link {

        IP4Connection connection;
        TaskRepository repository;

        Link(IP4Connection connection, TaskRepository repository) {
            this.connection = connection;
            this.repository = repository;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object instanceof Link) {
                Link link = (Link) object;
                return this.connection.equals(link.connection);
            }
            return false;
        }
        
        @Override
        public int hashCode() {
        	if(this.connection!=null)
        		return this.connection.hashCode();
        	return super.hashCode();
        }
        
    }

    private class LinkLabelProvider implements ITableLabelProvider {

        private Image connectionImage = null;
        private ILabelProvider repoDecorator = null;
        private ILabelProviderListener wrapListener = null;

        public LinkLabelProvider() {
            connectionImage = PerforceUIPlugin.getDescriptor(
                    IPerforceUIConstants.IMG_DEPOT_CONNECTION).createImage();

            repoDecorator = new DecoratingLabelProvider(
                    new TaskRepositoryLabelProvider(), PlatformUI
                            .getWorkbench().getDecoratorManager()
                            .getLabelDecorator());

            wrapListener = new ILabelProviderListener() {

                public void labelProviderChanged(LabelProviderChangedEvent event) {
                    if (viewer != null && !viewer.getTable().isDisposed()) {
                        viewer.refresh();
                    }
                }
            };

            repoDecorator.addListener(wrapListener);
        }

        public void removeListener(ILabelProviderListener listener) {

        }

        public boolean isLabelProperty(Object element, String property) {
            return false;
        }

        public void dispose() {
            if (this.connectionImage != null) {
                connectionImage.dispose();
            }
            if (repoDecorator != null) {
            	repoDecorator.removeListener(wrapListener);
                repoDecorator.dispose();
            }
        }

        public void addListener(ILabelProviderListener listener) {

        }

        public String getColumnText(Object element, int columnIndex) {
            switch (columnIndex) {
            case 0:
                return ((Link) element).connection.getName();
            case 1:
                return ((Link) element).repository.getRepositoryLabel();
            default:
                return ""; //$NON-NLS-1$
            }
        }

        public Image getColumnImage(Object element, int columnIndex) {
            switch (columnIndex) {
            case 0:
                return this.connectionImage;
            case 1:
                return this.repoDecorator.getImage(((Link) element).repository);
            default:
                return null;
            }
        }
    }

    private Composite displayArea;
    private TableViewer viewer;
    private ToolItem removeItem;

    private List<IP4Connection> unlinkedConnections = new ArrayList<IP4Connection>();
    private List<TaskRepository> unlinkedRepositories = new ArrayList<TaskRepository>();
    private List<Link> links = new ArrayList<Link>();

    /**
     * ConnectionMappingPreferencePage
     */
    public ConnectionMappingPreferencePage() {
        setDescription(Messages.ConnectionMappingPreferencePage_LinkConnection);
    }

    /**
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        this.displayArea = new Composite(parent, SWT.NONE);
        this.displayArea.setLayout(new GridLayout(2, false));
        this.displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                true));

        this.createViewer(this.displayArea);

        this.createToolbar(this.displayArea);

        return this.displayArea;
    }

    private void createToolbar(Composite parent) {
        ToolBar toolbar = new ToolBar(parent, SWT.VERTICAL);
        toolbar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));

        ToolItem addItem = new ToolItem(toolbar, SWT.PUSH);
        addItem.setToolTipText(Messages.ConnectionMappingPreferencePage_AddMapping);
        Image addImage = PerforceUIPlugin.getDescriptor(
                IPerforceUIConstants.IMG_ADD).createImage();
        P4UIUtils.registerDisposal(addItem, addImage);
        addItem.setImage(addImage);
        addItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                ConnectionMappingDialog dialog = new ConnectionMappingDialog(
                        getShell(),
                        unlinkedConnections
                                .toArray(new IP4Connection[unlinkedConnections
                                        .size()]),
                        unlinkedRepositories
                                .toArray(new TaskRepository[unlinkedRepositories
                                        .size()]));
                if (ConnectionMappingDialog.OK == dialog.open()) {
                    add(dialog.getConnection(), dialog.getRepository());
                }
            }
        });

        removeItem = new ToolItem(toolbar, SWT.PUSH);
        removeItem
                .setToolTipText(Messages.ConnectionMappingPreferencePage_RemoveMapping);
        Image removeImage = PerforceUIPlugin.getDescriptor(
                IPerforceUIConstants.IMG_DELETE).createImage();
        P4UIUtils.registerDisposal(removeItem, removeImage);
        removeItem.setImage(removeImage);
        removeItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                remove(getLinks());
            }
        });
        removeItem.setEnabled(false);
    }

    private Link[] getLinks() {
        List<Link> links = new ArrayList<Link>();
        IStructuredSelection selection = (IStructuredSelection) this.viewer
                .getSelection();
        for (Object select : selection.toArray()) {
            if (select instanceof Link) {
                links.add((Link) select);
            }
        }
        return links.toArray(new Link[links.size()]);
    }

    private void createViewer(Composite parent) {
        this.viewer = new TableViewer(parent, SWT.MULTI | SWT.BORDER
                | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
        this.viewer.getTable().setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, true, true));

        this.viewer.setContentProvider(new ArrayContentProvider());
        this.viewer.setLabelProvider(new LinkLabelProvider());

        this.viewer.getTable().setLinesVisible(true);
        this.viewer.getTable().setHeaderVisible(true);
        this.viewer.setSorter(new ViewerSorter() {

            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
                if (e1 instanceof Link && e2 instanceof Link) {
                    String name1 = ((Link) e1).connection.getName();
                    String name2 = ((Link) e2).connection.getName();
                    if (name1 != null && name2 != null) {
                        return name1.compareToIgnoreCase(name2);
                    }
                }
                return super.compare(viewer, e1, e2);
            }

        });

        TableLayout tableLayout = new TableLayout();
        this.viewer.getTable().setLayout(tableLayout);

        TableColumn column0 = new TableColumn(this.viewer.getTable(), SWT.LEFT);
        column0.setText(Messages.ConnectionMappingPreferencePage_Connection);

        tableLayout.addColumnData(new ColumnWeightData(50, 100));

        TableColumn column1 = new TableColumn(this.viewer.getTable(), SWT.LEFT);
        column1.setText(Messages.ConnectionMappingPreferencePage_TaskRepository);

        tableLayout.addColumnData(new ColumnWeightData(50, 100));

        loadLinks();

        this.viewer
                .addSelectionChangedListener(new ISelectionChangedListener() {

                    public void selectionChanged(SelectionChangedEvent event) {
                        removeItem.setEnabled(getLinks().length > 0);
                    }
                });
    }

    private void add(IP4Connection connection, TaskRepository repository) {
        if (connection != null && repository != null) {
            this.unlinkedConnections.remove(connection);
            this.unlinkedRepositories.remove(repository);
            this.links.add(new Link(connection, repository));
            viewer.setInput(this.links);
        }
    }

    private void remove(Link[] remove) {
        for (Link link : remove) {
            this.unlinkedConnections.add(link.connection);
            this.unlinkedRepositories.add(link.repository);
            this.links.remove(link);
        }
        viewer.setInput(this.links);
    }

    private void loadLinks() {
        for (TaskRepository repository : P4MylynUiUtils
                .getNonPerforceRepositories()) {
            this.unlinkedRepositories.add(repository);
        }
        for (IP4Connection connection : P4ConnectionManager.getManager()
                .getConnections()) {
            TaskRepository repository = P4MylynUiUtils
                    .getRepository(connection);
            if (repository != null
                    && !IP4MylynConstants.KIND.equals(repository
                            .getConnectorKind())) {
                links.add(new Link(connection, repository));
                this.unlinkedRepositories.remove(repository);
            } else {
                unlinkedConnections.add(connection);
            }
        }
        this.viewer.setInput(links);
    }

    /**
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench) {

    }

    /**
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        remove(this.links.toArray(new Link[this.links.size()]));
    }

    /**
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        LinkRepositoryAction linkAction = new LinkRepositoryAction();
        for (Link link : this.links) {
            linkAction.updateLink(link.connection, link.repository);
        }
        for (IP4Connection connection : this.unlinkedConnections) {
            linkAction.updateLink(connection, null);
        }
        return super.performOk();
    }

}
