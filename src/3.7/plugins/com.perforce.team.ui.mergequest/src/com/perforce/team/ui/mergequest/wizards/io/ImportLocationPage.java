/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.wizards.io;

import com.perforce.team.core.mergequest.builder.DepotPathBranchGraphBuilder;
import com.perforce.team.core.mergequest.builder.FileBranchGraphBuilder;
import com.perforce.team.core.mergequest.builder.HttpBranchGraphBuilder;
import com.perforce.team.core.mergequest.builder.IBranchGraphBuilder;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Container;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.mergequest.BranchGraphUtils;
import com.perforce.team.ui.views.SessionManager;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.wizard.WizardPage;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ImportLocationPage extends WizardPage {

    /**
     * HTTP_PATHS
     */
    public static final String HTTP_PATHS = "io.HTTP_PATHS"; //$NON-NLS-1$

    /**
     * DEPOT_PATHS
     */
    public static final String DEPOT_PATHS = "io.DEPOT_PATHS"; //$NON-NLS-1$

    /**
     * PATH_COUNT
     */
    public static final int PATH_COUNT = 10;

    /**
     * HTTP_PREFIX
     */
    public static final String HTTP_PREFIX = "http://"; //$NON-NLS-1$

    private Composite displayArea;

    private Button fileButton;
    private Combo fileCombo;
    private ToolItem fileBrowseButton;

    private Button urlButton;
    private Combo urlCombo;

    private Button depotButton;
    private Combo depotCombo;
    private ToolItem depotBrowseButton;

    private IP4Connection connection;
    private String path;

    /**
     * @param connection
     */
    public ImportLocationPage(IP4Connection connection) {
        super("importLocation"); //$NON-NLS-1$
        setTitle(Messages.ImportLocationPage_Title);
        setDescription(Messages.ImportLocationPage_Description);
        this.connection = connection;
    }

    /**
     * Save history
     */
    public void saveHistory() {
        if (P4UIUtils.okToUse(fileCombo)) {
            SessionManager.saveComboHistory(fileCombo, PATH_COUNT,
                    ExportGraphSelectionPage.PATHS);
            SessionManager.saveComboHistory(urlCombo, PATH_COUNT, HTTP_PATHS);
            SessionManager
                    .saveComboHistory(depotCombo, PATH_COUNT, DEPOT_PATHS);
        }
    }

    private void validate() {
        String message = null;
        if (message == null) {
            if (path.length() == 0) {
                if (fileButton.getSelection()) {
                    message = Messages.ImportLocationPage_EnterFilePath;
                } else if (urlButton.getSelection()) {
                    message = Messages.ImportLocationPage_EnterHttpUrl;
                } else if (depotButton.getSelection()) {
                    message = Messages.ImportLocationPage_EnterDepotPath;
                }
            } else {
                if (fileButton.getSelection()) {
                    File file = new File(path);
                    if (!file.exists()) {
                        message = Messages.ImportLocationPage_FileDoesNotExist;
                    } else if (!file.isFile()) {
                        message = Messages.ImportLocationPage_EnterPathToFile;
                    }
                } else if (urlButton.getSelection()) {
                    if (!path.startsWith("http://") //$NON-NLS-1$
                            && !path.startsWith("https://")) { //$NON-NLS-1$
                        message = Messages.ImportLocationPage_EnterValidUrl;
                    } else {
                        try {
                            URL url = new URL(path);
                            String host = url.getHost();
                            if (host == null || host.trim().length() == 0) {
                                throw new MalformedURLException();
                            }
                        } catch (MalformedURLException e) {
                            message = Messages.ImportLocationPage_UrlMalformed;
                        }
                    }
                } else if (depotButton.getSelection()) {
                    if (!path.startsWith(IP4Container.DEPOT_PREFIX)) {
                        message = Messages.ImportLocationPage_DepotPathMalformed;
                    }
                }
            }
        }
        setPageComplete(message == null);
        setErrorMessage(message);
    }

    /**
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        displayArea = new Composite(parent, SWT.NONE);
        GridLayout daLayout = new GridLayout(1, true);
        displayArea.setLayout(daLayout);
        GridData daData = new GridData(SWT.FILL, SWT.FILL, true, true);
        displayArea.setLayoutData(daData);

        new Label(displayArea, SWT.NONE)
                .setText(Messages.ImportLocationPage_SelectImportMethod);

        SelectionAdapter validateAdapter = new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (fileButton.getSelection()) {
                    path = fileCombo.getText();
                    validate();
                } else if (urlButton.getSelection()) {
                    path = urlCombo.getText();
                    validate();
                } else if (depotButton.getSelection()) {
                    path = depotCombo.getText();
                    validate();
                }
            }

        };

        fileButton = new Button(displayArea, SWT.RADIO);
        fileButton.setText(Messages.ImportLocationPage_FromFile);
        fileButton.addSelectionListener(validateAdapter);

        int areaIndent = 20;
        int areaSpacing = 5;

        Composite fileArea = new Composite(displayArea, SWT.NONE);
        GridLayout faLayout = new GridLayout(3, false);
        faLayout.marginLeft = areaIndent;
        faLayout.marginBottom = areaSpacing;
        faLayout.marginWidth = 0;
        faLayout.marginHeight = 0;
        fileArea.setLayout(faLayout);
        fileArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        new Label(fileArea, SWT.NONE).setText(Messages.ImportLocationPage_Path);
        fileCombo = new Combo(fileArea, SWT.SINGLE | SWT.DROP_DOWN);
        SessionManager.loadComboHistory(fileCombo,
                ExportGraphSelectionPage.PATHS);

        final Runnable fileComboRunnable = new Runnable() {

            public void run() {
                if (fileButton.getSelection()) {
                    path = fileCombo.getText();
                    validate();
                }
            }
        };
        fileCombo.addSelectionListener(P4UIUtils
                .createComboSelectionListener(fileComboRunnable));
        fileCombo.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                fileComboRunnable.run();
            }
        });

        fileCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Image browseImage = PerforceUIPlugin.getDescriptor(
                IPerforceUIConstants.IMG_FIND).createImage();
        P4UIUtils.registerDisposal(displayArea, browseImage);

        ToolBar fToolbar = new ToolBar(fileArea, SWT.FLAT);
        fileBrowseButton = new ToolItem(fToolbar, SWT.PUSH);
        fileBrowseButton.setImage(browseImage);
        fileBrowseButton.setToolTipText(Messages.ImportLocationPage_Browse);
        fileBrowseButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = new FileDialog(displayArea.getShell(),
                        SWT.OPEN);
                String selected = dialog.open();
                if (selected != null) {
                    fileCombo.setText(selected);
                }
            }

        });
        SessionManager.loadComboHistory(fileCombo,
                ExportGraphSelectionPage.PATHS);

        urlButton = new Button(displayArea, SWT.RADIO);
        urlButton.setText(Messages.ImportLocationPage_FromHttp);
        urlButton.addSelectionListener(validateAdapter);

        Composite urlArea = new Composite(displayArea, SWT.NONE);
        GridLayout uaLayout = new GridLayout(2, false);
        uaLayout.marginLeft = areaIndent;
        uaLayout.marginBottom = areaSpacing;
        uaLayout.marginWidth = 0;
        uaLayout.marginHeight = 0;
        urlArea.setLayout(uaLayout);
        urlArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        new Label(urlArea, SWT.NONE)
                .setText(Messages.ImportLocationPage_Location);
        urlCombo = new Combo(urlArea, SWT.SINGLE | SWT.DROP_DOWN);

        final Runnable urlComboRunnable = new Runnable() {

            public void run() {
                if (urlButton.getSelection()) {
                    path = urlCombo.getText();
                    validate();
                }
            }
        };
        urlCombo.addSelectionListener(P4UIUtils
                .createComboSelectionListener(urlComboRunnable));
        urlCombo.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                urlComboRunnable.run();
            }
        });

        urlCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        urlCombo.setText("http://"); //$NON-NLS-1$
        urlCombo.setEnabled(false);
        SessionManager.loadComboHistory(urlCombo, HTTP_PATHS);

        depotButton = new Button(displayArea, SWT.RADIO);
        depotButton.setText(Messages.ImportLocationPage_FromDepot
                + this.connection.getParameters().getPort());
        depotButton.addSelectionListener(validateAdapter);

        Composite depotArea = new Composite(displayArea, SWT.NONE);
        GridLayout depotAreaLayout = new GridLayout(3, false);
        depotAreaLayout.marginLeft = areaIndent;
        depotAreaLayout.marginBottom = areaSpacing;
        depotAreaLayout.marginWidth = 0;
        depotAreaLayout.marginHeight = 0;
        depotArea.setLayout(depotAreaLayout);
        depotArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        new Label(depotArea, SWT.NONE)
                .setText(Messages.ImportLocationPage_Path);
        depotCombo = new Combo(depotArea, SWT.SINGLE | SWT.DROP_DOWN);
        depotCombo.setText("//"); //$NON-NLS-1$

        final Runnable depotComboRunnable = new Runnable() {

            public void run() {
                if (depotButton.getSelection()) {
                    path = depotCombo.getText();
                    validate();
                }
            }
        };
        depotCombo.addSelectionListener(P4UIUtils
                .createComboSelectionListener(depotComboRunnable));
        depotCombo.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                depotComboRunnable.run();
            }
        });

        depotCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        depotCombo.setEnabled(false);
        SessionManager.loadComboHistory(depotCombo, DEPOT_PATHS);

        ToolBar dToolbar = new ToolBar(depotArea, SWT.FLAT);
        depotBrowseButton = new ToolItem(dToolbar, SWT.PUSH);
        depotBrowseButton.setImage(browseImage);
        depotBrowseButton.setToolTipText(Messages.ImportLocationPage_Browse);
        BranchGraphUtils.configureDepotBrowseButton(connection,
                depotBrowseButton, depotCombo);
        depotBrowseButton.setEnabled(false);

        SelectionAdapter adapter = new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                fileCombo.setEnabled(fileButton.getSelection());
                fileBrowseButton.setEnabled(fileButton.getSelection());
                urlCombo.setEnabled(urlButton.getSelection());
                depotCombo.setEnabled(depotButton.getSelection());
                depotBrowseButton.setEnabled(depotButton.getSelection());

                if (fileButton.getSelection()) {
                    path = fileCombo.getText();
                }
                if (urlButton.getSelection()) {
                    path = urlCombo.getText();
                }
                if (depotButton.getSelection()) {
                    path = depotCombo.getText();
                }

                validate();
            }

        };

        fileButton.addSelectionListener(adapter);
        urlButton.addSelectionListener(adapter);
        depotButton.addSelectionListener(adapter);

        fileButton.setSelection(true);
        fileCombo.setFocus();

        setPageComplete(false);
        setControl(displayArea);
    }

    /**
     * Create a new branch graph builder for the currently selected button
     * 
     * @return - builder
     */
    public IBranchGraphBuilder getBuilder() {
        IBranchGraphBuilder builder = null;
        if (fileButton.getSelection()) {
            builder = new FileBranchGraphBuilder(path);
        } else if (urlButton.getSelection()) {
            builder = new HttpBranchGraphBuilder(path);
        } else if (depotButton.getSelection()) {
            builder = new DepotPathBranchGraphBuilder(connection, path);
        }
        return builder;
    }

}
