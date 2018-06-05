/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.text.timelapse;

import com.perforce.team.ui.text.TextUtils;

import java.util.Arrays;

import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.jface.text.source.IVerticalRulerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public abstract class TextRuler implements IVerticalRulerColumn {

    private IColorPainter painter;

    /**
     * Max number
     */
    protected int maxNumber = -1;

    /**
     * Computer largest number shown
     * 
     * @return - largest number shown
     */
    protected abstract int computeMaxNumber();

    /**
     * Update the size of this ruler
     */
    public void updateSize() {
        maxNumber = computeMaxNumber();
        updateNumberOfDigits();
        computeIndentations();
        redraw();
    }

    /**
     * Set the color painter for this ruler
     * 
     * @param painter
     */
    public void setPainter(IColorPainter painter) {
        this.painter = painter;
    }

    /**
     * Internal listener class.
     */
    class InternalListener implements IViewportListener, ITextListener {

        private boolean fCachedRedrawState = true;

        public void viewportChanged(int verticalPosition) {
            if (fCachedRedrawState && verticalPosition != fScrollPos) {
                redraw();
            }
        }

        public void textChanged(TextEvent event) {

            fCachedRedrawState = event.getViewerRedrawState();
            if (!fCachedRedrawState) {
                return;
            }

            if (updateNumberOfDigits()) {
                computeIndentations();
                layout(event.getViewerRedrawState());
                return;
            }

            boolean viewerCompletelyShown = isViewerCompletelyShown();
            if (viewerCompletelyShown || fSensitiveToTextChanges
                    || event.getDocumentEvent() == null) {
                postRedraw();
            }
            fSensitiveToTextChanges = viewerCompletelyShown;
        }
    }

    /** Cached text viewer */
    private ITextViewer viewer;
    /** Cached text widget */
    private StyledText text;
    /** The columns canvas */
    private Canvas displayArea;
    /** Cache for the actual scroll position in pixels */
    private int fScrollPos;
    /** The drawable for double buffering */
    private Image buffer;
    /** The internal listener */
    private InternalListener fInternalListener = new InternalListener();
    /** The font of this column */
    private Font font;
    /** The indentation cache */
    protected int[] fIndentation;
    /** Indicates whether this column reacts on text change events */
    private boolean fSensitiveToTextChanges = false;
    /** The foreground color */
    private Color foreground;
    /** The background color */
    private Color background;
    /** Cached number of displayed digits */
    protected int fCachedNumberOfDigits = -1;
    /** Flag indicating whether a relayout is required */
    private boolean fRelayoutRequired = false;
    /**
     * Redraw runnable lock
     */
    private Object fRunnableLock = new Object();
    /**
     * Redraw runnable state
     */
    private boolean fIsRunnablePosted = false;
    /**
     * Redraw runnable
     */
    private Runnable fRunnable = new Runnable() {

        public void run() {
            synchronized (fRunnableLock) {
                fIsRunnablePosted = false;
            }
            redraw();
        }
    };

    private boolean listenToDocument = false;

    /**
     * Create a text ruler
     */
    public TextRuler() {
        this.listenToDocument = true;
    }

    /**
     * Create a text ruler
     * 
     * @param listenToDocument
     */
    public TextRuler(boolean listenToDocument) {
        this.listenToDocument = listenToDocument;
    }

    /**
     * Get text viewer
     * 
     * @return - text viewer
     */
    protected ITextViewer getViewer() {
        return this.viewer;
    }

    /**
     * Get text widget
     * 
     * @return - text widget
     */
    protected StyledText getTextWidget() {
        return this.text;
    }

    /**
     * Sets the foreground color of this column.
     * 
     * @param foreground
     *            the foreground color
     */
    public void setForeground(Color foreground) {
        this.foreground = foreground;
    }

    /**
     * Sets the background color of this column.
     * 
     * @param background
     *            the background color
     */
    public void setBackground(Color background) {
        this.background = background;
        if (displayArea != null && !displayArea.isDisposed()) {
            displayArea.setBackground(getBackground(displayArea.getDisplay()));
        }
    }

    /**
     * Returns the System background color for list widgets.
     * 
     * @param display
     *            the display
     * @return the System background color for list widgets
     */
    protected Color getBackground(Display display) {
        if (background == null) {
            return display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
        }
        return background;
    }

    /**
     * @see org.eclipse.jface.text.source.IVerticalRulerColumn#getControl()
     */
    public Control getControl() {
        return displayArea;
    }

    /**
     * @see org.eclipse.jface.text.source.IVerticalRulerColumn#getWidth()
     */
    public int getWidth() {
        return fIndentation[0] + 5;
    }

    /**
     * Computes the number of digits to be displayed. Returns <code>true</code>
     * if the number of digits changed compared to the previous call of this
     * method. If the method is called for the first time, the return value is
     * also <code>true</code>.
     * 
     * @return whether the number of digits has been changed
     */
    protected boolean updateNumberOfDigits() {
        if (viewer == null) {
            return false;
        }

        int digits = Math.max(1, this.maxNumber);

        if (fCachedNumberOfDigits != digits) {
            fCachedNumberOfDigits = digits;
            return true;
        }

        return false;
    }

    /**
     * Does the real computation of the number of digits. Subclasses may
     * override this method if they need extra space on the line number ruler.
     * 
     * @param maxNumber
     * @return the number of digits to be displayed on the line number ruler.
     */
    protected int computeNumberOfDigits(int maxNumber) {
        int digits = 2;
        while (maxNumber > Math.pow(10, digits) - 1) {
            ++digits;
        }
        return digits;
    }

    /**
     * Layouts the enclosing viewer to adapt the layout to changes of the size
     * of the individual components.
     * 
     * @param redraw
     *            <code>true</code> if this column can be redrawn
     */
    protected void layout(boolean redraw) {
        if (!redraw) {
            fRelayoutRequired = true;
            return;
        }

        fRelayoutRequired = false;
        if (viewer instanceof ITextViewerExtension) {
            ITextViewerExtension extension = (ITextViewerExtension) viewer;
            Control control = extension.getControl();
            if (control instanceof Composite && !control.isDisposed()) {
                Composite composite = (Composite) control;
                composite.layout(true);
            }
        }
    }

    /**
     * Computes the indentations for the given font and stores them in
     * <code>fIndentation</code>.
     */
    protected void computeIndentations() {
        if (displayArea == null || displayArea.isDisposed()) {
            return;
        }

        GC gc = new GC(displayArea);
        try {

            gc.setFont(displayArea.getFont());

            fIndentation = new int[fCachedNumberOfDigits + 1];

            char[] nines = new char[fCachedNumberOfDigits];
            Arrays.fill(nines, '9');
            String nineString = new String(nines);
            Point p = gc.stringExtent(nineString);
            fIndentation[0] = p.x;

            for (int i = 1; i <= fCachedNumberOfDigits; i++) {
                p = gc.stringExtent(nineString.substring(0, i));
                fIndentation[i] = fIndentation[0] - p.x;
            }

        } finally {
            gc.dispose();
        }
    }

    /**
     * @see org.eclipse.jface.text.source.IVerticalRulerColumn#createControl(org.eclipse.jface.text.source.CompositeRuler,
     *      org.eclipse.swt.widgets.Composite)
     */
    public Control createControl(CompositeRuler parentRuler,
            Composite parentControl) {

        viewer = parentRuler.getTextViewer();
        if(viewer!=null)
        	text = viewer.getTextWidget();

        displayArea = new Canvas(parentControl, SWT.NO_FOCUS
                | SWT.DOUBLE_BUFFERED);
        displayArea.setBackground(getBackground(displayArea.getDisplay()));
        displayArea.setForeground(foreground);
        displayArea
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));

        displayArea.addPaintListener(new PaintListener() {

            public void paintControl(PaintEvent event) {
                if (viewer != null) {
                    doubleBufferPaint(event.gc);
                }
            }
        });

        displayArea.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                handleDispose();
                viewer = null;
                text = null;
            }
        });

        if (viewer != null) {

            viewer.addViewportListener(fInternalListener);
            if (listenToDocument) {
                viewer.addTextListener(fInternalListener);
            }

            if (font == null) {
                if (text != null && !text.isDisposed()) {
                    font = text.getFont();
                }
            }
        }

        if (font != null) {
            displayArea.setFont(font);
        }

        updateNumberOfDigits();
        computeIndentations();
        return displayArea;
    }

    /**
     * Disposes the column's resources.
     */
    protected void handleDispose() {

        if (viewer != null) {
            viewer.removeViewportListener(fInternalListener);
            if (listenToDocument) {
                viewer.removeTextListener(fInternalListener);
            }
        }

        if (buffer != null) {
            buffer.dispose();
            buffer = null;
        }
    }

    /**
     * Double buffer drawing.
     * 
     * @param dest
     *            the GC to draw into
     */
    private void doubleBufferPaint(GC dest) {

        Point size = displayArea.getSize();

        if (size.x <= 0 || size.y <= 0) {
            return;
        }

        if (buffer != null) {
            Rectangle r = buffer.getBounds();
            if (r.width != size.x || r.height != size.y) {
                buffer.dispose();
                buffer = null;
            }
        }
        if (buffer == null) {
            buffer = new Image(displayArea.getDisplay(), size.x, size.y);
        }

        GC gc = new GC(buffer);
        gc.setFont(displayArea.getFont());
        if (foreground != null) {
            gc.setForeground(foreground);
        }

        try {
            gc.setBackground(getBackground(displayArea.getDisplay()));
            gc.fillRectangle(0, 0, size.x, size.y);

            ILineRange visibleLines = TextUtils.getVisibleModelLines(viewer);
            if (visibleLines == null) {
                return;
            }
            fScrollPos = text.getTopPixel();
            doPaint(gc, visibleLines);
        } finally {
            gc.dispose();
        }

        dest.drawImage(buffer, 0, 0);
    }

    /**
     * Returns <code>true</code> if the viewport displays the entire viewer
     * contents, i.e. the viewer is not vertically scrollable.
     * 
     * @return <code>true</code> if the viewport displays the entire contents,
     *         <code>false</code> otherwise
     */
    protected final boolean isViewerCompletelyShown() {
        return TextUtils.isShowingEntireContents(text);
    }

    /**
     * Paint lines
     * 
     * @param startLine
     * @param endLine
     * @param width
     * @param y
     * @param gc
     * @param display
     */
    protected void paintLines(int startLine, int endLine, int width, int y,
            GC gc, Display display) {
        for (int line = startLine; line < endLine; line++) {
            int widgetLine = TextUtils.modelLineToWidgetLine(viewer, line);
            if (widgetLine == -1) {
                continue;
            }

            int lineHeight = text.getLineHeight(text
                    .getOffsetAtLine(widgetLine));
            paintBackground(line, gc, y, width, lineHeight);
            paintLine(line, y, lineHeight, gc, display);
            y += lineHeight;
        }
    }

    /**
     * Paint line background
     * 
     * @param line
     * @param gc
     * @param y
     * @param width
     * @param height
     */
    protected void paintBackground(int line, GC gc, int y, int width, int height) {
        if (painter != null) {
            Color color = painter.getColor(line);
            if (color != null) {
                gc.setBackground(color);
                gc.fillRectangle(0, y, width, height);
            } else {
                gc.setBackground(getBackground(displayArea.getDisplay()));
            }
        }
    }

    /**
     * Draws the ruler column.
     * 
     * @param gc
     *            the GC to draw into
     * @param visibleLines
     *            the visible model lines
     */
    protected void doPaint(GC gc, ILineRange visibleLines) {
        Display display = text.getDisplay();

        // draw diff info
        int y = -TextUtils.getHiddenTopLinePixels(text);
        int lastLine = end(visibleLines);
        int width = displayArea.getSize().x;
        paintLines(visibleLines.getStartLine(), lastLine, width, y, gc, display);
    }

    private static int end(ILineRange range) {
        return range.getStartLine() + range.getNumberOfLines();
    }

    /**
     * Returns the difference between the baseline of the widget and the
     * baseline as specified by the font for <code>gc</code>. When drawing line
     * numbers, the returned bias should be added to obtain text lined up on the
     * correct base line of the text widget.
     * 
     * @param gc
     *            the <code>GC</code> to get the font metrics from
     * @param widgetLine
     *            the widget line
     * @return the baseline bias to use when drawing text that is lined up with
     *         <code>fCachedTextWidget</code>
     */
    private int getBaselineBias(GC gc, int widgetLine) {
        /*
         * https://bugs.eclipse.org/bugs/show_bug.cgi?id=62951 widget line
         * height may be more than the font height used for the line numbers,
         * since font styles (bold, italics...) can have larger font metrics
         * than the simple font used for the numbers.
         */
        int offset = text.getOffsetAtLine(widgetLine);
        int widgetBaseline = text.getBaseline(offset);

        FontMetrics fm = gc.getFontMetrics();
        int fontBaseline = fm.getAscent() + fm.getLeading();
        int baselineBias = widgetBaseline - fontBaseline;
        return Math.max(0, baselineBias);
    }

    /**
     * Paints the line. After this method is called the line numbers are painted
     * on top of the result of this method.
     * 
     * @param line
     *            the line of the document which the ruler is painted for
     * @param y
     *            the y-coordinate of the box being painted for
     *            <code>line</code>, relative to <code>gc</code>
     * @param lineheight
     *            the height of one line (and therefore of the box being
     *            painted)
     * @param gc
     *            the drawing context the client may choose to draw on.
     * @param display
     *            the display the drawing occurs on
     */
    protected void paintLine(int line, int y, int lineheight, GC gc,
            Display display) {
        int widgetLine = TextUtils.modelLineToWidgetLine(viewer, line);

        String s = getLineText(line);
        if (s != null && s.length() < fIndentation.length) {
            int indentation = fIndentation[s.length()];
            int baselineBias = getBaselineBias(gc, widgetLine);

            gc.drawString(s, indentation, y + baselineBias, true);
        }
    }

    /**
     * Get line text of specified line
     * 
     * @param line
     * @return - line text to draw
     */
    protected abstract String getLineText(int line);

    /**
     * Triggers a redraw in the display thread.
     */
    protected final void postRedraw() {
        if (displayArea != null && !displayArea.isDisposed()) {
            Display d = displayArea.getDisplay();
            if (d != null) {
                synchronized (fRunnableLock) {
                    if (fIsRunnablePosted) {
                        return;
                    }
                    fIsRunnablePosted = true;
                }
                d.asyncExec(fRunnable);
            }
        }
    }

    /**
     * Mark this text ruler for redraw
     */
    public void markForRedraw() {
        if (this.displayArea != null && !displayArea.isDisposed()) {
            this.displayArea.redraw();
        }
    }

    /**
     * @see org.eclipse.jface.text.source.IVerticalRulerColumn#redraw()
     */
    public void redraw() {
        if (viewer != null) {

            if (fRelayoutRequired) {
                layout(true);
                return;
            }

            if (displayArea != null && !displayArea.isDisposed()) {
                GC gc = new GC(displayArea);
                doubleBufferPaint(gc);
                gc.dispose();
            }
        }
    }

    /**
     * @see org.eclipse.jface.text.source.IVerticalRulerColumn#setModel(org.eclipse.jface.text.source.IAnnotationModel)
     */
    public void setModel(IAnnotationModel model) {
    }

    /**
     * @see org.eclipse.jface.text.source.IVerticalRulerColumn#setFont(org.eclipse.swt.graphics.Font)
     */
    public void setFont(Font font) {
        this.font = font;
        if (displayArea != null && !displayArea.isDisposed()) {
            displayArea.setFont(this.font);
            updateNumberOfDigits();
            computeIndentations();
        }
    }

    /**
     * Show or hide ruler
     * 
     * @param visible
     */
    public void setVisible(boolean visible) {
        ((GridData) this.displayArea.getLayoutData()).exclude = !visible;
        this.displayArea.setVisible(visible);
    }
}
