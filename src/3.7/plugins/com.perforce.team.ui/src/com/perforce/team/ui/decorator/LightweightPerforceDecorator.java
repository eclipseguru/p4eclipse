package com.perforce.team.ui.decorator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IContributorResourceAdapter;

import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.IProjectSettingsChangeListener;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Listener;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.actions.stub.Utils;
import com.perforce.team.ui.preferences.IPreferenceConstants;

public class LightweightPerforceDecorator extends LabelProvider implements ILightweightLabelDecorator, IP4Listener, IProjectSettingsChangeListener, IPropertyChangeListener{
	PerforceDecorator delegate=new PerforceDecorator(false){
	    public String getName() {
	    	return LightweightPerforceDecorator.class.getSimpleName()+":"+super.getName();
	    }

	};

	/**
	 * Property constant pointing back to the extension point id of the
	 * decorator
	 */
	public static final String DECORATOR_ID = "com.perforce.team.ui.decorator.LightweightPerforceDecorator"; //$NON-NLS-1$

    /**
     * Remove trailing whitespace from string
     * 
     * @param str
     * @return - string without trailing whitespace
     */
    public static String removeTrailingWhitespace(StringBuilder str) {
        int i = str.length() - 1;
        while (i > -1 && Character.isWhitespace(str.charAt(i))) {
            i = i - 1;
        }
        if (i == -1) {
            return null;
        }
        return str.substring(0, i + 1);
    }

    /**
     * File and Project text decoration format
     */
    protected boolean decorateIgnored;

    /**
     * fileDecoration
     */
    protected String fileDecoration = ""; //$NON-NLS-1$

    /**
     * projectDecoration
     */
    protected String projectDecoration = ""; //$NON-NLS-1$

    /**
     * outgoingChangeFlag
     */
    protected String outgoingChangeFlag = ""; //$NON-NLS-1$

    /**
     * ignoredText
     */
    protected String ignoredText = ""; //$NON-NLS-1$

    /**
     * unaddedChangeFlag
     */
    protected String unaddedChangeFlag = ""; //$NON-NLS-1$

    /**
     * showChangelists
     */
    protected boolean showChangelists = false;

    /**
     * The cache for storing previously gotten images
     */
    protected IconCache iconCache = new IconCache();

    // If true then this decorator is used for resources, if false
    // then it means it is the decorator for the pending changelist
    // or depot view.
    private boolean resourceDecorator;

	public LightweightPerforceDecorator() {
		super();
		delegate = PerforceDecorator.getActivePerforceDecorator();
		// register listeners
		init();
	}

    /**
     * Do initialization
     */
    private void init() {

        getPreferences();

        // Register listeners for change events
        if (resourceDecorator) {
        	PerforceProviderPlugin.addProjectSettingsChangeListener(this);
        }
        P4ConnectionManager.getManager().addListener(this);
        IPreferenceStore store = PerforceUIPlugin.getPlugin()
                .getPreferenceStore();
        store.addPropertyChangeListener(this);
    }
    
	public void dispose() {
        super.dispose();

        this.iconCache.clear();

        // Remove listeners for change events
        if (resourceDecorator) {
            PerforceProviderPlugin.removeProjectSettingsChangeListener(this);
        }

        // Remove file state listeners
        P4ConnectionManager.getManager().removeListener(this);

        // Remove property store listener
        IPreferenceStore store = PerforceUIPlugin.getPlugin()
                .getPreferenceStore();
        store.removePropertyChangeListener(this);
		
	}

	public void decorate(Object element, IDecoration decoration) {
//		// Don't decorate if UI plugin is not running
//		if (PerforceUIPlugin.getPlugin() == null)
//			return;
//
//		// Don't decorate if the workbench is not running
//		if (!PlatformUI.isWorkbenchRunning())
//			return;
//
//		final IResource resource = getResource(element);
//		if (resource == null)
//			decorateResourceMapping(element, decoration);
//		else {
//			try {
//				decorateResource(resource, decoration);
//			} catch(CoreException e) {
//				PerforceProviderPlugin.logError(e);
//			}
//		}
		
	}

	/**
	 * Decorates a resource mapping (i.e. a Working Set).
	 *
	 * @param element the element for which the decoration was initially called
	 * @param decoration the decoration
	 */
	private void decorateResourceMapping(Object element, IDecoration decoration) {
		@SuppressWarnings("restriction")
		ResourceMapping mapping = Utils.getResourceMapping(element);
//
//		IDecoratableResource decoRes = new DecoratableResourceMapping(mapping);
//
//		/*
//		 *  don't render question marks on working sets. !isTracked() can have two reasons:
//		 *   1) nothing is tracked.
//		 *   2) no indexDiff for the contained projects ready yet.
//		 *  in both cases, don't do anything to not pollute the display of the sets.
//		 */
//		if(!decoRes.isTracked())
//			return;

//		final DecorationHelper2 helper = new DecorationHelper2(
//				PerforceUIPlugin.getPlugin().getPreferenceStore());
//
//		helper.decorate(decoration, decoRes);
	}
	
	// -------- Refresh handling --------

	/**
	 * Perform a blanket refresh of all decorations
	 */
	public static void refresh() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				PerforceUIPlugin.getPlugin().getWorkbench().getDecoratorManager()
						.update(DECORATOR_ID);
			}
		});
	}

	// -------- Helper methods --------

	private static IResource getResource(Object actElement) {
		Object element = actElement;
		if (element instanceof ResourceMapping) {
			element = ((ResourceMapping) element).getModelObject();
		}

		IResource resource = null;
		if (element instanceof IResource) {
			resource = (IResource) element;
		} else if (element instanceof IAdaptable) {
			final IAdaptable adaptable = (IAdaptable) element;
			resource = (IResource) adaptable.getAdapter(IResource.class);
			if (resource == null) {
				final IContributorResourceAdapter adapter = (IContributorResourceAdapter) adaptable
						.getAdapter(IContributorResourceAdapter.class);
				if (adapter != null)
					resource = adapter.getAdaptedResource(adaptable);
			}
		}

		return resource;
	}

	/**
	 * Post a label event to the LabelEventJob
	 *
	 * Posts a generic label event. No specific elements are provided; all
	 * decorations shall be invalidated. Same as
	 * <code>postLabelEvent(null, true)</code>.
	 */
	private void postLabelEvent() {
		// Post label event to LabelEventJob
		LabelEventJob.getInstance().postLabelEvent(this);
	}

	void fireLabelEvent() {
		final LabelProviderChangedEvent event = new LabelProviderChangedEvent(
				this);
		// Re-trigger decoration process (in UI thread)
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				fireLabelProviderChanged(event);
			}
		});
	}

	public void propertyChange(PropertyChangeEvent event) {
		postLabelEvent();		
	}

	public void projectSettingsChanged(IProject project,
			ConnectionParameters params) {
		postLabelEvent();		
	}

	public void resoureChanged(P4Event event) {
		postLabelEvent();		
	}

    /**
     * Go get the decoration preferences
     */
    private void getPreferences() {
        IPreferenceStore store = PerforceUIPlugin.getPlugin()
                .getPreferenceStore();
        decorateIgnored = store
                .getBoolean(IPerforceUIConstants.PREF_IGNORED_TEXT);
        projectDecoration = store
                .getString(IPreferenceConstants.PROJECT_DECORATION_TEXT);
        fileDecoration = store
                .getString(IPreferenceConstants.FILE_DECORATION_TEXT);
        outgoingChangeFlag = store
                .getString(IPreferenceConstants.OUTGOING_CHANGE_DECORATION);
        unaddedChangeFlag = store
                .getString(IPreferenceConstants.UNADDED_CHANGE_DECORATION);
        ignoredText = store.getString(IPreferenceConstants.IGNORED_DECORATION);
        showChangelists = store
                .getBoolean(IPreferenceConstants.SHOW_CHANGELIST_IN_SYNC_VIEW);
    }

	public String getName() {
		return getClass().getSimpleName();
	}

}

/**
 * Job reducing label events to prevent unnecessary (i.e. redundant) event
 * processing
 */
class LabelEventJob extends Job {

	/**
	 * Constant defining the waiting time (in milliseconds) until an event is
	 * fired
	 */
	private static final long DELAY = 100L;

	private static LabelEventJob instance = new LabelEventJob("LabelEventJob"); //$NON-NLS-1$

	/**
	 * Get the LabelEventJob singleton
	 *
	 * @return the LabelEventJob singleton
	 */
	static LabelEventJob getInstance() {
		return instance;
	}

	private LabelEventJob(final String name) {
		super(name);
	}

	private LightweightPerforceDecorator plwDecorator = null;

	/**
	 * Post a label event
	 *
	 * @param decorator
	 *            The GitLightweightDecorator that is used to fire a
	 *            LabelProviderChangedEvent
	 */
	void postLabelEvent(final LightweightPerforceDecorator decorator) {
		if (this.plwDecorator == null)
			this.plwDecorator = decorator;
		if (getState() == SLEEPING || getState() == WAITING)
			cancel();
		schedule(DELAY);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		if (plwDecorator != null)
			plwDecorator.fireLabelEvent();
		return Status.OK_STATUS;
	}
}

