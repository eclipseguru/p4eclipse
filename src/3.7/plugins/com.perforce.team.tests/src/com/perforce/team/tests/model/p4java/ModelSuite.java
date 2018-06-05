/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.model.p4java;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ ConnectionParametersTest.class, CreateJobTest.class,
        EditClientTest.class, EditConnectionTest.class, EditClientTest.class,
        ErrorHandlerTest.class, LocalRevisionTest.class, P4BranchTest.class,
        P4ChangelistTest.class, P4CollectionTest.class, P4CommandTest.class,
        P4ConnectionTest.class, P4ConnectionManagerTest.class,
        P4DepotTest.class, P4EventTest.class, P4FileTest.class,
        P4FolderTest.class, P4IntegrationOptionsTest.class, P4JobTest.class,
        P4LabelTest.class, P4OperationTest.class,
        P4PendingChangelistTest.class, P4ResourceTest.class,
        P4RevisionTest.class, P4RunnerTest.class, P4StorageTest.class,
        P4WorkspaceTest.class, PermissionsTest.class })
public class ModelSuite {

}
