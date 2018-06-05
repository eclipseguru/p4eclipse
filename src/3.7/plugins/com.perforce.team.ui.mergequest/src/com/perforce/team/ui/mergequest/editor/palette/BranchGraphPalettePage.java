/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.editor.palette;

import org.eclipse.gef.ui.palette.FlyoutPaletteComposite;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.palette.PaletteViewerProvider;
import org.eclipse.gef.ui.views.palette.PaletteViewerPage;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchGraphPalettePage extends PaletteViewerPage {

    private IFlyoutPaletteProvider paletteProvider;

    /**
     * Constructor
     * 
     * @param provider
     *            the provider used to create a PaletteViewer
     * @param paletteProvider
     */
    public BranchGraphPalettePage(PaletteViewerProvider provider,
            IFlyoutPaletteProvider paletteProvider) {
        super(provider);
        this.paletteProvider = paletteProvider;
    }

    /**
     * @see org.eclipse.ui.part.IPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        FlyoutPaletteComposite splitter = this.paletteProvider.getFlyout();
        if (splitter != null) {
            splitter.setExternalViewer(viewer);
        }
    }

    /**
     * @see org.eclipse.ui.part.IPage#dispose()
     */
    @Override
    public void dispose() {
        FlyoutPaletteComposite splitter = this.paletteProvider.getFlyout();
        if (splitter != null) {
            splitter.setExternalViewer(null);
        }
        super.dispose();
    }

    /**
     * @return the PaletteViewer created and displayed by this page
     */
    public PaletteViewer getPaletteViewer() {
        return viewer;
    }
}
