package com.perforce.team.tests.unicode;

/**
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class MacOsRomanTest extends UnicodeTestCase {

    /**
     * @see com.perforce.team.tests.unicode.UnicodeTestCase#getP4Charset()
     */
    @Override
    protected String getP4Charset() {
        return "macosroman";
    }

    /**
     * @see com.perforce.team.tests.unicode.UnicodeTestCase#getRawPath()
     */
    @Override
    protected String getRawPath() {
        return "//depot/charset_files/Mac OS Roman/always_macosroman.txt";
    }

    /**
     * @see com.perforce.team.tests.unicode.UnicodeTestCase#getTranslatePath()
     */
    @Override
    protected String getTranslatePath() {
        return "//depot/charset_files/Mac OS Roman/translated.txt";
    }

}
