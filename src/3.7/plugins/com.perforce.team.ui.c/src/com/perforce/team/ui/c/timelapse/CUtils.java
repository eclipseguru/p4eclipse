/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.c.timelapse;

import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.ui.text.FastCPartitionScanner;
import org.eclipse.cdt.internal.ui.text.FastCPartitioner;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public final class CUtils {

    /**
     * Get c document provider
     * 
     * @return - document provider
     */
    public static IDocumentProvider getProvider() {
        return CUIPlugin.getDefault().getDocumentProvider();
    }

    /**
     * Get working copy for an editor input
     * 
     * @param input
     * @return - working copty
     */
    public static IWorkingCopy getWorkingCopy(IEditorInput input) {
        return CUIPlugin.getDefault().getDocumentProvider()
                .getWorkingCopy(input);
    }

    /**
     * Create a c document partitioner
     * 
     * @return - document partitioner
     */
    public static IDocumentPartitioner createPartitioner() {
        return new FastCPartitioner(new FastCPartitionScanner(), new String[] {
                ICPartitions.C_MULTI_LINE_COMMENT,
                ICPartitions.C_SINGLE_LINE_COMMENT, ICPartitions.C_STRING,
                ICPartitions.C_CHARACTER, ICPartitions.C_PREPROCESSOR });
    }

}
