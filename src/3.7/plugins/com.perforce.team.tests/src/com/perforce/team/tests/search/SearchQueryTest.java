/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.search;

import com.perforce.p4java.client.IClient;
import com.perforce.team.core.p4java.IP4Branch;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.search.query.QueryOptions;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.ui.search.query.P4SearchQuery;
import com.perforce.team.ui.search.results.P4SearchResult;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.search.ui.ISearchResult;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class SearchQueryTest extends ProjectBasedTestCase {

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
     * Test query
     */
    public void testQuery() {
        QueryOptions options = new QueryOptions("test");
        options.addPath(IP4Connection.ROOT);
        IP4Connection connection = createConnection();
        P4SearchQuery query = new P4SearchQuery(connection, options);
        assertNotNull(query.getLabel());
        assertNotNull(query.getText());
        assertNotNull(query.getSearchResult());
        assertTrue(query.canRerun());
        assertTrue(query.canRunInBackground());
        assertEquals(Status.OK_STATUS, query.run(new NullProgressMonitor()));

        ISearchResult result = query.getSearchResult();
        assertNotNull(result);
        assertNotNull(result.getLabel());
        assertNotNull(result.getTooltip());
        assertNotNull(result.getImageDescriptor());
        assertEquals(query, result.getQuery());
        assertTrue(result instanceof P4SearchResult);
        P4SearchResult p4Result = (P4SearchResult) result;
        assertEquals(2, p4Result.getMatchCount());

        assertEquals(connection, query.getAdapter(IP4Connection.class));
        assertEquals(connection, query.getAdapter(IP4Resource.class));
        assertNull(query.getAdapter(IP4Branch.class));
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/grep";
    }

}
