package com.perforce.team.core;

import java.io.IOException;

import org.eclipse.equinox.security.storage.StorageException;

/**
 * The interface for secure store which provide API for storing and to
 * retrieving credentials to and from.
 */
public interface ICredentialsStore {

    public void clear();

    public void flush() throws IOException;

    public String get(String key, String def) throws StorageException;

    public byte[] getByteArray(String key, byte[] def) throws StorageException;

    public String[] keys();

    public void put(String key, String value, boolean encrypt)
            throws StorageException;

    public void putByteArray(String key, byte[] value, boolean encrypt)
            throws StorageException;

    public void remove(String key);

    public void copyTo(ICredentialsStore target) throws StorageException;

}
