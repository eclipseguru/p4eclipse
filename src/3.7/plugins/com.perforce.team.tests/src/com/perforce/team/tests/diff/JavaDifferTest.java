/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.diff;

import com.perforce.team.ui.diff.IFileDiffer;
import com.perforce.team.ui.java.diff.JavaDiffer;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class JavaDifferTest extends DifferTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        addFile(getFile(), new ByteArrayInputStream(
                "public class UITeamProvider{}".getBytes()));
        addFile(getFile(),
                new ByteArrayInputStream(
                        "public class UITeamProvider{public void method1(){}}"
                                .getBytes()));
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.ui";
    }

    /**
     * @see com.perforce.team.tests.diff.DifferTestCase#getDifferClass()
     */
    @Override
    protected Class<? extends IFileDiffer> getDifferClass() {
        return JavaDiffer.class;
    }

    /**
     * @see com.perforce.team.tests.diff.DifferTestCase#getFile()
     */
    @Override
    protected IFile getFile() {
        return project.getFile(new Path(
                "src/com/perforce/team/ui/UITeamProvider.java"));
    }

}
