package com.perforce.team.ui.views;

/*
 * Copyright (c) 2003, 2004 Perforce Software.  All rights reserved.
 *
 */

import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import com.perforce.team.ui.PerforceUIPlugin;

/**
 * Base class for views that show information for the currently selected
 * project. Handles things like managing/unmanaging projects and tracking which
 * project is currently selected.
 */
public abstract class PerforceProjectView extends ViewPart {

    /**
     * SIZE_DELIMITER - delimiter used for view preferences
     */
    public static final String SIZE_DELIMITER = "###"; //$NON-NLS-1$

    /**
     * Gets the split items from a preference key
     * 
     * @param preference
     * @return - values
     */
    public static String[] getItems(String preference) {
        Set<String> splitItems = new HashSet<String>();
        if (preference != null) {
            String items = PerforceUIPlugin.getPlugin().getPreferenceStore()
                    .getString(preference);
            String[] splitStrings = items.split(SIZE_DELIMITER);
            for (String split : splitStrings) {
                if (split != null) {
                    split = split.trim();
                    if (split.length() > 0) {
                        splitItems.add(split);
                    }
                }
            }
        }
        return splitItems.toArray(new String[0]);
    }

    private IPerforceViewControl viewControl;
    private IPerforceView view=new PartBasedView(this);

    abstract protected IPerforceViewControl createViewControl(IPerforceView view);

    /**
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        this.viewControl=createViewControl(getPerforceView());
        this.viewControl.createViewControl(parent);
    }

    
    public IPerforceViewControl getPerforceViewControl() {
        return viewControl;
    }
    
    protected IPerforceView getPerforceView() {
        return view;
    }

    /**
     * Shutdown this view
     * 
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        getPerforceViewControl().dispose();
        super.dispose();
    }

    /**
     * Handle set focus event for this view
     */
    @Override
    public void setFocus() {
        getPerforceViewControl().setFocus();
    }
    
}
