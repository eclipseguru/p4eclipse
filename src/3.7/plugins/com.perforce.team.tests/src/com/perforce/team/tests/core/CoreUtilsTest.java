/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.core;

import com.perforce.p4api.PerforceFileAccess;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.PerforceConnectionFactory;
import com.perforce.team.core.p4java.P4Storage;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.ui.LabelProviderAdapter;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceStringMatcher;
import com.perforce.team.ui.timelapse.TimeLapseUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class CoreUtilsTest extends ProjectBasedTestCase {

    /**
     * Test getting files
     */
    public void testGetFiles() {
        List<IFile> files = P4CoreUtils.getAllFiles(null);
        assertNotNull(files);
        assertTrue(files.isEmpty());
        files = P4CoreUtils.getAllFiles(new IResource[] { project });
        assertNotNull(files);
        assertFalse(files.isEmpty());
    }

    /**
     * Test getting children
     */
    public void testGetChildren() {
        List<IFile> files = P4CoreUtils.getAllChildren(null);
        assertNotNull(files);
        assertTrue(files.isEmpty());
        files = P4CoreUtils.getAllChildren(project);
        assertNotNull(files);
        assertFalse(files.isEmpty());
    }

    /**
     * Test getting children from a non-existent container
     */
    public void testGetChildrenFailure() {
        IFolder folder = project.getFolder("invalid_folder");
        List<IFile> files = P4CoreUtils.getAllChildren(folder);
        assertNotNull(files);
        assertTrue(files.isEmpty());
    }

    /**
     * Simple test of position class
     */
    public void testPositionClass() {
        int start = 2;
        int end = 5;
        PerforceStringMatcher.Position position = new PerforceStringMatcher.Position(
                start, end);
        assertEquals(start, position.getStart());
        assertEquals(end, position.getEnd());
    }

    /**
     * Test PerforceFileAccess name parsing
     */
    public void testFilenameParsing() {

        // Test default constructor doesn't thrown an exception
        assertNotNull(new PerforceFileAccess());

        String name1 = "name1";
        String path1 = "/dir1/dir2/" + name1;
        String parsed1 = PerforceFileAccess.getFilename(path1);
        assertNotNull(parsed1);
        assertEquals(name1, parsed1);

        String name2 = "name2";
        String path2 = "C:\\dir1\\dir2\\" + name2;
        String parsed2 = PerforceFileAccess.getFilename(path2);
        assertNotNull(parsed2);
        assertEquals(name2, parsed2);

    }

    /**
     * Test PerforceFileAccess folder parsing
     */
    public void testFolderParsing() {

        String name1 = "name1";
        String dir1 = "/dir1/dir2";
        String path1 = dir1 + "/" + name1;
        String parsed1 = PerforceFileAccess.getFolder(path1);
        assertNotNull(parsed1);
        assertEquals(dir1, parsed1);

        String name2 = "name2";
        String dir2 = "C:\\dir1\\dir2";
        String path2 = dir2 + "\\" + name2;
        String parsed2 = PerforceFileAccess.getFolder(path2);
        assertNotNull(parsed2);
        assertEquals(dir2, parsed2);
    }

    /**
     * Test path formatting
     */
    public void testPathFormatting() {
        String at = "/dir1/dir2/test@1.txt";
        String hash = "/dir1/dir2/t#est2.txt";
        String asterix = "/dir1/dir2/te*st3.txt";
        String normal = "/dir1/dir2/test4.txt";
        String percent = "/dir1/dir2/test5%.txt";

        String[] output = PerforceConnectionFactory
                .formatFilenames(new String[] { at, hash, asterix, normal,
                        percent });
        assertNotNull(output);
        assertEquals(5, output.length);

        assertNotNull(output[0]);
        assertFalse(at.equals(output[0]));
        assertFalse(output[0].contains("@"));

        assertNotNull(output[1]);
        assertFalse(hash.equals(output[1]));
        assertFalse(output[1].contains("#"));

        assertNotNull(output[2]);
        assertFalse(asterix.equals(output[2]));
        assertFalse(output[2].contains("*"));

        assertNotNull(output[3]);
        assertEquals(normal, output[3]);

        assertNotNull(output[4]);
        assertFalse(percent.equals(output[4]));
    }

    /**
     * Test special char detection
     */
    public void testSpecialChars() {
        String at = "//depot/test@1.txt";
        String hash = "//depot/t#est2.txt";
        String asterix = "//depot/te*st3.txt";
        String normal = "//depot/test4.txt";
        String percent = "//depot/test5%.txt";
        assertFalse(PerforceConnectionFactory.hasSpecialChars(null));
        assertTrue(PerforceConnectionFactory.hasSpecialChars(at));
        assertTrue(PerforceConnectionFactory.hasSpecialChars(hash));
        assertTrue(PerforceConnectionFactory.hasSpecialChars(asterix));
        assertTrue(PerforceConnectionFactory.hasSpecialChars(percent));
        assertFalse(PerforceConnectionFactory.hasSpecialChars(normal));
    }

    /**
     * Test file copying
     */
    public void testCopy() {
        try {
            String sourceContent = "source file";
            File source = File.createTempFile("source", null);
            PrintWriter writer = new PrintWriter(source);
            writer.print(sourceContent);
            writer.flush();
            writer.close();
            File destination = File.createTempFile("destination", null);
            P4CoreUtils.copyFile(source, destination);
            BufferedReader reader = new BufferedReader(new FileReader(
                    destination));
            String content = reader.readLine();
            reader.close();
            assertEquals(sourceContent, content);
        } catch (Exception e) {
            assertFalse("Exception thrown: " + e.getMessage(), true);
        }
    }

    /**
     * Test {@link LabelProviderAdapter}
     */
    public void testLabelProviderAdapter() {
        LabelProviderAdapter adapter = new LabelProviderAdapter() {

            public String getText(Object element) {
                return "";
            }
        };
        assertNull(adapter.getImage(null));
        assertFalse(adapter.isLabelProperty(null, null));
        try {
            adapter.addListener(null);
            adapter.removeListener(null);
            adapter.dispose();
        } catch (Throwable e) {
            handle(e);
        }
    }

    /**
     * Test clipboard copying
     */
    public void testClipboard() {
        String copy = "text to copy to the clipboard";
        P4UIUtils.copyToClipboard(copy);
        Clipboard cp = new Clipboard(P4UIUtils.getDisplay());
        Object contents = cp.getContents(TextTransfer.getInstance());
        assertNotNull(contents);
        assertEquals(copy, contents);
    }

    /**
     * Test resource disposal
     */
    public void testDisposal() {
        Shell shell = new Shell(P4UIUtils.getDisplay());
        Color color = new Color(P4UIUtils.getDisplay(), new RGB(128, 128, 128));
        P4UIUtils.registerDisposal(shell, color);
        assertFalse(color.isDisposed());
        shell.dispose();
        assertTrue(color.isDisposed());
    }

    /**
     * Test content type lookup
     */
    public void testContentType() {
        assertNotNull(P4UIUtils.getDefaultTextDescriptor());
        assertNotNull(P4UIUtils.getContentType(new P4Storage() {

            @Override
            public String getName() {
                return "test.txt";
            }

            public InputStream getContents() throws CoreException {
                return new ByteArrayInputStream(new String("test text")
                        .getBytes());
            }
        }));
        assertNotNull(P4UIUtils.getDescriptor("Test.java", null));
    }

    /**
     * Test date formatting
     */
    public void testDate() {
        assertNotNull(P4UIUtils.getDateText(123456));
        assertNotNull(TimeLapseUtils.format(121212));
        assertNotNull(TimeLapseUtils.format(new Date(656521)));
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p07.2/p4-eclipse/com.perforce.team.plugin";
    }

}
