/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java.synchronize;

import com.perforce.p4java.CharsetDefs;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.impl.generic.core.file.FileSpec;
import com.perforce.p4java.option.client.ResolveFilesAutoOptions;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.P4Collection;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.variants.CachedResourceVariant;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class PerforceSyncFile extends CachedResourceVariant {

    /**
     * Types of variants
     */
    public enum VariantType {
        /**
         * Base revision
         */
        BASE,

        /**
         * Remote revision
         */
        REMOTE
    }

    private IP4File p4File;
    private VariantType type;

    /**
     * Creates a new perforce sync file
     * 
     * @param file
     * @param type
     */
    public PerforceSyncFile(IP4File file, VariantType type) {
        this.p4File = file;
        this.type = type;

        // Check for the being unresolved but without a fetch resolve record.
        // This can occur when an fstat is done but a resole -n has not been
        // yet.
        if (p4File.isUnresolved()) {
            IFileSpec spec = p4File.getIntegrationSpec();
            if (spec == null) {
                updateBase(p4File);
            }
        }
    }

    private InputStream getUnresolvedBaseStream(IP4File file) {
        InputStream stream = null;
        IFileSpec spec = file.getIntegrationSpec();
        if (spec != null) {
            int base = spec.getStartFromRev();
            if (base >= 0) {
                String from = spec.getFromFile();
                if (from != null) {
                    file = file.getConnection().getFile(new FileSpec(from));
                    if (file != null) {
                        stream = file.getRemoteContents(base);
                    }
                } else {
                    stream = file.getRemoteContents(base);
                }
            }
        }
        return stream;
    }

    /**
     * @see org.eclipse.team.core.variants.CachedResourceVariant#fetchContents(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    protected void fetchContents(IProgressMonitor monitor) throws TeamException {
        InputStream reader = null;
        switch (type) {
        case BASE:
            if (p4File.isUnresolved()) {
                // Try to fetch base
                reader = getUnresolvedBaseStream(p4File);

                // If base fetching failed then try to fetch again after an
                // update of the resolved integ spec
                if (reader == null) {
                    updateBase(p4File);
                    reader = getUnresolvedBaseStream(p4File);
                }
            } else {
                reader = this.p4File.getRemoteContents(this.p4File
                        .getHaveRevision());
            }
            break;
        case REMOTE:
            reader = this.p4File.getRemoteContents(this.p4File
                    .getHeadRevision());
            break;

        default:
            break;
        }
        if (reader == null) {
            reader = new ByteArrayInputStream(new byte[0]);
        }
        setContents(reader, monitor);
    }

    private void updateBase(IP4File file) {
        P4Collection collection = new P4Collection();
        collection.add(file);
        collection.resolve(new ResolveFilesAutoOptions().setShowActionsOnly(true).setShowBase(true));
    }

    /**
     * @see org.eclipse.team.core.variants.CachedResourceVariant#getCacheId()
     */
    @Override
    protected String getCacheId() {
        return this.p4File.getRemotePath() + getContentIdentifier();
    }

    /**
     * @see org.eclipse.team.core.variants.CachedResourceVariant#getCachePath()
     */
    @Override
    protected String getCachePath() {
        return this.p4File.getRemotePath() + getContentIdentifier();
    }

    /**
     * @see org.eclipse.team.core.variants.IResourceVariant#asBytes()
     */
    public byte[] asBytes() {
        return getCachePath().getBytes(CharsetDefs.DEFAULT);
    }

    /**
     * @see org.eclipse.team.core.variants.IResourceVariant#getContentIdentifier()
     */
    public String getContentIdentifier() {
        StringBuilder revision = new StringBuilder();
        switch (type) {
        case BASE:
            if (p4File.isUnresolved()) {
                IFileSpec spec = p4File.getIntegrationSpec();
                if (spec == null) {
                    updateBase(p4File);
                }
                if (spec != null && spec.getStartFromRev() >= 0) {
                    String fromFile = spec.getFromFile();
                    if (fromFile != null) {
                        int lastSlash = fromFile.lastIndexOf('/');
                        if (lastSlash >= 0 && lastSlash + 1 < fromFile.length()) {
                            revision.append(fromFile.substring(lastSlash + 1));
                        } else {
                            revision.append(fromFile);
                        }
                    }
                    revision.append('#');
                    revision.append(spec.getStartFromRev());
                }
            } else if (p4File.openedForDelete()) {
                revision.append('#');
                revision.append(this.p4File.getHeadRevision());
            } else {
                revision.append('#');
                revision.append(this.p4File.getHaveRevision());
            }
            break;
        case REMOTE:
            revision.append('#');
            revision.append(this.p4File.getHeadRevision());
            break;

        default:
            break;
        }
        if (revision.length() == 0) {
            revision.append("#0"); //$NON-NLS-1$
        }
        return revision.toString();
    }

    /**
     * @see org.eclipse.team.core.variants.IResourceVariant#getName()
     */
    public String getName() {
        return this.p4File.getName();
    }

    /**
     * @see org.eclipse.team.core.variants.IResourceVariant#isContainer()
     */
    public boolean isContainer() {
        return false;
    }

    /**
     * Gets the underlying p4 file
     * 
     * @return - p4 file
     */
    public IP4File getFile() {
        return this.p4File;
    }

}
