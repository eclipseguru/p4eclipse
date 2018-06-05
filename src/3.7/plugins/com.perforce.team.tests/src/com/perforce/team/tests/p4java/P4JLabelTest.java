package com.perforce.team.tests.p4java;

import com.perforce.p4java.core.ILabel;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Label;
import com.perforce.team.tests.ConnectionBasedTestCase;

/**
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class P4JLabelTest extends ConnectionBasedTestCase {

    /**
     * Test refresh
     */
    public void testRefresh() {
        IP4Connection connection = createConnection();
        IP4Label[] labels = connection.getLabels(1);
        assertNotNull(labels);
        assertEquals(1, labels.length);

        ILabel label = labels[0].getLabel();
        assertNotNull(label);

        assertFalse(label.canRefresh());
    }

}
