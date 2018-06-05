/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest;

import com.perforce.team.core.mergequest.model.BranchGraph;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.ui.LabelDecoratorAdapter;
import com.perforce.team.ui.LabelProviderAdapter;
import com.perforce.team.ui.StyledLabelProvider;
import com.perforce.team.ui.dialogs.P4StatusDialog;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ReorderGraphDialog extends P4StatusDialog {

    private TableViewer viewer;
    private Image graphImage;
    private List<IBranchGraph> pages = new ArrayList<IBranchGraph>();

    /**
     * @param parent
     * @param graphs
     */
    public ReorderGraphDialog(Shell parent, IBranchGraph[] graphs) {
        super(parent);
        setTitle(Messages.ReorderGraphDialog_DialogTitle);
        setModalResizeStyle();
        this.pages = new ArrayList<IBranchGraph>(Arrays.asList(graphs));
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite c = (Composite) super.createDialogArea(parent);

        Composite displayArea = new Composite(c, SWT.NONE);
        displayArea.setLayout(new GridLayout(2, false));
        displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        viewer = new TableViewer(displayArea, SWT.SINGLE | SWT.V_SCROLL
                | SWT.H_SCROLL | SWT.BORDER);
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
                                        .format(Messages.ReorderGraphDialog_SingleBranch,
                                                branchCount));
                            } else {
                                builder.append(MessageFormat
                                        .format(Messages.ReorderGraphDialog_MultipleBranches,
                                                branchCount));
                            }
                            builder.append(", "); //$NON-NLS-1$
                            if (mappingCount == 1) {
                                builder.append(MessageFormat
                                        .format(Messages.ReorderGraphDialog_SingleMapping,
                                                mappingCount));
                            } else {
                                builder.append(MessageFormat
                                        .format(Messages.ReorderGraphDialog_MultipleMappings,
                                                mappingCount));
                            }

                            return builder.toString();
                        }
                        return text;
                    }

                }, null);
        viewer.setLabelProvider(labelProvider);
        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setInput(this.pages);

        Composite buttons = new Composite(displayArea, SWT.NONE);
        GridLayout bLayout = new GridLayout(1, true);
        bLayout.marginHeight = 0;
        bLayout.marginWidth = 0;
        buttons.setLayout(bLayout);
        buttons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));

        Button moveUp = new Button(buttons, SWT.FLAT | SWT.PUSH);
        moveUp.setText(Messages.ReorderGraphDialog_MoveUp);
        moveUp.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                BranchGraph graph = (BranchGraph) ((IStructuredSelection) viewer
                        .getSelection()).getFirstElement();
                if (graph != null) {
                    int index = pages.indexOf(graph);
                    if (index > 0) {
                        pages.remove(index);
                        index--;
                        pages.add(index, graph);
                        viewer.refresh();
                    }
                }
            }

        });
        moveUp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Button moveDown = new Button(buttons, SWT.FLAT | SWT.PUSH);
        moveDown.setText(Messages.ReorderGraphDialog_MoveDown);
        moveDown.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                BranchGraph graph = (BranchGraph) ((IStructuredSelection) viewer
                        .getSelection()).getFirstElement();
                if (graph != null) {
                    int index = pages.indexOf(graph);
                    if (index + 1 < pages.size()) {
                        pages.remove(index);
                        index++;
                        pages.add(index, graph);
                        viewer.refresh();
                    }
                }
            }

        });
        moveDown.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        return c;
    }

    /**
     * Get selected re-ordering
     * 
     * @return - non-null but possibly empty array
     */
    public BranchGraph[] getOrder() {
        return pages.toArray(new BranchGraph[pages.size()]);
    }

}
