/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.views.tasks;

import com.perforce.team.core.mergequest.model.BranchSpecMapping;
import com.perforce.team.core.mergequest.model.DepotPathMapping;
import com.perforce.team.core.mergequest.model.Mapping;
import com.perforce.team.core.mergequest.model.Mapping.Direction;
import com.perforce.team.core.mergequest.processor.InterchangesProcessor;
import com.perforce.team.core.p4java.IP4ConnectionProvider;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.mergequest.IP4BranchGraphConstants;
import com.perforce.team.ui.mergequest.P4BranchGraphPlugin;
import com.perforce.team.ui.mergequest.editor.BranchGraphToolkit;
import com.perforce.team.ui.mergequest.parts.SharedResources;
import com.perforce.team.ui.mergequest.views.tasks.IIntegrateTaskContainer.Mode;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class IntegrateTaskViewer implements PropertyChangeListener {

    /**
     * MODE_SETTING
     */
    public static final String MODE_SETTING = "MODE_SETTING"; //$NON-NLS-1$

    /**
     * Format date
     * 
     * @param date
     * @return - string
     */
    public static String formatDate(Date date) {
        return DateFormat.getTimeInstance(DateFormat.FULL).format(date);
    }

    private IManagedForm form;

    private Composite parent;
    private TaskContainer sourceContainer;
    private TaskContainer targetContainer;
    private SharedResources resources;
    private ImageDescriptor flatImageDescriptor;
    private ImageDescriptor groupImageDescriptor;
    private ImageDescriptor dateImageDescriptor;
    private ImageDescriptor taskImageDescriptor;

    private Action refreshAction;

    private IP4ConnectionProvider provider;
    private InterchangesProcessor processor;

    private Mapping mapping;

    private Mode mode;

    private WorkbenchLabelProvider labelProvider;

    /**
     * Create new integrate task viewer
     * 
     * @param provider
     * @param processor
     */
    public IntegrateTaskViewer(IP4ConnectionProvider provider,
            InterchangesProcessor processor) {
        this.provider = provider;
        this.processor = processor;
        this.mode = getDefaultMode();
    }

    private Mode getDefaultMode() {
        Mode mode = null;
        String value = P4BranchGraphPlugin.getDefault().getPreferenceStore()
                .getString(MODE_SETTING);
        try {
            mode = Mode.valueOf(value);
        } catch (Exception e) {
            mode = Mode.FLAT;
        }
        return mode;
    }

    /**
     * Create mapping changelist viewer
     * 
     * @param parent
     */
    public void createControl(Composite parent) {
        labelProvider = new WorkbenchLabelProvider();
        final BranchGraphToolkit toolkit = new BranchGraphToolkit(
                P4UIUtils.getDisplay());

        form = new ManagedForm(toolkit, toolkit.createScrolledForm(parent));

        ScrolledForm formControl = form.getForm();
        formControl.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                toolkit.dispose();
            }
        });

        updateTitle();
        formControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        parent = formControl.getBody();
        this.parent = parent;
        this.resources = new SharedResources();
        this.parent.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                resources.dispose();
            }
        });
        flatImageDescriptor = PerforceUIPlugin
                .getDescriptor(IPerforceUIConstants.IMG_FLAT_LAYOUT);
        groupImageDescriptor = P4BranchGraphPlugin
                .getImageDescriptor(IP4BranchGraphConstants.USER_GROUP);
        dateImageDescriptor = PerforceUIPlugin
                .getDescriptor(IPerforceUIConstants.IMG_DATES);
        taskImageDescriptor = PerforceUIPlugin
                .getDescriptor(IPerforceUIConstants.IMG_CHG_JOB);
        parent.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                if (labelProvider != null) {
                    labelProvider.dispose();
                }
            }
        });

        GridLayout daLayout = new GridLayout(2, true);
        parent.setLayout(daLayout);

        Text description = new Text(parent, SWT.READ_ONLY | SWT.WRAP);
        description.setText(Messages.IntegrateTaskViewer_Description);
        toolkit.adapt(description, false, false);
        description.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.FILL, SWT.FILL).grab(true, false).span(2, 1)
                .create());

        targetContainer = new TaskContainer(this.provider);
        targetContainer.createControl(toolkit, parent);

        sourceContainer = new TaskContainer(this.provider, true);
        sourceContainer.createControl(toolkit, parent);

        toolkit.decorateFormHeading(form.getForm().getForm());

        createViewerToolbar(form.getForm().getForm().getToolBarManager());
    }

    private void createViewerToolbar(IToolBarManager manager) {

        refreshAction = new Action(Messages.IntegrateTaskViewer_RefreshTasks,
                PerforceUIPlugin
                        .getDescriptor(IPerforceUIConstants.IMG_REFRESH)) {

            @Override
            public void run() {
                refresh(true);
            }
        };
        manager.add(refreshAction);

        ContributionItem item = new ContributionItem() {

            @Override
            public void fill(final ToolBar parent, int index) {
                final ToolItem item = new ToolItem(parent, SWT.DROP_DOWN);
                changeMode(item, mode);
                item.addSelectionListener(new SelectionAdapter() {

                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        createListModeMenu(parent, item);
                    }

                    @Override
                    public void widgetDefaultSelected(SelectionEvent e) {
                        widgetSelected(e);
                    }
                });
            }

        };
        manager.add(item);
        manager.update(true);
    }

    private void changeMode(ToolItem item, Mode mode) {
        this.mode = mode;
        P4BranchGraphPlugin.getDefault().getPreferenceStore()
                .setValue(MODE_SETTING, this.mode.toString());
        switch (this.mode) {
        case FLAT:
            item.setImage(this.resources.getImage(this.flatImageDescriptor));
            item.setToolTipText(Messages.IntegrateTaskViewer_FlatMode);
            break;
        case USER:
            item.setImage(this.resources.getImage(this.groupImageDescriptor));
            item.setToolTipText(Messages.IntegrateTaskViewer_UserMode);
            break;
        case DATE:
            item.setImage(this.resources.getImage(this.dateImageDescriptor));
            item.setToolTipText(Messages.IntegrateTaskViewer_DateMode);
            break;
        case TASK:
            item.setImage(this.resources.getImage(this.taskImageDescriptor));
            item.setToolTipText(Messages.IntegrateTaskViewer_TaskMode);
            break;

        default:
            break;
        }
        sourceContainer.setMode(this.mode);
        targetContainer.setMode(this.mode);
    }

    private void createListModeMenu(final ToolBar toolbar, final ToolItem item) {
        Menu menu = new Menu(toolbar);

        final MenuItem listItem = new MenuItem(menu, SWT.CHECK);
        listItem.setText(Messages.IntegrateTaskViewer_ListMode);
        listItem.setImage(this.resources.getImage(flatImageDescriptor));
        listItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                changeMode(item, Mode.FLAT);
            }

        });

        final MenuItem userItem = new MenuItem(menu, SWT.CHECK);
        userItem.setText(Messages.IntegrateTaskViewer_UserMode);
        userItem.setImage(this.resources.getImage(this.groupImageDescriptor));
        userItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                changeMode(item, Mode.USER);
            }

        });

        final MenuItem dateItem = new MenuItem(menu, SWT.CHECK);
        dateItem.setText(Messages.IntegrateTaskViewer_DateMode);
        dateItem.setImage(this.resources.getImage(this.dateImageDescriptor));
        dateItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                changeMode(item, Mode.DATE);
            }

        });

        final MenuItem taskItem = new MenuItem(menu, SWT.CHECK);
        taskItem.setText(Messages.IntegrateTaskViewer_TaskMode);
        taskItem.setImage(this.resources.getImage(this.taskImageDescriptor));
        taskItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                changeMode(item, Mode.TASK);
            }

        });

        switch (this.mode) {
        case USER:
            userItem.setSelection(true);
            break;
        case DATE:
            dateItem.setSelection(true);
            break;
        case TASK:
            taskItem.setSelection(true);
            break;
        case FLAT:
        default:
            listItem.setSelection(true);
            break;
        }

        Rectangle bounds = item.getBounds();
        Point location = toolbar.toDisplay(bounds.x, bounds.y + bounds.height);
        menu.setLocation(location);
        menu.setVisible(true);
    }

    /**
     * Get source viewer
     * 
     * @return - tree viewer
     */
    public TreeViewer getSourceViewer() {
        return this.sourceContainer.getViewer();
    }

    /**
     * Get target viewer
     * 
     * @return - tree viewer
     */
    public TreeViewer getTargetViewer() {
        return this.targetContainer.getViewer();
    }

    private boolean forceMappingRefresh(Mapping mapping) {
    	if(processor==null)
    		return false;

        return (mapping.hasSourceChanges() && processor
                .getSourceInterchanges(mapping) == null)
                || (mapping.hasTargetChanges() && processor
                        .getTargetInterchanges(mapping) == null);
    }

    private void loadMapping(final Mapping mapping, boolean force) {
        if (!force) {
            force = forceMappingRefresh(mapping);
        }
        if (force) {
            sourceContainer.showLoading();
            targetContainer.showLoading();
            Mapping[] mappings = new Mapping[] { mapping };
            if(processor!=null){
	            processor.refresh(mappings);
	            processor.refreshInterchanges(mappings, new Runnable() {
	
	                public void run() {
	                    PerforceUIPlugin.asyncExec(new Runnable() {
	
	                        public void run() {
	                            if (mapping
	                                    .equals(IntegrateTaskViewer.this.mapping)
	                                    && P4UIUtils.okToUse(parent) && processor!=null) {
	                                updateDate(processor
	                                        .getLastRefreshDate(mapping));
	                                showMappings(processor
	                                        .getSourceInterchanges(mapping),
	                                        processor
	                                                .getTargetInterchanges(mapping));
	                            }
	                        }
	                    });
	                }
	            });
            }
        } else {
        	if(processor!=null){
				updateDate(processor.getLastRefreshDate(mapping));
				showMappings(processor.getSourceInterchanges(mapping),
						processor.getTargetInterchanges(mapping));
			}
        }
    }

    private void updateDate(Date date) {
        String dateText = ""; //$NON-NLS-1$
        if (date != null) {
            dateText = formatDate(date);
        }
        refreshAction.setToolTipText(MessageFormat.format(
                Messages.IntegrateTaskViewer_RefreshTasksTooltip, dateText));
    }

    private void showMappings(IP4SubmittedChangelist[] sourceLists,
            IP4SubmittedChangelist[] targetLists) {
        sourceContainer.setInput(this.mapping, sourceLists);
        targetContainer.setInput(this.mapping, targetLists);
        form.reflow(true);
    }

    private void refresh(boolean force) {
        Direction direction = mapping.getDirection();
        if (direction == Direction.BOTH) {
            ((GridLayout) this.parent.getLayout()).numColumns = 2;
        } else {
            ((GridLayout) this.parent.getLayout()).numColumns = 1;
        }
        sourceContainer.show(direction == Direction.BOTH
                || direction == Direction.SOURCE);
        targetContainer.show(direction == Direction.BOTH
                || direction == Direction.TARGET);
        form.getForm().setImage(labelProvider.getImage(mapping));
        updateTitle();
        loadMapping(mapping, force);
    }

    private void updateTitle() {
        if (mapping != null) {
            form.getForm().setText(mapping.getName());
        } else {
            form.getForm().setText(
                    Messages.IntegrateTaskViewer_NoMappingSelected);
        }

    }

    /**
     * Set viewer input
     * 
     * @param mapping
     * 
     */
    public void setInput(Mapping mapping) {
        if (this.mapping != null && this.mapping.equals(mapping)) {
            return;
        }
        sourceContainer.setInput(mapping, null);
        targetContainer.setInput(mapping, null);
        if (this.mapping != null) {
            this.mapping.removePropertyListener(this);
        }
        if (mapping != null) {
            this.mapping = mapping;
            this.mapping.addPropertyListener(this);
            refresh(false);
        } else {
            this.mapping = null;
            ((GridLayout) this.parent.getLayout()).numColumns = 2;
            sourceContainer.show(true);
            form.getForm().setImage(null);
            updateTitle();
        }
        form.reflow(true);
    }

    private boolean refreshTasks(String changedProperty) {
        if (changedProperty == null) {
            return false;
        }
        return Mapping.SOURCE_CHANGE_TYPE.equals(changedProperty)
                || Mapping.TARGET_CHANGE_TYPE.equals(changedProperty)
                || Mapping.CONNECTED.equals(changedProperty)
                || Mapping.NAME.equals(changedProperty);
    }

    private boolean forceRefresh(String changedProperty) {
        if (changedProperty == null) {
            return false;
        }
        return Mapping.LATEST_SOURCE_CHANGE.equals(changedProperty)
                || Mapping.LATEST_TARGET_CHANGE.equals(changedProperty)
                || Mapping.DIRECTION.equals(changedProperty)
                || DepotPathMapping.SOURCE_PATH.equals(changedProperty)
                || DepotPathMapping.TARGET_PATH.equals(changedProperty)
                || (mapping instanceof BranchSpecMapping && Mapping.NAME
                        .equals(changedProperty));
    }

    /**
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        final boolean force = forceRefresh(evt.getPropertyName());
        boolean refresh = force || refreshTasks(evt.getPropertyName());
        if (refresh) {
            PerforceUIPlugin.asyncExec(new Runnable() {

                public void run() {
                    refresh(force);
                }
            });
        }
    }

    /**
     * Dispose of the viewer.
     */
    public void dispose() {
        if (this.mapping != null) {
            this.mapping.removePropertyListener(this);
        }
    }
}
