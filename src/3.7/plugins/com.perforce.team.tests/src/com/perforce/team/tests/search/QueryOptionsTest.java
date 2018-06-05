/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.search;

import com.perforce.team.core.search.query.QueryOptions;
import com.perforce.team.tests.ConnectionBasedTestCase;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.AssertionFailedException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class QueryOptionsTest extends ConnectionBasedTestCase {

    /**
     * Test query options with just pattern set
     */
    public void testEmpty() {
        QueryOptions options = new QueryOptions("test");
        assertEquals("test", options.getPattern());
        assertNotNull(options.getPaths());
        assertEquals(-1, options.getLeadingContext());
        assertEquals(-1, options.getTrailingContext());
        assertFalse(options.isAllRevisions());
        assertNotNull(options.createOptions());
    }

    /**
     * Test query options path adding
     */
    public void testPaths() {
        Set<String> paths = new HashSet<String>();
        QueryOptions options = new QueryOptions(" ");
        options.addPath("test");
        paths.add("test");
        options.addPath("test2");
        paths.add("test2");
        assertNotNull(options.getPaths());
        assertEquals(2, options.getPaths().length);
        assertTrue(Arrays.equals(paths.toArray(), options.getPaths()));
        paths.add("test3");
        paths.add("test4");
        options.addPath("test3");
        options.addPaths(new String[] { "test3", "test4" });
        assertEquals(4, options.getPaths().length);
        assertTrue(Arrays.equals(paths.toArray(), options.getPaths()));
    }

    /**
     * Set setting values
     */
    public void testSetting() {
        QueryOptions options = new QueryOptions("b");
        options.setPattern("c");
        assertEquals("c", options.getPattern());
        options.setAllRevisions(true);
        assertTrue(options.isAllRevisions());
        options.setCaseInsensitive(true);
        assertTrue(options.isCaseInsensitive());
        options.setSearchBinaries(true);
        assertTrue(options.isSearchBinaries());
        options.setLeadingContext(5);
        assertEquals(5, options.getLeadingContext());
        options.setTrailingContext(10);
        assertEquals(10, options.getTrailingContext());
    }

    /**
     * Test null pattern
     */
    public void testNullPattern() {
        try {
            new QueryOptions(null);
            assertFalse(true);
        } catch (AssertionFailedException e) {
            assertNotNull(e);
        }
        QueryOptions options = new QueryOptions("a");
        options.setPattern(null);
        assertEquals("a", options.getPattern());
    }

}
