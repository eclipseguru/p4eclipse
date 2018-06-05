/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.editor;

import com.perforce.p4java.core.IChangelist;
import com.perforce.team.core.mylyn.IP4MylynConstants;
import com.perforce.team.core.mylyn.P4JobConnector;
import com.perforce.team.core.mylyn.P4MylynUtils;
import com.perforce.team.core.mylyn.P4JobConnector.IJobCallback;
import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4Listener;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4FormUIUtils;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.p4java.actions.FixJobAction;
import com.perforce.team.ui.p4java.actions.UnfixJobAction;
import com.perforce.team.ui.submitted.SubmittedChangelistDialog;
import com.perforce.team.ui.views.MenuFilter;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class FixFormPage extends FormPage implements IRefreshEditorPart,
        IP4Listener {

    /**
     * ID
     */
    public static final String ID = "jobFixes"; //$NON-NLS-1$

    private ISchedulingRule rule = P4Runner.createRule();
    private IP4Connection connection = null;

    /**
     * Job id
     */
    protected String id = null;

    private Composite body;

    private Section pendingSection;
    private PendingFixWidget pendingArea;
    private MenuManager pendingMenuManager;

    private Section submittedSection;
    private SubmittedFixWidget submittedArea;
    private MenuManager submittedMenuManager;

    /**
     * @param editor
     */
    public FixFormPage(FormEditor editor) {
        super(editor, ID, Messages.FixFormPage_Fixes);
    }

    /**
     * @see org.eclipse.ui.forms.editor.FormPage#init(org.eclipse.ui.IEditorSite,
     *      org.eclipse.ui.IEditorInput)
     */
    @Override
    public void init(IEditorSite site, IEditorInput input) {
        super.init(site, input);
        this.connection = getConnection();
        this.id = getJobId();
    }

    /**
     * Get the job id for this editor
     * 
     * @return - job id
     */
    protected String getJobId() {
        String id = null;
        IEditorInput input = getEditorInput();
        if (input instanceof TaskEditorInput) {
            ITask task = ((TaskEditorInput) input).getTask();
            if (task != null) {
                id = task.getTaskKey();
            }
        }
        return id;
    }

    /**
     * Get changelist ids that are fixes
     * 
     * @return - array of changelist ids
     */
    protected Integer[] getFixIds() {
        return this.connection.getFixIds(this.id);
    }

    /**
     * Load the fix changelists
     * 
     * @param monitor
     * @return - non-null but possible empty array of changelist fixes
     */
    protected IP4Changelist[] loadFixes(IProgressMonitor monitor) {
        Integer[] fixes = getFixIds();
        if(fixes==null)
        	return new IP4Changelist[0];
        
        monitor.beginTask("", fixes.length); //$NON-NLS-1$
        List<IP4Changelist> lists = new ArrayList<IP4Changelist>();
        for (Integer fix : fixes) {
            int id = fix.intValue();
            if (id > IChangelist.DEFAULT) {
                IP4Changelist list = connection.getChangelistById(id, null,
                        true, true);
                if (list != null) {
                    lists.add(list);
                }
            }
            monitor.worked(1);
        }
        monitor.done();
        return lists.toArray(new IP4Changelist[lists.size()]);
    }

    /**
     * Get the connection for this editor
     * 
     * @return - connection
     */
    protected IP4Connection getConnection() {
        IP4Connection connection = null;
        IEditorInput input = getEditorInput();
        if (input instanceof TaskEditorInput) {
            connection = P4MylynUtils.getConnection(((TaskEditorInput) input)
                    .getTaskRepository());
        }
        return connection;
    }

    private void createPendingSection(Composite parent,
            final FormToolkit toolkit) {
        pendingSection = toolkit.createSection(parent,
                ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR
                        | ExpandableComposite.TWISTIE);
        pendingSection.setText(Messages.FixFormPage_PendingFixes);
        pendingSection.setLayout(new GridLayout(2, false));
        pendingSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                true));
        P4FormUIUtils.registerExpansionSpaceGrabber(pendingSection, body);

        Composite area = toolkit.createComposite(pendingSection);
        GridLayout aLayout = new GridLayout(2, false);
        aLayout.marginHeight = 0;
        aLayout.marginWidth = 0;
        area.setLayout(aLayout);
        area.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        pendingSection.setClient(area);

        pendingArea = new PendingFixWidget(true, this.id) {

            @Override
            protected void createToolbar(Composite parent) {
                ToolBar toolbar = P4FormUIUtils.createSectionToolbar(toolkit,
                        pendingSection);
                fillToolbar(toolbar);
            }
            
            @Override
            public String getName() {
            	return FixFormPage.class.getSimpleName();
            }

        };
        pendingArea.createControl(area);
        pendingArea.setCallback(new Runnable() {

            public void run() {
                updateTitles(pendingArea.getSize(), submittedArea.getSize());
            }
        });

        this.pendingMenuManager = createMenu(pendingArea.getViewer());
    }

    private void createSubmittedSection(Composite parent,
            final FormToolkit toolkit) {
        submittedSection = toolkit.createSection(parent,
                ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR
                        | ExpandableComposite.TWISTIE);
        submittedSection.setText(Messages.FixFormPage_SubmittedFixes);
        submittedSection.setLayout(new GridLayout(2, false));
        submittedSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                true));
        P4FormUIUtils.registerExpansionSpaceGrabber(submittedSection, body);

        Composite area = toolkit.createComposite(submittedSection);
        GridLayout aLayout = new GridLayout(2, false);
        aLayout.marginHeight = 0;
        aLayout.marginWidth = 0;
        area.setLayout(aLayout);
        area.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        submittedSection.setClient(area);

        submittedArea = new SubmittedFixWidget(true, this.id) {

            @Override
            protected void createToolbar(Composite parent) {
                ToolBar toolbar = P4FormUIUtils.createSectionToolbar(toolkit,
                        submittedSection);
                fillSubmittedToolbar(toolbar);
                new ToolItem(toolbar, SWT.SEPARATOR);
                fillToolbar(toolbar);
            }
            
            @Override
            public String getName() {
            	return SubmittedFixWidget.class.getSimpleName();
            }

        };
        submittedArea.createControl(area);
        submittedArea.setCallback(new Runnable() {

            public void run() {
                updateTitles(pendingArea.getSize(), submittedArea.getSize());
            }
        });

        this.submittedMenuManager = createMenu(submittedArea.getViewer());
    }

    private IP4SubmittedChangelist[] getSelectedChangelists() {
        List<IP4SubmittedChangelist> jobs = new ArrayList<IP4SubmittedChangelist>();
        IStructuredSelection selection = (IStructuredSelection) this.submittedArea
                .getViewer().getSelection();
        for (Object element : selection.toArray()) {
            if (element instanceof IP4SubmittedChangelist) {
                jobs.add((IP4SubmittedChangelist) element);
            }
        }
        return jobs.toArray(new IP4SubmittedChangelist[jobs.size()]);
    }

    private void addFix(final IP4Job job) {
        if (job == null) {
            return;
        }
        PerforceUIPlugin.syncExec(new Runnable() {

            public void run() {
                SubmittedChangelistDialog dialog = new SubmittedChangelistDialog(
                        body.getShell(), getConnection());
                if (SubmittedChangelistDialog.OK == dialog.open()) {
                    IP4SubmittedChangelist[] lists = dialog.getSelected();
                    FixJobAction fix = new FixJobAction();
                    fix.fix(lists, job);
                }
            }
        });
    }

    private void removeFix(final IP4Job job) {
        if (job == null) {
            return;
        }
        PerforceUIPlugin.syncExec(new Runnable() {

            public void run() {
                IP4SubmittedChangelist[] lists = getSelectedChangelists();
                if (lists.length == 0) {
                    return;
                }
                String suffix = lists.length > 1
                        ? Messages.FixFormPage_Changelists
                        : Messages.FixFormPage_Changelist;
                String message = MessageFormat.format(
                        Messages.FixFormPage_Remove, lists.length, suffix,
                        job.getId());
                if (P4ConnectionManager.getManager().openConfirm(
                        Messages.FixFormPage_ConfirmRemoval, message)) {
                    UnfixJobAction unfix = new UnfixJobAction();
                    unfix.unfix(lists, job);
                }
            }
        });
    }

    private void fillSubmittedToolbar(ToolBar toolbar) {
        ToolItem addFixItem = new ToolItem(toolbar, SWT.PUSH);
        addFixItem.setToolTipText(Messages.FixFormPage_AddChangelist);
        Image addImage = PerforceUIPlugin.getDescriptor(
                IPerforceUIConstants.IMG_ADD).createImage();
        P4UIUtils.registerDisposal(addFixItem, addImage);
        addFixItem.setImage(addImage);
        addFixItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                P4JobConnector connector = (P4JobConnector) TasksUi
                        .getRepositoryConnector(IP4MylynConstants.KIND);
                IP4Job job = connector.getCachedJob(id, connection, true,
                        new IJobCallback() {

                            public void loaded(IP4Job job) {
                                addFix(job);
                            }
                        });
                addFix(job);
            }

        });

        ToolItem removeFixItem = new ToolItem(toolbar, SWT.PUSH);
        removeFixItem.setToolTipText(Messages.FixFormPage_RemoveChangelists);
        Image removeImage = PerforceUIPlugin.getDescriptor(
                IPerforceUIConstants.IMG_DELETE).createImage();
        P4UIUtils.registerDisposal(removeFixItem, removeImage);
        removeFixItem.setImage(removeImage);
        removeFixItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                P4JobConnector connector = (P4JobConnector) TasksUi
                        .getRepositoryConnector(IP4MylynConstants.KIND);
                IP4Job job = connector.getCachedJob(id, connection, true,
                        new IJobCallback() {

                            public void loaded(IP4Job job) {
                                removeFix(job);
                            }
                        });
                removeFix(job);
            }

        });
    }

    private MenuManager createMenu(TreeViewer viewer) {
        Tree tree = viewer.getTree();
        MenuManager manager = new MenuManager();
        Menu menu = manager.createContextMenu(tree);
        manager.addMenuListener(new IMenuListener() {

            public void menuAboutToShow(IMenuManager manager) {
                manager.add(new Separator("perforce.opengroup")); //$NON-NLS-1$
                manager.add(new Separator("perforce.group1")); //$NON-NLS-1$
                manager.add(new Separator("perforce.group2")); //$NON-NLS-1$
                manager.add(new Separator("perforce.group3")); //$NON-NLS-1$
                manager.add(new Separator("perforce.group4")); //$NON-NLS-1$
                manager.add(new Separator("perforce.group5")); //$NON-NLS-1$
                manager.add(new Separator(
                        IWorkbenchActionConstants.MB_ADDITIONS));
            }
        });
        manager.setRemoveAllWhenShown(true);
        tree.setMenu(menu);
        getSite().registerContextMenu(manager, viewer);
        manager.addMenuListener(MenuFilter.createTeamMainFilter());
        return manager;
    }

    /**
     * @see org.eclipse.ui.forms.editor.FormPage#createFormContent(org.eclipse.ui.forms.IManagedForm)
     */
    @Override
    protected void createFormContent(IManagedForm managedForm) {
        body = managedForm.getForm().getBody();
        body.setLayout(new GridLayout(1, true));
        FormToolkit toolkit = managedForm.getToolkit();
        createPendingSection(body, toolkit);
        createSubmittedSection(body, toolkit);
        loadFixes();
        P4Workspace.getWorkspace().addListener(this);
    }

    private void updateTitles(final int pending, final int submitted) {
        PerforceUIPlugin.syncExec(new Runnable() {

            public void run() {
                if (P4UIUtils.okToUse(pendingSection)) {
                    pendingSection.setText(MessageFormat.format(
                            Messages.FixFormPage_PendingFixesNumber, pending));
                    pendingSection.layout(true, true);
                }
                if (P4UIUtils.okToUse(submittedSection)) {
                    submittedSection.setText(MessageFormat.format(
                            Messages.FixFormPage_SubmittedFixesNumber,
                            submitted));
                    submittedSection.layout(true, true);
                }
            }
        });
    }

    private void loadFixes() {
        if (!P4UIUtils.okToUse(body)) {
            return;
        }
        if (id != null) {
            pendingArea.showLoading();
            submittedArea.showLoading();
            P4Runner.schedule(new P4Runnable() {

                @Override
                public String getTitle() {
                    return Messages.FixFormPage_LoadingFixes + id;
                }

                @Override
                public void run(IProgressMonitor monitor) {
                    IP4Changelist[] fixLists = loadFixes(monitor);
                    int pendingShown = pendingArea.setFixes(fixLists);
                    int submittedShown = submittedArea.setFixes(fixLists);
                    updateTitles(pendingShown, submittedShown);
                }

            }, rule);
        } else {
            updateTitles(0, 0);
        }
    }

    /**
     * @see org.eclipse.ui.forms.editor.FormPage#dispose()
     */
    @Override
    public void dispose() {
        P4Workspace.getWorkspace().removeListener(this);
        if (pendingMenuManager != null) {
            pendingMenuManager.dispose();
        }
        if (submittedMenuManager != null) {
            submittedMenuManager.dispose();
        }
        super.dispose();
    }

    /**
     * @see com.perforce.team.ui.mylyn.editor.IRefreshEditorPart#refresh()
     */
    public void refresh() {
        loadFixes();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Listener#resoureChanged(com.perforce.team.core.p4java.P4Event)
     */
    public void resoureChanged(P4Event event) {
        if (event.getType() == EventType.SUBMIT_CHANGELIST) {
            for (IP4PendingChangelist submitted : event.getPending()) {
                if (pendingArea.contains(submitted)) {
                    PerforceUIPlugin.asyncExec(new Runnable() {

                        public void run() {
                            loadFixes();
                        }
                    });
                    break;
                }
            }
        }
    }

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}
}
