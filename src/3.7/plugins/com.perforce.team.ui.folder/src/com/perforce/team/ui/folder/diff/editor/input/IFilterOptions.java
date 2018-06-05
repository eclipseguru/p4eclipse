/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.editor.input;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistable;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IFilterOptions extends IPersistable {

    /**
     * OPTIONS
     */
    String OPTIONS = "options"; //$NON-NLS-1$

    /**
     * ENABLE
     */
    String ENABLE = "enable"; //$NON-NLS-1$

    /**
     * HEAD
     */
    String HEAD = "head"; //$NON-NLS-1$

    /**
     * HAVE
     */
    String HAVE = "have"; //$NON-NLS-1$

    /**
     * CHANGELIST
     */
    String CHANGELIST = "changelist"; //$NON-NLS-1$

    /**
     * DATE
     */
    String DATE = "date"; //$NON-NLS-1$

    /**
     * REVISION
     */
    String REVISION = "revision"; //$NON-NLS-1$

    /**
     * LABEL
     */
    String LABEL = "label"; //$NON-NLS-1$

    /**
     * CLIENT
     */
    String CLIENT = "client"; //$NON-NLS-1$

    /**
     * Load options from memento
     * 
     * @param memento
     */
    void load(IMemento memento);

    /**
     * @return true if head filter, false otherwise
     */
    boolean isHeadFilter();

    /**
     * Set head filter
     * 
     * @param filter
     */
    void setHeadFilter(boolean filter);

    /**
     * @return true if have filter, false otherwise
     */
    boolean isHaveFilter();

    /**
     * Set have filter
     * 
     * @param filter
     */
    void setHaveFilter(boolean filter);

    /**
     * @return true date filter, false otherwise
     */
    boolean isDateFilter();

    /**
     * Set date filter
     * 
     * @param filter
     */
    void setDateFilter(boolean filter);

    /**
     * @return non-null date filter value
     */
    String getDate();

    /**
     * Set date filter value
     * 
     * @param date
     */
    void setDate(String date);

    /**
     * @return true if changelist filter, false otherwise
     */
    boolean isChangelistFilter();

    /**
     * Set changelist filter
     * 
     * @param filter
     */
    void setChangelistFilter(boolean filter);

    /**
     * @return non-null changelist filter value
     */
    String getChangelist();

    /**
     * Set changelist filter value
     * 
     * @param changelist
     */
    void setChangelist(String changelist);

    /**
     * @return true if revision filter, false otherwise
     */
    boolean isRevisionFilter();

    /**
     * Set revision filter
     * 
     * @param filter
     */
    void setRevisionFilter(boolean filter);

    /**
     * @return non-null revision filter value
     */
    String getRevision();

    /**
     * Set revision filter value
     * 
     * @param revision
     */
    void setRevision(String revision);

    /**
     * @return true if label filter, false otherwise
     */
    boolean isLabelFilter();

    /**
     * Set label filter
     * 
     * @param filter
     */
    void setLabelFilter(boolean filter);

    /**
     * @return non-null label filter value
     */
    String getLabel();

    /**
     * Set label filter value
     * 
     * @param label
     */
    void setLabel(String label);

    /**
     * @return true if client filter, false otherwise
     */
    boolean isClientFilter();

    /**
     * Set client filter
     * 
     * @param filter
     */
    void setClientFilter(boolean filter);

    /**
     * @return non-null client filter value
     */
    String getClient();

    /**
     * Set client filter value
     * 
     * @param client
     */
    void setClient(String client);

}