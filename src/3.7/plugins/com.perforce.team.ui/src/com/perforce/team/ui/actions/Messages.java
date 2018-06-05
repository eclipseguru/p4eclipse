package com.perforce.team.ui.actions;

import org.eclipse.osgi.util.NLS;

/**
 * @author Sehyo Chang
 * 
 */
public final class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.actions.messages"; //$NON-NLS-1$

    public static String CheckConsistencyAction_TASKTITLE;
    public static String CheckConsistencyAction_FOLDERTASKTITLE;
    public static String CheckConsistencyAction_CheckingConsistency;

    public static String CheckConsistencyAction_CHECKINGFILE;
    public static String CheckConsistencyAction_DIALOGTITLE;
    public static String CheckConsistencyAction_DIALOGMESSAGE;

    public static String CheckConsistencyAction_RefreshingResources;
    public static String DiffDepotAction_DIALOGTITLE;
    public static String DiffDepotAction_M;
    public static String DiffDepotAction_DIFF1;
    public static String DiffDepotAction_DIFF2;
    public static String DiffDepotAction_DIFF3;
    public static String DiffDepotAction_DIFF4;
    public static String DiffDepotAction_DIFFHAVE;
    public static String DiffDepotAction_DIFFHAVETITLE;
    public static String ManualResolveAction_EDITORTITLE;
    public static String ManualResolveAction_FILETITLE;
    public static String ManualResolveAction_DEPOTTITLE;
    public static String ManualResolveAction_BASEFILE;

    public static String ManualResolveAction_NoUnresolvedFilesMessage;

    public static String ManualResolveAction_NoUnresolvedFilesTitle;

    public static String OpenAction_AskOpenProjectMessage;

    public static String OpenAction_P4Eclipse;

    public static String TeamAction_StatusMessage;

	public static String ChangePropertyAction_UpdateConnection;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
