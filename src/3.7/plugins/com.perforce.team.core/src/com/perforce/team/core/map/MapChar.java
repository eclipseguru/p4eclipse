/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.map;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class MapChar implements Comparable<MapChar> {

    /**
     * MAP_CHAR_NAMES
     */
    public static final String MAP_CHAR_NAMES[] = { "0", "c", "/", "%", "*", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
            "." }; //$NON-NLS-1$

    /**
     * PARAM_BASE_PERCENT
     */
    public static final int PARAM_BASE_PERCENT = 0;

    /**
     * PARAM_BASE_STARS
     */
    public static final int PARAM_BASE_STARS = 10;

    /**
     * PARAM_BASE_DOTS
     */
    public static final int PARAM_BASE_DOTS = 20;

    /**
     * PARAM_BASE_TOP
     */
    public static final int PARAM_BASE_TOP = 23;

    /**
     * Parse session
     */
    public static class ParseSession {

        int nDots = 0;
        int nStars = 0;
    }

    /**
     * Character
     */
    public char c;

    /**
     * Param number
     */
    public int paramNumber;

    /**
     * Map Char Class
     */
    public MapCharClass cc;

    /**
     * Set the contents
     * 
     * @param p
     * @param session
     * @return true if set
     */
    public boolean set(StringBuilder p, ParseSession session) {
        if (p.length() == 0) {
            this.c = '\0';
            this.cc = MapCharClass.cEOS;
            return false;
        }
        this.c = p.charAt(0);

        if (c == '/') {
            cc = MapCharClass.cSLASH;
            p.deleteCharAt(0);
        } else if (c == '.' && p.charAt(1) == '.' && p.charAt(2) == '.') {
            cc = MapCharClass.cDOTS;
            paramNumber = session.nDots++;
            p.delete(0, 3);
        } else if (c == '%' && p.charAt(1) == '%' && p.charAt(2) >= '0'
                && p.charAt(2) <= '9') {
            cc = MapCharClass.cPERC;
            paramNumber = (char) (PARAM_BASE_PERCENT + (p.charAt(2) - '0'));
            p.delete(0, 3);
        } else if (c == '*') {
            cc = MapCharClass.cSTAR;
            paramNumber = (char) (PARAM_BASE_STARS + session.nStars++);
            p.deleteCharAt(0);
        } else {
            cc = MapCharClass.cCHAR;
            p.deleteCharAt(0);
        }

        return true;
    }

    /**
     * Make param
     * 
     * @param p
     * @param mc2
     * @param wildSlot
     */
    public void makeParam(StringBuilder p, MapChar mc2, int wildSlot) {
        if (cc == MapCharClass.cDOTS && mc2.cc == MapCharClass.cDOTS) {
            p.append("..."); //$NON-NLS-1$
        } else {
            p.append("%%"); //$NON-NLS-1$
            p.append(++wildSlot);
        }
    }

    /**
     * Get name
     * 
     * @return name
     */
    public String getName() {
        return MAP_CHAR_NAMES[cc.ordinal()];
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof MapChar) {
            return this.c == ((MapChar) obj).c;
        }
        return false;
    }

    @Override
    public int hashCode() {
    	return this.c;
    }
    
    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(MapChar o) {
        return this.c - o.c;
    }

    /**
     * Is wild
     * 
     * @return true if wild
     */
    public boolean isWild() {
        return cc.ordinal() >= MapCharClass.cPERC.ordinal();
    }

}
