/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.changelists;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.ui.BaseErrorProvider;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class PendingCombo extends BaseErrorProvider {

    private Combo combo;
    private DescriptionViewer comment;
    private IP4PendingChangelist[] changes;
    private int pendingId = -1;
    private String pendingComment = ""; //$NON-NLS-1$
    private String label = Messages.PendingCombo_PendingChangelist;
    private boolean resize = true;
    
    private IP4Resource resource;
    private int initialSelectedChangeListId;

    /**
     * Create a new pending changelist label and combo widget
     * 
     * @param label
     * @param resource
     */
    public PendingCombo(String label, IP4Resource resource) {
        this.resource=resource;
//        changes = resource.getConnection().getCachedPendingChangelists();
//        Arrays.sort(changes, new Comparator<IP4PendingChangelist>() {
//
//            public int compare(IP4PendingChangelist o1, IP4PendingChangelist o2) {
//                return o1.getId() - o2.getId();
//            }
//        });
        if (label != null) {
            this.label = label;
        }
    }

    /**
     * Create a new pending changelist label and combo widget
     * 
     * @param resource
     */
    public PendingCombo(IP4Resource resource) {
        this(null, resource);
    }

    /**
     * Create the pending combo
     * 
     * @param parent
     */
    public void createControl(Composite parent) {
        createControl(parent, null);
    }

    /**
     * Create the pending combo
     * 
     * @param parent
     * @param description
     */
    public void createControl(Composite parent, String description) {
        createControl(parent, 1, description);
    }

    /**
     * Create the pending combo
     * 
     * @param parent
     * @param hSpan
     */
    public void createControl(Composite parent, int hSpan) {
        createControl(parent, hSpan, -1);
    }

    /**
     * Create the pending combo
     * 
     * @param parent
     * @param hSpan
     * @param initialDescription
     */
    public void createControl(Composite parent, int hSpan,
            String initialDescription) {
        createControl(parent, hSpan, -1, initialDescription);
    }

    /**
     * Create the pending combo
     * 
     * @param parent
     * @param hSpan
     * @param selectedId
     */
    public void createControl(final Composite parent, int hSpan, int selectedId) {
        this.createControl(parent, hSpan, selectedId, null);
    }

    /**
     * Create the pending combo
     * 
     * @param parent
     * @param hSpan
     * @param selectedId
     * @param initialDescription
     */
    public void createControl(final Composite parent, int hSpan,
            int selectedId, String initialDescription) {
        this.createControl(parent, hSpan, selectedId, initialDescription, true);
    }

    /**
     * Create the pending combo
     * 
     * @param parent
     * @param hSpan
     * @param selectedId
     * @param initialDescription
     * @param resizeShell
     */
    public void createControl(final Composite parent, int hSpan,
            int selectedId, String initialDescription, boolean resizeShell) {
        this.initialSelectedChangeListId=selectedId;
        this.resize = resizeShell;
        Composite changelist = new Composite(parent, SWT.NONE);
        GridData cData = new GridData(SWT.FILL, SWT.FILL, true, false);
        cData.horizontalSpan = hSpan;
        changelist.setLayoutData(cData);
        GridLayout cLayout = new GridLayout(2, false);
        changelist.setLayout(cLayout);
        Label changelistLabel = new Label(changelist, SWT.LEFT);
        changelistLabel.setText(this.label);
        combo = new Combo(changelist, SWT.READ_ONLY | SWT.DROP_DOWN);
        combo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        combo.add(Messages.PendingCombo_New);

        final Label description = new Label(changelist, SWT.NONE);
        final GridData descriptionData = new GridData(SWT.FILL, SWT.CENTER,
                false, false);
        descriptionData.exclude = true;
        description.setText(Messages.PendingCombo_Description);
        description.setLayoutData(descriptionData);
        description.setVisible(false);

//        IP4Connection connection = null;
//        if (changes.length > 0) {
//            connection = changes[0].getConnection();
//            this.pendingId = changes[0].getId();
//        }
//
        comment = new DescriptionViewer(resource.getConnection());
        if (initialDescription != null) {
            pendingComment = initialDescription;
        }
        comment.createControl(changelist, pendingComment);
        comment.getDocument().addDocumentListener(new IDocumentListener() {

            public void documentChanged(DocumentEvent event) {
                pendingComment = comment.getDocument().get();
                validate();
            }

            public void documentAboutToBeChanged(DocumentEvent event) {

            }
        });
        final StyledText styledText = comment.getViewer().getTextWidget();
        final GridData commentData = (GridData) styledText.getLayoutData();
        commentData.heightHint = P4UIUtils.computePixelHeight(comment
                .getViewer().getTextWidget().getFont(), 5);
        commentData.exclude = true;
        styledText.setLayoutData(commentData);
        styledText.setVisible(false);

//        int select = 1;
//        boolean activeSet = false;
//        for (IP4PendingChangelist list : changes) {
//            combo.add(getText(list));
//            if (list.isActive()) {
//                select = combo.getItemCount() - 1;
//                this.pendingId = list.getId();
//                activeSet = true;
//            } else if (!activeSet && selectedId == list.getId()) {
//                select = combo.getItemCount() - 1;
//                this.pendingId = list.getId();
//            }
//        }
//        combo.select(select);
//
        combo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = combo.getSelectionIndex();
                boolean layout = false;
                if (index > 0) {
                    index--;
                    pendingId = changes[index].getId();
                    layout = styledText.isVisible();
                    commentData.exclude = true;
                    descriptionData.exclude = true;
                    styledText.setVisible(false);
                    description.setVisible(false);
                } else if (index == 0) {
                    pendingId = IP4PendingChangelist.NEW;
                    layout = !styledText.isVisible();
                    commentData.exclude = false;
                    descriptionData.exclude = false;
                    styledText.setVisible(true);
                    description.setVisible(true);
                }
                if (layout) {
                    parent.layout(true, true);
                    if (resize) {
                        Shell shell = parent.getShell();
                        Point size = shell.getSize();
                        Point newSize = shell.computeSize(SWT.DEFAULT,
                                SWT.DEFAULT, true);
                        size.y = newSize.y;
                        shell.setSize(size);
                    }
                }
                if (styledText.isVisible()) {
                    styledText.setFocus();
                }
                validate();
            }

        });
        
        initControl();
    }

    /*
     * @ali This separate the long time operation from UI thread.
     */
    private void initControl() {
        combo.setEnabled(false);
        P4Runner.schedule(new P4Runnable() {

            @Override
            public String getTitle() {
                return "Initialize pending combo...";
            }

            @Override
            public void run(IProgressMonitor monitor) {
                changes = resource.getConnection().getCachedPendingChangelists();
                Arrays.sort(changes, new Comparator<IP4PendingChangelist>() {

                    public int compare(IP4PendingChangelist o1, IP4PendingChangelist o2) {
                        return o1.getId() - o2.getId();
                    }
                });
                
                if (changes.length > 0) {
                    PendingCombo.this.pendingId = changes[0].getId();
                }
                
                PerforceUIPlugin.syncExec(new Runnable() {

                    public void run() {
                        if (!combo.isDisposed()) {
                            int select = 1;
                            boolean activeSet = false;
                            for (IP4PendingChangelist list : changes) {
                                combo.add(getText(list));
                                if (list.isActive()) {
                                    select = combo.getItemCount() - 1;
                                    PendingCombo.this.pendingId = list.getId();
                                    activeSet = true;
                                } else if (!activeSet && initialSelectedChangeListId == list.getId()) {
                                    select = combo.getItemCount() - 1;
                                    PendingCombo.this.pendingId = list.getId();
                                }
                            }
                            combo.setEnabled(true);
                            combo.select(select);
                            validate();
                        }
                    }
                });
            }

        });

    }

    private String getText(IP4PendingChangelist change) {
        if (change.getId() == 0) {
            return Messages.PendingCombo_Default;
        } else {
            StringBuilder desc = new StringBuilder();
            desc.append(Messages.PendingCombo_Change);
            desc.append(change.getId());

            String description = change.getShortDescription();
            if (description.length() > 0) {
                desc.append(' ');
                desc.append('{');
                desc.append(' ');
                desc.append(description);
                desc.append(' ');
                desc.append('}');
                desc.append(' ');
            }
            return desc.toString();
        }
    }

    /**
     * Get combo widget
     * 
     * @return - combo
     */
    public Combo getCombo() {
        return this.combo;
    }

    /**
     * Get id of selected pending changelist
     * 
     * @return - pending changelist id
     */
    public int getSelected() {
        return this.pendingId;
    }

    /**
     * Get initial comment of pending changelist
     * 
     * @return - pending changelist description
     */
    public String getDescription() {
        return this.pendingComment;
    }

    /**
     * @see com.perforce.team.ui.BaseErrorProvider#validate()
     */
    @Override
    public void validate() {
        if (!this.combo.isEnabled() 
                ||(this.combo.getSelectionIndex() == 0 && pendingComment.length() == 0)) {
            this.errorMessage = Messages.PendingCombo_EnterChangelistDescription;
        } else {
            this.errorMessage = null;
        }
        super.validate();
    }

}
