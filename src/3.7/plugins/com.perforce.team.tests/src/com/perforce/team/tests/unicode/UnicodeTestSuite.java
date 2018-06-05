package com.perforce.team.tests.unicode;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class UnicodeTestSuite {

    /**
     * Suite
     * 
     * @return - Test
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(
                "Test for com.perforce.team.tests.unicode");
        // $JUnit-BEGIN$
        suite.addTestSuite(Iso88595Test.class);
        suite.addTestSuite(Utf16LeTest.class);
        suite.addTestSuite(Iso885915Test.class);
        suite.addTestSuite(Utf16BeTest.class);
        suite.addTestSuite(Utf8Test.class);
        suite.addTestSuite(EucJpTest.class);
        suite.addTestSuite(ShiftJisTest.class);
        suite.addTestSuite(Koi8RTest.class);
        suite.addTestSuite(Iso88591Test.class);
        suite.addTestSuite(MacOsRomanTest.class);
        suite.addTestSuite(Cp1251Test.class);
        suite.addTestSuite(WinAnsiTest.class);
        // $JUnit-END$
        return suite;
    }

}
