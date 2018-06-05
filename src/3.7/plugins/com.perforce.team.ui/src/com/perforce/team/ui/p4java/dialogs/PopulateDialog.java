/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.dialogs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.SWTUtils;
import com.perforce.team.ui.changelists.DescriptionViewer;
import com.perforce.team.ui.dialogs.P4StatusDialog;
import com.perforce.team.ui.resource.ResourceBrowserDialog;
import com.perforce.team.ui.resource.VersionedResourceBrowserDialog;
import com.perforce.team.ui.swt.AbstractModelObject;
import com.perforce.team.ui.swt.PageBook;
import com.perforce.team.ui.swt.PageBook.BookPage;

public class PopulateDialog extends P4StatusDialog {
	public static class FileVersion extends AbstractModelObject{
		/**
		 * <ul>
		 * Annotations:
		 * <li><em>label:</em> //depot/project1/...@release1</li>
		 * <li><em>changelist:</em>//depot/project1/.project@123456</li>
		 * <li><em>revision:</em>//depot/project1/src/HelloWorld.java#2</li>
		 * <li><em>null:</em>the head revision</li>
		 * </ul>
		 * 
		 */
		String path; // could be a file or folder, in case of folder, a ... will be appended
		String revision; // @label, @change, #revision or null which means head
		
		public FileVersion(String path, String revision) {
			super();
			this.path = path;
			this.revision = revision;
		}

		public FileVersion(String pathAndRevision) {
			super();
			int index=findRevisionIndex(pathAndRevision);
			if(index>0){
				this.path = pathAndRevision.substring(0,index);
				this.revision = pathAndRevision.substring(index);
			}else{
				this.path = pathAndRevision;
			}
		}
		
		private int findRevisionIndex(String pathAndRevision) {
			int index=pathAndRevision.indexOf("@"); //$NON-NLS-1$
			if(index>0)
				return index;
			index=pathAndRevision.indexOf("#"); //$NON-NLS-1$
			if(index>0)
				return index;
			return index;
		}

		public String getPath() {
			return path;
		}


		public void setPath(String path) {
			String oldValue = this.path;
			this.path = path;
			firePropertyChange("path", oldValue, path); //$NON-NLS-1$
		}


		public String getRevision() {
			return revision;
		}


		public void setRevision(String revision) {
			String oldValue = this.revision;
			this.revision = revision;
			firePropertyChange("revision", oldValue, revision); //$NON-NLS-1$
		}


		@Override
		public String toString() {
			return revision==null?path:path+revision;
		}
	}
	
	enum PopulateMethod{
		SOURCE_TARGET(Messages.PopulateDialog_Specify_source_and_target),BRANCH(Messages.PopulateDialog_Specify_branch);
		
		private String desc;

		PopulateMethod(String desc){
			this.desc=desc;
		}
		
		@Override
		public String toString() {
			return desc;
		}
	}

	public static class PopulateModel{
		private List<FileVersion> sourceRevisions=new ArrayList<FileVersion>();
		private String targetPath;
		private PopulateMethod populateMethod;
		
		public PopulateModel(String sourcePath, String targetPath,
				PopulateMethod populateMethod) {
			super();
			this.sourceRevisions = new ArrayList<PopulateDialog.FileVersion>();
			if(!StringUtils.isEmpty(sourcePath))
				sourceRevisions.add(new FileVersion(sourcePath));
			this.targetPath = targetPath;
			this.populateMethod = populateMethod;
		}

		public List<FileVersion> getSourceRevisions() {
			return sourceRevisions;
		}

		public String getTargetPath() {
			return targetPath;
		}

		public void setTargetPath(String targetPath) {
			this.targetPath = targetPath;
		}

		public PopulateMethod getPopulateMethod() {
			return populateMethod;
		}

		public void setPopulateMethod(PopulateMethod populateMethod) {
			this.populateMethod = populateMethod;
		}

	}

	private class BranchPage extends BookPage{

		public BranchPage(Composite parent, int style) {
			super(parent, style);
			init(null);
		}

		public Object getId() {
			return PopulateMethod.BRANCH;
		}

		@Override
		public void createControl() {
			setLayout(new GridLayout(1,false));
			SWTUtils.createLabel(this, Messages.PopulateDialog_Not_implemented_yet);
		}

		@Override
		public void addListeners() {}

	}
	
	private class SourceTargetPage extends BookPage{
		private TableViewer sourceTable;
		private Button addBtn;
		private Button editBtn;
		private Button removeBtn;

		private Text targetText;
		private Button browseBtn;
		
		PopulateModel model;

		public SourceTargetPage(Composite parent, int style, PopulateModel model) {
			super(parent, style);
			this.model = model;
			init(model);
		}

		@Override
		public void createControl() {
			setLayout(new GridLayout(1,false));
	        createTableGroup(this);
	        createTargetGroup(this);
	        enableButtons();
		}
		
		private void createTargetGroup(Composite parent) {
			Composite group=new Composite(parent, SWT.NONE);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(group);;
			group.setLayout(new GridLayout(2,false));

			SWTUtils.createLabel(group, Messages.PopulateDialog_Choose_target_file_folders, 2);
			targetText=SWTUtils.createText(group, 1, SWT.NONE);//SWT.READ_ONLY);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(targetText);
			
			browseBtn=SWTUtils.createButton(group, Messages.PopulateDialog_Browse, SWT.PUSH, 1);
			browseBtn.setEnabled(false);
		}

		private void createTableGroup(Composite parent) {
			Composite group=new Composite(parent, SWT.NONE);
			GridDataFactory.fillDefaults().grab(true, true).applyTo(group);;
			group.setLayout(new GridLayout(2,false));
			
	        SWTUtils.createLabel(group, Messages.PopulateDialog_Source_files_folders, 2);
			sourceTable = createSourceTable(group);
	        GridDataFactory.fillDefaults().grab(true, true).span(1, 3).hint(400, 200).applyTo(sourceTable.getControl());;
	        
	        addBtn=SWTUtils.createButton(group, Messages.PopulateDialog_Add, SWT.PUSH, 1);
	        editBtn=SWTUtils.createButton(group, Messages.PopulateDialog_Edit, SWT.PUSH, 1);
	        removeBtn=SWTUtils.createButton(group, Messages.PopulateDialog_Remove, SWT.PUSH, 1);
	        
		}

		@Override
		public Object getId() {
			return PopulateMethod.SOURCE_TARGET;
		}

		@Override
		public void setInput(Object input) {
			if(input instanceof PopulateModel){
				PopulateModel m = ((PopulateModel) input);
				sourceTable.setInput(m.getSourceRevisions());
				targetText.setText(m.getTargetPath());
			}
		}
		
		@Override
		public void addListeners() {
			targetText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					model.setTargetPath(targetText.getText());
				}
			});
			
			sourceTable.addSelectionChangedListener(new ISelectionChangedListener() {
				
				public void selectionChanged(SelectionChangedEvent event) {
					enableButtons();
				}
			});
			
			addBtn.addSelectionListener(new SelectionAdapter(){
				public void widgetSelected(SelectionEvent e) {
					VersionedResourceBrowserDialog dlg = new VersionedResourceBrowserDialog(getShell(), new IP4Resource[]{connection}, ""); //$NON-NLS-1$
					if(Window.OK==dlg.open()){
						IP4Resource resource = dlg.getSelectedResource();
						FileVersion v = new FileVersion(resource.getActionPath(),dlg.getVersion());
						model.getSourceRevisions().add(v);
						updateBasePath();
						refreshAndResizeTable();
						SWTUtils.select(sourceTable, v);
					}
				}
			});

			removeBtn.addSelectionListener(new SelectionAdapter(){
				public void widgetSelected(SelectionEvent e) {
					IStructuredSelection sel=(IStructuredSelection) sourceTable.getSelection();
					for(Iterator<?> it=sel.iterator();it.hasNext();){
						model.getSourceRevisions().remove(it.next());
					}
					updateBasePath();
					refreshAndResizeTable();
				}
			});
						
			editBtn.addSelectionListener(new SelectionAdapter(){
				public void widgetSelected(SelectionEvent e) {
					FileVersion version = (FileVersion) SWTUtils.getSingleSelectedObject(sourceTable);
					
					VersionedResourceBrowserDialog dlg = new VersionedResourceBrowserDialog(getShell(), new IP4Resource[]{connection}, version.toString());
					if(Window.OK==dlg.open()){
						IP4Resource resource = dlg.getSelectedResource();
						model.getSourceRevisions().remove(version);
						FileVersion v = new FileVersion(resource.getActionPath(),dlg.getVersion());
						model.getSourceRevisions().add(v);
						updateBasePath();
						refreshAndResizeTable();
						SWTUtils.select(sourceTable, v);
					}
				}
			});
			
			sourceTable.setInput(model.getSourceRevisions());
			
			browseBtn.addSelectionListener(new SelectionAdapter() {
				
				public void widgetSelected(SelectionEvent e) {
					ResourceBrowserDialog dlg = new ResourceBrowserDialog(getShell(), new IP4Resource[]{connection}){
						@Override
						protected void okPressed() {
							IP4Resource sel = widget.getSelectedResource();
							if(sel==null || StringUtils.isEmpty(sel.getActionPath())){
								setErrorMessage(Messages.PopulateDialog_Must_select_file_or_folder);
								return;
							}
							super.okPressed();
						}
					};
					if(Window.OK==dlg.open()){
						String path = dlg.getSelectedResource().getActionPath();
						model.setTargetPath(path);
						targetText.setText(path);
					}
				}
			});
		}

		protected void refreshAndResizeTable() {
			sourceTable.refresh();
			for(TableColumn c:sourceTable.getTable().getColumns()){
				c.pack();
			}
		}

		protected void updateBasePath() {
			if(model.sourceRevisions.size()>1){
				String prefix=computeBasePath();
				if(StringUtils.isEmpty(prefix)){
					sourceTable.getTable().getColumn(1).setText(Messages.PopulateDialog_Base_path_unavailable);
				}else
					sourceTable.getTable().getColumn(1).setText(Messages.PopulateDialog_Base_path+prefix);
			}else{
				sourceTable.getTable().getColumn(1).setText(Messages.PopulateDialog_Path);
			}
		}

		protected void enableButtons() {
			boolean enable=!sourceTable.getSelection().isEmpty();
			editBtn.setEnabled(enable);
			removeBtn.setEnabled(enable);
		}

	}
	
	private static class Sorter extends ViewerComparator{

		  private int column;
		  private static final int DESCENDING = 1;
		  private int direction = DESCENDING;

		  public Sorter() {
		    this.column = 0;
		    direction = DESCENDING;
		  }

		  public int getDirection() {
		    return direction == 1 ? SWT.DOWN : SWT.UP;
		  }

		  public void setColumn(int column) {
		    if (column == this.column) {
		      // Same column as last sort; toggle the direction
		      direction = 1 - direction;
		    } else {
		      // New column; do an ascending sort
		      this.column = column;
		      direction = DESCENDING;
		    }
		  }

		  @Override
		  public int compare(Viewer viewer, Object e1, Object e2) {
			    FileVersion p1 = (FileVersion) e1;
			    FileVersion p2 = (FileVersion) e2;
		    int rc = 0;
		    switch (column) {
		    case 0:
		      break;
		    case 1:
		      rc = p1.getPath().compareTo(p2.getPath());
		      break;
		    case 2:
		    	if(p1.getRevision()!=null && p2.getRevision()!=null)
		    		rc = p1.getRevision().compareTo(p2.getRevision());
		      break;
		    default:
		      rc = 0;
		    }
		    // If descending order, flip the direction
		    if (direction == DESCENDING) {
		      rc = -rc;
		    }
		    return rc;
		  }		
	}
	private IP4Connection connection;
	
	
	private Composite displayArea;
	private ComboViewer branchCombo;
	private PageBook pageBook;
	
    private Button preview;

	private Label progressLabel;
	private ProgressBar progressBar;

	private PopulateModel model;
	private String basePath=""; //$NON-NLS-1$
	private String description=Messages.PopulateDialog_Branching; //$NON-NLS-1$


	private DescriptionViewer descriptionViewer;
	
	/**
     * Creates an populate dialog opened against a specified connection with
     * initial source and target path values
     */
    public PopulateDialog(Shell parent, IP4Connection connection,
            String sourcePath, String targetPath) {
        super(parent, Messages.PopulateDialog_Populate);
        setStatusLineAboveButtons(true);
        setModalResizeStyle();
        this.connection = connection;
        this.model = new PopulateModel(sourcePath, targetPath, PopulateMethod.SOURCE_TARGET);
    }
    
    @Override
    protected void okPressed() {
    	// TODO Auto-generated method stub
    	super.okPressed();
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        preview = createButton(parent, IDialogConstants.DETAILS_ID,
                Messages.IntegrateDialog_Preview, false);
        preview.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                runPreview();
            }

        });
        createButton(parent, IDialogConstants.OK_ID,
                Messages.PopulateDialog_Populate, true);
        createButton(parent, IDialogConstants.CANCEL_ID,
                Messages.IntegrateDialog_Cancel, false);
    }

    private void startPreview() {
        progressBar.setEnabled(true);
        progressBar.setVisible(true);
        progressLabel.setText(Messages.PopulateDialog_GeneratingPopulatePreview);
        preview.setEnabled(false);
    }

    private void stopPreview() {
        progressBar.setEnabled(false);
        progressBar.setVisible(false);
        progressLabel.setText(""); //$NON-NLS-1$
        preview.setEnabled(true);
    }

    private void runPreview() {
        startPreview();
        P4Runner.schedule(new P4Runnable() {

            @Override
            public String getTitle() {
                return Messages.PopulateDialog_GeneratingPopulatePreview;
            }

            @Override
            public void run(IProgressMonitor monitor) {
            	List<String> sources = getSourcePaths();
            	List<String> targets = getTargetPaths();
            	List<IP4Resource> populated = new ArrayList<IP4Resource>();
            	for(int i=0;i<sources.size();i++){
            		IP4Resource[] previewed = connection.populate(
            				sources.get(i), targets.get(i), true, getDescription());
            		for(IP4Resource r: previewed){
            			populated.add(r);
            		}
            	}
            	final IP4Resource[] previewed =populated.toArray(new IP4Resource[0]);
                PerforceUIPlugin.syncExec(new Runnable() {

                    public void run() {
                        if (!displayArea.isDisposed()) {
                            stopPreview();
                            IntegrationPreviewDialog dialog = new IntegrationPreviewDialog(getShell(), previewed, Messages.PopulateDialog_PopulatePreview);
                            dialog.open();
                        }
                    }
                });
            }

        });

    }

	public String computeBasePath() {
		String prefix=""; //$NON-NLS-1$
		for(FileVersion v:model.getSourceRevisions()){
			if(StringUtils.isEmpty(prefix)){
				prefix=v.getPath();
			}else{
				String p = v.getPath();
				int l=Math.min(p.length(), prefix.length());
				int index=-1;
				boolean match=true;
				for(int i=0;i<l;i++){
					index=i;
					if(prefix.charAt(i)!=p.charAt(i)){
						match=false;
						break;
					}
				}
				if(match){
					prefix=p.substring(0,l);
				}else{
					if(index>=0){
						prefix=p.substring(0,index);
					}else{
						prefix=""; //$NON-NLS-1$
						break;
					}
				}
			}
		}
		if(prefix.length()==2 && prefix.equals("//")) //$NON-NLS-1$
			prefix=""; //$NON-NLS-1$
		basePath=prefix;
		return prefix;
	}
    
    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite c = (Composite) super.createDialogArea(parent);

        displayArea = new Composite(c, SWT.NONE);
        GridLayout daLayout = new GridLayout(1, false);
        displayArea.setLayout(daLayout);
        displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        createBranchGroup(displayArea);
        
        createDetailGroup(displayArea);
        
        createDescription(displayArea);
        
        createProgressGroup(displayArea);

        addListeners();
        
        branchCombo.setSelection(new StructuredSelection(model.getPopulateMethod()));
        
		SWTUtils.addContentListener(c, new Runnable() {
			
			public void run() {
				showStatus();
			}
		});
        
        return c;
    }


	private void createDescription(Composite parent) {
		Label separator = new Label(parent, SWT.SEPARATOR|SWT.HORIZONTAL);
		separator.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true, false));
		
        SWTUtils.createLabel(parent, Messages.PopulateDialog_Description);

        descriptionViewer = new DescriptionViewer(connection);
        descriptionViewer.createControl(parent, description);
        descriptionViewer.getDocument().addDocumentListener(new IDocumentListener() {

            public void documentChanged(DocumentEvent event) {
                description = descriptionViewer.getDocument().get();
            }

            public void documentAboutToBeChanged(DocumentEvent event) {

            }
        });
        final StyledText styledText = descriptionViewer.getViewer().getTextWidget();
        final GridData commentData = (GridData) styledText.getLayoutData();
        commentData.heightHint = P4UIUtils.computePixelHeight(descriptionViewer
                .getViewer().getTextWidget().getFont(), 5);
        styledText.setLayoutData(commentData);
    }

	private void createProgressGroup(Composite displayArea) {
        progressLabel = new Label(displayArea, SWT.LEFT);
        progressLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                false));

        progressBar = new ProgressBar(displayArea, SWT.INDETERMINATE
                | SWT.SMOOTH | SWT.HORIZONTAL);
        progressBar
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        progressBar.setEnabled(false);
        progressBar.setVisible(false);
		
	}

	private void addListeners() {

		branchCombo.addSelectionChangedListener(new ISelectionChangedListener() {
			
			public void selectionChanged(SelectionChangedEvent event) {
				PopulateMethod id=(PopulateMethod) SWTUtils.getSingleSelectedObject(branchCombo);
				model.setPopulateMethod(id);
				pageBook.showPageById(id);
				pageBook.setInput(model.getSourceRevisions());
			}
		});
			
		model.setPopulateMethod(PopulateMethod.SOURCE_TARGET);
	}

	protected void showStatus() {
		IStatus s= findFirstError();
		setMessage(s.getSeverity(), s.getMessage());		
	}

	private IStatus findFirstError() {
		if(model.getPopulateMethod()==PopulateMethod.SOURCE_TARGET){
			if(StringUtils.isEmpty(model.getTargetPath())){
				return ValidationStatus.error(Messages.PopulateDialog_Target_cannot_empty);
			}
			if(model.getSourceRevisions().size()==0){
				return ValidationStatus.error(Messages.PopulateDialog_Source_cannot_empty);
			}
			String prefix = computeBasePath();
			if(StringUtils.isEmpty(prefix)){
				return ValidationStatus.error(Messages.PopulateDialog_No_common_source_path);
			}
		}
		return ValidationStatus.ok();
	}

	private void createBranchGroup(Composite parent) {
		Composite group=new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(group);
		group.setLayout(new GridLayout(2, false));
        SWTUtils.createLabel(group, Messages.PopulateDialog_Branch_method);
        branchCombo = SWTUtils.createEnumCombo(group, PopulateMethod.class);
        branchCombo.setFilters(new ViewerFilter[]{new ViewerFilter() { // hide branch, for future dev
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if(element==PopulateMethod.BRANCH)
					return false;
				return true;
			}
		}});
	}

	private void createDetailGroup(Composite parent) {
		pageBook = new PageBook(parent, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(pageBook);
		new SourceTargetPage(pageBook, SWT.NONE, model);
		new BranchPage(pageBook, SWT.NONE);
	}

	private TableViewer createSourceTable(Composite parent) {
		TableViewer viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		createColumns(parent, viewer);
		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		Sorter comparator= new Sorter();
		viewer.setComparator(comparator);

		viewer.setContentProvider(new ArrayContentProvider());
		return viewer;
	}

	// This will create the columns for the table
	private void createColumns(final Composite parent, final TableViewer viewer) {
		String[] titles = { "", Messages.PopulateDialog_Path, Messages.PopulateDialog_Revision}; //$NON-NLS-1$
		int[] bounds = { 50, 300, 100 };

		// First column is image
		TableViewerColumn col = createTableViewerColumn(viewer, titles[0],
				bounds[0], 0);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Image getImage(Object element) {
				FileVersion r = (FileVersion) element;
				if (r.path.endsWith("...")) //$NON-NLS-1$
					return getSharedImage(ISharedImages.IMG_OBJ_FOLDER);
				else
					return getSharedImage(ISharedImages.IMG_OBJ_FILE);
			}
			@Override
			public String getText(Object element) {
				return null;
			}
		});

		// Second column is for path
		col = createTableViewerColumn(viewer, titles[1], bounds[1], 1);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				FileVersion r = (FileVersion) element;
				return r.path;
			}
		});

		// Now the revision
		col = createTableViewerColumn(viewer, titles[2], bounds[2], 2);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				FileVersion r = (FileVersion) element;
				return r.revision;
			}
		});
	}

	protected Image getSharedImage(String id) {
		IWorkbench workbench = PlatformUI.getWorkbench();
		ISharedImages images = workbench.getSharedImages();
		return images.getImage(id);
	}

	private TableViewerColumn createTableViewerColumn(final TableViewer viewer,
			String title, int bound, final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer,
				SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		
        column.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	Sorter comparator = (Sorter)viewer.getComparator();
                comparator.setColumn(colNumber);
                int dir = comparator.getDirection();
                viewer.getTable().setSortDirection(dir);
                viewer.getTable().setSortColumn(column);
                viewer.refresh();
            }
        });

		return viewerColumn;
	}
	
	/**
	 * 
	 * @param target //depot/folder/...
	 * @param source //depot/a/b/f1@123
	 * @param base //depot/a/
	 * @return //depot/folder/b/f1@123
	 */
	private String adjustTargetPath(String target, String source, String base) {
		String suffix = source.substring(base.length());
		if(StringUtils.isEmpty(suffix)){
			return target;
		}

		String prefix = target.endsWith("...")?target.substring(0, target.length()-3):target; //$NON-NLS-1$
		if(suffix.endsWith("/")||prefix.startsWith("/")) //$NON-NLS-1$ //$NON-NLS-2$
			return prefix+suffix;
		else
			return prefix+"/"+suffix; //$NON-NLS-1$
	}

    public String getBasePath() {
		return basePath;
	}

	public String getDescription() {
		return description;
	}

	public List<String> getSourcePaths() {
		List<String> list=new ArrayList<String>();
		for(FileVersion ver: model.getSourceRevisions()){
			list.add(ver.toString());
		}
		return list;
	}

	public String getTargetPath() {
		return model.targetPath;
	}

	public List<String> getTargetPaths(){
    	List<String> sourcePaths=getSourcePaths();
    	String targetPath = getTargetPath();
    	String basePath = getBasePath();
    	List<String> targetPaths = new ArrayList<String>();
    	if(sourcePaths.size()==1){
    		targetPaths.add(targetPath);
    		return targetPaths;
    	}
    	
    	for(int i=0;i< sourcePaths.size();i++){
    		String sourcePath=sourcePaths.get(i);        				
    		String newTagetPath=adjustTargetPath(targetPath,sourcePath,basePath);
    		targetPaths.add(newTagetPath);
    	}
    	return targetPaths;
	}
}
