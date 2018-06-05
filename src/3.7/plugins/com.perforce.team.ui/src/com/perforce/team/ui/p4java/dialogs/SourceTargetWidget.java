package com.perforce.team.ui.p4java.dialogs;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Resource.Type;
import com.perforce.team.ui.p4java.dialogs.IntegrateToStreamDialog.ISourceTargetWidget;
import com.perforce.team.ui.preferences.IPreferenceConstants;
import com.perforce.team.ui.resource.ResourceBrowserDialog;
import com.perforce.team.ui.views.SessionManager;


/**
 * Widget for choosing a pair of classic depot resource.
 * 
 * @author ali
 *
 */
public class SourceTargetWidget extends Composite implements ISourceTargetWidget<String>{
    private Combo sourceText;
    private Combo targetText;

    // model
    private String sourcePath = null;
    private String targetPath = null;
    private IP4Resource resource;
    
    public SourceTargetWidget(Composite parent, int style, String title, IP4Resource resource) {
        super(parent, style);
        this.resource=resource;
        this.sourcePath = this.resource.getActionPath();
        this.targetPath = this.resource.getActionPath();
        createControl(this, title);
        addListeners();
        updateFromModel();
    }

    protected void createControl(Composite parent, String title) {
        parent.setLayout(new FillLayout());
        Group filePathArea = new Group(parent, SWT.NONE);
        filePathArea.setText(title);
//        filePathArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        GridLayout fpaLayout = new GridLayout(3, false);
        filePathArea.setLayout(fpaLayout);
//        filePathArea
//                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        Label sourceLabel = new Label(filePathArea, SWT.LEFT);
        sourceLabel.setText(Messages.IntegrateDialog_SourceLabel);

        sourceText = new Combo(filePathArea, SWT.DROP_DOWN);
        SessionManager.loadComboHistory(sourceText,
                IPreferenceConstants.SOURCE_FILE_HISTORY);
        sourceText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        createBrowseForFileButton(filePathArea, sourceText);

        Label targetLabel = new Label(filePathArea, SWT.LEFT);
        targetLabel.setText(Messages.IntegrateDialog_TargetLabel);

        targetText = new Combo(filePathArea, SWT.SINGLE | SWT.BORDER);
        SessionManager.loadComboHistory(targetText,
                IPreferenceConstants.TARGET_FILE_HISTORY);
        targetText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        createBrowseForFileButton(filePathArea, targetText);
    }
    

    private void createBrowseForFileButton(Composite parent, final Object update) {
        final Button browseSource = new Button(parent, SWT.PUSH);
        browseSource.setText(Messages.IntegrateDialog_Browse);
        browseSource.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                ResourceBrowserDialog dialog = new ResourceBrowserDialog(
                        browseSource.getShell(), resource.getConnection()
                                .members());
                if (dialog.open() == ResourceBrowserDialog.OK) {
                    IP4Resource resource = dialog.getSelectedResource();
                    if (resource != null) {
                        String actionPath = resource.getActionPath(Type.REMOTE);
                        if (actionPath != null) {
                            if (update instanceof Combo) {
                                ((Combo) update).setText(actionPath);
                            } else if (update instanceof Text) {
                                ((Text) update).setText(actionPath);
                            }
                        }
                    }
                }
            }

        });
    }


    private void addListeners() {

        ModifyListener pathListener = new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                updateToModel();
            }
        };
        
        sourceText.addModifyListener(pathListener);
//      sourceText.addModifyListener(validateListener);

        targetText.addModifyListener(pathListener);
//        targetText.addModifyListener(validateListener);
    }

    public void updateFromModel() {
        if (this.sourcePath != null) {
            sourceText.setText(this.sourcePath);
        }
        if (this.targetPath != null) {
            targetText.setText(targetPath);
        }
    }


    /**
     * Update the source, target, and branch values
     */
    public void updateToModel() {
        sourcePath = sourceText.getText();
        targetPath = targetText.getText();
    }
    
    public IStatus validate(){
        String source = sourceText.getText().trim();
        if (source.length() == 0) {
            return ValidationStatus.error(Messages.IntegrateDialog_MustEnterSourcePath);
        }
        String target = targetText.getText().trim();
        if (target.length() == 0) {
            return ValidationStatus.error(Messages.IntegrateDialog_MustEnterTargetPath);
        }
    
        updateToModel();
        return ValidationStatus.ok();
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public Composite getControl() {
        return this;
    }

    public boolean isShowAllSource() {
        return true;
    }

    public void postInit() {
        // TODO Auto-generated method stub
        
    }    
    
}
