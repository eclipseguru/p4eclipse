/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.model;

import com.perforce.p4java.core.IFileDiff;
import com.perforce.p4java.core.IFileDiff.Status;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.folder.IP4DiffFile;
import com.perforce.team.core.folder.P4DiffFile;
import com.perforce.team.core.map.MapFlag;
import com.perforce.team.core.map.MapTable;
import com.perforce.team.core.map.MapTableT;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Revision;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class FileDiffContainer {

    private GroupedDiffContainer leftGroup = null;
    private GroupedDiffContainer rightGroup = null;
    private Map<IP4DiffFile, FileEntry> entries = null;
    private MapTable table = new MapTable();

    private int contentCount = 0;
    private int identicalCount = 0;
    private int leftUniqueCount = 0;
    private int rightUniqueCount = 0;

    /**
     * Create a file diff container
     */
    public FileDiffContainer() {
        this.leftGroup = new GroupedDiffContainer();
        this.rightGroup = new GroupedDiffContainer();
        this.entries = new HashMap<IP4DiffFile, FileEntry>();
    }

    /**
     * Add mapping
     * 
     * @param left
     * @param right
     */
    public void addMapping(String left, String right) {
        if (left != null && right != null && right.length() > 0
                && left.length() > 0) {
            table.insert(left, right, MapFlag.MfMap);
        }
    }

    /**
     * Get file entries
     * 
     * @return non-null but possibly empty array of file entries
     */
    public FileEntry[] getEntries() {
        return this.entries.values()
                .toArray(new FileEntry[this.entries.size()]);
    }

    /**
     * Get a file entry for the specified diff file
     * 
     * @param file
     * @return file entry
     */
    public FileEntry getEntry(IP4DiffFile file) {
        FileEntry entry = null;
        if (file != null) {
            entry = this.entries.get(file);
        }
        return entry;
    }

    /**
     * Get grouped diff container for specified side
     * 
     * @param side
     *            - {@link Status#LEFT_ONLY} or {@link Status#RIGHT_ONLY}
     * @return grouped diff container
     */
    public GroupedDiffContainer getContainer(Status side) {
        return side == Status.LEFT_ONLY ? this.leftGroup : this.rightGroup;
    }

    private void setHistory(Collection<IP4DiffFile> diffs,
            IP4Connection connection) {

        Map<String, IP4DiffFile> fileDiffs = new HashMap<String, IP4DiffFile>();
        for (IP4DiffFile diff : diffs) {
            if (Status.IDENTICAL != diff.getDiff().getStatus()) {
                fileDiffs.put(diff.getRemotePath() + "#" + diff.getRevision(), //$NON-NLS-1$
                        diff);
            }
        }

        IP4Revision[] revs = connection.getHistory(
                fileDiffs.keySet().toArray(new String[fileDiffs.size()]), 1);
        if(revs==null)
        	return;
        
        for (IP4Revision rev : revs) {
        	IP4DiffFile diff = fileDiffs.get(rev.getRemotePath() + "#" + rev.getRevision()); //$NON-NLS-1$
            if (diff != null) {
                ((P4DiffFile) diff).setRevision(rev);
            }
        }
    }

    private void updateMapping(FileEntry file) {
        if (!table.isValid()) {
            return;
        }
        Status side = file.getFile().getDiff().getStatus();
        if (Status.LEFT_ONLY == side || Status.RIGHT_ONLY == side) {
            StringBuilder mapped = new StringBuilder();
            MapTableT tableSide = side == Status.LEFT_ONLY
                    ? MapTableT.LHS
                    : MapTableT.RHS;
            String from = file.getFile().getRemotePath();
            try {
                table.translate(tableSide, from, mapped);
                if (from.length() > 0) {
                    file.setVirtualPairPath(mapped.toString());
                }
            } catch (Throwable t) {
                PerforceProviderPlugin.logError(t);
            }
        }
    }

    /**
     * Add diffs to this container
     * 
     * @param diffs
     * @param connection
     */
    public void add(IFileDiff[] diffs, IP4Connection connection) {
        if (diffs != null) {
            Status status = null;
            FileEntry entry1 = null;
            FileEntry entry2 = null;
            List<IP4DiffFile> files = new LinkedList<IP4DiffFile>();
            for (IFileDiff diff : diffs) {
                status = diff.getStatus();
                if (status != null) {
                    P4DiffFile source = null;
                    P4DiffFile target = null;
                    switch (status) {
                    case LEFT_ONLY:
                        source = new P4DiffFile(connection, diff, true);
                        leftUniqueCount++;
                        break;
                    case RIGHT_ONLY:
                        target = new P4DiffFile(connection, diff, false);
                        rightUniqueCount++;
                        break;
                    case CONTENT:
                        source = new P4DiffFile(connection, diff, true);
                        target = new P4DiffFile(connection, diff, false);
                        contentCount++;
                        break;
                    case IDENTICAL:
                        identicalCount++;
                        if (".project".equals(P4CoreUtils.getName(diff //$NON-NLS-1$
                                .getDepotFile1()))) {
                            leftGroup
                                    .add(new P4DiffFile(connection, diff, true));
                        }
                        if (".project".equals(P4CoreUtils.getName(diff //$NON-NLS-1$
                                .getDepotFile2()))) {
                            rightGroup.add(new P4DiffFile(connection, diff,
                                    false));
                        }
                        break;
                    default:
                        break;
                    }
                    if (source != null) {
                        source.setPair(target);
                        entry1 = leftGroup.add(source);
                        updateMapping(entry1);
                        files.add(source);
                    } else {
                        entry1 = null;
                    }
                    if (target != null) {
                        target.setPair(source);
                        entry2 = rightGroup.add(target);
                        updateMapping(entry2);
                        files.add(target);
                    } else {
                        entry2 = null;
                    }
                    if (entry1 != null) {
                        entry1.setPair(entry2);
                        entries.put(source, entry1);
                    }
                    if (entry2 != null) {
                        entry2.setPair(entry1);
                        entries.put(target, entry2);
                    }
                }
            }
            setHistory(files, connection);
        }
    }

    /**
     * Finish the file diff container
     */
    public void finish() {
        leftGroup.finish();
        rightGroup.finish();
        leftGroup.completeUniquePairs(rightGroup);
        rightGroup.completeUniquePairs(leftGroup);
        leftGroup.disposeWorkingEntries();
        rightGroup.disposeWorkingEntries();
    }

    /**
     * Get content difference count
     * 
     * @return diff count
     */
    public int getContentCount() {
        return this.contentCount;
    }

    /**
     * Get identical count
     * 
     * @return diff count
     */
    public int getIdenticalCount() {
        return this.identicalCount;
    }

    /**
     * Get left only unique file count
     * 
     * @return diff count
     */
    public int getLeftUniqueCount() {
        return this.leftUniqueCount;
    }

    /**
     * Get right only unique file count
     * 
     * @return diff count
     */
    public int getRightUniqueCount() {
        return this.rightUniqueCount;
    }

    /**
     * Get unique count
     * 
     * @return diff count
     */
    public int getUniqueCount() {
        return getLeftUniqueCount() + getRightUniqueCount();
    }
}
