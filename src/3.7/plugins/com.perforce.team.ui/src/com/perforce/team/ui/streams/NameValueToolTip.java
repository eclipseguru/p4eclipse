package com.perforce.team.ui.streams;

import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;

import com.perforce.team.core.IConstants;

/**
 * A tooltip which consists of a title bar plus several name:value pairs.
 * <p/>
 * The tooltip will show right below the control.
 * 
 * @author ali
 */
abstract public class NameValueToolTip extends ToolTip
{
  public static final String ms_tooltipLineSep=IConstants.RETURN;
  public static final String ms_tooltipPairSep=IConstants.COLON;
	  
  private Control control;

  public NameValueToolTip(Control control, int style,
    boolean manualActivation){
    super(control, style, manualActivation);
    this.control=control;
  }

  protected Composite createToolTipContentArea(Event event, Composite parent)
  {
    Composite comp = new Composite(parent,SWT.NONE);
    
    GridLayout gridLayout = new GridLayout(1,false);
    gridLayout.marginBottom=0;
    gridLayout.marginTop=0;
    gridLayout.marginHeight=0;
    gridLayout.marginWidth=0;
    gridLayout.marginLeft=0;
    gridLayout.marginRight=0;
    gridLayout.verticalSpacing=1;
    comp.setLayout(gridLayout);
    
    Composite topArea = new Composite(comp,SWT.NONE);
    GridData data = new GridData(SWT.FILL,SWT.FILL,true,false);
    data.minimumWidth=300;// widthHint
    topArea.setLayoutData(data);
    topArea.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
    
    gridLayout = new GridLayout(1,false);
    gridLayout.marginBottom=2;
    gridLayout.marginTop=2;
    gridLayout.marginHeight=0;
    gridLayout.marginWidth=0;
    gridLayout.marginLeft=5;
    gridLayout.marginRight=2;
    
    topArea.setLayout(gridLayout);
    
    Label l = new Label(topArea,SWT.NONE);
    l.setText(getTitle());
    l.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
    l.setFont(new FontRegistry().getBold(Display.getCurrent().getSystemFont()
      .getFontData()[0].getName()));
    l.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
    l.setLayoutData(new GridData(GridData.FILL_BOTH));
    
    createContentArea(comp).setLayoutData(new GridData(GridData.FILL_BOTH));
    return comp;
  }

  protected Composite createContentArea(Composite parent) {
    Composite comp = new Composite(parent,SWT.NONE);
    comp.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
    GridLayout layout = new GridLayout(2, false);
    layout.marginWidth=5;
    comp.setLayout(layout);
    String[] tips = getPairString().split(ms_tooltipLineSep);;
    for(int i=0;i<tips.length;i++){
      GridData gridData=new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
      String[] pair = tips[i].split(ms_tooltipPairSep);
      Label title=new Label(comp, SWT.NONE);
      title.setFont(new FontRegistry().getBold(Display.getCurrent().getSystemFont()
      .getFontData()[0].getName()));
      title.setText(pair[0]);
      title.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
      title.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
      title.setLayoutData(gridData);
      
      Label cont=new Label(comp, SWT.NONE|SWT.WRAP);
      cont.setText(pair[1]);
      cont.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
      cont.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
    }
    return comp;
  }
  
  protected boolean shouldCreateToolTip(Event event) {
    String pairString=getPairString();
    return (pairString!=null && !pairString.trim().equals(""));
  }

  @Override
    public Point getLocation(Point tipSize, Event event) {
      return control.toDisplay(-control.getBorderWidth(),control.getSize().y-control.getBorderWidth());
    }
  
  /**
   * Get tooltip title
   * @return tooltip title
   */
  abstract protected String getTitle();
  
  /**
   * Get tooltip string
   * @return a string like name1:value1\nname2:value2\n...
   */
  abstract protected String getPairString();
}
