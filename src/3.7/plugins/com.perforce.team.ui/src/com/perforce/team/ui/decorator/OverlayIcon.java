package com.perforce.team.ui.decorator;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import com.perforce.team.ui.IPerforceUIConstants;

import java.util.Arrays;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

/**
 * An OverlayIcon consists of a main icon and several adornments.
 */
public class OverlayIcon extends CompositeImageDescriptor {

    // the base image
    private Image base;

    // the overlay images
    private ImageDescriptor[] overlays;

    // the size
    private Point size;

    // the locations
    private int[] locations;

    private int baseX = 0;
    private int baseY = 0;

    /**
     * OverlayIcon constructor.
     * 
     * @param base
     *            the base image
     * @param overlays
     *            the overlay images
     * @param locations
     *            the location of each image
     */
    public OverlayIcon(Image base, ImageDescriptor[] overlays, int[] locations) {
        this(base, overlays, locations, base.getBounds().width, base
                .getBounds().height);
    }

    /**
     * OverlayIcon constructor.
     * 
     * @param base
     *            the base image
     * @param overlays
     *            the overlay images
     * @param locations
     *            the location of each image
     * @param width
     * @param height
     */
    public OverlayIcon(Image base, ImageDescriptor[] overlays, int[] locations,
            int width, int height) {
        this.base = base;
        this.overlays = overlays;
        this.locations = locations;
        this.size = new Point(width, height);
    }

    /**
     * OverlayIcon constructor.
     * 
     * @param base
     *            the base image
     * @param overlays
     *            the overlay images
     * @param locations
     *            the location of each image
     * @param width
     * @param height
     * @param baseX
     * @param baseY
     */
    public OverlayIcon(Image base, ImageDescriptor[] overlays, int[] locations,
            int width, int height, int baseX, int baseY) {
        this(base, overlays, locations, width, height);
        this.baseX = baseX;
        this.baseY = baseY;
    }

    /**
     * Draw overlays
     * 
     * @param overlays
     * @param locations
     */
    protected void drawOverlays(ImageDescriptor[] overlays, int[] locations) {
        Point size = getSize();
        for (int i = 0; i < overlays.length; i++) {
            ImageDescriptor overlay = overlays[i];
            ImageData overlayData = overlay.getImageData();
            switch (locations[i]) {
            case IPerforceUIConstants.ICON_TOP_LEFT:
                drawImage(overlayData, 0, 0);
                break;
            case IPerforceUIConstants.ICON_TOP_RIGHT:
                drawImage(overlayData, size.x - overlayData.width, 0);
                break;
            case IPerforceUIConstants.ICON_BOTTOM_LEFT:
                drawImage(overlayData, 0, size.y - overlayData.height);
                break;
            case IPerforceUIConstants.ICON_BOTTOM_RIGHT:
                drawImage(overlayData, size.x - overlayData.width, size.y
                        - overlayData.height);
                break;
            default:
                break;
            }
        }
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OverlayIcon)) {
            return false;
        }
        OverlayIcon other = (OverlayIcon) o;
        return base.equals(other.base)
                && Arrays.equals(overlays, other.overlays);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int code = base.hashCode();
        for (int i = 0; i < overlays.length; i++) {
            code ^= overlays[i].hashCode();
        }
        return code;
    }

    /**
     * @see org.eclipse.jface.resource.CompositeImageDescriptor#drawCompositeImage(int,
     *      int)
     */
    @Override
    protected void drawCompositeImage(int width, int height) {
        drawImage(base.getImageData(), baseX, baseY);
        drawOverlays(overlays, locations);
    }

    /**
     * @see org.eclipse.jface.resource.CompositeImageDescriptor#getSize()
     */
    @Override
    protected Point getSize() {
        return size;
    }
}
