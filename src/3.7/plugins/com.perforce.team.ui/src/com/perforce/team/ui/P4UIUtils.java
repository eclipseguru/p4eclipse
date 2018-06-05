/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui;

import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckable;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.progress.UIJob;

import com.perforce.p4java.server.PerforceCharsets;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public final class P4UIUtils {

    /**
     * Default text editor id
     */
    public static final String DEFAULT_TEXT_EDITOR = "org.eclipse.ui.DefaultTextEditor"; //$NON-NLS-1$

    /**
     * Empty string
     */
    public static final String EMPTY = ""; //$NON-NLS-1$

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
            "MM/dd/yy hh:mm aaa"); //$NON-NLS-1$

    /**
     * VIEWER_HEIGHT_HINT - 5 rows with 16 pixel icons
     */
    public static final int VIEWER_HEIGHT_HINT = 80;

    private P4UIUtils() {
        // Does nothing
    }

    /**
     * Add standard perforce menu groups
     * 
     * @param manager
     */
    public static void addStandardPerforceMenus(IMenuManager manager) {
        if (manager != null) {
            manager.add(new Separator("perforce.group1")); //$NON-NLS-1$
            manager.add(new Separator("perforce.group2")); //$NON-NLS-1$
            manager.add(new Separator("perforce.group3")); //$NON-NLS-1$
            manager.add(new Separator("perforce.group4")); //$NON-NLS-1$
            manager.add(new Separator("perforce.group5")); //$NON-NLS-1$
            manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        }
    }

    /**
     * Create combo adapter that runs the specified runnable when a selection
     * event occurs.
     * 
     * This methods takes into account special Mac OS X "cocoa" issues with
     * selection events.
     * 
     * @param runnable
     * @return - selection listener
     */
    public static SelectionListener createComboSelectionListener(
            final Runnable runnable) {
        SelectionListener listener = null;
        if (runnable != null) {
            if (!P4CoreUtils.isCocoa()) {
                listener = new SelectionAdapter() {

                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        runnable.run();
                    }

                    @Override
                    public void widgetDefaultSelected(SelectionEvent e) {
                        widgetSelected(e);
                    }

                };
            } else {
                // Fix for job036670, special handle combo selection events on
                // cocoa since they send events when programmatic access is done
                // so changing the combo during a selection event can cause
                // infinite event loops.
                listener = new SelectionAdapter() {

                    private boolean ignore = false;
                    private boolean enter = false;
                    private String lastText = ""; //$NON-NLS-1$

                    private void runRunnable() {
                        try {
                            ignore = true;
                            runnable.run();
                        } finally {
                            ignore = false;
                        }
                    }

                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        if (e != null) {
                            if (!ignore) {
                                if (e.widget instanceof Combo) {
                                    Combo c = ((Combo) e.widget);
                                    // Fix for job038185, only process selection
                                    // events for enabled combo boxes only, this
                                    // fixes the issue where a combo box is
                                    // cleared while disabled and the event
                                    // fires causing the runnable to be run
                                    // without an actual change occurring
                                    if (c.isEnabled()) {
                                        if (enter) {
                                            runRunnable();
                                        } else {
                                            String text = c.getText();
                                            if (!lastText.equals(text)) {
                                                runRunnable();
                                                lastText = text;
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            runnable.run();
                        }
                    }

                    @Override
                    public void widgetDefaultSelected(SelectionEvent e) {
                        try {
                            enter = true;
                            widgetSelected(e);
                        } finally {
                            enter = false;
                        }
                    }

                };
            }
        }
        return listener;
    }

    /**
     * Create source viewer configuration
     * 
     * @param target
     * @return - text source viewer configuration
     */
    public static TextSourceViewerConfiguration createSourceViewerConfiguration(
            final Object target) {
        final IAdaptable adaptable = new IAdaptable() {

            public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
                return Platform.getAdapterManager().getAdapter(target, adapter);
            }
        };
        return createSourceViewerConfiguration(adaptable);
    }

    /**
     * Create a text source viewer configuration
     * 
     * @param adaptable
     * @return - text source viewer configuration
     */
    public static TextSourceViewerConfiguration createSourceViewerConfiguration(
            final IAdaptable adaptable) {
        return new TextSourceViewerConfiguration(EditorsUI.getPreferenceStore()) {

            @SuppressWarnings("rawtypes")
            @Override
            protected Map getHyperlinkDetectorTargets(ISourceViewer sourceViewer) {
                @SuppressWarnings("unchecked")
                Map<String, IAdaptable> targets = super.getHyperlinkDetectorTargets(sourceViewer);
                if (targets != null) {
                    targets.put("org.eclipse.ui.DefaultTextEditor", adaptable); //$NON-NLS-1$
                }
                return targets;

            }
        };
    }

    /**
     * Keep the checked state synced between move file pairs.
     * 
     * @param viewer
     * @param element
     * @param checked
     */
    public static void syncMovedFile(final ICheckable viewer, Object element,
            boolean checked) {
        if (viewer != null && element != null) {
            IP4File file = P4CoreUtils.convert(element, IP4File.class);
            if (file != null) {
                String moved = file.getMovedFile();
                if (moved != null) {
                    IP4File pair = file.getConnection().getFile(moved);
                    if (pair != null) {
                        viewer.setChecked(pair, checked);
                    }
                }
            }
        }
    }

    /**
     * Track moved files and keep the pairs checked. This should be called
     * before any other {@link ICheckStateListener} listeners are added since
     * the tracking listener calls
     * {@link CheckboxTableViewer#setChecked(Object, boolean)} which does not
     * fire a second event and so if this listener is first in line then
     * downstream listeners will take into account any additional
     * checking/unchecking the this listener will do to keep move/add and
     * move/delete pairs together.
     * 
     * This method should also be called after the initial checked elements have
     * been set.
     * 
     * @param viewer
     */
    public static void trackMovedFiles(final ICheckable viewer) {
        if (viewer != null) {
            Object[] checkedElements = null;
            if (viewer instanceof CheckboxTreeViewer) {
                checkedElements = ((CheckboxTreeViewer) viewer)
                        .getCheckedElements();
            } else if (viewer instanceof CheckboxTableViewer) {
                checkedElements = ((CheckboxTableViewer) viewer)
                        .getCheckedElements();
            }
            if (checkedElements != null) {
                for (Object element : checkedElements) {
                    syncMovedFile(viewer, element, true);
                }
            }
            viewer.addCheckStateListener(new ICheckStateListener() {

                public void checkStateChanged(CheckStateChangedEvent event) {
                    syncMovedFile(viewer, event.getElement(),
                            event.getChecked());
                }
            });
        }
    }

    /**
     * Computes the height in number of pixels needed to show numRows of the
     * specified font.
     * 
     * @param font
     * @param numRows
     * @return - height in pixels
     */
    public static int computePixelHeight(Font font, int numRows) {
        int height = SWT.DEFAULT;
        GC gc = new GC(getDisplay());
        if (font != null) {
            gc.setFont(font);
        }
        try {
            FontMetrics metrics = gc.getFontMetrics();
            height = metrics.getHeight() * numRows;
        } finally {
            gc.dispose();
        }
        return height;
    }

    /**
     * Computes the width in number of pixels need to show numColumns of the
     * specified font.
     * 
     * @param font
     * @param numColumns
     * @return width in pixels
     */
    public static int computePixelWidth(Font font, int numColumns) {
        int height = SWT.DEFAULT;
        GC gc = new GC(getDisplay());
        if (font != null) {
            gc.setFont(font);
        }
        try {
            FontMetrics metrics = gc.getFontMetrics();
            height = metrics.getAverageCharWidth() * numColumns;
        } finally {
            gc.dispose();
        }
        return height;
    }

    /**
     * Get image descriptor for a file name
     * 
     * @param filename
     * @return - editor image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String filename) {
        return PlatformUI.getWorkbench().getEditorRegistry()
                .getImageDescriptor(filename);
    }

    /**
     * Get formatted date of a timestamp
     * 
     * @param timestamp
     * @return - string date
     */
    public static String getDateText(long timestamp) {
        return DateFormat.getDateTimeInstance().format(new Date(timestamp));
    }

    /**
     * Gets the display of the workbench
     * 
     * @return - display
     */
    public static Display getDisplay() {
        return PlatformUI.getWorkbench().getDisplay();
    }

    /**
     * Gets the active shell from the workbench's display
     * 
     * @return - shell
     */
    public static Shell getShell() {
        return getDisplay().getActiveShell();
    }

    /**
     * Get the dialog shell from the active workbench window
     * 
     * @return - shell
     */
    public static Shell getDialogShell() {
        IWorkbenchWindow win = PerforceUIPlugin.getActiveWorkbenchWindow();
        if (win != null) {
            return win.getShell();
        }
        return null;
    }

    /**
     * Get displayable list of p4charsets with the corresponding java charset
     * name
     * 
     * @return - array of readable mappings between p4charsets and java charsets
     */
    public static String[] getDisplayCharsets() {
        List<String> charsets = new ArrayList<String>();
        for (String p4Charset : PerforceCharsets.getKnownCharsets()) {
            String display = getDisplayCharset(p4Charset);
            if (display != null) {
                charsets.add(display);
            }
        }
        return charsets.toArray(new String[0]);
    }

    /**
     * Get p4 charset from a display charset
     * 
     * @param displayCharset
     * @return - p4 charset
     */
    public static String getP4Charset(String displayCharset) {
        String p4Charset = null;
        if (displayCharset != null) {
            int index = displayCharset.indexOf(" ("); //$NON-NLS-1$
            if (index != -1) {
                p4Charset = displayCharset.substring(0, index);
            } else {
                p4Charset = displayCharset;
            }
        }
        return p4Charset;
    }

    /**
     * Get display charset from a p4 charset
     * 
     * @param p4Charset
     * @return - p4 charset
     */
    public static String getDisplayCharset(String p4Charset) {
        String displayCharset = null;
        if (p4Charset != null) {
            String javaCharset = PerforceCharsets.getJavaCharsetName(p4Charset);
            if (javaCharset != null) {
                StringBuilder builder = new StringBuilder();
                builder.append(p4Charset);
                if (!p4Charset.equals(javaCharset)) {
                    builder.append(" ("); //$NON-NLS-1$
                    builder.append(javaCharset);
                    builder.append(')');
                }
                displayCharset = builder.toString();
            }
        }
        return displayCharset;
    }

    /**
     * Opens a preference page
     * 
     * @param pageId
     * @param block
     * @return - preference dialog
     */
    public static PreferenceDialog openPreferencePage(String pageId,
            boolean block) {
        PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(
                getShell(), pageId, new String[] { pageId }, null);
        if (dialog != null) {
            dialog.setBlockOnOpen(block);
            dialog.open();
        }
        return dialog;
    }

    /**
     * Opens a preference page
     * 
     * @param pageId
     */
    public static void openPreferencePage(String pageId) {
        openPreferencePage(pageId, true);
    }

    /**
     * Opens a property page
     * 
     * @param pageId
     * @param element
     * @param block
     * @return - preference dialog
     */
    public static PreferenceDialog openPropertyPage(String pageId,
            IAdaptable element, boolean block) {
        return openPropertyPage(pageId, element, block, true);
    }

    /**
     * Opens a property page
     * 
     * @param pageId
     * @param element
     * @param block
     * @param filter
     * @return - preference dialog
     */
    public static PreferenceDialog openPropertyPage(String pageId,
            IAdaptable element, boolean block, boolean filter) {
        String[] filterIds = null;
        if (filter) {
            filterIds = new String[] { pageId };
        }
        PreferenceDialog dialog = PreferencesUtil.createPropertyDialogOn(
                getShell(), element, pageId, filterIds, null);
        if (dialog != null) {
            dialog.setBlockOnOpen(block);
            dialog.open();
        }
        return dialog;
    }

    /**
     * Opens a property page
     * 
     * @param pageId
     * @param element
     */
    public static void openPropertyPage(String pageId, IAdaptable element) {
        openPropertyPage(pageId, element, true);
    }

    /**
     * Get editor descriptor for specified name and content from editor
     * registry. Will fallback to using {@link #getDefaultTextDescriptor()} if
     * the registry returns null for the name and content type.
     * 
     * @param name
     * @param type
     * @return - editor descriptor
     */
    public static IEditorDescriptor getDescriptor(String name, IContentType type) {
        IEditorRegistry registry = PlatformUI.getWorkbench()
                .getEditorRegistry();
        IEditorDescriptor descriptor = registry.getDefaultEditor(name, type);
        if (descriptor == null && registry.isSystemInPlaceEditorAvailable(name)) {
            descriptor = registry
                    .findEditor(IEditorRegistry.SYSTEM_INPLACE_EDITOR_ID);
        }
        if (descriptor == null
                && registry.isSystemExternalEditorAvailable(name)) {
            descriptor = registry
                    .findEditor(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
        }
        if (descriptor == null) {
            descriptor = getDefaultTextDescriptor();
        }
        return descriptor;
    }

    /**
     * Get default text editor descriptor
     * 
     * @return - editor descriptor
     */
    public static IEditorDescriptor getDefaultTextDescriptor() {
        return PlatformUI.getWorkbench().getEditorRegistry()
                .findEditor(DEFAULT_TEXT_EDITOR);
    }

    /**
     * Get content type for storage object
     * 
     * @param storage
     * @return - content type of null if finding failed
     */
    public static IContentType getContentType(IStorage storage) {
        IContentType type = null;
        if (storage != null) {
            try {
                type = getStorageType(storage);
            } catch (IOException e) {
                PerforceProviderPlugin.logError(e);
            } catch (CoreException e) {
                PerforceProviderPlugin.logError(e);
            }
        }
        return type;
    }

    private static void showOpenError(final Exception exception) {
        P4ConnectionManager.getManager().openError(P4UIUtils.getShell(),
                Messages.P4UIUtils_ErrorOpeningEditor, exception.getMessage());
    }

    private static void scheduleShowOpenError(final Exception exception) {
        UIJob errorJob = new UIJob(Messages.P4UIUtils_DisplayingPerforceError) {

            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                showOpenError(exception);
                return Status.OK_STATUS;
            }
        };
        errorJob.schedule();
    }

    private static IEditorPart openEditor(IEditorInput input,
            IEditorDescriptor descriptor) throws PartInitException {
        IWorkbenchPage page = PerforceUIPlugin.getActivePage();
        return IDE.openEditor(page, input, descriptor.getId());
    }

    private static IContentType getStorageType(IStorage storage)
            throws IOException, CoreException {
        return Platform.getContentTypeManager().findContentTypeFor(
                storage.getContents(), storage.getName());
    }

    private static void forceOpenEditor(IEditorInput input,
            IEditorDescriptor descriptor, IPartListener callback) {
        if (descriptor != null) {
            try {
                IEditorPart part = openEditor(input, descriptor);
                if (part != null && callback != null) {
                    callback.partActivated(part);
                }
            } catch (PartInitException e) {
                boolean show = true;
                if (!DEFAULT_TEXT_EDITOR.equals(descriptor.getId())) {
                    descriptor = getDefaultTextDescriptor();
                    if (descriptor != null) {
                        try {
                            IEditorPart part = openEditor(input, descriptor);
                            if (part != null && callback != null) {
                                callback.partActivated(part);
                            }
                            show = false;
                        } catch (PartInitException e1) {
                            show = true;
                        }
                    }
                }
                if (show) {
                    showOpenError(e);
                }
            }
        }
    }

    private static boolean openExisting(final IStorageEditorInput input,
            final IPartListener listener) {
        final boolean opened[] = new boolean[] { false };
        PerforceUIPlugin.syncExec(new Runnable() {

            public void run() {
                IWorkbenchPage page = PerforceUIPlugin.getActivePage();
                if (page != null) {
                    IEditorPart existing = page.findEditor(input);
                    if (existing != null) {
                        opened[0] = true;
                        page.bringToTop(existing);
                        if (listener != null) {
                            listener.partActivated(existing);
                        }
                    }
                }
            }
        });
        return opened[0];
    }

    /**
     * Open an editor around the specified depot file editor input
     * 
     * @param input
     * @param listener
     */
    public static void openEditor(final IStorageEditorInput input,
            final IPartListener listener) {
        if (input != null) {
            if (openExisting(input, listener)) {
                return;
            }
            IP4Runnable runnable = new P4Runnable() {

                @Override
                public void run(IProgressMonitor monitor) {
                    Exception exception = null;
                    try {
                        final IStorage storage = input.getStorage();
                        if (storage != null) {
                            final IContentType type = getStorageType(storage);
                            UIJob openJob = new UIJob(
                                    Messages.P4UIUtils_OpeningEditor) {

                                @Override
                                public IStatus runInUIThread(
                                        IProgressMonitor monitor) {
                                    IEditorDescriptor descriptor = getDescriptor(
                                            storage.getName(), type);
                                    forceOpenEditor(input, descriptor, listener);
                                    return Status.OK_STATUS;
                                }
                            };
                            openJob.schedule();
                        }
                    } catch (CoreException e) {
                        exception = e;
                    } catch (IOException e) {
                        exception = e;
                    }
                    if (exception != null) {
                        scheduleShowOpenError(exception);
                    }
                }

                @Override
                public String getTitle() {
                    return MessageFormat.format(
                            Messages.P4UIUtils_FindingContentType,
                            input.getName());
                }

            };
            P4Runner.schedule(runnable);
        }
    }

    /**
     * Open an editor around the specified depot file editor input
     * 
     * @param input
     */
    public static void openEditor(final IStorageEditorInput input) {
        openEditor(input, (IPartListener) null);
    }

    /**
     * Format a time as a string
     * 
     * @param time
     * @return - string
     */
    public static String formatDate(long time) {
        Date date = new Date(time);
        return DateFormat.getDateTimeInstance(DateFormat.FULL,
                DateFormat.DEFAULT).format(date);
    }

    /**
     * Format a date to the standard label string format. This method returns
     * the empty string if the specified date is null.
     * 
     * @param date
     * @return - string
     */
    public static String formatLabelDate(Date date) {
        if (date != null) {
            synchronized (DATE_FORMAT) {
                return DATE_FORMAT.format(date);
            }
        } else {
            return EMPTY;
        }
    }

    /**
     * Format a date to the standard label string format.
     * 
     * @param date
     * @return - string
     */
    public static String formatLabelDate(long date) {
        return formatLabelDate(new Date(date));
    }

    /**
     * Register a listener to dispose the disposee when the disposer is
     * disposed.
     * 
     * @param disposer
     * @param disposee
     */
    public static void registerDisposal(Widget disposer, final Resource disposee) {
        if (disposee != null && disposer != null) {
            disposer.addDisposeListener(new DisposeListener() {

                public void widgetDisposed(DisposeEvent e) {
                    if (!disposee.isDisposed()) {
                        disposee.dispose();
                    }
                }
            });
        }
    }

    /**
     * Copy the specified contents to the clipboard
     * 
     * @param contents
     */
    public static void copyToClipboard(String contents) {
        if (contents != null && contents.length() > 0) {
            Clipboard clipboard = new Clipboard(getDisplay());
            try {
                clipboard.setContents(new Object[] { contents },
                        new Transfer[] { TextTransfer.getInstance() });
            } finally {
                clipboard.dispose();
            }
        }
    }

    /**
     * Get text from clipboard
     * 
     * @return non-null but possibly empty string
     */
    public static String pasteFromClipboard() {
        String contents = null;
        Clipboard clipboard = new Clipboard(getDisplay());
        try {
            contents = (String) clipboard.getContents(TextTransfer
                    .getInstance());
        } finally {
            clipboard.dispose();
        }
        if (contents == null) {
            contents = ""; //$NON-NLS-1$
        }
        return contents;
    }

    /**
     * Generate a bold version of the specified font
     * 
     * @param display
     * @param base
     * @return - bold font or null if display or base is null
     */
    public static Font generateBoldFont(Device display, Font base) {
        Font bold = null;
        if (display != null && base != null) {
            FontData[] data = base.getFontData();
            FontData[] boldData = new FontData[data.length];
            for (int i = 0; i < data.length; i++) {
                boldData[i] = new FontData(data[i].getName(),
                        data[i].getHeight(), SWT.BOLD | data[i].getStyle());
            }
            bold = new Font(display, boldData);
        }
        return bold;
    }

    /**
     * Is the viewer ok to use
     * 
     * @param viewer
     * @return - true if usable, false if null or disposed
     */
    public static boolean okToUse(Viewer viewer) {
        return viewer != null && okToUse(viewer.getControl());
    }

    /**
     * Is the control ok to use
     * 
     * @param control
     * @return - true if usable, false if null or disposed
     */
    public static boolean okToUse(Widget control) {
        return control != null && !control.isDisposed();
    }

    /**
     * Get image descriptor for element
     * 
     * @param element
     * @return image descriptor or null if none found
     */
    public static ImageDescriptor getImageDescriptor(Object element) {
        ImageDescriptor descriptor = null;
        if (element != null) {
            IWorkbenchAdapter workbenchAdapter = P4CoreUtils.convert(element,
                    IWorkbenchAdapter.class);
            if (workbenchAdapter != null) {
                descriptor = workbenchAdapter.getImageDescriptor(element);
            }
        }
        return descriptor;
    }

    /**
     * Insert white space to the beginning of each altRoot entry if necessary.
     * 
     * @param altRoots input and output.
     */
	public static void addIndentToAltRoots(String[] altRoots) {
		final String WHITESPACE=" "; //$NON-NLS-1$
		for(int i=0;i<altRoots.length;i++){
			if(!altRoots[i].startsWith(WHITESPACE))
				altRoots[i]=WHITESPACE+altRoots[i];
		}
	}
	
	public static String validateName(String name, String object){
	    String error=null;
        if (name.isEmpty()) {
            error=MessageFormat.format(Messages.P4UIUtils_NameMustNotEmpty, object);
        } else if (name.contains("@") || name.contains("#")) { //$NON-NLS-1$ //$NON-NLS-2$
            error=MessageFormat.format(Messages.P4UIUtils_RevisionCharacterNotAllowedInName, object.toLowerCase());
        } else if (name.contains("\"")) { //$NON-NLS-1$
            error=MessageFormat.format(Messages.P4UIUtils_QuoteCharNotAllowedInName, object.toLowerCase());
        } else if (name.contains(" ")) { //$NON-NLS-1$
            error=MessageFormat.format(Messages.P4UIUtils_SpaceNotAllowedInName, object.toLowerCase());
        }
        
        return error;
	}

}
