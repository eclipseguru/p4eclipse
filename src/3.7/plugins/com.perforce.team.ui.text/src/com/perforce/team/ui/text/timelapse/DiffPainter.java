/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.text.timelapse;

import com.perforce.team.ui.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class DiffPainter extends ColorPainter {

    /**
     * @see com.perforce.team.ui.text.timelapse.ColorPainter#dispose()
     */
    @Override
    public void dispose() {
        // Do not dispose since colors were obtained from editors plugin shared
        // color store
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.ColorPainter#internalLoadColors(java.lang.Object)
     */
    @Override
    protected Color[] internalLoadColors(Object data) {
        if (data instanceof ILineRange[]) {
            List<Color> colorList = new ArrayList<Color>();
            ILineRange[] chunks = (ILineRange[]) data;
            Color highlight = TextUtils
                    .getEditorColor(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR);
            Color color = null;
            for (ILineRange chunk : chunks) {
                for (int i = 0; i < chunk.getNumberOfLines(); i++) {
                    colorList.add(color);
                }
                if (color != null) {
                    color = null;
                } else {
                    color = highlight;
                }
            }
            return colorList.toArray(new Color[colorList.size()]);
        }
        return null;
    }

}
