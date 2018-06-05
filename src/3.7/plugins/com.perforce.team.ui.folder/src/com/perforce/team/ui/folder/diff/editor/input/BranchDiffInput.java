/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.editor.input;

import com.perforce.p4java.core.IBranchMapping;
import com.perforce.p4java.core.IBranchSpecSummary;
import com.perforce.p4java.core.IMapEntry.EntryType;
import com.perforce.p4java.core.ViewMap;
import com.perforce.p4java.impl.generic.core.BranchSpecSummary;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.p4java.IP4Branch;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Branch;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.folder.IP4FolderUiConstants;
import com.perforce.team.ui.folder.PerforceUiFolderPlugin;
import com.perforce.team.ui.folder.diff.editor.Messages;
import com.perforce.team.ui.folder.diff.model.FileDiffContainer;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchDiffInput extends PlatformObject implements
        IFolderDiffInput, IPersistableElement {

    private String branch;
    private IP4Branch fetchedBranch;
    private IP4Connection connection;
    private IDiffConfiguration leftConfig;
    private IDiffConfiguration rightConfig;
    private IDiffConfiguration headerConfig;

    /**
     * 
     * @param branch
     * @param connection
     * @param header
     * @param left
     * @param right
     */
    public BranchDiffInput(String branch, IP4Connection connection,
            IDiffConfiguration header, IDiffConfiguration left,
            IDiffConfiguration right) {
        Assert.isNotNull(branch, "Branch cannot be null"); //$NON-NLS-1$
        Assert.isNotNull(branch, "Connection cannot be null"); //$NON-NLS-1$
        this.branch = branch;
        this.connection = connection;
        this.headerConfig = header;
        this.leftConfig = left;
        this.rightConfig = right;

        if (this.headerConfig == null) {
            this.headerConfig = new DiffConfiguration(MessageFormat.format(
                    Messages.BranchDiffInput_Comparing, this.branch),
                    PerforceUIPlugin
                            .getDescriptor(IPerforceUIConstants.IMG_BRANCH));
        }
        if (this.leftConfig == null) {
            this.leftConfig = new DiffConfiguration(
                    Messages.BranchDiffInput_Source);
        }
        if (this.rightConfig == null) {
            this.rightConfig = new DiffConfiguration(
                    Messages.BranchDiffInput_Target);
        }
    }

    /**
     * 
     * @param branch
     * @param connection
     */
    public BranchDiffInput(String branch, IP4Connection connection) {
        this(branch, connection, null, null, null);
    }

    /**
     * @see com.perforce.team.ui.folder.diff.editor.input.IFolderDiffInput#generateDiffs(java.lang.String,
     *      java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
     */
    public FileDiffContainer generateDiffs(String leftFilter,
            String rightFilter, IProgressMonitor monitor) {
        FileDiffContainer container = new FileDiffContainer();
        if (leftFilter != null && rightFilter != null) {
            leftFilter = IP4Connection.ROOT + leftFilter;
            rightFilter = IP4Connection.ROOT + rightFilter;
        } else if (leftFilter != null) {
            leftFilter = IP4Connection.ROOT + leftFilter;
            rightFilter = IP4Connection.ROOT;
        } else if (rightFilter != null) {
            leftFilter = IP4Connection.ROOT;
            rightFilter = IP4Connection.ROOT + rightFilter;
        }
        IP4Branch p4Branch = fetchBranch();
        ViewMap<IBranchMapping> view = p4Branch.getView();
        if (view != null) {
            for (IBranchMapping mapping : view) {
                if (EntryType.INCLUDE == mapping.getType()) {
                    container.addMapping(mapping.getLeft(), mapping.getRight());
                }
            }
        }
        container.add(p4Branch.getDiffs(leftFilter, rightFilter), connection);
        container.finish();
        return container;
    }

    private IP4Branch createBranch() {
        IBranchSpecSummary summary = new BranchSpecSummary();
        summary.setName(getName());
        return new P4Branch(connection, summary, false);
    }

    /**
     * @see org.eclipse.ui.IEditorInput#exists()
     */
    public boolean exists() {
        return true;
    }

    /**
     * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
     */
    public ImageDescriptor getImageDescriptor() {
        return PerforceUiFolderPlugin
                .getDescriptor(IP4FolderUiConstants.EDITOR);
    }

    /**
     * @see org.eclipse.ui.IEditorInput#getName()
     */
    public String getName() {
        return this.branch;
    }

    /**
     * @see org.eclipse.ui.IEditorInput#getPersistable()
     */
    public IPersistableElement getPersistable() {
        return this;
    }

    /**
     * @see org.eclipse.ui.IEditorInput#getToolTipText()
     */
    public String getToolTipText() {
        return MessageFormat.format(Messages.BranchDiffInput_Comparing,
                this.branch);
    }

    /**
     * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
     */
    @Override
    public Object getAdapter(Class adapter) {
        if (IP4Connection.class == adapter || IP4Resource.class == adapter) {
            return this.connection;
        }
        return super.getAdapter(adapter);
    }

    /**
     * @see com.perforce.team.ui.folder.diff.editor.input.IFolderDiffInput#getLeftConfiguration()
     */
    public IDiffConfiguration getLeftConfiguration() {
        return this.leftConfig;
    }

    /**
     * @see com.perforce.team.ui.folder.diff.editor.input.IFolderDiffInput#getRightConfiguration()
     */
    public IDiffConfiguration getRightConfiguration() {
        return this.rightConfig;
    }

    /**
     * @see com.perforce.team.ui.folder.diff.editor.input.IFolderDiffInput#getHeaderConfiguration()
     */
    public IDiffConfiguration getHeaderConfiguration() {
        return this.headerConfig;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof BranchDiffInput) {
			return this.branch.equals(((BranchDiffInput) obj).branch)
					&& super.equals(obj);
        }
        return false;
    }

    @Override
    public int hashCode() {
    	if(this.branch!=null)
    		return this.branch.hashCode();
    	return super.hashCode();
    }
    
    /**
     * @see org.eclipse.ui.IPersistable#saveState(org.eclipse.ui.IMemento)
     */
    public void saveState(IMemento memento) {
        memento.putString(BranchDiffInputFactory.CONNECTION, this.connection
                .getParameters().toString());
        memento.putString(BranchDiffInputFactory.BRANCH_NAME, this.branch);
        IMemento optionsMemento = memento.createChild(LEFT_OPTIONS);
        this.leftConfig.getOptions().saveState(optionsMemento);
        optionsMemento = memento.createChild(RIGHT_OPTIONS);
        this.rightConfig.getOptions().saveState(optionsMemento);
    }

    /**
     * @see org.eclipse.ui.IPersistableElement#getFactoryId()
     */
    public String getFactoryId() {
        return BranchDiffInputFactory.ID;
    }

    private IP4Branch fetchBranch() {
        if (this.fetchedBranch == null) {
            IP4Branch branch = createBranch();
            branch.markForRefresh();
            branch.refresh();
            this.fetchedBranch = branch;
        }
        return this.fetchedBranch;
    }

    /**
     * @see com.perforce.team.ui.folder.diff.editor.input.IFolderDiffInput#refreshInput(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void refreshInput(IProgressMonitor monitor) {
        monitor.subTask(Messages.BranchDiffInput_Refreshing + branch);
        IP4Branch branch = fetchBranch();

        ViewMap<IBranchMapping> view = branch.getView();
        if (view != null) {
            List<String> sources = new ArrayList<String>();
            List<String> targets = new ArrayList<String>();
            for (IBranchMapping mapping : view) {
                if (EntryType.EXCLUDE != mapping.getType()) {
                    sources.add(mapping.getLeft());
                    targets.add(mapping.getRight());
                }
            }
            this.leftConfig.setLabel(P4CoreUtils.getCommonPath(sources));
            this.rightConfig.setLabel(P4CoreUtils.getCommonPath(targets));
        }
    }

    /**
     * @see com.perforce.team.ui.folder.diff.editor.input.IFolderDiffInput#getConnection()
     */
    public IP4Connection getConnection() {
        return this.connection;
    }

}
