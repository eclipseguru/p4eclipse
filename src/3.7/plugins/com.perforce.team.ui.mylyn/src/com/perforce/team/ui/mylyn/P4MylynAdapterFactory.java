/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.mylyn.internal.tasks.core.TaskTask;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput;
import org.eclipse.team.ui.history.IHistoryPageSource;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.ui.history.P4HistoryPage;
import com.perforce.team.ui.history.P4HistoryPageSource;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class P4MylynAdapterFactory implements IAdapterFactory {

    private static final Class[] CLASSES = new Class[] { TaskRepository.class,
            IP4Connection.class, IHistoryPageSource.class };

    /**
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object,
     *      java.lang.Class)
     */
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (TaskRepository.class == adapterType) {
            if (adaptableObject instanceof P4HistoryPage) {
                P4HistoryPage page = (P4HistoryPage) adaptableObject;
                IP4File file = page.getFileInput();
                if (file != null) {
                    return P4MylynUiUtils.getRepository(file.getConnection());
                }
            } else if (adaptableObject instanceof IP4Resource) {
                return P4MylynUiUtils
                        .getRepository(((IP4Resource) adaptableObject)
                                .getConnection());
            }
        } else if (IP4Connection.class == adapterType) {
            if (adaptableObject instanceof TaskEditorInput) {
                return P4MylynUiUtils
                        .getConnection(((TaskEditorInput) adaptableObject)
                                .getTask());
            }
        } else if (adapterType.isAssignableFrom(IHistoryPageSource.class)) {
            if (adaptableObject instanceof P4TaskEditorInput) {
                ITask task = ((TaskEditorInput) adaptableObject).getTask();
                IP4File file = P4MylynUiUtils.getJobSpecFile(task);
                if (file != null)
                    return new P4HistoryPageSource(file);
            } else if (adaptableObject instanceof TaskTask) {
                IP4File file = P4MylynUiUtils
                        .getJobSpecFile((ITask) adaptableObject);
                if (file != null)
                    return new P4HistoryPageSource(file);
            }
        }
        return null;
    }

    /**
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
     */
    public Class[] getAdapterList() {
        return CLASSES;
    }

}
