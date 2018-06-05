package com.perforce.team.tests.unicode;

/**
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class WinAnsiTest extends UnicodeTestCase {

    /**
     * @see com.perforce.team.tests.unicode.UnicodeTestCase#getP4Charset()
     */
    @Override
    protected String getP4Charset() {
        return "winansi";
    }

    /**
     * @see com.perforce.team.tests.unicode.UnicodeTestCase#getRawPath()
     */
    @Override
    protected String getRawPath() {
        return "//depot/charset_files/Windows 1252/always_winansi.txt";
    }

    /**
     * @see com.perforce.team.tests.unicode.UnicodeTestCase#getTranslatePath()
     */
    @Override
    protected String getTranslatePath() {
        return "//depot/charset_files/Windows 1252/translated.txt";
    }

}
