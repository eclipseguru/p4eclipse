/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.branchgraph.model;

import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.BranchGraph;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.core.mergequest.model.Mapping;
import com.perforce.team.core.mergequest.model.MappingException;
import com.perforce.team.core.mergequest.model.Mapping.Direction;
import com.perforce.team.core.mergequest.model.MappingDescriptor.ChangeType;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.P4Connection;
import com.perforce.team.tests.P4TestCase;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class BaseMappingTest extends P4TestCase {

    /**
     * Create mapping
     * 
     * @param id
     * @return non-null mapping
     */
    protected abstract Mapping createMapping(String id);

    /**
     * Test requesting interchanges with invalid input
     */
    public void testInvalidInterchanges() {
        Mapping mapping = createMapping("m1");
        IP4Connection connection = new P4Connection(null);
        try {
            mapping.refreshSourceInterchanges(connection);
            assertFalse("Exception not thrown", true);
        } catch (MappingException e) {
            assertNotNull(e);
        }
        try {
            mapping.refreshTargetInterchanges(connection);
            assertFalse("Exception not thrown", true);
        } catch (MappingException e) {
            assertNotNull(e);
        }
    }

    /**
     * Test empty mapping
     */
    public void testEmpty() {
        Mapping mapping = createMapping("m1");
        assertNotNull(mapping.getName());
        assertNull(mapping.getSourceId());
        assertNull(mapping.getSource());
        assertNull(mapping.getTargetId());
        assertNull(mapping.getTarget());
        assertNotNull(mapping.getType());
        try {
            assertFalse(mapping.refreshSourceStatus(null, null));
        } catch (MappingException e) {
            handle(e);
        }
        try {
            assertFalse(mapping.refreshTargetStatus(null, null));
        } catch (MappingException e) {
            handle(e);
        }
        try {
            assertNotNull(mapping.refreshSourceInterchanges(null));
        } catch (MappingException e) {
            handle(e);
        }
        try {
            assertNotNull(mapping.refreshTargetInterchanges(null));
        } catch (MappingException e) {
            handle(e);
        }
        assertEquals(-1, mapping.getLatestSource());
        assertEquals(-1, mapping.getLatestTarget());
        assertEquals(0, mapping.getSourceToTargetCount());
        assertEquals(0, mapping.getTargetToSourceCount());
        assertEquals(ChangeType.UNKNOWN, mapping.getSourceChange());
        assertEquals(ChangeType.UNKNOWN, mapping.getTargetChange());
        assertEquals(Direction.BOTH, mapping.getDirection());
        assertNotNull(mapping.getJoints());
        assertEquals(0, mapping.getJoints().length);
        assertEquals(0, mapping.getJointCount());
    }

    /**
     * Test update source change
     */
    public void testUpdateSourceChange() {
        Mapping mapping = createMapping("m1");
        final PropertyChangeEvent[] event = new PropertyChangeEvent[1];
        int latest = mapping.getLatestSource();
        mapping.addPropertyListener(Mapping.LATEST_SOURCE_CHANGE,
                new PropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent evt) {
                        event[0] = evt;
                    }
                });
        assertTrue(mapping.setLatestSource(100));
        assertNotNull(event[0]);
        assertEquals(mapping, event[0].getSource());
        assertEquals(latest, event[0].getOldValue());
        assertEquals(100, event[0].getNewValue());
        assertFalse(mapping.setLatestSource(mapping.getLatestSource()));
    }

    /**
     * Test update target change
     */
    public void testUpdateTargetChange() {
        Mapping mapping = createMapping("m1");
        final PropertyChangeEvent[] event = new PropertyChangeEvent[1];
        int latest = mapping.getLatestSource();
        mapping.addPropertyListener(Mapping.LATEST_TARGET_CHANGE,
                new PropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent evt) {
                        event[0] = evt;
                    }
                });
        assertTrue(mapping.setLatestTarget(922));
        assertNotNull(event[0]);
        assertEquals(mapping, event[0].getSource());
        assertEquals(latest, event[0].getOldValue());
        assertEquals(922, event[0].getNewValue());
        assertFalse(mapping.setLatestTarget(mapping.getLatestTarget()));
    }

    /**
     * Test update source count
     */
    public void testUpdateSourceCount() {
        Mapping mapping = createMapping("m1");
        final PropertyChangeEvent[] event = new PropertyChangeEvent[1];
        int count = mapping.getTargetToSourceCount();
        mapping.addPropertyListener(Mapping.SOURCE_CHANGE_COUNT,
                new PropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent evt) {
                        event[0] = evt;
                    }
                });
        assertTrue(mapping.setTargetToSourceCount(16));
        assertNotNull(event[0]);
        assertEquals(mapping, event[0].getSource());
        assertEquals(count, event[0].getOldValue());
        assertEquals(16, event[0].getNewValue());
        assertFalse(mapping.setTargetToSourceCount(mapping
                .getTargetToSourceCount()));
    }

    /**
     * Test update target count
     */
    public void testUpdateTargetCount() {
        Mapping mapping = createMapping("m1");
        final PropertyChangeEvent[] event = new PropertyChangeEvent[1];
        int count = mapping.getSourceToTargetCount();
        mapping.addPropertyListener(Mapping.TARGET_CHANGE_COUNT,
                new PropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent evt) {
                        event[0] = evt;
                    }
                });
        assertTrue(mapping.setSourceToTargetCount(5));
        assertNotNull(event[0]);
        assertEquals(mapping, event[0].getSource());
        assertEquals(count, event[0].getOldValue());
        assertEquals(5, event[0].getNewValue());
        assertFalse(mapping.setSourceToTargetCount(mapping
                .getSourceToTargetCount()));
    }

    /**
     * Test update source change type
     */
    public void testUpdateSourceType() {
        Mapping mapping = createMapping("m1");
        assertFalse(mapping.hasSourceChanges());
        final PropertyChangeEvent[] event = new PropertyChangeEvent[1];
        ChangeType type = mapping.getSourceChange();
        mapping.addPropertyListener(Mapping.SOURCE_CHANGE_TYPE,
                new PropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent evt) {
                        event[0] = evt;
                    }
                });
        assertTrue(mapping.setSourceChanges(ChangeType.VISIBLE_CHANGES));
        assertNotNull(event[0]);
        assertEquals(mapping, event[0].getSource());
        assertEquals(type, event[0].getOldValue());
        assertEquals(ChangeType.VISIBLE_CHANGES, event[0].getNewValue());
        assertFalse(mapping.setSourceChanges(mapping.getSourceChange()));
        assertTrue(mapping.hasSourceChanges());
    }

    /**
     * Test update target change type
     */
    public void testUpdateTargetType() {
        Mapping mapping = createMapping("m1");
        assertFalse(mapping.hasTargetChanges());
        final PropertyChangeEvent[] event = new PropertyChangeEvent[1];
        ChangeType type = mapping.getTargetChange();
        mapping.addPropertyListener(Mapping.TARGET_CHANGE_TYPE,
                new PropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent evt) {
                        event[0] = evt;
                    }
                });
        assertTrue(mapping.setTargetChanges(ChangeType.VISIBLE_CHANGES));
        assertNotNull(event[0]);
        assertEquals(mapping, event[0].getSource());
        assertEquals(type, event[0].getOldValue());
        assertEquals(ChangeType.VISIBLE_CHANGES, event[0].getNewValue());
        assertFalse(mapping.setTargetChanges(mapping.getTargetChange()));
        assertTrue(mapping.hasTargetChanges());
    }

    /**
     * Test update direction
     */
    public void testUpdateDirection() {
        Mapping mapping = createMapping("m1");
        assertEquals(Direction.BOTH, mapping.getDirection());
        final PropertyChangeEvent[] event = new PropertyChangeEvent[1];
        mapping.addPropertyListener(Mapping.DIRECTION,
                new PropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent evt) {
                        event[0] = evt;
                    }
                });
        assertTrue(mapping.setDirection(Direction.SOURCE));
        assertNotNull(event[0]);
        assertEquals(mapping, event[0].getSource());
        assertEquals(Direction.BOTH, event[0].getOldValue());
        assertEquals(Direction.SOURCE, event[0].getNewValue());
        assertFalse(mapping.setDirection(mapping.getDirection()));
    }

    /**
     * Test connecting a mapping to branches
     */
    public void testConnect() {
        IBranchGraph graph = new BranchGraph("g1");
        Branch b1 = graph.createBranch(null);
        Branch b2 = graph.createBranch(null);
        assertTrue(graph.add(b1));
        assertTrue(graph.add(b2));

        Mapping mapping = createMapping("m1");
        mapping.setGraph(graph);
        assertFalse(mapping.disconnect());
        final PropertyChangeEvent[] event = new PropertyChangeEvent[1];
        mapping.addPropertyListener(Mapping.CONNECTED,
                new PropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent evt) {
                        event[0] = evt;
                    }
                });
        assertTrue(mapping.connect(b1, b2));
        assertNotNull(event[0]);
        assertEquals(mapping, event[0].getSource());
        assertEquals(Boolean.FALSE, event[0].getOldValue());
        assertEquals(Boolean.TRUE, event[0].getNewValue());
        assertEquals(b1, mapping.getSource());
        assertEquals(b1.getId(), mapping.getSourceId());
        assertEquals(b2, mapping.getTarget());
        assertEquals(b2.getId(), mapping.getTargetId());
        assertEquals(mapping, b1.getSourceMapping(mapping.getId()));
        assertEquals(mapping, b2.getTargetMapping(mapping.getId()));
        assertTrue(Arrays.asList(b2.getSourceOwnedTargetMappings()).contains(
                b1.getSourceMapping(mapping.getId())));

        event[0] = null;
        assertTrue(mapping.disconnect());
        assertNotNull(event[0]);
        assertEquals(mapping, event[0].getSource());
        assertEquals(Boolean.TRUE, event[0].getOldValue());
        assertEquals(Boolean.FALSE, event[0].getNewValue());
        assertNull(b1.getSourceMapping(mapping.getId()));
        assertNull(b2.getTargetMapping(mapping.getId()));
    }
}
