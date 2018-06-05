/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.text.timelapse;

import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.ui.text.PerforceUiTextPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class AgingPainter extends ColorPainter {

    /**
     * DEFAULT_AGING_START
     */
    public static final RGB DEFAULT_MOST_RECENT = new RGB(88, 164, 80);

    /**
     * DEFAULT_AGING_END
     */
    public static final RGB DEFAULT_LEAST_RECENT = new RGB(220, 255, 220);

    private Map<RGB, Color> agingCache = new HashMap<RGB, Color>();

    private RGB mostRecent = DEFAULT_MOST_RECENT;
    private RGB leastRecent = DEFAULT_LEAST_RECENT;

    private ITextAnnotateModel model;
    private ITextViewer viewer;

    /**
     * Aging paint constructor.
     * 
     * @param model
     * @param viewer
     */
    public AgingPainter(ITextAnnotateModel model, ITextViewer viewer) {

        IPreferenceStore store = PerforceUiTextPlugin.getDefault()
                .getPreferenceStore();
        this.leastRecent = PreferenceConverter.getColor(store,
                TextTimeLapseEditor.LEAST_RECENT_COLOR);
        this.mostRecent = PreferenceConverter.getColor(store,
                TextTimeLapseEditor.MOST_RECENT_COLOR);
        this.model = model;
        this.viewer = viewer;
    }

    /**
     * Set most recent
     * 
     * @param mostRecent
     */
    public void setMostRecent(RGB mostRecent) {
        this.mostRecent = mostRecent;
    }

    /**
     * Set least recent
     * 
     * @param leastRecent
     */
    public void setLeastRecent(RGB leastRecent) {
        this.leastRecent = leastRecent;
    }

    private RGB getRGB(int start, int end) {
        int red = start * leastRecent.red / end + (end - start)
                * mostRecent.red / end;
        int green = start * leastRecent.green / end + (end - start)
                * mostRecent.green / end;
        int blue = start * leastRecent.blue / end + (end - start)
                * mostRecent.blue / end;
        return new RGB(red, green, blue);
    }

    private Color getAgingColor(int endPos, ILineRange range,
            IP4Revision revision) {
        int startPos = this.model.getPositionTo(range.getStartLine(), revision) + 1;
        RGB rgb = getRGB(startPos, endPos);
        Color aged = this.agingCache.get(rgb);
        if (aged == null) {
            aged = new Color(viewer.getTextWidget().getDisplay(), rgb);
            this.agingCache.put(rgb, aged);
        }
        return aged;
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.ColorPainter#dispose()
     */
    @Override
    public void dispose() {
        super.dispose();
        for (Color aged : this.agingCache.values()) {
            aged.dispose();
        }
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.ColorPainter#internalLoadColors(java.lang.Object)
     */
    @Override
    protected Color[] internalLoadColors(Object data) {
        if (data instanceof IP4Revision) {
            IP4Revision revision = (IP4Revision) data;
            List<Color> agedList = new ArrayList<Color>();
            int endPos = model.getRevisionCount() + 1;
            ILineRange[] chunks = this.model.getLineRanges(revision);
            for (ILineRange chunk : chunks) {
                Color aged = getAgingColor(endPos, chunk, revision);
                for (int i = 0; i < chunk.getNumberOfLines(); i++) {
                    agedList.add(aged);
                }
            }
            return agedList.toArray(new Color[agedList.size()]);
        }
        return null;
    }

}
