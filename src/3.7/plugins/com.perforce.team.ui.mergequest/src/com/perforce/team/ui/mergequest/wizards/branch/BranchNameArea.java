/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.wizards.branch;

import com.perforce.team.core.mergequest.P4BranchGraphCorePlugin;
import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.core.mergequest.model.registry.BranchType;
import com.perforce.team.ui.IErrorProvider;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.mergequest.BranchGraphUtils;
import com.perforce.team.ui.mergequest.BranchWorkbenchAdapter;
import com.perforce.team.ui.mergequest.parts.SharedResources;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchNameArea {

    /**
     * Names to branches
     */
    protected Map<String, Branch> namedBranchMap = new HashMap<String, Branch>();

    private SharedResources resources;
    private IBranchGraph graph;
    private Branch selection = null;
    private BranchDescriptor descriptor;
    private IErrorProvider provider;
    private BranchAssistant assistant;

    private ToolItem typeItem;
    private ITextViewer nameText;
    private boolean syncTypes = true;

    /**
     * Create a new branch name area
     * 
     * @param resources
     * @param graph
     * @param provider
     */
    public BranchNameArea(SharedResources resources, IBranchGraph graph,
            IErrorProvider provider) {
        this.resources = resources;
        this.graph = graph;
        this.provider = provider;
        for (Branch branch : this.graph.getBranches()) {
            String name = branch.getName();
            if (name != null) {
                this.namedBranchMap.put(name, branch);
            }
        }
    }

    /**
     * Sync types to existing branches with entered name
     * 
     * @param sync
     */
    public void setSyncTypes(boolean sync) {
        this.syncTypes = sync;
    }

    /**
     * Set focus
     */
    public void setFocus() {
        this.nameText.getTextWidget().setFocus();
    }

    /**
     * Init content assist
     */
    public void initContentAssist() {
        if (assistant == null && nameText.isEditable()) {
            BranchGraphUtils.addContentAssistDecoration(nameText
                    .getTextWidget());
            assistant = new BranchAssistant(resources);
            assistant.init();
            assistant.install(nameText);
            assistant.loadProposals(graph);
        }
    }

    /**
     * Get branch content assistant
     * 
     * @return branch assistant
     */
    public BranchAssistant getAssistant() {
        return this.assistant;
    }

    /**
     * Create branch edit area
     * 
     * @param parent
     * @param label
     * @param initial
     * @return branch descriptor
     */
    public BranchDescriptor createControl(Composite parent, String label,
            BranchDescriptor initial) {
        if (initial == null) {
            initial = new BranchDescriptor();
        }
        descriptor = new BranchDescriptor(initial);
        Composite sourceGroup = new Composite(parent, SWT.NONE);
        GridLayout sgLayout = new GridLayout(3, false);
        sgLayout.marginHeight = 0;
        sgLayout.marginWidth = 0;
        sgLayout.horizontalSpacing = 2;
        sourceGroup.setLayout(sgLayout);
        sourceGroup
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        final ToolBar toolbar = new ToolBar(sourceGroup, SWT.FLAT);
        typeItem = new ToolItem(toolbar, SWT.DROP_DOWN);

        setType(descriptor.getType());
        typeItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (typeItem.isEnabled()) {
                    addTypeMenu(descriptor, toolbar, typeItem);
                }
            }

        });

        Label nameLabel = new Label(sourceGroup, SWT.NONE);
        nameLabel.setText(label);

        nameText = new TextViewer(sourceGroup, SWT.SINGLE | SWT.BORDER);
        nameText.setDocument(new Document());
        GridData ntData = new GridData(SWT.FILL, SWT.FILL, true, false);
        ntData.horizontalIndent = 5;
        ntData.widthHint = P4UIUtils.computePixelWidth(nameText.getTextWidget()
                .getFont(), 25);
        nameText.getTextWidget().setLayoutData(ntData);

        if (initial.getName().length() > 0) {
            nameText.getTextWidget().setText(initial.getName());
            setEditable(false);
        }

        nameText.getDocument().addDocumentListener(new IDocumentListener() {

            public void documentChanged(DocumentEvent event) {
                descriptor.setName(nameText.getDocument().get().trim());
                syncTypes(descriptor);
                if (provider != null) {
                    provider.validate();
                }
            }

            public void documentAboutToBeChanged(DocumentEvent event) {

            }
        });

        return descriptor;
    }

    /**
     * Set the name area as editable
     * 
     * @param editable
     */
    public void setEditable(boolean editable) {
        nameText.getTextWidget().setEnabled(editable);
        nameText.setEditable(editable);
        typeItem.setEnabled(editable);
    }

    /**
     * Select all the name text
     */
    public void selectAll() {
        nameText.getTextWidget().selectAll();
    }

    /**
     * Create branch edit area
     * 
     * @param parent
     * @param label
     * @param branch
     * @return branch descriptor
     */
    public BranchDescriptor createControl(Composite parent, String label,
            Branch branch) {
        BranchDescriptor initial = new BranchDescriptor();
        if (branch != null) {
            initial.setName(branch.getName());
            initial.setType(getType(branch.getType()));
        }
        return createControl(parent, label, initial);
    }

    private BranchType getType(String type) {
        return P4BranchGraphCorePlugin.getDefault().getBranchRegistry()
                .getType(type);
    }

    /**
     * Sync existing name to current type
     * 
     * @param descriptor
     */
    protected void syncTypes(BranchDescriptor descriptor) {
        if (!syncTypes) {
            return;
        }
        selection = this.namedBranchMap.get(descriptor.getName());
        if (selection != null) {
            descriptor.setType(getType(selection.getType()));
            setType(descriptor.getType());
            typeItem.setEnabled(false);
        } else {
            typeItem.setEnabled(true);
        }
    }

    private void addTypeMenu(final BranchDescriptor descriptor,
            ToolBar toolbar, final ToolItem item) {
        Menu menu = new Menu(toolbar);

        for (final BranchType type : P4BranchGraphCorePlugin.getDefault()
                .getBranchRegistry()) {
            final MenuItem typeItem = new MenuItem(menu, SWT.CHECK);
            typeItem.setText(type.getLabel());
            typeItem.setImage(resources.getImage(BranchWorkbenchAdapter
                    .getTypeDescriptor(type.getType())));
            typeItem.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    descriptor.setType(type);
                    setType(descriptor.getType());
                    if (provider != null) {
                        provider.validate();
                    }
                }

            });
            if (type.equals(descriptor.getType())) {
                typeItem.setSelection(true);
            }
        }

        Rectangle bounds = item.getBounds();
        Point location = toolbar.toDisplay(bounds.x, bounds.y + bounds.height);
        menu.setLocation(location);
        menu.setVisible(true);
    }

    /**
     * 
     * @param type
     */
    public void setType(BranchType type) {
        typeItem.setImage(this.resources.getImage(BranchWorkbenchAdapter
                .getTypeDescriptor(type.getType())));
        typeItem.setToolTipText(type.getLabel());
    }

    /**
     * Get selected existing branch or null if new branch entered
     * 
     * @return branch
     */
    public Branch getSelection() {
        return this.selection;
    }

    /**
     * Get branch descriptor for new branch definition
     * 
     * @return branch description
     */
    public BranchDescriptor getDescriptor() {
        return this.descriptor;
    }

}
