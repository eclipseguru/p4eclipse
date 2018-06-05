/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.charts.diff;

import com.perforce.team.ui.charts.ChartCanvas;
import com.perforce.team.ui.charts.ChartUtils;
import com.perforce.team.ui.folder.diff.editor.FolderDiffPage;

import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.ChartWithoutAxes;
import org.eclipse.birt.chart.model.attribute.Anchor;
import org.eclipse.birt.chart.model.attribute.ChartDimension;
import org.eclipse.birt.chart.model.attribute.LegendItemType;
import org.eclipse.birt.chart.model.component.Series;
import org.eclipse.birt.chart.model.data.SeriesDefinition;
import org.eclipse.birt.chart.model.data.impl.SeriesDefinitionImpl;
import org.eclipse.birt.chart.model.impl.ChartWithoutAxesImpl;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Diff2OverviewChartPage extends Diff2ChartPage {

    private ChartCanvas typeChart;
    private ChartData typeChartData;

    /**
     * @param editor
     * @param diffPage
     */
    public Diff2OverviewChartPage(FormEditor editor, FolderDiffPage diffPage) {
        super(
                editor,
                "diff2AuthorChartPage", Messages.Diff2OverviewChartPage_ChartTitle, diffPage); //$NON-NLS-1$
    }

    /**
     * @see com.perforce.team.ui.charts.diff.Diff2ChartPage#createNumberSeries(double[])
     */
    @Override
    protected Series createNumberSeries(double[] values) {
        return ChartUtils.createPieNumberSeries(values);
    }

    private ChartCanvas createTypeChart(Composite parent) {
        ChartCanvas chart = new ChartCanvas() {

            @Override
            protected boolean shouldDraw() {
                return container != null;
            }

            @Override
            protected Chart createChart() {
                ChartWithoutAxes chart = ChartWithoutAxesImpl.create();
                chart.setDimension(ChartDimension.TWO_DIMENSIONAL_LITERAL);
                chart.getTitle().setVisible(false);
                chart.getLegend().setAnchor(Anchor.NORTH_EAST_LITERAL);
                chart.getLegend()
                        .setItemType(LegendItemType.CATEGORIES_LITERAL);

                SeriesDefinition xDefinition = ChartUtils
                        .createSeriesDefinition(chart);

                ChartPoint unique = new ChartPoint(
                        Messages.Diff2ChartPage_Unique);
                unique.set(container.getUniqueCount());
                ChartPoint identical = new ChartPoint(
                        Messages.Diff2ChartPage_Identical);
                identical.set(container.getIdenticalCount());
                ChartPoint differing = new ChartPoint(
                        Messages.Diff2ChartPage_Differing);
                differing.set(container.getContentCount());
                typeChartData = new ChartData(unique, identical, differing);

                Series seValues = createTextSeries(typeChartData);
                xDefinition.getSeries().add(seValues);

                SeriesDefinition yDefinition = SeriesDefinitionImpl.create();

                yDefinition.getSeries().add(createNumberSeries(typeChartData));
                xDefinition.getSeriesDefinitions().add(yDefinition);

                return chart;
            }

        };
        chart.createControl(parent);
        return chart;
    }

    /**
     * @see org.eclipse.ui.forms.editor.FormPage#createFormContent(org.eclipse.ui.forms.IManagedForm)
     */
    @Override
    protected void createFormContent(IManagedForm managedForm) {
        Composite body = managedForm.getForm().getBody();
        FormToolkit toolkit = managedForm.getToolkit();

        body.setLayout(GridLayoutFactory.swtDefaults().numColumns(1).create());

        Section typeSection = toolkit.createSection(body,
                ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR
                        | Section.DESCRIPTION);
        typeSection.setLayoutData(GridDataFactory.fillDefaults()
                .grab(true, true).create());
        typeSection.setText(Messages.Diff2ChartPage_DiffTypeChartTitle);
        typeSection
                .setDescription(Messages.Diff2ChartPage_DiffTypeChartDescription);

        Composite typeArea = toolkit.createComposite(typeSection);
        typeSection.setClient(typeArea);
        typeArea.setLayout(GridLayoutFactory.swtDefaults().create());
        typeArea.setLayoutData(GridDataFactory.fillDefaults().grab(true, true)
                .create());

        this.typeChart = createTypeChart(typeArea);

        initialize();
    }

    /**
     * @see com.perforce.team.ui.charts.diff.Diff2ChartPage#updateHeight()
     */
    @Override
    protected void updateHeight() {
        // Do nothing
    }

    /**
     * @see com.perforce.team.ui.charts.diff.Diff2ChartPage#getChart()
     */
    protected ChartCanvas getChart() {
        return this.typeChart;
    }

    /**
     * @see com.perforce.team.ui.charts.diff.Diff2ChartPage#getChartData()
     */
    protected ChartData getChartData() {
        return this.typeChartData;
    }

}
