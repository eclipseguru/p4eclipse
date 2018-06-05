/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.actions.p4java;

import com.perforce.team.tests.P4TestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.p4java.actions.AuthenticationAction;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class AuthenticationActionTest extends P4TestCase {

    /**
     * Basic test of authentication action
     */
    public void testAction() {
        Action wrap = Utils.getDisabledAction();
        AuthenticationAction action = new AuthenticationAction();
        action.selectionChanged(wrap, new StructuredSelection());
        assertTrue(wrap.isEnabled());
    }

}
