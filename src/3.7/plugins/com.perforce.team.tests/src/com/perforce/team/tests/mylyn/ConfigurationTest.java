/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.mylyn;

import com.perforce.team.core.mylyn.IP4JobConfiguration;
import com.perforce.team.core.mylyn.IP4MylynConstants;
import com.perforce.team.core.mylyn.P4JobConfigurationManager;
import com.perforce.team.tests.P4TestCase;
import com.perforce.team.ui.mylyn.IP4JobUiConfiguration;
import com.perforce.team.ui.mylyn.P4JobUiConfigurationManager;

import org.eclipse.mylyn.tasks.core.TaskRepository;

import junit.framework.AssertionFailedError;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ConfigurationTest extends P4TestCase {

    /**
     * Test default core configuration
     */
    public void testDefaultCore() {
        P4JobConfigurationManager manager = new P4JobConfigurationManager();
        IP4JobConfiguration config = manager.getConfiguration(null);
        assertNotNull(config);
        assertTrue(config.isConfigurationFor(null));
        try {
            config.isCommentField(null);
            config.isConfigurationFor(null);
            config.isCreatedDateField(null, null);
            config.isKindField(null);
            config.isModifiedDateField(null, null);
            config.isPriorityField(null);
        } catch (Throwable e) {
            handle(e);
        }
    }

    /**
     * Test core configuration extension point
     */
    public void testCoreExtension() {
        P4JobConfigurationManager manager = new P4JobConfigurationManager();
        TaskRepository repo = new TaskRepository(IP4MylynConstants.KIND,
                "testtest:1234");
        IP4JobConfiguration config = manager.getConfiguration(repo);
        assertNotNull(config);
        assertTrue(config instanceof TestCoreConfiguration);
    }

    /**
     * Test default ui configuration
     */
    public void testDefaultUi() {
        P4JobUiConfigurationManager manager = new P4JobUiConfigurationManager();
        IP4JobUiConfiguration config = manager.getConfiguration(null);
        assertNotNull(config);
        assertTrue(config.isConfigurationFor(null));
        try {
            assertNotNull(config.getTaskKindLabel(null));
            assertNull(config.getTaskKindOverlay(null));
            assertNull(config.getTaskPriorityOverlay(null));
        } catch (Throwable e) {
            if (!(e instanceof AssertionFailedError)) {
                handle(e);
            }
        }
    }

    /**
     * Test ui configuration extension point
     */
    public void testUiExtension() {
        P4JobUiConfigurationManager manager = new P4JobUiConfigurationManager();
        TaskRepository repo = new TaskRepository(IP4MylynConstants.KIND,
                "testtest:1234");
        IP4JobUiConfiguration config = manager.getConfiguration(repo);
        assertNotNull(config);
        assertTrue(config instanceof TestUiConfiguration);
    }

}
