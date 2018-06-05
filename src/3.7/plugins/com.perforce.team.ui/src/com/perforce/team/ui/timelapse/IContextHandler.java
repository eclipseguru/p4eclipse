/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.timelapse;

import com.perforce.team.core.p4java.IP4File;

import org.eclipse.core.runtime.content.IContentType;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public interface IContextHandler {

    /**
     * Context handler for runtime checking of whether a time lapse editor
     * should be open for the specified file and content type. This method
     * should validate that time lapse editor pre-requisites are met and should
     * prompt the user for any actions that need to be taken before the time
     * lapse editor will be open via call to
     * {@link IDE#openEditor(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IEditorInput, String)}
     * 
     * @param type
     * @param editorId
     * @param file
     * @return - boolean to open the time lapse editor, false to not
     */
    boolean timelapseRequested(IContentType type, String editorId, IP4File file);

}
