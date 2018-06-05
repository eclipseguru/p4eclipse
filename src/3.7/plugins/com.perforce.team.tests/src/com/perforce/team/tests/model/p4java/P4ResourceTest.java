/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.model.p4java;

import org.eclipse.core.runtime.NullProgressMonitor;

import com.perforce.p4java.client.IClient;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Container;
import com.perforce.team.core.p4java.P4Resource;
import com.perforce.team.tests.ConnectionBasedTestCase;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4ResourceTest extends ConnectionBasedTestCase {

    public class P4TestResource extends P4Resource {

        public String getActionPath() {
            return null;
        }

        public String getActionPath(Type preferredType) {
            return null;
        }

        public IClient getClient() {
            return null;
        }

        public String getClientPath() {
            return null;
        }

        public IP4Connection getConnection() {
            return null;
        }

        public String getLocalPath() {
            return null;
        }

        public String getName() {
            return null;
        }

        public IP4Container getParent() {
            return null;
        }

        public String getRemotePath() {
            return null;
        }

        public boolean isContainer() {
            return false;
        }

        public void refresh(int depth) {

        }

        public void refresh() {

        }

    }

    /**
     * Test the base methods on p4 resource that can't be tested directly
     * because it is abstract
     */
    public void testBaseResource() {
        try {
            P4TestResource resource = new P4TestResource();
            resource.add(0);
            resource.delete(0);
            resource.edit(0);
            resource.ignore();
            resource.revert();
            resource.sync(new NullProgressMonitor());
        } catch (Exception e) {
            assertFalse("Exception thrown from P4 resource", false);
        }
    }

}
