/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.preferences;

import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.dialogs.PerforceDialog;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class AdvancedPreferencePage extends PreferencePage implements
        IWorkbenchPreferencePage {

    /**
     * ID
     */
    public static final String ID = "com.perforce.team.ui.preferences.AdvancedPreferencePage"; //$NON-NLS-1$

    private static class PairDialog extends PerforceDialog {

        private Text nameText;
        private Text valueText;
        private String name = null;
        private String value = null;

        /**
         * @param parent
         */
        public PairDialog(Shell parent) {
            super(parent, Messages.AdvancedPreferencePage_AddProperty);
        }

        /**
         * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
         */
        @Override
        protected Control createDialogArea(Composite parent) {
            Composite c = (Composite) super.createDialogArea(parent);
            c.setLayout(new GridLayout(2, false));
            createLabel(c, Messages.AdvancedPreferencePage_NameLabel);
            nameText = createTextField(c);
            createLabel(c, Messages.AdvancedPreferencePage_ValueLabel);
            valueText = createTextField(c);
            return c;
        }

        /**
         * @see org.eclipse.jface.dialogs.Dialog#okPressed()
         */
        @Override
        protected void okPressed() {
            name = nameText.getText().trim();
            value = valueText.getText().trim();
            super.okPressed();
        }

        public String getName() {
            return this.name;
        }

        public String getValue() {
            return this.value;
        }

    }

    private Composite displayArea;

    private Label propertiesLabel;
    private TableViewer propertiesTable;
    private Button addButton;
    private Button removeButton;
    private Image addImage;
    private Image removeImage;

    private Map<String, String> properties;

    /**
     * Create an advanced preferenc page
     */
    public AdvancedPreferencePage() {
        setDescription(Messages.AdvancedPreferencePage_AdvancedP4EclipseSettings);
    }

    /**
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        displayArea = new Composite(parent, SWT.NONE);
        displayArea.setLayout(new GridLayout(1, true));
        displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createPropertyTable(displayArea);

        return displayArea;
    }

    /**
     * @see org.eclipse.jface.dialogs.DialogPage#dispose()
     */
    @Override
    public void dispose() {
        super.dispose();
        addImage.dispose();
        removeImage.dispose();
    }

    private void createPropertyTable(Composite parent) {
        Composite tableArea = new Composite(parent, SWT.NONE);
        tableArea.setLayout(new GridLayout(2, false));
        tableArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        propertiesLabel = new Label(tableArea, SWT.LEFT);
        GridData plData = new GridData(SWT.FILL, SWT.FILL, true, false);
        plData.verticalIndent = 10;
        plData.horizontalSpan = 2;
        propertiesLabel.setLayoutData(plData);
        propertiesLabel
                .setText(Messages.AdvancedPreferencePage_CustonP4JavaProperties);

        propertiesTable = new TableViewer(tableArea, SWT.FULL_SELECTION
                | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI);
        TableLayout ptLayout = new TableLayout();
        ptLayout.addColumnData(new ColumnWeightData(50, true));
        ptLayout.addColumnData(new ColumnWeightData(50, true));
        TableColumn nameColumn = new TableColumn(propertiesTable.getTable(),
                SWT.NONE);
        nameColumn.setText(Messages.AdvancedPreferencePage_Name);
        TableColumn valueColumn = new TableColumn(propertiesTable.getTable(),
                SWT.NONE);
        valueColumn.setText(Messages.AdvancedPreferencePage_Value);
        propertiesTable.getTable().setLayout(ptLayout);
        propertiesTable.getTable().setHeaderVisible(true);
        propertiesTable.getTable().setLinesVisible(true);
        propertiesTable.setContentProvider(new ArrayContentProvider());
        propertiesTable.setLabelProvider(new ITableLabelProvider() {

            public void removeListener(ILabelProviderListener listener) {

            }

            public boolean isLabelProperty(Object element, String property) {
                return false;
            }

            public void dispose() {

            }

            public void addListener(ILabelProviderListener listener) {

            }

            public String getColumnText(Object element, int columnIndex) {
                String value = null;
                if (columnIndex == 0) {
                    value = element.toString();
                } else if (columnIndex == 1) {
                    value = properties.get(element);
                }
                if (value == null) {
                    value = ""; //$NON-NLS-1$
                }
                return value;
            }

            public Image getColumnImage(Object element, int columnIndex) {
                return null;
            }
        });
        propertiesTable.getTable().setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, true, true));
        propertiesTable.setSorter(new ViewerSorter());
        Composite buttons = new Composite(tableArea, SWT.NONE);
        buttons.setLayout(new GridLayout(1, true));
        buttons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        addButton = new Button(buttons, SWT.PUSH);
        addImage = PerforceUIPlugin.getPlugin()
                .getImageDescriptor(IPerforceUIConstants.IMG_ADD).createImage();
        addButton
                .setToolTipText(Messages.AdvancedPreferencePage_AddPropertyTooltip);
        addButton.setImage(addImage);
        addButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                PairDialog dialog = new PairDialog(addButton.getShell());
                if (PairDialog.OK == dialog.open()) {
                    String name = dialog.getName();
                    String value = dialog.getValue();
                    properties.put(name, value);
                    propertiesTable.refresh();
                }
            }

        });
        removeButton = new Button(buttons, SWT.PUSH);
        removeImage = PerforceUIPlugin.getPlugin()
                .getImageDescriptor(IPerforceUIConstants.IMG_DELETE)
                .createImage();
        removeButton.setImage(removeImage);
        removeButton
                .setToolTipText(Messages.AdvancedPreferencePage_RemovePropertyTooltip);
        removeButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection selection = (IStructuredSelection) propertiesTable
                        .getSelection();
                for (Object key : selection.toArray()) {
                    properties.remove(key);
                }
                propertiesTable.refresh();
            }
        });

        loadProperties();
    }

    private void loadProperties() {
        properties = new HashMap<String, String>();
        String current = getPreferenceStore().getString(
                IPreferenceConstants.CUSTOM_P4JAVA_PROPERTIES);
        String[] pairs = current.split(IPreferenceConstants.VALUE_DELIMITER);
        for (String pair : pairs) {
            String[] sections = pair.split(IPreferenceConstants.PAIR_DELIMITER);
            if (sections.length == 2) {
                properties.put(sections[0], sections[1]);
            }
        }
        propertiesTable.setInput(properties.keySet());
    }

    private void saveProperties() {
        StringBuilder pairs = new StringBuilder();
        for (String key : properties.keySet()) {
            String value = properties.get(key);
            pairs.append(key);
            pairs.append(IPreferenceConstants.PAIR_DELIMITER);
            pairs.append(value);
            pairs.append(IPreferenceConstants.VALUE_DELIMITER);
        }
        getPreferenceStore()
                .setValue(IPreferenceConstants.CUSTOM_P4JAVA_PROPERTIES,
                        pairs.toString());

    }

    /**
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        properties.clear();
        propertiesTable.refresh();
        super.performDefaults();
    }

    /**
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        saveProperties();
        return super.performOk();
    }

    /**
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench) {
        setPreferenceStore(PerforceUIPlugin.getPlugin().getPreferenceStore());
    }

}
