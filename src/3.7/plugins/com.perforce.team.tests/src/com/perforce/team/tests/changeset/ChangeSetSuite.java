/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.changeset;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ ChangeSetManagerTest.class, PendingChangeSetTest.class,
        SubmittedChangeSetTest.class })
public class ChangeSetSuite {

}
