/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.actions.p4java;

import com.perforce.team.tests.P4TestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.actions.ServerPropertiesAction;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ServerPropertiesActionTest extends P4TestCase {

    /**
     * Basic test of server properties action
     */
    public void testAction() {
        Action wrap = Utils.getDisabledAction();
        ServerPropertiesAction action = new ServerPropertiesAction();
        action.selectionChanged(wrap, new StructuredSelection());
        assertTrue(wrap.isEnabled());
    }
}
