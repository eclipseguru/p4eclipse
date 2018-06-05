package com.perforce.team.ui.streams;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.RadioState;

/**
 * Handler for radion button show list/tree.
 *  
 * @author ali
 *
 */
public class ShowStreamsRadioHandler extends AbstractHandler implements IHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
	    if(HandlerUtil.matchesRadioState(event))
	        return null; // we are already in the updated state - do nothing
	 
	    String currentState = event.getParameter(RadioState.PARAMETER_ID);
	    
	    // and finally update the current state
	    HandlerUtil.updateRadioState(event.getCommand(), currentState);
	    
	    IWorkbenchPart part = HandlerUtil.getActivePart(event);
	    if(part instanceof StreamsView){
	        ((StreamsView)part).refresh(false,false);
	    }
	    
		return null;
	}

}
