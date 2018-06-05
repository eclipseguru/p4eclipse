package com.perforce.team.tests.symlink;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;

import com.perforce.p4java.CharsetDefs;
import com.perforce.p4java.impl.mapbased.rpc.sys.RpcPerforceFileType;
import com.perforce.team.tests.P4TestCase;

/**
 * @author ali
 */
public class SymlinkTest extends P4TestCase {

	File folder;
	File folderlink; // link to folder
	File file;
	File filelink;
	
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        log("Starting test: " + toString());
        folder = new File("/tmp/folder/subfolder");
        folder.mkdirs();
        
        file = new File(folder, "foo");
		try {
			// Create file
			FileWriter fstream = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("Contents of a linked file");
			// Close the output stream
			out.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		
		Process process = Runtime.getRuntime().exec("ln -s /tmp/folder/subfolder/foo /tmp/link");
	    InputStream in = process.getInputStream();
	    while ((in.read()) != -1) {
	    }
	    in.close();

	    process = Runtime.getRuntime().exec("ln -s /tmp/folder/subfolder /tmp/folderlink");
	    in = process.getInputStream();
	    while ((in.read()) != -1) {
	    }
	    in.close();
	    
	    folderlink = new File("/tmp/folder/subfolder");
	    filelink = new File("/tmp/link");
	    
	    file.deleteOnExit();
	    filelink.deleteOnExit();
	    folder.deleteOnExit();
	    folderlink.deleteOnExit();
    }
    
    @Override
    protected void tearDown() throws Exception {
        log("Stopping test: " + toString());
        
        file.delete();
        filelink.delete();
        folder.delete();
        folderlink.delete();
        super.tearDown();
    }

    /**
     * Test link to file
     */
    public void testPath() {
    	RpcPerforceFileType type = RpcPerforceFileType.inferFileType(filelink, false, CharsetDefs.UTF8);
    	assertTrue(type==RpcPerforceFileType.FST_SYMLINK);

    	type = RpcPerforceFileType.inferFileType(folderlink, false, CharsetDefs.UTF8);
//    	try {
//    		assertTrue(!folderlink.getAbsolutePath().equals(folderlink.getCanonicalPath()));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
    	
    	assertTrue(type!=RpcPerforceFileType.FST_SYMLINK);
    }


}
