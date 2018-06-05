package com.perforce.team.ui.streams.wizard;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.progress.UIJob;

import com.perforce.p4java.core.IDepot;
import com.perforce.p4java.core.IStream;
import com.perforce.p4java.core.IStreamSummary;
import com.perforce.p4java.core.IStreamSummary.Type;
import com.perforce.p4java.option.server.GetStreamsOptions;
import com.perforce.team.core.IConstants;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Stream;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.ui.streams.Messages;
import com.perforce.team.ui.streams.StreamUtil;
import com.perforce.team.ui.streams.StreamsSuggestProvider;
import com.perforce.team.ui.streams.SuggestBox;
import com.perforce.team.ui.streams.SuggestBox.SelectionModel;


public class StreamParentSelectionWidget extends Composite {

    private IP4Connection connection;

    private StreamsSuggestProvider provider;
    private SuggestBox streamCombo;
    private Combo depotCombo;
    private Label depotLabel;
    private Label streamLabel;
    
    private ChangeableWriteValue locationValue=new ChangeableWriteValue(); // location: 1) String as Depot path, 2)SelectionModel as stream 3) TaskStreamModel
    private IStream stream;
    
    private IRunnableContext context;
    
    private SelectionListener depotListener=new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			updateLocationValue();
		}
	};
    
    public WritableValue getLocationValue(){
        return locationValue;
    }
    
    public StreamParentSelectionWidget(Composite parent, int style, IP4Connection conn, IRunnableContext context) {
        super(parent, style);
        this.connection=conn;
        this.context=context;
		provider = new StreamsSuggestProvider(connection) {
			@Override
			protected List<String> initPaths(String filter) {
				List<String> paths = null;
				if (filter.startsWith("//")) { // in case of search based on stream path
					paths = new ArrayList<String>();
					String path=filter;
					if(filter.length()>2){
						if(!filter.substring(2).contains("/")){//$NON-NLS-1$
							if(filter.endsWith("*"))//$NON-NLS-1$
								path=filter+"/*";//$NON-NLS-1$
							else
								path=filter+"*/*";//$NON-NLS-1$ */... should also work.
						}else{
							if(filter.endsWith("*"))//$NON-NLS-1$
								path=filter;
							else
								path=filter+"*";//$NON-NLS-1$
						}
					}else{
						path=filter+"...";//$NON-NLS-1$
					}
					paths.add(path);
					return paths;
				}
				paths = super.initPaths(filter);
				return paths;
			}

			@Override
			public List<Object> fetchElement(String filter) {
				List<Object> fetchElements = super.fetchElement(filter);
				List<Object> result = new ArrayList<Object>();
				for (Object obj : fetchElements) {
					if (obj instanceof IStreamSummary) {
						if (((IStreamSummary) obj).getType() == Type.TASK) {
							if (stream.getType() == Type.TASK)
								continue;
						}
					}
					result.add(obj);
				}
				return result;
			}

		};
		createControl(this);
	}

    private void createControl(Composite parent) {
        GridLayoutFactory.swtDefaults().margins(1,5).applyTo(parent);
        
        boolean visible=true;
        depotLabel = new Label(parent, SWT.NONE);
        depotLabel.setText(Messages.StreamWizard_ParentStreamToBranchFrom);
        GridDataFactory.swtDefaults().span(1,1).align(SWT.BEGINNING, SWT.CENTER).exclude(!visible).applyTo(depotLabel);
        depotCombo=new Combo(parent, SWT.DROP_DOWN|SWT.READ_ONLY);
        depotCombo.setVisible(visible);
        GridDataFactory.swtDefaults().span(1,1).align(SWT.FILL, SWT.CENTER).grab(true, false).exclude(!visible).applyTo(depotCombo);
        
        
        visible=false;
        streamLabel = new Label(parent, SWT.NONE);
        streamLabel.setText(Messages.StreamWizard_ParentStreamToBranchFrom);
        GridDataFactory.swtDefaults().span(1,1).align(SWT.BEGINNING, SWT.CENTER).exclude(!visible).applyTo(streamLabel);        
        streamCombo = new SuggestBox(parent, SWT.None, provider);
        streamCombo.setVisible(visible);
        GridDataFactory.swtDefaults().span(1,1).align(SWT.FILL, SWT.CENTER).indent(10,5).grab(true, false).exclude(!visible).applyTo(streamCombo);
        
        addListeners();

    }
    
    private void addListeners() {
    	depotCombo.addSelectionListener(depotListener);
    	streamCombo.getModel().addValueChangeListener(new IValueChangeListener() {
			
			public void handleValueChange(ValueChangeEvent event) {
				updateLocationValue();
			}
		});
	}

	protected void updateLocationValue() {
		SelectionModel model=(SelectionModel) streamCombo.getModel().getValue();
		String depot = null;
		if(depotCombo.isVisible())
			depot = depotCombo.getText();

    	if(!streamCombo.getVisible()){
    		locationValue.setValue(depot);
    	}else if(!depotCombo.getVisible()){
    		locationValue.setValue(model);
    	}else{
    		locationValue.setValue(new TaskStreamModel(depot, model));
    	} 
	}

	public void setType(Type childType){
		setType(childType, true);
	}
	
	private void setType(Type childType, boolean propagateChange){
        if(childType==Type.MAINLINE){
            depotLabel.setText(Messages.StreamWizard_DepotMainlineStreamLocated);
            showControl(true,depotLabel);
            showControl(true,depotCombo);
            showControl(false,streamLabel);
            showControl(false,streamCombo);
            
            String parent=null;
            if(this.stream!=null){
                IStreamSummary sum=this.stream;
                parent=StreamUtil.getDepotName(sum);
            }
            if(propagateChange)
                parent=depotCombo.getText();
            
            if(parent!=null){
                int index = ArrayUtils.indexOf(depotCombo.getItems(),parent);
                if(index>=0)
                    depotCombo.select(index);
            }
            if(propagateChange){
                locationValue.setValue(parent);
            }
        }else if(childType==Type.VIRTUAL){
            streamLabel.setText(Messages.StreamWizard_ParentStreamToSourceFrom);
            showControl(false,depotLabel);
            showControl(false,depotCombo);
            showStreamCombo();
        }else if(childType==Type.DEVELOPMENT || childType==Type.RELEASE){
            streamLabel.setText(Messages.StreamWizard_ParentStreamToBranchFrom);
            showControl(false,depotLabel);
            showControl(false,depotCombo);
            showStreamCombo();
        }else if(childType==Type.TASK){
            depotLabel.setText(Messages.StreamWizard_DepotTaskStreamLocated);
            showControl(true,depotLabel);
            showControl(true,depotCombo);
            streamLabel.setText(Messages.StreamWizard_ParentStreamToBranchFrom);
            showControl(true,streamLabel);
            showControl(true,streamCombo);
        }
    }

	private void showControl(boolean visible, Control control) {
		Object ld = control.getLayoutData();
		if(ld instanceof GridData){
			GridData data=(GridData) ld;
			data.exclude=!visible;
			control.setVisible(visible);
			Composite parent = control.getParent();
			if(parent!=null) {
				parent.layout();
				parent = parent.getParent();
				if(parent!=null) 
					parent.layout();
			}
		}
	}

	private void showStreamCombo() {
		showControl(true, streamLabel);
		showControl(true, streamCombo);
        
        if(stream!=null && !StreamUtil.isParentEmpty(stream)){ // update UI per stream setting
            IStreamSummary s=this.stream;
            String parent=s.getParent();
            
            if(StreamUtil.isStreamPathFormat(parent)){
            	parent=StreamUtil.normalizeStreamPathForQuery(parent);
                IP4Stream ps = this.connection.getStream(parent);
                if(ps!=null){
                    streamCombo.updateModel(new SelectionModel(parent, ps.getStreamSummary()));
                }else
                    streamCombo.setTextQuietlyAndUpdateModel(parent);
            }
        }else if(stream!=null){ // update stream per UI setting
        	SelectionModel model = (SelectionModel) streamCombo.getModel().getValue();
        	if(model!=null){
        		IStreamSummary s=(IStreamSummary) model.getSelection();
        		if(s!=null){
        			stream.setParent(s.getParent());
        			String depot=StreamUtil.getDepot(s);
        			String root=StreamUtil.getStreamRoot(stream);
        			StreamUtil.updateStream(stream, root, depot);
        		}
        	}
        }
        updateLocationValue();
    }

    public void init(IP4Connection conn, IStream s) {
        this.stream = s;
        this.connection = conn;

        try {
            context.run(false, false, new IRunnableWithProgress() {

                public void run(final IProgressMonitor monitor)
                        throws InvocationTargetException, InterruptedException {
                    monitor.beginTask(
                            Messages.StreamsFilterWidget_LoadingDepots, 10);
                    monitor.worked(5);

                    P4Runner.schedule(new P4Runnable() {

                        @Override
                        public String getTitle() {
                            return Messages.StreamsFilterWidget_LoadingDepots;
                        }

                        @Override
                        public void run(IProgressMonitor monitor) {
                            try {
                                final List<IDepot> depots = connection.getDepots();

                                GetStreamsOptions opts = new GetStreamsOptions();
                                opts.setFields("Name,Stream"); //$NON-NLS-1$

                                UIJob job = new UIJob(
                                        Messages.StreamsFilterWidget_LoadingDepots) {

                                    @Override
                                    public IStatus runInUIThread(
                                            IProgressMonitor monitor) {
                                        monitor.done();
                                        if (depotCombo.isDisposed())
                                            return Status.CANCEL_STATUS;

                                        depotCombo.clearSelection();
                                        depotCombo.removeAll();
                                        depotCombo.add(IConstants.EMPTY_STRING);
                                        for (IDepot depot : depots) {
                                            if (IDepot.DepotType.STREAM
                                                    .equals(depot
                                                            .getDepotType())) {
                                                depotCombo.add(depot.getName());
                                            }
                                        }
                                        
                                        if (stream != null) {
                                            setType(stream.getType(), false);
                                            String parentDepot = StreamUtil.getDepotName(stream);
                                            if(parentDepot!=null){
                                            	selectDepot(depotCombo.indexOf(parentDepot), false);
                                            }
                                            streamCombo.updateModel(new SelectionModel(null, StreamUtil.getParentStream(stream, connection)));
                                        }

                                        return Status.OK_STATUS;
                                    }

                                };
                                job.schedule();
                            } catch (Throwable e) {
                                PerforceProviderPlugin.logError(e);
                            }

                        }
                    });
                }
            });
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
    	super.setEnabled(enabled);
    	this.streamCombo.setEnabled(enabled);
    	if(!enabled){
    		this.depotCombo.setBackground(getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
    	}else{ 
    		this.depotCombo.setBackground(getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
    	}
    }

	private void selectDepot(int index,
			boolean propagateChange) {
		if(!propagateChange)
			depotCombo.removeSelectionListener(depotListener);
		depotCombo.select(index);
		if(!propagateChange)
			depotCombo.addSelectionListener(depotListener);
		
	}

    public static class TaskStreamModel{
    	String depot;
    	SelectionModel branchStream;
		public TaskStreamModel(String depot, SelectionModel parentStream) {
			super();
			this.depot = depot;
			this.branchStream = parentStream;
		}
		public String getDepot() {
			return depot;
		}
		public void setDepot(String depot) {
			this.depot = depot;
		}
		public SelectionModel getBranchStream() {
			return branchStream;
		}
		public void setBranchStream(SelectionModel branchStream) {
			this.branchStream = branchStream;
		}
		
    }
    
}
