/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.charts.diff;

import com.perforce.team.ui.P4FormUIUtils;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.charts.ChartCanvas;
import com.perforce.team.ui.charts.ChartUtils;
import com.perforce.team.ui.folder.diff.editor.FolderDiffPage;
import com.perforce.team.ui.folder.diff.model.FileDiffContainer;
import com.perforce.team.ui.folder.diff.model.IFolderDiffListener;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.ChartWithAxes;
import org.eclipse.birt.chart.model.attribute.Anchor;
import org.eclipse.birt.chart.model.attribute.ChartDimension;
import org.eclipse.birt.chart.model.attribute.Fill;
import org.eclipse.birt.chart.model.attribute.IntersectionType;
import org.eclipse.birt.chart.model.attribute.LegendItemType;
import org.eclipse.birt.chart.model.attribute.Orientation;
import org.eclipse.birt.chart.model.attribute.Palette;
import org.eclipse.birt.chart.model.attribute.Position;
import org.eclipse.birt.chart.model.attribute.impl.ColorDefinitionImpl;
import org.eclipse.birt.chart.model.attribute.impl.PaletteImpl;
import org.eclipse.birt.chart.model.component.Axis;
import org.eclipse.birt.chart.model.component.Series;
import org.eclipse.birt.chart.model.data.SeriesDefinition;
import org.eclipse.birt.chart.model.impl.ChartWithAxesImpl;
import org.eclipse.birt.chart.util.FillUtil;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.layout.GridData;
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
public abstract class Diff2ChartPage extends FormPage implements
        IFolderDiffListener {

    /**
     * Chart data class
     */
    protected static class ChartData {

        private Map<String, ChartPoint> points = new HashMap<String, ChartPoint>();
        private ChartPoint[] sorted = null;

        /**
         * Create empty chart data
         */
        public ChartData() {

        }

        /**
         * Creat chart data with specified points
         * 
         * @param points
         */
        public ChartData(ChartPoint... points) {
            for (ChartPoint point : points) {
                add(point);
            }
        }

        /**
         * Get chart data size
         * 
         * @return size
         */
        public int getSize() {
            return this.points.size();
        }

        /**
         * Is chart data valid to display?
         * 
         * @return true if valid, false otherwise
         */
        public boolean isValid() {
            for (ChartPoint point : getPoints()) {
                if (point.value > 0) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Add point
         * 
         * @param point
         */
        public void add(ChartPoint point) {
            if (point != null) {
                this.points.put(point.name, point);
                sorted = null;
            }
        }

        /**
         * Get points
         * 
         * @return non-null but possibly empty array of points
         */
        public ChartPoint[] getPoints() {
            if (sorted == null) {
                sorted = this.points.values().toArray(
                        new ChartPoint[points.size()]);
                Arrays.sort(sorted, new Comparator<ChartPoint>() {

                    public int compare(ChartPoint o1, ChartPoint o2) {
                        int value = (int) (o1.value - o2.value);
                        if (value == 0) {
                            value = o2.name.compareToIgnoreCase(o1.name);
                        }
                        return value;
                    }
                });
            }
            return sorted;
        }

        /**
         * Get point by name
         * 
         * @param name
         * @return point
         */
        public ChartPoint get(String name) {
            ChartPoint point = null;
            if (name != null) {
                point = this.points.get(name);
                if (point == null) {
                    point = new ChartPoint(name);
                    add(point);
                }
            }
            return point;
        }

        /**
         * Get point names
         * 
         * @return non-null but possibly empty array of names
         */
        public String[] getNames() {
            ChartPoint[] chartPoints = getPoints();
            String[] names = new String[chartPoints.length];
            for (int i = 0; i < names.length; i++) {
                names[i] = chartPoints[i].name;
            }
            return names;
        }

        /**
         * Get values
         * 
         * @return non-null but possibly empty array of values
         */
        public double[] getValues() {
            ChartPoint[] chartPoints = getPoints();
            double[] values = new double[chartPoints.length];
            for (int i = 0; i < values.length; i++) {
                values[i] = chartPoints[i].value;
            }
            return values;
        }

    }

    /**
     * Chart point class
     */
    protected static class ChartPoint implements Comparable<ChartPoint> {

        String name;
        double value;
        Fill fill;

        /**
         * Create chart point with specified name
         * 
         * @param name
         */
        public ChartPoint(String name) {
            Assert.isNotNull(name, "Name cannot be null"); //$NON-NLS-1$
            this.name = name;
            this.value = 0.0;
        }

        /**
         * Set value
         * 
         * @param value
         */
        public void set(double value) {
            if (value < 0.0) {
                value = 0.0;
            }
            this.value = value;
        }

        /**
         * Increment the value
         */
        public void increment() {
            ++this.value;
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof ChartPoint) {
                return name.equals(((ChartPoint) obj).name);
            }
            return false;
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return name.hashCode();
        }

        /**
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(ChartPoint o) {
            return name.compareTo(o.name);
        }

    }

    /**
     * Folder diff page
     */
    protected FolderDiffPage diffPage;

    /**
     * File diff container
     */
    protected FileDiffContainer container;

    private Section diffSection;
    private ChartData diffChartData;
    private ChartCanvas diffChart;
    private Section uniqueSection;
    private ChartData uniqueChartData;
    private ChartCanvas uniqueChart;

    /**
     * Diff palette
     */
    protected Palette diffPalette;

    /**
     * Unique palette
     */
    protected Palette uniquePalette;

    /**
     * @param editor
     * @param id
     * @param title
     * @param page
     */
    public Diff2ChartPage(FormEditor editor, String id, String title,
            FolderDiffPage page) {
        super(editor, id, title);
        this.diffPage = page;
    }

    /**
     * Load palettes
     * 
     * @param diffChartData
     * @param uniqueChartData
     */
    protected void loadPalettes(ChartData diffChartData,
            ChartData uniqueChartData) {
        diffPalette = PaletteImpl.create(ColorDefinitionImpl.GREY());
        diffPalette.shift(1);
        uniquePalette = PaletteImpl.create(ColorDefinitionImpl.GREY());
        uniquePalette.getEntries().clear();
        ChartPoint[] diffPoints = diffChartData.getPoints();
        for (int i = 0; i < diffPoints.length; i++) {
            uniqueChartData.get(diffPoints[i].name).fill = FillUtil
                    .getPaletteFill(diffPalette.getEntries(), i);
        }
        for (ChartPoint point : uniqueChartData.getPoints()) {
            uniquePalette.getEntries().add(point.fill);
        }
    }

    /**
     * Create standard chart
     * 
     * @return chart with axes
     */
    protected ChartWithAxes createStandardChart() {
        ChartWithAxes chart = ChartWithAxesImpl.create();
        chart.setUnitSpacing(33);
        chart.setDimension(ChartDimension.TWO_DIMENSIONAL_LITERAL);
        chart.getTitle().setVisible(false);
        chart.setOrientation(Orientation.HORIZONTAL_LITERAL);
        chart.getLegend().setAnchor(Anchor.NORTH_EAST_LITERAL);
        chart.getLegend().setItemType(LegendItemType.CATEGORIES_LITERAL);
        chart.getLegend().setVisible(false);

        Axis xAxis = chart.getPrimaryBaseAxes()[0];
        xAxis.getLineAttributes().setVisible(false);
        Axis yAxis = chart.getPrimaryOrthogonalAxis(xAxis);
        yAxis.getOrigin().setType(IntersectionType.MAX_LITERAL);
        yAxis.setLabelPosition(Position.RIGHT_LITERAL);
        yAxis.getLineAttributes().setVisible(false);

        return chart;
    }

    /**
     * Create number series
     * 
     * @param values
     * @return series
     */
    protected Series createNumberSeries(double[] values) {
        return ChartUtils.createBarNumberSeries(values);
    }

    /**
     * Create number series
     * 
     * @param values
     * @return number series
     */
    protected Series createNumberSeries(ChartData values) {
        Series series = createNumberSeries(values.getValues());
        series.setLabelPosition(Position.OUTSIDE_LITERAL);
        series.setTranslucent(true);
        return series;
    }

    /**
     * Create text series
     * 
     * @param values
     * @return series
     */
    protected Series createTextSeries(ChartData values) {
        return ChartUtils.createTextSeries(values.getNames());
    }

    /**
     * Initialize the listening to the diff page and the load of the initial
     * diffs if they exist
     */
    protected void initialize() {
        this.diffPage.addListener(this);
        diffsGenerated(this.diffPage.getContainer());
    }

    /**
     * Update chart height
     */
    protected void updateHeight() {
        ChartUtils.updateHeight(uniqueChart, uniqueChartData.getSize());
        ChartUtils.updateHeight(diffChart, diffChartData.getSize());
    }

    /**
     * Load charts
     */
    protected void loadCharts() {
        this.diffChartData = new ChartData();
        this.uniqueChartData = new ChartData();
        generateChartData(this.diffChartData, this.uniqueChartData);
        loadPalettes(this.diffChartData, this.uniqueChartData);
        diffChart.buildChart();
        uniqueChart.buildChart();
    }

    /**
     * Create diff chart
     * 
     * @param parent
     * @return chart canvas
     */
    protected ChartCanvas createDiffChart(Composite parent) {
        this.diffChart = new ChartCanvas() {

            @Override
            protected boolean shouldDraw() {
                return container != null;
            }

            @Override
            protected Chart createChart() {
                ChartWithAxes chart = createStandardChart();

                Axis xAxis = chart.getPrimaryBaseAxes()[0];
                SeriesDefinition xDefinition = ChartUtils
                        .createSeriesDefinition(xAxis);
                xDefinition.setSeriesPalette(diffPalette);

                Series seValues = createTextSeries(diffChartData);
                xDefinition.getSeries().add(seValues);

                SeriesDefinition yDefinition = ChartUtils
                        .createSeriesDefinition(chart
                                .getPrimaryOrthogonalAxis(xAxis));
                Series seCategory = createNumberSeries(diffChartData);
                yDefinition.getSeries().add(seCategory);

                return chart;
            }

        };
        this.diffChart.createControl(parent);
        return this.diffChart;
    }

    /**
     * Create unique chart
     * 
     * @param parent
     * @return chart canvas
     */
    protected ChartCanvas createUniqueChart(Composite parent) {
        this.uniqueChart = new ChartCanvas() {

            @Override
            protected boolean shouldDraw() {
                return container != null;
            }

            @Override
            protected Chart createChart() {
                ChartWithAxes chart = createStandardChart();

                Axis xAxis = chart.getPrimaryBaseAxes()[0];
                SeriesDefinition xDefinition = ChartUtils
                        .createSeriesDefinition(xAxis);
                xDefinition.setSeriesPalette(uniquePalette);

                Series seValues = createTextSeries(uniqueChartData);
                xDefinition.getSeries().add(seValues);

                SeriesDefinition yDefinition = ChartUtils
                        .createSeriesDefinition(chart
                                .getPrimaryOrthogonalAxis(xAxis));
                Series seCategory = createNumberSeries(uniqueChartData);
                yDefinition.getSeries().add(seCategory);

                return chart;
            }

        };
        uniqueChart.createControl(parent);
        return uniqueChart;
    }

    /**
     * Generate chart data
     * 
     * @param diffChartData
     * @param uniqueChartData
     */
    protected void generateChartData(ChartData diffChartData,
            ChartData uniqueChartData) {
        // Does nothing, sub-classes may override
    }

    /**
     * Refresh charts
     */
    protected void refreshCharts() {
        if (P4UIUtils.okToUse(getPartControl())) {
            boolean diffValid = diffChartData.isValid();
            boolean uniqueValid = uniqueChartData.isValid();
            diffSection.setVisible(diffValid);
            uniqueSection.setVisible(uniqueValid);
            ((GridData) uniqueSection.getLayoutData()).exclude = !uniqueValid;
            ((GridData) diffSection.getLayoutData()).exclude = !diffValid;
            updateHeight();
            if (diffValid) {
                diffChart.redraw(false);
            }
            if (uniqueValid) {
                uniqueChart.redraw(false);
            }
            getManagedForm().reflow(true);
        }
    }

    /**
     * @see com.perforce.team.ui.folder.diff.model.IFolderDiffListener#diffsGenerated(com.perforce.team.ui.folder.diff.model.FileDiffContainer)
     */
    public void diffsGenerated(FileDiffContainer container) {
        this.container = container;
        if (this.container != null) {
            loadCharts();

            Runnable runnable = new Runnable() {

                public void run() {
                    refreshCharts();
                }
            };
            if (PerforceUIPlugin.isUIThread()) {
                runnable.run();
            } else {
                PerforceUIPlugin.asyncExec(runnable);
            }
        }
    }

    /**
     * Update section descriptions
     * 
     * @param diffSection
     * @param uniqueSection
     */
    protected void updateSectionDescriptions(Section diffSection,
            Section uniqueSection) {
        // Does nothing, sub-classes may override
    }

    /**
     * @see org.eclipse.ui.forms.editor.FormPage#createFormContent(org.eclipse.ui.forms.IManagedForm)
     */
    @Override
    protected void createFormContent(IManagedForm managedForm) {
        Composite body = managedForm.getForm().getBody();
        FormToolkit toolkit = managedForm.getToolkit();

        body.setLayout(GridLayoutFactory.swtDefaults().numColumns(2).create());

        diffSection = toolkit.createSection(body, ExpandableComposite.EXPANDED
                | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE
                | Section.DESCRIPTION);
        diffSection.setLayoutData(GridDataFactory.fillDefaults()
                .grab(true, true).create());
        diffSection.setText(Messages.Diff2ChartPage_ContentTitle);

        Composite diffArea = toolkit.createComposite(diffSection);
        diffSection.setClient(diffArea);
        diffArea.setLayout(GridLayoutFactory.swtDefaults().create());
        diffArea.setLayoutData(GridDataFactory.fillDefaults().grab(true, true)
                .create());

        uniqueSection = toolkit.createSection(body,
                ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR
                        | ExpandableComposite.TWISTIE | Section.DESCRIPTION);
        uniqueSection.setLayoutData(GridDataFactory.fillDefaults()
                .grab(true, true).create());
        uniqueSection.setText(Messages.Diff2ChartPage_UniqueTitle);

        Composite uniqueArea = toolkit.createComposite(uniqueSection);
        uniqueSection.setClient(uniqueArea);
        uniqueArea.setLayout(GridLayoutFactory.swtDefaults().create());
        uniqueArea.setLayoutData(GridDataFactory.fillDefaults()
                .grab(true, true).create());

        P4FormUIUtils.registerExpansionSpaceGrabber(uniqueSection, body, true,
                true);
        P4FormUIUtils.registerExpansionSpaceGrabber(diffSection, body, true,
                true);

        updateSectionDescriptions(diffSection, uniqueSection);

        createDiffChart(diffArea);
        createUniqueChart(uniqueArea);

        initialize();
    }

}
