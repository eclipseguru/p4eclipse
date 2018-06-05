/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.editor;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.mergequest.P4BranchGraphCorePlugin;
import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.BranchSpecMapping;
import com.perforce.team.core.mergequest.model.DepotPathMapping;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.core.mergequest.model.Mapping;
import com.perforce.team.core.mergequest.model.Mapping.Direction;
import com.perforce.team.core.mergequest.model.registry.BranchType;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.branches.EditBranchAction;
import com.perforce.team.ui.mergequest.BranchWorkbenchAdapter;
import com.perforce.team.ui.mergequest.IP4BranchGraphConstants;
import com.perforce.team.ui.mergequest.P4BranchGraphPlugin;
import com.perforce.team.ui.mergequest.actions.BranchIntegrateAction;
import com.perforce.team.ui.mergequest.actions.MappingCreateAction;
import com.perforce.team.ui.mergequest.actions.MappingEditAction;
import com.perforce.team.ui.mergequest.actions.MappingIntegrateAction;
import com.perforce.team.ui.mergequest.actions.ShowTasksAction;
import com.perforce.team.ui.mergequest.commands.BranchCreateCommand;
import com.perforce.team.ui.mergequest.commands.BranchEditCommand;
import com.perforce.team.ui.mergequest.commands.CommandAction;
import com.perforce.team.ui.mergequest.commands.MappingCreateCommand;
import com.perforce.team.ui.mergequest.parts.SharedResources;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.ActionFactory;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class GraphContextMenuProvider extends ContextMenuProvider {

    private ActionRegistry registry;
    private IBranchGraph graph;
    private Rectangle location = new Rectangle(100, 100, 0, 0);

    /**
     * @param graph
     * @param resources
     * @param viewer
     * @param registry
     */
    public GraphContextMenuProvider(IBranchGraph graph,
            SharedResources resources, EditPartViewer viewer,
            ActionRegistry registry) {
        super(viewer);
        this.graph = graph;
        this.registry = registry;
        Control control = viewer.getControl();
        if (control != null) {
            // Store the location of the last mouse down event for when creating
            // new branches
            control.addMouseListener(new MouseListener() {

                public void mouseUp(MouseEvent e) {

                }

                public void mouseDown(MouseEvent e) {
                    location.x = e.x;
                    location.y = e.y;
                }

                public void mouseDoubleClick(MouseEvent e) {

                }
            });
        }
    }

    private void addRemoveAction(IMenuManager menu) {
        IAction remove = this.registry.getAction(ActionFactory.DELETE.getId());
        if (remove != null) {
            menu.appendToGroup(GEFActionConstants.GROUP_EDIT, remove);
        }
    }

    private IAction createBranchAction(BranchType type) {
        final BranchCreateCommand command = new BranchCreateCommand(type,
                this.graph, this.location);
        IAction createBranch = new CommandAction(command, getViewer());
        createBranch.setImageDescriptor(BranchWorkbenchAdapter
                .getTypeDescriptor(type.getType()));
        createBranch.setText(MessageFormat.format(
                Messages.GraphContextMenuProvider_AddBranch, type.getType()));
        return createBranch;
    }

    private void addEmptySelectionActions(IMenuManager menu) {
        menu.appendToGroup(GEFActionConstants.GROUP_EDIT, new Separator());

        for (BranchType type : P4BranchGraphCorePlugin.getDefault()
                .getBranchRegistry()) {
            menu.appendToGroup(GEFActionConstants.GROUP_EDIT,
                    createBranchAction(type));
        }

        menu.appendToGroup(GEFActionConstants.GROUP_EDIT, new Separator());

        Branch[] branches = this.graph.getBranches();

        IAction createMapping = new CommandAction(new MappingCreateCommand(
                this.graph, BranchSpecMapping.TYPE), getViewer());
        createMapping.setEnabled(branches.length > 0);
        createMapping
                .setText(Messages.GraphContextMenuProvider_AddBranchSpecMapping);
        createMapping.setImageDescriptor(PerforceUIPlugin
                .getDescriptor(IPerforceUIConstants.IMG_BRANCH));
        menu.appendToGroup(GEFActionConstants.GROUP_EDIT, createMapping);

        createMapping = new CommandAction(new MappingCreateCommand(this.graph,
                DepotPathMapping.TYPE), getViewer());
        createMapping
                .setText(Messages.GraphContextMenuProvider_AddDepotPathMapping);
        createMapping
                .setImageDescriptor(P4BranchGraphPlugin
                        .getImageDescriptor(IP4BranchGraphConstants.DEPOT_PATH_MAPPING));
        createMapping.setEnabled(branches.length > 0);
        menu.appendToGroup(GEFActionConstants.GROUP_EDIT, createMapping);
    }

    /**
     * @see org.eclipse.gef.ContextMenuProvider#buildContextMenu(org.eclipse.jface.action.IMenuManager)
     */
    @Override
    public void buildContextMenu(IMenuManager menu) {
        menu.add(new Separator(GEFActionConstants.GROUP_EDIT));
        menu.add(new Separator(GEFActionConstants.GROUP_UNDO));
        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        List<?> parts = getViewer().getSelectedEditParts();
        if (parts.size() == 1) {
            Object part = parts.get(0);
            addBranchActions(part, menu);
            addMappingActions(part, menu);
        } else if (parts.size() > 1) {
            addRemoveAction(menu);
        } else if (parts.size() == 0) {
            addEmptySelectionActions(menu);
        }

        IAction action = this.registry.getAction(ActionFactory.UNDO.getId());
        if (action != null) {
            menu.appendToGroup(GEFActionConstants.GROUP_UNDO, action);
        }
        action = this.registry.getAction(ActionFactory.REDO.getId());
        if (action != null) {
            menu.appendToGroup(GEFActionConstants.GROUP_UNDO, action);
        }
    }

    private void addDirectionMenu(final Mapping mapping, IMenuManager menu) {
        Branch source = mapping.getSource();
        Branch target = mapping.getTarget();

        String sourceName = source != null ? source.getName() : ""; //$NON-NLS-1$
        String targetName = target != null ? target.getName() : ""; //$NON-NLS-1$
        MenuManager direction = new MenuManager(
                Messages.GraphContextMenuProvider_ChangeDirection, null,
                "changeDirection"); //$NON-NLS-1$
        menu.appendToGroup(GEFActionConstants.GROUP_EDIT, direction);

        IAction sourceToTargetAction = new Action(MessageFormat.format(
                Messages.GraphContextMenuProvider_BranchToBranch, sourceName,
                targetName), IAction.AS_CHECK_BOX) {

            @Override
            public void run() {
                mapping.setDirection(Direction.TARGET);
            }
        };
        sourceToTargetAction.setImageDescriptor(P4BranchGraphPlugin
                .getImageDescriptor(IP4BranchGraphConstants.ARROW_E));
        direction.add(sourceToTargetAction);

        IAction bothAction = new Action(
                Messages.GraphContextMenuProvider_BothDirections,
                IAction.AS_CHECK_BOX) {

            @Override
            public void run() {
                mapping.setDirection(Direction.BOTH);
            }
        };
        bothAction.setImageDescriptor(P4BranchGraphPlugin
                .getImageDescriptor(IP4BranchGraphConstants.ARROW_WE));
        direction.add(bothAction);

        IAction targetToSourceAction = new Action(MessageFormat.format(
                Messages.GraphContextMenuProvider_BranchToBranch, targetName,
                sourceName), IAction.AS_CHECK_BOX) {

            @Override
            public void run() {
                mapping.setDirection(Direction.SOURCE);
            }

        };
        targetToSourceAction.setImageDescriptor(P4BranchGraphPlugin
                .getImageDescriptor(IP4BranchGraphConstants.ARROW_W));
        direction.add(targetToSourceAction);

        switch (mapping.getDirection()) {
        case SOURCE:
            targetToSourceAction.setChecked(true);
            break;
        case TARGET:
            sourceToTargetAction.setChecked(true);
            break;
        case BOTH:
        default:
            bothAction.setChecked(true);
        }
    }

    private void addMappingActions(Object editPart, IMenuManager menu) {
        final Mapping mapping = P4CoreUtils.convert(editPart, Mapping.class);
        if (mapping != null) {

            MappingEditAction editAction = new MappingEditAction(mapping);
            editAction.setText(Messages.GraphContextMenuProvider_EditConnector);
            editAction.setImageDescriptor(PerforceUIPlugin
                    .getDescriptor(IPerforceUIConstants.IMG_EDIT));
            menu.appendToGroup(GEFActionConstants.GROUP_EDIT, editAction);

            Action showTasks = new Action() {

                @Override
                public void run() {
                    ShowTasksAction show = new ShowTasksAction();
                    show.runAction();
                }
            };
            showTasks.setText(Messages.GraphContextMenuProvider_ShowTasks);
            showTasks.setImageDescriptor(P4BranchGraphPlugin
                    .getImageDescriptor(IP4BranchGraphConstants.TASKS));
            menu.appendToGroup(GEFActionConstants.GROUP_EDIT, showTasks);

            Direction direction = mapping.getDirection();
            if (direction == Direction.TARGET || direction == Direction.BOTH) {
                MappingIntegrateAction toAction = new MappingIntegrateAction(
                        mapping, false);
                toAction.setText(MessageFormat.format(
                        Messages.GraphContextMenuProvider_IntegrateToBranch,
                        mapping.getTarget().getName()));
                toAction.setImageDescriptor(PerforceUIPlugin
                        .getDescriptor(IPerforceUIConstants.IMG_INTEGRATE));
                menu.appendToGroup(GEFActionConstants.GROUP_EDIT, toAction);
            }

            if (direction == Direction.SOURCE || direction == Direction.BOTH) {
                MappingIntegrateAction fromAction = new MappingIntegrateAction(
                        mapping, true);
                fromAction.setText(MessageFormat.format(
                        Messages.GraphContextMenuProvider_IntegrateToBranch,
                        mapping.getSource().getName()));
                fromAction.setImageDescriptor(PerforceUIPlugin
                        .getDescriptor(IPerforceUIConstants.IMG_INTEGRATE));
                menu.appendToGroup(GEFActionConstants.GROUP_EDIT, fromAction);
            }

            addDirectionMenu(mapping, menu);

            if (mapping instanceof BranchSpecMapping) {
                addBranchSpecMappingActions((BranchSpecMapping) mapping, menu);
            }

            addRemoveAction(menu);
        }
    }

    private void addBranchSpecMappingActions(final BranchSpecMapping mapping,
            IMenuManager menu) {
        IBranchGraph graph = mapping.getGraph();
        if (graph != null) {
            final IP4Connection connection = graph.getConnection();
            if (connection != null) {
                Action editSpec = new Action(
                        Messages.GraphContextMenuProvider_EditBranchSpec) {

                    @Override
                    public void run() {
                        EditBranchAction edit = new EditBranchAction();
                        edit.selectionChanged(null, new StructuredSelection(
                                mapping.generateBranch(connection)));
                        edit.run(null);
                    }
                };
                menu.appendToGroup(GEFActionConstants.GROUP_EDIT, editSpec);
            }
        }
    }

    private void addBranchActions(Object editPart, IMenuManager menu) {
        final Branch branch = P4CoreUtils.convert(editPart, Branch.class);
        if (branch != null) {

            CommandAction editAction = new CommandAction(new BranchEditCommand(
                    branch), getViewer());
            editAction.setText(Messages.GraphContextMenuProvider_EditBranch);
            editAction.setImageDescriptor(PerforceUIPlugin
                    .getDescriptor(IPerforceUIConstants.IMG_EDIT));
            menu.appendToGroup(GEFActionConstants.GROUP_EDIT, editAction);

            MenuManager integrateFrom = new MenuManager(
                    Messages.GraphContextMenuProvider_IntegrateFrom,
                    PerforceUIPlugin
                            .getDescriptor(IPerforceUIConstants.IMG_INTEGRATE),
                    "integrateFrom"); //$NON-NLS-1$
            menu.appendToGroup(GEFActionConstants.GROUP_EDIT, integrateFrom);

            MenuManager integrateTo = new MenuManager(
                    Messages.GraphContextMenuProvider_IntegrateTo,
                    PerforceUIPlugin
                            .getDescriptor(IPerforceUIConstants.IMG_INTEGRATE),
                    "integrateTo"); //$NON-NLS-1$
            menu.appendToGroup(GEFActionConstants.GROUP_EDIT, integrateTo);

            for (Mapping mapping : branch.getSourceMappings()) {
                integrateTo
                        .add(new BranchIntegrateAction(mapping, false, false));
            }

            for (Mapping mapping : branch.getTargetMappings()) {
                if (mapping.getDirection() == Direction.BOTH
                        || mapping.getDirection() == Direction.TARGET) {
                    integrateFrom.add(new BranchIntegrateAction(mapping, true,
                            true));
                }
                if (mapping.getDirection() == Direction.BOTH
                        || mapping.getDirection() == Direction.SOURCE) {
                    integrateTo.add(new BranchIntegrateAction(mapping, false,
                            true));
                }
            }

            addRemoveAction(menu);
            addNewMappingActions(branch, menu);

        }
    }

    private void addNewMappingActions(Branch branch, IMenuManager menu) {
        menu.appendToGroup(GEFActionConstants.GROUP_EDIT, new Separator());

        MenuManager addMapping = new MenuManager(
                Messages.GraphContextMenuProvider_AddBranchSpecMapping,
                PerforceUIPlugin.getDescriptor(IPerforceUIConstants.IMG_BRANCH),
                "addBranchSpecMapping"); //$NON-NLS-1$
        menu.appendToGroup(GEFActionConstants.GROUP_EDIT, addMapping);

        MappingCreateAction createFrom = new MappingCreateAction(graph,
                BranchSpecMapping.TYPE);
        createFrom.setText(MessageFormat.format(
                Messages.GraphContextMenuProvider_From, branch.getName()));
        createFrom.setInitialSource(branch);
        addMapping.add(createFrom);

        MappingCreateAction createTo = new MappingCreateAction(graph,
                BranchSpecMapping.TYPE);
        createTo.setText(MessageFormat.format(
                Messages.GraphContextMenuProvider_To, branch.getName()));
        createTo.setInitialTarget(branch);
        addMapping.add(createTo);

        addMapping = new MenuManager(
                Messages.GraphContextMenuProvider_AddDepotPathMapping,
                P4BranchGraphPlugin
                        .getImageDescriptor(IP4BranchGraphConstants.DEPOT_PATH_MAPPING),
                "addDepotPathMapping"); //$NON-NLS-1$
        menu.appendToGroup(GEFActionConstants.GROUP_EDIT, addMapping);

        createFrom = new MappingCreateAction(graph, DepotPathMapping.TYPE);
        createFrom.setText(MessageFormat.format(
                Messages.GraphContextMenuProvider_From, branch.getName()));
        createFrom.setInitialSource(branch);
        addMapping.add(createFrom);

        createTo = new MappingCreateAction(graph, DepotPathMapping.TYPE);
        createTo.setText(MessageFormat.format(
                Messages.GraphContextMenuProvider_To, branch.getName()));
        createTo.setInitialTarget(branch);
        addMapping.add(createTo);
    }
}
