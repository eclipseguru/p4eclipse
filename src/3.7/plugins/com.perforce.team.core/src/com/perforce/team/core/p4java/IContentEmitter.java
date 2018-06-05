/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

/**
 * Interface for classes that can emit string representations of themselves for
 * different content types.
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IContentEmitter {

    /**
     * Gets this object as a string represenation of the passed in content type
     * 
     * @param contentType
     * @return - string representation
     */
    String getAs(String contentType);

    /**
     * Gets the possible content types that this object can be converted to
     * 
     * @return - string array of content types
     */
    String[] getSupportedTypes();

}
