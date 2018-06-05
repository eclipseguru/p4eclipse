/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.editor;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.folder.IP4DiffFile;
import com.perforce.team.ui.changelists.FolderFileLabelProvider;
import com.perforce.team.ui.changelists.Folder.Type;
import com.perforce.team.ui.folder.diff.model.FileDiffElement;

import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class FileDiffLabelProvider extends FolderFileLabelProvider {

    /**
     */
    public FileDiffLabelProvider() {
        super();
    }

    /**
     * 
     * @param type
     */
    public FileDiffLabelProvider(Type type) {
        super(type);
    }

    /**
     * Get decorated text
     * 
     * @param text
     * @param element
     * @return - text
     */
    @Override
    protected String getDecoratedText(String text, Object element) {
        return text;
    }

    /**
     * @see com.perforce.team.ui.diff.DiffLabelProvider#getDiffImage(org.eclipse.compare.structuremergeviewer.IDiffElement)
     */
    @Override
    protected Image getDiffImage(IDiffElement diff) {
        int kind = diff.getKind();
        if (kind == Differencer.ADDITION) {
            kind = Differencer.DELETION;
        }
        return configuration.getImage(diff.getImage(), kind);
    }

    /**
     * @see com.perforce.team.ui.diff.DiffLabelProvider#getColumnImage(java.lang.Object,
     *      int)
     */
    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        if (element instanceof IP4DiffFile) {
            ImageDescriptor desc = PlatformUI.getWorkbench()
                    .getEditorRegistry()
                    .getImageDescriptor(((IP4DiffFile) element).getName());
            if (desc != null) {
                Image image = images.get(desc);
                if (image == null) {
                    image = desc.createImage();
                    images.put(desc, image);
                }
                return enlargeImage(image);
            }
        } else if (element instanceof FileDiffElement) {
            element = P4CoreUtils.convert(element, IDiffElement.class);
        }
        return super.getColumnImage(element, columnIndex);
    }

}
