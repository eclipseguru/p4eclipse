/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.editor;

import com.perforce.team.core.folder.IP4DiffFile;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.folder.PerforceUiFolderPlugin;
import com.perforce.team.ui.folder.diff.model.FileDiffContainer;
import com.perforce.team.ui.folder.diff.model.FileDiffElement;
import com.perforce.team.ui.folder.diff.model.FileEntry;
import com.perforce.team.ui.folder.diff.viewer.DiffArea;
import com.perforce.team.ui.folder.preferences.IPreferenceConstants;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class LinkCanvas implements IPropertyChangeListener {

    private class Pair {

        boolean source = true;
        ItemSettings left = new ItemSettings();
        ItemSettings right = new ItemSettings();

        public Pair(boolean source) {
            this.source = source;
        }

        public boolean isValid() {
            return left.item != null && right.item != null
                    && left.item.getParent() != right.item.getParent();
        }

        public boolean isSelected() {
            if (!left.noPair && !right.noPair) {
                return leftSelected == left.item && rightSelected == right.item;
            }
            if (left.noPair) {
                return rightSelected == right.item;
            }
            if (right.noPair) {
                return leftSelected == left.item;
            }
            return false;
        }

        public Color getBackground() {
            if (isSelected()) {
                return left.item.getDisplay().getSystemColor(
                        SWT.COLOR_LIST_SELECTION);
            } else {
                if (this.source) {
                    return left.item.getBackground(0);
                } else {
                    return right.item.getBackground(0);
                }
            }
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (obj instanceof Pair) {
                return left.equals(((Pair) obj).left)
						&& right.equals(((Pair) obj).right)
						&& super.equals(obj);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
        	int hash = 0;
        	if(this.left!=null)
        		hash+=this.left.hashCode();
        	if(this.right!=null)
        		hash+=this.right.hashCode()*31;
        	
        	if(hash>0)
        		return hash;
        	
        	return super.hashCode();
        }

    }

    private static class ItemSettings {

        Rectangle bounds = null;
        boolean noPair = false;
        TreeItem item;

        private Rectangle getBounds(TreeItem item) {
            if (bounds == null) {
                Rectangle newBounds = item.getBounds();
                if (newBounds.height <= 0) {
                    item = item.getParentItem();
                    if (item != null) {
                        newBounds = getBounds(item);
                    }
                }
                if (noPair) {
                    newBounds.y += newBounds.height;
                    newBounds.height = 1;
                }
                bounds = newBounds;
            }
            return bounds;
        }

        Rectangle getBounds() {
            return getBounds(this.item);
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (obj instanceof ItemSettings) {
                return this.item.getData().equals(
                        ((ItemSettings) obj).item.getData());
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
        	if(this.item.getData()!=null)
        		return this.item.getData().hashCode();
        	return super.hashCode();
        }

    }

    private Image buffer;
    private DiffArea leftArea;
    private DiffArea rightArea;
    private Tree leftTree;
    private Tree rightTree;
    private TreeItem leftSelected;
    private TreeItem rightSelected;
    private int maxY = -1;
    private Canvas canvas;
    private boolean enabled = true;

    private Map<TreeItem, Pair> settings = new LinkedHashMap<TreeItem, Pair>();
    private FileDiffContainer container;

    /**
     * Create link canvas
     * 
     * @param parent
     */
    public LinkCanvas(Composite parent) {
        canvas = new Canvas(parent, SWT.NO_FOCUS | SWT.DOUBLE_BUFFERED);
        canvas.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                dispose();
            }
        });
        canvas.addPaintListener(new PaintListener() {

            public void paintControl(PaintEvent e) {
                Point size = canvas.getSize();
                if (buffer != null) {
                    Rectangle r = buffer.getBounds();
                    if (r.width != size.x || r.height != size.y) {
                        buffer.dispose();
                        buffer = null;
                    }
                }
                if (buffer == null) {
                    buffer = new Image(e.display, size.x, size.y);
                }

                GC gc = new GC(buffer);
                try {
                    gc.setBackground(e.gc.getBackground());
                    gc.fillRectangle(0, 0, size.x, size.y);
                    paint(gc);
                    e.gc.drawImage(buffer, 0, 0);
                } finally {
                    gc.dispose();
                }
            }
        });
        GridData data = GridDataFactory.swtDefaults()
                .hint(getCenterWidth(), -1).grab(false, true)
                .align(SWT.BEGINNING, SWT.FILL).create();
        canvas.setLayoutData(data);
        PerforceUiFolderPlugin.getDefault().getPreferenceStore()
                .addPropertyChangeListener(this);
    }

    /**
     * Set container model
     * 
     * @param container
     */
    public void setContainer(FileDiffContainer container) {
        this.container = container;
    }

    private void dispose() {
        PerforceUiFolderPlugin.getDefault().getPreferenceStore()
                .removePropertyChangeListener(LinkCanvas.this);
    }

    /**
     * Get control
     * 
     * @return canvas
     */
    public Canvas getControl() {
        return this.canvas;
    }

    /**
     * Get center width
     * 
     * @return width
     */
    protected int getCenterWidth() {
        return 34;
    }

    private void paintPair(Pair pair, int leftOffset, int rightOffset, int x1,
            int x2, GC g) {
        Rectangle leftBounds = pair.left.getBounds();
        Rectangle rightBounds = pair.right.getBounds();

        int y1 = leftBounds.y + leftOffset + 1;
        int y2 = rightBounds.y + rightOffset + 1;
        int h1 = leftBounds.height;
        int h2 = rightBounds.height;
        int control = (x2 - x1) / 2;

        Path path = new Path(g.getDevice());
        try {
            path.moveTo(x1, y1);
            path.cubicTo(control, y1, control, y2, x2, y2);
            path.lineTo(x2, y2 + h2);
            path.cubicTo(control, y2 + h2, control, y1 + h1, x1, y1 + h1);
            path.lineTo(x1, y1);
            g.setBackground(pair.getBackground());
            g.fillPath(path);
        } finally {
            path.dispose();
        }
    }

    private void paint(GC g) {
        if (!enabled) {
            return;
        }
        if (settings.isEmpty()) {
            return;
        }
        g.setLineWidth(0);
        int x1 = 0;
        int x2 = canvas.getSize().x;
        int leftOffset = leftArea.getTreeOffset();
        int rightOffset = rightArea.getTreeOffset();
        if (maxY == -1) {
            updateClipping();
        }
        int lo = leftOffset > 0 ? leftOffset : 0;
        int ro = rightOffset > 0 ? rightOffset : 0;
        if (maxY != -1) {
            g.setClipping(x1, Math.max(lo, ro), x2, maxY);
        }

        // Adjust for scrolled area in trees
        lo += this.leftTree.getLocation().y;
        ro += this.rightTree.getLocation().y;

        Pair last = null;
        for (Pair setting : this.settings.values()) {
            paintPair(setting, lo, ro, x1, x2, g);
            if (setting.isSelected()) {
                last = setting;
            }
        }

        // Paint selected pair last
        if (last != null) {
            paintPair(last, lo, ro, x1, x2, g);
        }
    }

    private void updateClipping() {
        maxY = leftTree.getParent().getParent().getSize().y;
    }

    /**
     * Set trees
     * 
     * @param left
     * @param right
     */
    public void setDiffAreas(DiffArea left, DiffArea right) {
        if (this.leftArea != null) {
            return;
        }
        this.leftArea = left;
        this.rightArea = right;
        TreeViewer leftViewer = left.getViewer().getViewer();
        TreeViewer rightViewer = right.getViewer().getViewer();
        this.leftTree = leftViewer.getTree();
        this.rightTree = rightViewer.getTree();
        refresh();
        leftTree.getParent().addControlListener(new ControlAdapter() {

            @Override
            public void controlResized(ControlEvent e) {
                updateClipping();
            }

        });
        TreeListener expansionListener = new TreeListener() {

            public void treeExpanded(TreeEvent e) {
                ((TreeItem) e.item).setExpanded(true);
                refresh();
            }

            public void treeCollapsed(TreeEvent e) {
                ((TreeItem) e.item).setExpanded(false);
                refresh();
            }
        };
        leftTree.addTreeListener(expansionListener);
        rightTree.addTreeListener(expansionListener);

        ISelectionChangedListener postSelectionListener = new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                updateSelection();
                redraw();
            }
        };
        leftViewer.addPostSelectionChangedListener(postSelectionListener);
        rightViewer.addPostSelectionChangedListener(postSelectionListener);
    }

    private void updateSelection() {
        TreeItem[] selected = leftTree.getSelection();
        if (selected.length == 0) {
            leftSelected = null;
        } else {
            leftSelected = selected[0];
        }
        selected = rightTree.getSelection();
        if (selected.length == 0) {
            rightSelected = null;
        } else {
            rightSelected = selected[0];
        }
    }

    /**
     * Refresh the items displaying links for
     */
    public void refresh() {
        if (this.leftTree != null) {
            this.settings.clear();
            updateItems(this.leftTree.getItems(), rightArea.getViewer()
                    .getViewer(), true);
            updateItems(this.rightTree.getItems(), leftArea.getViewer()
                    .getViewer(), false);
            redraw();
        }
    }

    private void getPair(TreeViewer viewer, TreeItem sourceItem,
            Object element, ItemSettings setting) {
        if (element == null) {
            return;
        }
        setting.item = (TreeItem) viewer.testFindItem(element);
        if (setting.item != null) {
            if (setting.noPair && element instanceof IP4DiffFile) {
                setting.item = getLastItem(setting.item);
            }
        } else {
            if (element instanceof FileDiffElement) {
                setting.noPair = true;
                findUniqueDiffPair(viewer, sourceItem, element, setting);
            }
        }
    }

    private ItemSettings getTreeItemPair(TreeItem item) {
        if (item != null) {
            Pair pair = this.settings.get(item);
            if (pair != null) {
                if (item.getParent() == pair.left.item.getParent()) {
                    return pair.right;
                } else {
                    return pair.left;
                }
            }
        }
        return null;
    }

    private void findUniqueDiffPair(TreeViewer viewer, TreeItem item,
            Object element, ItemSettings setting) {
        TreeItem parent = item.getParentItem();
        if (parent != null) {
            int index = parent.indexOf(item);
            if (index > 0) {
                item = parent.getItem(index - 1);
                ItemSettings pairSetting = getTreeItemPair(item);
                if (pairSetting != null) {
                    setting.item = pairSetting.item;
                    if (!pairSetting.noPair) {
                        setting.item = getLastItem(setting.item);
                    }
                }
                if (setting.item == null) {
                    element = getDataPair(item.getData(), setting);
                    getPair(viewer, item, element, setting);
                }
            } else {
                item = parent;
                ItemSettings pairSetting = getTreeItemPair(item);
                if (pairSetting != null) {
                    setting.item = pairSetting.item;
                }
                if (setting.item == null) {
                    element = getDataPair(item.getData(), setting);
                    if (element != null) {
                        setting.item = (TreeItem) viewer.testFindItem(element);
                    }
                }
            }
        }
    }

    private TreeItem getLastItem(TreeItem parent) {
        if (parent == null) {
            return null;
        }
        TreeItem lastChild = parent;
        int count = parent.getItemCount();
        if (parent.getExpanded() && count > 0) {
            lastChild = getLastItem(parent.getItem(count - 1));
        }
        return lastChild;
    }

    private Object getDataPair(Object data, ItemSettings setting) {
        Object pair = null;
        if (data instanceof IP4DiffFile) {
            IP4DiffFile diff = (IP4DiffFile) data;
            pair = ((IP4DiffFile) data).getPair();
            if (pair == null && this.container != null) {
                FileEntry entry = this.container.getEntry(diff);
                pair = entry.getProvider().getUniquePair(entry);
                setting.noPair = true;
            }
        } else if (data instanceof FileDiffElement) {
            pair = data;
        }
        return pair;
    }

    private void updateItems(TreeItem[] items, TreeViewer viewer,
            boolean sourceItems) {
        for (TreeItem item : items) {
            Object data = item.getData();
            if (data instanceof IP4DiffFile || data instanceof FileDiffElement) {
                Pair match = new Pair(sourceItems);
                ItemSettings setting = sourceItems ? match.right : match.left;
                getPair(viewer, item, getDataPair(data, setting), setting);
                if (sourceItems) {
                    match.left.item = item;
                } else {
                    match.right.item = item;
                }
                if (match.isValid()) {
                    if (sourceItems) {
                        this.settings.put(match.left.item, match);
                    } else {
                        this.settings.put(match.right.item, match);
                    }
                }
            }
            if (item.getExpanded()) {
                updateItems(item.getItems(), viewer, sourceItems);
            }
        }
    }

    /**
     * Redraw the canvas
     */
    public void redraw() {
        canvas.redraw();
    }

    /**
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event) {
        if (IPreferenceConstants.CONTENT_COLOR.equals(event.getProperty())
                || IPreferenceConstants.DIFF_CONTENT_COLOR.equals(event
                        .getProperty())) {
            PerforceUIPlugin.asyncExec(new Runnable() {

                public void run() {
                    if (P4UIUtils.okToUse(canvas)) {
                        redraw();
                    }
                }
            });
        }
    }

    /**
     * Set enabled
     * 
     * @param enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
