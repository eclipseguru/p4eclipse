/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.branchgraph.interchanges;

import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.BranchGraph;
import com.perforce.team.core.mergequest.model.DepotPathMapping;
import com.perforce.team.core.mergequest.model.Mapping;
import com.perforce.team.core.mergequest.model.Mapping.Direction;
import com.perforce.team.core.mergequest.model.MappingDescriptor.ChangeType;
import com.perforce.team.core.mergequest.processor.InterchangesProcessor;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4ConnectionProvider;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class InterchangesTest extends ProjectBasedTestCase {

    private BranchGraph graph;
    private DepotPathMapping mapping;

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        addFile(project.getFile(new Path("main/foo.txt")));
        addFile(project.getFile(new Path("rel1/bar.txt")));

        graph = new BranchGraph("bg1");
        Branch b1 = graph.createBranch(null);
        assertTrue(graph.add(b1));
        Branch b2 = graph.createBranch(null);
        assertTrue(graph.add(b2));
        mapping = graph.createDepotPathMapping(null);
        assertTrue(graph.add(mapping));
        mapping.setSourcePath("//depot/branches/main/...");
        mapping.setTargetPath("//depot/branches/rel1/...");
        assertTrue(mapping.connect(b1, b2));
        assertEquals(b1, mapping.getSource());
        assertEquals(b2, mapping.getTarget());
    }

    /**
     * Test interchanges processor with bad inputs
     */
    public void testProcessorNull() {
        final IP4Connection connection = createConnection();
        graph.setConnection(connection);

        InterchangesProcessor processor = new InterchangesProcessor(
                new IP4ConnectionProvider() {

                    public IP4Connection getConnection() {
                        return connection;
                    }
                }, graph);
        assertNull(processor.getTargetInterchanges(null));
        assertNull(processor.getSourceInterchanges(null));
        assertNull(processor.getLastRefreshDate(null));
        processor.clear(null);
        processor.refresh();
        processor.refresh((Mapping[]) null);
        processor.refresh((Runnable) null);
        processor.refresh(null, null);
        processor.refreshInterchanges();
        processor.refreshInterchanges((Mapping[]) null);
        processor.refreshInterchanges((Runnable) null);
        processor.refreshInterchanges(null, null);
    }

    /**
     * Test interchanges process with valid inputs
     */
    public void testProcessorValid() {
        final IP4Connection connection = createConnection();
        graph.setConnection(connection);
        mapping.setDirection(Direction.BOTH);

        InterchangesProcessor processor = new InterchangesProcessor(
                new IP4ConnectionProvider() {

                    public IP4Connection getConnection() {
                        return connection;
                    }
                }, graph);

        IP4SubmittedChangelist[] lists = processor
                .getSourceInterchanges(mapping);
        assertNull(lists);
        lists = processor.getTargetInterchanges(mapping);
        assertNull(lists);

        assertNull(processor.getLastRefreshDate(mapping));

        final boolean[] done = new boolean[] { false };
        processor.refresh(new Runnable() {

            public void run() {
                done[0] = true;
            }
        });
        while (!done[0]) {
            Utils.sleep(.1);
        }
        done[0] = false;
        processor.refreshInterchanges(new Runnable() {

            public void run() {
                done[0] = true;
            }
        });
        while (!done[0]) {
            Utils.sleep(.1);
        }

        lists = processor.getSourceInterchanges(mapping);
        assertNotNull(lists);
        assertEquals(1, lists.length);
        lists = processor.getTargetInterchanges(mapping);
        assertNotNull(lists);
        assertEquals(1, lists.length);

        assertNotNull(processor.getLastRefreshDate(mapping));
    }

    /**
     * Test status refresh
     */
    public void testRefresh() {
        final IP4Connection connection = createConnection();
        graph.setConnection(connection);
        mapping.setDirection(Direction.BOTH);

        InterchangesProcessor processor = new InterchangesProcessor(
                new IP4ConnectionProvider() {

                    public IP4Connection getConnection() {
                        return connection;
                    }
                }, graph);

        assertEquals(ChangeType.UNKNOWN, mapping.getSourceChange());
        assertEquals(-1, mapping.getLatestSource());
        assertEquals(0, mapping.getSourceToTargetCount());

        assertEquals(ChangeType.UNKNOWN, mapping.getTargetChange());
        assertEquals(-1, mapping.getLatestTarget());
        assertEquals(0, mapping.getTargetToSourceCount());

        final boolean[] done = new boolean[] { false };
        processor.refresh(new Runnable() {

            public void run() {
                done[0] = true;
            }
        });
        while (!done[0]) {
            Utils.sleep(.1);
        }

        assertEquals(ChangeType.VISIBLE_CHANGES, mapping.getSourceChange());
        assertEquals(ChangeType.VISIBLE_CHANGES, mapping.getTargetChange());
        assertTrue(mapping.getLatestSource() > 0);
        assertTrue(mapping.getLatestTarget() > 0);
        assertFalse(mapping.getLatestSource() == mapping.getLatestTarget());
    }

    /**
     * Test status refresh
     */
    public void testEventRefresh() {
        final IP4Connection connection = createConnection();
        graph.setConnection(connection);
        mapping.setDirection(Direction.BOTH);

        InterchangesProcessor processor = new InterchangesProcessor(
                new IP4ConnectionProvider() {

                    public IP4Connection getConnection() {
                        return connection;
                    }
                }, graph);

        final boolean[] done = new boolean[] { false };

        final Runnable waiter = new Runnable() {

            public void run() {
                done[0] = true;
            }
        };

        processor.refresh(waiter);
        while (!done[0]) {
            Utils.sleep(.1);
        }

        assertEquals(ChangeType.VISIBLE_CHANGES, mapping.getSourceChange());
        assertEquals(ChangeType.VISIBLE_CHANGES, mapping.getTargetChange());
        assertTrue(mapping.getLatestSource() > 0);
        assertTrue(mapping.getLatestTarget() > 0);

        mapping.setTargetPath(mapping.getSourcePath());

        done[0] = false;

        P4Runner.schedule(new P4Runnable() {

            @Override
            public void run(IProgressMonitor monitor) {
                waiter.run();
            }

        }, processor.getRule());

        while (!done[0]) {
            Utils.sleep(.1);
        }

        assertEquals(ChangeType.NO_CHANGES, mapping.getSourceChange());
        assertEquals(ChangeType.NO_CHANGES, mapping.getTargetChange());
        assertTrue(mapping.getLatestSource() > 0);
        assertTrue(mapping.getLatestTarget() > 0);
        assertEquals(mapping.getLatestSource(), mapping.getLatestTarget());

    }

    /**
     * Test bidirectional
     */
    public void testBidirectional() {
        final IP4Connection connection = createConnection();
        graph.setConnection(connection);
        mapping.setDirection(Direction.BOTH);
        assertEquals(-1, mapping.getLatestSource());
        assertEquals(-1, mapping.getLatestTarget());
        assertEquals(ChangeType.UNKNOWN, mapping.getSourceChange());
        assertEquals(ChangeType.UNKNOWN, mapping.getTargetChange());

        InterchangesProcessor processor = new InterchangesProcessor(
                new IP4ConnectionProvider() {

                    public IP4Connection getConnection() {
                        return connection;
                    }
                }, graph);
        final boolean[] done = new boolean[] { false };
        processor.refresh(new Runnable() {

            public void run() {
                done[0] = true;
            }
        });
        while (!done[0]) {
            Utils.sleep(.1);
        }
        assertTrue(mapping.getLatestSource() > 0);
        assertTrue(mapping.getLatestTarget() > 0);
        assertFalse(mapping.getLatestSource() == mapping.getLatestTarget());
        assertEquals(ChangeType.VISIBLE_CHANGES, mapping.getSourceChange());
        assertEquals(ChangeType.VISIBLE_CHANGES, mapping.getTargetChange());
        done[0] = false;
        processor.refreshInterchanges(new Runnable() {

            public void run() {
                done[0] = true;
            }
        });
        while (!done[0]) {
            Utils.sleep(.1);
        }
        assertEquals(1, mapping.getSourceToTargetCount());
        assertEquals(1, mapping.getTargetToSourceCount());
    }

    /**
     * Test source
     */
    public void testSource() {
        final IP4Connection connection = createConnection();
        graph.setConnection(connection);
        mapping.setDirection(Direction.SOURCE);
        assertEquals(-1, mapping.getLatestSource());
        assertEquals(-1, mapping.getLatestTarget());
        assertEquals(ChangeType.UNKNOWN, mapping.getSourceChange());
        assertEquals(ChangeType.UNKNOWN, mapping.getTargetChange());

        InterchangesProcessor processor = new InterchangesProcessor(
                new IP4ConnectionProvider() {

                    public IP4Connection getConnection() {
                        return connection;
                    }
                }, graph);
        final boolean[] done = new boolean[] { false };
        processor.refresh(new Runnable() {

            public void run() {
                done[0] = true;
            }
        });
        while (!done[0]) {
            Utils.sleep(.1);
        }
        assertTrue(mapping.getLatestSource() > 0);
        assertTrue(mapping.getLatestTarget() > 0);
        assertFalse(mapping.getLatestSource() == mapping.getLatestTarget());
        assertEquals(ChangeType.VISIBLE_CHANGES, mapping.getSourceChange());
        assertEquals(ChangeType.UNKNOWN, mapping.getTargetChange());
        done[0] = false;
        processor.refreshInterchanges(new Runnable() {

            public void run() {
                done[0] = true;
            }
        });
        while (!done[0]) {
            Utils.sleep(.1);
        }
        assertEquals(0, mapping.getSourceToTargetCount());
        assertEquals(1, mapping.getTargetToSourceCount());
    }

    /**
     * Test target
     */
    public void testTarget() {
        final IP4Connection connection = createConnection();
        graph.setConnection(connection);
        mapping.setDirection(Direction.TARGET);
        assertEquals(-1, mapping.getLatestSource());
        assertEquals(-1, mapping.getLatestTarget());
        assertEquals(ChangeType.UNKNOWN, mapping.getSourceChange());
        assertEquals(ChangeType.UNKNOWN, mapping.getTargetChange());

        InterchangesProcessor processor = new InterchangesProcessor(
                new IP4ConnectionProvider() {

                    public IP4Connection getConnection() {
                        return connection;
                    }
                }, graph);
        final boolean[] done = new boolean[] { false };
        processor.refresh(new Runnable() {

            public void run() {
                done[0] = true;
            }
        });
        while (!done[0]) {
            Utils.sleep(.1);
        }
        assertTrue(mapping.getLatestSource() > 0);
        assertTrue(mapping.getLatestTarget() > 0);
        assertFalse(mapping.getLatestSource() == mapping.getLatestTarget());
        assertEquals(ChangeType.UNKNOWN, mapping.getSourceChange());
        assertEquals(ChangeType.VISIBLE_CHANGES, mapping.getTargetChange());
        done[0] = false;
        processor.refreshInterchanges(new Runnable() {

            public void run() {
                done[0] = true;
            }
        });
        while (!done[0]) {
            Utils.sleep(.1);
        }
        assertEquals(1, mapping.getSourceToTargetCount());
        assertEquals(0, mapping.getTargetToSourceCount());
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/branches";
    }

}
