/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.patch.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ErrorCollector implements IErrorCollector {

    /**
     * Get a non-null collector
     * 
     * @param collector
     * @return non-null error collector
     */
    public static IErrorCollector collectorFor(IErrorCollector collector) {
        if (collector == null) {
            collector = new ErrorCollector();
        }
        return collector;
    }

    private List<Throwable> errors = new ArrayList<Throwable>();

    /**
     * @see com.perforce.team.ui.patch.model.IErrorCollector#collect(java.lang.Throwable)
     */
    public void collect(Throwable throwable) {
        if (throwable != null) {
            errors.add(throwable);
        }
    }

    /**
     * @see com.perforce.team.ui.patch.model.IErrorCollector#getErrorCount()
     */
    public int getErrorCount() {
        return errors.size();
    }

    /**
     * @see com.perforce.team.ui.patch.model.IErrorCollector#getErrors()
     */
    public Throwable[] getErrors() {
        return errors.toArray(new Throwable[errors.size()]);
    }

    /**
     * @see com.perforce.team.ui.patch.model.IErrorCollector#done()
     */
    public void done() {
        // Sub-classes should override
    }

}
