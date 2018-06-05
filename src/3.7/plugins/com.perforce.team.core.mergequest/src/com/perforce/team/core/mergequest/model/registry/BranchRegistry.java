/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mergequest.model.registry;

import com.perforce.team.core.extensions.ExtensionPointRegistry;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchRegistry extends ExtensionPointRegistry implements
        Iterable<BranchType> {

    /**
     * TYPE_ATTRIBUTE
     */
    public static final String TYPE_ATTRIBUTE = "type"; //$NON-NLS-1$

    /**
     * LABEL_ATTRIBUTE
     */
    public static final String LABEL_ATTRIBUTE = "label"; //$NON-NLS-1$

    /**
     * FIRMNESS_ATTRIBUTE
     */
    public static final String FIRMNESS_ATTRIBUTE = "firmness"; //$NON-NLS-1$

    /**
     * BRANCH_ELEMENT
     */
    public static final String BRANCH_ELEMENT = "branch"; //$NON-NLS-1$

    /**
     * Special firmness value which denotes the default branch type to use
     */
    public static final int DEFAULT_FIRMNESS = 1;

    String extensionPointId;

    private BranchType defaultType = null;
    private Map<String, BranchType> types;

    /**
     * Create branch registry
     * 
     * @param extensionPointId
     */
    public BranchRegistry(String extensionPointId) {
        this.extensionPointId = extensionPointId;
        this.types = new TreeMap<String, BranchType>();
        IConfigurationElement[] elements = Platform.getExtensionRegistry()
                .getConfigurationElementsFor(this.extensionPointId);
        buildBranches(elements);
    }

    /**
     * Get branch types
     * 
     * @return non-null but possibly empty array of types
     */
    public BranchType[] getTypes() {
        return this.types.values().toArray(new BranchType[this.types.size()]);
    }

    /**
     * Get type
     * 
     * @param type
     *            value
     * @return branch type or special unknown if not found
     */
    public BranchType getType(String type) {
        BranchType bType = null;
        if (type != null) {
            bType = this.types.get(type);
        }
        if (bType == null) {
            bType = BranchType.UNKNOWN;
        }
        return bType;
    }

    /**
     * Get default type
     * 
     * @return non-null default branch type
     */
    public BranchType getDefaultType() {
        return defaultType != null ? defaultType : BranchType.UNKNOWN;
    }

    /**
     * Build the elements
     * 
     * @param elements
     */
    protected void buildBranches(IConfigurationElement[] elements) {
        for (IConfigurationElement element : elements) {
            if (BRANCH_ELEMENT.equals(element.getName())) {
                String type = element.getAttribute(TYPE_ATTRIBUTE);
                if (type != null && type.length() > 0) {
                    String label = element.getAttribute(LABEL_ATTRIBUTE);
                    if (label == null || label.length() == 0) {
                        label = type;
                    }
                    int firmness = getInteger(
                            element.getAttribute(FIRMNESS_ATTRIBUTE), -1);
                    BranchType branchType = new BranchType(type, label,
                            firmness);
                    this.types.put(type, branchType);
                    if (branchType.getFirmness() == DEFAULT_FIRMNESS) {
                        this.defaultType = branchType;
                    }
                }
            }
        }
    }

    /**
     * @see java.lang.Iterable#iterator()
     */
    public Iterator<BranchType> iterator() {
        return this.types.values().iterator();
    }
}
