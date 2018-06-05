/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.model.p4java;

import com.perforce.team.core.p4java.P4IntegrationOptions2;
import com.perforce.team.tests.P4TestCase;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4IntegrationOptionsTest extends P4TestCase {

    /**
     * Basic test of the integration options test
     */
    public void testIntegrationOptions() {
        P4IntegrationOptions2 options = new P4IntegrationOptions2();
        assertFalse(options.isBaselessMerge());
        assertFalse(options.isUseHaveRev());
        assertFalse(options.isDisplayBaseDetails());
        assertFalse(options.isDontCopyToClient());
        assertFalse(options.isForce());
        assertFalse(options.isPropagateType());
        assertFalse(options.isIntegrateAroundDeleted());
        assertFalse(options.isReverseMapping());

        options.setBaselessMerge(true);
        options.setForce(true);
        options.setUseHaveRev(true);
        options.setDisplayBaseDetails(true);
        options.setDontCopyToClient(true);
        options.setPropagateType(true);
        options.setIntegrateAroundDeleted(true);
        options.setReverseMapping(true);
        options.setTrySafeResolve(true);

        assertTrue(options.isBaselessMerge());
        assertTrue(options.isUseHaveRev());
        assertTrue(options.isDisplayBaseDetails());
        assertTrue(options.isDontCopyToClient());
        assertTrue(options.isForce());
        assertTrue(options.isPropagateType());
        assertTrue(options.isIntegrateAroundDeleted());
        assertTrue(options.isReverseMapping());
        assertTrue(options.isTrySafeResolve());

        options = new P4IntegrationOptions2(true, true, true,
                false, true, false, true, false, false);

        assertFalse(options.isBaselessMerge());
        assertTrue(options.isUseHaveRev());
        assertTrue(options.isDisplayBaseDetails());
        assertTrue(options.isDontCopyToClient());
        assertFalse(options.isForce());
        assertFalse(options.isPropagateType());
        assertTrue(options.isIntegrateAroundDeleted());
        assertTrue(options.isReverseMapping());
        assertFalse(options.isTrySafeResolve());
    }

}
