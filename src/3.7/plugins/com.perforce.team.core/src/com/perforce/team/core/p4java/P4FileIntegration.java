/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

/**
 * Helper class to encapsulte the source, target, and range of an integration.
 * Designed to make methods that do integrations require less parameters.
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4FileIntegration {

    private String source;
    private String target;
    private String start;
    private String end;

    /**
     * P4 file integration
     */
    public P4FileIntegration() {
        this.source = null;
        this.target = null;
        this.start = null;
        this.end = null;
    }

    /**
     * @return the source
     */
    public String getSource() {
        return source;
    }

    /**
     * @param source
     *            the source to set
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * @return the target
     */
    public String getTarget() {
        return target;
    }

    /**
     * @param target
     *            the target to set
     */
    public void setTarget(String target) {
        this.target = target;
    }

    /**
     * @return the start
     */
    public String getStart() {
        return start;
    }

    /**
     * @param start
     *            the start to set
     */
    public void setStart(String start) {
        this.start = start;
    }

    /**
     * @return the end
     */
    public String getEnd() {
        return end;
    }

    /**
     * @param end
     *            the end to set
     */
    public void setEnd(String end) {
        this.end = end;
    }

}
