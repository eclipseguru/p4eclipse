package com.perforce.p4java.server.delegator;

import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.AccessException;
import com.perforce.p4java.exception.ConnectionException;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.exception.RequestException;
import com.perforce.p4java.option.client.UndoFilesOptions;

import java.util.List;

public interface IUndoDelegator {

    /**
     * Undo one or more previously submitted files. The 'undone' changes remain a
     * part of the history, but the new revisions submitted after 'p4 undo' will
     * reverse their effect.
     * <p>
     * If a single revision is specified, the specified revision is undone.
     * If a revision range is specified, the entire range is undone.
     * <p>
     *
     * @param fileSpecs    if non-empty, undo the specified files;
     *                     otherwise undo all qualifying files
     * @param changelistId - if not IChangelist.UNKNOWN, the files are opened
     *                     in the numbered pending changelist instead of the
     *                     'default' changelist.
     * @param listOnly     – if true, don't actually perform the move, just
     *                     return what would happen if the move was performed
     * @return list of IFileSpec objects representing the results of this undo
     * @throws ConnectionException - if the Perforce server is unreachable or
     *                             is not connected.
     * @throws RequestException    – if the Perforce server encounters an error
     *                             during its processing of the request
     * @throws AccessException     – if the Perforce server denies access to the
     *                             caller
     */
    List<IFileSpec> undoFiles(List<IFileSpec> fileSpecs, int changelistId, boolean listOnly)
            throws ConnectionException, RequestException, AccessException;

    /**
     * Undo one or more previously submitted files. The 'undone' changes remain a
     * part of the history, but the new revisions submitted after 'p4 undo' will
     * reverse their effect.
     * <p>
     * If a single revision is specified, the specified revision is undone.
     * If a revision range is specified, the entire range is undone.
     * <p>
     *
     * @param fileSpecs if non-empty, undo the specified files;
     *                  otherwise undo all qualifying files
     * @param opts      possibly-null UndoFilesOptions object object specifying
     *                  method options.
     * @return non-null but possibly-empty list of qualifying files to undo. Not
     * all fields in individual file specs will be valid or make sense
     * to be accessed.
     * @throws P4JavaException if an error occurs processing this method and its
     *                         parameters.
     */
    List<IFileSpec> undoFiles(List<IFileSpec> fileSpecs, UndoFilesOptions opts)
            throws P4JavaException;
}
