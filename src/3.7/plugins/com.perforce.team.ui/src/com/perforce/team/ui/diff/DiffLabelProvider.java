/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.diff;

import com.perforce.team.ui.PerforceLabelProvider;
import com.perforce.team.ui.decorator.OverlayIcon;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.internal.ICompareUIConstants;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class DiffLabelProvider extends PerforceLabelProvider {

    /**
     * Compare configuration
     */
    protected CompareConfiguration configuration;

    private Map<OverlayIcon, Image> enlarged = new HashMap<OverlayIcon, Image>();

    /**
     *
     */
    public DiffLabelProvider() {
        this(new CompareConfiguration());
    }

    /**
     * @param configuration
     */
    public DiffLabelProvider(CompareConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * @param decorateResources
     */
    public DiffLabelProvider(boolean decorateResources) {
        this(new CompareConfiguration(), decorateResources);
    }

    /**
     * Create a new diff label provider
     * 
     * 
     * @param decorateResources
     * @param decorateLabels
     */
    public DiffLabelProvider(boolean decorateResources, boolean decorateText,
            boolean decorateImages) {
        this(new CompareConfiguration(), decorateResources, decorateText,
                decorateImages);
    }

    /**
     * Create a new diff label provider
     * 
     * @param configuration
     * @param decorateResources
     * @param decorateLabels
     */
    public DiffLabelProvider(CompareConfiguration configuration,
            boolean decorateResources, boolean decorateText,
            boolean decorateImages) {
        super(decorateResources, decorateText, decorateImages);
        this.configuration = configuration;
    }

    /**
     * @param configuration
     * @param decorateResources
     */
    public DiffLabelProvider(CompareConfiguration configuration,
            boolean decorateResources) {
        super(decorateResources);
        this.configuration = configuration;
    }

    /**
     * @see com.perforce.team.ui.PerforceLabelProvider#dispose()
     */
    @Override
    public void dispose() {
        super.dispose();
        this.configuration.dispose();
        for (Image image : this.enlarged.values()) {
            image.dispose();
        }
    }

    /**
     * Enlarge an image to the compare image width
     * 
     * @param orig
     * @return enlarged image
     */
    protected Image enlargeImage(Image orig) {
        OverlayIcon bigger = new OverlayIcon(orig, new ImageDescriptor[0],
                new int[0], ICompareUIConstants.COMPARE_IMAGE_WIDTH,
                orig.getBounds().height, 0, 0);
        Image converted = this.enlarged.get(bigger);
        if (converted == null) {
            converted = bigger.createImage();
            this.enlarged.put(bigger, converted);
        }
        return converted;
    }

    /**
     * Get diff image
     * 
     * @param diff
     * @return image
     */
    protected Image getDiffImage(IDiffElement diff) {
        return configuration.getImage(diff.getImage(), diff.getKind());
    }

    /**
     * @see com.perforce.team.ui.PerforceLabelProvider#getColumnImage(java.lang.Object,
     *      int)
     */
    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        if (element instanceof IDiffElement) {
            return getDiffImage((IDiffElement) element);
        } else {
            Image orig = super.getColumnImage(element, columnIndex);
            if (orig != null) {
                return enlargeImage(orig);
            }
        }
        return null;
    }

}
