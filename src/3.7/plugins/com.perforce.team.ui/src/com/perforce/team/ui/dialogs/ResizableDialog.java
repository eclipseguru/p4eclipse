package com.perforce.team.ui.dialogs;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import java.util.ResourceBundle;

import org.eclipse.compare.CompareUI;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Base class for resizable Dialogs with persistent window bounds.
 */
public abstract class ResizableDialog extends Dialog {

    // dialog store id constants
    private final static String DIALOG_BOUNDS_KEY = "ResizableDialogBounds"; //$NON-NLS-1$
    private static final String X = "x"; //$NON-NLS-1$
    private static final String Y = "y"; //$NON-NLS-1$
    private static final String WIDTH = "width"; //$NON-NLS-1$
    private static final String HEIGHT = "height"; //$NON-NLS-1$

    protected ResourceBundle fBundle;
    private Rectangle fNewBounds;
    private IDialogSettings fSettings;
    private String fContextId;

    public ResizableDialog(Shell parent, ResourceBundle bundle) {
        super(parent);
        setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);

        fBundle = bundle;

        fSettings = CompareUI.getPlugin().getDialogSettings();
    }

    public void setHelpContextId(String contextId) {
        fContextId = contextId;
    }

    /*
     * @see org.eclipse.jface.window.Window#configureShell(Shell)
     */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        if (fContextId != null)
            PlatformUI.getWorkbench().getHelpSystem()
                    .setHelp(newShell, fContextId);
    }

    @Override
    protected Point getInitialSize() {
		Point result = super.getInitialSize();

        final Shell s = getShell();
        if (s != null) {
            s.addControlListener(new ControlListener() {

                public void controlMoved(ControlEvent arg0) {
                    fNewBounds = s.getBounds();
                }

                public void controlResized(ControlEvent arg0) {
                    fNewBounds = s.getBounds();
                }
            });
        }

        IDialogSettings bounds = fSettings.getSection(DIALOG_BOUNDS_KEY);
        if (bounds == null) {
            if (fBundle != null) {
                Shell shell = getParentShell();
                if (shell != null) {
                    Point parentSize = shell.getSize();
                    if (result.x <= 0)
                        result.x = parentSize.x - 300;
                    if (result.y <= 0)
                        result.y = parentSize.y - 200;
                }
            } else {
                Shell shell = getParentShell();
                if (shell != null) {
                    Point parentSize = shell.getSize();
                    result.x = parentSize.x - 100;
                    result.y = parentSize.y - 100;
                }
            }
            if (result.x < 700)
                result.x = 700;
            if (result.y < 500)
                result.y = 500;
        } else {
            try {
                result.x = bounds.getInt(WIDTH);
            } catch (NumberFormatException e) {
                result.x = 700;
            }
            try {
                result.y = bounds.getInt(HEIGHT);
            } catch (NumberFormatException e) {
                result.y = 500;
            }
        }

        return result;
    }

    @Override
    protected Point getInitialLocation(Point initialSize) {
        Point loc = super.getInitialLocation(initialSize);

        IDialogSettings bounds = fSettings.getSection(DIALOG_BOUNDS_KEY);
        if (bounds != null) {
            try {
                loc.x = bounds.getInt(X);
            } catch (NumberFormatException e) {
            }
            try {
                loc.y = bounds.getInt(Y);
            } catch (NumberFormatException e) {
            }
        }
        return loc;
    }

    @Override
    public boolean close() {
        boolean closed = super.close();
        if (closed && fNewBounds != null)
            saveBounds(fNewBounds);
        return closed;
    }

    private void saveBounds(Rectangle bounds) {
        IDialogSettings dialogBounds = fSettings.getSection(DIALOG_BOUNDS_KEY);
        if (dialogBounds == null) {
            dialogBounds = new DialogSettings(DIALOG_BOUNDS_KEY);
            fSettings.addSection(dialogBounds);
        }
        dialogBounds.put(X, bounds.x);
        dialogBounds.put(Y, bounds.y);
        dialogBounds.put(WIDTH, bounds.width);
        dialogBounds.put(HEIGHT, bounds.height);
    }
}
