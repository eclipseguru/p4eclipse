/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.search.results;

import com.perforce.team.ui.LabelDecoratorAdapter;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class SearchLabelDecorator extends LabelDecoratorAdapter {

    /**
     * @see com.perforce.team.ui.LabelDecoratorAdapter#decorateText(java.lang.String,
     *      java.lang.Object)
     */
    @Override
    public String decorateText(String text, Object element) {
        if (element instanceof RevisionMatch) {
            RevisionMatch match = (RevisionMatch) element;
            if (match.isHave()) {
                return text + Messages.SearchLabelDecorator_Have;
            }
        }
        return super.decorateText(text, element);
    }

}
