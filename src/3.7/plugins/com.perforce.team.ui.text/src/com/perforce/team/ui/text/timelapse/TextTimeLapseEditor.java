/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.text.timelapse;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineBackgroundEvent;
import org.eclipse.swt.custom.LineBackgroundListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.FindReplaceAction;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.text.PerforceUiTextPlugin;
import com.perforce.team.ui.text.TextUtils;
import com.perforce.team.ui.timelapse.IAnnotateModel.Type;
import com.perforce.team.ui.timelapse.TimeLapseEditor;
import com.perforce.team.ui.timelapse.TimeLapseInput;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public abstract class TextTimeLapseEditor extends TimeLapseEditor {

    /**
     * MOST_RECENT_COLOR
     */
    public static final String MOST_RECENT_COLOR = "com.perforce.team.ui.text.timelapse.MOST_RECENT_COLOR"; //$NON-NLS-1$

    /**
     * LEAST_RECENT_COLOR
     */
    public static final String LEAST_RECENT_COLOR = "com.perforce.team.ui.text.timelapse.LEAST_RECENT_COLOR"; //$NON-NLS-1$

    /**
     * SHOW_AGING
     */
    public static final String SHOW_AGING = "com.perforce.team.ui.text.timelapse.SHOW_AGING"; //$NON-NLS-1$

    /**
     * SHOW_AUTHORS
     */
    public static final String SHOW_AUTHORS = "com.perforce.team.ui.text.timelapse.SHOW_AUTHORS"; //$NON-NLS-1$

    /**
     * SHOW_RANGES
     */
    public static final String SHOW_RANGES = "com.perforce.team.ui.text.timelapse.SHOW_RANGES"; //$NON-NLS-1$

    /**
     * WHITESPACE_TYPE
     */
    public static final String WHITESPACE_TYPE = "com.perforce.team.ui.text.timelapse.WHITESPACE_TYPE"; //$NON-NLS-1$

    private static final int BLOCK_IMG_SIZE = 16;

    private ITextAnnotateModel model;
    private boolean aging = false;
    private IP4File.WhitespaceIgnoreType ignoreType;
    private boolean showingAuthors = false;
    private boolean showingRanges = false;
    private int topIndexModelLine = -1;

    /**
     * Is the editor currently in {@link #updateDocument(IEditorInput)}
     */
    protected boolean updatingDoc = false;

    /**
     * Is the entire editor control redraw and updated when
     * {@link #updateDocument(IEditorInput)} is called?
     * 
     * Currently this is on Windows and Linux systems to reduce flickering of
     * editor when showing a folding area
     */
    protected boolean redraw = false;

    /**
     * Background painters
     */
    private IColorPainter currentPainter;
    private IColorPainter diffPainter;
    private AgingPainter agingPainter;
    private RGB leastRecentRgb;
    private RGB mostRecentRgb;

    private Font textFont = null;

    /**
     * Editor rulers
     */
    private CompositeRuler ruler = null;
    private LineNumberRuler numbersRuler = null;
    private RevisionRuler lowerRevisionRuler;
    private UserRuler lowerUserRuler;
    private RevisionRuler upperRevisionRuler;
    private UserRuler upperUserRuler;

    private FindReplaceAction findAction;

    /**
     * @see com.perforce.team.ui.timelapse.TimeLapseEditor#fillToolbar(org.eclipse.swt.widgets.ToolBar)
     */
    @Override
    protected void fillToolbar(final ToolBar toolbar) {
        super.fillToolbar(toolbar);

        final ToolItem agingItem = new ToolItem(toolbar, SWT.DROP_DOWN);
        agingItem.setToolTipText(Messages.TextTimeLapseEditor_TextAging);
        agingItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                createAgingMenu(toolbar, agingItem);
            }

        });
        Image agingIcon = PerforceUiTextPlugin.getImageDescriptor(
                PerforceUiTextPlugin.IMG_AGING).createImage();
        P4UIUtils.registerDisposal(agingItem, agingIcon);
        agingItem.setImage(agingIcon);

        final ToolItem whitespaceItem = new ToolItem(toolbar, SWT.DROP_DOWN);
        whitespaceItem
                .setToolTipText(Messages.TextTimeLapseEditor_WhitespaceOptions);
        whitespaceItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                createWhitespaceMenu(toolbar, whitespaceItem);
            }
        });
        Image whitespaceIcon = PerforceUiTextPlugin.getImageDescriptor(
                PerforceUiTextPlugin.IMG_WHITESPACE).createImage();
        P4UIUtils.registerDisposal(whitespaceItem, whitespaceIcon);
        whitespaceItem.setImage(whitespaceIcon);

        final ToolItem userItem = new ToolItem(toolbar, SWT.CHECK);
        userItem.setToolTipText(Messages.TextTimeLapseEditor_ShowAuthorsInRuler);
        userItem.setSelection(this.showingAuthors);
        userItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                showingAuthors = userItem.getSelection();
                if (showingAuthors) {
                    createAuthorRulers();
                    redrawRulers();
                } else {
                    ruler.removeDecorator(lowerUserRuler);
                    ruler.removeDecorator(upperUserRuler);
                }
                updatePreferences();
            }

        });
        Image userIcon = PerforceUiTextPlugin.getImageDescriptor(
                PerforceUiTextPlugin.IMG_USER).createImage();
        P4UIUtils.registerDisposal(userItem, userIcon);
        userItem.setImage(userIcon);

        final ToolItem rangeItem = new ToolItem(toolbar, SWT.CHECK);
        rangeItem
                .setToolTipText(Messages.TextTimeLapseEditor_ShowRangesInRuler);
        rangeItem.setSelection(this.showingRanges);
        rangeItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                showingRanges = rangeItem.getSelection();
                if (showingRanges) {
                    createRevisionRulers();
                    redrawRulers();
                } else {
                    ruler.removeDecorator(lowerRevisionRuler);
                    ruler.removeDecorator(upperRevisionRuler);
                }
                updatePreferences();
            }

        });
        Image rangeIcon = PerforceUiTextPlugin.getImageDescriptor(
                PerforceUiTextPlugin.IMG_DATE).createImage();
        P4UIUtils.registerDisposal(rangeItem, rangeIcon);
        rangeItem.setImage(rangeIcon);
    }

    private void createWhitespaceMenu(final ToolBar toolbar,
            final ToolItem whitespaceItem) {
        Menu menu = new Menu(toolbar);

        final MenuItem ignoreAll = new MenuItem(menu, SWT.CHECK);
        ignoreAll
                .setText(Messages.TextTimeLapseEditor_IgnoreLineEndingAndAllWS);
        ignoreAll.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateIgnoreType(IP4File.WhitespaceIgnoreType.ALL);
            }

        });

        final MenuItem ignoreNone = new MenuItem(menu, SWT.CHECK);
        ignoreNone
                .setText(Messages.TextTimeLapseEditor_RecognizeLineEndingAndWS);
        ignoreNone.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateIgnoreType(null);
            }

        });

        final MenuItem ignoreWhitespace = new MenuItem(menu, SWT.CHECK);
        ignoreWhitespace
                .setText(Messages.TextTimeLapseEditor_IgnoreLineEndingAndWS);
        ignoreWhitespace.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateIgnoreType(IP4File.WhitespaceIgnoreType.WHITESPACE);
            }

        });

        final MenuItem ignoreLineEndings = new MenuItem(menu, SWT.CHECK);
        ignoreLineEndings
                .setText(Messages.TextTimeLapseEditor_IgnoreLineEnding);
        ignoreLineEndings.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateIgnoreType(IP4File.WhitespaceIgnoreType.LINE_ENDINGS);
            }

        });

        if (ignoreType != null) {
            switch (ignoreType) {
            case ALL:
                ignoreAll.setSelection(true);
                break;
            case WHITESPACE:
                ignoreWhitespace.setSelection(true);
                break;
            case LINE_ENDINGS:
                ignoreLineEndings.setSelection(true);
                break;
            default:
                break;
            }
        } else {
            ignoreNone.setSelection(true);
        }

        Rectangle bounds = whitespaceItem.getBounds();
        Point location = toolbar.toDisplay(bounds.x, bounds.y + bounds.height);
        menu.setLocation(location);
        menu.setVisible(true);
    }

    private void updateIgnoreType(IP4File.WhitespaceIgnoreType type) {
        if (this.ignoreType != type) {
            this.ignoreType = type;
            updatePreferences();
            preserveSelectionLoad();
        }
    }

    private void createAgingMenu(final ToolBar toolbar, final ToolItem agingItem) {
        Menu menu = new Menu(toolbar);
        final MenuItem showAging = new MenuItem(menu, SWT.CHECK);
        showAging.setText(Messages.TextTimeLapseEditor_DisplayTextAging);
        showAging.setSelection(aging);
        showAging.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                aging = showAging.getSelection();
                updatePreferences();
                updateAgingColors();
            }

        });

        final IPreferenceStore store = PerforceUiTextPlugin.getDefault()
                .getPreferenceStore();

        Image leastRecentImage = createBlockImage(toolbar.getDisplay(),
                leastRecentRgb);
        P4UIUtils.registerDisposal(menu, leastRecentImage);
        Image mostRecentImage = createBlockImage(toolbar.getDisplay(),
                mostRecentRgb);
        P4UIUtils.registerDisposal(menu, mostRecentImage);

        final MenuItem mostRecentColor = new MenuItem(menu, SWT.PUSH);
        mostRecentColor
                .setText(Messages.TextTimeLapseEditor_ConfigureMostRexentTextColor);
        mostRecentColor.setImage(mostRecentImage);
        mostRecentColor.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                ColorDialog dialog = new ColorDialog(toolbar.getShell());
                dialog.setRGB(mostRecentRgb);
                RGB newMostRecent = dialog.open();
                if (newMostRecent != null) {
                    PreferenceConverter.setValue(store, MOST_RECENT_COLOR,
                            newMostRecent);
                    mostRecentRgb = newMostRecent;
                    reloadAgingColors();
                }
            }

        });

        MenuItem leastRecentColor = new MenuItem(menu, SWT.PUSH);
        leastRecentColor
                .setText(Messages.TextTimeLapseEditor_ConfigureLeastRecentTextColor);
        leastRecentColor.setImage(leastRecentImage);
        leastRecentColor.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                ColorDialog dialog = new ColorDialog(toolbar.getShell());
                dialog.setRGB(leastRecentRgb);
                RGB newLeastRecent = dialog.open();
                if (newLeastRecent != null) {
                    PreferenceConverter.setValue(store, LEAST_RECENT_COLOR,
                            newLeastRecent);
                    leastRecentRgb = newLeastRecent;
                    reloadAgingColors();
                }
            }

        });

        Rectangle bounds = agingItem.getBounds();
        Point location = toolbar.toDisplay(bounds.x, bounds.y + bounds.height);
        menu.setLocation(location);
        menu.setVisible(true);
    }

    private Image createBlockImage(Display display, RGB rgb) {
        Image image = new Image(display, BLOCK_IMG_SIZE, BLOCK_IMG_SIZE);
        GC gc = new GC(image);
        Color color = new Color(display, rgb);
        try {
            gc.setBackground(new Color(display, rgb));
            gc.fillRectangle(0, 0, BLOCK_IMG_SIZE, BLOCK_IMG_SIZE);
        } finally {
            gc.dispose();
            color.dispose();
        }
        return image;
    }

    private void reloadAgingColors() {
        agingPainter.setLeastRecent(this.leastRecentRgb);
        agingPainter.setMostRecent(this.mostRecentRgb);
        agingPainter.loadColors(getRevision());
        updateAgingColors();
    }

    private void updateAgingColors() {
        resetPainter();
        getViewer().getTextWidget().redraw();
        getViewer().getTextWidget().update();
        redrawRulers();
    }

    /**
     * @see com.perforce.team.ui.timelapse.TimeLapseEditor#createHeader(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createHeader(Composite parent) {
        // Does nothing by default, subclasses should override.
    }

    private void updatePreferences() {
        IPreferenceStore store = PerforceUiTextPlugin.getDefault()
                .getPreferenceStore();
        store.setValue(SHOW_AGING, this.aging);
        store.setValue(SHOW_AUTHORS, this.showingAuthors);
        store.setValue(SHOW_RANGES, this.showingRanges);

        String type = ""; //$NON-NLS-1$
        if (ignoreType != null) {
            type = ignoreType.toString();
        }
        store.setValue(WHITESPACE_TYPE, type);
    }

    /**
     * @see com.perforce.team.ui.timelapse.TimeLapseEditor#updateDisplay(com.perforce.team.ui.timelapse.IAnnotateModel.Type)
     */
    @Override
    protected void updateDisplay(Type type) {
        super.updateDisplay(type);
        // Fix for job036652, only update revision rulers if showing range
        // option is enabled
        if (showingRanges) {
            ruler.removeDecorator(lowerRevisionRuler);
            ruler.removeDecorator(upperRevisionRuler);
            createRevisionRulers(type);
            redrawRulers();
        }

    }

    /**
     * Get the view showing text
     * 
     * @return - text viewer
     */
    protected abstract ITextViewer getViewer();

    /**
     * Refresh based on input
     * 
     * @param input
     */
    protected abstract void refresh(IEditorInput input);

    /**
     * Convert a widget line
     * 
     * @param widgetLine
     * @return - converted line
     */
    protected int convertToModelLine(int widgetLine) {
        return widgetLine;
    }

    /**
     * Convert a model line
     * 
     * @param modelLine
     * @return - converted line
     */
    protected int convertToWidgetLine(int modelLine) {
        return modelLine;
    }

    /**
     *
     */
    public TextTimeLapseEditor() {
    }

    /**
     * @see com.perforce.team.ui.timelapse.TimeLapseEditor#init(org.eclipse.ui.IEditorSite,
     *      org.eclipse.ui.IEditorInput)
     */
    @Override
    public void init(IEditorSite site, IEditorInput input)
            throws PartInitException {
        super.init(site, input);
        IPreferenceStore store = PerforceUiTextPlugin.getDefault()
                .getPreferenceStore();
        this.aging = store.getBoolean(SHOW_AGING);
        this.leastRecentRgb = PreferenceConverter.getColor(store,
                LEAST_RECENT_COLOR);
        this.mostRecentRgb = PreferenceConverter.getColor(store,
                MOST_RECENT_COLOR);
        this.showingAuthors = store.getBoolean(SHOW_AUTHORS);
        this.showingRanges = store.getBoolean(SHOW_RANGES);

        try {
            this.ignoreType = IP4File.WhitespaceIgnoreType.valueOf(store
                    .getString(WHITESPACE_TYPE));
        } catch (IllegalArgumentException e) {
            this.ignoreType = null;
        }
    }

    /**
     * Create the text annotate model to use for the specified p4 file
     * 
     * @param file
     * @return - non-null text annotate model
     */
    protected ITextAnnotateModel createTextAnnotateModel(IP4File file) {
        return new TextAnnotateModel(file, null,
                ((TimeLapseInput) getEditorInput()).useChangelistKeys());
    }

    /**
     * Get current annotate model
     * 
     * @return - text annotate model
     */
    public ITextAnnotateModel getAnnotateModel() {
        return this.model;
    }

    /**
     * @see com.perforce.team.ui.timelapse.TimeLapseEditor#loadEditor(com.perforce.team.core.p4java.IP4File,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    protected void loadEditor(IP4File file, IProgressMonitor monitor) {
        super.loadEditor(file, monitor);
        monitor.setTaskName(Messages.TextTimeLapseEditor_LoadP4Annotate
                + file.getActionPath());
        if (model == null) {
            model = createTextAnnotateModel(file);
        }
        model.clear();
        model.load(getRevisions(), showBranches(), getIgnoreType());
    }

    /**
     * Get configured whitespace ignore type
     * 
     * @return - current whitespace ignore type
     */
    protected IP4File.WhitespaceIgnoreType getIgnoreType() {
        return this.ignoreType;
    }

    /**
     * @see com.perforce.team.ui.timelapse.TimeLapseEditor#generateInput(com.perforce.team.core.p4java.IP4Revision)
     */
    @Override
    protected IStorageEditorInput generateInput(IP4Revision revision) {
        return model.generateInput(revision);
    }

    /**
     * @see com.perforce.team.ui.timelapse.TimeLapseEditor#updateDocument(org.eclipse.ui.IEditorInput)
     */
    @Override
    protected void updateDocument(IEditorInput input) {
        ITextViewer viewer = getViewer();
        if (viewer != null) {
            if (redraw) {
                getControl().setRedraw(false);
            }
            try {
                updatingDoc = true;
                this.topIndexModelLine = viewer.getTextWidget().getTopIndex();
                this.topIndexModelLine = convertToModelLine(topIndexModelLine);
                refresh(input);
                updateColors();
                updateRulers();
                updatingDoc = false;
                viewer.getTextWidget().redraw();
                updateVisibleRange();
                redrawRulers();
                findAction.update();
            } finally {
                if (redraw) {
                    getControl().setRedraw(true);
                    getControl().update();
                }
            }
        }

    }

    /**
     * Set viewer redraw
     * 
     * @param redraw
     */
    protected void setRedraw(boolean redraw) {
        getViewer().getTextWidget().setRedraw(redraw);
    }

    /**
     * Update the visible range. By default the top index will be set with the
     * last pre-update top index. Subclasses should override to show/select
     * custom ranges after {@link #refresh(IEditorInput)} is called during the
     * {@link #updateDocument(IEditorInput)} block.
     */
    protected void updateVisibleRange() {
        if (this.topIndexModelLine > -1) {
            setRedraw(false);
            getViewer().getTextWidget().setTopIndex(
                    convertToWidgetLine(this.topIndexModelLine));
            setRedraw(true);
        }
    }

    /**
     * Update text viewer line background colors
     */
    protected void updateColors() {
        IP4Revision revision = getRevision();
        agingPainter.loadColors(revision);
        diffPainter.loadColors(this.model.getLineRanges(revision));
    }

    /**
     * Get rgb value from the editor preference store
     * 
     * @param key
     * @return - rgb
     */
    protected RGB getEditorRgb(String key) {
        return TextUtils.getEditorRgb(key);
    }

    /**
     * Get editor color from the shared text colors store
     * 
     * @param key
     * @return - color
     */
    protected Color getEditorColor(String key) {
        return TextUtils.getEditorColor(key);
    }

    /**
     * Get editor shared text colors
     * 
     * @return - shared text colors
     */
    protected ISharedTextColors getSharedTextColors() {
        return TextUtils.getSharedTextColors();
    }

    private void resetPainter() {
        if (aging) {
            this.currentPainter = agingPainter;
        } else {
            this.currentPainter = diffPainter;
        }
        setRulerPainter(this.currentPainter);
    }

    private void setRulerPainter(IColorPainter painter) {
        if (this.numbersRuler != null) {
            this.numbersRuler.setPainter(painter);
        }
        if (this.lowerRevisionRuler != null) {
            this.lowerRevisionRuler.setPainter(painter);
        }

        if (this.upperRevisionRuler != null) {
            this.upperRevisionRuler.setPainter(painter);
        }
        if (this.lowerUserRuler != null) {
            this.lowerUserRuler.setPainter(painter);
        }
        if (this.upperUserRuler != null) {
            this.upperUserRuler.setPainter(painter);
        }
    }

    private void setRulerFont() {
        if (this.textFont != null) {
            if (this.lowerUserRuler != null) {
                this.lowerUserRuler.setFont(textFont);
            }
            if (this.lowerRevisionRuler != null) {
                this.lowerRevisionRuler.setFont(textFont);
            }
            if (this.upperUserRuler != null) {
                this.upperUserRuler.setFont(textFont);
            }
            if (this.upperRevisionRuler != null) {
                this.upperRevisionRuler.setFont(textFont);
            }
            if (this.numbersRuler != null) {
                this.numbersRuler.setFont(textFont);
            }
        }
    }

    private void updateRulers() {
        IP4Revision current = getRevision();
        if (this.lowerRevisionRuler != null) {
            this.lowerRevisionRuler.setRevision(current);
            this.lowerRevisionRuler.updateSize();
        }
        if (this.upperRevisionRuler != null) {
            this.upperRevisionRuler.setRevision(current);

            this.upperRevisionRuler.updateSize();
        }
        if (this.lowerUserRuler != null) {
            this.lowerUserRuler.setRevision(current);
            this.lowerUserRuler.updateSize();
        }
        if (this.upperUserRuler != null) {
            this.upperUserRuler.setRevision(current);
            this.upperUserRuler.updateSize();
        }
        if (this.numbersRuler != null) {
            this.numbersRuler.updateSize();
        }
    }

    private void createAuthorRulers() {
        lowerUserRuler = new LowerUserRuler(model);
        lowerUserRuler
                .setBackground(getEditorColor(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND));
        lowerUserRuler
                .setForeground(getEditorColor(AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND));
        ruler.addDecorator(this.showingRanges ? 1 : 0, lowerUserRuler);

        upperUserRuler = new UpperUserRuler(model);
        upperUserRuler
                .setBackground(getEditorColor(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND));
        upperUserRuler
                .setForeground(getEditorColor(AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND));
        ruler.addDecorator(this.showingRanges ? 3 : 1, upperUserRuler);

        IP4Revision revision = getRevision();
        if (revision != null) {
            lowerUserRuler.setRevision(revision);
            upperUserRuler.setRevision(revision);
            lowerUserRuler.updateSize();
            upperUserRuler.updateSize();
        }
        if (this.textFont != null) {
            lowerUserRuler.setFont(this.textFont);
            upperUserRuler.setFont(this.textFont);
        }
        resetPainter();
    }

    private void createRevisionRulers() {
        createRevisionRulers(getDisplayType());
    }

    private void createRevisionRulers(Type type) {
        lowerRevisionRuler = new LowerRevisionRuler(model);
        lowerRevisionRuler.setType(type);
        lowerRevisionRuler
                .setBackground(getEditorColor(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND));
        lowerRevisionRuler
                .setForeground(getEditorColor(AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND));
        ruler.addDecorator(0, lowerRevisionRuler);

        upperRevisionRuler = new UpperRevisionRuler(model);
        upperRevisionRuler.setType(type);
        upperRevisionRuler
                .setBackground(getEditorColor(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND));
        upperRevisionRuler
                .setForeground(getEditorColor(AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND));
        ruler.addDecorator(this.showingAuthors ? 2 : 1, upperRevisionRuler);

        IP4Revision revision = getRevision();
        if (revision != null) {
            lowerRevisionRuler.setRevision(revision);
            upperRevisionRuler.setRevision(revision);
            lowerRevisionRuler.updateSize();
            upperRevisionRuler.updateSize();
        }
        if (this.textFont != null) {
            lowerRevisionRuler.setFont(this.textFont);
            upperRevisionRuler.setFont(this.textFont);
        }
        resetPainter();
    }

    /**
     * Configure the viewer to have a custom font, be read-only, and fill space.
     * 
     * @param fontPrefName
     */
    protected void configureViewer(String fontPrefName) {
        ITextViewer viewer = getViewer();
        if (viewer != null) {
            GridData viewerData = new GridData(SWT.FILL, SWT.FILL, true, true);
            if (viewer instanceof Viewer) {
                ((Viewer) viewer).getControl().setLayoutData(viewerData);
            }
            this.textFont = JFaceResources.getFont(fontPrefName);
            final StyledText text = viewer.getTextWidget();
            text.setFont(this.textFont);
            viewer.setEditable(false);
            text.setLayoutData(viewerData);
            agingPainter = new AgingPainter(model, viewer);
            diffPainter = new DiffPainter();
            text.addLineBackgroundListener(new LineBackgroundListener() {

                public void lineGetBackground(LineBackgroundEvent event) {
                    int line = text.getLineAtOffset(event.lineOffset);
                    line = convertToModelLine(line);
                    event.lineBackground = currentPainter.getColor(line);
                }
            });
            setRulerFont();
            resetPainter();

            findAction = new FindReplaceAction(Messages.RESOURCE_BUNDLE, null, this);
            findAction
                    .setActionDefinitionId(IWorkbenchCommandConstants.EDIT_FIND_AND_REPLACE);
            getEditorSite().getKeyBindingService().registerAction(findAction);
        }
    }

    /**
     * @see org.eclipse.ui.part.WorkbenchPart#getAdapter(java.lang.Class)
     */
    @Override
    public Object getAdapter(Class adapter) {
        if (IFindReplaceTarget.class == adapter) {
            ITextViewer viewer = getViewer();
            if (viewer != null) {
                return viewer.getFindReplaceTarget();
            }
        }
        return super.getAdapter(adapter);
    }

    /**
     * Redraw all rulers by explicitly calling {@link #redrawRulers(boolean)}
     * with true.
     */
    protected void redrawRulers() {
        redrawRulers(true);
    }

    /**
     * Redraw rules by re-laying out the composite ruler they belong to. If
     * explicit if specified is true then redraw is called on each ruler
     * individually before re-laying out the parent ruler.
     * 
     * @param explicit
     */
    protected void redrawRulers(boolean explicit) {
        if (explicit) {
            if (numbersRuler != null) {
                numbersRuler.markForRedraw();
            }
            if (upperRevisionRuler != null) {
                upperRevisionRuler.markForRedraw();
            }
            if (lowerRevisionRuler != null) {
                lowerRevisionRuler.markForRedraw();
            }
            if (upperUserRuler != null) {
                upperUserRuler.markForRedraw();
            }
            if (lowerUserRuler != null) {
                lowerUserRuler.markForRedraw();
            }
        }
        if (ruler != null) {
            ITextViewer viewer = getViewer();
            Control parent = viewer.getTextWidget();
            if (viewer instanceof ITextViewerExtension) {
                ITextViewerExtension extension = (ITextViewerExtension) viewer;
                parent = extension.getControl();
            }
            if (parent instanceof Composite && !parent.isDisposed()) {
                ((Composite) parent).layout(true);
            }
        }
    }

    /**
     * @see com.perforce.team.ui.timelapse.TimeLapseEditor#dispose()
     */
    @Override
    public void dispose() {
        super.dispose();
        if (agingPainter != null) {
            agingPainter.dispose();
        }
        if (diffPainter != null) {
            diffPainter.dispose();
        }
        if (findAction != null) {
            getEditorSite().getKeyBindingService().unregisterAction(findAction);
        }
    }

    /**
     * Get composite ruler for this text time lapse editor, may be null if
     * {@link #configureRulers(boolean)} is never called.
     * 
     * @return - composite ruler
     */
    protected CompositeRuler getRuler() {
        return this.ruler;
    }

    /**
     * Configure rulers
     * 
     * @param addLineNumbers
     * @return - composite ruler
     */
    protected CompositeRuler configureRulers(boolean addLineNumbers) {
        ruler = new CompositeRuler(0);

        if (showingRanges) {
            createRevisionRulers();
        }

        if (showingAuthors) {
            createAuthorRulers();
        }

        if (numbersRuler == null && addLineNumbers) {
            numbersRuler = new LineNumberRuler();
            numbersRuler
                    .setBackground(getEditorColor(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND));
            numbersRuler
                    .setForeground(getEditorColor(AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND));
            ruler.addDecorator(4, numbersRuler);
        }
        return ruler;
    }

}
