package com.perforce.team.ui.streams;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;

import com.perforce.p4java.core.IStreamSummary;
import com.perforce.p4java.option.server.GetStreamsOptions;
import com.perforce.team.core.IConstants;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.streams.SuggestBox.ISuggestProvider;
import com.perforce.team.ui.streams.SuggestBox.SelectionModel;

/**
 * Stream suggester which provide filtered streams to the suggest box.
 * 
 * @author ali
 *
 */
public class StreamsSuggestProvider implements ISuggestProvider {
	
	protected IP4Connection connection;
	
	public IP4Connection getConnection() {
		return connection;
	}

	public void setConnection(IP4Connection connection) {
		this.connection = connection;
	}

	public StreamsSuggestProvider(IP4Connection conn){
		this.connection=conn;
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

	public void updateElement(Object parent, int index, TreeViewer viewer) {
        if (parent instanceof List) {
            List<?> list = (List<?>) parent;
            if ((index >= 0) && (index < list.size())) {
                Object obj = list.get(index);
                viewer.replace(parent, index, obj);
                viewer.setChildCount(obj, 0);
            }
        }
	}

	public String getColumnText(Object element, int columnIndex) {
		if(element instanceof IStreamSummary){
			IStreamSummary sum=(IStreamSummary) element;
			return sum.getName()+IConstants.SPACE+IConstants.LPARENTH+sum.getStream()+IConstants.RPARENTH;
		}
		return null;
	}

	public List<Object> fetchElement(String filter) {
	    
	    Assert.isNotNull(filter);
	    if(StringUtils.isEmpty(filter)){
	        return initEmpty(filter);
	    }
		
		if((!StringUtils.isEmpty(filter) && filter.trim().length()==0) || filter.equals("/")){ // do nothing for empty or "/"
		    return new ArrayList<Object>();
		}

		filter=filter.trim();
		GetStreamsOptions opts=initOptions(filter);
		List<String> paths=initPaths(filter);
		
		List<IStreamSummary> summaries =connection.getStreams(paths, opts);
		List<Object> result=new ArrayList<Object>();
		result.addAll(summaries);
		return result;
	}
	
    protected List<Object> initEmpty(String filter) {
        return fetchElement("//");
    }

    protected List<String> initPaths(String filter) {
        List<String> paths=null;
        if(filter.startsWith("//")){ // in case of search based on stream path
            String path = StreamUtil.normalizePath(filter);
            paths=new ArrayList<String>();
            if(StreamUtil.isStreamPathFormat(path)){
                paths.add(path+"*"); //example //Grinder/a*
            }else{
                paths.add(path+"*/*"); // example //Grinder*/*
            }
        }
        return paths;
    }

    protected GetStreamsOptions initOptions(String filter) {
        GetStreamsOptions opts = new GetStreamsOptions();
        if(!filter.startsWith("//")){ // in case of search based on stream path
            opts.setFilter("Name="+filter+"*"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return opts;
    }

	public Image getColumnImage(Object element, int columnIndex) {
		// Do not waste resource
		return null;
	}

	public String getFilterText(Object selection) {
		if(selection instanceof IStreamSummary){
		    return StreamUtil.getStreamDisplayText((IStreamSummary)selection);
		}
		return null;
	}

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // TODO Auto-generated method stub
    }

    public boolean hasTooltip() {
        return true;
    }

    public String getTooltipTitle(SelectionModel value) {
        return Messages.StreamsSuggestProvider_Stream;
    }

    public String getTooltips(SelectionModel value) {
        final String UNKNOWN=Messages.StreamsSuggestProvider_Unknown;
        final String NAME=Messages.StreamsSuggestProvider_Name;
        final String ROOT=Messages.StreamsSuggestProvider_Root;
        StringBuilder sb=new StringBuilder();
        if(value!=null){
            if(value.getSelection() instanceof IStreamSummary){
                IStreamSummary sum=(IStreamSummary) value.getSelection();
                sb.append(NAME+NameValueToolTip.ms_tooltipPairSep+sum.getName()+NameValueToolTip.ms_tooltipLineSep);
                sb.append(ROOT+NameValueToolTip.ms_tooltipPairSep+sum.getStream()+NameValueToolTip.ms_tooltipLineSep);
            }else if(value.getText()!=null && !value.getText().equals(IConstants.EMPTY_STRING)){
                String text = value.getText();
                if(text.startsWith("//")){ //$NON-NLS-1$
                    sb.append(NAME+NameValueToolTip.ms_tooltipPairSep+UNKNOWN+NameValueToolTip.ms_tooltipLineSep);
                    sb.append(ROOT+NameValueToolTip.ms_tooltipPairSep+value.getText()+NameValueToolTip.ms_tooltipLineSep);
                }else{
                    sb.append(NAME+NameValueToolTip.ms_tooltipPairSep+value.getText()+NameValueToolTip.ms_tooltipLineSep);
                    sb.append(ROOT+NameValueToolTip.ms_tooltipPairSep+UNKNOWN+NameValueToolTip.ms_tooltipLineSep);
                }
            }else{
                sb.append(NAME+NameValueToolTip.ms_tooltipPairSep+UNKNOWN+NameValueToolTip.ms_tooltipLineSep);
                sb.append(ROOT+NameValueToolTip.ms_tooltipPairSep+UNKNOWN+NameValueToolTip.ms_tooltipLineSep);
            }
        }else{
            sb.append(NAME+NameValueToolTip.ms_tooltipPairSep+UNKNOWN+NameValueToolTip.ms_tooltipLineSep);
            sb.append(ROOT+NameValueToolTip.ms_tooltipPairSep+UNKNOWN+NameValueToolTip.ms_tooltipLineSep);
        }
        return sb.toString();
    }

    public Class<?> getElementType() {
        return IStreamSummary.class;
    }

}
