package com.perforce.team.ui;

import java.text.MessageFormat;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.perforce.p4java.exception.TrustException;
import com.perforce.team.ui.dialogs.DialogUtils;

public class P4TrustDialog extends MessageDialog {

    static final int CANCEL = 0;
    static final int CONNECT = 1;
    String msgRed = null;
    String msgBold = null;
    String trustLabel = null;

    // We need to trick Dialog so it doesn't reorder buttons. It wants to
    // move the default button to the right (on mac at least) but we want
    // the default Cancel on the left and non-default Connect on the right.
    // So, initially we lie about what the default button is, then after
    // Dialog does its button rearranging in initializeBounds(), we can
    // safely set the true default button.

    P4TrustDialog(Shell shell, String serverHostPort, String serverIpPort,
            String fingerprint, TrustException.Type type) {
        super(shell, Messages.P4TrustDialog_TrustError, null,
                "", MessageDialog.WARNING, options(), CONNECT); //$NON-NLS-2$
        if (type == TrustException.Type.NEW_CONNECTION) {
            msgBold = MessageFormat.format(
                    Messages.P4TrustDialog_NewConnectionMessage,
                    serverHostPort, serverIpPort);
            trustLabel = Messages.P4TrustDialog_TrustNewFingerprint;
        } else if (type == TrustException.Type.NEW_KEY) {
            msgRed = Messages.P4TrustDialog_InterceptWarning;
            msgBold = MessageFormat.format(
                    Messages.P4TrustDialog_NewKeyMessage, serverHostPort,
                    serverIpPort);
            trustLabel = Messages.P4TrustDialog_TrustChangedFingerprint;

        }
        message = MessageFormat.format(
                Messages.P4TrustDialog_FingerprintMessage, fingerprint);
    }

    static String[] options() {
        return new String[] { IDialogConstants.CANCEL_LABEL,
                Messages.P4TrustDialog_Connect };
    }

    /*
     * In order to get part of the message in bold, and to get the checkbox
     * aligned with it, need to override createMessageArea and create a
     * composite with various parts in it in place of the standard Label.
     * 
     * Parts of this are borrowed from the implementation in
     * IconAndMessageDialog.
     * 
     * @see
     * org.eclipse.jface.dialogs.IconAndMessageDialog#createMessageArea(org.
     * eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createMessageArea(Composite composite) {

        // create image, just like in IconAndMessageDialog
        Image image = getImage();
        if (image != null) {
            imageLabel = new Label(composite, SWT.NULL);
            image.setBackground(imageLabel.getBackground());
            imageLabel.setImage(image);
            imageLabel.getAccessible().addAccessibleListener(
                    new AccessibleAdapter() {

                        public void getName(AccessibleEvent event) {
                            event.result = JFaceResources.getString("warning"); //$NON-NLS-1$
                        }
                    });
            GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.BEGINNING)
                    .applyTo(imageLabel);
        }

        // create area to the right of the icon for message and checkbox
        Composite rightArea = new Composite(composite, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        rightArea.setLayout(layout);
        GridDataFactory
                .fillDefaults()
                .align(SWT.FILL, SWT.BEGINNING)
                .grab(true, false)
                .hint(convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH),
                        SWT.DEFAULT).applyTo(rightArea);

        // RED warning with fingerprint changes
        if (msgRed != null) {
            Label redMessageLabel = new Label(rightArea, getMessageLabelStyle());
            redMessageLabel.setText(msgRed);
            redMessageLabel.setForeground(new Color(getShell().getDisplay(),
                    255, 0, 0));
            GridDataFactory
                    .fillDefaults()
                    .align(SWT.FILL, SWT.BEGINNING)
                    .grab(true, false)
                    .hint(convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH),
                            SWT.DEFAULT).applyTo(redMessageLabel);
        }

        // first part of message needs to be shown in bold
        Label boldMessageLabel = new Label(rightArea, getMessageLabelStyle());
        boldMessageLabel.setText(msgBold);
        FontData[] fd = boldMessageLabel.getFont().getFontData();
        fd[0].setStyle(SWT.BOLD);
        boldMessageLabel.setFont(new Font(getShell().getDisplay(), fd[0]));
        GridDataFactory
                .fillDefaults()
                .align(SWT.FILL, SWT.BEGINNING)
                .grab(true, false)
                .hint(convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH),
                        SWT.DEFAULT).applyTo(boldMessageLabel);

        // normal message, just like in IconAndMessageDialog
        if (message != null) {
            messageLabel = new Label(rightArea, getMessageLabelStyle());
            messageLabel.setText(message);
            GridDataFactory
                    .fillDefaults()
                    .align(SWT.FILL, SWT.BEGINNING)
                    .grab(true, false)
                    .hint(convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH),
                            SWT.DEFAULT).applyTo(messageLabel);
        }

        // Create trust checkbox
        // Connect button is disabled until trust checkbox is checked
        final Button trustButton = DialogUtils.createCheck(rightArea,
                trustLabel);
        trustButton.setSelection(false);
        trustButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                getButton(CONNECT).setEnabled(trustButton.getSelection());
            }
        });
        GridDataFactory
                .fillDefaults()
                .align(SWT.FILL, SWT.BEGINNING)
                .grab(true, false)
                .hint(convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH),
                        SWT.DEFAULT).applyTo(trustButton);

        return composite;
    }

    @Override
    protected void initializeBounds() {
        super.initializeBounds();
        // now that Dialog has done its button order checking we can safely
        // set the true default button
        getShell().setDefaultButton(getButton(CANCEL));
        // this is also a convenient time to disable the connect button
        getButton(CONNECT).setEnabled(false);
    }
}
