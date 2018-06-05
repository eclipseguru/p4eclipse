/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.search.results.tree;

import com.perforce.p4java.core.IFileLineMatch;
import com.perforce.p4java.core.IFileLineMatch.MatchType;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.ui.LabelProviderAdapter;
import com.perforce.team.ui.search.results.FileMatch;
import com.perforce.team.ui.search.results.ISearchResultProvider;
import com.perforce.team.ui.search.results.P4SearchResult;
import com.perforce.team.ui.search.results.RevisionMatch;

import java.text.MessageFormat;

import org.eclipse.jface.resource.DeviceResourceManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.search.ui.text.Match;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class SearchTreeLabelProvider extends LabelProviderAdapter {

    /**
     * Image resource manager
     */
    protected DeviceResourceManager imageManager;

    /**
     * Search result provider
     */
    protected ISearchResultProvider page;

    private boolean showFileNames = false;

    /**
     * Create a search label provider
     * 
     * @param page
     * @param display
     */
    public SearchTreeLabelProvider(ISearchResultProvider page, Display display) {
        this.page = page;
        this.imageManager = new DeviceResourceManager(display);
    }

    private int getRevisionCount(IP4File file) {
        int count = 0;
        P4SearchResult result = page.getResult();
        if (result != null) {
            count = result.getRevisions(file).length;
        }
        return count;
    }

    private int getLineCount(RevisionMatch revision) {
        int count = 0;
        P4SearchResult result = page.getResult();
        if (result != null) {
            Match[] matches = result.getMatches(revision);
            for (Match match : matches) {
                if (MatchType.MATCH == ((FileMatch) match).getMatch().getType()) {
                    count++;
                }
            }
        }
        return count;
    }

    private StyledString getFileMatchText(IP4File file) {
        StyledString styled = new StyledString(getText(file));

        int matches = getRevisionCount(file);
        styled.append(MessageFormat.format(
                Messages.SearchTreeLabelProvider_NumRevisions, matches),
                StyledString.COUNTER_STYLER);
        return styled;
    }

    private StyledString getRevisionMatchText(RevisionMatch revision) {
        StyledString styled = new StyledString(getText(revision));

        int matches = getLineCount(revision);
        styled.append(MessageFormat.format(
                Messages.SearchTreeLabelProvider_NumMatches, matches),
                StyledString.COUNTER_STYLER);
        return styled;
    }

    private StyledString getLineMatchText(FileMatch result) {
        IFileLineMatch match = result.getMatch();
        StyledString styled = new StyledString(match.getLineNumber() + ":", //$NON-NLS-1$
                StyledString.QUALIFIER_STYLER);
        styled.append(' ');
        styled.append(P4CoreUtils.removeWhitespace(match.getLine()));
        return styled;
    }

    /**
     * @see com.perforce.team.ui.LabelProviderAdapter#getStyledText(java.lang.Object)
     */
    @Override
    public StyledString getStyledText(Object element) {
        if (element instanceof RevisionMatch) {
            return getRevisionMatchText((RevisionMatch) element);
        } else if (element instanceof FileMatch) {
            return getLineMatchText((FileMatch) element);
        } else if (element instanceof IP4File) {
            return getFileMatchText((IP4File) element);
        }
        return super.getStyledText(element);
    }

    /**
     * @see com.perforce.team.ui.LabelProviderAdapter#getImage(java.lang.Object)
     */
    @Override
    public Image getImage(Object element) {
        if (element instanceof IP4File) {
            String name = ((IP4File) element).getName();
            ImageDescriptor desc = PlatformUI.getWorkbench()
                    .getEditorRegistry().getImageDescriptor(name);
            if (desc != null) {
                return (Image) imageManager.get(desc);
            }
        } else {
            IWorkbenchAdapter adapter = P4CoreUtils.convert(element,
                    IWorkbenchAdapter.class);
            if (adapter != null) {
                ImageDescriptor desc = adapter.getImageDescriptor(element);
                if (desc != null) {
                    return (Image) imageManager.get(desc);
                }
            }
        }
        return super.getImage(element);
    }

    /**
     * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
     */
    public String getText(Object element) {
        if (element instanceof IP4File) {
            if (showFileNames) {
                return ((IP4File) element).getName();
            } else {
                return ((IP4File) element).getRemotePath();
            }
        } else {
            IWorkbenchAdapter adapter = P4CoreUtils.convert(element,
                    IWorkbenchAdapter.class);
            if (adapter != null) {
                return adapter.getLabel(element);
            }
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * @see com.perforce.team.ui.LabelProviderAdapter#dispose()
     */
    @Override
    public void dispose() {
        super.dispose();
        this.imageManager.dispose();
    }

    /**
     * Set whether to show names or depot paths for file labels
     * 
     * @param show
     */
    public void setShowFileNames(boolean show) {
        this.showFileNames = show;
    }

}
