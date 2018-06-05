/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.text.timelapse;

import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.ui.text.timelapse.ITextAnnotateModel.Line;
import com.perforce.team.ui.timelapse.IAnnotateModel.Type;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class UpperRevisionRuler extends RevisionRuler {

    /**
     * @param model
     */
    public UpperRevisionRuler(ITextAnnotateModel model) {
        super(model);
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.RevisionRuler#createDisplayString(com.perforce.team.ui.text.timelapse.ITextAnnotateModel.Line,
     *      com.perforce.team.core.p4java.IP4Revision, int,
     *      com.perforce.team.ui.timelapse.IAnnotateModel.Type)
     */
    @Override
    protected String createDisplayString(Line line, IP4Revision revision,
            int lineNumber, Type type) {
        int key = model.getRevisionId(revision);
        if (latest == key || latest == line.upper) {
            return "..."; //$NON-NLS-1$
        }

        IP4Revision upperRev = model.getRevisionById(line.upper);
        upperRev = model.getNext(upperRev);
        if(upperRev==null)
        	return "";
        
        switch (type) {
        case REVISION:
            return Integer.toString(upperRev.getRevision());
        case CHANGELIST:
            return Integer.toString(upperRev.getChangelist());
        case DATE:
            return model.getDate(model.getRevisionId(upperRev));
        default:
            return Integer.toString(upperRev.getChangelist());
        }
    }
}
