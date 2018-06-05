/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.viewer;

import com.perforce.team.ui.PerforceUIPlugin;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class SynchronizedSlider {

    /**
     * BOTTOM_PADDING
     */
    public static final int BOTTOM_PADDING = 25;

    /**
     * Scroll session
     */
    public static class ScrollSession {

        private Tree tree;
        private int treeSize;
        private int parentSize;

        /**
         * Create scroll session for tree
         * 
         * @param tree
         */
        public ScrollSession(Tree tree) {
            this.tree = tree;
            this.treeSize = tree.getSize().y;
            this.parentSize = tree.getParent().getParent().getSize().y;
        }

        private TreeItem getNext(TreeItem current, boolean includeChildren) {
            TreeItem next = current;
            int count = current.getItemCount();
            if (includeChildren && count > 0 && current.getExpanded()) {
                // First child
                next = current.getItem(0);
            } else if (current.getParentItem() != null) {
                TreeItem parent = next.getParentItem();
                int children = parent.getItemCount();
                int index = parent.indexOf(current);
                if (children > 1 && index + 1 < children) {
                    // Next sibling
                    next = parent.getItem(index + 1);
                } else {
                    // Next sibling of parent
                    next = getNext(parent, false);
                }
            } else {
                Tree parent = next.getParent();
                int children = parent.getItemCount();
                int index = parent.indexOf(current);
                if (children > 1 && index + 1 < children) {
                    // Next sibling
                    next = parent.getItem(index + 1);
                } else {
                    next = null;
                }
            }
            return next;
        }

        private TreeItem getNext(TreeItem current) {
            return getNext(current, true);
        }

        private TreeItem getLastItem(TreeItem parent) {
            TreeItem lastChild = parent;
            int count = parent.getItemCount();
            if (parent.getExpanded() && count > 0) {
                lastChild = getLastItem(parent.getItem(count - 1));
            }
            return lastChild;
        }

        /**
         * Get first item
         * 
         * @return first tree item
         */
        public TreeItem getFirstItem() {
            TreeItem first = null;
            if (tree.getItemCount() > 0) {
                first = tree.getItem(0);
            }
            return first;
        }

        private TreeItem getPrevious(TreeItem current) {
            TreeItem previous = current;
            if (current.getParentItem() != null) {
                TreeItem parent = previous.getParentItem();
                int children = parent.getItemCount();
                int index = parent.indexOf(current);
                if (children > 1 && index - 1 >= 0) {
                    // Last child of previous sibling
                    previous = getLastItem(parent.getItem(index - 1));
                } else {
                    // Last child of parent
                    previous = parent;
                }
            } else {
                Tree parent = previous.getParent();
                int children = parent.getItemCount();
                int index = parent.indexOf(current);
                if (children > 1 && index - 1 >= 0) {
                    // Last child of previous sibling
                    previous = getLastItem(parent.getItem(index - 1));
                }
            }
            return previous;
        }

        /**
         * Scroll to the top
         * 
         * @return new top item
         */
        public TreeItem top() {
            TreeItem newTop = getFirstItem();
            if (newTop != null) {
                scroll(newTop, true);
            }
            return newTop;
        }

        private void scroll(TreeItem item, boolean force) {
            int y = item.getBounds().y;
            if (force || shouldScroll(y)) {
                tree.setLocation(0, -y);
            }
        }

        /**
         * Scroll the tree
         * 
         * @param amount
         * @param top
         * @return new top item
         */
        public TreeItem scroll(int amount, TreeItem top) {
            if (top == null) {
                return null;
            }
            TreeItem newTop = null;
            if (amount > 0) {
                for (int i = 0; i < amount; i++) {
                    newTop = getNext(top);
                    if (newTop != null && shouldScroll(newTop.getBounds().y)) {
                        top = newTop;
                    } else {
                        break;
                    }
                }
            } else if (amount < 0) {
                amount = Math.abs(amount);
                for (int i = 0; i < amount; i++) {
                    newTop = getPrevious(top);
                    if (newTop != null) {
                        top = newTop;
                    } else {
                        break;
                    }
                }
            }
            if (top != null) {
                scroll(top, false);
            }
            return top;
        }

        private boolean shouldScroll(int y) {
            if (treeSize <= parentSize) {
                return false;
            } else {
                return treeSize - y > parentSize - BOTTOM_PADDING;
            }
        }
    }

    private ListenerList listeners = new ListenerList();
    private Slider slider;
    private Tree left;
    private Tree right;
    private int previous = 0;
    private TreeItem leftTop = null;
    private TreeItem rightTop = null;
    private boolean arrowDown = false;

    final SelectionAdapter selection = new SelectionAdapter() {

        @Override
        public void widgetSelected(SelectionEvent e) {
            int newSelection = slider.getSelection();
            if (slider.isEnabled() && newSelection != previous) {
                ScrollSession leftSession = new ScrollSession(left);
                ScrollSession rightSession = new ScrollSession(right);
                if (leftTop == null) {
                    leftTop = leftSession.getFirstItem();
                }
                if (rightTop == null) {
                    rightTop = rightSession.getFirstItem();
                }
                int diff = newSelection - previous;
                if (newSelection == slider.getMinimum()) {
                    SynchronizedSlider.this.leftTop = leftSession.top();
                    SynchronizedSlider.this.rightTop = rightSession.top();
                } else {
                    SynchronizedSlider.this.leftTop = leftSession.scroll(diff,
                            leftTop);
                    SynchronizedSlider.this.rightTop = rightSession.scroll(
                            diff, rightTop);
                }
            }
            previous = newSelection;
            fireSelection(e);
        }

    };

    private MouseWheelListener wheelListener = new MouseWheelListener() {

        public void mouseScrolled(MouseEvent e) {
            if (!slider.isEnabled() || e.count == 0) {
                return;
            }
            int change = e.count > 0 ? -1 : 1;
            slider.setSelection(slider.getSelection() + change);
            Event event = new Event();
            event.widget = e.widget;
            selection.widgetSelected(new SelectionEvent(event));
        }
    };

    private ControlAdapter sizeListener = new ControlAdapter() {

        @Override
        public void controlResized(ControlEvent e) {
            refresh();
        }

    };

    private TreeListener expansionListener = new TreeListener() {

        public void treeExpanded(TreeEvent e) {
            PerforceUIPlugin.asyncExec(new Runnable() {

                public void run() {
                    refresh();
                }
            });
        }

        public void treeCollapsed(TreeEvent e) {
            treeExpanded(e);
        }
    };

    private SelectionListener treeSelectionListener = new SelectionAdapter() {

        @Override
        public void widgetSelected(SelectionEvent e) {
            if (!arrowDown) {
                return;
            }
            TreeItem item = (TreeItem) e.item;
            if (item == null) {
                return;
            }
            // Scroll selected item then scroll pair item
            while (scroll(item, e.widget))
                ;

            TreeItem pair = null;
            if (left == item.getParent()) {
                pair = getItemPair(right);
                e.widget = right;
            } else {
                pair = getItemPair(left);
                e.widget = left;
            }
            if (pair != null) {
                while (scroll(pair, e.widget))
                    ;
            }
        }

        private boolean scroll(TreeItem item, Widget tree) {
            int count = 0;
            if (isItemAbove(item)) {
                count = -1;
            } else if (isItemBelow(item)) {
                count = 1;
            }
            if (count != 0) {
                slider.setSelection(slider.getSelection() + count);
                Event event = new Event();
                event.widget = tree;
                selection.widgetSelected(new SelectionEvent(event));
            }
            return count != 0;
        }

        private TreeItem getItemPair(Tree tree) {
            TreeItem[] items = tree.getSelection();
            if (items != null && items.length == 1) {
                return items[0];
            } else {
                return null;
            }
        }

    };

    /**
     * Create a synchronized slider
     * 
     * @param parent
     */
    public SynchronizedSlider(Composite parent) {
        this.slider = new Slider(parent, SWT.VERTICAL);
        this.slider.setLayoutData(GridDataFactory.fillDefaults()
                .grab(false, true).create());
        this.slider.setEnabled(false);
        this.slider.setSelection(0);
        this.slider.setThumb(1);
        this.slider.setIncrement(1);
        this.slider.setPageIncrement(1);
        this.slider.setMinimum(0);
    }

    /**
     * Add selection listener
     * 
     * @param listener
     */
    public void addSelectionListener(SelectionListener listener) {
        if (listener != null) {
            this.listeners.add(listener);
        }
    }

    /**
     * Remove selection listener
     * 
     * @param listener
     */
    public void removeSelectionListener(SelectionListener listener) {
        if (listener != null) {
            this.listeners.remove(listener);
        }
    }

    /**
     * Fire selection event to all listeners
     * 
     * @param e
     */
    protected void fireSelection(SelectionEvent e) {
        for (Object listener : this.listeners.getListeners()) {
            ((SelectionListener) listener).widgetSelected(e);
        }
    }

    /**
     * Update parent to tree preferred size
     * 
     * @param tree
     * @return tree preferred height
     */
    private int updateTreeSize(Tree tree) {
        Point parent = tree.getParent().getSize();
        Point preferred = tree.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        tree.getParent().setSize(parent.x, Math.max(parent.y, preferred.y));
        return preferred.y;
    }

    /**
     * Refresh slider
     */
    public void refresh() {
        int leftPreferred = updateTreeSize(this.left);
        int rightPreferred = updateTreeSize(this.right);
        int leftParent = left.getParent().getParent().getSize().y;
        int rightParent = right.getParent().getParent().getSize().y;
        if (leftPreferred > leftParent || rightPreferred > rightParent) {
            slider.setEnabled(true);
            int leftMissing = getMissingItems(left.getItemHeight(), leftParent,
                    leftPreferred);
            int rightMissing = getMissingItems(right.getItemHeight(),
                    rightParent, rightPreferred);
            slider.setMaximum(Math.max(leftMissing, rightMissing) + 1);
        } else {
            if (slider.isEnabled()) {
                resetTop();
                scrollToTop();
            }
        }
    }

    /**
     * Clear out current top items
     */
    public void resetTop() {
        this.leftTop = null;
        this.rightTop = null;
    }

    /**
     * Scroll bar and trees to top
     */
    public void scrollToTop() {
        if (slider.isEnabled()) {
            slider.setSelection(0);
            slider.setEnabled(false);
        }
        previous = 0;
        resetTop();
        left.setLocation(0, 0);
        right.setLocation(0, 0);
    }

    private int getMissingItems(int itemHeight, int controlHeight,
            int preferredHeight) {
        if (itemHeight < 1 || controlHeight >= preferredHeight) {
            return 0;
        }
        return (preferredHeight / itemHeight - controlHeight / itemHeight);
    }

    /**
     * Get slider control
     * 
     * @return slider
     */
    public Slider getSlider() {
        return this.slider;
    }

    private boolean isItemBelow(TreeItem item) {
        Rectangle treeBounds = item.getParent().getParent().getParent()
                .getBounds();
        Rectangle itemBounds = item.getBounds();
        int yOffset = item.getParent().getBounds().y;
        return itemBounds.y + itemBounds.height + yOffset > treeBounds.height;
    }

    private boolean isItemAbove(TreeItem item) {
        Rectangle treeBounds = item.getParent().getBounds();
        Rectangle itemBounds = item.getBounds();
        return itemBounds.y + treeBounds.y < 0;
    }

    /**
     * Add scroll listener to control
     * 
     * @param control
     */
    public void addScrollListener(Control control) {
        if (control != null) {
            control.addMouseWheelListener(wheelListener);
        }
    }

    /**
     * Set tree to synchronize scrolling
     * 
     * @param leftTree
     * @param rightTree
     */
    public void setTrees(Tree leftTree, Tree rightTree) {
        if (this.left != null) {
            return;
        }
        this.left = leftTree;
        this.right = rightTree;

        this.slider.addSelectionListener(selection);
        addScrollListener(left);
        addScrollListener(right);

        left.getParent().addControlListener(sizeListener);
        right.getParent().addControlListener(sizeListener);

        left.addTreeListener(expansionListener);
        right.addTreeListener(expansionListener);

        left.addSelectionListener(treeSelectionListener);
        right.addSelectionListener(treeSelectionListener);

        KeyListener arrowListener = new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == SWT.ARROW_DOWN || e.keyCode == SWT.ARROW_UP) {
                    arrowDown = true;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.keyCode == SWT.ARROW_DOWN || e.keyCode == SWT.ARROW_UP) {
                    arrowDown = false;
                }
            }

        };

        left.addKeyListener(arrowListener);
        right.addKeyListener(arrowListener);

    }
}
