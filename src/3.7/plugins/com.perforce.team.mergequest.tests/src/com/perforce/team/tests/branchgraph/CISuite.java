/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.branchgraph;

import com.perforce.team.tests.branchgraph.builder.BuilderSuite;
import com.perforce.team.tests.branchgraph.interchanges.InterchangesSuite;
import com.perforce.team.tests.branchgraph.model.ModelSuite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import junit.framework.Test;
import junit.framework.TestCase;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ InterchangesSuite.class, ModelSuite.class,
        BuilderSuite.class })
public class CISuite extends TestCase {

    /**
     * Suite
     * 
     * @return - Test
     */
    public static Test suite() {
        return new CISuite();
    }

}
