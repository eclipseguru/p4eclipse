/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.charts.diff;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.PerforceProviderPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.ChartWithAxes;
import org.eclipse.birt.chart.model.attribute.Anchor;
import org.eclipse.birt.chart.model.attribute.ChartDimension;
import org.eclipse.birt.chart.model.attribute.IntersectionType;
import org.eclipse.birt.chart.model.attribute.LegendItemType;
import org.eclipse.birt.chart.model.attribute.Orientation;
import org.eclipse.birt.chart.model.attribute.Position;
import org.eclipse.birt.chart.model.attribute.RiserType;
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
import org.eclipse.birt.chart.model.type.BarSeries;
import org.eclipse.birt.chart.model.type.impl.BarSeriesImpl;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ChangelistChart {

    /**
     * SEPARATOR
     */
    public static final String SEPARATOR = "==== "; //$NON-NLS-1$

    /**
     * ADD_LINE
     */
    public static final String ADD_LINE = "add"; //$NON-NLS-1$

    /**
     * EDIT_LINE
     */
    public static final String EDIT_LINE = "changed"; //$NON-NLS-1$

    /**
     * DELETE_LINE
     */
    public static final String DELETE_LINE = "deleted"; //$NON-NLS-1$

    /**
     * MAX_FILE_COUNT
     */
    public static final int MAX_FILE_COUNT = 100;

    private static class FileDiff implements Comparable<FileDiff> {

        String path;

        /**
         * Create file diff
         * 
         * @param path
         */
        public FileDiff(String path) {
            this.path = path;
        }

        Double adds;
        Double edits;
        Double deletes;

        public int getTotalDiffs() {
            int sum = 0;
            if (adds != null) {
                sum += adds;
            }
            if (edits != null) {
                sum += edits;
            }
            if (deletes != null) {
                sum += deletes;
            }
            return sum;
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return path.hashCode();
        }

        /**
         * Get name
         * 
         * @return name
         */
        public String getName() {
            return P4CoreUtils.getName(path);
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof FileDiff) {
                FileDiff other = (FileDiff) obj;
                return getTotalDiffs() == other.getTotalDiffs()
                        && path.equals(other.path);
            }
            return false;
        }

        /**
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(FileDiff o) {
            int compare = o.getTotalDiffs() - getTotalDiffs();
            if (compare == 0) {
                compare = path.compareToIgnoreCase(o.path);
            }
            return compare;
        }
    }

    private int totalFileCount = 0;
    private int displayedFileCount = 0;

    private Set<FileDiff> parseDiffs(InputStream stream, Charset charset) {
        Set<FileDiff> changes = new TreeSet<FileDiff>();
        if (stream != null) {
            FileDiff diff = null;
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(stream, charset));
                String line = reader.readLine();
                while (line != null) {
                    if (line.startsWith(SEPARATOR)) {
                        line = line.substring(SEPARATOR.length());
                        int hash = line.indexOf('#');
                        if (hash != -1) {
                            diff = new FileDiff(line.substring(0, hash));
                        } else {
                            diff = null;
                        }
                    } else if (diff != null) {
                        if (line.startsWith(ADD_LINE)) {
                            double add = parseSummaryLine(line);
                            if (add > 0) {
                                diff.adds = add;
                            }
                        } else if (line.startsWith(EDIT_LINE)) {
                            double edit = parseChangeLine(line);
                            if (edit > 0) {
                                diff.edits = edit;
                            }
                            if (diff.getTotalDiffs() > 0) {
                                changes.add(diff);
                            }
                            diff = null;
                        } else if (line.startsWith(DELETE_LINE)) {
                            double delete = parseSummaryLine(line);
                            if (delete > 0) {
                                diff.deletes = delete;
                            }
                        }
                    }
                    line = reader.readLine();
                }
            } catch (IOException e) {
                PerforceProviderPlugin.logError(e);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        PerforceProviderPlugin.logError(e);
                    }
                }
            }
        }
        return changes;
    }

    private double parseChangeLine(String line) {
        double number = 0;
        int space = line.lastIndexOf('/');
        if (space != -1) {
            line = line.substring(0, space).trim();
            space = line.lastIndexOf(' ');
            if (space != -1) {
                line = line.substring(space).trim();
            }
            try {
                number = Double.parseDouble(line);
            } catch (NumberFormatException nfe) {
                number = 0;
            }
        }
        return number;
    }

    private double parseSummaryLine(String line) {
        double number = 0;
        int space = line.lastIndexOf(' ');
        if (space != -1) {
            line = line.substring(0, space).trim();
            space = line.lastIndexOf(' ');
            if (space != -1) {
                line = line.substring(space).trim();
            }
            try {
                number = Double.parseDouble(line);
            } catch (NumberFormatException nfe) {
                number = 0;
            }
        }
        return number;
    }

    /**
     * Get number of files displaying diff counts for
     * 
     * @return - file count
     */
    public int getDisplayedFileCount() {
        return this.displayedFileCount;
    }

    /**
     * Get total file count
     * 
     * @return total file count
     */
    public int getTotalFileCount() {
        return this.totalFileCount;
    }

    /**
     * Build diff chart from canvas
     * 
     * @param stream
     * @param charset 
     * @return - diff chart
     */
    public Chart build(InputStream stream, Charset charset) {
        final Set<FileDiff> changes = parseDiffs(stream, charset);

        ChartWithAxes chart = ChartWithAxesImpl.create();
        chart.setDimension(ChartDimension.TWO_DIMENSIONAL_LITERAL);
        chart.getTitle().setVisible(false);
        chart.setOrientation(Orientation.HORIZONTAL_LITERAL);
        chart.getLegend().setAnchor(Anchor.NORTH_EAST_LITERAL);
        chart.getLegend().setItemType(LegendItemType.SERIES_LITERAL);
        chart.getLegend().setVisible(true);
        chart.getTitle().getLabel().getCaption()
                .setValue(Messages.ChangelistChart_LineDifferencesPerFile);

        this.totalFileCount = changes.size();
        this.displayedFileCount = Math.min(MAX_FILE_COUNT, this.totalFileCount);

        FileDiff[] files = new FileDiff[this.displayedFileCount];
        int index = this.displayedFileCount - 1;
        for (FileDiff diff : changes) {
            files[index] = diff;
            index--;
            if (index < 0) {
                break;
            }
        }

        String[] names = new String[this.displayedFileCount];
        for (int i = 0; i < this.displayedFileCount; i++) {
            names[i] = files[i].getName();
        }

        TextDataSet data = TextDataSetImpl.create(names);
        Series seCategory = SeriesImpl.create();
        seCategory.setDataSet(data);

        SeriesDefinition xDefinition = SeriesDefinitionImpl.create();

        Axis xAxis = chart.getPrimaryBaseAxes()[0];
        xAxis.getSeriesDefinitions().add(xDefinition);
        xDefinition.getSeries().add(seCategory);

        SeriesDefinition yDefinition = SeriesDefinitionImpl.create();

        Axis yAxis = chart.getPrimaryOrthogonalAxis(xAxis);
        yAxis.getSeriesDefinitions().add(yDefinition);

        Double[] adds = new Double[this.displayedFileCount];
        Double[] edits = new Double[this.displayedFileCount];
        Double[] deletes = new Double[this.displayedFileCount];

        for (int i = 0; i < this.displayedFileCount; i++) {
            FileDiff diff = files[i];
            adds[i] = diff.adds;
            edits[i] = diff.edits;
            deletes[i] = diff.deletes;
        }

        xAxis.getLabel().getCaption().getFont().setSize(12);
        yAxis.getLabel().getCaption().getFont().setSize(12);
        yAxis.getOrigin().setType(IntersectionType.MAX_LITERAL);
        yAxis.setLabelPosition(Position.RIGHT_LITERAL);

        yDefinition.getSeriesPalette().shift(1);
        yDefinition.getSeries().add(
                createSeries(Messages.ChangelistChart_Added, adds));

        yDefinition.getSeries().add(
                createSeries(Messages.ChangelistChart_Changed, edits));

        yDefinition.getSeries().add(
                createSeries(Messages.ChangelistChart_Deleted, deletes));

        return chart;
    }

    private Series createSeries(String name, Double[] values) {
        NumberDataSet set = NumberDataSetImpl.create(values);
        BarSeries series = (BarSeries) BarSeriesImpl.create();
        series.setRiserOutline(null);
        series.setRiser(RiserType.RECTANGLE_LITERAL);
        series.setLabelPosition(Position.INSIDE_LITERAL);
        series.setTranslucent(true);
        series.setStacked(true);
        series.setDataSet(set);
        series.getLabel().setVisible(true);
        series.setSeriesIdentifier(name);
        return series;
    }
}
