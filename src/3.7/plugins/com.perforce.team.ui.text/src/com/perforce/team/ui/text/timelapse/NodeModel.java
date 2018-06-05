/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.text.timelapse;

import com.perforce.p4java.server.PerforceCharsets;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.ui.timelapse.IRevisionInputCache;
import com.perforce.team.ui.timelapse.ITempFileInput;

import java.io.File;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IStorageEditorInput;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public abstract class NodeModel implements INodeModel {

    /**
     * Node record
     */
    public static class NodeRecord {

        int hash;
        Object element;
        int have = -1;
        int head = -1;
    }

    /**
     * Record class
     */
    public static class Record implements IRecord {

        private String id;
        private IP4Revision revision;
        private ChangeType type;

        /**
         * Create a record
         * 
         * @param id
         * @param revision
         * @param type
         */
        public Record(String id, IP4Revision revision, ChangeType type) {
            this.id = id;
            this.revision = revision;
            this.type = type;
        }

        /**
         * @see com.perforce.team.ui.text.timelapse.INodeModel.IRecord#getId()
         */
        public String getId() {
            return this.id;
        }

        /**
         * @see com.perforce.team.ui.text.timelapse.INodeModel.IRecord#getRevision()
         */
        public IP4Revision getRevision() {
            return this.revision;
        }

        /**
         * @see com.perforce.team.ui.text.timelapse.INodeModel.IRecord#getType()
         */
        public ChangeType getType() {
            return this.type;
        }

    }

    private Map<IP4Revision, Map<String, NodeRecord>> records = Collections
            .synchronizedMap(new HashMap<IP4Revision, Map<String, NodeRecord>>());
    private Map<String, ChangeType[]> typeStore = Collections
            .synchronizedMap(new HashMap<String, ChangeType[]>());
    private IP4Revision[] revisions = null;
    private IRevisionInputCache cache = null;
    private MessageDigest digester = null;
    private boolean complete = false;

    /**
     * Node model
     * 
     * @param revisions
     * @param inputCache
     */
    public NodeModel(IP4Revision[] revisions, IRevisionInputCache inputCache) {
        this.revisions = revisions;
        this.cache = inputCache;
        try {
            this.digester = MessageDigest.getInstance("MD5"); //$NON-NLS-1$
        } catch (NoSuchAlgorithmException e) {
            this.digester = null;
        }
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.INodeModel#clear()
     */
    public void clear() {
        this.records.clear();
        this.typeStore.clear();
        this.complete = false;
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.INodeModel#clear(com.perforce.team.core.p4java.IP4Revision)
     */
    public void clear(IP4Revision revision) {
        if (this.records.remove(revision) != null) {
            this.complete = false;
        }
    }

    /**
     * Get a node record for a specified revision and id
     * 
     * @param revision
     * @param id
     * @return - node record or null if none found
     */
    protected NodeRecord getRecord(IP4Revision revision, String id) {
        NodeRecord record = null;
        Map<String, NodeRecord> revRecords = records.get(revision);
        if (revRecords != null) {
            record = revRecords.get(id);
        }
        return record;
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.INodeModel#getTotalChangeCount(com.perforce.team.core.p4java.IP4Revision,
     *      java.lang.String)
     */
    public int getTotalChangeCount(IP4Revision revision, String id) {
        int hash = 0;
        int change = 0;
        NodeRecord record = getRecord(revision, id);
        if (record != null) {
            if (record.head > 0) {
                change = record.head;
            } else {
                for (IP4Revision rev : this.revisions) {
                    NodeRecord found = getRecord(rev, id);
                    if (found != null && hash != found.hash) {
                        change++;
                        hash = found.hash;
                    } else if (found == null && hash != 0) {
                        // Record was deleted
                        change++;
                        hash = 0;
                    }
                }
            }
            record.head = change;
        }
        return change;
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.INodeModel#getRelativeChangeCount(com.perforce.team.core.p4java.IP4Revision,
     *      java.lang.String)
     */
    public int getRelativeChangeCount(IP4Revision revision, String id) {
        int hash = 0;
        int change = 0;
        NodeRecord record = getRecord(revision, id);
        if (record != null) {
            if (record.have > 0) {
                change = record.have;
            } else {
                for (IP4Revision rev : this.revisions) {
                    NodeRecord found = getRecord(rev, id);
                    if (found != null && hash != found.hash) {
                        change++;
                        hash = found.hash;
                    } else if (found == null && hash != 0) {
                        // Record was deleted
                        change++;
                        hash = 0;
                    }
                    if (rev.equals(revision)) {
                        break;
                    }
                }
            }
            record.have = change;
        }
        return change;
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.INodeModel#getNodeRecords(java.lang.String)
     */
    public IRecord[] getNodeRecords(String id) {
        IRecord[] records = new IRecord[this.revisions.length];
        ChangeType[] types = getRecords(id);
        for (int i = 0; i < types.length; i++) {
            records[i] = new Record(id, this.revisions[i], types[i]);
        }
        return records;
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.INodeModel#getChangeRevisions(java.lang.String)
     */
    public IP4Revision[] getChangeRevisions(String id) {
        int hash = 0;
        List<IP4Revision> changes = new ArrayList<IP4Revision>();
        for (IP4Revision revision : this.revisions) {
            Map<String, NodeRecord> revRecords = records.get(revision);
            if (revRecords != null) {
                NodeRecord found = revRecords.get(id);
                if (found != null && hash != found.hash) {
                    changes.add(revision);
                    hash = found.hash;
                }
            }
        }
        return changes.toArray(new IP4Revision[changes.size()]);
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.INodeModel#getChangeCount(java.lang.String)
     */
    public int getChangeCount(String id) {
        int changes = 0;
        int hash = 0;
        for (IP4Revision revision : this.revisions) {
            Map<String, NodeRecord> revRecords = records.get(revision);
            if (revRecords != null) {
                NodeRecord found = revRecords.get(id);
                if (found != null && hash != found.hash) {
                    changes++;
                    hash = found.hash;
                }
            }
        }
        return changes;
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.INodeModel#getRevisions(java.lang.String)
     */
    public IP4Revision[] getRevisions(String id) {
        List<IP4Revision> foundRevisions = new ArrayList<IP4Revision>();
        if (id != null) {
            for (IP4Revision revision : this.revisions) {
                Map<String, NodeRecord> revRecords = records.get(revision);
                if (revRecords != null && revRecords.containsKey(id)) {
                    foundRevisions.add(revision);
                }
            }
        }
        return foundRevisions.toArray(new IP4Revision[foundRevisions.size()]);
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.INodeModel#getRecords(java.lang.String)
     */
    public ChangeType[] getRecords(String id) {
        ChangeType[] types = new ChangeType[0];
        if (id != null) {
            types = typeStore.get(id);
            if (types == null) {
                types = new ChangeType[this.revisions.length];
                Arrays.fill(types, ChangeType.UNCHANGED);
                int index = 0;
                ChangeType lastType = ChangeType.UNCHANGED;
                int lastHash = 0;
                for (IP4Revision revision : this.revisions) {
                    Map<String, NodeRecord> revRecords = records.get(revision);
                    if (revRecords != null) {
                        NodeRecord found = revRecords.get(id);
                        switch (lastType) {
                        case ADD:
                        case EDIT:
                            if (found != null) {
                                if (found.hash != lastHash) {
                                    lastHash = found.hash;
                                    types[index] = ChangeType.EDIT;
                                }
                            } else {
                                types[index] = ChangeType.DELETE;
                                lastHash = 0;
                            }
                            break;
                        case DELETE:
                            if (found != null) {
                                lastHash = found.hash;
                                types[index] = ChangeType.ADD;
                            }
                            break;
                        case UNCHANGED:
                            if (found != null) {
                                if (lastHash == 0) {
                                    types[index] = ChangeType.ADD;
                                } else if (found.hash != lastHash) {
                                    types[index] = ChangeType.EDIT;
                                }
                                lastHash = found.hash;
                            } else if (lastHash != 0) {
                                types[index] = ChangeType.DELETE;
                                lastHash = 0;
                            }
                            break;

                        default:
                            break;
                        }
                    }
                    lastType = types[index];
                    index++;
                }
                typeStore.put(id, types);
            }
        }
        return types;
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.INodeModel#buildAll()
     */
    public void buildAll() {
        clear();
        for (IP4Revision revision : revisions) {
            build(revision);
        }
        this.complete = true;
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.INodeModel#isComplete()
     */
    public boolean isComplete() {
        return this.complete;
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.INodeModel#build(com.perforce.team.core.p4java.IP4Revision)
     */
    public void build(IP4Revision revision) {
        clear(revision);
        IStorageEditorInput input = this.cache.getRevisionInput(revision);
        if (input != null) {
            Object nodeInput = parseInput(input, revision);
            if (nodeInput != null) {
                findNodes(nodeInput, revision);
            }
            if (input instanceof ITempFileInput) {
                deleteTempFile((ITempFileInput) input);
            }
        }
    }

    /**
     * Delete the temp file if configured to be deleted by the input
     * 
     * @param tempInput
     */
    protected void deleteTempFile(ITempFileInput tempInput) {
        if (tempInput.deletePostLoad()) {
            File file = tempInput.getFile();
            if (file != null) {
                if(!file.delete()){
                	String msg = MessageFormat.format(Messages.NodeModel_DeleteFileError,file.getAbsolutePath());
                	PerforceProviderPlugin.logError(msg);
                }
            }
        }
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.INodeModel#getNode(java.lang.String,
     *      com.perforce.team.core.p4java.IP4Revision)
     */
    public Object getNode(String id, IP4Revision revision) {
        Object node = null;
        Map<String, NodeRecord> revRecords = records.get(revision);
        if (revRecords != null) {
            NodeRecord record = revRecords.get(id);
            if (record != null) {
                node = record.element;
            }
        }
        return node;
    }

    /**
     * Add a record
     * 
     * @param hash
     * @param id
     * @param id2
     * @param element
     * @param revision
     */
    protected void addRecord(int hash, String id, String id2, Object element,
            IP4Revision revision) {
        if (id != null) {
            NodeRecord record = new NodeRecord();
            record.hash = hash;
            record.element = element;
            Map<String, NodeRecord> revRecords = records.get(revision);
            if (revRecords == null) {
                revRecords = Collections
                        .synchronizedMap(new HashMap<String, NodeRecord>());
                records.put(revision, revRecords);
            }
            revRecords.put(id, record);
            if (id2 != null) {
                revRecords.put(id2, record);
            }
        }
    }

    /**
     * Get element for specified revision
     * 
     * @param revision
     * @param id
     * @return - element
     */
    public Object getElement(IP4Revision revision, String id) {
        Object element = null;
        Map<String, NodeRecord> revRecords = records.get(revision);
        if (revRecords != null) {
            NodeRecord record = revRecords.get(id);
            if (record != null) {
                element = record.element;
            }
        }
        return element;
    }

    /**
     * Hash the non-null content
     * 
     * @param content
     *            non-null
     * @return - int hash
     */
    protected int computeHash(String content, Charset charset) {
        if (digester != null) {
            digester.update(content.getBytes(charset));
            return new BigInteger(1, digester.digest()).intValue();
        } else {
            return content.hashCode();
        }
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.INodeModel#isChanged(java.lang.String,
     *      com.perforce.team.core.p4java.IP4Revision)
     */
    public boolean isChanged(String id, IP4Revision revision) {
        boolean changed = false;
        if (id != null && revision != null) {
            IRecord[] records = getNodeRecords(id);
            for (IRecord record : records) {
                if (revision == record.getRevision()) {
                    changed = record.getType() != ChangeType.UNCHANGED;
                    break;
                }
            }
        }
        return changed;
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.INodeModel#parseInput(org.eclipse.ui.IEditorInput,
     *      com.perforce.team.core.p4java.IP4Revision)
     */
    public abstract Object parseInput(IEditorInput input, IP4Revision revision);

    /**
     * @see com.perforce.team.ui.text.timelapse.INodeModel#findNodes(java.lang.Object,
     *      com.perforce.team.core.p4java.IP4Revision)
     */
    public abstract void findNodes(Object nodeInput, IP4Revision revision);

}
