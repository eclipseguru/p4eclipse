/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.properties;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.mergequest.commands.BranchConstraintCommand;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchPropertySource implements IPropertySource {

    /**
     * Name
     */
    public static final String NAME = "name"; //$NON-NLS-1$

    /**
     * X coordinate
     */
    public static final String X = "x"; //$NON-NLS-1$

    /**
     * Y coordinate
     */
    public static final String Y = "y"; //$NON-NLS-1$

    /**
     * Width value
     */
    public static final String WIDTH = "width"; //$NON-NLS-1$

    /**
     * Height value
     */
    public static final String HEIGHT = "height"; //$NON-NLS-1$

    /**
     * Type value
     */
    public static final String TYPE = "type"; //$NON-NLS-1$

    /**
     * Id value
     */
    public static final String ID = "id"; //$NON-NLS-1$

    private Branch branch = null;
    private IPropertyDescriptor[] descriptors;

    /**
     * Create a new branch property source
     * 
     * @param branch
     */
    public BranchPropertySource(Branch branch) {
        this.descriptors = null;
        this.branch = branch;
    }

    /**
     * @see org.eclipse.ui.views.properties.IPropertySource#getEditableValue()
     */
    public Object getEditableValue() {
        return this.branch;
    }

    /**
     * @see org.eclipse.ui.views.properties.IPropertySource#getPropertyDescriptors()
     */
    public IPropertyDescriptor[] getPropertyDescriptors() {
        if (this.descriptors == null) {
            List<IPropertyDescriptor> descriptorList = new ArrayList<IPropertyDescriptor>();
            descriptorList.add(new PropertyDescriptor(NAME,
                    Messages.BranchPropertySource_Name));
            descriptorList.add(new TextPropertyDescriptor(X,
                    Messages.BranchPropertySource_X));
            descriptorList.add(new TextPropertyDescriptor(Y,
                    Messages.BranchPropertySource_Y));
            descriptorList.add(new TextPropertyDescriptor(WIDTH,
                    Messages.BranchPropertySource_Width));
            descriptorList.add(new TextPropertyDescriptor(HEIGHT,
                    Messages.BranchPropertySource_Height));
            descriptorList.add(new PropertyDescriptor(TYPE,
                    Messages.BranchPropertySource_Type));
            descriptorList.add(new PropertyDescriptor(ID,
                    Messages.BranchPropertySource_Id));
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
        if (NAME.equals(id)) {
            value = this.branch.getName();
        } else if (X.equals(id)) {
            value = this.branch.getX();
        } else if (Y.equals(id)) {
            value = this.branch.getY();
        } else if (WIDTH.equals(id)) {
            value = this.branch.getWidth();
        } else if (HEIGHT.equals(id)) {
            value = this.branch.getHeight();
        } else if (TYPE.equals(id)) {
            value = this.branch.getType();
        } else if (ID.equals(id)) {
            value = this.branch.getId();
        }
        if (value != null) {
            value = value.toString();
        }
        return value;
    }

    /**
     * @see org.eclipse.ui.views.properties.IPropertySource#isPropertySet(java.lang.Object)
     */
    public boolean isPropertySet(Object id) {
        return true;
    }

    /**
     * @see org.eclipse.ui.views.properties.IPropertySource#resetPropertyValue(java.lang.Object)
     */
    public void resetPropertyValue(Object id) {

    }

    private Integer getInteger(Object value) {
        Integer integer = null;
        if (value != null) {
            try {
                integer = Integer.valueOf(value.toString());
            } catch (NumberFormatException nfe) {
                integer = null;
            }
        }
        return integer;
    }

    private void updateSize(int width, int height) {
        changeConstraints(RequestConstants.REQ_RESIZE, branch.getX(),
                branch.getY(), width, height);
    }

    private void updateLocation(int x, int y) {
        changeConstraints(RequestConstants.REQ_MOVE, x, y, branch.getWidth(),
                branch.getHeight());
    }

    private void changeConstraints(String type, int x, int y, int width,
            int height) {
        Rectangle rectangle = new Rectangle(x, y, width, height);
        BranchConstraintCommand command = new BranchConstraintCommand(branch,
                new ChangeBoundsRequest(type), rectangle);
        CommandStack stack = P4CoreUtils.convert(PerforceUIPlugin
                .getActivePage().getActiveEditor(), CommandStack.class);
        if (stack != null) {
            stack.execute(command);
        } else {
            command.execute();
        }
    }

    /**
     * @see org.eclipse.ui.views.properties.IPropertySource#setPropertyValue(java.lang.Object,
     *      java.lang.Object)
     */
    public void setPropertyValue(Object id, Object value) {
        if (X.equals(id)) {
            Integer intValue = getInteger(value);
            if (intValue != null) {
                updateLocation(intValue.intValue(), branch.getY());
            }
        } else if (Y.equals(id)) {
            Integer intValue = getInteger(value);
            if (intValue != null) {
                updateLocation(branch.getX(), intValue.intValue());
            }
        } else if (WIDTH.equals(id)) {
            Integer intValue = getInteger(value);
            if (intValue != null) {
                updateSize(intValue.intValue(), branch.getHeight());
            }
        } else if (HEIGHT.equals(id)) {
            Integer intValue = getInteger(value);
            if (intValue != null) {
                updateSize(branch.getWidth(), intValue.intValue());
            }
        }
    }
}
