/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.branchgraph.model;

import com.perforce.team.core.mergequest.P4BranchGraphCorePlugin;
import com.perforce.team.core.mergequest.model.registry.BranchRegistry;
import com.perforce.team.core.mergequest.model.registry.BranchType;
import com.perforce.team.ui.mergequest.BranchWorkbenchAdapter;
import com.perforce.team.ui.mergequest.descriptors.DescriptorRegistry;
import com.perforce.team.ui.mergequest.descriptors.ElementDescriptor;

import java.util.Arrays;

import junit.framework.TestCase;

import org.eclipse.core.runtime.AssertionFailedException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchExtensionPointTest extends TestCase {

    /**
     * Test plugin branch registry
     */
    public void testPluginRegistry() {
        assertNotNull(P4BranchGraphCorePlugin.getDefault());
        assertNotNull(P4BranchGraphCorePlugin.getDefault().getBranchRegistry());
        assertNotNull(P4BranchGraphCorePlugin.getDefault().getBranchRegistry()
                .iterator());
        assertNotNull(P4BranchGraphCorePlugin.getDefault().getBranchRegistry()
                .getType(null));
    }

    /**
     * Test branch type contribution
     */
    public void testTypeContribution() {
        BranchRegistry reg = P4BranchGraphCorePlugin.getDefault()
                .getBranchRegistry();
        BranchType type = reg.getType("testType");
        assertNotNull(type);
        assertNotNull(type.toString());
        assertEquals("testType", type.getType());
        assertEquals(4321, type.getFirmness());
        assertEquals("My Test Branch Type", type.getLabel());
        assertTrue(Arrays.asList(reg.getTypes()).contains(type));
    }

    /**
     * Test branch type descriptor contribution
     */
    public void testDescriptorContribution() {
        DescriptorRegistry reg = BranchWorkbenchAdapter.getDescriptorRegistry();
        assertNotNull(reg);
        assertNotNull(reg.getDescriptors());
        assertNotNull(reg.getName());
        ElementDescriptor desc = reg.getDescriptor("testType");
        assertNotNull(desc);
        assertNotNull(desc.getDescription());
        assertEquals("branch", desc.getName());
        assertNotNull(desc.getIcon());
        assertEquals("testType", desc.getType());
        assertFalse(desc.isImportant());
    }

    /**
     * Test equals
     */
    public void testEquals() {
        BranchType type1 = new BranchType("a", "a", 1);
        assertEquals(type1, type1);
        assertEquals(type1.hashCode(), type1.hashCode());
        assertFalse(type1.equals(""));
        BranchType type2 = new BranchType("a", "a", 1);
        assertEquals(type1, type2);
        assertEquals(type1.hashCode(), type2.hashCode());
        BranchType type3 = new BranchType("b", "b", 1);
        assertFalse(type1.equals(type3));
        BranchType type4 = new BranchType("a", "a", 2);
        assertFalse(type1.equals(type4));
    }

    /**
     * Test null type
     */
    public void testNullType() {
        try {
            new BranchType(null, "abc", 10);
            assertFalse("Exception not thrown with null type", true);
        } catch (AssertionFailedException t) {
            assertNotNull(t);
        }
    }

    /**
     * Test empty type
     */
    public void testEmptyType() {
        try {
            new BranchType("", "my type", 234);
            assertFalse("Exception not thrown with empty type", true);
        } catch (AssertionFailedException t) {
            assertNotNull(t);
        }
    }

    /**
     * Test null label
     */
    public void testNullLabel() {
        try {
            new BranchType("1", null, 10);
            assertFalse("Exception not thrown with null label", true);
        } catch (AssertionFailedException t) {
            assertNotNull(t);
        }
    }

    /**
     * Test empty label
     */
    public void testEmptyLabel() {
        try {
            new BranchType("1", "", 234);
            assertFalse("Exception not thrown with empty label", true);
        } catch (AssertionFailedException t) {
            assertNotNull(t);
        }
    }

}
