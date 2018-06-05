/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.text.timelapse;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.ui.preferences.IPreferenceHandler;
import com.perforce.team.ui.text.PerforceUiTextPlugin;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class TextPreferenceHandler implements IPreferenceHandler {

    private BooleanFieldEditor foldingEditor;
    private BooleanFieldEditor rangeEditor;
    private BooleanFieldEditor authorEditor;
    private BooleanFieldEditor agingEditor;
    private ColorFieldEditor mostRecentEditor;
    private ColorFieldEditor leastRecentEditor;
    private ColorFieldEditor tickColorEditor;
    private BooleanFieldEditor linkEditor;
    private BooleanFieldEditor buildEditor;
    private BooleanFieldEditor filterEditor;
    private ComboFieldEditor whitespaceEditor;

    /**
     * @see com.perforce.team.ui.preferences.IPreferenceHandler#addControls(org.eclipse.swt.widgets.Composite)
     */
    public void addControls(Composite parent) {
        IPreferenceStore store = PerforceUiTextPlugin.getDefault()
                .getPreferenceStore();
        authorEditor = new BooleanFieldEditor(TextTimeLapseEditor.SHOW_AUTHORS,
                Messages.TextPreferenceHandler_DisplayAuthorInfo, parent);
        authorEditor.setPreferenceStore(store);
        authorEditor.load();

        rangeEditor = new BooleanFieldEditor(TextTimeLapseEditor.SHOW_RANGES,
                Messages.TextPreferenceHandler_DisplayRevisionInfo, parent);
        rangeEditor.setPreferenceStore(store);
        rangeEditor.load();

        agingEditor = new BooleanFieldEditor(TextTimeLapseEditor.SHOW_AGING,
                Messages.TextPreferenceHandler_DisplayTextAging, parent);
        agingEditor.setPreferenceStore(store);
        agingEditor.load();

        Composite whitespaceArea = new Composite(parent, SWT.NONE);
        GridLayout waLayout = new GridLayout(1, true);
        waLayout.marginWidth = 0;
        waLayout.marginHeight = 0;
        whitespaceArea.setLayout(waLayout);
        whitespaceArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                false));

        whitespaceEditor = new ComboFieldEditor(
                TextTimeLapseEditor.WHITESPACE_TYPE,
                Messages.TextPreferenceHandler_WhitespaceOptions,
                new String[][] {
                        {
                                Messages.TextPreferenceHandler_IgnoreLineEndingAndAllWS,
                                IP4File.WhitespaceIgnoreType.ALL.toString() },
                        {
                                Messages.TextPreferenceHandler_RecognizeLineEndingAndWS,
                                "" }, //$NON-NLS-1$
                        {
                                Messages.TextPreferenceHandler_IgnoreLineEndingAndWS,
                                IP4File.WhitespaceIgnoreType.WHITESPACE
                                        .toString() },
                        {
                                Messages.TextPreferenceHandler_IgnoreLineEnding,
                                IP4File.WhitespaceIgnoreType.LINE_ENDINGS
                                        .toString() } }, whitespaceArea);
        whitespaceEditor.setPreferenceStore(store);
        whitespaceEditor.load();

        Composite colorArea = new Composite(parent, SWT.NONE);
        GridLayout caLayout = new GridLayout(1, true);
        caLayout.marginWidth = 0;
        caLayout.marginHeight = 0;
        colorArea.setLayout(caLayout);
        colorArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        leastRecentEditor = new ColorFieldEditor(
                TextTimeLapseEditor.LEAST_RECENT_COLOR,
                Messages.TextPreferenceHandler_LeastRecentTextBGColor,
                colorArea);
        leastRecentEditor.setPreferenceStore(store);
        leastRecentEditor.load();

        mostRecentEditor = new ColorFieldEditor(
                TextTimeLapseEditor.MOST_RECENT_COLOR,
                Messages.TextPreferenceHandler_10, colorArea);
        mostRecentEditor.setPreferenceStore(store);
        mostRecentEditor.load();

        tickColorEditor = new ColorFieldEditor(
                NodeModelTimeLapseEditor.TICK_CHANGE_COLOR,
                Messages.TextPreferenceHandler_NodeChangeFGColor, colorArea);
        tickColorEditor.setPreferenceStore(store);
        tickColorEditor.load();

        linkEditor = new BooleanFieldEditor(
                NodeTimeLapseEditor.LINK_EDITOR,
                Messages.TextPreferenceHandler_LinkEditorWithOutlineViewSelection,
                parent);
        linkEditor.setPreferenceStore(store);
        linkEditor.load();

        foldingEditor = new BooleanFieldEditor(
                NodeTimeLapseEditor.SHOW_FOLDING,
                Messages.TextPreferenceHandler_DisplayCodeFolding, parent);
        foldingEditor.setPreferenceStore(store);
        foldingEditor.load();

        buildEditor = new BooleanFieldEditor(NodeModelTimeLapseEditor.BUILD,
                Messages.TextPreferenceHandler_GenerateMethodHistory, parent);
        buildEditor.setPreferenceStore(store);
        buildEditor.load();

        filterEditor = new BooleanFieldEditor(
                NodeModelTimeLapseEditor.FILTER,
                Messages.TextPreferenceHandler_OnlyShowRevsWhereSelectedNodeChanges,
                parent);
        filterEditor.setPreferenceStore(store);
        filterEditor.load();

    }

    /**
     * @see com.perforce.team.ui.preferences.IPreferenceHandler#defaults()
     */
    public void defaults() {
        authorEditor.loadDefault();
        rangeEditor.loadDefault();
        agingEditor.loadDefault();
        leastRecentEditor.loadDefault();
        mostRecentEditor.loadDefault();
        tickColorEditor.loadDefault();
        foldingEditor.loadDefault();
        buildEditor.loadDefault();
        filterEditor.loadDefault();
        linkEditor.loadDefault();
        whitespaceEditor.loadDefault();
    }

    /**
     * @see com.perforce.team.ui.preferences.IPreferenceHandler#save()
     */
    public void save() {
        authorEditor.store();
        rangeEditor.store();
        agingEditor.store();
        leastRecentEditor.store();
        mostRecentEditor.store();
        tickColorEditor.store();
        foldingEditor.store();
        buildEditor.store();
        filterEditor.store();
        linkEditor.store();
        whitespaceEditor.store();
    }

}
