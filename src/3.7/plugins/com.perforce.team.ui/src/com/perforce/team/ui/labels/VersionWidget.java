package com.perforce.team.ui.labels;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;

import com.perforce.team.ui.BaseErrorProvider;
import com.perforce.team.ui.p4java.dialogs.VersionComboViewer;
import com.perforce.team.ui.p4java.dialogs.VersionComboViewer.VersionType;

/**
 * A general version selection widget.<p/>
 * <pre>
 * <code>
 *    versionWidget.createControl(parent)     
 *       	.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
 *    versionWidget.filterVersion(VersionType.Revision, VersionType.ChangeList, VersionType.Datetime);</code>
 * </pre>
 * @author ali
 * 
 */
public class VersionWidget extends BaseErrorProvider {
	// UI controls
    private Button latestRevisionButton;
    private Button specificRevisionButton;
    private Text revisionText;
    private VersionComboViewer revisionCombo;
    
    // model
    private String version = null;
    
    private static Map<VersionType, String> errorMap=new HashMap<VersionType, String>();
    private static Map<VersionType, String> patternMap=new HashMap<VersionType, String>();
    static{
    	errorMap.put(VersionType.Revision, Messages.VersionWidget_MustEnterRevisionNumber);
    	errorMap.put(VersionType.ChangeList, Messages.VersionWidget_MustEnterChangelistNumber);
    	errorMap.put(VersionType.Datetime, Messages.VersionWidget_MustEnterDate);
    	errorMap.put(VersionType.Label, Messages.VersionWidget_MustEnterLabelName);

    	patternMap.put(VersionType.Revision, "#\\d+");
    	patternMap.put(VersionType.ChangeList, "@\\d+");
    	patternMap.put(VersionType.Datetime, "@((19|20)\\d\\d)/(0?[1-9]|1[012])/(0?[1-9]|[12][0-9]|3[01])(:([01]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9])?"); //2011/23/12:23:00:00 
    	patternMap.put(VersionType.Label, "@.+");
    }

    public Composite createControl(Composite parent) {
        Composite revisionArea = new Composite(parent, SWT.NONE);
        GridLayout raLayout = new GridLayout(3, false);
        raLayout.marginHeight = 0;
        raLayout.marginWidth = 0;
        revisionArea.setLayout(raLayout);

        final ModifyListener textListener = new ModifyListener() {

            public void modifyText(ModifyEvent e) {
            	String text = revisionText.getText().trim();
            	String first="@";
                if (revisionCombo.getSelectedObject() == VersionType.Revision) {
                    first="#";//$NON-NLS-1$
                }
                version = text.startsWith(first)?text:first + text;
                validate();
            }
        };

        final SelectionListener revisionListener = new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                revisionText.setEnabled(specificRevisionButton.getSelection());
                revisionCombo.setEnabled(specificRevisionButton.getSelection());
                if (latestRevisionButton.getSelection()) {
                    version = null;
                } else {
                    textListener.modifyText(new ModifyEvent(new Event()));
                }
                validate();
            }

        };

        latestRevisionButton = new Button(revisionArea, SWT.RADIO);
        latestRevisionButton
                .setText(Messages.LabelFilesWidget_AtLatestRevision);
        latestRevisionButton.setSelection(true);
        GridData lrbData = new GridData(SWT.FILL, SWT.FILL, true, false);
        lrbData.horizontalSpan = 3;
        latestRevisionButton.setLayoutData(lrbData);
        latestRevisionButton.addSelectionListener(revisionListener);

        specificRevisionButton = new Button(revisionArea, SWT.RADIO);
        specificRevisionButton
                .setText(Messages.LabelFilesWidget_SpecifyRevision);
        specificRevisionButton.addSelectionListener(revisionListener);
        revisionCombo = new VersionComboViewer(revisionArea, SWT.READ_ONLY | SWT.DROP_DOWN);
        revisionCombo.select(VersionType.Revision);
        revisionCombo.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				revisionListener.widgetSelected(null);
			}
		});
        revisionText = new Text(revisionArea, SWT.SINGLE | SWT.BORDER);
        revisionText
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        revisionText.addModifyListener(textListener);

        revisionListener.widgetSelected(null);

        return revisionArea;
    }

    public String getVersion(){
    	return this.version;
    }
    
    public void setVersion(String version){
    	this.version=version;
    	VersionType type = getType(version);
    	if(type==null){
    		latestRevisionButton.setSelection(true);
    		specificRevisionButton.setSelection(false);
    	}else{
    		latestRevisionButton.setSelection(false);
    		specificRevisionButton.setSelection(true);
    		revisionCombo.select(type);
    		revisionText.setText(version.substring(1));
    	}
    }
    
    public void filterVersion(VersionType... types){
    	revisionCombo.filter(types);
    	revisionCombo.refresh();
    }
    
    @Override
    public void validate() {
        if (specificRevisionButton.getSelection()) {
        	VersionType type = revisionCombo.getSelectedObject();
        	if(version!=null&&!version.matches(patternMap.get(type))){
        		this.errorMessage = errorMap.get(type);
        	}else{
            	this.errorMessage = null;
            }
        }
        super.validate();
    }
    
    public VersionType getType(String type){
    	if(type!=null){
        	for(VersionType t:VersionType.values()){
        		String pattern = patternMap.get(t);
        		if(type.matches(pattern))
        			return t;
        	}
    	}
    	return null;
    }

}
