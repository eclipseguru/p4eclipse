package com.perforce.p4java.option.server;

import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.exception.OptionsException;
import com.perforce.p4java.option.Options;
import com.perforce.p4java.server.IServer;

import java.util.List;

public class StreamlogOptions extends Options {

	/**
	 * Options:  -c  changelist# -h -i -l -L -t -m max
	 */
	public static final String OPTIONS_SPECS = "i:c:cl b:i b:h b:l b:L i:m:gtz";


	/**
	 * If non-negative, displays the stream submitted at the specified
	 * changelist number
	 */
	protected int changelistId = IChangelist.UNKNOWN;

	/**
	 * Includes inherited stream history. For a stream created by
	 * branching (using 'p4 integrate'), streamlog lists the revisions of the
	 * stream's ancestors up to the branch points that led to the specified
	 * revision.  Stream history inherited by renaming (using 'p4 move') is
	 * always displayed regardless of whether -i is specified.
	 */
	protected boolean includeInherited = false;

	/**
	 * Displays the stream content history instead of stream name
	 * history.  The list includes revisions of other streams that were
	 * branched or copied (using 'p4 integrate' and 'p4 resolve -at') to
	 * the specified revision.  Revisions that were replaced by copying
	 * or branching are omitted, even if they are part of the history of
	 * the specified revision.
	 */
	protected boolean includeHistory = false;

	/**
	 * Lists the full text of the changelist descriptions.
	 */
	protected boolean fullText = false;

	/**
	 * Lists the full text of the changelist descriptions,
	 * truncated to 250 characters if longer.
	 */
	protected boolean fullTextTruncated = false;

	/**
	 * Displays at most 'max' revisions per stream
	 * argument specified.
	 */
	protected int maxResults = 0;


	/**
	 * Default constructor.
	 */
	public StreamlogOptions() {
		super();
	}

	public StreamlogOptions(String... options) {
		super(options);
	}

	@Override
	public List<String> processOptions(IServer server) throws OptionsException {
		this.optionList = this.processFields(OPTIONS_SPECS,
				this.changelistId,
				this.includeInherited,
				this.includeHistory,
				this.fullText,
				this.fullTextTruncated,
				this.maxResults);

		return this.optionList;
	}

	public void setChangelistId(int changelistId) {
		this.changelistId = changelistId;
	}

	public int getChangelistId() {
		return changelistId;
	}

	public void setIncludeInherited(boolean includeInherited) {
		this.includeInherited = includeInherited;
	}

	public boolean isIncludeInherited() {
		return includeInherited;
	}

	public void setIncludeHistory(boolean includeHistory) {
		this.includeHistory = includeHistory;
	}

	public boolean isIncludeHistory() {
		return includeHistory;
	}

	public void setFullText(boolean fullText) {
		this.fullText = fullText;
	}

	public boolean isFullText() {
		return fullText;
	}

	public void setFullTextTruncated(boolean fullTextTruncated) {
		this.fullTextTruncated = fullTextTruncated;
	}

	public boolean isFullTextTruncated() {
		return fullTextTruncated;
	}

	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}

	public int getMaxResults() {
		return maxResults;
	}
}
