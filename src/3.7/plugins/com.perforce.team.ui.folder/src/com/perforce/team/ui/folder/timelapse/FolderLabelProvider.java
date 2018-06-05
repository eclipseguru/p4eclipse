/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.timelapse;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.perforce.p4java.core.file.IFileRevisionData;
import com.perforce.p4java.core.file.IRevisionIntegrationData;
import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.core.p4java.P4File;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.text.PerforceUiTextPlugin;
import com.perforce.team.ui.text.timelapse.NodeModelTimeLapseEditor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class FolderLabelProvider implements IStyledLabelProvider {

    private IP4Revision revision = null;
    private Color changeColor;
    private String root = null;
    private WorkbenchLabelProvider provider = new WorkbenchLabelProvider();

    /**
     * Folder label provider
     */
    public FolderLabelProvider() {
        this.changeColor = new Color(P4UIUtils.getDisplay(),
                PreferenceConverter.getColor(PerforceUiTextPlugin.getDefault()
                        .getPreferenceStore(),
                        NodeModelTimeLapseEditor.TICK_CHANGE_COLOR));
    }

    /**
     * Set the revision
     * 
     * @param revision
     * @param root
     */
    public void setRevision(IP4Revision revision, String root) {
        this.revision = revision;
        this.root = root;
    }

    /**
     * @see org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider#getImage(java.lang.Object)
     */
    public Image getImage(Object element) {
        return provider.getImage(element);
    }

    /**
     * @see org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider#getStyledText(java.lang.Object)
     */
    public StyledString getStyledText(Object element) {
        if (revision != null) {
            int revisionId = revision.getChangelist();
            if (element instanceof FileEntry) {
                final FileEntry revisionFile = (FileEntry) element;
                final IFileRevisionData file = revisionFile.getData(revisionId);
                int id = file.getChangelistId();
                if (revisionId == id) {
                    StyledString text = new StyledString(
                            revisionFile.getLabel(revisionFile), new Styler() {

                                @Override
                                public void applyStyles(TextStyle textStyle) {
                                    textStyle.foreground = changeColor;
                                    if (P4File.isActionDelete(file.getAction())) {
                                        textStyle.strikeout = true;
                                    }
                                }
                            });
                    List<IRevisionIntegrationData> integs = file
                            .getRevisionIntegrationData();
                    if (integs != null && integs.size() > 0) {
                        for (IRevisionIntegrationData integ : integs) {
                            if (integ.getHowFrom().equals("moved from")) { //$NON-NLS-1$
                                String from = integ.getFromFile();
                                if (from != null && from.startsWith(root)) {
                                    from = from.substring(root.length() + 1);
                                }
                                text.append(MessageFormat
                                        .format(Messages.FolderLabelProvider_From,
                                                from));
                            } else if (integ.getHowFrom().equals("branch from")) { //$NON-NLS-1$
                                String from = integ.getFromFile();
                                if (from != null && from.startsWith(root)) {
                                    from = from.substring(root.length() + 1);
                                }
                                text.append(MessageFormat
                                        .format(Messages.FolderLabelProvider_From,
                                                from));
                            }
                        }
                    }
                    return text;
                } else if (!revisionFile.isSynced(revisionId)) {
                    StyledString text = new StyledString(
                            revisionFile.getLabel(revisionFile), new Styler() {

                                @Override
                                public void applyStyles(TextStyle textStyle) {
                                    textStyle.foreground = P4UIUtils
                                            .getDisplay().getSystemColor(
                                                    SWT.COLOR_DARK_RED);
                                }
                            });
                    return text;
                } else {
                    return new StyledString(revisionFile.getLabel(revisionFile));
                }
            } else if (element instanceof FolderEntry) {
                final FolderEntry revisionFolder = (FolderEntry) element;
                StyledString text = new StyledString(provider.getText(element));
                if (revisionFolder.isMoveAdd(revisionId)) {
                    String from = revisionFolder.getMoveFrom(revisionId);
                    if (from != null) {
                        text.append(" "); //$NON-NLS-1$
                        text.append(from, new Styler() {

                            @Override
                            public void applyStyles(TextStyle textStyle) {
                                textStyle.strikeout = true;
                            }
                        });
                    }
                }
                return text;
            }
        }
        return new StyledString(provider.getText(element));
    }

    /**
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    public void addListener(ILabelProviderListener listener) {

    }

    /**
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
     */
    public void dispose() {
        provider.dispose();
        this.changeColor.dispose();
    }

    /**
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object,
     *      java.lang.String)
     */
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    /**
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    public void removeListener(ILabelProviderListener listener) {

    }

}
