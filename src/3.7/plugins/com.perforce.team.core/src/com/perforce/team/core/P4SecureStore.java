package com.perforce.team.core;

import java.io.IOException;

import org.eclipse.equinox.security.storage.EncodingUtils;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;

/**
 * This class wraps the Eclipse secure store. The store is identified by a URL.
 */
public class P4SecureStore implements ICredentialsStore {

    private static final String ID_PLUGIN = "com.perforce.team.core"; //$NON-NLS-1$

    private final String url; // URL locates the node where credentials are
                              // stored

    public static final ICredentialsStore INSTANCE = new P4SecureStore(
            "default"); //$NON-NLS-1$

    private P4SecureStore(String url) {
        this.url = url;
    }

    public void clear() {
        getSecurePreferences().removeNode();
    }

    public void flush() throws IOException {
        getSecurePreferences().flush();
    }

    public String get(String key, String def) throws StorageException {
        return getSecurePreferences().get(key, def);
    }

    public byte[] getByteArray(String key, byte[] def) throws StorageException {
        return getSecurePreferences().getByteArray(key, def);
    }

    private ISecurePreferences getSecurePreferences() {
        ISecurePreferences securePreferences = SecurePreferencesFactory
                .getDefault().node(ID_PLUGIN);
        securePreferences = securePreferences.node(EncodingUtils
                .encodeSlashes(getUrl()));
        return securePreferences;
    }

    public String getUrl() {
        return url;
    }

    public String[] keys() {
        return getSecurePreferences().keys();
    }

    public void put(String key, String value, boolean encrypt)
            throws StorageException {
        getSecurePreferences().put(key, value, encrypt);
    }

    public void putByteArray(String key, byte[] value, boolean encrypt)
            throws StorageException {
        getSecurePreferences().putByteArray(key, value, encrypt);
    }

    public void remove(String key) {
        getSecurePreferences().remove(key);
    }

    public void copyTo(ICredentialsStore target) throws StorageException {
        ISecurePreferences preferences = getSecurePreferences();
        for (String key : preferences.keys()) {
            target.put(key, preferences.get(key, null),
                    preferences.isEncrypted(key));
        }
    }

}
