package com.perforce.team.ui.streams;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import com.perforce.team.core.IConstants;
import com.perforce.team.ui.SWTUtils;

/**
 * A suggest box which allow google style searching. 
 * <p/> 
 * Usage:
 * 
 *      SuggestBox instance = new SuggestBox(shell, SWT.NONE, exampleProvider);
 *      instance.getModel().addValueChangeListener(new IValueChangeListener() {
 *          public void handleValueChange(ValueChangeEvent event) {
 *              System.out.println("model="+event.getObservableValue().getValue());
 *          }
 *      });
 * 
 * @author ali
 * 
 */
public class SuggestBox extends Composite {
	public interface ISuggestProvider{
		void updateChildCount(Object element, int currentChildCount,
				TreeViewer viewer);
		void updateElement(Object parent, int index, TreeViewer viewer);
		String getColumnText(Object element, int columnIndex);
		
		List<Object> fetchElement(String name);
		Image getColumnImage(Object element, int columnIndex);
		String getFilterText(Object selectedObj);
        void inputChanged(Viewer viewer, Object oldInput, Object newInput);

        boolean hasTooltip();
        String getTooltipTitle(SelectionModel value);
        String getTooltips(SelectionModel value);
        
        Class<?> getElementType();
	}
	
	public static class SelectionModel{
	    private String text;
	    private Object selection;
	    public SelectionModel(String text, Object sel){
	        this.setText(text);
	        this.setSelection(sel);
	    }
        public String getText() {
            return text;
        }
        public void setText(String text) {
            this.text = text;
        }
        public Object getSelection() {
            return selection;
        }
        public void setSelection(Object selection) {
            this.selection = selection;
        }
        @Override
        public String toString() {
            return (text==null?"null":text)+", "+(selection==null?"null":selection.toString()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
	}

    private static final boolean DEBUG = false;

    private static void debugPrint(String msg) {
        if (DEBUG)
            System.out.println(msg);
    }

    private static final int VISIBLE_LINE_NUM = 10;
    
    WritableValue m_model=new WritableValue(SWTObservables.getRealm(this.getDisplay()), null, null); // type of SelectionModel

    private Text m_text;
    private Button m_dropDown;

    private ChoiceDialog m_dialog;

	private ISuggestProvider m_provider;
	public void setSuggestProvider(ISuggestProvider provider){
	    if(m_provider!=provider){
	        m_provider=provider;
	        refresh();
	    }
	}
	
	private String m_prevText="";//$NON-NLS-1$

    private ModifyListener m_modifyListener=new ModifyListener() {
        public void modifyText(ModifyEvent e) {
            popupAndRepositionDialog(m_text.getText());
        }
    };

    public SuggestBox(Composite parent, int style, ISuggestProvider provider) {
        super(parent, style);
        this.m_provider=provider;
        createControl(this);

        addListeners();
    }
    
    /**
     * Value selected (Observable). Type of SelectionModel
     * @return observable selected value
     */
    public WritableValue getModel() {
        return m_model;
    }

    @Override
    public boolean setFocus() {
        return m_text.setFocus();
    }
    
    private void createControl(Composite parent) {
        if(SWTUtils.isReadonlyStyle(this)){
            GridLayoutFactory.swtDefaults().numColumns(1).margins(0, 5).spacing(0, 5).applyTo(parent);
            
            m_text = SWTUtils.createText(parent, 1, SWT.BORDER|SWT.READ_ONLY);
            GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
                    .grab(true, false).applyTo(m_text);
            setTooltip(m_text);
        }else{
            FormLayout layout = new FormLayout ();
            parent.setLayout(layout);
            
            m_text = new Text(parent, SWT.BORDER);
            m_dropDown = new Button(parent, SWT.SINGLE|SWT.ARROW|SWT.DOWN);
            FormData dropDownData = new FormData ();
            m_dropDown.setLayoutData(dropDownData);
            dropDownData.right = new FormAttachment(100,0);
            dropDownData.top = new FormAttachment (0, 5);	
            dropDownData.bottom = new FormAttachment (100, -5);	


            FormData textData = new FormData ();
            m_text.setLayoutData(textData);
            textData.left = new FormAttachment (0);
        	textData.right= new FormAttachment (m_dropDown, 0, SWT.RIGHT);
        	textData.top = new FormAttachment (0, 5);	
        	textData.bottom = new FormAttachment (100, -5);	

        	setTooltip(m_text);

            SWTUtils.decorate(this, SWT.TOP|SWT.LEFT);
            SWTUtils.updateDecoration(this, ValidationStatus.info(Messages.StreamsFilterWidget_TypeToFilterChoices));
        }
        
        if(parent.getParent()!=null) // on certain ws, like rhel, labels are cutoff sometime.
            parent.getParent().layout(true);

    }

    private void setTooltip(Text control) {

        if(m_provider!=null && m_provider.hasTooltip()){
            new NameValueToolTip(control,ToolTip.NO_RECREATE,true) {
                
                @Override
                protected String getTitle() {
                    return m_provider.getTooltipTitle((SelectionModel) m_model.getValue());
                }
                
                @Override
                protected String getPairString() {
                    return m_provider.getTooltips((SelectionModel) m_model.getValue());
                }
            }.activate();
        }
    }

    private void addListeners() {
        m_text.addModifyListener(m_modifyListener);
        m_text.addListener(SWT.KeyDown, new Listener() {
            public void handleEvent(Event e) {
                switch (e.type) {
                case SWT.KeyDown:
                    if (e.keyCode == SWT.ARROW_UP || e.keyCode == SWT.ARROW_DOWN) {
                        e.doit = false;
                        popupAndRepositionDialog(m_text.getText());
                    }
                    break;
                }
            }
        });
        if(m_dropDown!=null){
            m_dropDown.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    popupAndRepositionDialog("");
                }
            });
        }
    }

    private void popupAndRepositionDialog(String filter) {
        if (isDialogVisible()) {
            return;
        }

        if (m_dialog == null) {
            m_dialog = new ChoiceDialog(m_text.getShell(), SWT.NO_TRIM | SWT.SHEET);
        }

        final Rectangle bound = m_text.getParent().getBounds();
        Point pt = m_text.toDisplay(-m_text.getBorderWidth(),
                -m_text.getBorderWidth());
        bound.x = pt.x-2;
        bound.y = pt.y;
        m_dialog.open(bound,filter);

    }

    protected boolean isDialogVisible() {
        if (m_dialog != null) {
            return true;
        }
        return false;
    }

    private void closeDialog() {
        if(isDialogVisible()){

            if(m_dialog.fSelectedObj==null && !StringUtils.isEmpty(m_dialog.buffer) && !m_dialog.buffer.startsWith("//")){
                if(m_dialog.fTreeViewer.getTree().getItemCount()>0){
                    Object data = m_dialog.fTreeViewer.getTree().getItem(0).getData();
                    if(m_provider.getElementType().isInstance(data)){
                        m_dialog.fSelectedObj=data;
                    }else{
                      List<Object> matches = m_provider.fetchElement(m_dialog.buffer);
                      if(matches.size()>0)
                          m_dialog.fSelectedObj=matches.get(0);
                    }
                }else{
                    m_dialog.fSelectedObj=null;
                }
            }

            if(!StringUtils.isEmpty(m_dialog.buffer) && m_dialog.buffer.startsWith("//")){
                List<Object> datas=m_provider.fetchElement(m_dialog.buffer);
                if(datas.size()>0)
                    m_dialog.fSelectedObj=datas.get(0);
                else
                    m_dialog.fSelectedObj=null;
            }
            
            m_model.setValue(new SelectionModel(m_dialog.buffer, m_dialog.fSelectedObj));

            String newText = "";//$NON-NLS-1$
            if(m_dialog.fSelectedObj!=null){
                String filterText = m_provider.getFilterText(m_dialog.fSelectedObj);
                if(filterText!=null)
                    newText=filterText;
	            
            }else if(!StringUtils.isEmpty(m_dialog.buffer) && m_dialog.buffer.startsWith("//")){
                newText=m_dialog.buffer;
            }
            if(!m_text.getText().equals(newText)){
                m_text.setText(""); //$NON-NLS-1$
                m_text.append(newText);
            }
            
            m_prevText=newText;
            
            m_dialog.getShell().close();
            m_text.setFocus();
            m_dialog = null;
        }
    }

    public static void main(String[] args) {
        ISuggestProvider exampleProvider=new ISuggestProvider() {
            public void inputChanged(Viewer viewer, Object oldInput,
                    Object newInput) {
                // TODO Auto-generated method stub
                
            }
            public void updateElement(Object parent, int index,
                    TreeViewer viewer) {
                if (parent instanceof List) {
                    List<?> list = (List<?>) parent;
                    if ((index >= 0) && (index < list.size())) {
                        Object obj = list.get(index);
                        viewer.replace(parent, index, obj);
                        viewer.setChildCount(obj, 0);
                    }
                }
            }
            
            public void updateChildCount(Object element, int currentChildCount,
                    TreeViewer viewer) {
                if(element instanceof List){
                    @SuppressWarnings("rawtypes")
                    int size = ((List)element).size();
                    if (size != currentChildCount) {
                        viewer.setChildCount(element, size);
                    }
                }
            }
            
            public String getColumnText(Object element, int columnIndex) {
                return element==null?"null":element.toString(); //$NON-NLS-1$
            }
            
            public List<Object> fetchElement(String text) {
                try {
                    Thread.sleep(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                List<Object> list=new ArrayList<Object>();
                for(int i=0;i<10;i++){
                    list.add(text+"0"+i); //$NON-NLS-1$
                }
                return list;
            }

            public Image getColumnImage(Object element, int columnIndex) {
                return null;
            }

			public String getFilterText(Object selectedObj) {
				return selectedObj.toString();
			}
            public boolean hasTooltip() {
                return true;
            }
            public String getTooltipTitle(SelectionModel value) {
                return "Tooltip";
            }
            public String getTooltips(SelectionModel value) {
                return "Name: 123\nRoot://tmp";
            }
            public Class<?> getElementType() {
                return String.class;
            }

        };

        Display display = new Display();
        final Shell shell = new Shell(display);
        shell.setSize(800, 400);
        shell.setLayout(new GridLayout());
        SuggestBox instance = new SuggestBox(shell, SWT.NONE, exampleProvider);
        instance.getModel().addValueChangeListener(new IValueChangeListener() {
            
            public void handleValueChange(ValueChangeEvent event) {
                System.out.println("model="+event.getObservableValue().getValue()); //$NON-NLS-1$
            }
        });
        
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
                .grab(true, false).applyTo(instance);
        
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }

    class ChoiceDialog extends Dialog {

        private final Object BROWSE = Messages.SuggestBox_Browse;
        private Object LOADING = new Object();

        private String buffer=""; //$NON-NLS-1$

        private Job fUpdateFilterJob;
        private Object fSelectedObj;

        private Shell fShell;
        private Text fText;
        private Button fDropDown;
        private TreeViewer fTreeViewer;
        private ISelectionChangedListener fSelectionChangedListener = new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                Object sel = getSelection();
                if(sel!=LOADING && sel!=BROWSE && sel!=null){
                    buffer=m_provider.getColumnText(sel, 0);
                    closeDialog();
                }
            }
        };
        private DisposeListener fDisposeListener = new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                debugPrint("shell::widgetDisposed() : text=" + m_text.getText()); //$NON-NLS-1$
                m_dialog=null;
            }
        };
        private Listener fActivateListener = new Listener() {

            public void handleEvent(Event e) {
                switch (e.type) {
                case SWT.Deactivate: // in case user select vertical scroll bar,
                                     // an activate event will be fired
                    cancelChoice();
                    break;
                }
            }
        };
        private Listener fListener = new Listener() {

            public void handleEvent(Event e) {
                switch (e.type) {
                case SWT.KeyDown:
                    // by default, UP|DOWN will move cursor to left and right.
                    // We disable them here.
                    if (e.keyCode == SWT.ARROW_UP
                            || e.keyCode == SWT.ARROW_DOWN
                            || e.keyCode == SWT.PAGE_UP
                            || e.keyCode == SWT.PAGE_DOWN) {
                        e.doit = false
                                ; 
                    }
                    break;
                case SWT.KeyUp:
                    e.doit = false;
                    if (e.keyCode == SWT.ARROW_UP) {
                         choiceLineUp();
                    } else if (e.keyCode == SWT.ARROW_DOWN) {
                         choiceLineDown();
                    } else if (e.keyCode == SWT.PAGE_UP) {
                         choicePageUp();
                    } else if (e.keyCode == SWT.PAGE_DOWN) {
                         choicePageDown();
                    } else if (e.keyCode == SWT.CR || e.keyCode == SWT.LF) {
                        e.doit = applyChoice();
                    } else if (e.keyCode == SWT.ESC) {
                        cancelChoice();
                        e.doit = false;
                    } else if (e.keyCode == SWT.ARROW_LEFT) {
                    } else if (e.keyCode == SWT.ARROW_RIGHT) {
                    } else {
                    	if(!fText.getText().equals(buffer)){
                    		buffer=fText.getText();
                    		restartUpdateFilterJob();
                    	}
                    }
                    break;
                    
                case SWT.Traverse:
                    if(e.detail==SWT.TRAVERSE_ESCAPE){
                        cancelChoice();
                        e.doit=false;
                    }
                    break;

                }

            }

			private void choicePageDown() {
				down(VISIBLE_LINE_NUM);
			}

			private void choicePageUp() {
				up(VISIBLE_LINE_NUM);
			}

			private void choiceLineDown() {
				down(1);
			}

			private void choiceLineUp() {
				up(1);
			}
			
		    private void up(int delta)
		    {
		      if(fShell.isDisposed())
		        return;

		      Tree tree= fTreeViewer.getTree();
		      
		      ISelection selection = fTreeViewer.getSelection();
		      if(selection==null||selection.isEmpty()){
		        if(tree.getItemCount()>0)
		          tree.select(tree.getItem(tree.getItemCount()-1));
		      }else{
		        int index = tree.indexOf(tree.getSelection()[0])-delta;
		        if(index>=0){
		          tree.select(tree.getItem(index));
		        }else{
		          if(delta==1)
		            tree.select(tree.getItem(tree.getItemCount()-1));
		          else
		            tree.select(tree.getItem(0));
		        }
		      }
		      tree.showSelection();
		      showSelectionInText();
		    }

			private void down(int delta)
	        {
	          if(fShell.isDisposed())
	            return;
	          
	          Tree tree = fTreeViewer.getTree();
	          
	          ISelection selection = fTreeViewer.getSelection();
	          if(selection==null || selection.isEmpty()){
	            if(tree.getItemCount()>0){
	              tree.select(tree.getItem(0));
	            }
	          }else{
	            int index = tree.indexOf(tree.getSelection()[0])+delta;
	            if(index<tree.getItemCount()){
	              tree.select(tree.getItem(index));
	            }else{
	              if(delta==1)
	                tree.select(tree.getItem(0));
	              else
	                tree.select(tree.getItem(tree.getItemCount()-1));
	            }
	          }
	          tree.showSelection();
	          showSelectionInText();
	        }
	        
        };

        class SuggestContentProvider implements ILazyTreeContentProvider {

            public void dispose() {
            }

            public void inputChanged(Viewer viewer, Object oldInput,
                    Object newInput) {
                if(m_provider!=null)
                    m_provider.inputChanged(viewer, oldInput, newInput);
            }

            public Object getParent(Object element) {
                return null;
            }

            public void updateChildCount(Object element, int currentChildCount) {
                if(m_provider!=null)
                    m_provider.updateChildCount(element,currentChildCount,fTreeViewer);
            }

            public void updateElement(Object parent, int index) {
				if (parent == LOADING && index == 0) {
					fTreeViewer.replace(parent, 0, LOADING);
				} else
					m_provider.updateElement(parent, index, fTreeViewer);
            }
        }

        class SuggestLabelProvider extends LabelProvider implements ITableLabelProvider{

            public String getColumnText(Object element, int columnIndex) {
              if (columnIndex == 0 && element == LOADING) {
	              return Messages.SuggestBox_Loading;
	          } else {
	              return m_provider.getColumnText(element, columnIndex);
	          }
            }
            public Image getColumnImage(Object element, int columnIndex) {
                if (columnIndex == 0) {
                    if (element == LOADING) {
                        return null;
                    }
                }
                return m_provider.getColumnImage(element, columnIndex);
            }

        }

        public ChoiceDialog(Shell parent, int style) {
            super(parent, style);
        }

        public Shell getShell() {
            return fShell;
        }

        private Object getSelection() {
            ISelection sel = fTreeViewer.getSelection();
            if (sel instanceof IStructuredSelection) {
                if (!sel.isEmpty()){
                    fSelectedObj=((IStructuredSelection) sel).getFirstElement();
                    return fSelectedObj;
                }
            }
            return null;
        }

        private Shell createShell() {
            Shell parent = getParent();
            Shell shell = new Shell(parent, SWT.MODELESS);//|SWT.NO_TRIM);
            
            FormLayout layout = new FormLayout ();
            shell.setLayout(layout);
            
            fText = new Text(shell, SWT.BORDER);
            fDropDown = new Button(shell, SWT.SINGLE|SWT.ARROW|SWT.DOWN);
            FormData dropDownData = new FormData ();
            fDropDown.setLayoutData(dropDownData);
            dropDownData.right = new FormAttachment(100,0);
            dropDownData.top = new FormAttachment (0, 5);


            FormData textData = new FormData ();
            fText.setLayoutData(textData);
            textData.left = new FormAttachment (0);
        	textData.right= new FormAttachment (fDropDown, 5, SWT.RIGHT);
        	textData.top = new FormAttachment (fDropDown, 5, SWT.CENTER);

			fTreeViewer = new TreeViewer(shell, SWT.VIRTUAL
					| SWT.FULL_SELECTION | SWT.SINGLE);
            FormData viewData = new FormData ();
            fTreeViewer.getControl().setLayoutData(viewData);
            viewData.left = new FormAttachment (0);
        	viewData.right= new FormAttachment (100,0);
        	viewData.top = new FormAttachment (fText, 5, SWT.BOTTOM);	
        	viewData.bottom = new FormAttachment (100, -5);	
			
            fTreeViewer.setContentProvider(new SuggestContentProvider());
            fTreeViewer.setLabelProvider(new SuggestLabelProvider());
            fTreeViewer.setUseHashlookup(true);
            fTreeViewer.getTree().setLinesVisible(true);

            // We create column explicitly in order to set the column width
            new TreeColumn(fTreeViewer.getTree(), SWT.FILL);

            createUpdateFilterJob();
            
            return shell;
        }

        private void repositionShell(Shell shell, Rectangle bound) {
            shell.pack();

            int rowHight = (fTreeViewer.getTree().getItemHeight() + fTreeViewer
                    .getTree().getGridLineWidth());
            shell.setSize(Math.max(shell.getSize().x, bound.width), rowHight
                    * (VISIBLE_LINE_NUM) + shell.getBorderWidth() * 4);

            Point oldOrigin = new Point(bound.x, bound.y);
            Point newOrigin = new Point(0, 0);
            Rectangle screenBound = getParent().getDisplay().getBounds();

            // find a better location to popup the shell
            if (oldOrigin.x < 0)
                newOrigin.x = 0;
            else if (oldOrigin.x > screenBound.width - shell.getSize().x) {
                newOrigin.x = screenBound.width - shell.getSize().x;
            } else {
                newOrigin.x = oldOrigin.x+2;//shift 2 pixel for border
            }
            if (oldOrigin.y < 0)
                newOrigin.y = 0;
            else if (oldOrigin.y > screenBound.height - shell.getSize().y
                    - bound.height) {
                newOrigin.y = screenBound.height - shell.getSize().y;
            } else {
                newOrigin.y = oldOrigin.y;
            }

            shell.setLocation(newOrigin);
            debugPrint("repositionShell(): bound=" + shell.getBounds()); //$NON-NLS-1$
            shell.layout();
        }

        private void restartUpdateFilterJob() {
        	if(fUpdateFilterJob!=null)
        		fUpdateFilterJob.cancel();
            createUpdateFilterJob();
            fUpdateFilterJob.schedule(500);
        }

        private void createUpdateFilterJob() {
            fTreeViewer.setInput(LOADING);
            fTreeViewer.getTree().setItemCount(1);

            final String filter=fText.getText();
            final Display display=fText.getDisplay();
            
            fUpdateFilterJob = new Job("Update Filter") { //$NON-NLS-1$

                protected IStatus run(IProgressMonitor monitor) {

                	debugPrint("RRRRRRRRRRRRRRRRRRRRRRRRRR----> "+filter); //$NON-NLS-1$
                	if(m_provider==null)
                		return Status.OK_STATUS;
                	
                	// fetch elements
                    final List<Object> list=m_provider.fetchElement(filter);
                    // list.add(0,BROWSE);
                    display.asyncExec(new Runnable() {

                        public void run() {
                        	debugPrint("UUUUUUUUUUUUUUUUUUUUUUUUU----> "+filter); //$NON-NLS-1$
                        	if(!fText.isDisposed()&& filter.equals(fText.getText())){
                            	debugPrint("NNNNNNNNNNNNNNNNNNNNNNNNN----> "+filter); //$NON-NLS-1$
                        		fTreeViewer.setInput(list);
                        	}
                        }
                    });
                    return Status.OK_STATUS;
                }
            };
            fUpdateFilterJob.setSystem(true);
        }

        public void setDefaultSelection() {
            debugPrint("setDefaultSelection()"); //$NON-NLS-1$

            Tree tree = fTreeViewer.getTree();

            if (tree.getSelection() != null)
                return;

            if (tree.getItemCount() > 0) {
                tree.select(tree.getItem(0));
            }
        }

        public int open(Rectangle bound, String filter) {
            fShell = createShell();

            repositionShell(fShell, bound);

            fShell.open();
            fTreeViewer.getTree().getColumn(0)
                    .setWidth(fTreeViewer.getTree().getClientArea().width);

            fText.setFocus();
            fText.append(filter);
            
            addDialogListeners();
            
            // filtering the viewer whenever there is a filter text available.
            restartUpdateFilterJob();

            Display display = getShell().getDisplay();
            while (!fShell.isDisposed()) {
                if (!display.readAndDispatch())
                    display.sleep();
            }

            return 0;
        }

        private void addDialogListeners() {
            fShell.addDisposeListener(fDisposeListener);
            fShell.addListener(SWT.Deactivate, fActivateListener);
            
            fText.addListener(SWT.Traverse, fListener);
            fText.addListener(SWT.KeyUp, fListener);
            fText.addListener(SWT.KeyDown, fListener);
            
            fDropDown.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    cancelChoice();
                }
            });
            
            fTreeViewer.addSelectionChangedListener(fSelectionChangedListener);
        }

        private void showSelectionInText() {
            if(BROWSE.equals(getSelection()))
                return;
            else if(LOADING.equals(getSelection()))
                return;
            updateText(m_provider.getColumnText(getSelection(), 0));
            fSelectedObj=getSelection();
        }

        private void updateText(String newText) {
            buffer=newText;
            if(buffer!=null){
	            fText.setText(""); //$NON-NLS-1$
	            fText.append(buffer);
            }
        }

        protected boolean applyChoice() {
            debugPrint("applyChoice() = START");
            if (m_dialog != null) {
                closeDialog();
            }

            return true;
        }

        protected void cancelChoice() {
            debugPrint("cancelChoice()"); //$NON-NLS-1$
            if(!m_prevText.equals(m_dialog.buffer)){ // restore previous text and close dialog
                String txt=m_prevText;
                setTextQuietly(txt);
            }
            if(isDialogVisible()){
                m_dialog.getShell().close();
                m_text.setFocus();
                m_dialog = null;
            }
        }

    }

    public void setTextQuietlyAndUpdateModel(String text) {
        setTextQuietly(text);
        m_model.setValue(new SelectionModel(text, null));
    }

    public void updateModel(SelectionModel newModel) {
        m_model.setValue(newModel);
        refresh();
    }

    private void setTextQuietly(String txt){
        m_text.removeModifyListener(m_modifyListener);
        m_text.setText(IConstants.EMPTY_STRING);
        if(txt!=null)
            m_text.append(txt);
        m_text.addModifyListener(m_modifyListener);
        m_prevText=txt;

    }
    public String getText() {
        return m_text.getText();
    }

    public void refresh() {
        SelectionModel selection = (SelectionModel) m_model.getValue();
        if(selection!=null && selection.getSelection()!=null){
            setTextQuietly(m_provider.getFilterText(selection.getSelection()));
        }
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        m_text.setEnabled(enabled);
        if(m_dropDown!=null)
            m_dropDown.setEnabled(enabled);
    	if(!enabled){
    		this.m_text.setBackground(getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
    	}else{ 
    		this.m_text.setBackground(getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
    	}
        
    }

}
