/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.tooltip;

import java.text.MessageFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.DepotPathMapping;
import com.perforce.team.core.mergequest.model.Mapping;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class MappingToolTip extends BaseToolTip {

    private Mapping mapping = null;

    /**
     * @param control
     * @param mapping
     */
    public MappingToolTip(Control control, Mapping mapping) {
        super(control);
        this.mapping = mapping;
    }

    /**
     * @see com.perforce.team.ui.mergequest.tooltip.BaseToolTip#createInnerContent(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createInnerContent(Composite parent) {
        CLabel mappingLabel = new CLabel(parent, SWT.NONE);
        mappingLabel.setText(mapping.getName());

        StringBuilder sourceText = new StringBuilder();
        StringBuilder targetText = new StringBuilder();

        Branch source = mapping.getSource();
        if (source != null) {
            sourceText.append(MessageFormat.format(
                    Messages.MappingToolTip_Source, mapping.getSource()
                            .getName()));
        }
        Branch target = mapping.getTarget();
        if (target != null) {
            targetText.append(MessageFormat.format(
                    Messages.MappingToolTip_Target, mapping.getTarget()
                            .getName()));
        }

        if (mapping instanceof DepotPathMapping) {
            DepotPathMapping dMapping = (DepotPathMapping) mapping;
            sourceText.append('\n').append(
                    Messages.MappingToolTip_SourcePath
                            + dMapping.getSourcePath());
            targetText.append('\n').append(
                    Messages.MappingToolTip_TargetPath
                            + dMapping.getTargetPath());
        }

        if (sourceText.length() > 0) {
            CLabel sourceLabel = new CLabel(parent, SWT.NONE);
            sourceLabel.setText(sourceText.toString());
        }
        if (targetText.length() > 0) {
            CLabel targetLabel = new CLabel(parent, SWT.NONE);
            targetLabel.setText(targetText.toString());
        }
    }
}
