/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.editor.palette;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.mergequest.editor.IBranchGraphEditor;
import com.perforce.team.ui.mergequest.editor.IBranchGraphPage;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.gef.ui.views.palette.PalettePage;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.Page;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchGraphEditorPalettePage extends Page implements PalettePage,
        IPage, IAdaptable {

    private IBranchGraphEditor editor;

    private Composite displayArea;
    private StackLayout daLayout;
    private Map<IBranchGraphPage, PalettePage> pages;

    private IPageChangedListener pageListener = new IPageChangedListener() {

        public void pageChanged(PageChangedEvent event) {
            updateActivePage();
        }
    };

    /**
     * Create editor palette page
     * 
     * @param editor
     */
    public BranchGraphEditorPalettePage(IBranchGraphEditor editor) {
        this.pages = new HashMap<IBranchGraphPage, PalettePage>();
        this.editor = editor;
    }

    /**
     * @see org.eclipse.ui.part.Page#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {
        this.displayArea = new Composite(parent, SWT.NONE);
        this.daLayout = new StackLayout();
        this.displayArea.setLayout(this.daLayout);
        this.displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                true));
        this.editor.addPageChangedListener(this.pageListener);
        updateActivePage();
    }

    private PalettePage createPalettePage(IBranchGraphPage page) {
        PalettePage palette = P4CoreUtils.convert(page, PalettePage.class);
        if (palette != null) {
            palette.createControl(this.displayArea);
            pages.put(page, palette);
        }
        return palette;
    }

    private void updateActivePage() {
        IBranchGraphPage active = editor.getActiveGraphPage();
        if (active != null) {
            PalettePage palette = this.pages.get(active);
            if (palette == null) {
                palette = createPalettePage(active);
            }
            if (palette != null) {
                this.daLayout.topControl = palette.getControl();
            } else {
                this.daLayout.topControl = null;
            }
            this.displayArea.layout(true, true);
        }
    }

    /**
     * @see org.eclipse.ui.part.Page#dispose()
     */
    @Override
    public void dispose() {
        this.editor.removePageChangedListener(this.pageListener);
        pages.clear();
        super.dispose();
    }

    /**
     * @see org.eclipse.ui.part.Page#getControl()
     */
    @Override
    public Control getControl() {
        return this.displayArea;
    }

    /**
     * @see org.eclipse.ui.part.Page#setFocus()
     */
    @Override
    public void setFocus() {
        this.displayArea.setFocus();
    }

    /**
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class adapter) {
        return Platform.getAdapterManager().getAdapter(this, adapter);
    }

    /**
     * Is this page disposed?
     * 
     * @return true if disposed, false otherwise
     */
    public boolean isDisposed() {
        return !P4UIUtils.okToUse(this.displayArea);
    }

}
