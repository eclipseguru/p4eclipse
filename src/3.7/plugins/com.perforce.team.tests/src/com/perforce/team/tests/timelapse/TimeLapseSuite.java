/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.timelapse;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ CTimeLapseTest.class, JavaTimeLapseTest.class,
        RubyTimeLapseTest.class, TextTimeLapseTest.class,
        TimeLapseActionTest.class, TimeLapseTest.class, })
public class TimeLapseSuite {

}
