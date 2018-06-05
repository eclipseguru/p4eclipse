/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.charts.annotate;

import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.ui.text.timelapse.ITextAnnotateModel;
import com.perforce.team.ui.text.timelapse.ITextAnnotateModel.Line;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.birt.chart.model.ChartWithAxes;
import org.eclipse.birt.chart.model.attribute.Anchor;
import org.eclipse.birt.chart.model.attribute.ChartDimension;
import org.eclipse.birt.chart.model.attribute.LegendItemType;
import org.eclipse.birt.chart.model.component.Axis;
import org.eclipse.birt.chart.model.component.Series;
import org.eclipse.birt.chart.model.component.impl.SeriesImpl;
import org.eclipse.birt.chart.model.data.NumberDataSet;
import org.eclipse.birt.chart.model.data.SeriesDefinition;
import org.eclipse.birt.chart.model.data.TextDataSet;
import org.eclipse.birt.chart.model.data.impl.NumberDataSetImpl;
import org.eclipse.birt.chart.model.data.impl.SeriesDefinitionImpl;
import org.eclipse.birt.chart.model.data.impl.TextDataSetImpl;
import org.eclipse.birt.chart.model.impl.ChartWithAxesImpl;
import org.eclipse.birt.chart.model.type.AreaSeries;
import org.eclipse.birt.chart.model.type.impl.AreaSeriesImpl;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class AnnotateChart {

    private void fillModel(ITextAnnotateModel model, List<String> revs,
            Map<String, Double[]> userLineCounts) {
        int count = model.getRevisionCount();
        IP4Revision rev = model.getEarliest();
        int index = 0;
        while (rev != null) {
            revs.add("#" + rev.getRevision()); //$NON-NLS-1$
            Line[] lines = model.getLines(rev);
            for (Line line : lines) {
                String author = model.getAuthor(line.lower);
                Double[] lineCounts = userLineCounts.get(author);
                if (lineCounts == null) {
                    lineCounts = new Double[count];
                    userLineCounts.put(author, lineCounts);
                }
                Double value = lineCounts[index];
                if (value == null) {
                    value = 1.0;
                } else {
                    value += 1.0;
                }
                lineCounts[index] = value;
            }
            rev = model.getNext(rev);
            index++;
        }
    }

    /**
     * Build the a chart for the specified model
     * 
     * @param model
     * @return - chart with axes
     */
    public ChartWithAxes build(ITextAnnotateModel model) {
        ChartWithAxes chart = ChartWithAxesImpl.create();
        chart.setDimension(ChartDimension.TWO_DIMENSIONAL_LITERAL);
        chart.getTitle().setVisible(false);
        chart.getLegend().setItemType(LegendItemType.SERIES_LITERAL);
        chart.getLegend().setVisible(true);
        chart.getLegend().setAnchor(Anchor.NORTH_EAST_LITERAL);

        List<String> revs = new ArrayList<String>();
        final Map<String, Double[]> userLineCounts = new HashMap<String, Double[]>();
        fillModel(model, revs, userLineCounts);

        TextDataSet data = TextDataSetImpl.create(revs);
        Series seCategory = SeriesImpl.create();
        seCategory.setDataSet(data);

        SeriesDefinition xDefinition = SeriesDefinitionImpl.create();

        Axis xAxis = chart.getPrimaryBaseAxes()[0];
        xAxis.getSeriesDefinitions().add(xDefinition);
        xDefinition.getSeries().add(seCategory);

        SeriesDefinition yDefinition = SeriesDefinitionImpl.create();
        yDefinition.getSeriesPalette().shift(1);
        Axis yAxis = chart.getPrimaryOrthogonalAxis(xAxis);
        yAxis.getSeriesDefinitions().add(yDefinition);

        String[] authors = userLineCounts.keySet().toArray(
                new String[userLineCounts.size()]);
        Arrays.sort(authors, new Comparator<String>() {

            public int compare(String o1, String o2) {
                Double[] values1 = userLineCounts.get(o1);
                Double[] values2 = userLineCounts.get(o2);
                Double value1 = values1[values1.length - 1];
                if (value1 == null) {
                    value1 = 0.0;
                }
                Double value2 = values2[values2.length - 1];
                if (value2 == null) {
                    value2 = 0.0;
                }
                return (int) (value2 - value1);
            }
        });

        for (String author : authors) {
            Double[] lineCounts = userLineCounts.get(author);
            yDefinition.getSeries().add(createSeries(author, lineCounts));
        }

        return chart;
    }

    private Series createSeries(String author, Double[] values) {
        NumberDataSet set = NumberDataSetImpl.create(values);
        AreaSeries series = (AreaSeries) AreaSeriesImpl.create();
        series.setDataSet(set);
        series.setStacked(true);
        series.setTranslucent(true);
        series.setSeriesIdentifier(author);
        return series;
    }
}
