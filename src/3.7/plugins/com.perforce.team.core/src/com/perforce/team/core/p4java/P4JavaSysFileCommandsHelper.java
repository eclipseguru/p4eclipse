/*
 * Copyright 2009 Perforce Software Inc., All Rights Reserved.
 */
package com.perforce.team.core.p4java;

import com.perforce.p4java.impl.generic.sys.ISystemFileCommandsHelper;

import java.io.File;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;

/**
 * Simple helper class for P4Java's RPC implementation to help P4Java's pure
 * native RPC implementation do some of the things that it can't do under JDK 5
 * and / or JDK 6 -- currently mostly permissions- and symlink-related.
 * <p>
 * 
 * Class should be registered with the P4Java server factory before first server
 * retrieval call if you're on a JDK 5 platform or you want full file
 * permissions semantics on JDK 6.
 * <p>
 * 
 * Note that this class must be usable as a singleton instantiation, and any
 * changes here must keep thread safety in mind, and also not cause thread
 * blocking or unnecessary delays to the caller.
 * 
 * @version $Id$
 */

public class P4JavaSysFileCommandsHelper implements ISystemFileCommandsHelper {

    /**
     * Inlined here for Eclipse 3.2 compatibility
     */
    public static final int ATTRIBUTE_SYMLINK = 1 << 5;

    /**
     * @see com.perforce.p4java.impl.generic.sys.ISystemFileCommandsHelper#canExecute(java.lang.String)
     */
    public boolean canExecute(String fileName) {
        IFileStore lfNative = EFS.getLocalFileSystem().fromLocalFile(
                new File(fileName));

        IFileInfo info = lfNative.fetchInfo();

        return info != null && info.getAttribute(EFS.ATTRIBUTE_EXECUTABLE);
    }

    /**
     * @see com.perforce.p4java.impl.generic.sys.ISystemFileCommandsHelper#setExecutable(java.lang.String,
     *      boolean, boolean)
     */
    public boolean setExecutable(String fileName, boolean executable,
            boolean ownerOnly) {
        IFileStore lfNative = EFS.getLocalFileSystem().fromLocalFile(
                new File(fileName));

        IFileInfo info = lfNative.fetchInfo();

        boolean set = false;
        if (info != null) {
            info.setAttribute(EFS.ATTRIBUTE_EXECUTABLE, executable);

            try {
                lfNative.putInfo(info, EFS.SET_ATTRIBUTES, null);
            } catch (CoreException e) {
                set = false;
            }
            set = true;
        }

        return set;
    }

    /**
     * @see com.perforce.p4java.impl.generic.sys.ISystemFileCommandsHelper#setWritable(java.lang.String,
     *      boolean)
     */
    public boolean setWritable(String fileName, boolean writable) {
    	/*
    	 * job064738: files remain read-only after check-out.
    	 * 
    	 * Special process on Solaris for the following known issue:
    	 * 
    	 * Eclipse cannot make a readonly file writable on Solaris. 
    	 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=32396.
    	 * 
    	 * The workaround is to call Java API instead of using native API.
    	 * But this may not work when refactoring, since it still need
    	 * underline EFS support. Anyway, this change is still better 
    	 * than nothing.  
    	 */
        String os = Platform.getOS();
        if(os.toLowerCase().contains("solaris")){
        	new File(fileName).setWritable(writable);
        }
        
        IFileStore lfNative = EFS.getLocalFileSystem().fromLocalFile(
                new File(fileName));

        IFileInfo info = lfNative.fetchInfo();

        boolean set = false;
        if (info != null) {
            info.setAttribute(EFS.ATTRIBUTE_READ_ONLY, !writable);

            try {
                lfNative.putInfo(info, EFS.SET_ATTRIBUTES, null);
            } catch (CoreException e) {
                set = false;
            }
            set = true;
        }

        return set;
    }

    /**
     * @see com.perforce.p4java.impl.generic.sys.ISystemFileCommandsHelper#isSymlink(java.lang.String)
     */
    public boolean isSymlink(String fileName) {
        IFileStore lfNative = EFS.getLocalFileSystem().fromLocalFile(
                new File(fileName));

        IFileInfo info = lfNative.fetchInfo();

        return info != null && info.getAttribute(ATTRIBUTE_SYMLINK);
    }

    /**
     * @see com.perforce.p4java.impl.generic.sys.ISystemFileCommandsHelper#setReadable(java.lang.String,
     *      boolean, boolean)
     */
    public boolean setReadable(String fileName, boolean readable,
            boolean ownerOnly) {
        IFileStore lfNative = EFS.getLocalFileSystem().fromLocalFile(
                new File(fileName));

        IFileInfo info = lfNative.fetchInfo();

        boolean set = false;
        if (info != null) {
            info.setAttribute(EFS.ATTRIBUTE_READ_ONLY, readable);

            try {
                lfNative.putInfo(info, EFS.SET_ATTRIBUTES, null);
            } catch (CoreException e) {
                set = false;
            }
            set = true;
        }

        return set;
    }

    /**
     * @see com.perforce.p4java.impl.generic.sys.ISystemFileCommandsHelper#setOwnerReadOnly(java.lang.String)
     */
    public boolean setOwnerReadOnly(String fileName) {
        // Not supported by Eclipse API
        return false;
    }
}
