package com.perforce.team.ui.p4java.dialogs;

import java.util.List;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.perforce.p4java.core.IStream;
import com.perforce.p4java.core.IStreamSummary;
import com.perforce.p4java.core.IStreamSummary.Type;
import com.perforce.p4java.option.Options;
import com.perforce.p4java.option.client.CopyFilesOptions;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Stream;
import com.perforce.team.core.p4java.P4FileIntegration;
import com.perforce.team.ui.streams.StreamUtil;


public class CopyToStreamDialog extends IntegrateToStreamDialog {

    public CopyToStreamDialog(Shell parent, IP4Resource connection) {
        super(parent, connection, Messages.CopyToStreamDialog_Title);
    }

    protected IRevisionRangeWidget createRevisionRangeWidget(Composite parent) {
        return new RevisionUptoWidget2(parent,SWT.NONE,Messages.CopyToStreamDialog_LimitRevisionRange, getConnection());
    }

    @Override
    protected List<IStreamSummary> getPreferredStreams(IP4Resource resource) {
        IStream stream=StreamUtil.getStream(resource.getConnection());
        return StreamUtil.getPreferredCopySources(stream,resource.getConnection());
    }

    @Override
    protected SourceTargetStreamWidget createSourceTargetWidget(Composite parent, final IP4Resource resource){
        return new SourceTargetStreamWidget(parent,SWT.NONE,Messages.CopyToStreamDialog_SourceTargetTitle,resource){

            public Runnable getNonUIJob() {
                return new Runnable() {
                    
                    public void run() {
                    	if(resource instanceof IP4Stream)
                    		targetStream=(IStream) resource.getAdapter(IStream.class);
                    	else
                    		targetStream = StreamUtil.getStream(resource.getConnection());
                        preferredStreams.addAll(StreamUtil
                                .getPreferredCopySources(targetStream,
                                        resource.getConnection()));
                    }
                };
            }
        };
    }
    
    @Override
    protected CopyFilesOptions updatePreviewOptions() {
        CopyFilesOptions option = updateOptions();
        option.setNoUpdate(true);
        return option;
    }

    @Override
    protected Options updateNonPreviewOptions() {
        CopyFilesOptions option = updateOptions();
        option.setNoUpdate(false);
        return option;
    }
    
    private CopyFilesOptions updateOptions() {
        CopyFilesOptions option=new CopyFilesOptions();
        option.setBidirectional(false);
        option.setChangelistId(getChangelist());
        option.setForceStreamCopy(false);
        IStreamSummary source = pathWidget.getSourcePath();
        IStreamSummary target = pathWidget.getTargetPath();
        
        // Code try to compensate the virtual stream
        IStreamSummary effectiveTarget=null;
        if(target.getType()==Type.VIRTUAL){
            effectiveTarget=StreamUtil.getBaseParent(target, getConnection());
        }else{
            effectiveTarget=getConnection().getStreamSummary(target.getStream());
        }
        
        IStreamSummary effectiveSource=null;
        if(source.getType()==Type.VIRTUAL){
            effectiveSource=StreamUtil.getBaseParent(source, getConnection());
        }else{
            effectiveSource=getConnection().getStreamSummary(source.getStream());
        }
        
        Assert.isNotNull(effectiveSource);
        Assert.isNotNull(effectiveTarget);
        
        if(effectiveTarget.getStream().equals(effectiveSource.getParent())
                || effectiveTarget.getStream().equals(StreamUtil.getBaseParent(effectiveSource))){
            option.setStream(source.getStream());
            option.setReverseMapping(false);
        }else if(effectiveSource.getStream().equals(effectiveTarget.getParent())
                || effectiveSource.getStream().equals(StreamUtil.getBaseParent(effectiveTarget))){
            option.setStream(target.getStream());
            option.setReverseMapping(true);
        }else{
            if((effectiveTarget.getType()==Type.RELEASE && effectiveSource.getType()!=Type.DEVELOPMENT)
               ||effectiveTarget.getType()==Type.DEVELOPMENT && effectiveSource.getType()==Type.MAINLINE){
                option.setStream(target.getStream());
                option.setParentStream(source.getStream());
                option.setReverseMapping(true);
            }else{
                option.setStream(source.getStream());
                option.setParentStream(target.getStream());
                option.setReverseMapping(false);
            }
        }
        return option;
    }

    @Override
    protected IP4Resource[] doIntegrate(IP4Connection connection,
            P4FileIntegration integration, String description, Options options) {
        if(options instanceof CopyFilesOptions)
            return connection.copyStream(integration, description, (CopyFilesOptions) options);
        return null;
    }

    @Override
    protected String getOkButtonText() {
        return Messages.CopyToStreamDialog_CopyButtonText;
    }

    @Override
    protected IStatus validateTaskStreamOp() {
    	IStreamSummary source = pathWidget.getSourcePath();
    	IStreamSummary target = pathWidget.getTargetPath();
    	if(target.getType()==Type.TASK){
    		if(!target.getStream().equals(source.getParent())){
    			String msg = Messages.CopyToStreamDialog_TaskStreamCannotCopyToNonParent;
    			return ValidationStatus.error(msg);
    		}
    	}
    	return ValidationStatus.ok();
    	
    }
    

}
