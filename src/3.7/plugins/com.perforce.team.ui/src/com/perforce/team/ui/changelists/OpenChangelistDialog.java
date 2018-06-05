/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.changelists;

import com.perforce.p4java.core.IChangelist;
import com.perforce.team.ui.dialogs.OpenResourceDialog;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class OpenChangelistDialog extends OpenResourceDialog {

    private int id = IChangelist.UNKNOWN;

    /**
     * @param parent
     * @param title
     */
    public OpenChangelistDialog(Shell parent, String title) {
        super(parent, title, Messages.OpenChangelistDialog_ChangelistNumber);
    }

    /**
     * @see com.perforce.team.ui.dialogs.OpenResourceDialog#getListener()
     */
    @Override
    protected ModifyListener getListener() {
        return new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                String number = ((Text) e.widget).getText().trim();
                try {
                    int clId = Integer.parseInt(number);
                    if (clId > 0) {
                        id = clId;
                        setErrorMessage(null);
                    } else {
                        setErrorMessage(Messages.OpenChangelistDialog_MustEnterPositiveInteger);
                    }
                } catch (NumberFormatException nfe) {
                    setErrorMessage(Messages.OpenChangelistDialog_MustEnterValidInteger);
                }
            }
        };
    }

    /**
     * Get entered id
     * 
     * @return - changelist id
     */
    public int getId() {
        return id;
    }

}
