/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.wizards.mapping;

import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.Mapping;
import com.perforce.team.core.mergequest.model.Mapping.Direction;
import com.perforce.team.ui.IErrorProvider;
import com.perforce.team.ui.mergequest.wizards.branch.BranchDescriptor;
import com.perforce.team.ui.mergequest.wizards.branch.BranchNameArea;

import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IMappingArea extends IErrorProvider {

    /**
     * Create mapping
     * 
     * @return mapping
     */
    Mapping createMapping();

    /**
     * Create control
     * 
     * @param parent
     * @param container
     */
    void createControl(Composite parent, IWizardContainer container);

    /**
     * Set source branch
     * 
     * @param branch
     */
    void setSource(Branch branch);

    /**
     * Set target branch
     * 
     * @param target
     */
    void setTarget(Branch target);

    /**
     * Set existing mapping
     * 
     * @param mapping
     */
    void setExistingMapping(Mapping mapping);

    /**
     * Get source descriptor
     * 
     * @return branch descriptor
     */
    BranchDescriptor getSourceDescriptor();

    /**
     * Get target descriptor
     * 
     * @return branch descriptor
     */
    BranchDescriptor getTargetDescriptor();

    /**
     * Get source branch selection
     * 
     * @return source branch or null if existing source branch not selected
     */
    Branch getSourceSelection();

    /**
     * Get target branch selection
     * 
     * @return target branch or null if existing target branch not selected
     */
    Branch getTargetSelection();

    /**
     * Get mapping direction
     * 
     * @return direction
     */
    Direction getDirection();

    /**
     * Get source area
     * 
     * @return branch name area
     */
    BranchNameArea getSourceArea();

    /**
     * Get target area
     * 
     * @return branch name area
     */
    BranchNameArea getTargetArea();

}
