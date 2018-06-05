/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.timelapse;

import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.timelapse.IAnnotateModel;
import com.perforce.team.ui.timelapse.TimeLapseInput;
import com.perforce.team.ui.timelapse.TimeLapsePreferencePage;
import com.perforce.team.ui.timelapse.TimeLapseRegistry;
import com.perforce.team.ui.timelapse.IAnnotateModel.Type;

import junit.framework.TestCase;

import org.eclipse.jface.preference.PreferenceDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class TimeLapseTest extends TestCase {

    /**
     * Test time lapse registry
     */
    public void testRegistry() {
        assertNotNull(TimeLapseRegistry.getRegistry());
        assertNotNull(TimeLapseRegistry.getRegistry().getContentTypes());
        assertTrue(TimeLapseRegistry.getRegistry().getContentTypes().length >= 2);
        assertNotNull(TimeLapseRegistry.getRegistry().getEditorId(
                "org.eclipse.core.runtime.text"));
        assertNotNull(TimeLapseRegistry.getRegistry().getPreferenceHandlers());
        assertTrue(TimeLapseRegistry.getRegistry().getPreferenceHandlers().length > 0);
    }

    /**
     * Test time lapse input
     */
    public void testInput() {
        TimeLapseInput input = new TimeLapseInput(null);
        assertNull(input.getFile());
        assertFalse(input.exists());
    }

    /**
     * Test {@link IAnnotateModel.Type}
     */
    public void testModel() {
        Type[] types = Type.values();
        assertNotNull(types);
        for (Type type : types) {
            assertNotNull(type);
            assertNotNull(type.toString());
            assertEquals(type, Type.valueOf(type.toString()));
        }
    }

    /**
     * Test pref page
     */
    public void testPreferencePage() {
        PreferenceDialog dialog = P4UIUtils.openPreferencePage(
                TimeLapsePreferencePage.ID, false);
        assertNotNull(dialog);
        try {
            assertNotNull(dialog.getSelectedPage());
            assertTrue(dialog.getSelectedPage() instanceof TimeLapsePreferencePage);
        } finally {
            dialog.close();
        }
    }

}
