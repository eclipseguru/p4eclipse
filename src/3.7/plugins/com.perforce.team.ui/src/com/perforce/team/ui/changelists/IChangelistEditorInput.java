/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.changelists;

import com.perforce.team.core.p4java.IP4Changelist;

import org.eclipse.ui.IEditorInput;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IChangelistEditorInput extends IEditorInput {

    IP4Changelist getChangelist();

}
