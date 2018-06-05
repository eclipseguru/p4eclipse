package com.perforce.team.tests.unicode;

import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.p4java.actions.RemoveAction;
import com.perforce.team.ui.p4java.actions.SyncRevisionAction;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.eclipse.jface.viewers.StructuredSelection;

/**
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public abstract class UnicodeTestCase extends ConnectionBasedTestCase {

    /**
     * Path of file that will be translated with {@link #getP4Charset()}
     * 
     * @return - depot path of file to translate
     */
    protected abstract String getTranslatePath();

    /**
     * Path of binary file that is the translated equivalent of
     * {@link #getTranslatePath()} when encoded with {@link #getP4Charset()}
     * 
     * @return - depot path of binary translated file
     */
    protected abstract String getRawPath();

    /**
     * Get p4 charset to use
     * 
     * @return - p4 charset
     */
    protected abstract String getP4Charset();

    /**
     * Get revision to sync to
     * 
     * @return - raw rev number
     */
    protected int getRawRevision() {
        return -1;
    }

    /**
     * Get revision to sync to
     * 
     * @return - trans rev number
     */
    protected int getTransRevision() {
        return -1;
    }

    /**
     * Creates the parameters
     */
    @Override
    protected void initParameters() {
        assertNotNull(System.getProperty("p4.client.unicode"));
        assertNotNull(System.getProperty("p4.user.unicode"));
        assertNotNull(System.getProperty("p4.password.unicode"));
        assertNotNull(System.getProperty("p4.port.unicode"));
        String charset = getP4Charset();
        assertNotNull(charset);
        parameters = new ConnectionParameters();
        parameters.setClient(System.getProperty("p4.client.unicode"));
        parameters.setUser(System.getProperty("p4.user.unicode"));
        parameters.setPort(System.getProperty("p4.port.unicode"));
        parameters.setPassword(System.getProperty("p4.password.unicode"));
        parameters.setCharset(charset);
    }

    /**
     * @see com.perforce.team.tests.P4TestCase#useRpc()
     */
    @Override
    protected boolean useRpc() {
        return true;
    }

    /**
     * Test translation
     */
    public void testTranslation() {
        IP4Connection connection = createConnection();

        String raw = getRawPath();
        assertNotNull(raw);
        String trans = getTranslatePath();
        assertNotNull(trans);

        IP4File rawFile = connection.getFile(raw);
        assertNotNull(rawFile);
        assertNotNull(rawFile.getLocalPath());

        IP4File transFile = connection.getFile(trans);
        assertNotNull(transFile);
        assertNotNull(transFile.getLocalPath());

        P4Collection collection = new P4Collection(new IP4Resource[] { rawFile,
                transFile });
        try {

            RemoveAction remove = new RemoveAction();
            remove.setAsync(false);
            RemoveAction.setNeedConfirm(false);
            remove.selectionChanged(null, new StructuredSelection(new Object[] {
                    rawFile, transFile }));
            remove.run(null);

            assertEquals(0, rawFile.getHaveRevision());
            assertEquals(0, transFile.getHaveRevision());

            SyncRevisionAction sync = new SyncRevisionAction();
            sync.setAsync(false);
            sync.selectionChanged(null, new StructuredSelection(
                    new Object[] { rawFile }));
            int rev = getRawRevision();
            if (rev > 0) {
                sync.runAction(Integer.toString(rev));
            } else {
                sync.runAction("#head");
            }

            sync.selectionChanged(null, new StructuredSelection(
                    new Object[] { transFile }));
            rev = getTransRevision();
            if (rev > 0) {
                sync.runAction(Integer.toString(rev));
            } else {
                sync.runAction("#head");
            }

            assertTrue(rawFile.isSynced());
            assertTrue(transFile.isSynced());

            try {
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                assertNotNull(md5);
                byte[] rawBytes = Utils.getContent(
                        new FileInputStream(rawFile.getLocalPath())).getBytes();
                assertTrue(rawBytes.length > 0);
                md5.update(rawBytes);
                byte[] rawDigest = md5.digest();
                byte[] transBytes = Utils.getContent(
                        new FileInputStream(transFile.getLocalPath()))
                        .getBytes();
                assertTrue(transBytes.length > 0);
                md5.update(transBytes);
                byte[] transDigest = md5.digest();

                assertTrue(Arrays.equals(rawDigest, transDigest));
                assertEquals(rawBytes.length, transBytes.length);
            } catch (NoSuchAlgorithmException e) {
                handle("MD5 not found", e);
            } catch (FileNotFoundException e) {
                handle("File not found", e);
            } catch (Exception e) {
                handle(e);
            }

            collection.edit();
            assertTrue(rawFile.openedForEdit());
            assertTrue(transFile.openedForEdit());
            P4Collection unchanged = collection.previewUnchangedRevert();
            assertNotNull(unchanged);
            IP4Resource[] members = unchanged.members();
            assertEquals(2, members.length);
            if (rawFile.equals(members[0])) {
                assertEquals(transFile, members[1]);
            } else {
                assertEquals(transFile, members[0]);
                assertEquals(rawFile, members[1]);
            }
        } finally {
            collection.revert();
        }

    }
}
