/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.connection;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.file.FileSpecOpStatus;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.team.core.IConstants;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4ClientOperation;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.P4ClientOperation;
import com.perforce.team.core.p4java.builder.P4FileSpecBuilder;
import com.perforce.team.ui.ConfigWizard;
import com.perforce.team.ui.IErrorDisplay;
import com.perforce.team.ui.IErrorProvider;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.PerforceLabelProvider;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.SWTUtils;
import com.perforce.team.ui.actions.NewServerAction;
import com.perforce.team.ui.operations.RetryableOperation;

/**
 * Sharing project wizard page. The page allow user create new server
 * connection, create client root if necessary, change the relative path under
 * client root, and also allow user to de-select the projects to share.
 * <ul>
 * <li>For project under client root, default will leave project where it is</li>
 * <li>For project not under client root, default is move project as direct
 * child of client root</li>
 * </ul>
 * 
 * @author Alex Li (ali@perforce.com)
 */
public class SelectConnectionWizardPage extends WizardPage implements
        IErrorDisplay {

    private static final String NULL_ROOT = "null";
    
	private ComboViewer serverCombo;
    private Button newServerButton;
    private ClientPathWidget clientPathWidget;
    private CheckboxTableViewer projectMoveViewer;
    private ProjectMoveLabelProvider projectMoveLabelProvider = new ProjectMoveLabelProvider();
    private Map<IProject, Boolean> projectMap=new HashMap<IProject, Boolean>();
    
    private boolean clientPathInvalid=false;

    /**
     * @param pageName
     */
    public SelectConnectionWizardPage(String pageName) {
        super(pageName,
                Messages.SelectConnectionWizardPage_EnterConnectionInfo,
                PerforceUIPlugin.getPlugin().getImageDescriptor(
                        IPerforceUIConstants.IMG_SHARE_WIZARD));
        setDescription(Messages.SelectConnectionWizardPage_DefineConnection);
    }

    /**
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        Composite displayArea = new Composite(parent, SWT.NONE);
        displayArea.setLayout(new GridLayout(3, false));
        displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Label serverLabel=SWTUtils.createLabel(displayArea,
                Messages.SelectConnectionWizardPage_ServerConnection);
        GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER).grab(false, false).applyTo(serverLabel);
        
        serverCombo = new ComboViewer(displayArea, SWT.READ_ONLY);
        serverCombo.setLabelProvider(new PerforceLabelProvider(false));
        serverCombo.setContentProvider(new ArrayContentProvider());
        serverCombo.setSorter(new ViewerSorter());
        serverCombo.setFilters(new ViewerFilter[] { new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement,
                    Object element) {
                if (element instanceof IP4Connection) {
                    if (!((IP4Connection) element).isOffline())
                        return true;
                }
                return false;
            }

        } });

        GridData vData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        serverCombo.getControl().setLayoutData(vData);
        serverCombo.setInput(P4ConnectionManager.getManager().getConnections());
        serverCombo
                .addSelectionChangedListener(new ISelectionChangedListener() {

                    public void selectionChanged(SelectionChangedEvent event) {
                        Object element = ((IStructuredSelection) event
                                .getSelection()).getFirstElement();
                        if (element instanceof IP4Connection) {
                            IP4Connection connection = (IP4Connection) element;
                            try {
                                if (connection.getClient() != null) {
                                	String currentRoot=getActiveRoot(connection);
                                    File root = new File(currentRoot);
                                    if(!validateClientRoot(root))
                                        return;
                                    clientPathInvalid=false;
                                } else {
                                    serverCombo.refresh(true); // refresh to remove off-line connection
                                    clientPathInvalid=true;
                                }
                            } catch (Exception e) {
                                PerforceProviderPlugin.logError(e);
                                clientPathInvalid=true;
                            } finally{
                                clientPathWidget.setInput(getConnection());
                                refreshProjectMoveViewer();
                                updateEnablement();
                            }
                        }
                    }
                });

        newServerButton = new Button(displayArea, SWT.PUSH);
        GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER).grab(false, false).applyTo(newServerButton);
        newServerButton
                .setText(Messages.SelectConnectionWizardPage_NewServerConnection);
        newServerButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                new NewServerAction().run(null);
                serverCombo.setInput(P4ConnectionManager.getManager()
                        .getConnections());
                serverCombo.refresh();
                serverCombo.setSelection(null);
            }
        });

        clientPathWidget = new ClientPathWidget();
        clientPathWidget.createControl(displayArea);
        clientPathWidget.setInput(getConnection());
        
        projectMoveViewer = createProjectMoveViewer(displayArea);
        
        updateEnablement();
        setControl(displayArea);
    }

    protected String getActiveRoot(IP4Connection connection) {
        String currentRoot = connection
                .getCurrentDirectory();
        if (currentRoot == null)
            currentRoot = getClientRootWithProgress(connection);//connection.getClientRoot();
		return currentRoot;
	}

	protected String getClientRootWithProgress(final IP4Connection connection) {
    	final String[] root=new String[1];
    	try {
			getContainer().run(false, false, new IRunnableWithProgress() {
				
				public void run(IProgressMonitor monitor) throws InvocationTargetException,
						InterruptedException {
					monitor.beginTask(Messages.SelectConnectionWizardPage_FetchingRoot, 100);
					monitor.worked(50);
					root[0]=connection.getClientRoot();
					monitor.worked(50);
					monitor.done();
				}
			});
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	
		return root[0];
	}

	protected boolean validateClientRoot(File root) {
		if(root==null)
			return false;
		
		// only on windows allow null root
		if(NULL_ROOT.equalsIgnoreCase(root.getPath())){
			if(P4CoreUtils.isWindows()){
				return true;
			}else{
	    		MessageDialog.openError(getShell(), Messages.SelectConnectionWizardPage_Error, Messages.SelectConnectionWizardPage_ClientRootCannotBeNull);
	    		serverCombo.setSelection(null);
	    		return false;
			}
		}
		
    	if(root.isAbsolute()){
	    	if(!root.exists()){
	    		MessageDialog.openError(getShell(), Messages.SelectConnectionWizardPage_Error, MessageFormat.format(Messages.SelectConnectionWizardPage_ClientRootNotExist,root.toString()));
	    		serverCombo.setSelection(null);
	    		return false;
	    	}else if(!root.canWrite()){
	    		MessageDialog.openError(getShell(), Messages.SelectConnectionWizardPage_Error, MessageFormat.format(Messages.SelectConnectionWizardPage_ClientRootNotWritable,root.toString()));
	    		serverCombo.setSelection(null);
	    		return false;
	    	}
	    	return true ;
    	}
		MessageDialog.openError(getShell(), Messages.SelectConnectionWizardPage_Error, MessageFormat.format(Messages.SelectConnectionWizardPage_ClientRootNotExist,root.toString()));
		serverCombo.setSelection(null);
    	return false;
    }

    protected void refreshProjectMoveViewer() {
        IPath targetPath = clientPathWidget.getTargetClientPath();
        projectMoveLabelProvider.targetFolder = targetPath;
        projectMoveViewer.refresh();
        resizeViewerLastColumn(projectMoveViewer);
    }

    private CheckboxTableViewer createProjectMoveViewer(Composite parent) {
        Table projectMoveTable = new Table(parent, SWT.MULTI
                | SWT.FULL_SELECTION | SWT.CHECK);
        CheckboxTableViewer viewer = new CheckboxTableViewer(projectMoveTable);
        GridDataFactory.fillDefaults().span(3, 1).grab(true, true)
                .applyTo(projectMoveTable);

        TableColumn tc;
        tc = new TableColumn(projectMoveTable, SWT.NONE);
        tc.setText(Messages.SelectConnectionWizardPage_Relocate);
        tc.setWidth(100);

        tc = new TableColumn(projectMoveTable, SWT.NONE);
        tc.setText(Messages.SelectConnectionWizardPage_ProjectNameColumnHeader);
        tc.setWidth(150);

        tc = new TableColumn(projectMoveTable, SWT.NONE);
        tc.setText(Messages.SelectConnectionWizardPage_CurrentLocationColumnHeader);
        tc.setWidth(250);

        tc = new TableColumn(projectMoveTable, SWT.NONE);
        tc.setText(Messages.SelectConnectionWizardPage_NewLocationTargetHeader);
        tc.setWidth(350);

        projectMoveTable.setHeaderVisible(true);

        viewer.setLabelProvider(projectMoveLabelProvider);
        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setSorter(new ViewerSorter());
        GridData vData = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
        vData.heightHint = 100;
        viewer.getTable().setLayoutData(vData);

        viewer.addCheckStateListener(new ICheckStateListener() {

            public void checkStateChanged(CheckStateChangedEvent event) {
                IProject prj=(IProject) event.getElement();
                projectMap.put(prj, event.getChecked());
                projectMoveViewer.refresh(prj);
                resizeViewerLastColumn(projectMoveViewer);
            }
        });

        initProjectMap();
        viewer.setInput(projectMap.keySet().toArray(new IProject[0]));
        resizeViewerColumns(viewer);

        TableItem[] children = viewer.getTable().getItems();
        for (int i = 0; i < children.length; i++) {
            TableItem item = children[i];
            IProject prj=(IProject) item.getData();
            item.setChecked(projectMap.get(prj));
        }

        return viewer;

    }

    private void resizeViewerLastColumn(CheckboxTableViewer viewer){
        viewer.getTable().getColumns()[viewer.getTable().getColumnCount()-1].pack();
    }
    
    private void resizeViewerColumns(CheckboxTableViewer viewer){
        for(TableColumn col:viewer.getTable().getColumns()){
        	col.pack();
        }
    }
    
    private void initProjectMap() {
        projectMap.clear();
        for(IProject prj: ((ConfigWizard) getWizard()).getProjects()){
            projectMap.put(prj, true); // default is to relocate shared projects
        }
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible)
            updateEnablement();
    }

    private void updateEnablement() {

        if (serverCombo.getSelection().isEmpty()) {
            setDescription(Messages.SelectConnectionWizardPage_SelectOrCreateConnection);
        } else if (clientPathInvalid) {
            setErrorMessage(Messages.SelectConnectionWizardPage_InvalidClientPath);
        } else {
            setErrorMessage(null);
        }
        setPageComplete(getErrorMessage() == null
                && !serverCombo.getSelection().isEmpty());
    }

    /**
     * Get the selected connection
     * 
     * @return - connection
     */
    public IP4Connection getConnection() {
        IStructuredSelection selection = (IStructuredSelection) serverCombo
                .getSelection();

        IP4Connection connection = null;
        if (selection.getFirstElement() instanceof IP4Connection) {
            connection = (IP4Connection) selection.getFirstElement();
        }
        return connection;
    }

    /**
     * @see com.perforce.team.ui.IErrorDisplay#setErrorMessage(java.lang.String,
     *      com.perforce.team.ui.IErrorProvider)
     */
    public void setErrorMessage(String message, IErrorProvider provider) {
        super.setErrorMessage(message);
        setPageComplete(getErrorMessage() == null);
    }

    public Map<IProject, File> getProjects(boolean relocate) {
        Map<IProject, File> ret = new HashMap<IProject, File>();
        IPath targetPath = clientPathWidget.getTargetClientPath();
        for(IProject project: projectMap.keySet()){
            if(projectMap.get(project) && relocate){// relocate
                boolean isPrefix = targetPath
                        .isPrefixOf(project.getLocation());
                if (isPrefix) {
                    ret.put(project, new File(project.getLocation().toOSString()));
                } else {
                    IPath targetLocation = targetPath
                            .append(project.getLocation().lastSegment());
                    ret.put(project, targetLocation.toFile());
                }
            }
            if(!projectMap.get(project) && !relocate){// non relocate
                ret.put(project, new File(project.getLocation().toOSString()));
            }
        }
        
        return ret;
    }

    private class ProjectMoveLabelProvider extends LabelProvider implements
            ITableLabelProvider {

        IPath targetFolder; // client root+relative path

        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        public String getColumnText(Object element, int columnIndex) {
            IProject prj = (IProject) element;
            switch (columnIndex) {
            case 1:
                return prj.getName();
            case 2:
                return prj.getLocation().toOSString();
            case 3:
                if(projectMap.get(prj)){
                    if (targetFolder != null && !targetFolder.isEmpty()) {
                        IPath showPath = null;
                        if (targetFolder.isPrefixOf(prj.getLocation())) {
                            showPath = prj.getLocation();
                        } else {
                            showPath = targetFolder.append(prj.getLocation().lastSegment());
                        }
                        if (showPath != null)
                            return showPath.toOSString();
                    }
                } 
                return null;
            default:
                return null;
            }
        }

    }

    private class ClientPathWidget{
        private Composite clientPathControl;
        
        private Label clientRootLabel;
        private Text clientRelPathText;
        private Button browseButton;
        public void createControl(Composite parent){
            Label targetLabel=SWTUtils.createLabel(parent,
                    Messages.SelectConnectionWizardPage_TargetClientPath);
            GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(targetLabel);

            Composite container=new Composite(parent, SWT.NONE);
            GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).span(1,1).grab(true, false).applyTo(container);
            GridLayoutFactory.fillDefaults().numColumns(2).applyTo(container);
            clientRootLabel=new Label(container, SWT.NONE);
            GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(clientRootLabel);
            // clientRootLabel.setFont(new Font(parent.getDisplay(),"Arial", 12, SWT.BOLD));
            clientRootLabel.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
            clientRelPathText = SWTUtils.createText(container);
            GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(clientRelPathText);
            clientRelPathText.addModifyListener(new ModifyListener() {

                public void modifyText(ModifyEvent e) {
                    refreshProjectMoveViewer();
                }
            });

            browseButton = new Button(parent, SWT.PUSH);
            browseButton.setText(Messages.SelectConnectionWizardPage_Browse);
            GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(browseButton);
            browseButton.addSelectionListener(new SelectionAdapter() {

                public void widgetSelected(SelectionEvent e) {
                    DirectoryDialog dlg = new DirectoryDialog(
                            SelectConnectionWizardPage.this.getShell(), SWT.OPEN);
                    IPath rootdir = new Path(getClientRoot());
                    IPath filter = rootdir.append(clientRelPathText.getText());
                    dlg.setFilterPath(filter.toString());
                    String result = dlg.open();
                    if (result != null) {
                        IPath newdir = new Path(result.trim());
                        if (!rootdir.isPrefixOf(newdir)) {
                            MessageDialog
                                    .openError(
                                            null,
                                            Messages.SelectConnectionWizardPage_Error,
                                            MessageFormat
                                                    .format(Messages.SelectConnectionWizardPage_Error_RelativePathNotInClientRoot,
                                                            rootdir.toOSString()));
                            return;
                        } else {
                            clientRelPathText.setText(newdir
                                    .removeFirstSegments(rootdir.segmentCount())
                                    .setDevice(null).toString());
                        }
                    }
                }
            });

            clientPathControl=container;

        }
        
        public void setInput(IP4Connection connection) {
            getBrowseClientPathButton().setEnabled(connection!=null);
            if(connection==null){
            	if(!getRelativePath().isEmpty())
            		setRelativePath(IConstants.EMPTY_STRING);
                setClientRoot(IConstants.EMPTY_STRING);
            }else{
                setClientRoot(getActiveRoot(connection));
            }
            clientRelPathText.setEnabled(!getClientRoot().isEmpty());
        }

        public IPath getTargetClientPath() {
        	String root = getClientRoot();
        	if(P4CoreUtils.isWindows() && NULL_ROOT.equalsIgnoreCase(root))
        		return new Path(getRelativePath());
            return new Path(root).append(new Path(getRelativePath()));
        }

        public Composite getClientPathControl(){
            return clientPathControl;
        }
        
        public String getClientRoot(){
            return clientRootLabel.getText().trim();
        }
        public String getRelativePath(){
            return clientRelPathText.getText().trim();
        }
        public void setClientRoot(String text) {
        	if(text==null)
        		text=IConstants.EMPTY_STRING;
            clientRootLabel.setText(text);
            getClientPathControl().layout(true);
        }
        public void setRelativePath(String text) {
            clientRelPathText.setText(text);
        }
        private Button getBrowseClientPathButton(){
            return browseButton;
        }
    }
    public void setConnection(IP4Connection ip4Connection) {
        serverCombo.setSelection(new StructuredSelection(ip4Connection));
    }

    public void setRelativePath(String rpath) {
        clientPathWidget.setRelativePath(rpath);
    }

    public Map<IProject, String> getInvalidProject(
            final Map<IProject, File> projectsToMove, final IP4Connection connection) {
        final Map<IProject, String> errorProjects = new HashMap<IProject, String>();
        try {
			getContainer().run(false, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException,
						InterruptedException {
					monitor.beginTask(Messages.SelectConnectionWizardPage_ValidateProjects, projectsToMove.size()+1);
					monitor.worked(1);
			        for (Map.Entry<IProject, File> entry : projectsToMove.entrySet()) {

			            IProject project = entry.getKey();

			            final List<IFileSpec> specList = P4FileSpecBuilder
			                    .makeFileSpecList(new String[] { new File(entry.getValue(),
			                            ".project").toString() }); //$NON-NLS-1$
			            try {
			                List<IFileSpec> added = RetryableOperation.whereWithRetry(connection, specList);//connection.getClient().where(specList);
			                if (added.size() == 1) {
			                    IFileSpec spec = added.get(0);
			                    if (spec.getOpStatus() != FileSpecOpStatus.VALID
			                            && spec.getOpStatus() != FileSpecOpStatus.INFO)
			                        errorProjects.put(project, spec.getStatusMessage());
			                }
			            } catch (Throwable e) {
			                errorProjects.put(project, e.getLocalizedMessage());
			                PerforceProviderPlugin.logError(e);
			            } finally{
			            	monitor.worked(1);
			            }
			        }
			        monitor.done();
				}
			});
		} catch (InvocationTargetException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

        return errorProjects;
    }
    
}
