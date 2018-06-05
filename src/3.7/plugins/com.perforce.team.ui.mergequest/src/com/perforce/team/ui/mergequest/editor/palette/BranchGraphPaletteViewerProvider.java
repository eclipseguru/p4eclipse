/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.editor.palette;

import com.perforce.team.ui.mergequest.P4BranchGraphPlugin;

import org.eclipse.gef.EditDomain;
import org.eclipse.gef.dnd.TemplateTransferDragSourceListener;
import org.eclipse.gef.ui.palette.DefaultPaletteViewerPreferences;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite.FlyoutPreferences;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.palette.PaletteViewerPreferences;
import org.eclipse.gef.ui.palette.PaletteViewerProvider;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchGraphPaletteViewerProvider extends PaletteViewerProvider {

    /**
     * Get flyout palette preferences
     * 
     * @return flyout preferences
     */
    public static FlyoutPreferences getPalettePreferences() {
        return FlyoutPaletteComposite
                .createFlyoutPreferences(P4BranchGraphPlugin.getDefault()
                        .getPluginPreferences());
    }

    /**
     * @param graphicalViewerDomain
     */
    public BranchGraphPaletteViewerProvider(EditDomain graphicalViewerDomain) {
        super(graphicalViewerDomain);
    }

    /**
     * @see org.eclipse.gef.ui.palette.PaletteViewerProvider#configurePaletteViewer(org.eclipse.gef.ui.palette.PaletteViewer)
     */
    @Override
    protected void configurePaletteViewer(PaletteViewer viewer) {
        super.configurePaletteViewer(viewer);
        IPreferenceStore store = P4BranchGraphPlugin.getDefault()
                .getPreferenceStore();
        viewer.setPaletteViewerPreferences(new DefaultPaletteViewerPreferences(
                store));
        // This must be done here since creating default palette viewer
        // preferences sets defaults and the desired defaults are different
        store.setDefault(PaletteViewerPreferences.PREFERENCE_LAYOUT,
                PaletteViewerPreferences.LAYOUT_DETAILS);
        viewer.addDragSourceListener(new TemplateTransferDragSourceListener(
                viewer));
    }

}
