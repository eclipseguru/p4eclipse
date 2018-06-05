/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.editor.outline;

import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.Mapping;
import com.perforce.team.ui.LabelDecoratorAdapter;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.StyledLabelProvider;
import com.perforce.team.ui.mergequest.BranchWorkbenchAdapter;
import com.perforce.team.ui.mergequest.MappingWorkbenchAdapter.DepotPath;

import java.text.MessageFormat;

import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchMappingLabelProvider {

    private TreeViewer viewer;
    private WorkbenchLabelProvider wrapper;
    private Font bold;

    private IStyledLabelProvider styledLabelProvider;

    private ILabelDecorator decorator = new LabelDecoratorAdapter() {

        @Override
        public String decorateText(String text, Object element) {
            if (element instanceof MappingProxy) {
                Mapping mapping = ((MappingProxy) element).getMapping();
                return MessageFormat.format(
                        Messages.BranchMappingLabelProvider_MappingDetails,
                        text, mapping.getName());
            }
            return text;
        }

    };

    /**
     * Create a branch mapping label provider
     * 
     * @param viewer
     */
    public BranchMappingLabelProvider(TreeViewer viewer) {
        this.viewer = viewer;
        this.wrapper = new WorkbenchLabelProvider();
        this.styledLabelProvider = new StyledLabelProvider(wrapper) {

            Styler boldStyler = new Styler() {

                @Override
                public void applyStyles(TextStyle textStyle) {
                    textStyle.font = bold;
                }
            };

            @Override
            public StyledString getStyledText(Object element) {
                StyledString styled = new StyledString();
                if (element instanceof Branch) {
                    Branch branch = (Branch) element;
                    String text = wrapper.getText(element);
                    if (BranchWorkbenchAdapter.isImportant(branch.getType())) {
                        styled.append(text, boldStyler);
                    } else {
                        styled.append(text);
                    }
                    styled.append(MessageFormat.format(
                            Messages.BranchMappingLabelProvider_Count,
                            branch.getMappingCount()),
                            StyledString.COUNTER_STYLER);
                } else if (element instanceof DepotPath) {
                    DepotPath path = (DepotPath) element;
                    styled.append(path.getName());
                    styled.append(" : ", StyledString.QUALIFIER_STYLER); //$NON-NLS-1$
                    styled.append(getText(element),
                            StyledString.QUALIFIER_STYLER);
                } else {
                    styled.append(super.getStyledText(element));
                }
                return styled;
            }

            @Override
            public void dispose() {
                super.dispose();
                bold.dispose();
            }

        };
        this.bold = P4UIUtils.generateBoldFont(viewer.getTree().getDisplay(),
                viewer.getTree().getFont());
        DecoratingStyledCellLabelProvider labelProvider = new DecoratingStyledCellLabelProvider(
                styledLabelProvider, decorator, null);
        this.viewer.setLabelProvider(labelProvider);
    }
}
