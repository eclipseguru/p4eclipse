/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.wizards.mapping;

import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.core.mergequest.model.Mapping;
import com.perforce.team.core.mergequest.model.Mapping.Direction;
import com.perforce.team.core.mergequest.model.registry.BranchType;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.BaseErrorProvider;
import com.perforce.team.ui.mergequest.BranchGraphUtils;
import com.perforce.team.ui.mergequest.IP4BranchGraphConstants;
import com.perforce.team.ui.mergequest.parts.SharedResources;
import com.perforce.team.ui.mergequest.wizards.branch.BranchDescriptor;
import com.perforce.team.ui.mergequest.wizards.branch.BranchNameArea;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class MappingArea extends BaseErrorProvider implements
        IMappingArea {

    /**
     * Graph
     */
    protected IBranchGraph graph;

    /**
     * Connection
     */
    protected IP4Connection connection;

    /**
     * Source branch
     */
    protected Branch source;

    /**
     * Target branch
     */
    protected Branch target;

    /**
     * Source branch descriptor
     */
    protected BranchDescriptor sourceDescriptor = null;

    /**
     * Target branch descriptor
     */
    protected BranchDescriptor targetDescriptor = null;

    /**
     * Source branch name area
     */
    protected BranchNameArea sourceArea = null;

    /**
     * Target branch name area
     */
    protected BranchNameArea targetArea = null;

    /**
     * Mapping
     */
    protected Mapping mapping;

    /**
     * Direction flag
     */
    protected Direction direction = Direction.BOTH;

    /**
     * Shared resource
     */
    protected SharedResources resources = new SharedResources();

    /**
     * Add content assist decoration
     * 
     * @param control
     */
    public static void addContentAssistDecoration(Control control) {
        BranchGraphUtils.addContentAssistDecoration(control);
    }

    /**
     * @param graph
     * @param connection
     */
    public MappingArea(IBranchGraph graph, IP4Connection connection) {
        this.graph = graph;
        this.connection = connection;
    }

    /**
     * @see com.perforce.team.ui.mergequest.wizards.mapping.IMappingArea#setExistingMapping(com.perforce.team.core.mergequest.model.Mapping)
     */
    public void setExistingMapping(Mapping mapping) {
        this.mapping = mapping;
    }

    /**
     * 
     * @param name
     *            - non-null
     * @param branch
     *            - non-null
     * @param source
     *            - true to look in source mappings, false for target mappings
     * @return true if mapping with name exists in branch
     */
    protected boolean checkNameExists(String name, Branch branch, boolean source) {
        boolean exists = false;
        if (name != null && branch != null) {
            Mapping found = null;
            if (source) {
                found = branch.getSourceMappingByName(name);
            } else {
                found = branch.getTargetMappingByName(name);
            }
            if (found != null) {
                if (mapping == null) {
                    exists = true;
                } else {
                    exists = !found.equals(mapping);
                }
            }
        }
        return exists;
    }

    /**
     * Validate area
     * 
     * @return error message or null if none
     */
    protected abstract String validateArea();

    /**
     * Get created mapping
     * 
     * @return mapping
     */
    public abstract Mapping createMapping();

    /**
     * 
     * @see com.perforce.team.ui.BaseErrorProvider#validate()
     */
    @Override
    public final void validate() {
        String message = validateArea();
        if (message == null && this.source == null) {
            if (this.sourceDescriptor.getName().length() == 0) {
                message = Messages.MappingArea_EnterSourceName;
            } else if (BranchType.UNKNOWN.equals(this.sourceDescriptor
                    .getType())) {
                message = Messages.MappingArea_SelectSourceBranchType;
            }
        }
        if (message == null && this.target == null) {
            if (this.targetDescriptor.getName().length() == 0) {
                message = Messages.MappingArea_EnterTargetName;
            } else if (BranchType.UNKNOWN.equals(this.targetDescriptor
                    .getType())) {
                message = Messages.MappingArea_SelectTargetBranchType;
            }
        }
        if (message == null) {
            if (this.sourceDescriptor.getName().equals(
                    this.targetDescriptor.getName())) {
                message = Messages.MappingArea_SourceAndTargetMustDiffer;
            }
        }
        errorMessage = message;
        super.validate();
    }

    /**
     * @see com.perforce.team.ui.mergequest.wizards.mapping.IMappingArea#setSource(com.perforce.team.core.mergequest.model.Branch)
     */
    public void setSource(Branch source) {
        this.source = source;
    }

    /**
     * @see com.perforce.team.ui.mergequest.wizards.mapping.IMappingArea#setTarget(com.perforce.team.core.mergequest.model.Branch)
     */
    public void setTarget(Branch target) {
        this.target = target;
    }

    /**
     * Create branch edit area
     * 
     * @param parent
     * @param label
     */
    protected void createSourceBranchArea(Composite parent, String label) {
        this.sourceArea = createBranchArea(parent, label, this.source);
        this.sourceDescriptor = this.sourceArea.getDescriptor();
    }

    /**
     * Create branch edit area
     * 
     * @param parent
     * @param label
     */
    protected void createTargetBranchArea(Composite parent, String label) {
        this.targetArea = createBranchArea(parent, label, this.target);
        this.targetDescriptor = this.targetArea.getDescriptor();
    }

    /**
     * Create branch name area
     * 
     * @param parent
     * @param label
     * @param branch
     * @return branch name area
     */
    protected BranchNameArea createBranchArea(Composite parent, String label,
            Branch branch) {
        BranchNameArea nameArea = new BranchNameArea(resources, graph, this);
        nameArea.createControl(parent, label, branch);
        nameArea.initContentAssist();
        if (this.mapping != null) {
            nameArea.setEditable(false);
        }
        return nameArea;
    }

    /**
     * Create direction area
     * 
     * @param parent
     * @param style
     * @return control
     */
    protected Control createDirectionArea(Composite parent, final int style) {
        final ToolBar toolbar = new ToolBar(parent, SWT.FLAT | style);

        final ToolItem sourceToTargetItem = new ToolItem(toolbar, SWT.RADIO);
        sourceToTargetItem.setToolTipText(Messages.MappingArea_SourceToTarget);

        final ToolItem biItem = new ToolItem(toolbar, SWT.RADIO);
        biItem.setToolTipText(Messages.MappingArea_BothDirections);

        final ToolItem targetToSourceItem = new ToolItem(toolbar, SWT.RADIO);
        targetToSourceItem.setToolTipText(Messages.MappingArea_TargetToSource);

        if ((style & SWT.VERTICAL) != 0) {
            sourceToTargetItem.setImage(resources
                    .getImage(IP4BranchGraphConstants.ARROW_E));
            targetToSourceItem.setImage(resources
                    .getImage(IP4BranchGraphConstants.ARROW_W));
            biItem.setImage(resources
                    .getImage(IP4BranchGraphConstants.ARROW_WE));
            toolbar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
                    true));
        } else {
            sourceToTargetItem.setImage(resources
                    .getImage(IP4BranchGraphConstants.ARROW_S));
            targetToSourceItem.setImage(resources
                    .getImage(IP4BranchGraphConstants.ARROW_N));
            biItem.setImage(resources
                    .getImage(IP4BranchGraphConstants.ARROW_NS));
            toolbar.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true,
                    false));
        }

        if (mapping != null) {
            direction = mapping.getDirection();
            switch (direction) {
            case SOURCE:
                targetToSourceItem.setSelection(true);
                break;
            case TARGET:
                sourceToTargetItem.setSelection(true);
                break;
            case BOTH:
                biItem.setSelection(true);
                break;
            default:
                break;
            }
        } else {
            biItem.setSelection(true);
        }

        SelectionAdapter selectionListener = new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (sourceToTargetItem.getSelection()) {
                    direction = Direction.TARGET;
                } else if (targetToSourceItem.getSelection()) {
                    direction = Direction.SOURCE;
                } else if (biItem.getSelection()) {
                    direction = Direction.BOTH;
                }
            }

        };

        sourceToTargetItem.addSelectionListener(selectionListener);
        biItem.addSelectionListener(selectionListener);
        targetToSourceItem.addSelectionListener(selectionListener);

        return toolbar;
    }

    /**
     * @see com.perforce.team.ui.mergequest.wizards.mapping.IMappingArea#getSourceDescriptor()
     */
    public BranchDescriptor getSourceDescriptor() {
        return this.sourceDescriptor;
    }

    /**
     * @see com.perforce.team.ui.mergequest.wizards.mapping.IMappingArea#getTargetDescriptor()
     */
    public BranchDescriptor getTargetDescriptor() {
        return this.targetDescriptor;
    }

    /**
     * @see com.perforce.team.ui.mergequest.wizards.mapping.IMappingArea#getDirection()
     */
    public Direction getDirection() {
        return this.direction;
    }

    /**
     * @see com.perforce.team.ui.mergequest.wizards.mapping.IMappingArea#getSourceSelection()
     */
    public Branch getSourceSelection() {
        return this.sourceArea != null ? this.sourceArea.getSelection() : null;
    }

    /**
     * @see com.perforce.team.ui.mergequest.wizards.mapping.IMappingArea#getTargetSelection()
     */
    public Branch getTargetSelection() {
        return this.targetArea != null ? this.targetArea.getSelection() : null;
    }

    /**
     * @see com.perforce.team.ui.mergequest.wizards.mapping.IMappingArea#getSourceArea()
     */
    public BranchNameArea getSourceArea() {
        return this.sourceArea;
    }

    /**
     * @see com.perforce.team.ui.mergequest.wizards.mapping.IMappingArea#getTargetArea()
     */
    public BranchNameArea getTargetArea() {
        return this.targetArea;
    }
}
