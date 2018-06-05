/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class MapTable {

    private static Comparator<MapItem> sortcmplhs = new Comparator<MapItem>() {

        public int compare(MapItem e1, MapItem e2) {
            int r = e1.lhs().compare(e2.lhs());
            return r != 0 ? r : e2.slot() - e1.slot();
        }
    };

    private static Comparator<MapItem> sortcmprhs = new Comparator<MapItem>() {

        public int compare(MapItem e1, MapItem e2) {
            int r = e1.rhs().compare(e2.rhs());
            return r != 0 ? r : e2.slot() - e1.slot();
        }
    };

    int count;
    MapItem entry;
    MapTree[] trees = new MapTree[2];

    /**
     * Create empty map table
     */
    public MapTable() {
        this.trees[0] = new MapTree();
        this.trees[1] = new MapTree();
    }

    /**
     * Reverse the table
     */
    public void reverse() {
        entry = entry.reverse();
    }

    /**
     * Insert into table
     * 
     * @param table
     * @param fwd
     * @param rev
     */
    public void insert(MapTable table, boolean fwd, boolean rev) {
        MapItem map = null;

        for (map = table.entry; map != null; map = map.next()) {
            if (fwd) {
                insert(map.lhs().toString(), map.rhs().toString(), map.flag());
            }
            if (rev) {
                insert(map.rhs().toString(), map.lhs().toString(), map.flag());
            }
        }

        reverse();
    }

    /**
     * Insert into table
     * 
     * @param lhs
     * @param rhs
     * @param mapFlag
     */
    public void insert(String lhs, String rhs, MapFlag mapFlag) {
        entry = new MapItem(entry, lhs, rhs, mapFlag, count++);
    }

    /**
     * Sort table
     * 
     * @param direction
     * @return array of map items
     */
    public MapItem[] sort(MapTableT direction) {
        if (trees[direction.ordinal()].sort != null) {
            return trees[direction.ordinal()].sort;
        }

        // Create sort tree
        MapItem map = entry;
        List<MapItem> vecp = new ArrayList<MapItem>();
        for (; map != null; map = map.next()) {
            vecp.add(map);
        }
        MapItem[] vec = vecp.toArray(new MapItem[vecp.size()]);

        if (direction == MapTableT.LHS) {
            Arrays.sort(vec, sortcmplhs);
        } else {
            Arrays.sort(vec, sortcmprhs);
        }

        return trees[direction.ordinal()].sort = vec;
    }

    private void makeTree(MapTableT dir) {
        int depth = 0;

        MapItem[] vec = sort(dir);

        trees[dir.ordinal()].tree = new MapItem[] { MapItem.tree(vec, 0,
                vec.length, dir, null, depth) };
    }

    /**
     * Translate
     * 
     * @param dir
     * @param from
     * @param to
     * @return map item
     */
    public MapItem translate(MapTableT dir, String from, StringBuilder to) {
        if (!isValid()) {
            return null;
        }
        if (trees[dir.ordinal()].tree == null) {
            makeTree(dir);
        }

        MapItem map = trees[dir.ordinal()].tree[0].match(dir, from);

        if (map != null) {
            MapParams params = new MapParams();

            map.ths(dir).match2(from, params);
            map.ohs(dir).expand(from, to, params);
        }

        return map;
    }

    /**
     * Is this table valid
     * 
     * @return true if valid, false if invalid
     */
    public boolean isValid() {
        return this.entry != null;
    }
}
