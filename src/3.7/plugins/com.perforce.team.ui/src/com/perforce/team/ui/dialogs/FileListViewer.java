package com.perforce.team.ui.dialogs;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceLabelProvider;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

/**
 * List control to allow user to select from a list of files
 */
public class FileListViewer extends CheckboxTableViewer {

    // Default dimensions
    private static final int DEFAULT_WIDTH = 500;
    private static final int DEFAULT_HEIGHT = 150;

    /**
     * Constructor. Takes list of files to display. Files may be either resource
     * files (IFile) or perforce files (IPerforceFile)
     * 
     * @param parent
     *            parent window
     * @param files
     *            list of files to display
     * @param checkedFiles
     *            the list of files to check
     * @param small
     *            true if control should be half height
     */
    public FileListViewer(Composite parent, Object[] files,
            Object[] checkedFiles, boolean small) {
        super(new Table(parent, SWT.BORDER | SWT.MULTI | SWT.CHECK));

        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        data.widthHint = DEFAULT_WIDTH;
        if (small) {
            data.heightHint = DEFAULT_HEIGHT / 2;
        } else {
            data.heightHint = DEFAULT_HEIGHT;
        }
        getControl().setLayoutData(data);

        setSorter(new ViewerSorter());

        setContentProvider(new ArrayContentProvider());
        setLabelProvider(new PerforceLabelProvider() {

            @Override
            public String getColumnText(Object element, int columnIndex) {
                if (element instanceof IP4Resource) {
                    String path = ((IP4Resource) element)
                            .getActionPath(IP4Resource.Type.REMOTE);
                    String decorated = decorator.getLabelDecorator()
                            .decorateText(path, element);
                    if (decorated != null) {
                        path = decorated;
                    }
                    return path;
                } else if (element instanceof IFile) {
                    return PerforceProviderPlugin
                            .getResourcePath((IFile) element);
                }
                return super.getColumnText(element, columnIndex);
            }

        });

        setInput(files);
        setCheckedElements(checkedFiles);
        P4UIUtils.trackMovedFiles(this);
    }
}
