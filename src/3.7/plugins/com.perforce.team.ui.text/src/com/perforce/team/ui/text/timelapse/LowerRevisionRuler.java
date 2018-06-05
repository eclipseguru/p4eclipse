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
public class LowerRevisionRuler extends RevisionRuler {

    /**
     * @param model
     */
    public LowerRevisionRuler(ITextAnnotateModel model) {
        super(model);
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.RevisionRuler#createDisplayString(Line,
     *      IP4Revision, int, Type)
     */
    @Override
    protected String createDisplayString(Line line, IP4Revision revision,
            int lineNumber, Type type) {
        IP4Revision lowerRev = model.getRevisionById(line.lower);
        switch (type) {
        case REVISION:
            return Integer.toString(lowerRev.getRevision());
        case CHANGELIST:
            return Integer.toString(lowerRev.getChangelist());
        case DATE:
            return model.getDate(model.getRevisionId(lowerRev));
        default:
            return Integer.toString(lowerRev.getChangelist());
        }
    }

}
