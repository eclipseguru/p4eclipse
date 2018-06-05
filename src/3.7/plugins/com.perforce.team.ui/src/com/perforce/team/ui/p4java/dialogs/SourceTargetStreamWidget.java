package com.perforce.team.ui.p4java.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.perforce.p4java.core.IStream;
import com.perforce.p4java.core.IStreamSummary;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.ui.p4java.dialogs.IntegrateToStreamDialog.ILongtimeTask;
import com.perforce.team.ui.p4java.dialogs.IntegrateToStreamDialog.ISourceTargetWidget;
import com.perforce.team.ui.streams.StreamComboViewer;
import com.perforce.team.ui.streams.StreamUtil;
import com.perforce.team.ui.streams.StreamsSuggestProvider;


/**
 * Widget for choosing a pair of Stream depot resource.
 * 
 * @author ali
 *
 */
public class SourceTargetStreamWidget extends Composite implements ISourceTargetWidget<IStreamSummary>,ILongtimeTask{

    // model
    private IStreamSummary sourcePath = null;
    private IStreamSummary targetPath = null;
    
    private IP4Resource resource;
    
    private boolean showAll=false;
    private Button browseAllBtn;
    private StreamComboViewer sourceCombo;
    private StreamComboViewer targetCombo;
    
    protected List<IStreamSummary> preferredStreams=new ArrayList<IStreamSummary>();
    protected IStream targetStream=null;
    
    public SourceTargetStreamWidget(Composite parent, int style, String title, IP4Resource resource) {
        super(parent, style);
        this.resource=resource;
        createControl(this, title);
        addListeners();
        updateFromModel();
    }
    
    protected StreamsSuggestProvider createSourceProvider(IP4Resource resource){
        return new StreamsSuggestProvider(resource.getConnection());
    }
    
    protected void createControl(Composite parent, String title) {
        parent.setLayout(new FillLayout());
        Group filePathArea = new Group(parent, SWT.NONE);
        filePathArea.setText(title);

        GridLayout fpaLayout = new GridLayout(3, false);
        filePathArea.setLayout(fpaLayout);

        Label sourceLabel = new Label(filePathArea, SWT.LEFT);
        sourceLabel.setText(Messages.IntegrateDialog_SourceLabel);

        sourceCombo = new StreamComboViewer(filePathArea, SWT.NONE);
        sourceCombo.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        createBrowseForStreamButton(filePathArea, sourceCombo);

        Label targetLabel = new Label(filePathArea, SWT.LEFT);
        targetLabel.setText(Messages.IntegrateDialog_TargetLabel);

        targetCombo = new StreamComboViewer(filePathArea, SWT.SINGLE | SWT.BORDER);
        targetCombo.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        
    }

    private void addListeners() {

    }

    public void updateFromModel() {
        if(this.resource!=null){
//            SelectionModel src = new SuggestBox.SelectionModel(null, sourcePath);
//            sourceBox.updateModel(src);
//            SelectionModel dst = new SuggestBox.SelectionModel(null, targetPath);
//            targetBox.updateModel(dst);
        }
    }


    /**
     * Update the source, target, and branch values
     */
    private void updateToModel() {
        sourcePath = sourceCombo.getSelectedObject();
        targetPath = targetCombo.getSelectedObject();
    }
    
    public IStatus validate(){
        if(sourceCombo.getSelectedObject()==null)
            return ValidationStatus.error(Messages.IntegrateDialog_MustEnterSourcePath);
        if(targetCombo.getSelectedObject()==null)
            return ValidationStatus.error(Messages.IntegrateDialog_MustEnterTargetPath);

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

    private void createBrowseForStreamButton(Composite parent, final Object update) {
        browseAllBtn = new Button(parent, SWT.PUSH);
        browseAllBtn.setText(Messages.SourceTargetStreamWidget_Browse);
        browseAllBtn.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                BrowseStreamDialog dlg=new BrowseStreamDialog(getShell(),resource.getConnection(), Messages.SourceTargetStreamWidget_InfornationText);
                if(Window.OK==dlg.open()){
                    List<IStreamSummary> newList=new ArrayList<IStreamSummary>();
                    IStreamSummary sum = dlg.getSelectedStream();
                    if(sum!=null){
                        newList.addAll(preferredStreams);

                        IStreamSummary s = StreamUtil.matchStream(sum.getStream(),preferredStreams);
                        if(s!=null){
                            newList.remove(s);
                        }
                        
                        newList.add(0, sum);

                    }
                    sourceCombo.setInput(newList.toArray());
                    sourceCombo.select(dlg.getSelectedStream());
                }
            }
        });
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        browseAllBtn.setEnabled(enabled);
        sourceCombo.getControl().setEnabled(enabled);
    }

    public Runnable getNonUIJob() {
        return new Runnable() {
            
            public void run() {
//                targetStream = StreamUtil.getStream(resource.getConnection());
//                preferredStreams.addAll(StreamUtil
//                        .getPreferredCopySources(targetStream,
//                                resource.getConnection()));
            }
        };
    }

    public Runnable getUIJob() {
        return new Runnable(){
            public void run() {
                if(!sourceCombo.getControl().isDisposed())
                    sourceCombo.setInput(preferredStreams.toArray());
                if(!targetCombo.getControl().isDisposed()){
                    targetCombo.setInput(new IStreamSummary[]{targetStream});
                    targetCombo.select(targetStream);
                }
            }
        };
    }
    
}
