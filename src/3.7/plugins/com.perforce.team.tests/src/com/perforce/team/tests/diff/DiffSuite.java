/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.diff;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ CDiffTest.class, DiffRegistryTest.class,
        JavaDifferTest.class, PropertiesDiffTest.class, QuickDiffTest.class })
public class DiffSuite {

}
