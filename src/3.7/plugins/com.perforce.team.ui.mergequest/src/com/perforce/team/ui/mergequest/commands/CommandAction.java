/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.commands;

import com.perforce.team.core.P4CoreUtils;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.action.Action;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class CommandAction extends Action {

    private Command command;
    private Object stackProvider;

    /**
     * Create new command action
     * 
     * @param command
     * @param stackProvider
     */
    public CommandAction(Command command, Object stackProvider) {
        this.command = command;
        this.stackProvider = stackProvider;
    }

    /**
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        if (command != null && command.canExecute()) {
            CommandStack stack = P4CoreUtils.convert(this.stackProvider,
                    CommandStack.class);
            if (stack != null) {
                stack.execute(command);
            } else {
                command.execute();
            }
        }
    }

}
