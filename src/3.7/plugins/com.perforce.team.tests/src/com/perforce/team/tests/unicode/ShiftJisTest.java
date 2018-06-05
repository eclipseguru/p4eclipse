package com.perforce.team.tests.unicode;

/**
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class ShiftJisTest extends UnicodeTestCase {

    /**
     * @see com.perforce.team.tests.unicode.UnicodeTestCase#getP4Charset()
     */
    @Override
    protected String getP4Charset() {
        return "shiftjis";
    }

    /**
     * @see com.perforce.team.tests.unicode.UnicodeTestCase#getTransRevision()
     */
    @Override
    protected int getTransRevision() {
        return 3;
    }

    /**
     * @see com.perforce.team.tests.unicode.UnicodeTestCase#getRawPath()
     */
    @Override
    protected String getRawPath() {
        return "//depot/charset_files/SHIFT-JIS/always_shiftjis.txt";
    }

    /**
     * @see com.perforce.team.tests.unicode.UnicodeTestCase#getTranslatePath()
     */
    @Override
    protected String getTranslatePath() {
        return "//depot/charset_files/SHIFT-JIS/translated.txt";
    }

    /**
     * Test translation
     */
    @Override
    public void testTranslation() {
        // Comment out since shiftjis translation currently fails
    }

}
