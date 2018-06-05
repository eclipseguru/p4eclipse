/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.parts;

import org.eclipse.swt.widgets.Control;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IControlProvider {

    /**
     * Get current control
     * 
     * @return control
     */
    Control getCurrentControl();

}
