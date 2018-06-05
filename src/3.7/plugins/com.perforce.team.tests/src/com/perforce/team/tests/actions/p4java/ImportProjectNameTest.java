package com.perforce.team.tests.actions.p4java;

import com.perforce.p4java.client.IClient;
import com.perforce.team.core.p4java.builder.P4FileSpecBuilder;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.impl.generic.core.Changelist;
import com.perforce.p4java.impl.generic.core.file.FileSpec;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Folder;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Folder;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.tests.PerforceTestsPlugin;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.PerforceProjectSetSerializer;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.jobs.Job;

/**
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class ImportProjectNameTest extends ConnectionBasedTestCase {

    /**
     * Test correct project name on import
     */
    public void testName() {
        IProject project = null;
        try {
            String path = "//depot/projectName" + System.currentTimeMillis();
            String projectDepotPath = path + "/.project";
            String projectFilePath = path + "/File.txt";
            IP4Connection connection = createConnection();
            IP4Folder projectP4Folder = new P4Folder(connection, null, path);
            assertNotNull(projectP4Folder.getClient());
            assertNotNull(projectP4Folder.getRemotePath());
            projectP4Folder.updateLocation();

            String local = projectP4Folder.getLocalPath();
            assertNotNull(local);

            File projectFile = new File(local, ".project");

            URL fileUrl = PerforceTestsPlugin.getDefault().getBundle()
                    .getEntry("/resources/project1/.project");
            assertNotNull(fileUrl);

            try {
                new File(local).mkdirs();
                assertTrue(projectFile.createNewFile());
                fileUrl = FileLocator.toFileURL(fileUrl);
                P4CoreUtils.copyFile(new File(fileUrl.getFile()), projectFile);
            } catch (Throwable e) {
                e.printStackTrace();
                handle(e);
            }

            File otherFile = new File(local, "File.txt");

            fileUrl = PerforceTestsPlugin.getDefault().getBundle()
                    .getEntry("/resources/project1/File.txt");
            assertNotNull(fileUrl);

            try {
                assertTrue(otherFile.createNewFile());
                fileUrl = FileLocator.toFileURL(fileUrl);
                P4CoreUtils.copyFile(new File(fileUrl.getFile()), otherFile);
            } catch (Throwable e) {
                handle(e);
            }

            IClient client = connection.getClient();
            assertNotNull(client);

            try {
                List<IFileSpec> specs = P4FileSpecBuilder
                        .makeFileSpecList(new String[] { projectFilePath,
                                projectDepotPath });
                for (IFileSpec spec : specs) {
                    ((FileSpec) spec)
                            .setDepotPath(spec.getOriginalPathString());
                }
                List<IFileSpec> addedSpecs = client.addFiles(specs, false, 0,
                        null, false);
                assertNotNull(addedSpecs);
                addedSpecs = P4FileSpecBuilder.getValidFileSpecs(addedSpecs);
                Changelist cl = (Changelist) client.getServer()
                        .getChangelist(0);
                cl.setId(0);
                List<IFileSpec> files = cl.getFiles(false, false);
                files.clear();
                files.addAll(specs);
                cl.setDescription("Test project name submit");
                cl.submit(false);
            } catch (P4JavaException e) {
                handle(e);
            }
            Job importJob = PerforceProjectSetSerializer.createProjects(
                    new P4Collection(new IP4Resource[] { projectP4Folder }),
                    Utils.getShell(), false);
            try {
                importJob.join();
            } catch (InterruptedException e) {
            	e.printStackTrace();
            }

            String correctName = "NameDifferentThanFolder";
            project = ResourcesPlugin.getWorkspace().getRoot()
                    .getProject(correctName);
            assertNotNull(project);
            assertTrue(project.exists());
            assertTrue(project.isAccessible());
            assertTrue(project.isOpen());

            assertEquals(local, project.getLocation().makeAbsolute()
                    .toOSString());

            IFile file = project.getFile("File.txt");
            assertNotNull(file);
            assertTrue(file.exists());
        } finally {
            if (project != null) {
                try {
                    Utils.deleteAndRevert(project);
                } catch (CoreException e) {
                    handle(e);
                }
            }
        }
    }

}
