/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.labels;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.perforce.team.core.p4java.IP4Label;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Resource.Type;
import com.perforce.team.ui.BaseErrorProvider;
import com.perforce.team.ui.PerforceLabelProvider;
import com.perforce.team.ui.p4java.dialogs.VersionComboViewer.VersionType;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class LabelFilesWidget extends BaseErrorProvider {

    private Composite displayArea;

    private Text nameText;
    private Button browseButton;

    private Button addButton;
    private Button removeButton;
    
    private VersionWidget versionWidget=new VersionWidget();

    private IP4Resource[] resources;
    private Type type;

    private boolean delete = false;
    private String selectedLabel = null;
    
    /**
     * Creates a new label widget
     * 
     * @param resources
     * @param type
     */
    public LabelFilesWidget(IP4Resource[] resources, Type type) {
        this.resources = resources;
        this.type = type;
    }

    private void createLabelArea(Composite parent) {
        Composite labelArea = new Composite(parent, SWT.NONE);
        labelArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        GridLayout laLayout = new GridLayout(3, false);
        laLayout.marginWidth = 0;
        laLayout.marginHeight = 0;
        labelArea.setLayout(laLayout);

        Label nameLabel = new Label(labelArea, SWT.NONE);
        nameLabel.setText(Messages.LabelFilesWidget_Name);

        nameText = new Text(labelArea, SWT.SINGLE | SWT.BORDER);
        nameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        nameText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                selectedLabel = nameText.getText();
                validate();
            }
        });

        browseButton = new Button(labelArea, SWT.PUSH);
        browseButton.setText(Messages.LabelFilesWidget_Browse);
        browseButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (resources.length > 0) {
                    SelectLabelDialog dialog = new SelectLabelDialog(
                            browseButton.getShell(), resources[0]
                                    .getConnection());
                    if (SelectLabelDialog.OK == dialog.open()) {
                        IP4Label selected = dialog.getSelected();
                        if (selected != null && selected.getName() != null) {
                            nameText.setText(selected.getName());
                            selectedLabel = selected.getName();
                            validate();
                        }
                    }
                }
            }

        });
    }

    private void createFilesArea(Composite parent) {
        Composite filesArea = new Composite(parent, SWT.NONE);
        filesArea.setLayout(new GridLayout(1, true));
        filesArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        SelectionListener buttonListener = new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                delete = removeButton.getSelection();
            }

        };

        addButton = new Button(filesArea, SWT.RADIO);
        addButton.setText(Messages.LabelFilesWidget_ApplySelectedLabelToFiles);
        addButton.setSelection(true);
        addButton.addSelectionListener(buttonListener);

        removeButton = new Button(filesArea, SWT.RADIO);
        removeButton
                .setText(Messages.LabelFilesWidget_RemoveSelectedLabelFromFiles);
        removeButton.addSelectionListener(buttonListener);

        Label filesLabel = new Label(filesArea, SWT.LEFT);
        filesLabel.setText(Messages.LabelFilesWidget_Files);

        TableViewer filesViewer = new TableViewer(filesArea, SWT.SINGLE
                | SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
        GridData fvData = new GridData(SWT.FILL, SWT.FILL, true, true);
        fvData.heightHint = 100;
        filesViewer.getTable().setLayoutData(fvData);
        filesViewer.setLabelProvider(new PerforceLabelProvider(false) {

            @Override
            public String getColumnText(Object element, int columnIndex) {
                if (element instanceof IP4Resource) {
                    return ((IP4Resource) element).getActionPath(type);
                }
                return super.getColumnText(element, columnIndex);
            }
        });
        filesViewer.setContentProvider(new ArrayContentProvider());
        filesViewer.setInput(this.resources);
    }

    private void createRevisionArea(Composite parent) {
        versionWidget.createControl(parent)     
        	.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        versionWidget.filterVersion(VersionType.Revision, VersionType.ChangeList, VersionType.Datetime);
    }

    /**
     * Create new label widget
     * 
     * @param parent
     */
    public void createControl(Composite parent) {
        displayArea = new Composite(parent, SWT.NONE);
        displayArea.setLayout(new GridLayout(1, true));
        displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createLabelArea(displayArea);
        createFilesArea(displayArea);
        createRevisionArea(displayArea);
    }

    /**
     * Delete files from label?
     * 
     * @return - true if removing files from label, false if adding
     */
    public boolean deleteFromLabel() {
        return this.delete;
    }

    /**
     * Get selected label name
     * 
     * @return - label name
     */
    public String getSelectedLabel() {
        return this.selectedLabel;
    }

    /**
     * Get selected revision
     * 
     * @return - revision
     */
    public String getRevision() {
        return versionWidget.getVersion();
    }

    /**
     * @see com.perforce.team.ui.IErrorProvider#validate()
     */
    @Override
    public void validate() {
        this.errorMessage = null;
        String name = nameText.getText().trim();
        if (name.length() == 0) {
            this.errorMessage = Messages.VersionWidget_MustEnterLabelName;
        } else {
        	this.versionWidget.validate();
        	errorMessage=this.versionWidget.getErrorMessage();
        }
        super.validate();
    }
}
