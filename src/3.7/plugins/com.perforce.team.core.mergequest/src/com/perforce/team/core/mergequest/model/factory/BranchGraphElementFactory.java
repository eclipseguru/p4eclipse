/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mergequest.model.factory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class BranchGraphElementFactory implements
        IBranchGraphElementFactory {

    /**
     * Counter
     */
    protected AtomicInteger counter = new AtomicInteger(-1);

    /**
     * Create a a new element factory
     */
    public BranchGraphElementFactory() {

    }

    /**
     * Get element prefix
     * 
     * @return prefix non-null prefix
     */
    protected abstract String getPrefix();

    /**
     * Generate a new id for this element
     * 
     * @return non-null, non-empty unique id
     */
    protected String generateId() {
        return getPrefix() + counter.incrementAndGet();
    }

    /**
     * Set the counter to the new value
     * 
     * @param value
     */
    protected void setCounter(int value) {
        if (value > counter.get()) {
            counter.set(value);
        }
    }

    /**
     * @see com.perforce.team.core.mergequest.model.factory.IBranchGraphElementFactory#update(java.lang.String)
     */
    public void update(String id) {
        if (id != null) {
            String prefix = getPrefix();
            int index = id.indexOf(prefix);
            int offset = index + prefix.length();
            if (index == 0 && offset < id.length()) {
                String suffix = id.substring(offset);
                try {
                    setCounter(Integer.parseInt(suffix));
                } catch (NumberFormatException nfe) {
                    // Ignore and don't update counter
                }
            }
        }
    }

    /**
     * Get valid id for specified id
     * 
     * @param id
     * @return generated id if specified id is empty
     */
    protected String getId(String id) {
        if (id == null || id.length() == 0) {
            id = generateId();
        } else {
            update(id);
        }
        return id;
    }

}
