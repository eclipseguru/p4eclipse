/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.search.results;

import com.perforce.p4java.core.IFileLineMatch;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.editor.DepotFileEditorInput;
import com.perforce.team.ui.search.P4UiSearchPlugin;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class FileMatch extends Match implements IWorkbenchAdapter, IDepotMatch,
        IAdaptable {

    private IFileLineMatch match;
    private IP4Connection connection;
    private int lineNumber;

    /**
     * @param connection
     * @param revision
     * @param match
     */
    public FileMatch(IP4Connection connection, RevisionMatch revision,
            IFileLineMatch match) {
        super(revision, 0, 0);
        this.connection = connection;
        this.match = match;
        this.lineNumber = this.match.getLineNumber();
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
     */
    public String getLabel(Object object) {
        return MessageFormat.format("{0}: {1}", this.lineNumber, //$NON-NLS-1$
                P4CoreUtils.removeWhitespace(this.match.getLine()));
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
     */
    public ImageDescriptor getImageDescriptor(Object object) {
        switch (match.getType()) {
        case BEFORE:
        case AFTER:
            return P4UiSearchPlugin.getDescriptor("icons/line_other.png"); //$NON-NLS-1$
        case MATCH:
        default:
            return P4UiSearchPlugin.getDescriptor("icons/line_match.png"); //$NON-NLS-1$
        }
    }

    /**
     * Get file line match
     * 
     * @return - file line match
     */
    public IFileLineMatch getMatch() {
        return this.match;
    }

    /**
     * Get revision match that this file match is associated with
     * 
     * @return - revision match
     */
    public RevisionMatch getRevisionMatch() {
        return (RevisionMatch) getElement();
    }

    /**
     * @see com.perforce.team.ui.search.results.IDepotMatch#getDepotPath()
     */
    public String getDepotPath() {
        return getRevisionMatch().getDepotPath();
    }

    /**
     * @see com.perforce.team.ui.search.results.IDepotMatch#openInEditor()
     */
    public void openInEditor() {
        if (connection != null) {
            P4Runner.schedule(new P4Runnable() {

                @Override
                public void run(IProgressMonitor monitor) {
                    final IP4File file = connection.getFile(match
                            .getDepotFile());
                    if (file != null) {
                        DepotFileEditorInput input = new DepotFileEditorInput(
                                file, "#" + match.getRevision()); //$NON-NLS-1$
                        LineSelectionListener listener = new LineSelectionListener(
                                lineNumber - 1);
                        P4UIUtils.openEditor(input, listener);
                    }
                }

            });
        }
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object o) {
        return new Object[0];
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
     */
    public Object getParent(Object o) {
        return null;
    }

    /**
     * @see com.perforce.team.ui.search.results.IDepotMatch#getRevision()
     */
    public int getRevision() {
        return getRevisionMatch().getRevision();
    }

    /**
     * Get line number of match
     * 
     * @return - line number
     */
    public int getLineNumber() {
        return this.lineNumber;
    }

    /**
     * @see com.perforce.team.ui.search.results.IDepotMatch#getFile()
     */
    public IP4File getFile() {
        return getRevisionMatch().getFile();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getDepotPath() + "#" + getRevision() + ":" + getLineNumber(); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof FileMatch) {
            FileMatch other = (FileMatch) obj;
            return getLineNumber() == other.getLineNumber()
                    && getRevisionMatch().equals(other.getRevisionMatch());
        }
        return false;
    }

    /**
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class adapter) {
        Object adapted = null;
        if (IP4Connection.class == adapter) {
            adapted = connection;
        } else if (IP4Resource.class == adapter || IP4File.class == adapter) {
            adapted = getFile();
        }
        return adapted;
    }

}