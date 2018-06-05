package com.perforce.team.ui.streams;

import java.io.Serializable;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.progress.UIJob;

import com.perforce.p4java.core.IStreamSummary;
import com.perforce.team.core.IConstants;
import com.perforce.team.core.p4java.IP4CommandListener;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Stream;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.core.p4java.P4Stream;
import com.perforce.team.core.p4java.P4Stream.StreamCache;
import com.perforce.team.core.p4java.P4Stream.VirturalRoot;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceLabelProvider;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.dialogs.DialogUtils;
import com.perforce.team.ui.submitted.SubmittedChangelistTable;
import com.perforce.team.ui.submitted.SubmittedChangelistTable.TableConfig;
import com.perforce.team.ui.viewer.FilterViewer;
import com.perforce.team.ui.views.SessionManager;

import static com.perforce.team.core.p4java.IP4CommandListener.P4CommandAdapter;

/**
 * Streams tree viewer showing the stream list in flat or tree structure.
 * <p/>
 * UI also include a filter panel on top and a detail panel at bottom.
 * 
 * @author ali
 *
 */
public class FilteredStreamsViewer extends FilterViewer implements IDoubleClickListener{

    private WritableValue filterModel;
    private IObservableValue selectionValue; // viewerSelection: IStream

    private Composite filterAndViewerArea;
    private SashForm sash;
    private StreamsFilterWidget filterWidget;

    private TreeViewer viewer;
    private SteamsViewerSorter<Object> sorter=new SteamsViewerSorter<Object>(COL_NAME,SORT_ASCEND);
    private IP4Connection connection;

    private StreamCache summaryCache = new StreamCache();
    private List<IP4Stream> summaryList;
    private VirturalRoot treeRoot;

    private boolean displayDetails;

    private IWorkbenchPartSite site;
	private StreamDetailPanel detailPanel;

	private IP4CommandListener handler = new P4CommandAdapter() {
		private Map<Integer, String> map=new HashMap<Integer, String>();
		public void error(final int id, final String line) {
			P4UIUtils.getDisplay().asyncExec(new Runnable() {
				public void run() {
					if(map.containsKey(id)){
						String cmd = map.remove(id);
						MessageDialog.openError(null, com.perforce.team.ui.Messages.P4Command_Error+IConstants.SPACE+IConstants.SQUOTE+cmd+IConstants.SQUOTE, line);
					}
				}
			});
		}
		public void command(int id, String line) {
			map.put(id, line);
		};
	};

    public FilteredStreamsViewer(IWorkbenchPartSite site) {
    	this.site=site;
	}

	public void createPartControl(Composite parent) {
        // Model
        filterModel = new WritableValue(new StreamsFilterModel(),
                StreamsFilterModel.class);

        filterModel.addChangeListener(new IChangeListener() {

            public void handleChange(ChangeEvent event) {
                refreshInput(true);
            }
        });

        // TODO: add help
        // PlatformUI.getWorkbench().getHelpSystem()
        // .setHelp(parent, IHelpContextIds.SUBMITTED_VIEW);

        sash = DialogUtils.createSash(parent);

        filterAndViewerArea = new Composite(sash, SWT.NONE);
        GridLayoutFactory.swtDefaults().margins(0, 0).spacing(5, 0)
                .numColumns(1).equalWidth(true).applyTo(filterAndViewerArea);
        filterAndViewerArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
                true, true));
        {
            filterComposite = filterWidget = new StreamsFilterWidget(
                    filterAndViewerArea, SWT.NONE, filterModel,true);

            viewer = createViewer(filterAndViewerArea);
            GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer.getTree());

            selectionValue = ViewersObservables.observeSingleSelection(viewer);
            selectionValue.addValueChangeListener(new IValueChangeListener() {
                public void handleValueChange(ValueChangeEvent event) {
                    updateDetail();
                }
            });
        }

        detailPanel=new StreamDetailPanel(sash, SWT.V_SCROLL | SWT.BORDER);

    }

	public Composite getControl() {
        return sash;
    }

	public TreeViewer getTreeViewer(){
	    return viewer;
	}
	
	public void enableAutoUpdate(boolean enable){
	    filterWidget.enableAutoUpdate(enable);
	}
	
    private void updateSash() {
        if (needDisplayDetails()) {
            sash.setMaximizedControl(null);
            // updateDetail
            updateDetail();
        } else {
            sash.setMaximizedControl(filterAndViewerArea);
        }
    }

    private void updateDetail() {
        detailPanel.setInput(getSelectedStream());
    }
    
    IP4Stream getSelectedStream(){
        Object value = selectionValue.getValue();
        if (value instanceof IP4Stream) {
            return (IP4Stream)value;
        }
        return null;

    }

    private boolean needDisplayDetails() {
        return displayDetails;
    }

    public void setConnection(IP4Connection conn) {
        this.connection = conn;
        filterWidget.setConnection(conn);
    }

    public void clearCache(IP4Connection connection) {
        summaryCache.clear(connection);
    }

    public void setFocus() {
        this.filterWidget.setFocus();
    }

    public void showDisplayDetails(boolean checked) {
        displayDetails = checked;
        updateSash();
    }

    public void refreshInput(final boolean retrieve) {
        
        if(!connectionOK(connection)){
            return;
        }
        
    	final IP4Connection curConnection=connection;
    	
        filterWidget.enableFilters(false);

        viewer.setInput(loading);
        viewer.getTree().setItemCount(1);
        final boolean showList = StreamsViewCommandHelper.showList();
        final boolean showTree = StreamsViewCommandHelper.showTree();
        final StreamsFilterModel filter = getFilterModel();

        P4Runner.schedule(new P4Runnable() {

            @Override
            public String getTitle() {
                return Messages.StreamsTreeViewer_LoadingStreams;
            }

            @Override
            public void run(IProgressMonitor monitor) {
                if (curConnection != connection) {
                    return;
                }
                initSummaryCache(connection, filter, showList, retrieveCount, retrieve);

                if(showList)
                    Collections.sort(summaryList, sorter);

                if (showTree) {
                    treeRoot = P4Stream.constructTree(summaryList, summaryCache);
                    sortTree(treeRoot, sorter);
                }

                UIJob job = new UIJob(Messages.FilteredStreamsViewer_UpdateStreamsJob) {

                    @Override
                    public IStatus runInUIThread(IProgressMonitor monitor) {
                        if (curConnection != connection) {
                            return Status.CANCEL_STATUS;
                        }
                        
                        if (okToUse()) {
                            if (showList) {
                                viewer.setInput(summaryList);
                                viewer.getTree().setItemCount(
                                        summaryList.size());
                            } else if (showTree) {
                                viewer.setInput(treeRoot);
                                viewer.getTree().setItemCount(
                                        treeRoot.getChildren().size());
                            }
                            updateMatchLabel(summaryList.size());
                            updateSash();

                            // Calling update here loads the virtual tree
                            // nodes, so the selection can be set successfully
                            expand(); 
                            filterWidget.enableFilters(true);
                        }
                        return Status.OK_STATUS;
                    }

                };
                job.schedule();
            }
        });

    }

    protected void sortTree(IP4Stream treeRoot,
            SteamsViewerSorter<Object> sorter) {
        Collections.sort(treeRoot.getChildren(),sorter);
        for(IP4Stream node: treeRoot.getChildren()){
            sortTree(node,sorter);
        }
    }

    protected void initSummaryCache(IP4Connection conn,
            StreamsFilterModel filter, boolean showList, int retrieveCount, boolean retrieve) {
        try {
        	if(retrieve || summaryList==null){
        		try {
        			P4Workspace.getWorkspace().addCommandListener(handler);
        			summaryList = conn.getFilteredStreams(filter.isShowUnloadedOnly(), filter.getPaths(), filter.getFilterString(), retrieveCount);
				} finally {
					P4Workspace.getWorkspace().removeCommandListener(handler);
				}
        	}

            if(!showList && summaryList!=null){
        	    summaryCache.add(conn, summaryList);
                if(!StringUtils.isEmpty(filter.getFilterString())){
                    completeSummaryCache(summaryList, summaryCache);
                }
        	}
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

	private List<IP4Stream> completeSummaryCache(
            List<IP4Stream> subList, StreamCache cache) {
        List<IP4Stream> fullList=new ArrayList<IP4Stream>();
        fullList.addAll(subList);
        for(IP4Stream sum: subList){
            List<IP4Stream> path=fetchParent(sum,cache);
            fullList.addAll(path);
        }
        return fullList;
    }

    private List<IP4Stream> fetchParent(IP4Stream sum, StreamCache cache) {
        List<IP4Stream> list=new ArrayList<IP4Stream>();
        try {
            String parentPath = sum.getStreamSummary().getParent();
            if(StringUtils.isEmpty(parentPath)||parentPath.equals("none")){ //$NON-NLS-1$
                return list;
            }
            
            IP4Stream parent=cache.get(sum.getConnection(), parentPath);
            if(parent==null){
                parent=sum.getConnection().getStream(parentPath);
                if(parent!=null){
	                cache.add(parent);
	                list.add(parent);
                }
            }
            if(parent!=null){
	            List<IP4Stream> plist = fetchParent(parent,cache);
	            list.addAll(plist);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    StreamsFilterModel getFilterModel() {
    	return filterWidget.computeModel();
    }

    // ////////////////// Viewer ///////////////
    public static final String COLUMN_SIZES = "com.perforce.team.ui.streamsTreeColumns";  //$NON-NLS-1$
    // column index
    private static final int COL_NAME = 0;
    private static final int COL_ROOT = 1;
    private static final int COL_PARENT = 2;
    private static final int COL_OWNER = 3;
    private static final int COL_TYPE = 4;
    private static final int COL_DESC = 5;
    private static final int COL_ACCESS = 6;
    private static final int COL_UPDATE = 7;
    
    // sort order
    private static final String SORT_ORDER = "SORT_ORDER";//$NON-NLS-1$
    private static final boolean SORT_ASCEND = true;
	private static final String TREE_VIEWER_CONTEXT_MENU = "tree"; //$NON-NLS-1$
    
    public static Object loading = new Object();

    private Image loadingImage;

    private Link showMore;
    private Label matchLabel;
    private int retrieveCount=0;

    public TreeViewer createViewer(Composite parent) {
    	Composite comp=new Composite(parent, SWT.NONE);
    	GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).grab(true, false).applyTo(comp);
    	GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(comp);
    	
    	matchLabel = new Label(comp, SWT.None);
    	GridDataFactory.swtDefaults().align(SWT.END, SWT.FILL).grab(false,false).applyTo(matchLabel);
    	
        showMore = new Link(comp, SWT.PUSH);
        showMore.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                showMore();
            }

        });
        showMore.setLayoutData(new GridData(SWT.END, SWT.FILL, false, false));
        retrieveCount = getMaxStreamsPreference();
        updateMoreButton(false);

        TreeViewer viewer = new TreeViewer(parent, SWT.VIRTUAL | SWT.BORDER
                | SWT.FULL_SELECTION);

        initTree(viewer);
        return viewer;
    }

    private void initTree(TreeViewer viewer) {
        viewer.setUseHashlookup(true);
        final Tree tree = viewer.getTree();
        tree.setHeaderVisible(true);
        tree.setLinesVisible(false);

        TableConfig config = SubmittedChangelistTable.getConfig();
        tree.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                saveColumnSizes(tree);
            }

        });

        TableLayout layout = new TableLayout();
        tree.setLayout(layout);

        // Add all columns to table
        TreeColumn nameCol = addColumn(tree, COL_NAME, Messages.FilteredStreamsViewer_StreamCol, 3, config.redrawOnResize);
        addColumn(tree, COL_ROOT, Messages.FilteredStreamsViewer_StreamRootCol, 5, config.redrawOnResize);
        addColumn(tree, COL_PARENT, Messages.FilteredStreamsViewer_ParentCol, 5, config.redrawOnResize);
        addColumn(tree, COL_OWNER, Messages.FilteredStreamsViewer_OwnerCol, 10, config.redrawOnResize);
        addColumn(tree, COL_TYPE, Messages.FilteredStreamsViewer_TypeCol, 20, config.redrawOnResize);
        addColumn(tree, COL_DESC, Messages.FilteredStreamsViewer_DescCol, 30, config.redrawOnResize);
        addColumn(tree, COL_ACCESS, Messages.FilteredStreamsViewer_AccessCol, 10, config.redrawOnResize);
        addColumn(tree, COL_UPDATE, Messages.FilteredStreamsViewer_UpdateCol, 10, config.redrawOnResize);
        
        // default sorting column
        tree.setSortColumn(nameCol);
        tree.setSortDirection(SWT.UP);

        Map<String, Integer> columnSizes = loadColumnSizes();

        for (TreeColumn column : tree.getColumns()) {
            int width = 200;
            if (columnSizes.containsKey(column.getText())) {
                int size = columnSizes.get(column.getText()).intValue();
                if (size > 0) {
                    width = size;
                }
            }
            layout.addColumnData(new ColumnPixelData(width, true));
        }

        viewer.setContentProvider(new StreamsLazyContentProvider());
        viewer.setLabelProvider(new StreamLabelProvider());
    
        addListeners(viewer);
        
        createImages();
        
        registerContextMenu(viewer);
    }

    private void addListeners(final TreeViewer viewer) {
        viewer.addDoubleClickListener(this);
	}

	private void registerContextMenu(TreeViewer viewer) {
		MenuManager menuMgr = new MenuManager(IConstants.EMPTY_STRING,TREE_VIEWER_CONTEXT_MENU);
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
                fillContextMenu(manager);
			}
        });
        
        // Create menu.
        Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		
		// Register menu for extension.
		if(this.site!=null){
    		this.site.registerContextMenu(StreamsView.VIEW_ID+"."+TREE_VIEWER_CONTEXT_MENU, menuMgr, viewer); //$NON-NLS-1$
    		this.site.setSelectionProvider(viewer);
		}
	}

	protected void fillContextMenu(IMenuManager manager) {
	}

	private void createImages() {
        loadingImage = PerforceUIPlugin.getPlugin()
                .getImageDescriptor(IPerforceUIConstants.IMG_LOADING)
                .createImage();
    }

    private TreeColumn addColumn(final Tree tree, final int colno, String title,
            int weight, boolean redrawOnResize) {
        final TreeColumn col = new TreeColumn(tree, SWT.NONE);
        if (redrawOnResize) {
            col.addListener(SWT.Resize, new Listener() {

                public void handleEvent(Event event) {
                    tree.redraw();
                }
            });
        }
        col.setResizable(true);
        col.setText(title);
        col.setData(SORT_ORDER, SORT_ASCEND);
        col.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                boolean order = (Boolean) col.getData(SORT_ORDER);
                sorter=new SteamsViewerSorter<Object>(colno, !order);
                tree.setSortColumn(col);
                tree.setSortDirection(order?SWT.DOWN:SWT.UP);
                col.setData(SORT_ORDER, !order);
                refreshInput(false);
            }
        });
        return col;
    }

    protected void saveColumnSizes(Tree tree) {
        SessionManager.saveColumnPreferences(tree, COLUMN_SIZES);
    }

    private Map<String, Integer> loadColumnSizes() {
        return SessionManager.loadColumnSizes(COLUMN_SIZES);
    }

    private void updateMoreButton(boolean layout) {
        if (showMore != null && !showMore.isDisposed()) {
            int max = getMaxStreamsPreference();
            if (max == -1) {
                showMore.setText(Messages.StreamsTreeViewer_ShowMore);
                showMore.setEnabled(false);
            } else {
                showMore.setText(MessageFormat.format(
                        Messages.StreamsTreeViewer_ShowNumMore, max));
                showMore.setEnabled(true);
            }
            if (layout) {
                showMore.getParent().layout(new Control[] { showMore });
            }
        }
    }
    
    private int getMaxStreamsPreference() {
        return PerforceUIPlugin.getPlugin().getPreferenceStore().getInt(
                IPerforceUIConstants.PREF_RETRIEVE_NUM_STREAMS);
    }

    /**
     * Refresh the retrieve count with the latest value from the pref store
     */
    public void refreshRetrieveCount() {
        retrieveCount = getMaxStreamsPreference();
    }

    /**
     * Update show more link
     * 
     */
    public void updateMoreLink() {
        refreshRetrieveCount();
        updateMoreButton(true);
        refreshInput(true);
    }

    /**
     * Shows the next amount of streams
     */
    public void showMore() {
        if (retrieveCount != -1) {
            retrieveCount += getMaxStreamsPreference();
        }
        refreshInput(true);
    }

    public void updateMatchLabel(int num) {
        String format = num>1?Messages.FilteredStreamsViewer_Matches:Messages.FilteredStreamsViewer_Match;
    	matchLabel.setText(MessageFormat.format(format, num));
    	matchLabel.getParent().layout(new Control[]{matchLabel,showMore});
    	matchLabel.getParent().getParent().layout(new Control[]{matchLabel.getParent()});
    }
    
    /**
     * Submitted tree table content provider
     */
    private class StreamsLazyContentProvider implements
            ILazyTreeContentProvider {

        /**
         * @see org.eclipse.jface.viewers.IContentProvider#dispose()
         */
        public void dispose() {
        }

        /**
         * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
         *      java.lang.Object, java.lang.Object)
         */
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        /**
         * @see org.eclipse.jface.viewers.ILazyTreeContentProvider#getParent(java.lang.Object)
         */
        public Object getParent(Object element) {
            return null;
        }

        /**
         * @see org.eclipse.jface.viewers.ILazyTreeContentProvider#updateChildCount(java.lang.Object,
         *      int)
         */
        public void updateChildCount(Object element, int currentChildCount) {// used by expand/collapse
            if(element instanceof IP4Stream){
                int size = ((IP4Stream)element).getChildren().size();
                if (size != currentChildCount) {
                    viewer.setChildCount(element, size);
                }

            }
        }

        /**
         * @see org.eclipse.jface.viewers.ILazyTreeContentProvider#updateElement(java.lang.Object,
         *      int)
         */
        public void updateElement(Object parent, int index) {
            if (parent == loading && index == 0) {
                viewer.replace(parent, 0, loading);
            } else if (parent == summaryList && summaryList != null) {
                if ((index >= 0) && (index < summaryList.size())) {
                    IP4Stream sum = summaryList.get(index);
                    viewer.replace(parent, index, sum);
                    viewer.setChildCount(sum, 0);
                }

            } else if (parent instanceof IP4Stream) {
            	IP4Stream node = (IP4Stream) parent;
                if (index >= 0 && index < node.getChildren().size()) {
                	IP4Stream child = node.getChildren().get(index);
                    viewer.replace(node, index, child);
                    viewer.setChildCount(child, child.getChildren().size());
                }
            }
        }
    }

    /**
     * Submitted label provider class
     */
    private class StreamLabelProvider extends PerforceLabelProvider {
    	SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");

        /**
         * Creates the submitted label provider and initializes the images
         */
        public StreamLabelProvider() {
            super(false);
        }

        /**
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
         *      int)
         */
        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            if (columnIndex == 0) {
                if (element == loading) {
                    return loadingImage;
                }
                if(element instanceof IP4Stream){
                    return super.getColumnImage((IP4Stream) element, columnIndex);
                }
            }
            return super.getColumnImage(element, columnIndex);
        }

        /**
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
         *      int)
         */
        @Override
        public String getColumnText(Object element, int columnIndex) {
            IStreamSummary summary = null;
            if (element instanceof IP4Stream) {
                summary = ((IP4Stream) element).getStreamSummary();
            }

            if (summary != null) {
                switch (columnIndex) {
                case COL_NAME:
                    return summary.getName();
                case COL_ROOT:
                    return summary.getStream();// P4UIUtils.formatLabelDate(list.getDate());
                case COL_PARENT:
                    return summary.getParent();
                case COL_OWNER:
                    return summary.getOwnerName();
                case COL_TYPE:
                    return summary.getType().name().toLowerCase();// P4CoreUtils.removeWhitespace(list.getDescription());
                case COL_DESC:
                    return summary.getDescription();
                case COL_ACCESS:
                	Date access=summary.getAccessed();
                	return access!=null?formatter.format(access):IConstants.EMPTY_STRING;
                case COL_UPDATE:
                	Date update=summary.getUpdated();
                	return update!=null?formatter.format(update):IConstants.EMPTY_STRING;
                default:
                    return super.getColumnText(element, columnIndex);
                }
            }

            if (columnIndex == 0 && element == loading) {
                return Messages.FilteredStreamsViewer_Loading;
            } else {
                return ""; //$NON-NLS-1$
            }
        }
        
        public Font getFont(Object element) {
          if (element instanceof IP4Stream) {
              IP4Stream summary = (IP4Stream) element;
              if(connection!=null){
            	  String s = connection.getClient().getStream();
            	  IStreamSummary ss = summary.getStreamSummary();
            	  if(s!=null && ss!=null && s.equals(ss.getStream())){
                	  return JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);
            	  }
              }
              if(!summaryList.contains(summary)){
                  return JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT);
              }
          }
          return null;
        }
        
        public Color getBackground(Object element) {
            if (element instanceof IP4Stream) {
                IP4Stream summary = (IP4Stream) element;
                if(!summaryList.contains(summary)){
                    return P4UIUtils.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND);
                }
            }
            return null;
        }
        
        public Color getForeground(Object element) {
            if (element instanceof IP4Stream) {
                IP4Stream summary = (IP4Stream) element;
                if(!summaryList.contains(summary)){
                    return P4UIUtils.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND);
                }
            }
            return null;
        }
    }

    private static class SteamsViewerSorter<T> implements Comparator<T>, Serializable{
        
		private static final long serialVersionUID = -2467687937528407637L;
		private int column;
        private boolean ascend;

        public SteamsViewerSorter(int column, boolean ascend) {
            this.column=column;
            this.ascend=ascend;
        }
        
        public int compare(T e1, T e2) {
            IStreamSummary s1=null;
            IStreamSummary s2=null;
            
            if(e1 instanceof IStreamSummary && e2 instanceof IStreamSummary){
                s1=(IStreamSummary) e1;
                s2=(IStreamSummary) e2;
            }else if(e1 instanceof IP4Stream && e2 instanceof IP4Stream){
                s1=((IP4Stream)e1).getStreamSummary();
                s2=((IP4Stream)e2).getStreamSummary();
            }
            
            int result=0;
            if(s1!=null && s2!=null){
                if(!ascend){
                    IStreamSummary s0=s1;
                    s1=s2;
                    s2=s0;
                }

                switch(column){
                case COL_NAME:
                    result=s1.getName().compareTo(s2.getName());
                    break;
                case COL_ROOT:
                    result=s1.getStream().compareTo(s2.getStream());
                    break;
                case COL_PARENT:
                    result=s1.getParent().compareTo(s2.getParent());
                    break;
                case COL_OWNER:
                    result=s1.getOwnerName().compareTo(s2.getOwnerName());
                    break;
                case COL_TYPE:
                    result=s1.getType().name().compareTo(s2.getType().name());
                    break;
                case COL_DESC:
                    result=s1.getDescription().compareTo(s2.getDescription());
                    break;
                case COL_ACCESS:
                	if(s1.getAccessed()!=null && s2.getAccessed()!=null){
                		result=s1.getAccessed().compareTo(s2.getAccessed());
                	}
                    break;
                case COL_UPDATE:
                	if(s1.getUpdated()!=null && s2.getUpdated()!=null){
                		result=s1.getUpdated().compareTo(s2.getUpdated());
                	}
                    break;
                }
            }
            return result;
        }
        
    }
    
    public boolean okToUse() {
        return P4UIUtils.okToUse(viewer);
    }

    public void clearFilter() {
        StreamsFilterModel model = getFilterModel();
        if (model != null)
            model.reset();

        filterWidget.reset();

    }

	public IP4Connection getConnection() {
		return this.connection;
	}

	public void expand() {
	    boolean expand = PerforceUIPlugin.getPlugin().getPreferenceStore().getBoolean(StreamsViewControl.TREE_EXPAND);
	    if(expand)
	        viewer.expandAll();
	    else{
	        viewer.expandToLevel(2); // make sure virtual nodes are materialized.
	        viewer.collapseAll();
	    }
	}

    protected boolean connectionOK(IP4Connection connection) {
        return connection != null && !connection.isOffline()
                && !connection.isDisposed();
    }
    
    public void doubleClick(DoubleClickEvent event) {
        if (event.getSelection() instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection) event
                    .getSelection();
            Object element = selection.getFirstElement();
            if (element instanceof IP4Stream) {
                EditStreamAction edit = new EditStreamAction();
                edit.selectionChanged(null, new StructuredSelection(
                        element));
                edit.doubleClick(null);
                viewer.refresh(element);
                viewer.setSelection(null);
                viewer.setSelection(new StructuredSelection(element));
            }
        }
    }

}
