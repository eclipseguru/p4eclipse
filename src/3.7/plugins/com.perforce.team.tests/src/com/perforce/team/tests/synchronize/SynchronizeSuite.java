/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.synchronize;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ CommitOperationTest.class, HistoryOperationTest.class,
        PerforceSubscriberTest.class, ReopenOperationTest.class,
        ResolveModelOperationTest.class, RevertModelOperationTest.class,
        SyncWizardTest.class, TeamSynchronizeActionTest.class,
        UpdateModelOperationTest.class })
public class SynchronizeSuite {

}
