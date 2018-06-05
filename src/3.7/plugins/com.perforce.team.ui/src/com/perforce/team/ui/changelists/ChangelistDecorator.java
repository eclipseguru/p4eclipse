/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.changelists;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.ui.decorator.PerforceDecorator;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ChangelistDecorator extends PerforceDecorator {

    /**
     * Create changelist decorator
     * 
     * @param decorateResources
     */
    public ChangelistDecorator(boolean decorateResources) {
        super(decorateResources);
    }

    /**
     * Create changelist decorator
     */
    public ChangelistDecorator() {
        super();
    }

    /**
     * @see com.perforce.team.ui.decorator.PerforceDecorator#decorateText(java.lang.String,
     *      java.lang.Object)
     */
    @Override
    public String decorateText(String text, Object o) {
        IP4Changelist list = P4CoreUtils.convert(o, IP4Changelist.class);
        if (list instanceof IP4SubmittedChangelist
                || list instanceof IP4PendingChangelist) {
            String description = list.getShortDescription();
            if (description.length() > 0) {
                return text + " { " + description + " }"; //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        return super.decorateText(text, o);
    }
    
    public String getName() {
    	return ChangelistDecorator.class.getSimpleName()+":"+super.getName();
    }
}
