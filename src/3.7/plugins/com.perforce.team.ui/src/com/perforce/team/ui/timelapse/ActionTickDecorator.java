/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.timelapse;

import com.perforce.p4java.core.file.FileAction;
import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class ActionTickDecorator implements ITickDecorator {

    private Image addImage;
    private int addXOffset;
    private Image editImage;
    private int editXOffset;
    private Image deleteImage;
    private int deleteXOffset;
    private Image branchImage;
    private int branchXOffset;
    private Image integrateImage;
    private int integrateXOffset;

    private Image addOtherImage;
    private int addOtherXOffset;
    private Image editOtherImage;
    private int editOtherXOffset;
    private Image deleteOtherImage;
    private int deleteOtherXOffset;
    private Image branchOtherImage;
    private int branchOtherXOffset;
    private Image integrateOtherImage;
    private int integrateOtherXOffset;

    /**
     * Create a file action tick decorator
     */
    public ActionTickDecorator() {
        PerforceUIPlugin plugin = PerforceUIPlugin.getPlugin();

        // Current user file action icons
        addImage = plugin.getImageDescriptor(IPerforceUIConstants.IMG_DEC_ADD)
                .createImage();
        addXOffset = addImage.getBounds().width / 2;

        editImage = plugin
                .getImageDescriptor(IPerforceUIConstants.IMG_DEC_EDIT)
                .createImage();
        editXOffset = editImage.getBounds().width / 2;

        deleteImage = plugin.getImageDescriptor(
                IPerforceUIConstants.IMG_DEC_DELETE).createImage();
        deleteXOffset = deleteImage.getBounds().width / 2;

        branchImage = plugin.getImageDescriptor(
                IPerforceUIConstants.IMG_DEC_BRANCH).createImage();
        branchXOffset = branchImage.getBounds().width / 2;

        integrateImage = plugin.getImageDescriptor(
                IPerforceUIConstants.IMG_DEC_INTEGRATE).createImage();
        integrateXOffset = integrateImage.getBounds().width / 2;

        // Other user file action icons
        addOtherImage = plugin.getImageDescriptor(
                IPerforceUIConstants.IMG_DEC_ADD_OTHER).createImage();
        addOtherXOffset = addOtherImage.getBounds().width / 2;

        editOtherImage = plugin.getImageDescriptor(
                IPerforceUIConstants.IMG_DEC_EDIT_OTHER).createImage();
        editOtherXOffset = editOtherImage.getBounds().width / 2;

        deleteOtherImage = plugin.getImageDescriptor(
                IPerforceUIConstants.IMG_DEC_DELETE_OTHER).createImage();
        deleteOtherXOffset = deleteOtherImage.getBounds().width / 2;

        branchOtherImage = plugin.getImageDescriptor(
                IPerforceUIConstants.IMG_DEC_BRANCH_OTHER).createImage();
        branchOtherXOffset = branchOtherImage.getBounds().width / 2;

        integrateOtherImage = plugin.getImageDescriptor(
                IPerforceUIConstants.IMG_DEC_INTEGRATE_OTHER).createImage();
        integrateOtherXOffset = integrateOtherImage.getBounds().width / 2;
    }

    /**
     * Get the file action for a revision
     * 
     * @param revision
     * @return - file action
     */
    protected FileAction getAction(IP4Revision revision) {
        return revision.getAction();
    }

    /**
     * Is the specified revision modified by the connection owner
     * 
     * @param revision
     * @return true if modified by owner, false otherwise
     */
    protected boolean isModifiedByOwner(IP4Revision revision) {
        return revision.isModifiedByOwner();
    }

    /**
     * @see com.perforce.team.ui.timelapse.ITickDecorator#decorate(com.perforce.team.core.p4java.IP4Revision,
     *      int, int, org.eclipse.swt.graphics.GC)
     */
    public void decorate(IP4Revision revision, int x, int y, GC gc) {
        FileAction action = getAction(revision);
        if (action != null) {
            boolean owned = isModifiedByOwner(revision);
            switch (action) {
            case ADD:
            case MOVE_ADD:
                if (owned) {
                    gc.drawImage(addImage, x - addXOffset, y);
                } else {
                    gc.drawImage(addOtherImage, x - addOtherXOffset, y);
                }
                break;
            case BRANCH:
                if (owned) {
                    gc.drawImage(branchImage, x - branchXOffset, y);
                } else {
                    gc.drawImage(branchOtherImage, x - branchOtherXOffset, y);
                }
                break;
            case EDIT:
                if (owned) {
                    gc.drawImage(editImage, x - editXOffset, y);
                } else {
                    gc.drawImage(editOtherImage, x - editOtherXOffset, y);
                }
                break;
            case INTEGRATE:
                if (owned) {
                    gc.drawImage(integrateImage, x - integrateXOffset, y);
                } else {
                    gc.drawImage(integrateOtherImage,
                            x - integrateOtherXOffset, y);
                }
                break;
            case DELETE:
            case MOVE_DELETE:
                if (owned) {
                    gc.drawImage(deleteImage, x - deleteXOffset, y);
                } else {
                    gc.drawImage(deleteOtherImage, x - deleteOtherXOffset, y);
                }
            default:
                break;
            }
        }
    }

    /**
     * @see com.perforce.team.ui.timelapse.ITickDecorator#dispose()
     */
    public void dispose() {
        if (editImage != null) {
            editImage.dispose();
        }
        if (addImage != null) {
            addImage.dispose();
        }
        if (deleteImage != null) {
            deleteImage.dispose();
        }
        if (deleteOtherImage != null) {
            deleteOtherImage.dispose();
        }
        if (addOtherImage != null) {
            addOtherImage.dispose();
        }
        if (editOtherImage != null) {
            editOtherImage.dispose();
        }
        if (integrateImage != null) {
            integrateImage.dispose();
        }
        if (integrateOtherImage != null) {
            integrateOtherImage.dispose();
        }
        if (branchImage != null) {
            branchImage.dispose();
        }
        if (branchOtherImage != null) {
            branchOtherImage.dispose();
        }
    }
}
