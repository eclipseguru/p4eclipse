package com.perforce.p4java.option.client;

import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.exception.OptionsException;
import com.perforce.p4java.option.Options;
import com.perforce.p4java.server.IServer;

import java.util.List;

public class UndoFilesOptions extends Options {

    /**
     * Options: -c[changelist], -n
     */
    public static final String OPTIONS_SPECS = "i:c:clz b:n";

    /**
     * If not IChangelist.UNKNOWN, the files are opened in the numbered
     * pending changelist instead of the 'default' changelist.
     * Corresponds to the -c flag.
     */
    protected int changelistId = IChangelist.UNKNOWN;

    /**
     * If true, don't actually perform the undo, just return what would
     * happen if the undo was performed. Corresponds to the -n flag.
     */
    protected boolean listOnly = false;

    /**
     * Default constructor.
     */
    public UndoFilesOptions() {
        super();
    }

    /**
     * Strings-based constructor; see 'p4 help [command]' for possible options.
     * <p>
     *
     * <b>WARNING: you should not pass more than one option or argument in each
     * string parameter. Each option or argument should be passed-in as its own
     * separate string parameter, without any spaces between the option and the
     * option value (if any).<b>
     * <p>
     *
     * <b>NOTE: setting options this way always bypasses the internal options
     * values, and getter methods against the individual values corresponding to
     * the strings passed in to this constructor will not normally reflect the
     * string's setting. Do not use this constructor unless you know what you're
     * doing and / or you do not also use the field getters and setters.</b>
     *
     * @see com.perforce.p4java.option.Options#Options(java.lang.String...)
     */
    public UndoFilesOptions(String... options) {
        super(options);
    }


    /**
     * Explicit-value constructor.
     */
    public UndoFilesOptions(boolean listOnly, int changelistId) {
        super();
        this.listOnly = listOnly;
        this.changelistId = changelistId;
    }

    /**
     * @see com.perforce.p4java.option.Options#processOptions(com.perforce.p4java.server.IServer)
     */
    @Override
    public List<String> processOptions(IServer server) throws OptionsException {
        this.optionList = this.processFields(OPTIONS_SPECS,
                this.getChangelistId(),
                this.isListOnly());
        return this.optionList;
    }

    public int getChangelistId() {
        return changelistId;
    }

    public UndoFilesOptions setChangelistId(int changelistId) {
        this.changelistId = changelistId;
        return this;
    }

    public boolean isListOnly() {
        return listOnly;
    }

    public UndoFilesOptions setListOnly(boolean listOnly) {
        this.listOnly = listOnly;
        return this;
    }
}
