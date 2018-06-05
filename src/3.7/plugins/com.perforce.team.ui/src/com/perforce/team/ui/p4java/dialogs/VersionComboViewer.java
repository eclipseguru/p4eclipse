package com.perforce.team.ui.p4java.dialogs;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Composite;


public class VersionComboViewer extends ComboViewer {

    public enum VersionType{
        Revision(Messages.IntegrateDialog_Revision,"#"), //$NON-NLS-1$
        ChangeList(Messages.IntegrateDialog_Changelist,"@"), //$NON-NLS-1$
        Label(Messages.IntegrateDialog_Label,"@"), //$NON-NLS-1$
        Datetime(Messages.IntegrateDialog_Datetime,"@"); //$NON-NLS-1$
        
        private String label;
        private String prefix;
        
        VersionType(String label, String prefix){
            this.label=label;
            this.prefix=prefix;
        }
        
        public String getLabel(){
            return label;
        }
        
        public String getPrefix(){
            return prefix;
        }
        
    }
    

    public VersionComboViewer(Composite parent, int style) {
        super(parent, style);
        
        setContentProvider(new ArrayContentProvider());
        setLabelProvider(new LabelProvider(){
            @Override
            public String getText(Object element) {
                if(element instanceof VersionType){
                    return ((VersionType) element).getLabel();
                }
                return super.getText(element);
            }
        });
        setInput(VersionType.values());
        select(VersionType.Revision);
//        setSelection(new StructuredSelection(VersionType.Revision));
    }
    
    public VersionType getSelectedObject(){
        IStructuredSelection sel=(IStructuredSelection) getSelection();
        return (VersionType) sel.getFirstElement();
    }
    
    public void select(VersionType type){
    	setSelection(new StructuredSelection(type));
    }

    public void setEnabled(boolean enabled){
        getControl().setEnabled(enabled);
    }
    
    public void filter(final VersionType... types){
    	ViewerFilter filter = new ViewerFilter() {
			
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				for(VersionType t: types){
					if(t==element)
						return true;
				}
				return false;
			}
		};
    	setFilters(new ViewerFilter[]{filter});
    }

}
