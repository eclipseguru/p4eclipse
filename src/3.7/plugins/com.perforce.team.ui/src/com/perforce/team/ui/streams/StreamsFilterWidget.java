package com.perforce.team.ui.streams;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.progress.UIJob;

import com.perforce.p4java.core.IDepot;
import com.perforce.p4java.core.IStreamSummary;
import com.perforce.p4java.option.server.GetStreamsOptions;
import com.perforce.team.core.IConstants;
import com.perforce.team.core.P4JavaEnumHelper;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.SWTUtils;
import com.perforce.team.ui.streams.SuggestBox.SelectionModel;
import com.perforce.team.ui.views.PerforceProjectView;
import com.perforce.team.ui.views.SessionManager;

/**
 * Streams Filter Widget
 * 
 * @author ali
 *
 */
public class StreamsFilterWidget extends Composite{
    // preferece key in widget data map
    private static final String PREF_KEY = "prefId"; //$NON-NLS-1$
    private static final int HISTORY_SIZE=10;
    
	// preference keys
    public static final String STREAMS_NAME_HISTORY = "com.perforce.team.ui.streams.STREAMS_NAME_HISTORY"; //$NON-NLS-1$
    public static final String STREAMS_OWNER_HISTORY = "com.perforce.team.ui.streams.STREAMS_OWNER_HISTORY"; //$NON-NLS-1$
    public static final String STREAMS_PARENT_HISTORY = "com.perforce.team.ui.streams.STREAMS_PARENT_HISTORY"; //$NON-NLS-1$

    // Observable others can listen to 
	private WritableValue filterModel;

	private Combo nameCombo;
	private Combo ownerCombo;
	private Combo typeCombo;
	private SuggestBox parentCombo;
	private Combo depotCombo;
	private Button showUnloadedOnlyBtn; // show unloaded task stream only
    
    private boolean hasParent; // has parent filter
 
    private SelectionListener comboAdapter = P4UIUtils
            .createComboSelectionListener(new Runnable() {
                public void run() {
                	showUnloadedOnlyBtn.setVisible(IStreamSummary.Type.TASK.name().equalsIgnoreCase(typeCombo.getText()));
                    updateObservable();
                }
            });
    private StreamsSuggestProvider provider;

    //////// relate to update on text change for name and user combo ////////////
    private FocusListener focusAdapter=new FocusListener() {
        public void focusLost(FocusEvent e) {
            if(autoUpdate)
                updateObservable();
        }
        public void focusGained(FocusEvent e) {
        }
    };
    
    private boolean autoUpdate=false;
    public void enableAutoUpdate(boolean enable){
        this.autoUpdate=enable;
    }
    //////////////// end ////////////////////////////////////////////////
    
    public StreamsFilterWidget(Composite parent, int style, WritableValue filterModel, boolean hasParent) {
        super(parent, style);
    	this.filterModel=filterModel;
    	this.hasParent=hasParent;
    	
    	createControl();
    	
    	addListeners();
    }
    
    private void addListeners() {
        // Listen for submitted view preference changes
        IPreferenceStore store =PerforceUIPlugin.getPlugin().getPreferenceStore();
        store.addPropertyChangeListener(new IPropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                String property = event.getProperty();
                if (property == IPerforceUIConstants.PREF_STREAM_DISPLAY) {
                    if (parentCombo != null) {
                        parentCombo.refresh();
                    }
                }
            }
        });
    }

    public void createControl() {
    	
        GridLayout fcLayout = new GridLayout(6, false);
        this.setLayout(fcLayout);
        this.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                false));
        // name
        SWTUtils.createLabel(this, Messages.StreamsFilterWidget_Name);
        nameCombo=createComboFromHistory(this, STREAMS_NAME_HISTORY);
        GridDataFactory.swtDefaults().span(1,1).align(SWT.FILL, SWT.CENTER).indent(10, -1).grab(true, false).applyTo(nameCombo);

		nameCombo.addSelectionListener(comboAdapter);
		nameCombo.addFocusListener(focusAdapter);
        createClearButton(this,nameCombo, comboAdapter);
            
        // owner
        SWTUtils.createLabel(this, Messages.StreamsFilterWidget_Owner);
        ownerCombo=createComboFromHistory(this, STREAMS_OWNER_HISTORY);
        GridDataFactory.swtDefaults().span(1,1).align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(ownerCombo);
		ownerCombo.addSelectionListener(comboAdapter);
        nameCombo.addFocusListener(focusAdapter);
        createClearButton(this,ownerCombo, comboAdapter);
        
        // type
        SWTUtils.createLabel(this, Messages.StreamsFilterWidget_Type);
        typeCombo=new Combo(this, SWT.DROP_DOWN);
        GridDataFactory.swtDefaults().span(1,1).align(SWT.FILL, SWT.CENTER).indent(10, -1).grab(true, false).applyTo(typeCombo);
        for (IStreamSummary.Type item : P4JavaEnumHelper.filterUnknownValues(IStreamSummary.Type.values())) {
            typeCombo.add(item.name().toLowerCase());
        }
		typeCombo.addSelectionListener(comboAdapter);
        createClearButton(this,typeCombo, comboAdapter);
        
        // depot
        SWTUtils.createLabel(this, Messages.StreamsFilterWidget_Depot);
        depotCombo=new Combo(this, SWT.DROP_DOWN);
        GridDataFactory.swtDefaults().span(1,1).align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(depotCombo);
		depotCombo.addSelectionListener(comboAdapter);
        createClearButton(this,depotCombo, comboAdapter);

        // parent
        if(hasParent){
            createParentFilter();
        }
        
        // Show unloaded task stream only
        showUnloadedOnlyBtn=new Button(this, SWT.CHECK);
        showUnloadedOnlyBtn.setText(Messages.StreamsFilterWidget_ShowUnloadedTaskStreamOnly);
        GridDataFactory.swtDefaults().span(3,1).align(SWT.BEGINNING, SWT.CENTER).grab(false, false).applyTo(showUnloadedOnlyBtn);
        showUnloadedOnlyBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(IStreamSummary.Type.TASK.name().equalsIgnoreCase(typeCombo.getText()))
					updateObservable();
			}
		});
        showUnloadedOnlyBtn.setVisible(false);
    }

	private void createParentFilter() {
		provider=new StreamsSuggestProvider(null);
		SWTUtils.createLabel(this, Messages.StreamsFilterWidget_Parent);
		parentCombo = new SuggestBox(this, SWT.None, provider);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).indent(10, -1).grab(true, false).applyTo(parentCombo);
        createClearButton(this,parentCombo, comboAdapter);
		
        parentCombo.getModel().addValueChangeListener(new IValueChangeListener() {
            public void handleValueChange(ValueChangeEvent event) {
                updateObservable();
            }
        });
        
    }

    protected void updateObservable() {
		saveComboHistory(nameCombo);
		saveComboHistory(ownerCombo);
		StreamsFilterModel newModel = computeModel();
		this.filterModel.setValue(newModel);
	}

	public StreamsFilterModel computeModel() {
		StreamsFilterModel newModel = new StreamsFilterModel()
				.name(nameCombo.getText().trim())
				.owner(ownerCombo.getText().trim())
				.type(typeCombo.getText().trim())
				.depot(depotCombo.getText().trim());
		if (IStreamSummary.Type.TASK.name().equalsIgnoreCase(
				typeCombo.getText()))
			newModel.showUnloadedOnly(showUnloadedOnlyBtn.getSelection());
		else
			newModel.showUnloadedOnly(false);

		if (hasParent) {
			SelectionModel change = (SelectionModel) parentCombo.getModel()
					.getValue();
			if (change != null) {
				if (change.getSelection() instanceof IStreamSummary) {
					newModel.parent(((IStreamSummary) change.getSelection())
							.getStream());
				} else {
					newModel.parent(change.getText());
				}
			}
		}

		return newModel;
	}

	public void enableFilters(boolean enabled) {
        nameCombo.setEnabled(enabled);
        ownerCombo.setEnabled(enabled);
        typeCombo.setEnabled(enabled);
        depotCombo.setEnabled(enabled);
        if(hasParent)
            parentCombo.setEnabled(enabled);
        showUnloadedOnlyBtn.setEnabled(enabled);
    }

    private Combo createComboFromHistory(Composite parent, String prefId){
        String[] items = PerforceProjectView.getItems(prefId);
        Combo combo = new Combo(parent, SWT.DROP_DOWN);
        for (String item : items) {
            combo.add(item);
        }
        combo.setData(PREF_KEY,prefId);
        return combo;
    }
    
    private void saveComboHistory(Combo combo) {
        Object key=combo.getData(PREF_KEY);
        Assert.isTrue(key instanceof String);
        Assert.isTrue(!StringUtils.isEmpty((String) key));

        String selection = combo.getText().trim();
        if(!StringUtils.isEmpty(selection)){
            List<String> items = new ArrayList<String>();
            items.add(selection);
            for(String item: combo.getItems()){
                if(!items.contains(item)){
                    items.add(item);
                }
                if(items.size()==HISTORY_SIZE)
                    break;
            }
            combo.removeAll();
            for(String item: items){
                combo.add(item);
            }
            combo.select(0);
            SessionManager.saveHistory(items, (String) key);
        }
    }
    
	private void createClearButton(Composite parent, final Control target, final SelectionListener comboAdapter) {
        Image clearImage = PerforceUIPlugin.getPlugin()
                .getImageDescriptor(IPerforceUIConstants.IMG_CLEAR)
                .createImage();

        ToolBar folderFileBar = new ToolBar(parent, SWT.FLAT);
        ToolItem folderFileClearItem = new ToolItem(folderFileBar, SWT.PUSH);
        folderFileClearItem.setImage(clearImage);
        folderFileClearItem
                .setToolTipText(Messages.StreamsFilterWidget_ClearFilter);
        folderFileClearItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
            	if(target instanceof Text){
            		((Text)target).setText(IConstants.EMPTY_STRING);
            	}else if(target instanceof Combo){
            		((Combo)target).setText(IConstants.EMPTY_STRING);
            	}else if(target instanceof SuggestBox){
            	    ((SuggestBox)target).setTextQuietlyAndUpdateModel(IConstants.EMPTY_STRING);
            	}
            	comboAdapter.widgetSelected(null);
            }

        });		
	}

	@Override
	public boolean setFocus() {
	    if(nameCombo!=null)
	        return nameCombo.setFocus();
	    else
	        return super.setFocus();
	}

    public void reset() {
        for(Control child: getChildren()){
            if(child instanceof Combo){
                ((Combo) child).clearSelection();
                ((Combo) child).setText(IConstants.EMPTY_STRING);
            }else if(child instanceof Text){
                ((Text) child).setText(IConstants.EMPTY_STRING);
            }
        }
    }

    public void setConnection(final IP4Connection conn) {
    	provider.setConnection(conn);
    	
        P4Runner.schedule(new P4Runnable() {

            @Override
            public String getTitle() {
                return Messages.StreamsFilterWidget_LoadingDepots;
            }

            @Override
            public void run(IProgressMonitor monitor) {
                try {
                    final List<IDepot> depots = conn.getDepots();
                    GetStreamsOptions opts = new GetStreamsOptions();
                    opts.setFields("Name,Stream"); //$NON-NLS-1$

                    UIJob job = new UIJob(Messages.StreamsFilterWidget_LoadingDepots) {

                        @Override
                        public IStatus runInUIThread(IProgressMonitor monitor) {
                            if(!depotCombo.isDisposed()){
                            	String depotName = depotCombo.getText();
                                depotCombo.clearSelection();
                                depotCombo.removeAll();
                                for(IDepot depot: depots){
                                    if(IDepot.DepotType.STREAM.equals(depot.getDepotType())){
                                        depotCombo.add(depot.getName());
                                    }
                                   
                                }
                                
                               int selection = depotCombo.indexOf(depotName);                                
                               if (selection >= 0)                              
                               {                         	
                                	depotCombo.select(selection);
                               }
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

//	private void initBinding(){
//		DataBindingContext dbc = new DataBindingContext();
//		widgetValue = WidgetProperties.selection().observe(genderCombo);
//		modelValue = BeansObservables.observeValue(person, "gender");
//		dbc.bindValue(widgetValue, modelValue);
//		IObservableValue widgetValue = WidgetProperties.text(SWT.Modify)
//				.observe(firstName);
//		IObservableValue modelValue = BeanProperties.value(Person.class,
//				"firstName").observe(person);
//		ctx.bindValue(widgetValue, modelValue);
//	}
}
