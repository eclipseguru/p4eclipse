package com.perforce.team.ui.streams.wizard;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.perforce.p4java.core.IStream;
import com.perforce.p4java.core.IStreamSummary;
import com.perforce.p4java.core.IStreamSummary.IOptions;
import com.perforce.p4java.core.IStreamSummary.Type;
import com.perforce.team.ui.SWTUtils;


public class PropagationOptionWidget extends Composite {

    private StackLayout stackLayout;
    
    private IStreamSummary.Type type=Type.DEVELOPMENT;
    private IOptions options;

    private Composite devControl;
    private Composite relControl;
    private Composite mainControl;
    private Composite virtualControl;
    private Composite taskControl;

	private Button toParentRel;
	private Button fromParentRel;

	private Button toParentDev;
	private Button fromParentDev;
	
	private Button toParentTask;
	private Button fromParentTask;
	
	private boolean enableListener=true;
	private boolean init=true;

    private IStream stream;

    public PropagationOptionWidget(Composite parent, int style, IStream stream) {
        super(parent, style);
        this.stream=stream;
        this.options=stream.getOptions();
        this.type=stream.getType();
        createControl(this);
    }

    public void setType(Type type){
        this.type=type;
        if(type == Type.DEVELOPMENT){
            stackLayout.topControl=devControl;

            if(!init){
	            // set default value
	            boolean state=enableListener;
	            enableListener=false;
	            fromParentDev.setSelection(false);
	            toParentDev.setSelection(true);
	            enableListener=state;
            }

            if(enableListener){
                options.setNoFromParent(!fromParentDev.getSelection());
                options.setNoToParent(!toParentDev.getSelection());
            }
        }else if(type == Type.RELEASE){
            stackLayout.topControl=relControl;
            
            if(!init){
	            // set default value
	            boolean state=enableListener;
	            enableListener=false;
	            fromParentRel.setSelection(false);
	            toParentRel.setSelection(true);
	            enableListener=state;
            }

            if(enableListener){
                options.setNoFromParent(!fromParentRel.getSelection());
                options.setNoToParent(!toParentRel.getSelection());
            }
        }else if(type == Type.TASK){
            stackLayout.topControl=taskControl;
            
            if(!init){
	            // set default value
	            boolean state=enableListener;
	            enableListener=false;
	            fromParentTask.setSelection(true);
	            toParentTask.setSelection(true);
	            enableListener=state;
            }

            if(enableListener){
                options.setNoFromParent(!fromParentTask.getSelection());
                options.setNoToParent(!toParentTask.getSelection());
            }
        }else if(type == Type.MAINLINE){
            stackLayout.topControl=mainControl;
            if(enableListener){
                options.setNoFromParent(true);
                options.setNoToParent(true);
            }
        }else if(type == Type.VIRTUAL){
            stackLayout.topControl=virtualControl;
            if(enableListener){
                options.setNoFromParent(true);
                options.setNoToParent(true);
            }
        }
        layout();
    }
    
    private void createControl(Composite parent) {
        stackLayout=new StackLayout();
        parent.setLayout(stackLayout);
        devControl=new Composite(parent, SWT.None);
        relControl=new Composite(parent, SWT.None);
        taskControl=new Composite(parent, SWT.None);
        mainControl=new Composite(parent, SWT.None);
        virtualControl=new Composite(parent, SWT.None);
        
        createDevControl(devControl);
        createRelControl(relControl);
        createTaskControl(taskControl);
        createMainControl(mainControl);
        createVirtualControl(virtualControl);
        
        stackLayout.topControl=devControl; // default

        enableListener=false;
        init();
        enableListener=true;

        addListeners();
    }

    private void addListeners() {
    	toParentDev.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(enableListener)
					options.setNoToParent(!toParentDev.getSelection());
			}
		});
    	fromParentDev.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(enableListener)
					options.setNoFromParent(!fromParentDev.getSelection());
			}
		});

    	toParentRel.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(enableListener)
					options.setNoToParent(!toParentRel.getSelection());
			}
		});
    	fromParentRel.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(enableListener)
					options.setNoFromParent(!fromParentRel.getSelection());
			}
		});
    	
    	toParentTask.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(enableListener)
					options.setNoToParent(!toParentTask.getSelection());
			}
		});
    	fromParentTask.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(enableListener)
					options.setNoFromParent(!fromParentTask.getSelection());
			}
		});
    	
	}

	private void createVirtualControl(Composite parent) {
        GridLayoutFactory.swtDefaults().applyTo(parent);
        SWTUtils.createLabel(parent, Messages.PropagationOptionWidget_VirtualStreamDescription);
    }

    private void createMainControl(Composite parent) {
        GridLayoutFactory.swtDefaults().applyTo(parent);
        SWTUtils.createLabel(parent, Messages.PropagationOptionWidget_MainlineStreamDescription);
    }

    private void createRelControl(Composite parent) {
        GridLayoutFactory.swtDefaults().applyTo(parent);
        toParentRel=SWTUtils.createCheckBox(parent, Messages.PropagationOptionWidget_AllowMergeToParent);
        fromParentRel=SWTUtils.createCheckBox(parent, Messages.PropagationOptionWidget_AllowCopyFromParent);
    }

    private void createTaskControl(Composite parent) {
        GridLayoutFactory.swtDefaults().applyTo(parent);
        toParentTask=SWTUtils.createCheckBox(parent, Messages.PropagationOptionWidget_AllowMergeToParent);
        fromParentTask=SWTUtils.createCheckBox(parent, Messages.PropagationOptionWidget_AllowCopyFromParent);
    }
    
    private void createDevControl(Composite parent) {
        GridLayoutFactory.swtDefaults().applyTo(parent);
        toParentDev=SWTUtils.createCheckBox(parent, Messages.PropagationOptionWidget_AllowCopyToParent);
        fromParentDev=SWTUtils.createCheckBox(parent, Messages.PropagationOptionWidget_AllowMergeFromParent);
    }

    public IOptions getOptions(){
    	return this.options;
    }

	protected void init() {
		if(this.options!=null){
	    	if(this.type==Type.DEVELOPMENT){
	    		toParentDev.setSelection(!this.options.isNoToParent());
	    		fromParentDev.setSelection(!this.options.isNoFromParent());
	    	}else if(this.type==Type.RELEASE){
	    		toParentRel.setSelection(!this.options.isNoToParent());
	    		fromParentRel.setSelection(!this.options.isNoFromParent());
	    	}else if(this.type==Type.TASK){
	    		toParentTask.setSelection(!this.options.isNoToParent());
	    		fromParentTask.setSelection(!this.options.isNoFromParent());
	    	}
		}
        setType(stream.getType());
        init=false;
	}

}
