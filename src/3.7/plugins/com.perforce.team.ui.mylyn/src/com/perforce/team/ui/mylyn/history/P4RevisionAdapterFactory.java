/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.history;

import java.io.File;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.mylyn.internal.team.ui.LinkedTaskInfo;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.team.ui.AbstractTaskReference;

import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.core.p4java.P4Revision;
import com.perforce.team.ui.mylyn.P4MylynUiUtils;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class P4RevisionAdapterFactory implements IAdapterFactory {

    private static final Class[] CLASSES = new Class[] {
            AbstractTaskReference.class, TaskRepository.class };

    /**
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object,
     *      java.lang.Class)
     */
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (adaptableObject instanceof IP4Revision) {
            IP4Revision revision = (IP4Revision) adaptableObject;
            if (AbstractTaskReference.class == adapterType) {
                TaskRepository repository = P4MylynUiUtils
                        .getRepository(revision.getConnection());
                String url = repository != null
                        ? repository.getRepositoryUrl()
                        : null;
                
                String jobId=null;

                IP4Changelist list = revision.getConnection().getChangelistById(revision.getChangelist(), null,
                        true, false);
                if(list!=null){
	                String[] jobIds = list.getJobIds();
	                if(jobIds!=null && jobIds.length>0)
	                    jobId=jobIds[0];
                }
                    
                return new LinkedTaskInfo(url, jobId, null,
                        revision.getComment());
            } else if (TaskRepository.class == adapterType) {
                return P4MylynUiUtils.getRepository(revision.getConnection());
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
    
    private static String getFileName(String nameWithExtension){
    	if(nameWithExtension!=null){
    		String[] segs = nameWithExtension.split("\\.");
    		if(segs!=null && segs.length>0)
    			return segs[0];
    	}
    	return null;
    }

}
