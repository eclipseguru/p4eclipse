/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.wizards.mapping;

import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.core.mergequest.model.Mapping;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.IErrorDisplay;
import com.perforce.team.ui.IErrorProvider;
import com.perforce.team.ui.mergequest.wizards.branch.BranchAssistant;
import com.perforce.team.ui.mergequest.wizards.branch.BranchDescriptor;
import com.perforce.team.ui.mergequest.wizards.branch.BranchNameArea;

import org.eclipse.jface.text.contentassist.ContentAssistEvent;
import org.eclipse.jface.text.contentassist.ICompletionListener;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class MappingWizardPage extends WizardPage implements
        IErrorDisplay {

    private Branch initialSource;
    private Branch initialTarget;

    private Composite displayArea;
    private IMappingArea mappingArea;

    private IBranchGraph graph;
    private boolean inSession = false;

    private Mapping existing = null;

    /**
     * @param pageName
     * @param graph
     */
    public MappingWizardPage(String pageName, IBranchGraph graph) {
        super(pageName);
        this.graph = graph;
    }

    /**
     * Set existing mapping
     * 
     * @param mapping
     */
    public void setExistingMapping(Mapping mapping) {
        this.existing = mapping;
    }

    /**
     * Get mapping area
     * 
     * @return mapping area
     */
    public IMappingArea getMappingArea() {
        return this.mappingArea;
    }

    /**
     * Create mapping area
     * 
     * @param graph
     * @param connection
     * @return non-null mapping area
     */
    protected abstract IMappingArea createMappingArea(IBranchGraph graph,
            IP4Connection connection);

    /**
     * Hook completion listener to branch name area content assistant
     * 
     * @param area
     */
    protected void hookCompletionListener(BranchNameArea area) {
        if (area != null) {
            BranchAssistant assistant = area.getAssistant();
            if (assistant != null) {
                assistant.addCompletionListener(new ICompletionListener() {

                    public void selectionChanged(ICompletionProposal proposal,
                            boolean smartToggle) {

                    }

                    public void assistSessionStarted(ContentAssistEvent event) {
                        inSession = true;
                        setPageComplete(false);
                    }

                    public void assistSessionEnded(ContentAssistEvent event) {
                        inSession = false;
                        setPageComplete(getErrorMessage() == null);
                    }
                });
            }
        }
    }

    /**
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        displayArea = new Composite(parent, SWT.NONE);
        displayArea.setLayout(new GridLayout(1, true));
        GridData daData = new GridData(SWT.FILL, SWT.FILL, true, true);
        displayArea.setLayoutData(daData);

        this.mappingArea = createMappingArea(graph, graph.getConnection());
        this.mappingArea.setExistingMapping(this.existing);
        this.mappingArea.setSource(this.initialSource);
        this.mappingArea.setTarget(this.initialTarget);
        this.mappingArea.createControl(displayArea, getContainer());
        this.mappingArea.setErrorDisplay(this);

        this.mappingArea.validate();
        setPageComplete(getErrorMessage() == null);
        setErrorMessage(null);

        hookCompletionListener(this.mappingArea.getSourceArea());
        hookCompletionListener(this.mappingArea.getTargetArea());

        setControl(displayArea);
    }

    /**
     * Create configured mapping
     * 
     * @return - mapping
     */
    public Mapping createMapping() {
        return this.mappingArea.createMapping();
    }

    /**
     * @param source
     */
    public void setSource(Branch source) {
        this.initialSource = source;
    }

    /**
     * @param target
     */
    public void setTarget(Branch target) {
        this.initialTarget = target;
    }

    /**
     * @see com.perforce.team.ui.IErrorDisplay#setErrorMessage(java.lang.String,
     *      com.perforce.team.ui.IErrorProvider)
     */
    public void setErrorMessage(String message, IErrorProvider provider) {
        if (inSession) {
            setPageComplete(false);
        } else {
            setPageComplete(message == null);
        }
        setErrorMessage(message);
    }

    /**
     * @return source descriptor
     */
    public BranchDescriptor getSourceDescriptor() {
        return this.mappingArea.getSourceDescriptor();
    }

    /**
     * @return target descriptor
     */
    public BranchDescriptor getTargetDescriptor() {
        return this.mappingArea.getTargetDescriptor();
    }

    /**
     * @return source branch
     */
    public Branch getSourceSelection() {
        return this.mappingArea.getSourceSelection();
    }

    /**
     * @return target branch
     */
    public Branch getTargetSelection() {
        return this.mappingArea.getTargetSelection();
    }

}
