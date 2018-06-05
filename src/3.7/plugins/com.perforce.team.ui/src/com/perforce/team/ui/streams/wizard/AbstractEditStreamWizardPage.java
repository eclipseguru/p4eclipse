package com.perforce.team.ui.streams.wizard;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import com.perforce.p4java.core.IStream;
import com.perforce.p4java.core.IStreamIgnoredMapping;
import com.perforce.p4java.core.IStreamRemappedMapping;
import com.perforce.p4java.core.IStreamSummary.IOptions;
import com.perforce.p4java.core.IStreamViewMapping;
import com.perforce.p4java.core.IStreamViewMapping.PathType;
import com.perforce.p4java.core.ViewMap;
import com.perforce.p4java.impl.generic.core.Stream.StreamViewMapping;
import com.perforce.p4java.impl.generic.core.StreamSummary;
import com.perforce.team.core.p4java.IP4Connection;

/**
 * Base class as EditStreamWizard page.
 * 
 * @author ali
 *
 */
public abstract class AbstractEditStreamWizardPage extends WizardPage implements Listener{

    private IStatus status=Status.OK_STATUS;
    
    protected AbstractEditStreamWizardPage(String pageName) {
        super(pageName);
    }

    protected void createLine(Composite parent, int ncol) 
    {
        Label line = new Label(parent, SWT.SEPARATOR|SWT.HORIZONTAL|SWT.BOLD);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(ncol,1).grab(true, false).applyTo(line);
    }
    
    public void handleEvent(Event event) {
        // TODO Auto-generated method stub
        
    }

    public void saveSettings(){
    }
    protected void restoreFromSettings() {
    }
    
    protected void addListeners()
    {
    }
    
    final public IStatus validate(){
        return this.status=doValidate();
    }

    protected IStatus doValidate() {
        return Status.OK_STATUS;
    }

    public EditStreamWizard getWizard(){
        IWizard container = super.getWizard();
        Assert.isTrue(container instanceof EditStreamWizard);
        return (EditStreamWizard)container;
    }
    
    public IDialogSettings getDialogSettings(){
        return getWizard().getDialogSettings();
    }

    public IP4Connection getConnection(){
        return getWizard().getConnection();
    }

    protected IStream getStream(){
        return getWizard().getStream();
    }
    
    protected IStream getOriginalStream(){
        return getWizard().getOriginalStream();
    }
    
    protected IOptions getOptions(IStream stream){
        IOptions opt = stream.getOptions();
        if(opt==null)
            stream.setOptions(new StreamSummary.Options());
        return stream.getOptions();
    }
    
    protected ViewMap<IStreamViewMapping> getStreamView(IStream stream){
        ViewMap<IStreamViewMapping> view = stream.getStreamView();
        if(view==null){
            ViewMap<IStreamViewMapping> v = new ViewMap<IStreamViewMapping>();
            v.addEntry(new StreamViewMapping(0, PathType.SHARE, "...", null)); //$NON-NLS-1$
            stream.setStreamView(v);
        }
        return stream.getStreamView();
    }

    protected ViewMap<IStreamRemappedMapping> getRemappedView(IStream stream){
        ViewMap<IStreamRemappedMapping> view = stream.getRemappedView();
        if(view==null){
            ViewMap<IStreamRemappedMapping> v = new ViewMap<IStreamRemappedMapping>();
            stream.setRemappedView(v);
        }
        return stream.getRemappedView();
    }

    protected ViewMap<IStreamIgnoredMapping> getIgnoredView(IStream stream){
        ViewMap<IStreamIgnoredMapping> view = stream.getIgnoredView();
        if(view==null){
            ViewMap<IStreamIgnoredMapping> v = new ViewMap<IStreamIgnoredMapping>();
            stream.setIgnoredView(v);
        }
        return stream.getIgnoredView();
    }

    
    /**
     * Applies the status to the status line of a dialog page.
     */
    protected void applyToStatusLine(IStatus status) {
        this.status=status;
        String message= status.getMessage();
        if (message.length() == 0) message= null;
        switch (status.getSeverity()) {
            case IStatus.OK:
                setErrorMessage(null);
                setMessage(null);
                break;
            case IStatus.WARNING:
                setErrorMessage(null);
                setMessage(message, WizardPage.WARNING);
                break;              
            case IStatus.INFO:
                setErrorMessage(null);
                setMessage(message, WizardPage.INFORMATION);
                break;          
            default:
                setErrorMessage(message);
                setMessage(null);
                break;      
        }
    }
    
    @Override
    public boolean canFlipToNextPage() {
        return status.isOK() && super.canFlipToNextPage();
    }
    
    @Override
    public boolean isPageComplete() {
        return super.isPageComplete() && status.isOK();
    }
}