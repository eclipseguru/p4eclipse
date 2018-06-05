/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.branchgraph.builder;

import com.perforce.team.core.mergequest.builder.DepotPathBranchGraphBuilder;
import com.perforce.team.tests.Utils;
import com.perforce.team.tests.branchgraph.BranchGraphTestPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class DepotBuilderTest extends BaseContainerBuilderTest {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        File file = new File(Utils.getBundlePath(BranchGraphTestPlugin
                .getDefault().getBundle(), "/resources/sample.xml"));
        assertTrue(file.exists());
        addFile(project.getFile("sample.xml"), new FileInputStream(file));
    }

    /**
     * Test loading a container from a depot path
     */
    public void testLoadDepot() {
        DepotPathBranchGraphBuilder builder = new DepotPathBranchGraphBuilder(
                createConnection(), "//depot/testbg/sample.xml");
        assertNotNull(builder.getContainerFactory());

        try {
            validateContainer(builder.load());
        } catch (IOException e) {
            handle(e);
        }
    }

}
