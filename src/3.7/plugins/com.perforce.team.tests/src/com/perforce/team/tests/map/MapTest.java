/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.map;

import com.perforce.team.core.map.MapFlag;
import com.perforce.team.core.map.MapTable;
import com.perforce.team.core.map.MapTableT;
import com.perforce.team.tests.P4TestCase;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class MapTest extends P4TestCase {

    /**
     * Test map
     */
    public void testMap1() {
        MapTable table = new MapTable();
        table.insert("//depot/...", "//test/...", MapFlag.MfMap);
        String from = "//depot/test.txt";
        StringBuilder to = new StringBuilder();
        table.translate(MapTableT.LHS, from, to);
        assertEquals("//test/test.txt", to.toString());
    }

    /**
     * Test map
     */
    public void testMap2() {
        MapTable table = new MapTable();
        table.insert("//depot/...", "//test/...", MapFlag.MfMap);
        String from = "//depot/a/test.txt";
        StringBuilder to = new StringBuilder();
        table.translate(MapTableT.LHS, from, to);
        assertEquals("//test/a/test.txt", to.toString());
    }

    /**
     * Test map
     */
    public void testMap3() {
        MapTable table = new MapTable();
        table.insert("//depot/...", "//depot/test/...", MapFlag.MfMap);
        String from = "//depot/a/test.txt";
        StringBuilder to = new StringBuilder();
        table.translate(MapTableT.LHS, from, to);
        assertEquals("//depot/test/a/test.txt", to.toString());
    }

    /**
     * Test map
     */
    public void testMap4() {
        MapTable table = new MapTable();
        table.insert("//depot/*.txt", "//depot/*.exe", MapFlag.MfMap);
        String from = "//depot/test.txt";
        StringBuilder to = new StringBuilder();
        table.translate(MapTableT.LHS, from, to);
        assertEquals("//depot/test.exe", to.toString());
    }

    /**
     * Test map
     */
    public void testMap5() {
        MapTable table = new MapTable();
        table.insert("//depot/...", "//test/...", MapFlag.MfMap);
        String from = "//test/test.txt";
        StringBuilder to = new StringBuilder();
        table.translate(MapTableT.RHS, from, to);
        assertEquals("//depot/test.txt", to.toString());
    }

    /**
     * Test map
     */
    public void testMap6() {
        MapTable table = new MapTable();
        table.insert("//depot/...", "//test/...", MapFlag.MfMap);
        String from = "//test/a/test.txt";
        StringBuilder to = new StringBuilder();
        table.translate(MapTableT.RHS, from, to);
        assertEquals("//depot/a/test.txt", to.toString());
    }

    /**
     * Test map
     */
    public void testMap7() {
        MapTable table = new MapTable();
        table.insert("//depot/...", "//depot/test/...", MapFlag.MfMap);
        String from = "//depot/test/test.txt";
        StringBuilder to = new StringBuilder();
        table.translate(MapTableT.RHS, from, to);
        assertEquals("//depot/test.txt", to.toString());
    }

    /**
     * Test map
     */
    public void testMap8() {
        MapTable table = new MapTable();
        table.insert("//depot/*.txt", "//depot/*.exe", MapFlag.MfMap);
        String from = "//depot/test.exe";
        StringBuilder to = new StringBuilder();
        table.translate(MapTableT.RHS, from, to);
        assertEquals("//depot/test.txt", to.toString());
    }

    /**
     * Test map
     */
    public void testMap9() {
        MapTable table = new MapTable();
        table.insert("//depot/...", "//depot/...", MapFlag.MfMap);
        String from = "//depot/test.txt";
        StringBuilder to = new StringBuilder();
        table.translate(MapTableT.LHS, from, to);
        assertEquals("//depot/test.txt", to.toString());
    }

    /**
     * Test map
     */
    public void testMap10() {
        MapTable table = new MapTable();
        table.insert("//depot/...", "//depot/...", MapFlag.MfMap);
        String from = "//depot/test.txt";
        StringBuilder to = new StringBuilder();
        table.translate(MapTableT.RHS, from, to);
        assertEquals("//depot/test.txt", to.toString());
    }

}
