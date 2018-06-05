package com.perforce.team.ui.p4java.dialogs;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.model.IWorkbenchAdapter;

import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.ui.SWTUtils;
import com.perforce.team.ui.p4java.dialogs.IntegrateToStreamDialog.IDepotFileChooser;
import com.perforce.team.ui.resource.LazyResourceBrowserDialog;


public class DepotFileChooser extends Composite implements IDepotFileChooser{

    private IP4Resource resource;
    
    private Text pathText;
    
    private String path="";

    private IWorkbenchAdapter adapter;

    DepotFileChooser(Composite parent, int style, IP4Resource resource, IWorkbenchAdapter adapter) {
        super(parent, style);
        this.resource=resource;
        this.adapter=adapter;
        createControl(this);
        addListeners();
    }

    private void addListeners() {
        // TODO Auto-generated method stub
        
    }

    public Control getControl() {
        return this;
    }

    /**
     * return Non-null string
     */
    public String getDepotPath() {
        return path;
    }

    private void createControl(Composite parent) {
        GridLayoutFactory.swtDefaults().margins(1, 5).numColumns(2).applyTo(parent);
        
        SWTUtils.createLabel(parent, Messages.DepotFileChooser_MergeOnlyFileFolders, 2);
        pathText=SWTUtils.createText(parent);
        if(resource!=null){
        	String actionPath = resource.getActionPath(IP4Resource.Type.REMOTE);
        	if(actionPath!=null)
        		pathText.setText(actionPath);
        }
        
        Button browseBtn=SWTUtils.createButton(parent, Messages.DepotFileChooser_Browse, SWT.PUSH, 1);
        browseBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                LazyResourceBrowserDialog dialog = new LazyResourceBrowserDialog(
                        getShell(), adapter);
                
                if (Window.OK == dialog.open()) {
                    IP4Resource r = dialog.getSelectedResource();
                    if (r != null) {
                        String actionPath = r
                                .getActionPath(IP4Resource.Type.REMOTE);
                        if (actionPath != null) {
                            pathText.setText(actionPath);
                            validate();
                        }
                    }
                }
            }

        });
        
    }
    
    public IStatus validate(){
        this.path = this.pathText.getText().trim();
//        if (this.path.length() == 0) {
//            return ValidationStatus.error(Messages.DepotFileChooser_MergeFolderPathEmptyError);
//        } else if (!this.path.startsWith("//")) { //$NON-NLS-1$
//            return ValidationStatus.error(Messages.DepotFileChooser_DepotFolderNotStartWithDepotError);
//        }
        return ValidationStatus.ok();
    }

}
