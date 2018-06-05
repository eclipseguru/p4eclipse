package com.perforce.team.ui.p4java.dialogs;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.perforce.team.ui.p4java.dialogs.IntegrateToStreamDialog.IRevisionRangeWidget;
import com.perforce.team.ui.p4java.dialogs.VersionComboViewer.VersionType;


public class RevisionRangeWidget extends Composite implements IRevisionRangeWidget{

    private Button startButton;
    private VersionComboViewer startCombo;
    private Text startText;
    private Label startHelp;
    private Button endButton;
    private VersionComboViewer endCombo;
    private Text endText;
    private Label endHelp;

    // model
    private String startOption = null;
    private String endOption = null;

    public RevisionRangeWidget(Composite parent, int style, String title) {
        super(parent, style);
        createControl(this,title);

        init();
        
        addListeners();
    }

    private void init() {
        boolean enabledStart=true;
        if(startOption==null){
            enabledStart=false;
        }
        startButton.setSelection(enabledStart);
        startCombo.setEnabled(enabledStart);
        startText.setEnabled(enabledStart);
        startHelp.setVisible(enabledStart);
        
        boolean enableEnd=true;
        if(endOption==null){
            enableEnd=false;
        }
        endButton.setSelection(enableEnd);
        endCombo.setEnabled(enableEnd);
        endText.setEnabled(enableEnd);
        endHelp.setVisible(enableEnd);
            
    }

    private void addListeners() {
        startButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean enabled = startButton.getSelection();
                startCombo.setEnabled(enabled);
                startText.setEnabled(enabled);
                startHelp.setVisible(enabled);
                if (!enabled) {
                    startOption = null;
                }
            }

        });
        startCombo.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                startOption = getPrefix(startCombo)+startText.getText();
            }
        });
        startText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                String prefix = getPrefix(startCombo);
                startOption = prefix + startText.getText();
            }

        });
        endButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean enabled = endButton.getSelection();
                endCombo.setEnabled(enabled);
                endText.setEnabled(enabled);
                endHelp.setVisible(enabled);
                if (!enabled) {
//                    startButton.setEnabled(enabled);
//                    startCombo.setEnabled(enabled);
//                    startText.setEnabled(enabled);
//                    startButton.setSelection(false);
                    endOption = null;
//                    startOption = null;
                }
            }

        });
        endCombo.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                endOption = getPrefix(endCombo)+endText.getText();
            }
        });
        
        endText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                endOption = getPrefix(endCombo)+endText.getText();
            }

        });
    }

    private void createControl(Composite parent, String title) {
        parent.setLayout(new FillLayout());
        Group limitArea = new Group(parent, SWT.NONE);
        limitArea.setText(title);

        GridLayout laLayout = new GridLayout(4, false);
        limitArea.setLayout(laLayout);

        startButton = new Button(limitArea, SWT.CHECK);
        startButton.setText(Messages.IntegrateDialog_Start);

        startCombo = new VersionComboViewer(limitArea, SWT.READ_ONLY);
        startCombo.setEnabled(false);

        startText = new Text(limitArea, SWT.SINGLE | SWT.BORDER);
        startText.setEnabled(false);
        GridDataFactory.swtDefaults().hint(50, SWT.DEFAULT).align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(startText);

        startHelp = new Label(limitArea, SWT.LEFT);
        startHelp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        addComboListener(startCombo, startHelp);

        endButton = new Button(limitArea, SWT.CHECK);
        endButton.setText(Messages.IntegrateDialog_End);

        endCombo = new VersionComboViewer(limitArea, SWT.READ_ONLY);
        endCombo.setEnabled(false);

        endText = new Text(limitArea, SWT.SINGLE | SWT.BORDER);
        endText.setEnabled(false);
        GridDataFactory.swtDefaults().hint(50, SWT.DEFAULT).align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(endText);

        endHelp = new Label(limitArea, SWT.LEFT);
        endHelp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        addComboListener(endCombo, endHelp);
        
        startHelp.setText(Messages.RevisionRangeWidget_SelectVersionTypeAndVersion);
        endHelp.setText(Messages.RevisionRangeWidget_SelectVersionTypeAndVersion);
    }


    private void addComboListener(final VersionComboViewer combo, final Label help) {
        combo.addSelectionChangedListener(new ISelectionChangedListener() {
            
            public void selectionChanged(SelectionChangedEvent event) {
                VersionType type = combo.getSelectedObject();
                if (type==VersionType.Revision) {
                    help.setText(Messages.IntegrateDialog_EnterRevisionNumber);
                } else if (type==VersionType.ChangeList) {
                    help.setText(Messages.IntegrateDialog_EnterChangelistNumber);
                } else if (type==VersionType.Label) {
                    help.setText(Messages.IntegrateDialog_EnterLabel);
                } else{
                    help.setText(Messages.RevisionRangeWidget_SelectVersionTypeAndVersion);
                }
                help.setVisible(true);
                help.getParent().layout(true);
            }
        });
    }
    
    private String getPrefix(VersionComboViewer combo) {
        VersionType type = combo.getSelectedObject();
        if(type!=null)
            return type.getPrefix();
        return ""; //$NON-NLS-1$
    }
    
    public IStatus validate(){
        boolean hasStart = startButton.getSelection();
        if(hasStart && startOption==null)
            return ValidationStatus.error(Messages.RevisionRangeWidget_StartVersionEmptyError);
        
        boolean hasEnd = endButton.getSelection();
        if(hasEnd && endOption==null)
            return ValidationStatus.error(Messages.RevisionRangeWidget_EndVersionEmptyError);

        return ValidationStatus.ok();
    }

    public String getStart(){
        return startOption;
    }
    
    public String getEnd(){
        return endOption;
    }

    public Composite getControl() {
        return this;
    }    

}
