/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.map;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class MapItem {

    /**
     * Create tree
     * 
     * @param start
     * @param startIndex
     * @param endIndex
     * @param dir
     * @param parent
     * @param depth
     * @return map item root of tree
     */
    public static MapItem tree(MapItem[] start, int startIndex, int endIndex,
            MapTableT dir, MapItem parent, int depth) {
        if (start == null || start.length == 0 || startIndex == endIndex) {
            return null;
        }
        int li = startIndex;
        int ri;

        if (start[startIndex] == start[endIndex - 1]
                || start[startIndex].isParent(start[endIndex - 1], dir)) {
            ri = endIndex;
        } else {
            ri = startIndex + (endIndex - startIndex) / 2;

            while (li < ri && !start[li].isParent(start[ri], dir)) {
                ++li;
            }

            while (ri < start.length && start[li].isParent(start[ri], dir)) {
                ++ri;
            }
        }

        MapWhole t = start[li].whole(dir.ordinal());

        int depthBelow = 0;

        t.overlap = 0;
        t.maxSlot = start[li].slot;

        t.left = tree(start, startIndex, li, dir, start[li], depthBelow);
        t.center = tree(start, li + 1, ri, dir, start[li], depthBelow);
        t.right = tree(start, ri, endIndex, dir, start[li], depthBelow);

        if (parent != null) {
            if (parent.whole(dir.ordinal()).maxSlot < t.maxSlot) {
                parent.whole(dir.ordinal()).maxSlot = t.maxSlot;
            }

            t.overlap = t.half.getCommonLen(parent.ths(dir));
        }

        return start[li];
    }

    MapWhole[] halves = new MapWhole[2];
    MapItem chain;
    MapFlag mapFlag;
    int slot;

    MapItem(MapItem c, String l, String r, MapFlag f, int s) {
        halves[MapTableT.LHS.ordinal()] = new MapWhole();
        halves[MapTableT.RHS.ordinal()] = new MapWhole();
        halves[MapTableT.LHS.ordinal()].half = new MapHalf(l);
        halves[MapTableT.RHS.ordinal()].half = new MapHalf(r);
        mapFlag = f;
        chain = c;
        slot = s;
    }

    MapHalf lhs() {
        return half(MapTableT.LHS.ordinal());
    }

    MapHalf rhs() {
        return half(MapTableT.RHS.ordinal());
    }

    MapHalf ths(MapTableT dir) {
        return half(dir.ordinal());
    }

    MapHalf ohs(MapTableT dir) {
        return half(1 - dir.ordinal());
    }

    MapItem next() {
        return chain;
    }

    MapFlag flag() {
        return mapFlag;
    }

    int slot() {
        return slot;
    }

    MapWhole whole(int dir) {
        return halves[dir];
    }

    MapHalf half(int dir) {
        return halves[dir].half;
    }

    boolean isParent(MapItem other, MapTableT dir) {
        return ths(dir).getFixedLen() == ths(dir).getCommonLen(other.ths(dir));
    }

    /**
     * Reverse
     * 
     * @return reversed map item
     */
    public MapItem reverse() {
        MapItem m = this;
        MapItem entry = null;
        int top = m != null ? m.slot : 0;

        while (m != null) {
            MapItem n = m.chain;
            m.chain = entry;
            m.slot = top - m.slot;
            entry = m;
            m = n;
        }

        return entry;
    }

    /**
     * Match to map item
     * 
     * @param dir
     * @param from
     * @return map item
     */
    public MapItem match(MapTableT dir, String from) {
        int coff = 0;
        int best = -1;
        MapItem map = null;
        MapItem tree = this;
        MapParams params = new MapParams();

        while (tree != null) {
            MapWhole t = tree.whole(dir.ordinal());

            if (best > t.maxSlot) {
                break;
            }

            if (coff > t.overlap) {
                coff = t.overlap;
            }

            int r = 0;

            if (coff < t.half.getFixedLen()) {
                r = t.half.match1(from, coff);
            }

            if (r == 0 && best < tree.slot && t.half.match2(from, params) != 0) {
                map = tree;
                best = map.slot;
            }

            if (r < 0) {
                tree = t.left;
            } else if (r > 0) {
                tree = t.right;
            } else {
                tree = t.center;
            }
        }

        if (map == null || map.mapFlag == MapFlag.MfUnmap) {
            return null;
        }

        return map;
    }

}
