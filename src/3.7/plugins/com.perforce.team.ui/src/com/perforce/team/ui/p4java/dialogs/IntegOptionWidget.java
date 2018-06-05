package com.perforce.team.ui.p4java.dialogs;

import java.text.MessageFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.perforce.team.core.p4java.P4IntegrationOptions;
import com.perforce.team.core.p4java.P4IntegrationOptions2;
import com.perforce.team.core.p4java.P4IntegrationOptions3;

public enum IntegOptionWidget{
	INTEG2(2){
		@Override
	    public void createOptionsArea(Composite parent) {
	        fileOptions = new Group(parent, SWT.NONE);
	        fileOptions.setText(MessageFormat.format(Messages.IntegrateDialog_AdvancedOptions,level));
	        GridLayout oLayout = new GridLayout(2, false);
	        fileOptions.setLayout(oLayout);
	        fileOptions
	                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	        doNotCopyButton = new Button(fileOptions, SWT.CHECK);
	        doNotCopyButton.setText(Messages.IntegrateDialog_DontCopyTargetFiles);
	        baselessMergesButton = new Button(fileOptions, SWT.CHECK);
	        baselessMergesButton
	                .setText(Messages.IntegrateDialog_EnableBaselessMerges);
	        disregardAllHistoryButton = new Button(fileOptions, SWT.CHECK);
	        disregardAllHistoryButton
	                .setText(Messages.IntegrateDialog_DisregardIntegHistory);
	        integrateAroundDeletedButton = new Button(fileOptions, SWT.CHECK);
	        integrateAroundDeletedButton
	                .setText(Messages.IntegrateDialog_EnableIntegAroundDeletedRevs);
	        doNotGetLatestButton = new Button(fileOptions, SWT.CHECK);
	        doNotGetLatestButton.setText(Messages.IntegrateDialog_DontGetLatestRev);
	        propogateFiletypesButton = new Button(fileOptions, SWT.CHECK);
	        propogateFiletypesButton
	                .setText(Messages.IntegrateDialog_PropagateSourceFiletypes);
	        reverseButton = new Button(fileOptions, SWT.CHECK);
	        reverseButton.setText(Messages.IntegrateDialog_ReverseBranchMappings);
	    }
		
		@Override
		public void initControl(P4IntegrationOptions options){
			super.initControl(options);
	        if (options instanceof P4IntegrationOptions2) {
	        	P4IntegrationOptions2 opt=(P4IntegrationOptions2) options;
	            baselessMergesButton.setSelection(opt.isBaselessMerge());
	            integrateAroundDeletedButton.setSelection(opt
	                    .isIntegrateAroundDeleted());
	            propogateFiletypesButton.setSelection(opt.isPropagateType());
	        }			
		}
		
		@Override
		public void updateOptions(P4IntegrationOptions options) {
			super.updateOptions(options);
			
	        if (options instanceof P4IntegrationOptions2) {
	        	P4IntegrationOptions2 opt=(P4IntegrationOptions2) options;
			    opt.setBaselessMerge(baselessMergesButton.getSelection());
			    opt.setIntegrateAroundDeleted(integrateAroundDeletedButton
			    		.getSelection());
			    opt.setPropagateType(propogateFiletypesButton.getSelection());
	        }
		}

		@Override
		public P4IntegrationOptions getDefaultIntegrationOptions() {
			return new P4IntegrationOptions2();
		}
		

	}, 
	INTEG3(3){

		@Override
		public void createOptionsArea(Composite parent) {
	        fileOptions = new Group(parent, SWT.NONE);
	        fileOptions.setText(MessageFormat.format(Messages.IntegrateDialog_AdvancedOptions,level));
	        GridLayout oLayout = new GridLayout(2, false);
	        fileOptions.setLayout(oLayout);
	        fileOptions
	                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	        doNotCopyButton = new Button(fileOptions, SWT.CHECK);
	        doNotCopyButton.setText(Messages.IntegrateDialog_DontCopyTargetFiles);
	        integrateAroundDeletedButton = new Button(fileOptions, SWT.CHECK);
	        integrateAroundDeletedButton
	        .setText(Messages.IntegOptionWidget_EnableIntegrationsAroundDeletedRev);
	        disregardAllHistoryButton = new Button(fileOptions, SWT.CHECK);
	        disregardAllHistoryButton
	                .setText(Messages.IntegrateDialog_DisregardIntegHistory);
	        branchResolvesButton = new Button(fileOptions, SWT.CHECK);
	        branchResolvesButton
	                .setText(Messages.IntegOptionWidget_BranchResolves);

	        doNotGetLatestButton = new Button(fileOptions, SWT.CHECK);
	        doNotGetLatestButton.setText(Messages.IntegrateDialog_DontGetLatestRev);
	        deleteResolvesButton = new Button(fileOptions, SWT.CHECK);
	        deleteResolvesButton.setText(Messages.IntegOptionWidget_DeleteResolves);
	        propogateFiletypesButton = new Button(fileOptions, SWT.CHECK);
	        propogateFiletypesButton
	                .setText(Messages.IntegrateDialog_PropagateSourceFiletypes);
	        skipIntegratedButton = new Button(fileOptions, SWT.CHECK);
	        skipIntegratedButton.setText(Messages.IntegOptionWidget_SkipIntegratedRevs);
	        reverseButton = new Button(fileOptions, SWT.CHECK);
	        reverseButton.setText(Messages.IntegrateDialog_ReverseBranchMappings);
		}

		@Override
		public void initControl(P4IntegrationOptions options){
			super.initControl(options);
	        if (options instanceof P4IntegrationOptions3) {
	        	P4IntegrationOptions3 opt=(P4IntegrationOptions3) options;
	            integrateAroundDeletedButton.setSelection(opt.isIntegrateAroundDeleted());
	            branchResolvesButton.setSelection(opt.isBranchResolves());
	            deleteResolvesButton.setSelection(opt.isDeleteResolves());
	            skipIntegratedButton.setSelection(opt.isSkipIntegratedRevs());
	        }			
		}
		
		@Override
		public void updateOptions(P4IntegrationOptions options) {
			super.updateOptions(options);
			
	        if (options instanceof P4IntegrationOptions3) {
	        	P4IntegrationOptions3 opt=(P4IntegrationOptions3) options;
				opt.setIntegrateAroundDeleted(integrateAroundDeletedButton
						.getSelection());
			    opt.setBranchResolves(branchResolvesButton.getSelection());
			    opt.setDeleteResolves(deleteResolvesButton.getSelection());
			    opt.setSkipIntegratedRevs(skipIntegratedButton.getSelection());
	        }
		}

		@Override
		public P4IntegrationOptions getDefaultIntegrationOptions() {
			return new P4IntegrationOptions3();
		}
		
	};
	
    protected Group fileOptions;
    // left columns
    protected Button doNotCopyButton;
    protected Button disregardAllHistoryButton;
    protected Button doNotGetLatestButton;
    protected Button reverseButton;
    
    // right columns
    protected Button integrateAroundDeletedButton;
    // integ -2
    protected Button baselessMergesButton;
    protected Button propogateFiletypesButton;
    // integ -3
    protected Button branchResolvesButton;
    protected Button deleteResolvesButton;
    protected Button skipIntegratedButton;

    protected int level;
    
    private IntegOptionWidget(int level){
    	this.level=level;
    }
    
    public abstract void createOptionsArea(Composite parent);
	public abstract P4IntegrationOptions getDefaultIntegrationOptions();
    
	public void updateOptions(P4IntegrationOptions options){
	    options.setDontCopyToClient(doNotCopyButton.getSelection());
	    options.setUseHaveRev(doNotGetLatestButton.getSelection());
	    options.setForce(disregardAllHistoryButton.getSelection());
	    options.setReverseMapping(reverseButton.getSelection());
	}
	
	public void enableReverseButton(boolean enabled){
        reverseButton.setEnabled(enabled);			
	}
	
	public void initControl(P4IntegrationOptions options){
        if (options != null) {
            doNotCopyButton.setSelection(options.isDontCopyToClient());
            disregardAllHistoryButton.setSelection(options.isForce());
            doNotGetLatestButton.setSelection(options.isUseHaveRev());
            reverseButton.setSelection(options.isReverseMapping());
        }
	}
	
	///////////////////////// START: For Test only api /////////////////////////////
    /**
     * Set reverse mappings button
     * 
     * @param reverse
     */
    public void setReverseMappings(boolean reverse) {
    	if(this.reverseButton!=null)
    		this.reverseButton.setSelection(reverse);
    }

    /**
     * Set do not copy button
     * 
     * @param doNotCopy
     */
    public void setDoNotCopy(boolean doNotCopy) {
    	if(this.doNotCopyButton!=null)
    		this.doNotCopyButton.setSelection(doNotCopy);
    }

    /**
     * Set baseless merge button
     * 
     * @param merge
     */
    public void setBaselessMerge(boolean merge) {
    	if(this.baselessMergesButton!=null)
    		this.baselessMergesButton.setSelection(merge);
    }

    /**
     * Set disregard history button
     * 
     * @param disregard
     */
    public void setDisregardHistory(boolean disregard) {
    	if(this.disregardAllHistoryButton!=null)
    		this.disregardAllHistoryButton.setSelection(disregard);
    }

    /**
     * Set integrate around deleted button
     * 
     * @param integrate
     */
    public void setIntegrateAroundDeleted(boolean integrate) {
    	if(this.integrateAroundDeletedButton!=null)
    		this.integrateAroundDeletedButton.setSelection(integrate);
    }

    /**
     * Set do not get latest revision button
     * 
     * @param latest
     */
    public void setDoNotGetLatest(boolean latest) {
    	if(this.doNotGetLatestButton!=null)
    		this.doNotGetLatestButton.setSelection(latest);
    }

    /**
     * Set propogate filetypes button
     * 
     * @param propogate
     */
    public void setPropogateFiletypes(boolean propogate) {
    	if(this.propogateFiletypesButton!=null)
    		this.propogateFiletypesButton.setSelection(propogate);
    }
	///////////////////////// END: For Test only api /////////////////////////////

}