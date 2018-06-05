package com.perforce.team.ui.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

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
 *
 */
public class PageBook extends Composite {

    /**
     * <p>
     * [Issue: This class should be declared private.]
     * </p>
     */
    public class PageBookLayout extends Layout {

        protected Point computeSize(Composite composite, int wHint, int hHint,
                boolean flushCache) {
            if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT) {
				return new Point(wHint, hHint);
			}

            Point result = null;
            if (currentPage != null) {
                result = currentPage.computeSize(wHint, hHint, flushCache);
            } else {
                //Rectangle rect= composite.getClientArea();
                //result= new Point(rect.width, rect.height);
                result = new Point(0, 0);
            }
            if (wHint != SWT.DEFAULT) {
				result.x = wHint;
			}
            if (hHint != SWT.DEFAULT) {
				result.y = hHint;
			}
            return result;
        }

        protected void layout(Composite composite, boolean flushCache) {
            if (currentPage != null) {
                currentPage.setBounds(composite.getClientArea());
            }
        }
    }

    /**
     * The current control; <code>null</code> if none.
     */
    private BookPage currentPage = null;

    /**
     * Creates a new empty pagebook.
     *
     * @param parent the parent composite
     * @param style the SWT style bits
     */
    public PageBook(Composite parent, int style) {
        super(parent, style);
        setLayout(new PageBookLayout());
    }

    /**
     * Shows the given page. This method has no effect if the given page is not
     * contained in this pagebook.
     *
     * @param page the page to show
     */
    public void showPage(BookPage page) {
    	System.out.println("showPage(Control) "+page);
		if (page.isDisposed() || page.getParent() != this) {
			return;
		}

		currentPage = page;

        // show new page
		page.setVisible(true);
		layout(true);

		// hide old (and all others) *after* new page has been made visible in
		// order to avoid flashing
		Control[] children = getChildren();
		for (int i = 0; i < children.length; i++) {
			Control child = children[i];
			if (child != page && !child.isDisposed()) {
				child.setVisible(false);
			}
		}
    }

    public BookPage showPageById(Object id){
		System.out.println("showPage(Object) "+id);

    	for(Control c:getChildren()){
    		if(c instanceof BookPage){
    			BookPage page = ((BookPage) c);
    			if(page.getId().equals(id)){
    				showPage(page);
    				return page;
    			}
    		}
    	}
    	return null;
    }
    
    public BookPage getActivePage(){
    	for(Control c:getChildren()){
    		if(c instanceof BookPage && c.isVisible()){
    			return (BookPage) c;
    		}
    	}
    	return null;
    }
    
    public void setInput(Object input){
    	BookPage p = getActivePage();
    	if(p!=null)
    		p.setInput(input);    	
    }
    
    public static abstract class BookPage extends Composite{
    	public BookPage(Composite parent, int style) {
			super(parent, style);
		}
    	
    	final protected void init(Object input){
    		createControl();
    		addListeners();
    		setInput(input);
    	}

		public void setInput(Object input) {
		}

		public abstract void createControl();
		public abstract Object getId();
		
		public void addListeners(){};
    }

}
