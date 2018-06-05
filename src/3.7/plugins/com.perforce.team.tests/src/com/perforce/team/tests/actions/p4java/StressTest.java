/**
 * Copyright (c) 2012Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.actions.p4java;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.StructuredSelection;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.p4java.actions.RevertAction;

/**
 * @author ali
 */
public class StressTest extends ProjectBasedTestCase {

	/**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        addFile(project.getFile("plugin.xml"));
        addFile(project.getFile(new Path("META-INF/MANIFEST.MF")));
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p2012.2/revert5000";
    }

//    /**
//     * Test action enablement
//     */
//    public void testEnablement() {
//        IP4Connection connection = createConnection();
//        IP4PendingChangelist defaultList = connection.getPendingChangelist(0);
//        assertNotNull(defaultList);
//
//        Action wrap = Utils.getDisabledAction();
//        RevertAllAction action = new RevertAllAction();
//        action.selectionChanged(wrap, new StructuredSelection());
//        assertFalse(wrap.isEnabled());
//
//        action.selectionChanged(wrap, new StructuredSelection(defaultList));
//        assertTrue(wrap.isEnabled());
//    }

    /**
     * Tests the revert action
     */
    public void testAdd5000Action() {
    	
    	final int TOTAL=5000;
    	final String FOLDER = "newadd";
    	
        File pfolder = project.getLocation().toFile();
        File folder=new File(pfolder, FOLDER);
        folder.mkdirs();
        
        // create and add 5000 files to the project
        for(int i=0;i<TOTAL;i++){
            ReadableByteChannel streamChannel = null;
            WritableByteChannel fileChannel = null;
            String fileName="file_"+i;
            try {
                InputStream in = new ByteArrayInputStream(fileName.getBytes());
                if (in != null) {
                    OutputStream output = new FileOutputStream(new File(folder,fileName));
                    streamChannel = Channels.newChannel(in);
                    fileChannel = Channels.newChannel(output);

                    ByteBuffer buffer = ByteBuffer.allocateDirect(P4CoreUtils.BUFFER_SIZE);
                    while (streamChannel.read(buffer) != -1) {
                        buffer.flip();
                        fileChannel.write(buffer);
                        buffer.compact();
                    }
                    buffer.flip();
                    while (buffer.hasRemaining()) {
                        fileChannel.write(buffer);
                    }
                }
            } catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
                if (streamChannel != null) {
                    try {
                        streamChannel.close();
                    } catch (IOException e) {
                        PerforceProviderPlugin.logError(e);
                    }
                }
                if (fileChannel != null) {
                    try {
                        fileChannel.close();
                    } catch (IOException e) {
                        PerforceProviderPlugin.logError(e);
                    }
                }

            }

        }
        try {
			project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		} catch (CoreException e) {
			e.printStackTrace();
		}
        
        System.out.println(project.getLocation().toOSString());
        
        // mark new files for add
        long start=System.currentTimeMillis();
        markForAdd(project.getFolder(FOLDER));
        System.out.println("adding "+5000+" takes time (ms): "+(System.currentTimeMillis()-start));
        
        RevertAction revert = new RevertAction();
        revert.selectionChanged(null, new StructuredSelection(project.getFolder(FOLDER)));
        start=System.currentTimeMillis();
        revert.runAction(false);
        System.out.println("revert "+5000+" takes time (ms): "+(System.currentTimeMillis()-start));
    }

	private void markForAdd(IFolder folder) {
        IResource[] selected;
		try {
			selected = folder.members();
			P4Collection collection = P4ConnectionManager.getManager().createP4Collection();
			for (IResource select : selected) {
				if (select instanceof IResource) {
					// Use local path if any local resources are in the
					// selection
					collection.setType(IP4Resource.Type.LOCAL);
					if (!PerforceProviderPlugin
							.isIgnoredHint((IResource) select)) {
						IP4Resource resource = P4ConnectionManager.getManager()
								.getResource((IResource) select);
						collection.add(resource);
					}
				}
			}
			collection.add();
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

}
