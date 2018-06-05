/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.text.timelapse.form;

import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.text.timelapse.INodeModel;
import com.perforce.team.ui.text.timelapse.NodeTickDecorator;
import com.perforce.team.ui.timelapse.ActionTickDecorator;
import com.perforce.team.ui.timelapse.IAuthorProvider;
import com.perforce.team.ui.timelapse.ITickDecorator;

import org.eclipse.swt.graphics.Image;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class FormTickDecorator extends NodeTickDecorator {

    private static class FormActionTickDecorator extends ActionTickDecorator {

        private FormTickDecorator parent;

        /**
         * Create form action tick decorator
         * 
         * @param parent
         */
        public FormActionTickDecorator(FormTickDecorator parent) {
            this.parent = parent;
        }

        /**
         * @see com.perforce.team.ui.timelapse.ActionTickDecorator#isModifiedByOwner(com.perforce.team.core.p4java.IP4Revision)
         */
        @Override
        protected boolean isModifiedByOwner(IP4Revision revision) {
            return this.parent.isModifiedByOwner(revision);
        }

    }

    private Image baseImage;

    private IAuthorProvider authorProvider = new IAuthorProvider() {

        public String getAuthor(IP4Revision revision) {
            return revision.getAuthor();
        }
    };

    /**
     * @param model
     */
    public FormTickDecorator(INodeModel model) {
        super(model);
        this.baseImage = PerforceUIPlugin.getDescriptor(
                IPerforceUIConstants.IMG_EMPTY).createImage();
    }

    /**
     * Create the action tick decorator to use in this node decorator
     * 
     * @return non-null action tick decorator
     */
    @Override
    protected ITickDecorator createActionTickDecorator() {
        return new FormActionTickDecorator(this);
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeTickDecorator#isModifiedByOwner(com.perforce.team.core.p4java.IP4Revision)
     */
    @Override
    protected boolean isModifiedByOwner(IP4Revision revision) {
        return revision.getConnection().isOwner(
                authorProvider.getAuthor(revision));
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeTickDecorator#dispose()
     */
    @Override
    public void dispose() {
        this.baseImage.dispose();
        super.dispose();
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeTickDecorator#getBaseImage(java.lang.Object)
     */
    @Override
    protected Image getBaseImage(Object node) {
        return this.baseImage;
    }

    /**
     * Set author provider
     * 
     * @param provider
     */
    public void setAuthorProvider(IAuthorProvider provider) {
        if (provider != null) {
            this.authorProvider = provider;
        }
    }
}
