/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.wizards.io;

import com.perforce.team.core.mergequest.model.BranchGraph;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.ui.LabelDecoratorAdapter;
import com.perforce.team.ui.LabelProviderAdapter;
import com.perforce.team.ui.StyledLabelProvider;
import com.perforce.team.ui.mergequest.IP4BranchGraphConstants;
import com.perforce.team.ui.mergequest.P4BranchGraphPlugin;

import java.text.MessageFormat;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class CheckboxBranchGraphViewer {

    private CheckboxTableViewer viewer;
    private Image graphImage = null;
    private IBranchGraph[] graphs;

    /**
     * Create viewer
     * 
     * @param graphs
     */
    public CheckboxBranchGraphViewer(IBranchGraph[] graphs) {
        this.graphs = graphs;
    }

    /**
     * Get viewer
     * 
     * @return - viewer
     */
    public CheckboxTableViewer getViewer() {
        return viewer;
    }

    /**
     * Create controls
     * 
     * @param parent
     */
    public void createControl(Composite parent) {
        viewer = CheckboxTableViewer.newCheckList(parent, SWT.SINGLE
                | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        viewer.getTable().setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, true, true));
        this.graphImage = P4BranchGraphPlugin.getImageDescriptor(
                IP4BranchGraphConstants.EDITOR).createImage();
        final ILabelProvider baseLabelProvider = new LabelProviderAdapter() {

            @Override
            public void dispose() {
                super.dispose();
                if (graphImage != null) {
                    graphImage.dispose();
                }
            }

            public String getText(Object element) {
                return ((BranchGraph) element).getName();
            }

            @Override
            public Image getImage(Object element) {
                return graphImage;
            }

        };
        IBaseLabelProvider labelProvider = new DecoratingStyledCellLabelProvider(
                new StyledLabelProvider(baseLabelProvider),
                new LabelDecoratorAdapter() {

                    @Override
                    public String decorateText(String text, Object element) {
                        if (element instanceof BranchGraph) {
                            BranchGraph graph = (BranchGraph) element;
                            int mappingCount = graph.getMappings().length;
                            int branchCount = graph.getBranches().length;

                            StringBuilder builder = new StringBuilder(text);
                            builder.append(" : "); //$NON-NLS-1$
                            if (branchCount == 1) {
                                builder.append(MessageFormat
                                        .format(Messages.CheckboxBranchGraphViewer_SingleBranch,
                                                branchCount));
                            } else {
                                builder.append(MessageFormat
                                        .format(Messages.CheckboxBranchGraphViewer_MultipleBranches,
                                                branchCount));
                            }
                            builder.append(", "); //$NON-NLS-1$
                            if (mappingCount == 1) {
                                builder.append(MessageFormat
                                        .format(Messages.CheckboxBranchGraphViewer_SingleMapping,
                                                mappingCount));
                            } else {
                                builder.append(MessageFormat
                                        .format(Messages.CheckboxBranchGraphViewer_MultipleMappings,
                                                mappingCount));
                            }

                            return builder.toString();
                        }
                        return text;
                    }

                }, null);
        viewer.setLabelProvider(labelProvider);
        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setInput(this.graphs);
    }
}
