/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.p4java.admin;

import com.perforce.p4java.core.IMapEntry;

/**
 * Describes a protection entry (line) in a Perforce triggers
 * table. These are described in more detail in the various
 * main Perforce admin documentation pages.<p>
 */

public interface ITriggerEntry extends IMapEntry {
	
	public enum TriggerType {

		ARCHIVE("archive"),
		AUTH_CHECK("auth-check"),
		AUTH_CHECK_2FA("auth-check-2fa"),
		AUTH_CHECK_SSO("auth-check-sso"),
		AUTH_INIT_2FA("auth-init-2fa"),
		AUTH_INVALIDATE("auth-invalidate"),
		AUTH_PRE_2FA("auth-pre-2fa"),
		AUTH_PRE_SSO("auth-pre-sso"),
		AUTH_SET("auth-set"),
		BGTASK("bgtask"),
		CHANGE_COMMIT("change-commit"),
		CHANGE_CONTENT("change-content"),
		CHANGE_FAILED("change-failed"),
		CHANGE_SUBMIT("change-submit"),
		COMMAND("command"),
		EDGE_CONTENT("edge-content"),
		EDGE_SUBMIT("edge-submit"),
		EXTENSION_RUN("extension-run"),
		FAILED_OVER("failed-over"),
		FIX_ADD("fix-add"),
		FIX_DELETE("fix-delete"),
		FORM_COMMIT("form-commit"),
		FORM_DELETE("form-delete"),
		FORM_IN("form-in"),
		FORM_OUT("form-out"),
		FORM_SAVE("form-save"),
		GLOBAL_EXTCFG("global-extcfg"),
		GRAPH_FORK_REPO("graph-fork-repo"),
		GRAPH_LFS_PUSH("graph-lfs-push"),
		GRAPH_PUSH_COMPLETE("graph-push-complete"),
		GRAPH_PUSH_REFERENCE("graph-push-reference"),
		GRAPH_PUSH_REFERENCE_COMPLETE("graph-push-reference-complete"),
		GRAPH_PUSH_START("graph-push-start"),
		HEARTBEAT_DEAD("heartbeat-dead"),
		HEARTBEAT_MISSING("heartbeat-missing"),
		HEARTBEAT_RESUMED("heartbeat-resumed"),
		JOURNAL_ROTATE("journal-rotate"),
		JOURNAL_ROTATE_LOCK("journal-rotate-lock"),
		PULL_ARCHIVE("pull-archive"),
		PUSH_COMMIT("push-commit"),
		PUSH_CONTENT("push-content"),
		PUSH_SUBMIT("push-submit"),
		SERVICE_CHECK("service-check"),
		SHELVE_COMMIT("shelve-commit"),
		SHELVE_DELETE("shelve-delete"),
		SHELVE_SUBMIT("shelve-submit");

		private final String triggerType;

	    private TriggerType(String triggerType) {
	        this.triggerType = triggerType;
	    }

		/**
		 * Return a suitable Trigger type as inferred from the passed-in
		 * string, which is assumed to be the string form of a Depot type.
		 * Otherwise return the null.
		 */
		public static TriggerType fromString(String triggerType) {
			if (triggerType != null) {
				for (TriggerType tt : TriggerType.values()) {
					if (triggerType.equalsIgnoreCase(tt.toString())) {
						return tt;
					}
				}
			}
			return null;
		}
	    
	    public String toString(){
	       return this.triggerType;
	    }		
	};

	/**
	 * Gets the trigger name.
	 * 
	 * @return the trigger name
	 */
	String getName();
	
	/**
	 * Sets the trigger name.
	 * 
	 * @param name
	 *            the trigger name
	 */
	void setName(String name);
	
	/**
	 * Gets the trigger type.
	 * 
	 * @return the trigger type
	 */
	TriggerType getTriggerType();
	
	/**
	 * Sets the trigger type.
	 * 
	 * @param type
	 *            the trigger type
	 */
	void setTriggerType(TriggerType type);
	
	/**
	 * For change and submit triggers, a file pattern to match files in the
	 * changelist. This file pattern can be an exclusion mapping (-pattern), to
	 * exclude files. For form triggers, the name of the form (branch, client,
	 * etc). For fix triggers 'fix' is required as the path value. For
	 * authentication triggers, 'auth' is required as the path value. For
	 * archive triggers, a file pattern to match the name of the file being
	 * accessed in the archive. Note that, due to lazy copying when branching
	 * files, the name of the file in the archive can not be the same as the
	 * name of the file in the depot. For command triggers, use the name of the
	 * command to match, e.g. 'pre-user-$cmd' or a regular expression, e.g.
	 * '(pre|post)-user-add'. *
	 * 
	 * @return the depot file path pattern or form type
	 */
	String getPath();
	
	/**
	 * For change and submit triggers, a file pattern to match files in the
	 * changelist. This file pattern can be an exclusion mapping (-pattern), to
	 * exclude files. For form triggers, the name of the form (branch, client,
	 * etc). For fix triggers 'fix' is required as the path value. For
	 * authentication triggers, 'auth' is required as the path value. For
	 * archive triggers, a file pattern to match the name of the file being
	 * accessed in the archive. Note that, due to lazy copying when branching
	 * files, the name of the file in the archive can not be the same as the
	 * name of the file in the depot. For command triggers, use the name of the
	 * command to match, e.g. 'pre-user-$cmd' or a regular expression, e.g.
	 * '(pre|post)-user-add'. *
	 * 
	 * @param path
	 *            the depot file path pattern or form type
	 */
	void setPath(String path);

	/**
	 * Gets the trigger command. If the command contains spaces, enclose it in
	 * double quotes.
	 * 
	 * @return the trigger comamnd
	 */
	String getCommand();
	
	/**
	 * Sets the trigger command. If the command contains spaces, enclose it in
	 * double quotes.
	 * 
	 * @param command
	 *            the trigger command
	 */
	void setCommand(String command);
}
