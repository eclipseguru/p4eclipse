/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.preferences;

import com.perforce.p4java.core.IJobSpec.IJobSpecField;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.mylyn.editor.JobField;
import com.perforce.team.ui.mylyn.editor.JobFieldGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class JobLayoutPreferencePage extends PreferencePage implements
        IWorkbenchPreferencePage {

    private static class ConnectionRule implements ISchedulingRule {

        private IP4Connection connection;

        public ConnectionRule(IP4Connection connection) {
            this.connection = connection;
        }

        /**
         * @see org.eclipse.core.runtime.jobs.ISchedulingRule#contains(org.eclipse.core.runtime.jobs.ISchedulingRule)
         */
        public boolean contains(ISchedulingRule rule) {
            if (rule instanceof ConnectionRule) {
                return this.connection
                        .equals(((ConnectionRule) rule).connection);
            }
            return false;
        }

        /**
         * @see org.eclipse.core.runtime.jobs.ISchedulingRule#isConflicting(org.eclipse.core.runtime.jobs.ISchedulingRule)
         */
        public boolean isConflicting(ISchedulingRule rule) {
            if (rule instanceof ConnectionRule) {
                return this.connection
                        .equals(((ConnectionRule) rule).connection);
            }
            return false;
        }

    }

    private IP4Connection[] connections = null;
    private IP4Connection selected = null;
    private Map<IP4Connection, List<JobFieldGroup>> fields = new HashMap<IP4Connection, List<JobFieldGroup>>();
    private Composite displayArea;
    private TreeViewer specViewer = null;
    private Combo connectionCombo;
    private Object loading = new Object();

    private void setSelection(IP4Connection connection) {
        if (connection != null && P4UIUtils.okToUse(specViewer)) {
            this.selected = connection;
            specViewer.setInput(this.selected);
        }
    }

    private void loadConnection(final IP4Connection connection) {
        specViewer.setInput(loading);
        P4Runner.schedule(new P4Runnable() {

            @Override
            public void run(IProgressMonitor monitor) {
                if (!fields.containsKey(connection)) {
                    JobFieldGroup corePage = JobFieldGroup.loadPage(connection,
                            JobFieldGroup.CORE_FIELDS);
                    JobFieldGroup advancedPage = JobFieldGroup.loadPage(
                            connection, JobFieldGroup.ADVANCED_FIELDS);
                    if (advancedPage == null) {
                        advancedPage = new JobFieldGroup(connection,
                                JobFieldGroup.ADVANCED_FIELDS);
                    }
                    final List<JobFieldGroup> pages = new ArrayList<JobFieldGroup>();
                    if (corePage != null) {
                        pages.add(corePage);
                    }
                    pages.add(advancedPage);
                    fields.put(connection, pages);
                    if (connection == selected) {
                        PerforceUIPlugin.syncExec(new Runnable() {

                            public void run() {
                                setSelection(connection);
                            }
                        });
                    }
                }
            }

        }, new ConnectionRule(connection));
    }

    private IBaseLabelProvider createLabelProvider() {
        final JobSpecLabelProvider wrapper = new JobSpecLabelProvider() {

            private Image loadingImage = PerforceUIPlugin.getDescriptor(
                    IPerforceUIConstants.IMG_LOADING).createImage();

            @Override
            public void dispose() {
                super.dispose();
                loadingImage.dispose();
            }

            @Override
            public Image getImage(Object element) {
                if (element == loading) {
                    return loadingImage;
                } else {
                    return super.getImage(element);
                }
            }

            @Override
            public String getText(Object element) {
                if (element == loading) {
                    return Messages.JobLayoutPreferencePage_Loading;
                } else {
                    return super.getText(element);
                }
            }
        };
        IStyledLabelProvider styledProvider = new IStyledLabelProvider() {

            private Font boldFont = P4UIUtils.generateBoldFont(specViewer
                    .getTree().getDisplay(), specViewer.getTree().getFont());

            public Image getImage(Object element) {
                return wrapper.getImage(element);
            }

            public StyledString getStyledText(Object element) {
                if (element instanceof JobField
                        && IP4Job.JOB_DESCRIPTION_CODE == ((JobField) element)
                                .getField().getCode()) {
                    return new StyledString(wrapper.getText(element),
                            new Styler() {

                                @Override
                                public void applyStyles(TextStyle textStyle) {
                                    textStyle.font = boldFont;
                                }
                            });
                }
                return new StyledString(wrapper.getText(element));
            }

            public void addListener(ILabelProviderListener listener) {

            }

            public void dispose() {
                wrapper.dispose();
                boldFont.dispose();
            }

            public boolean isLabelProperty(Object element, String property) {
                return false;
            }

            public void removeListener(ILabelProviderListener listener) {

            }

        };
        ILabelDecorator decorator = new ILabelDecorator() {

            public void removeListener(ILabelProviderListener listener) {

            }

            public boolean isLabelProperty(Object element, String property) {
                return false;
            }

            public void dispose() {

            }

            public void addListener(ILabelProviderListener listener) {
            }

            public String decorateText(String text, Object element) {
                if (element instanceof JobField) {
                    IJobSpecField field = ((JobField) element).getField();
                    return text + " : " + field.getDataType(); //$NON-NLS-1$
                }
                return text;
            }

            public Image decorateImage(Image image, Object element) {
                return null;
            }
        };
        return new DecoratingStyledCellLabelProvider(styledProvider, decorator,
                null);
    }

    private void createSpecViewer(Composite parent, int numColumns) {
        Label viewerLabel = new Label(parent, SWT.WRAP);
        viewerLabel.setText(Messages.JobLayoutPreferencePage_DragAndDropFields);
        GridData vlData = new GridData(SWT.FILL, SWT.FILL, true, false);
        vlData.horizontalSpan = numColumns;
        viewerLabel.setLayoutData(vlData);

        specViewer = new TreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL
                | SWT.BORDER | SWT.MULTI);
        specViewer.setLabelProvider(createLabelProvider());
        specViewer.setContentProvider(new ITreeContentProvider() {

            public void inputChanged(Viewer viewer, Object oldInput,
                    Object newInput) {

            }

            public void dispose() {

            }

            public Object[] getElements(Object inputElement) {
                return getChildren(inputElement);
            }

            public boolean hasChildren(Object element) {
                return element instanceof IP4Connection
                        || element instanceof JobFieldGroup;
            }

            public Object getParent(Object element) {
                return null;
            }

            public Object[] getChildren(Object parentElement) {
                Object[] children = null;
                if (parentElement instanceof IP4Connection) {
                    children = fields.get(parentElement).toArray();
                } else if (parentElement instanceof JobFieldGroup) {
                    children = ((JobFieldGroup) parentElement).getFields();
                } else if (parentElement == loading) {
                    children = new Object[] { parentElement };
                }
                if (children == null) {
                    children = new Object[0];
                }
                return children;
            }
        });

        GridData svData = new GridData(SWT.FILL, SWT.FILL, true, true);
        svData.horizontalSpan = numColumns;
        specViewer.getTree().setLayoutData(svData);
        specViewer.addDragSupport(DND.DROP_COPY | DND.DROP_DEFAULT
                | DND.DROP_MOVE,
                new Transfer[] { LocalSelectionTransfer.getTransfer() },
                new DragSourceAdapter() {

                    private IStructuredSelection selection = null;

                    @Override
                    public void dragFinished(DragSourceEvent event) {
                        selection = null;
                    }

                    @Override
                    public void dragStart(DragSourceEvent event) {
                        selection = (IStructuredSelection) specViewer
                                .getSelection();
                        for (Object element : selection.toArray()) {
                            if (element instanceof JobFieldGroup
                                    || (element instanceof JobField && IP4Job.JOB_DESCRIPTION_CODE == ((JobField) element)
                                            .getField().getCode())) {
                                event.doit = false;
                                selection = null;
                                break;
                            }
                        }
                    }

                    @Override
                    public void dragSetData(DragSourceEvent event) {
                        if (this.selection != null
                                && LocalSelectionTransfer.getTransfer()
                                        .isSupportedType(event.dataType)) {
                            LocalSelectionTransfer.getTransfer().setSelection(
                                    this.selection);
                            event.data = this.selection;
                        }
                    }

                });
        specViewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
        specViewer.addDropSupport(DND.DROP_COPY | DND.DROP_DEFAULT
                | DND.DROP_MOVE,
                new Transfer[] { LocalSelectionTransfer.getTransfer() },
                new DropTargetAdapter() {

                    @Override
                    public void drop(DropTargetEvent event) {
                        if (event.item != null
                                && event.data instanceof IStructuredSelection) {
                            JobFieldGroup page = null;
                            int index = 0;
                            if (event.item.getData() instanceof JobField) {
                                JobField field = (JobField) event.item
                                        .getData();
                                page = field.getParent();
                                index = page.indexOf(field);
                            } else if (event.item.getData() instanceof JobFieldGroup) {
                                page = (JobFieldGroup) event.item.getData();
                                index = 0;
                            }
                            if (page != null) {
                                Object[] elements = ((IStructuredSelection) event.data)
                                        .toArray();
                                List<JobField> fields = new ArrayList<JobField>(
                                        elements.length);
                                for (Object element : elements) {
                                    if (element instanceof JobField) {
                                        fields.add((JobField) element);
                                    }
                                }
                                page.move(fields.toArray(new JobField[fields
                                        .size()]), index);
                            }
                            specViewer.refresh();
                        }
                    }

                    @Override
                    public void dragOver(DropTargetEvent event) {
                        if (event.item != null) {
                            if (event.item.getData() instanceof JobField) {
                                event.feedback = DND.FEEDBACK_INSERT_BEFORE
                                        | DND.FEEDBACK_SCROLL
                                        | DND.FEEDBACK_EXPAND;
                            } else if (event.item.getData() instanceof JobFieldGroup) {
                                event.feedback = DND.FEEDBACK_SELECT
                                        | DND.FEEDBACK_SCROLL
                                        | DND.FEEDBACK_EXPAND;
                            }
                        }
                    }

                });
    }

    /**
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        displayArea = new Composite(parent, SWT.NONE);
        displayArea.setLayout(new GridLayout(2, false));
        displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Label connectionsLabel = new Label(displayArea, SWT.NONE);
        connectionsLabel.setText(Messages.JobLayoutPreferencePage_Connections);

        connectionCombo = new Combo(displayArea, SWT.READ_ONLY | SWT.DROP_DOWN);

        this.connections = P4ConnectionManager.getManager().getConnections();
        for (IP4Connection connection : this.connections) {
            connectionCombo.add(connection.toString());
        }
        connectionCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                false));
        connectionCombo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = connectionCombo.getSelectionIndex();
                if (index >= 0 && index < connections.length) {
                    selected = connections[index];
                    if (!fields.containsKey(selected)) {
                        specViewer.setInput(loading);
                        loadConnection(selected);
                    } else {
                        setSelection(connections[index]);
                    }
                }
            }

        });

        createSpecViewer(this.displayArea, 2);
        return displayArea;
    }

    /**
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        super.performDefaults();
        IP4Connection connection = this.selected;
        if (connection != null) {
            if (fields.containsKey(connection)) {
                JobFieldGroup corePage = JobFieldGroup.loadDefaultPages(
                        connection, JobFieldGroup.CORE_FIELDS);
                JobFieldGroup advancedPage = new JobFieldGroup(connection,
                        JobFieldGroup.ADVANCED_FIELDS);
                final List<JobFieldGroup> pages = new ArrayList<JobFieldGroup>();
                if (corePage != null) {
                    corePage.setIndex(pages.size());
                    pages.add(corePage);
                }
                advancedPage.setIndex(pages.size());
                pages.add(advancedPage);
                fields.put(connection, pages);
                setSelection(connection);
            }
        }
    }

    /**
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        boolean ok = super.performOk();
        if (selected != null) {
            List<JobFieldGroup> pages = this.fields.get(this.selected);
            JobFieldGroup.savePages(this.selected,
                    pages.toArray(new JobFieldGroup[pages.size()]));
        }
        return ok;
    }

    /**
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench) {

    }

}
