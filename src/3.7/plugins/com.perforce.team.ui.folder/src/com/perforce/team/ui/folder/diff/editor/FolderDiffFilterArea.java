/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.editor;

import java.text.MessageFormat;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Label;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.ui.DatePicker;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.folder.diff.editor.input.IFilterOptions;
import com.perforce.team.ui.labels.SelectLabelDialog;
import com.perforce.team.ui.submitted.SubmittedChangelistDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class FolderDiffFilterArea {

    private IFilterOptions options;
    private IP4Connection connection;

    private Composite displayArea;

    private Section section;
    private Button latestButton;
    private Button revisionButton;
    private Combo revisionCombo;

    private Composite haveArea;

    private Composite dateArea;
    private Text dateText;

    private Composite changelistArea;
    private Text changelistText;

    private Composite revisionArea;
    private Spinner revisionSpinner;

    private Composite labelArea;
    private Text labelText;

    private Composite clientArea;
    private Text clientText;

    /**
     * Create area with specified options
     * 
     * @param options
     * @param connection
     */
    public FolderDiffFilterArea(IFilterOptions options, IP4Connection connection) {
        this.options = options;
        this.connection = connection;
    }

    /**
     * Get section
     * 
     * @return section
     */
    public Section getSection() {
        return this.section;
    }

    /**
     * Create filter area
     * 
     * @param parent
     * @param toolkit
     */
    public void createControl(Composite parent, FormToolkit toolkit) {
        section = toolkit.createSection(parent, Section.DESCRIPTION
                | Section.EXPANDED | ExpandableComposite.TWISTIE
                | ExpandableComposite.TITLE_BAR);
        section.setText(Messages.FolderDiffFilterArea_Title);
        section.setDescription(Messages.FolderDiffFilterArea_Description);
        section.setLayoutData(GridDataFactory.fillDefaults().grab(true, false)
                .create());

        displayArea = toolkit.createComposite(section);
        displayArea.setLayout(GridLayoutFactory.swtDefaults().numColumns(3)
                .spacing(0, 5).create());
        displayArea.setLayoutData(GridDataFactory.fillDefaults()
                .grab(true, false).create());
        section.setClient(displayArea);

        GridData singleData = GridDataFactory.fillDefaults().grab(true, false)
                .span(3, 1).create();

        latestButton = toolkit.createButton(displayArea,
                Messages.FolderDiffFilterArea_LatestRevisionOption, SWT.RADIO);
        latestButton.setLayoutData(singleData);
        revisionButton = toolkit.createButton(displayArea, "", SWT.RADIO); //$NON-NLS-1$

        revisionCombo = new Combo(displayArea, SWT.READ_ONLY | SWT.DROP_DOWN);
        revisionCombo.add(Messages.FolderDiffFilterArea_HaveRevision);
        revisionCombo.add(Messages.FolderDiffFilterArea_Date);
        revisionCombo.add(Messages.FolderDiffFilterArea_SubmittedChangelist);
        revisionCombo.add(Messages.FolderDiffFilterArea_RevisionNumber);
        revisionCombo.add(Messages.FolderDiffFilterArea_Label);
        revisionCombo.add(Messages.FolderDiffFilterArea_ClientWorkspace);

        revisionCombo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateSelectedOptions();
                updateLayout();
                updateFocus();
                displayArea.layout(true, true);
            }

        });

        createHaveArea(displayArea, toolkit);
        createDateArea(displayArea, toolkit);
        createChangelistArea(displayArea, toolkit);
        createRevisionArea(displayArea, toolkit);
        createLabelArea(displayArea, toolkit);
        createClientArea(displayArea, toolkit);

        section.addExpansionListener(new ExpansionAdapter() {

            @Override
            public void expansionStateChanged(ExpansionEvent e) {
                updateTitle();
            }
        });
        updateLayout();
        loadOptions();
        SelectionListener enableListener = new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                options.setHeadFilter(latestButton.getSelection());
                revisionCombo.setEnabled(revisionButton.getSelection());
                updateSelectedOptions();
                updateFocus();
                updateEnablement();
            }

        };
        latestButton.addSelectionListener(enableListener);
        revisionButton.addSelectionListener(enableListener);
        enableListener.widgetSelected(null);
    }

    private void updateEnablement() {
        boolean enabled = revisionButton.getSelection();
        setEnabled(enabled, haveArea);
        setEnabled(enabled, dateArea);
        setEnabled(enabled, changelistArea);
        setEnabled(enabled, revisionArea);
        setEnabled(enabled, labelArea);
        setEnabled(enabled, clientArea);
    }

    private void setEnabled(boolean enable, Control control) {
        control.setEnabled(enable);
        if (control instanceof Composite) {
            for (Control child : ((Composite) control).getChildren()) {
                setEnabled(enable, child);
            }
        }
    }

    private void setVisible(boolean visible, Composite composite) {
        composite.setVisible(visible);
        ((GridData) composite.getLayoutData()).exclude = !visible;
    }

    private void updateSelectedOptions() {
        int index = revisionCombo.getSelectionIndex();
        options.setHaveFilter(index == 0);
        options.setDateFilter(index == 1);
        options.setChangelistFilter(index == 2);
        options.setRevisionFilter(index == 3);
        options.setLabelFilter(index == 4);
        options.setClientFilter(index == 5);
    }

    private void updateFocus() {
        if (!revisionButton.getSelection()) {
            return;
        }
        if (options.isDateFilter()) {
            dateText.selectAll();
            dateText.setFocus();
        }
        if (options.isChangelistFilter()) {
            changelistText.selectAll();
            changelistText.setFocus();
        }
        if (options.isRevisionFilter()) {
            revisionSpinner.setFocus();
        }
        if (options.isLabelFilter()) {
            labelText.selectAll();
            labelText.setFocus();
        }
        if (options.isClientFilter()) {
            clientText.selectAll();
            clientText.setFocus();
        }
    }

    private void updateLayout() {
        setVisible(options.isHaveFilter(), haveArea);
        setVisible(options.isDateFilter(), dateArea);
        setVisible(options.isChangelistFilter(), changelistArea);
        setVisible(options.isRevisionFilter(), revisionArea);
        setVisible(options.isLabelFilter(), labelArea);
        setVisible(options.isClientFilter(), clientArea);
    }

    private void loadOptions() {

        if (options.isHeadFilter()) {
            latestButton.setSelection(true);
        } else {
            revisionButton.setSelection(true);
        }

        if (options.isHaveFilter()) {
            revisionCombo.select(0);
        } else if (options.isDateFilter()) {
            revisionCombo.select(1);
        } else if (options.isChangelistFilter()) {
            revisionCombo.select(2);
        } else if (options.isRevisionFilter()) {
            revisionCombo.select(3);
        } else if (options.isLabelFilter()) {
            revisionCombo.select(4);
        } else if (options.isClientFilter()) {
            revisionCombo.select(5);
        }

        changelistText.setText(options.getChangelist());
        labelText.setText(options.getLabel());
        dateText.setText(options.getDate());
        try {
            int revision = Integer.parseInt(options.getRevision());
            if (revision >= 0) {
                revisionSpinner.setSelection(revision);
            }
        } catch (NumberFormatException nfe) {
            revisionSpinner.setSelection(1);
        }
        clientText.setText(options.getClient());

        if (revisionCombo.getSelectionIndex() == -1) {
            revisionCombo.select(0);
            setVisible(true, haveArea);
        }
    }

    private void createHaveArea(Composite parent, FormToolkit toolkit) {
        haveArea = new Composite(parent, SWT.NONE);
        haveArea.setLayout(GridLayoutFactory.fillDefaults().numColumns(2)
                .create());
        haveArea.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.FILL, SWT.CENTER).grab(true, false).create());
        Label haveLabel = toolkit.createLabel(haveArea,
                Messages.FolderDiffFilterArea_HaveRevisionOption);
        haveLabel.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.FILL, SWT.CENTER).grab(true, false).create());
    }

    private void createDateArea(Composite parent, FormToolkit toolkit) {
        dateArea = new Composite(parent, SWT.NONE);
        dateArea.setLayout(GridLayoutFactory.fillDefaults().numColumns(2)
                .create());
        dateArea.setLayoutData(GridDataFactory.fillDefaults().grab(true, false)
                .create());
        DatePicker picker = new DatePicker();
        picker.createControl(dateArea);
        dateText = toolkit.createText(dateArea, ""); //$NON-NLS-1$
        dateText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                options.setDate(dateText.getText().trim());
            }
        });
        picker.setText(dateText);
        dateText.setLayoutData(GridDataFactory.fillDefaults().grab(true, false)
                .create());
    }

    private void createChangelistArea(Composite parent, FormToolkit toolkit) {
        changelistArea = new Composite(parent, SWT.NONE);
        changelistArea.setLayout(GridLayoutFactory.fillDefaults().numColumns(2)
                .create());
        changelistArea.setLayoutData(GridDataFactory.fillDefaults()
                .grab(true, false).create());

        ToolBar toolbar = new ToolBar(changelistArea, SWT.FLAT);
        toolkit.adapt(toolbar, false, false);
        ToolItem browse = new ToolItem(toolbar, SWT.PUSH);
        Image browseImage = PerforceUIPlugin.getDescriptor(
                IPerforceUIConstants.IMG_FIND).createImage();
        P4UIUtils.registerDisposal(browse, browseImage);
        browse.setImage(browseImage);
        browse.setToolTipText(Messages.FolderDiffFilterArea_BrowseChangelists);
        browse.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                SubmittedChangelistDialog dialog = new SubmittedChangelistDialog(
                        changelistArea.getShell(), connection);
                dialog.setSingle(true);
                if (SubmittedChangelistDialog.OK == dialog.open()) {
                    IP4SubmittedChangelist[] list = dialog.getSelected();
                    if (list.length == 1) {
                        changelistText.setText(Integer.toString(list[0].getId()));
                    }
                }
            }
        });

        changelistText = toolkit.createText(changelistArea, ""); //$NON-NLS-1$
        changelistText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                options.setChangelist(changelistText.getText().trim());
            }
        });
        changelistText.addVerifyListener(new VerifyListener() {

            public void verifyText(VerifyEvent e) {
                if (e.text.length() == 1
                        && !Character.isDigit(e.text.charAt(0))) {
                    e.doit = false;
                } else if (e.text.length() > 1) {
                    e.text = e.text.trim();
                    try {
                        Integer.parseInt(e.text);
                    } catch (NumberFormatException nfe) {
                        e.doit = false;
                    }
                }
            }
        });
        changelistText.setLayoutData(GridDataFactory.fillDefaults()
                .grab(true, false).create());
    }

    private void createLabelArea(Composite parent, FormToolkit toolkit) {
        labelArea = new Composite(parent, SWT.NONE);
        labelArea.setLayout(GridLayoutFactory.fillDefaults().numColumns(2)
                .create());
        labelArea.setLayoutData(GridDataFactory.fillDefaults()
                .grab(true, false).create());

        ToolBar toolbar = new ToolBar(labelArea, SWT.FLAT);
        toolkit.adapt(toolbar, false, false);
        ToolItem browse = new ToolItem(toolbar, SWT.PUSH);
        Image browseImage = PerforceUIPlugin.getDescriptor(
                IPerforceUIConstants.IMG_FIND).createImage();
        P4UIUtils.registerDisposal(browse, browseImage);
        browse.setImage(browseImage);
        browse.setToolTipText(Messages.FolderDiffFilterArea_BrowseLabels);
        browse.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                SelectLabelDialog dialog = new SelectLabelDialog(changelistArea
                        .getShell(), connection);
                if (SelectLabelDialog.OK == dialog.open()) {
                    IP4Label label = dialog.getSelected();
                    if (label != null) {
                        labelText.setText(label.getName());
                    }
                }
            }
        });

        labelText = toolkit.createText(labelArea, ""); //$NON-NLS-1$
        labelText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                options.setLabel(labelText.getText().trim());
            }
        });
        labelText.setLayoutData(GridDataFactory.fillDefaults()
                .grab(true, false).create());
    }

    private void createRevisionArea(Composite parent, FormToolkit toolkit) {
        revisionArea = new Composite(parent, SWT.NONE);
        revisionArea.setLayout(GridLayoutFactory.fillDefaults().numColumns(1)
                .create());
        revisionArea.setLayoutData(GridDataFactory.fillDefaults()
                .grab(false, false).create());
        revisionSpinner = new Spinner(revisionArea, SWT.BORDER);
        revisionSpinner.setMinimum(0);
        revisionSpinner.setMaximum(Integer.MAX_VALUE);
        revisionSpinner.setIncrement(1);
        toolkit.adapt(revisionSpinner, false, false);
        revisionSpinner.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                options.setRevision(Integer.toString(revisionSpinner
                        .getSelection()));
            }
        });
        revisionSpinner.setLayoutData(GridDataFactory.fillDefaults()
                .grab(false, false).create());
    }

    private void createClientArea(Composite parent, FormToolkit toolkit) {
        clientArea = new Composite(parent, SWT.NONE);
        clientArea.setLayout(GridLayoutFactory.fillDefaults().numColumns(2)
                .create());
        clientArea.setLayoutData(GridDataFactory.fillDefaults()
                .grab(true, false).create());
        clientText = toolkit.createText(clientArea, ""); //$NON-NLS-1$
        clientText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                options.setClient(clientText.getText().trim());
            }
        });
        clientText.setLayoutData(GridDataFactory.fillDefaults()
                .grab(true, false).create());
    }

    /**
     * Set expanded
     * 
     * @param expanded
     */
    public void setExpanded(boolean expanded) {
        section.setExpanded(expanded);
        updateTitle();
    }

    private void updateTitle() {
        if (section.isExpanded()) {
            section.setText(Messages.FolderDiffFilterArea_Title);
        } else {
            String suffix = ""; //$NON-NLS-1$
            if (latestButton.getSelection()) {
                suffix = Messages.FolderDiffFilterArea_LatestRevisionTitle;
            } else if (revisionButton.getSelection()) {
                int index = revisionCombo.getSelectionIndex();
                switch (index) {
                case 0:
                    suffix = Messages.FolderDiffFilterArea_HaveRevisionTitle;
                    break;
                case 1:
                    suffix = dateText.getText().trim();
                    break;
                case 2:
                    suffix = Messages.FolderDiffFilterArea_ChangelistTitle
                            + options.getChangelist();
                    break;
                case 3:
                    suffix = Messages.FolderDiffFilterArea_RevisionTitle
                            + options.getRevision();
                    break;
                case 4:
                    suffix = Messages.FolderDiffFilterArea_LabelTitle
                            + options.getLabel();
                    break;
                case 5:
                    suffix = Messages.FolderDiffFilterArea_ClientTitle
                            + options.getClient();
                    break;
                default:
                    break;
                }
            }
            section.setText(MessageFormat.format(
                    Messages.FolderDiffFilterArea_TitleCollapsed, suffix));
            section.layout(true);
        }
    }

    private String getComboFilter() {
        String filter = ""; //$NON-NLS-1$
        int index = revisionCombo.getSelectionIndex();
        switch (index) {
        case 0:
            filter = "#have"; //$NON-NLS-1$
            break;
        case 1:
            filter = "@" + dateText.getText().trim(); //$NON-NLS-1$
            break;
        case 2:
            filter = "@" + changelistText.getText().trim(); //$NON-NLS-1$
            break;
        case 3:
            filter = "#" + revisionSpinner.getSelection(); //$NON-NLS-1$
            break;
        case 4:
            filter = "@" + labelText.getText().trim(); //$NON-NLS-1$
            break;
        case 5:
            filter = "@" + clientText.getText().trim(); //$NON-NLS-1$
        default:
            break;
        }
        return filter;
    }

    /**
     * Get filter spec
     * 
     * @return filter string
     */
    public String getFilterSpec() {
        String filterSpec = null;
        if (revisionButton.getSelection()) {
            filterSpec = getComboFilter();
        }
        return filterSpec;
    }
}
