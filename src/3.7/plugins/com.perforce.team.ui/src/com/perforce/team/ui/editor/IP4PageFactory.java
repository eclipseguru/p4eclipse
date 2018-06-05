/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.editor;

import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IP4PageFactory {

    /**
     * Create a page for a form editor
     * 
     * @param parent
     *            - form editor parent
     * @return - form page or null to add no page
     */
    IFormPage createPage(FormEditor parent);

}
