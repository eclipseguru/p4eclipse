/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn;

import com.perforce.p4java.server.PerforceCharsets;
import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.mylyn.IP4MylynConstants;
import com.perforce.team.core.mylyn.P4JobConnector;
import com.perforce.team.core.mylyn.P4MylynUtils;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.P4Depot;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.mylyn.preferences.IPreferenceConstants;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.internal.tasks.core.IRepositoryConstants;
import org.eclipse.mylyn.internal.tasks.core.TaskList;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public final class P4MylynUiUtils {

    /**
     * JOB_SEGMENT
     */
    public static final String JOB_SEGMENT = "/job/"; //$NON-NLS-1$

    /**
     * Create a task repository for the specified connection
     * 
     * @param connection
     * @return - task repository
     */
    public static TaskRepository createTaskRepository(IP4Connection connection) {
        TaskRepository repository = null;
        if (connection != null) {
            repository = new TaskRepository(IP4MylynConstants.KIND, connection
                    .getParameters().getPort());
            setTaskSettings(connection, repository);
        }
        return repository;
    }

    /**
     * Set the p4-specified task repository properties for the specified
     * connection.
     * 
     * @param connection
     * @param repository
     */
    public static void setTaskSettings(IP4Connection connection,
            TaskRepository repository) {
        if (repository != null && connection != null) {
            repository.setProperty(IRepositoryConstants.PROPERTY_CATEGORY,
                    IRepositoryConstants.CATEGORY_TASKS);
            ConnectionParameters params = connection.getParameters();
            if (params.getClient() != null) {
                repository.setProperty(IP4MylynConstants.P4_CLIENT,
                        params.getClient());
            }
            if (params.getPort() != null) {
                repository.setProperty(IP4MylynConstants.P4_PORT,
                        params.getPort());
            }
            if (params.getUser() != null) {
                repository.setProperty(IP4MylynConstants.P4_USER,
                        params.getUser());
                // Store user in credentials since certain decorations are based
                // off task repository owner and that is derived from user name
                // set via credentials
                repository.setCredentials(AuthenticationType.REPOSITORY,
                        new AuthenticationCredentials(params.getUser(), ""), //$NON-NLS-1$
                        false);
            }
            if (params.getCharset() != null) {
                repository.setProperty(IP4MylynConstants.P4_CHARSET,
                        params.getCharset());
                String javaCharset = PerforceCharsets.getJavaCharsetName(params
                        .getCharset());
                if (javaCharset != null) {
                    repository.setCharacterEncoding(javaCharset);
                }
            }
        }
    }

    /**
     * Get a task repository from a p4 connection
     * 
     * @param connection
     * @return - task repository
     */
    public static TaskRepository getRepository(IP4Connection connection) {
        TaskRepository repository = null;
        if (connection != null) {
            repository = getRepository(connection.getParameters());
        }
        return repository;
    }

    /**
     * Get a task repository from a p4 connection
     * 
     * @param params
     * @return - task repository
     */
    public static TaskRepository getRepository(ConnectionParameters params) {
        return getRepository(params, true);
    }

    /**
     * Get a task repository from a p4 connection
     * 
     * @param params
     * @param checkPerforce
     * @return - task repository
     */
    public static TaskRepository getRepository(ConnectionParameters params,
            boolean checkPerforce) {
        TaskRepository repository = null;
        if (params != null) {
            String kind = P4MylynUiUtils.getConnectionSetting(
                    IPreferenceConstants.CONNECTION_LINK_KIND, params);
            String url = P4MylynUiUtils.getConnectionSetting(
                    IPreferenceConstants.CONNECTION_LINK_URL, params);
            if (kind != null && kind.length() > 0 && url != null
                    && url.length() > 0) {
                repository = TasksUi.getRepositoryManager().getRepository(kind,
                        url);
            }
            if (repository == null && checkPerforce) {
                repository = findRepository(params, IP4MylynConstants.KIND);
            }
        }
        return repository;
    }

    /**
     * Find a repository with the specified kind that has properties of a
     * connection
     * 
     * @param connection
     * @param connectorKind
     * @return - task repository
     */
    public static TaskRepository findRepository(IP4Connection connection,
            String connectorKind) {
        TaskRepository repository = null;
        if (connection != null) {
            repository = findRepository(connection.getParameters(),
                    connectorKind);
        }
        return repository;
    }

    /**
     * Find a repository with the specified kind that has properties of a
     * connection
     * 
     * @param params
     * @param connectorKind
     * @return - task repository
     */
    public static TaskRepository findRepository(ConnectionParameters params,
            String connectorKind) {
        TaskRepository repository = null;
        if (params != null && connectorKind != null) {
            for (TaskRepository taskRepo : TasksUi.getRepositoryManager()
                    .getRepositories(connectorKind)) {
                IP4Connection taskConnection = P4MylynUtils
                        .getConnection(taskRepo);
                if (taskConnection != null
                        && params.equals(taskConnection.getParameters())) {
                    repository = taskRepo;
                    break;
                }
            }
        }
        return repository;
    }

    /**
     * Get a task repository from an editor
     * 
     * @param editor
     * @return - task repository
     */
    public static TaskRepository getRepository(TaskEditor editor) {
        TaskRepository repository = null;
        if (editor != null) {
            TaskEditorInput input = editor.getTaskEditorInput();
            if (input != null) {
                repository = TasksUiUtil.getOutgoingNewTaskRepository(input
                        .getTask());
                if (repository == null) {
                    repository = input.getTaskRepository();
                }
            }
        }
        return repository;
    }

    /**
     * Get a connection from a task
     * 
     * @param task
     * @return - p4 connection
     */
    public static IP4Connection getConnection(ITask task) {
        IP4Connection connection = null;
        if (task != null) {
            connection = getConnection(task.getConnectorKind(),
                    task.getRepositoryUrl());
        }
        return connection;
    }

    /**
     * Get a connection from a task repository
     * 
     * @param repository
     * @return - p4 connection
     */
    public static IP4Connection getConnection(TaskRepository repository) {
        IP4Connection connection = null;
        if (repository != null) {
            connection = getConnection(repository.getConnectorKind(),
                    repository.getRepositoryUrl());
        }
        return connection;
    }

    /**
     * Get a task repository for a specified task
     * 
     * @param task
     * @return - repository or null if not found
     */
    public static TaskRepository getRepository(ITask task) {
        TaskRepository repository = null;
        if (task != null) {
            repository = getRepository(task.getConnectorKind(),
                    task.getRepositoryUrl());
        }
        return repository;
    }

    /**
     * Get task repository with the specified connector kind and url
     * 
     * @param repositoryKind
     * @param repositoryUrl
     * @return - task repo or null if not found
     */
    public static TaskRepository getRepository(String repositoryKind,
            String repositoryUrl) {
        TaskRepository repo = null;
        if (repositoryKind != null && repositoryUrl != null) {
            repo = TasksUi.getRepositoryManager().getRepository(repositoryKind,
                    repositoryUrl);
        }
        return repo;
    }

    /**
     * Get linked connection
     * 
     * @param repository
     * @return - connection or null if none linked to repository
     */
    public static IP4Connection getLinkedConnection(TaskRepository repository) {
        IP4Connection connection = null;
        if (repository != null) {
            TaskRepository linked = null;
            for (IP4Connection candidate : P4ConnectionManager.getManager()
                    .getConnections()) {
                linked = getRepository(candidate);
                if (repository.equals(linked)) {
                    connection = candidate;
                    break;
                }
            }
        }
        return connection;
    }

    /**
     * Get a connection from a task
     * 
     * @param repositoryKind
     * 
     * @param repositoryUrl
     * @return - p4 connection
     */
    public static IP4Connection getConnection(String repositoryKind,
            String repositoryUrl) {
        IP4Connection connection = null;
        if (repositoryKind != null && repositoryUrl != null) {
            TaskRepository repository = TasksUi.getRepositoryManager()
                    .getRepository(repositoryKind, repositoryUrl);
            if (repository != null) {
                connection = getLinkedConnection(repository);
                if (connection == null) {
                    connection = P4MylynUtils.getConnection(repository);
                }
            }
        }
        return connection;
    }

    /**
     * Get task from a repo url and id. The id specified should be the actual
     * job id, not the encoded task id handle.
     * 
     * @param repoUrl
     * @param id
     * @return - task
     */
    public static ITask getTask(String repoUrl, String id) {
        return getTaskList().getTaskByKey(repoUrl, id);
    }

    /**
     * Get task list
     * 
     * @return - task list
     */
    public static TaskList getTaskList() {
        return TasksUiPlugin.getTaskList();
    }

    /**
     * Set a connection setting
     * 
     * @param baseKey
     * @param value
     * @param params
     * @return - true if connection setting was set
     */
    public static boolean setConnectionSetting(String baseKey, String value,
            ConnectionParameters params) {
        boolean set = false;
        if (baseKey != null && value != null && params != null) {
            if (!baseKey.endsWith(".")) { //$NON-NLS-1$
                baseKey += "."; //$NON-NLS-1$
            }
            String port = params.getPort();
            if (port != null) {
                PerforceUiMylynPlugin.getDefault().getPreferenceStore()
                        .setValue(baseKey + port, value);
                set = true;
            }
        }

        return set;
    }

    /**
     * Set a connection setting
     * 
     * @param baseKey
     * @param value
     * @param connection
     * @return - true if connection setting was set
     */
    public static boolean setConnectionSetting(String baseKey, String value,
            IP4Connection connection) {
        boolean set = false;
        if (connection != null) {
            set = setConnectionSetting(baseKey, value,
                    connection.getParameters());
        }
        return set;
    }

    /**
     * Get a connection setting
     * 
     * @param baseKey
     * @param params
     * @return - connection setting
     */
    public static String getConnectionSetting(String baseKey,
            ConnectionParameters params) {
        String value = null;
        if (baseKey != null && params != null) {
            if (!baseKey.endsWith(".")) { //$NON-NLS-1$
                baseKey += "."; //$NON-NLS-1$
            }
            String port = params.getPort();
            if (port != null) {
                value = PerforceUiMylynPlugin.getDefault().getPreferenceStore()
                        .getString(baseKey + port);
            }
        }
        if (value == null) {
            value = ""; //$NON-NLS-1$
        }
        return value;
    }

    /**
     * Get a connection setting
     * 
     * @param baseKey
     * @param connection
     * @return - connection setting
     */
    public static String getConnectionSetting(String baseKey,
            IP4Connection connection) {
        String value = null;
        if (connection != null) {
            value = getConnectionSetting(baseKey, connection.getParameters());
        }
        if (value == null) {
            value = ""; //$NON-NLS-1$
        }
        return value;
    }

    /**
     * Get non-Perforce task repositories. Checks connector kind against
     * {@link IP4MylynConstants#KIND}.
     * 
     * @return - non-null but possibly empty array of task repositories
     */
    public static TaskRepository[] getNonPerforceRepositories() {
        List<TaskRepository> repos = new ArrayList<TaskRepository>();
        for (TaskRepository repo : TasksUi.getRepositoryManager()
                .getAllRepositories()) {
            if (!IP4MylynConstants.KIND.equals(repo.getConnectorKind())) {
                repos.add(repo);
            }
        }
        return repos.toArray(new TaskRepository[repos.size()]);
    }

    /**
     * Get perforce connector
     * 
     * @return - perforce repository connector
     */
    public static P4JobConnector getPerforceConnector() {
        return (P4JobConnector) TasksUi
                .getRepositoryConnector(IP4MylynConstants.KIND);
    }

    /**
     * Get perforce connector
     * 
     * @return - perforce repository connector
     */
    public static P4JobConnectorUi getPerforceConnectorUi() {
        return (P4JobConnectorUi) TasksUi
                .getRepositoryConnectorUi(IP4MylynConstants.KIND);
    }

    public static IP4File getJobSpecFile(ITask task) {
        IP4File file = null;
        IP4Connection connection = P4MylynUiUtils.getConnection(task);
        if (connection != null && !connection.isOffline()) {
            if (connection.needsRefresh()) {
                connection.refresh();
            }
            P4Depot spec = connection.getSpecDepot();
            if (spec != null) {
                String jobPath = getJobSpecPath(spec, task);
                IP4File jobFile = connection.getFile(jobPath);
                if (jobFile != null && jobFile.isRemote()) {
                    file = jobFile;
                }
            }
        }
        return file;
    }

    public static String getJobSpecPath(P4Depot specDepot, ITask task) {
        String jobSpecPath = null;
        String remote = specDepot.getRemotePath();
        if (remote != null) {
            StringBuilder specPath = new StringBuilder(remote);
            specPath.append(JOB_SEGMENT);
            if (task != null) {
                String id = task.getTaskKey();
                if (id != null) {
                    specPath.append(id);
                    String suffix = specDepot.getSuffix();
                    if (suffix != null) {
                        specPath.append(suffix);
                    }
                    jobSpecPath = specPath.toString();
                }
            }
        }
        return jobSpecPath;
    }

}
