
package com.perforce.team.ui.streams.wizard;

import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.util.Tracing;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import com.perforce.p4java.core.IStream;
import com.perforce.p4java.core.IStreamSummary;
import com.perforce.p4java.core.IStreamSummary.Type;
import com.perforce.team.core.IConstants;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.SWTUtils;
import com.perforce.team.ui.dialogs.FixedWidthScrolledComposite;
import com.perforce.team.ui.streams.StreamTypeComboViewer;
import com.perforce.team.ui.streams.StreamUtil;
import com.perforce.team.ui.streams.SuggestBox.SelectionModel;
import com.perforce.team.ui.streams.wizard.StreamParentSelectionWidget.TaskStreamModel;

/**
 * Basic setting page.
 * 
 * @author ali
 *
 */
public class BasicSettingPage extends AbstractEditStreamWizardPage {
	// UI
    private Text nameText;
    private StreamTypeComboViewer typeCombo;
    private PropagationOptionWidget propWidget;
    private StreamParentSelectionWidget parentWidget;
    private Text descText;
//    private Button createWorkspaceBtn;
    private Button populateBtn;
    
    public BasicSettingPage() {
        super(BasicSettingPage.class.getName());
        setImageDescriptor(PerforceUIPlugin.getPlugin().getImageDescriptor(
                IPerforceUIConstants.IMG_STREAM_WIZARD));
        setTitle(com.perforce.team.ui.streams.wizard.Messages.BasicSettingPage_Title);
        setDescription(com.perforce.team.ui.streams.wizard.Messages.BasicSettingPage_Description);
    }

    public void createControl(Composite parent) {

        IStreamSummary sum=getStream();
        
        FixedWidthScrolledComposite scroll = new FixedWidthScrolledComposite(parent, SWT.V_SCROLL);
        scroll.setLayout(new FillLayout());
        Composite composite = new Composite(scroll, SWT.NONE);
        GridLayoutFactory.fillDefaults().applyTo(composite);
        
        SWTUtils.createLabel(composite, com.perforce.team.ui.streams.wizard.Messages.BasicSettingPage_StreamName);
        nameText=SWTUtils.createText(composite);
        if(sum!=null && sum.getName()!=null)
            nameText.setText(sum.getName());
        
        SWTUtils.createLabel(composite, com.perforce.team.ui.streams.wizard.Messages.BasicSettingPage_StreamType);
        typeCombo=new StreamTypeComboViewer(composite);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(typeCombo.getControl());
        typeCombo.setInput(IStreamSummary.Type.values());
        typeCombo.setConnection(getConnection());
        typeCombo.setFromStreamType(Type.UNKNOWN);
        typeCombo.setParentStreamType(Type.UNKNOWN);
        typeCombo.setDifferentParentDepot(false);
        if(getWizard().isEditMode()){
        	typeCombo.setFromStreamType(getStream().getType());
        	IStreamSummary parentStream = StreamUtil.getParentStream(getStream(), getConnection());
        	if(parentStream!=null){
    			String depot = StreamUtil.getDepot(getStream());
    			String pdepot = StreamUtil.getDepot(parentStream);
				typeCombo.setDifferentParentDepot(!depot.equals(pdepot));
        		typeCombo.setParentStreamType(parentStream.getType());
        	}
        }
        if(sum!=null && sum.getType()!=null){
        	// this does not make selection visible, i.e., typeCombo.getSelection() is empty
        	typeCombo.setSelection(new StructuredSelection(sum.getType()));
        }
        
        SWTUtils.createLabel(composite, com.perforce.team.ui.streams.wizard.Messages.BasicSettingPage_ChangePropagation);
        propWidget=new PropagationOptionWidget(composite,SWT.NONE, getStream());
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(propWidget);
        
        parentWidget=new StreamParentSelectionWidget(composite, SWT.NONE, getConnection(), getWizard().getContainer());
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(parentWidget);
        parentWidget.init(getConnection(),getStream());
        enableParentWidget();

        SWTUtils.createLabel(composite, com.perforce.team.ui.streams.wizard.Messages.BasicSettingPage_StreamDescription);
        descText=SWTUtils.createTextArea(composite, 1, 1, 4);
        if(sum!=null && sum.getDescription()!=null){
            descText.setText(sum.getDescription());
        }
        
//        createWorkspaceBtn=SWTUtils.createButton(composite, com.perforce.team.ui.streams.wizard.Messages.BasicSettingPage_CreateWorkspace, SWT.CHECK, 1);
//        GridData gd=(GridData) createWorkspaceBtn.getLayoutData();
//        gd.exclude=getWizard().isEditMode();
//        createWorkspaceBtn.setVisible(!getWizard().isEditMode());
//        createWorkspaceBtn.setSelection(!getWizard().isEditMode());

        populateBtn=SWTUtils.createButton(composite, com.perforce.team.ui.streams.wizard.Messages.BasicSettingPage_BranchFromParent, SWT.CHECK, 1);
        GridData gd=(GridData) populateBtn.getLayoutData();
        gd.exclude=getWizard().isEditMode();
        populateBtn.setVisible(!getWizard().isEditMode());
        populateBtn.setSelection(!getWizard().isEditMode()); // selected by default

        scroll.setContent(composite);
        scroll.setExpandHorizontal(true);
        scroll.setExpandVertical(true);
        Point def = composite.computeSize(SWT.DEFAULT,
                SWT.DEFAULT);
        scroll.setMinSize(def);
//        Point b1=createWorkspaceBtn.computeSize(SWT.DEFAULT, SWT.DEFAULT);
//        Point b2=populateBtn.computeSize(SWT.DEFAULT, SWT.DEFAULT);
//        scroll.setMinSize(def.x, def.y+b1.y+b2.y);

        // set the composite as the control for this page
        setControl(scroll);

        restoreFromSettings();

        addListeners();

        scroll.getContent().setEnabled(!getWizard().isEditMode() || !getWizard().isStreamLocked());
    }

	@Override
	public boolean canFlipToNextPage() {
		if(getWizard().isStreamLocked())
			return true;
		
		return super.canFlipToNextPage();
	}
	
    protected void validateAndUpdate() {
        if((getWizard().isEditMode())&&getWizard().isStreamLocked()){
        	applyToStatusLine(ValidationStatus.warning(getWizard().getStreamLockMessage()));
        	return;
        }

        Tracing.printTrace("StreamWizard>>", StreamUtil.print(getStream())); //$NON-NLS-1$
        IStatus status = validate();
        applyToStatusLine(status);
        getWizard().getContainer().updateButtons();
    }


    @Override
    protected void addListeners() {
        WizardDialog dialog=(WizardDialog) getWizard().getContainer();
        dialog.addPageChangedListener(new IPageChangedListener() {
            public void pageChanged(PageChangedEvent event) {
                if(event.getSelectedPage()==BasicSettingPage.this){
                    validateAndUpdate();
                }
            }
        });
        
        nameText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                getStream().setName(nameText.getText());
                if(!getWizard().isEditMode()){
                    String depot=StreamUtil.getDepot(getStream());
                    if(StringUtils.isEmpty(depot)){
                       depot=StreamUtil.getParentDepot(getStream()); 
                    }
                    if(!StringUtils.isEmpty(depot) && !getWizard().rootFolderChanged){
                        StreamUtil.updateStream(getStream(), getStream().getName(), depot);
                    }
                }
            }
        });
        typeCombo.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel=(IStructuredSelection) typeCombo.getSelection();
				Object obj = sel.getFirstElement();
				if(obj instanceof Type){
					Type type=(Type) obj;
					getStream().setType(type);
					propWidget.setType(type);
					parentWidget.setType(type);
					showPopulateOption();
					enableParentWidget();
				}
			}
		});
        descText.addModifyListener(new ModifyListener() {
            
            public void modifyText(ModifyEvent e) {
                getStream().setDescription(descText.getText());
            }
        });
        
        SWTUtils.addContentListener(new Control[]{nameText,typeCombo.getControl(),descText, propWidget}, new Runnable() {
            public void run() {
                validateAndUpdate();
            }
        });
        
        parentWidget.getLocationValue().addValueChangeListener(new IValueChangeListener() {
            
            public void handleValueChange(ValueChangeEvent event) {
                Object newValue = event.diff.getNewValue();
                String btnText=""; //$NON-NLS-1$
                if(newValue instanceof SelectionModel){ // dev+ release + virtual
                	String stream=null;
                    SelectionModel value=(SelectionModel) newValue;
                  if(value!=null){
                      Object obj = value.getSelection(); 
                      if(obj instanceof IStreamSummary){
                          stream=((IStreamSummary) obj).getStream();
                      }
                  }
                  getStream().setParent(stream);
                  if(!StringUtils.isEmpty(getStream().getName())){
                      if(getStream().getStream()==null){
                          String depot = StreamUtil.getParentDepot(getStream());
                          getStream().setStream(depot+"/"+getStream().getName()); //$NON-NLS-1$
                      }
                  }
                  btnText=com.perforce.team.ui.streams.wizard.Messages.BasicSettingPage_BranchFromParent;
                }else if(newValue instanceof String){ // mainline type, parent be null
                    String depot=StreamUtil.normalizePath((String) newValue);
                    StreamUtil.updateStreamDepot(getStream(),depot);
                    getStream().setParent(null);
                    if(!StringUtils.isEmpty(getStream().getName())){
                        if(getStream().getStream()==null){
                            String d = StreamUtil.getDepot(getStream());
                            getStream().setStream(d+"/"+getStream().getName()); //$NON-NLS-1$
                        }
                    }
                    btnText=com.perforce.team.ui.streams.wizard.Messages.BasicSettingPage_PopulateMainStreamFromDepot;
                }else if(newValue instanceof TaskStreamModel){ // task stream, set parent and update stream depot
                	TaskStreamModel tm = (TaskStreamModel) newValue;
            		SelectionModel sm = tm.getBranchStream();
                	if(!StringUtils.isEmpty(tm.getDepot())){
                		String depot=StreamUtil.normalizePath(tm.getDepot());
                        StreamUtil.updateStreamDepot(getStream(),depot);
                        IStreamSummary ss = (IStreamSummary)sm.getSelection();
                        if(ss!=null)
                        	getStream().setParent(ss.getStream());
                        else
                        	getStream().setParent(null);
                	}else{
                		String stream=null;
                        if(sm!=null){
                            Object obj = sm.getSelection(); 
                            if(obj instanceof IStreamSummary){
                                stream=((IStreamSummary) obj).getStream();
                            }
                        }
                        getStream().setParent(stream);
                        if(!StringUtils.isEmpty(getStream().getName())){
                            if(getStream().getStream()==null){
                                String depot = StreamUtil.getParentDepot(getStream());
                                getStream().setStream(depot+"/"+getStream().getName()); //$NON-NLS-1$
                            }
                        }
                	}
                	if(sm!=null && sm.getSelection()!=null){
                		btnText=com.perforce.team.ui.streams.wizard.Messages.BasicSettingPage_BranchFromParent;
                	}else{
                		btnText=com.perforce.team.ui.streams.wizard.Messages.BasicSettingPage_PopulateTaskStreamFromDepot;
                	}
                }
                populateBtn.setText(btnText);
                System.err.println(btnText);
                validateAndUpdate();
            }
        });

    }

    protected void showPopulateOption() {
    	Type t = getStream().getType();
    	GridData gd = (GridData) populateBtn.getLayoutData();
    	boolean visible=false;
    	if(!getWizard().isEditMode()){
	    	if(t==Type.TASK||t==Type.DEVELOPMENT||t==Type.RELEASE||t==Type.MAINLINE){
	    		visible=true;
	    	}
    	}
    	populateBtn.setVisible(visible);
    	gd.exclude=!visible;
    	populateBtn.getParent().layout();
	}

	protected void enableParentWidget() {
		if(getWizard().isEditMode()){
			parentWidget.setEnabled(getWizard().getOriginalStream().getType()!=Type.TASK);
		}		
	}

	@Override
    public IStatus doValidate() {
        IStream stream = getStream();
        Assert.isNotNull(stream);
        if(StringUtils.isEmpty(stream.getName())){
            return ValidationStatus.error(com.perforce.team.ui.streams.wizard.Messages.BasicSettingPage_NameEmptyError);
        }
        
        if(stream.getType()==null){
            return ValidationStatus.error(com.perforce.team.ui.streams.wizard.Messages.BasicSettingPage_TypeEmptyError);
        }
        
        if(stream.getType()!=IStreamSummary.Type.MAINLINE && stream.getType()!=IStreamSummary.Type.TASK){
            if(StreamUtil.isParentEmpty(stream))
                return ValidationStatus.error(com.perforce.team.ui.streams.wizard.Messages.BasicSettingPage_ParentStreamNotExistError);
        }else{
            String depot = StreamUtil.getDepotName(stream);
            if(StringUtils.isEmpty(depot) || IConstants.UNKNOWN.equals(depot))
                return ValidationStatus.error(com.perforce.team.ui.streams.wizard.Messages.BasicSettingPage_StreamDepotNotSet);
        }
        
        if(stream.getStream()!=null && stream.getStream().equals(stream.getParent())){
            return ValidationStatus.error(Messages.AdvancedSettingPage_CannotCreateSelfParentStream);
        }
        
        if(!StreamUtil.isValidStreamFormat(stream))
            return ValidationStatus.error(MessageFormat.format(Messages.AdvancedSettingPage_StreamFormatIncorrect,stream.getStream()));

        return super.doValidate();
    }

	public boolean isPopulate() {
		if(!getWizard().isEditMode()){
			Type t = getStream().getType();
			if(t!=Type.VIRTUAL)
				return populateBtn.getSelection();
		}
		return false;
	}
    
}
