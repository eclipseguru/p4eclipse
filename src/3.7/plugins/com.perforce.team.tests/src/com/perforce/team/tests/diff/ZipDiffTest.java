/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.diff;

import com.perforce.team.ui.diff.IFileDiffer;
import com.perforce.team.ui.zip.ZipDiffer;

import org.eclipse.core.resources.IFile;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ZipDiffTest extends DifferTestCase {

    /**
     * @see com.perforce.team.tests.diff.DifferTestCase#getDifferClass()
     */
    @Override
    protected Class<? extends IFileDiffer> getDifferClass() {
        return ZipDiffer.class;
    }

    /**
     * @see com.perforce.team.tests.diff.DifferTestCase#getFile()
     */
    @Override
    protected IFile getFile() {
        return project.getFile("pluginsrc.zip");
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p07.2/p4-eclipse/build/frameworks/objfac/eclipse/plugins/com.objfac.ant.preprocess_0.9.1";
    }

}
