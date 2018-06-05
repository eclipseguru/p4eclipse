/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.actions;

import com.perforce.team.core.mylyn.P4MylynUtils;
import com.perforce.team.ui.p4java.actions.P4Action;

import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class OpenTaskAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        if (this.getSelection() != null) {
            for (Object object : this.getSelection().toArray()) {
                ITask task = P4MylynUtils.getTask(object);
                if (task!=null) {
                    TasksUiUtil.openTask(task);
                }
            }
        }
    }

}
