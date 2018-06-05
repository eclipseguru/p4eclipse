package com.perforce.team.ui.streams;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.perforce.p4java.core.IStreamSummary;
import com.perforce.team.ui.SWTUtils;

/**
 * A stream combo viewer.
 * 
 * @author ali
 *
 */
public class StreamComboViewer extends ComboViewer {

    public StreamComboViewer(Composite parent, int style) {
        super(parent, style|SWT.READ_ONLY);
        
        this.setLabelProvider(new LabelProvider(){
            @Override
            public String getText(Object element) {
                if(element instanceof IStreamSummary){
                    return StreamUtil.getStreamDisplayText((IStreamSummary) element);
                }
                return super.getText(element);
            }
        });
        this.setContentProvider(new ArrayContentProvider());
    }
    
    public void select(IStreamSummary element){
        this.setSelection(new StructuredSelection(element));
    }

    public IStreamSummary getSelectedObject(){
        Object obj = SWTUtils.getSingleSelectedObject(this);
        if(obj instanceof IStreamSummary)
            return (IStreamSummary) obj;
        return null;
    }

}
