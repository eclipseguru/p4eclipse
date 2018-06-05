
package com.perforce.team.ui.streams.wizard;

import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.perforce.p4java.core.IStream;
import com.perforce.p4java.core.IStreamSummary;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.SWTUtils;
import com.perforce.team.ui.dialogs.FixedWidthScrolledComposite;
import com.perforce.team.ui.streams.StreamUtil;

public class AdvancedSettingPage extends AbstractEditStreamWizardPage {

    private Text ownerText;
    private Text rootFolderText;
    private Text pathsText;
    private Text remappedText;
    private Text ignoredText;
    private Button lockedBtn;
    private Button submitBtn;
    private Label depotPathLabel;

    public AdvancedSettingPage() {
        super(AdvancedSettingPage.class.getName());
        setImageDescriptor(PerforceUIPlugin.getPlugin().getImageDescriptor(
                IPerforceUIConstants.IMG_STREAM_WIZARD));
        setTitle(Messages.AdvancedSettingPage_Title);
        setDescription(Messages.AdvancedSettingPage_Description);

    }

    public void createControl(Composite parent) {
        IStream sum=(IStream) getStream();

        // create the composite to hold the widgets
        FixedWidthScrolledComposite scroll = new FixedWidthScrolledComposite(parent, SWT.V_SCROLL);
        scroll.setLayout(new FillLayout());
        Composite composite = new Composite(scroll, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);
        
        SWTUtils.createLabel(composite, Messages.AdvancedSettingPage_Owner);
        ownerText=SWTUtils.createText(composite);
        if(sum!=null && sum.getOwnerName()!=null){
            ownerText.setText(sum.getOwnerName());
        }
        
        SWTUtils.createPlaceholder(composite, 1);
        lockedBtn=SWTUtils.createCheckBox(composite, Messages.AdvancedSettingPage_StreamLockedToOwner);
        SWTUtils.createPlaceholder(composite, 1);
        submitBtn=SWTUtils.createCheckBox(composite, Messages.AdvancedSettingPage_SubmittingRestrictToOwner);
        if(sum!=null){
            lockedBtn.setSelection(sum.getOptions().isLocked());
            submitBtn.setSelection(sum.getOptions().isOwnerSubmit());
        }
        
        SWTUtils.createLabel(composite, Messages.AdvancedSettingPage_RootFolder);
        rootFolderText=SWTUtils.createText(composite);    
        SWTUtils.createLabel(composite, Messages.AdvancedSettingPage_DepotPath);
        depotPathLabel=SWTUtils.createLabel(composite, ""); //$NON-NLS-1$
        if(sum!=null && sum.getStream()!=null){
            rootFolderText.setText(StreamUtil.getStreamRoot(sum));
            depotPathLabel.setText(StreamUtil.makeStreamAbsolute(sum.getStream()));
        }
        
        GridDataFactory.swtDefaults().span(2,1).applyTo(
                SWTUtils.createLabel(composite, Messages.AdvancedSettingPage_StreamViewPaths));
        pathsText=SWTUtils.createTextArea(composite, 2, 1, 4);
        if(sum!=null && sum.getStreamView()!=null){
            pathsText.setText(StreamUtil.getStreamViewPaths(sum.getStreamView()));
        }
        
        GridDataFactory.swtDefaults().span(2,1).applyTo(
                SWTUtils.createLabel(composite, Messages.AdvancedSettingPage_RemmappedPaths));
        remappedText=SWTUtils.createTextArea(composite, 2, 1, 4);
        if(sum!=null && sum.getRemappedView()!=null){
            remappedText.setText(StreamUtil.getRemmapedPaths(sum.getRemappedView()));
        }
        
        GridDataFactory.swtDefaults().span(2,1).applyTo(
                SWTUtils.createLabel(composite, Messages.AdvancedSettingPage_IgnoredPaths));
        ignoredText=SWTUtils.createTextArea(composite, 2, 1, 4);
        if(sum!=null && sum.getIgnoredView()!=null){
            ignoredText.setText(StreamUtil.getIgnoredPatterns(sum.getIgnoredView()));
        }

        scroll.setContent(composite);
        scroll.setExpandHorizontal(true);
        scroll.setExpandVertical(true);
        scroll.setMinSize(composite.computeSize(SWT.DEFAULT,
                SWT.DEFAULT));

        // set the composite as the control for this page
        setControl(scroll);

        restoreFromSettings();

        addListeners();
        
        scroll.getContent().setEnabled(!getWizard().isEditMode() || !getWizard().isStreamLocked());

    }

    @Override
    protected void addListeners() {
        WizardDialog dialog=(WizardDialog) getWizard().getContainer();
        dialog.addPageChangedListener(new IPageChangedListener() {
            public void pageChanged(PageChangedEvent event) {
                if(event.getSelectedPage()==AdvancedSettingPage.this){
                    updatePage();
                }
            }
        });

        lockedBtn.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                getStream().getOptions().setLocked(lockedBtn.getSelection());
                validateAndUpdate();
            }
        });
        submitBtn.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                getStream().getOptions().setOwnerSubmit(submitBtn.getSelection());
                validateAndUpdate();
            }
        });
        
        ownerText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                getStream().setOwnerName(ownerText.getText());
                validateAndUpdate();
            }
        });
        rootFolderText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                String streamRoot = rootFolderText.getText();
                String streamPath=StreamUtil.getParentDepot(getStream())+"/"+streamRoot; //$NON-NLS-1$
                if(getStream().getType()==IStreamSummary.Type.MAINLINE){
                    streamPath=StreamUtil.getDepot(getStream())+"/"+streamRoot; //$NON-NLS-1$
                }
                depotPathLabel.setText(StreamUtil.makeStreamAbsolute(streamPath));
                depotPathLabel.getParent().layout();
                getStream().setStream(streamPath);
                validateAndUpdate();
                if(!getWizard().rootFolderChanged)
                    getWizard().rootFolderChanged=true;
            }
        });
        pathsText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                getStream().setStreamView(StreamUtil.parseStreamViewMapping(pathsText.getText()));
                validateAndUpdate();
            }
        });
        remappedText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                getStream().setRemappedView(StreamUtil.parseRemappedMapping(remappedText.getText()));
                validateAndUpdate();
            }
        });
        ignoredText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                getStream().setIgnoredView(StreamUtil.parseIgnoredMapping(ignoredText.getText()));
                validateAndUpdate();
            }
        });
        
    }

    protected void updatePage() {
        if(getStream().getType()== IStreamSummary.Type.VIRTUAL){
            submitBtn.setSelection(false);
            submitBtn.setEnabled(false);
            submitBtn.setToolTipText(Messages.AdvancedSettingPage_SubmitRestrictionNeedMadeInSourceStream);
        }else{
            submitBtn.setEnabled(true);
            submitBtn.setToolTipText(null);
        }
        rootFolderText.setEnabled(!getWizard().isEditMode());
        if(StringUtils.isEmpty(rootFolderText.getText()) || !getWizard().rootFolderChanged){
            if(getWizard().isEditMode())
                rootFolderText.setText(StreamUtil.getStreamRoot(getStream())); // edit mode
            else
                rootFolderText.setText(StreamUtil.escapeWhiteSpace(getStream().getName())); // new mode
            getWizard().rootFolderChanged=false;
        }
    }

    @Override
    protected IStatus doValidate() {
        if(getWizard().isEditMode() && getWizard().isStreamLocked()){
        	return ValidationStatus.warning(getWizard().getStreamLockMessage());
        }

        IStream stream = getStream();
        Assert.isNotNull(stream);
        if(StringUtils.isEmpty(stream.getOwnerName())){
            return ValidationStatus.error(Messages.AdvancedSettingPage_StreamOwnerEmptyError);
        }
        
        if(StringUtils.isEmpty(stream.getStream())){
            return ValidationStatus.error(Messages.AdvancedSettingPage_StreamRootEmptyError);
        }
 
        if(stream.getStream()!=null && stream.getStream().equals(stream.getParent())){
            return ValidationStatus.error(Messages.AdvancedSettingPage_CannotCreateSelfParentStream);
        }
        
        if(!StreamUtil.isValidStreamFormat(stream))
            return ValidationStatus.error(MessageFormat.format(Messages.AdvancedSettingPage_StreamFormatIncorrect,stream.getStream()));

        return super.doValidate();
    }
    
    protected void validateAndUpdate() {
        StreamUtil.print(getStream());
        IStatus status = validate();
        applyToStatusLine(status);
        getWizard().getContainer().updateButtons();
    }

}
