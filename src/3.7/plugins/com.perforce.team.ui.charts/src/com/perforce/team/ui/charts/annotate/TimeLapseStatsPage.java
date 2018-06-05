/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.charts.annotate;

import com.perforce.p4java.core.file.FileAction;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.charts.ChartCanvas;
import com.perforce.team.ui.charts.P4ChartUiPlugin;
import com.perforce.team.ui.charts.preferences.IPreferenceConstants;
import com.perforce.team.ui.text.timelapse.ITextAnnotateModel.Line;
import com.perforce.team.ui.text.timelapse.TextTimeLapseEditor;
import com.perforce.team.ui.timelapse.IAuthorProvider;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.ChartWithAxes;
import org.eclipse.birt.chart.model.attribute.Anchor;
import org.eclipse.birt.chart.model.attribute.ChartDimension;
import org.eclipse.birt.chart.model.attribute.Position;
import org.eclipse.birt.chart.model.attribute.RiserType;
import org.eclipse.birt.chart.model.component.Axis;
import org.eclipse.birt.chart.model.component.Series;
import org.eclipse.birt.chart.model.component.impl.SeriesImpl;
import org.eclipse.birt.chart.model.data.NumberDataSet;
import org.eclipse.birt.chart.model.data.SeriesDefinition;
import org.eclipse.birt.chart.model.data.impl.NumberDataSetImpl;
import org.eclipse.birt.chart.model.data.impl.SeriesDefinitionImpl;
import org.eclipse.birt.chart.model.impl.ChartWithAxesImpl;
import org.eclipse.birt.chart.model.type.BarSeries;
import org.eclipse.birt.chart.model.type.impl.BarSeriesImpl;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Caret;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class TimeLapseStatsPage extends TimeLapsePage {

    private ManagedForm form;
    private Image headerImage;
    private Caret caret;
    private StyledText statsText;

    private Map<Double, Double> myRevisions;
    private Map<Double, Double> othersRevisions;
    private Section histogramSection;
    private ChartCanvas histogram;
    private String name = ""; //$NON-NLS-1$
    private String range = ""; //$NON-NLS-1$

    /**
     * @see org.eclipse.ui.part.Page#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {
        myRevisions = new TreeMap<Double, Double>();
        othersRevisions = new TreeMap<Double, Double>();

        form = new ManagedForm(parent);
        ScrolledForm formControl = form.getForm();
        FormToolkit toolkit = form.getToolkit();
        formControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        formControl.setText(name);
        if (headerImage == null && name.length() > 0) {
            ImageDescriptor descriptor = P4UIUtils.getImageDescriptor(name);
            if (descriptor != null) {
                headerImage = descriptor.createImage();
                P4UIUtils.registerDisposal(getControl(), headerImage);
                form.getForm().setImage(headerImage);
            }
        }
        toolkit.decorateFormHeading(formControl.getForm());

        Composite body = formControl.getBody();
        body.setBackgroundMode(SWT.INHERIT_DEFAULT);
        GridLayout bLayout = new GridLayout(1, true);
        bLayout.marginHeight = 0;
        bLayout.marginWidth = 0;
        bLayout.verticalSpacing = 2;
        body.setLayout(bLayout);
        toolkit.adapt(body);

        statsText = new StyledText(body, SWT.NONE);
        statsText.setEditable(false);
        caret = new Caret(statsText, SWT.NONE);
        caret.setSize(0, 0);
        statsText.setCaret(caret);
        statsText.setLineSpacing(2);
        GridData stData = new GridData(SWT.FILL, SWT.FILL, true, false);
        stData.horizontalIndent = 5;
        stData.verticalIndent = 2;
        statsText.setLayoutData(stData);
        statsText.addLineStyleListener(new LineStyleListener() {

            public void lineGetStyle(LineStyleEvent event) {
                List<StyleRange> styles = new ArrayList<StyleRange>();
                int colon = event.lineText.indexOf(':');
                if (colon != -1) {
                    StyleRange style = new StyleRange(event.lineOffset, colon,
                            null, null, SWT.BOLD);
                    styles.add(style);
                    int lParen = event.lineText.indexOf('(');
                    if (lParen != -1) {
                        int rParen = event.lineText.indexOf(')', lParen);
                        if (rParen != -1) {
                            StyleRange green = new StyleRange(
                                    event.lineOffset + lParen + 1,
                                    rParen - lParen - 1,
                                    event.display
                                            .getSystemColor(SWT.COLOR_DARK_GREEN),
                                    null);
                            styles.add(green);
                        }
                    }
                    event.styles = styles.toArray(new StyleRange[styles.size()]);
                }
            }
        });

        histogramSection = toolkit.createSection(body,
                ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
        histogramSection.setText(MessageFormat.format(
                Messages.TimeLapseStatsPage_ChangesPerYear, range));
        histogramSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                true));
        histogramSection.setExpanded(P4ChartUiPlugin.getDefault()
                .getPreferenceStore()
                .getBoolean(IPreferenceConstants.EXPAND_HISTOGRAM));

        Composite histogramBody = toolkit.createComposite(histogramSection);
        GridLayout hbLayout = new GridLayout(1, true);
        hbLayout.marginHeight = 0;
        hbLayout.marginWidth = 0;
        histogramBody.setLayout(hbLayout);
        GridData hbData = new GridData(SWT.FILL, SWT.FILL, true, true);
        hbData.minimumHeight = P4UIUtils.VIEWER_HEIGHT_HINT * 2;
        histogramBody.setLayoutData(hbData);
        histogramSection.setClient(histogramBody);

        histogram = new ChartCanvas() {

            @Override
            protected boolean shouldDraw() {
                return model != null && model.getRevisionCount() != 0;
            }

            @Override
            protected Chart createChart() {
                ChartWithAxes chart = ChartWithAxesImpl.create();
                chart.setDimension(ChartDimension.TWO_DIMENSIONAL_LITERAL);
                chart.getTitle().setVisible(false);
                chart.getLegend().setVisible(true);
                chart.getLegend().setAnchor(Anchor.NORTH_EAST_LITERAL);

                NumberDataSet data = NumberDataSetImpl.create(myRevisions
                        .keySet());
                Series seCategory = SeriesImpl.create();
                seCategory.setDataSet(data);

                SeriesDefinition xDefinition = SeriesDefinitionImpl.create();

                Axis xAxis = chart.getPrimaryBaseAxes()[0];
                xAxis.getSeriesDefinitions().add(xDefinition);
                xDefinition.getSeries().add(seCategory);

                SeriesDefinition yDefinition = SeriesDefinitionImpl.create();

                Axis yAxis = chart.getPrimaryOrthogonalAxis(xAxis);
                yAxis.getSeriesDefinitions().add(yDefinition);

                xAxis.getLabel().getCaption().getFont().setSize(12);
                yAxis.getLabel().getCaption().getFont().setSize(12);

                yDefinition.getSeriesPalette().shift(1);

                yDefinition.getSeries().add(
                        createSeries(Messages.TimeLapseStatsPage_Others,
                                othersRevisions.values()));
                yDefinition.getSeries().add(
                        createSeries(getUser(), myRevisions.values()));

                return chart;
            }

            private Series createSeries(String name, Collection<Double> values) {
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
        };
        histogram.createControl(histogramBody);
        refresh();
    }

    /**
     * @see org.eclipse.ui.part.Page#dispose()
     */
    @Override
    public void dispose() {
        if (this.caret != null) {
            this.caret.dispose();
        }
        if (this.histogramSection != null) {
            P4ChartUiPlugin
                    .getDefault()
                    .getPreferenceStore()
                    .setValue(IPreferenceConstants.EXPAND_HISTOGRAM,
                            histogramSection.isExpanded());
        }
        super.dispose();
    }

    /**
     * @see org.eclipse.ui.part.Page#getControl()
     */
    @Override
    public Control getControl() {
        return this.form.getForm();
    }

    /**
     * @see org.eclipse.ui.part.Page#setFocus()
     */
    @Override
    public void setFocus() {
        this.form.getForm().setFocus();
    }

    private String getName(String path) {
        String name = path;
        int lastSlash = name.lastIndexOf('/');
        if (lastSlash != -1 && lastSlash + 1 < name.length()) {
            name = name.substring(lastSlash + 1);
        }
        return name;
    }

    /**
     * @see com.perforce.team.ui.charts.annotate.TimeLapsePage#setEditor(com.perforce.team.ui.text.timelapse.TextTimeLapseEditor)
     */
    @Override
    public void setEditor(TextTimeLapseEditor editor) {
        super.setEditor(editor);
        IP4Resource resource = P4CoreUtils.convert(
                this.editor.getEditorInput(), IP4Resource.class);
        if (resource != null) {
            name = resource.getName();
        }
    }

    private String getUser() {
        return this.model != null ? this.model.getLatest().getConnection()
                .getParameters().getUser() : ""; //$NON-NLS-1$
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private boolean isModifiedBy(IAuthorProvider provider, IP4Revision revision) {
        return revision.getConnection().isOwner(provider.getAuthor(revision));
    }

    private double getPercentage(int count, int by) {
        if (by == 0) {
            return 0.0;
        }
        return ((double) count / by) * 100.0;
    }

    private void refresh() {
        StringBuilder content = new StringBuilder();
        if (this.model != null && this.model.getRevisionCount() > 0) {
            IP4Revision latest = this.model.getLatest();

            int renameCount = 0;
            int mergeCount = 0;
            int branchCount = 0;
            int editCount = 0;
            int moveCount = 0;
            int earliestYear = Integer.MAX_VALUE;
            int latestYear = Integer.MIN_VALUE;
            IP4Revision revision = this.model.getEarliest();
            String revName = P4CoreUtils.getName(revision.getRemotePath());
            Calendar calendar = new GregorianCalendar();
            int myRevs = 0;
            String user = latest.getConnection().getParameters().getUser();
            Set<String> olderNames = new HashSet<String>();
            this.myRevisions.clear();
            this.othersRevisions.clear();
            IAuthorProvider provider = null;
            if (model instanceof IAuthorProvider) {
                provider = ((IAuthorProvider) model);
            } else {
                provider = new IAuthorProvider() {

                    public String getAuthor(IP4Revision revision) {
                        return revision.getAuthor();
                    }
                };
            }
            while (revision != null) {
                Date date = new Date(revision.getTimestamp());
                calendar.setTime(date);
                double revYear = calendar.get(Calendar.YEAR);
                if (!myRevisions.containsKey(revYear)) {
                    myRevisions.put(revYear, 0.0);
                }
                if (!othersRevisions.containsKey(revYear)) {
                    othersRevisions.put(revYear, 0.0);
                }
                if (isModifiedBy(provider, revision)) {
                    myRevisions.put(revYear, myRevisions.get(revYear) + 1.0);
                    myRevs++;
                } else {
                    othersRevisions.put(revYear,
                            othersRevisions.get(revYear) + 1.0);
                }
                earliestYear = Math.min(earliestYear, (int) revYear);
                latestYear = Math.max(latestYear, (int) revYear);

                FileAction action = revision.getAction();
                if (action != null) {
                    switch (action) {
                    case INTEGRATE:
                        mergeCount++;
                        break;
                    case BRANCH:
                        branchCount++;
                        break;
                    case EDIT:
                        editCount++;
                        break;
                    case MOVE_ADD:
                        moveCount++;
                        break;
                    default:
                        break;
                    }
                }
                String currName = getName(revision.getRemotePath());
                if (!revName.equals(currName)) {
                    renameCount++;
                    olderNames.add(revName);
                    revName = currName;
                }
                revision = this.model.getNext(revision);
            }

            int myLines = 0;
            Line[] latestLines = this.model.getLines(latest);
            for (Line line : latestLines) {
                if (isModifiedBy(provider, model.getRevisionById(line.lower))) {
                    myLines++;
                }
            }

            int revCount = this.model.getRevisionCount();

            double revPercentage = round(getPercentage(myRevs, revCount));
            double linePercentage = round(getPercentage(myLines,
                    latestLines.length));
            double mergePercentage = round(getPercentage(mergeCount, revCount));
            double branchPercentage = round(getPercentage(branchCount, revCount));
            double editPercentage = round(getPercentage(editCount, revCount));
            double movePercentage = round(getPercentage(moveCount, revCount));

            int revId = this.model.getRevisionId(latest);

            content.append(MessageFormat.format(
                    Messages.TimeLapseStatsPage_LastChanged,
                    this.model.getDate(revId)));

            String author = this.model.getAuthor(revId);
            if (author != null && author.length() > 0) {
                content.append(MessageFormat.format(
                        Messages.TimeLapseStatsPage_ChangedBy, author));
            }

            content.append('\n').append('\n');

            content.append(
                    MessageFormat.format(
                            Messages.TimeLapseStatsPage_LinesByUser, user,
                            myLines, latestLines.length, linePercentage))
                    .append('\n').append('\n');

            content.append(
                    MessageFormat.format(
                            Messages.TimeLapseStatsPage_RevisionsByUser, user,
                            myRevs, this.model.getRevisionCount(),
                            revPercentage)).append('\n').append('\n');

            content.append(
                    MessageFormat.format(Messages.TimeLapseStatsPage_Edits,
                            editCount, editPercentage)).append('\n');

            content.append(
                    MessageFormat.format(Messages.TimeLapseStatsPage_Merges,
                            mergeCount, mergePercentage)).append('\n');

            content.append(
                    MessageFormat.format(Messages.TimeLapseStatsPage_Branches,
                            branchCount, branchPercentage)).append('\n');

            content.append(
                    MessageFormat.format(Messages.TimeLapseStatsPage_Moves,
                            moveCount, movePercentage)).append('\n');

            content.append(MessageFormat.format(
                    Messages.TimeLapseStatsPage_Renames, renameCount));

            String sep = "    "; //$NON-NLS-1$
            olderNames.remove(name);
            if (olderNames.size() > 0) {
                content.append('\n');
            }
            for (String older : olderNames) {
                content.append(sep);
                sep = ", "; //$NON-NLS-1$
                content.append(older);
            }

            if (earliestYear != latestYear) {
                range = MessageFormat.format(
                        Messages.TimeLapseStatsPage_RangeMultiple,
                        Integer.toString(earliestYear),
                        Integer.toString(latestYear));
            } else {
                range = MessageFormat.format(
                        Messages.TimeLapseStatsPage_RangeSingle,
                        Integer.toString(earliestYear));
            }
            histogramSection.setText(MessageFormat.format(
                    Messages.TimeLapseStatsPage_ChangesPerYear, range));

            statsText.setText(content.toString());
            histogram.redraw(true);
            form.reflow(true);
        }
    }

    /**
     * @see com.perforce.team.ui.charts.annotate.TimeLapsePage#modelRefreshed()
     */
    @Override
    protected void modelRefreshed() {
        PerforceUIPlugin.asyncExec(new Runnable() {

            public void run() {
                if (P4UIUtils.okToUse(getControl())) {
                    refresh();
                }
            }
        });
    }

}
