package com.perforce.team.ui.streams;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.progress.UIJob;

import com.perforce.p4java.core.IStream;
import com.perforce.p4java.core.IStreamIgnoredMapping;
import com.perforce.p4java.core.IStreamRemappedMapping;
import com.perforce.p4java.core.IStreamSummary;
import com.perforce.p4java.core.IStreamSummary.IOptions;
import com.perforce.p4java.core.IStreamViewMapping;
import com.perforce.p4java.core.ViewMap;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.team.core.IConstants;
import com.perforce.team.core.p4java.IP4Stream;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.SWTUtils;
import com.perforce.team.ui.dialogs.FixedWidthScrolledComposite;

/**
 * Stream Detail panel.
 * 
 * @author ali
 *
 */
public class StreamDetailPanel extends FixedWidthScrolledComposite {

	private IP4Stream stream;
	
    private Text nameText;
    private Text rootText;
    private Text modText;
    private Text parentText;
    private Text lastText;
    private Text typeText;
    private Text ownerText;
    private Text descText;
    private Text pathsText;
    private Text remappedText;
    private Text ignoredText;
    private Text viewText;
    private Button fromParentChk;
    private Button toParentChk;
    private Button lockedChk;
    private Button submitFileChk;

    public StreamDetailPanel(Composite parent, int style) {
		super(parent, style);

		setLayout(new FillLayout());
        createControl();
        setExpandHorizontal(true);
        setExpandVertical(true);
	}

	private Composite createControl() {
        final Composite parent = new Composite(this, SWT.NONE);
        this.setContent(parent);

        final int TXT_SINGLE_STYLE= SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY;
        final int TXT_MULTI_STYLE= SWT.MULTI | SWT.BORDER  |SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY;
        
        GridLayoutFactory.swtDefaults().numColumns(4).margins(10, 10).applyTo(parent);
        
        SWTUtils.createLabel(parent, Messages.StreamDetailPanel_Stream);
        nameText=SWTUtils.createText(parent, 1, TXT_SINGLE_STYLE);
        
        SWTUtils.createLabel(parent, Messages.StreamDetailPanel_StreamRoot);
        rootText=SWTUtils.createText(parent, 1, TXT_SINGLE_STYLE);
        
        SWTUtils.createLabel(parent, Messages.StreamDetailPanel_DateModified);
        modText=SWTUtils.createText(parent, 1, TXT_SINGLE_STYLE);
        
        SWTUtils.createLabel(parent, Messages.StreamDetailPanel_Parent);
        parentText=SWTUtils.createText(parent, 1, TXT_SINGLE_STYLE);
        
        SWTUtils.createLabel(parent, Messages.StreamDetailPanel_LastAccessed);
        lastText=SWTUtils.createText(parent, 1, TXT_SINGLE_STYLE);
        
        SWTUtils.createLabel(parent, Messages.StreamDetailPanel_Type);
        typeText = SWTUtils.createText(parent, 1, TXT_SINGLE_STYLE);
        
        SWTUtils.createLabel(parent, Messages.StreamDetailPanel_Owner);
        ownerText = SWTUtils.createText(parent, 1, TXT_SINGLE_STYLE);
        
        SWTUtils.createLabel(parent, IConstants.EMPTY_STRING, 2); // dumb label
        
        SWTUtils.createLabel(parent, Messages.StreamDetailPanel_Description);
        descText = SWTUtils.createTextArea(parent, 3, 1, 4, TXT_MULTI_STYLE);
        
        SWTUtils.createLabel(parent, Messages.StreamDetailPanel_Options);
        createOptions(parent);
        
        SWTUtils.createLabel(parent, Messages.StreamDetailPanel_Paths);
        pathsText = SWTUtils.createTextArea(parent,3, 1, 4, TXT_MULTI_STYLE);
        
        SWTUtils.createLabel(parent, Messages.StreamDetailPanel_Remapped);
        remappedText = SWTUtils.createTextArea(parent,3, 1, 4, TXT_MULTI_STYLE);

        SWTUtils.createLabel(parent, Messages.StreamDetailPanel_Ignored);
        ignoredText = SWTUtils.createTextArea(parent,3, 1, 4, TXT_MULTI_STYLE);

        SWTUtils.createLabel(parent, Messages.StreamDetailPanel_ClientView);
        viewText = SWTUtils.createTextArea(parent,3, 1, 4, TXT_MULTI_STYLE);

        setMinSize(parent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        return parent;
	}

    private void createOptions(Composite composite) {
        Composite parent = new Composite(composite, SWT.NONE);
        GridDataFactory.fillDefaults().span(3,1).applyTo(parent);
        
        GridLayoutFactory.swtDefaults().numColumns(1).applyTo(parent);
        
        submitFileChk=SWTUtils.createCheckBox(parent, Messages.StreamDetailPanel_OptRestictOwner);
        lockedChk=SWTUtils.createCheckBox(parent, Messages.StreamDetailPanel_OptLocked);
        toParentChk=SWTUtils.createCheckBox(parent, Messages.StreamDetailPanel_OptToParent);
        fromParentChk=SWTUtils.createCheckBox(parent, Messages.StreamDetailPanel_OptFromParent);
        
        parent.setEnabled(false);
    }

    public void setInput(final IP4Stream stream) {
        if(this.stream!=stream){
            this.stream=stream;
            P4Runner.schedule(new P4Runnable() {

                @Override
                public String getTitle() {
                    return MessageFormat.format(Messages.StreamsTreeViewer_LoadingStream,stream==null?IConstants.EMPTY_STRING:stream.getName());
                }

                @Override
                public void run(IProgressMonitor monitor) {
                    try {
                        
                        if(stream!=null && stream.needsRefresh()){
                            stream.fetchDetail();
                        }
                        UIJob job = new UIJob(IConstants.EMPTY_STRING) {

                            @Override
                            public IStatus runInUIThread(IProgressMonitor monitor) {
                                if(!StreamDetailPanel.this.isDisposed())
                                    updateControls(stream);
                                return Status.OK_STATUS;
                            }
                        };
                        job.schedule();
                    } catch (P4JavaException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void updateControls(IP4Stream stream) {
        if(stream==null){
            reset();
            return;
        }
        IStreamSummary sum = stream.getStreamSummary();
        if(sum instanceof IStream){
            IStream s=(IStream) sum;
            nameText.setText(s.getName());
            rootText.setText(s.getStream());
            modText.setText(P4UIUtils.formatLabelDate(s.getUpdated()));
            parentText.setText(s.getParent()==null?"":s.getParent());
            lastText.setText(P4UIUtils.formatLabelDate(s.getAccessed()));
            typeText.setText(s.getType().name().toLowerCase());
            ownerText.setText(s.getOwnerName());
            descText.setText(s.getDescription()==null?IConstants.EMPTY_STRING:s.getDescription());

            IOptions opt = s.getOptions();
            updateOptions(opt);
            
            ViewMap<IStreamRemappedMapping> remap = s.getRemappedView();
            remappedText.setText(StreamUtil.getRemmapedPaths(remap));
            
            ViewMap<IStreamIgnoredMapping> ignoreView = s.getIgnoredView();
            ignoredText.setText(StreamUtil.getIgnoredPatterns(ignoreView));
            
            ViewMap<IStreamViewMapping> viewMap = s.getStreamView();
            pathsText.setText(StreamUtil.getStreamViewPaths(viewMap));
            
            viewText.setText(StreamUtil.getStreamClientView(stream));

        }else{
            reset();
        }
        
    }

    private void updateOptions(IOptions opt) {
        fromParentChk.setSelection(!opt.isNoFromParent());
        toParentChk.setSelection(!opt.isNoToParent());
        lockedChk.setSelection(opt.isLocked());
        submitFileChk.setSelection(opt.isOwnerSubmit());        
    }

    private void reset() {
        nameText.setText(IConstants.EMPTY_STRING);
        rootText.setText(IConstants.EMPTY_STRING);
        modText.setText(IConstants.EMPTY_STRING);
        parentText.setText(IConstants.EMPTY_STRING);
        lastText.setText(IConstants.EMPTY_STRING);
        typeText.setText(IConstants.EMPTY_STRING);
        ownerText.setText(IConstants.EMPTY_STRING);
        descText.setText(IConstants.EMPTY_STRING);
        
        remappedText.setText(IConstants.EMPTY_STRING);
        ignoredText.setText(IConstants.EMPTY_STRING);
        pathsText.setText(IConstants.EMPTY_STRING);
        viewText.setText(IConstants.EMPTY_STRING);

        fromParentChk.setSelection(false);
        toParentChk.setSelection(false);
        lockedChk.setSelection(false);
        submitFileChk.setSelection(false);        

    }

}
