/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.tooltip;

import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.Mapping;
import com.perforce.team.core.mergequest.model.Mapping.Direction;
import com.perforce.team.core.mergequest.model.MappingDescriptor.ChangeType;

import java.text.MessageFormat;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchToolTip extends BaseToolTip {

    private Branch branch;

    /**
     * @param control
     * @param branch
     */
    public BranchToolTip(Control control, Branch branch) {
        super(control);
        this.branch = branch;
    }

    /**
     * @see com.perforce.team.ui.mergequest.tooltip.BaseToolTip#createInnerContent(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createInnerContent(Composite parent) {
        CLabel label = new CLabel(parent, SWT.NONE);
        label.setText(branch.getName());

        int count = branch.getMappingCount();
        if (count != 1) {
            CLabel countLabel = new CLabel(parent, SWT.NONE);
            countLabel.setText(MessageFormat.format(
                    Messages.BranchToolTip_MultiConnectedMappings, count));
        } else {
            CLabel countLabel = new CLabel(parent, SWT.NONE);
            countLabel.setText(Messages.BranchToolTip_SingleConnectedMapping);
        }
        if (count > 0) {

            StringBuilder incoming = new StringBuilder(
                    Messages.BranchToolTip_IncomingIntegrations);

            StringBuilder outgoing = new StringBuilder(
                    Messages.BranchToolTip_OutgoingIntegrations);

            Set<String> outgoingNames = new TreeSet<String>();
            Set<String> incomingNames = new TreeSet<String>();

            for (Mapping mapping : branch.getTargetMappings()) {
                Branch source = mapping.getSource();
                if (source != null) {
                    if (mapping.getDirection() != Direction.TARGET
                            && mapping.getSourceChange() == ChangeType.VISIBLE_CHANGES) {
                        outgoingNames.add(source.getName());
                    }
                    if (mapping.getDirection() != Direction.SOURCE
                            && mapping.getTargetChange() == ChangeType.VISIBLE_CHANGES) {
                        incomingNames.add(source.getName());
                    }
                }
            }

            for (Mapping mapping : branch.getSourceMappings()) {
                Branch target = mapping.getTarget();
                if (target != null) {
                    if (mapping.getDirection() != Direction.TARGET
                            && mapping.getSourceChange() == ChangeType.VISIBLE_CHANGES) {
                        incomingNames.add(target.getName());
                    }
                    if (mapping.getDirection() != Direction.SOURCE
                            && mapping.getTargetChange() == ChangeType.VISIBLE_CHANGES) {
                        outgoingNames.add(target.getName());
                    }
                }
            }

            if (incomingNames.isEmpty()) {
                incoming.append('\n').append(Messages.BranchToolTip_None);
            } else {
                for (String name : incomingNames) {
                    incoming.append('\n').append(name);
                }
            }

            if (outgoingNames.isEmpty()) {
                outgoing.append('\n').append(Messages.BranchToolTip_None);
            } else {
                for (String name : outgoingNames) {
                    outgoing.append('\n').append(name);
                }
            }

            new CLabel(parent, SWT.NONE).setText(incoming.toString());
            new CLabel(parent, SWT.NONE).setText(outgoing.toString());

        }
    }
}
