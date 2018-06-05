/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.diff;

import com.perforce.team.ui.diff.IFileDiffer;
import com.perforce.team.ui.java.diff.PropertiesDiffer;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class PropertiesDiffTest extends DifferTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        addFile(getFile(), new ByteArrayInputStream("test=a".getBytes()));
        addFile(getFile(),
                new ByteArrayInputStream("test=c\ntest2=test".getBytes()));
    }

    /**
     * @see com.perforce.team.tests.diff.DifferTestCase#getDifferClass()
     */
    @Override
    protected Class<? extends IFileDiffer> getDifferClass() {
        return PropertiesDiffer.class;
    }

    /**
     * @see com.perforce.team.tests.diff.DifferTestCase#getFile()
     */
    @Override
    protected IFile getFile() {
        return project.getFile("plugin.properties");
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.plugin";
    }

}
