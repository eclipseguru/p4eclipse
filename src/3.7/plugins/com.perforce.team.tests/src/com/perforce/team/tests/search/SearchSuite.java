/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.search;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
@RunWith(Suite.class)
@SuiteClasses({ DepotPathTest.class, PluginTest.class, QueryOptionsTest.class,
        SearchQueryTest.class, SearchTest.class, SettingsTest.class })
public class SearchSuite {

}
