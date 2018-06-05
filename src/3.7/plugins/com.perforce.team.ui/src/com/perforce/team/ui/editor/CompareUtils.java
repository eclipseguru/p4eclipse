/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.editor;

import com.perforce.p4api.PerforceFileAccess;
import com.perforce.p4java.impl.generic.core.file.FileSpec;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.ILocalRevision;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.ui.P4UIUtils;

import java.io.File;
import java.io.InputStream;
import java.text.MessageFormat;

import org.eclipse.compare.CompareUI;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.ui.progress.UIJob;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public final class CompareUtils {

    /**
     * TITLE
     */
    public static final String TITLE = Messages.CompareUtils_DiffTwoRevs;

    /**
     * Compares two depot paths and revisions and opens a compare editor
     * 
     * @param file
     * @param depotPath1
     * @param depotPath2
     * @param revision1
     * @param revision2
     */
    public static void doCompare(IP4File file, String depotPath1,
            String depotPath2, int revision1, int revision2) {
        try {
            String fileType = new Path(file.getRemotePath()).getFileExtension();
            String title = TITLE
                    + PerforceFileAccess.getFilename(file.getRemotePath());
            IP4Connection connection = file.getConnection();
            IP4File p4File1 = connection.getFile(new FileSpec(depotPath1));
            IP4File p4File2 = connection.getFile(new FileSpec(depotPath2));
            if(p4File1!=null && p4File2!=null){
	            InputStream reader1 = p4File1.getRemoteContents(revision1);
	            InputStream reader2 = p4File2.getRemoteContents(revision2);
	            if(reader1!=null && reader2!=null){
		            final File file1 = P4CoreUtils.createFile(reader1);
		            final File file2 = P4CoreUtils.createFile(reader2);
		            if(file1!=null && file2!=null){
			            IP4CompareNode leftNode = new P4FileNode(file1, file.getName()
			                    + "#" + revision1, fileType, file.getName()); //$NON-NLS-1$
			            IP4CompareNode rightNode = new P4FileNode(file2, file.getName()
			                    + "#" + revision2, fileType, file.getName()); //$NON-NLS-1$
			            openCompare(title, leftNode, rightNode);
		            }
	            }
            }
        } catch (Exception e) {
            PerforceProviderPlugin.logError(e);
        }
    }

    private static String getRevisionLabel(Object revision) {
        StringBuilder label = new StringBuilder();
        if (revision instanceof IP4Revision) {
            IP4Revision p4Revision = (IP4Revision) revision;
            label.append(MessageFormat.format(Messages.CompareUtils_RemoteRev,
                    p4Revision.getName(), p4Revision.getRevision()));
            String folder = p4Revision.getRemotePath();
            int lastSlash = folder.lastIndexOf('/');
            if (lastSlash != -1) {
                folder = folder.substring(0, lastSlash);
                label.append(" - "); //$NON-NLS-1$
                label.append(folder);
            }
        } else if (revision instanceof ILocalRevision) {
            ILocalRevision localRevision = (ILocalRevision) revision;
            label.append(MessageFormat.format(Messages.CompareUtils_LocalRev,
                    localRevision.getName(),
                    P4UIUtils.getDateText(localRevision.getTimestamp())));
        } else if (revision instanceof IFileRevision) {
            label.append(((IFileRevision) revision).getName());
        } else if (revision instanceof IFile) {
            label.append(MessageFormat.format(
                    Messages.CompareUtils_WorkspaceFile,
                    ((IFile) revision).getName()));
        } else if (revision instanceof File) {
            label.append(MessageFormat.format(Messages.CompareUtils_LocalFile,
                    ((File) revision).getName()));
        }
        return label.toString();
    }

    private static IP4CompareNode createNode(IFileRevision revision,
            String label, String type, IProgressMonitor monitor) {
        IP4CompareNode node = null;
        if (revision instanceof IP4Revision) {
            try {
                InputStream stream = revision.getStorage(monitor).getContents();
                File file = P4CoreUtils.createFile(stream);
                node = new P4FileNode(file, label, type, revision.getName());
                ((P4FileNode) node).setContentIdentifier(revision
                        .getContentIdentifier());
            } catch (CoreException e) {
            }
        } else if (revision instanceof ILocalRevision) {
            // Fix for job036964, create a P4ResourceNode when dealing with a
            // local current file revision
            ILocalRevision local = (ILocalRevision) revision;
            if (local.isCurrent()) {
                IFile file = local.getFile();
                if (file != null) {
                    node = new P4ResourceNode(file, label);
                }
            }
        }
        if (node == null) {
            try {
                node = new P4StorageNode(revision.getStorage(monitor), label,
                        type);
                ((P4StorageNode) node).setContentIdentifier(revision
                        .getContentIdentifier());
            } catch (CoreException e) {
                PerforceProviderPlugin.logError(e);
            }
        }
        return node;
    }

    private static String getType(IP4File p4File, File file) {
        String name = null;
        if (p4File != null) {
            name = p4File.getName();
        }
        if (name == null && file != null) {
            name = file.getName();
        }

        String type = null;
        if (name != null) {
            type = new Path(name).getFileExtension();
        }
        return type;
    }

    /**
     * Open local compare against an Eclipse resource
     * 
     * @param file1
     * @param file2
     * @param file2Label
     * @param title
     */
    public static void openLocalCompare(IFile file1, File file2,
            String file2Label, String title) {
        openLocalCompare(file1, file2, null, file2Label, title);
    }

    /**
     * Open local compare against an Eclipse resource
     * 
     * @param file1
     * @param file2
     * @param file1Label
     * @param file2Label
     * @param title
     */
    public static void openLocalCompare(File file1, File file2,
            String file1Label, String file2Label, String title) {
        if (file1 != null && file2 != null) {
            String fileType = new Path(file1.getAbsolutePath())
                    .getFileExtension();
            if (file1Label == null) {
                file1Label = getRevisionLabel(file1);
            }
            IP4CompareNode leftNode = new P4FileNode(file1, file1Label,
                    fileType, true, file1.getName());

            IP4CompareNode rightNode = new P4FileNode(file2, file2Label,
                    fileType, file1.getName());

            openCompare(title, leftNode, rightNode);
        }

    }

    /**
     * Open local compare against an Eclipse resource
     * 
     * @param file1
     * @param file2
     * @param file1Label
     * @param file2Label
     * @param title
     */
    public static void openLocalCompare(IFile file1, File file2,
            String file1Label, String file2Label, String title) {
        if (file1 != null && file2 != null) {
            String fileType = file1.getFileExtension();
            if (file1Label == null) {
                file1Label = getRevisionLabel(file1);
            }
            IP4CompareNode leftNode = new P4ResourceNode(file1, file1Label);

            IP4CompareNode rightNode = new P4FileNode(file2, file2Label,
                    fileType, file1.getName());

            openCompare(title, leftNode, rightNode);
        }

    }

    /**
     * Open local compare against an Eclipse resource
     * 
     * @param file
     * @param file2
     * @param label
     */
    public static void openLocalCompare(IFile file, File file2, String label) {
        if (file != null && file2 != null) {
            String title = TITLE + file.getName();
            openLocalCompare(file, file2, label, title);
        }
    }

    /**
     * Open local compare against an Eclipse resource
     * 
     * @param file
     * @param revision
     */
    public static void openLocalCompare(IP4File file, IFileRevision revision) {
        if (file != null) {
            IFile localFile = file.getLocalFileForLocation();
            if (localFile != null) {
                openLocalCompare(localFile, revision);
            } else {
                String local = file.getLocalPath();
                if (local != null) {
                    File compareFile = new File(local);
                    if (compareFile.exists()) {
                        openLocalCompare(compareFile, revision);
                    }
                }
            }
        }
    }

    /**
     * Open local compare against a non-Eclipse resource
     * 
     * @param p4File
     * @param file
     * @param label
     */
    public static void openLocalCompare(IP4File p4File, File file, String label) {
        if (file != null && p4File != null) {
            String fileType = getType(p4File, file);
            IP4CompareNode rightNode = new P4FileNode(file, label, fileType,
                    p4File.getName());

            IFile localFile = p4File.getLocalFileForLocation();
            if (localFile != null) {
                openLocalCompare(localFile, rightNode);
            } else {
                String local = p4File.getLocalPath();
                if (local != null) {
                    File compareFile = new File(local);
                    if (compareFile.exists()) {
                        openLocalCompare(compareFile, rightNode);
                    }
                }
            }
        }
    }

    /**
     * Open local compare against a non-Eclipse resource
     * 
     * @param p4File
     * @param revision
     * @param file
     * @param label
     */
    public static void openLocalCompare(IP4File p4File, int revision,
            File file, String label) {
        if (file != null && p4File != null) {
            String fileType = getType(p4File, file);
            String fileLabel = label == null ? getRevisionLabel(file) : label;
            String name = p4File.getName();
            IP4CompareNode leftNode = new P4FileNode(file, fileLabel, fileType,
                    true, name);

            InputStream reader1 = p4File.getRemoteContents(revision);
            final File file1 = P4CoreUtils.createFile(reader1);

            IP4CompareNode rightNode = new P4FileNode(file1, name + "#" //$NON-NLS-1$
                    + revision, fileType, name);

            String title = TITLE + name;
            openCompare(title, leftNode, rightNode);
        }
    }

    /**
     * Open local compare against a non-Eclipse resource
     * 
     * @param file
     * @param revision
     */
    public static void openLocalCompare(File file, IFileRevision revision) {
        if (file != null && revision != null) {
            String fileType = new Path(file.getAbsolutePath())
                    .getFileExtension();
            String fileLabel = getRevisionLabel(file);
            IP4CompareNode leftNode = new P4FileNode(file, fileLabel, fileType,
                    true, file.getName());

            String rightLabel = getRevisionLabel(revision);
            IP4CompareNode rightNode = createNode(revision, rightLabel,
                    fileType, new NullProgressMonitor());

            String title = TITLE + file.getName();
            openCompare(title, leftNode, rightNode);
        }
    }

    /**
     * Open local compare against a non-Eclipse resource
     * 
     * @param file
     * @param rightNode
     */
    public static void openLocalCompare(File file, IP4CompareNode rightNode) {
        if (file != null && rightNode != null) {
            String fileType = new Path(file.getAbsolutePath())
                    .getFileExtension();
            String fileLabel = getRevisionLabel(file);
            IP4CompareNode leftNode = new P4FileNode(file, fileLabel, fileType,
                    true, file.getName());

            String title = TITLE + file.getName();
            openCompare(title, leftNode, rightNode);
        }
    }

    /**
     * Open local compare against an Eclipse resource
     * 
     * @param file
     * @param rightNode
     */
    public static void openLocalCompare(IFile file, IP4CompareNode rightNode) {
        if (file != null && rightNode != null) {
            IP4CompareNode leftNode = new P4ResourceNode(file,
                    getRevisionLabel(file));

            String title = TITLE + file.getName();
            openCompare(title, leftNode, rightNode);
        }
    }

    /**
     * Open local compare against an Eclipse resource
     * 
     * @param file
     * @param revision
     */
    public static void openLocalCompare(IFile file, IFileRevision revision) {
        if (file != null && revision != null) {
            String fileType = file.getFileExtension();
            IP4CompareNode leftNode = new P4ResourceNode(file,
                    getRevisionLabel(file));

            String rightLabel = getRevisionLabel(revision);
            IP4CompareNode rightNode = createNode(revision, rightLabel,
                    fileType, new NullProgressMonitor());

            String title = TITLE + file.getName();
            openCompare(title, leftNode, rightNode);
        }
    }

    /**
     * 
     * @param title
     * @param leftNode
     * @param rightNode
     */
    public static void openCompare(final String title,
            final IP4CompareNode leftNode, final IP4CompareNode rightNode) {
        UIJob job = new UIJob(Messages.CompareUtils_OpeningCompareEditor) {

            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                P4CompareEditorInput input = new P4CompareEditorInput(title,
                        leftNode, rightNode);
                CompareUI.openCompareEditor(input);
                return Status.OK_STATUS;
            }

        };
        job.schedule();
    }

    /**
     * @param left
     * @param right
     */
    public static void openCompare(IFileRevision left, IFileRevision right) {
        String fileType = new Path(left.getName()).getFileExtension();
        String leftLabel = getRevisionLabel(left);
        String rightLabel = getRevisionLabel(right);

        IP4CompareNode leftNode = createNode(left, leftLabel, fileType,
                new NullProgressMonitor());
        IP4CompareNode rightNode = createNode(right, rightLabel, fileType,
                new NullProgressMonitor());

        String title = TITLE + left.getName();
        openCompare(title, leftNode, rightNode);
    }

}
