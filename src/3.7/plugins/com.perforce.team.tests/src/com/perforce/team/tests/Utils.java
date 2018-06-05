/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.eclipse.compare.internal.ICompareUIConstants;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.intro.IIntroManager;
import org.eclipse.ui.intro.IIntroPart;
import org.osgi.framework.Bundle;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.decorator.OverlayIcon;
import com.perforce.team.ui.p4java.actions.RevertAction;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public final class Utils {

    /**
     * Get bundle path to bundle entry from the core tests plugin
     * 
     * @param bundleEntry
     * @return path
     * @throws IOException
     */
    public static String getBundlePath(String bundleEntry) throws IOException {
        return getBundlePath(PerforceTestsPlugin.getDefault().getBundle(),
                bundleEntry);
    }

    /**
     * Get bundle path to bundle entry
     * 
     * @param bundle
     * 
     * @param bundleEntry
     * @return path
     * @throws IOException
     */
    public static String getBundlePath(Bundle bundle, String bundleEntry)
            throws IOException {
        URL url = bundle.getEntry(bundleEntry);
        Assert.assertNotNull(bundleEntry, url);
        url = FileLocator.toFileURL(url);
        Assert.assertNotNull(bundleEntry, url);
        return url.getPath();
    }

    /**
     * Enlarge an image to the compare image width
     * 
     * @param orig
     * @return enlarged image
     */
    public static Image enlargeImage(Image orig) {
        OverlayIcon bigger = new OverlayIcon(orig, new ImageDescriptor[0],
                new int[0], ICompareUIConstants.COMPARE_IMAGE_WIDTH,
                orig.getBounds().height, 0, 0);
        return bigger.createImage();
    }

    /**
     * Gets a disabled action
     * 
     * @return - disabled action object
     */
    public static Action getDisabledAction() {
        Action action = new Action() {
        };
        action.setEnabled(false);
        return action;
    }

    /**
     * Add and submit a random file
     * 
     * @param prefix
     * @param project
     * @param connection
     * 
     * @return - created and submitted file
     * @throws Exception
     */
    public static IFile addSubmitRandom(String prefix, IProject project,
            IP4Connection connection) throws Exception {
        IFile file = project.getFile(prefix + "_random_"
                + System.currentTimeMillis() + ".txt");
        fillFile(file);
        addSubmit(file, connection);
        return file;
    }

    /**
     * Add and submit file
     * 
     * @param file
     * @param connection
     * 
     * @throws Exception
     */
    public static void addSubmit(IFile file, IP4Connection connection)
            throws Exception {
        IP4File p4File = (IP4File) connection.getResource(file);
        if (p4File.getHeadRevision() > 0) {
            throw new Exception("Head revision greater than zero before adding");
        }
        p4File.add(0);
        p4File.refresh();
        if (!p4File.openedForAdd()) {
            throw new Exception("File not opened for add");
        }
        IP4PendingChangelist defaultList = connection.getPendingChangelist(0);
        defaultList.submit("Created random file", new IP4File[] { p4File },new NullProgressMonitor());
        p4File.refresh();
        if (p4File.getHeadRevision() != 1) {
            throw new Exception("Head revision not 1 after submit");
        }
    }

    /**
     * Sleep and dispatch ui event in 100 ms intervals for the number of seconds
     * specified
     * 
     * @param seconds
     */
    public static void sleep(double seconds) {
        for (int i = 0; i < seconds * 10; i++) {
            try {
                while (Display.getCurrent().readAndDispatch())
                    ;
                Thread.sleep(100);
            } catch (Exception e) {
            } catch (Error e) {
            }
        }
    }

    /**
     * Set all the decorator preferences to off
     */
    public static void clearDecoratorPrefs() {
        getUIStore().setValue(IPerforceUIConstants.PREF_FILE_OPEN_ICON, 0);
        getUIStore().setValue(IPerforceUIConstants.PREF_FILE_SYNC_ICON, 0);
        getUIStore().setValue(IPerforceUIConstants.PREF_FILE_SYNC2_ICON, 0);
        getUIStore()
                .setValue(IPerforceUIConstants.PREF_FILE_UNRESOLVED_ICON, 0);
        getUIStore().setValue(IPerforceUIConstants.PREF_FILE_LOCK_ICON, 0);
        getUIStore().setValue(IPerforceUIConstants.PREF_FILE_OTHER_ICON, 0);
    }

    /**
     * Wait for jobs of family to finsish
     * 
     * @param family
     */
    public static void waitForFamily(Object family) {
        if (family == null) {
            return;
        }
        try {
            Job.getJobManager().join(family, null);
        } catch (OperationCanceledException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Wait for the {@link ResourcesPlugin#FAMILY_AUTO_BUILD} job finishes
     */
    public static void waitForBuild() {
        waitForFamily(ResourcesPlugin.FAMILY_AUTO_BUILD);
    }

    /**
     * Fills a file withs some sample content
     * 
     * @param file
     * @throws Exception
     */
    public static void fillFile(IFile file) throws Exception {
        fillFile(file, "/resources/Test.txt");
    }

    /**
     * Fill a file with content loaded from bundle at specified path
     * 
     * @param file
     * @param path
     * @throws Exception
     */
    public static void fillFile(IFile file, String path) throws Exception {
        URL fileUrl = PerforceTestsPlugin.getDefault().getBundle()
                .getEntry(path);
        fileUrl = FileLocator.toFileURL(fileUrl);
        fillFile(file, fileUrl.openStream());
    }

    /**
     * Fill a file the content of a stream
     * 
     * @param file
     * @param stream
     * @throws CoreException
     */
    public static void fillFile(IFile file, InputStream stream)
            throws CoreException {
        if (file.exists()) {
            file.setContents(stream, 0, null);
        } else {
            file.create(stream, true, null);
        }
    }

    /**
     * Fill a file with string content
     * 
     * @param file
     * @param content
     * @throws CoreException
     */
    public static void fillFileWithString(IFile file, String content)
            throws CoreException {
    	final boolean[] done=new boolean[]{false};
    	IProgressMonitor monitor=new IProgressMonitor() {
			
			@Override
			public void worked(int work) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void subTask(String name) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setTaskName(String name) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setCanceled(boolean value) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean isCanceled() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public void internalWorked(double work) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void done() {
				done[0]=true;
			}
			
			@Override
			public void beginTask(String name, int totalWork) {
				// TODO Auto-generated method stub
				
			}
		};
    	ByteArrayInputStream stream = new ByteArrayInputStream(content.getBytes());
    	if (file.exists()) {
            file.setContents(stream, 0, monitor);
        } else {
            file.create(stream, true, monitor);
        }
    	while(!done[0]){
    		sleep(.1);
    	}
    }

    /**
     * Gets the file contents as a string
     * 
     * @param file
     * @return - string of the file contents
     * @throws Exception
     */
    public static String getContent(IFile file) throws Exception {
        return getContent(file.getContents(true));
    }

    /**
     * Get the contents from an input stream reader into a string
     * 
     * @param inputReader
     * @return - string content of the input stream reader
     * @throws Exception
     */
    public static String getContent(InputStreamReader inputReader)
            throws Exception {
        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = new BufferedReader(inputReader);
        try {
            int read = reader.read();
            while (read != -1) {
                buffer.append((char) read);
                read = reader.read();
            }
        } finally {
            reader.close();
        }
        return buffer.toString();
    }

    /**
     * Get the contents from an input stream into a string
     * 
     * @param stream
     * @return - string content of the input stream
     * @throws Exception
     */
    public static String getContent(InputStream stream) throws Exception {
        return getContent(new InputStreamReader(stream));
    }

    /**
     * Delete and revert an array of projects
     * 
     * @param projects
     * @throws CoreException
     */
    public static void deleteAndRevert(IProject[] projects)
            throws CoreException {
        if (projects != null) {
            for (IProject project : projects) {
                if (project != null) {
                    RevertAction revertAction = new RevertAction();
                    revertAction.setAsync(false);
                    revertAction.selectionChanged(null,
                            new StructuredSelection(project));
                    revertAction.runAction(false);

                    if (project != null && project.exists()) {
                        project.refreshLocal(IResource.DEPTH_INFINITE, null);
                        project.accept(new IResourceVisitor() {

                            public boolean visit(IResource resource)
                                    throws CoreException {
                                ResourceAttributes attrs = resource
                                        .getResourceAttributes();
                                if (attrs != null) {
                                    attrs.setReadOnly(false);
                                    try {
                                        resource.setResourceAttributes(attrs);
                                    } catch (CoreException e) {
                                    }
                                }
                                return true;
                            }
                        });
                        project.delete(true, true, null);
                    }
                }
            }
        }
    }

    /**
     * Delete and revert a project
     * 
     * @param project
     * @throws CoreException
     */
    public static void deleteAndRevert(IProject project) throws CoreException {
        deleteAndRevert(new IProject[] { project });
    }

    /**
     * Gets the shell from the current display
     * 
     * @return - shell
     */
    public static Shell getShell() {
        Shell shell = Display.getCurrent().getActiveShell();
        if(shell==null)
        	shell= Display.getDefault().getActiveShell();
        return shell;
    }

    /**
     * Get the perforce ui plugin
     * 
     * @return - perforce ui plugin
     */
    public static PerforceUIPlugin getUIPlugin() {
        return PerforceUIPlugin.getPlugin();
    }

    /**
     * Get an image descriptor from the perforce ui plugin
     * 
     * @param id
     * @return - image descriptor
     */
    public static ImageDescriptor getUIDescriptor(String id) {
        return getUIPlugin().getImageDescriptor(id);
    }

    /**
     * Get the perforce ui plugin pref store
     * 
     * @return - pref store
     */
    public static IPreferenceStore getUIStore() {
        return getUIPlugin().getPreferenceStore();
    }

    /**
     * Close any open intro part
     */
    public static void closeIntro() {
        IIntroManager manager = PlatformUI.getWorkbench().getIntroManager();
        if (manager != null) {
            IIntroPart part = manager.getIntro();
            if (part != null) {
                manager.closeIntro(part);
            }
        }
    }

    public static String getErrorMessages(Collection<Throwable> set){
    	StringBuilder sb=new StringBuilder();
    	for(Throwable t : set){
    		sb.append(t.getMessage()+"\n");
    	}
    	return sb.toString();
    }

    public static String getErrorMessages(Throwable[] set){
    	StringBuilder sb=new StringBuilder();
    	for(Throwable t : set){
    		sb.append(t.getMessage()+"\n");
    	}
    	return sb.toString();
    }

    public static boolean waitForRefresh(){
    	return waitForRefresh(10);
    }
    
    public static boolean waitForRefresh(int maxSeconds){
    	final boolean[] done=new boolean[]{false};
    	WaitForRefresh refresh = new WaitForRefresh(){
    		@Override
    		protected void onRefreshDone() {
    			super.onRefreshDone();
    			done[0]=true;
    		}
    	};
    	int total=(int) (maxSeconds/0.1); // how many 0.1 secs
    	while(!done[0] && total>0){
    		if(refresh.isRefreshing())
				try {
					Thread.sleep(100); // 0.1 sec
					total--;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
    		else
    			break;
    	}
    	
    	return done[0];
    }
    
    private static class WaitForRefresh{
    	private Set<Job> fRefreshJobs=Collections.synchronizedSet(new HashSet<Job>());
    	private IJobChangeListener fJobListener= new IJobChangeListener() {
    		public void sleeping(IJobChangeEvent event) {}
    		public void scheduled(IJobChangeEvent event) {}
    		public void running(IJobChangeEvent event) {}
    		public void done(IJobChangeEvent event) {
        		fRefreshJobs.remove(event.getJob());
        		if (fRefreshJobs.isEmpty()) {
        			onRefreshDone();
        		}
    		}
    		public void awake(IJobChangeEvent event) {}
    		public void aboutToRun(IJobChangeEvent event) {}
    	};

    	protected void onRefreshDone() {
    		notify();
    	}
    	
		public WaitForRefresh() {
    		updateRefreshJobs(ResourcesPlugin.FAMILY_AUTO_REFRESH);
    		updateRefreshJobs(ResourcesPlugin.FAMILY_MANUAL_REFRESH);
    	}
		
		public boolean isRefreshing(){
    		if (fRefreshJobs.size() != 0) {
    			return true;
    		}
    		return false;
		}
    	
    	private void updateRefreshJobs(Object jobFamily) {
    		IJobManager jobManager = Job.getJobManager();

    		Job[] refreshJobs = jobManager.find(jobFamily);
    		if (refreshJobs != null) {
    			for (Job j : refreshJobs) {
    				if (fRefreshJobs.add(j)) {
    					j.addJobChangeListener(fJobListener);
    					// In case the job has finished in the meantime
    					if (j.getState() == Job.NONE) {
    						fRefreshJobs.remove(j);
    					}
    				}
    			}
    		}
    	}

    }
    
}
