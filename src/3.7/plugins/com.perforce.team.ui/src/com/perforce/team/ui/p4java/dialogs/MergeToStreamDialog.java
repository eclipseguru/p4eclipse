package com.perforce.team.ui.p4java.dialogs;

import java.util.List;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.model.WorkbenchAdapter;

import com.perforce.p4java.core.IStream;
import com.perforce.p4java.core.IStreamSummary;
import com.perforce.p4java.core.IStreamSummary.Type;
import com.perforce.p4java.option.Options;
import com.perforce.p4java.option.client.MergeFilesOptions;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Container;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Stream;
import com.perforce.team.core.p4java.P4FileIntegration;
import com.perforce.team.ui.streams.StreamUtil;


public class MergeToStreamDialog extends IntegrateToStreamDialog {

    public MergeToStreamDialog(Shell parent, IP4Resource resource) {
        super(parent, resource, Messages.MergeToStreamDialog_MergeToStream);
    }

    protected IRevisionRangeWidget createRevisionRangeWidget(Composite parent) {
        return new RevisionRangeWidget2(parent,SWT.NONE,Messages.MergeToStreamDialog_LimitRevisionRange, getConnection());
    }

    protected Point getPreferredSize() {
        return new Point(700,600);
    }

    protected IDepotFileChooser createFolderWidget(Composite parent,
            final IP4Resource resource){
        return new DepotFileChooser(parent, SWT.NONE, resource, new WorkbenchAdapter() {
            public Object[] getChildren(Object o) { // filter out the unrelated resources.
                String stream = resource.getClient().getStream();
                IP4Resource[] resources=resource.getConnection().members();
                
                IP4Resource streamRoot=resources[0];
                
                IP4Container depot=null;
                for(int i=0;i<resources.length;i++){
                    if(stream.startsWith(resources[i].getRemotePath()) && resources[i] instanceof IP4Container){
                        depot=(IP4Container) resources[i];
                        break;
                    }
                }
                if(depot!=null){
	                depot.refresh(1);
	                for(IP4Resource m: depot.members()){
	                    if(m.getRemotePath().startsWith(stream)){
	                        streamRoot=m;
	                        break;
	                    }
	                }
                }
                return new Object[]{streamRoot};
            }
        });
    }
    
    @Override
    protected List<IStreamSummary> getPreferredStreams(IP4Resource resource) {
        IStream stream=StreamUtil.getStream(resource.getConnection());
        return StreamUtil.getPreferredMergeSources(stream,resource.getConnection());
    }

    @Override
    protected SourceTargetStreamWidget createSourceTargetWidget(Composite parent, final IP4Resource resource){
        return new SourceTargetStreamWidget(parent,SWT.NONE,Messages.MergeToStreamDialog_InformationText,resource){

            public Runnable getNonUIJob() {
                return new Runnable() {
                    
                    public void run() {
                    	if(resource instanceof IP4Stream)
                    		targetStream=(IStream) resource.getAdapter(IStream.class);
                    	else
                    		targetStream = StreamUtil.getStream(resource.getConnection());
                        preferredStreams.addAll(StreamUtil
                                .getPreferredMergeSources(targetStream,
                                        resource.getConnection()));
                    }
                };
            }
        };
    }

    @Override
    protected Options updatePreviewOptions() {
        MergeFilesOptions option = updateOptions();
        option.setShowActionsOnly(true);
        return option;
    }

    @Override
    protected IP4Resource[] doIntegrate(IP4Connection connection,
            P4FileIntegration integration, String description, Options options) {
        if(options instanceof MergeFilesOptions)
            return connection.mergeStream(integration, description, (MergeFilesOptions) options);
        return null;
    }

    @Override
    protected Options updateNonPreviewOptions() {
        MergeFilesOptions option = updateOptions();
        option.setShowActionsOnly(false);
        return option;
    }

    private MergeFilesOptions updateOptions() {
        MergeFilesOptions option=new MergeFilesOptions();
        option.setBidirectionalInteg(false);
        option.setChangelistId(getChangelist());
        option.setForceStreamMerge(false);
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
    protected String getOkButtonText() {
        return Messages.MergeToStreamDialog_Merge;
    }
    
    @Override
    protected IStatus validateTaskStreamOp() {
    	IStreamSummary source = pathWidget.getSourcePath();
    	IStreamSummary target = pathWidget.getTargetPath();
    	if(target.getType()==Type.TASK){
	    	if(!source.getStream().equals(target.getParent())){
	    		String msg = Messages.MergeToStreamDialog_TaskStreamCannotMergeFromNonParent;
	    		return ValidationStatus.error(msg);
	    	}
    	}
    	return ValidationStatus.ok();
    	
    }

}
