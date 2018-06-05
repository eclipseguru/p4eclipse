/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.editor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IColorDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.perforce.p4java.core.IFileDiff.Status;
import com.perforce.p4java.core.file.FileAction;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.folder.IP4DiffFile;
import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.decorator.P4Decoration;
import com.perforce.team.ui.decorator.PerforceDecorator;
import com.perforce.team.ui.folder.PerforceUiFolderPlugin;
import com.perforce.team.ui.folder.diff.model.FileDiffElement;
import com.perforce.team.ui.folder.preferences.IPreferenceConstants;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class FileDiffDecorator extends PerforceDecorator implements
        IColorDecorator {

    private Color contentColor;
    private Color uniqueColor;
    private Color diffContentColor;
    private Color diffUniqueColor;

    /**
     * @param decorateResources
     */
    public FileDiffDecorator(boolean decorateResources) {
        super(decorateResources);
        PerforceUiFolderPlugin.getDefault().getPreferenceStore()
                .addPropertyChangeListener(this);
        loadColors();
    }

    private void loadColors() {
        if (this.contentColor != null) {
            this.contentColor.dispose();
        }
        if (this.uniqueColor != null) {
            this.uniqueColor.dispose();
        }
        if (diffContentColor != null) {
            this.diffContentColor.dispose();
        }
        if (this.diffUniqueColor != null) {
            this.diffUniqueColor.dispose();
        }
        IPreferenceStore store = PerforceUiFolderPlugin.getDefault()
                .getPreferenceStore();
        Display display = P4UIUtils.getDisplay();

        this.contentColor = new Color(display, PreferenceConverter.getColor(
                store, IPreferenceConstants.CONTENT_COLOR));
        this.uniqueColor = new Color(display, PreferenceConverter.getColor(
                store, IPreferenceConstants.UNIQUE_COLOR));

        this.diffContentColor = new Color(display,
                PreferenceConverter.getColor(store,
                        IPreferenceConstants.DIFF_CONTENT_COLOR));
        this.diffUniqueColor = new Color(display, PreferenceConverter.getColor(
                store, IPreferenceConstants.DIFF_UNIQUE_COLOR));
    }

    /**
     * @see com.perforce.team.ui.decorator.PerforceDecorator#dispose()
     */
    @Override
    public void dispose() {
        super.dispose();
        this.contentColor.dispose();
        this.uniqueColor.dispose();
        PerforceUiFolderPlugin.getDefault().getPreferenceStore()
                .removePropertyChangeListener(this);
    }

    /**
     * Decorate a diff file. Diff files have a subset of decorations so only
     * revision, type, action, and name are currently added. This method still
     * honors the current decoration preferences for files for the text
     * decorations supported.
     * 
     * @param text
     * @param diffFile
     * @return - decorated text or null
     */
    public String decorateDiffFile(String text, IP4DiffFile diffFile) {
        Map<String, String> bindings = new HashMap<String, String>();
        IFileSpec spec = diffFile.getFile().getP4JFile();
        if (spec != null) {

            int have = spec.getEndRevision();
            if (have > 0) {
                bindings.put(P4Decoration.HAVE_VARIABLE, Integer.toString(have));
            }

            String type = spec.getFileType();
            if (type != null) {
                bindings.put(P4Decoration.TYPE_VARIABLE, type);
            }

            IP4Revision revision = P4CoreUtils.convert(diffFile,
                    IP4Revision.class);
            if (revision != null) {
                FileAction action = revision.getAction();
                if (action != null) {
                    bindings.put(P4Decoration.ACTION_VARIABLE, action
                            .toString().toLowerCase());
                }
            }

            bindings.put(P4Decoration.NAME_VARIABLE, text);
        }
        StringBuilder decorated = P4Decoration.decorate(fileDecoration,
                bindings);
        return removeTrailingWhitespace(decorated);
    }

    /**
     * @see com.perforce.team.ui.decorator.PerforceDecorator#decorateImage(org.eclipse.swt.graphics.Image,
     *      java.lang.Object)
     */
    @Override
    public Image decorateImage(Image image, Object o) {
        return image;
    }

    /**
     * @see com.perforce.team.ui.decorator.PerforceDecorator#decorateText(java.lang.String,
     *      java.lang.Object)
     */
    @Override
    public String decorateText(String text, Object o) {
        String decorated = null;
        if (o instanceof IP4DiffFile) {
            decorated = decorateDiffFile(text, (IP4DiffFile) o);
        } else {
            decorated = super.decorateText(text, o);
        }
        return decorated;
    }

    /**
     * @see org.eclipse.jface.viewers.IColorDecorator#decorateForeground(java.lang.Object)
     */
    public Color decorateForeground(Object element) {
        return null;
    }

    /**
     * @see org.eclipse.jface.viewers.IColorDecorator#decorateBackground(java.lang.Object)
     */
    public Color decorateBackground(Object element) {
        if (element instanceof IP4DiffFile) {
            IP4DiffFile file = (IP4DiffFile) element;
            Status status = file.getDiff().getStatus();
            if (status == Status.CONTENT) {
                return this.contentColor;
            } else if (status == Status.LEFT_ONLY
                    || status == Status.RIGHT_ONLY) {
                return this.uniqueColor;
            }
        } else if (element instanceof FileDiffElement) {
            switch (((FileDiffElement) element).getElement().getKind()) {
            case Differencer.ADDITION:
            case Differencer.DELETION:
                return this.diffUniqueColor;
            case Differencer.CHANGE:
                return this.diffContentColor;
            default:
                break;
            }
        }
        return null;
    }

    /**
     * @see com.perforce.team.ui.decorator.PerforceDecorator#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        String property = event.getProperty();
        if (IPreferenceConstants.UNIQUE_COLOR.equals(property)
                || IPreferenceConstants.CONTENT_COLOR.equals(property)
                || IPreferenceConstants.DIFF_CONTENT_COLOR.equals(property)
                || IPreferenceConstants.DIFF_UNIQUE_COLOR.equals(property)) {
            loadColors();
            fireLabelProviderChanged(new LabelProviderChangedEvent(this));
        } else {
            super.propertyChange(event);
        }
    }

    public String getName() {
    	return FileDiffDecorator.class.getSimpleName()+":"+super.getName();
    }

}
