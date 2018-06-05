/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;

import java.util.Collection;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class BaseHyperlinkDetector extends AbstractHyperlinkDetector {

    /**
     * Generate hyperlinks
     * 
     * @param value
     * @param regionOffset
     * @param positionOffset
     * @return - collection of hyperlinks
     */
    protected abstract Collection<IHyperlink> generateHyperlinks(String value,
            int regionOffset, int positionOffset);

    /**
     * @see org.eclipse.jface.text.hyperlink.IHyperlinkDetector#detectHyperlinks(org.eclipse.jface.text.ITextViewer,
     *      org.eclipse.jface.text.IRegion, boolean)
     */
    public IHyperlink[] detectHyperlinks(ITextViewer textViewer,
            IRegion region, boolean canShowMultipleHyperlinks) {
        if (textViewer == null || region == null) {
            return null;
        }
        IDocument document = textViewer.getDocument();
        if (document == null || document.getLength() == 0
                || region.getOffset() >= document.getLength()) {
            return null;
        }
        try {
            int positionOffset = -1;

            // Use entire line when region length is unspecified
            if (region.getLength() == 0) {
                IRegion lineRegion = document.getLineInformationOfOffset(region
                        .getOffset());
                if (lineRegion != null) {
                    positionOffset = region.getOffset()
                            - lineRegion.getOffset();
                    region = lineRegion;
                }
            }

            int regionOffset = region.getOffset();
            String value = document.get(regionOffset, region.getLength());
            Collection<IHyperlink> links = generateHyperlinks(value,
                    regionOffset, positionOffset);
            if (links != null && !links.isEmpty()) {
                return links.toArray(new IHyperlink[links.size()]);
            }
        } catch (BadLocationException e) {
            return null;
        }
        return null;
    }

    /**
     * Accept a match at the given start and length when compared to the
     * position offset
     * 
     * @param offset
     * @param start
     * @param length
     * @return - true to be added as a hyperlink, false to ignore
     */
    protected boolean accept(int offset, int start, int length) {
        return offset < 0 || offset >= start && offset <= start + length;
    }

    /**
     * Get connection for object
     * 
     * @param object
     * @return - p4 connection
     */
    protected IP4Connection getResourceConnection(Object object) {
        IResource resource = P4CoreUtils.convert(object, IResource.class);
        if (resource != null) {
            return P4ConnectionManager.getManager().getConnection(
                    resource.getProject(), false);
        } else {
            return null;
        }
    }

    /**
     * Get editor input if active part is an editor
     * 
     * @return - editor input
     */
    protected IEditorInput getActiveEditorInput() {
        IWorkbenchPage page = PerforceUIPlugin.getActivePage();
        if (page != null) {
            IWorkbenchPart part = page.getActivePart();
            if (part instanceof IEditorPart) {
                return ((IEditorPart) part).getEditorInput();
            }
        }
        return null;
    }

    /**
     * Get connection for context
     * 
     * @return - p4 connection
     */
    protected IP4Connection getConnection() {
        // First check if we are dealing with more than one connection
        IP4Connection[] connections = P4ConnectionManager.getManager()
                .getConnections();
        if (connections.length == 1) {
            return connections[0];
        }

        // Search for IResource or IP4Resource to get connection
        IP4Connection connection = null;
        Object resource = getAdapter(IResource.class);
        connection = getResourceConnection(resource);
        if (connection == null) {
            resource = getAdapter(IP4Resource.class);
            if (resource instanceof IP4Resource) {
                connection = ((IP4Resource) resource).getConnection();
            }
        }

        // Search active part if editor and can adapt to IResource
        if (connection == null) {
            IEditorInput input = getActiveEditorInput();
            if (input != null) {
                resource = input.getAdapter(IResource.class);
                connection = getResourceConnection(resource);

                // Try adapter manager to adapt input to a connection
                if (connection == null) {
                    Object adapted = Platform.getAdapterManager().getAdapter(
                            input, IP4Connection.class);
                    if (adapted instanceof IP4Connection) {
                        connection = (IP4Connection) adapted;
                    }
                }
            }
        }
        return connection;
    }

}
