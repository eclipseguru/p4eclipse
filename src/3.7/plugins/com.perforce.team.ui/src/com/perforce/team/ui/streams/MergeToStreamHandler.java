package com.perforce.team.ui.streams;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;

import com.perforce.team.core.p4java.IP4Stream;
import com.perforce.team.ui.p4java.actions.MergeToStreamAction;


public class MergeToStreamHandler extends AbstractHandler implements IHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
        if(selection instanceof StructuredSelection){
            Object element = ((StructuredSelection) selection).getFirstElement();
            if(element instanceof IP4Stream){
            	MergeToStreamAction action = new MergeToStreamAction();
            	action.mergeToStream((IP4Stream) element);
            }
        }
        return null;
    }
}
