/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ICheckable;
import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchAdapter;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.Tracing;
import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4Container;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class PerforceContentProvider extends BaseWorkbenchContentProvider
        implements ITreeContentProvider {

    /**
     * EMPTY
     */
    public static final Object[] EMPTY = new Object[0];

    /**
     * Loading object class
     */
    public static class Loading extends WorkbenchAdapter {

        /**
         * @see org.eclipse.ui.model.WorkbenchAdapter#getImageDescriptor(java.lang.Object)
         */
        @Override
        public ImageDescriptor getImageDescriptor(Object object) {
            return PerforceUIPlugin
                    .getDescriptor(IPerforceUIConstants.IMG_LOADING);
        }

        /**
         * @see org.eclipse.ui.model.WorkbenchAdapter#getLabel(java.lang.Object)
         */
        @Override
        public String getLabel(Object object) {
            return Messages.PerforceContentProvider_Loading;
        }
    }

    /**
     * Resource rule
     */
    public static class ResourceRule implements ISchedulingRule {

        private IP4Resource resource = null;

        /**
         * Creates a new resource rule
         *
         * @param resource
         */
        public ResourceRule(IP4Resource resource) {
            this.resource = resource;
        }

        /**
         * @see org.eclipse.core.runtime.jobs.ISchedulingRule#contains(org.eclipse.core.runtime.jobs.ISchedulingRule)
         */
        public boolean contains(ISchedulingRule rule) {
            if (rule instanceof ResourceRule) {
                return this.resource == ((ResourceRule) rule).resource;
            }
            return false;
        }

        /**
         * @see org.eclipse.core.runtime.jobs.ISchedulingRule#isConflicting(org.eclipse.core.runtime.jobs.ISchedulingRule)
         */
        public boolean isConflicting(ISchedulingRule rule) {
            return contains(rule);
        }

    }

    protected Object[] roots = EMPTY;

    /**
     * Structured viewer
     */
    protected StructuredViewer viewer;
    private boolean loadAsync = false;

    /**
     * Creates a new content provider for a structured viewer
     *
     * @param viewer
     */
    public PerforceContentProvider(StructuredViewer viewer) {
        this(viewer, false);
    }

    /**
     * Creates a new content provider for a structured view
     *
     * @param viewer
     * @param async
     */
    public PerforceContentProvider(StructuredViewer viewer, boolean async) {
        this.viewer = viewer;
        this.loadAsync = async;
    }

    /**
     * Creates a new content provider for a structured view
     *
     * @param viewer
     * @param context
     */
    public PerforceContentProvider(StructuredViewer viewer, Object context) {
        this(viewer, false, context);
    }

    /**
     * Creates a new content provider for a structured view
     *
     * @param viewer
     * @param async
     * @param context
     */
    public PerforceContentProvider(StructuredViewer viewer, boolean async,
            Object context) {
        this(viewer, async);
    }

    /**
     * Get members of a container, sub-classes may override.
     *
     * @param container
     * @return - array of members
     */
    protected IP4Resource[] getMembers(IP4Container container) {
        return container.members();
    }

    /**
     * Generate the rule to use for loading the container
     *
     * @param container
     * @return - scheduling rule or null to use no rule
     */
    protected ISchedulingRule generateRule(IP4Container container) {
        return new ResourceRule(container);
    }

    /**
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
     */
    @Override
    public Object[] getChildren(final Object parentElement) {
        if (parentElement instanceof IP4Container) {
            final IP4Container container = (IP4Container) parentElement;
            if (!container.needsRefresh()) {
                return getMembers(container);
            } else {
                if (loadAsync) {
                    final Loading loading = new Loading();
                    asyncUpdateContainer(container);
                    return new Object[] { loading };
                } else {
                	if (container.needsRefresh())
                        container.refresh();
                    return container.members();
                }
            }
        }
        return super.getChildren(parentElement);
    }

	protected void asyncUpdateContainer(final IP4Container container) {
		ISchedulingRule rule = generateRule(container);
		P4Runner.schedule(new P4Runnable() {

			@Override
			public String getTitle() {
				return MessageFormat
						.format(com.perforce.team.ui.Messages.PerforceContentProvider_FetchingChildren,
								container.getName());
			}

			@Override
			public void run(IProgressMonitor monitor) {
				// Check container again to ensure it wasn't already
				// refreshed on another thread successfully
				if (container.needsRefresh()) {
					container.refresh();
				}
				P4UIUtils.getDisplay().syncExec(new Runnable() {

					public void run() {
						if (P4UIUtils.okToUse(viewer)) {
							// keep the check state. In case of ContainerCheckboxTreeViewer, this will
							// a. restore container's check state
							// b. set children's check state based on container's
							if(viewer instanceof ICheckable){
								// save container's old state
								boolean checkState = ((ICheckable) viewer).getChecked(container);
								// refresh container, this may lose the check state
								viewer.refresh(container);
								// reset container state, which may affect children's check state
								((ICheckable) viewer).setChecked(container, checkState);
								for(IP4Resource child:container.members()){
									if(child instanceof IP4Container){
										((ICheckable) viewer).setChecked(child, checkState);
									}else
										((ICheckable) viewer).setChecked(child, false);
								}
							}else
								viewer.refresh(container);
						}
					}

				});
			}

		}, rule);

	}

    /**
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
     */
    @Override
    public Object getParent(Object element) {
        Object parent = null;
        if (element instanceof IP4Resource) {
            parent = ((IP4Resource) element).getParent();
        } else {
            parent = super.getParent(element);
        }
        return parent;
    }

    /**
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
     */
    @Override
    public boolean hasChildren(Object element) {
        boolean children = false;
        if (element instanceof IP4Changelist
                && !((IP4Changelist) element).needsRefresh()) {
            children = getMembers((IP4Changelist) element).length > 0;
        } else if (element instanceof IP4Container) {
            children = true;
        } else if (element instanceof Loading) {
            return false;
        } else {
            children = super.hasChildren(element);
        }
        return children;
    }

    /**
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    @Override
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof Loading) {
            return new Object[] { inputElement };
        }
        return this.roots;
    }

    /**
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    @Override
    public void dispose() {

    }

    /**
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
     *      java.lang.Object, java.lang.Object)
     */
    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (newInput instanceof Object[]) {
            this.roots = (Object[]) newInput;
        } else if (newInput instanceof IP4Resource) {
            this.roots = new Object[] { newInput };
        } else if (newInput instanceof Collection) {
            this.roots = ((Collection<?>) newInput).toArray();
        }
    }

    /**
     * @return the loadAsync
     */
    public boolean isLoadAsync() {
        return this.loadAsync;
    }

    /**
     * @param loadAsync
     *            the loadAsync to set
     */
    public void setLoadAsync(boolean loadAsync) {
        this.loadAsync = loadAsync;
    }

//    /////////////////// LazyTreeContentProvider API////////////////////////
    public void updateElement(final Object parent, final int index) {
		if(viewer instanceof TreeViewer){
			//Tracing.printTrace("updateElement", MessageFormat.format("parent={0}, index={1}",parent,index));//$NON-NLS-1$,$NON-NLS-2$
			if(parent==roots){
				Tracing.printExecTime(() -> {
					if (index >= 0 && index < roots.length) {
						Object el = roots[index];
						((TreeViewer) viewer).replace(parent, index, el);
						updateChildCount(el, -1);
					} else {
						PerforceProviderPlugin.logError("index outof bound of roots");
					}
				}, "updateElement", "index={0},parent={1}", index, roots);
			}else if (parent instanceof IP4Container) {
				final IP4Container container = (IP4Container) parent;
				if (!container.needsRefresh()) {
					Tracing.printExecTime(() -> {
						IP4Resource[] members = getMembers((IP4Container) parent);
						if (members.length > index) {
							Object element = members[index];
							((TreeViewer) viewer).replace(parent, index, element);
							updateChildCount(element, -1);
						}
					}, "updateElement", "P4ContainerNeedsNoRefresh,index={0}, parent={1}", index, parent);
				} else {
					if (isLoadAsync()) {
						Tracing.printExecTime(() -> {
							Loading loading = new Loading();
							((TreeViewer) viewer).replace(container, 0, loading);
							updateChildCount(container, 1);
							asyncUpdateContainer(container, index);
						}, "updateElement", "AsyncLoad, index={0}, parent={1}", index, parent);
					} else {
						Tracing.printExecTime(()->{
							container.refresh();
							IP4Resource[] members = getMembers((IP4Container) container);
							if(members.length>index && index>=0){
								Object element=members[index];
								((TreeViewer) viewer).replace(container, index, element);
								updateChildCount(element, -1);
							}
						}, "updateElement", "SyncLoad, index={0}, parent={1}",index, parent);
					}
				}
			}
		}
	}

	public void updateChildCount(Object element, int currentChildCount) {
		if(viewer instanceof TreeViewer){
//    		Tracing.printTrace("updateChildCount", "Element="+element);
		    if (element instanceof IP4Container){
		    	if(!((IP4Container) element).needsRefresh()) {
		    		Tracing.printTrace("updateChildCount", "Element=IP4Container[{0}], NoRefresh, children={1}",element,getMembers((IP4Container) element).length);//$NON-NLS-1$,$NON-NLS-2$
		    		((TreeViewer) viewer).setChildCount(element, getMembers((IP4Container) element).length);
		    	}else {// always assume there is a PENDING child for the container
		    		Tracing.printTrace("updateChildCount", "Element=IP4Container[{0}], Refresh, children={1}",element,1);//$NON-NLS-1$,$NON-NLS-2$
		    		((TreeViewer) viewer).setChildCount(element, 1);
		    	}
		    } else if (element instanceof Loading) {
	    		Tracing.printTrace("updateChildCount", "Element=Loading");//$NON-NLS-1$,$NON-NLS-2$
		    	((TreeViewer) viewer).setChildCount(element, 0);
		    } else if (element == roots){
	    		Tracing.printTrace("updateChildCount", "Element=roots{0}, NoRefresh",roots.length>1000?Arrays.toString(new int[]{roots.length}):Arrays.toString(roots));//$NON-NLS-1$,$NON-NLS-2$
		    	((TreeViewer) viewer).setChildCount(element, roots.length);
		    }else {
		    	if(element!=null){
			    	int length = super.getChildren(element).length;
		    		Tracing.printTrace("updateChildCount", "Element=[{0}], length={1}",element,length);//$NON-NLS-1$,$NON-NLS-2$
		    		((TreeViewer) viewer).setChildCount(element, length);
		    	}
		    }
		}
	}

	protected void asyncUpdateContainer(final IP4Container container, final int index) {
		Tracing.printTrace("asyncUpdateContainer", "AsyncUpdateContainer + {0}, container={1}", index, container);//$NON-NLS-1$,$NON-NLS-2$;
		ISchedulingRule rule = generateRule(container);
		P4Runner.schedule(new P4Runnable() {

			@Override
			public String getTitle() {
				return MessageFormat
						.format(com.perforce.team.ui.Messages.PerforceContentProvider_FetchingChildren,
								container.getName());
			}

			@Override
			public void run(IProgressMonitor monitor) {
				// Check container again to ensure it wasn't already
				// refreshed on another thread successfully
				if (container.needsRefresh()) {
					Tracing.printExecTime(()-> refreshContainer(container), "AsyncUpdateContainer:refreshContainer()", "{0}, container={1}", index, container);
				}
				P4UIUtils.getDisplay().syncExec(new Runnable() {

					public void run() {
						if (P4UIUtils.okToUse(viewer)) {
							// keep the check state. In case of ContainerCheckboxTreeViewer, this will
							// a. restore container's check state
							// b. set children's check state based on container's
							if(viewer instanceof ICheckable){
								// save container's old state
								boolean checkState = ((ICheckable) viewer).getChecked(container);
								// refresh container, this may lose the check state
								viewer.refresh(container);
								// reset container state, which may affect children's check state
								((ICheckable) viewer).setChecked(container, checkState);
								for(IP4Resource child:container.members()){
									if(child instanceof IP4Container){
										((ICheckable) viewer).setChecked(child, checkState);
									}else
										((ICheckable) viewer).setChecked(child, false);
								}
							}else{
								if(viewer instanceof TreeViewer && viewer.getContentProvider() instanceof ILazyTreeContentProvider){
									Tracing.printExecTime(()-> updateContainerNode((TreeViewer)viewer, container,index), "AsyncUpdateContainer:updateContainerNode()", "{0}, container={1}", index, container);
								} else
									viewer.refresh(container);
							}
						}
					}

				});
			}

		}, rule);

	}

	synchronized private void refreshContainer(IP4Container container) {
		container.refresh();
	}

	protected void updateContainerNode(TreeViewer viewer, IP4Container container,
			int index) {
		viewer.refresh(container);
		IP4Resource[] members = container.members();
		viewer.setChildCount(container, members.length);
//		IP4Resource[] members = container.members();
//		viewer.remove(container,0);
//		viewer.add(container, members);
//		viewer.setChildCount(container, members.length);
//		viewer.setExpandedElements(members);
//		IP4Resource[] members = container.members();
//		viewer.setChildCount(container, members.length);
//		if(index<members.length)
//			((TreeViewer)viewer).replace(container, index, members[index]);

	}

}
