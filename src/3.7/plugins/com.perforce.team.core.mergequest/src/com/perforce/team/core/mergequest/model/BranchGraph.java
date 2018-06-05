/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mergequest.model;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.mergequest.model.factory.BranchFactory;
import com.perforce.team.core.mergequest.model.factory.BranchMappingFactory;
import com.perforce.team.core.mergequest.model.factory.DepotPathMappingFactory;
import com.perforce.team.core.mergequest.model.factory.IBranchGraphElementFactory;
import com.perforce.team.core.p4java.IP4Connection;

import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchGraph extends BranchGraphElement implements IBranchGraph {

    private IP4Connection connection;
    private Map<String, IBranchGraphElement> elements;
    private IBranchGraphElementFactory depotMappingFactory;
    private IBranchGraphElementFactory branchMappingFactory;
    private IBranchGraphElementFactory branchFactory;

    private PropertyChangeListener globalListener = new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent evt) {
            changeSupport.firePropertyChange(evt);
        }

    };

    /**
     * Create an empty branch graph
     * 
     * @param id
     */
    public BranchGraph(String id) {
        this(id, id, null);
    }

    /**
     * Create an empty branch graph
     * 
     * @param id
     * @param name
     * @param connection
     * 
     */
    public BranchGraph(String id, String name, IP4Connection connection) {
        super(id, name);
        this.connection = connection;
        this.elements = new HashMap<String, IBranchGraphElement>();
        this.branchFactory = new BranchFactory();
        this.depotMappingFactory = new DepotPathMappingFactory();
        this.branchMappingFactory = new BranchMappingFactory();
    }

    /**
     * @see com.perforce.team.core.mergequest.model.BranchGraphElement#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof IBranchGraph) {
			return P4CoreUtils.equals(connection,
					((IBranchGraph) obj).getConnection())
					&& super.equals(obj);
        }
        return false;
    }

    @Override
    public int hashCode() {
    	// to prevent coverity complain HE_EQUALS_NO_HASHCODE
		return this.connection == null ? super.hashCode() : super.hashCode()
				+ this.connection.hashCode() * 31;
    }
    
    /**
     * @see com.perforce.team.core.mergequest.model.IBranchGraph#setConnection(com.perforce.team.core.p4java.IP4Connection)
     */
    public void setConnection(IP4Connection connection) {
        this.connection = connection;
    }

    /**
     * @see com.perforce.team.core.mergequest.model.IBranchGraph#getBranches()
     */
    public Branch[] getBranches() {
        Collection<Branch> branches = getElements(Branch.class);
        return branches.toArray(new Branch[branches.size()]);
    }

    /**
     * @see com.perforce.team.core.mergequest.model.IBranchGraph#isEmpty()
     */
    public boolean isEmpty() {
        return this.elements.isEmpty();
    }

    /**
     * @see com.perforce.team.core.mergequest.model.IBranchGraph#getMappings()
     */
    public Mapping[] getMappings() {
        Collection<Mapping> mappings = getElements(Mapping.class);
        return mappings.toArray(new Mapping[mappings.size()]);
    }

    /**
     * @see com.perforce.team.core.mergequest.model.IBranchGraph#getConnection()
     */
    public IP4Connection getConnection() {
        return this.connection;
    }

    /**
     * @see com.perforce.team.core.mergequest.model.IBranchGraph#createBranch(java.lang.String)
     */
    public Branch createBranch(String id) {
        return (Branch) this.branchFactory.create(id, this);
    }

    /**
     * @see com.perforce.team.core.mergequest.model.IBranchGraph#createBranchSpecMapping(java.lang.String)
     */
    public BranchSpecMapping createBranchSpecMapping(String id) {
        return (BranchSpecMapping) this.branchMappingFactory.create(id, this);
    }

    /**
     * @see com.perforce.team.core.mergequest.model.IBranchGraph#createDepotPathMapping(java.lang.String)
     */
    public DepotPathMapping createDepotPathMapping(String id) {
        return (DepotPathMapping) this.depotMappingFactory.create(id, this);
    }

    /**
     * @see com.perforce.team.core.mergequest.model.IBranchGraph#add(com.perforce.team.core.mergequest.model.IBranchGraphElement)
     */
    public boolean add(IBranchGraphElement element) {
        boolean added = false;
        if (element != null && !this.elements.containsKey(element.getId())) {
            element.setGraph(this);
            this.elements.put(element.getId(), element);
            added = true;
            if (added) {
                element.addPropertyListener(this.globalListener);
                changeSupport.firePropertyChange(ELEMENT_ADDED, null, element);
            }
        }
        return added;
    }

    /**
     * @see com.perforce.team.core.mergequest.model.IBranchGraph#remove(com.perforce.team.core.mergequest.model.IBranchGraphElement)
     */
    public boolean remove(IBranchGraphElement element) {
        boolean removed = false;
        if (element != null) {
            removed = this.elements.remove(element.getId()) != null;
            element.setGraph(null);
            if (removed) {
                element.removePropertyListener(this.globalListener);
                changeSupport
                        .firePropertyChange(ELEMENT_REMOVED, element, null);
            }
        }
        return removed;
    }

    /**
     * @see com.perforce.team.core.mergequest.model.IBranchGraph#getElementById(java.lang.String)
     */
    public IBranchGraphElement getElementById(String id) {
        return id != null ? this.elements.get(id) : null;
    }

    /**
     * @see com.perforce.team.core.mergequest.model.IBranchGraph#containsElement(java.lang.String)
     */
    public boolean containsElement(String id) {
        return id != null ? this.elements.containsKey(id) : false;
    }

    /**
     * @see com.perforce.team.core.mergequest.model.IBranchGraph#getElements(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public <ElementType extends IBranchGraphElement> Collection<ElementType> getElements(
            Class<ElementType> elementClass) {
        List<ElementType> typedElements = new ArrayList<ElementType>();
        if (elementClass != null) {
            for (IBranchGraphElement element : this.elements.values()) {
                if (elementClass.isInstance(element)) {
                    typedElements.add((ElementType) element);
                }
            }
        }
        return typedElements;
    }

    /**
     * @see com.perforce.team.core.mergequest.model.IBranchGraphElement#getGraph()
     */
    @Override
    public IBranchGraph getGraph() {
        return this;
    }

    /**
     * @see com.perforce.team.core.mergequest.model.IBranchGraphElement#setGraph(com.perforce.team.core.mergequest.model.IBranchGraph)
     */
    @Override
    public void setGraph(IBranchGraph graph) {
        // Ignore
    }

    /**
     * @see com.perforce.team.core.mergequest.model.IBranchGraph#getBranch(java.lang.String)
     */
    public Branch getBranch(String id) {
        return getElementById(id, Branch.class);
    }

    /**
     * @see com.perforce.team.core.mergequest.model.IBranchGraph#getElementById(java.lang.String,
     *      java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public <ElementType extends IBranchGraphElement> ElementType getElementById(
            String id, Class<ElementType> elementClass) {
        ElementType element = null;
        if (elementClass != null) {
            IBranchGraphElement candidate = getElementById(id);
            if (candidate != null && elementClass.isInstance(candidate)) {
                element = (ElementType) candidate;
            }
        }
        return element;
    }

    /**
     * @see com.perforce.team.core.mergequest.model.IBranchGraph#getMapping(java.lang.String)
     */
    public Mapping getMapping(String id) {
        return getElementById(id, Mapping.class);
    }

    /**
     * @see com.perforce.team.core.mergequest.model.IBranchGraph#getElements()
     */
    public IBranchGraphElement[] getElements() {
        return this.elements.values().toArray(
                new IBranchGraphElement[this.elements.size()]);
    }

    /**
     * @see com.perforce.team.core.mergequest.model.IBranchGraph#updateFactories()
     */
    public void updateFactories() {
        for (IBranchGraphElement element : getElements()) {
            if (element instanceof Branch) {
                this.branchFactory.update(element.getId());
            } else if (element instanceof DepotPathMapping) {
                this.depotMappingFactory.update(element.getId());
            } else if (element instanceof BranchSpecMapping) {
                this.branchMappingFactory.update(element.getId());
            }
        }
    }

    /**
     * Does this graph contain a branch at the specified point?
     * 
     * @param point
     * @param branches
     * @return true if contains branch at point, false otherwise
     */
    protected boolean containsBranchAtPoint(Point point, Branch[] branches) {
        for (Branch branch : branches) {
            if (branch.containsPoint(point)) {
                return true;
            }
        }
        return false;
    }

    private void translatePoint(Point point, int[] coords, double factor) {
        point.translate((int) (coords[0] * factor), (int) (coords[1] * factor));
    }

    /**
     * @see com.perforce.team.core.mergequest.model.IBranchGraph#findEmptyLocation(java.awt.Point,
     *      int)
     */
    public Point findEmptyLocation(Point startingLocation, int increment) {
        if (startingLocation == null) {
            startingLocation = new Point(100, 100);
        }
        if (increment <= 0) {
            increment = 25;
        }
        Point empty = new Point(startingLocation);
        Branch[] branches = getBranches();
        if (!containsBranchAtPoint(empty, branches)) {
            return empty;
        }

        double factor = 1;

        int[][] combinations = new int[8][2];

        combinations[0][0] = increment;
        combinations[0][1] = 0;

        combinations[1][0] = increment;
        combinations[1][1] = increment;

        combinations[2][0] = 0;
        combinations[2][1] = increment;

        combinations[3][0] = -increment;
        combinations[3][1] = increment;

        combinations[4][0] = -increment;
        combinations[4][1] = 0;

        combinations[5][0] = -increment;
        combinations[5][1] = -increment;

        combinations[6][0] = 0;
        combinations[6][1] = -increment;

        combinations[7][0] = increment;
        combinations[7][1] = -increment;

        boolean pointTaken = true;
        int trial = 0;
        int[] increments = null;
        while (pointTaken) {
            pointTaken = containsBranchAtPoint(empty, branches);
            if (pointTaken) {
                // Undo last translation
                if (increments != null) {
                    translatePoint(empty, increments, factor * -1);
                }
                if (trial == combinations.length) {
                    factor += .5;
                    trial = 0;
                }
                increments = combinations[trial];
                translatePoint(empty, increments, factor);
                trial++;
            }
        }
        return empty;
    }
}
