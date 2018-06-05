/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.preferences;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ AdvancedPreferencePageTest.class,
        ConsolePreferencePageTest.class, GeneralPreferencePageTest.class,
        LabelPreferencePageTest.class, VariablesDialogTest.class, })
public class PreferencesSuite {

}
