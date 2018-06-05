/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.figures;

import com.perforce.team.core.mergequest.model.BranchSpecMapping;
import com.perforce.team.core.mergequest.model.DepotPathMapping;
import com.perforce.team.core.mergequest.model.Mapping;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.mergequest.IP4BranchGraphConstants;
import com.perforce.team.ui.mergequest.actions.MappingDisplayAction;
import com.perforce.team.ui.mergequest.actions.MappingIntegrateAction;
import com.perforce.team.ui.mergequest.editor.GraphContext;
import com.perforce.team.ui.mergequest.parts.SharedResources;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class CalloutFigure extends RoundedRectangle {

    /**
     * 
     * @param context
     * @param mapping
     * @param images
     */
    public CalloutFigure(GraphContext context, Mapping mapping,
            SharedResources images) {
        this.setBorder(new MarginBorder(1));
        this.setOpaque(true);

        this.setLayoutManager(new GridLayout(2, false));

        createHeader(mapping, images);
        createLinks(context, mapping, images);
    }

    private void createHeader(Mapping mapping, SharedResources images) {
        GridData data = new GridData(SWT.BEGINNING, SWT.FILL, true, false);
        data.horizontalSpan = 3;
        data.horizontalIndent = 21;

        if (mapping instanceof DepotPathMapping) {
            this.add(new Label(images
                    .getImage(IP4BranchGraphConstants.DEPOT_PATH_MAPPING)));

            DepotPathMapping dpm = (DepotPathMapping) mapping;
            Label source = new Label(Messages.CalloutFigure_SourcePath
                    + dpm.getSourcePath());
            this.add(source);
            Label target = new Label(Messages.CalloutFigure_TargetPath
                    + dpm.getTargetPath());
            this.add(target);
            this.getLayoutManager().setConstraint(target, data);
        } else if (mapping instanceof BranchSpecMapping) {
            this.add(new Label(images.getImage(PerforceUIPlugin
                    .getDescriptor(IPerforceUIConstants.IMG_BRANCH))));

            Label name = new Label(Messages.CalloutFigure_SpecName
                    + mapping.getName());
            this.add(name);
        }
    }

    private void createLinks(GraphContext context, Mapping mapping,
            SharedResources images) {
        LinkFigure integTarget = createBulletLink(images);
        integTarget.setText(Messages.CalloutFigure_IntegrateTo
                + mapping.getTarget().getName());
        integTarget.setAction(new MappingIntegrateAction(mapping));
        this.add(new Label());
        this.add(integTarget);

        LinkFigure integSource = createBulletLink(images);
        integSource.setText(Messages.CalloutFigure_IntegrateTo
                + mapping.getSource().getName());
        integSource.setAction(new MappingIntegrateAction(mapping, true));
        this.add(new Label("")); //$NON-NLS-1$
        this.add(integSource);

        LinkFigure viewChanges = createBulletLink(images);
        viewChanges.setText(Messages.CalloutFigure_ShowChangelists);
        viewChanges.setAction(new MappingDisplayAction(context, mapping));
        this.add(new Label());
        this.add(viewChanges);

    }

    private LinkFigure createBulletLink(SharedResources images) {
        LinkFigure link = new LinkFigure();
        link.setForegroundColor(images.getColor(new RGB(0, 0x33, 0x99)));
        link.setIcon(images.getImage("icons/bullet.png")); //$NON-NLS-1$
        link.setIconTextGap(0);
        this.add(link);
        return link;
    }

    /**
     * @see org.eclipse.draw2d.RoundedRectangle#outlineShape(org.eclipse.draw2d.Graphics)
     */
    @Override
    protected void outlineShape(Graphics graphics) {
        graphics.setForegroundColor(P4UIUtils.getDisplay().getSystemColor(
                SWT.COLOR_DARK_GRAY));
        super.outlineShape(graphics);
    }

}
