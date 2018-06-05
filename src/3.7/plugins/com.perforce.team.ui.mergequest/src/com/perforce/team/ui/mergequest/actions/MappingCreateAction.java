/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.actions;

import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.BranchSpecMapping;
import com.perforce.team.core.mergequest.model.DepotPathMapping;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.core.mergequest.model.Mapping;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.mergequest.wizards.branch.BranchDescriptor;
import com.perforce.team.ui.mergequest.wizards.mapping.NewMappingWizard;
import com.perforce.team.ui.mergequest.wizards.mapping.branch.BranchMappingWizard;
import com.perforce.team.ui.mergequest.wizards.mapping.depot.DepotPathMappingWizard;

import java.awt.Point;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class MappingCreateAction extends Action {

    /**
     * Starting x coordinate
     */
    public static final int STARTING_X = 100;

    /**
     * Start y coordinate
     */
    public static final int STARTING_Y = 100;

    /**
     * Branch buffer spacing
     */
    public static final int BRANCH_BUFFER = 200;

    private IBranchGraph graph;

    private Object type;
    private Branch source;
    private Branch target;
    private int sourceTerminal = 0;
    private int targetTerminal = 0;

    private Mapping lastCreated = null;

    /**
     * Create mapping create action
     * 
     * @param graph
     * @param type
     */
    public MappingCreateAction(IBranchGraph graph, Object type) {
        this.graph = graph;
        this.type = type;
    }

    /**
     * Get last created mapping
     * 
     * @return mapping
     */
    public Mapping getLastCreated() {
        return this.lastCreated;
    }

    /**
     * Set source terminal
     * 
     * @param terminal
     */
    public void setSourceTerminal(int terminal) {
        this.sourceTerminal = terminal;
    }

    /**
     * Set target terminal
     * 
     * @param terminal
     */
    public void setTargetTerminal(int terminal) {
        this.targetTerminal = terminal;
    }

    /**
     * Set initial source branch
     * 
     * @param source
     */
    public void setInitialSource(Branch source) {
        this.source = source;
    }

    /**
     * Set initial target branch
     * 
     * @param target
     */
    public void setInitialTarget(Branch target) {
        this.target = target;
    }

    /**
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        NewMappingWizard wizard = null;
        if (BranchSpecMapping.TYPE.equals(this.type)) {
            wizard = new BranchMappingWizard(graph);
        } else if (DepotPathMapping.TYPE.equals(this.type)) {
            wizard = new DepotPathMappingWizard(graph);
        }
        if (wizard != null) {
            wizard.setInitialSource(this.source);
            wizard.setInitialTarget(this.target);
            WizardDialog dialog = new WizardDialog(P4UIUtils.getDialogShell(),
                    wizard);
            if (WizardDialog.OK == dialog.open()) {
                Mapping created = wizard.getMapping();
                Branch sourceBranch = wizard.getSourceSelection();
                Branch targetBranch = wizard.getTargetSelection();

                Point sourceLocation = null;
                Point targetLocation = null;

                if (sourceBranch == null && targetBranch == null) {
                    sourceLocation = this.graph.findEmptyLocation(new Point(
                            STARTING_X, STARTING_X), BRANCH_BUFFER);
                    targetLocation = this.graph.findEmptyLocation(
                            new Point(sourceLocation.x + BRANCH_BUFFER,
                                    sourceLocation.y), BRANCH_BUFFER);
                } else if (sourceBranch == null && targetBranch != null) {
                    sourceLocation = this.graph.findEmptyLocation(new Point(
                            targetBranch.getX(), targetBranch.getY()),
                            BRANCH_BUFFER);
                } else if (sourceBranch != null && targetBranch == null) {
                    targetLocation = this.graph.findEmptyLocation(new Point(
                            sourceBranch.getX(), sourceBranch.getY()),
                            BRANCH_BUFFER);
                }

                if (sourceBranch == null) {
                    sourceBranch = this.graph.createBranch(null);
                    BranchDescriptor sourceDescriptor = wizard
                            .getSourceDescriptor();
                    sourceBranch.setName(sourceDescriptor.getName());
                    sourceBranch.setType(sourceDescriptor.getType().getType());
                    sourceBranch
                            .setLocation(sourceLocation.x, sourceLocation.y);
                    graph.add(sourceBranch);
                }
                if (targetBranch == null) {
                    targetBranch = this.graph.createBranch(null);
                    BranchDescriptor targetDescriptor = wizard
                            .getTargetDescriptor();
                    targetBranch.setName(targetDescriptor.getName());
                    targetBranch.setType(targetDescriptor.getType().getType());
                    targetBranch
                            .setLocation(targetLocation.x, targetLocation.y);
                    graph.add(targetBranch);
                }
                if (graph.add(created)) {
                    created.setSourceAnchor(sourceTerminal);
                    created.setTargetAnchor(targetTerminal);
                    created.connect(sourceBranch, targetBranch);
                    lastCreated = created;
                }
            }
        }
    }
}
