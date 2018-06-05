package com.perforce.team.ui.editor;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.editor.messages"; //$NON-NLS-1$
    public static String CompareUtils_DiffTwoRevs;
    public static String CompareUtils_LocalFile;
    public static String CompareUtils_LocalRev;
    public static String CompareUtils_OpeningCompareEditor;
    public static String CompareUtils_RemoteRev;
    public static String CompareUtils_WorkspaceFile;
    public static String P4QuickDiffProvider_RefreshingDocument;
    public static String P4QuickDiffProvider_RefreshingDocumentFor;
    public static String Proposal_EmptyProposal;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
