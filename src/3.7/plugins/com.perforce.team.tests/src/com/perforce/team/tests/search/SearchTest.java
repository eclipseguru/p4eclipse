/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.search;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.IFileLineMatch;
import com.perforce.p4java.option.server.MatchingLinesOptions;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.tests.ProjectBasedTestCase;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class SearchTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        IClient client = createConnection().getClient();

        IFile file = project.getFile("grep.txt");
        addFile(client, file, new ByteArrayInputStream("empty".getBytes()));
        addFile(client, file,
                new ByteArrayInputStream("testa\ntest".getBytes()));
        addFile(client, file,
                new ByteArrayInputStream("testab\ntesta".getBytes()));
    }

    /**
     * Test grepping the depot for head rev matches only
     */
    public void testSingleRevision() {
        IP4Connection connection = createConnection();
        IP4File file = (IP4File) connection.getResource(project
                .getFile("grep.txt"));
        assertNotNull(file);
        String search = "test";
        IFileLineMatch matches[] = connection.searchDepot(search);
        assertNotNull(matches);
        assertEquals(2, matches.length);
        for (IFileLineMatch match : matches) {
            assertNotNull(match.getDepotFile());
            assertEquals(file.getRemotePath(), match.getDepotFile());
            assertNotNull(match.getLine());
            assertTrue(match.getLine().contains(search));
            assertEquals(file.getHeadRevision(), match.getRevision());
            assertTrue(match.getLineNumber() >= 0);
        }
    }

    /**
     * Test grepping the depot for matches in revisions
     */
    public void testAllRevisions() {
        IP4Connection connection = createConnection();
        IP4File file = (IP4File) connection.getResource(project
                .getFile("grep.txt"));
        assertNotNull(file);
        String search = "test";
        MatchingLinesOptions options = new MatchingLinesOptions();
        options.setAllRevisions(true);
        IFileLineMatch matches[] = connection.searchDepot(search, options,
                IP4Connection.ROOT);
        assertNotNull(matches);
        assertEquals(4, matches.length);
        for (IFileLineMatch match : matches) {
            assertNotNull(match.getDepotFile());
            assertEquals(file.getRemotePath(), match.getDepotFile());
            assertNotNull(match.getLine());
            assertTrue(match.getLine().contains(search));
            assertTrue(match.getRevision() > 1);
            assertTrue(match.getLineNumber() >= 0);
        }
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/grep";
    }

}
