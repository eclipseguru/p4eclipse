/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.patch;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ ClipboardTest.class, FileTest.class, PatchTest.class,
        PluginTest.class, UnicodeTest.class, WorkspaceTest.class })
public class PatchSuite {

}
