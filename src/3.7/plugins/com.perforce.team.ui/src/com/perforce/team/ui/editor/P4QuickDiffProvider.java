/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.editor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.quickdiff.IQuickDiffReferenceProvider;

import com.perforce.p4java.CharsetDefs;
import com.perforce.p4java.server.PerforceCharsets;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Listener;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.ui.P4ConnectionManager;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public abstract class P4QuickDiffProvider implements
        IQuickDiffReferenceProvider {

    /**
     * File being diff'ed against
     */
    private IP4File file = null;

    private IFile localFile = null;

    /**
     * Current reference document
     */
    protected IDocument document = null;

    /**
     * Quick diff provider id
     */
    protected String id = null;

    private IP4Listener listener = new IP4Listener() {

        public void resoureChanged(P4Event event) {
            if (file != null && EventType.REFRESHED == event.getType()) {
                for (IP4File eventFile : event.getFiles()) {
                    if (file.equals(eventFile)) {
                        if (shouldRefresh(file)) {
                            runPrintJob();
                        }
                    }
                }
            }
        }
		public String getName() {
			return P4QuickDiffProvider.this.getClass().getSimpleName();
		}
    };

    private Job printJob = null;

    private void runPrintJob() {
        if (printJob == null) {
            printJob = new Job(Messages.P4QuickDiffProvider_RefreshingDocument) {

                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    if (file != null) {
                        String path = file.getLocalPath();
                        if (path != null) {
                            monitor.setTaskName(NLS
                                    .bind(Messages.P4QuickDiffProvider_RefreshingDocumentFor,
                                            path));
                        }
                        refreshDocument(monitor);
                    }
                    return Status.OK_STATUS;
                }
            };
            printJob.setRule(new ISchedulingRule() {

                public boolean isConflicting(ISchedulingRule rule) {
                    return rule == this;
                }

                public boolean contains(ISchedulingRule rule) {
                    return rule == this;
                }
            });
        }

        if (printJob.getState() != Job.NONE) {
            printJob.cancel();
        }
        printJob.schedule();
    }

    /**
     * @see org.eclipse.ui.texteditor.quickdiff.IQuickDiffReferenceProvider#dispose()
     */
    public void dispose() {
        P4ConnectionManager.getManager().removeListener(listener);
        if (printJob != null) {
            printJob.cancel();
        }
    }

    /**
     * @see org.eclipse.ui.texteditor.quickdiff.IQuickDiffReferenceProvider#getId()
     */
    public String getId() {
        return this.id;
    }

    /**
     * Should the document refresh?
     * 
     * @param file
     * @return - true to refresh, false otherwise
     */
    protected abstract boolean shouldRefresh(IP4File file);

    /**
     * Get input stream for the specified file
     * 
     * @param file
     * @param monitor
     * 
     * @return - stream
     */
    protected abstract InputStream getInputStream(IP4File file,
            IProgressMonitor monitor);

    private IDocument loadStream(IDocument loadedDoc, InputStream stream,
            IProgressMonitor monitor) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[1024];
            int read = stream.read(buffer);
            while (read > 0) {
                byteStream.write(buffer, 0, read);
                read = stream.read(buffer);
            }
        } catch (IOException e) {
            PerforceProviderPlugin.logError(e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ioe) {
                    // Suppress close exception
                }
            }
        }
        String charset = file.getConnection().getParameters()
                .getCharsetNoNone();
        if (charset != null) {
            charset = PerforceCharsets.getJavaCharsetName(charset);
        }
        if (charset == null) {
        	charset=CharsetDefs.DEFAULT_NAME;
        }
        try {
        	loadedDoc.set(byteStream.toString(charset));
        } catch (UnsupportedEncodingException e) {
        	PerforceProviderPlugin.logError(e);
        }
        return loadedDoc;
    }

    /**
     * Refresh the document
     * 
     * @param monitor
     */
    protected void refreshDocument(IProgressMonitor monitor) {
        if (this.document == null) {
            this.document = new Document();
        }
        IP4Resource resource = P4ConnectionManager.getManager().getResource(
                this.localFile);
        if (resource instanceof IP4File) {
            IP4File p4File = (IP4File) resource;
            if (p4File != null) {
                // Store latest retrieved p4 file
                this.file = p4File;
                InputStream stream = getInputStream(p4File, monitor);
                if (stream != null) {
                    loadStream(this.document, stream, monitor);
                }
            }
        }
    }

    /**
     * @see org.eclipse.ui.texteditor.quickdiff.IQuickDiffReferenceProvider#getReference(org.eclipse.core.runtime.IProgressMonitor)
     */
    public IDocument getReference(IProgressMonitor monitor)
            throws CoreException {
        if (this.document == null) {
            refreshDocument(monitor);
        }
        return this.document;
    }

    /**
     * @see org.eclipse.ui.texteditor.quickdiff.IQuickDiffReferenceProvider#isEnabled()
     */
    public boolean isEnabled() {
        return this.localFile != null;
    }

    /**
     * @see org.eclipse.ui.texteditor.quickdiff.IQuickDiffReferenceProvider#setActiveEditor(org.eclipse.ui.texteditor.ITextEditor)
     */
    public void setActiveEditor(ITextEditor editor) {
        P4ConnectionManager.getManager().addListener(listener);
        IEditorInput input = editor.getEditorInput();
        if (input != null) {
            IFile localFile = null;
            Object resource = input.getAdapter(IResource.class);
            if (resource instanceof IFile) {
                localFile = (IFile) resource;
            }
            if (localFile == null) {
                if (input instanceof IFileEditorInput) {
                    localFile = ((IFileEditorInput) input).getFile();
                } else if (input instanceof IPathEditorInput) {
                    IPath path = ((IPathEditorInput) input).getPath();
                    if (path != null) {
                        localFile = ResourcesPlugin.getWorkspace().getRoot()
                                .getFile(path);
                    }
                }
            }
            this.localFile = localFile;
        }
    }

    /**
     * @see org.eclipse.ui.texteditor.quickdiff.IQuickDiffReferenceProvider#setId(java.lang.String)
     */
    public void setId(String id) {
        this.id = id;
    }

}
