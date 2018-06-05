/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.branchgraph.model;

import com.perforce.team.core.mergequest.model.PropertyElement;
import com.perforce.team.tests.P4TestCase;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class PropertyElementTest extends P4TestCase {

    /**
     * Test methods on {@link PropertyElement}
     */
    public void testProperties() {
        PropertyElement element = new PropertyElement();
        element.addPropertyListener(null);
        element.addPropertyListener(null, null);
        element.removePropertyListener(null);
        element.removePropertyListener(null, null);
    }

}
