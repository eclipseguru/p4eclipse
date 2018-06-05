/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.editor;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylyn.internal.tasks.core.data.ITaskDataManagerListener;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataManager;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.actions.SynchronizeEditorAction;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorDescriptionPart;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorPlanningPart;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.ITaskDataWorkingCopy;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.ide.IDE;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.mylyn.IP4MylynConstants;
import com.perforce.team.core.mylyn.P4MylynUtils;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.P4Depot;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.mylyn.P4MylynUiUtils;
import com.perforce.team.ui.p4java.actions.ShowHistoryAction;
import com.perforce.team.ui.text.timelapse.form.FormTimeLapseEditor;
import com.perforce.team.ui.timelapse.TimeLapseInput;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * @author Alex Li (ali@perforce.com)
 * 
 */
public class P4JobEditorPage extends AbstractTaskEditorPage {

    private static Field TASK_DATA_LISTENER;

    static {
        try {
            TASK_DATA_LISTENER = AbstractTaskEditorPage.class
                    .getDeclaredField("TASK_DATA_LISTENER"); //$NON-NLS-1$
            if (TASK_DATA_LISTENER != null) {
                TASK_DATA_LISTENER.setAccessible(true);
            }
        } catch (SecurityException e) {
            TASK_DATA_LISTENER = null;
        } catch (NoSuchFieldException e) {
            TASK_DATA_LISTENER = null;
        } catch (IllegalArgumentException e) {
            TASK_DATA_LISTENER = null;
        }
    }

    private JobFieldGroup page;
    private boolean isMainPage;
    private boolean allowModelRefresh = false;

    /**
     * @param editor
     * @param id
     * @param label
     * @param page
     * @param submit
     * @param isMainPage
     */
    public P4JobEditorPage(TaskEditor editor, String id, String label,
            JobFieldGroup page, boolean submit, boolean isMainPage) {
        super(editor, id, label, IP4MylynConstants.KIND);
        this.page = page;
        this.isMainPage = isMainPage;
        setNeedsPrivateSection(this.isMainPage); // Show Planning section
        setNeedsSubmit(submit);
        setNeedsSubmitButton(submit);
        
        if(page==null){
        	scheduleRefresh();
        }
    }

    private void scheduleRefresh() {
		Job job=new Job(Messages.P4JobEditor_RefreshJobEditor) {
			
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				if(page==null){
					TaskRepository repository = P4MylynUiUtils.getRepository(getEditor());
					if(isMainPage)
						page=JobFieldGroup.loadCorePage(P4MylynUtils.getConnection(repository));
					else
						page=JobFieldGroup.loadAdvancedPage(P4MylynUtils.getConnection(repository));
			        P4UIUtils.getDisplay().asyncExec(new Runnable() {

						public void run() {
							refresh();							
						}
			        });
				}
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		
		job.schedule();
	}

	private void showJobLookupError() {
        ITask task = getTask();
        if (task != null) {
            String key = task.getTaskKey();
            String message = MessageFormat.format(
                    Messages.P4JobEditorPage_HistoryNotFound, key);
            P4ConnectionManager.getManager().openError(
                    P4UIUtils.getDialogShell(),
                    Messages.P4JobEditorPage_ErrorRetrievingHistory, message);
        }
    }

    /**
     * @see org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage#fillToolBar(org.eclipse.jface.action.IToolBarManager)
     */
    @Override
    public void fillToolBar(IToolBarManager toolBarManager) {
        if (isMainPage) {
            super.fillToolBar(toolBarManager);

            TaskData data = getModelTaskData();
            if (data != null && !data.isNew()) {
                toolBarManager.remove(SynchronizeEditorAction.ID);

                final Action refresh = new Action(
                        Messages.P4JobEditorPage_Refresh) {

                    @Override
                    public void run() {
                        SynchronizeEditorAction syncEditor = new SynchronizeEditorAction();
                        syncEditor.selectionChanged(new StructuredSelection(
                                getTaskEditor()));
                        syncEditor.run();
                        PerforceUIPlugin.asyncExec(new Runnable() {

                            public void run() {
                                IFormPage page = getTaskEditor().findPage(
                                        FixFormPage.ID);
                                if (page instanceof IRefreshEditorPart) {
                                    ((IRefreshEditorPart) page).refresh();
                                }
                            }
                        });
                    }

                };
                refresh.setImageDescriptor(PerforceUIPlugin
                        .getDescriptor(IPerforceUIConstants.IMG_REFRESH));
                toolBarManager.appendToGroup("repository", refresh); //$NON-NLS-1$
            }

            final Action viewHistory = new Action(
                    Messages.P4JobEditorPage_ShowHistory,
                    PerforceUIPlugin
                            .getDescriptor(IPerforceUIConstants.IMG_HISTORY)) {

                @Override
                public void run() {
                    IP4File file = P4MylynUiUtils.getJobSpecFile(getTask());
                    if (file != null) {
                        ShowHistoryAction history = new ShowHistoryAction();
                        history.selectionChanged(null, new StructuredSelection(
                                file));
                        history.run(null);
                    } else {
                        showJobLookupError();
                    }
                }
            };
            viewHistory.setEnabled(false);
            toolBarManager.appendToGroup("open", viewHistory); //$NON-NLS-1$

            final Action timeLapse = new Action(
                    Messages.P4JobEditorPage_ViewTimeLapse,
                    PerforceUIPlugin
                            .getDescriptor(IPerforceUIConstants.IMG_TIME_LAPSE)) {

                @Override
                public void run() {
                    IP4File file = P4MylynUiUtils.getJobSpecFile(getTask());
                    if (file != null) {
                        TimeLapseInput input = new TimeLapseInput(file, false,
                                false);
                        try {
                            IDE.openEditor(PerforceUIPlugin.getActivePage(),
                                    input, FormTimeLapseEditor.ID);
                        } catch (PartInitException e) {
                            PerforceProviderPlugin.logError(e);
                        }
                    } else {
                        showJobLookupError();
                    }
                }
            };
            timeLapse.setEnabled(false);
            toolBarManager.appendToGroup("open", timeLapse); //$NON-NLS-1$

            P4Runner.schedule(new P4Runnable() {

                @Override
                public void run(IProgressMonitor monitor) {
                    IP4Connection connection = P4MylynUiUtils
                            .getConnection(getTask());
                    if (connection != null && !connection.isOffline()) {
                        if (connection.needsRefresh()) {
                            connection.refresh();
                        }
                        P4Depot spec = connection.getSpecDepot();
                        if (spec != null) {
                            viewHistory.setEnabled(true);
                            timeLapse.setEnabled(true);
                        }
                    }
                }
            });
        }
    }

    /**
     * @see org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage#createPartDescriptors()
     */
    @Override
    protected Set<TaskEditorPartDescriptor> createPartDescriptors() {

        Set<TaskEditorPartDescriptor> descriptors = new LinkedHashSet<TaskEditorPartDescriptor>();
        
        TaskEditorPartDescriptor attributes = new TaskEditorPartDescriptor(
                ID_PART_ATTRIBUTES) {

            @Override
            public AbstractTaskEditorPart createPart() {
                AbstractTaskEditorPart part = new JobTaskEditorPart(page);
                return part;
            }
        };
        attributes.setPath(PATH_ATTRIBUTES);
        descriptors.add(attributes);

        if (isMainPage) {
            if (needsPrivateSection()) {
                descriptors.add(new TaskEditorPartDescriptor(ID_PART_PLANNING) {
                    @Override
                    public AbstractTaskEditorPart createPart() {
                        return new TaskEditorPlanningPart();
                    }
                }.setPath(PATH_ATTRIBUTES));
            }
            
            TaskEditorPartDescriptor description = new TaskEditorPartDescriptor(
                    ID_PART_DESCRIPTION) {

                @Override
                public AbstractTaskEditorPart createPart() {
                    TaskEditorDescriptionPart part = new TaskEditorDescriptionPart() {

                        @Override
                        protected void fillToolBar(ToolBarManager toolBar) {
                            super.fillToolBar(toolBar);
                            for (IContributionItem item : toolBar.getItems()) {
                                if (item instanceof ActionContributionItem) {
                                    ActionContributionItem ai = ((ActionContributionItem) item);
                                    if (ai.getAction().getImageDescriptor() == TasksUiImages.COMMENT_REPLY_SMALL) {
                                        toolBar.remove(item);
                                    }
                                }
                            }
                        }
                    };
                    part.setExpandVertically(true);
                    return part;
                }
            };
            description.setPath(PATH_ATTRIBUTES);
            descriptors.add(description);
        }

        return descriptors;
    }

    private TaskDataModel createMainTaskDataModel(TaskEditorInput input)
            throws CoreException {
        ITaskDataWorkingCopy taskDataState = null;
        try {
            taskDataState = TasksUi.getTaskDataManager().getWorkingCopy(
                    getTask());
        } catch (OperationCanceledException e) {
            taskDataState = TasksUi.getTaskDataManager().getWorkingCopy(
                    getTask());
        }
        TaskRepository taskRepository = TasksUi.getRepositoryManager()
                .getRepository(taskDataState.getConnectorKind(),
                        taskDataState.getRepositoryUrl());
        return new TaskDataModel(taskRepository, input.getTask(), taskDataState) {

            @Override
            public void refresh(IProgressMonitor monitor) throws CoreException {
                if (allowModelRefresh) {
                    super.refresh(monitor);
                }
            }

        };
    }

    /**
     * @see org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage#createModel(org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput)
     */
    @Override
    protected TaskDataModel createModel(TaskEditorInput input)
            throws CoreException {
        TaskDataModel model = null;
        if (!isMainPage) {
            // Use main page model
            IFormPage main = getTaskEditor()
                    .findPage(JobFieldGroup.CORE_FIELDS);
            if (main instanceof P4JobEditorPage) {
                model = ((P4JobEditorPage) main).getModel();
            }
        } else {
            model = createMainTaskDataModel(input);
        }
        if (model == null) {
            model = super.createModel(input);
        }
        return model;
    }

    /**
     * This method is overriden to only allow refresh to be called on the
     * {@link TaskDataModel} instance during calls to this method.
     * 
     * This prevents the same model object being refreshed twice which will then
     * cause inconsistencies between the task data used on one page and the task
     * data used on the second page.
     * 
     * This method has to be overriden since refreshInput is private and is
     * called by this method.
     * 
     * @see org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage#refresh()
     */
    @Override
    public void refresh() {
        allowModelRefresh = true;
        try {
            super.refresh();
        } finally {
            allowModelRefresh = false;
        }

        // Refresh non-main page in case main page refresh is called directly
        // and not part of a sequential refresh of all pages
        if (isMainPage) {
            IFormPage advanced = getTaskEditor().findPage(
                    JobFieldGroup.ADVANCED_FIELDS);
            if (advanced instanceof P4JobEditorPage) {
                ((P4JobEditorPage) advanced).refresh();
            }
        }
    }

    private TaskData getModelTaskData() {
        TaskData data = null;
        TaskDataModel model = getModel();
        if (model != null) {
            data = model.getTaskData();
        }
        return data;
    }

    /**
     * @see org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage#init(org.eclipse.ui.IEditorSite,
     *      org.eclipse.ui.IEditorInput)
     */
    @Override
    public void init(IEditorSite site, IEditorInput input) {
        super.init(site, input);
        if (!isMainPage) {
            // Fix for job038085, force the UI-loading of the Other tab up front
            // instead of when selected since it needs to share a data model
            // with main page. This shouldn't be noticeable by the user since
            // the switching occurs on first-load of the editor before a layout
            // has been done. Fixes issue where super class assumes a form
            // available when the task data model changes and createPartControl
            // must be called on the page for a form to be present. This can
            // eventually be removed when:
            // https://bugs.eclipse.org/bugs/show_bug.cgi?id=306029 and
            // https://bugs.eclipse.org/bugs/show_bug.cgi?id=306113 are fixed
            getEditor().setActivePage(JobFieldGroup.ADVANCED_FIELDS);
            getEditor().setActivePage(JobFieldGroup.CORE_FIELDS);

            // Non-main page does not need task data listener since the main
            // page handles changes from that listener
            if (TASK_DATA_LISTENER != null) {
                try {
                    Object taskDataListener = TASK_DATA_LISTENER.get(this);
                    if (taskDataListener instanceof ITaskDataManagerListener) {
                        TaskDataManager manager = TasksUiPlugin
                                .getTaskDataManager();
                        if (manager != null) {
                            manager.removeListener((ITaskDataManagerListener) taskDataListener);
                        }
                    }
                } catch (SecurityException e) {
                    // Ignore
                } catch (IllegalArgumentException e) {
                    // Ignore
                } catch (IllegalAccessException e) {
                    // Ignore
                }
            }
        }
    }

}
