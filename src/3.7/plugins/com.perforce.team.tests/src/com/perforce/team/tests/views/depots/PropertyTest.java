/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.views.depots;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4FilePropertySource;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class PropertyTest extends ProjectBasedTestCase {

    /**
     * 
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        addFile(project.getFile("Jamfile"));
    }

    /**
     * Test of {@link P4FilePropertySource}
     */
    public void testSource() {
        IFile file = project.getFile("Jamfile");
        assertTrue(file.exists());
        IP4Resource resource = P4ConnectionManager.getManager().getResource(
                file);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        P4FilePropertySource source = new P4FilePropertySource(p4File);
        assertNull(source.getEditableValue());
        IPropertyDescriptor[] descriptors = source.getPropertyDescriptors();
        assertNotNull(descriptors);
        assertTrue(descriptors.length > 0);
        for (IPropertyDescriptor descriptor : descriptors) {
            assertNull(descriptor.getCategory());
            assertNull(descriptor.getDescription());
            assertNotNull(descriptor.getDisplayName());
            assertNotNull(descriptor.getId());
            assertNotNull(source.getPropertyValue(descriptor.getId()));
            assertFalse(source.isPropertySet(descriptor.getId()));
        }
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p07.2/p4-eclipse/native/com.perforce.p4api.cnative";
    }

}
