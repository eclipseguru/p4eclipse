/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.diff;

import com.perforce.team.ui.c.diff.CDiffer;
import com.perforce.team.ui.diff.IFileDiffer;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class CDiffTest extends DifferTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        addFile(project.getFile(new Path("src/ClientUser.cpp")),
                new ByteArrayInputStream("int main( int argc, char **argv ){}"
                        .getBytes()));

        addFile(project.getFile(new Path("src/ClientUser.cpp")),
                new ByteArrayInputStream(
                        "int main( int argc, char **argv ){ int a; int b;}"
                                .getBytes()));
    }

    /**
     * @see com.perforce.team.tests.diff.DifferTestCase#getDifferClass()
     */
    @Override
    protected Class<? extends IFileDiffer> getDifferClass() {
        return CDiffer.class;
    }

    /**
     * @see com.perforce.team.tests.diff.DifferTestCase#getFile()
     */
    @Override
    protected IFile getFile() {
        return project.getFile(new Path("src/ClientUser.cpp"));
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p07.2/p4-eclipse/native/com.perforce.p4api.cnative";
    }

}
