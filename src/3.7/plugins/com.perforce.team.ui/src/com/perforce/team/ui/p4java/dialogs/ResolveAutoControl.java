package com.perforce.team.ui.p4java.dialogs;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.progress.UIJob;

import com.perforce.p4java.option.client.ResolveFilesAutoOptions;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.p4java.dialogs.ResolveWizard.ResolveItem;

public class ResolveAutoControl extends Composite {
	private IResolveControlContainer container;

    private Button acceptSourceButton;
    private Button acceptTargetButton;
    private Button mergeSafeButton;
    private Button mergeNoConflictsButton;
    private Button mergeWithConflictsButton;
    private CheckboxTableViewer resolveTable;
    private Button resolveButton;
    private Button binaryMerge;
    private Button selectButton;
    private int numResolveOperations = 0;
    private int numResolveItems = 0;

	public ResolveAutoControl(Composite parent, int style, IResolveControlContainer container) {
		super(parent, style);
		this.container=container;
		createControl();
	}

	public void init(){
        resolveTable.setInput(container.getResolveWizard().unresolved);
        resolveTable.setAllChecked(true);
	}
	
	private void createControl() {
        setLayout(new GridLayout(1, false));

        createFilesGroup(this);
        createMiddleGroup(this);
        createOptionsGroup(this);
        createResolveButton(this);
        setDefaultSelections();

	}

    private void createFilesGroup(Composite parent) {
        final Composite buttons = new Composite(parent, SWT.NONE);
        buttons.setLayout(new GridLayout(2, false));
        buttons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        Label label = new Label(buttons, SWT.NONE);
        label.setText(Messages.ResolveWizardAutoPage_FilesToResolve);
        label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        final Menu menu = new Menu(parent.getShell(), SWT.POP_UP);
        MenuItem item = new MenuItem(menu, SWT.PUSH);
        item.setText(Messages.ResolveWizardAutoPage_None);
        item.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event e) {
                resolveTable.setAllChecked(false);
            }
        });
        item = new MenuItem(menu, SWT.PUSH);
        item.setText(Messages.ResolveWizardAutoPage_All);
        item.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event e) {
                resolveTable.setAllChecked(true);
            }
        });
        item = new MenuItem(menu, SWT.PUSH);
        item.setText(Messages.ResolveWizardAutoPage_BinaryFiles);
        item.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event e) {
                selectAllWithFileType("binary"); //$NON-NLS-1$
            }
        });
        item = new MenuItem(menu, SWT.PUSH);
        item.setText(Messages.ResolveWizardAutoPage_TextFiles);
        item.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event e) {
                selectAllWithFileType("text"); //$NON-NLS-1$
            }
        });
        item = new MenuItem(menu, SWT.PUSH);
        item.setText(Messages.ResolveWizardAutoPage_ContentResolves);
        item.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event e) {
                selectAllWithResolveType("content"); //$NON-NLS-1$
            }
        });
        item = new MenuItem(menu, SWT.PUSH);
        item.setText(Messages.ResolveWizardAutoPage_MoveResolves);
        item.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event e) {
                selectAllWithResolveType("move"); //$NON-NLS-1$
            }
        });
        item = new MenuItem(menu, SWT.PUSH);
        item.setText(Messages.ResolveWizardAutoPage_FiletypeResolves);
        item.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event e) {
                selectAllWithResolveType("filetype"); //$NON-NLS-1$
            }
        });
        item = new MenuItem(menu, SWT.PUSH);
        item.setText(Messages.ResolveWizardAutoPage_DeleteResolves);
        item.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event e) {
                selectAllWithResolveType("delete"); //$NON-NLS-1$
            }
        });
        item = new MenuItem(menu, SWT.PUSH);
        item.setText(Messages.ResolveWizardAutoPage_BranchResolves);
        item.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event e) {
                selectAllWithResolveType("branch"); //$NON-NLS-1$
            }
        });
        item = new MenuItem(menu, SWT.PUSH);
        item.setText(Messages.ResolveWizardAutoPage_AttributeResolves);
        item.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event e) {
                selectAllWithResolveType("attributes"); //$NON-NLS-1$
            }
        });
        selectButton = new Button(buttons, SWT.PUSH);
        selectButton.setImage(PerforceUIPlugin.getDescriptor(
                IPerforceUIConstants.IMG_FILTER_ICON_MENU).createImage());
        selectButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {

                Rectangle rect = selectButton.getBounds();
                Point pt = new Point(rect.x, rect.y + rect.height);
                pt = buttons.toDisplay(pt);
                menu.setLocation(pt.x, pt.y);
                menu.setVisible(true);
                while (!menu.isDisposed() && menu.isVisible()) {
                    if (!menu.getDisplay().readAndDispatch())
                        menu.getDisplay().sleep();
                }
            }
        });
        createViewer(parent);
    }

    private void createMiddleGroup(Composite parent) {
        binaryMerge = new Button(parent, SWT.CHECK);
        binaryMerge
                .setText(Messages.ResolveWizardAutoPage_MergeBinary);
        binaryMerge
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    }

    private void createResolveButton(Composite parent) {
        resolveButton = new Button(parent, SWT.PUSH);
        resolveButton.setText(Messages.ResolveWizardAutoPage_ResolveSelected);
        resolveButton.setLayoutData(new GridData(SWT.RIGHT, SWT.BEGINNING,
                false, false));
        resolveButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                runResolve();
            }

        });

    }

    void createOptionsGroup(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setText(Messages.ResolveWizardAutoPage_ResolveOptions);
        group.setLayout(new GridLayout(2, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Composite autoResolveChoices = new Composite(group, SWT.NONE);
        autoResolveChoices.setLayout(new GridLayout(1, true));
        autoResolveChoices.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                false));
        acceptSourceButton = new Button(autoResolveChoices, SWT.RADIO);
        acceptSourceButton.setText(Messages.ResolveWizardAutoPage_AcceptSource);

        acceptTargetButton = new Button(autoResolveChoices, SWT.RADIO | SWT.TOP
                | SWT.BOLD);
        acceptTargetButton.setText(Messages.ResolveWizardAutoPage_AcceptTarget);

        mergeSafeButton = new Button(autoResolveChoices, SWT.RADIO);
        mergeSafeButton
                .setText(Messages.ResolveWizardAutoPage_SafeAutoResolve);

        mergeNoConflictsButton = new Button(autoResolveChoices, SWT.RADIO);
        mergeNoConflictsButton.setText(Messages.ResolveWizardAutoPage_AllowMergeNoConflicts);

        mergeWithConflictsButton = new Button(autoResolveChoices, SWT.RADIO);
        mergeWithConflictsButton
                .setText(Messages.ResolveWizardAutoPage_AllowMergeWithConflicts);
    }

    private void setDefaultSelections() {
        IPreferenceStore store = PerforceUIPlugin.getPlugin()
                .getPreferenceStore();

        String action = store
                .getString(IPerforceUIConstants.PREF_RESOLVE_DEFAULT_ACTION);
        if (action.equals("accept_source")) //$NON-NLS-1$
            acceptSourceButton.setSelection(true);
        else if (action.equals("accept_target")) //$NON-NLS-1$
            acceptTargetButton.setSelection(true);
        else if (action.equals("accept_merge_safe")) //$NON-NLS-1$
            mergeSafeButton.setSelection(true);
        else if (action.equals("accept_merge_no_conflicts")) //$NON-NLS-1$
            mergeNoConflictsButton.setSelection(true);
        else if (action.equals("accept_merge_with_conflicts")) //$NON-NLS-1$
            mergeWithConflictsButton.setSelection(true);

        binaryMerge
                .setSelection(store
                        .getBoolean(IPerforceUIConstants.PREF_RESOLVE_MERGE_BINARY_AS_TEXT));
    }

    ResolveFilesAutoOptions getAutoOption() {
        ResolveFilesAutoOptions options = new ResolveFilesAutoOptions();
        if (mergeSafeButton.getSelection()) {
            options.setSafeMerge(true);
        } else if (acceptSourceButton.getSelection()) {
            options.setAcceptTheirs(true);
        } else if (acceptTargetButton.getSelection()) {
            options.setAcceptYours(true);
        } else if (mergeNoConflictsButton.getSelection()) {
            // don't set any options in this case
        } else if (mergeWithConflictsButton.getSelection()) {
            options.setForceResolve(true);
        }
        if (binaryMerge.getSelection()) {
            options.setForceTextualMerge(true);
        }
        return options;
    }

    private void createViewer(Composite parent) {
        // TableColumnLayout won't work correctly unless TableViewer is in
        // a composite by itself.
        Composite comp = new Composite(parent, SWT.NONE);
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        data.heightHint = P4UIUtils.VIEWER_HEIGHT_HINT * 2;
        comp.setLayoutData(data);

        TableColumnLayout layout = new TableColumnLayout();
        comp.setLayout(layout);

        resolveTable = CheckboxTableViewer.newCheckList(comp, SWT.SINGLE
                | SWT.FULL_SELECTION | SWT.BORDER);

        final Table table = resolveTable.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        createColumns(comp, resolveTable, layout);

        resolveTable.setContentProvider(ArrayContentProvider.getInstance());
        resolveTable.addCheckStateListener(new ICheckStateListener() {

            public void checkStateChanged(CheckStateChangedEvent event) {
                validate();
            }

        });
    }

    private void createColumns(final Composite parent,
            final TableViewer viewer, TableColumnLayout layout) {
        TableViewerColumn col = createTableViewerColumn(Messages.ResolveWizardAutoPage_Resolve, 50,
                layout);
        col.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {
                ResolveItem item = (ResolveItem) element;
                return item.file.getClientPath();
            }
        });

        col = createTableViewerColumn(Messages.ResolveWizardAutoPage_ResolveWith, 40, layout);
        col.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {
                ResolveItem item = (ResolveItem) element;
                // startFromRev will be -1 when action resolves are nearby
                return MessageFormat.format("{0} #{1}, #{2}", //$NON-NLS-1$
                        item.getFromFile(),
                        Math.max(0, item.getStartFromRev()) + 1,
                        item.getEndFromRev());
            }
        });

        col = createTableViewerColumn(Messages.ResolveWizardAutoPage_ResolveType, 10, layout);
        col.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {
                ResolveItem item = (ResolveItem) element;
                return item.file.getIntegrationSpecs()[item.index]
                        .getResolveType();
            }
        });
    }

    private TableViewerColumn createTableViewerColumn(String title, int weight,
            TableColumnLayout layout) {
        final TableViewerColumn viewerColumn = new TableViewerColumn(
                resolveTable, SWT.NONE);
        final TableColumn column = viewerColumn.getColumn();
        column.setText(title);
        layout.setColumnData(column, new ColumnWeightData(weight, true));
        return viewerColumn;
    }

    private void validate() {
        Table table = resolveTable.getTable();
        for (int i = 0; i < table.getItemCount(); i++) {
            ResolveItem item = (ResolveItem) table.getItem(i).getData();
            if (!resolveTable.getChecked(item))
                continue;
            if (item.isMove()) {
                selectAllItemsForFile(item.file);
            } else if (item.isContent()) {
                selectAllContentItemsForFile(item.file);
            }
        }

        String message = null;
        int checkCount = resolveTable.getCheckedElements().length;
        resolveButton.setEnabled(checkCount > 0);
        if (message == null && checkCount == 0) {
            message = Messages.ResolveWizardAutoPage_SelectFiles;
        }
        container.setMessage(message);
    }

    private void selectAllItemsForFile(IP4File file) {
        Table table = resolveTable.getTable();
        for (int i = 0; i < table.getItemCount(); i++) {
            ResolveItem item = (ResolveItem) table.getItem(i).getData();
            if (item.file == file) {
                resolveTable.setChecked(item, true);
            }
        }
    }

    private void selectAllContentItemsForFile(IP4File file) {
        Table table = resolveTable.getTable();
        for (int i = 0; i < table.getItemCount(); i++) {
            ResolveItem item = (ResolveItem) table.getItem(i).getData();
            if (item.file == file && item.isContent()) {
                resolveTable.setChecked(item, true);
            }
        }
    }

    private void selectAllWithFileType(String type) {
        Table table = resolveTable.getTable();
        for (int i = 0; i < table.getItemCount(); i++) {
            ResolveItem item = (ResolveItem) table.getItem(i).getData();
            resolveTable.setChecked(item, StringUtils.equals(type,item.file.getOpenedType()));
        }
        validate();
    }

    private void selectAllWithResolveType(String type) {
        Table table = resolveTable.getTable();
        for (int i = 0; i < table.getItemCount(); i++) {
            ResolveItem item = (ResolveItem) table.getItem(i).getData();
            resolveTable.setChecked(item, type.equals(item.getResolveType()));
        }
        validate();
    }

    private static class ResolveOperation {

        IP4File file = null;
        ResolveFilesAutoOptions options = null;
    }

    private void runResolve() {
        assert (numResolveItems == 0);
        List<ResolveOperation> operations = new ArrayList<ResolveOperation>();
        Table table = resolveTable.getTable();
        ResolveOperation op = new ResolveOperation();
        for (int i = 0; i < table.getItemCount(); i++) {
            ResolveItem item = (ResolveItem) table.getItem(i).getData();
            if (!resolveTable.getChecked(item))
                continue;
            numResolveItems++;
            if (op.file != item.file) {
                if (op.file != null) {
                    operations.add(op);
                    op = new ResolveOperation();
                }
                op.file = item.file;
                op.options = getAutoOption();
            }
            if(item.getResolveType()!=null)
            	op.options.setResolveResolveType(item.getResolveType(), true);
        }
        if (op.file != null) {
            operations.add(op);
        }
        if (!operations.isEmpty()) {
            startResolve(operations);
        }
    }

    private void startResolve(List<ResolveOperation> operations) {
        container.setMessage(Messages.ResolveWizardAutoPage_AutoResolvingFiles);
        updateButtons(false);

        assert (numResolveOperations == 0);
        numResolveOperations = operations.size();

        for (ResolveOperation op : operations) {
            final P4Collection collection = P4ConnectionManager.getManager()
                    .createP4Collection();
            collection.setType(IP4Resource.Type.LOCAL);
            collection.add(op.file);
            final IP4File file = op.file;
            final ResolveFilesAutoOptions options = op.options;
            IP4Runnable runnable = new P4Runnable() {

                @Override
                public void run(IProgressMonitor monitor) {
                    // the real resolve
                    final int numIntegSpecsBefore = file.getIntegrationSpecs() != null ? file.getIntegrationSpecs().length : 0;
                    collection.resolve(options);
                    // followed by a preview resolve to refresh
                    file.refresh();
                    collection.resolve(new ResolveFilesAutoOptions().setShowActionsOnly(true)
                            .setShowBase(true));
                    final int numIntegSpecsAfter = file.getIntegrationSpecs() != null ? file.getIntegrationSpecs().length : 0;
                    UIJob job = new UIJob(
                            Messages.ResolveWizardAutoPage_UpdatingDialog) {

                        @Override
                        public IStatus runInUIThread(IProgressMonitor monitor) {
                            stopResolve(file, numIntegSpecsBefore - numIntegSpecsAfter);
                            return Status.OK_STATUS;
                        }
                    };
                    job.setSystem(true);
                    job.schedule();
                }

            };

            P4Runner.schedule(runnable);
        }
    }

    private void stopResolve(IP4File file, int numResolved) {
        if (!P4UIUtils.okToUse(resolveTable))
            return;
        
        numResolveItems -= numResolved;
        if (--numResolveOperations == 0) {

            // rebuild table to reflect current info for this file
            container.getResolveWizard().updateUnresolvedItems();
            resolveTable.setInput(container.getResolveWizard().unresolved);

            if (numResolveItems > 0) {
                MessageDialog.openInformation(getShell(),
                        Messages.ResolveWizardAutoPage_ResolveFailedTitle,
                        MessageFormat.format(
                                Messages.ResolveWizardAutoPage_ResolveFailedMessage,
                                numResolveItems));
                numResolveItems = 0;
            }

            resolveTable.getTable().setEnabled(true);
            updateButtons(true);
        }
    }

    private void updateButtons(boolean enabled) {
        selectButton.setEnabled(enabled);
        resolveButton.setEnabled(enabled);
        mergeSafeButton.setEnabled(enabled);
        mergeNoConflictsButton.setEnabled(enabled);
        acceptTargetButton.setEnabled(enabled);
        acceptSourceButton.setEnabled(enabled);
        mergeWithConflictsButton.setEnabled(enabled);
    }

}
