package com.perforce.team.tests.streams;

import org.junit.Test;

import com.perforce.p4java.core.IStream;
import com.perforce.p4java.core.IStreamSummary;
import com.perforce.p4java.core.IStreamSummary.Type;
import com.perforce.p4java.impl.generic.core.Stream;
import com.perforce.p4java.server.IOptionsServer;
import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.tests.ConnectionBasedTestCase;

/**
 * Test the get streams performance.
 */
public class StreamsTest extends ConnectionBasedTestCase {
    final String SLASH="/"; 
    final String DEPOTPATH="//TestStreamDepot";

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
        parameters.setClient("ali_perforce_linux");
        parameters.setUser("ali");
        parameters.setPort("p4eclipse:1999");
        parameters.setPassword("ali");
        parameters.setCharset("utf8");
    }

    @Override
    protected void startServer() {
    }

	/**
	 * Test IOptionsServer.getStreams method
	 */
	public void createStreams() {
        String mainName="testmain";
        
        for(int i=0;i<10000;i++){
            String index="_"+String.format("%04d", i);
            createStream(mainName+index,DEPOTPATH+SLASH+mainName+index, IStreamSummary.Type.MAINLINE);
        }
	}
	
    private void createStream(String streamName, String streamPath, Type type) {
        IP4Connection connection = createConnection(parameters);
        IOptionsServer server = (IOptionsServer) connection.getServer();
        try {
            // Create a stream
            IStream newStream = Stream.newStream(server, streamPath,
                    type.name().toLowerCase(), null, null, null, null, null, null, null);

            String retVal = server.createStream(newStream);

            // The stream should be created
            assertNotNull(retVal);
            assertEquals(retVal, "Stream " + streamPath + " saved.");

            // Get the newly created stream
            IStream returnedStream = server.getStream(streamPath);
            assertNotNull(returnedStream);
        }catch(Throwable t){
            
        }
    }
    
    @Test
    public void testGetStreams(){
        IP4Connection connection = createConnection(parameters);
        long time = System.currentTimeMillis();
        connection.getFilteredStreams(false, null, null, 1);
        long t1 = System.currentTimeMillis()-time;
        connection.getFilteredStreams(false, null, null, 9999);
        long t9999 = System.currentTimeMillis() - time;
        System.out.println("t1="+t1+", t9999="+t9999);
    }   
    
}
