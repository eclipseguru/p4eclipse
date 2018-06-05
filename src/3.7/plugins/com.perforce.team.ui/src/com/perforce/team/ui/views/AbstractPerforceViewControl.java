package com.perforce.team.ui.views;

import java.text.MessageFormat;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.progress.UIJob;

import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.IProjectSettingsChangeListener;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Listener;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.PerforceUIPlugin;


public abstract class AbstractPerforceViewControl implements IPerforceViewControl, IProjectSettingsChangeListener, ISelectionListener{

    /**
     * Get the name to display as an informative message for what will be
     * displayed if a perforce connection or resource is selected.
     * 
     * @return - display name
     */
    protected abstract String getSelectedName();
    
    /**
     * Create control
     * 
     * @param parent
     */
    protected abstract void createControl(Composite parent);
    
    
    private IPerforceView view;
    
    protected IPerforceView getView(){
        return view;
    }

    public AbstractPerforceViewControl(IPerforceView view){
        this.view=view;
    }
    
    protected void registerContextMenu(MenuManager menuManager,
            ISelectionProvider selectionProvider){
    	IWorkbenchPartSite site = getView().getSite();
        if(site!=null){
            site.registerContextMenu(menuManager, selectionProvider);
            site.setSelectionProvider(selectionProvider);
        }
    }
    
    protected IActionBars getActionBars(){
    	IWorkbenchPartSite site = getView().getSite();
        if(site instanceof IViewSite){
            return ((IViewSite)site).getActionBars();
        }    
        return null;
    }
    
    public IP4Connection getConnection() {
        return this.p4Connection;
    }
    
    /**
     * P4 connection
     */
    protected IP4Connection p4Connection;

    /**
     * Main display area
     */
    protected Composite displayArea;

    /**
     * Connection label
     */
    private Label connectionLabel;

    /**
     * Stack composite
     */
    private Composite stackComposite;

    /**
     * Stack layout
     */
    private StackLayout stackLayout;

    /**
     * Label to display when no Perforce resource selected
     */
    private Label noConnectionSelected;

    /**
     * Should layout be done?
     */
    protected boolean layout = false;

    /**
     * Project scheduling rule
     */
    protected static final ISchedulingRule RULE = new ISchedulingRule() {

        public boolean isConflicting(ISchedulingRule rule) {
            return this == rule;
        }

        public boolean contains(ISchedulingRule rule) {
            return this == rule;
        }
    };

    /**
     * P4 listener
     */
    protected IP4Listener p4Listener = new IP4Listener() {

        public void resoureChanged(P4Event event) {
            final IP4Connection current = p4Connection;
            if (current != null && EventType.REMOVED == event.getType()) {
                for (IP4Connection connection : event.getConnections()) {
                    if (current.equals(connection)) {
                        UIJob invalidateConnection = new UIJob(
                                Messages.PerforceProjectView_UpdateViewForRemovedConnection) {

                            @Override
                            public IStatus runInUIThread(
                                    IProgressMonitor monitor) {
                                if (p4Connection == current) {
                                    showNoConnection();
                                }
                                return Status.OK_STATUS;
                            }
                        };
                        invalidateConnection.setSystem(true);
                        invalidateConnection.schedule();
                        // Break so invalidate will only ever be called once
                        // per event regardless of the contents of the event
                        break;
                    }
                }
            }
        }
		public String getName() {
			return AbstractPerforceViewControl.class.getSimpleName();
		}
    };

    public Composite createViewControl(Composite parent) {
        this.stackComposite = new Composite(parent, SWT.NONE);
        this.stackLayout = new StackLayout();
        this.stackComposite.setLayout(this.stackLayout);
        this.stackComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
                true, true));

        this.noConnectionSelected = new Label(this.stackComposite, SWT.WRAP);
        this.noConnectionSelected.setText(MessageFormat
                .format(Messages.PerforceProjectView_SelectAResource,
                        getSelectedName()));
        stackLayout.topControl = this.noConnectionSelected;

        this.displayArea = new Composite(this.stackComposite, SWT.NONE);
        GridLayout daLayout = new GridLayout(1, true);
        daLayout.marginHeight = 0;
        daLayout.marginWidth = 0;
        daLayout.horizontalSpacing = 0;
        daLayout.verticalSpacing = 0;
        this.displayArea.setLayout(daLayout);
        this.displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                true));

        this.connectionLabel = new Label(this.displayArea, SWT.WRAP);
        this.connectionLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
                true, false));
        updateConnectionLabel();

        createControl(this.displayArea);
        setInitialConnection(getView().getConnection());
        layout = true;
        
        return this.stackComposite;
    }

    private void setInitialConnection(IP4Connection connection){
        if (connection != null) {
            selectionChanged(null, new StructuredSelection(connection));
        }
    }

    /**
     * Update connection label
     */
    protected void updateConnectionLabel() {
        IP4Connection connection = this.p4Connection;
        if (connection != null) {
            String name = connection.getName();
            if (name == null) {
                name = ""; //$NON-NLS-1$
            }
            this.connectionLabel.setText(MessageFormat.format(
                    Messages.PerforceProjectView_Connection, name));
        }
    }

    /**
     * Remove project listeners
     */
    final protected void removeProjectListeners() {

        // Remover listeners
        IWorkbenchPartSite site = getView().getSite();
        if(site != null)
            site.getPage().removeSelectionListener(this);
        PerforceProviderPlugin.removeProjectSettingsChangeListener(this);
        P4ConnectionManager.getManager().removeListener(p4Listener);
    }

    /**
     * Add project listeners
     */
    final protected void addProjectListeners() {

        // Listen for project manage/unmanage events
        PerforceProviderPlugin.addProjectSettingsChangeListener(this);
        P4ConnectionManager.getManager().addListener(p4Listener);
        // add myself as a selection listener
        IWorkbenchPartSite site = getView().getSite();
        if(site != null)
            site.getPage().addSelectionListener(this);
    }

    /**
     * Handle projects being managed/unmanaged
     * 
     * @param project
     * @param params
     */
    public void projectSettingsChanged(IProject project,
            ConnectionParameters params) {
        changeConnection(P4ConnectionManager.getManager()
                .getConnection(project));
    }

    private void connectionSelected(final IP4Connection connection) {
        P4Runner.schedule(new P4Runnable() {

            @Override
            public void run(IProgressMonitor monitor) {
                changeConnection(connection);
            }

        });
    }

    /**
     * Handle selection events and change input according to which provider has
     * been selected.
     * 
     * @param part
     * @param selection
     */
    public void selectionChanged(IWorkbenchPart part,final ISelection selection) {
        P4Runner.schedule(new P4Runnable() {

            @Override
            public void run(IProgressMonitor monitor) {
                connectionSelected(getConnection(selection));
            }

        }, RULE);
    }

    /**
     * Change connection based on resource in selection. This method changed the
     * connection by loading the resource from the selection in a synchronous
     * manner.
     * 
     * @param selection
     */
    public void changeConnection(ISelection selection) {
        changeConnection(getConnection(selection));
    }

    public static IP4Connection getConnection(ISelection selection) {
        IP4Connection connection = null;
        if (selection instanceof IStructuredSelection) {
            Object element = ((IStructuredSelection) selection)
                    .getFirstElement();
            IResource resource = getResource(element);
            if (resource != null) {
                // This is an attempt to get a connection object in the quickest
                // way. Since all the project views need are connections, try to
                // short-circuit a full IP4Resource load by getting the
                // connection through the project properties. Getting an
                // IP4Resource may result in one or more fstats which
                // is unnecessary since only the connection is needed at this
                // point. This method is run in a runnable that
                // contains a scheduling rule so it is important to finish
                // quickly since other views may be trying to load.
                IProject project = resource.getProject();
                if (project != null) {
                    connection = P4ConnectionManager.getManager()
                            .getConnection(project);
                }
                if (connection == null) {
                    IP4Resource p4Resource = P4ConnectionManager.getManager()
                            .getResource(resource);
                    if (p4Resource != null) {
                        connection = p4Resource.getConnection();
                    }
                }
            } else if (element instanceof IP4Connection) {
                connection = (IP4Connection) element;
                if (!connection.isOffline() && !connection.isConnected()) {
                    connection.connect();
                }
            } else if (element instanceof IP4Resource) {
                connection = ((IP4Resource) element).getConnection();
            }
        }
        return connection;
    }

    /**
     * Gets a p4 resource from an element that can be adapted to an IResource
     * and then find the corresponding p4 resource
     * 
     * @param element
     * @return - p4 resource
     */
    protected IP4Resource getP4Resource(Object element) {
        IP4Resource p4Resource = null;
        IResource resource = getResource(element);
        if (resource != null) {
            p4Resource = P4ConnectionManager.getManager().getResource(resource);
        }
        return p4Resource;
    }

    /**
     * Get the resource from an object
     */
    public static IResource getResource(Object obj) {
        if (obj instanceof IResource) {
            return (IResource) obj;
        } else if (obj instanceof IAdaptable) {
            return (IResource) ((IAdaptable) obj).getAdapter(IResource.class);
        }
        return null;
    }

    /**
     * Changes the connection
     * 
     * @param newConnection
     */
    protected void changeConnection(final IP4Connection newConnection) {
        if (newConnection != null
                && (!showingConnection() || newConnection != p4Connection)) {
            PerforceUIPlugin.syncExec(new Runnable() {

                public void run() {
                    p4Connection = newConnection;
                    setConnection(p4Connection);
                }
            });
        }
    }

    /**
     * Is the view currently showing a connection?
     * 
     * This method should not need to be run on the UI-thread
     * 
     * @return - true if view is displaying a connection
     */
    protected boolean showingConnection() {
        return stackLayout != null
                && stackLayout.topControl != noConnectionSelected;
    }

    /**
     * Set top control
     * 
     * @param layout
     */
    protected void showDisplayArea(boolean layout) {
        stackLayout.topControl = this.displayArea;
        if (layout) {
            stackComposite.layout(true, true);
        }
    }

    /**
     * Set top control
     * 
     */
    protected void showDisplayArea() {
        showDisplayArea(this.layout);
    }

    /**
     * Show no connection
     * 
     * @param layout
     */
    protected void showNoConnection(boolean layout) {
        stackLayout.topControl = noConnectionSelected;
        if (layout) {
            stackComposite.layout(true, true);
        }
    }

    /**
     * Show no connection
     */
    protected void showNoConnection() {
        showNoConnection(this.layout);
    }

    /**
     * Set connection of this view
     * 
     * @param connection
     */
    protected void setConnection(IP4Connection connection) {
        updateConnectionLabel();
        this.setViewerInput(connection);
    }

    /**
     * Sets the viewer input, subclasses should override
     * 
     * @param connection
     */
    protected void setViewerInput(IP4Connection connection) {
    }

    /**
     * Returns true if the connection is non-null, not offline, and not disposed
     * 
     * @param connection
     * @return - true if okay
     */
    protected boolean connectionOK(IP4Connection connection) {
        return connection != null && !connection.isOffline()
                && !connection.isDisposed();
    }


}
