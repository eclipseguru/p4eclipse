/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.PlatformObject;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public abstract class P4Storage extends PlatformObject implements IStorage {

    /**
     * @see org.eclipse.core.resources.IStorage#getFullPath()
     */
    public IPath getFullPath() {
        return null;
    }

    /**
     * @see org.eclipse.core.resources.IStorage#getName()
     */
    public String getName() {
        return null;
    }

    /**
     * @see org.eclipse.core.resources.IStorage#isReadOnly()
     */
    public boolean isReadOnly() {
        return true;
    }

}
