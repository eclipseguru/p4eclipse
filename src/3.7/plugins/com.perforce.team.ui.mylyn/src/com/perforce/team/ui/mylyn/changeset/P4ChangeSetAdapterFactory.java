/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.changeset;

import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.synchronize.IP4ChangeSet;
import com.perforce.team.ui.mylyn.P4MylynUiUtils;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.mylyn.internal.team.ui.LinkedTaskInfo;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.team.ui.AbstractTaskReference;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class P4ChangeSetAdapterFactory implements IAdapterFactory {

    private static final Class[] CLASSES = new Class[] { AbstractTaskReference.class };

    /**
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object,
     *      java.lang.Class)
     */
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (AbstractTaskReference.class == adapterType) {
            if (adaptableObject instanceof P4ContextChangeSet) {
                P4ContextChangeSet set = (P4ContextChangeSet) adaptableObject;
                ITask task = set.getTask();
                if (task != null) {
                    return new LinkedTaskInfo(task, set);
                }
            }
            if (adaptableObject instanceof IP4ChangeSet) {
                IP4ChangeSet revision = (IP4ChangeSet) adaptableObject;
                TaskRepository repository = P4MylynUiUtils
                        .getRepository(revision.getConnection());
                String url = repository != null
                        ? repository.getRepositoryUrl()
                        : null;
                        
                String jobId=null;
                IP4Changelist list = revision.getChangelist();
                String[] jobIds = list.getJobIds();
                if(jobIds!=null && jobIds.length>0)
                    jobId=jobIds[0];
                        
                return new LinkedTaskInfo(url, jobId, null,
                        revision.getComment());
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
