/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.actions;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.Mapping;

import java.text.MessageFormat;

import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchIntegrateAction extends MappingIntegrateAction {

    /**
     * Create a integrate action to the specified target branch
     * 
     * @param mapping
     * @param reverse
     * @param isSource
     */
    public BranchIntegrateAction(Mapping mapping, boolean reverse,
            boolean isSource) {
        super(mapping, reverse);
        Branch branch = isSource ? mapping.getSource() : mapping.getTarget();
        setText(MessageFormat.format(Messages.BranchIntegrateAction_Label,
                branch.getName(), mapping.getName()));
        IWorkbenchAdapter adapter = P4CoreUtils.convert(branch,
                IWorkbenchAdapter.class);
        if (adapter != null) {
            setImageDescriptor(adapter.getImageDescriptor(branch));
        }
    }
}
