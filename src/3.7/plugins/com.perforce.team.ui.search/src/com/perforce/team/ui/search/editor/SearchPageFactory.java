/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.search.editor;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4ShelvedChangelist;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.ui.editor.P4PageFactoryAdapter;

import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class SearchPageFactory extends P4PageFactoryAdapter {

    /**
     * @see com.perforce.team.ui.editor.P4PageFactoryAdapter#createPage(org.eclipse.ui.forms.editor.FormEditor)
     */
    @Override
    public IFormPage createPage(FormEditor parent) {
        ChangelistSearchPage page = null;
        IP4Changelist changelist = P4CoreUtils.convert(parent.getEditorInput(),
                IP4Changelist.class);
        if (changelist != null
                && changelist.getConnection().isSearchSupported()
                && (changelist instanceof IP4SubmittedChangelist || changelist instanceof IP4ShelvedChangelist)) {
            page = new ChangelistSearchPage(parent);
        }
        return page;
    }

}
