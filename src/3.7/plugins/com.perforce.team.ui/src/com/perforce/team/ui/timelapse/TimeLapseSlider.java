/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.timelapse;

import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.timelapse.IAnnotateModel.Type;

import java.util.Arrays;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class TimeLapseSlider {

    /**
     * Slider listener for revision changes
     */
    public interface IRevisionListener {

        /**
         * Revision changed
         * 
         * @param revision
         */
        void revisionChanged(IP4Revision revision);

        /**
         * Display type changed
         * 
         * @param type
         */
        void displayChanged(Type type);

    }

    private static final RGB MARK_BG = new RGB(50, 91, 144);

    private static final int MARGIN_BOTTOM = 5;

    private static final int SLIDER_X1 = 15;

    private static final int SLIDER_X2 = 20;

    private static final int SLIDER_ARC = 10;

    private static final int TICK_X1 = SLIDER_ARC;

    private static final int TICK_X2 = TICK_X1;

    private static final int SLIDER_Y1 = 5;

    private static final int SLIDER_HEIGHT = 13;

    private static final int TICK_HEIGHT = 4;

    private static final int TICK1_Y1 = SLIDER_Y1 + SLIDER_HEIGHT - TICK_HEIGHT;

    private static final int TICK1_Y2 = TICK1_Y1 + TICK_HEIGHT;

    private static final int TICK2_Y1 = TICK1_Y2;

    private static final int TICK2_Y2 = TICK2_Y1 + TICK_HEIGHT;

    private static final int LABEL_Y1 = TICK2_Y2 + 2;

    private static final int MARK_Y1 = SLIDER_Y1 - 1;

    private static final int MARK_WIDTH = 9;

    private static final int MARK_ARC = 3;

    private static final int MARK_HEIGHT = SLIDER_HEIGHT + MARK_ARC;

    private static final int MARK_X1 = MARK_WIDTH / 2;

    private static final int BOTTOM_PADDING = 2;

    private static final int DECORATOR_Y = 16;

    private static final int TICK_PADDING = 4;

    private static final int TICK_PADDING_OFFSET = TICK_PADDING / 2;

    private static final String SLIDER_BG_PATH = "images/slider_bg.png"; //$NON-NLS-1$

    private static final String GRABBER_FG_PATH = "images/grabber.png"; //$NON-NLS-1$

    private int[] spaces = new int[0];
    private int position = 0;
    private int decoratorY = 0;
    private Type type = null;
    private Composite displayArea;
    private Combo options;
    private ToolBar toolbar;
    private ToolItem next;
    private ToolItem previous;
    private Canvas slider;
    private Image buffer;
    private Cursor currentCursor = null;
    private Cursor moveCursor = null;
    private Color inactiveTickBg = null;
    private Color markBg = null;
    private Font sliderFont = null;
    private ITickFormatter formatter = null;
    private ITickPositionHandler positioner = null;
    private boolean drawDecorations = false;
    private ITickDecorator decorator = null;

    private boolean mouseDown = false;
    private boolean inRedraw = false;
    private boolean inUpdate = false;
    private boolean inActionUpdate = false;
    private IP4Revision[] allRevisions;
    private IP4Revision[] revisions;
    private IRevisionListener listener;

    private Type[] scaleTypes = new Type[] { Type.REVISION, Type.CHANGELIST,
            Type.DATE, };

    /**
     * Creates a new slider
     * 
     */
    public TimeLapseSlider() {
        this.revisions = new IP4Revision[0];
    }

    /**
     * @return the formatter
     */
    public ITickFormatter getFormatter() {
        return this.formatter;
    }

    /**
     * @param formatter
     *            the formatter to set
     */
    public void setFormatter(ITickFormatter formatter) {
        this.formatter = formatter;
    }

    /**
     * @return the positioner
     */
    public ITickPositionHandler getPositioner() {
        return this.positioner;
    }

    /**
     * @param positioner
     *            the positioner to set
     */
    public void setPositioner(ITickPositionHandler positioner) {
        this.positioner = positioner;
    }

    /**
     * @param decorator
     *            the decorator to set
     */
    public void setDecorator(ITickDecorator decorator) {
        this.decorator = decorator;
    }

    /**
     * Creates a new slider
     * 
     * @param revisions
     */
    public TimeLapseSlider(IP4Revision[] revisions) {
        setRevisions(revisions);
    }

    /**
     * Creates a new slider
     * 
     * @param revisions
     * @param scaleTypes
     */
    public TimeLapseSlider(IP4Revision[] revisions, Type[] scaleTypes) {
        setRevisions(revisions);
        if (scaleTypes != null && scaleTypes.length > 0
                && scaleTypes[0] != null) {
            this.scaleTypes = scaleTypes;
        }
    }

    /**
     * Filter the revisions displayed against the configured
     * {@link ITickPositionHandler}
     * 
     * @param reset
     * @return - true if the slider was redraw/updated, false otherwise
     */
    public boolean filter(boolean reset) {
        IP4Revision[] previous = this.revisions;
        int newPosition = -1;
        if (this.positioner != null && this.positioner.isEnabled()) {
            this.revisions = this.positioner.filter(allRevisions);
            newPosition = this.positioner.getNewPosition();
        } else {
            this.revisions = allRevisions;
        }
        if (newPosition < 0) {
            newPosition = this.revisions.length - 1;
        }
        boolean force = true;
        if (!reset) {
            if (previous != null && position >= 0 && position < previous.length) {
                // Filtered input is different to try to find new position
                if (!Arrays.equals(previous, this.revisions)) {
                    IP4Revision previousRev = previous[position];
                    int found = this.revisions==null?-1:Arrays
                            .binarySearch(this.revisions, previousRev);
                    if (found >= 0) {
                        newPosition = found;
                    }
                } else {
                    // Leave position alone if arrays are equal
                    newPosition = position;
                    force = false;
                }
            }
        }
        return updatePosition(newPosition, force);
    }

    private void setRevisions(IP4Revision[] revisions) {
        this.allRevisions = revisions;
        filter(true);
    }

    /**
     * Update the revisions displayed in this slider. This must be called from
     * the ui-thread since the slider is redrawn in this method. If the
     * specified revision is null or not found then the latest revisionw will be
     * displayed.
     * 
     * @param newRevisions
     * @param revision
     */
    public void resetRevisions(IP4Revision[] newRevisions, IP4Revision revision) {
        setRevisions(newRevisions);
        if (revision != null) {
            for (int i = 0; i < revisions.length; i++) {
                if (revision.getContentIdentifier().equals(
                        revisions[i].getContentIdentifier())) {
                    this.position = i;
                    break;
                }
            }
        }
        redraw();
        update();
        updateActions();
    }

    /**
     * Get the slider's toolbar
     * 
     * @return - toolbar
     */
    public ToolBar getToolbar() {
        return this.toolbar;
    }

    /**
     * Set slider's listener
     * 
     * @param listener
     */
    public void setListener(IRevisionListener listener) {
        this.listener = listener;
    }

    private int getTickStart(int width) {
        return revisions.length == 1 ? width / 2 : SLIDER_X1 + TICK_X1;
    }

    private int[] getTickSpacing(int width) {
        int spaces = revisions.length;
        int[] spacing = new int[spaces];
        int space = width - SLIDER_X2 - TICK_X1 - TICK_X2;
        int tickSpacing = space;
        if (spaces > 2) {
            tickSpacing = tickSpacing / (spaces - 1);
        }
        int overflow = space - ((spaces - 1) * tickSpacing);
        if (overflow > 0) {
            int each = Math.max(1, overflow / spaces);
            for (int i = 0; i < spacing.length - 1; i++) {
                int size = tickSpacing;
                if (overflow > 0) {
                    size += each;
                    overflow -= each;
                }
                spacing[i] = size;
            }
        } else {
            Arrays.fill(spacing, tickSpacing);
        }
        return spacing;
    }

    private int getTickOffset(int tickStart) {
        int offset = tickStart;
        for (int i = 0; i < position; i++) {
            offset += this.spaces[i];
        }
        return offset;
    }

    private int getGrabPosition(int width) {
        return getTickOffset(getTickStart(width));
    }

    private boolean updatePosition(int newPosition) {
        return updatePosition(newPosition, false);
    }

    private boolean updatePosition(int newPosition, boolean force) {
        if (force || this.position != newPosition) {
            this.position = newPosition;
            inUpdate = true;
            inRedraw = true;
            inActionUpdate = true;
            if (this.listener != null) {
            	if(this.revisions!=null && this.revisions.length>this.position){
            		IP4Revision rev = this.revisions[this.position];
            		if(rev!=null)
            			this.listener.revisionChanged(rev);
            	}
            }
            inRedraw = false;
            redraw();
            inUpdate = false;
            update();
            inActionUpdate = false;
            updateActions();
            return true;
        }
        return false;
    }

    /**
     * Redraw the slider
     */
    public void redraw() {
        if (!inRedraw && this.slider != null) {
            this.slider.redraw();
        }
    }

    /**
     * Update the slider
     */
    public void update() {
        if (!inUpdate && this.slider != null) {
            slider.update();
        }
    }

    private void createToolbar(Composite parent) {
        Composite toolArea = new Composite(parent, SWT.NONE);
        GridLayout taLayout = new GridLayout(3, false);
        taLayout.marginHeight = 0;
        taLayout.marginWidth = 0;
        toolArea.setLayout(taLayout);
        toolArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Label scaleLabel = new Label(toolArea, SWT.LEFT);
        scaleLabel.setText(Messages.TimeLapseSlider_Scale);

        options = new Combo(toolArea, SWT.DROP_DOWN | SWT.READ_ONLY);
        for (Type type : this.scaleTypes) {
            if (type != null) {
                switch (type) {
                case REVISION:
                    options.add(Messages.TimeLapseSlider_Revisions);
                    break;
                case CHANGELIST:
                    options.add(Messages.TimeLapseSlider_Changelists);
                    break;
                case DATE:
                    options.add(Messages.TimeLapseSlider_Date);
                    break;
                default:
                    break;
                }
            }
        }
        this.type = this.scaleTypes[0];
        options.select(0);
        options.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = options.getSelectionIndex();
                if (index >= 0) {
                    type = scaleTypes[index];
                }
                redraw();
                update();
                if (listener != null) {
                    listener.displayChanged(type);
                }
            }

        });

        toolbar = new ToolBar(toolArea, SWT.FLAT | SWT.WRAP);
        toolbar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        previous = new ToolItem(toolbar, SWT.PUSH);
        previous.setImage(PlatformUI.getWorkbench().getSharedImages()
                .getImage(ISharedImages.IMG_TOOL_BACK));
        previous.setToolTipText(Messages.TimeLapseSlider_ShowPreviousRev);
        previous.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                int newPosition = position - 1;
                if (positioner != null && positioner.isEnabled()) {
                    newPosition = positioner.getPrevious(position);
                }
                if (newPosition >= 0) {
                    updatePosition(newPosition);
                }
            }

        });

        next = new ToolItem(toolbar, SWT.PUSH);
        next.setToolTipText(Messages.TimeLapseSlider_ShowNextRev);
        next.setImage(PlatformUI.getWorkbench().getSharedImages()
                .getImage(ISharedImages.IMG_TOOL_FORWARD));
        next.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                int newPosition = position + 1;
                if (positioner != null && positioner.isEnabled()) {
                    newPosition = positioner.getNext(position);
                }
                if (newPosition >= 0 && newPosition < revisions.length) {
                    updatePosition(newPosition);
                }
            }

        });
    }

    /**
     * Update action enablement
     */
    public void updateActions() {
        if (!inActionUpdate && next != null && previous != null) {
            if (positioner == null || !positioner.isEnabled()) {
                next.setEnabled(revisions==null?false:position + 1 < revisions.length);
                previous.setEnabled(position > 0);
            } else {
                next.setEnabled(positioner.hasNextPosition(position));
                previous.setEnabled(positioner.hasPreviousPosition(position));
            }
        }
    }

    private int computeTickStart(int position) {
        int start = 0;
        for (int i = 0; i < position; i++) {
            start += spaces[i];
        }
        return start;
    }

    private int computeHeightHint() {
        int height = SLIDER_HEIGHT + SLIDER_Y1 + TICK_HEIGHT;
        int textHeight = MARGIN_BOTTOM;
        if (sliderFont != null) {
            GC gc = new GC(slider);
            try {
                gc.setFont(sliderFont);
                FontMetrics metrics = gc.getFontMetrics();
                textHeight = metrics.getHeight();
            } finally {
                gc.dispose();
            }
        }
        height += textHeight + BOTTOM_PADDING;
        return height;
    }

    private void updateSliderHeight(boolean layout) {
        GridData sliderData = (GridData) slider.getLayoutData();
        if (drawDecorations) {
            sliderData.heightHint = decoratorY + DECORATOR_Y;
        } else {
            sliderData.heightHint = decoratorY;
        }
        if (layout) {
            Composite parent = slider.getParent();
            if (parent.getParent() != null) {
                parent.getParent().layout(true, true);
            } else {
                parent.layout(true, true);
            }
        }
    }

    private void createBar(Composite parent) {
        slider = new Canvas(parent, SWT.NO_FOCUS | SWT.DOUBLE_BUFFERED);
        moveCursor = new Cursor(slider.getDisplay(), SWT.CURSOR_SIZEWE);
        P4UIUtils.registerDisposal(slider, moveCursor);
        markBg = new Color(slider.getDisplay(), MARK_BG);
        inactiveTickBg = slider.getDisplay()
                .getSystemColor(SWT.COLOR_DARK_GRAY);
        // Use text font since it is probably mono-spaced
        sliderFont = JFaceResources.getFont(JFaceResources.TEXT_FONT);
        P4UIUtils.registerDisposal(slider, markBg);

        final Image sliderBg = PerforceUIPlugin.imageDescriptorFromPlugin(
                PerforceUIPlugin.ID, SLIDER_BG_PATH).createImage();
        final Rectangle sliderBgBounds = sliderBg.getBounds();
        P4UIUtils.registerDisposal(slider, sliderBg);

        final Image grabberFg = PerforceUIPlugin.imageDescriptorFromPlugin(
                PerforceUIPlugin.ID, GRABBER_FG_PATH).createImage();
        final Rectangle grabberFgBounds = grabberFg.getBounds();
        P4UIUtils.registerDisposal(slider, grabberFg);

        GridData sliderData = new GridData(SWT.FILL, SWT.FILL, true, true);
        decoratorY = computeHeightHint();
        slider.setLayoutData(sliderData);
        updateSliderHeight(false);
        slider.addPaintListener(new PaintListener() {

            public void paintControl(PaintEvent e) {
                Point size = slider.getSize();

                if (buffer != null) {
                    Rectangle r = buffer.getBounds();
                    if (r.width != size.x || r.height != size.y) {
                        buffer.dispose();
                        buffer = null;
                    }
                }
                if (buffer == null) {
                    buffer = new Image(e.display, size.x, size.y);
                }

                GC gc = new GC(buffer);

                try {

                    gc.setFont(sliderFont);
                    // Draw slider
                    gc.setBackground(e.gc.getBackground());
                    gc.fillRectangle(0, 0, size.x, size.y);
                    gc.drawImage(sliderBg, 0, 0, sliderBgBounds.width,
                            sliderBgBounds.height, SLIDER_X1, SLIDER_Y1, size.x
                                    - SLIDER_X2, 13);

                    gc.setFont(sliderFont);
                    // Draw slider
                    gc.setForeground(e.display.getSystemColor(SWT.COLOR_BLACK));
                    gc.drawRectangle(SLIDER_X1, SLIDER_Y1, size.x - SLIDER_X2,
                            SLIDER_HEIGHT);

                    // Draw ticks
                    spaces = getTickSpacing(size.x);
                    int tickStart = getTickStart(size.x);
                    Color black = e.display.getSystemColor(SWT.COLOR_BLACK);
                    gc.setForeground(black);
                    paintSlider(e, gc, tickStart, size, black, type);

                    // Draw slider marker indicating current revision shown
                    int grabberPosition = getGrabPosition(size.x);
                    gc.drawImage(grabberFg, 0, 0, grabberFgBounds.width,
                            grabberFgBounds.height, grabberPosition - MARK_X1,
                            MARK_Y1, MARK_WIDTH, MARK_HEIGHT);

                    e.gc.drawImage(buffer, 0, 0);
                } finally {
                    gc.dispose();
                }
            }
        });
        slider.addMouseListener(new MouseListener() {

            public void mouseUp(MouseEvent e) {
                mouseDown = false;
                updateCursor(e);
            }

            public void mouseDown(MouseEvent e) {
                mouseDown = true;
                updateCursor(e);
            }

            public void mouseDoubleClick(MouseEvent e) {
                mouseDown = false;
                updateCursor(e);
                changePosition(e);
            }
        });
        slider.addMouseMoveListener(new MouseMoveListener() {

            public void mouseMove(MouseEvent e) {
                updateCursor(e);
                if (draggingGrab()) {
                    changePosition(e);
                }
            }
        });
    }

    private void paintSlider(PaintEvent e, GC gc, int tickStart, Point size,
            Color black, Type type) {
        int space = 0;
        int lastX = 0;
        int currTick = tickStart;
        if (formatter == null) {
            for (IP4Revision revision : revisions) {
                lastX = drawTick(size.x, currTick, gc, lastX, revision, type);
                currTick += spaces[space];
                space++;
            }
            // Overlay current tick so it is always visible
            drawTick(size.x, tickStart + computeTickStart(position), gc, black,
                    -1, revisions[position], type);
        } else {
            for (int i = 0; i < revisions.length; i++) {
                Color bg = formatter.format(revisions[i], i, e);
                lastX = drawTick(size.x, currTick, gc, bg, lastX, revisions[i],
                        type);
                currTick += spaces[space];
                space++;
            }
            Color bg = formatter.format(revisions[position], position, e);
            if (bg == null
                    || bg == e.display.getSystemColor(SWT.COLOR_DARK_GRAY)) {
                bg = black;
            }
            // Overlay current tick so it is always visible
            drawTick(size.x, tickStart + computeTickStart(position), gc, bg,
                    -1, revisions[position], type);
        }
    }

    private int drawTick(int width, int x, GC gc, String text, Color color,
            int lastEndX, IP4Revision revision) {
        int endX = lastEndX;
        gc.setForeground(color);
        gc.drawLine(x, TICK1_Y1, x, TICK1_Y2);
        gc.drawLine(x, TICK2_Y1, x, TICK2_Y2);
        int size = gc.stringExtent(text).x + TICK_PADDING;
        int tickX = x - (size / 2) + TICK_PADDING_OFFSET;
        if (tickX < 0) {
            // Correct if tick overflows the left
            tickX = 1;
        } else if (x + size > width) {
            // Correct if tick overflows the right
            tickX = width - size;
        }
        if (tickX > lastEndX) {
            if (drawDecorations && decorator != null) {
                decorator.decorate(revision, x, decoratorY, gc);
            }
            endX = tickX + size;
            gc.drawText(text, tickX, LABEL_Y1);
        }
        return endX;
    }

    private int drawTick(int width, int x, GC gc, Color color, int lastEndX,
            IP4Revision revision, Type type) {
        switch (type) {
        case REVISION:
            return drawTick(width, x, gc,
                    Integer.toString(revision.getRevision()), color, lastEndX,
                    revision);
        case CHANGELIST:
            return drawTick(width, x, gc,
                    Integer.toString(revision.getChangelist()), color,
                    lastEndX, revision);
        case DATE:
            return drawTick(width, x, gc,
                    TimeLapseUtils.format(revision.getTimestamp()), color,
                    lastEndX, revision);

        default:
            return 0;
        }
    }

    private int drawTick(int width, int x, GC gc, int lastEndX,
            IP4Revision revision, Type type) {
        return drawTick(width, x, gc, this.inactiveTickBg, lastEndX, revision,
                type);
    }

    private void changePosition(MouseEvent e) {
        // find closest position
        int closest = getClosestPosition(e);
        // Switch if not current position
        updatePosition(closest);
    }

    private int getClosestPosition(MouseEvent e) {
        Point size = slider.getSize();
        int start = getTickStart(size.x);
        int current = start;
        int closest = 0;
        int space = 0;
        while (current < e.x && closest < this.revisions.length) {
            int spacing = this.spaces[space];
            if (e.x > current + (spacing / 2)) {
                closest++;
                current += spacing;
            } else {
                break;
            }
            space++;
        }
        closest = Math.min(this.revisions.length - 1, closest);
        return closest;
    }

    private boolean draggingGrab() {
        return mouseDown && currentCursor == moveCursor;
    }

    private void updateCursor(MouseEvent e) {
        if (overGrab(e)) {
            currentCursor = moveCursor;
            slider.setCursor(currentCursor);
        } else if (!mouseDown && currentCursor == moveCursor) {
            currentCursor = null;
            slider.setCursor(currentCursor);
        }
    }

    private boolean overGrab(MouseEvent e) {
        Point size = slider.getSize();
        int grabberX = getGrabPosition(size.x) - MARK_X1;
        return e.x >= grabberX && e.x <= grabberX + MARK_WIDTH
                && e.y >= MARK_Y1 && e.y <= MARK_Y1 + MARK_HEIGHT;
    }

    /**
     * Set whether or not to draw tick decorations
     * 
     * @param draw
     */
    public void setDrawDecorations(boolean draw) {
        this.drawDecorations = draw;
        if (slider != null) {
            updateSliderHeight(true);
        }
    }

    /**
     * Create slider control
     * 
     * @param parent
     */
    public void createControl(Composite parent) {
        displayArea = new Composite(parent, SWT.NONE);
        GridLayout daLayout = new GridLayout(1, true);
        daLayout.verticalSpacing = 2;
        displayArea.setLayout(daLayout);
        displayArea
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        createToolbar(displayArea);
        createBar(displayArea);
        updateActions();
    }

}
