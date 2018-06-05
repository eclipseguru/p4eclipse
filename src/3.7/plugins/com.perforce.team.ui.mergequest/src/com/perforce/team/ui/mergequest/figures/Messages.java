/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.figures;

import org.eclipse.osgi.util.NLS;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.mergequest.figures.messages"; //$NON-NLS-1$

    /**
     * CalloutFigure_IntegrateTo
     */
    public static String CalloutFigure_IntegrateTo;

    /**
     * CalloutFigure_ShowChangelists
     */
    public static String CalloutFigure_ShowChangelists;

    /**
     * CalloutFigure_SourcePath
     */
    public static String CalloutFigure_SourcePath;

    /**
     * CalloutFigure_SpecName
     */
    public static String CalloutFigure_SpecName;

    /**
     * CalloutFigure_TargetPath
     */
    public static String CalloutFigure_TargetPath;

    /**
     * ChangeCountFigure_OneOrMore
     */
    public static String ChangeCountFigure_OneOrMore;

    /**
     * ChangeCountFigure_Unknown
     */
    public static String ChangeCountFigure_Unknown;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
