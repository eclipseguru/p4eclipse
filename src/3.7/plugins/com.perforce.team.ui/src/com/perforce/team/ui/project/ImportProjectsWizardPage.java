/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.project;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.progress.UIJob;

import com.perforce.p4java.core.IDepot.DepotType;
import com.perforce.team.core.p4java.builder.P4FileSpecBuilder;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.option.server.GetDirectoriesOptions;
import com.perforce.p4java.server.IOptionsServer;
import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.Policy;
import com.perforce.team.core.Tracing;
import com.perforce.team.core.p4java.ErrorHandler;
import com.perforce.team.core.p4java.IErrorHandler;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Container;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Folder;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4BrowsableConnection;
import com.perforce.team.core.p4java.P4Connection;
import com.perforce.team.core.p4java.P4Depot;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceContentProvider;
import com.perforce.team.ui.PerforceLabelProvider;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.SWTUtils;
import com.perforce.team.ui.connection.BaseConnectionWizardPage;
import com.perforce.team.ui.connection.IConnectionWizard;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * @author ali (ali@perforce.com)
 */
public class ImportProjectsWizardPage extends BaseConnectionWizardPage {

    private Composite displayArea;

    private P4DepotContainerCheckedTreeViewer depotViewer;
    
	private ListViewer projectsViewer; // for preview projects to import only

    private Label descLabel;

    private Link totalLink;

    private ConnectionParameters params = new ConnectionParameters();

    private Button importButton;

    private IP4Folder[] folders = new IP4Folder[0];
    private boolean importSelected = false;

    private boolean isOptional = false;
    private boolean errorShown = false;
    
    private boolean selectFromDepotViewer=false; // to prevent inf loop

    private IErrorHandler errorHandler = new ErrorHandler() {

        @Override
        public boolean shouldRetry(IP4Connection connection,
                P4JavaException exception) {
            boolean retry = false;
            if (!errorShown) {
                retry = P4ConnectionManager.getManager().displayException(
                        connection, exception, true, false);
                // Mark error shown if not retrying
                errorShown = !retry;
            }
            return retry;
        }

    };

    /**
     * @param pageName
     * @param optional
     *            - true to show optional checkbox
     */
    public ImportProjectsWizardPage(String pageName, boolean optional) {
        super(pageName);
        setTitle(Messages.ImportProjectsWizardPage_ChooseProjectsTitle);
        setDescription(Messages.ImportProjectsWizardPage_ChooseProjectsDescription);
        setImageDescriptor(PerforceUIPlugin.getPlugin().getImageDescriptor(
                IPerforceUIConstants.IMG_IMPORT_WIZARD));
        this.isOptional = optional;
    }

    /**
     * @see org.eclipse.jface.wizard.WizardPage#canFlipToNextPage()
     */
    @Override
    public boolean canFlipToNextPage() {
        return false;
    }

    private void loadConnection() {
        this.params = getCurrentParams();
        try {
            getContainer().run(true, true, new IRunnableWithProgress() {

                public void run(IProgressMonitor monitor)
                        throws InvocationTargetException, InterruptedException {
                    if (monitor == null) {
                        monitor = new NullProgressMonitor();
                    }
                    monitor.beginTask(
                            MessageFormat
                                    .format(Messages.ImportProjectsWizardPage_CreatingConnection,
                                            params.getPort()), 4);
                    final P4BrowsableConnection connection = new P4BrowsableConnection(
                            params);
                    connection.setErrorHandler(errorHandler);
                    connection.login(getPassword());
                    if (monitor.isCanceled()) {
                        return;
                    }
                    monitor.worked(1);
                    connection.connect();
                    if (monitor.isCanceled()) {
                        return;
                    }
                    monitor.worked(1);
                    connection.setShowClientOnly(connection.clientExists());
                    connection.setShowFoldersWIthOnlyDeletedFiles(false);
                    
                    if (monitor.isCanceled()) {
                        return;
                    }
                    monitor.worked(1);
                    updateAuthTicket(connection);
                    PerforceUIPlugin.syncExec(new Runnable() {

                        public void run() {
                            if (P4UIUtils.okToUse(depotViewer)) {
                                depotViewer
                                        .setInput(new IP4Container[] { connection });
                            }
                        }
                    });
                    monitor.worked(1);
                    monitor.done();
                }
            });
        } catch (InvocationTargetException e) {
            PerforceProviderPlugin.logError(e);
        } catch (InterruptedException e) {
            PerforceProviderPlugin.logError(e);
        }
    }

    /**
     * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            errorShown = false;
            loadConnection();

            ArrayList<IP4Folder> current = new ArrayList<IP4Folder>();
            Object[] checked = depotViewer.getCheckedElements();
            for (Object obj : checked) {
                if (obj instanceof IP4Folder) {
                    current.add((IP4Folder) obj);
                }
            }

            validateStreamDirectories(current.toArray(new IP4Folder[0]));
        }
        super.setVisible(visible);
    }

    /**
     * Is the import option selected
     * 
     * @return - true if import selected or required
     */
    public boolean isImportSelected() {
        return this.importSelected;
    }

    /**
     * Get imported folders
     * 
     * @return - array of p4 folders
     */
    public IP4Folder[] getImportedFolders() {
        return this.folders;
    }

    /**
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        displayArea = new Composite(parent, SWT.NONE);
        displayArea.setLayout(new GridLayout(2, false));
        displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        importSelected = true;
        if (isOptional) {
            importButton = new Button(displayArea, SWT.CHECK);
            importButton.setLayoutData(new GridData(SWT.BEGINNING,SWT.CENTER,false,false,2,1));
            importButton.setSelection(true);
            importButton
                    .setText(Messages.ImportProjectsWizardPage_ImportExistingDepotFolders);
            importButton.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    boolean importing = importButton.getSelection();
                    depotViewer.getTree().setEnabled(importing);
                    if (!importing) {
                        depotViewer.setSubtreeChecked(depotViewer.getTree().getData(), false);
                    }
                    importSelected = importing;
                }

            });
        }

        SashForm sash = new SashForm(displayArea, SWT.HORIZONTAL);
        sash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,2,1));
        Composite left = new Composite(sash, SWT.NONE);
        left.setLayout(new GridLayout());
        Composite right = new Composite(sash, SWT.NONE);
        right.setLayout(new GridLayout(2,false));
        
        SWTUtils.createLabel(left,Messages.ImportProjectsWizardPage_RemoteFolders);
        SWTUtils.createLabel(right,Messages.ImportProjectsWizardPage_EclipseProjectFolders);
        totalLink = new Link(right, SWT.PUSH);
        totalLink.setEnabled(true);
        totalLink.setText("0");//("<a>0</a>"); //$NON-NLS-1$

        depotViewer = new P4DepotContainerCheckedTreeViewer(left, SWT.SINGLE
              | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        depotViewer.getTree().setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, true, true,1,1));
        depotViewer.setAutoExpandLevel(2);
        PerforceContentProvider provider = new PerforceContentProvider(
                depotViewer);
        provider.setLoadAsync(false); // This is to avoid lazy loading, since this can cause inaccurate checked item counting when import an unexpanded node's sub folders as projects. 
        depotViewer.setContentProvider(provider);
        depotViewer.setLabelProvider(new PerforceLabelProvider());
        depotViewer.addCheckStateListener(new ICheckStateListener() {

            public void checkStateChanged(final CheckStateChangedEvent event) {
            	Object element = event.getElement();
            	if (!(element instanceof IP4Folder) 
                        && event.getChecked()) {
					if ((element instanceof P4Depot)) {
						DepotType depotType = ((P4Depot) element).getType();
						if (depotType != DepotType.LOCAL && depotType != DepotType.STREAM)
							depotViewer.setChecked(element, false);
					} else {
						depotViewer.setChecked(element, false);
					}
                }
            	depotViewer.removeChildOnlyNode(element);
            	Tracing.printTrace("ImportProjects",MessageFormat.format("removeChildOnlyNode {0}, childOnlySet={1}",element,depotViewer.getChildOnlySet()));//$NON-NLS-1$ //$NON-NLS-2$,$NON-NLS-2$
            	Tracing.printExecTime(Policy.DEBUG, "ImportProjects", "TREE: collect children", new Runnable() {//$NON-NLS-1$ //$NON-NLS-2$
					
					public void run() {
						updateSelectedFolders(event.getElement());
					}
				});
                validateStreamDirectories(folders);
            }
        });
        depotViewer.addFilter(new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement,
                    Object element) {
                if (element instanceof IP4File) {
                    IP4File resource = (IP4File) element;
                    if (resource.getConnection() instanceof P4BrowsableConnection) {
                    	if(resource.isHeadActionDelete()) // hide deleted files
                    		return false;
                        // Fix for job034668, show all files for non-existent
                        // clients.
                        return true;
                    } else {
                        // Show file is mapped in the client view
                        return resource.getLocalPath() != null;
                    }
                } else if (element instanceof P4Depot) {
                    return ((P4Depot) element).isLocal();
                }
                return true;
            }

        });
        depotViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				Object obj = SWTUtils.getSingleSelectedObject(depotViewer);
				if(obj!=null && depotViewer.getChecked(obj) && !depotViewer.getGrayed(obj)){
					selectFromDepotViewer=true;
					projectsViewer.setSelection(event.getSelection(), true);
					selectFromDepotViewer=false;
				}
			}
		});
        setDepotViewerMenu();

        projectsViewer=new ListViewer(right,SWT.V_SCROLL|SWT.H_SCROLL);
        projectsViewer.setContentProvider(new ArrayContentProvider());
        projectsViewer.getControl().setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true,2,1));
        projectsViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if(!selectFromDepotViewer)
					depotViewer.setSelection(event.getSelection(), true);
			}
		});
        
        descLabel = new Label(displayArea, SWT.WRAP);
        descLabel.setText(Messages.ImportProjectsWizardPage_ImportProjectsHelpMessage);
        descLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 2, 1));
        
        setControl(displayArea);
        if (!isOptional) {
            setPageComplete(false);
        }
    }

	private void setDepotViewerMenu() {
		MenuManager contextMenu = new MenuManager("#PopUp");//$NON-NLS-1$
		contextMenu.add(new Separator("additions"));//$NON-NLS-1$
		contextMenu.setRemoveAllWhenShown(true);
		contextMenu.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				manager.add(new Action(Messages.ImportProjectsWizardPage_ImportSubFoldersAsProjects) {
					public boolean isEnabled() {
						Object sel= SWTUtils.getSingleSelectedObject(depotViewer);
						if(sel instanceof IP4Container){
							if(depotViewer.getExpandedState(sel)){
								return super.isEnabled() && hasSubfolders((IP4Container) sel);
							}
							return super.isEnabled();
						}
						return false;
					}
					private boolean hasSubfolders(IP4Container container) {
						for(IP4Resource res:container.members()){
							if(res instanceof IP4Container){
								return true;
							}
						}
						return false;
					}
					public void run() {
						final Object sel= SWTUtils.getSingleSelectedObject(depotViewer);
						if(sel instanceof IP4Container){
							boolean expanded = depotViewer.getExpandedState(sel);
							boolean checked = depotViewer.getChecked(sel);
							boolean grayed = depotViewer.getGrayed(sel);
							if(!expanded){
								Tracing.printExecTime(Policy.DEBUG, "ImortProjects", MessageFormat.format("expandCollapseNode {0}", sel), new Runnable() {//$NON-NLS-1$ //$NON-NLS-2$,$NON-NLS-2$
									public void run() {
										depotViewer.setExpandedState(sel, true);										
									}
								});
							}
							
			            	if(hasSubfolders((IP4Container) sel)){
			            		depotViewer.addChildOnlyNode(sel);
			            		Tracing.printTrace("ImportProjects",MessageFormat.format("addChildOnlyNode {0}, childOnlySet={1}",sel,depotViewer.getChildOnlySet()));//$NON-NLS-1$ //$NON-NLS-2$
			            		if(!checked)
			            			depotViewer.setChecked(sel, true);
			            		if(!grayed)
			            			depotViewer.setGrayed(sel, true);
			            		updateSelectedFolders(sel);
			            		validateStreamDirectories(folders);
			            	}else{
			            		MessageDialog.openWarning(depotViewer.getTree().getShell(), Messages.ImportProjectsWizardPage_InvalidOperation, Messages.ImportProjectsWizardPage_NoSubFolderAvailableError);
			            	}
						}
					}
				});
			}
		});
		Menu menu = contextMenu.createContextMenu(depotViewer.getControl());
		depotViewer.getControl().setMenu(menu);
	}

	protected void updateSelectedFolders(Object sel) {
    	List<IP4Folder> result=new ArrayList<IP4Folder>();
    	Tree tree = depotViewer.getTree();
    	for(TreeItem item:tree.getItems()){
    		collectChildren(item,result);
    	}
    	folders=result.toArray(new IP4Folder[0]);
    	
		totalLink.setText(folders.length+"");//("<a>"+folders.length+"</a>");//$NON-NLS-1$ //$NON-NLS-2$
		totalLink.getParent().layout();

        projectsViewer.setInput(folders); // update projects viewer
        projectsViewer.setSelection(new StructuredSelection(sel));

    	Tracing.printTrace("ImportProjects", "\n\nSELECTED:"+Arrays.toString(folders)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private void collectChildren(TreeItem item, List<IP4Folder> result) {
		if(item.getChecked()){
			if(!item.getGrayed()){
				Object element=item.getData();
				if(element instanceof IP4Folder){
					result.add((IP4Folder) element);
				}else if(element instanceof P4Depot){
					P4Depot depot = (P4Depot)element;
					if(depot.needsRefresh()){
						depot.refresh();
						for(IP4Resource res:depot.members()){
							if(res instanceof IP4Folder)
								result.add((IP4Folder) res);
						}
					}else{
						for(TreeItem subItem:item.getItems()){
							collectChildren(subItem, result);
						}
					}
				}else if(element instanceof P4Connection){
					for(TreeItem subItem:item.getItems()){
						collectChildren(subItem, result);
					}
				}
			}else{ // grayed
				for(TreeItem subItem:item.getItems()){
					collectChildren(subItem, result);
				}
			}
		}else{ // unchecked
		}
	}

	protected void validateStreamDirectories(final IP4Folder[] folders) {
        if (!isOptional) {
            setPageComplete(folders.length > 0);
            if (folders.length > 0) {
                setPageComplete(true);
                setErrorMessage(null);
            } else {
                setPageComplete(false);
                setErrorMessage(Messages.ImportProjectsWizardPage_SelectAtLeastOneFolder);
            }
        }else{
            final String stream=((IConnectionWizard)getWizard()).getStream();
            try {
                getContainer().run(true, true, new IRunnableWithProgress() {

                    public void run(IProgressMonitor monitor)
                            throws InvocationTargetException, InterruptedException {
                        if (monitor == null) {
                            monitor = new NullProgressMonitor();
                        }
                        monitor.beginTask(Messages.ImportProjectsWizardPage_ValidatingFolders,folders.length);
                        
                        final List<IP4Folder> invalidFolders=new ArrayList<IP4Folder>();
                        for(IP4Folder folder: folders){
                            IOptionsServer server = (IOptionsServer) folder.getConnection().getServer();
                            String dir = folder.getRemotePath();//+"/*"; //$NON-NLS-1$
                            List<IFileSpec> fileSpecs;
                            try {
                                GetDirectoriesOptions options = new GetDirectoriesOptions();
                                if(!StringUtils.isEmpty(stream))
                                    options.setStream(stream);
                                else
                                    break;
                                fileSpecs = server.getDirectories(
                                        P4FileSpecBuilder.makeFileSpecList(dir),
                                        options);
                                if(fileSpecs==null || fileSpecs.size()==0)
                                    invalidFolders.add(folder);
                            } catch (P4JavaException e) {
                                e.printStackTrace();
                            }
                            monitor.worked(1);
                            if (monitor.isCanceled()) {
                                break;
                            }
                        }
                        monitor.done();
                        
                        UIJob job = new UIJob(Messages.ImportProjectsWizardPage_UpdateStatus) {

                            @Override
                            public IStatus runInUIThread(IProgressMonitor monitor) {
                                if(invalidFolders.size()==0){
                                    setPageComplete(true);
                                    setErrorMessage(null);
                                }else{
                                    StringBuilder sb=new StringBuilder();
                                    for(IP4Folder folder: invalidFolders){
                                        sb.append(folder.getRemotePath()+", "); //$NON-NLS-1$
                                    }
                                    sb.deleteCharAt(sb.length()-1);
                                    sb.deleteCharAt(sb.length()-1);
                                    setPageComplete(false);
                                    setErrorMessage(MessageFormat.format(Messages.ImportProjectsWizardPage_FoldersNotInClientView,sb.toString()));
                                }
                                return Status.OK_STATUS;
                            }

                        };
                        job.schedule();
                    }
                });
            } catch (InvocationTargetException e) {
                PerforceProviderPlugin.logError(e);
            } catch (InterruptedException e) {
                PerforceProviderPlugin.logError(e);
            }

        }
        
    }

}
