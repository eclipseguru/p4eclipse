/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE.SharedImages;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.perforce.p4java.core.IDepot.DepotType;
import com.perforce.p4java.core.IStreamSummary;
import com.perforce.p4java.core.file.FileAction;
import com.perforce.p4java.core.file.IExtendedFileSpec;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Container;
import com.perforce.team.core.p4java.IP4Folder;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4ShelveFile;
import com.perforce.team.core.p4java.IP4ShelvedChangelist;
import com.perforce.team.core.p4java.IP4Stream;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.core.p4java.IP4SubmittedFile;
import com.perforce.team.core.p4java.P4Depot;
import com.perforce.team.ui.PerforceContentProvider.Loading;
import com.perforce.team.ui.decorator.PerforceDecorator;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class PerforceLabelProvider implements ITableLabelProvider,
        ILabelProvider, IFontProvider, IColorProvider {

    /**
     * Decorating label provider
     */
    protected DecoratingLabelProvider decorator = null;

    /**
     * Decoration setting for showing folders that are imported as project with
     * the project icon
     */
    protected boolean decorateLocalFolders = false;

    /**
     * Image cache
     */
    protected Map<ImageDescriptor, Image> images = new HashMap<ImageDescriptor, Image>();

    /**
     * Creates a new perforce label provider setup to not be a "live" decorator.
     */
    public PerforceLabelProvider() {
        this(false);
    }

    /**
     * Creates a new perforce label provider
     * 
     * @param decorateResources
     */
    public PerforceLabelProvider(boolean decorateResources) {
        this.decorator = new DecoratingLabelProvider(
                new WorkbenchLabelProvider(), new PerforceDecorator(
                        decorateResources){
                    @Override
                    public String getName() {
                    	return PerforceLabelProvider.class.getSimpleName()+"3";
                    }
                });
    }

    /**
     * Create a new perforce label provider
     * 
     * @param decorateResources
     * @param decorateTextLabels
     * @param decorateImageLabels
     */
    public PerforceLabelProvider(final boolean decorateResources,
            boolean decorateTextLabels, boolean decorateImageLabels) {
        if (decorateTextLabels && decorateImageLabels) {
            this.decorator = new DecoratingLabelProvider(
                    new WorkbenchLabelProvider(), new PerforceDecorator(
                            decorateResources){
                    	public String getName() {
                    		return PerforceLabelProvider.class.getSimpleName()+"1";
                    	}
                    });
        } else if (decorateImageLabels) {
            this.decorator = new DecoratingLabelProvider(
                    new WorkbenchLabelProvider(), new LabelDecoratorAdapter() {

                        private PerforceDecorator decorator = new PerforceDecorator(
                                decorateResources){
                        	public String getName() {
                        		return PerforceLabelProvider.class.getSimpleName()+"2";
                        	}
                        };

                        @Override
                        public Image decorateImage(Image image, Object element) {
                            return decorator.decorateImage(image, element);
                        }

                        @Override
                        public void dispose() {
                            decorator.dispose();
                            super.dispose();
                        }

                    });
        } else if (decorateTextLabels) {
            this.decorator = new DecoratingLabelProvider(
                    new WorkbenchLabelProvider(), new PerforceDecorator(
                            decorateResources) {

                        @Override
                        public Image decorateImage(Image image, Object element) {
                            return image;
                        }
                        
                        @Override
                        public String getName() {
                        	return PerforceLabelProvider.class.getSimpleName()+"4";
                        }

                    });
        } else {
            this.decorator = new DecoratingLabelProvider(
                    new WorkbenchLabelProvider(), new LabelDecoratorAdapter() {

                        @Override
                        public Image decorateImage(Image image, Object element) {
                            return image;
                        }

                    });
        }
    }

    /**
     * Set decorate project folders as eclipse projects
     * 
     * @param decorate
     */
    public void setDecorateLocalFolders(boolean decorate) {
        this.decorateLocalFolders = decorate;
    }

    private ImageDescriptor getConnectionImage(IP4Connection connection) {
        if (!connection.isOffline()) {
            return PerforceUIPlugin
                    .getDescriptor(IPerforceUIConstants.IMG_DEPOT_CONNECTION);
        } else {
            return PerforceUIPlugin
                    .getDescriptor(IPerforceUIConstants.IMG_DEPOT_CONNECTION_OFFLINE);
        }
    }

    private ImageDescriptor getSubmittedFileImage(IP4SubmittedFile file) {
        ImageDescriptor desc = null;
        FileAction action = file.getAction();
        if (action != null) {
            switch (action) {
            case ADD:
            case MOVE_ADD:
                desc = PerforceUIPlugin
                        .getDescriptor(IPerforceUIConstants.IMG_REVISION_ADD);
                break;
            case DELETE:
            case MOVE_DELETE:
                desc = PerforceUIPlugin
                        .getDescriptor(IPerforceUIConstants.IMG_REVISION_DELETE);
                break;
            case EDIT:
                desc = PerforceUIPlugin
                        .getDescriptor(IPerforceUIConstants.IMG_REVISION_EDIT);
                break;
            case INTEGRATE:
                desc = PerforceUIPlugin
                        .getDescriptor(IPerforceUIConstants.IMG_REVISION_INTEGRATE);
                break;
            case BRANCH:
                desc = PerforceUIPlugin
                        .getDescriptor(IPerforceUIConstants.IMG_REVISION_BRANCH);
                break;
            case PURGE:
                desc = PerforceUIPlugin
                        .getDescriptor(IPerforceUIConstants.IMG_REVISION_PURGE);
                break;
            default:
                break;
            }
        }
        return desc;
    }

    private String getShelvedChangelistText(IP4ShelvedChangelist list) {
        StringBuilder changeDescription = new StringBuilder(
                Messages.PerforceLabelProvider_ShelvedFiles);
        if (!list.needsRefresh()) {
            changeDescription.append(" ("); //$NON-NLS-1$
            changeDescription.append(list.members().length);
            changeDescription.append(')');
        }
        return changeDescription.toString();
    }

    private String getSubmittedFileText(IP4SubmittedFile file) {
        IFileSpec spec = file.getFileSpec();
        int rev = 0;
        if (spec != null) {
            rev = spec.getEndRevision();
        }
        return file.getRemotePath() + "#" + rev; //$NON-NLS-1$
    }

    private String getShelvedFileText(IP4ShelveFile file) {
        IFileSpec spec = file.getFile().getP4JFile();
        if (spec instanceof IExtendedFileSpec) {
            int rev = ((IExtendedFileSpec) spec).getHaveRev();
            if (rev >= 0) {
                return file.getRemotePath() + "#" + rev; //$NON-NLS-1$
            }
        }
        return file.getRemotePath();
    }

    private String getChangelistText(IP4Changelist change) {
        StringBuilder changeDescription = new StringBuilder(
                Messages.PerforceLabelProvider_Change);
        int id = change.getId();
        if (id > 0) {
            changeDescription.append(id);
        }
        String description = change.getShortDescription();
        String user = change.getUserName();
        String client = change.getClientName();
        if (user != null && client != null) {
            changeDescription.append(" " + user + "@" + client); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (description.length() > 0) {
            changeDescription.append(" { " + description + " }"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return changeDescription.toString();
    }

    private ImageDescriptor getDepotImage(P4Depot depot) {
        ImageDescriptor descriptor = null;
        DepotType type = depot.getType();
        if (type == null) {
            type = DepotType.LOCAL;
        }
        switch (type) {
        case REMOTE:
            descriptor = PerforceUIPlugin
                    .getDescriptor(IPerforceUIConstants.IMG_DEPOT_REMOTE);
            break;
        case SPEC:
            descriptor = PerforceUIPlugin
                    .getDescriptor(IPerforceUIConstants.IMG_DEPOT_SPEC);
            break;
        case STREAM:
            descriptor = PerforceUIPlugin
                    .getDescriptor(IPerforceUIConstants.IMG_DEPOT_STREAM);
            break;
        default:
            descriptor = PerforceUIPlugin
                    .getDescriptor(IPerforceUIConstants.IMG_DEPOT_DEPOT);
        }
        return descriptor;
    }

    private ImageDescriptor getStreamImage(IP4Stream element) {
        ImageDescriptor descriptor = PerforceUIPlugin
                .getDescriptor(IPerforceUIConstants.IMG_STREAM);
        return descriptor;
    }
    
    /**
     * This base implementation only returns images for a column index of 0
     * 
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
     *      int)
     */
    public Image getColumnImage(Object element, int columnIndex) {
        if (columnIndex == 0 && element != null) {
            ImageDescriptor desc = null;
            Image image = null;
            if (element instanceof Loading) {
                desc = PerforceUIPlugin
                        .getDescriptor(IPerforceUIConstants.IMG_LOADING);
            } else {
                image = decorator.getImage(element);
                if (image == null) {
                    String text = null;
                    if (element instanceof IP4Job) {
                        desc = PerforceUIPlugin
                                .getDescriptor(IPerforceUIConstants.IMG_CHG_JOB);
                    } else if (element instanceof IP4Connection) {
                        desc = getConnectionImage((IP4Connection) element);
                    } else if (element instanceof P4Depot) {
                        desc = getDepotImage((P4Depot) element);
                    } else if (element instanceof IP4Stream) {
                        desc = getStreamImage((IP4Stream) element);
                    } else if (element instanceof IP4SubmittedChangelist) {
                        desc = PerforceUIPlugin
                                .getDescriptor(IPerforceUIConstants.IMG_CHG_SUBMITTED);
                    } else if (element instanceof IP4ShelvedChangelist) {
                        desc = PerforceUIPlugin
                                .getDescriptor(IPerforceUIConstants.IMG_SHELVE);
                    } else if (element instanceof IP4Changelist) {
                        if (((IP4Changelist) element).isReadOnly()) {
                            desc = PerforceUIPlugin
                                    .getDescriptor(IPerforceUIConstants.IMG_CHG_OTHER);
                        } else {
                            desc = PerforceUIPlugin
                                    .getDescriptor(IPerforceUIConstants.IMG_CHG_OUR);
                        }
                    } else if (element instanceof IP4Folder) {
                        if (decorateLocalFolders && desc == null) {
                            IContainer[] locals = ((IP4Folder) element)
                                    .getLocalContainers();
                            if (locals != null && locals.length > 0
                                    && locals[0] instanceof IProject) {
                                return PlatformUI.getWorkbench()
                                        .getSharedImages()
                                        .getImage(SharedImages.IMG_OBJ_PROJECT);
                            }
                        }
                        if (desc == null) {
                            if (isStreamRoot(element))
                                desc = PerforceUIPlugin
                                        .getDescriptor(IPerforceUIConstants.IMG_DEPOT_STREAM_FOLDER);
                            if (desc == null)
                                desc = PerforceUIPlugin
                                        .getDescriptor(IPerforceUIConstants.IMG_DEPOT_FOLDER);
                        }
                    } else if (element instanceof IP4Container) {
                        desc = PerforceUIPlugin
                                .getDescriptor(IPerforceUIConstants.IMG_DEPOT_FOLDER);
                    } else if (element instanceof IP4SubmittedFile) {
                        desc = getSubmittedFileImage((IP4SubmittedFile) element);
                    } else {
                        text = element.toString();
                        int lastSlash = text.lastIndexOf('/');
                        if (lastSlash != -1 && lastSlash + 1 < text.length()) {
                            text = text.substring(lastSlash + 1);
                        }
                        desc = PlatformUI.getWorkbench().getEditorRegistry()
                                .getImageDescriptor(text);
                    }
                }
            }
            if (desc != null) {
                image = images.get(desc);
                if (image == null) {
                    image = desc.createImage();
                    images.put(desc, image);
                }
                image = decorator.getLabelDecorator().decorateImage(image,
                        element);
            }
            return image;
        }
        return null;
    }

    private boolean isStreamRoot(Object element) {
        if (!(element instanceof IP4Folder))
            return false;
        final IP4Folder folder = (IP4Folder) element;
        if (!(folder.getParent() instanceof P4Depot))
            return false;
        final P4Depot depot = (P4Depot) folder.getParent();
        if (depot.getType() != DepotType.STREAM)
            return false;
        for (IStreamSummary stream : depot.getStreams()) {
            if (stream.getStream().equals(folder.getRemotePath()))
                return true;
        }
        return false;
    }

    /**
     * Get decorated text
     * 
     * @param text
     * @param element
     * @return - text
     */
    protected String getDecoratedText(String text, Object element) {
        String decorated = decorator.getLabelDecorator().decorateText(text,
                element);
        if (decorated == null) {
            decorated = text;
        }
        return decorated;
    }

    /**
     * This base implementation only returns text for a column index of 0
     * 
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
     *      int)
     */
    public String getColumnText(Object element, int columnIndex) {
        if (element == null) {
            return null;
        }
        if (columnIndex == 0) {
            String text = null;
            if (element instanceof Loading) {
                text = Messages.PerforceLabelProvider_Loading;
            } else if (element instanceof IP4ShelvedChangelist) {
                return getShelvedChangelistText((IP4ShelvedChangelist) element);
            } else if (element instanceof IP4Changelist
                    && !((IP4Changelist) element).isDefault()) {
                text = getChangelistText((IP4Changelist) element);
                text = getDecoratedText(text, element);
            } else if (element instanceof IP4Job) {
                text = ((IP4Job) element).getId();
            } else if (element instanceof IP4SubmittedFile) {
                text = getSubmittedFileText((IP4SubmittedFile) element);
            } else if (element instanceof IP4ShelveFile) {
                text = getShelvedFileText((IP4ShelveFile) element);
            } else if (element instanceof IP4Resource) {
                text = ((IP4Resource) element).getName();
                text = getDecoratedText(text, element);
            }
            if (text == null) {
                text = decorator.getText(element);
            }
            if (text == null || text.length() == 0) {
                text = element.toString();
            }
            return text;
        }
        return element.toString();
    }

    /**
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    public void addListener(ILabelProviderListener listener) {
        if (decorator != null) {
            decorator.addListener(listener);
        }
    }

    /**
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
     */
    public void dispose() {
        if (decorator != null) {
            decorator.dispose();
        }
        for (Image img : images.values()) {
            if (img != null && !img.isDisposed()) {
                img.dispose();
            }
        }
        images.clear();
    }

    /**
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object,
     *      java.lang.String)
     */
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    /**
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    public void removeListener(ILabelProviderListener listener) {
        if (decorator != null) {
            decorator.removeListener(listener);
        }
    }

    /**
     * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
     */
    public Image getImage(Object element) {
        return getColumnImage(element, 0);
    }

    /**
     * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
     */
    public String getText(Object element) {
        return getColumnText(element, 0);
    }

    /**
     * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
     */
    public Font getFont(Object element) {
        if (element instanceof IP4PendingChangelist) {
            IP4PendingChangelist list = (IP4PendingChangelist) element;
            if (list.isActive()) {
                return JFaceResources.getFontRegistry().getBold(
                        JFaceResources.DEFAULT_FONT);
            }
        }
        return null;
    }

    public Color getForeground(Object element) {
        return null;
    }

    public Color getBackground(Object element) {
        return null;
    }
}
