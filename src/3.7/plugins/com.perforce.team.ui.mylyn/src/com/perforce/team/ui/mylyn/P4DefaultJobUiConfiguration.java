/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.team.ui.FocusedTeamUiPlugin;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskComment;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.LegendElement;
import org.eclipse.mylyn.tasks.ui.TaskHyperlink;
import org.eclipse.ui.PlatformUI;

import com.perforce.team.core.IConstants;
import com.perforce.team.core.mylyn.P4DefaultJobConfiguration;
import com.perforce.team.core.mylyn.P4MylynUtils;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.ui.mylyn.IPendingChangelistLocator.ITaskLocatorToken;
import com.perforce.team.ui.mylyn.preferences.IPreferenceConstants;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4DefaultJobUiConfiguration implements IP4JobUiConfiguration,
        IP4JobUiConfiguration2 {

    /**
     * COMMENT_PATTERN
     */
    public static final String COMMENT_PATTERN = Messages.P4DefaultJobUiConfiguration_WorkingChangelistDescription;

    private IPendingChangelistLocator locator = new P4DefaultPendingChangelistLocator();

    /**
     * @see com.perforce.team.ui.mylyn.IP4JobUiConfiguration#findHyperlinks(org.eclipse.mylyn.tasks.core.TaskRepository,
     *      java.lang.String, int, int)
     */
    public IHyperlink[] findHyperlinks(TaskRepository repository, String text,
            int index, int textOffset) {
        Matcher matcher = P4DefaultJobConfiguration.JOB_PATTERN.matcher(text);
        List<IHyperlink> links = new ArrayList<IHyperlink>();
        while (matcher.find()) {
            if (index == -1
                    || (matcher.start() < index && matcher.end() > index)) {
                String jobName = matcher.group();
                IRegion region = new Region(textOffset + matcher.start(),
                        jobName.length());
                links.add(new P4TaskHyperlink(region, repository, jobName));
            }
        }
        return links.isEmpty() ? null : links.toArray(new IHyperlink[links
                .size()]);
    }

    /**
     * @see com.perforce.team.ui.mylyn.IP4JobUiConfiguration#getTaskKindOverlay(org.eclipse.mylyn.tasks.core.ITask)
     */
    public ImageDescriptor getTaskKindOverlay(ITask task) {
        return null;
    }

    /**
     * @see com.perforce.team.ui.mylyn.IP4JobUiConfiguration#isConfigurationFor(org.eclipse.mylyn.tasks.core.TaskRepository)
     */
    public boolean isConfigurationFor(TaskRepository repository) {
        return true;
    }

    /**
     * @see com.perforce.team.ui.mylyn.IP4JobUiConfiguration#getReplyText(org.eclipse.mylyn.tasks.core.TaskRepository,
     *      org.eclipse.mylyn.tasks.core.ITask,
     *      org.eclipse.mylyn.tasks.core.ITaskComment, boolean)
     */
    public String getReplyText(TaskRepository taskRepository, ITask task,
            ITaskComment taskComment, boolean includeTask) {
        if (taskComment != null) {
            return MessageFormat.format(
                    Messages.P4DefaultJobUiConfiguration_CommentReplyText,
                    taskComment.getNumber());
        } else {
            return null;
        }
    }

    /**
     * @see com.perforce.team.ui.mylyn.IP4JobUiConfiguration#getTaskKindLabel(org.eclipse.mylyn.tasks.core.ITask)
     */
    public String getTaskKindLabel(ITask task) {
        return "Job"; //$NON-NLS-1$
    }

    /**
     * @see com.perforce.team.ui.mylyn.IP4JobUiConfiguration#getTaskPriorityOverlay(org.eclipse.mylyn.tasks.core.ITask)
     */
    public ImageDescriptor getTaskPriorityOverlay(ITask task) {
        return null;
    }

    /**
     * @see com.perforce.team.ui.mylyn.IP4JobUiConfiguration#getLegendElements()
     */
    public Collection<LegendElement> getLegendElements() {
        return Collections.emptyList();
    }

    /**
     * @see com.perforce.team.ui.mylyn.IP4JobUiConfiguration#getContextChangelistDescription(org.eclipse.mylyn.tasks.core.TaskRepository,
     *      org.eclipse.mylyn.tasks.core.ITask)
     */
    public String getContextChangelistDescription(
            TaskRepository taskRepository, ITask task) {
    	/*
    	 * job059713: use the Mylyn commit comment template for the changelist description
    	 */
    	ITaskLocatorToken token = this.locator.generateToken(task);
    	return getCommitComment(task, token);
    }

    /**
     * @see com.perforce.team.ui.mylyn.IP4JobUiConfiguration2#getPendingChangelistLocator()
     */
    public IPendingChangelistLocator getPendingChangelistLocator() {
        return this.locator;
    }

	public static String getCommitComment(ITask task, ITaskLocatorToken token) {
	    boolean useMylyn=PerforceUiMylynPlugin.getDefault().getPreferenceStore()
	    		.getBoolean(IPreferenceConstants.USE_MYLYN_TEAM_COMMENT);

	    if(useMylyn){
			String template = FocusedTeamUiPlugin.getDefault().getPreferenceStore()
					.getString(FocusedTeamUiPlugin.COMMIT_TEMPLATE);
			if(template!=null)
				return FocusedTeamUiPlugin.getDefault().getCommitTemplateManager()
					.generateComment(task, template);
			return IConstants.EMPTY_STRING;
	    }else
	    	return token != null ? token.getComment() : IConstants.EMPTY_STRING; 
	}

	private static class P4TaskHyperlink implements IHyperlink{

    	TaskHyperlink link;
    	
    	public P4TaskHyperlink(IRegion region, TaskRepository repository, String taskId) {
    		link=new TaskHyperlink(region, repository, taskId);
    	}
    	
		public IRegion getHyperlinkRegion() {
			return link.getHyperlinkRegion();
		}

		public String getTypeLabel() {
			return link.getTypeLabel();
		}

		public String getHyperlinkText() {
			return link.getHyperlinkText();
		}

		public void open() {
			
			Job job = new Job(MessageFormat.format(Messages.P4DefaultJobUiConfiguration_OpenJob,link.getTaskId())){
				boolean jobExist=false;

				protected IStatus run(IProgressMonitor monitor) {
					monitor.beginTask(IConstants.EMPTY_STRING, 100); //$NON-NLS-1$
					monitor.worked(20);
					
					TaskRepository repository = link.getRepository();
					String taskId = link.getTaskId();
					if (repository != null) {
						ITask task = (ITask) TasksUiPlugin.getTaskList().getTask(repository.getRepositoryUrl(), taskId);
						if (task == null) {
							task = TasksUiPlugin.getTaskList().getTaskByKey(repository.getRepositoryUrl(), taskId);
						}
						if (task == null) {
							// search for task
							IP4Connection connection = P4MylynUtils.getConnection(repository);
							
							final IP4Job[] existJobs = connection.getJobs(null,	1, "Job="+taskId); //$NON-NLS-1$
							
							monitor.worked(50);
							
							if(existJobs.length==1){
								jobExist=true;
							}
						}else
							jobExist=true;
					}
					
					PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
						public void run() {
							if(jobExist)
								link.open();
							else
								MessageDialog.openError(null, Messages.P4DefaultJobUiConfiguration_Error, 
										MessageFormat.format(Messages.P4DefaultJobUiConfiguration_TaskDoesnotExist,link.getTaskId()));
						}});
					
					monitor.done();
					return Status.OK_STATUS;
				}
				
			};
			job.schedule();
			
		}
    	
    }
}
