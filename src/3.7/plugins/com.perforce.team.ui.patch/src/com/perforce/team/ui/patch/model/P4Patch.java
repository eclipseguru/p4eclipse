/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.patch.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.compare.patch.WorkspacePatcherUI;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import com.perforce.p4java.CharsetDefs;
import com.perforce.p4java.core.file.FileAction;
import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Folder;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Resource.Type;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.ui.P4ConnectionManager;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4Patch {

    /**
     * SERVER_DATE_FORMAT
     */
    public static final String SERVER_DATE_FORMAT = "yyyy-MM-dd kk:mm:ss.SSS Z"; //$NON-NLS-1$

    /**
     * PATCH_DATE_FORMAT
     */
    public static final String PATCH_DATE_FORMAT = "yyyy/MM/dd kk:mm:ss"; //$NON-NLS-1$

    /**
     * NULL_DATE
     */
    public static final String NULL_DATE = "1970/01/01 00:00:00"; //$NON-NLS-1$

    /**
     * NULL_PATH
     */
    public static final String NULL_PATH = "/dev/null"; //$NON-NLS-1$

    /**
     * MINUS
     */
    public static final String FILE_MINUS = "---"; //$NON-NLS-1$

    /**
     * PLUS
     */
    public static final String FILE_PLUS = "+++"; //$NON-NLS-1$
    
    /**
     * RANGE
     */
    public static final String LINE_RANGE = "@@"; //$NON-NLS-1$
    
    private boolean async = true;
    private IPatchStream stream;
    private Object[] resources;
    private DateFormat serverFormat;
    private DateFormat patchFormat;

    /**
     * Create a patch
     * 
     * @param stream
     * @param resources
     */
    public P4Patch(IPatchStream stream, Object[] resources) {
        Assert.isNotNull(stream, "Patch stream cannot be null"); //$NON-NLS-1$
        Assert.isNotNull(resources, "Resources cannot be null"); //$NON-NLS-1$
        this.stream = stream;
        this.resources = resources;
        this.serverFormat = new SimpleDateFormat(SERVER_DATE_FORMAT);
        this.serverFormat.setLenient(true);
        this.patchFormat = new SimpleDateFormat(PATCH_DATE_FORMAT);
    }

    /**
     * Set async
     * 
     * @param async
     */
    public void setAsync(boolean async) {
        this.async = async;
    }

    private void addResource(IResource resource, IP4File p4File,
            Map<IProject, List<IP4File>> projects) {
        if (resource != null) {
            IProject project = resource.getProject();
            List<IP4File> resources = projects.get(project);
            if (resources == null) {
                resources = new ArrayList<IP4File>();
                projects.put(project, resources);
            }
            resources.add(p4File);
        }
    }

    private String getRelativePath(String diffPath, IP4Connection connection) {
        String relative = null;
        IP4File file = connection.getFile(diffPath);
        if (file != null) {
            IFile workspaceFile = file.getLocalFileForLocation();
            if (workspaceFile != null) {
                relative = getRelative(workspaceFile);
            }
        }
        return relative;
    }

    private String getRelative(IFile file) {
        String relative = null;
        IPath relativePath = file.getProjectRelativePath();
        if (relativePath != null) {
            relative = relativePath.toPortableString();
        }
        return relative;
    }

    private String getModDate(IFile file) {
        return patchFormat.format(new Date(file.getModificationStamp()));
    }

    private void printIndex(String relativePath, PrintWriter writer) {
        writer.println("Index: " + relativePath); //$NON-NLS-1$
        writer.println("==================================================================="); //$NON-NLS-1$
    }

    private void printDetails(IP4Connection connection, PrintWriter writer) {
        ConnectionParameters params = connection.getParameters();

        writer.print("# P4 Server: "); //$NON-NLS-1$
        writer.print(params.getPort());

        writer.print(" User: "); //$NON-NLS-1$
        writer.print(params.getUser());

        writer.print(" Client: "); //$NON-NLS-1$
        writer.print(params.getClient());

        String charset = params.getCharsetNoNone();
        if (charset != null) {
            writer.print(" Charset: "); //$NON-NLS-1$
            writer.print(charset);
        }

        writer.println();
    }

    private boolean isFileEntry(String line) {
        return isFileStart(line) || line.startsWith(FILE_PLUS);
    }

    private boolean isFileStart(String line) {
        return line.startsWith(FILE_MINUS);
    }

    private int getLineCount(IFile file) throws CoreException, IOException {
        int lines = 0;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(
                    file.getContents(), P4CoreUtils.charsetForName(file.getCharset())));
            String line = reader.readLine();
            while (line != null) {
                lines++;
                line = reader.readLine();
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
        return lines;
    }

    private void printRevision(IP4File file, PrintWriter writer) {
        String path = file.getActionPath(Type.REMOTE);
        if (path != null) {
            writer.print("# " + path); //$NON-NLS-1$
            int have = file.getHaveRevision();
            if (have > 0) {
                writer.print("#" + have); //$NON-NLS-1$
            }
            FileAction action = file.getAction();
            if (action != null) {
                writer.print(' ');
                writer.print('<');
                writer.print(action.toString().toLowerCase(Locale.US));
                writer.print('>');
            }
            writer.println();
        }
    }

    private void printAdd(IP4File file, PrintWriter writer)
            throws CoreException, IOException {
        IFile local = file.getLocalFileForLocation();
        if (local != null) {
            String relative = getRelative(local);
            printIndex(relative, writer);
            printRevision(file, writer);
            writer.println(FILE_MINUS + " " + NULL_PATH + "	" + NULL_DATE); //$NON-NLS-1$ //$NON-NLS-2$
            writer.println(FILE_PLUS + " " + relative + "	" + NULL_DATE); //$NON-NLS-1$ //$NON-NLS-2$

            String line = null;
            BufferedReader reader = null;
            int lines = getLineCount(local);
            
            String end=lines>1?","+lines:"";//$NON-NLS-1$ //$NON-NLS-2$
            writer.println(LINE_RANGE + " " + "-0,0 +1" + end + " " + LINE_RANGE); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            
            try {
                reader = new BufferedReader(new InputStreamReader(
                        local.getContents(), P4CoreUtils.charsetForName(local.getCharset())));
                for (int i = 0; i < lines - 1; i++) {
                    line = reader.readLine();
                    if (line == null) {
                        throw new IOException("Error reading added file"); //$NON-NLS-1$
                    }
                    writer.print('+');
                    writer.println(line);
                }
                int read = reader.read();
                boolean trailingNewline = false;
                if (read != -1) {
                    writer.print('+');
                }
                while (read != -1) {
                    if (read == '\n') {
                        trailingNewline = true;
                    }
                    writer.print((char) read);
                    read = reader.read();
                }
                if (!trailingNewline) {
                    writer.println();
                    writer.println("\\ No newline at end of file"); //$NON-NLS-1$
                }
            } finally {
            	if(reader!=null)
            		reader.close();
            }
        }
    }

    private void printEdit(InputStream diff, PrintWriter writer,
            IP4Connection connection, IP4File file) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(diff,
                CharsetDefs.UTF8));
        try {
            String line = reader.readLine();
            while (line != null) {
                if (isFileEntry(line)) {
                    int firstTab = line.indexOf('\t');
                    if (firstTab > 4) {
                        String path = line.substring(4, firstTab);
                        String relative = getRelativePath(path, connection);
                        if (relative != null) {
                            line = line.replace(path, relative);
                            firstTab = line.indexOf('\t');
                            line = replaceDate(line, firstTab + 1);
                            if (isFileStart(line)) {
                                printIndex(relative, writer);
                                printRevision(file, writer);
                            }
                        }
                    }
                }
                writer.println(line);
                line = reader.readLine();
            }
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    private String convertDate(String date) {
        String converted = null;
        try {
            Date serverDate = serverFormat.parse(date);
            converted = patchFormat.format(serverDate);
        } catch (ParseException e) {
            converted = null;
        }
        return converted;
    }

    private String replaceDate(String line, int dateIndex) {
        String date = line.substring(dateIndex);
        String converted = convertDate(date);
        if (converted != null) {
            return line.replace(date, converted);
        } else {
            return line;
        }
    }

    private void printDelete(IP4File file, PrintWriter writer)
            throws CoreException, IOException {
        IFile local = file.getLocalFileForLocation();
        if (local != null) {
            String relative = getRelative(local);
            printIndex(relative, writer);
            printRevision(file, writer);
            writer.println(FILE_MINUS + " " + relative + "	" //$NON-NLS-1$ //$NON-NLS-2$
                    + getModDate(local));
            writer.println(FILE_PLUS + " " + NULL_PATH + "	" + NULL_DATE); //$NON-NLS-1$ //$NON-NLS-2$

            int lines=0;
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    file.getHaveContents(), ConnectionParameters.getJavaCharset(file)));
            try {
                String line = reader.readLine();
                while (line != null) {
                    lines++;
                    line = reader.readLine();
                }
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    // Ignore
                }
            }

            String end=lines>1?","+lines:"";//$NON-NLS-1$ //$NON-NLS-2$
            writer.println(LINE_RANGE + " " + "-1" + end + " +0,0" + " " + LINE_RANGE); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

            reader = new BufferedReader(new InputStreamReader(
                    file.getHaveContents(), ConnectionParameters.getJavaCharset(file)));
            try {
                String line = reader.readLine();
                while (line != null) {
                    writer.print('-');
                    writer.println(line);
                    line = reader.readLine();
                }
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    private void logCommand(String path) {
        P4Workspace.getWorkspace().getCallback()
                .issuingServerCommand(-1, "diff -du -t " + path); //$NON-NLS-1$
    }

    private void collectorErrorOutput(InputStream stream,
            IErrorCollector collector) {
        StringBuilder errorBuffer = new StringBuilder();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, CharsetDefs.DEFAULT));
        try {
            String line = reader.readLine();
            while (line != null) {
                errorBuffer.append(line).append('\n');
                line = reader.readLine();
            }
        } catch (IOException e) {
            PerforceProviderPlugin.logError(e);
        } finally {
            try {
                reader.close();
            } catch (IOException e1) {
                // Ignore
            }
        }
        if (errorBuffer.length() > 0) {
            collector.collect(new PatchException(errorBuffer.toString()));
        }
    }

    private void generateDiffs(List<IP4File> resources,
            final PrintWriter writer, IProgressMonitor monitor,
            IErrorCollector errorCollector) {
        if (!resources.isEmpty()) {
            final IP4Connection connection = resources.get(0).getConnection();
            if (!connection.isOffline()) {

                monitor.subTask(Messages.P4Patch_AddingPerforceMetadata);
                printDetails(connection, writer);

                PatchDiffRunner runner = null;
                try {
                    List<IP4File> edits = new ArrayList<IP4File>();
                    for (IP4File file : resources) {
                        if (file.openedForAdd()) {
                            monitor.subTask(file.getActionPath(Type.REMOTE));
                            printAdd(file, writer);
                            monitor.worked(1);
                        } else if (file.openedForDelete()) {
                            monitor.subTask(file.getActionPath(Type.REMOTE));
                            printDelete(file, writer);
                            monitor.worked(1);
                        } else if (file.openedForEdit()) {
                            edits.add(file);
                        }
                    }
                    runner = new PatchDiffRunner(connection);
                    for (final IP4File file : edits) {
                        String actionPath = file.getActionPath(Type.REMOTE);
                        if (actionPath != null) {
                            monitor.subTask(actionPath);
                            logCommand(actionPath);

                            Process process = runner.run(actionPath);

                            printEdit(process.getInputStream(), writer,
                                    connection, file);

                            collectorErrorOutput(process.getErrorStream(),
                                    errorCollector);

                            process.waitFor();
                        }
                        monitor.worked(1);
                    }
                } catch (IOException e) {
                    errorCollector.collect(e);
                    PerforceProviderPlugin.logError(e);
                } catch (CoreException e) {
                    errorCollector.collect(e);
                    PerforceProviderPlugin.logError(e);
                } catch (InterruptedException e) {
                    errorCollector.collect(e);
                    PerforceProviderPlugin.logError(e);
                } finally {
                    if (runner != null) {
                        runner.dispose();
                    }
                }
            }
        }
    }

    private Map<IProject, List<IP4File>> generateProjectGroups(IP4File[] files) {
        Map<IProject, List<IP4File>> projects = new HashMap<IProject, List<IP4File>>();
        for (IP4File resource : files) {
            IFile file = resource.getLocalFileForLocation();
            addResource(file, resource, projects);
        }
        return projects;
    }

    private IP4File[] getAllFiles() {
        List<IP4File> files = new ArrayList<IP4File>();
        IP4File file = null;
        for (Object resource : resources) {
            if (resource instanceof IResource) {
                resource = P4ConnectionManager.getManager().getResource(
                        (IResource) resource);
            }
            if (resource instanceof IP4Folder) {
                IP4Folder folder = (IP4Folder) resource;
                for (IP4File localFile : folder.getAllLocalFiles()) {
                    files.add(localFile);
                }
            } else if (resource instanceof IP4PendingChangelist) {
                IP4PendingChangelist list = (IP4PendingChangelist) resource;
                for (IP4Resource member : list.getFiles()) {
                    file = P4CoreUtils.convert(member, IP4File.class);
                    if (file != null) {
                        files.add(file);
                    }
                }
            } else {
                file = P4CoreUtils.convert(resource, IP4File.class);
                if (file != null) {
                    files.add(file);
                }
            }
        }
        return files.toArray(new IP4File[files.size()]);
    }

    /**
     * Create workspace operation for patch creation
     * 
     * @param collector
     * @return operation
     */
    protected WorkspaceModifyOperation createOperation(
            final IErrorCollector collector) {
        final WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {

            @Override
            protected void execute(IProgressMonitor monitor)
                    throws CoreException, InvocationTargetException,
                    InterruptedException {
                monitor.beginTask("", 2); //$NON-NLS-1$

                SubProgressMonitor refreshMonitor = new SubProgressMonitor(
                        monitor, 1);
                refreshMonitor.beginTask("", 1); //$NON-NLS-1$
                refreshMonitor.subTask(Messages.P4Patch_RefreshingFiles);
                IP4File[] files = getAllFiles();
                if (refreshMonitor.isCanceled()) {
                    return;
                }
                refreshMonitor.done();
                Map<IProject, List<IP4File>> groups = generateProjectGroups(files);

                SubProgressMonitor patchMonitor = new SubProgressMonitor(
                        monitor, 1);
                patchMonitor.beginTask("", files.length); //$NON-NLS-1$
                PrintWriter writer = null;
                try {
                    stream.initialize(patchMonitor);
                    if (patchMonitor.isCanceled()) {
                        return;
                    }
                    writer = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(stream.getStream(),
                                    stream.getCharset())), true);
                    writer.println(WorkspacePatcherUI.getWorkspacePatchHeader());
                    writer.println("### Generated using the 'p4 diff -du' command"); //$NON-NLS-1$
                    for (Map.Entry<IProject, List<IP4File>> entry : groups.entrySet()) {
                    	IProject project=entry.getKey();
                        monitor.subTask(project.getName());
                        writer.println(WorkspacePatcherUI
                                .getWorkspacePatchProjectHeader(project));
                        generateDiffs(entry.getValue(), writer,
                                patchMonitor, collector);
                    }
                } catch (IOException e) {
                    collector.collect(e);
                    PerforceProviderPlugin.logError(e);
                } finally {
                    if (writer != null) {
                        writer.close();
                    }
                    try {
                        stream.finish(patchMonitor);
                    } catch (IOException e) {
                        PerforceProviderPlugin.logError(e);
                    }
                    patchMonitor.done();
                    monitor.done();
                }

            }
        };
        return operation;
    }

    /**
     * Generate the patch
     * 
     * @param collector
     */
    public void generate(IErrorCollector collector) {
        final IErrorCollector errorCollector = ErrorCollector
                .collectorFor(collector);
        final WorkspaceModifyOperation operation = createOperation(errorCollector);
        IP4Runnable runnable = new P4Runnable() {

            @Override
            public String getTitle() {
                return Messages.P4Patch_GeneratingPatch;
            }

            @Override
            public void run(IProgressMonitor monitor) {
                try {
                    operation.run(monitor);
                } catch (InvocationTargetException e) {
                    PerforceProviderPlugin.logError(e);
                } catch (InterruptedException e) {
                    PerforceProviderPlugin.logError(e);
                } finally {
                    errorCollector.done();
                }
            }

        };
        if (async) {
            P4Runner.schedule(runnable);
        } else {
            runnable.run(new NullProgressMonitor());
        }
    }
}
