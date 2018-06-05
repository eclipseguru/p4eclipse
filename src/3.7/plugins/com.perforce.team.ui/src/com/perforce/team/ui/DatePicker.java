/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui;

import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class DatePicker {

    class DateDialog extends PopupDialog {

        private Point location;
        private int year;
        private int month;
        private int day;
        private DateTime dateTime;

        /**
         * @param parent
         * @param location
         */
        public DateDialog(Shell parent, Point location) {
            super(parent, SWT.NONE, true, false, false, false, false, "", ""); //$NON-NLS-1$ //$NON-NLS-2$
            this.location = location;
        }

        @Override
        protected void adjustBounds() {
            getShell().setLocation(location);
        }

        /**
         * @see org.eclipse.jface.dialogs.PopupDialog#close()
         */
        @Override
        public boolean close() {
            if (getReturnCode() == OK) {
                if (day > 0 && month > 0 && year > 0) {
                    StringBuilder date = new StringBuilder();
                    date.append(year);
                    date.append('/');
                    date.append(month);
                    date.append('/');
                    date.append(day);
                    if (combo != null) {
                        combo.setText(date.toString());
                    } else if (text != null) {
                        text.setText(date.toString());
                    }
                }
            }
            return super.close();
        }

        protected void updateValues() {
            year = dateTime.getYear();
            month = dateTime.getMonth() + 1;
            day = dateTime.getDay();
        }

        protected void setInitialValue() {
            String value = ""; //$NON-NLS-1$
            if (combo != null) {
                value = combo.getText().trim();
            } else if (text != null) {
                value = text.getText().trim();
            }
            if (value.length() > 0) {
                String[] segments = value.split("/"); //$NON-NLS-1$
                if (segments.length == 3) {
                    try {
                        int parsedYear = Integer.parseInt(segments[0]);
                        int parsedMonth = Integer.parseInt(segments[1]);
                        int parsedDay = Integer.parseInt(segments[2]);
                        if (parsedYear > 0 && parsedMonth > 0 && parsedDay > 0) {
                            dateTime.setYear(parsedYear);
                            dateTime.setMonth(parsedMonth - 1);
                            dateTime.setDay(parsedDay);
                        }
                    } catch (NumberFormatException e) {
                        // Ignore
                    }
                }
            }
        }

        /**
         * @see org.eclipse.jface.dialogs.PopupDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
         */
        @Override
        protected Control createDialogArea(Composite parent) {
            Composite composite = (Composite) super.createDialogArea(parent);
            dateTime = new DateTime(composite, SWT.CALENDAR);
            setInitialValue();
            dateTime.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            dateTime.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    updateValues();
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    updateValues();
                    close();

                }
            });
            Link link = new Link(composite, SWT.NONE);
            link.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
            link.setText(Messages.DatePicker_CloseLink);
            link.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    updateValues();
                    close();
                }

            });
            return composite;
        }
    }

    private ToolBar toolbar;
    private ToolItem item;
    private Combo combo;
    private Text text;

    /**
     * Set the combo widget to fill with the date selection
     * 
     * @param combo
     */
    public void setCombo(Combo combo) {
        this.combo = combo;
    }

    /**
     * Set the text widget to fill with the date selection
     * 
     * @param text
     */
    public void setText(Text text) {
        this.text = text;
    }

    /**
     * Set the date picker as visible or invisible
     * 
     * @param visible
     */
    public void setVisible(boolean visible) {
        this.toolbar.setVisible(visible);
    }

    /**
     * Create a date picker
     * 
     * @param parent
     */
    public void createControl(Composite parent) {
        toolbar = new ToolBar(parent, SWT.FLAT);
        item = new ToolItem(toolbar, SWT.PUSH);
        Image showImage = PerforceUIPlugin.getDescriptor(
                IPerforceUIConstants.IMG_CALENDAR).createImage();
        P4UIUtils.registerDisposal(item, showImage);
        item.setImage(showImage);
        item.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Rectangle bounds = item.getBounds();
                Point location = toolbar.toDisplay(bounds.x, bounds.y
                        + bounds.height);
                DateDialog dialog = new DateDialog(toolbar.getShell(), location);
                dialog.open();
            }

        });
    }
}
