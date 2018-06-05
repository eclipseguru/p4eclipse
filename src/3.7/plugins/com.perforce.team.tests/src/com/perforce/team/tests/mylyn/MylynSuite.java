/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.mylyn;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ BulkJobEditorTest.class, ConfigurationTest.class,
        ConnectorTest.class, JobTaskTest.class, MessagesTest.class,
        ProxyTest.class, TaskLinkTest.class, UtilTest.class })
public class MylynSuite {

}
