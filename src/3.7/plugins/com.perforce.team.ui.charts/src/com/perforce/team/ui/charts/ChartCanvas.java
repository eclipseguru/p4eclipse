/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.charts;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.ui.P4UIUtils;

import org.eclipse.birt.chart.device.IDeviceRenderer;
import org.eclipse.birt.chart.device.IDisplayServer;
import org.eclipse.birt.chart.exception.ChartException;
import org.eclipse.birt.chart.factory.GeneratedChartState;
import org.eclipse.birt.chart.factory.Generator;
import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.attribute.Bounds;
import org.eclipse.birt.chart.model.attribute.impl.BoundsImpl;
import org.eclipse.birt.chart.util.PluginSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class ChartCanvas {

    private Image image;
    private IDeviceRenderer renderer;
    private GeneratedChartState state;
    private Canvas canvas;

    /**
     * Chart
     */
    protected Chart chart;

    private void generateChart() {
        if (chart == null) {
            buildChart();
        }
        Point size = canvas.getSize();
        Bounds bounds = BoundsImpl.create(0, 0, size.x, size.y);
        GC gc = null;
        try {
            gc = new GC(canvas);
            renderer.setProperty(IDeviceRenderer.GRAPHICS_CONTEXT, gc);
            IDisplayServer server = renderer.getDisplayServer();
            int resolution = server.getDpiResolution();
            bounds.scale(72d / resolution);
            state = Generator.instance().build(server, chart, bounds, null,
                    null, null);
        } catch (ChartException ex) {
            PerforceProviderPlugin.logError(ex);
        } finally {
            if (gc != null) {
                gc.dispose();
            }
        }
    }

    /**
     * Build the chart
     */
    public void buildChart() {
        chart = createChart();
    }

    private void drawImage(Rectangle rectangle) {
        GC gc = null;
        try {
            if (image != null) {
                image.dispose();
            }
            image = new Image(canvas.getDisplay(), rectangle.width,
                    rectangle.height);
            gc = new GC(image);
            renderer.setProperty(IDeviceRenderer.GRAPHICS_CONTEXT, gc);
            Generator.instance().render(renderer, state);
        } catch (ChartException ex) {
            PerforceProviderPlugin.logError(ex);
        } finally {
            if (gc != null) {
                gc.dispose();
            }
        }
    }

    /**
     * Create a chart to draw on the canvas
     * 
     * @return - chart
     */
    protected abstract Chart createChart();

    /**
     * Should the chart be drawn
     * 
     * @return - true to draw, false to not draw
     */
    protected abstract boolean shouldDraw();

    /**
     * Create the chart canvas
     * 
     * @param parent
     */
    public void createControl(Composite parent) {
        canvas = new Canvas(parent, SWT.DOUBLE_BUFFERED);
        canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        canvas.setBackgroundMode(SWT.INHERIT_DEFAULT);

        try {
            renderer = PluginSettings.instance().getDevice("dv.SWT"); //$NON-NLS-1$
            canvas.addDisposeListener(new DisposeListener() {

                public void widgetDisposed(DisposeEvent e) {
                    if (renderer != null) {
                        renderer.dispose();
                    }
                    if (image != null) {
                        image.dispose();
                    }
                }
            });
            canvas.addPaintListener(new PaintListener() {

                public void paintControl(PaintEvent e) {
                    if (!shouldDraw()) {
                        return;
                    }
                    Rectangle imageBounds = null;
                    Rectangle area = canvas.getClientArea();
                    if (image != null) {
                        imageBounds = image.getBounds();
                        if (imageBounds.width != area.width
                                || imageBounds.height != area.height) {
                            image.dispose();
                            image = null;
                        }
                    }
                    if (image == null || chart == null) {
                        generateChart();
                        drawImage(area);
                    }
                    if (image != null) {
                        imageBounds = image.getBounds();
                        e.gc.drawImage(image, 0, 0, imageBounds.width,
                                imageBounds.height, 0, 0, area.width,
                                area.height);
                    }
                }
            });
        } catch (ChartException exception) {
            PerforceProviderPlugin.logError(exception);
        }
    }

    /**
     * Get main control
     * 
     * @return composite
     */
    public Composite getControl() {
        return this.canvas;
    }

    /**
     * Redraw the canvas and optionally rebuild the chart model
     * 
     * @param rebuildChart
     */
    public void redraw(boolean rebuildChart) {
        if (P4UIUtils.okToUse(canvas)) {
            if (rebuildChart) {
                chart = null;
            }
            canvas.redraw();
            canvas.update();
        }
    }

}
