/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mergequest.builder.xml;

import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.core.mergequest.model.IBranchGraphElement;
import com.perforce.team.core.mergequest.model.Mapping;
import com.perforce.team.core.mergequest.model.Mapping.Direction;
import com.perforce.team.core.mergequest.model.Mapping.Joint;
import com.perforce.team.core.mergequest.model.MappingDescriptor.ChangeType;

import java.util.Locale;

import org.w3c.dom.Element;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class MappingBuilder extends ElementBuilder {

    /**
     * TARGET_CHANGES_ATTRIBUTE
     */
    public static final String TARGET_CHANGES_ATTRIBUTE = "targetChanges"; //$NON-NLS-1$

    /**
     * SOURCE_CHANGES_ATTRIBUTE
     */
    public static final String SOURCE_CHANGES_ATTRIBUTE = "sourceChanges"; //$NON-NLS-1$

    /**
     * TARGET_COUNT_ATTRIBUTE
     */
    public static final String TARGET_COUNT_ATTRIBUTE = "targetCount"; //$NON-NLS-1$

    /**
     * SOURCE_COUNT_ATTRIBUTE
     */
    public static final String SOURCE_COUNT_ATTRIBUTE = "sourceCount"; //$NON-NLS-1$

    /**
     * LATEST_TARGET_ATTRIBUTE
     */
    public static final String LATEST_TARGET_ATTRIBUTE = "latestTarget"; //$NON-NLS-1$

    /**
     * LATEST_SOURCE_ATTRIBUTE
     */
    public static final String LATEST_SOURCE_ATTRIBUTE = "latestSource"; //$NON-NLS-1$

    /**
     * DIRECTION_ATTRIBUTE
     */
    public static final String DIRECTION_ATTRIBUTE = "direction"; //$NON-NLS-1$

    /**
     * SOURCE_ATTRIBUTE
     */
    public static final String SOURCE_ATTRIBUTE = "source"; //$NON-NLS-1$

    /**
     * TARGET_ATTRIBUTE
     */
    public static final String TARGET_ATTRIBUTE = "target"; //$NON-NLS-1$

    /**
     * SOURCE_ANCHOR_ATTRIBUTE
     */
    public static final String SOURCE_ANCHOR_ATTRIBUTE = "sourceAnchor"; //$NON-NLS-1$

    /**
     * TARGET_ANCHOR_ATTRIBUTE
     */
    public static final String TARGET_ANCHOR_ATTRIBUTE = "targetAnchor"; //$NON-NLS-1$

    /**
     * JOINTS_ATTRIBUTE
     */
    public static final String JOINTS_ATTRIBUTE = "joints"; //$NON-NLS-1$

    /**
     * Get change type
     * 
     * @param type
     * @return change type
     */
    protected ChangeType getType(String type) {
        ChangeType change = null;
        if (type != null) {
            try {
                change = ChangeType.valueOf(type.toUpperCase(Locale.ENGLISH));
            } catch (IllegalArgumentException e) {
                change = null;
            }
        }
        return change;
    }

    /**
     * Get direction from string
     * 
     * @param value
     * @return direction
     */
    protected Direction getDirection(String value) {
        Direction direction = null;
        if (value != null) {
            try {
                direction = Direction.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException ile) {
                direction = null;
            }
        }
        return direction;
    }

    /**
     * Save core mapping fields
     * 
     * @param element
     * @param mapping
     */
    protected void saveMappingFields(Element element, Mapping mapping) {
        element.setAttribute(IElementConstants.ID_ATTRIBUTE, mapping.getId());
        element.setAttribute(DIRECTION_ATTRIBUTE, mapping.getDirection()
                .toString());
        Branch source = mapping.getSource();
        if (source != null) {
            element.setAttribute(SOURCE_ATTRIBUTE, source.getId());
        }
        Branch target = mapping.getTarget();
        if (target != null) {
            element.setAttribute(TARGET_ATTRIBUTE, target.getId());
        }
        element.setAttribute(IElementConstants.NAME_ATTRIBUTE,
                mapping.getName());
        element.setAttribute(TARGET_COUNT_ATTRIBUTE,
                Integer.toString(mapping.getSourceToTargetCount()));
        element.setAttribute(SOURCE_COUNT_ATTRIBUTE,
                Integer.toString(mapping.getTargetToSourceCount()));
        element.setAttribute(SOURCE_CHANGES_ATTRIBUTE, mapping
                .getSourceChange().toString());
        element.setAttribute(TARGET_CHANGES_ATTRIBUTE, mapping
                .getTargetChange().toString());
        element.setAttribute(LATEST_SOURCE_ATTRIBUTE,
                Integer.toString(mapping.getLatestSource()));
        element.setAttribute(LATEST_TARGET_ATTRIBUTE,
                Integer.toString(mapping.getLatestTarget()));
        element.setAttribute(SOURCE_ANCHOR_ATTRIBUTE,
                Integer.toString(mapping.getSourceAnchor()));
        element.setAttribute(TARGET_ANCHOR_ATTRIBUTE,
                Integer.toString(mapping.getTargetAnchor()));
        StringBuilder joints = new StringBuilder();
        for (Joint joint : mapping.getJoints()) {
            joints.append(joint.toString()).append(';');
        }
        element.setAttribute(JOINTS_ATTRIBUTE, joints.toString());
    }

    /**
     * @see com.perforce.team.core.mergequest.builder.xml.ElementBuilder#complete(com.perforce.team.core.mergequest.model.IBranchGraphElement,
     *      com.perforce.team.core.mergequest.model.IBranchGraph)
     */
    @Override
    public void complete(IBranchGraphElement element, IBranchGraph graph) {
        if (element instanceof Mapping) {
            Mapping mapping = (Mapping) element;
            boolean connected = false;
            Branch source = mapping.getSource();
            Branch target = mapping.getTarget();
            if (source != null && target != null) {
                mapping.connect();
                connected = true;
            }
            if (!connected) {
                graph.remove(mapping);
            }
        }
    }

    private int getInteger(String value) throws NumberFormatException {
        if (value == null) {
            throw new NumberFormatException();
        }
        value = value.trim();
        if (value.length() == 0) {
            throw new NumberFormatException();
        }
        return Integer.parseInt(value);
    }

    /**
     * Load core mapping fields
     * 
     * @param element
     * @param mapping
     */
    protected void loadMappingFields(Element element, Mapping mapping) {
        String name = element.getAttribute(IElementConstants.NAME_ATTRIBUTE);
        mapping.setName(name);
        mapping.setDirection(getDirection(element
                .getAttribute(DIRECTION_ATTRIBUTE)));
        mapping.setTargetChanges(getType(element
                .getAttribute(TARGET_CHANGES_ATTRIBUTE)));
        mapping.setSourceChanges(getType(element
                .getAttribute(SOURCE_CHANGES_ATTRIBUTE)));
        mapping.setSourceToTargetCount(getInteger(element,
                TARGET_COUNT_ATTRIBUTE));
        mapping.setTargetToSourceCount(getInteger(element,
                SOURCE_COUNT_ATTRIBUTE));
        mapping.setLatestSource(getInteger(element, LATEST_SOURCE_ATTRIBUTE));
        mapping.setLatestTarget(getInteger(element, LATEST_TARGET_ATTRIBUTE));
        mapping.setSourceId(element.getAttribute(SOURCE_ATTRIBUTE));
        mapping.setTargetId(element.getAttribute(TARGET_ATTRIBUTE));
        mapping.setSourceAnchor(getInteger(element, SOURCE_ANCHOR_ATTRIBUTE));
        mapping.setTargetAnchor(getInteger(element, TARGET_ANCHOR_ATTRIBUTE));

        String joints = element.getAttribute(JOINTS_ATTRIBUTE);
        if (joints.length() > 0) {
            String[] segments = joints.split(";"); //$NON-NLS-1$
            for (String segment : segments) {
                String[] joint = segment.split(","); //$NON-NLS-1$
                if (joint.length == 4) {
                    try {
                        int x1 = getInteger(joint[0]);
                        int y1 = getInteger(joint[1]);
                        int x2 = getInteger(joint[2]);
                        int y2 = getInteger(joint[3]);
                        mapping.addJoint(new Joint(x1, y1, x2, y2));
                    } catch (NumberFormatException nfe) {
                        // Skip and continue
                    }
                }
            }
        }
    }

}
