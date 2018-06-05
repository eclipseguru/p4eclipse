/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.text.timelapse;

import com.perforce.p4java.core.file.FileAction;
import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.decorator.OverlayIcon;
import com.perforce.team.ui.text.timelapse.INodeModel.ChangeType;
import com.perforce.team.ui.text.timelapse.INodeModel.IRecord;
import com.perforce.team.ui.timelapse.ActionTickDecorator;
import com.perforce.team.ui.timelapse.ITickDecorator;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public abstract class NodeTickDecorator implements ITickDecorator {

    /**
     * IMAGE_SIZE
     */
    public static final int IMAGE_SIZE = 16;

    private Map<IP4Revision, ChangeType> types = new HashMap<IP4Revision, ChangeType>();
    private Map<OverlayIcon, Image> decorations = new HashMap<OverlayIcon, Image>();
    private Image lastBase;
    private Image blankImage;

    private INodeModel model;
    private String nodeId;

    /**
     * Action tick decorator
     */
    protected ITickDecorator actionDecorator;

    /**
     * Create a node tick decorator
     * 
     * @param model
     */
    public NodeTickDecorator(INodeModel model) {
        this(model, null);
        actionDecorator = createActionTickDecorator();
    }

    /**
     * Create the action tick decorator to use in this node decorator
     * 
     * @return non-null action tick decorator
     */
    protected ITickDecorator createActionTickDecorator() {
        return new ActionTickDecorator();
    }

    /**
     * Create a node tick decorator
     * 
     * @param model
     * @param nodeId
     */
    public NodeTickDecorator(INodeModel model, String nodeId) {
        this.model = model;
        setNodeId(nodeId);
    }

    /**
     * @return - node id
     */
    public String getNodeId() {
        return this.nodeId;
    }

    /**
     * Set the node id to decorator ticks for
     * 
     * @param id
     */
    public void setNodeId(String id) {
        this.nodeId = id;
        this.lastBase = null;
        this.types.clear();
        if (this.nodeId != null && this.model != null) {
            IRecord[] records = this.model.getNodeRecords(nodeId);
            for (IRecord record : records) {
                this.types.put(record.getRevision(), record.getType());
            }
        }
    }

    /**
     * Set the node model
     * 
     * @param model
     */
    public void setModel(INodeModel model) {
        this.model = model;
    }

    /**
     * Get base image for a specified node
     * 
     * @param node
     * @return - image or null to not decorate
     */
    protected abstract Image getBaseImage(Object node);

    /**
     * Is the revision modified by the connection owner
     * 
     * @param revision
     * @return true if modified owner, false otherwise
     */
    protected boolean isModifiedByOwner(IP4Revision revision) {
        return revision.isModifiedByOwner();
    }

    private ImageDescriptor getFileDescriptor(IP4Revision revision) {
        String id = null;
        FileAction action = revision.getAction();
        if (action != null) {
            boolean owned = isModifiedByOwner(revision);
            switch (action) {
            case ADD:
            case MOVE_ADD:
                if (owned) {
                    id = IPerforceUIConstants.IMG_DEC_ADD;
                } else {
                    id = IPerforceUIConstants.IMG_DEC_ADD_OTHER;
                }
                break;
            case BRANCH:
                if (owned) {
                    id = IPerforceUIConstants.IMG_DEC_BRANCH;
                } else {
                    id = IPerforceUIConstants.IMG_DEC_BRANCH_OTHER;
                }
                break;
            case EDIT:
                if (owned) {
                    id = IPerforceUIConstants.IMG_DEC_EDIT;
                } else {
                    id = IPerforceUIConstants.IMG_DEC_EDIT_OTHER;
                }
                break;
            case INTEGRATE:
                if (owned) {
                    id = IPerforceUIConstants.IMG_DEC_INTEGRATE;
                } else {
                    id = IPerforceUIConstants.IMG_DEC_INTEGRATE_OTHER;
                }
                break;
            case DELETE:
            case MOVE_DELETE:
                if (owned) {
                    id = IPerforceUIConstants.IMG_DEC_DELETE;
                } else {
                    id = IPerforceUIConstants.IMG_DEC_DELETE_OTHER;
                }
                break;
            default:
                break;
            }
        }
        return id != null ? PerforceUIPlugin.getDescriptor(id) : null;
    }

    /**
     * Get decoration for a change type
     * 
     * @param type
     * @param revision
     * @return - image descriptor of decoration
     */
    protected ImageDescriptor[] getDecoration(ChangeType type,
            IP4Revision revision) {
        ImageDescriptor[] descriptor = new ImageDescriptor[2];
        descriptor[0] = getFileDescriptor(revision);
        switch (type) {
        case ADD:
            descriptor[1] = PerforceUIPlugin
                    .getDescriptor(IPerforceUIConstants.IMG_DEC_ADD_FUNC);
            break;
        case DELETE:
            descriptor[1] = PerforceUIPlugin
                    .getDescriptor(IPerforceUIConstants.IMG_DEC_DELETE_FUNC);
            break;
        case EDIT:
            descriptor[1] = PerforceUIPlugin
                    .getDescriptor(IPerforceUIConstants.IMG_DEC_EDIT_FUNC);
            break;
        default:
            return null;
        }
        return descriptor[0] != null && descriptor[1] != null
                ? descriptor
                : null;
    }

    /**
     * @see com.perforce.team.ui.timelapse.ITickDecorator#decorate(com.perforce.team.core.p4java.IP4Revision,
     *      int, int, org.eclipse.swt.graphics.GC)
     */
    public void decorate(IP4Revision revision, int x, int y, GC gc) {
        if (this.nodeId != null && model != null) {
            ChangeType type = this.types.get(revision);
            if (type != null && type != ChangeType.UNCHANGED) {
                if (ChangeType.DELETE != type) {
                    Object node = model.getNode(this.nodeId, revision);
                    if (node != null) {
                        Image base = getBaseImage(node);
                        if (base != null) {
                            lastBase = base;
                        }
                        ImageDescriptor[] decorators = getDecoration(type,
                                revision);
                        if (base != null && decorators != null) {
                            OverlayIcon icon = new OverlayIcon(
                                    base,
                                    decorators,
                                    new int[] {
                                            IPerforceUIConstants.ICON_BOTTOM_LEFT,
                                            IPerforceUIConstants.ICON_TOP_LEFT, });
                            Image full = this.decorations.get(icon);
                            if (full == null) {
                                full = icon.createImage();
                                this.decorations.put(icon, full);
                            }
                            gc.drawImage(full, x - full.getBounds().width / 2,
                                    y);
                        }
                    }
                } else {
                    if (blankImage == null) {
                        ImageData data = new ImageData(IMAGE_SIZE, IMAGE_SIZE,
                                1, new PaletteData(new RGB[] { gc
                                        .getBackground().getRGB(), }));
                        blankImage = new Image(gc.getDevice(), data);
                    }
                    Image base = this.lastBase;
                    if (base == null) {
                        base = blankImage;
                    }
                    ImageDescriptor[] decorators = getDecoration(type, revision);
                    if (decorators != null) {

                        OverlayIcon icon = new OverlayIcon(base, decorators,
                                new int[] {
                                        IPerforceUIConstants.ICON_BOTTOM_LEFT,
                                        IPerforceUIConstants.ICON_TOP_LEFT, });
                        Image full = this.decorations.get(icon);
                        if (full == null) {
                            full = icon.createImage();
                            this.decorations.put(icon, full);
                        }
                        gc.drawImage(full, x - full.getBounds().width / 2, y);
                    }
                }
            }
        } else {
            actionDecorator.decorate(revision, x, y, gc);
        }
    }

    /**
     * @see com.perforce.team.ui.timelapse.ITickDecorator#dispose()
     */
    public void dispose() {
        actionDecorator.dispose();
        for (Image image : this.decorations.values()) {
            image.dispose();
        }
        if (blankImage != null) {
            blankImage.dispose();
        }
    }

}
