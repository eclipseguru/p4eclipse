/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.editor.outline;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.IBranchGraphElement;
import com.perforce.team.core.mergequest.model.Mapping;

import java.text.MessageFormat;
import java.util.Collection;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchAdapter;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class MappingProxy extends WorkbenchAdapter implements IAdaptable {

    /**
     * Generate proxies for mapping
     * 
     * @param mapping
     * @return non-null but possibly empty array of mapping proxies
     */
    public static MappingProxy[] generateProxies(Mapping mapping) {
        MappingProxy[] proxies = null;
        if (mapping != null) {
            Branch source = mapping.getSource();
            Branch target = mapping.getTarget();
            if (source != null && target != null) {
                proxies = new MappingProxy[] {
                        new MappingProxy(source, mapping),
                        new MappingProxy(target, mapping) };
            } else if (source != null) {
                proxies = new MappingProxy[] { new MappingProxy(source, mapping) };
            } else if (target != null) {
                proxies = new MappingProxy[] { new MappingProxy(target, mapping) };
            }
        }
        if (proxies == null) {
            proxies = new MappingProxy[0];
        }
        return proxies;
    }

    /**
     * Add generated proxies for specified mapping to collection
     * 
     * @param mapping
     * @param collection
     */
    public static void addProxies(Mapping mapping, Collection<Object> collection) {
        if (collection != null) {
            for (MappingProxy proxy : generateProxies(mapping)) {
                collection.add(proxy);
            }
        }
    }

    private Branch parent;
    private Mapping mapping;
    private IWorkbenchAdapter wrapped;

    /**
     * Create new mapping proxy
     * 
     * @param parent
     * @param mapping
     */
    public MappingProxy(Branch parent, Mapping mapping) {
        this.parent = parent;
        this.mapping = mapping;
        this.wrapped = P4CoreUtils.convert(mapping, IWorkbenchAdapter.class);
    }

    /**
     * Is this proxy originating from the source branch?
     * 
     * @return true if source originated, false otherwise
     */
    public boolean isSource() {
        return this.parent != null
                && this.parent.equals(this.mapping.getSource());
    }

    /**
     * Is this proxy originating from the target branch?
     * 
     * @return true if target originated, false otherwise
     */
    public boolean isTarget() {
        return this.parent != null
                && this.parent.equals(this.mapping.getTarget());
    }

    /**
     * @see org.eclipse.ui.model.WorkbenchAdapter#getImageDescriptor(java.lang.Object)
     */
    @Override
    public ImageDescriptor getImageDescriptor(Object object) {
        if (this.wrapped != null) {
            return this.wrapped.getImageDescriptor(this.mapping);
        }
        return super.getImageDescriptor(object);
    }

    /**
     * @see org.eclipse.ui.model.WorkbenchAdapter#getChildren(java.lang.Object)
     */
    @Override
    public Object[] getChildren(Object object) {
        if (this.wrapped != null) {
            return this.wrapped.getChildren(this.mapping);
        }
        return super.getChildren(object);
    }

    /**
     * @see org.eclipse.ui.model.WorkbenchAdapter#getLabel(java.lang.Object)
     */
    @Override
    public String getLabel(Object object) {
        String label = ""; //$NON-NLS-1$
        Branch source = this.mapping.getSource();
        Branch target = this.mapping.getTarget();
        if (source != null && target != null) {
            if (this.parent.equals(source)) {
                label = MessageFormat.format(Messages.MappingProxy_To,
                        target.getName());
            } else if (parent.equals(target)) {
                label = MessageFormat.format(Messages.MappingProxy_From,
                        source.getName());
            }
        }
        return label;
    }

    /**
     * Get mapping
     * 
     * @return mapping
     */
    public Mapping getMapping() {
        return this.mapping;
    }

    /**
     * Get branch parent
     * 
     * @return branch
     */
    public Branch getBranch() {
        return this.parent;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof MappingProxy) {
            MappingProxy proxy = (MappingProxy) obj;
            return this.parent.equals(proxy.parent)
                    && this.mapping.equals(proxy.mapping);
        }
        return false;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.mapping.hashCode();
    }

    /**
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class adapter) {
        if (Mapping.class == adapter || IBranchGraphElement.class == adapter) {
            return this.mapping;
        }
        return Platform.getAdapterManager().getAdapter(this, adapter);
    }

}
