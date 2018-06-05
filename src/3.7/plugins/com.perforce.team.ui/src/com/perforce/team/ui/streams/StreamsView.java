package com.perforce.team.ui.streams;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.progress.UIJob;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.Policy;
import com.perforce.team.core.Tracing;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Listener;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.views.IPerforceView;
import com.perforce.team.ui.views.Messages;
import com.perforce.team.ui.views.PerforceProjectView;

/**
 * A view showing streams as flat list or tree.
 * 
 * @author chengdong
 *
 */
public class StreamsView extends PerforceProjectView{

    /**
     * The ID for this view
     */
    public static final String VIEW_ID = "com.perforce.team.ui.StreamsView"; //$NON-NLS-1$

    public static StreamsView findView() {
        return (StreamsView) PerforceUIPlugin.getActivePage().findView(
                    VIEW_ID);
    }

    public static StreamsView showView() {
        try {
            return (StreamsView) PerforceUIPlugin.getActivePage().showView(
                    VIEW_ID);
        } catch (PartInitException e) {
            PerforceProviderPlugin.logError(e);
        }
        return null;
    }

    private IP4Listener p4Listener = new IP4Listener() {

        public void resoureChanged(final P4Event event) {
            UIJob job = new UIJob(Messages.DepotView_RefreshingDepotViewTree) {

                @Override
                public IStatus runInUIThread(IProgressMonitor monitor) {
                	Tracing.printExecTime(Policy.DEBUG, StreamsView.this.getClass().getSimpleName()+":resourceChanged()", event.toString(), new Runnable() {

						public void run() {
							
							if (okToUse()) {
								switch (event.getType()) {

								case REFRESHED:
									for(IP4Resource r:event.getResources()){
										if(r instanceof IP4Connection && r==getPerforceViewControl().getConnection()){
											getPerforceViewControl().refresh();
										}
									}
									break;
								default:
									break;
								}
							}
						}
					});
                    return Status.OK_STATUS;
                }

            };
            job.setSystem(true);
            job.schedule();
        }
        
		public String getName() {
			return StreamsView.this.getClass().getSimpleName();
		}
    };

    @Override
    public void createPartControl(Composite parent) {
    	// TODO Auto-generated method stub
    	super.createPartControl(parent);
    	P4ConnectionManager.getManager().addListener(p4Listener);
    }

    @Override
    public void dispose() {
        super.dispose();
        P4ConnectionManager.getManager().removeListener(p4Listener);
    }
    
    @Override
    protected StreamsViewControl createViewControl(IPerforceView view) {
        return new StreamsViewControl(view);
    }
    
    @Override
    public StreamsViewControl getPerforceViewControl() {
        return (StreamsViewControl) super.getPerforceViewControl();
    }
    
    public void refresh(boolean clearCache, boolean retrieve){
        getPerforceViewControl().refresh(clearCache,retrieve);
    }

    public void expandAll(){
        getPerforceViewControl().expandAll();
    }
    
    public void collapseAll(){
        getPerforceViewControl().collapseAll();
    }

    public boolean okToUse() {
        return getPerforceViewControl()!= null && getPerforceViewControl().getTreeViewer()!=null && !getPerforceViewControl().getTreeViewer().getTree().isDisposed();
    }

}
