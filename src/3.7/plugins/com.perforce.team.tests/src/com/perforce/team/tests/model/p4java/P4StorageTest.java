/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.model.p4java;

import com.perforce.team.core.p4java.P4Storage;
import com.perforce.team.tests.ConnectionBasedTestCase;

import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class P4StorageTest extends ConnectionBasedTestCase {

    /**
     * Simple p4 storage test
     */
    public void testStorage() {
        P4Storage storage = new P4Storage() {

            public InputStream getContents() throws CoreException {
                return null;
            }
        };
        try {
            assertNull(storage.getContents());
        } catch (CoreException e) {
            handle(e);
        }
        assertNull(storage.getName());
        assertNull(storage.getFullPath());
        assertTrue(storage.isReadOnly());
    }

}
