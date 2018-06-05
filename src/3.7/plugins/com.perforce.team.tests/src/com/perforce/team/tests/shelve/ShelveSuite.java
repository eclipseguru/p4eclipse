/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.shelve;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ ShelveDialogTest.class, ShelveTest.class,
        ShelveViewTest.class, UnshelveTest.class })
public class ShelveSuite {

}
