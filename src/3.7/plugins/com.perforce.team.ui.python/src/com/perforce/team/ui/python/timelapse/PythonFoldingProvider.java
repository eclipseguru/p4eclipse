/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.perforce.team.ui.python.timelapse;

import org.eclipse.core.runtime.ILog;
import org.eclipse.dltk.python.core.PythonNature;
import org.eclipse.dltk.python.internal.ui.PythonUI;
import org.eclipse.dltk.python.ui.text.IPythonPartitions;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;

/**
 * Python folding provider
 */
public class PythonFoldingProvider extends ASTFoldingProvider {

    /**
     * @param viewer
     * @param support
     */
    public PythonFoldingProvider(ProjectionViewer viewer,
            ProjectionSupport support) {
        super(viewer, support);
    }

    /**
     * @see com.perforce.team.ui.python.timelapse.ASTFoldingProvider#getCommentPartition()
     */
    @Override
    protected String getCommentPartition() {
        return IPythonPartitions.PYTHON_COMMENT;
    }

    /**
     * @see com.perforce.team.ui.python.timelapse.ASTFoldingProvider#getPartition()
     */
    @Override
    protected String getPartition() {
        return IPythonPartitions.PYTHON_PARTITIONING;
    }

    /**
     * @see com.perforce.team.ui.python.timelapse.ASTFoldingProvider#getPartitionScanner()
     */
    @Override
    protected IPartitionTokenScanner getPartitionScanner() {
        return PythonUI.getDefault().getTextTools().getPartitionScanner();
    }

    /**
     * @see com.perforce.team.ui.python.timelapse.ASTFoldingProvider#getPartitionTypes()
     */
    @Override
    protected String[] getPartitionTypes() {
        return IPythonPartitions.PYTHON_PARTITION_TYPES;
    }

    /**
     * @see com.perforce.team.ui.python.timelapse.ASTFoldingProvider#getNatureId()
     */
    @Override
    protected String getNatureId() {
        return PythonNature.NATURE_ID;
    }

    /**
     * @see com.perforce.team.ui.python.timelapse.ASTFoldingProvider#getLog()
     */
    @Override
    protected ILog getLog() {
        return PythonUI.getDefault().getLog();
    }

    /**
     * @see com.perforce.team.ui.python.timelapse.ASTFoldingProvider#getHandle(com.perforce.team.ui.python.timelapse.ASTFoldingProvider.ScriptProjectionAnnotation)
     */
    @Override
    protected String getHandle(ScriptProjectionAnnotation annotation) {
        return annotation != null ? PythonNodeModel.getPythonHandle(annotation
                .getElement()) : null;
    }

}
