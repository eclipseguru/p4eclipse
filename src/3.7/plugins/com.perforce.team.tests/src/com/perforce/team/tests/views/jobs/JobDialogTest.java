/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.views.jobs;

import com.perforce.p4java.core.IJobSpec.IJobSpecField;
import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.jobs.EditJobDialog;
import com.perforce.team.ui.jobs.NewJobDialog;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class JobDialogTest extends ConnectionBasedTestCase {

    /**
     * @see com.perforce.team.tests.ConnectionBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        createJob();
        addDepotFile(createConnection().getClient(),
                "//depot/JobDialogTest/test.txt");
    }

    private void checkFields(Widget[] fields) {
        assertNotNull(fields);
        assertTrue(fields.length > 0);

        for (Widget field : fields) {
            Object data = field.getData();
            assertNotNull(data);
            assertTrue(data instanceof IJobSpecField);
            IJobSpecField jobField = (IJobSpecField) data;
            if (IP4Job.SELECT_DATA_TYPE.equals(jobField.getDataType())) {
                assertTrue(field instanceof Combo);
                Combo combo = (Combo) field;
                assertTrue(combo.getItemCount() > 0);
                assertTrue(combo.getText().length() > 0);
            } else {
                assertTrue(field instanceof Text);
                Text text = (Text) field;
                if (IP4Job.ALWAYS_FIELD_TYPE.equals(jobField.getFieldType())) {
                    assertTrue((text.getStyle() & SWT.READ_ONLY) != 0);
                }
                if (IP4Job.WORD_DATA_TYPE.equals(jobField.getDataType())
                        || IP4Job.LINE_DATA_TYPE.equals(jobField.getDataType())
                        || IP4Job.DATE_DATA_TYPE.equals(jobField.getDataType())) {
                    assertEquals(0, text.getStyle() & SWT.MULTI);
                } else if (IP4Job.TEXT_DATA_TYPE.equals(jobField.getDataType())) {
                    assertTrue((text.getStyle() & SWT.MULTI) != 0);
                }
            }
        }
    }

    /**
     * Test the job add dialog
     */
    public void testAddDialog() {
        IP4Connection connection = createConnection();
        IP4Job job = connection.getJob("");
        assertNotNull("Error retrieving default job template", job);
        NewJobDialog dialog = new NewJobDialog(P4UIUtils.getShell(),
                connection, job);
        dialog.setBlockOnOpen(false);
        dialog.open();
        checkFields(dialog.getFields());
        Map<String, Object> fields = new HashMap<String, Object>();
        dialog.loadFields(fields);
        assertFalse(fields.isEmpty());
        for (String key : fields.keySet()) {
            Object value = fields.get(key);
            assertNotNull(value);
        }
        assertTrue(dialog.cancel());
        assertFalse(dialog.save());
        dialog.close();
        assertNull(dialog.getCreatedJob());
        assertNotNull(dialog.getAddedChangelists());
        assertEquals(0, dialog.getAddedChangelists().length);
    }

    /**
     * Test the job edit dialog
     */
    public void testEditDialog() {
        IP4Connection connection = createConnection();
        IP4Job[] jobs = connection.getJobs(1);
        assertNotNull(jobs);
        assertEquals(1, jobs.length);
        IP4Job job = jobs[0];
        assertNotNull(job);
        EditJobDialog dialog = new EditJobDialog(P4UIUtils.getShell(), job,
                null);
        dialog.setBlockOnOpen(false);
        dialog.open();
        checkFields(dialog.getFields());
        assertNull(dialog.getUpdatedJob());
        assertNull(dialog.getAddedChangelists());
        assertNull(dialog.getRemovedChangelists());
        dialog.updateChanges();
        assertNotNull(dialog.getUpdatedJob());
        assertNotNull(dialog.getAddedChangelists());
        assertEquals(0, dialog.getAddedChangelists().length);
        assertNotNull(dialog.getRemovedChangelists());
        assertEquals(0, dialog.getRemovedChangelists().length);
        Map<String, Object> fields = new HashMap<String, Object>();
        dialog.loadFields(fields);
        assertFalse(fields.isEmpty());
        for (String key : fields.keySet()) {
            Object value = fields.get(key);
            assertNotNull(value);
        }
        assertTrue(dialog.cancel());
        assertTrue(dialog.save());
        dialog.close();
    }

    /**
     * Test show changelist associated with a job
     */
    public void testShowChangelist() {
        IP4Connection connection = createConnection();
        IP4Job job = null;
        IP4SubmittedChangelist list = null;
        EditJobDialog dialog = null;

        try {
            IP4Job[] jobs = connection.getJobs(1);
            assertNotNull(jobs);
            assertEquals(1, jobs.length);
            job = jobs[0];
            assertNotNull(job);
            IP4SubmittedChangelist[] lists = connection
                    .getSubmittedChangelists(1);
            assertNotNull(lists);
            assertEquals(1, lists.length);
            list = lists[0];
            assertNotNull(list);
            assertFalse(Arrays.asList(list.getJobs()).contains(jobs));
            new P4Collection(new IP4Resource[] { job }).fix(list);
            dialog = new EditJobDialog(P4UIUtils.getShell(), job, null);
            dialog.setBlockOnOpen(false);
            dialog.open();
            checkFields(dialog.getFields());
            dialog.updateChanges();
            Map<String, Object> fields = new HashMap<String, Object>();
            dialog.loadFields(fields);
            assertFalse(fields.isEmpty());
            for (String key : fields.keySet()) {
                Object value = fields.get(key);
                assertNotNull(value);
            }
        } finally {
            if (dialog != null) {
                dialog.close();
            }
            if (list != null && job != null) {
                new P4Collection(new IP4Resource[] { job }).unfix(list);
            }
        }
    }

    /**
     * Test add a changelist
     */
    public void testAddChangelist() {
        IP4Connection connection = createConnection();
        IP4Job job = null;
        IP4SubmittedChangelist list = null;
        EditJobDialog dialog = null;

        try {
            IP4Job[] jobs = connection.getJobs(1);
            assertNotNull(jobs);
            assertEquals(1, jobs.length);
            job = jobs[0];
            assertNotNull(job);
            IP4SubmittedChangelist[] lists = connection
                    .getSubmittedChangelists(1);
            assertNotNull(lists);
            assertEquals(1, lists.length);
            list = lists[0];
            assertNotNull(list);
            assertFalse(Arrays.asList(list.getJobs()).contains(jobs));
            dialog = new EditJobDialog(P4UIUtils.getShell(), job, null);
            dialog.setBlockOnOpen(false);
            dialog.open();
            checkFields(dialog.getFields());

            assertNotNull(dialog.getChangelistViewer());
            dialog.addChangelist(list, true);

            dialog.updateChanges();

            assertNotNull(dialog.getAddedChangelists());
            assertEquals(1, dialog.getAddedChangelists().length);
            assertEquals(list, dialog.getAddedChangelists()[0]);

        } finally {
            if (dialog != null) {
                dialog.close();
            }
        }
    }

    /**
     * Test remove a changelist
     */
    public void testRemoveChangelist() {
        IP4Connection connection = createConnection();
        IP4Job job = null;
        IP4SubmittedChangelist list = null;
        EditJobDialog dialog = null;

        try {
            IP4Job[] jobs = connection.getJobs(1);
            assertNotNull(jobs);
            assertEquals(1, jobs.length);
            job = jobs[0];
            assertNotNull(job);
            IP4SubmittedChangelist[] lists = connection
                    .getSubmittedChangelists(1);
            assertNotNull(lists);
            assertEquals(1, lists.length);
            list = lists[0];
            assertNotNull(list);
            assertFalse(Arrays.asList(list.getJobs()).contains(jobs));
            new P4Collection(new IP4Resource[] { job }).fix(list);
            IP4Changelist[] fixes = connection.getFixes(job);
            dialog = new EditJobDialog(P4UIUtils.getShell(), job, fixes);
            dialog.setBlockOnOpen(false);
            dialog.open();
            checkFields(dialog.getFields());

            assertNotNull(dialog.getChangelistViewer());
            dialog.getChangelistViewer().setChecked(list, false);

            dialog.updateChanges();

            assertNotNull(dialog.getRemovedChangelists());
            assertEquals(1, dialog.getRemovedChangelists().length);
            assertEquals(list, dialog.getRemovedChangelists()[0]);

        } finally {
            if (dialog != null) {
                dialog.close();
            }
            if (list != null && job != null) {
                new P4Collection(new IP4Resource[] { job }).unfix(list);
            }
        }
    }
}
