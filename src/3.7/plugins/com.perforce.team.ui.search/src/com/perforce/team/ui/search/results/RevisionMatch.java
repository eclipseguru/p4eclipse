/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.search.results;

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
import org.eclipse.ui.model.WorkbenchAdapter;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class RevisionMatch extends WorkbenchAdapter implements IDepotMatch,
        IAdaptable {

    private IP4File file;
    private int revision;

    /**
     * Create a revision match
     * 
     * @param file
     * @param revision
     */
    public RevisionMatch(IP4File file, int revision) {
        this.file = file;
        this.revision = revision;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof RevisionMatch) {
            RevisionMatch other = (RevisionMatch) obj;
            if (this.revision == other.revision && this.file.equals(other.file)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see com.perforce.team.ui.search.results.IDepotMatch#getDepotPath()
     */
    public String getDepotPath() {
        return this.file.getRemotePath() + "#" + this.revision; //$NON-NLS-1$
    }

    /**
     * Is this match in the current head revision?
     * 
     * @return - true if head revision, false otherwise
     */
    public boolean isHead() {
        return this.revision == this.file.getHeadRevision();
    }

    /**
     * Is this match in the current have revision?
     * 
     * @return - true if have revision, false otherwise
     */
    public boolean isHave() {
        return this.revision == this.file.getHaveRevision();
    }

    /**
     * @see org.eclipse.ui.model.WorkbenchAdapter#getLabel(java.lang.Object)
     */
    @Override
    public String getLabel(Object object) {
        return MessageFormat.format(Messages.RevisionMatch_Revision,
                Integer.toString(this.revision));
    }

    /**
     * @see org.eclipse.ui.model.WorkbenchAdapter#getImageDescriptor(java.lang.Object)
     */
    @Override
    public ImageDescriptor getImageDescriptor(Object object) {
        if (!isHave()) {
            return P4UiSearchPlugin.getDescriptor("icons/match_revision.png"); //$NON-NLS-1$
        } else {
            return P4UiSearchPlugin
                    .getDescriptor("icons/match_revision_have.png"); //$NON-NLS-1$
        }
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
        return getDepotPath();
    }

    /**
     * @see com.perforce.team.ui.search.results.IDepotMatch#getFile()
     */
    public IP4File getFile() {
        return this.file;
    }

    /**
     * @see com.perforce.team.ui.search.results.IDepotMatch#openInEditor()
     */
    public void openInEditor() {
        if (file == null) {
            return;
        }
        P4Runner.schedule(new P4Runnable() {

            @Override
            public void run(IProgressMonitor monitor) {
                DepotFileEditorInput input = new DepotFileEditorInput(file, "#" //$NON-NLS-1$
                        + revision);
                P4UIUtils.openEditor(input, null);
            }

        });
    }

    /**
     * @see com.perforce.team.ui.search.results.IDepotMatch#getRevision()
     */
    public int getRevision() {
        return this.revision;
    }

    /**
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class adapter) {
        Object adapted = null;
        if (IP4Resource.class == adapter || IP4File.class == adapter) {
            adapted = getFile();
        }
        return adapted;
    }
}
