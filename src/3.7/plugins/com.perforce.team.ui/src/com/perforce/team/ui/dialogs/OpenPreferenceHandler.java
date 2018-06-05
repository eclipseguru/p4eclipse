package com.perforce.team.ui.dialogs;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;

import com.perforce.team.ui.P4UIUtils;

/**
 * General actions for invoking the certain preference page.
 * 
 * @author ali
 *
 */
public class OpenPreferenceHandler extends AbstractHandler implements IHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        String pageId = event.getParameter("com.perforce.team.ui.commands.openpreference.pageid"); 
        P4UIUtils.openPreferencePage(pageId);
        return null; 
    }

}
