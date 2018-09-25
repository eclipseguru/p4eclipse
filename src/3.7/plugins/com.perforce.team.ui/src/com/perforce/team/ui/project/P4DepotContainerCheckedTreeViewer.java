package com.perforce.team.ui.project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

import com.perforce.team.core.Tracing;
import com.perforce.team.core.p4java.IP4Container;

/**
 * CheckboxTreeViewer with special behaviour of the checked / gray state on
 * container (IP4Container) nodes:
 *
 * The grayed state is used to visualize the checked state of its children.
 * Containers are checked and non-gray if all contained children are checked. The
 * container is grayed if some but not all children are checked.
 *
 * For container which has Leaf (IP4File) nodes, no matter container node is
 * checked or not, its children leaf nodes will not checked.
 */
public class P4DepotContainerCheckedTreeViewer extends CheckboxTreeViewer {

	private Set<Object> childrenOnlySet=new HashSet<Object>();
	public boolean addChildOnlyNode(Object obj){
		return childrenOnlySet.add(obj);
	}
	public boolean removeChildOnlyNode(Object obj){
		return childrenOnlySet.remove(obj);
	}
	public String getChildOnlySet(){
		return Arrays.toString(childrenOnlySet.toArray());
	}

    public P4DepotContainerCheckedTreeViewer(Composite parent) {
        super(parent);
        initViewer();
    }

    public P4DepotContainerCheckedTreeViewer(Composite parent, int style) {
        super(parent, style);
        initViewer();
    }

    /**
     * Constructor for ContainerCheckedTreeViewer.
     * @see CheckboxTreeViewer#CheckboxTreeViewer(Tree)
     */
    public P4DepotContainerCheckedTreeViewer(Tree tree) {
        super(tree);
        initViewer();
    }

    private void initViewer() {
        setUseHashlookup(true);
        addCheckStateListener(new ICheckStateListener() {
            public void checkStateChanged(CheckStateChangedEvent event) {
                doCheckStateChanged(event.getElement());
            }
        });
        addTreeListener(new ITreeViewerListener() {
            public void treeCollapsed(TreeExpansionEvent event) {
            }

            public void treeExpanded(TreeExpansionEvent event) {
                Widget item = findItem(event.getElement());
                if (item instanceof TreeItem) {
                    initializeItem((TreeItem) item);
                }
            }
        });
    }

    protected void doCheckStateChanged(Object element) {
        Widget item = findItem(element);
        if (item instanceof TreeItem) {
            TreeItem treeItem = (TreeItem) item;
            treeItem.setGrayed(false);
            updateChildrenItems(treeItem);
            updateParentItems(treeItem.getParentItem());
        }
    }

    protected void doGrayStateChanged(Object element) {
        Widget item = findItem(element);
        if (item instanceof TreeItem) {
            TreeItem treeItem = (TreeItem) item;
            updateParentItems(treeItem.getParentItem());
        }
    }

    /**
     * Updates the check state of all created children
     */
    private void updateChildrenItems(TreeItem parent) {
        Item[] children = getChildren(parent);
        boolean state = parent.getChecked();
        for (int i = 0; i < children.length; i++) {
            TreeItem curr = (TreeItem) children[i];
            if (curr.getData() != null
                    && ((curr.getChecked() != state) || curr.getGrayed())) {
            	if(curr.getData() instanceof IP4Container){
                    curr.setChecked(state);
                    curr.setGrayed(false);
                    updateChildrenItems(curr);
            	}else{
                    curr.setChecked(false);
                    curr.setGrayed(false);
            	}
            }
        }
    }

    /**
     * Updates the check / gray state of all parent items
     */
    private void updateParentItems(TreeItem item) {
        if (item != null) {
            Item[] children = getChildren(item);
            boolean containsChecked = false;
            boolean containsUnchecked = false;
            boolean containsGrayChecked = false;
            boolean hasContainerChildren = false;
            for (int i = 0; i < children.length; i++) {
                TreeItem curr = (TreeItem) children[i];
                if(curr.getData() instanceof IP4Container){
                	hasContainerChildren=true;
	                containsChecked |= curr.getChecked();
	                containsUnchecked |= !curr.getChecked();
	                containsGrayChecked |= (curr.getGrayed() && curr.getChecked());
                }else{ // leaf(IP4Files) nodes
                	containsUnchecked=true;
                }
            }
            if(hasContainerChildren){
            	Tracing.printTrace(getClass().getSimpleName(), "updateParentItems(): chlidrenOnlySet= {0}",getChildOnlySet()); //$NON-NLS-1$
            	item.setChecked(containsChecked); // must setChecked to notify listeners
            	if(!containsUnchecked && children.length==1){
            		item.setGrayed(containsChecked);
            	}else{
            		item.setChecked(containsChecked);
		            item.setGrayed((containsChecked && containsUnchecked)||containsGrayChecked||childrenOnlySet.contains(item.getData()));
            	}
            }
            updateParentItems(item.getParentItem());
        }
    }

    /**
     * The item has expanded. Updates the checked state of its children.
     */
    private void initializeItem(TreeItem item) {
        if (item.getChecked() && !item.getGrayed()) {
            updateChildrenItems(item);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ICheckable#setChecked(java.lang.Object, boolean)
     */
    public boolean setChecked(Object element, boolean state) {
        if (super.setChecked(element, state)) {
            doCheckStateChanged(element);
            return true;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ICheckable#setGrayed(java.lang.Object, boolean)
     */
    public boolean setGrayed(Object element, boolean state) {
        if (super.setGrayed(element, state)) {
            doGrayStateChanged(element);
            return true;
        }
        return false;
    }


    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.CheckboxTreeViewer#setCheckedElements(java.lang.Object[])
     */
    public void setCheckedElements(Object[] elements) {
        super.setCheckedElements(elements);
        for (int i = 0; i < elements.length; i++) {
            doCheckStateChanged(elements[i]);
        }
    }


    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.AbstractTreeViewer#setExpanded(org.eclipse.swt.widgets.Item, boolean)
     */
    protected void setExpanded(Item item, boolean expand) {
        super.setExpanded(item, expand);
        if (expand && item instanceof TreeItem) {
            initializeItem((TreeItem) item);
        }
    }


    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.CheckboxTreeViewer#getCheckedElements()
     */
    public Object[] getCheckedElements() {
        Object[] checked = super.getCheckedElements();
        // add all items that are children of a checked node but not created yet
        ArrayList<Object> result = new ArrayList<Object>();
        for (int i = 0; i < checked.length; i++) {
            Object curr = checked[i];
            result.add(curr);
            Widget item = findItem(curr);
            if (item != null) {
                Item[] children = getChildren(item);
                // check if contains the dummy node
                if (children.length == 1 && children[0].getData() == null) {
                    // not yet created
                    collectChildren(curr, result);
                }
            }
        }
        return result.toArray();
    }

	/**
	 * Recursively add the filtered children of element to the result.
	 * @param element
	 * @param result
	 */
    private void collectChildren(Object element, ArrayList<Object> result) {
        Object[] filteredChildren = getFilteredChildren(element);
        for (int i = 0; i < filteredChildren.length; i++) {
            Object curr = filteredChildren[i];
            result.add(curr);
            collectChildren(curr, result);
        }
    }

}
