/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.timelapse;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface ITimeLapseListener {

    /**
     * Time-lapse editor specified has finished loading
     * 
     * @param editor
     *            - non-null time lapse editor
     */
    void loaded(TimeLapseEditor editor);

}
