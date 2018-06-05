/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.wizards.mapping.depot;

import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.DepotPathMapping;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.core.mergequest.model.IMappingVisitor;
import com.perforce.team.core.mergequest.model.Mapping;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Container;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Resource.Type;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.mergequest.TextContentAssist;
import com.perforce.team.ui.mergequest.wizards.mapping.MappingArea;
import com.perforce.team.ui.resource.ResourceBrowserDialog;

import java.text.MessageFormat;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class DepotPathArea extends MappingArea {

    /**
     * MIN_DEPOT_PATH_LENGTH
     */
    public static final int MIN_DEPOT_PATH_LENGTH = 5;

    private String name = null;
    private String sourcePath = null;
    private String targetPath = null;

    private Composite displayArea;

    private Text nameText;
    private Text sourcePathText;
    private Text targetPathText;

    /**
     * @param graph
     * @param connection
     */
    public DepotPathArea(IBranchGraph graph, IP4Connection connection) {
        super(graph, connection);
    }

    /**
     * @see com.perforce.team.ui.mergequest.wizards.mapping.MappingArea#createMapping()
     */
    @Override
    public Mapping createMapping() {
        DepotPathMapping mapping = graph.createDepotPathMapping(null);
        mapping.setName(this.name);
        mapping.setSourcePath(this.sourcePath);
        mapping.setTargetPath(this.targetPath);
        mapping.setDirection(getDirection());
        return mapping;
    }

    /**
     * @see com.perforce.team.ui.mergequest.wizards.mapping.MappingArea#validateArea()
     */
    @Override
    protected String validateArea() {
        String message = null;
        name = nameText.getText().trim();
        sourcePath = sourcePathText.getText().trim();
        targetPath = targetPathText.getText().trim();

        if (message == null && name.length() == 0) {
            message = Messages.DepotPathArea_EnterMappingName;
        }

        if (message == null && sourcePath.length() == 0) {
            message = Messages.DepotPathArea_EnterSourcePath;
        }

        if (message == null && targetPath.length() == 0) {
            message = Messages.DepotPathArea_EnterTargetPath;
        }

        if (message == null
                && !sourcePath.startsWith(IP4Container.DEPOT_PREFIX)) {
            message = Messages.DepotPathArea_SourcePathStartsWith;
        }

        if (message == null
                && !targetPath.startsWith(IP4Container.DEPOT_PREFIX)) {
            message = Messages.DepotPathArea_TargetPathStartsWith;
        }

        if (message == null && sourcePath.length() < MIN_DEPOT_PATH_LENGTH) {
            message = Messages.DepotPathArea_ValidSourcePath;
        }

        if (message == null && targetPath.length() < MIN_DEPOT_PATH_LENGTH) {
            message = Messages.DepotPathArea_ValidTargetPath;
        }

        if (message == null && checkNameExists(name, this.source, true)) {
            message = MessageFormat.format(Messages.DepotPathArea_NameInUse,
                    this.source.getName());
        }

        if (message == null && checkNameExists(name, this.target, false)) {
            message = MessageFormat.format(Messages.DepotPathArea_NameInUse,
                    this.target.getName());
        }

        if (message == null && sourcePath.equals(targetPath)) {
            message = Messages.DepotPathArea_PathsCannotBeSame;
        }

        return message;
    }

    private void createNameArea(Composite parent) {
        Composite nameArea = new Composite(parent, SWT.NONE);
        GridLayout naLayout = new GridLayout(2, false);
        naLayout.marginHeight = 0;
        naLayout.marginWidth = 0;
        nameArea.setLayout(naLayout);
        nameArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Label nameLabel = new Label(nameArea, SWT.LEFT);
        nameLabel.setText(Messages.DepotPathArea_MappingName);

        nameText = new Text(nameArea, SWT.SINGLE | SWT.BORDER);
        nameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        nameText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                validate();
            }
        });
    }

    private String getDefaultPath(Branch branch) {
        final String[] defaultPath = new String[] { null };
        IMappingVisitor visitor = new IMappingVisitor() {

            public boolean visit(Mapping mapping, Branch branch) {
                String candidate = branch.equals(mapping.getSource())
                        ? ((DepotPathMapping) mapping).getSourcePath()
                        : ((DepotPathMapping) mapping).getTargetPath();
                if (defaultPath[0] == null) {
                    defaultPath[0] = candidate;
                } else if (!defaultPath[0].equals(candidate)) {
                    defaultPath[0] = null;
                    return false;
                }
                return true;
            }
        };
        branch.accept(visitor, DepotPathMapping.TYPE, true, true);
        return defaultPath[0];
    }

    private void fillDefaults() {
        String defaultSourcePath = null;
        String defaultTargetPath = null;
        String name = null;

        DepotPathMapping depotMapping = (DepotPathMapping) this.mapping;
        if (depotMapping != null) {
            defaultSourcePath = depotMapping.getSourcePath();
            defaultTargetPath = depotMapping.getTargetPath();
            name = depotMapping.getName();
        }
        // Default source path
        if (defaultSourcePath == null && this.source != null) {
            defaultSourcePath = getDefaultPath(this.source);
        }

        // Default target path
        if (defaultTargetPath == null && this.target != null) {
            defaultTargetPath = getDefaultPath(this.target);
        }

        // Default mapping name
        if (name == null && this.source != null && this.target != null) {
            String baseName = MessageFormat.format(
                    Messages.DepotPathArea_DefaultName, this.source.getName(),
                    this.target.getName());
            int suffix = 1;
            name = baseName;
            while (this.source.getSourceMappingByName(name) != null
                    || this.target.getTargetMappingByName(name) != null) {
                name = MessageFormat.format(
                        Messages.DepotPathArea_NameCollision, baseName,
                        Integer.toString(suffix));
                suffix++;
            }
        }

        if (name == null) {
            name = ""; //$NON-NLS-1$
        }
        if (defaultSourcePath == null) {
            defaultSourcePath = ""; //$NON-NLS-1$
        }
        if (defaultTargetPath == null) {
            defaultTargetPath = ""; //$NON-NLS-1$
        }
        this.targetPathText.setText(defaultTargetPath);
        this.sourcePathText.setText(defaultSourcePath);
        this.nameText.setText(name);
        this.nameText.selectAll();
    }

    private void createDepotViewArea(Composite parent) {
        createNameArea(parent);

        Group sourceArea = new Group(parent, SWT.NONE);
        sourceArea.setText(Messages.DepotPathArea_Source);
        GridLayout saLayout = new GridLayout(1, true);
        sourceArea.setLayout(saLayout);
        sourceArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        sourcePathText = createPathArea(sourceArea);

        createDirectionArea(parent, SWT.HORIZONTAL);

        Group targetArea = new Group(parent, SWT.NONE);
        targetArea.setText(Messages.DepotPathArea_Target);
        GridLayout taLayout = new GridLayout(1, true);
        targetArea.setLayout(taLayout);
        targetArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        targetPathText = createPathArea(targetArea);

        createSourceBranchArea(sourceArea, Messages.DepotPathArea_BranchName);
        createTargetBranchArea(targetArea, Messages.DepotPathArea_BranchName);

        Set<String> depotPaths = new TreeSet<String>();
        for (Mapping mapping : this.graph.getMappings()) {
            if (mapping instanceof DepotPathMapping) {
                DepotPathMapping depotMapping = (DepotPathMapping) mapping;
                depotPaths.add(depotMapping.getSourcePath());
                depotPaths.add(depotMapping.getTargetPath());
            }
        }
        String[] assistItems = depotPaths
                .toArray(new String[depotPaths.size()]);
        if (assistItems.length > 0) {
            addContentAssistDecoration(sourcePathText);
            addContentAssistDecoration(targetPathText);
        }
        new TextContentAssist(sourcePathText, assistItems);
        new TextContentAssist(targetPathText, assistItems);

    }

    private Text createPathArea(Composite parent) {
        Composite sourcePathArea = new Composite(parent, SWT.NONE);
        GridLayout spaLayout = new GridLayout(3, false);
        spaLayout.marginHeight = 0;
        spaLayout.marginHeight = 0;
        sourcePathArea.setLayout(spaLayout);
        sourcePathArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                false));

        Label sourcePathLabel = new Label(sourcePathArea, SWT.NONE);
        sourcePathLabel.setText(Messages.DepotPathArea_DepotPath);

        Text text = new Text(sourcePathArea, SWT.SINGLE | SWT.BORDER);
        text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        text.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                validate();
            }
        });

        ToolBar sourceBar = new ToolBar(sourcePathArea, SWT.FLAT);
        ToolItem sourceItem = new ToolItem(sourceBar, SWT.PUSH);
        Image image = this.resources.getImage(PerforceUIPlugin
                .getDescriptor(IPerforceUIConstants.IMG_FIND));
        sourceItem.setImage(image);
        configureBrowseButton(sourceItem, text);
        return text;
    }

    private void configureBrowseButton(final ToolItem browse, final Text field) {
        browse.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (connection == null) {
                    return;
                }
                ResourceBrowserDialog dialog = new ResourceBrowserDialog(field
                        .getShell(), connection.members());
                if (dialog.open() == ResourceBrowserDialog.OK) {
                    IP4Resource resource = dialog.getSelectedResource();
                    if (resource != null) {
                        String actionPath = resource.getActionPath(Type.REMOTE);
                        if (actionPath != null) {
                            field.setText(actionPath);
                        }
                    }
                }
            }

        });
    }

    /**
     * @see com.perforce.team.ui.mergequest.wizards.mapping.IMappingArea#createControl(org.eclipse.swt.widgets.Composite,
     *      org.eclipse.jface.wizard.IWizardContainer)
     */
    public void createControl(Composite parent, IWizardContainer container) {
        displayArea = new Composite(parent, SWT.NONE);
        displayArea.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                resources.dispose();
            }
        });
        GridLayout daLayout = new GridLayout(1, false);
        daLayout.marginHeight = 0;
        daLayout.marginWidth = 0;
        daLayout.verticalSpacing = 2;
        displayArea.setLayout(daLayout);
        GridData daData = new GridData(SWT.FILL, SWT.FILL, true, true);
        displayArea.setLayoutData(daData);

        createDepotViewArea(this.displayArea);
        fillDefaults();
    }

}
