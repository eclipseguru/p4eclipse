/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.text.timelapse;

import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.ui.text.timelapse.INodeModel.ChangeType;
import com.perforce.team.ui.timelapse.ITickFormatter;
import com.perforce.team.ui.timelapse.ITickPositionHandler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class NodeTickFormatter implements ITickFormatter, ITickPositionHandler {

    /**
     * TICK_BG
     */
    public static final RGB TICK_BG = new RGB(0, 128, 0);

    private INodeModel model;
    private ChangeType[] types;
    private ChangeType[] filteredTypes;
    private boolean enabled = false;
    private boolean filter = false;
    private Color color = null;
    private String currentId = null;

    /**
     * Create a node tick formatter
     * 
     * @param model
     * @param color
     */
    public NodeTickFormatter(INodeModel model, Color color) {
        this.model = model;
        this.color = color;
    }

    /**
     * Create a node tick formatter
     * 
     * @param model
     */
    public NodeTickFormatter(INodeModel model) {
        this(model, null);
    }

    /**
     * @return the color
     */
    public Color getColor() {
        return this.color;
    }

    /**
     * @param color
     *            the color to set
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Get current node id
     * 
     * @return - node id
     */
    public String getId() {
        return this.currentId;
    }

    /**
     * Set the node id to filter on. This method updates the return value of
     * {@link #isEnabled()} by setting enabled if id is non-null and false if id
     * is null.
     * 
     * @param id
     */
    public void setFilter(String id) {
        this.currentId = id;
        if (id != null) {
            this.enabled = true;
            this.types = this.model.getRecords(id);
            this.filteredTypes = filter(this.types);
        } else {
            this.enabled = false;
            this.types = null;
            this.filteredTypes = null;
        }
    }

    /**
     * @see com.perforce.team.ui.timelapse.ITickFormatter#format(com.perforce.team.core.p4java.IP4Revision,
     *      int, org.eclipse.swt.events.PaintEvent)
     */
    public Color format(IP4Revision revision, int tickNumber, PaintEvent event) {
        if (filteredTypes != null && this.color != null) {
            switch (filteredTypes[tickNumber]) {
            case ADD:
            case DELETE:
            case EDIT:
                return this.color;
            case UNCHANGED:
            default:
                return event.display.getSystemColor(SWT.COLOR_DARK_GRAY);
            }
        }
        return event.display.getSystemColor(SWT.COLOR_DARK_GRAY);
    }

    /**
     * @see com.perforce.team.ui.timelapse.ITickPositionHandler#getNext(int)
     */
    public int getNext(int position) {
        int next = -1;
        if (filteredTypes != null) {
            for (int i = position + 1; i < filteredTypes.length; i++) {
                if (filteredTypes[i] != ChangeType.UNCHANGED) {
                    next = i;
                    break;
                }
            }
        }
        return next;
    }

    /**
     * @see com.perforce.team.ui.timelapse.ITickPositionHandler#getPrevious(int)
     */
    public int getPrevious(int position) {
        int previous = -1;
        if (filteredTypes != null) {
            for (int i = position - 1; i >= 0; i--) {
                if (filteredTypes[i] != ChangeType.UNCHANGED) {
                    previous = i;
                    break;
                }
            }
        }
        return previous;
    }

    /**
     * @see com.perforce.team.ui.timelapse.ITickPositionHandler#hasNextPosition(int)
     */
    public boolean hasNextPosition(int position) {
        return getNext(position) != -1;
    }

    /**
     * @see com.perforce.team.ui.timelapse.ITickPositionHandler#hasPreviousPosition(int)
     */
    public boolean hasPreviousPosition(int position) {
        return getPrevious(position) != -1;
    }

    /**
     * @see com.perforce.team.ui.timelapse.ITickPositionHandler#contains(int)
     */
    public boolean contains(int position) {
        boolean contains = false;
        if (filteredTypes != null && position >= 0
                && position < filteredTypes.length) {
            contains = filteredTypes[position] != ChangeType.UNCHANGED;
        }
        return contains;
    }

    /**
     * Set node tick formatter enablement
     * 
     * @param enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @see com.perforce.team.ui.timelapse.ITickPositionHandler#isEnabled()
     */
    public boolean isEnabled() {
        return this.enabled && this.filteredTypes != null;
    }

    /**
     * @return the filter
     */
    public boolean isFilter() {
        return this.filter;
    }

    /**
     * @param filter
     *            the filter to set
     */
    public void setFilter(boolean filter) {
        this.filter = filter;
    }

    /**
     * @see com.perforce.team.ui.timelapse.ITickPositionHandler#getTickCount()
     */
    public int getTickCount() {
        int count = -1;
        if (this.filteredTypes != null) {
            count = 0;
            for (ChangeType type : this.filteredTypes) {
                if (type != ChangeType.UNCHANGED) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Filter the change types to an array of non-unchanged change types
     */
    public void filter() {
        this.filteredTypes = filter(this.types);
    }

    private ChangeType[] filter(ChangeType[] types) {
        ChangeType[] newFiltered = null;
        if (filter) {
            if (types != null) {
                List<ChangeType> filtered = new ArrayList<ChangeType>();
                for (int i = 0; i < types.length; i++) {
                    if (types[i] != ChangeType.UNCHANGED) {
                        filtered.add(types[i]);
                    }
                }
                newFiltered = filtered.toArray(new ChangeType[filtered.size()]);
            }
        } else {
            newFiltered = types;
        }
        return newFiltered;
    }

    /**
     * @see com.perforce.team.ui.timelapse.ITickPositionHandler#filter(com.perforce.team.core.p4java.IP4Revision[])
     */
    public IP4Revision[] filter(IP4Revision[] revisions) {
        if (filter && types != null && types.length == revisions.length) {
            List<IP4Revision> filtered = new ArrayList<IP4Revision>();
            List<ChangeType> filteredTypes = new ArrayList<ChangeType>();
            for (int i = 0; i < types.length; i++) {
                if (types[i] != ChangeType.UNCHANGED) {
                    filtered.add(revisions[i]);
                    filteredTypes.add(types[i]);
                }
            }
            this.filteredTypes = filteredTypes
                    .toArray(new ChangeType[filteredTypes.size()]);
            revisions = filtered.toArray(new IP4Revision[filtered.size()]);
        }
        return revisions;
    }

    /**
     * 
     * @see com.perforce.team.ui.timelapse.ITickPositionHandler#getNewPosition()
     */
    public int getNewPosition() {
        int nonDelete = -1;
        if (filter && filteredTypes != null) {
            ChangeType curr = null;
            for (int position = filteredTypes.length - 1; position >= 0; position--) {
                curr = filteredTypes[position];
                if (curr != ChangeType.DELETE && curr != ChangeType.UNCHANGED) {
                    nonDelete = position;
                    break;
                }
            }
        }
        return nonDelete;
    }
}
