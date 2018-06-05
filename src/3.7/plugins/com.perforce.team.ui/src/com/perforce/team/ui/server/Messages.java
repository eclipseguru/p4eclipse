package com.perforce.team.ui.server;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.server.messages"; //$NON-NLS-1$
    public static String ClientWidget_Access;
    public static String ClientWidget_AltRoots;
    public static String ClientWidget_AtChangeMustBeInteger;
    public static String ClientWidget_Description;
    public static String ClientWidget_Host;
    public static String ClientWidget_IllegalServerName;
	public static String ClientWidget_InputChangeListNumberMsg;
    public static String ClientWidget_LineEnd;
	public static String ClientWidget_LoadingStreamView;
    public static String ClientWidget_NoneSelected;
    public static String ClientWidget_Options;
    public static String ClientWidget_Owner;
    public static String ClientWidget_Refreshing;
	public static String ClientWidget_RestrictToServerId;
    public static String ClientWidget_Root;
    public static String ClientWidget_Stream;
    public static String ClientWidget_StreamAtChange;
    public static String ClientWidget_SubmitOptions;
    public static String ClientWidget_Update;
    public static String ClientWidget_View;
    public static String ClientWidget_Workspace;
    public static String EditClientDialog_ClientNotChanged;
	public static String EditClientDialog_Save;
	public static String EditClientDialog_SpecNotFound;
    public static String EditClientDialog_Workspace;
    public static String ServerInfoDialog_ClientAddress;
    public static String ServerInfoDialog_ClientHost;
    public static String ServerInfoDialog_ClientInformation;
    public static String ServerInfoDialog_ClientName;
    public static String ServerInfoDialog_ClientRoot;
    public static String ServerInfoDialog_ConnectionInformation;
    public static String ServerInfoDialog_ConnectionInformationFor;
    public static String ServerInfoDialog_EditClient;
    public static String ServerInfoDialog_ServerAddress;
    public static String ServerInfoDialog_ServerDate;
    public static String ServerInfoDialog_ServerId;
    public static String ServerInfoDialog_ServerInformation;
    public static String ServerInfoDialog_ServerLicense;
    public static String ServerInfoDialog_ServerRoot;
    public static String ServerInfoDialog_ServerUptime;
    public static String ServerInfoDialog_ServerVersion;
    public static String ServerInfoDialog_ServerEncryption;
    public static String ServerInfoDialog_BrokerVersion;
    public static String ServerInfoDialog_BrokerAddress;
    public static String ServerInfoDialog_BrokerEncryption;
    public static String ServerInfoDialog_SandboxVersion;
    public static String ServerInfoDialog_SandboxPort;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
