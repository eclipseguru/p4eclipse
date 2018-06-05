/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.properties;

import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.IBranchGraphElement;
import com.perforce.team.core.mergequest.model.Mapping;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class MappingPropertySource implements IPropertySource {

    /**
     * SOURCE_NAME
     */
    public static final String SOURCE_NAME = "SOURCE_NAME"; //$NON-NLS-1$

    /**
     * TARGET_NAME
     */
    public static final String TARGET_NAME = "TARGET_NAME"; //$NON-NLS-1$

    /**
     * Id value
     */
    public static final String ID = "id"; //$NON-NLS-1$

    private IPropertyDescriptor[] descriptors = null;
    private Mapping mapping;

    /**
     * Create a new mapping property source
     * 
     * @param mapping
     */
    public MappingPropertySource(Mapping mapping) {
        this.mapping = mapping;
    }

    /**
     * @see org.eclipse.ui.views.properties.IPropertySource#getEditableValue()
     */
    public Object getEditableValue() {
        return null;
    }

    /**
     * @see org.eclipse.ui.views.properties.IPropertySource#getPropertyDescriptors()
     */
    public IPropertyDescriptor[] getPropertyDescriptors() {
        if (this.descriptors == null) {
            List<IPropertyDescriptor> descriptorList = new ArrayList<IPropertyDescriptor>();
            descriptorList.add(new PropertyDescriptor(IBranchGraphElement.NAME,
                    Messages.MappingPropertySource_Name));
            descriptorList.add(new PropertyDescriptor(SOURCE_NAME,
                    Messages.MappingPropertySource_SourceName));
            descriptorList.add(new PropertyDescriptor(TARGET_NAME,
                    Messages.MappingPropertySource_TargetName));
            descriptorList.add(new PropertyDescriptor(
                    Mapping.LATEST_SOURCE_CHANGE,
                    Messages.MappingPropertySource_LatestSource));
            descriptorList.add(new PropertyDescriptor(
                    Mapping.LATEST_TARGET_CHANGE,
                    Messages.MappingPropertySource_LatestTarget));
            descriptorList.add(new PropertyDescriptor(
                    Mapping.SOURCE_CHANGE_COUNT,
                    Messages.MappingPropertySource_SourceCount));
            descriptorList.add(new PropertyDescriptor(
                    Mapping.TARGET_CHANGE_COUNT,
                    Messages.MappingPropertySource_TargetCount));
            descriptorList.add(new PropertyDescriptor(Mapping.DIRECTION,
                    Messages.MappingPropertySource_Direction));
            descriptorList.add(new PropertyDescriptor(ID,
                    Messages.MappingPropertySource_Id));
            this.descriptors = descriptorList
                    .toArray(new IPropertyDescriptor[descriptorList.size()]);
        }
        return this.descriptors;
    }

    /**
     * @see org.eclipse.ui.views.properties.IPropertySource#getPropertyValue(java.lang.Object)
     */
    public Object getPropertyValue(Object id) {
        Object value = null;
        if (IBranchGraphElement.NAME.equals(id)) {
            value = mapping.getName();
        } else if (Mapping.LATEST_SOURCE_CHANGE.equals(id)) {
            value = mapping.getLatestSource();
        } else if (Mapping.LATEST_TARGET_CHANGE.equals(id)) {
            value = mapping.getLatestTarget();
        } else if (Mapping.SOURCE_CHANGE_COUNT.equals(id)) {
            value = mapping.getTargetToSourceCount();
        } else if (Mapping.TARGET_CHANGE_COUNT.equals(id)) {
            value = mapping.getSourceToTargetCount();
        } else if (Mapping.DIRECTION.equals(id)) {
            value = mapping.getDirection().toString();
        } else if (SOURCE_NAME.equals(id)) {
            Branch branch = mapping.getSource();
            if (branch != null) {
                value = branch.getName();
            }
        } else if (TARGET_NAME.equals(id)) {
            Branch branch = mapping.getTarget();
            if (branch != null) {
                value = branch.getName();
            }
        } else if (ID.equals(id)) {
            value = mapping.getId();
        }
        return value;
    }

    /**
     * @see org.eclipse.ui.views.properties.IPropertySource#isPropertySet(java.lang.Object)
     */
    public boolean isPropertySet(Object id) {
        return false;
    }

    /**
     * @see org.eclipse.ui.views.properties.IPropertySource#resetPropertyValue(java.lang.Object)
     */
    public void resetPropertyValue(Object id) {

    }

    /**
     * @see org.eclipse.ui.views.properties.IPropertySource#setPropertyValue(java.lang.Object,
     *      java.lang.Object)
     */
    public void setPropertyValue(Object id, Object value) {

    }

}
