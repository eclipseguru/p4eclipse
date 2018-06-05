package com.perforce.team.ui.p4java.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;

import com.perforce.team.core.P4ClientUtil;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;

public class ResolveWizard extends Wizard {

    private ResolveWizardMethodPage methodPage = new ResolveWizardMethodPage(
            "methodPage"); //$NON-NLS-1$
    private ResolveWizardAutoPage autoResolvePage = new ResolveWizardAutoPage(
            "autoResolvePage"); //$NON-NLS-1$
    private ResolveWizardInteractivePage interactiveResolvePage = new ResolveWizardInteractivePage(
            "interactiveResolvePage"); //$NON-NLS-1$

    static class ResolveItem {

        public IP4File file;
        public int index;

        String getResolveType() {
            return file.getIntegrationSpecs()[index].getResolveType();
        }

        String getFromFile() {
            return file.getIntegrationSpecs()[index].getFromFile();
        }

        int getStartFromRev() {
            return file.getIntegrationSpecs()[index].getStartFromRev();
        }

        int getEndFromRev() {
            return file.getIntegrationSpecs()[index].getEndFromRev();
        }

        boolean isAttributes() {
            return "attributes".equals(getResolveType()); //$NON-NLS-1$
        }

        boolean isBranch() {
            return "branch".equals(getResolveType()); //$NON-NLS-1$
        }

        boolean isContent() {
        	// see job055514. for legacy server like 2009.2, there is no resolve type.
            return getResolveType()==null|| "content".equals(getResolveType()); //$NON-NLS-1$
        }

        boolean isDelete() {
            return "delete".equals(getResolveType()); //$NON-NLS-1$
        }

        boolean isMove() {
            return "move".equals(getResolveType()); //$NON-NLS-1$
        }

        boolean isType() {
            return "type".equals(getResolveType()); //$NON-NLS-1$
        }
        
        boolean isResolveShelvedChange() {
        	return P4ClientUtil.isResolveShelvedChange(file.getIntegrationSpecs()[index]);
        }
        
        String computeTheirRev(){
        	return P4ClientUtil.computeTheirRev(file.getIntegrationSpecs()[index]);
        }
    }

    IP4Resource[] unresolvedResources;
    ResolveItem[] unresolved;
    ResolveItem[] unresolvedContent;

    public ResolveWizard(IP4Resource[] unresolved) {
        setNeedsProgressMonitor(false);
        unresolvedResources = unresolved;
        updateUnresolvedItems();
    }

    public void updateUnresolvedItems() {
        List<ResolveItem> items = new ArrayList<ResolveItem>();
        List<ResolveItem> contentItems = new ArrayList<ResolveItem>();
        if (unresolvedResources != null) {
            for (IP4Resource resource : unresolvedResources) {
                if (resource instanceof IP4File) {
                    IP4File file = (IP4File) resource;
                    if (file.getIntegrationSpecs() != null)
                        for (int i = 0; i < file.getIntegrationSpecs().length; i++) {
                            ResolveItem item = new ResolveItem();
                            item.file = file;
                            item.index = i;
                            items.add(item);
                            if (item.isContent()) {
                                contentItems.add(item);
                            }
                        }
                }
            }
        }
        this.unresolved = items.toArray(new ResolveItem[0]);
        this.unresolvedContent = contentItems.toArray(new ResolveItem[0]);
    }

    public void removeResolvedItem(ResolveItem item) {
        List<ResolveItem> items = new ArrayList<ResolveItem>(
                Arrays.asList(unresolved));
        items.remove(item);
        unresolved = items.toArray(new ResolveItem[0]);
        List<ResolveItem> contentItems = new ArrayList<ResolveItem>(
                Arrays.asList(unresolvedContent));
        contentItems.remove(item);
        unresolvedContent = contentItems.toArray(new ResolveItem[0]);
    }

    @Override
    public boolean performFinish() {
    	PerforceUIPlugin.getPlugin().getPreferenceStore()
    		.setValue(IPerforceUIConstants.PREF_RESOLVE_MIGRATED,true);
	
    	String mode =IPerforceUIConstants.RESOLVE_PROMPT;
    	
    	if(methodPage.rememberDefaultSelected()){
	    	if(methodPage.autoSelected())
	    		mode=IPerforceUIConstants.RESOLVE_AUTO;
	    	else if(methodPage.interactiveSelected())
	    		mode=IPerforceUIConstants.RESOLVE_INTERACTIVE;

	    	PerforceUIPlugin.getPlugin().getPreferenceStore()
	    	.setValue(IPerforceUIConstants.PREF_RESOLVE_DEFAULT_MODE,mode);
    	}
    	
        return true;
    }

    @Override
    public void createPageControls(Composite pageContainer) {
        super.createPageControls(pageContainer);
        setWindowTitle(Messages.ResolveWizard_Resolve);
    }

    @Override
    public void addPages() {
        addPage(methodPage);
        addPage(autoResolvePage);
        addPage(interactiveResolvePage);
    }

    @Override
    public IWizardPage getStartingPage() {
        String mode = PerforceUIPlugin.getPlugin().getPreferenceStore()
                .getString(IPerforceUIConstants.PREF_RESOLVE_DEFAULT_MODE);
        Boolean migrated = PerforceUIPlugin.getPlugin().getPreferenceStore()
                .getBoolean(IPerforceUIConstants.PREF_RESOLVE_MIGRATED);
        
        if(!migrated) // in case user has workspace data from previous release
        	return methodPage;;

        if (IPerforceUIConstants.RESOLVE_AUTO.equals(mode)) 
            return autoResolvePage;
        else if (IPerforceUIConstants.RESOLVE_INTERACTIVE.equals(mode))
            return interactiveResolvePage;
        else if (IPerforceUIConstants.RESOLVE_PROMPT.equals(mode))
            return methodPage;
        else
        	return methodPage;
    }

    @Override
    public IWizardPage getNextPage(IWizardPage currentPage) {
        if (currentPage == methodPage) {
            if (methodPage.autoSelected())
                return autoResolvePage;
            else
                return interactiveResolvePage;
        } else
            return null;
    }

    @Override
    public IWizardPage getPreviousPage(IWizardPage currentPage) {
        if (currentPage == methodPage)
            return null;
        else
            return methodPage;
    }
    
    @Override
    public boolean canFinish() {
    	if(getContainer().getCurrentPage()==methodPage)
    		return false;
    	return super.canFinish();
    }
}
