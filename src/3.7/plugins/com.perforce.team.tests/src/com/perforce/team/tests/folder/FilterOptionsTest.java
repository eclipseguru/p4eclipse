/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.folder;

import com.perforce.team.tests.P4TestCase;
import com.perforce.team.ui.folder.diff.editor.input.FilterOptions;
import com.perforce.team.ui.folder.diff.editor.input.IFilterOptions;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class FilterOptionsTest extends P4TestCase {

    /**
     * Test empty options
     */
    public void testEmpty() {
        IFilterOptions options = new FilterOptions();
        assertTrue(options.isHeadFilter());
        assertFalse(options.isChangelistFilter());
        assertFalse(options.isClientFilter());
        assertFalse(options.isDateFilter());
        assertFalse(options.isHaveFilter());
        assertFalse(options.isLabelFilter());
        assertFalse(options.isRevisionFilter());
        assertEquals("", options.getChangelist());
        assertEquals("", options.getClient());
        assertEquals("", options.getDate());
        assertEquals("", options.getLabel());
        assertEquals("", options.getRevision());
        assertEquals(new FilterOptions(), options);
    }

    /**
     * Test null setting
     */
    public void testNull() {
        IFilterOptions options = new FilterOptions();
        options.setChangelist(null);
        assertNotNull(options.getChangelist());
        options.setClient(null);
        assertNotNull(options.getClient());
        options.setDate(null);
        assertNotNull(options.getDate());
        options.setLabel(null);
        assertNotNull(options.getLabel());
        options.setRevision(null);
        assertNotNull(options.getRevision());
    }

    /**
     * Test serialization/deserialization
     */
    public void testIO() {
        IFilterOptions options = new FilterOptions();
        options.setHaveFilter(true);
        options.setHeadFilter(true);
        options.setClientFilter(false);
        options.setChangelistFilter(false);
        options.setRevisionFilter(true);
        options.setDateFilter(true);
        options.setChangelist("1");
        options.setRevision("2");
        options.setDate("3");
        options.setClient("4");
        options.setLabel("5");
        IMemento memento = XMLMemento.createWriteRoot("test");
        assertNotNull(memento);
        options.saveState(memento);
        IFilterOptions options2 = new FilterOptions();
        assertFalse(options.equals(options2));
        options2.load(memento);
        assertEquals(options, options2);
        assertEquals(options.hashCode(), options2.hashCode());
    }

}
