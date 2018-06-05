/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.core;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ AutoResolveTest.class, 
        ClientBuilderTest.class, ConfigWizardTest.class,
        ConnectionMappedExceptionTest.class, CoreUtilsTest.class,
        DecoratorTest.class, DiffHaveTest.class, DiffTest.class,
        DragDataTest.class, FileAdapterFactoryTest.class,
        FileModificationManagerTest.class, FixingTest.class,
        IgnoredFilesTest.class, LiveSubmitTest.class,
        LocalLabelProviderTest.class, LockingTest.class, LogListenerTest.class,
        MessagesTest.class, OpenEditorActionTest.class,
        OtherDecoratorTest.class, PerforceContentProviderTest.class,
        PerforceLabelProviderTest.class, PerforcePerspectiveTest.class,
        PerforceStatusTest.class, ProviderPluginTest.class,
        RefreshActionTest.class, RevertDigestTest.class,
        SameClientDecoratorTest.class, SessionManagerTest.class,
        SubmitTest.class, SyncTest.class, UtilityTest.class, })
public class CoreSuite {

}
