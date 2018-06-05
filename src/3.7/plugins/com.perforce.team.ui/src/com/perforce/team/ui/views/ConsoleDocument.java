package com.perforce.team.ui.views;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.GapTextStore;

import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;

/**
 * The document for the console view
 */
public class ConsoleDocument extends AbstractDocument {

    // The different line types
    /**
     * COMMAND
     */
    public static final int COMMAND = 0;

    /**
     * MESSAGE
     */
    public static final int MESSAGE = 1;

    /**
     * ERROR
     */
    public static final int ERROR = 2;

    /**
     * DELIMITER
     */
    public static final int DELIMITER = 3;

    // Holds the type of each line in the document
    private List<Integer> lineTypes = new ArrayList<Integer>();

    /**
     * Constructor
     */
    public ConsoleDocument() {
        setTextStore(new GapTextStore(512, 1024, 0f));
        setLineTracker(new DefaultLineTracker());
        completeInitialization();
    }

    /**
     * Get the type of a specific line
     * 
     * @param offset
     * @return - line type
     */
    public int getLineType(int offset) {
        try {
            int line = getLineOfOffset(offset);
            if (line < lineTypes.size())
                return lineTypes.get(line);
        } catch (BadLocationException e) {
        }
        return 0;
    }

    /**
     * Clear all lines
     */
    public void clear() {
        lineTypes.clear();
        set(""); //$NON-NLS-1$
    }

    /**
     * Append a new line to the document
     * 
     * @param type
     * @param line
     */
    public void appendLine(int type, String line) {
        if (type == COMMAND && lineTypes.size() != 0) {
            appendLine(DELIMITER, ""); //$NON-NLS-1$
        }
        lineTypes.add(type);
        try {
            replace(getLength(), 0, " " + line + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
            keepPreviousCommands();
        } catch (BadLocationException e) {
        }
    }

    /**
     * Purge all but the output of the last N commands from the document
     */
    private void keepPreviousCommands() throws BadLocationException {
        int number = PerforceUIPlugin.getPlugin().getPreferenceStore()
                .getInt(IPerforceUIConstants.PREF_CONSOLE_COMMANDS);
        // Ignore negative/zero values
        if (number <= 0) {
            return;
        }
        // Get the index of the line and character to keep
        List<Integer> commandLines = getCommandLines();
        if (commandLines.size() <= number) {
            return;
        }
        int lineIndex = commandLines.get(commandLines.size() - number);
        int characterIndex = getLineOffset(lineIndex);

        // Keep everything from the character to the end
        set(get(characterIndex, getLength() - characterIndex));

        // Adjust the line types: remove lined before lineIndex
        for(Iterator<Integer> it=lineTypes.iterator();it.hasNext() && lineIndex>0;){
        	it.next();
        	it.remove();
        	lineIndex--;
        }
    }

    /**
     * Return the indicies of the lines that contain command strings
     */
    private List<Integer> getCommandLines() {
        List<Integer> commandLineList = new ArrayList<Integer>();
        for (int i = 0; i < lineTypes.size(); i++) {
            if (lineTypes.get(i) == COMMAND) {
                commandLineList.add(new Integer(i));
            }
        }
        return commandLineList;
    }
}
