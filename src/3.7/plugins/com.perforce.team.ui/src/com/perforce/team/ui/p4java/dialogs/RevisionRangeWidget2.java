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


public class RevisionRangeWidget2 extends Composite implements IRevisionRangeWidget{

    private Button endButton;
    private VersionComboViewer endCombo;
    private Text endText;
    private Button endBrowseBtn;

    // model
    private Object startVer = null;
    private Object endVer = null;
    
    private String startOption =  null;
    private String endOption = null;

    private IP4Connection connection;
    private Button startButton;
    private VersionComboViewer startCombo;
    private Text startText;
    private Button startBrowseBtn;

    public RevisionRangeWidget2(Composite parent, int style, String title, IP4Connection connection) {
        super(parent, style);
        this.connection=connection;
        
        createControl(this,title);

        init();
        
        addListeners();
    }

    private void init() {
        boolean enableStart=true;
        if(startOption==null){
            enableStart=false;
        }
        startButton.setSelection(enableStart);
        startCombo.setEnabled(enableStart);
        startText.setEnabled(enableStart);
        startBrowseBtn.setEnabled(enableStart);

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
        startButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean enabled = startButton.getSelection();
                startCombo.setEnabled(enabled);
                startText.setEnabled(enabled);
                startBrowseBtn.setEnabled(startCombo.getSelectedObject()!=null && startCombo.getSelectedObject()!=VersionType.Revision && enabled);
                if (!enabled) {
                    startOption = null;
                }

                if(startButton.getSelection() && !endButton.getSelection()){
                    endButton.setSelection(true);
                    endCombo.setEnabled(true);
                    endText.setEnabled(true);
                    endBrowseBtn.setEnabled(endCombo.getSelectedObject()!=null&&endCombo.getSelectedObject()!=VersionType.Revision);
                }
            }
        });
        startCombo.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                startText.setText(""); //$NON-NLS-1$
                startOption = getPrefix(startCombo)+startText.getText();
                startBrowseBtn.setEnabled(startCombo.getSelectedObject()!=null&&startCombo.getSelectedObject()!=VersionType.Revision);
            }
        });
        startText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                startOption = getPrefix(startCombo)+startText.getText();
            }
        });
        startBrowseBtn.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent e) {
                VersionType type = startCombo.getSelectedObject();
                if(type==VersionType.Revision){
                    
                }else if(type==VersionType.Label){
                    BrowseLabelDialog dlg=new BrowseLabelDialog(getShell(), connection);
                    if(Window.OK==dlg.open()){
                        startVer=dlg.getSelectedLabel();
                        startText.setText(((IP4Label)startVer).getLabel().getName());
                    }
                }else if(type==VersionType.ChangeList){
                    BrowseChangeListDialog dlg=new BrowseChangeListDialog(getShell(), connection);
                    if(Window.OK==dlg.open()){
                        startVer=dlg.getSelectedChangeList();
                        startText.setText(((IP4Changelist)startVer).getChangelist().getId()+""); //$NON-NLS-1$
                    }
                }
            }
        });

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

        limitArea.setLayout(new GridLayout(4, false));

        startButton = new Button(limitArea, SWT.CHECK);
        startButton.setText(Messages.RevisionRangeWidget2_Start);

        startCombo = new VersionComboViewer(limitArea, SWT.READ_ONLY);
        startCombo.setEnabled(false);

        startText = new Text(limitArea, SWT.SINGLE | SWT.BORDER);
        startText.setEnabled(false);
        GridDataFactory.swtDefaults().hint(50, SWT.DEFAULT).align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(startText);

        startBrowseBtn = new Button(limitArea, SWT.LEFT);
        startBrowseBtn.setText(Messages.RevisionRangeWidget2_Browse);
        startBrowseBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

        endButton = new Button(limitArea, SWT.CHECK);
        endButton.setText(Messages.RevisionRangeWidget2_End);

        endCombo = new VersionComboViewer(limitArea, SWT.READ_ONLY);
        endCombo.setEnabled(false);

        endText = new Text(limitArea, SWT.SINGLE | SWT.BORDER);
        endText.setEnabled(false);
        GridDataFactory.swtDefaults().hint(50, SWT.DEFAULT).align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(endText);

        endBrowseBtn = new Button(limitArea, SWT.LEFT);
        endBrowseBtn.setText(Messages.RevisionRangeWidget2_Browse);
        endBrowseBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
    }

    private String getPrefix(VersionComboViewer combo) {
        VersionType type = combo.getSelectedObject();
        if(type!=null)
            return type.getPrefix();
        return ""; //$NON-NLS-1$
    }
    
    public IStatus validate(){
        
        boolean hasStart = startButton.getSelection();
        if(hasStart){
            if(startCombo.getSelectedObject()==null){
                return ValidationStatus.error(Messages.RevisionRangeWidget2_StartTypeEmptyError);
            }else if(StringUtils.isEmpty(startText.getText())){
                return ValidationStatus.error(Messages.RevisionRangeWidget2_StartVersionEmptyError);
            }
        }
        boolean hasEnd = endButton.getSelection();
        if(hasEnd){
            if(endCombo.getSelectedObject()==null){
                return ValidationStatus.error(Messages.RevisionRangeWidget2_EndTypeEmptyError);
            }else if(StringUtils.isEmpty(endText.getText())){
                return ValidationStatus.error(Messages.RevisionRangeWidget2_EndVersionEmptyError);
            }
        }
        return ValidationStatus.ok();
    }

    public String getStart(){
        return startOption ;
    }
    
    public String getEnd(){
        return endOption;
    }
    
    public Composite getControl() {
        return this;
    }    

}
