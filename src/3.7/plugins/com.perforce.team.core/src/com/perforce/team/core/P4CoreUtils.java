/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;

import com.perforce.p4java.CharsetDefs;
import com.perforce.p4java.core.file.FileAction;
import com.perforce.p4java.core.file.FileSpecOpStatus;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.impl.generic.core.file.FileSpec;
import com.perforce.p4java.impl.mapbased.server.Server;
import com.perforce.p4java.server.CmdSpec;
import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Folder;
import com.perforce.team.core.p4java.P4ProgressListener;
import com.perforce.team.core.p4java.ProgressMonitorProgressPresenter;

/**
 * P4 Core utility class
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public final class P4CoreUtils {
	private static Random rand = new Random(System.currentTimeMillis());

    /**
     * BUFFER_SIZE
     */
    public static final int BUFFER_SIZE = 8192;

    private static final boolean IS_MAC;
    private static final boolean IS_WINDOWS;
    private static final boolean IS_LINUX;
    private static final boolean IS_COCOA;

    static {
        String os = Platform.getOS();
        IS_MAC = Platform.OS_MACOSX.equals(os);
        IS_WINDOWS = Platform.OS_WIN32.equals(os);
        IS_LINUX = Platform.OS_LINUX.equals(os);
        if (IS_MAC) {
            // Only set cocoa flag on mac os x
            IS_COCOA = "cocoa".equals(Platform.getWS()); //$NON-NLS-1$
        } else {
            IS_COCOA = false;
        }
    }

    private static Comparator<IP4Changelist> CHANGELIST_COMPARATOR = null;

    private P4CoreUtils() {
        // Does nothing
    }

    /**
     * Get resource action path
     * 
     * @param resource
     * @return path that may be null
     */
    public static String getResourceActionPath(IResource resource) {
        StringBuilder path = null;
        if (resource != null) {
            IPath location = resource.getLocation();
            if (location != null) {
                location = location.makeAbsolute();
            }
            if (location != null) {
                path = new StringBuilder(location.toOSString());
                if (resource instanceof IContainer) {
                    int length = path.length();
                    if (length > 0) {
                        if (path.charAt(length - 1) != File.separatorChar) {
                            path.append(IP4Folder.DIR_ELLIPSIS);
                        }
                    }
                }
            }
        }
        return path != null ? path.toString() : null;
    }

    /**
     * Get common path for specified resource paths
     * 
     * @param resourcePaths
     * @return common path
     */
    public static String getCommonPath(String... resourcePaths) {
        if (resourcePaths != null) {
            return getCommonPath(Arrays.asList(resourcePaths));
        } else {
            return getCommonPath((Collection<String>) null);
        }

    }

    /**
     * Get common path for specified resource paths
     * 
     * @param resourcePaths
     * @return common path
     */
    public static String getCommonPath(Collection<String> resourcePaths) {
        StringBuilder path = null;
        String[] paths = null;
        if (resourcePaths != null && !resourcePaths.isEmpty()) {
            for (String resourcePath : resourcePaths) {
                String[] segments = resourcePath.split("/"); //$NON-NLS-1$
                if (paths != null) {
                    int common = 0;
                    int length = Math.min(paths.length, segments.length);
                    for (int i = 0; i < length; i++) {
                        if (!segments[i].equals(paths[i])) {
                            break;
                        } else {
                            common++;
                        }
                    }
                    String[] newPaths = new String[common];
                    System.arraycopy(segments, 0, newPaths, 0, common);
                    paths = newPaths;
                } else {
                    paths = new String[segments.length - 1];
                    System.arraycopy(segments, 0, paths, 0, segments.length - 1);
                }
            }
        }
        if (paths != null) {
            path = new StringBuilder("//"); //$NON-NLS-1$
            for (String segment : paths) {
                if (segment.length() > 0) {
                    path.append(segment);
                    path.append('/');
                }
            }
            path.append("..."); //$NON-NLS-1$
        }
        if (path == null) {
            path = new StringBuilder("//..."); //$NON-NLS-1$
        }
        return path.toString();
    }

    /**
     * Get name from depot path
     * 
     * @param depotPath
     * @return file name or empty string if not found
     */
    public static String getName(String depotPath) {
        String name = ""; //$NON-NLS-1$
        if (depotPath != null) {
            int lastSlash = depotPath.lastIndexOf('/');
            if (lastSlash >= 0 && lastSlash + 1 < depotPath.length()) {
                name = depotPath.substring(lastSlash + 1);
            }
        }
        return name;
    }

    /**
     * Convert an object to the specified class
     * 
     * @param <Converted>
     * @param object
     * @param target
     * @return - converted object or null if can't be converted
     */
    public static <Converted> Converted convert(Object object,
            Class<Converted> target) {
        if (object != null && target != null) {
            if (target.isInstance(object)) {
                @SuppressWarnings("unchecked")
                Converted converted = (Converted)object;
                return converted;
            } else if (object instanceof IAdaptable) {
                object = ((IAdaptable) object).getAdapter(target);
                if (object != null && target.isInstance(object)) {
                    @SuppressWarnings("unchecked")
                    Converted converted = (Converted)object;
                    return converted;
                }
            }
        }
        return null;
    }

    /**
     * Is mac os x the os?
     * 
     * @return - true if on mac os x
     */
    public static boolean isMac() {
        return IS_MAC;
    }

    /**
     * Is an Eclipse Mac OS X cocoa build being used?
     * 
     * @return - true if cocoa, false otherwise
     */
    public static boolean isCocoa() {
        return IS_COCOA;
    }

    /**
     * Is windows the os?
     * 
     * @return - true if on windows
     */
    public static boolean isWindows() {
        return IS_WINDOWS;
    }

    /**
     * Is linux the os?
     * 
     * @return - true if on linux
     */
    public static boolean isLinux() {
        return IS_LINUX;
    }

    /**
     * Get resource from object
     * 
     * @param obj
     * @return - resource or null if object couldn't be converted
     */
    public static IResource getResource(Object obj) {
        IResource resource = null;
        if (obj instanceof IResource) {
            resource = (IResource) obj;
        } else if (obj instanceof IAdaptable) {
            Object adapter = ((IAdaptable) obj).getAdapter(IResource.class);
            if (adapter instanceof IResource) {
                resource = (IResource) adapter;
            }
        }
        return resource;
    }

    /**
     * Sort the changelists in the list
     * 
     * @param lists
     */
    public static void sort(IP4Changelist[] lists) {
        if (CHANGELIST_COMPARATOR == null) {
            CHANGELIST_COMPARATOR = new Comparator<IP4Changelist>() {

                public int compare(IP4Changelist change1, IP4Changelist change2) {
                    return change1.getId() - change2.getId();
                }
            };
        }
        Arrays.sort(lists, CHANGELIST_COMPARATOR);
    }

    /**
     * Gets all the files either in the resources array or inside any containers
     * found in the resources array and sub-containers.
     * 
     * @param resources
     * @return - non-null list of files
     */
    public static List<IFile> getAllFiles(IResource[] resources) {
        List<IFile> files = new ArrayList<IFile>();
        if (resources != null) {
            for (IResource resource : resources) {
                if (resource instanceof IContainer) {
                    files.addAll(getAllChildren((IContainer) resource));
                } else if (resource instanceof IFile) {
                    files.add((IFile) resource);
                }
            }
        }
        return files;
    }

    /**
     * Gets all the children at all depths inside this container and
     * sub-containers
     * 
     * @param container
     * @return - non-null list of files
     */
    public static List<IFile> getAllChildren(IContainer container) {
        List<IFile> files = null;
        if (container != null) {
            try {
                files = getAllFiles(container.members());
            } catch (CoreException e) {
                PerforceProviderPlugin.logError(e);
                files = new ArrayList<IFile>();
            }
        } else {
            files = new ArrayList<IFile>();
        }
        return files;
    }

    /**
     * Copy the contents from one file to another
     * 
     * @param source
     * @param destination
     * @throws Exception
     */
//    public static void copyFile(File source, File destination) throws Exception {
//        ReadableByteChannel streamChannel = null;
//        WritableByteChannel fileChannel = null;
//        try {
//            FileInputStream stream = new FileInputStream(source);
//            if (stream != null) {
//                OutputStream output = new FileOutputStream(destination);
//                streamChannel = Channels.newChannel(stream);
//                fileChannel = Channels.newChannel(output);
//
//                ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
//                while (streamChannel.read(buffer) != -1) {
//                    buffer.flip();
//                    fileChannel.write(buffer);
//                    buffer.compact();
//                }
//                buffer.flip();
//                while (buffer.hasRemaining()) {
//                    fileChannel.write(buffer);
//                }
//            }
//        } finally {
//            if (streamChannel != null) {
//                try {
//                    streamChannel.close();
//                } catch (IOException e) {
//                    PerforceProviderPlugin.logError(e);
//                }
//            }
//            if (fileChannel != null) {
//                try {
//                    fileChannel.close();
//                } catch (IOException e) {
//                    PerforceProviderPlugin.logError(e);
//                }
//            }
//
//        }
//    }
    public static void copyFile(File source, File destination) throws Exception {
    	FileInputStream stream=null;
    	OutputStream output=null;
        try {
            stream = new FileInputStream(source);
            if (stream != null) {
                output = new FileOutputStream(destination);

                byte[] buffer=new byte[BUFFER_SIZE];
                int len=-1;
                while ((len=stream.read(buffer)) != -1) {
                	output.write(buffer,0,len);
                	output.flush();
                }
            }
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    PerforceProviderPlugin.logError(e);
                }
            }
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    PerforceProviderPlugin.logError(e);
                }
            }

        }
    }

    /**
     * Create a temp file from an input stream
     * 
     * @param stream
     * @return - temp file with contents of stream
     */
    public static File createFile(InputStream stream) {
        File file = null;
        if (stream != null) {
            ReadableByteChannel streamChannel = null;
            WritableByteChannel fileChannel = null;
            try {
                file = File.createTempFile("p4ws", ".tmp"); //$NON-NLS-1$ //$NON-NLS-2$
                file.deleteOnExit();
                final OutputStream output = new FileOutputStream(file);
                streamChannel = Channels.newChannel(stream);
                fileChannel = Channels.newChannel(output);

                ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
                while (streamChannel.read(buffer) != -1) {
                    buffer.flip();
                    fileChannel.write(buffer);
                    buffer.compact();
                }
                buffer.flip();
                while (buffer.hasRemaining()) {
                    fileChannel.write(buffer);
                }
            } catch (IOException e) {
                PerforceProviderPlugin.logError(e);
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
        return file;
    }

    /**
     * Get all files contained within a resource.
     * 
     * @param resource
     *            the resource to check for files
     * @param files
     *            found files are added to this list
     */
    protected static void getResourceFiles(IResource resource, List<IFile> files) {
        if (resource instanceof IFile) {
            if (!files.contains(resource)) {
                files.add((IFile) resource);
            }
        } else if (resource instanceof IContainer) {
            try {
                IResource[] members = ((IContainer) resource).members();
                for (int i = 0; i < members.length; i++) {
                    getResourceFiles(members[i], files);
                }
            } catch (CoreException e) {
                PerforceProviderPlugin.logError(e);
            }
        }
    }

    /**
     * Get all files contained within an array of resources.
     * 
     * @param resources
     *            the resources to check for files
     * @return the array of files contained with the resources
     */
    public static IFile[] getResourceFiles(IResource[] resources) {
        List<IFile> files = new ArrayList<IFile>();
        for (int i = 0; i < resources.length; i++) {
            getResourceFiles(resources[i], files);
        }
        return files.toArray(new IFile[files.size()]);
    }

    /**
     * Removes any characters that have a decimal char value that is less than
     * 32. This method will return the empty string for the case where the
     * specified string is null.
     * 
     * @param value
     * @return - fixed string, non-null but possibly empty
     */
    public static String removeWhitespace(String value) {
        if (value == null) {
            return ""; //$NON-NLS-1$
        }
        StringBuilder buffer = new StringBuilder(value.toString());
        for (int i = 0; i < buffer.length(); i++) {
            if (buffer.charAt(i) < 32) {
                buffer.setCharAt(i, ' ');
            }
        }
        return buffer.toString().trim();
    }
    
    public static boolean isEmpty(Collection<?> c){
        return c==null || c.isEmpty();
    }

    /**
     * delete the parent dir if it is empty.
     * 
     * @param file the file which has been deleted before this call.
     */
    public static void deleteEmptyParentDir(File file) {
        if(file==null)
            return;
        
        String p = file.getParent();
        
        if(p!=null){
            File d = new File(p);
            if(!d.exists()){ // parent may be delete, but we keep going in case parent'parent not
                deleteEmptyParentDir(d);
                return;
            }
            
            String[] c = d.list();
            if (c!=null && c.length==0) {
                if(d.delete())
                	deleteEmptyParentDir(d);
            }
        }
    }

    public static int getRandomInt(){
    	return Math.abs(rand.nextInt(Integer.MAX_VALUE));
    }

	public static List<IFileSpec> extractFileSpecs(
			IP4Connection connection, Map<String, Object>[] results) throws P4JavaException {
		List<IFileSpec> specList = new ArrayList<IFileSpec>();
		if(results!=null){
			for(Map<String, Object> map:results){
				IFileSpec spec=extractFileSpec(map, connection);
				if(spec!=null) 
					specList.add(spec);
			}
		}
		return specList;
	}

	public static List<IFileSpec> extractFileSpecs(
			IP4Connection connection, List<Map<String, Object>> results) throws P4JavaException {
		List<IFileSpec> specList = new ArrayList<IFileSpec>();
		if(results!=null){
			for(Map<String, Object> map:results){
				IFileSpec spec=extractFileSpec(map, connection);
				if(spec!=null) 
					specList.add(spec);
			}
		}
		return specList;
	}
	
	public static IFileSpec extractFileSpec(Map<String, Object> map, IP4Connection connection) throws P4JavaException {
		if (map.get("submittedChange") != null) { // see Changelist.java::submit(SubmitOptions opts) line 476-487
			Integer id = new Integer((String) map.get("submittedChange"));
			FileSpec spec = new FileSpec(FileSpecOpStatus.INFO, "Submitted as change " + id); // $NON-NLS-1$
			return spec;
		}else{ // if (map.containsKey("dir") ||map.containsKey("depotFile")||map.containsKey("clientFile")||map.containsKey("path") || map.containsKey("change")) { // $NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$ $NON-NLS-4$
			Server server = (Server) connection.getServer();
			IFileSpec spec = server.handleFileReturn(map, connection.getClient());
			return spec;
		}
	}

	public static P4ProgressListener createStreamCallback(IP4Connection connection, CmdSpec cmd,
			IProgressMonitor monitor) {
		return createStreamCallback(connection, cmd.name().toLowerCase(), monitor);
	}

	public static P4ProgressListener createStreamCallback(IP4Connection connection, String cmdName,
			IProgressMonitor monitor) {
		P4ProgressListener handler = new P4ProgressListener(cmdName, connection);
		ProgressMonitorProgressPresenter presenter = new ProgressMonitorProgressPresenter();
		presenter.setMonitor(monitor);
		handler.setPresenter(presenter);
		return handler;
	}
	
	public static String printMap(Map<String, Object> map) {
		StringBuilder sb=new StringBuilder();
		sb.append("{"); //$NON-NLS-1$
		for(Map.Entry<String,Object> entry: map.entrySet()){
			sb.append(entry.getKey()+","); //$NON-NLS-1$
			sb.append(entry.getValue()+"; "); //$NON-NLS-1$
		}
		sb.append("}"); //$NON-NLS-1$
		return sb.toString();
	}

	public static String getFileSpecsActionReport(List<IFileSpec> specs) {
		Map<FileAction, Integer> summary=new HashMap<FileAction, Integer>();
		for(IFileSpec spec: specs){
			if (spec.getOpStatus() == FileSpecOpStatus.VALID) {
				FileAction action = spec.getAction();
				if(!summary.containsKey(action)){
					summary.put(action, 0);
				}
				summary.put(action, summary.get(action)+1);
			}
		}
		
		final StringBuilder sb=new StringBuilder();
		for(FileAction act: FileAction.values()){
			if(summary.containsKey(act)){
				sb.append(MessageFormat.format("  {0} files {1}\n", summary.get(act), act.name().toLowerCase()));
			}
		}
		if(sb.length()>0)
			sb.deleteCharAt(sb.length()-1);
		
		return sb.toString();
	}

    
    /**
     * P4D/LINUX26X86_64/2011.1.MAIN-TEST_ONLY/370818 (2011/10/19)
     * @return Major and minor array, for example, return  int[]{2011,1} for server info P4D/LINUX26X86_64/2011.1/370818 (2011/10/19).
     */
    public static int[] getVersion(String serverInfo){
        int[] result=new int[2];
        Assert.isTrue(StringUtils.isNotEmpty(serverInfo));
        String[] list = serverInfo.split("/",4);
        Pattern reg=Pattern.compile("\\d{4}.\\d.*");
        if(reg.matcher(list[2]).matches()){
            String[] versions = list[2].split("\\.");
            result[0]=Integer.parseInt(versions[0]);
            result[1]=Integer.parseInt(versions[1]);
        }
        return result;
    }
    
    public static Charset charsetForName(String name){
    	try {
    		return Charset.forName(name);
		} catch (Exception e) {
			PerforceProviderPlugin.logWarning(e);
			return CharsetDefs.DEFAULT;
		}
    }
    
	// these should be in Java 7 java.util.Objects. But since we also need
	// support java 6, so we recreate these here.
    public static int hashCode(Object obj){
    	if(obj ==null)
    		return 0;
    	
    	return obj.hashCode();
    }
    
    public static boolean equals(Object obj1, Object obj2) {
    	if(obj1==null || obj2==null)
    		return obj1==obj2;
    	if(obj1==obj2)
    		return true;
    	return obj1.equals(obj2);
    }


}
