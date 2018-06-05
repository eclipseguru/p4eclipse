/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.descriptors;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ElementDescriptor {

    private String name;
    private String type;
    private String description;
    private ImageDescriptor icon;
    private boolean important = false;

    /**
     * Create element descriptor
     * 
     * @param name
     * @param type
     * @param icon
     * @param description
     * @param important
     */
    public ElementDescriptor(String name, String type, ImageDescriptor icon,
            String description, boolean important) {
        this.name = name;
        this.type = type;
        this.icon = icon;
        this.description = description;
        this.important = important;
    }

    /**
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return the type
     */
    public String getType() {
        return this.type;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @return the icon
     */
    public ImageDescriptor getIcon() {
        return this.icon;
    }

    /**
     * @return true if important, false otherwise
     */
    public boolean isImportant() {
        return this.important;
    }
}
