/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Resource.Type;
import com.perforce.team.ui.mergequest.wizards.mapping.Messages;
import com.perforce.team.ui.resource.ResourceBrowserDialog;

import java.text.MessageFormat;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.fieldassist.ContentAssistCommandAdapter;
import org.eclipse.ui.keys.IBindingService;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public final class BranchGraphUtils {

    /**
     * Configure browse button
     * 
     * @param connection
     * @param browse
     * @param field
     */
    public static void configureDepotBrowseButton(
            final IP4Connection connection, final ToolItem browse,
            final Control field) {
        if (connection == null || browse == null || field == null) {
            return;
        }
        browse.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                ResourceBrowserDialog dialog = new ResourceBrowserDialog(field
                        .getShell(), connection.members());
                if (dialog.open() == ResourceBrowserDialog.OK) {
                    IP4Resource resource = dialog.getSelectedResource();
                    if (resource != null) {
                        String actionPath = resource.getActionPath(Type.REMOTE);
                        if (actionPath != null) {
                            if (field instanceof Combo) {
                                ((Combo) field).setText(actionPath);
                            } else if (field instanceof Text) {
                                ((Text) field).setText(actionPath);
                            }
                        }
                    }
                }
            }

        });
    }

    /**
     * Add content assist decoration
     * 
     * @param control
     */
    public static void addContentAssistDecoration(Control control) {
        ControlDecoration decoration = new ControlDecoration(control, SWT.TOP
                | SWT.LEFT);

        FieldDecorationRegistry registry = FieldDecorationRegistry.getDefault();
        FieldDecoration fieldDecoration = registry
                .getFieldDecoration(FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
        if (fieldDecoration != null) {
            decoration.setDescriptionText(fieldDecoration.getDescription());
            decoration.setImage(fieldDecoration.getImage());
            IBindingService bindingService = (IBindingService) PlatformUI
                    .getWorkbench().getService(IBindingService.class);
            if (bindingService != null) {
                String commandValue = bindingService
                        .getBestActiveBindingFormattedFor(ContentAssistCommandAdapter.CONTENT_PROPOSAL_COMMAND);
                if (commandValue != null && commandValue.length() > 0) {
                    decoration.setDescriptionText(MessageFormat.format(
                            Messages.MappingArea_ContentAssistAvailable,
                            commandValue));
                }
            }
        }
    }

}
