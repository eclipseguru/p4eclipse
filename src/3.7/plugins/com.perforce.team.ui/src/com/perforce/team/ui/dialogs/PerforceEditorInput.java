package com.perforce.team.ui.dialogs;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.internal.ComparePreferencePage;
import org.eclipse.compare.internal.CompareUIPlugin;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

import com.perforce.team.core.PerforceProviderPlugin;

public class PerforceEditorInput extends CompareEditorInput {

    private String leftFile;
    private String rightFile;
    private String ancestorFile;
    private String type;
    private boolean merge;
    private Object leftNode;

    public static class DiffFile implements IStructureComparator,
            IStreamContentAccessor, ITypedElement {

        File depot;
        String type;

        public DiffFile(File depot, String type) {
            this.depot = depot;
            this.type = type;
        }

        public Object[] getChildren() {
            return new Object[0];
        }

        public String getName() {
            return depot.getName();
        }

        public Image getImage() {
            return PlatformUI.getWorkbench().getEditorRegistry()
                    .getImageDescriptor("xxx." + type).createImage(); //$NON-NLS-1$
        }

        public String getType() {
            return type;
        }

        public InputStream getContents() {
            try {
                return new FileInputStream(depot);
            } catch (FileNotFoundException e) {
                return null;
            }
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof ITypedElement) {
                String otherName = ((ITypedElement) other).getName();
                return getName().equals(otherName);
            }
            return false;
        }
        
        @Override
        public int hashCode() {
        	// to prevent coverity complain HE_EQUALS_NO_HASHCODE
        	int hash=0;
        	if(this.depot!=null)
        		hash+=this.depot.hashCode();
        	if(this.type!=null)
        		hash+=this.type.hashCode();
        	if(hash>0)
        		return hash;
        	return super.hashCode();
        }

    }

    private static class PerforceCompareConfiguration extends
            CompareConfiguration {

        public PerforceCompareConfiguration(boolean merge, String path,
                String leftLabel, String rightLabel, String ancestorLabel) {
            setLeftLabel(leftLabel);
            setRightLabel(rightLabel);
            setAncestorLabel(ancestorLabel);
            ImageDescriptor desc = PlatformUI.getWorkbench()
                    .getEditorRegistry().getImageDescriptor(path);
            setRightImage(desc.createImage());
            setLeftImage(desc.createImage());
            setAncestorImage(desc.createImage());
            if (merge) {
                setLeftEditable(true);
                setRightEditable(false);
                // The following changed in Eclipse 3.2 -> 3.3 with the addition
                // of the ICompareUIConstants interface
                // Set both the old and new show ancestor pane properties
                setProperty(
                        CompareUIPlugin.PLUGIN_ID + ".AncestorVisible", true); //$NON-NLS-1$
                setProperty(ComparePreferencePage.INITIALLY_SHOW_ANCESTOR_PANE,
                        true);
            } else {
                setLeftEditable(false);
                setRightEditable(false);
            }
        }

        @Override
        public String getAncestorLabel(Object element) {
            return super.getAncestorLabel(element);
        }
    }

    public PerforceEditorInput(String title, boolean merge, String fileType,
            String leftFile, String leftLabel, String rightFile,
            String rightLabel, String ancestorFile, String ancestorLabel) {
        super(new PerforceCompareConfiguration(merge, leftFile, leftLabel,
                rightLabel, ancestorLabel));
        this.leftFile = leftFile;
        this.rightFile = rightFile;
        this.ancestorFile = ancestorFile;
        this.type = fileType;
        this.merge = merge;
        setTitle(title);
    }

    public boolean allowMerge() {
        return merge;
    }

    @Override
    public void saveChanges(IProgressMonitor pm) throws CoreException {
        super.saveChanges(pm);
        if (leftNode instanceof BufferedResourceNode) {
            ((BufferedResourceNode) leftNode).commit(pm);
        }
    }

    @Override
    protected Object prepareInput(IProgressMonitor monitor) {
        IFile leftResource = PerforceProviderPlugin.getWorkspaceFile(leftFile);
        IFile rightResource = PerforceProviderPlugin
                .getWorkspaceFile(rightFile);
        if (leftResource == null) {
            leftNode = new DiffFile(new File(leftFile), type);
        } else {
            leftNode = new BufferedResourceNode(leftResource);
        }
        Object rightNode;
        if (rightResource == null) {
            rightNode = new DiffFile(new File(rightFile), type);
        } else {
            rightNode = new BufferedResourceNode(rightResource);
        }
        Object ancestorNode = null;
        boolean threeway = false;
        if (ancestorFile != null) {
            ancestorNode = new DiffFile(new File(ancestorFile), type);
            threeway = true;
        }
        return new Differencer().findDifferences(threeway, monitor, null,
                ancestorNode, leftNode, rightNode);
    }

    /**
     * @return the leftFile
     */
    public String getLeftFile() {
        return this.leftFile;
    }

    /**
     * @return the rightFile
     */
    public String getRightFile() {
        return this.rightFile;
    }
}
