package com.perforce.team.ui.decorator;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.ui.ISharedImages;
import org.eclipse.team.ui.TeamImages;

import com.perforce.p4java.core.file.FileAction;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4ShelveFile;
import com.perforce.team.core.p4java.IP4SubmittedFile;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.PerforceUIPlugin;

/**
 * Icon cache class
 */
public class IconCache {

    private Map<OverlayIcon, Image> cache = Collections
            .synchronizedMap(new HashMap<OverlayIcon, Image>());

    private Image unmanagedBaseEclipse = null;
    private Image shelveFileBaseImage = null;

    /**
     * Open decorator location
     */
    protected int openLocation;

    /**
     * Sync decorator location
     */
    protected int syncLocation;

    /**
     * Not synced decorator location
     */
    protected int notSyncLocation;

    /**
     * Unresolved decorator location
     */
    protected int unresolvedLocation;

    /**
     * Lock decorator location
     */
    protected int lockLocation;

    /**
     * Open on other client decorator location
     */
    protected int otherLocation;

    /**
     * Project decorator location
     */
    protected int projectLocation;

    /**
     * Ignored decorator location
     */
    protected int ignoredLocation;

    /**
     * Local only decorator location
     */
    protected int localOnlyLocation;

    /**
     * Stream or sandbox decorator location on connection
     */
    protected int streamAndSandboxLocation;

    /**
     * Stream or sandbox decorator location on project
     */
    protected int streamAndSandboxProjectLocation;

    /**
     * Show custom icon for files outside of eclipse workspace
     */
    protected boolean markUnmanaged;

    /**
     * Icon cache
     */
    public IconCache() {
        clear();
    }

    /**
     * Get action descriptor for a non-null p4j file action and whether the
     * action was done by the current user
     * 
     * @param action
     * @param openedByOwner
     * @return - image descriptor
     */
    public ImageDescriptor getActionDescriptor(FileAction action,
            boolean openedByOwner) {
        ImageDescriptor descriptor = null;
		if (action != null)
			switch (action) {
			case ADD:
			case MOVE_ADD:
				if (openedByOwner) {
					descriptor = getAddImage();
				} else {
					descriptor = getAddOtherImage();
				}
				break;
			case BRANCH:
				if (openedByOwner) {
					descriptor = getBranchImage();
				} else {
					descriptor = getBranchOtherImage();
				}
				break;
			case INTEGRATE:
				if (openedByOwner) {
					descriptor = getIntegrateImage();
				} else {
					descriptor = getIntegrateOtherImage();
				}
				break;
			case DELETE:
			case MOVE_DELETE:
				if (openedByOwner) {
					descriptor = getDeleteImage();
				} else {
					descriptor = getDeleteOtherImage();
				}
				break;
			default:
				break;
			}
        if (descriptor == null) {
            if (openedByOwner) {
                descriptor = getEditImage();
            } else {
                descriptor = getEditOtherImage();
            }
        }
        return descriptor;
    }

    /**
     * Dispose the icon cache. This should only be called when the images
     * produced by this icon cache are no longer needed.
     */
    public void dispose() {
        if (unmanagedBaseEclipse != null && !unmanagedBaseEclipse.isDisposed()) {
            unmanagedBaseEclipse.dispose();
        }
        if (shelveFileBaseImage != null && !shelveFileBaseImage.isDisposed()) {
            shelveFileBaseImage.dispose();
        }
        for (Image image : cache.values()) {
            if (!image.isDisposed()) {
                image.dispose();
            }
        }
    }

    /**
     * Clears the cache and updates the preferences for icon locations
     */
    public void clear() {
        cache.clear();

        IPreferenceStore store = PerforceUIPlugin.getPlugin()
                .getPreferenceStore();
        openLocation = store.getInt(IPerforceUIConstants.PREF_FILE_OPEN_ICON);
        notSyncLocation = store
                .getInt(IPerforceUIConstants.PREF_FILE_SYNC_ICON);
        syncLocation = store.getInt(IPerforceUIConstants.PREF_FILE_SYNC2_ICON);
        unresolvedLocation = store
                .getInt(IPerforceUIConstants.PREF_FILE_UNRESOLVED_ICON);
        lockLocation = store.getInt(IPerforceUIConstants.PREF_FILE_LOCK_ICON);
        otherLocation = store.getInt(IPerforceUIConstants.PREF_FILE_OTHER_ICON);
        markUnmanaged = store
                .getBoolean(IPerforceUIConstants.PREF_MARK_UNMANAGED_FILES);
        projectLocation = store.getInt(IPerforceUIConstants.PREF_PROJECT_ICON);
        ignoredLocation = store.getInt(IPerforceUIConstants.PREF_IGNORED_ICON);
        localOnlyLocation = store
                .getInt(IPerforceUIConstants.PREF_LOCAL_ONLY_ICON);
        streamAndSandboxLocation = store
                .getInt(IPerforceUIConstants.PREF_STREAM_SANDBOX_ICON);
        streamAndSandboxProjectLocation = store
                .getInt(IPerforceUIConstants.PREF_STREAM_SANDBOX_PROJECT_ICON);
    }

    /**
     * Get the project image for this project. This will be the core eclipse
     * shared icon on the bottom right.
     * 
     * @param base
     * @param project
     * @return - decorated icon if project is a perforce shared project
     */
    public Image getProjectImage(Image base, IProject project) {
        Image decorated = base;
        IP4Connection connection = P4ConnectionManager.getManager()
                .getConnection(project, false);
        if (connection != null) {
            decorated = decorateProjectImageBasedonConnection(base, connection);
        }
        return decorated;
    }

    /**
     * Get the connection image for this connection.
     * 
     * @param base
     * @param connection
     * @return - decorated icon with connection online/offline status
     */
    public Image decorateProjectImageBasedonConnection(Image base, IP4Connection connection) {
        Image decorated = base;
        if (connection != null) {
            ImageDescriptor[] descriptors = new ImageDescriptor[1];
            if (connection.isOffline()) {
                descriptors[0] = getProjectDecorationOffline();
            } else if (getProjectDecorationOnline() != null) {
                descriptors[0] = getProjectDecorationOnline();
            }
            if (descriptors[0] != null) {
                OverlayIcon icon = new OverlayIcon(base, descriptors,
                        new int[] { projectLocation });
                Image image = cache.get(icon);
                if (image == null) {
                    image = icon.createImage();
                    cache.put(icon, image);
                }
                decorated = image;
            }
            
            // try to decorate sandbox and stream projects
            descriptors = new ImageDescriptor[1];
            if (connection.isSandbox()) {
                descriptors[0] = getSandboxDecoration();
            } else if (!connection.isOffline()) {
            	if(connection.getClient()!=null && !StringUtils.isEmpty(connection.getClient().getStream())){
            		descriptors[0] = getStreamDecoration();
            	}
            }
            if (descriptors[0] != null) {
                OverlayIcon icon = new OverlayIcon(base, descriptors,
                        new int[] { streamAndSandboxProjectLocation });
                Image image = cache.get(icon);
                if (image == null) {
                    image = icon.createImage();
                    cache.put(icon, image);
                }
                decorated = image;
            }

        }
        return decorated;
    }

    /**
     * Get ignored image
     * 
     * @param base
     * @return - ignored image
     */
    public Image getIgnoredImage(Image base) {
        Image decorated = base;
        if (ignoredLocation > 0) {
            ImageDescriptor[] descriptors = new ImageDescriptor[] { getIgnoredDecoration() };
            OverlayIcon icon = new OverlayIcon(base, descriptors,
                    new int[] { ignoredLocation });
            Image image = cache.get(icon);
            if (image == null) {
                image = icon.createImage();
                cache.put(icon, image);
            }
            decorated = image;
        }
        return decorated;
    }

    /**
     * Get decoration pending changelist image
     * 
     * @param base
     * @param list
     * @return - decorated image
     */
    public Image getImage(Image base, IP4PendingChangelist list) {
        if (list != null && list.isShelved()) {
            Image decorated = base;
            ImageDescriptor[] descriptors = new ImageDescriptor[] { getShelvedDecoration() };
            OverlayIcon icon = new OverlayIcon(base, descriptors,
                    new int[] { IPerforceUIConstants.ICON_TOP_LEFT });
            Image image = cache.get(icon);
            if (image == null) {
                image = icon.createImage();
                cache.put(icon, image);
            }
            decorated = image;
            return decorated;
        }
        return base;
    }

    public Image getImage(Image base, IP4Connection conn) {
    	// decorate Sandbox (and stream) or Stream only connection on the bottom left
        if (conn != null){
        	Image decorated = base;
        	OverlayIcon icon = null;
        	if(conn.isSandbox()) { // sandbox always a stream connection, so we only decorate it as sandbox
	            ImageDescriptor[] descriptors = new ImageDescriptor[] { getSandboxDecoration() };
	            icon = new OverlayIcon(decorated, descriptors,
	                    new int[] { streamAndSandboxLocation });
	        	Image image = cache.get(icon);
	        	if (image == null) {
	        		image = icon.createImage();
	        		cache.put(icon, image);
	        	}
	        	decorated = image;
        	}else if(!conn.isOffline() && conn.getClient()!=null && !StringUtils.isEmpty(conn.getClient().getStream())){
	            ImageDescriptor[] descriptors = new ImageDescriptor[] { getStreamDecoration() };
	            icon = new OverlayIcon(decorated, descriptors,
	                    new int[] { streamAndSandboxLocation });
	            Image image = cache.get(icon);
	            if (image == null) {
	            	image = icon.createImage();
	            	cache.put(icon, image);
	            }
	            decorated = image;
        	}

        	return decorated;
        }
        return base;
    }
    
    /**
     * Get image for a shelved file
     * 
     * @param file
     * @return - image
     */
    public Image getImage(IP4ShelveFile file) {
        Image image = null;
        if (file != null) {
            IP4File p4File = file.getFile();
            if (p4File != null && p4File.getAction() != null) {
                ImageDescriptor descriptor = null;
                switch (p4File.getAction()) {
                case ADD:
                    descriptor = PerforceUIPlugin
                            .getDescriptor(IPerforceUIConstants.IMG_SHELVE_FILE_ADD);
                    break;
                case MOVE_ADD:
                    descriptor = PerforceUIPlugin
                            .getDescriptor(IPerforceUIConstants.IMG_SHELVE_FILE_MOVEADD);
                    break;
                case BRANCH:
                    descriptor = PerforceUIPlugin
                            .getDescriptor(IPerforceUIConstants.IMG_SHELVE_FILE_BRANCH);
                    break;
                case INTEGRATE:
                    descriptor = PerforceUIPlugin
                            .getDescriptor(IPerforceUIConstants.IMG_SHELVE_FILE_INTEGRATE);
                    break;
                case DELETE:
                    descriptor = PerforceUIPlugin
                            .getDescriptor(IPerforceUIConstants.IMG_SHELVE_FILE_DELETE);
                    break;
                case MOVE_DELETE:
                    descriptor = PerforceUIPlugin
                            .getDescriptor(IPerforceUIConstants.IMG_SHELVE_FILE_MOVEDELETE);
                    break;
                case EDIT:
                    descriptor = PerforceUIPlugin
                            .getDescriptor(IPerforceUIConstants.IMG_SHELVE_FILE_EDIT);
                    break;
                default:
                    break;
                }
                if (descriptor != null) {
                    OverlayIcon icon = new OverlayIcon(getShelveFileBaseImage(),
                            new ImageDescriptor[] { descriptor },
                            new int[] { IPerforceUIConstants.ICON_TOP_LEFT });
                    image = this.cache.get(icon);
                    if (image == null) {
                        image = icon.createImage();
                        this.cache.put(icon, image);
                    }
                }
            }
        }
        return image;
    }

    /**
     * Get image
     * 
     * @param base
     * @param file
     * @return - image
     */
    public Image getImage(Image base, IP4SubmittedFile file) {
        return base;
    }

    /**
     * Get image
     * 
     * @param base
     * @param file
     * @param managedFile
     * @param ignored
     * @return - image
     */
    public Image getImage(Image base, IP4File file, boolean managedFile,
            boolean ignored) {
        if (file != null) {
            OverlayIcon icon;
            if (!managedFile && markUnmanaged) {
                base = getUnmanagedBaseEclipse();
            }
            icon = getOtherFileIcon(base, file, ignored);
            if (icon != null) {
                Image image = cache.get(icon);
                if (image == null) {
                    image = icon.createImage();
                    cache.put(icon, image);
                }
                return image;
            }
        }
        return base;
    }

    /**
     * Get image
     * 
     * @param base
     * @param managedFile
     * @return - image
     */
    public Image getImage(Image base, boolean managedFile) {
        if (markUnmanaged && !managedFile) {
            return this.getUnmanagedBaseEclipse();
        } else {
            return base;
        }
    }

    private FileAction getAction(String action) {
        FileAction fileAction = null;
        try {
            fileAction = FileAction.fromString(action);
        } catch (Throwable e) {
            fileAction = null;
        }
        return fileAction;
    }

    private OverlayIcon getOtherFileIcon(Image base, IP4File p4File,
            boolean ignored) {
        List<ImageDescriptor> overlays = new ArrayList<ImageDescriptor>();
        int[] locations = new int[6];
        if (ignoredLocation > 0 && ignored) {
            overlays.add(getIgnoredDecoration());
            locations[overlays.size() - 1] = ignoredLocation;
        }
        if (p4File.getP4JFile() != null) {
            if (openLocation > 0 && p4File.isOpened() && p4File.openedByOwner()) {
                overlays.add(getActionDescriptor(p4File.getAction(), true));
                locations[overlays.size() - 1] = openLocation;
            }

            if (otherLocation > 0) {
                if (p4File.openedElsewhere()) {
                    List<String> actions = p4File.getOtherActions();
                    if (actions.size() > 0) {
                        String action = actions.get(0);
                        overlays.add(getActionDescriptor(getAction(action),
                                false));
                        locations[overlays.size() - 1] = otherLocation;
                    }
                } else if (p4File.isOpened() && p4File.openedByOtherOwner()) {
                    overlays.add(getActionDescriptor(p4File.getAction(), false));
                    locations[overlays.size() - 1] = otherLocation;
                }
            }
            // // Only show sync image if path is in client view and is not
            // deleted
            if ((syncLocation > 0 || notSyncLocation > 0)
                    && !(p4File.getHaveRevision() <= 0 && p4File
                            .getHeadAction() == FileAction.DELETE)
                    && p4File.getHeadRevision() > 0
                    && p4File.getClientPath() != null) {
                if (p4File.isSynced()) {
                    if (syncLocation > 0) {
                        overlays.add(getSyncImage());
                        locations[overlays.size() - 1] = syncLocation;
                    }
                } else {
                    if (notSyncLocation > 0) {
                        overlays.add(getNotsyncImage());
                        locations[overlays.size() - 1] = notSyncLocation;
                    }
                }
            }
            if (lockLocation > 0 && p4File.isLocked()) {
                overlays.add(getLockImage());
                locations[overlays.size() - 1] = lockLocation;
            }
            if (unresolvedLocation > 0 && p4File.isUnresolved()) {
                overlays.add(getUnresolveImage());
                locations[overlays.size() - 1] = unresolvedLocation;
            }
        } else {
            if (!ignored && localOnlyLocation > 0) {
                overlays.add(getLocalOnlyDecoration());
                locations[overlays.size() - 1] = localOnlyLocation;
            }
        }
        OverlayIcon icon = null;
        if (!overlays.isEmpty()) {
            int[] locations2 = new int[overlays.size()];
            System.arraycopy(locations, 0, locations2, 0, locations2.length);
            ImageDescriptor[] overlays2 = overlays
                    .toArray(new ImageDescriptor[overlays.size()]);
            icon = new OverlayIcon(base, overlays2, locations2);
        }
        return icon;
    }

    private ImageDescriptor createAndGetImageDesc(String key){
    	PerforceUIPlugin plugin = PerforceUIPlugin.getPlugin();	
    	return plugin.getImageDescriptor(key);
    }
    
	private ImageDescriptor getEditImage() {
		return createAndGetImageDesc(IPerforceUIConstants.IMG_DEC_EDIT);
	}

	private ImageDescriptor getAddImage() {
		return createAndGetImageDesc(IPerforceUIConstants.IMG_DEC_ADD);
	}

	private ImageDescriptor getDeleteImage() {
		return createAndGetImageDesc(IPerforceUIConstants.IMG_DEC_DELETE);
	}

	private ImageDescriptor getSyncImage() {
		return createAndGetImageDesc(IPerforceUIConstants.IMG_DEC_SYNC);
	}

	private ImageDescriptor getNotsyncImage() {
		return createAndGetImageDesc(IPerforceUIConstants.IMG_DEC_NOTSYNC);
	}

	private ImageDescriptor getLockImage() {
		return createAndGetImageDesc(IPerforceUIConstants.IMG_DEC_LOCK);
	}

	private ImageDescriptor getUnresolveImage() {
		return createAndGetImageDesc(IPerforceUIConstants.IMG_DEC_UNRESOLVED);
	}

	private ImageDescriptor getEditOtherImage() {
		return createAndGetImageDesc(IPerforceUIConstants.IMG_DEC_EDIT_OTHER);
	}

	private ImageDescriptor getAddOtherImage() {
		return createAndGetImageDesc(IPerforceUIConstants.IMG_DEC_ADD_OTHER);
	}

	private ImageDescriptor getDeleteOtherImage() {
		return createAndGetImageDesc(IPerforceUIConstants.IMG_DEC_DELETE_OTHER);
	}

	private ImageDescriptor getBranchImage() {
		return createAndGetImageDesc(IPerforceUIConstants.IMG_DEC_BRANCH);
	}

	private ImageDescriptor getBranchOtherImage() {
		return createAndGetImageDesc(IPerforceUIConstants.IMG_DEC_BRANCH_OTHER);
	}

	private ImageDescriptor getIntegrateImage() {
		return createAndGetImageDesc(IPerforceUIConstants.IMG_DEC_INTEGRATE);
	}

	private ImageDescriptor getIntegrateOtherImage() {
		return createAndGetImageDesc(IPerforceUIConstants.IMG_DEC_INTEGRATE_OTHER);
	}

	private ImageDescriptor getUnmanagedFileEclipseImage() {
		return createAndGetImageDesc(IPerforceUIConstants.IMG_UNMANAGED_FILE_ECLIPSE);
	}

	private Image getUnmanagedBaseEclipse() {
		if (this.unmanagedBaseEclipse==null){
			this.unmanagedBaseEclipse=getUnmanagedFileEclipseImage().createImage();
		}
		return unmanagedBaseEclipse;
	}

	private Image getShelveFileBaseImage() {
		if(this.shelveFileBaseImage == null){
			this.shelveFileBaseImage = PerforceUIPlugin.getPlugin().getImageDescriptor(
	                IPerforceUIConstants.IMG_SHELVE_FILE).createImage();
		}
		return shelveFileBaseImage;
	}

	private ImageDescriptor getProjectDecorationOnline() {
		return TeamImages
                .getImageDescriptor(ISharedImages.IMG_CHECKEDIN_OVR);
	}

	private ImageDescriptor getProjectDecorationOffline() {
		return createAndGetImageDesc(IPerforceUIConstants.IMG_DEC_OFFLINE);
	}

	private ImageDescriptor getIgnoredDecoration() {
		return createAndGetImageDesc(IPerforceUIConstants.IMG_DEC_IGNORE);
	}

	private ImageDescriptor getLocalOnlyDecoration() {
		return createAndGetImageDesc(IPerforceUIConstants.IMG_DEC_LOCAL);
	}

	private ImageDescriptor getShelvedDecoration() {
		return createAndGetImageDesc(IPerforceUIConstants.IMG_DEC_SHELVED);
	}
	
	private ImageDescriptor getSandboxDecoration() {
		return createAndGetImageDesc(IPerforceUIConstants.IMG_DEC_SANDBOX);
	}

	private ImageDescriptor getStreamDecoration() {
		return createAndGetImageDesc(IPerforceUIConstants.IMG_DEC_STREAM);
	}
}
