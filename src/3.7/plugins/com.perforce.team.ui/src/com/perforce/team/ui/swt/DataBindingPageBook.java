package com.perforce.team.ui.swt;

import org.eclipse.core.databinding.AggregateValidationStatus;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.widgets.Composite;

/**
 * A pagebook is a composite control where only a single control is visible
 * at a time. It is similar to a notebook, but without tabs.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * <p>
 * Note that although this class is a subclass of <code>Composite</code>,
 * it does not make sense to set a layout on it.
 * </p>
 * This class support databinding, it assume each page has its own databinding context.
 *
 */
public class DataBindingPageBook extends PageBook {

	ListenerList listenerList=new ListenerList();
    
	public DataBindingPageBook(Composite parent, int style) {
		super(parent, style);
	}
	
	@Override
	public DataBindingBookPage getActivePage() {
		return (DataBindingBookPage) super.getActivePage();
	}
	
	public AggregateValidationStatus getAggregateStatus(){
		DataBindingBookPage activePage = getActivePage();
		if(activePage==null)
			return null;
		return activePage.getAggregateStatus();
	}
	
	public void addStatusListener(IValueChangeListener listener){
		listenerList.add(listener);
	}
	
	public void removeStatusListener(IValueChangeListener listener){
		listenerList.remove(listener);
	}
	
	@Override
	public void showPage(BookPage page) {
		DataBindingBookPage p = getActivePage();
		Object[] listeners = listenerList.getListeners();
		if(p!=null){
			for(Object obj: listeners){
				p.getAggregateStatus().removeValueChangeListener(((IValueChangeListener)obj));
			}
		}
		super.showPage(page);
		final AggregateValidationStatus status = ((DataBindingBookPage)page).getAggregateStatus();
		for(Object obj: listeners){
			IValueChangeListener listener = (IValueChangeListener)obj;
			status.addValueChangeListener(listener);
			listener.handleValueChange(new ValueChangeEvent(status, null));
		}
		
	}

    public static abstract class DataBindingBookPage extends BookPage{
    	
    	private DataBindingContext dbc;
		private AggregateValidationStatus aggregateStatus;

		public DataBindingBookPage(Composite parent, int style) {
			super(parent, style);
		}
    	
		final protected void init() {
			createControl();
			dbc=new DataBindingContext();
			binding(dbc);
			aggregateStatus = new AggregateValidationStatus(dbc.getBindings(),
					AggregateValidationStatus.MAX_SEVERITY);
		}

		public abstract void binding(DataBindingContext dbc);

		public AggregateValidationStatus getAggregateStatus(){
			return aggregateStatus;
		};
    	
    }



}
