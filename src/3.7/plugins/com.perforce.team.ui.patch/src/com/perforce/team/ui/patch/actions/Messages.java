/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.patch.actions;

import org.eclipse.osgi.util.NLS;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.patch.actions.messages"; //$NON-NLS-1$

    /**
     * CreatePatchAction_MultipleErrorsMessage
     */
    public static String CreatePatchAction_MultipleErrorsMessage;

    /**
     * CreatePatchAction_NotSupported_Description
     */
    public static String CreatePatchAction_NotSupported_Description;

    /**
     * CreatePatchAction_NotSupported_Title
     */
    public static String CreatePatchAction_NotSupported_Title;

    /**
     * CreatePatchAction_OpeningWizard_Titlte
     */
    public static String CreatePatchAction_OpeningWizard_Titlte;

    /**
     * CreatePatchAction_PatchErrorMessage
     */
    public static String CreatePatchAction_PatchErrorMessage;

    /**
     * CreatePatchAction_PatchFailedMessage
     */
    public static String CreatePatchAction_PatchFailedMessage;

    /**
     * CreatePatchAction_PatchFailedTitle
     */
    public static String CreatePatchAction_PatchFailedTitle;

    /**
     * CreatePatchAction_SingleErrorMessage
     */
    public static String CreatePatchAction_SingleErrorMessage;

    /**
     * PatchSynchronizePageActionGroup_CreatePatchTitle
     */
    public static String PatchSynchronizePageActionGroup_CreatePatchTitle;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
