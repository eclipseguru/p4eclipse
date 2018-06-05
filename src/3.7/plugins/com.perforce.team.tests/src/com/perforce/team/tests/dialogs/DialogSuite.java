/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.dialogs;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ AuthDialogTest.class, ErrorDialogTest.class,
        FiletypeDialogTest.class, JobFixDialogTest.class,
        MoveChangeDialogTest.class, PasswordDialogTest.class,
        ProjectPropertiesDialogTest.class, ResourceBrowserTest.class,
        SyncRevisionDialogTest.class, SwitchClientDialogTest.class})
public class DialogSuite {

}
