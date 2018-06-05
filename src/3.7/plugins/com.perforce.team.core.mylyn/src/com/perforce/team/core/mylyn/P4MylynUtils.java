/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mylyn;

import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.P4Workspace;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class P4MylynUtils {

    private static final DateFormat P4_FORMATTER = new SimpleDateFormat(
            "yyyy/MM/dd HH:mm:ss"); //$NON-NLS-1$

    /**
     * Format copied from Mylyn editor util class which is not visible
     */
    private static final DateFormat MYLYN_FORMATTER = DateFormat
            .getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);

    private static final SimpleDateFormat COMMENT_FORMATTER1 = new SimpleDateFormat(
            "MM/dd/yyyy HH:mm:ss"); //$NON-NLS-1$

    /**
     * VALUE_REGEX
     */
    public static final String VALUE_REGEX = "([=<>&\\^\\|\\)\\(\\*])"; //$NON-NLS-1$

    /**
     * VALUE_REPLACE_REGEX
     */
    public static final String VALUE_REPLACE_REGEX = "\\\\$1"; //$NON-NLS-1$

    /**
     * Escape any operators found in job query value
     * 
     * @param value
     * @return - escaped job query value
     */
    public static String escapeJobQueryValue(String value) {
        return value != null ? value.replaceAll(VALUE_REGEX,
                VALUE_REPLACE_REGEX) : null;
    }

    /**
     * Get a task from an object
     * 
     * @param object
     * @return - task or null if can't be converted
     */
    public static ITask getTask(Object object) {
        return P4CoreUtils.convert(object, ITask.class);
    }

    /**
     * Parse a string date into a java Date object
     * 
     * @param date
     * @return - java date object or null if specified string is null or parsing
     *         fails
     */
    public static synchronized Date parseCommentDate(String date) {
        Date parsed = null;
        if (date != null) {
            try {
                parsed = COMMENT_FORMATTER1.parse(date);
            } catch (ParseException e) {
                parsed = null;
            }
        }
        return parsed;
    }

    /**
     * Parse a string date into a java Date object
     * 
     * @param date
     * @return - java date object or null if specified string is null or parsing
     *         fails
     */
    public static synchronized Date parseDate(String date) {
        Date parsed = null;
        if (date != null) {
            try {
                parsed = P4_FORMATTER.parse(date);
            } catch (ParseException e) {
                parsed = null;
            }
        }
        return parsed;
    }

    /**
     * Format a java Date object into a string
     * 
     * @param date
     * @return - Perforce date formatted string
     */
    public static synchronized String formatToP4Date(Date date) {
        String formatted = null;
        if (date != null) {
            formatted = P4_FORMATTER.format(date);
        }
        return formatted;
    }

    /**
     * Format a java Date object into a string
     * 
     * @param date
     * @return - Perforce date formatted string
     */
    public static synchronized String formatToMylynDate(Date date) {
        String formatted = null;
        if (date != null) {
            formatted = MYLYN_FORMATTER.format(date);
        }
        return formatted;
    }

    /**
     * Format a java Date object into a string
     * 
     * @param date
     * @return - Perforce date formatted string
     */
    public static synchronized String formatCommentDate(Date date) {
        String formatted = null;
        if (date != null) {
            formatted = COMMENT_FORMATTER1.format(date);
        }
        return formatted;
    }

    /**
     * Get a connection from a repository
     * 
     * @param repository
     * @return - p4 connection
     */
    public static IP4Connection getConnection(TaskRepository repository) {
        IP4Connection connection = null;
        if (repository != null) {
            String port = repository.getProperty(IP4MylynConstants.P4_PORT);
            if (port != null) {
                port = port.trim();
            }
            String user = repository.getProperty(IP4MylynConstants.P4_USER);
            if (user != null) {
                user = user.trim();
            }
            String client = repository.getProperty(IP4MylynConstants.P4_CLIENT);
            if (client != null) {
                client = client.trim();
            }
            if (port != null && port.length() > 0 && user != null
                    && user.length() > 0 && client != null
                    && client.length() > 0) {
                ConnectionParameters params = new ConnectionParameters();
                params.setPort(port);
                params.setUser(user);
                params.setClient(client);
                params.setCharset(repository
                        .getProperty(IP4MylynConstants.P4_CHARSET));
                connection = P4Workspace.getWorkspace().getConnection(params);
            }
        }
        return connection;
    }

    /**
     * Get status object of connection not being retrievable from a task
     * repository
     * 
     * @return - error status
     */
    public static IStatus getConnectionErrorStatus() {
        return getErrorStatus(Messages.P4MylynUtils_ConnectionRetrievalFailed);
    }

    /**
     * Get status object of connection being offline
     * 
     * @return - error status
     */
    public static IStatus getConnectionOfflineStatus() {
        return getErrorStatus(Messages.P4MylynUtils_ConnectionOffline);
    }

    /**
     * Get error status with specified message and throwable
     * 
     * @param message
     * @param throwable
     * @return - error status
     */
    public static IStatus getErrorStatus(String message, Throwable throwable) {
        return new Status(IStatus.ERROR, PerforceCoreMylynPlugin.PLUGIN_ID,
                IStatus.OK, message, throwable);
    }

    /**
     * Get error status with specified message
     * 
     * @param message
     * @return - error status
     */
    public static IStatus getErrorStatus(String message) {
        return getErrorStatus(message, null);
    }

}
