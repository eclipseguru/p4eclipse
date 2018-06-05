/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.branchgraph.model;

import junit.framework.Test;
import junit.framework.TestCase;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ BranchExtensionPointTest.class,
        BranchSpecMappingTest.class, BranchTest.class, ChangeTypeTest.class,
        ContainerTest.class, DepotPathMappingTest.class, DirectionTest.class,
        GraphTest.class, PropertyElementTest.class })
public class ModelSuite extends TestCase {

    /**
     * Suite
     * 
     * @return - Test
     */
    public static Test suite() {
        return new ModelSuite();
    }

}
