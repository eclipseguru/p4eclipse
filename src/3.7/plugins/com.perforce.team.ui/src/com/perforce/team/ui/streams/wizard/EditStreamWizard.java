package com.perforce.team.ui.streams.wizard;

import java.text.MessageFormat;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

import com.perforce.p4java.core.IStream;
import com.perforce.p4java.core.IStreamSummary.Type;
import com.perforce.team.core.P4LogUtils;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Stream;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.p4java.actions.PopulateAction;
import com.perforce.team.ui.streams.StreamUtil;

/**
 * Wizard for edit/create stream spec
 */
public class EditStreamWizard extends Wizard implements IWorkbenchWizard {

    // wizard pages
    BasicSettingPage basicPage;
    AdvancedSettingPage advancedPage;

    private boolean editMode; // New or edit mode
    private IP4Connection connection;
    public boolean isEditMode(){
        return editMode;
    }

    // editing mode variables
    private IStream orgStream;
    private String snapShot;
    
    // new mode variables
    private IStream editStream;
    
    boolean rootFolderChanged; // whether stream root folder is changed manually.
    
    // stream locked and message.
	private boolean streamLocked;
	private String streamLockMessage;
	
	public String getStreamLockMessage() {
		return streamLockMessage;
	}

	public boolean isStreamLocked(){
		return streamLocked;
	}
    
    public EditStreamWizard(IP4Stream stream, boolean editMode) {
        super();
        this.editMode=editMode;
        setWindowTitle(MessageFormat.format(Messages.EditStreamWizard_StreamWizardTitle,(editMode?Messages.EditStreamWizard_Edit:Messages.EditStreamWizard_New)));
        connection = stream.getConnection();
        if(editMode){
            orgStream=(IStream) stream.getAdapter(IStream.class);
        }else{
            orgStream=StreamUtil.createNewStream(stream);
            orgStream.setDescription(MessageFormat.format(Messages.EditStreamWizard_CreatedBy, connection.getUser()));
        }
        editStream=StreamUtil.copyStream(orgStream);
        snapShot=ReflectionToStringBuilder.reflectionToString(editStream, new P4LogUtils.RecursiveToStringStyle(-1));
        initDialogSettings();
        
        setNeedsProgressMonitor(true);
        
        setStreamLockFlag(stream);
    }

    private void setStreamLockFlag(IP4Stream stream) {
        String owner = stream.getStreamSummary().getOwnerName();
        String editor = stream.getConnection().getUser();
        streamLocked=!(editor.equals(owner) || !stream.getStreamSummary().getOptions().isLocked());
        streamLockMessage=MessageFormat.format(Messages.EditStreamWizard_LockedBy,owner);
	}

	public EditStreamWizard(IP4Connection conn) {
        super();
        this.editMode=false;
        connection = conn;
        orgStream=StreamUtil.createNewStream(null);
        orgStream.setOwnerName(connection.getUser());
        orgStream.setDescription(MessageFormat.format(Messages.EditStreamWizard_CreatedBy, connection.getUser()));
        
        editStream=StreamUtil.copyStream(orgStream);
        snapShot=ReflectionToStringBuilder.reflectionToString(editStream, new P4LogUtils.RecursiveToStringStyle(-1));
        initDialogSettings();
        
        setNeedsProgressMonitor(true);
    }

    private void initDialogSettings() {
    }

    @Override
    public IDialogSettings getDialogSettings() {
        IDialogSettings dialogSettings = PerforceUIPlugin.getPlugin().getDialogSettings();
        IDialogSettings section = dialogSettings.getSection("EditStreamsWizard");//$NON-NLS-1$
        if (section == null) {
            section = dialogSettings.addNewSection("EditStreamsWizard");//$NON-NLS-1$
        }
        setDialogSettings(section);
        return section;
    }
    
    public void addPages() {
        basicPage = new BasicSettingPage();
        addPage(basicPage);
        advancedPage = new AdvancedSettingPage();
        addPage(advancedPage);
    }

    /**
     * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
     */
    public void init(IWorkbench workbench, IStructuredSelection selection) {
    }

    public boolean canFinish() {
        return super.canFinish() && modelChanged() && !StreamUtil.isStreamEmpty(getStream());
    }

    private boolean modelChanged() {
        String newShot = ReflectionToStringBuilder.reflectionToString(editStream, new P4LogUtils.RecursiveToStringStyle(-1));
        return !snapShot.equals(newShot);
    }

    @Override
    public boolean performFinish() {

        for(IWizardPage page:getPages()){
            if(page instanceof AbstractEditStreamWizardPage){
                ((AbstractEditStreamWizardPage)page).saveSettings();
            }
        }
        
        if(isEditMode()){
        	boolean doit=true;
        	Type orgType = orgStream.getType();
        	Type editType = editStream.getType();
        	if(orgType==Type.TASK && orgType!=editType){
        		doit=MessageDialog.openQuestion(null, Messages.EditStreamWizard_ConvertOrphanWarn, Messages.EditStreamWizard_ConvertOrphanToMain);
        	}
        	if(doit){
	        	try {
	        		connection.updateStream(editStream);
	        		StreamUtil.updateStream(editStream,orgStream);
	        	} catch (Exception e) {
	        		MessageDialog.openError(null, Messages.EditStreamWizard_Error, e.getLocalizedMessage());
	        		return false;
	        	}
        	}else
        		return false;
        }else{
            try {
                getConnection().createStream(editStream);
                if(basicPage.isPopulate()){
                	populateStream(editStream);
                }
            } catch (Exception e) {
                MessageDialog.openError(null, Messages.EditStreamWizard_Error, e.getLocalizedMessage());
                return false;
            }
        }
        
        return true;
    }
    
    private void populateStream(IStream stream) {
    	if(stream.getParent()!=null)
    		connection.populateStream(stream);
    	else{
    		PopulateAction action = new PopulateAction();
    		action.populate(connection, stream.getStream()+"/..."); //$NON-NLS-1$
    	}
    	
	}

	public IP4Connection getConnection() {
        return connection;
    }
    public IStream getStream() {
        return editStream;
    }
    public IStream getOriginalStream() {
        return orgStream;
    }
    
    @Override
    public boolean needsProgressMonitor() {
        return true;
    }
    
    public static IStatus validate(IP4Stream stream){
        return ValidationStatus.ok();
    }

}
