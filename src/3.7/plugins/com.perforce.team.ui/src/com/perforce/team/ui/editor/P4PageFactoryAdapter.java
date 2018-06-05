/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.editor;

import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;

/**
 * Base class for {@link IP4PageFactory} interface. Clients should subclass this
 * class instead of directly implementing {@link IP4PageFactory}.
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4PageFactoryAdapter implements IP4PageFactory {

    /**
     * Create a p4 page factory
     */
    public P4PageFactoryAdapter() {

    }

    /**
     * @see com.perforce.team.ui.editor.IP4PageFactory#createPage(org.eclipse.ui.forms.editor.FormEditor)
     */
    public IFormPage createPage(FormEditor parent) {
        return null;
    }

}
