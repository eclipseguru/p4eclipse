package com.perforce.team.ui.decorator;

/*
 * Copyright (c) 2003, 2004 Perforce Software.  All rights reserved.
 *
 */

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;
import org.eclipse.ui.IDecoratorManager;

import com.perforce.p4java.core.IDepot.DepotType;
import com.perforce.p4java.core.IStreamSummary;
import com.perforce.p4java.core.file.FileAction;
import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.IProjectSettingsChangeListener;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.PerforceTeamProvider;
import com.perforce.team.core.Policy;
import com.perforce.team.core.Tracing;
import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Folder;
import com.perforce.team.core.p4java.IP4Listener;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4ShelveFile;
import com.perforce.team.core.p4java.IP4Stream;
import com.perforce.team.core.p4java.IP4SubmittedFile;
import com.perforce.team.core.p4java.P4Depot;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4File;
import com.perforce.team.core.p4java.synchronize.IP4ChangeSet;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.IgnoredFiles;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.preferences.IPreferenceConstants;

/**
 * The Perforce decorator. The decorator now computes all decorations in the
 * foreground. This is because a) it is reasonably fast and b) it is very
 * reliable. Computing decorations in a background thread always has problems
 * when firing a LabelProviderChangedEvent since these sometimes get ignored and
 * the label will not get refreshed!
 */
public class PerforceDecorator extends LabelProvider implements
        ILabelDecorator, IProjectSettingsChangeListener,
        IPropertyChangeListener, IP4Listener {

    /**
     * The ID for this decorator
     */
    public static final String ID = "com.perforce.team.ui.decorator.PerforceDecorator"; //$NON-NLS-1$

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
     * connectionDecoration
     */
    protected String connectionDecoration = ""; //$NON-NLS-1$

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

    private boolean suspendDecoration = false;

    /**
     * Suspend the plugin-level decorator from performing any perforce-related
     * text/image decorations
     */
    public static void suspendDecoration() {
        PerforceDecorator decorator = getActivePerforceDecorator();
        if (decorator != null) {
            decorator.suspendDecoration = true;
        }
    }

    /**
     * Unsuspend the plugin-level decorator from performing any perforce-related
     * text/image decorations
     */
    public static void unsuspendDecoration() {
        PerforceDecorator decorator = getActivePerforceDecorator();
        if (decorator != null) {
            decorator.suspendDecoration = false;
        }
    }

    private String decorateText(IP4File file, String text, boolean ignored) {
        return decorateText(file, text, false, false, ignored);
    }

    private String getIncomingAction(IP4File file) {
        String headAction = null;
        FileAction action = file.getHeadAction();
        if (action != null) {
            headAction = action.toString().toLowerCase();
        }
        return headAction;
    }

    private String decorateText(IP4File file, String text,
            boolean showChangelist, boolean showIncoming, boolean ignored) {
        Map<String, String> bindings = new HashMap<String, String>();
        if (file.getHeadRevision() > 0) {
            bindings.put(P4Decoration.HAVE_VARIABLE,
                    Integer.toString(file.getHaveRevision()));
            bindings.put(P4Decoration.HEAD_VARIABLE,
                    Integer.toString(file.getHeadRevision()));
            bindings.put(P4Decoration.HEAD_CHANGE_VARIABLE,
                    Integer.toString(file.getHeadChange()));
        } else {
            if (!ignored && !file.isOpened()) {
                bindings.put(P4Decoration.UNADDED_CHANGE_VARIABLE,
                        unaddedChangeFlag);
            }
        }

        String type = file.getOpenedType();
        if (type == null) {
            type = file.getHeadType();
        }
        if (type != null) {
            bindings.put(P4Decoration.TYPE_VARIABLE, type);
        }

        if (file.isOpened() && file.openedByOwner()) {
            bindings.put(P4Decoration.OUTGOING_CHANGE_VARIABLE,
                    outgoingChangeFlag);
        }

        FileAction action = file.getAction();
        String actionText = action != null
                ? action.toString().toLowerCase()
                : null;
        if (action == null) {
            if (showIncoming) {
                actionText = getIncomingAction(file);
            } else if (P4File.isActionDelete(file.getHeadAction())) {
                if (file.getHaveRevision() == 0) {
                    actionText = "-deleted-";
                } else {
                    actionText = "-head rev deleted-";
                }
            }
        }

        if (actionText != null) {
            bindings.put(P4Decoration.ACTION_VARIABLE, actionText);
        }

        bindings.put(P4Decoration.NAME_VARIABLE, text);

        StringBuilder decorated = P4Decoration.decorate(fileDecoration,
                bindings);
        if (showChangelist) {
            int id = file.getChangelistId();
            if (id == 0) {
                decorated.append(" <Changelist: Default>");
            } else if (id > 0) {
                addChangelist(decorated, id);
            } else {
                id = file.getHeadChange();
                if (id > 0) {
                    addChangelist(decorated, id);
                }
            }
        }
        return removeTrailingWhitespace(decorated);
    }

    private void addChangelist(StringBuilder buffer, int id) {
        buffer.append(" <Changelist: #");
        buffer.append(id);
        buffer.append('>');
    }

    /**
     * Get decoration text for a project
     * 
     * @param text project name
     * @param connection
     * @return - the decorated project name
     */
    protected String decorateProjectWithConnection(String name, IP4Connection connection) {
        String decorated = name;
        if (connection != null) {
            ConnectionParameters params = connection.getParameters();

            Map<String, String> bindings = new HashMap<String, String>();
            bindings.put(P4Decoration.SERVER_VARIABLE, params.getPort());
            bindings.put(P4Decoration.CLIENT_VARIABLE, params.getClient());
            bindings.put(P4Decoration.USER_VARIABLE, params.getUser());
            bindings.put(P4Decoration.CHARSET_VARIABLE,
                    params.getCharsetNoNone());
            bindings.put(P4Decoration.NAME_VARIABLE, name);
            
            updateSandBoxAndStreamDecoration(bindings,connection);

            decorated = P4Decoration.decorate(projectDecoration, bindings)
                    .toString();
        }
        return decorated;
    }

    private void updateSandBoxAndStreamDecoration(Map<String, String> bindings,
			IP4Connection connection) {
        if (connection.isOffline()) {
            bindings.put(P4Decoration.OFFLINE_VARIABLE, IPerforceUIConstants.DEC_OFFLINE);
        }
        if (connection.isSandbox()) {
            bindings.put(P4Decoration.SANDBOX_VARIABLE, IPerforceUIConstants.DEC_SANDBOX);
        }

        if(!connection.isOffline() && connection.getClient()!=null && connection.getClient().getStream()!=null){
        	String streamStr=connection.getClient().getStream();
        	if(!StringUtils.isEmpty(streamStr.trim())){
	        	IP4Stream stream = connection.getStream(streamStr.trim());
	        	if(stream!=null && stream.getStreamSummary()!=null){
		        	IStreamSummary sum = stream.getStreamSummary();
		        	bindings.put(P4Decoration.STREAM_NAME_VARIABLE, sum.getName());
		        	bindings.put(P4Decoration.STREAM_ROOT_VARIABLE, sum.getStream());
	        	}
        	}
        }
	}

	/**
     * Add the current ignored text decoration to the specified string
     * 
     * @param text
     * @return - decorated text
     */
    protected String decorateIgnoredResource(String text) {
        return text + " " + ignoredText; //$NON-NLS-1$
    }

    /**
     * Decorator constructor.
     */
    public PerforceDecorator() {
        resourceDecorator = true;
        init();
    }

    /**
     * Inlined here since between Eclipse 3.2 and 3.3 the hierarchy of
     * LabelProvider was changed and this function was moved to
     * BaseLabelProvider. This causes issues because pluginbuilder builds
     * against 3.3 and therefore injects a reference to BaseLabelProvider when
     * compiling that then causes {@link NoClassDefFoundError} exceptions when
     * the plugin is installed on Eclipse 3.2.
     * 
     * Fixes job032543.
     * 
     * @see org.eclipse.jface.viewers.LabelProvider#fireLabelProviderChanged(org.eclipse.jface.viewers.LabelProviderChangedEvent)
     */
    @Override
    protected void fireLabelProviderChanged(
            final LabelProviderChangedEvent event) {
        Object[] listeners = getListeners();
        for (int i = 0; i < listeners.length; ++i) {
            if (listeners[i] instanceof ILabelProviderListener) {
                final ILabelProviderListener l = (ILabelProviderListener) listeners[i];
                SafeRunnable.run(new SafeRunnable() {

                    public void run() {
                        l.labelProviderChanged(event);
                    }
                });
            }
        }
    }

    /**
     * Decorator constructor.
     * 
     * @param resourceDecorator
     *            set to false if this is to be the decorator for the pending
     *            changelist or depot view.
     */
    public PerforceDecorator(boolean resourceDecorator) {
        this.resourceDecorator = resourceDecorator;
        init();
    }

    /**
     * Get the active Perforce decorator
     * 
     * @return the active Perforce decorator. Returns null if not active
     */
    public static PerforceDecorator getActivePerforceDecorator() {
        IDecoratorManager manager = PerforceUIPlugin.getPlugin().getWorkbench()
                .getDecoratorManager();
        if (manager.getEnabled(ID)) {
            return (PerforceDecorator) manager.getLabelDecorator(ID);
        }
        return null;
    }

    /**
     * Shutting down
     */
    @Override
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

    /**
     * Capture when projects are managed or unmanaged or connection settings are
     * changed.
     * 
     * @param project
     * @param params
     */
    public void projectSettingsChanged(IProject project,
            ConnectionParameters params) {

        // Refresh the project
        resourceStateChanged(new IResource[] { project });
    }

    private boolean isValidPref(String property) {
        return IPerforceUIConstants.PREF_FILE_FORMAT.equals(property)
                || IPerforceUIConstants.PREF_PROJECT_FORMAT.equals(property)
                || IPerforceUIConstants.PREF_IGNORED_TEXT.equals(property)
                || IPerforceUIConstants.PREF_FILE_OPEN_ICON.equals(property)
                || IPerforceUIConstants.PREF_FILE_SYNC_ICON.equals(property)
                || IPerforceUIConstants.PREF_FILE_SYNC2_ICON.equals(property)
                || IPerforceUIConstants.PREF_FILE_UNRESOLVED_ICON
                        .equals(property)
                || IPerforceUIConstants.PREF_FILE_LOCK_ICON.equals(property)
                || IPerforceUIConstants.PREF_MARK_UNMANAGED_FILES
                        .equals(property)
                || IPerforceUIConstants.PREF_FILE_OTHER_ICON.equals(property)
                || IPreferenceConstants.IGNORED_DECORATION.equals(property)
                || IPerforceUIConstants.PREF_IGNORED_ICON.equals(property)
                || IPreferenceConstants.SHOW_CHANGELIST_IN_SYNC_VIEW
                        .equals(property)
                || IPreferenceConstants.CONNECTION_DECORATION_TEXT.equals(property)
                || IPreferenceConstants.PROJECT_DECORATION_TEXT
                        .equals(property)
                || IPreferenceConstants.FILE_DECORATION_TEXT.equals(property)
                || IPreferenceConstants.OUTGOING_CHANGE_DECORATION
                        .equals(property)
                || IPerforceUIConstants.PREF_LOCAL_ONLY_ICON.equals(property)
                || IPerforceUIConstants.PREF_STREAM_SANDBOX_ICON.equals(property)
                || IPerforceUIConstants.PREF_STREAM_SANDBOX_PROJECT_ICON.equals(property)
                ;
    }

    /**
     * Capture changes to decoration preferences. Note that we piggyback the
     * PREF_MARK_UNMANAGED_FILES event here as they're properly handled in the
     * same icon cache code. This will <i>definitely</i> need revisiting later
     * -- HR.
     * 
     * @param event
     *            the property change event
     */
    public void propertyChange(PropertyChangeEvent event) {
        if (isValidPref(event.getProperty())) {

            // Discard all icons
            iconCache.clear();

            // Go get the latest preferences
            getPreferences();

            // Refresh everything
            fireLabelProviderChanged(new LabelProviderChangedEvent(this));
        }
    }

    /**
     * This method is called if resources have changed or need to be refreshed
     * 
     * @param resources
     *            the list of resources which have changed
     */
    public void resourceStateChanged(final IResource[] resources) {
        boolean gotFolder = false;
        // Drop any caches
        for (int i = 0; i < resources.length; i++) {
            int type = resources[i].getType();
            if (type == IResource.PROJECT || type == IResource.FOLDER) {
                gotFolder = true;
            }
        }
        // If we have Projects or Folders then refresh everything
        // Otherwise just do the specific resources.
        final boolean gotFolder2 = gotFolder;

        PerforceUIPlugin.getDisplay().asyncExec(new Runnable() {

            public void run() {
                if (gotFolder2) {
                    fireLabelProviderChanged(new LabelProviderChangedEvent(
                            PerforceDecorator.this));
                } else {
                    fireLabelProviderChanged(new LabelProviderChangedEvent(
                            PerforceDecorator.this, resources));
                }

            }
        });

    }

    /**
     * Provide image decoration for this object
     * 
     * @param image
     *            the image to decorate
     * @param o
     *            the object to decorate
     * @return the decorated image
     */
    public Image decorateImage(Image image, Object o) {
        if (suspendDecoration || image == null) {
            return null;
        }

        IResource resource = PerforceProviderPlugin.getResource(o);
        boolean managedFile = resource != null;

        if (resource instanceof IProject) {
            image = iconCache.getProjectImage(image, (IProject) resource);
        } else if (o instanceof IP4File) {
            IP4File p4Resource = (IP4File) o;
            image = iconCache.getImage(image, p4Resource, managedFile, false);
        } else if (o instanceof IFile) {
            IP4Resource p4Resource = P4ConnectionManager.getManager()
                    .asyncGetResource((IFile) o); // this may return null. But it will fire resource change event to refresh the file.
            if (p4Resource instanceof IP4File) {
                boolean ignored = iconCache.ignoredLocation > 0
                        && IgnoredFiles.isIgnored((IFile) o);
                image = iconCache.getImage(image, (IP4File) p4Resource,
                        managedFile, ignored);
            }
        } else if (o instanceof IP4PendingChangelist) {
            image = iconCache.getImage(image, (IP4PendingChangelist) o);
        } else if (o instanceof IP4ShelveFile) {
            image = iconCache.getImage((IP4ShelveFile) o);
        } else if (o instanceof IP4SubmittedFile) {
            image = iconCache.getImage(image, (IP4SubmittedFile) o);
        } else if (o instanceof IP4Connection) {
            image = iconCache.getImage(image, (IP4Connection) o);
        } else if (o instanceof IAdaptable) {
            IP4File file = P4CoreUtils.convert(o, IP4File.class);
            if (file != null) {
                image = iconCache.getImage(image, file, managedFile, false);
            }
        }
        return image;
    }

    /**
     * Decorated a project
     * 
     * @param text
     * @param project
     * @return - decorated text or null
     */
    public String decorateProject(String text, IProject project) {
        String decorated = null;
        PerforceTeamProvider provider = PerforceProviderPlugin
                .getPerforceProviderFor(project);
        if (provider != null) {
            decorated = getProjectText(text, project);
        }
        return decorated;
    }

    /**
     * Decorate sync element
     * 
     * @param text
     * @param element
     * @return - decorated text or null
     */
    public String decorateSyncElement(String text,
            ISynchronizeModelElement element) {
        IResource resource = element.getResource();
        if (resource instanceof IFile) {
            IP4Resource p4Resource = P4ConnectionManager.getManager()
                    .asyncGetResource(resource);
            if (p4Resource instanceof IP4File) {
                return getFileText(text, (IP4File) p4Resource, showChangelists,
                        true);
            }
        } else if (resource instanceof IProject) {
            return decorateProject(text, (IProject) resource);
        } else if (element instanceof IAdaptable) {
            IP4ChangeSet set = P4CoreUtils.convert(element, IP4ChangeSet.class);
            if (set != null) {
                IP4Changelist change = set.getChangelist();
                if (change != null) {
                    StringBuilder changeDescription = new StringBuilder(
                            set.getName());
                    String user = change.getUserName();
                    String client = change.getClientName();
                    if (user != null && client != null) {
                        changeDescription.append(" " + user + "@" + client); //$NON-NLS-1$ //$NON-NLS-2$
                    }

                    if (!change.isDefault()) {
                        String description = change.getShortDescription();
                        if (description.length() > 0) {
                            changeDescription
                                    .append(" { " + description + " }"); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                    }
                    return changeDescription.toString();
                }
            }
        }
        return null;
    }

    /**
     * Decorate resource
     * 
     * @param text
     * @param resource
     * @return - decorated text or null
     */
    public String decorateResource(String text, IResource resource) {
        String decorated = null;
        boolean ignored = decorateIgnored
                && PerforceTeamProvider.getPerforceProvider(resource) != null
                && IgnoredFiles.isIgnored(resource);
        if (resource instanceof IFile) {
            IP4Resource p4Resource = P4ConnectionManager.getManager()
                    .asyncGetResource(resource);
            if (p4Resource instanceof IP4File) {
                decorated = getFileText(text, (IP4File) p4Resource, ignored);
            }
        }
        if (ignored) {
            decorated = decorateIgnoredResource(text);
        }
        return decorated;
    }

    /**
     * Decorate a connection
     * 
     * @param text
     * @param connection
     * @return - decorated text or original text if no decoration
     */
    private String decorateConnection(String text, IP4Connection connection) {
        Map<String, String> bindings = new HashMap<String, String>();
        
        updateSandBoxAndStreamDecoration(bindings, connection);
        
        if(bindings.isEmpty())
        	return text;

        bindings.put(P4Decoration.NAME_VARIABLE, text);

        String decorated = P4Decoration.decorate(connectionDecoration, bindings)
                .toString();
        return decorated;
    }

    /**
     * Decorate a shelved file. Shelved files have a subset of decorations so
     * only have, type, name, and action are currently added. This method still
     * honors the current decoration preferences for files for the text
     * decorations supported.
     * 
     * @param text
     * @param shelvedFile
     * @return - decorated text or null
     */
    public String decorateShelvedFile(String text, IP4ShelveFile shelvedFile) {
        IP4File file = shelvedFile.getFile();
        Map<String, String> bindings = new HashMap<String, String>();

        int have = file.getHaveRevision();
        if (have > 0) {
            bindings.put(P4Decoration.HAVE_VARIABLE, Integer.toString(have));
        }

        String type = file.getOpenedType();
        if (type == null) {
            type = file.getHeadType();
        }
        if (type != null) {
            bindings.put(P4Decoration.TYPE_VARIABLE, type);
        }

        FileAction action = file.getAction();
        String actionText = action != null
                ? action.toString().toLowerCase()
                : null;
        if (action == null) {
            if (P4File.isActionDelete(file.getHeadAction())) {
                if (file.getHaveRevision() == 0) {
                    actionText = "-deleted-";
                } else {
                    actionText = "-head rev deleted-";
                }
            }
        }

        if (actionText != null) {
            bindings.put(P4Decoration.ACTION_VARIABLE, actionText);
        }

        bindings.put(P4Decoration.NAME_VARIABLE, text);

        StringBuilder decorated = P4Decoration.decorate(fileDecoration,
                bindings);
        return removeTrailingWhitespace(decorated);
    }

    /**
     * Decorate a submitted file. Submitted files have a subset of decorations
     * so only head, type, name, and action are currently added. This method
     * still honors the current decoration preferences for files for the text
     * decorations supported.
     * 
     * @param text
     * @param submittedFile
     * @return - decorated text or null
     */
    public String decorateSubmittedFile(String text,
            IP4SubmittedFile submittedFile) {
        IP4File file = submittedFile.getFile();
        Map<String, String> bindings = new HashMap<String, String>();
        bindings.put(P4Decoration.HAVE_VARIABLE,
                Integer.toString(submittedFile.getRevision()));

        String type = file.getOpenedType();
        if (type == null) {
            type = file.getHeadType();
        }
        if (type != null) {
            bindings.put(P4Decoration.TYPE_VARIABLE, type);
        }

        FileAction action = file.getAction();
        String actionText = action != null
                ? action.toString().toLowerCase()
                : null;
        if (action == null) {
            if (P4File.isActionDelete(file.getHeadAction())) {
                if (file.getHaveRevision() == 0) {
                    actionText = "-deleted-";
                } else {
                    actionText = "-head rev deleted-";
                }
            }
        }

        if (actionText != null) {
            bindings.put(P4Decoration.ACTION_VARIABLE, actionText);
        }

        bindings.put(P4Decoration.NAME_VARIABLE, text);

        StringBuilder decorated = P4Decoration.decorate(fileDecoration,
                bindings);
        return removeTrailingWhitespace(decorated);
    }

    /**
     * Decorate a folder. The only possible decoration is for folders which are
     * the root of a stream, which are decorated with the stream name.
     * 
     * @param text
     * @param folder
     * @return - decorated text or null
     */
    public String decorateFolder(String text, IP4Folder folder) {
        String streamName = streamNameForStreamRoot(folder);
        if (streamName.isEmpty())
            return text;
        return MessageFormat.format("{0} ({1})", text, streamName);
    }

    /*
     * return stream name if folder is the root of a stream, else ""
     */
    private String streamNameForStreamRoot(IP4Folder folder) {
        if (!(folder.getParent() instanceof P4Depot))
            return "";
        final P4Depot depot = (P4Depot) folder.getParent();
        if (depot.getType() != DepotType.STREAM)
            return "";
        for (IStreamSummary stream : depot.getStreams()) {
            if (stream.getStream().equals(folder.getRemotePath()))
                return stream.getName();
        }
        return "";
    }

    /**
     * Provide text decoration for this object
     * 
     * @param text
     *            the text to decorate
     * @param o
     *            the object to decorate
     * @return the decorated text
     */
    public String decorateText(String text, Object o) {
        if (text != null && !suspendDecoration) {
            String decorated = null;
            if (o instanceof IP4File) {
                decorated = getFileText(text, (IP4File) o);
            } else if (o instanceof IP4Connection) {
                decorated = decorateConnection(text, (IP4Connection) o);
            } else if (o instanceof IP4Folder) {
                decorated = decorateFolder(text, (IP4Folder) o);
            } else if (o instanceof IProject) {
                return decorateProject(text, (IProject) o);
            } else if (o instanceof IResource) {
                decorated = decorateResource(text, (IResource) o);
            } else if (o instanceof ISynchronizeModelElement) {
                decorated = decorateSyncElement(text,
                        (ISynchronizeModelElement) o);
            } else if (o instanceof IP4ShelveFile) {
                decorated = decorateShelvedFile(text, (IP4ShelveFile) o);
            } else if (o instanceof IP4SubmittedFile) {
                decorated = decorateSubmittedFile(text, (IP4SubmittedFile) o);
            } else if (o instanceof IAdaptable) {
                IP4File file = P4CoreUtils.convert(o, IP4File.class);
                if (file != null) {
                    decorated = getFileText(text, file);
                }
            }
            if (decorated != null) {
                return decorated;
            }
        }
        return null;
    }

    /**
     * Get decoration text for projects
     * 
     * @param text
     *            the text to decorate
     * @param project
     *            - project to decorate
     * @return the decorated text
     */
    protected String getProjectText(String text, IProject project) {
        IP4Connection connection = P4ConnectionManager.getManager()
                .getConnection(project, false);
        if (connection != null) {
            text = decorateProjectWithConnection(text, connection);
        }
        return text;
    }

    /**
     * Get the decorated file text
     * 
     * @param text
     * @param file
     * @return - decorated file text
     */
    public String getFileText(String text, IP4File file) {
        return getFileText(text, file, false);
    }

    /**
     * Get the decorated file text
     * 
     * @param text
     * @param file
     * @param ignored
     * @return - decorated file text
     */
    public String getFileText(String text, IP4File file, boolean ignored) {
        return decorateText(file, text, ignored);
    }

    /**
     * Get the decorated file text
     * 
     * @param text
     * @param file
     * @param showChangelist
     * @param showIncoming
     * @return - decorated file text
     */
    public String getFileText(String text, IP4File file,
            boolean showChangelist, boolean showIncoming) {
        return decorateText(file, text, showChangelist, showIncoming, false);
    }

    /**
     * Fire event to say that resource labels need to be updated
     * 
     * @param event
     *            the event to broadcast
     */
    private void postLabelEvent(final LabelProviderChangedEvent event) {
        PerforceUIPlugin.asyncExec(new Runnable() {

            public void run() {
                fireLabelProviderChanged(event);
            }
        });
    }

    /**
     * Append text to buffer, put in seperator if there is already content in
     * the buffer
     * 
     * @param buffer
     *            the string buffer
     * @param text
     *            the text to append
     * @param sep
     *            the seperator to use if we have previous content
     */
    protected void append(StringBuffer buffer, String text, String sep) {
        if (buffer.length() > 0) {
            buffer.append(sep);
        }
        buffer.append(text);
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

    /**
     * Go get the decoration preferences
     */
    private void getPreferences() {
        IPreferenceStore store = PerforceUIPlugin.getPlugin()
                .getPreferenceStore();
        decorateIgnored = store
                .getBoolean(IPerforceUIConstants.PREF_IGNORED_TEXT);
        connectionDecoration = store
                .getString(IPreferenceConstants.CONNECTION_DECORATION_TEXT);
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

    /**
     * @see com.perforce.team.core.p4java.IP4Listener#resoureChanged(com.perforce.team.core.p4java.P4Event)
     */
    public void resoureChanged(final P4Event event) {
		Tracing.printExecTime(Policy.DEBUG, PerforceDecorator.this.getClass().getSimpleName()+":resourceChanged()", event.toString(), new Runnable(){//$NON-NLS-1$
			
			public void run() {
				if (resourceDecorator) {
					postLabelEvent(new LabelProviderChangedEvent(PerforceDecorator.this,
							event.getLocalResources()));
				} else {
					postLabelEvent(new LabelProviderChangedEvent(PerforceDecorator.this,
							event.getResources()));
				}
			}
		});
    }

	public String getName() {
		return getClass().getSimpleName();
	}
}
