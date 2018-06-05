/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.search;

import com.perforce.team.core.mylyn.IP4MylynConstants;
import com.perforce.team.core.mylyn.P4MylynUtils;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.IErrorDisplay;
import com.perforce.team.ui.IErrorProvider;

import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositoryQueryPage;
import org.eclipse.mylyn.tasks.ui.wizards.ITaskSearchPageContainer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class P4JobQueryPage extends AbstractRepositoryQueryPage implements
        IErrorDisplay {

    private Text queryTitleText;
    private Button manualButton;
    private Button buildableButton;
    private Button limitButton;
    private Spinner limitSpinner;
    private Text queryValueText;
    private FilterManager manager;

    private String localErrorMessage = null;

    /**
     * @param taskRepository
     * @param query
     */
    public P4JobQueryPage(TaskRepository taskRepository, IRepositoryQuery query) {
        super("jobQuery", taskRepository, query); //$NON-NLS-1$
        setTitle(Messages.P4JobQueryPage_JobQuery);
        setDescription(Messages.P4JobQueryPage_SearchJobs);
    }

    /**
     * @param taskRepository
     */
    public P4JobQueryPage(TaskRepository taskRepository) {
        this(taskRepository, null);
    }

    /**
     * @see org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositoryQueryPage#setControlsEnabled(boolean)
     */
    @Override
    public void setControlsEnabled(boolean enabled) {
    }

    /**
     * @see org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositoryQueryPage#applyTo(org.eclipse.mylyn.tasks.core.IRepositoryQuery)
     */
    @Override
    public void applyTo(IRepositoryQuery query) {
        query.setUrl(getTaskRepository().getRepositoryUrl());

        query.setSummary(getQueryTitle());

        if (buildableButton.getSelection()) {
            query.setAttribute(IP4MylynConstants.P4_JOB_SEARCH_TYPE,
                    IP4MylynConstants.COMPLEX);
            String fullQuery = this.manager.getQuery();
            query.setAttribute(IP4MylynConstants.P4_JOB_QUERY, fullQuery);
            query.setAttribute(IP4MylynConstants.P4_JOB_COMPLEX_QUERY,
                    this.manager.toStorageString());

        } else {
            query.setAttribute(IP4MylynConstants.P4_JOB_SEARCH_TYPE,
                    IP4MylynConstants.SIMPLE);
            query.setAttribute(IP4MylynConstants.P4_JOB_QUERY,
                    queryValueText.getText());
        }

        if (limitButton.getSelection()) {
            int limit = limitSpinner.getSelection();
            query.setAttribute(IP4MylynConstants.P4_JOB_MAX,
                    Integer.toString(limit));
        } else {
            query.setAttribute(IP4MylynConstants.P4_JOB_MAX, null);
        }
    }

    /**
     * @see org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositoryQueryPage#getQueryTitle()
     */
    @Override
    public String getQueryTitle() {
        return inSearchContainer()
                ? Messages.P4JobQueryPage_NewSearch
                : this.queryTitleText != null
                        ? this.queryTitleText.getText()
                        : null;
    }

    /**
     * @see org.eclipse.jface.wizard.WizardPage#setPageComplete(boolean)
     */
    @Override
    public void setPageComplete(boolean complete) {
        super.setPageComplete(complete);
        ITaskSearchPageContainer container = getSearchContainer();
        if (container != null) {
            container.setPerformActionEnabled(complete);
        }
    }

    private void syncBuildable() {
        if (buildableButton.getSelection()) {
            String built = manager.getQuery();
            queryValueText.setText(built);
        }
    }

    private void loadValues() {
        boolean useDefault = true;
        IRepositoryQuery query = getQuery();
        if (query != null) {
            String type = query
                    .getAttribute(IP4MylynConstants.P4_JOB_SEARCH_TYPE);
            if (IP4MylynConstants.COMPLEX.equals(type)) {
                String fullQuery = query
                        .getAttribute(IP4MylynConstants.P4_JOB_COMPLEX_QUERY);
                manager.fromStorageString(fullQuery);
                useDefault = false;
            } else if (IP4MylynConstants.SIMPLE.equals(type)) {
                String value = query
                        .getAttribute(IP4MylynConstants.P4_JOB_QUERY);
                if (value != null) {
                    this.queryValueText.setText(value);
                }
                manager.loadDefaults();
            }
            String title = query.getSummary();
            if (title != null) {
                this.queryTitleText.setText(title);
            }

            String limitValue = query
                    .getAttribute(IP4MylynConstants.P4_JOB_MAX);
            if (limitValue != null) {
                try {
                    int limit = Integer.parseInt(limitValue);
                    limitButton.setSelection(true);
                    limitSpinner.setEnabled(true);
                    limitSpinner.setSelection(limit);
                } catch (NumberFormatException nfe) {
                    // Ignore
                }
            }
        } else {
            useDefault = false;
            manager.loadDefaults();
        }

        if (useDefault) {
            manualButton.setSelection(true);
            queryValueText.setEnabled(true);
            manager.setEnabled(false);
        } else {
            buildableButton.setSelection(true);
            queryValueText.setEnabled(false);
            manager.setEnabled(true);
            syncBuildable();
        }
    }

    /**
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        Composite displayArea = new Composite(parent, SWT.NONE);
        displayArea.setLayout(new GridLayout(1, true));
        displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        if (!inSearchContainer()) {
            Composite titleArea = new Composite(displayArea, SWT.NONE);
            GridLayout taLayout = new GridLayout(2, false);
            titleArea.setLayout(taLayout);
            titleArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                    false));

            Label titleLabel = new Label(titleArea, SWT.NONE);
            titleLabel.setText(Messages.P4JobQueryPage_QueryTitle);

            queryTitleText = new Text(titleArea, SWT.SINGLE | SWT.BORDER);
            queryTitleText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                    false));
            queryTitleText.addModifyListener(new ModifyListener() {

                public void modifyText(ModifyEvent e) {
                    setPageComplete(isPageComplete());
                }
            });
        }

        SelectionAdapter buttonAdapter = new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean manual = manualButton.getSelection();
                queryValueText.setEnabled(manual);
                manager.setEnabled(!manual);
                if (manual) {
                    setErrorMessage(null, null);
                } else {
                    manager.validate();
                }
            }

        };

        buildableButton = new Button(displayArea, SWT.RADIO);
        buildableButton.setText(Messages.P4JobQueryPage_SearchJobsWithRules);
        buildableButton.addSelectionListener(buttonAdapter);

        IP4Connection connection = P4MylynUtils
                .getConnection(getTaskRepository());

        this.manager = new FilterManager();
        this.manager.setErrorDisplay(this);
        this.manager.createControl(displayArea, connection);
        this.manager.addListener(new IFilterListener() {

            public void removed(FilterEntry removed) {
                syncBuildable();
            }

            public void modified(FilterEntry modified) {
                syncBuildable();
            }

            public void added(FilterEntry entry) {
                syncBuildable();
            }

            public void wordsModified(String words) {
                syncBuildable();
            }
        });

        manualButton = new Button(displayArea, SWT.RADIO);
        manualButton.setText(Messages.P4JobQueryPage_SeachJobsWithQuery);
        manualButton.addSelectionListener(buttonAdapter);

        queryValueText = new Text(displayArea, SWT.SINGLE | SWT.BORDER);
        GridData qvtData = new GridData(SWT.FILL, SWT.FILL, true, false);
        qvtData.horizontalIndent = 15;
        queryValueText.setLayoutData(qvtData);

        Composite limitArea = new Composite(displayArea, SWT.NONE);
        GridLayout laLayout = new GridLayout(2, false);
        laLayout.marginHeight = 0;
        laLayout.marginWidth = 0;
        limitArea.setLayout(laLayout);
        limitArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        limitButton = new Button(limitArea, SWT.CHECK);
        limitButton.setText(Messages.P4JobQueryPage_LimitResults);
        limitButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                limitSpinner.setEnabled(limitButton.getSelection());
            }

        });

        limitSpinner = new Spinner(limitArea, SWT.NONE);
        limitSpinner.setMinimum(1);
        limitSpinner.setMaximum(Integer.MAX_VALUE);
        limitSpinner.setSelection(100);
        limitSpinner.setEnabled(false);

        setControl(displayArea);

        loadValues();

        if(connection.isOffline())
        	setErrorMessage(Messages.P4JobQueryPage_OfflineError,null);
        
        setPageComplete(isPageComplete());
    }

    /**
     * @see org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositoryQueryPage#isPageComplete()
     */
    @Override
    public boolean isPageComplete() {
        if (this.localErrorMessage != null) {
            setErrorMessage(this.localErrorMessage);
            return false;
        }
        return super.isPageComplete();
    }

    /**
     * @see com.perforce.team.ui.IErrorDisplay#setErrorMessage(java.lang.String,
     *      com.perforce.team.ui.IErrorProvider)
     */
    public void setErrorMessage(String message, IErrorProvider provider) {
        this.localErrorMessage = message;
        setPageComplete(isPageComplete());
    }

    /**
     * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        setPageComplete(isPageComplete());
    }

}
