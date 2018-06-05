/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.actions.p4java;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ AddActionTest.class, AddIgnoreActionTest.class,
        AuthenticationActionTest.class, ChangeFiletypeActionTest.class,
        CheckConsistencyActionTest.class, DeleteActionTest.class,
        DeleteChangelistActionTest.class, EditActionTest.class,
        EditAdaptedActionTest.class, EditChangelistActionTest.class,
        EditJobActionTest.class, FilePropertiesActionTest.class,
        ImportProjectNameTest.class, ImportProjectTest.class,
        IntegrateActionTest.class, LabelFilesTest.class,
        ManualResolveActionTest.class, MoveToAnotherChangelistActionTest.class,
        NewChangelistActionTest.class, NewJobActionTest.class,
        NewServerActionTest.class, OpenWildcardTest.class,
        RecoverDeleteActionTest.class, RemoveActionTest.class,
        RemoveServerActionTest.class, ResolveTest.class,
        RevertActionTest.class, RevertAllActionTest.class,
        ScheduleResolveActionTest.class, ServerInfoActionTest.class,
        ServerPropertiesActionTest.class, ShareProjectsActionTest.class,
        SubmitActionTest.class, SyncPreviewActionTest.class,
        UnmanageActionTest.class, WorkOfflineActionTest.class,
        WorkOnlineActionTest.class })
public class ActionSuite {

}
