package com.perforce.team.ui.p4java.dialogs;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
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
import com.perforce.team.ui.p4java.actions.ManualResolveAction;
import com.perforce.team.ui.p4java.dialogs.ResolveWizard.ResolveItem;
import com.perforce.team.ui.p4merge.P4MergeResolveAction;

public class ResolveInteractiveControl extends Composite {
	private IResolveControlContainer container;

    private Button eclipseCompareButton;
    private Button p4MergeButton;
    private Button resolveButton;
    private TableViewer resolveTable;
    private int selectedItem;

	public ResolveInteractiveControl(Composite parent, int style, IResolveControlContainer container) {
		super(parent, style);
		this.container=container;
		createControl();
	}

    private void createControl() {
        setLayout(new GridLayout(1, false));

        createFilesGroup(this);
        createMiddleGroup(this);
        createOptionsGroup(this);
        createResolveButton(this);
        selectItem();
	}

	private void createFilesGroup(Composite composite) {
        Label label = new Label(composite, SWT.NONE);
        label.setText(Messages.ResolveWizardInteractivePage_FilesToResolve);
        createViewer(composite);
    }

    private void createMiddleGroup(Composite composite) {
        Composite fileButtons = new Composite(composite, SWT.NONE);
        fileButtons.setLayout(new GridLayout(3, false));
        fileButtons
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    }

    void createOptionsGroup(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setText(Messages.ResolveWizardInteractivePage_ResolveOptions);
        group.setLayout(new GridLayout(2, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Composite autoResolveChoices = new Composite(group, SWT.NONE);
        autoResolveChoices.setLayout(new GridLayout(1, true));
        autoResolveChoices.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                false));
        eclipseCompareButton = new Button(autoResolveChoices, SWT.RADIO);
        eclipseCompareButton.setText(Messages.ResolveWizardInteractivePage_UseEclipse);

        p4MergeButton = new Button(autoResolveChoices, SWT.RADIO | SWT.TOP
                | SWT.BOLD);
        p4MergeButton.setText(Messages.ResolveWizardInteractivePage_UseP4Merge);

        setDefaultSelection();
    }

    private void createResolveButton(Composite parent) {
        resolveButton = new Button(parent, SWT.PUSH);
        resolveButton.setText(Messages.ResolveWizardInteractivePage_ResolveFile);
        resolveButton.setLayoutData(new GridData(SWT.RIGHT, SWT.BEGINNING,
                false, false));
        resolveButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                runResolve();
            }

        });

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

        resolveTable = new TableViewer(comp, SWT.SINGLE | SWT.FULL_SELECTION
                | SWT.BORDER);

        final Table table = resolveTable.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        createColumns(comp, resolveTable, layout);

        resolveTable.setContentProvider(ArrayContentProvider.getInstance());
        resolveTable.setInput(container.getResolveWizard().unresolved);
        resolveTable
                .addSelectionChangedListener(new ISelectionChangedListener() {

                    public void selectionChanged(SelectionChangedEvent event) {
                        selectItem();
                    }

                });

        updateColors();
        table.select(0);
    }

    private void updateColors() {
        final Table table = resolveTable.getTable();
        Map<IP4File, Integer> startFromRevs = new HashMap<IP4File, Integer>();
        for (int i = 0; i < table.getItemCount(); i++) {
            ResolveItem item = (ResolveItem) table.getItem(i).getData();
            if (!startFromRevs.containsKey(item.file)
                    || startFromRevs.get(item.file) > item.getStartFromRev()) {
                startFromRevs.put(item.file, item.getStartFromRev());
            }
        }
        for (int i = 0; i < table.getItemCount(); i++) {
            ResolveItem item = (ResolveItem) table.getItem(i).getData();
            if (item.getStartFromRev() > startFromRevs.get(item.file)
                    || !item.isContent()) {
                table.getItem(i).setForeground(
                        getDisplay()
                                .getSystemColor(
                                        SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
            }

        }

    }

    protected void selectItem() {
        Table table = resolveTable.getTable();
        table.setFocus();
        int selectedIndex = table.getSelectionIndex();
        if (selectedIndex == -1) {
            updateButtons(false);
            return;
        }

        ResolveItem selectedItem = (ResolveItem) table.getItem(selectedIndex)
                .getData();

        if (!selectedItem.isContent()) {
            container.setErrorMessage(Messages.ResolveWizardInteractivePage_ActionResolveMustBeAutoresolved);
            updateButtons(false);
            return;
        }
        // if step resolve, make sure only first step is selected
        int firstStep = selectedIndex;
        for (int i = 0; i < table.getItemCount(); i++) {
            if (i == selectedIndex)
                continue;
            ResolveItem item = (ResolveItem) table.getItem(i).getData();
            if (item.file == selectedItem.file
                    && item.getStartFromRev() < selectedItem.getStartFromRev()) {
                firstStep = i;
            }
        }
        if (selectedIndex != firstStep) {
            container.setErrorMessage(Messages.ResolveWizardInteractivePage_MustResolveEarlierStepsFirst);
            updateButtons(false);
        } else {
            container.setErrorMessage(null);
            updateButtons(true);
        }
    }

    private void updateButtons(boolean enabled) {
        resolveButton.setEnabled(enabled);
        eclipseCompareButton.setEnabled(enabled);
        p4MergeButton.setEnabled(enabled);
    }

    private void createColumns(final Composite parent,
            final TableViewer viewer, TableColumnLayout layout) {
        TableViewerColumn col = createTableViewerColumn(Messages.ResolveWizardInteractivePage_Resolve, 50,
                layout);
        col.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {
                ResolveItem item = (ResolveItem) element;
                return item.file.getClientPath();
            }
        });

        col = createTableViewerColumn(Messages.ResolveWizardInteractivePage_ResolveWith, 40, layout);
        col.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {
                ResolveItem item = (ResolveItem) element;
                if(item.isResolveShelvedChange()){
	                return MessageFormat.format("{0}{1}}", //$NON-NLS-1$
	                        item.getFromFile(),
	                        item.computeTheirRev());
                }else{
	                // startFromRev will be -1 when action resolves are nearby
	                return MessageFormat.format("{0} #{1}, #{2}", //$NON-NLS-1$
	                        item.getFromFile(),
	                        Math.max(0, item.getStartFromRev()) + 1,
	                        item.getEndFromRev());
                }
            }
        });

        col = createTableViewerColumn(Messages.ResolveWizardInteractivePage_ResolveType, 10, layout);
        col.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {
                ResolveItem item = (ResolveItem) element;
                return item.getResolveType()==null?"":item.getResolveType();
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

    void setDefaultSelection() {
        IPreferenceStore store = PerforceUIPlugin.getPlugin()
                .getPreferenceStore();
        String tool = store
                .getString(IPerforceUIConstants.PREF_RESOLVE_INTERACTIVE_MERGE_TOOL);
        if (tool.equals("eclipse_compare")) //$NON-NLS-1$
            eclipseCompareButton.setSelection(true);
        else
            p4MergeButton.setSelection(true);
    }

    private void runResolve() {
        selectedItem = resolveTable.getTable().getSelectionIndex();
        if (selectedItem == -1)
            return;
        ResolveItem item = (ResolveItem) resolveTable.getTable()
                .getItem(selectedItem).getData();

        P4Collection collection = P4ConnectionManager.getManager()
                .createP4Collection();
        collection.setType(IP4Resource.Type.LOCAL);
        collection.add(item.file);

        if (eclipseCompareButton.getSelection()) {
            runEclipseCompare(item);
        } else if (p4MergeButton.getSelection()) {
            runP4Merge(item);
        }
    }

    private void runEclipseCompare(final ResolveItem item) {
        startResolve();
        stopResolve(new ManualResolveAction().runManualResolve(item.file,
                item.index));
    }

    private void runP4Merge(final ResolveItem item) {
        startResolve();
        final P4MergeResolveAction p4mergeResolve = new P4MergeResolveAction();
        p4mergeResolve.setAsync(false);
//        p4mergeResolve.setShell(P4UIUtils.getShell());
        IP4Runnable runnable = new P4Runnable() {

            @Override
            public void run(IProgressMonitor monitor) {
                final IP4Resource resolved = p4mergeResolve.runResolve(
                        item.file, item.index);
                UIJob job = new UIJob(Messages.ResolveWizardInteractivePage_UpdatingDialog) {

                    @Override
                    public IStatus runInUIThread(IProgressMonitor monitor) {
                        stopResolve(resolved);
                        return Status.OK_STATUS;
                    }
                };
                job.setSystem(true);
                job.schedule();
            }
        };
        P4Runner.schedule(runnable);
    }

    private void startResolve() {
        resolveTable.getTable().setEnabled(false);
        updateButtons(false);

    }

    private void stopResolve(IP4Resource resolved) {

        if (!P4UIUtils.okToUse(resolveTable))
            return;

        // reload resolve info for file we just tried to resolve
        Table table = resolveTable.getTable();
        ResolveItem item = (ResolveItem) table.getItem(selectedItem).getData();
        P4ConnectionManager
                .getManager()
                .createP4Collection(new IP4Resource[] { item.file })
                .resolve(
                        new ResolveFilesAutoOptions().setShowActionsOnly(true)
                                .setShowBase(true));

        // rebuild table to reflect current info for this file
        container.getResolveWizard().updateUnresolvedItems();
        resolveTable.setInput(container.getResolveWizard().unresolved);
        updateColors();

        // select first item with same file and if possible same resolve item
        selectedItem = -1;
        for (int i = 0; i < table.getItemCount(); i++) {
            ResolveItem rItem = (ResolveItem) table.getItem(i).getData();
            if (rItem.file == item.file) {
                if (rItem.index == item.index)
                    selectedItem = i;
                else if (selectedItem == -1)
                    selectedItem = i;
            }
        }
        table.select(selectedItem);

        // reenable table and buttons
        table.setEnabled(true);
        selectItem();
    }

	public void init(){
        resolveTable.setInput(container.getResolveWizard().unresolved);
	}
	
}
