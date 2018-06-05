/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.map;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class MapParams {

    /**
     * PARAM_VECTOR_LENGTH
     */
    public static final int PARAM_VECTOR_LENGTH = 30;

    MapParam[] vector = new MapParam[PARAM_VECTOR_LENGTH];

    /**
     * Create new map params
     */
    public MapParams() {
        for (int i = 0; i < vector.length; i++) {
            vector[i] = new MapParam();
        }
    }

}
