package com.perforce.team.tests.unicode;

/**
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class Iso885915Test extends UnicodeTestCase {

    /**
     * @see com.perforce.team.tests.unicode.UnicodeTestCase#getP4Charset()
     */
    @Override
    protected String getP4Charset() {
        return "iso8859-15";
    }

    /**
     * @see com.perforce.team.tests.unicode.UnicodeTestCase#getRawPath()
     */
    @Override
    protected String getRawPath() {
        return "//depot/charset_files/ISO 8859-15/always_iso8859-15.txt";
    }

    /**
     * @see com.perforce.team.tests.unicode.UnicodeTestCase#getTranslatePath()
     */
    @Override
    protected String getTranslatePath() {
        return "//depot/charset_files/ISO 8859-15/translated.txt";
    }

}
