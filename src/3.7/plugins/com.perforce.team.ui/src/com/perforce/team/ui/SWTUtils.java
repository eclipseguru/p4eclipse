package com.perforce.team.ui;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PreferenceLinkArea;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

/**
 * A collection of factory methods for creating common SWT controls
 */
public class SWTUtils {
    public static final String KEY_DECORATOR = "Control.decoration"; //$NON-NLS-1$
    public static final FieldDecoration INFO_INDICATOR = FieldDecorationRegistry.getDefault().
            getFieldDecoration(FieldDecorationRegistry.DEC_INFORMATION);

        public static final FieldDecoration ERROR_INDICATOR = FieldDecorationRegistry.getDefault().
            getFieldDecoration(FieldDecorationRegistry.DEC_ERROR);
        
        public static final FieldDecoration WARNING_INDICATOR = FieldDecorationRegistry.getDefault().
            getFieldDecoration(FieldDecorationRegistry.DEC_WARNING);
    
	/** */
	public static final int MARGINS_DEFAULT = -1;

	/** */
	public static final int MARGINS_NONE = 0;

	/** */
	public static final int MARGINS_DIALOG = 1;

	/**
	 * Creates a preference link which will open in the specified container
	 *
	 * @param container
	 * @param parent
	 * @param pageId
	 * @param text
	 *
	 * @return the created link
	 */
	public static PreferenceLinkArea createPreferenceLink(
			IWorkbenchPreferenceContainer container, Composite parent,
			String pageId, String text) {
		final PreferenceLinkArea area = new PreferenceLinkArea(parent,
				SWT.NONE, pageId, text, container, null);
		return area;
	}

	/**
	 * Creates a grid data with the specified metrics
	 *
	 * @param width
	 * @param height
	 * @param hFill
	 * @param vFill
	 *
	 * @return the created grid data
	 */
	public static GridData createGridData(int width, int height, boolean hFill,
			boolean vFill) {
		return createGridData(width, height, hFill ? SWT.FILL : SWT.BEGINNING,
				vFill ? SWT.FILL : SWT.CENTER, hFill, vFill);
	}

	/**
	 * Creates a grid data with the specified metrics
	 *
	 * @param width
	 * @param height
	 * @param hAlign
	 * @param vAlign
	 * @param hGrab
	 * @param vGrab
	 *
	 * @return the created grid data
	 */
	public static GridData createGridData(int width, int height, int hAlign,
			int vAlign, boolean hGrab, boolean vGrab) {
		final GridData gd = new GridData(hAlign, vAlign, hGrab, vGrab);
		gd.widthHint = width;
		gd.heightHint = height;
		return gd;
	}

	/**
	 * Creates a horizontal grid data with the default metrics
	 *
	 * @return the created grid data
	 */
	public static GridData createHFillGridData() {
		return createHFillGridData(1);
	}

	/**
	 * Creates a horizontal grid data with the specified span
	 *
	 * @param span
	 *
	 * @return the created grid data
	 */
	public static GridData createHFillGridData(int span) {
		final GridData gd = createGridData(0, SWT.DEFAULT, SWT.FILL,
				SWT.CENTER, true, false);
		gd.horizontalSpan = span;
		return gd;
	}

	/**
	 * Creates a horizontal fill composite with the specified margins
	 *
	 * @param parent
	 * @param margins
	 *
	 * @return the created composite
	 */
	public static Composite createHFillComposite(Composite parent, int margins) {
		return createHFillComposite(parent, margins, 1);
	}

	/**
	 * Creates a horizontal fill composite with the specified margins and
	 * columns
	 *
	 * @param parent
	 * @param margins
	 * @param columns
	 *
	 * @return the created composite
	 */
	public static Composite createHFillComposite(Composite parent, int margins,
			int columns) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());
		composite.setLayoutData(createHFillGridData());
		composite.setLayout(createGridLayout(columns,
				new PixelConverter(parent), margins));
		return composite;
	}

	/**
	 * Creates a horizontal/vertical fill composite with the specified margins
	 *
	 * @param parent
	 * @param margins
	 *
	 * @return the created composite
	 */
	public static Composite createHVFillComposite(Composite parent, int margins) {
		return createHVFillComposite(parent, margins, 1);
	}

	/**
	 * Creates a horizontal/vertical fill composite with the specified margins
	 * and columns
	 *
	 * @param parent
	 * @param margins
	 * @param columns
	 *
	 * @return the created composite
	 */
	public static Composite createHVFillComposite(Composite parent,
			int margins, int columns) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());
		composite.setLayoutData(createHVFillGridData());
		composite.setLayout(createGridLayout(columns,
				new PixelConverter(parent), margins));
		return composite;
	}

	/**
	 * Creates a horizontal fill group with the specified text and margins
	 *
	 * @param parent
	 * @param text
	 * @param margins
	 * @return the created group
	 */
	public static Group createHFillGroup(Composite parent, String text,
			int margins) {
		return createHFillGroup(parent, text, margins, 1);
	}

	/**
	 * Creates a horizontal fill group with the specified text, margins and rows
	 *
	 * @param parent
	 * @param text
	 * @param margins
	 * @param rows
	 *
	 * @return the created group
	 */
	public static Group createHFillGroup(Composite parent, String text,
			int margins, int rows) {
		final Group group = new Group(parent, SWT.NONE);
		group.setFont(parent.getFont());
		group.setLayoutData(createHFillGridData());
		if (text != null)
			group.setText(text);
		group.setLayout(createGridLayout(rows, new PixelConverter(parent),
				margins));
		return group;
	}

	/**
	 * Creates a horizontal/vertical fill group with the specified text and
	 * margins
	 *
	 * @param parent
	 * @param text
	 * @param margins
	 *
	 * @return the created group
	 */
	public static Group createHVFillGroup(Composite parent, String text,
			int margins) {
		return createHVFillGroup(parent, text, margins, 1);
	}

	/**
	 * Creates a horizontal/vertical fill group with the specified text, margins
	 * and rows
	 *
	 * @param parent
	 * @param text
	 * @param margins
	 * @param rows
	 *
	 * @return the created group
	 */
	public static Group createHVFillGroup(Composite parent, String text,
			int margins, int rows) {
		final Group group = new Group(parent, SWT.NONE);
		group.setFont(parent.getFont());
		group.setLayoutData(createHVFillGridData());
		if (text != null)
			group.setText(text);
		group.setLayout(createGridLayout(rows, new PixelConverter(parent),
				margins));
		return group;
	}

	/**
	 * Creates a horizontal/vertical fill grid data with the default metrics
	 *
	 * @return the created grid data
	 */
	public static GridData createHVFillGridData() {
		return createHVFillGridData(1);
	}

	/**
	 * Creates a horizontal/vertical fill grid data with the specified span
	 *
	 * @param span
	 *
	 * @return the created grid data
	 */
	public static GridData createHVFillGridData(int span) {
		final GridData gd = createGridData(0, 0, true, true);
		gd.horizontalSpan = span;
		return gd;
	}

	/**
	 * Creates a grid layout with the specified number of columns and the
	 * standard spacings.
	 *
	 * @param numColumns
	 *            the number of columns
	 * @param converter
	 *            the pixel converter
	 * @param margins
	 *            one of <code>MARGINS_DEFAULT</code>, <code>MARGINS_NONE</code>
	 *            or <code>MARGINS_DIALOG</code>.
	 *
	 * @return the created grid layout
	 */
	public static GridLayout createGridLayout(int numColumns,
			PixelConverter converter, int margins) {
		Assert.isTrue(margins == MARGINS_DEFAULT || margins == MARGINS_NONE
				|| margins == MARGINS_DIALOG);

		final GridLayout layout = new GridLayout(numColumns, false);
		layout.horizontalSpacing = converter
				.convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing = converter
				.convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);

		switch (margins) {
		case MARGINS_NONE:
			layout.marginLeft = layout.marginRight = 0;
			layout.marginTop = layout.marginBottom = 0;
			break;
		case MARGINS_DIALOG:
			layout.marginLeft = layout.marginRight = converter
					.convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
			layout.marginTop = layout.marginBottom = converter
					.convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
			break;
		case MARGINS_DEFAULT:
			layout.marginLeft = layout.marginRight = layout.marginWidth;
			layout.marginTop = layout.marginBottom = layout.marginHeight;
		}
		layout.marginWidth = layout.marginHeight = 0;
		return layout;
	}

	/**
	 * Creates a label with the specified message
	 *
	 * @param parent
	 * @param message
	 *
	 * @return the created label
	 */
	public static Label createLabel(Composite parent, String message) {
		return createLabel(parent, message, 1);
	}

	/**
	 * Creates a label with the specified message and span
	 *
	 * @param parent
	 * @param message
	 * @param span
	 *
	 * @return the created label
	 */
	public static Label createLabel(Composite parent, String message, int span) {
		final Label label = new Label(parent, SWT.WRAP);
		if (message != null)
			label.setText(message);
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).span(span,1).applyTo(label);
		return label;
	}

	public static Button createButton(Composite parent, String message, int style, int span){
        final Button button = new Button(parent, style);
        button.setText(message);
        GridData gd = createGridData(SWT.DEFAULT, SWT.DEFAULT, SWT.FILL,
                SWT.TOP, false, false);
        gd.horizontalSpan = span;
        button.setLayoutData(gd);
        return button;
	}
	
	/**
	 * Creates a check box with the specified message
	 *
	 * @param parent
	 * @param message
	 *
	 * @return the created check box
	 */
	public static Button createCheckBox(Composite parent, String message) {
		return createCheckBox(parent, message, 1);
	}

	/**
	 * Creates a check box with the specified message and span
	 *
	 * @param parent
	 * @param message
	 * @param span
	 *
	 * @return the created check box
	 */
	public static Button createCheckBox(Composite parent, String message,
			int span) {
		final Button button = new Button(parent, SWT.CHECK);
		button.setText(message);
		button.setLayoutData(createHFillGridData(span));
		return button;
	}

	/**
	 * Creates a radio button with the specified message
	 *
	 * @param parent
	 * @param message
	 *
	 * @return the created radio button
	 */
	public static Button createRadioButton(Composite parent, String message) {
		return createRadioButton(parent, message, 1);
	}

	/**
	 * Creates a radio button with the specified message and span
	 *
	 * @param parent
	 * @param message
	 * @param span
	 *
	 * @return the created radio button
	 */
	public static Button createRadioButton(Composite parent, String message,
			int span) {
		final Button button = new Button(parent, SWT.RADIO);
		button.setText(message);
		button.setLayoutData(createHFillGridData(span));
		return button;
	}

	/**
	 * Creates a text control
	 *
	 * @param parent
	 *
	 * @return the created text control
	 */
	public static Text createText(Composite parent) {
		return createText(parent, 1);
	}

	public static Text createText(Composite parent, int span, int style) {
        final Text text = new Text(parent, style);
        text.setLayoutData(createHFillGridData(span));
        return text;
    }

	/**
	 * Creates a text control with the specified span
	 *
	 * @param parent
	 * @param span
	 *
	 * @return the created text control
	 */
	public static Text createText(Composite parent, int span) {
	    return createText(parent,span,SWT.SINGLE | SWT.BORDER);
	}
	
    public static Text createTextArea(Composite parent, int hspan, int vspan, int heightInChars) {
        return createTextArea(parent, hspan, vspan, heightInChars, SWT.MULTI | SWT.BORDER  |SWT.V_SCROLL | SWT.H_SCROLL);
    }
    
    public static Text createTextArea(Composite parent, int hspan, int vspan, int heightInChars, int style) {
        final Text text = new Text(parent, style);
        final GridData gd = createGridData(0,0, SWT.FILL,
                SWT.FILL, true, true);
        gd.horizontalSpan = hspan;
        gd.verticalSpan = vspan;

        gd.minimumHeight = new PixelConverter(parent)
        .convertHeightInCharsToPixels(heightInChars);
        
        text.setLayoutData(gd);
        return text;
    }	

	/**
	 * Creates a place holder with the specified height and span
	 *
	 * @param parent
	 * @param heightInChars
	 * @param span
	 *
	 * @return the created place holder
	 */
	public static Control createPlaceholder(Composite parent,
			int heightInChars, int span) {
		Assert.isTrue(heightInChars > 0);
		final Control placeHolder = new Composite(parent, SWT.NONE);
		final GridData gd = new GridData(SWT.BEGINNING, SWT.TOP, false, false);
		gd.heightHint = new PixelConverter(parent)
				.convertHeightInCharsToPixels(heightInChars);
		gd.horizontalSpan = span;
		placeHolder.setLayoutData(gd);
		return placeHolder;
	}

	/**
	 * Creates a place holder with the specified height
	 *
	 * @param parent
	 * @param heightInChars
	 * @return the created place holder
	 */
	public static Control createPlaceholder(Composite parent, int heightInChars) {
		return createPlaceholder(parent, heightInChars, 1);
	}

	/**
	 * Creates a pixel converter
	 *
	 * @param control
	 *
	 * @return the created pixel converter
	 */
	public static PixelConverter createDialogPixelConverter(Control control) {
		Dialog.applyDialogFont(control);
		return new PixelConverter(control);
	}

	/**
	 * Calculates the size of the specified controls, using the specified
	 * converter
	 *
	 * @param converter
	 * @param controls
	 *
	 * @return the size of the control(s)
	 */
	public static int calculateControlSize(PixelConverter converter,
			Control[] controls) {
		return calculateControlSize(converter, controls, 0, controls.length - 1);
	}

	/**
	 * Calculates the size of the specified subset of controls, using the
	 * specified converter
	 *
	 * @param converter
	 * @param controls
	 * @param start
	 * @param end
	 *
	 * @return the created control
	 */
	public static int calculateControlSize(PixelConverter converter,
			Control[] controls, int start, int end) {
		int minimum = converter
				.convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		for (int i = start; i <= end; i++) {
			final int length = controls[i]
					.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
			if (minimum < length)
				minimum = length;
		}
		return minimum;
	}

	/**
	 * Equalizes the specified controls using the specified converter
	 *
	 * @param converter
	 * @param controls
	 */
	public static void equalizeControls(PixelConverter converter,
			Control[] controls) {
		equalizeControls(converter, controls, 0, controls.length - 1);
	}

	/**
	 * Equalizes the specified subset of controls using the specified converter
	 *
	 * @param converter
	 * @param controls
	 * @param start
	 * @param end
	 */
	public static void equalizeControls(PixelConverter converter,
			Control[] controls, int start, int end) {
		final int size = calculateControlSize(converter, controls, start, end);
		for (int i = start; i <= end; i++) {
			final Control button = controls[i];
			if (button.getLayoutData() instanceof GridData) {
				((GridData) button.getLayoutData()).widthHint = size;
			}
		}
	}

	/**
	 * Gets the width of the longest string in <code>strings</code>, using the
	 * specified pixel converter
	 *
	 * @param converter
	 * @param strings
	 *
	 * @return the width of the longest string
	 */
	public static int getWidthInCharsForLongest(PixelConverter converter,
			String[] strings) {
		int minimum = 0;
		for (int i = 0; i < strings.length; i++) {
			final int length = converter.convertWidthInCharsToPixels(strings[i]
					.length());
			if (minimum < length)
				minimum = length;
		}
		return minimum;
	}

	private static class PixelConverter {

		private final FontMetrics fFontMetrics;

		public PixelConverter(Control control) {
			GC gc = new GC(control);
			try {
				gc.setFont(control.getFont());
				fFontMetrics = gc.getFontMetrics();
			} finally {
				gc.dispose();
			}
		}

		public int convertHeightInCharsToPixels(int chars) {
			return Dialog.convertHeightInCharsToPixels(fFontMetrics, chars);
		}

		public int convertHorizontalDLUsToPixels(int dlus) {
			return Dialog.convertHorizontalDLUsToPixels(fFontMetrics, dlus);
		}

		public int convertVerticalDLUsToPixels(int dlus) {
			return Dialog.convertVerticalDLUsToPixels(fFontMetrics, dlus);
		}

		public int convertWidthInCharsToPixels(int chars) {
			return Dialog.convertWidthInCharsToPixels(fFontMetrics, chars);
		}
	}

	/**
	 * Decorates the image with the given overlay
	 * @param image
	 * @param overlay
	 * @return the decorated image
	 */
	public static Image getDecoratedImage(final Image image, final ImageDescriptor overlay) {
		Image decoratedImage;
		// create one
		CompositeImageDescriptor cd = new CompositeImageDescriptor() {

			@Override
			protected Point getSize() {
				Rectangle bounds = image.getBounds();
				return new Point(bounds.width, bounds.height);
			}

			@Override
			protected void drawCompositeImage(int width, int height) {
				drawImage(image.getImageData(), 0, 0);
				drawImage(overlay.getImageData(), 0, 0);

			}
		};
		decoratedImage = cd.createImage();

		return decoratedImage;
	}

    public static Control decorate(final Control control, int position){
        ControlDecoration decoration = new ControlDecoration(control, position);
        decoration.setShowHover(true);
        decoration.setMarginWidth(2);
        control.setData(KEY_DECORATOR, decoration);
        control.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                Object dec = e.widget.getData(KEY_DECORATOR);
                if(dec instanceof ControlDecoration){
                    ((ControlDecoration)dec).dispose();
                }
            }
        });
        return control;
    }

    private static ControlDecoration getDecoration(Control control) {
        return (ControlDecoration) control.getData(KEY_DECORATOR);
    }
    
    public static boolean isDisposed(Control control){
        if(control==null || control.isDisposed())
            return true;
        if(control instanceof Composite){
            for(Control child: ((Composite) control).getChildren()){
                if(isDisposed(child))
                    return true;
            }
        }
        return false;
    }
    
    public static boolean updateDecoration(Control control, IStatus status){
        if(isDisposed(control))
            return false;
        
        ControlDecoration dec=getDecoration(control);
        if(dec==null)
            return false;
        
        dec.setDescriptionText(status.getMessage());
        adjustLayout(control, status);
        switch(status.getSeverity()){
          case IStatus.OK:
            dec.hide();
            return true;
          case IStatus.INFO:
            dec.setImage(INFO_INDICATOR.getImage());
            break;
          case IStatus.WARNING:
            dec.setImage(WARNING_INDICATOR.getImage());
            break;
          case IStatus.ERROR:
            dec.setImage(ERROR_INDICATOR.getImage());
            break;
        }
        dec.show();
//      adjustLayout(control, status); // not needed on lucid, but need on hardy
        return true;
    }

    public static boolean adjustLayout(Control control, IStatus status) {
        if(control==null)
            return false;
        
        ControlDecoration dec = getDecoration(control);
        if(dec==null)
            return false;
        Object layoutData = control.getLayoutData();
        if (layoutData instanceof GridData) {
            int indent = FieldDecorationRegistry.getDefault()
            .getMaximumDecorationWidth()+2;

//            if(status.isOK())
//                indent=-1;
                    
            ((GridData) layoutData).horizontalIndent = indent;
            if(control.getParent()!=null){
                control.getParent().layout(new Control[]{control});
                return true;
            }
        }
        return false;
    }

    public static void addContentListener(Control[] controls, Runnable runnable) {
        for(Control control : controls){
            addContentListener(control, runnable);
        }
    }
    
    public static void addContentListener(Control control, final Runnable runnable) {
        if (control instanceof Button || control instanceof Combo || control instanceof CCombo
                || control instanceof Spinner) {
            control.addListener(SWT.Selection, new Listener() {
                public void handleEvent(Event event) {
                    runnable.run();
                }
            });
        } else if (control instanceof Text
                || (control instanceof Spinner && !isReadonlyStyle(control))
                || (control instanceof Combo && !isReadonlyStyle(control))
                || (control instanceof CCombo && !isReadonlyStyle(control))) {
            control.addListener(SWT.Modify, new Listener() {
                public void handleEvent(Event event) {
                    runnable.run();
                }
            });
        } else if (control instanceof Composite) {
            for (Control c : ((Composite)control).getChildren()) {
                addContentListener(c, runnable);
            }
        }
    }

    /**
     * @param control
     * @return true if control style is SWT.READ_ONLY
     */
    public static boolean isReadonlyStyle(Control control)
    {
      return isReadonlyStyle(control.getStyle());
    }

    /**
     * @param style
     * @return true if style is SWT.READ_ONLY
     */
    public static boolean isReadonlyStyle(int style)
    {
      return ((style & SWT.READ_ONLY)!=0);
    }

    public static Object getSingleSelectedObject(Viewer viewer){
        ISelection selection = viewer.getSelection();
        if(selection instanceof IStructuredSelection){
            return ((IStructuredSelection) selection).getFirstElement();
        }
        return null;
    }

	public static <T> ComboViewer createEnumCombo(Composite parent,
			Class<T> enm) {
		T[] input= enm.getEnumConstants();
		
		ComboViewer viewer = new ComboViewer(parent);
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setInput(input);
		GridDataFactory.fillDefaults().grab(false, false).applyTo(viewer.getControl());
		return viewer;
	}
	
	public static void select(StructuredViewer viewer, Object obj){
		viewer.setSelection(new StructuredSelection(obj));
	}
	
	/**
	 * This is a helper method created to get the location on screen of a
	 * composite. It does not take into account multiple monitors.
	 * 
	 * @param cmpTarget
	 *            The composite whose location on screen is required
	 * @return The location of the composite on screen.
	 */
	public static Point getScreenLocation( Composite cmpTarget )
	{
		Point ptScreen = new Point( 0, 0 );
		try
		{
			Composite cTmp = cmpTarget;
			while ( !( cTmp instanceof Shell ) )
			{
				ptScreen.x += cTmp.getLocation( ).x;
				ptScreen.y += cTmp.getLocation( ).y;
				cTmp = cTmp.getParent( );
			}
		}
		catch ( Exception e )
		{
			MessageDialog.openError(cmpTarget.getShell(), "Error", e.getLocalizedMessage() );
		}
		return cmpTarget.getShell( ).toDisplay( ptScreen );
	}

	/**
	 * This is a helper method created to center a shell on the screen. It
	 * centers the shell on the primary monitor in a multi-monitor
	 * configuration.
	 * 
	 * @param shell
	 *            The shell to be centered on screen
	 */
	public static void centerOnScreen( Shell shell )
	{
		if ( Display.getCurrent( ).getActiveShell( ) == null )
		{
			centerOnMonitor( Display.getCurrent( ).getPrimaryMonitor( ), shell );
		}
		else
		{
			centerOnMonitor( Display.getCurrent( )
					.getActiveShell( )
					.getMonitor( ), shell );
		}
	}

	/**
	 * Center shell on specified monitor.
	 * 
	 * @param monitor specified monitor will display shell.
	 * @param shell the shell to be centered on monitor.
	 */
	public static void centerOnMonitor( Monitor monitor, Shell shell) {
		
		Rectangle clientArea = monitor.getClientArea();
		shell.setLocation( clientArea.x + ( clientArea.width / 2 ) - ( shell.getSize( ).x / 2 ),
				clientArea.y + ( clientArea.height / 2 ) - ( shell.getSize( ).y / 2 ) );
	}

}
