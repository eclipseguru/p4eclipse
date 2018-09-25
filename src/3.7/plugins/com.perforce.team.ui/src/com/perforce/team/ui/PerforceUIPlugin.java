package com.perforce.team.ui;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.statushandlers.StatusManager;
import org.osgi.framework.BundleContext;

import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.P4SecureStore;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.PerforceTeamProvider;
import com.perforce.team.core.Tracing;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.synchronize.P4ChangeSetManager;
import com.perforce.team.ui.decorator.PerforceDecorator;
import com.perforce.team.ui.p4java.actions.RefreshAction;
import com.perforce.team.ui.refactor.MoveDeleteManager;

/**
 * Perforce UI plugin class
 * <ul> Updated by ali:
 *   <li>Lazy loading images</li>
 * </ul>
 */
public class PerforceUIPlugin extends AbstractUIPlugin implements IStartup{

    /**
     * ID
     */
    public static final String ID = "com.perforce.team.ui"; //$NON-NLS-1$

    /**
     * Context
     */
	public static final String CONTEXT_PENDING_VIEW = "com.perforce.team.ui.PendingChangelistView"; //$NON-NLS-1$

    private Map<String, ImageDescriptor> imageDescriptors = new HashMap<String, ImageDescriptor>(
            32);

    private static PerforceUIPlugin instance;
    private PerforceMarkerManager markerManager;
    private LogListener logListener;

	private IExecutionListener refreshListener=new IExecutionListener() {

	      public void notHandled(String commandId,
	              NotHandledException exception) {
	      }

	      public void postExecuteFailure(String commandId,
	              ExecutionException exception) {
	      }

	      public void postExecuteSuccess(String commandId,
	              Object returnValue) {
	          if (IWorkbenchCommandConstants.FILE_REFRESH.equals(commandId)) {
	        	  final ISelection selection = getActiveWorkbenchWindow().getSelectionService().getSelection();
		    	  Tracing.printTrace("REFRESH:postExec","Selection= {0}", getActiveWorkbenchWindow().getSelectionService().getSelection());

		    	  try {
		    		  if(refreshRevisionOnRefresh()){
			    		  getDisplay().asyncExec(new Runnable() {

			    			  public void run() {
			    				  try {
			    					  RefreshAction refresh = new RefreshAction();
			    					  refresh.setAsync(false);
			    					  refresh.selectionChanged(null, selection);
			    					  refresh.run(null);
			    				  } catch (Exception e) {
			    					  PerforceProviderPlugin.logWarning(e);
			    				  }
			    			  }
			    		  });
		    		  }
					} catch (Exception e) {
						PerforceProviderPlugin.logWarning(e);
					}
	          }

	      }

	      public void preExecute(String commandId, ExecutionEvent event) {
	    	  Tracing.printTrace("REFRESH:preExec","Selection= {0}", getActiveWorkbenchWindow().getSelectionService().getSelection());
	      }

	  };

    /**
     * Create the perforce ui plugin
     */
    public PerforceUIPlugin() {
        instance = this;
        logListener = new LogListener();
    }

    /**
     * Get image descriptor from the plugin instance
     *
     * @param id
     * @return - image descriptor
     */
    public static ImageDescriptor getDescriptor(String id) {
        return getPlugin().getImageDescriptor(id);
    }

    /**
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);

        logListener.init();
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        markerManager = new PerforceMarkerManager();
        workspace.addResourceChangeListener(markerManager,
                IResourceChangeEvent.POST_BUILD);
        workspace.addResourceChangeListener(new IResourceChangeListener() {

            public void resourceChanged(IResourceChangeEvent event) {

                updateDecorator(event);

            }
        }, IResourceChangeEvent.POST_CHANGE);

        PerforceTeamProvider
                .registerFileModicationsValidator(new FileModificationValidatorManager());
        PerforceTeamProvider.registerMoveDeleteHook(new MoveDeleteManager());
    }

    /**
     * update decorator based on resource events
     *
     * @param event
     */
    private void updateDecorator(IResourceChangeEvent event) {
        /**
         * this handle case where project is opened
         */
        IResourceDelta[] resourceDelta = event.getDelta().getAffectedChildren();
        for (int i = 0; i < resourceDelta.length; i++) {
            IResourceDelta delta = resourceDelta[i];
            int flags = delta.getFlags();
            IResource resource = delta.getResource();
            boolean fopened = (flags & IResourceDelta.OPEN) != 0;
            if ((resource instanceof IProject) && fopened) {
                PerforceDecorator decorator = PerforceDecorator
                        .getActivePerforceDecorator();
                if (decorator != null) {
                    decorator
                            .resourceStateChanged(new IResource[] { resource });
                }
            }
        }
    }

    /**
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.removeResourceChangeListener(markerManager);
        PerforceTeamProvider.registerFileModicationsValidator(null);

        P4ChangeSetManager.getChangeSetManager().dispose();

        removeRefreshListener();
        super.stop(context);
    }

    /**
     * Get the plugin instance
     *
     * @return - plugin instance
     */
    public static PerforceUIPlugin getPlugin() {
        return instance;
    }

    /**
     * Get the display
     *
     * @return - display
     */
    public static Display getDisplay() {
        Display display = Display.getCurrent();
        if (display == null) {
            display = Display.getDefault();
        }
        return display;
    }

    /**
     * Is the current calling thread the UI thread?
     *
     * @return - true if current thread is UI thread, false otherwise
     */
    public static boolean isUIThread() {
        if (Display.findDisplay(Thread.currentThread()) == null) {
            return false;
        }
        return true;
    }

    /**
     * Run the runnable code asynchronously on the display returned from
     * {@link #getDisplay()}
     *
     * @param code
     */
    public static void asyncExec(Runnable code) {
        getDisplay().asyncExec(code);
    }

    /**
     * Run the runnable code synchronously on the display returned from
     * {@link #getDisplay()}
     *
     * @param code
     */
    public static void syncExec(Runnable code) {
        getDisplay().syncExec(code);
    }

    /**
     * Get active workbench window
     *
     * @return - workbench window
     */
    public static IWorkbenchWindow getActiveWorkbenchWindow() {
        return getPlugin().getWorkbench().getActiveWorkbenchWindow();
    }

    /**
     * Get active page
     *
     * @return - workbench page
     */
    public static IWorkbenchPage getActivePage() {
        IWorkbenchWindow window = getActiveWorkbenchWindow();
        return window == null ? null : window.getActivePage();
    }

    /**
     * Get current label decorator
     *
     * @return - label decorator
     */
    public static ILabelDecorator getLabelDecorator() {
        return getPlugin().getWorkbench().getDecoratorManager()
                .getLabelDecorator();
    }

    /**
     * Save any resources currently being edited
     *
     * @param page
     *            the page containing the resources
     * @param paths
     *            the paths to the resources
     *
     */
    public static void saveDirtyResources(IWorkbenchPage page, String[] paths) {
        IEditorPart[] editors = page.getDirtyEditors();
        Map<IFile, IEditorPart> editorLookup = new HashMap<IFile, IEditorPart>();
        for (int i = 0; i < editors.length; i++) {
            IEditorInput input = editors[i].getEditorInput();
            if (input instanceof IFileEditorInput) {
                IFile file = ((IFileEditorInput) input).getFile();
                editorLookup.put(file, editors[i]);
            }
        }

        IFile[] resourceFiles = PerforceProviderPlugin.getWorkspaceFiles(paths);
        for (int i = 0; i < resourceFiles.length; i++) {
            IEditorPart editor = editorLookup.get(resourceFiles[i]);
            if (editor != null) {
                page.saveEditor(editor, true);
            }
        }
    }

    /**
     * Save the dirty resources for the p4 files specified that may be in an
     * open dirty editor
     *
     * @param page
     * @param p4Files
     */
    public static void saveDirtyResources(IWorkbenchPage page, IP4File[] p4Files) {
        if (p4Files != null) {
            for (IP4File file : p4Files) {
                saveDirtyResources(page, file.getLocalFiles());
            }
        }
    }

    /**
     * Save dirty resources
     *
     * @param page
     * @param files
     */
    public static void saveDirtyResources(IWorkbenchPage page, IFile[] files) {
        if (files != null) {
            List<IFile> list = Arrays.asList(files);
            IEditorPart[] editors = page.getDirtyEditors();
            for (int i = 0; i < editors.length; i++) {
                IEditorInput input = editors[i].getEditorInput();
                if (input instanceof IFileEditorInput) {
                    IFile file = ((IFileEditorInput) input).getFile();
                    if (list.contains(file)) {
                        page.saveEditor(editors[i], true);
                    }
                }
            }
        }
    }

    /**
     * Convenience method for logging statuses to the plugin log
     *
     * @param status
     *            the status to log
     */
    public static void log(IStatus status) {
        getPlugin().getLog().log(status);
    }

	/**
	 * Handle an error. The error is logged. If <code>show</code> is
	 * <code>true</code> the error is shown to the user.
	 *
	 * @param message 		a localized message
	 * @param throwable
	 * @param show
	 */
	public static void handleError(String message, Throwable throwable,
			boolean show) {
		IStatus status = new Status(IStatus.ERROR, ID, message,
				throwable);
		int style = StatusManager.LOG;
		if (show)
			style |= StatusManager.SHOW;
		StatusManager.getManager().handle(status, style);
	}

	/**
	 * Shows an error. The error is NOT logged.
	 *
	 * @param message
	 *            a localized message
	 * @param throwable
	 */
	public static void showError(String message, Throwable throwable) {
		IStatus status = new Status(IStatus.ERROR, ID, message,
				throwable);
		StatusManager.getManager().handle(status, StatusManager.SHOW);
	}


    private void createImageDescriptor(String id, URL root) {
        try {
            URL url = new URL(root, IPerforceUIConstants.ICON_PATH + id);
            imageDescriptors.put(id, ImageDescriptor.createFromURL(url));
        } catch (MalformedURLException e) {
        }

    }

    /**
     * Get image descriptor
     *
     * @param id
     * @return - image descriptor or null if none for specified id
     */
    public ImageDescriptor getImageDescriptor(String id) {
    	if(imageDescriptors.get(id)==null){
    		URL root = this.getBundle().getEntry("/"); //$NON-NLS-1$
    		createImageDescriptor(id, root);
    	}
        return imageDescriptors.get(id);
    }

	public static Image getImage( String pluginRelativePath )
	{
		ImageRegistry registry = JFaceResources.getImageRegistry( );
		String resourcePath = ID + "/" + pluginRelativePath; //$NON-NLS-1$
		Image image = registry.get( resourcePath );
		if ( image == null )
		{
			URL url;
			try {
				url = new URL( PerforceUIPlugin.getPlugin().getBundle( )
						.getEntry( "/" ), pluginRelativePath );//$NON-NLS-1$
				if ( url != null )
				{
					image = new Image( Display.getCurrent( ), url.openStream( ) );
				}
				registry.put( resourcePath, image );
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return image;
	}

    static public void storePasswordInUI(final ConnectionParameters params){
        P4UIUtils.getDisplay().asyncExec(new Runnable(){

            public void run() {
                try {
                    String pw = params.peekPassword();
                    String key = params.getStorageKey();
                    if (params.savePassword() && pw!=null && !pw.isEmpty()){
                        P4SecureStore.INSTANCE.put(key,pw, true);
                    }
                } catch (StorageException e) {
                    PerforceProviderPlugin.logError(e);
                    try { // remove the key if can not be stored securely
                        P4SecureStore.INSTANCE.remove(params.getStorageKey());
                    } catch (Exception e2) {
                    }
                }
            }
        });
    }

	public void earlyStartup() {
		registerRefreshListener();
	}

    private void registerRefreshListener() {
    	// Add listener to monitor Refresh commands
    	ICommandService commandService = (ICommandService) PlatformUI
    	      .getWorkbench().getAdapter(ICommandService.class);
    	if (commandService != null) {
    	  commandService.addExecutionListener(refreshListener);
    	}
	}

    private void removeRefreshListener() {
    	// Remove listener to monitor Refresh commands
    	ICommandService commandService = (ICommandService) PlatformUI
    			.getWorkbench().getAdapter(ICommandService.class);
    	if (commandService != null) {
    		commandService.removeExecutionListener(refreshListener);
    	}
	}

	public static boolean refreshRevisionOnRefresh() {
		IPreferenceStore store = PerforceUIPlugin.getPlugin().getPreferenceStore();
		return store.getBoolean(IPerforceUIConstants.PREF_REFRESH_REVISION_ON_REFRESH);
	}

}
