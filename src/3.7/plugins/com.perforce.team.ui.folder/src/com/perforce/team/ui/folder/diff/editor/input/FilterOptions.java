/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.editor.input;

import org.eclipse.ui.IMemento;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class FilterOptions implements IFilterOptions {

    private boolean headFilter;
    private boolean haveFilter;
    private boolean dateFilter;
    private boolean changelistFilter;
    private boolean revisionFilter;
    private boolean labelFilter;
    private boolean clientFilter;

    private String date;
    private String changelist;
    private String revision;
    private String label;
    private String client;

    /**
     * Create empty filter options
     */
    public FilterOptions() {
        headFilter = true;
        haveFilter = false;
        dateFilter = false;
        changelistFilter = false;
        revisionFilter = false;
        labelFilter = false;
        clientFilter = false;

        date = ""; //$NON-NLS-1$
        changelist = ""; //$NON-NLS-1$
        revision = ""; //$NON-NLS-1$
        label = ""; //$NON-NLS-1$
        client = ""; //$NON-NLS-1$
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "[headFilter=" + isHeadFilter() + ", haveFilter=" //$NON-NLS-1$ //$NON-NLS-2$
                + isHaveFilter() + ", changelistFilter=" + isChangelistFilter() //$NON-NLS-1$
                + ", revisionFilter=" + isRevisionFilter() + ", dateFilter=" //$NON-NLS-1$ //$NON-NLS-2$
                + isDateFilter() + ", labelFilter=" + isLabelFilter() //$NON-NLS-1$
                + ", clientFilter=" + isClientFilter() + ", changelist=" //$NON-NLS-1$ //$NON-NLS-2$
                + getChangelist() + ", revision=" + getRevision() + ", date=" //$NON-NLS-1$ //$NON-NLS-2$
                + getDate() + ", label=" + getLabel() + ", client=" //$NON-NLS-1$ //$NON-NLS-2$
                + getClient() + "]"; //$NON-NLS-1$
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof IFilterOptions) {
            IFilterOptions other = (IFilterOptions) obj;
            if (isChangelistFilter() != other.isChangelistFilter()) {
                return false;
            }
            if (isClientFilter() != other.isClientFilter()) {
                return false;
            }
            if (isDateFilter() != other.isDateFilter()) {
                return false;
            }
            if (isHaveFilter() != other.isHaveFilter()) {
                return false;
            }
            if (isHeadFilter() != other.isHeadFilter()) {
                return false;
            }
            if (isLabelFilter() != other.isLabelFilter()) {
                return false;
            }
            if (isRevisionFilter() != other.isRevisionFilter()) {
                return false;
            }
            if (!getChangelist().equals(other.getChangelist())) {
                return false;
            }
            if (!getClient().equals(other.getClient())) {
                return false;
            }
            if (!getDate().equals(other.getDate())) {
                return false;
            }
            if (!getLabel().equals(other.getLabel())) {
                return false;
            }
            if (!getRevision().equals(other.getRevision())) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Load filter options from memento
     * 
     * @param memento
     */
    public void load(IMemento memento) {
        if (memento != null) {
            IMemento head = memento.getChild(HEAD);
            if (head != null) {
                setHeadFilter(head.getBoolean(ENABLE));
            }

            IMemento have = memento.getChild(HAVE);
            if (have != null) {
                setHaveFilter(have.getBoolean(ENABLE));
            }

            IMemento changelist = memento.getChild(CHANGELIST);
            if (changelist != null) {
                setChangelistFilter(changelist.getBoolean(ENABLE));
                setChangelist(changelist.getTextData());
            }

            IMemento date = memento.getChild(DATE);
            if (date != null) {
                setDateFilter(date.getBoolean(ENABLE));
                setDate(date.getTextData());
            }

            IMemento revision = memento.getChild(REVISION);
            if (revision != null) {
                setRevisionFilter(revision.getBoolean(ENABLE));
                setRevision(revision.getTextData());
            }

            IMemento label = memento.getChild(LABEL);
            if (label != null) {
                setLabelFilter(label.getBoolean(ENABLE));
                setLabel(label.getTextData());
            }

            IMemento client = memento.getChild(CLIENT);
            if (client != null) {
                setClientFilter(client.getBoolean(ENABLE));
                setClient(client.getTextData());
            }

        }
    }

    /**
     * @see org.eclipse.ui.IPersistable#saveState(org.eclipse.ui.IMemento)
     */
    public void saveState(IMemento memento) {

        IMemento head = memento.createChild(HEAD);
        head.putBoolean(ENABLE, isHeadFilter());

        IMemento have = memento.createChild(HAVE);
        have.putBoolean(ENABLE, isHaveFilter());

        IMemento changelist = memento.createChild(CHANGELIST);
        changelist.putBoolean(ENABLE, isChangelistFilter());
        changelist.putTextData(getChangelist());

        IMemento date = memento.createChild(DATE);
        date.putBoolean(ENABLE, isDateFilter());
        date.putTextData(getDate());

        IMemento revision = memento.createChild(REVISION);
        revision.putBoolean(ENABLE, isRevisionFilter());
        revision.putTextData(getRevision());

        IMemento label = memento.createChild(LABEL);
        label.putBoolean(ENABLE, isLabelFilter());
        label.putTextData(getLabel());

        IMemento client = memento.createChild(CLIENT);
        client.putBoolean(ENABLE, isClientFilter());
        client.putTextData(getClient());
    }

    /**
     * @see com.perforce.team.ui.folder.diff.editor.input.IFilterOptions#isHeadFilter()
     */
    public boolean isHeadFilter() {
        return this.headFilter;
    }

    /**
     * @see com.perforce.team.ui.folder.diff.editor.input.IFilterOptions#setHeadFilter(boolean)
     */
    public void setHeadFilter(boolean filter) {
        this.headFilter = filter;
    }

    /**
     * @see com.perforce.team.ui.folder.diff.editor.input.IFilterOptions#isHaveFilter()
     */
    public boolean isHaveFilter() {
        return this.haveFilter;
    }

    /**
     * 
     * @see com.perforce.team.ui.folder.diff.editor.input.IFilterOptions#setHaveFilter(boolean)
     */
    public void setHaveFilter(boolean filter) {
        this.haveFilter = filter;
    }

    /**
     * @see com.perforce.team.ui.folder.diff.editor.input.IFilterOptions#isDateFilter()
     */
    public boolean isDateFilter() {
        return this.dateFilter;
    }

    /**
     * @see com.perforce.team.ui.folder.diff.editor.input.IFilterOptions#setDateFilter(boolean)
     */
    public void setDateFilter(boolean filter) {
        this.dateFilter = filter;
    }

    /**
     * @see com.perforce.team.ui.folder.diff.editor.input.IFilterOptions#getDate()
     */
    public String getDate() {
        return this.date;
    }

    /**
     * @see com.perforce.team.ui.folder.diff.editor.input.IFilterOptions#setDate(java.lang.String)
     */
    public void setDate(String date) {
        if (date == null) {
            date = ""; //$NON-NLS-1$
        }
        this.date = date;
    }

    /**
     * @see com.perforce.team.ui.folder.diff.editor.input.IFilterOptions#isChangelistFilter()
     */
    public boolean isChangelistFilter() {
        return this.changelistFilter;
    }

    /**
     * @see com.perforce.team.ui.folder.diff.editor.input.IFilterOptions#setChangelistFilter(boolean)
     */
    public void setChangelistFilter(boolean filter) {
        this.changelistFilter = filter;
    }

    /**
     * @see com.perforce.team.ui.folder.diff.editor.input.IFilterOptions#getChangelist()
     */
    public String getChangelist() {
        return this.changelist;
    }

    /**
     * @see com.perforce.team.ui.folder.diff.editor.input.IFilterOptions#setChangelist(java.lang.String)
     */
    public void setChangelist(String changelist) {
        if (changelist == null) {
            changelist = ""; //$NON-NLS-1$
        }
        this.changelist = changelist;
    }

    /**
     * @see com.perforce.team.ui.folder.diff.editor.input.IFilterOptions#isRevisionFilter()
     */
    public boolean isRevisionFilter() {
        return this.revisionFilter;
    }

    /**
     * @see com.perforce.team.ui.folder.diff.editor.input.IFilterOptions#setRevisionFilter(boolean)
     */
    public void setRevisionFilter(boolean filter) {
        this.revisionFilter = filter;
    }

    /**
     * @see com.perforce.team.ui.folder.diff.editor.input.IFilterOptions#getRevision()
     */
    public String getRevision() {
        return this.revision;
    }

    /**
     * @see com.perforce.team.ui.folder.diff.editor.input.IFilterOptions#setRevision(java.lang.String)
     */
    public void setRevision(String revision) {
        if (revision == null) {
            revision = ""; //$NON-NLS-1$
        }
        this.revision = revision;
    }

    /**
     * @see com.perforce.team.ui.folder.diff.editor.input.IFilterOptions#isLabelFilter()
     */
    public boolean isLabelFilter() {
        return this.labelFilter;
    }

    /**
     * @see com.perforce.team.ui.folder.diff.editor.input.IFilterOptions#setLabelFilter(boolean)
     */
    public void setLabelFilter(boolean filter) {
        this.labelFilter = filter;
    }

    /**
     * @see com.perforce.team.ui.folder.diff.editor.input.IFilterOptions#getLabel()
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * @see com.perforce.team.ui.folder.diff.editor.input.IFilterOptions#setLabel(java.lang.String)
     */
    public void setLabel(String label) {
        if (label == null) {
            label = ""; //$NON-NLS-1$
        }
        this.label = label;
    }

    /**
     * @see com.perforce.team.ui.folder.diff.editor.input.IFilterOptions#isClientFilter()
     */
    public boolean isClientFilter() {
        return this.clientFilter;
    }

    /**
     * @see com.perforce.team.ui.folder.diff.editor.input.IFilterOptions#setClientFilter(boolean)
     */
    public void setClientFilter(boolean filter) {
        this.clientFilter = filter;
    }

    /**
     * @see com.perforce.team.ui.folder.diff.editor.input.IFilterOptions#getClient()
     */
    public String getClient() {
        return this.client;
    }

    /**
     * @see com.perforce.team.ui.folder.diff.editor.input.IFilterOptions#setClient(java.lang.String)
     */
    public void setClient(String client) {
        if (client == null) {
            client = ""; //$NON-NLS-1$
        }
        this.client = client;
    }

}
