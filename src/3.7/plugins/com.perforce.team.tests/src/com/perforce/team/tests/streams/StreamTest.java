/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.streams;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.perforce.p4java.core.IStreamSummary;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.option.server.GetStreamsOptions;
import com.perforce.p4java.server.IOptionsServer;
import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.IConstants;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Stream;
import com.perforce.team.core.p4java.P4Stream;
import com.perforce.team.tests.ConnectionBasedTestCase;

/**
 * @author ali
 */
public class StreamTest extends ConnectionBasedTestCase {

    
    /**
     * @see com.perforce.team.tests.ConnectionBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    /**
     * @throws Exception
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    public void tearDown() throws Exception {
        try {

        } finally {
            super.tearDown();
        }
    }

    @Override
    protected void initParameters() {
        parameters = new ConnectionParameters();
        parameters.setClient("abcde");
        parameters.setUser("ali");
        parameters.setPort("play:1999");
        parameters.setPassword("changeitnow");
    }

    @Override
    protected void startServer() {
    }
    
    private List<IStreamSummary> getStreams(IP4Connection connection, String path)
            throws P4JavaException {
        List<IStreamSummary> streams = new ArrayList<IStreamSummary>();
        if(path==null)
            path = "//...";
        if (connection == null || path == null)
            return streams;
        if (!(connection.getServer() instanceof IOptionsServer))
            return streams;
        IOptionsServer os = (IOptionsServer)connection.getServer();
        List<String> paths = new ArrayList<String>();
        paths.add(path);
        streams = os.getStreams(paths, null);
        return streams;
    }

    private List<IStreamSummary> getFilteredStreams(IP4Connection connection, String filter)
            throws P4JavaException {
        List<IStreamSummary> streams = new ArrayList<IStreamSummary>();
        String path = "//...";
        if (connection == null || path == null)
            return streams;
        if (!(connection.getServer() instanceof IOptionsServer))
            return streams;
        IOptionsServer os = (IOptionsServer)connection.getServer();
        List<String> paths = new ArrayList<String>();
        paths.add(path);
        
        GetStreamsOptions opts = new GetStreamsOptions();
        opts.setFields("Stream,Owner,Parent");
//        opts.setMaxResults(10);
        opts.setFilter(filter);

        streams = os.getStreams(paths, opts);

        return streams;
    }
    
    private String printStreamSummary(IStreamSummary sum){
        StringBuilder sb=new StringBuilder();
        sb.append("Name="+sum.getName()+IConstants.COMA);
        sb.append("Parent="+sum.getParent()+IConstants.COMA);
        sb.append("Type="+sum.getType()+IConstants.COMA);
        sb.append("Owner="+sum.getOwnerName()+IConstants.COMA);
        sb.append("Description="+sum.getDescription()+IConstants.RETURN);
        return sb.toString();
    }
    
  
//    /**
//     * Tests getStreams
//     */
//    public void testGetStreams() {
//        IP4Connection connection = createConnection(parameters);
//        try {
//            List<IP4Stream> streams = connection.getFilteredStreams(null, null, 0);
//            
//            assertNotNull(streams);
//            assertTrue(streams.size()>0);
//
//            StreamCache cache = new StreamCache();
//            cache.clear(connection);
//            cache.add(connection, streams);
//            
//            for(IP4Stream sum: streams){
//                assertNotNull(cache.get(connection, sum.getStreamSummary().getStream()));
//                assertEquals(sum.getStreamSummary().getStream(), cache.get(connection, sum.getStreamSummary().getStream()).getStreamSummary().getStream());
//            }
//            
////            System.out.println(ReflectionToStringBuilder.toString(streams));
////            System.out.println(ReflectionToStringBuilder.toString(cache));
//
//            StreamsViewTreeRoot root = StreamsViewTreeNode.constructTree(streams, cache);
//            System.out.println(root.toString(IConstants.EMPTY_STRING));
//            
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        
//    }

    public void testGetStreams2() {
        IP4Connection connection = createConnection(parameters);
        try {
            List<IP4Stream> streams = connection.getFilteredStreams(false, null, null, 0);
            
            assertNotNull(streams);
            assertTrue(streams.size()>0);

            P4Stream.StreamCache cache = new P4Stream.StreamCache();
            cache.clear(connection);
            cache.add(connection, streams);
            
            for(IP4Stream sum: streams){
                assertNotNull(cache.get(connection, sum.getStreamSummary().getStream()));
                assertEquals(sum.getStreamSummary().getStream(), cache.get(connection, sum.getStreamSummary().getStream()).getStreamSummary().getStream());
            }
            
//            System.out.println(ReflectionToStringBuilder.toString(streams));
//            System.out.println(ReflectionToStringBuilder.toString(cache));

            P4Stream root = P4Stream.constructTree(streams, cache);
            System.out.println(P4Stream.toString(IConstants.EMPTY_STRING,root));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    @Test
    public void testGetStream() {
        IP4Connection connection = createConnection(parameters);
        try {
            IP4Stream stream = connection.getStream("//StreamLiz/main");
            
            assertNotNull(stream);

            System.out.println(P4Stream.toString(IConstants.EMPTY_STRING,stream));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
//    /**
//     * Tests getStreams
//     */
//    public void testGetFilteredStreams() {
//        IP4Connection connection = createConnection(parameters);
//        try {
//            List<IP4Stream> streams = connection.getStreams(connection, null);
//            
//            assertNotNull(streams);
//            assertTrue(streams.size()>0);
//
//            StreamSummaryCache cache = new StreamSummaryCache();
//            cache.clear(connection);
//            cache.add(connection, streams);
//            
//            for(IStreamSummary sum: streams){
//                assertNotNull(cache.get(connection, sum.getStream()));
//                assertEquals(sum.getStream(), cache.get(connection, sum.getStream()).getStream());
//            }
//            
////            streams = getFilteredStreams(connection, "Owner=perforce");
//            streams = getFilteredStreams(connection, null);
//            StreamsViewTreeRoot root = StreamsViewTreeNode.constructTree(connection, streams, cache);
//            System.out.println(root.toString(IConstants.EMPTY_STRING));
//            
//        } catch (P4JavaException e) {
//            e.printStackTrace();
//        }
//        
//    }

}
