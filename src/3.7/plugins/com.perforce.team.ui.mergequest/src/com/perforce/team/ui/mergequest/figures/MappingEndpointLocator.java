/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.figures;

import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.ConnectionLocator;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class MappingEndpointLocator extends ConnectionLocator {

    private int xOffset = 20;
    private int yOffset = 20;

    /**
     * @param connection
     */
    public MappingEndpointLocator(Connection connection) {
        super(connection);
    }

    /**
     * @param connection
     * @param alignment
     */
    public MappingEndpointLocator(Connection connection, int alignment) {
        super(connection, alignment);
    }

    /**
     * Get target point
     * 
     * @param p1
     * @param p2
     * @param useDistance
     * @return target point
     */
    protected Point getTargetPoint(Point p1, Point p2, boolean useDistance) {
        double angle = computeAngle(p1, p2);
        Point point = convert(p1, p2, Math.cos(angle), Math.sin(angle),
                useDistance);
        point.negate().translate(p2);
        return point;
    }

    /**
     * Get source point
     * 
     * @param p1
     * @param p2
     * @param useDistance
     * @return source point
     */
    protected Point getSourcePoint(Point p1, Point p2, boolean useDistance) {
        double angle = computeAngle(p1, p2);
        angle = Math.PI / 2.0 - angle;
        Point point = convert(p1, p2, Math.sin(angle), Math.cos(angle),
                useDistance);
        point.translate(p1);
        return point;
    }

    /**
     * Compute angle of two points
     * 
     * @param p1
     * @param p2
     * @return angle in radians
     */
    protected double computeAngle(Point p1, Point p2) {
        double opp = Math.abs(p1.y - p2.y);
        double adj = Math.abs(p2.x - p1.x);
        double angle = 0.0;
        if (adj != 0) {
            angle = Math.atan(opp / adj);
        } else {
            angle = Math.PI / 2;
        }
        return angle;
    }

    /**
     * Generate a converted location that takes into account the x and y offsets
     * as well as the conversion factors
     * 
     * @param p1
     * @param p2
     * @param xConversion
     * @param yConversion
     * @param useDistance
     * @return converted point
     */
    protected Point convert(Point p1, Point p2, double xConversion,
            double yConversion, boolean useDistance) {
        int segments = useDistance ? 4 : 2;
        int apart = (int) (Math.abs(p1.getDistance(p2)) / segments);
        double tx = Math.min(xOffset, apart) * xConversion;
        double ty = Math.min(yOffset, apart) * yConversion;
        if (p1.x > p2.x) {
            tx *= -1.0;
        }
        if (p1.y > p2.y) {
            ty *= -1.0;
        }
        return new Point(tx, ty);
    }

    /**
     * @see org.eclipse.draw2d.ConnectionLocator#getLocation(org.eclipse.draw2d.geometry.PointList)
     */
    @Override
    protected Point getLocation(PointList points) {
        int alignment = getAlignment();
        Point p1 = points.getFirstPoint();
        Point p2 = points.getLastPoint();
        Point point = null;
        if (alignment == TARGET) {
            boolean useDistance = true;
            if (points.size() >= 3) {
                p1 = points.getPoint(points.size() - 2);
                useDistance = false;
            }
            point = getTargetPoint(p1, p2, useDistance);
        } else if (alignment == SOURCE) {
            boolean useDistance = true;
            if (points.size() >= 3) {
                p2 = points.getPoint(1);
                useDistance = false;
            }
            point = getSourcePoint(p1, p2, useDistance);
        } else {
            point = super.getLocation(points);
        }
        return point;
    }

    /**
     * @return the xOffset
     */
    public int getXOffset() {
        return this.xOffset;
    }

    /**
     * @param xOffset
     *            the xOffset to set
     */
    public void setXOffset(int xOffset) {
        this.xOffset = xOffset;
    }

    /**
     * @return the yOffset
     */
    public int getYOffset() {
        return this.yOffset;
    }

    /**
     * @param yOffset
     *            the yOffset to set
     */
    public void setYOffset(int yOffset) {
        this.yOffset = yOffset;
    }

    /**
     * Set offsets
     * 
     * @param xOffset
     * @param yOffset
     */
    public void setOffsets(int xOffset, int yOffset) {
        setXOffset(xOffset);
        setYOffset(yOffset);
    }
}
