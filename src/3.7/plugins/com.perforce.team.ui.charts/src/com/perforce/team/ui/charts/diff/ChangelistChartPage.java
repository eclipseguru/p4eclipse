/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.charts.diff;

import com.perforce.p4java.CharsetDefs;
import com.perforce.p4java.core.file.DiffType;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.option.server.DescribeOptions;
import com.perforce.p4java.server.IServer;
import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4ShelvedChangelist;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.charts.ChartCanvas;
import com.perforce.team.ui.charts.ChartUtils;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.MessageFormat;

import org.eclipse.birt.chart.model.Chart;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ChangelistChartPage extends FormPage {

    /**
     * ID - editor page id
     */
    public static final String ID = "changelistDiffChart"; //$NON-NLS-1$

    private boolean displayed = false;
    private int displayCount = 0;
    private int totalCount = 0;

    private Section graphSection;
    private Composite displayArea;
    private Composite loadingArea;
    private CLabel loadingLabel;
    private ChartCanvas canvas;
    private InputStream stream;
    private Charset charset=CharsetDefs.DEFAULT;

    /**
     * @param editor
     */
    public ChangelistChartPage(FormEditor editor) {
        super(editor, ID, Messages.ChangelistChartPage_DiffChart);
    }

    /**
     * @see org.eclipse.ui.forms.editor.FormPage#createFormContent(org.eclipse.ui.forms.IManagedForm)
     */
    @Override
    protected void createFormContent(IManagedForm managedForm) {
        FormToolkit toolkit = managedForm.getToolkit();
        Composite body = managedForm.getForm().getBody();
        body.setLayout(new GridLayout(1, true));
        graphSection = toolkit.createSection(body,
                ExpandableComposite.TITLE_BAR | Section.DESCRIPTION);
        graphSection.setText(Messages.ChangelistChart_LineDifferencesPerFile);
        graphSection
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout sLayout = new GridLayout(1, true);
        sLayout.marginWidth = 0;
        graphSection.setLayout(sLayout);

        displayArea = toolkit.createComposite(graphSection);
        GridLayout areaLayout = new GridLayout(1, true);
        areaLayout.marginHeight = 0;
        areaLayout.marginWidth = 0;
        displayArea.setLayout(areaLayout);
        displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        graphSection.setClient(displayArea);

        createLoadingArea(toolkit, displayArea);

        canvas = new ChartCanvas() {

            @Override
            protected boolean shouldDraw() {
                return displayed && stream != null;
            }

            @Override
            protected Chart createChart() {
                ChangelistChart chart = new ChangelistChart();
                Chart changeChart = chart.build(stream, charset);
                displayCount = chart.getDisplayedFileCount();
                totalCount = chart.getTotalFileCount();
                return changeChart;
            }
        };
        canvas.createControl(displayArea);
        toolkit.adapt(canvas.getControl());
    }

    private void updateHeight() {
        ChartUtils.updateHeight(canvas, displayCount);
    }

    private void createLoadingArea(FormToolkit toolkit, Composite parent) {
        loadingArea = toolkit.createComposite(parent);
        loadingArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout laLayout = new GridLayout(1, true);
        loadingArea.setLayout(laLayout);

        loadingLabel = new CLabel(loadingArea, SWT.NONE);
        toolkit.adapt(loadingLabel, false, false);
        loadingLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true,
                true));

        Image loadingImage = PerforceUIPlugin.getDescriptor(
                IPerforceUIConstants.IMG_LOADING).createImage();
        P4UIUtils.registerDisposal(loadingLabel, loadingImage);
        loadingLabel.setImage(loadingImage);
    }

    private IP4Changelist getChangelist() {
        return P4CoreUtils.convert(getEditor().getEditorInput(),
                IP4Changelist.class);
    }

    private void showLoading(boolean visible) {
        loadingArea.setVisible(visible);
        ((GridData) loadingArea.getLayoutData()).exclude = !visible;

        if (visible) {
            canvas.getControl().setVisible(false);
            ((GridData) canvas.getControl().getLayoutData()).exclude = true;
        } else {
            boolean files = displayCount > 0;
            canvas.getControl().setVisible(files);
            ((GridData) canvas.getControl().getLayoutData()).exclude = !files;
            if (files) {
                if (displayCount >= totalCount) {
                    graphSection
                            .setDescription(Messages.ChangelistChartPage_SectionDescription);
                } else {
                    graphSection
                            .setDescription(MessageFormat
                                    .format(Messages.ChangelistChartPage_LimitedSectionDescription,
                                            displayCount, totalCount));
                }
            } else {
                graphSection
                        .setDescription(Messages.ChangelistChartPage_EmptyDescription);
            }
        }
        displayArea.layout(true, true);
    }

    private void load() {
        final IP4Changelist changelist = getChangelist();
        if (changelist == null) {
            return;
        }
        final boolean shelved = changelist instanceof IP4ShelvedChangelist;
        final String id = Integer.toString(changelist.getId());
        loadingLabel.setText(MessageFormat.format(
                Messages.ChangelistChartPage_BuildingDiffChart, id));
        showLoading(true);
        P4Runner.schedule(new P4Runnable() {

			@Override
            public void run(IProgressMonitor monitor) {
                IServer server = changelist.getServer();
                if (server == null) {
                    return;
                }
                try {
                    DescribeOptions options = new DescribeOptions(
                            DiffType.SUMMARY_DIFF, shelved);
                    stream = server.getChangelistDiffsStream(
                            changelist.getId(), options);
                    charset = ConnectionParameters.getJavaCharset(changelist);
                } catch (P4JavaException e) {
                    PerforceProviderPlugin.logError(e);
                } catch (P4JavaError e) {
                    PerforceProviderPlugin.logError(e);
                }
                if (stream != null) {
                    canvas.buildChart();
                    PerforceUIPlugin.syncExec(new Runnable() {

                        public void run() {
                            if (P4UIUtils.okToUse(displayArea)) {
                                updateHeight();
                                showLoading(false);
                                canvas.redraw(false);
                                getManagedForm().reflow(true);
                            }
                        }
                    });
                }
            }

            @Override
            public String getTitle() {
                return MessageFormat.format(
                        Messages.ChangelistChartPage_LoadingDiffs, id);
            }
        });
    }

    /**
     * @see org.eclipse.ui.forms.editor.FormPage#setActive(boolean)
     */
    @Override
    public void setActive(boolean active) {
        if (active) {
            if (!displayed) {
                load();
            }
            displayed = true;
        }
        super.setActive(active);
    }

}
