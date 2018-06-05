package com.perforce.team.ui.streams;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler for streams view refresh command.
 * 
 * @author ali
 *
 */
public class RefreshHandler extends AbstractHandler implements IHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
	    IWorkbenchPart part = HandlerUtil.getActivePart(event);
	    if(part instanceof StreamsView){
	        ((StreamsView)part).refresh(true,true);
	    }
		return null;
	}

}
