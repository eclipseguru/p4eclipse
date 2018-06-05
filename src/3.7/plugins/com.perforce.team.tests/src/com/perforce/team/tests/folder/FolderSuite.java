/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.folder;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
@RunWith(Suite.class)
@SuiteClasses({ DiffEditorTest.class, DiffFileTest.class, FileEntryTest.class,
        FilterOptionsTest.class, PluginTest.class })
public class FolderSuite {

}
