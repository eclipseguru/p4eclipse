/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.editor;

import com.perforce.team.core.mergequest.processor.InterchangesProcessor;
import com.perforce.team.core.p4java.IP4ConnectionProvider;

import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.ui.IEditorPart;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IBranchGraphEditor extends IEditorPart, IPageChangeProvider,
        IP4ConnectionProvider {

    /**
     * Get interchanges processor
     * 
     * @return - processor
     */
    InterchangesProcessor getProcessor();

    /**
     * Get active graph page
     * 
     * @return - branch graph page
     */
    IBranchGraphPage getActiveGraphPage();

}
