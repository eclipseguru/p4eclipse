/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.history;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.ui.history.HistoryPage;
import org.eclipse.team.ui.history.IHistoryPageSource;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.progress.UIJob;

import com.perforce.p4java.core.file.FileAction;
import com.perforce.p4java.core.file.IRevisionIntegrationData;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.p4java.ILocalRevision;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Listener;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.core.p4java.P4Revision;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.dialogs.IHelpContextIds;
import com.perforce.team.ui.editor.CompareUtils;
import com.perforce.team.ui.editor.RevisionEditorInput;
import com.perforce.team.ui.p4java.actions.SyncRevisionAction;
import com.perforce.team.ui.preferences.IPreferenceConstants;
import com.perforce.team.ui.views.HistoryDropAdapter;
import com.perforce.team.ui.views.SessionManager;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class P4HistoryPage extends HistoryPage {

    /**
     * COLUMN_SIZES - preference key for column sizes
     */
    public static final String COLUMN_SIZES = "com.perforce.team.ui.historycolumns"; //$NON-NLS-1$

    /**
     * DISPLAY_BRANCHING_HISTORY
     */
    public static final String DISPLAY_BRANCHING_HISTORY = "com.perforce.team.ui.history.display_branching_history"; //$NON-NLS-1$

    /**
     * GROUP_REVISIONS
     */
    public static final String GROUP_REVISIONS = "com.perforce.team.ui.history.GROUP_REVISIONS"; //$NON-NLS-1$

    /**
     * COMPARE_MODE
     */
    public static final String COMPARE_MODE = "com.perforce.team.ui.history.COMPARE_MODE"; //$NON-NLS-1$

    /**
     * SHOW_SEARCH
     */
    public static final String SHOW_SEARCH = "com.perforce.team.ui.history.SHOW_SEARCH"; //$NON-NLS-1$

    /**
     * WRAP_TEXT
     */
    public static final String WRAP_TEXT = "com.perforce.team.ui.history.WRAP_TEXT"; //$NON-NLS-1$

    /**
     * SHOW_COMMENTS
     */
    public static final String SHOW_COMMENTS = "com.perforce.team.ui.history.SHOW_COMMENTS"; //$NON-NLS-1$

    /**
     * LOCAL_MODE
     */
    public static final String LOCAL_MODE = "LOCAL_MODE"; //$NON-NLS-1$

    /**
     * REMOTE_MODE
     */
    public static final String REMOTE_MODE = "REMOTE_MODE"; //$NON-NLS-1$

    /**
     * ALL_MODE
     */
    public static final String ALL_MODE = "ALL_MODE"; //$NON-NLS-1$

    /**
     * REVISION_COLUMN
     */
    public static final String REVISION_COLUMN = Messages.P4HistoryPage_Revision;

    /**
     * FILENAME_COLUMN
     */
    public static final String FILENAME_COLUMN = Messages.P4HistoryPage_FileName;

    /**
     * CHANGELIST_COLUMN
     */
    public static final String CHANGELIST_COLUMN = Messages.P4HistoryPage_Changelist;

    /**
     * DATE_COLUMN
     */
    public static final String DATE_COLUMN = Messages.P4HistoryPage_Date;

    /**
     * USER_COLUMN
     */
    public static final String USER_COLUMN = Messages.P4HistoryPage_User;

    /**
     * ACTION_COLUMN
     */
    public static final String ACTION_COLUMN = Messages.P4HistoryPage_Action;

    /**
     * DESCRIPTION_COLUMN
     */
    public static final String DESCRIPTION_COLUMN = Messages.P4HistoryPage_Description;

    private IP4File file = null;

    // Viewer for revision list
    private SashForm sash;
    private Composite composite;

    private Composite searchArea;
    private Text searchText;
    private String searchString = null;

    private TreeViewer treeViewer;
    private HistorySorter sorter;

    // For change description
    private SourceViewer textViewer;
    private IP4File currentFile;
    private IP4Revision latestRev;
    private IP4Listener p4Listener = new IP4Listener() {

        public void resoureChanged(P4Event event) {
            if (EventType.REFRESHED == event.getType()
                    && event.contains(currentFile)) {
                UIJob refreshJob = new UIJob(
                        Messages.P4HistoryPage_RefreshingRevisionHistory) {

                    @Override
                    public IStatus runInUIThread(IProgressMonitor monitor) {
                        if (okToUse()) {
                            treeViewer.refresh();
                        }
                        return Status.OK_STATUS;
                    }
                };
                refreshJob.schedule();
            }
        }
		public String getName() {
			return P4HistoryPage.this.getClass().getSimpleName();
		}
    };

    // Action to switch branching on/off
    private Action branchAction;

    // Action to compare 2 revisions
    private Action compareAction;

    // Action to group by date
    private Action groupAction;

    // Treat double-click as a diff against have
    private Action modeAction;

    // Hide/show the search field
    private Action toggleSearchAction;

    // Wrap text in changelist description
    private Action wrapTextAction;

    // Show local revisions only
    private Action showLocalAction;

    // Show remote revisions only
    private Action showRemoteAction;

    // Show all revisions
    private Action showAllAction;

    // Show changelist description area
    private Action showComments;

    // Open a revision in a read-only editor
    private Action openRevisionAction;

    // Sync to the selected revision
    private Action syncRevisionAction;

    private final Object loading = new Object();
    private boolean isLoading = false;

    /**
     * Handles displaying full changelist description when revision is selected
     */
    private class TableSelectionChangedListener implements
            ISelectionChangedListener {

        /**
         * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
         */
        public void selectionChanged(SelectionChangedEvent event) {
            IStructuredSelection selection = (IStructuredSelection) treeViewer
                    .getSelection();
            if (selection.getFirstElement() instanceof IFileRevision) {
                IFileRevision entry = (IFileRevision) selection
                        .getFirstElement();
                String desc = entry.getComment();
                if (desc != null) {
                    textViewer.setDocument(new Document(desc));
                    return;
                }
            }

            // If not set by non null comment then clear
            textViewer.setDocument(new Document("")); //$NON-NLS-1$
            return;
        }
    }

    /**
     * Revision group
     */
    private static class RevisionGroup {

        Date date;
        String label;

    }

    /**
     * Label provider for the revision history list
     */
    private class HistoryLabelProvider extends LabelProvider implements
            ITableLabelProvider, IFontProvider {

        private Image groupImage = null;

        /**
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
         *      int)
         */
        public Image getColumnImage(Object element, int columnIndex) {
            if (element instanceof RevisionGroup && columnIndex == 0) {
                if (groupImage == null) {
                    groupImage = PerforceUIPlugin.getPlugin()
                            .getImageDescriptor(IPerforceUIConstants.IMG_DATES)
                            .createImage();
                }
                return groupImage;
            }
            return null;
        }

        /**
         * @see org.eclipse.jface.viewers.BaseLabelProvider#dispose()
         */
        @Override
        public void dispose() {
            super.dispose();
            if (groupImage != null) {
                groupImage.dispose();
            }
        }

        /**
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
         *      int)
         */
        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof IP4Revision) {
                IP4Revision revision = (IP4Revision) element;
                switch (columnIndex) {
                case 0:
                    StringBuilder builder = new StringBuilder();
                    if (isHaveRevision(revision)) {
                        builder.append('*');
                    }
                    builder.append(revision.getRevision());
                    return builder.toString();
                case 1:
                    return revision.getRemotePath();
                case 2:
                    return getIntText(revision.getChangelist());
                case 3:
                    return getDateText(revision.getTimestamp());
                case 4:
                    return revision.getAuthor();
                case 5:
                    return getActionText(revision);
                case 6:
                    return convert(revision.getComment());
                default:
                    return ""; //$NON-NLS-1$
                }
            } else if (element instanceof IFileRevision) {
                IFileRevision revision = (IFileRevision) element;
                switch (columnIndex) {
                case 1:
                    return revision.getURI().getPath();
                case 3:
                    return getDateText(revision.getTimestamp());
                case 6:
                    if (revision instanceof ILocalRevision
                            && ((ILocalRevision) revision).isCurrent()) {
                        return Messages.P4HistoryPage_LatestWorkspaceRevision;
                    }
                default:
                    return ""; //$NON-NLS-1$
                }
            } else if (element instanceof RevisionGroup) {
                if (columnIndex == 0) {
                    return ((RevisionGroup) element).label;
                }
            }
            return ""; //$NON-NLS-1$
        }

        /**
         * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
         */
        public Font getFont(Object element) {
            if (element instanceof RevisionGroup) {
                return JFaceResources.getFontRegistry().getBold(
                        JFaceResources.DEFAULT_FONT);
            } else if (element instanceof IP4Revision) {
                IP4Revision data = (IP4Revision) element;
                if (isHaveRevision(data)) {
                    return JFaceResources.getFontRegistry().getBold(
                            JFaceResources.DEFAULT_FONT);
                }
            } else if (element instanceof ILocalRevision) {
                if (((ILocalRevision) element).isCurrent()) {
                    return JFaceResources.getFontRegistry().getBold(
                            JFaceResources.DEFAULT_FONT);
                }
            }
            return null;
        }
    }

    private static String getActionText(IP4Revision data) {
        StringBuilder actionText = new StringBuilder();

        // Fix for job036698, files of +S<n> type may be missing action
        // information so add null checks for file and integ actions
        FileAction fileAction = data.getAction();
        if (fileAction != null) {
            actionText.append(fileAction.toString().toLowerCase());
        }

        IRevisionIntegrationData[] integActions = data.getIntegrationData();
        if (integActions != null) {
            for (IRevisionIntegrationData integData : integActions) {
                if (integData != null) {
                    if (actionText.length() > 0) {
                        actionText.append(";"); //$NON-NLS-1$
                    }
                    actionText.append(" " + integData.getHowFrom() + " " //$NON-NLS-1$ //$NON-NLS-2$
                            + integData.getFromFile());
                    int end = integData.getEndFromRev();
                    if (end > -1) {
                        actionText.append("#" + end); //$NON-NLS-1$
                    }
                }
            }
        }
        return convert(actionText.toString());
    }

    private static String convert(String unconverted) {
        return P4CoreUtils.removeWhitespace(unconverted);
    }

    private static String getDateText(long timestamp) {
        return DateFormat.getDateTimeInstance().format(new Date(timestamp));
    }

    private static String getIntText(int integer) {
        return Integer.toString(integer);
    }

    /**
     * History content provider
     */
    private class HistoryContentProvider implements ITreeContentProvider {

        private IFileRevision[] history = null;

        private RevisionGroup thisMonth = null;
        private RevisionGroup beforeThisMonth = null;

        HistoryContentProvider() {
            Calendar calendar = new GregorianCalendar();
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            thisMonth = new RevisionGroup();
            thisMonth.date = calendar.getTime();
            thisMonth.label = Messages.P4HistoryPage_ThisMonth;

            beforeThisMonth = new RevisionGroup();
            beforeThisMonth.date = new Date(thisMonth.date.getTime() - 1000l);
            beforeThisMonth.label = Messages.P4HistoryPage_OlderThanThisMonth;
        }

        /**
         * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
         */
        public Object[] getChildren(Object parentElement) {
            if (thisMonth == parentElement) {
                List<IFileRevision> thisMonths = new ArrayList<IFileRevision>();
                for (IFileRevision data : history) {
                    if (beforeThisMonth.date.before(new Date(data
                            .getTimestamp()))) {
                        thisMonths.add(data);
                    }
                }
                return thisMonths.toArray(new IFileRevision[thisMonths.size()]);
            } else if (beforeThisMonth == parentElement) {
                List<IFileRevision> thisMonths = new ArrayList<IFileRevision>();
                for (IFileRevision data : history) {
                    if (!beforeThisMonth.date.before(new Date(data
                            .getTimestamp()))) {
                        thisMonths.add(data);
                    }
                }
                return thisMonths.toArray(new IFileRevision[thisMonths.size()]);
            }
            return new Object[0];
        }

        /**
         * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
         */
        public Object getParent(Object element) {
            if (element instanceof IFileRevision) {
                IFileRevision data = (IFileRevision) element;
                if (beforeThisMonth.date.before(new Date(data.getTimestamp()))) {
                    return thisMonth;
                } else {
                    return beforeThisMonth;
                }
            }
            return null;
        }

        /**
         * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
         */
        public boolean hasChildren(Object element) {
            return element == thisMonth || element == beforeThisMonth;
        }

        /**
         * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
         */
        public Object[] getElements(Object inputElement) {
            if (groupAction.isChecked()) {
                return new Object[] { thisMonth, beforeThisMonth };
            } else {
                return history;
            }
        }

        /**
         * @see org.eclipse.jface.viewers.IContentProvider#dispose()
         */
        public void dispose() {

        }

        /**
         * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
         *      java.lang.Object, java.lang.Object)
         */
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            if (newInput instanceof IFileRevision[]) {
                this.history = (IFileRevision[]) newInput;
            } else {
                this.history = new IFileRevision[0];
            }
        }

    }

    /**
     * Create a new p4 history page
     * 
     * @param input
     */
    public P4HistoryPage(Object input) {
        setInput(input);
    }

    private boolean isHaveRevision(IP4Revision data) {
        return data.getRevision() == file.getHaveRevision()
                && data.getRemotePath().equals(file.getRemotePath());
    }

    private Map<String, Integer> loadColumnSizes() {
        return SessionManager.loadColumnSizes(COLUMN_SIZES);
    }

    /**
     * Save the current column sizes
     */
    private void saveColumnSizes() {
        SessionManager
                .saveColumnPreferences(treeViewer.getTree(), COLUMN_SIZES);
    }

    /**
     * Get the tree viewer
     * 
     * @return - tree viewer
     */
    public Viewer getViewer() {
        return treeViewer;
    }

    /**
     * Is branching being displayed (checks the preference in the store)
     * 
     * @return - true is displaying branching history
     */
    public boolean isBranchingDisplayed() {
        return PerforceUIPlugin.getPlugin().getPreferenceStore()
                .getBoolean(DISPLAY_BRANCHING_HISTORY);
    }

    private void openEditor(IP4Revision data) {
        RevisionEditorInput input = new RevisionEditorInput(data);
        P4UIUtils.openEditor(input);
    }

    /**
     * Shows the history from a p4 file
     * 
     * @param file
     */
    private void showHistory(final IP4File file) {
        if (!okToUse()) {
            return;
        }
        this.isLoading = true;
        this.currentFile = file;
        final String name = file.getName();
        final String loading = NLS.bind(
                Messages.P4HistoryPage_LoadingHistoryFor, name);
        compareAction.setEnabled(false);
        treeViewer.setInput(this.loading);

        P4Runner.schedule(new P4Runnable() {

            @Override
            public String getTitle() {
                return loading;
            }

            @Override
            public void run(IProgressMonitor monitor) {
                monitor.beginTask(getTitle(), 2);
                final IFileRevision[] history = file.getCompleteHistory(
                        isBranchingDisplayed(), monitor);
                final IP4Revision[] latestRevision = new IP4Revision[] { null };
                for (IFileRevision rev : history) {
                    if (rev instanceof IP4Revision) {
                        if (latestRevision[0] != null) {
                            IP4Revision p4Rev = (IP4Revision) rev;
                            if (p4Rev.getChangelist() > latestRevision[0]
                                    .getChangelist()) {
                                latestRevision[0] = (IP4Revision) rev;
                            }
                        } else {
                            latestRevision[0] = (IP4Revision) rev;
                        }
                    }
                }
                monitor.done();
                PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

                    public void run() {
                        if (currentFile == file
                                && P4UIUtils.okToUse(treeViewer)) {
                            treeViewer.setInput(history);
                            latestRev = latestRevision[0];
                            isLoading = false;
                        }
                    }

                });
            }
        });
    }

    /**
     * Create the table showing the revision history
     */
    private Tree createTable(Composite parent) {
        Tree table = new Tree(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI
                | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableLayout layout = new TableLayout();
        table.setLayout(layout);

        treeViewer = new TreeViewer(table);
        treeViewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
        treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                if (event.getSelection() instanceof IStructuredSelection) {
                    compareAction.setEnabled(((IStructuredSelection) event
                            .getSelection()).size() == 2);
                }
            }

        });
        treeViewer.addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {
                IStructuredSelection selection = (IStructuredSelection) treeViewer
                        .getSelection();
                if (modeAction.isChecked()) {
                    if (selection.size() == 2) {
                        compareSelection(true);
                    } else if (selection.size() == 1) {
                        Object element = selection.getFirstElement();
                        if (element instanceof IFileRevision) {
                            IFileRevision data = (IFileRevision) element;

                            // Perform local compare when have rev is greater
                            // than zero else perform remote compare against
                            // latest revision
                            if (file.getHaveRevision() > 0) {
                                CompareUtils.openLocalCompare(file, data);
                            } else if (latestRev != null) {
                                CompareUtils.openCompare(latestRev, data);
                            }
                        }
                    }
                } else {
                    for (Object element : selection.toArray()) {
                        if (element instanceof IP4Revision) {
                            openEditor((IP4Revision) element);
                        }
                    }
                }
            }
        });
        treeViewer.getTree().addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                saveColumnSizes();
            }

        });
        treeViewer.addFilter(new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement,
                    Object element) {
                if (element instanceof RevisionGroup) {
                    return true;
                } else if (element instanceof IFileRevision) {
                    if (!filterByMode((IFileRevision) element)) {
                        return false;
                    } else if (searchString != null
                            && element instanceof IP4Revision) {
                        IP4Revision data = (IP4Revision) element;
                        if (getIntText(data.getRevision())
                                .indexOf(searchString) != -1) {
                            return true;
                        } else if (data.getRemotePath().indexOf(searchString) != -1) {
                            return true;
                        } else if (getIntText(data.getChangelist()).indexOf(
                                searchString) != -1) {
                            return true;
                        } else if (getDateText(data.getTimestamp()).indexOf(
                                searchString) != -1) {
                            return true;
                        } else if (data.getAuthor().indexOf(searchString) != -1) {
                            return true;
                        } else if (getActionText(data).indexOf(searchString) != -1) {
                            return true;
                        } else if (convert(data.getComment()).indexOf(
                                searchString) != -1) {
                            return true;
                        }
                        return false;
                    }
                }
                return true;
            }
        });

        SelectionListener headerListener = new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                // column selected - need to sort
                TreeColumn column = (TreeColumn) e.widget;
                sorter.setSortColumn(column.getText());
                treeViewer.refresh();
            }
        };

        // Add all columns to table
        addColumn(treeViewer, REVISION_COLUMN);
        addColumn(treeViewer, FILENAME_COLUMN);
        addColumn(treeViewer, CHANGELIST_COLUMN);
        TreeColumn dateColumn = addColumn(treeViewer, DATE_COLUMN);
        addColumn(treeViewer, USER_COLUMN);
        addColumn(treeViewer, ACTION_COLUMN);
        addColumn(treeViewer, DESCRIPTION_COLUMN);

        for (TreeColumn column : treeViewer.getTree().getColumns()) {
            column.addSelectionListener(headerListener);
        }

        table.setSortColumn(dateColumn);
        table.setSortDirection(SWT.DOWN);

        Map<String, Integer> columnSizes = loadColumnSizes();

        for (TreeColumn column : treeViewer.getTree().getColumns()) {
            int width = 100;
            if (columnSizes.containsKey(column.getText())) {
                int size = columnSizes.get(column.getText()).intValue();
                if (size > 0) {
                    width = size;
                }
            }
            layout.addColumnData(new ColumnPixelData(width, true));
        }

        treeViewer.setContentProvider(new HistoryContentProvider());
        treeViewer.setLabelProvider(new HistoryLabelProvider());
        treeViewer.addDragSupport(DND.DROP_COPY | DND.DROP_LINK | DND.DROP_MOVE
                | DND.DROP_DEFAULT,
                new Transfer[] { LocalSelectionTransfer.getTransfer() },
                new HistoryDragAdapter(treeViewer));
        treeViewer.addDropSupport(
                DND.DROP_COPY | DND.DROP_LINK | DND.DROP_MOVE
                        | DND.DROP_DEFAULT,
                new Transfer[] { ResourceTransfer.getInstance(),
                        FileTransfer.getInstance(),
                        LocalSelectionTransfer.getTransfer(), },
                new HistoryDropAdapter(this));

        sorter = new HistorySorter(treeViewer.getTree(), DATE_COLUMN) {

            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
                if (e1 instanceof RevisionGroup && e2 instanceof RevisionGroup) {
                    return ((RevisionGroup) e1).date
                            .compareTo(((RevisionGroup) e2).date) * -1;
                }
                return super.compare(viewer, e1, e2);
            }

        };
        treeViewer.setSorter(sorter);

        return table;
    }

    /**
     * Add another column to the table
     */
    private TreeColumn addColumn(TreeViewer viewer, String title) {
        TreeColumn col = new TreeColumn(viewer.getTree(), SWT.NONE);
        col.setResizable(true);
        col.setText(title);
        return col;
    }

    /**
     * Is this view's main control not disposed
     * 
     * @return - true if not disposed
     */
    public boolean okToUse() {
        return treeViewer != null && treeViewer.getTree() != null
                && !treeViewer.getTree().isDisposed();
    }

    /**
     * Compare two revisions
     * 
     * @param revision1
     * @param revision2
     */
    public void compare(IFileRevision revision1, IFileRevision revision2) {
        if (revision1 == null || revision2 == null) {
            return;
        }
        boolean swap = false;

        // Swap if revision2 is local and current
        if (revision2 instanceof ILocalRevision
                && ((ILocalRevision) revision2).isCurrent()) {
            swap = true;
        }

        if (revision1 instanceof IP4Revision
                && revision2 instanceof ILocalRevision) {
            // Swap if revision2 is local and revision1 is remote
            swap = true;
        } else if ((revision1 instanceof IP4Revision && revision2 instanceof IP4Revision)
                || (revision1 instanceof ILocalRevision && revision2 instanceof ILocalRevision)) {
            // Swap if revision2 is earlier than revision1
            swap = revision2.getTimestamp() > revision1.getTimestamp();
        }

        if (swap) {
            CompareUtils.openCompare(revision2, revision1);
        } else {
            CompareUtils.openCompare(revision1, revision2);
        }

    }

    private void createModeActions(IToolBarManager toolbar) {
        PerforceUIPlugin plugin = PerforceUIPlugin.getPlugin();

        String mode = plugin.getPreferenceStore().getString(
                IPreferenceConstants.HISTORY_SHOW_MODE);

        showAllAction = new Action("", Action.AS_RADIO_BUTTON) { //$NON-NLS-1$

            @Override
            public void run() {
                PerforceUIPlugin
                        .getPlugin()
                        .getPreferenceStore()
                        .setValue(IPreferenceConstants.HISTORY_SHOW_MODE,
                                ALL_MODE);
                treeViewer.refresh();
            }

        };
        showAllAction.setImageDescriptor(plugin
                .getImageDescriptor(IPerforceUIConstants.IMG_ALL_MODE));

        showLocalAction = new Action("", Action.AS_RADIO_BUTTON) { //$NON-NLS-1$

            @Override
            public void run() {
                PerforceUIPlugin
                        .getPlugin()
                        .getPreferenceStore()
                        .setValue(IPreferenceConstants.HISTORY_SHOW_MODE,
                                LOCAL_MODE);
                treeViewer.refresh();
            }
        };
        showLocalAction.setImageDescriptor(plugin
                .getImageDescriptor(IPerforceUIConstants.IMG_LOCAL_MODE));

        showRemoteAction = new Action("", Action.AS_RADIO_BUTTON) { //$NON-NLS-1$

            @Override
            public void run() {
                PerforceUIPlugin
                        .getPlugin()
                        .getPreferenceStore()
                        .setValue(IPreferenceConstants.HISTORY_SHOW_MODE,
                                REMOTE_MODE);
                treeViewer.refresh();
            }
        };
        showRemoteAction.setImageDescriptor(plugin
                .getImageDescriptor(IPerforceUIConstants.IMG_REMOTE_MODE));

        if (mode.equals(LOCAL_MODE)) {
            showLocalAction.setChecked(true);
        } else if (mode.equals(ALL_MODE)) {
            showAllAction.setChecked(true);
        } else {
            showRemoteAction.setChecked(true);
        }

        toolbar.add(showAllAction);
        toolbar.add(showLocalAction);
        toolbar.add(showRemoteAction);
    }

    private void createPulldownActions(IMenuManager pulldown) {
        PerforceUIPlugin plugin = PerforceUIPlugin.getPlugin();

        branchAction = new Action(
                Messages.P4HistoryPage_DisplayBranchingHistory) {

            @Override
            public int getStyle() {
                return IAction.AS_CHECK_BOX;
            }
        };
        branchAction.setChecked(isBranchingDisplayed());
        branchAction.addPropertyChangeListener(new IPropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent event) {
                PerforceUIPlugin
                        .getPlugin()
                        .getPreferenceStore()
                        .setValue(DISPLAY_BRANCHING_HISTORY,
                                branchAction.isChecked());
                if (currentFile != null) {
                    showHistory(currentFile);
                } else {
                    treeViewer.refresh();
                }
            }
        });

        toggleSearchAction = new Action(Messages.P4HistoryPage_ShowSearchField,
                Action.AS_CHECK_BOX) {

            @Override
            public void run() {
                boolean checked = isChecked();
                PerforceUIPlugin.getPlugin().getPreferenceStore()
                        .setValue(SHOW_SEARCH, checked);
                searchArea.setVisible(checked);
                ((GridData) searchArea.getLayoutData()).exclude = !checked;
                searchArea.getParent().layout(true, true);
                if (!checked) {
                    searchString = null;
                    treeViewer.refresh();
                }

            }
        };
        toggleSearchAction
                .setToolTipText(Messages.P4HistoryPage_ShowSearchFieldTooltip);
        toggleSearchAction.setChecked(plugin.getPreferenceStore().getBoolean(
                SHOW_SEARCH));

        wrapTextAction = new Action(
                Messages.P4HistoryPage_WrapDescriptionViewer,
                Action.AS_CHECK_BOX) {

            @Override
            public void run() {
                boolean checked = isChecked();
                PerforceUIPlugin.getPlugin().getPreferenceStore()
                        .setValue(WRAP_TEXT, checked);
                textViewer.getTextWidget().setWordWrap(checked);
            }

        };
        wrapTextAction.setChecked(plugin.getPreferenceStore().getBoolean(
                WRAP_TEXT));

        showComments = new Action(Messages.P4HistoryPage_ShowDescriptionViewer,
                Action.AS_CHECK_BOX) {

            @Override
            public void run() {
                boolean checked = isChecked();
                PerforceUIPlugin.getPlugin().getPreferenceStore()
                        .setValue(SHOW_COMMENTS, checked);
                if (checked) {
                    sash.setMaximizedControl(null);
                } else {
                    sash.setMaximizedControl(composite);
                }
            }

        };
        showComments.setChecked(plugin.getPreferenceStore().getBoolean(
                SHOW_COMMENTS));

        pulldown.add(branchAction);
        pulldown.add(toggleSearchAction);
        pulldown.add(showComments);
        pulldown.add(wrapTextAction);
        pulldown.update(false);
    }

    private boolean filterByMode(IFileRevision revision) {
        if (showAllAction.isChecked()) {
            return true;
        } else if (showLocalAction.isChecked()) {
            return revision instanceof ILocalRevision;
        } else if (showRemoteAction.isChecked()) {
            return revision instanceof P4Revision;
        } else {
            return true;
        }
    }

    /**
     * Create and add actions to the toolbar and context menu
     */
    private void createActions() {
        PerforceUIPlugin plugin = PerforceUIPlugin.getPlugin();

        groupAction = new Action("", Action.AS_CHECK_BOX) { //$NON-NLS-1$

            @Override
            public void run() {
                PerforceUIPlugin.getPlugin().getPreferenceStore()
                        .setValue(GROUP_REVISIONS, isChecked());
                treeViewer.refresh();
                treeViewer.expandAll();
            }
        };
        groupAction.setToolTipText(Messages.P4HistoryPage_GroupRevisionsByDate);
        groupAction.setImageDescriptor(PerforceUIPlugin.getPlugin()
                .getImageDescriptor(IPerforceUIConstants.IMG_DATES));
        groupAction.setChecked(plugin.getPreferenceStore().getBoolean(
                GROUP_REVISIONS));

        compareAction = new Action(Messages.P4HistoryPage_DiffTwoRevisions,
                plugin.getImageDescriptor(IPerforceUIConstants.IMG_DIFF)) {

            @Override
            public void run() {
                compareSelection(true);
            }
        };
        compareAction.setEnabled(false);

        modeAction = new Action(Messages.P4HistoryPage_CompareMode,
                Action.AS_CHECK_BOX) {

            @Override
            public void run() {
                PerforceUIPlugin.getPlugin().getPreferenceStore()
                        .setValue(COMPARE_MODE, isChecked());
            }
        };
        modeAction.setToolTipText(Messages.P4HistoryPage_CompareModeTooltip);
        modeAction.setImageDescriptor(PerforceUIPlugin.getPlugin()
                .getImageDescriptor(IPerforceUIConstants.IMG_COMPARE));
        modeAction.setChecked(plugin.getPreferenceStore().getBoolean(
                COMPARE_MODE));

        openRevisionAction = new Action(Messages.P4HistoryPage_OpenInEditor) {

            @Override
            public void run() {
                IStructuredSelection selection = (IStructuredSelection) treeViewer
                        .getSelection();
                if (selection.getFirstElement() instanceof IP4Revision) {
                    IP4Revision revision = (IP4Revision) selection
                            .getFirstElement();
                    openEditor(revision);
                }
            }
        };

        syncRevisionAction = new Action(Messages.P4HistoryPage_GetRevision,
                plugin.getImageDescriptor(IPerforceUIConstants.IMG_SYNC)) {

            @Override
            public void run() {
                IStructuredSelection selection = (IStructuredSelection) treeViewer
                        .getSelection();
                if (selection.size() == 1
                        && selection.getFirstElement() instanceof IP4Revision) {
                    IP4Revision revision = (IP4Revision) selection
                            .getFirstElement();
                    IP4File file = getFileInput();
                    if (!isBranchRevision(revision)) {
                        SyncRevisionAction action = new SyncRevisionAction();
                        action.selectionChanged(null, new StructuredSelection(
                                file));
                        action.runAction(revision.getRevision());
                    }
                }
            }
        };

        MenuManager manager = new MenuManager();
        Menu menu = manager.createContextMenu(treeViewer.getControl());
        manager.addMenuListener(new IMenuListener() {

            public void menuAboutToShow(IMenuManager manager) {
                // Only add compare action if two items selected
                IStructuredSelection selection = (IStructuredSelection) treeViewer
                        .getSelection();
                if (!selection.isEmpty()) {
                    if (selection.size() == 2) {
                        manager.add(compareAction);
                    } else if (selection.size() == 1
                            && selection.getFirstElement() instanceof IP4Revision) {
                        manager.add(openRevisionAction);
                        if (!isBranchRevision((IP4Revision) selection
                                .getFirstElement())) {
                            manager.add(syncRevisionAction);
                        }
                    }
                }
                manager.add(new Separator(
                        IWorkbenchActionConstants.MB_ADDITIONS));
            }
        });
        manager.setRemoveAllWhenShown(true);
        treeViewer.getControl().setMenu(menu);
        getSite().registerContextMenu(IWorkbenchActionConstants.MB_ADDITIONS,
                manager, treeViewer);

        // Create the local tool bar
        IActionBars bars = getSite().getActionBars();
        IToolBarManager tbm = bars.getToolBarManager();

        tbm.add(groupAction);
        tbm.add(new Separator());
        createModeActions(tbm);
        tbm.add(new Separator());
        tbm.add(modeAction);
        tbm.add(new Separator());
        tbm.add(compareAction);
        tbm.update(false);

        // Create the pulldown menu
        createPulldownActions(bars.getMenuManager());
    }

    private boolean isBranchRevision(IP4Revision revision) {
        return !getFileInput().getRemotePath().equals(revision.getRemotePath());
    }

    /**
     * Compares the currently selected revisions
     * 
     * @param async
     */
    public void compareSelection(boolean async) {
        if (!okToUse()) {
            return;
        }
        ISelection sel = treeViewer.getSelection();
        if (sel instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection) sel;
            if (selection.size() == 2) {
                Object[] revisions = selection.toArray();
                if (revisions[0] instanceof IFileRevision
                        && revisions[1] instanceof IFileRevision) {
                    IFileRevision data1 = (IFileRevision) revisions[0];
                    IFileRevision data2 = (IFileRevision) revisions[1];
                    compare(data1, data2);
                }
            }
        }
    }

    /**
     * Is this view loading?
     * 
     * @return - true if loading, false otherwise
     */
    public boolean isLoading() {
        return this.isLoading;
    }

    /**
     * @see org.eclipse.team.ui.history.HistoryPage#inputSet()
     */
    @Override
    public boolean inputSet() {
        this.file = null;
        // Load history of input
        showHistory(getFileInput());
        return true;
    }

    /**
     * Get file input for this p4 history page
     * 
     * @return - p4 file, may be null
     */
    public IP4File getFileInput() {
        if (this.file == null) {
            Object input = getInput();
            Object resource = P4CoreUtils.getResource(getInput());
            if (resource instanceof IFile) {
                IP4Resource p4File = P4ConnectionManager.getManager()
                        .getResource((IFile) resource);
                if (p4File instanceof IP4File && p4File.getConnection() != null
                        && !p4File.getConnection().isOffline()) {
                    this.file = (IP4File) p4File;
                }
            } else if (input instanceof IP4File) {
                this.file = (IP4File) input;
            } else if (input instanceof P4HistoryPageSource) {
                this.file = ((P4HistoryPageSource) input).getFile();
            } else if (input instanceof IAdaptable) {
                Object pageSource = Platform.getAdapterManager().getAdapter(
                        input, IHistoryPageSource.class);
                if (pageSource instanceof P4HistoryPageSource) {
                    this.file = ((P4HistoryPageSource) pageSource).getFile();
                }
            }
        }
        return this.file;
    }

    /**
     * @see org.eclipse.ui.part.Page#dispose()
     */
    @Override
    public void dispose() {
        P4ConnectionManager.getManager().removeListener(p4Listener);
        super.dispose();
    }

    private void createSearch(Composite parent) {
        searchArea = new Composite(parent, SWT.NONE);
        GridLayout saLayout = new GridLayout(2, false);
        saLayout.marginHeight = 0;
        saLayout.marginWidth = 0;
        searchArea.setLayout(saLayout);
        GridData saData = new GridData(SWT.FILL, SWT.FILL, true, false);
        saData.exclude = !PerforceUIPlugin.getPlugin().getPreferenceStore()
                .getBoolean(SHOW_SEARCH);
        searchArea.setLayoutData(saData);

        Label searchLabel = new Label(searchArea, SWT.LEFT);
        searchLabel.setText(Messages.P4HistoryPage_Search);

        searchText = new Text(searchArea, SWT.SINGLE | SWT.BORDER);
        searchText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        searchText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                searchString = searchText.getText().trim();
                if (searchString.length() == 0) {
                    searchString = null;
                }
                treeViewer.refresh();
            }
        });
    }

    /**
     * @see org.eclipse.ui.part.Page#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {
        P4ConnectionManager.getManager().addListener(p4Listener);
        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(parent, IHelpContextIds.HISTORY_VIEW);
        sash = new SashForm(parent, SWT.VERTICAL);

        composite = new Composite(sash, SWT.NONE);
        GridLayout gl = new GridLayout(1, true);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        gl.verticalSpacing = 0;
        composite.setLayout(gl);

        createSearch(composite);

        Tree table = createTable(composite);
        GridData gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.grabExcessVerticalSpace = true;
        gd.horizontalAlignment = GridData.FILL;
        gd.verticalAlignment = GridData.FILL;
        table.setLayoutData(gd);

        textViewer = new SourceViewer(sash, null, SWT.H_SCROLL | SWT.V_SCROLL
                | SWT.MULTI | SWT.BORDER | SWT.READ_ONLY);

        textViewer.configure(new TextSourceViewerConfiguration(EditorsUI.getPreferenceStore()){
			@SuppressWarnings("rawtypes")
			protected Map getHyperlinkDetectorTargets(ISourceViewer targetViewer) {
                @SuppressWarnings("unchecked")
				Map<String, IAdaptable> targets = super.getHyperlinkDetectorTargets(textViewer);
                if (targets != null) {
                    targets.put("org.eclipse.ui.DefaultTextEditor", P4HistoryPage.this); //$NON-NLS-1$
                }
                return targets;
			}

			@Override
			public int getHyperlinkStateMask(ISourceViewer viewer) {
				return SWT.NONE;
			}
        	
        });

        boolean wrap = PerforceUIPlugin.getPlugin().getPreferenceStore()
                .getBoolean(WRAP_TEXT);
        if (wrap) {
            textViewer.getTextWidget().setWordWrap(wrap);
        }

        boolean showViewer = PerforceUIPlugin.getPlugin().getPreferenceStore()
                .getBoolean(SHOW_COMMENTS);
        if (!showViewer) {
            sash.setMaximizedControl(composite);
        }

        treeViewer
                .addSelectionChangedListener(new TableSelectionChangedListener());
        createActions();
    }

    /**
     * @see org.eclipse.team.ui.history.IHistoryPage#getDescription()
     */
    public String getDescription() {
        return getFileInput().getRemotePath();
    }

    /**
     * @see org.eclipse.team.ui.history.IHistoryPage#getName()
     */
    public String getName() {
        return getFileInput().getName();
    }

    /**
     * @see org.eclipse.team.ui.history.IHistoryPage#isValidInput(java.lang.Object)
     */
    public boolean isValidInput(Object object) {
        return object instanceof IP4File;
    }

    /**
     * @see org.eclipse.team.ui.history.IHistoryPage#refresh()
     */
    public void refresh() {
        showHistory(getFileInput());
    }

    /**
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class adapter) {
        Object adapted = null;
        IP4File currentFile = file;
        if (currentFile != null) {
            if (IP4Resource.class == adapter || IP4File.class == adapter) {
                adapted = currentFile;
            } else if (IP4Connection.class == adapter) {
                adapted = currentFile.getConnection();
            } else if (IResource.class == adapter) {
                adapted = currentFile.getLocalFileForLocation();
            }
        }
        if (adapted == null) {
            return Platform.getAdapterManager().getAdapter(this, adapter);
        }
        return adapted;
    }

    /**
     * @see org.eclipse.ui.part.Page#getControl()
     */
    @Override
    public Control getControl() {
        return this.sash;
    }

    /**
     * @see org.eclipse.ui.part.Page#setFocus()
     */
    @Override
    public void setFocus() {
        this.sash.setFocus();
    }

}
