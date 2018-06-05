/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.editor.input;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Resource.Type;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.folder.IP4FolderUiConstants;
import com.perforce.team.ui.folder.PerforceUiFolderPlugin;
import com.perforce.team.ui.folder.diff.editor.Messages;
import com.perforce.team.ui.folder.diff.model.FileDiffContainer;

import java.text.MessageFormat;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class FolderDiffInput extends PlatformObject implements
        IFolderDiffInput, IPersistableElement {

    private static class ComparePair implements Comparable<ComparePair> {

        String left;
        String right;

        /**
         * Create compare pair
         * 
         * @param left
         * @param right
         */
        public ComparePair(String left, String right) {
            Assert.isNotNull(left, "Left cannot be null"); //$NON-NLS-1$
            Assert.isNotNull(right, "Right cannot be null"); //$NON-NLS-1$
            this.left = left;
            this.right = right;
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return toString().hashCode();
        }

        /**
         * 
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other instanceof ComparePair) {
                ComparePair pair2 = (ComparePair) other;
                return this.left.equals(pair2.left)
                        && right.equals(pair2.right);
            }
            return false;
        }

        @Override
        public String toString() {
            return this.left + " " + this.right; //$NON-NLS-1$
        }

        /**
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(ComparePair o) {
            return toString().compareToIgnoreCase(o.toString());
        }
    }

    /**
     * Connection
     */
    protected IP4Connection connection;
    private Set<ComparePair> paths = new TreeSet<FolderDiffInput.ComparePair>();
    private String lastLeftFilter = ""; //$NON-NLS-1$
    private String lastRightFilter = ""; //$NON-NLS-1$

    private Set<String> depotPaths = new TreeSet<String>();

    private IDiffConfiguration leftConfig;
    private IDiffConfiguration rightConfig;
    private IDiffConfiguration headerConfig;

    /**
     * @param connection
     */
    public FolderDiffInput(IP4Connection connection) {
        this(connection, null, null, new DiffConfiguration(
                Messages.FolderDiffInput_HeaderTitle,
                PerforceUIPlugin
                        .getDescriptor(IPerforceUIConstants.IMG_DEPOT_FOLDER)));
    }

    /**
     * @param connection
     * @param left
     * @param right
     * @param header
     */
    public FolderDiffInput(IP4Connection connection, IDiffConfiguration left,
            IDiffConfiguration right, IDiffConfiguration header) {
        this.connection = connection;

        if (left == null) {
            left = new DiffConfiguration();
        }
        if (right == null) {
            right = new DiffConfiguration();
        }
        if (header == null) {
            header = new DiffConfiguration();
        }
        this.leftConfig = left;
        this.rightConfig = right;
        this.headerConfig = header;
    }

    /**
     * Add paths
     * 
     * @param left
     * @param right
     */
    public void addPaths(String left, String right) {
        if (left != null && right != null) {
            this.paths.add(new ComparePair(left, right));
            if (this.paths.size() == 1) {
                if (getLeftConfiguration().getLabel(getLeftConfiguration())
                        .length() == 0) {
                    getLeftConfiguration().setLabel(left);
                }
                if (getRightConfiguration().getLabel(getRightConfiguration())
                        .length() == 0) {
                    getRightConfiguration().setLabel(right);
                }
            }
        }
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
        return Messages.FolderDiffInput_Name;
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
        return MessageFormat.format(Messages.FolderDiffInput_Tooltip,
                this.leftConfig.getLabel(this.leftConfig), this.lastLeftFilter,
                this.rightConfig.getLabel(this.rightConfig),
                this.lastRightFilter);
    }

    /**
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @Override
    public Object getAdapter(Class adapter) {
        if (IP4Connection.class == adapter || IP4Resource.class == adapter) {
            return this.connection;
        }
        return super.getAdapter(adapter);
    }

    /**
     * Add paths
     * 
     * @param leftFilter
     * @param rightFilter
     * @param container
     */
    protected void addPaths(String leftFilter, String rightFilter,
            FileDiffContainer container) {
        for (ComparePair pair : this.paths) {
            container.addMapping(pair.left, pair.right);
            container.add(
                    connection.getDiffs(pair.left + leftFilter, pair.right
                            + rightFilter), connection);
        }
    }

    /**
     * @see com.perforce.team.ui.folder.diff.editor.input.IFolderDiffInput#generateDiffs(java.lang.String,
     *      java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
     */
    public FileDiffContainer generateDiffs(String leftFilter,
            String rightFilter, IProgressMonitor monitor) {
        FileDiffContainer container = new FileDiffContainer();
        if (leftFilter == null) {
            leftFilter = ""; //$NON-NLS-1$
        }
        if (rightFilter == null) {
            rightFilter = ""; //$NON-NLS-1$
        }
        this.lastLeftFilter = leftFilter;
        this.lastRightFilter = rightFilter;
        for (String depot : depotPaths) {
            container.addMapping(depot, depot);
        }
        addPaths(leftFilter, rightFilter, container);
        container.finish();
        return container;
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
        } else if (obj instanceof FolderDiffInput) {
            FolderDiffInput other = (FolderDiffInput) obj;
            return this.lastLeftFilter.equals(other.lastLeftFilter)
                    && this.lastRightFilter.equals(other.lastRightFilter)
                    && this.paths.equals(other.paths);
        }
        return super.equals(obj);
    }
    
    @Override
    public int hashCode() {
    	int hash = 0;
    	if(this.lastLeftFilter!=null)
    		hash+=this.lastLeftFilter.hashCode();
    	if(this.lastRightFilter!=null)
    		hash+=this.lastRightFilter.hashCode()*31;
    	if(this.paths!=null)
    		hash+=this.paths.hashCode()*11;
    	
    	if(hash>0)
    		return hash;    	
    	return super.hashCode();
    }

    /**
     * Save paths
     * 
     * @param memento
     */
    protected void savePaths(IMemento memento) {
        for (ComparePair pair : this.paths) {
            IMemento pairMemento = memento
                    .createChild(FolderDiffInputFactory.PAIR);
            pairMemento.putString(FolderDiffInputFactory.LEFT_PATH, pair.left);
            pairMemento
                    .putString(FolderDiffInputFactory.RIGHT_PATH, pair.right);
        }
    }

    /**
     * @see org.eclipse.ui.IPersistable#saveState(org.eclipse.ui.IMemento)
     */
    public void saveState(IMemento memento) {
        memento.putString(FolderDiffInputFactory.CONNECTION, this.connection
                .getParameters().toString());
        savePaths(memento);
        IMemento optionsMemento = memento.createChild(LEFT_OPTIONS);
        this.leftConfig.getOptions().saveState(optionsMemento);
        optionsMemento = memento.createChild(RIGHT_OPTIONS);
        this.rightConfig.getOptions().saveState(optionsMemento);
    }

    /**
     * @see org.eclipse.ui.IPersistableElement#getFactoryId()
     */
    public String getFactoryId() {
        return FolderDiffInputFactory.ID;
    }

    /**
     * @see com.perforce.team.ui.folder.diff.editor.input.IFolderDiffInput#refreshInput(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void refreshInput(IProgressMonitor monitor) {
        for (IP4Resource depot : connection.members()) {
            depotPaths.add(depot.getActionPath(Type.REMOTE));
        }
    }

    /**
     * @see com.perforce.team.ui.folder.diff.editor.input.IFolderDiffInput#getConnection()
     */
    public IP4Connection getConnection() {
        return this.connection;
    }

}
