package com.perforce.team.ui.dialogs;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

public class DialogUtils {

    private DialogUtils() {
    }

    public static Text createTextField(Composite parent, int width, boolean fill) {
        Text text = new Text(parent, SWT.SINGLE | SWT.BORDER);
        GridData data;
        if (fill) {
            data = new GridData(GridData.FILL_HORIZONTAL);
        } else {
            data = new GridData();
        }
        data.verticalAlignment = GridData.CENTER;
        data.grabExcessVerticalSpace = false;
        data.widthHint = width;
        text.setLayoutData(data);
        return text;
    }

    public static Button createButton(Composite parent, String text, int flags) {
        Button button = new Button(parent, SWT.PUSH);
        button.setText(text);
        button.setLayoutData(new GridData(flags));
        return button;
    }

    public static Text createTextField(Composite parent) {
        return createTextField(parent, IDialogConstants.ENTRY_FIELD_WIDTH, true);
    }

    public static Label createBlank(Composite parent) {
        return createLabel(parent, ""); //$NON-NLS-1$
    }

    public static Label createLabel(Composite parent, String text) {
        Label label = new Label(parent, SWT.LEFT);
        label.setText(text);
        GridData data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        label.setLayoutData(data);
        return label;
    }

    public static Label createLabel(Composite parent, String text, int style) {
        Label label = new Label(parent, style);
        label.setText(text);
        GridData data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        label.setLayoutData(data);
        return label;
    }

    public static Combo createCombo(Composite parent, String[] items,
            boolean readonly) {
        int flags = SWT.DROP_DOWN;
        if (readonly) {
            flags |= SWT.READ_ONLY;
        }
        Combo combo = new Combo(parent, flags);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        combo.setLayoutData(data);
        for (int i = 0; i < items.length; i++) {
            combo.add(items[i]);
        }
        return combo;
    }

    public static Button createRadio(Composite parent, String text) {
        return createRadio(parent, SWT.NONE, text);
    }

    public static Button createRadio(Composite parent, int style, String text) {
        Button radio = new Button(parent, SWT.RADIO | SWT.NO_FOCUS);
        radio.setText(text);
        radio.setLayoutData(new GridData());
        return radio;
    }

    public static Button createCheck(Composite parent, String text) {
        Button check = new Button(parent, SWT.CHECK);
        check.setText(text);
        check.setLayoutData(new GridData());
        return check;
    }

    public static List createList(Composite parent, int vspan, int height) {
        List list = new List(parent, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
        GridData data = new GridData();
        data.verticalSpan = vspan;
        data.heightHint = height;
        list.setLayoutData(data);
        return list;
    }

    public static List createList(Composite parent, int vspan, int height,
            int width, boolean multi) {
        List list = new List(parent, SWT.BORDER | SWT.V_SCROLL);
        GridData data = new GridData();
        data.verticalSpan = vspan;
        data.heightHint = height;
        data.widthHint = width;
        list.setLayoutData(data);
        return list;
    }

    public static Group createGroup(Composite parent, String title, int numcols) {
        Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
        group.setText(title);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout layout = new GridLayout();
        layout.numColumns = numcols;
        group.setLayout(layout);
        return group;
    }

    public static Label createSeparator(Composite composite, int colspan) {
        Label separator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.horizontalSpan = colspan;
        gridData.grabExcessHorizontalSpace = true;
        separator.setLayoutData(gridData);
        return separator;
    }

    public static Composite createComposite(Composite parent) {
        return createComposite(parent, 1, 0);
    }

    public static Composite createComposite(Composite parent, int numcols) {
        return createComposite(parent, numcols, 0);
    }

    public static Composite createComposite(Composite parent, int numcols,
            int flags) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(flags));

        GridLayout layout = new GridLayout();
        layout.numColumns = numcols;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);
        return composite;
    }

    public static Text createTextEditor(Composite parent) {
        Text text = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.WRAP
                | SWT.V_SCROLL);
        GridData data = new GridData(GridData.FILL_BOTH
                | GridData.GRAB_VERTICAL | GridData.GRAB_HORIZONTAL);
        data.heightHint = 150;
        text.setLayoutData(data);
        return text;
    }

    public static TextViewer createTextViewer(Composite parent) {
        TextViewer viewer = new TextViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL
                | SWT.MULTI | SWT.BORDER | SWT.READ_ONLY);
        GridData data = new GridData(GridData.FILL_BOTH
                | GridData.GRAB_VERTICAL | GridData.GRAB_HORIZONTAL);
        data.heightHint = 150;
        viewer.getControl().setLayoutData(data);
        return viewer;
    }

    public static TextViewer createTextViewer(Composite parent, int style) {
        TextViewer viewer = new TextViewer(parent, style);
        GridData data = new GridData(GridData.FILL_BOTH
                | GridData.GRAB_VERTICAL | GridData.GRAB_HORIZONTAL);
        data.heightHint = 150;
        viewer.getControl().setLayoutData(data);
        return viewer;
    }

    public static SashForm createSash(Composite parent) {
        SashForm sash = new SashForm(parent, SWT.VERTICAL);
        GridData data = new GridData(GridData.FILL_BOTH
                | GridData.GRAB_VERTICAL | GridData.GRAB_HORIZONTAL);
        data.heightHint = 300;
        sash.setLayoutData(data);
        return sash;
    }

    public static Composite createTitledArea(Composite parent, int flags) {
        Composite composite = createComposite(parent, 1, flags);
        ((GridLayout) composite.getLayout()).verticalSpacing = 2;
        return composite;
    }

    /**
     * Create a selectable "label" which is a read-only text field
     * 
     * @param parent
     * @param defaultText
     * @return - read-only text field
     */
    public static Text createSelectableLabel(Composite parent,
            String defaultText) {
        return createSelectableLabel(parent, null, defaultText);
    }

    /**
     * Create a selectable "label" which is a read-only text field
     * 
     * @param parent
     * @param data
     * @return - read-only text field
     */
    public static Text createSelectableLabel(Composite parent, GridData data) {
        return createSelectableLabel(parent, data, null);
    }

    /**
     * Create a selectable "label" which is a read-only text field
     * 
     * @param parent
     * @param data
     * @param defaultText
     * @return - read-only text field
     */
    public static Text createSelectableLabel(Composite parent, GridData data,
            String defaultText) {
        Text text = new Text(parent, SWT.READ_ONLY);
        if (data == null) {
            data = new GridData();
            data.grabExcessHorizontalSpace = true;
            data.horizontalAlignment = GridData.FILL;
        }
        text.setLayoutData(data);
        if (defaultText != null) {
            text.setText(defaultText);
        }
        text.setBackground(text.getDisplay().getSystemColor(
                SWT.COLOR_WIDGET_BACKGROUND));
        return text;
    }
}
