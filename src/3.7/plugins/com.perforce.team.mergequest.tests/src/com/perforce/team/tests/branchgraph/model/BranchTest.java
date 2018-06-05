/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.branchgraph.model;

import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.BranchGraph;
import com.perforce.team.core.mergequest.model.DepotPathMapping;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.core.mergequest.model.IBranchGraphElement;
import com.perforce.team.core.mergequest.model.IMappingVisitor;
import com.perforce.team.core.mergequest.model.Mapping;
import com.perforce.team.tests.P4TestCase;

import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.AssertionFailedException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchTest extends P4TestCase {

    /**
     * Test creating a branch with a null id
     */
    public void testNullId() {
        try {
            new Branch(null);
            assertFalse("Exception not thrown with null id", true);
        } catch (AssertionFailedException t) {
            assertNotNull(t);
        }
    }

    /**
     * Test creating a branch with an empty id
     */
    public void testEmptyId() {
        try {
            new Branch("");
            assertFalse("Exception not thrown with empty id", true);
        } catch (AssertionFailedException t) {
            assertNotNull(t);
        }
    }

    /**
     * Test empty branch
     */
    public void testEmpty() {
        String id = "id1";
        Branch branch = new Branch(id);
        assertEquals(id, branch.getId());
        assertNotNull(branch.toString());
        assertNotNull(branch.getAllMappings());
        assertNotNull(branch.getSourceMappings());
        assertNotNull(branch.getTargetMappings());
        assertNotNull(branch.getSourceOwnedTargetMappings());
        assertEquals(0, branch.getMappingCount());
        assertEquals(branch, branch.getAdapter(IBranchGraphElement.class));
    }

    /**
     * Test resizing a branch
     */
    public void testResize() {
        Branch branch = new Branch("resize");
        final PropertyChangeEvent[] resizeEvent = new PropertyChangeEvent[1];
        branch.addPropertyListener(Branch.SIZE, new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                resizeEvent[0] = evt;
            }
        });

        branch.setSize(0, 0);
        assertNull(resizeEvent[0]);

        int width = branch.getWidth();
        int height = branch.getHeight();
        branch.setSize(width + 10, height + 10);
        assertNotNull(resizeEvent[0]);
        assertEquals(branch, resizeEvent[0].getSource());
        assertEquals(Branch.SIZE, resizeEvent[0].getPropertyName());
        assertEquals(new Rectangle(width, height), resizeEvent[0].getOldValue());
        assertEquals(new Rectangle(width + 10, height + 10),
                resizeEvent[0].getNewValue());

        resizeEvent[0] = null;
    }

    /**
     * Test moving a branch
     */
    public void testMove() {
        Branch branch = new Branch("move");
        final PropertyChangeEvent[] moveEvent = new PropertyChangeEvent[1];
        branch.addPropertyListener(Branch.LOCATION,
                new PropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent evt) {
                        moveEvent[0] = evt;
                    }
                });
        int x = branch.getX();
        int y = branch.getY();
        branch.setLocation(x + 1, y + 1);
        assertNotNull(moveEvent[0]);
        assertEquals(branch, moveEvent[0].getSource());
        assertEquals(Branch.LOCATION, moveEvent[0].getPropertyName());
        assertEquals(new Point(x, y), moveEvent[0].getOldValue());
        assertEquals(new Point(x + 1, y + 1), moveEvent[0].getNewValue());
    }

    /**
     * Test equals
     */
    public void testEquals() {
        Branch branch = new Branch("branch1");
        Branch branch2 = new Branch(branch.getId());
        assertEquals(branch, branch);
        assertEquals(branch, branch2);
        assertEquals(branch.hashCode(), branch2.hashCode());
        assertFalse(branch.equals(null));
        IBranchGraph sameId = new BranchGraph(branch.getId());
        assertFalse(branch.equals(sameId));
    }

    /**
     * Test branch type setting and getting
     */
    public void testType() {
        Branch branch = new Branch("b");
        assertNotNull(branch.getType());
        branch.setType(null);
        assertNotNull(branch.getType());
        branch.setType("main");
        assertEquals("main", branch.getType());
    }

    /**
     * Test source mapping
     */
    public void testSourceMapping() {
        IBranchGraph graph = new BranchGraph("g");
        Branch branch = new Branch("a");
        final PropertyChangeEvent[] event = new PropertyChangeEvent[] { null };
        branch.addPropertyListener(Branch.SOURCE_MAPPINGS,
                new PropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent evt) {
                        event[0] = evt;
                    }
                });
        assertTrue(graph.add(branch));
        assertNull(branch.getSourceMapping(null));
        assertNull(branch.getSourceMappingByName(null));
        assertNull(branch.getSourceMapping("m"));
        assertNull(branch.getSourceMappingByName("mapping"));
        Mapping m = new DepotPathMapping("m");
        assertTrue(graph.add(m));
        m.setSourceId(branch.getId());
        m.setName("mapping");
        assertTrue(branch.add(m));
        assertEquals(m, branch.getSourceMapping("m"));
        assertEquals(m, branch.getSourceMappingByName("mapping"));
        assertTrue(Arrays.asList(branch.getSourceMappings()).contains(m));
        assertTrue(Arrays.asList(branch.getAllMappings()).contains(m));
        assertNull(branch.getSourceMappingByName(null));
        assertNotNull(event[0]);
        assertEquals(branch, event[0].getSource());
        assertEquals(null, event[0].getOldValue());
        assertEquals(m, event[0].getNewValue());
        branch.disconnect();
        assertNull(branch.getSourceMapping("m"));
        assertNull(branch.getSourceMappingByName("mapping"));
    }

    /**
     * Test target mapping
     */
    public void testTargetMapping() {
        IBranchGraph graph = new BranchGraph("g");
        Branch branch = new Branch("a");
        final PropertyChangeEvent[] event = new PropertyChangeEvent[] { null };
        branch.addPropertyListener(Branch.TARGET_MAPPINGS,
                new PropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent evt) {
                        event[0] = evt;
                    }
                });
        assertTrue(graph.add(branch));
        assertNull(branch.getTargetMapping(null));
        assertNull(branch.getTargetMappingByName(null));
        assertNull(branch.getTargetMapping("m"));
        assertNull(branch.getTargetMappingByName("mapping"));
        Mapping m = new DepotPathMapping("m");
        assertTrue(graph.add(m));
        m.setTargetId(branch.getId());
        m.setName("mapping");
        assertTrue(branch.add(m));
        assertEquals(m, branch.getTargetMapping("m"));
        assertEquals(m, branch.getTargetMappingByName("mapping"));
        assertTrue(Arrays.asList(branch.getTargetMappings()).contains(m));
        assertTrue(Arrays.asList(branch.getAllMappings()).contains(m));
        assertNull(branch.getTargetMappingByName(null));
        assertNotNull(event[0]);
        assertEquals(branch, event[0].getSource());
        assertEquals(null, event[0].getOldValue());
        assertEquals(m, event[0].getNewValue());
        branch.disconnect();
        assertNull(branch.getTargetMapping("m"));
        assertNull(branch.getTargetMappingByName("mapping"));
    }

    /**
     * Test source mapping removal
     */
    public void testSourceRemove() {
        IBranchGraph graph = new BranchGraph("g");
        Branch branch = new Branch("a");
        assertTrue(graph.add(branch));
        Mapping m = new DepotPathMapping("m");
        assertTrue(graph.add(m));
        m.setSourceId(branch.getId());
        m.setName("mapping");
        assertFalse(branch.remove(m));
        assertTrue(branch.add(m));
        assertEquals(m, branch.getSourceMapping("m"));
        assertEquals(m, branch.getSourceMappingByName("mapping"));

        final PropertyChangeEvent[] event = new PropertyChangeEvent[] { null };
        branch.addPropertyListener(Branch.SOURCE_MAPPINGS,
                new PropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent evt) {
                        event[0] = evt;
                    }
                });
        assertTrue(branch.remove(m));
        assertNotNull(event[0]);
        assertEquals(branch, event[0].getSource());
        assertEquals(null, event[0].getNewValue());
        assertEquals(m, event[0].getOldValue());
    }

    /**
     * Test target mapping removal
     */
    public void testTargetRemove() {
        IBranchGraph graph = new BranchGraph("g");
        Branch branch = new Branch("a");
        assertTrue(graph.add(branch));
        Mapping m = new DepotPathMapping("m");
        assertTrue(graph.add(m));
        m.setTargetId(branch.getId());
        m.setName("mapping");
        assertFalse(branch.remove(m));
        assertTrue(branch.add(m));
        assertEquals(m, branch.getTargetMapping("m"));
        assertEquals(m, branch.getTargetMappingByName("mapping"));

        final PropertyChangeEvent[] event = new PropertyChangeEvent[] { null };
        branch.addPropertyListener(Branch.TARGET_MAPPINGS,
                new PropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent evt) {
                        event[0] = evt;
                    }
                });
        assertTrue(branch.remove(m));
        assertNotNull(event[0]);
        assertEquals(branch, event[0].getSource());
        assertEquals(null, event[0].getNewValue());
        assertEquals(m, event[0].getOldValue());
    }

    /**
     * Test visiting mappings in a branch
     */
    public void testVisiting() {
        IBranchGraph graph = new BranchGraph("g");
        Branch branch = new Branch("a");
        assertTrue(graph.add(branch));
        Mapping m = new DepotPathMapping("m");
        assertTrue(graph.add(m));
        m.setSourceId(branch.getId());
        assertTrue(branch.add(m));

        final Mapping[] found = new Mapping[] { null };
        branch.accept(new IMappingVisitor() {

            public boolean visit(Mapping mapping, Branch branch) {
                found[0] = mapping;
                return true;
            }
        }, DepotPathMapping.TYPE, true, false);
        assertEquals(m, found[0]);

        found[0] = null;
        branch.accept(new IMappingVisitor() {

            public boolean visit(Mapping mapping, Branch branch) {
                found[0] = mapping;
                return true;
            }
        }, null, true, false);
        assertEquals(m, found[0]);

        found[0] = null;
        branch.accept(new IMappingVisitor() {

            public boolean visit(Mapping mapping, Branch branch) {
                found[0] = mapping;
                return true;
            }
        }, DepotPathMapping.TYPE, true, true);
        assertEquals(m, found[0]);

        found[0] = null;
        branch.accept(new IMappingVisitor() {

            public boolean visit(Mapping mapping, Branch branch) {
                found[0] = mapping;
                return true;
            }
        }, null, true, true);
        assertEquals(m, found[0]);

        assertTrue(branch.remove(m));
        m = new DepotPathMapping("m2");
        assertTrue(graph.add(m));
        m.setTargetId(branch.getId());
        assertTrue(branch.add(m));

        found[0] = null;
        branch.accept(new IMappingVisitor() {

            public boolean visit(Mapping mapping, Branch branch) {
                found[0] = mapping;
                return true;
            }
        }, DepotPathMapping.TYPE, true, true);
        assertEquals(m, found[0]);

        found[0] = null;
        branch.accept(new IMappingVisitor() {

            public boolean visit(Mapping mapping, Branch branch) {
                found[0] = mapping;
                return true;
            }
        }, null, true, true);
        assertEquals(m, found[0]);

        found[0] = null;
        branch.accept(new IMappingVisitor() {

            public boolean visit(Mapping mapping, Branch branch) {
                found[0] = mapping;
                return true;
            }
        }, DepotPathMapping.TYPE, false, true);
        assertEquals(m, found[0]);

        found[0] = null;
        branch.accept(new IMappingVisitor() {

            public boolean visit(Mapping mapping, Branch branch) {
                found[0] = mapping;
                return true;
            }
        }, null, false, true);
        assertEquals(m, found[0]);

        // Test visitor returning false
        final AtomicInteger count = new AtomicInteger(0);
        m = new DepotPathMapping("m3");
        assertTrue(graph.add(m));
        m.setSourceId(branch.getId());
        assertTrue(branch.add(m));
        m = new DepotPathMapping("m4");
        assertTrue(graph.add(m));
        m.setSourceId(branch.getId());
        assertTrue(branch.add(m));
        assertTrue(branch.getSourceMappings().length >= 2);
        branch.accept(new IMappingVisitor() {

            public boolean visit(Mapping mapping, Branch branch) {
                count.incrementAndGet();
                return false;
            }
        }, null, true, true);
        assertEquals(1, count.intValue());

        count.set(0);
        branch.accept(new IMappingVisitor() {

            public boolean visit(Mapping mapping, Branch branch) {
                count.incrementAndGet();
                return false;
            }
        }, DepotPathMapping.TYPE, true, true);
        assertEquals(1, count.intValue());
    }

}
