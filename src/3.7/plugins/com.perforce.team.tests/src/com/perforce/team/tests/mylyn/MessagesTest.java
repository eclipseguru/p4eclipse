/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.mylyn;

import com.perforce.team.tests.P4TestCase;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.eclipse.osgi.util.NLS;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class MessagesTest extends P4TestCase {

    private int check(Class<? extends NLS> nls) {
        int checked = 0;
        Field[] fields = nls.getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isPublic(field.getModifiers())
                    && Modifier.isStatic(field.getModifiers())) {
                Object value;
                try {
                    value = field.get(null);
                    if (value instanceof String) {
                        checked++;
                        assertTrue(value.toString().length() > 0);
                    }
                } catch (IllegalArgumentException e) {
                    handle(e);
                } catch (IllegalAccessException e) {
                    handle(e);
                }

            }
        }
        return checked;
    }

    /**
     * Test core messages
     */
    public void testCore() {
        int checked = check(com.perforce.team.core.mylyn.Messages.class);
        assertTrue(checked > 0);
    }

    /**
     * Test changeset messages
     */
    public void testChangeset() {
        int checked = check(com.perforce.team.ui.mylyn.changeset.Messages.class);
        assertTrue(checked > 0);
    }

    /**
     * Test ui messages
     */
    public void testUi() {
        int checked = check(com.perforce.team.ui.mylyn.Messages.class);
        assertTrue(checked > 0);
    }

    /**
     * Test connection messages
     */
    public void testConnection() {
        int checked = check(com.perforce.team.ui.mylyn.connection.Messages.class);
        assertTrue(checked > 0);
    }

    /**
     * Test editor messages
     */
    public void testEditor() {
        int checked = check(com.perforce.team.ui.mylyn.editor.Messages.class);
        assertTrue(checked > 0);
    }

    /**
     * Test job messages
     */
    public void testJob() {
        int checked = check(com.perforce.team.ui.mylyn.job.Messages.class);
        assertTrue(checked > 0);
    }

    /**
     * Test preferences messages
     */
    public void testPreferences() {
        int checked = check(com.perforce.team.ui.mylyn.preferences.Messages.class);
        assertTrue(checked > 0);
    }

    /**
     * Test search messages
     */
    public void testSearch() {
        int checked = check(com.perforce.team.ui.mylyn.search.Messages.class);
        assertTrue(checked > 0);
    }

}
