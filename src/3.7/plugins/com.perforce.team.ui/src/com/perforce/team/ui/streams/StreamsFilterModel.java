package com.perforce.team.ui.streams;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.perforce.team.core.IConstants;

/**
 * Model object for streams filter.
 * To bind: BeanProperties.values(StreamFilterModel.class, new String[] { "name", "state" })
 * 
 * @author ali
 *
 */
public class StreamsFilterModel extends ModelObject{
    // Property Names - used for fire property change event.
    // When refactor, make sure these are same as name of properties.
    public static final String NAME="name"; //$NON-NLS-1$
    public static final String PARENT="parent"; //$NON-NLS-1$
    public static final String OWNER="owner"; //$NON-NLS-1$
    public static final String DEPOT="depot"; //$NON-NLS-1$
    public static final String TYPE="type"; //$NON-NLS-1$
    
    // properties
    private String name = IConstants.EMPTY_STRING;
    private String parent = IConstants.EMPTY_STRING;
    private String owner = IConstants.EMPTY_STRING;
    private String depot = IConstants.EMPTY_STRING;
    private String type = IConstants.EMPTY_STRING;
	private boolean showUnloadedOnly=false; // only show loaded by default
    
    public StreamsFilterModel() {
    }
    
    public StreamsFilterModel(String name, String parent, String owner,
            String depot, String type) {
        super();
        this.name(name).parent(parent).owner(owner).depot(depot).type(type);
    }

    public void reset(){
    	this.name(IConstants.EMPTY_STRING).parent(IConstants.EMPTY_STRING).owner(IConstants.EMPTY_STRING).depot(IConstants.EMPTY_STRING).type(IConstants.EMPTY_STRING);
    }
    
    public StreamsFilterModel name(String name){
    	this.name=name;
    	return this;
    }
    public StreamsFilterModel parent(String parent){
    	this.parent=parent;
    	return this;
    }
    public StreamsFilterModel owner(String owner){
    	this.owner=owner;
    	return this;
    }
    public StreamsFilterModel depot(String depot){
    	this.depot=depot;
    	return this;
    }
    public StreamsFilterModel showUnloadedOnly(boolean showUnloadedOnly) {
    	this.showUnloadedOnly=showUnloadedOnly;
    	return this;
	}
    public StreamsFilterModel type(String type){
    	this.type=type;
    	return this;
    }
    
    public String getName() {
        return name;
    }

    
    public void setName(String name) {
        firePropertyChange(NAME, this.name, this.name=name);
    }

    
    public String getParent() {
        return parent;
    }

    
    public void setParent(String parent) {
        firePropertyChange(PARENT, this.parent, this.parent=parent);
    }

    
    public String getOwner() {
        return owner;
    }

    
    public void setOwner(String owner) {
        firePropertyChange(OWNER, this.owner, this.owner=owner);
    }

    
    public String getDepot() {
        return depot;
    }

    
    public void setDepot(String depot) {
        firePropertyChange(DEPOT, this.depot, this.depot=depot);
    }

    
    public String getType() {
        return type;
    }

    public boolean isShowUnloadedOnly(){
    	return this.showUnloadedOnly;
    }
    
    public void setType(String type) {
        firePropertyChange(TYPE, this.type, this.type=type);
    }

    public boolean hasDepot(){
        return !StringUtils.isEmpty(getDepot());
    }
    
    public String getFilterString(){
        StringBuilder sb=new StringBuilder();
        if(!getName().isEmpty())
            sb.append(" Name=*"+getName().trim()+"*"); //$NON-NLS-1$ //$NON-NLS-2$
        if(!getOwner().isEmpty())
            sb.append(" Owner="+getOwner().trim()); //$NON-NLS-1$
        if(!getType().isEmpty())
            sb.append(" Type="+getType().trim()); //$NON-NLS-1$
        if(!getParent().isEmpty())
            sb.append(" Parent="+getParent().trim()); //$NON-NLS-1$
        
        if(sb.length()>0)
        	return sb.toString().trim();
        else 
        	return null;
        
    }
    
    public List<String> getPaths(){
    	if(!StringUtils.isEmpty(getDepot())){
    		List<String> paths=new ArrayList<String>();
    		paths.add("//"+getDepot().trim()+"/..."); //$NON-NLS-1$ //$NON-NLS-2$
    		return paths;
    	}
    	return null;
    }
    
    public String getDepotString(){
        if(!StringUtils.isEmpty(getDepot()))
            return "//"+getDepot().trim()+"/..."; //$NON-NLS-1$ //$NON-NLS-2$
        return IConstants.EMPTY_STRING;
    }
    
    @Override
    public String toString() {
        return getFilterString() + getDepotString();
    }

	public boolean isEmpty() {
		return StringUtils.isEmpty(getFilterString()) && StringUtils.isEmpty(getDepotString());
	}
}
