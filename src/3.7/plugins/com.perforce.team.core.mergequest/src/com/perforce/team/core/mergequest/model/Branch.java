/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mergequest.model;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Branch extends BranchGraphElement {

    /**
     * LOCATION
     */
    public static final String LOCATION = "location"; //$NON-NLS-1$

    /**
     * SIZE
     */
    public static final String SIZE = "size"; //$NON-NLS-1$

    /**
     * TYPE
     */
    public static final String TYPE = "type"; //$NON-NLS-1$

    /**
     * SOURCE_MAPPINGS
     */
    public static final String SOURCE_MAPPINGS = "sourceMappings"; //$NON-NLS-1$

    /**
     * TARGET_MAPPINGS
     */
    public static final String TARGET_MAPPINGS = "targetMappings"; //$NON-NLS-1$

    private Map<String, Mapping> sourceMappings;
    private Map<String, Mapping> targetMappings;
    private String type = ""; //$NON-NLS-1$
    private int x = -1;
    private int y = -1;
    private int width = -1;
    private int height = -1;

    /**
     * Create a new branch
     * 
     * @param id
     */
    public Branch(String id) {
        this(id, null);
    }

    /**
     * Create a new branch
     * 
     * @param id
     * @param graph
     */
    public Branch(String id, IBranchGraph graph) {
        super(id, graph);
        this.sourceMappings = new HashMap<String, Mapping>();
        this.targetMappings = new HashMap<String, Mapping>();
    }

    /**
     * Add a mapping to this branch
     * 
     * @param mapping
     * @return true if added, false otherwise
     */
    public boolean add(Mapping mapping) {
        boolean added = false;
        if (mapping != null) {
            if (this.equals(mapping.getSource())) {
                this.sourceMappings.put(mapping.getId(), mapping);
                added = true;
                this.changeSupport.firePropertyChange(SOURCE_MAPPINGS, null,
                        mapping);
            } else if (this.equals(mapping.getTarget())) {
                this.targetMappings.put(mapping.getId(), mapping);
                added = true;
                this.changeSupport.firePropertyChange(TARGET_MAPPINGS, null,
                        mapping);
            }
        }
        return added;
    }

    /**
     * Remove mapping from this branch
     * 
     * @param mapping
     * @return - true if removed, false otherwise
     */
    public boolean remove(Mapping mapping) {
        boolean removed = false;
        if (mapping != null) {
            if (this.equals(mapping.getSource())) {
                removed = this.sourceMappings.remove(mapping.getId()) != null;
                if (removed) {
                    changeSupport.firePropertyChange(SOURCE_MAPPINGS, mapping,
                            null);
                }
            } else if (this.equals(mapping.getTarget())) {
                removed = this.targetMappings.remove(mapping.getId()) != null;
                if (removed) {
                    changeSupport.firePropertyChange(TARGET_MAPPINGS, mapping,
                            null);
                }
            }
        }
        return removed;
    }

    /**
     * Get mappings from this branch
     * 
     * @return - non-null but possibly empty array of mappings
     */
    public Mapping[] getSourceMappings() {
        return this.sourceMappings.values().toArray(
                new Mapping[this.sourceMappings.size()]);
    }

    /**
     * Get source mapping with specified id
     * 
     * @param id
     * @return mapping or null if not found
     */
    public Mapping getSourceMapping(String id) {
        Mapping mapping = null;
        if (id != null) {
            mapping = this.sourceMappings.get(id);
        }
        return mapping;
    }

    /**
     * Get source mapping by name connected to this branch mapping
     * 
     * @param name
     * @return mapping or null if not found
     */
    public Mapping getSourceMappingByName(String name) {
        Mapping mapping = null;
        if (name != null) {
            for (Mapping candidate : getSourceMappings()) {
                if (name.equals(candidate.getName())) {
                    mapping = candidate;
                    break;
                }
            }
        }
        return mapping;
    }

    /**
     * Get target mapping by name connected to this branch mapping
     * 
     * @param name
     * @return mapping or null if not found
     */
    public Mapping getTargetMappingByName(String name) {
        Mapping mapping = null;
        if (name != null) {
            for (Mapping candidate : getTargetMappings()) {
                if (name.equals(candidate.getName())) {
                    mapping = candidate;
                    break;
                }
            }
        }
        return mapping;
    }

    /**
     * Get target mapping with specified name
     * 
     * @param id
     * @return mapping or null if not found
     */
    public Mapping getTargetMapping(String id) {
        Mapping mapping = null;
        if (id != null) {
            mapping = this.targetMappings.get(id);
        }
        return mapping;
    }

    /**
     * Get mappings to this branch
     * 
     * @return - non-null but possibly empty array of mappings
     */
    public Mapping[] getTargetMappings() {
        return this.targetMappings.values().toArray(
                new Mapping[this.targetMappings.size()]);
    }

    /**
     * Get target mappings owned by the source branch
     * 
     * @return non-null but possibly empty array of mappings
     */
    public Mapping[] getSourceOwnedTargetMappings() {
        List<Mapping> mappings = new ArrayList<Mapping>();
        for (Mapping mapping : this.targetMappings.values()) {
            Mapping sourceOwned = mapping.getSource().getSourceMapping(
                    mapping.getId());
            if (sourceOwned != null) {
                mappings.add(sourceOwned);
            }
        }
        return mappings.toArray(new Mapping[mappings.size()]);
    }

    /**
     * Get all mappings connected to this branch
     * 
     * @return - non-null but possibly empty array of mappings
     */
    public Mapping[] getAllMappings() {
        Mapping[] sources = getSourceMappings();
        Mapping[] targets = getTargetMappings();
        Mapping[] combined = new Mapping[sources.length + targets.length];
        if (combined.length > 0) {
            System.arraycopy(sources, 0, combined, 0, sources.length);
            System.arraycopy(targets, 0, combined, sources.length,
                    targets.length);
        }
        return combined;
    }

    /**
     * Get mapping count
     * 
     * @return mapping count connected to this branch
     */
    public int getMappingCount() {
        return this.sourceMappings.size() + this.targetMappings.size();
    }

    /**
     * Get branch type
     * 
     * @return - type
     */
    public String getType() {
        return this.type;
    }

    /**
     * Set type of branch
     * 
     * @param type
     *            - ignored if null
     */
    public void setType(String type) {
        if (type != null && !type.equals(this.type)) {
            String previous = this.type;
            this.type = type;
            changeSupport.firePropertyChange(TYPE, previous, type);
        }
    }

    /**
     * Get x coordinate of branch
     * 
     * @return - x
     */
    public int getX() {
        return this.x;
    }

    /**
     * Set location
     * 
     * @param x
     * @param y
     */
    public void setLocation(int x, int y) {
        boolean change = false;
        int previousX = this.x;
        int previousY = this.y;
        if (x != this.x) {
            this.x = x;
            change = true;
        }
        if (y != this.y) {
            this.y = y;
            change = true;
        }
        if (change) {
            changeSupport.firePropertyChange(LOCATION, new Point(previousX,
                    previousY), new Point(this.x, this.y));
        }
    }

    /**
     * Set size
     * 
     * @param width
     * @param height
     */
    public void setSize(int width, int height) {
        if (width <= 0) {
            width = -1;
        }
        if (height <= 0) {
            height = -1;
        }
        boolean change = false;
        int previousWidth = this.width;
        int previousHeight = this.height;
        if (width != this.width) {
            this.width = width;
            change = true;
        }
        if (height != this.height) {
            this.height = height;
            change = true;
        }
        if (change) {
            changeSupport.firePropertyChange(SIZE, new Rectangle(previousWidth,
                    previousHeight), new Rectangle(this.width, this.height));
        }
    }

    /**
     * Get y coordinate of branch
     * 
     * @return - y
     */
    public int getY() {
        return this.y;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Branch && super.equals(obj);
    }

    @Override
    public int hashCode() {
    	// to prevent coverity complain HE_EQUALS_NO_HASHCODE
    	return super.hashCode();
    }
    
    /**
     * Disconnect all mappings from this branch
     */
    public void disconnect() {
        for (Mapping mapping : getSourceMappings()) {
            mapping.disconnect();
        }
        for (Mapping mapping : getTargetMappings()) {
            mapping.disconnect();
        }
    }

    /**
     * Accept the specified mapping visitor
     * 
     * @param visitor
     * @param type
     *            - mapping type to visit
     * @param source
     *            - true to visit source mapping
     * @param target
     *            - true to visit target mappings
     */
    public void accept(IMappingVisitor visitor, String type, boolean source,
            boolean target) {
        if (visitor != null) {
            Mapping[] mappings = null;
            if (source && target) {
                mappings = getAllMappings();
            } else if (source) {
                mappings = getSourceMappings();
            } else if (target) {
                mappings = getTargetMappings();
            }
            if (mappings != null && mappings.length > 0) {
                if (type != null) {
                    for (Mapping mapping : mappings) {
                        if (type.equals(mapping.getType())) {
                            if (!visitor.visit(mapping, this)) {
                                break;
                            }
                        }
                    }
                } else {
                    for (Mapping mapping : mappings) {
                        if (!visitor.visit(mapping, this)) {
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * @return the width
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * Does this branch contain the specified point
     * 
     * @param point
     * @return true if contains point, false if point falls outside of current
     *         branch bounds
     */
    public boolean containsPoint(Point point) {
        if (point == null) {
            return false;
        }

        if (this.width != -1) {
            if (point.x < this.x || point.x > this.x + this.width) {
                return false;
            }
        } else {
            if (point.x != this.x) {
                return false;
            }
        }

        if (this.height != -1) {
            if (point.y < this.y || point.y > this.y + this.height) {
                return false;
            }
        } else {
            if (point.y != this.y) {
                return false;
            }
        }

        return true;
    }

}
