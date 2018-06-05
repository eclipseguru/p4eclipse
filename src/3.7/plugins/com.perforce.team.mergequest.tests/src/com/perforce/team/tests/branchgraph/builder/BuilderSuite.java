/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.branchgraph.builder;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ BranchBuilderTest.class,
        BranchSpecMappingBuilderTest.class, DepotBuilderTest.class,
        DepotPathMappingBuilderTest.class, FileBuilderTest.class,
        RegistryTest.class })
public class BuilderSuite {

}
