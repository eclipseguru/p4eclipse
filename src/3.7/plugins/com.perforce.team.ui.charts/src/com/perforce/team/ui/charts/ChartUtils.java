/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.charts;

import org.eclipse.birt.chart.model.ChartWithoutAxes;
import org.eclipse.birt.chart.model.attribute.RiserType;
import org.eclipse.birt.chart.model.component.Axis;
import org.eclipse.birt.chart.model.component.Series;
import org.eclipse.birt.chart.model.component.impl.SeriesImpl;
import org.eclipse.birt.chart.model.data.NumberDataSet;
import org.eclipse.birt.chart.model.data.SeriesDefinition;
import org.eclipse.birt.chart.model.data.impl.NumberDataSetImpl;
import org.eclipse.birt.chart.model.data.impl.SeriesDefinitionImpl;
import org.eclipse.birt.chart.model.data.impl.TextDataSetImpl;
import org.eclipse.birt.chart.model.type.BarSeries;
import org.eclipse.birt.chart.model.type.PieSeries;
import org.eclipse.birt.chart.model.type.impl.BarSeriesImpl;
import org.eclipse.birt.chart.model.type.impl.PieSeriesImpl;
import org.eclipse.swt.layout.GridData;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public final class ChartUtils {

    /**
     * DEFAULT_BAR_SIZE
     */
    public static final int DEFAULT_BAR_SIZE = 40;

    /**
     * AXIS_AREA
     */
    public static final int AXIS_AREA = 100;

    /**
     * Create series definition for chart
     * 
     * @param chart
     * @return series definition
     */
    public static SeriesDefinition createSeriesDefinition(ChartWithoutAxes chart) {
        SeriesDefinition definition = SeriesDefinitionImpl.create();
        chart.getSeriesDefinitions().add(definition);
        definition.getSeriesPalette().shift(1);
        return definition;
    }

    /**
     * Create series definition for axis
     * 
     * @param axis
     * @return series definition
     */
    public static SeriesDefinition createSeriesDefinition(Axis axis) {
        SeriesDefinition definition = SeriesDefinitionImpl.create();
        axis.getSeriesDefinitions().add(definition);
        definition.getSeriesPalette().shift(1);
        return definition;
    }

    /**
     * Create text series
     * 
     * @param values
     * @return series
     */
    public static Series createTextSeries(Object values) {
        Series seValues = SeriesImpl.create();
        seValues.setDataSet(TextDataSetImpl.create(values));
        return seValues;
    }

    /**
     * Create pie number series
     * 
     * @param values
     * @return pie series
     */
    public static PieSeries createPieNumberSeries(Object values) {
        NumberDataSet data = NumberDataSetImpl.create(values);
        PieSeries series = (PieSeries) PieSeriesImpl.create();
        series.setDataSet(data);
        series.getLabel().setVisible(true);
        return series;
    }

    /**
     * Create bar number series
     * 
     * @param values
     * @return bar series
     */
    public static BarSeries createBarNumberSeries(Object values) {
        NumberDataSet data = NumberDataSetImpl.create(values);
        BarSeries series = (BarSeries) BarSeriesImpl.create();
        series.setRiserOutline(null);
        series.setRiser(RiserType.RECTANGLE_LITERAL);
        series.setDataSet(data);
        series.getLabel().setVisible(true);
        return series;
    }

    /**
     * Update canvas height based on count
     * 
     * @param canvas
     * @param count
     */
    public static void updateHeight(ChartCanvas canvas, int count) {
        int target = AXIS_AREA + count * DEFAULT_BAR_SIZE;
        GridData data = (GridData) canvas.getControl().getLayoutData();
        data.heightHint = target;
        data.grabExcessVerticalSpace = false;
    }

}
