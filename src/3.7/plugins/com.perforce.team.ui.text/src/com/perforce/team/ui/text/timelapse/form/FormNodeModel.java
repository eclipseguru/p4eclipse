/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.text.timelapse.form;

import com.perforce.p4java.server.PerforceCharsets;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.ui.text.timelapse.IFilterNodeModel;
import com.perforce.team.ui.text.timelapse.NodeModel;
import com.perforce.team.ui.timelapse.IAuthorProvider;
import com.perforce.team.ui.timelapse.IRevisionInputCache;
import com.perforce.team.ui.timelapse.ITempFileInput;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.ui.IEditorInput;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class FormNodeModel extends NodeModel implements IFilterNodeModel,
        IAuthorProvider {

    private Set<String> fieldNames = new TreeSet<String>();
    private Map<IP4Revision, Form> forms = new HashMap<IP4Revision, Form>();

    /**
     * @param revisions
     * @param inputCache
     */
    public FormNodeModel(IP4Revision[] revisions, IRevisionInputCache inputCache) {
        super(revisions, inputCache);
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeModel#clear()
     */
    @Override
    public void clear() {
        super.clear();
//        this.forms.clear();
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeModel#clear(com.perforce.team.core.p4java.IP4Revision)
     */
    @Override
    public void clear(IP4Revision revision) {
        super.clear(revision);
//        this.forms.remove(revision);
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeModel#deleteTempFile(com.perforce.team.ui.timelapse.ITempFileInput)
     */
    @Override
    protected void deleteTempFile(ITempFileInput tempInput) {
        // Don't delete the temp file since the editor document provider will
        // use it
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeModel#findNodes(java.lang.Object,
     *      com.perforce.team.core.p4java.IP4Revision)
     */
    @Override
    public void findNodes(Object nodeInput, IP4Revision revision) {
        if (nodeInput instanceof Form) {
            Form form = (Form) nodeInput;
            for (FormField field : form.getFields()) {
                if (field != null) {
                	String name = field.getName();
                	fieldNames.add(name);
                    int hash = computeHash(field.getValue(),P4CoreUtils.charsetForName(PerforceCharsets.getJavaCharsetName(revision.getCharset())));
                    addRecord(hash, name, null, field, revision);
                }
            }
        }
    }

    /**
     * Get a form field for a specified id and revision
     * 
     * @param id
     * @param revision
     * @return - form field or null if absent from revision
     */
    public FormField getFormField(String id, IP4Revision revision) {
        return (FormField) getElement(revision, id);
    }

    /**
     * Get first form field
     * 
     * @param revision
     * @return - form field
     */
    public FormField getFirstFormField(IP4Revision revision) {
        FormField field = null;
        if (revision != null) {
            Form form = this.forms.get(revision);
            if (form != null) {
                field = form.getFirst();
            }
        }
        return field;
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeModel#parseInput(org.eclipse.ui.IEditorInput,
     *      com.perforce.team.core.p4java.IP4Revision)
     */
    @Override
    public Object parseInput(IEditorInput input, IP4Revision revision) {
        Form form = new Form(revision.getConnection());
        form.build(input);
        this.forms.put(revision, form);
        return form;
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.IFilterNodeModel#getFilterKey(java.lang.String)
     */
    public String getFilterKey(String filterLabel) {
        return filterLabel;
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.IFilterNodeModel#getFilterLabels()
     */
    public Collection<String> getFilterLabels() {
        return this.fieldNames;
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.INodeModel#getHandle(java.lang.Object)
     */
    public String getHandle(Object element) {
        return element instanceof FormField
                ? ((FormField) element).getName()
                : null;
    }

    /**
     * @see com.perforce.team.ui.timelapse.IAuthorProvider#getAuthor(com.perforce.team.core.p4java.IP4Revision)
     */
    public String getAuthor(IP4Revision revision) {
        String author = null;
        if (revision != null) {
            Form form = this.forms.get(revision);
            if (form != null) {
                author = form.getAuthor();
            }
        }
        return author;
    }

}
