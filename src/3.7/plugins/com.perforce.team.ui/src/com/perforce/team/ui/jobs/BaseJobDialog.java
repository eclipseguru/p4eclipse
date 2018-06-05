/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.jobs;

import com.perforce.p4java.core.IJobSpec;
import com.perforce.p4java.core.IJobSpec.IJobSpecField;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.dialogs.P4FormDialog;
import com.perforce.team.ui.submitted.SubmittedChangelistDialog;
import com.perforce.team.ui.submitted.SubmittedSorter;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class BaseJobDialog extends P4FormDialog {

    /**
     * displayArea
     */
    protected Composite displayArea;

    /**
     * fieldArea
     */
    protected Composite fieldArea;

    /**
     * singleFieldsArea
     */
    protected Composite singleFieldsArea;

    /**
     * multiFieldsArea
     */
    protected Composite multiFieldsArea;

    /**
     * changelistArea
     */
    protected Composite changelistArea;

    /**
     * changelistTable
     */
    protected CheckboxTableViewer changelistTable;

    private Label changelistLabel;

    private Text changelistText;

    private Button addButton;

    private Button browseButton;

    private List<Widget> fields = new ArrayList<Widget>();

    private ModifyListener modifyListener = new ModifyListener() {

        public void modifyText(ModifyEvent e) {
            modified = true;
        }
    };

    /**
     * Changelists currently in the table
     */
    protected Set<IP4Changelist> changelists = new HashSet<IP4Changelist>();

    private ITableLabelProvider labelProvider = new ITableLabelProvider() {

        Image submittedIcon = null;
        Image pendingIcon = null;

        public void removeListener(ILabelProviderListener listener) {

        }

        public boolean isLabelProperty(Object element, String property) {
            return false;
        }

        public void dispose() {
            if (submittedIcon != null) {
                submittedIcon.dispose();
            }
            if (pendingIcon != null) {
                pendingIcon.dispose();
            }
        }

        public void addListener(ILabelProviderListener listener) {

        }

        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof IP4Changelist) {
                IP4Changelist list = (IP4Changelist) element;
                switch (columnIndex) {
                case 0:
                    return Integer.toString(list.getId());
                case 1:
                    return list.getDate().toString();
                case 2:
                    return list.getUserName();
                case 3:
                    return P4CoreUtils.removeWhitespace(list.getDescription());
                default:
                    return ""; //$NON-NLS-1$
                }
            }
            return ""; //$NON-NLS-1$
        }

        public Image getColumnImage(Object element, int columnIndex) {
            if (columnIndex == 0) {
                if (element instanceof IP4PendingChangelist) {
                    if (pendingIcon == null) {
                        pendingIcon = PerforceUIPlugin
                                .getPlugin()
                                .getImageDescriptor(
                                        IPerforceUIConstants.IMG_CHG_OUR)
                                .createImage();
                    }
                    return pendingIcon;
                } else if (element instanceof IP4SubmittedChangelist) {
                    if (submittedIcon == null) {
                        submittedIcon = PerforceUIPlugin
                                .getPlugin()
                                .getImageDescriptor(
                                        IPerforceUIConstants.IMG_CHG_SUBMITTED)
                                .createImage();
                    }
                    return submittedIcon;
                }
            }
            return null;
        }
    };

    /**
     * @param parent
     */
    public BaseJobDialog(Shell parent) {
        super(parent);
        setModalResizeStyle();
    }

    /**
     * @param parent
     * @param title
     */
    public BaseJobDialog(Shell parent, String title) {
        super(parent, title);
        setModalResizeStyle();
    }

    /**
     * Get the connection that this job dialog is being shown for
     * 
     * @return - p4 connection
     */
    protected abstract IP4Connection getConnection();

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite c = (Composite) super.createDialogArea(parent);

        displayArea = new Composite(c, SWT.NONE);
        GridLayout daLayout = new GridLayout(1, true);
        daLayout.marginWidth = 0;
        daLayout.marginHeight = 0;
        displayArea.setLayout(daLayout);
        displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createFieldArea(displayArea);
        createChangelistArea(displayArea);

        return c;
    }

    private void createFieldArea(Composite parent) {
        fieldArea = new Composite(parent, SWT.NONE);
        GridLayout faLayout = new GridLayout(1, true);
        faLayout.marginWidth = 0;
        faLayout.marginHeight = 0;
        fieldArea.setLayout(faLayout);
        fieldArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        singleFieldsArea = new Composite(fieldArea, SWT.NONE);
        GridLayout sfaLayout = new GridLayout(2, true);
        sfaLayout.marginHeight = 0;
        sfaLayout.marginWidth = 0;
        singleFieldsArea.setLayout(sfaLayout);
        singleFieldsArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                false));

        GridLayout twoDiffLayout = new GridLayout(2, false);
        twoDiffLayout.marginHeight = 0;
        twoDiffLayout.marginWidth = 0;

        Composite leftSide = new Composite(singleFieldsArea, SWT.NONE);
        leftSide.setLayout(twoDiffLayout);
        leftSide.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Composite rightSide = new Composite(singleFieldsArea, SWT.NONE);
        rightSide.setLayout(twoDiffLayout);
        rightSide.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        multiFieldsArea = new Composite(fieldArea, SWT.NONE);
        multiFieldsArea.setLayout(twoDiffLayout);
        multiFieldsArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                true));

        IP4Connection conn = getConnection();
        if(conn!=null){
	        IJobSpec spec = conn.getJobSpec();
	        if (spec != null) {
	            List<IJobSpecField> fields = spec.getFields();
	
	            int singleFields = 0;
	            for (IJobSpecField field : fields) {
	                if (!IP4Job.TEXT_DATA_TYPE.equals(field.getDataType())) {
	                    singleFields++;
	                }
	            }
	            int left = 0;
	            for (IJobSpecField field : fields) {
	                if (IP4Job.TEXT_DATA_TYPE.equals(field.getDataType())) {
	                    addMultiField(multiFieldsArea, field,
	                            spec.getFieldPreset(field.getName()));
	                } else {
	                    if (left < singleFields / 2) {
	                        left++;
	                        addSingleField(leftSide, field,
	                                spec.getFieldValues(field.getName()),
	                                spec.getFieldPreset(field.getName()));
	                    } else {
	                        addSingleField(rightSide, field,
	                                spec.getFieldValues(field.getName()),
	                                spec.getFieldPreset(field.getName()));
	                    }
	                }
	            }
	        }
        }
    }

    /**
     * Should the created ui field be read-only for the given job spec field and
     * preset?
     * 
     * @param field
     * @param preset
     * @return - true if widget sould be marked as not editable
     */
    protected boolean isReadonly(IJobSpecField field, String preset) {
        return IP4Job.ALWAYS_FIELD_TYPE.equals(field.getFieldType())
                || IP4Job.ONCE_FIELD_TYPE.equals(field.getFieldType());
    }

    private void addSingleField(Composite parent, IJobSpecField field,
            List<String> values, String preset) {
        Label label = new Label(parent, SWT.LEFT);
        label.setText(field.getName() + ":"); //$NON-NLS-1$
        if (IP4Job.SELECT_DATA_TYPE.equals(field.getDataType())) {
            Combo combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY
                    | SWT.BORDER);
            combo.setData(field);
            combo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
            combo.add(Messages.BaseJobDialog_NoneSelected);
            combo.select(0);
            if (values != null) {
                for (String value : values) {
                    combo.add(value);
                }
            }
            setField(combo, field, preset);
            combo.addModifyListener(modifyListener);
            addField(combo);
        } else {
            int flags = SWT.SINGLE;
            if (isReadonly(field, preset)) {
                flags |= SWT.READ_ONLY;
            } else {
                flags |= SWT.BORDER;
            }
            Text text = new Text(parent, flags);
            text.setData(field);
            text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

            setField(text, field, preset);
            text.addModifyListener(modifyListener);
            addField(text);
            
            if (IP4Job.JOB_NAME_CODE == field.getCode()) {
                addFormNameValidation(text, "job");
            }
        }
    }

    private void addMultiField(Composite parent, IJobSpecField field,
            String preset) {
        Label label = new Label(parent, SWT.LEFT);
        label.setText(field.getName() + ":"); //$NON-NLS-1$
        label.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
        Text text = new Text(parent, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
        text.setData(field);
        GridData tData = new GridData(SWT.FILL, SWT.FILL, true, true);
        if (IP4Job.JOB_DESCRIPTION_CODE == field.getCode()) {
            tData.heightHint = 90;
        } else {
            tData.heightHint = 40;
        }
        text.setLayoutData(tData);
        if (isReadonly(field, preset)) {
            text.setEditable(false);
        }
        setField(text, field, preset);
        text.addModifyListener(modifyListener);
        addField(text);
    }

    private void createChangelistTable(Composite parent) {
        changelistTable = CheckboxTableViewer
                .newCheckList(parent, SWT.SINGLE | SWT.FULL_SELECTION
                        | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        changelistTable.setLabelProvider(labelProvider);
        changelistTable.setContentProvider(new ArrayContentProvider());
        GridData ctData = new GridData(SWT.FILL, SWT.FILL, true, true);
        ctData.heightHint = 80;
        changelistTable.getTable().setLayoutData(ctData);
        changelistTable.getTable().setHeaderVisible(true);

        TableColumn sortColumn = addColumn(changelistTable.getTable(),
                SubmittedSorter.CHANGELIST, 100);
        addColumn(changelistTable.getTable(), SubmittedSorter.DATE, 100);
        addColumn(changelistTable.getTable(), SubmittedSorter.USER, 100);
        addColumn(changelistTable.getTable(), SubmittedSorter.DESCRIPTION, 200);

        SubmittedSorter sorter = new SubmittedSorter(changelistTable.getTable());
        sorter.init(changelistTable, sortColumn, SWT.DOWN);
        changelistTable.setSorter(sorter);

        addChangelists(changelistTable);
        changelistTable.setInput(this.changelists);
        changelistTable.setAllChecked(true);
    }

    /**
     * Add a changelist and optionally refresh the table
     * 
     * @param changelist
     * @param refresh
     */
    public void addChangelist(IP4Changelist changelist, boolean refresh) {
        if (changelist != null) {
            this.changelists.add(changelist);
            if (refresh) {
                changelistTable.refresh();
                changelistTable.setChecked(changelist, true);
            }
        }
    }

    /**
     * Remove a changelist and optionally refresh the table
     * 
     * @param changelist
     * @param refresh
     */
    public void removeChangelist(IP4Changelist changelist, boolean refresh) {
        if (changelist != null) {
            this.changelists.remove(changelist);
            if (refresh) {
                changelistTable.refresh();
            }
        }
    }

    /**
     * Set the job spec field
     * 
     * @param text
     * @param field
     * @param preset
     */
    protected abstract void setField(Text text, IJobSpecField field,
            String preset);

    /**
     * Set the job spec field
     * 
     * @param combo
     * @param field
     * @param preset
     */
    protected abstract void setField(Combo combo, IJobSpecField field,
            String preset);

    /**
     * Add any changelists to the table before it is shown
     * 
     * @param viewer
     */
    protected abstract void addChangelists(TableViewer viewer);

    private TableColumn addColumn(Table table, String text, int width) {
        TableColumn column = new TableColumn(table, SWT.LEFT);
        column.setText(text);
        column.setWidth(width);
        return column;
    }

    private void createChangelistArea(Composite parent) {
        changelistArea = new Composite(parent, SWT.NONE);
        GridLayout caLayout = new GridLayout(1, true);
        caLayout.marginHeight = 0;
        caLayout.marginWidth = 0;
        changelistArea.setLayout(caLayout);
        changelistArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                false));

        createChangelistTable(changelistArea);

        Composite addArea = new Composite(parent, SWT.NONE);
        GridLayout aaLayout = new GridLayout(4, false);
        aaLayout.marginHeight = 0;
        aaLayout.marginWidth = 0;
        addArea.setLayout(aaLayout);
        addArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        changelistLabel = new Label(addArea, SWT.LEFT);
        changelistLabel.setText(Messages.BaseJobDialog_Changelist);
        changelistText = new Text(addArea, SWT.SINGLE | SWT.BORDER);
        changelistText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                true));

        addButton = new Button(addArea, SWT.PUSH);
        addButton.setText(Messages.BaseJobDialog_Add);
        addButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                String id = changelistText.getText().trim();
                try {
                    int listId = Integer.parseInt(id);
                    IP4Changelist changelist = (getConnection()==null)?null:
                            getConnection().getChangelistById(listId, null, false, true);
                    if (changelist != null) {
                        addChangelist(changelist, true);
                    } else {
                        MessageDialog
                                .openError(
                                        getShell(),
                                        Messages.BaseJobDialog_ChangelistNotFoundTitle,
                                        MessageFormat
                                                .format(Messages.BaseJobDialog_ChangelistNotFoundMessage,
                                                        listId));
                    }
                } catch (NumberFormatException e1) {
                    MessageDialog
                            .openError(
                                    getShell(),
                                    Messages.BaseJobDialog_InvalidChangelistTitle,
                                    MessageFormat
                                            .format(Messages.BaseJobDialog_InvalidChangelistMessage,
                                                    Integer.MAX_VALUE));
                }
            }

        });

        browseButton = new Button(addArea, SWT.PUSH);
        browseButton.setText(Messages.BaseJobDialog_Browse);
        browseButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                SubmittedChangelistDialog dialog = new SubmittedChangelistDialog(
                        browseButton.getShell(), getConnection());
                if (SubmittedChangelistDialog.OK == dialog.open()) {
                    for (IP4SubmittedChangelist selected : dialog.getSelected()) {
                        addChangelist(selected, true);
                    }
                }
            }

        });
    }

    /**
     * Add a field
     * 
     * @param field
     */
    protected void addField(Widget field) {
        this.fields.add(field);
    }

    /**
     * Get the text of a widget
     * 
     * @param widget
     * @param checkReadOnly
     * @return - text or null if widget doesn't have text
     */
    protected String getWidgetText(Widget widget, boolean checkReadOnly) {
        String text = null;
        if (widget instanceof Combo) {
            if (((Combo) widget).getSelectionIndex() > 0) {
                text = ((Combo) widget).getText();
            }
        } else if (widget instanceof Text) {
            if (!checkReadOnly || (widget.getStyle() & SWT.READ_ONLY) == 0) {
                text = ((Text) widget).getText();
            }

        }
        return text;
    }

    /**
     * Get the fields in the dialog
     * 
     * @return - array of widgets
     */
    public Widget[] getFields() {
        return this.fields.toArray(new Widget[0]);
    }

    /**
     * Get change list viewer
     * 
     * @return - changelist table viewer
     */
    public CheckboxTableViewer getChangelistViewer() {
        return this.changelistTable;
    }

    /**
     * @see com.perforce.team.ui.dialogs.P4FormDialog#getModelLabel()
     */
    @Override
    protected String getModelLabel() {
        return Messages.BaseJobDialog_Job;
    }

}
