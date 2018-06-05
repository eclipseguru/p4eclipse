package com.perforce.team.ui.p4java.dialogs;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Label;
import com.perforce.team.ui.p4java.dialogs.IntegrateToStreamDialog.IRevisionRangeWidget;
import com.perforce.team.ui.p4java.dialogs.VersionComboViewer.VersionType;


public class RevisionUptoWidget2 extends Composite implements IRevisionRangeWidget{

    private Button endButton;
    private VersionComboViewer endCombo;
    private Text endText;
    private Button endBrowseBtn;

    // model
    private Object endVer = null;
    
    private String endOption = null;

    private IP4Connection connection;

    public RevisionUptoWidget2(Composite parent, int style, String title, IP4Connection connection) {
        super(parent, style);
        this.connection=connection;
        
        createControl(this,title);

        init();
        
        addListeners();
    }

    private void init() {
        boolean enableEnd=true;
        if(endOption==null){
            enableEnd=false;
        }
        endButton.setSelection(enableEnd);
        endCombo.setEnabled(enableEnd);
        endText.setEnabled(enableEnd);
        endBrowseBtn.setEnabled(enableEnd);
            
    }
    
    private void addListeners() {
        endButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean enabled = endButton.getSelection();
                endCombo.setEnabled(enabled);
                endText.setEnabled(enabled);
                endBrowseBtn.setEnabled(endCombo.getSelectedObject()!=null && enabled);
                if (!enabled) {
                    endOption = null;
                }
            }

        });
        endCombo.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                endText.setText(""); //$NON-NLS-1$
                endOption = getPrefix(endCombo)+endText.getText();
                endBrowseBtn.setEnabled(endCombo.getSelectedObject()!=null&&endCombo.getSelectedObject()!=VersionType.Revision);
            }
        });
        
        endText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                endOption = getPrefix(endCombo)+endText.getText();
            }

        });
        
        endBrowseBtn.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent e) {
                VersionType type = endCombo.getSelectedObject();
                if(type==VersionType.Revision){
                    
                }else if(type==VersionType.Label){
                    BrowseLabelDialog dlg=new BrowseLabelDialog(getShell(), connection);
                    if(Window.OK==dlg.open()){
                        endVer=dlg.getSelectedLabel();
                        endText.setText(((IP4Label)endVer).getLabel().getName());
                    }
                }else if(type==VersionType.ChangeList){
                    BrowseChangeListDialog dlg=new BrowseChangeListDialog(getShell(), connection);
                    if(Window.OK==dlg.open()){
                        endVer=dlg.getSelectedChangeList();
                        endText.setText(((IP4Changelist)endVer).getChangelist().getId()+""); //$NON-NLS-1$
                    }
                }
            }
        });
    }

    private void createControl(Composite parent, String title) {
        parent.setLayout(new FillLayout());
        Group limitArea = new Group(parent, SWT.NONE);
        limitArea.setText(title);

        GridLayout laLayout = new GridLayout(4, false);
        limitArea.setLayout(laLayout);

        endButton = new Button(limitArea, SWT.CHECK);
        endButton.setText(Messages.RevisionUptoWidget2_RevisionsUpTo);

        endCombo = new VersionComboViewer(limitArea, SWT.READ_ONLY);
        endCombo.setEnabled(false);

        endText = new Text(limitArea, SWT.SINGLE | SWT.BORDER);
        endText.setEnabled(false);
        GridDataFactory.swtDefaults().hint(50, SWT.DEFAULT).align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(endText);

        endBrowseBtn = new Button(limitArea, SWT.LEFT);
        endBrowseBtn.setText(Messages.RevisionUptoWidget2_Browse);
        endBrowseBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
    }

    private String getPrefix(VersionComboViewer combo) {
        VersionType type = combo.getSelectedObject();
        if(type!=null)
            return type.getPrefix();
        return ""; //$NON-NLS-1$
    }
    
    public IStatus validate(){
        
        boolean hasEnd = endButton.getSelection();
        if(hasEnd){
            if(endCombo.getSelectedObject()==null){
                return ValidationStatus.error(Messages.RevisionUptoWidget2_VersionTypeNotSetError);
            }else if(StringUtils.isEmpty(endText.getText())){
                return ValidationStatus.error(Messages.RevisionUptoWidget2_VersionNotSetError);
            }
        }
        return ValidationStatus.ok();
    }

    public String getStart(){
        return null ;
    }
    
    public String getEnd(){
        return endOption;
    }
    
    public Composite getControl() {
        return this;
    }    

}
