/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.editor.palette;

import org.eclipse.gef.ui.palette.FlyoutPaletteComposite;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IFlyoutPaletteProvider {

    /**
     * Get flyout palette composite
     * 
     * @return flyout
     */
    FlyoutPaletteComposite getFlyout();

}
