/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.wizards.branch;

import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.ui.IErrorDisplay;
import com.perforce.team.ui.IErrorProvider;
import com.perforce.team.ui.mergequest.parts.SharedResources;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchWizardPage extends WizardPage implements IErrorDisplay,
        IErrorProvider {

    private IBranchGraph graph;
    private SharedResources resources;
    private BranchDescriptor initial;
    private BranchDescriptor descriptor;
    private BranchNameArea nameArea;

    /**
     * Current branch names
     */
    protected Set<String> names = new HashSet<String>();

    /**
     * @param pageName
     * @param graph
     * @param initial
     */
    public BranchWizardPage(String pageName, IBranchGraph graph,
            BranchDescriptor initial) {
        super(pageName);
        this.initial = initial;
        this.graph = graph;
        for (Branch branch : graph.getBranches()) {
            this.names.add(branch.getName());
        }
        this.names.remove(this.initial.getName());
    }

    /**
     * @see com.perforce.team.ui.IErrorDisplay#setErrorMessage(java.lang.String,
     *      com.perforce.team.ui.IErrorProvider)
     */
    public void setErrorMessage(String message, IErrorProvider provider) {
        setPageComplete(message == null);
        setErrorMessage(message);
    }

    /**
     * @see com.perforce.team.ui.IErrorProvider#validate()
     */
    public void validate() {
        String message = null;
        String name = this.descriptor.getName();
        if (message == null && name.length() == 0) {
            message = Messages.BranchWizardPage_EnterBranchName;
        }
        if (message == null && names.contains(name)) {
            message = MessageFormat.format(
                    Messages.BranchWizardPage_BranchExists, name);
        }
        setErrorMessage(message, null);
    }

    /**
     * Create branch name area
     * 
     * @param parent
     * @param resources
     * @param graph
     * @param provider
     * @return branch name area
     */
    protected BranchNameArea createNameArea(Composite parent,
            SharedResources resources, IBranchGraph graph,
            IErrorProvider provider) {
        BranchNameArea area = new BranchNameArea(resources, graph, provider);
        this.descriptor = area.createControl(parent,
                Messages.BranchWizardPage_Name, this.initial);
        return area;
    }

    /**
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        this.resources = new SharedResources();

        Composite displayArea = new Composite(parent, SWT.NONE);
        displayArea.setLayout(GridLayoutFactory.swtDefaults().equalWidth(false)
                .create());
        displayArea.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.FILL, SWT.FILL).grab(true, true).create());

        this.nameArea = createNameArea(displayArea, this.resources, this.graph,
                this);

        this.nameArea.setFocus();

        setControl(displayArea);
        setPageComplete(false);
    }

    /**
     * @return created branch descriptor
     */
    public BranchDescriptor getCreatedBranch() {
        return this.descriptor;
    }

    /**
     * @see com.perforce.team.ui.IErrorProvider#setErrorDisplay(com.perforce.team.ui.IErrorDisplay)
     */
    public void setErrorDisplay(IErrorDisplay display) {

    }

}
