/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.java.editor;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.P4Storage;
import com.perforce.team.ui.diff.IFileDiffer;
import com.perforce.team.ui.editor.BaseContentAssistProvider;
import com.perforce.team.ui.editor.IProposal;
import com.perforce.team.ui.editor.Proposal;
import com.perforce.team.ui.java.PerforceUiJavaPlugin;
import com.perforce.team.ui.java.diff.JavaDiffer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class JavaContentAssistProvider extends BaseContentAssistProvider {

    /**
     * @see com.perforce.team.ui.editor.BaseContentAssistProvider#getProposals(java.lang.Object,
     *      java.lang.Object)
     */
    @Override
    public IProposal[] getProposals(Object modelContext, Object uiContext) {
        if (modelContext instanceof IP4PendingChangelist) {
            IP4File[] files = ((IP4PendingChangelist) modelContext)
                    .getPendingFiles();
            JavaDiffer differ = new JavaDiffer();
            Set<IProposal> proposals = new HashSet<IProposal>();
            for (final IP4File file : files) {
                if (file.openedForEdit()) {
                    String name = file.getName();
                    if (name != null) {
                        IContentType type = Platform.getContentTypeManager()
                                .findContentTypeFor(name);
                        if (type != null
                                && PerforceUiJavaPlugin.JAVA_CONTENT_TYPE
                                        .equals(type.getId())) {
                            compare(file, differ, proposals);
                        }
                    }
                }
            }
            return proposals.toArray(new IProposal[proposals.size()]);
        }
        return super.getProposals(modelContext, uiContext);
    }

    private void compare(final IP4File file, IFileDiffer differ,
            Collection<IProposal> proposals) {
        final File local = file.toFile();
        if (local != null && local.exists()) {
            P4Storage left = new P4Storage() {

                public InputStream getContents() throws CoreException {
                    try {
                        return new FileInputStream(local);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    return new ByteArrayInputStream(new byte[0]);
                }
            };
            P4Storage right = new P4Storage() {

                public InputStream getContents() throws CoreException {
                    return file.getHeadContents();
                }
            };
            differ.generateDiff(file, file, left, right);
            Object[] diffs = differ.getDiff(file);
            for (Object diff : diffs) {
                walkDiffs(proposals, diff);
            }
        }
    }

    private void walkDiffs(Collection<IProposal> proposals, Object diff) {
        if (diff instanceof IDiffContainer) {
            for (IDiffElement child : ((IDiffContainer) diff).getChildren()) {
                walkDiffs(proposals, child);
            }
        }
        if (diff instanceof IDiffElement) {
            IDiffElement element = (IDiffElement) diff;
            if (element.getKind() == Differencer.ADDITION
                    || element.getKind() == Differencer.CHANGE
                    || element.getKind() == Differencer.DELETION) {
                String name = ((IDiffElement) diff).getName();
                if (name != null) {
                    proposals.add(new Proposal(name));
                }
            }
        }
    }
}
