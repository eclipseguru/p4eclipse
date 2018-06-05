/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.editor.palette;

import com.perforce.team.core.mergequest.P4BranchGraphCorePlugin;
import com.perforce.team.core.mergequest.model.BranchSpecMapping;
import com.perforce.team.core.mergequest.model.DepotPathMapping;
import com.perforce.team.core.mergequest.model.registry.BranchType;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.mergequest.BranchWorkbenchAdapter;
import com.perforce.team.ui.mergequest.IP4BranchGraphConstants;
import com.perforce.team.ui.mergequest.P4BranchGraphPlugin;
import com.perforce.team.ui.mergequest.descriptors.DescriptorRegistry;
import com.perforce.team.ui.mergequest.descriptors.ElementDescriptor;
import com.perforce.team.ui.mergequest.requests.BranchCreationFactory;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.palette.MarqueeToolEntry;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PanningSelectionToolEntry;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchGraphPaletteRoot extends PaletteRoot {

    /**
     * Create branch graph palette root
     */
    public BranchGraphPaletteRoot() {
        add(getBranchesDrawer());
        add(getMappingsDrawer());
        add(getToolsContainer());
    }

    private PaletteContainer getMappingsDrawer() {
        PaletteDrawer tools = new PaletteDrawer(
                Messages.BranchGraphPaletteRoot_BranchMappingsDrawer,
                P4BranchGraphPlugin
                        .getImageDescriptor(IP4BranchGraphConstants.CONNECTORS_DRAWER));

        ImageDescriptor mappingToolDescriptor = PerforceUIPlugin
                .getDescriptor(IPerforceUIConstants.IMG_BRANCH);

        ToolEntry mappingTool = new ConnectionCreationToolEntry(
                Messages.BranchGraphPaletteRoot_BranchSpecTool,
                Messages.BranchGraphPaletteRoot_BranchSpecToolDescription,
                new CreationFactory() {

                    public Object getNewObject() {
                        return null;
                    }

                    public Object getObjectType() {
                        return BranchSpecMapping.TYPE;
                    }
                }, mappingToolDescriptor, mappingToolDescriptor);
        tools.add(mappingTool);

        ImageDescriptor depotPathMappingToolDescriptor = P4BranchGraphPlugin
                .getImageDescriptor(IP4BranchGraphConstants.DEPOT_PATH_MAPPING);

        ToolEntry depotPathMappingTool = new ConnectionCreationToolEntry(
                Messages.BranchGraphPaletteRoot_DepotPathTool,
                Messages.BranchGraphPaletteRoot_DepotPathToolDescription,
                new CreationFactory() {

                    public Object getNewObject() {
                        return null;
                    }

                    public Object getObjectType() {
                        return DepotPathMapping.TYPE;
                    }
                }, depotPathMappingToolDescriptor,
                depotPathMappingToolDescriptor);
        tools.add(depotPathMappingTool);

        return tools;
    }

    private PaletteContainer getToolsContainer() {
        PaletteDrawer tools = new PaletteDrawer(
                Messages.BranchGraphPaletteRoot_ToolsDrawer);

        ToolEntry selectionTool = new PanningSelectionToolEntry(
                Messages.BranchGraphPaletteRoot_SelectTool,
                Messages.BranchGraphPaletteRoot_SelectToolDescription);
        tools.add(selectionTool);
        this.setDefaultEntry(selectionTool);

        ToolEntry marqueeTool = new MarqueeToolEntry(
                Messages.BranchGraphPaletteRoot_MarqueTool,
                Messages.BranchGraphPaletteRoot_MarqueToolDescription);
        tools.add(marqueeTool);

        return tools;
    }

    private PaletteContainer getBranchesDrawer() {
        PaletteDrawer drawer = new PaletteDrawer(
                Messages.BranchGraphPaletteRoot_BranchesDrawer,
                P4BranchGraphPlugin
                        .getImageDescriptor(IP4BranchGraphConstants.CODELINE_DRAWER));

        DescriptorRegistry descriptors = BranchWorkbenchAdapter
                .getDescriptorRegistry();
        BranchType[] types = P4BranchGraphCorePlugin.getDefault()
                .getBranchRegistry().getTypes();
        Arrays.sort(types, new Comparator<BranchType>() {

            public int compare(BranchType o1, BranchType o2) {
                return o1.getLabel().compareToIgnoreCase(o2.getLabel());
            }
        });
        for (BranchType type : types) {

            ElementDescriptor descriptor = descriptors.getDescriptor(type
                    .getType());

            String branchLabel = type.getLabel();
            ImageDescriptor icon = null;
            String details = ""; //$NON-NLS-1$
            if (descriptor != null) {
                icon = descriptor.getIcon();
                details = descriptor.getDescription();
            }
            CreationFactory creationFactory = new BranchCreationFactory(type);

            CombinedTemplateCreationEntry stagingComponent = new CombinedTemplateCreationEntry(
                    branchLabel, details, creationFactory, creationFactory,
                    icon, icon);
            drawer.add(stagingComponent);
        }

        return drawer;
    }

}
