package com.perforce.team.ui.streams;

import org.eclipse.core.commands.State;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * Command Helper for commands related to the StreamsView.
 * 
 * @author ali
 *
 */
public class StreamsViewCommandHelper {

	public static final String SHOWSTREAMS_RADIO_CMD="com.perforce.team.ui.commands.showStreams.radio";
	public static final String SHOWSTREAMS_STATE="org.eclipse.ui.commands.radioState";
	public static final String SHOWSTREAMS_STATE_TREE="Tree";
	public static final String SHOWSTREAMS_STATE_LIST="List";
	
	public static boolean showList(){
		State state = getCommandService().getCommand(SHOWSTREAMS_RADIO_CMD).getState(SHOWSTREAMS_STATE);
		return state.getValue().equals(SHOWSTREAMS_STATE_LIST);
	}
	
	public static boolean showTree(){
		State state = getCommandService().getCommand(SHOWSTREAMS_RADIO_CMD).getState(SHOWSTREAMS_STATE);
		return state.getValue().equals(SHOWSTREAMS_STATE_TREE);
	}
	
    public static ICommandService getCommandService(){
        return (ICommandService) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(ICommandService.class);
    }

    public static IHandlerService getHandlerService(){
        return (IHandlerService) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(IHandlerService.class);
    }
    
    public static ISelectionService getSelectionService(){
        return (ISelectionService) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(ISelectionService.class);
    }
    
    
}
