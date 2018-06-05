package com.perforce.team.ui.p4java.dialogs;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.perforce.p4java.core.IStreamSummary;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.ui.p4java.dialogs.IntegrateToStreamDialog.ISourceTargetWidget;
import com.perforce.team.ui.streams.StreamsSuggestProvider;
import com.perforce.team.ui.streams.SuggestBox;
import com.perforce.team.ui.streams.SuggestBox.SelectionModel;


/**
 * Widget for choosing a pair of Stream depot resource. This use SuggestBox to choose stream.
 * This is not used currently
 * 
 * @author ali
 *
 */
public class SourceTargetStreamWidget2 extends Composite implements ISourceTargetWidget<IStreamSummary>{
    private SuggestBox sourceBox;
    private SuggestBox targetBox;

    // model
    private IStreamSummary sourcePath = null;
    private IStreamSummary targetPath = null;
    
    private IP4Resource resource;
    private StreamsSuggestProvider sourceProvider;
    
    private boolean showAll=false;
    private Button browseAllBtn;
    
    public SourceTargetStreamWidget2(Composite parent, int style, String title, IP4Resource resource, IStreamSummary source, IStreamSummary target) {
        super(parent, style);
        this.resource=resource;
        this.sourcePath = source;
        this.targetPath = target;
        this.sourceProvider=createSourceProvider(resource);
        createControl(this, title);
        addListeners();
        updateFromModel();
    }
    
    protected StreamsSuggestProvider createSourceProvider(IP4Resource resource){
        return new StreamsSuggestProvider(resource.getConnection());
    }
    
    protected StreamsSuggestProvider createTargetProvider(IP4Resource resource){
        return null;
    }

    protected void createControl(Composite parent, String title) {
        parent.setLayout(new FillLayout());
        Group filePathArea = new Group(parent, SWT.NONE);
        filePathArea.setText(title);

        GridLayout fpaLayout = new GridLayout(3, false);
        filePathArea.setLayout(fpaLayout);
        Label sourceLabel = new Label(filePathArea, SWT.LEFT);
        sourceLabel.setText(Messages.IntegrateDialog_SourceLabel);

        sourceBox = new SuggestBox(filePathArea, SWT.NONE, sourceProvider);
        GridDataFactory.fillDefaults().grab(true, false).indent(10,-1).applyTo(sourceBox);
        
        createBrowseForFileButton(filePathArea);

        Label targetLabel = new Label(filePathArea, SWT.LEFT);
        targetLabel.setText(Messages.IntegrateDialog_TargetLabel);

        targetBox = new SuggestBox(filePathArea, SWT.READ_ONLY, sourceProvider);
        GridDataFactory.fillDefaults().grab(true, false).indent(10,-1).applyTo(targetBox);
    }
    
    private void createBrowseForFileButton(Composite parent){
        browseAllBtn = new Button(parent, SWT.PUSH);
        browseAllBtn.setText(Messages.SourceTargetStreamWidget2_Browse);
        browseAllBtn.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                BrowseStreamDialog dlg=new BrowseStreamDialog(getShell(),resource.getConnection(), Messages.SourceTargetStreamWidget2_InformationText);
                if(Window.OK==dlg.open()){
                    sourceBox.updateModel(new SelectionModel(null, dlg.getSelectedStream()));
                }
            }
        });
    }

    private void addListeners() {

    }

    public void updateFromModel() {
        if(this.resource!=null){
            SelectionModel src = new SuggestBox.SelectionModel(null, sourcePath);
            sourceBox.updateModel(src);
            SelectionModel dst = new SuggestBox.SelectionModel(null, targetPath);
            targetBox.updateModel(dst);
        }
    }


    /**
     * Update the source, target, and branch values
     */
    public void updateToModel() {
        IStreamSummary src = (IStreamSummary) ((SelectionModel)sourceBox.getModel().getValue()).getSelection();
        sourcePath = src;
        IStreamSummary dst = (IStreamSummary) ((SelectionModel)targetBox.getModel().getValue()).getSelection();
        targetPath = dst;
    }
    
    public IStatus validate(){
        SelectionModel sModel = (SelectionModel) sourceBox.getModel().getValue();
        if(sModel==null || sModel.getSelection()==null){
            return ValidationStatus.error(Messages.IntegrateDialog_MustEnterSourcePath);
        }
        SelectionModel tModel = (SelectionModel) targetBox.getModel().getValue();
        if(tModel==null || tModel.getSelection()==null){
            return ValidationStatus.error(Messages.IntegrateDialog_MustEnterTargetPath);
        }
    
        IStreamSummary source=(IStreamSummary) sModel.getSelection();
        IStreamSummary target=(IStreamSummary) tModel.getSelection();
        String ss=source.getStream();
        String ts=target.getStream();
        if(ss!=null && ss.equals(ts)){
            return ValidationStatus.error(Messages.SourceTargetStreamWidget2_SourceTargetSameError);
        }
        
        updateToModel();
        return ValidationStatus.ok();
    }

    public IStreamSummary getSourcePath() {
        return sourcePath;
    }

    public IStreamSummary getTargetPath() {
        return targetPath;
    }

    public Composite getControl() {
        return this;
    }

    public boolean isShowAllSource() {
        return showAll;
    }

    public void postInit() {
    }    

}
