package com.perforce.team.ui.views;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * Console Action
 */
public class ConsoleAction extends Action implements IUpdate {

    private int operationCode = -1;
    private ITextOperationTarget operationTarget;

    /**
     * Create a new console action
     * 
     * @param viewer
     * @param operationCode
     * @param text
     */
    public ConsoleAction(ITextViewer viewer, int operationCode, String text) {
        this.operationCode = operationCode;
        operationTarget = viewer.getTextOperationTarget();
        setText(text);
        update();
    }

    /**
     * @see org.eclipse.ui.texteditor.IUpdate#update()
     */
    public void update() {
        boolean wasEnabled = isEnabled();
        boolean isEnabled = operationTarget != null
                && operationTarget.canDoOperation(operationCode);
        setEnabled(isEnabled);
        if (wasEnabled != isEnabled) {
            firePropertyChange(ENABLED, wasEnabled
                    ? Boolean.TRUE
                    : Boolean.FALSE, isEnabled ? Boolean.TRUE : Boolean.FALSE);
        }
    }

    /**
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        if (operationCode != -1 && operationTarget != null) {
            operationTarget.doOperation(operationCode);
        }
    }
}
