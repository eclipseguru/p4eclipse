/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.map;

import com.perforce.team.core.map.MapChar.ParseSession;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class MapHalf {

    private static class Backup {

        int mc;
        MapParam param;
    }

    private static class Retry {

        int inputIndex;
        int backupIndex;

        public Retry(int index, int lastBackup) {
            this.inputIndex = index;
            this.backupIndex = lastBackup;
        }
    }

    /**
     * PARAM_VECTOR_LENGTH
     */
    public static final int PARAM_VECTOR_LENGTH = 30;

    /**
     * PARAM_MAX_BACKTRACK
     */
    public static final int PARAM_MAX_BACKTRACK = 10;

    /**
     * PARAM_MAX_WILDS
     */
    public static final int PARAM_MAX_WILDS = 10;

    /**
     * Compare grid
     */
    public static final int[][] CmpGrid = new int[][] {

    { 0, -1, -1, 0, 0, 0 }, { 1, -2, -2, 1, 1, 1 }, { 1, -2, 2, 1, 1, 1 },
            { 0, -1, -1, 0, 0, 0 }, { 0, -1, -1, 0, 0, 0 },
            { 0, -1, -1, 0, 0, 0 }, };

    String string;
    MapChar[] mapChar;
    int mapTail;
    int mapEnd;
    int fixedLen;
    boolean isWild;
    int nWilds = 0;

    /**
     * @param p
     */
    public MapHalf(String p) {
        int l = p.length() + 1;
        this.string = p;
        mapChar = new MapChar[l];

        ParseSession session = new ParseSession();

        int mc = 0;
        StringBuilder buffer = new StringBuilder(p);
        while ((mapChar[mc] = new MapChar()).set(buffer, session)) {
            mc++;
        }

        mapEnd = mc;

        while (mc > 0
                && (mapChar[mc - 1].cc == MapCharClass.cCHAR || mapChar[mc - 1].cc == MapCharClass.cSLASH)) {
            --mc;
        }

        mapTail = mc;

        mc = 0;

        while (mapChar[mc].cc == MapCharClass.cCHAR
                || mapChar[mc].cc == MapCharClass.cSLASH) {
            ++mc;
        }

        isWild = mapChar[mc].cc != MapCharClass.cEOS;
        fixedLen = mc;

        for (nWilds = 0, mc = 0; mapChar[mc].cc != MapCharClass.cEOS; mc++) {
            if (mapChar[mc].isWild()) {
                ++nWilds;
            }
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.string;
    }

    boolean hasSubDirs(int match) {
        MapChar mc = mapChar[match];

        while (mc.cc != MapCharClass.cEOS && mc.cc != MapCharClass.cSLASH
                && mc.cc != MapCharClass.cDOTS) {
            match++;
            mc = mapChar[match];
        }

        return mc.cc != MapCharClass.cEOS;
    }

    /**
     * Has end slash ellipses
     * 
     * @return true if ends with /...
     */
    public boolean hasEndSlashEllipses() {

        MapChar mc = mapChar[mapEnd - 1];

        if (!isWild) {
            return false;
        }

        if (mc == mapChar[0]) {
            return false;
        }
        mc = mapChar[mapEnd - 2];
        if (mc.cc != MapCharClass.cSLASH && mc.c != '\\') {
            return false;
        }

        return mc.cc == MapCharClass.cDOTS;
    }

    /**
     * Compare to another map half
     * 
     * @param item
     * @return compare value
     */
    public int compare(MapHalf item) {
        int mc1 = 0;
        int mc2 = 0;

        int l = fixedLen < item.fixedLen ? fixedLen : item.fixedLen;

        for (; l-- != 0 && mapChar[mc1].equals(item.mapChar[mc2]); ++mc1, ++mc2) {

        }

        for (;; ++mc1, ++mc2) {
            int d;

            switch (CmpGrid[mapChar[mc1].cc.ordinal()][item.mapChar[mc2].cc
                    .ordinal()]) {
            case -1:
                return -1;
            case 1:
                return 1;
            case 0:
                return 0;
            case -2:
                d = mapChar[mc1].compareTo(item.mapChar[mc2]);
                if (d != 0) {
                    return d;
                }
            case 2:
                break;
            }
        }
    }

    /**
     * @return fixed length
     */
    public int getFixedLen() {
        return fixedLen;
    }

    /**
     * Get common length
     * 
     * @param prev
     * @return common length
     */
    public int getCommonLen(MapHalf prev) {
        int matchLen = 0;
        MapChar mc1 = mapChar[matchLen];
        MapChar mc2 = prev.mapChar[matchLen];
        while (matchLen < fixedLen && mc1.compareTo(mc2) == 0) {
            ++matchLen;
            mc1 = mapChar[matchLen];
            mc2 = prev.mapChar[matchLen];
        }

        return matchLen;
    }

    /**
     * Match
     * 
     * @param from
     * @param coff
     * @return match value
     */
    public int match1(String from, int coff) {
        int r = 0;
        for (; coff < fixedLen && coff < from.length(); ++coff) {
            r = mapChar[coff].c - from.charAt(coff);
            if (r != 0) {
                return -r;
            }
        }
        if (from.length() < fixedLen) {
            r = -1;
        }

        return r;
    }

    /**
     * @param from
     * @param params
     * @return match value
     */
    public int match2(String from, MapParams params) {
        if (from.length() < fixedLen) {
            return 0;
        }

        int input;
        int mc = fixedLen;

        if (isWild) {
            for (int i = from.length() - 1, index = mapEnd - 1; index > mapTail; i--, index--) {
                if (mapChar[index].c - from.charAt(i) != 0) {
                    return 0;
                }
            }
        }

        input = fixedLen;

        Backup[] backup = new Backup[PARAM_MAX_BACKTRACK * 2];
        for (int i = 0; i < backup.length; i++) {
            backup[i] = new Backup();
        }
        int bIndex = 0;

        for (;;) {

            switch (mapChar[mc].cc) {
            case cDOTS:
            case cPERC:
            case cSTAR:
                backup[bIndex].param = params.vector[mapChar[mc].paramNumber];
                backup[bIndex].param.start = input;

                if (mapChar[mc].cc != MapCharClass.cDOTS) {
                    while (input < from.length() && from.charAt(input) != '/') {
                        input++;
                    }
                } else {
                    input = from.length();
                }

                backup[bIndex].param.end = input;
                backup[bIndex].mc = ++mc;
                bIndex++;
                break;

            case cSLASH:
            case cCHAR:
                do {
                    if (input >= from.length()
                            || mapChar[mc].c != from.charAt(input)) {
                        mc++;
                        Retry retried = new Retry(input, bIndex);
                        mc = retry(bIndex, backup, from, retried);
                        if (mc == -1) {
                            return 0;
                        }
                        input = retried.inputIndex;
                        bIndex = retried.backupIndex;
                        break;
                    } else {
                        mc++;
                        input++;
                    }
                } while (mapChar[mc].cc == MapCharClass.cCHAR
                        || mapChar[mc].cc == MapCharClass.cSLASH);
                break;

            case cEOS:
                if (input > from.length()) {
                    Retry retried = new Retry(input, bIndex);
                    mc = retry(bIndex, backup, from, retried);
                    if (mc == -1) {
                        return 0;
                    }
                    input = retried.inputIndex;
                    bIndex = retried.backupIndex;
                    break;
                }
                return 1;
            }
        }
    }

    private int retry(int bIndex, Backup[] backup, String from, Retry retry) {
        int mc = -1;
        for (;; --bIndex) {
            if (bIndex <= 0) {
                mc = -1;
                break;
            }
            mc = backup[bIndex - 1].mc;
            retry.inputIndex = --backup[bIndex - 1].param.end;
            if (retry.inputIndex >= backup[bIndex - 1].param.start) {
                break;
            }
        }
        retry.backupIndex = bIndex;
        return mc;
    }

    /**
     * @param from
     * @param output
     * @param params
     */
    public void expand(String from, StringBuilder output, MapParams params) {
        int slot;
        for (MapChar mc : mapChar) {
            if (mc.cc == MapCharClass.cEOS) {
                break;
            }
            if (mc.isWild()) {
                slot = mc.paramNumber;
                int start = params.vector[slot].start;
                int end = params.vector[slot].end;
                output.append(from.substring(start, end));
            } else {
                output.append(mc.c);
            }
        }
    }
}
