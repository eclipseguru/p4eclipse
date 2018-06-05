/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.views.tasks;

import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.core.mergequest.model.Mapping;
import com.perforce.team.core.p4java.IP4ConnectionProvider;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.ui.DecorationLabel;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4FormUIUtils;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceContentProvider;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.mergequest.IP4BranchGraphConstants;
import com.perforce.team.ui.mergequest.Messages;
import com.perforce.team.ui.mergequest.actions.MappingIntegrateAction;
import com.perforce.team.ui.mergequest.parts.SharedResources;
import com.perforce.team.ui.submitted.SubmittedFileContentProvider;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class TaskContainer implements IIntegrateTaskContainer,
        PropertyChangeListener {

    private IP4SubmittedChangelist[] lists;
    private List<DateTaskGroup> dateAdapters = new ArrayList<DateTaskGroup>();
    private Map<String, UserTaskGroup> userAdapters = new HashMap<String, UserTaskGroup>();
    private Map<String, JobTaskGroup> taskAdapters = new HashMap<String, JobTaskGroup>();

    private Section sourceSection;
    private Mode sourceMode = Mode.FLAT;
    private Composite sourceSide;
    private CLabel sourceLabel;
    private Label arrowLabel;
    private CLabel targetLabel;
    private DecorationLabel sourceListLabel;
    private ToolBar toolbar;
    private TreeViewer sourceViewer;
    private WorkbenchLabelProvider labelProvider;
    private IP4ConnectionProvider provider = null;
    private Mapping mapping = null;
    private IBranchGraph graph = null;
    private SharedResources resources = new SharedResources();

    private boolean reverse = false;

    /**
     * Create a task container
     * 
     * @param provider
     */
    public TaskContainer(IP4ConnectionProvider provider) {
        this.provider = provider;
    }

    /**
     * Create a task container
     * 
     * @param provider
     * @param reverse
     */
    public TaskContainer(IP4ConnectionProvider provider, boolean reverse) {
        this(provider);
        this.reverse = reverse;
    }

    /**
     * Create task container control
     * 
     * @param toolkit
     * @param parent
     */
    public void createControl(FormToolkit toolkit, Composite parent) {
        parent.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                resources.dispose();
                unhookGraphListener();
            }
        });

        labelProvider = new WorkbenchLabelProvider();

        parent.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                if (labelProvider != null) {
                    labelProvider.dispose();
                }
            }
        });
        sourceSection = toolkit.createSection(parent,
                ExpandableComposite.TITLE_BAR
                        | ExpandableComposite.LEFT_TEXT_CLIENT_ALIGNMENT);
        GridData ssData = new GridData(SWT.FILL, SWT.FILL, true, true);
        sourceSection.setLayoutData(ssData);

        Composite sourceHeader = P4FormUIUtils.createSectionTextClient(toolkit,
                sourceSection, 5);

        sourceSide = toolkit.createComposite(sourceSection);
        sourceSection.setClient(sourceSide);
        GridLayout ssLayout = new GridLayout(1, false);
        ssLayout.marginHeight = 0;
        ssLayout.marginWidth = 0;
        ssLayout.verticalSpacing = 0;
        sourceSide.setLayout(ssLayout);
        sourceSide.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        sourceLabel = new CLabel(sourceHeader, SWT.NONE);
        sourceLabel.setText(""); //$NON-NLS-1$
        sourceLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER,
                false, false));
        sourceLabel.setFont(sourceSection.getFont());
        sourceLabel.setForeground(sourceSection.getTitleBarForeground());

        arrowLabel = new Label(sourceHeader, SWT.LEFT);
        arrowLabel
                .setImage(resources.getImage(IP4BranchGraphConstants.ARROW_E));
        arrowLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
                false));
        arrowLabel.setVisible(false);

        targetLabel = new CLabel(sourceHeader, SWT.NONE);
        targetLabel.setText(""); //$NON-NLS-1$
        targetLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER,
                false, false));
        targetLabel.setFont(sourceSection.getFont());
        targetLabel.setForeground(sourceSection.getTitleBarForeground());

        sourceListLabel = new DecorationLabel(sourceHeader, "(", ")"); //$NON-NLS-1$ //$NON-NLS-2$
        sourceListLabel.setForeground(sourceSection.getTitleBarForeground());
        sourceListLabel.getControl().setLayoutData(
                new GridData(SWT.FILL, SWT.CENTER, true, false));

        toolbar = new ToolBar(sourceHeader, SWT.FLAT);
        toolbar.setVisible(false);
        ToolItem integrate = new ToolItem(toolbar, SWT.PUSH);
        integrate.setToolTipText(Messages.TaskContainer_IntegrateMapping);
        integrate.setImage(resources.getImage(PerforceUIPlugin
                .getDescriptor(IPerforceUIConstants.IMG_INTEGRATE)));
        integrate.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                MappingIntegrateAction integ = new MappingIntegrateAction(
                        mapping, reverse);
                integ.run();
            }
        });

        ToolItem collapse = new ToolItem(toolbar, SWT.PUSH);
        collapse.setImage(resources.getImage(PerforceUIPlugin
                .getDescriptor(IPerforceUIConstants.IMG_COLLAPSE)));
        collapse.setToolTipText(Messages.TaskContainer_CollapseAll);
        collapse.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                sourceViewer.collapseAll();
            }

        });

        sourceViewer = createListViewer(sourceSide, 1);
        IntegrateTaskLabelProvider provider = new IntegrateTaskLabelProvider(
                this);
        sourceViewer.setSorter(new IntegrateTaskSorter(provider
                .getLabelProvider()));
    }

    /**
     * Set mode
     * 
     * @param mode
     */
    public void setMode(Mode mode) {
        if (mode != null) {
            this.sourceMode = mode;
            setInput();
        }
    }

    private TreeViewer createListViewer(Composite parent, int hSpan) {
        TreeViewer viewer = new TreeViewer(parent, SWT.SINGLE | SWT.H_SCROLL
                | SWT.BORDER | SWT.V_SCROLL);
        GridData vData = new GridData(SWT.FILL, SWT.FILL, true, true);
        vData.widthHint = 300;
        vData.heightHint = P4UIUtils.VIEWER_HEIGHT_HINT;
        vData.horizontalSpan = hSpan;
        viewer.getTree().setLayoutData(vData);
        viewer.setContentProvider(new SubmittedFileContentProvider(viewer, true));
        return viewer;
    }

    /**
     * Show or hide the container
     * 
     * @param show
     */
    public void show(boolean show) {
        ((GridData) sourceSection.getLayoutData()).exclude = !show;
        sourceSection.setVisible(show);
    }

    private void setInput() {
        if (lists == null) {
            lists = new IP4SubmittedChangelist[0];
        }
        switch (this.sourceMode) {
        case FLAT:
            sourceViewer.setInput(lists);
            break;
        case USER:
            setExpandedInput(userAdapters.values().toArray());
            break;
        case DATE:
            setExpandedInput(dateAdapters.toArray());
            break;
        case TASK:
            if (taskAdapters.isEmpty() && lists.length > 0) {
                loadTasks();
            } else {
                setExpandedInput(taskAdapters.values().toArray());
            }
        default:
            break;
        }
    }

    private void setExpandedInput(Object[] input) {
        sourceViewer.setInput(input);
    }

    private void loadTasks() {
        showLoading();
        final IP4SubmittedChangelist[] taskLists = this.lists;
        P4Runner.schedule(new P4Runnable() {

            private Map<String, JobTaskGroup> taskAdapters = new HashMap<String, JobTaskGroup>();

            @Override
            public String getTitle() {
                return Messages.TaskContainer_LoadingFixes;
            }

            private void addList(String fix, IP4SubmittedChangelist list) {
                String id = fix;
                if (id == null) {
                    id = JobTaskGroup.NO_TASKS_ATTACHED;
                }
                JobTaskGroup adapter = taskAdapters.get(fix);
                if (adapter == null) {
                    IP4Job job = null;
                    if (fix != null) {
                        job = list.getConnection().getJob(id);
                    }
                    adapter = new JobTaskGroup(job);
                    taskAdapters.put(fix, adapter);
                }
                adapter.add(list);
            }

            @Override
            public void run(IProgressMonitor monitor) {
                monitor.beginTask(getTitle(), taskLists.length);
                for (IP4SubmittedChangelist list : taskLists) {
                    monitor.subTask(MessageFormat.format(
                            Messages.TaskContainer_Changelist,
                            Integer.toString(list.getId())));
                    String[] fixes = list.getJobIds();
                    if (fixes.length == 0) {
                        addList(null, list);
                    } else {
                        for (String fix : fixes) {
                            addList(fix, list);
                        }
                    }
                    monitor.worked(1);
                    if (monitor.isCanceled()) {
                        break;
                    }
                }
                monitor.done();
                PerforceUIPlugin.syncExec(new Runnable() {

                    public void run() {
                        if (P4UIUtils.okToUse(sourceViewer)
                                && Arrays.equals(taskLists,
                                        TaskContainer.this.lists)) {
                            TaskContainer.this.taskAdapters = taskAdapters;
                            if (Mode.TASK == sourceMode) {
                                setInput();
                            }
                        }
                    }
                });
            }

        });
    }

    private void updateHeader(Mapping newMapping, IP4SubmittedChangelist[] lists) {
        Branch source = null;
        Branch target = null;
        if (newMapping != null) {
            source = reverse ? newMapping.getTarget() : newMapping.getSource();
            target = reverse ? newMapping.getSource() : newMapping.getTarget();
        }
        if (newMapping != null && source != null && target != null) {
            toolbar.setVisible(true);
            arrowLabel.setVisible(true);
            sourceLabel.setImage(labelProvider.getImage(source));
            sourceLabel.setText(source.getName());
            targetLabel.setImage(labelProvider.getImage(target));
            targetLabel.setText(target.getName());

            String message = Messages.IntegrateTaskViewer_ChangelistCount;
            if (lists.length == 1) {
                message = Messages.IntegrateTaskViewer_OneChangelist;
            }
            sourceListLabel
                    .setText(MessageFormat.format(message, lists.length));
        } else {
            toolbar.setVisible(false);
            ((GridData) sourceSection.getLayoutData()).exclude = false;
            arrowLabel.setVisible(false);
            sourceSection.setVisible(true);
            sourceLabel.setImage(null);
            sourceLabel.setText(""); //$NON-NLS-1$
            targetLabel.setText(""); //$NON-NLS-1$
            targetLabel.setImage(null);
            sourceListLabel.setText(null);
        }
    }

    private void updateGroups(Mapping mapping, IP4SubmittedChangelist[] lists) {
        userAdapters.clear();
        dateAdapters.clear();
        taskAdapters.clear();

        if (mapping != null) {
            Calendar calendar = new GregorianCalendar();
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            Date thisMonth = calendar.getTime();

            calendar = new GregorianCalendar();
            calendar.set(Calendar.DAY_OF_YEAR, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            Date thisYear = calendar.getTime();

            DateTaskGroup month = new DateTaskGroup(
                    Messages.TaskContainer_ThisMonth, thisMonth);
            DateTaskGroup year = new DateTaskGroup(
                    Messages.TaskContainer_ThisYear, thisYear);
            DateTaskGroup earlier = new DateTaskGroup(MessageFormat.format(
                    Messages.TaskContainer_EarlierThan,
                    Integer.toString(calendar.get(Calendar.YEAR))), new Date(
                    thisYear.getTime() - 1));

            dateAdapters.add(month);
            dateAdapters.add(year);
            dateAdapters.add(earlier);
            for (IP4SubmittedChangelist list : lists) {
                String username = list.getUserName();
                if (username != null) {
                    UserTaskGroup user = this.userAdapters.get(username);
                    if (user == null) {
                        user = new UserTaskGroup(username, this.provider);
                        this.userAdapters.put(username, user);
                    }
                    user.add(list);
                }
                Date date = list.getDate();
                if (date != null) {
                    if (date.after(thisMonth)) {
                        month.add(list);
                    } else if (date.after(thisYear)) {
                        year.add(list);
                    } else {
                        earlier.add(list);
                    }
                }
            }
        }
    }

    private void hookGraphListener() {
        if (graph != null) {
            graph.addPropertyListener(Branch.NAME, this);
            graph.addPropertyListener(Branch.TYPE, this);
        }
    }

    private void unhookGraphListener() {
        if (graph != null) {
            graph.removePropertyListener(Branch.NAME, this);
            graph.removePropertyListener(Branch.TYPE, this);
        }
    }

    /**
     * Set input to container
     * 
     * @param mapping
     * @param lists
     */
    public void setInput(Mapping mapping, IP4SubmittedChangelist[] lists) {
        unhookGraphListener();
        this.mapping = mapping;
        if (this.mapping != null) {
            this.graph = this.mapping.getGraph();
        } else {
            this.graph = null;
        }
        hookGraphListener();
        if (lists == null) {
            lists = new IP4SubmittedChangelist[0];
        }
        this.lists = lists;
        updateHeader(mapping, lists);
        updateGroups(mapping, lists);
        setInput();
        sourceSection.layout();
    }

    /**
     * @see com.perforce.team.ui.mergequest.views.tasks.IIntegrateTaskContainer#getViewer()
     */
    public TreeViewer getViewer() {
        return this.sourceViewer;
    }

    /**
     * Show the loading indicator in the viewer
     */
    public void showLoading() {
        PerforceContentProvider cProvider = (PerforceContentProvider) this.sourceViewer
                .getContentProvider();
        this.sourceViewer.setInput(new PerforceContentProvider.Loading());//cProvider.new Loading());
    }

    /**
     * @see com.perforce.team.ui.mergequest.views.tasks.IIntegrateTaskContainer#getMode()
     */
    public Mode getMode() {
        return this.sourceMode;
    }

    /**
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        PerforceUIPlugin.asyncExec(new Runnable() {

            public void run() {
                if (P4UIUtils.okToUse(sourceSection)) {
                    updateHeader(mapping, lists);
                    sourceSection.layout();
                }
            }
        });
    }

}
