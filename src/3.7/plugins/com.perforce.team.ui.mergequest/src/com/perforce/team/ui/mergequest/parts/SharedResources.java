/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.parts;

import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.decorator.OverlayIcon;
import com.perforce.team.ui.mergequest.P4BranchGraphPlugin;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class SharedResources {

    private Map<String, Image> images = new HashMap<String, Image>();
    private Map<ImageDescriptor, Image> descriptors = new HashMap<ImageDescriptor, Image>();
    private Map<OverlayIcon, Image> overlays = new HashMap<OverlayIcon, Image>();
    private Map<RGB, Color> colors = new HashMap<RGB, Color>();

    /**
     * Get image
     * 
     * @param descriptor
     * @return - image
     */
    public Image getImage(ImageDescriptor descriptor) {
        Image image = null;
        if (descriptor != null) {
            image = descriptors.get(descriptor);
            if (image == null) {
                image = descriptor.createImage();
                if (image != null) {
                    descriptors.put(descriptor, image);
                }
            }
        }
        return image;
    }

    /**
     * Get image
     * 
     * @param path
     * @return - image
     */
    public Image getImage(String path) {
        Image image = null;
        if (path != null) {
            image = images.get(path);
            if (image == null) {
                ImageDescriptor desc = P4BranchGraphPlugin
                        .getImageDescriptor(path);
                if (desc != null) {
                    image = desc.createImage();
                    if (image != null) {
                        images.put(path, image);
                    }
                }
            }
        }
        return image;
    }

    /**
     * Get color
     * 
     * @param rgb
     * @return - color
     */
    public Color getColor(RGB rgb) {
        Color color = null;
        if (rgb != null) {
            color = this.colors.get(rgb);
            if (color == null) {
                color = new Color(P4UIUtils.getDisplay(), rgb);
                colors.put(rgb, color);
            }
        }
        return color;
    }

    /**
     * Get overlay image from an overlay icon
     * 
     * @param icon
     * @return image
     */
    public Image getOverlay(OverlayIcon icon) {
        Image image = null;
        if (icon != null) {
            image = this.overlays.get(icon);
            if (image == null) {
                image = icon.createImage();
                if (image != null) {
                    this.overlays.put(icon, image);
                }
            }
        }
        return image;
    }

    /**
     * Get system color
     * 
     * @param id
     * @return - color
     */
    public Color getSystemColor(int id) {
        return P4UIUtils.getDisplay().getSystemColor(id);
    }

    /**
     * Dispose of images and colors
     */
    public void dispose() {
        for (Image image : images.values()) {
            if (!image.isDisposed()) {
                image.dispose();
            }
        }
        for (Image image : overlays.values()) {
            if (!image.isDisposed()) {
                image.dispose();
            }
        }
        for (Image image : descriptors.values()) {
            if (!image.isDisposed()) {
                image.dispose();
            }
        }
        for (Color color : colors.values()) {
            if (!color.isDisposed()) {
                color.dispose();
            }
        }
    }

}
