package com.perforce.team.ui.streams;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Platform;

import com.perforce.p4java.core.IStream;
import com.perforce.p4java.core.IStream.IExtraTag;
import com.perforce.p4java.core.IStreamIgnoredMapping;
import com.perforce.p4java.core.IStreamRemappedMapping;
import com.perforce.p4java.core.IStreamSummary;
import com.perforce.p4java.core.IStreamSummary.IOptions;
import com.perforce.p4java.core.IStreamViewMapping;
import com.perforce.p4java.core.IStreamViewMapping.PathType;
import com.perforce.p4java.core.ViewMap;
import com.perforce.p4java.exception.NullPointerError;
import com.perforce.p4java.impl.generic.core.Stream;
import com.perforce.p4java.impl.generic.core.Stream.StreamIgnoredMapping;
import com.perforce.p4java.impl.generic.core.Stream.StreamRemappedMapping;
import com.perforce.p4java.impl.generic.core.Stream.StreamViewMapping;
import com.perforce.p4java.impl.generic.core.StreamSummary.Options;
import com.perforce.team.core.IConstants;
import com.perforce.team.core.P4LogUtils;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Stream;
import com.perforce.team.core.p4java.P4Depot;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.dialogs.StreamsPreferencesDialog;

/**
 * Utility for streams.
 * 
 * @author ali
 *
 */
public class StreamUtil {
    
    public static final String DELIMITER=" "; //$NON-NLS$ //$NON-NLS-1$
    
    public static final String EXTRA_TAG_BASEPARENT="baseParent"; //$NON-NLS-1$
    public static final String EXTRA_TAG_FIRMERTHANPARENT="firmerThanParent"; //$NON-NLS-1$
    public static final String EXTRA_TAG_UNKNOW_VALUE="n/a"; //$NON-NLS-1$
    public static final String DEFAULT_PARENT="none"; //$NON-NLS-1$

    public static boolean connectionOK(IP4Connection connection) {
        return connection != null && !connection.isOffline()
                && !connection.isDisposed();
    }

    public static String getStreamClientView(IP4Stream stream) {
        List<String> mappings = stream.getClientView();
        StringBuilder sb=new StringBuilder();
        for(String mapping:mappings){
            sb.append(mapping+IConstants.RETURN);
        }
        return sb.toString();
    }

    public static String getRemmapedPaths(ViewMap<IStreamRemappedMapping> remap) {
        StringBuilder sb = new StringBuilder();
        for(IStreamRemappedMapping mapping:remap.getEntryList()){
            String lhs = mapping.getLeftRemapPath();
            String rhs = mapping.getRightRemapPath();
            sb.append((lhs==null?IConstants.EMPTY_STRING:lhs+DELIMITER)
                    +(rhs==null?IConstants.EMPTY_STRING:rhs)
                    +IConstants.RETURN);
        }
        return sb.toString();
    }

    public static String getIgnoredPatterns(ViewMap<IStreamIgnoredMapping> ignoreView) {
        StringBuilder sb = new StringBuilder();
        for(IStreamIgnoredMapping mapping:ignoreView.getEntryList()){
            sb.append(mapping.getIgnorePath()+IConstants.RETURN);
        }
        return sb.toString();
    }

    public static String getStreamViewPaths(ViewMap<IStreamViewMapping> viewMap) {
        StringBuilder sb=new StringBuilder();
        for(IStreamViewMapping mapping:viewMap.getEntryList()){
            PathType type = mapping.getPathType();
            String lhs = mapping.getViewPath();
            String rhs = mapping.getDepotPath();
            sb.append((type==null?IConstants.EMPTY_STRING:type.toString()+DELIMITER)
                    +(lhs==null?IConstants.EMPTY_STRING:lhs+DELIMITER)
                    +(rhs==null?IConstants.EMPTY_STRING:rhs)
                    +IConstants.RETURN);
        }
        return sb.toString();
    }


    public static String makeStreamAbsolute(String streamRoot) {
        Assert.isNotNull(streamRoot);
        StringBuilder sb=new StringBuilder();
        if(!streamRoot.startsWith("//"))
            sb.append("//");
        sb.append(streamRoot);
        if(!streamRoot.endsWith("/"))
            sb.append("/");
        return sb.toString();
    }


    public static ViewMap<IStreamViewMapping> parseStreamViewMapping(
            String lines) {
        ViewMap<IStreamViewMapping> streamView = new ViewMap<IStreamViewMapping>();
        if (lines != null) {
            String[] viewPaths = lines.split(SystemUtils.LINE_SEPARATOR);
            for (int i=0;i<viewPaths.length;i++) {
                String mapping=viewPaths[i];
                if (mapping == null) {
                    throw new NullPointerError("null view mapping string passed to Stream.newStream");
                }
                
                // fix for job052946:
                // please refer to com.perforce.p4java.impl.generic.core.Stream#StreamViewMapping
                // for why we need an extra  " "
                // in case: only type is specified, not lhs and rhs
                // we need add an extra " " to help p4java to parse the mapping correctly.
                if(-1 == mapping.indexOf(" ")){ 
                    mapping=mapping+" ";
                }
                    
                StreamViewMapping entry = new StreamViewMapping(i, mapping);
                if(entry.getPathType()==null)
                    entry.setPathType(PathType.SHARE);
                streamView.addEntry(entry);
            }
        }
        return streamView;
    }
    
    public static ViewMap<IStreamRemappedMapping> parseRemappedMapping(
            String lines){
        ViewMap<IStreamRemappedMapping> remappedView = new ViewMap<IStreamRemappedMapping>();
        if(lines!=null){
            String[] remappedPaths = lines.split(SystemUtils.LINE_SEPARATOR);
            for (int i=0;i<remappedPaths.length;i++) {
                String mapping = remappedPaths[i];
                if (mapping == null) {
                    throw new NullPointerError("null remapped mapping string passed to Stream.newStream");
                }
                remappedView.addEntry(new StreamRemappedMapping(i, mapping));
            }
        }
        return remappedView;
    }
    
    public static ViewMap<IStreamIgnoredMapping> parseIgnoredMapping(
            String lines){
        ViewMap<IStreamIgnoredMapping> ignoredMapping = new ViewMap<IStreamIgnoredMapping>();
        if(lines!=null){
            String[] ignoredPaths = lines.split(SystemUtils.LINE_SEPARATOR);
            if (ignoredPaths != null) {
                for (int i=0;i<ignoredPaths.length;i++) {
                    String mapping=ignoredPaths[i];
                    if (mapping == null) {
                        throw new NullPointerError("null ignored path string passed to Stream.newStream");
                    }
                    ignoredMapping.addEntry(new StreamIgnoredMapping(i, mapping));
                }
            }
        }
        return ignoredMapping;
    }

    public static boolean isParentEmpty(IStreamSummary stream) {
        String parent = stream.getParent();
        if(!StringUtils.isEmpty(parent)){
            if(!parent.equals("none") && !parent.equals("//none/")) // please refer to com.perforce.p4java.impl.generic.core.Stream.java
                return false;
        }
        return true;
    }
    
    public static boolean isStreamEmpty(IStreamSummary stream){
        String s = stream.getStream();
        if(!StringUtils.isEmpty(s)){
            if(!s.equals("none") && !s.equals("//none/")) // please refer to com.perforce.p4java.impl.generic.core.Stream.java
                return false;
        }
        return true;
        
    }
    
    public static String getDepotName(IStreamSummary sum){
        String path = sum.getStream();
        if(StringUtils.isEmpty(path))
            return IConstants.EMPTY_STRING;
        
        Assert.isTrue(path.startsWith("//"));
        
        String[] segments = path.substring(2).split("/");
        if(sum.getType()==IStreamSummary.Type.MAINLINE){
            Assert.isTrue(segments!=null && segments.length>0);
        }else{
            Assert.isTrue(segments!=null && segments.length>1);
        }
        return segments[0];
    }
    
    public static String getDepot(IStreamSummary sum){
        String path = sum.getStream();
        if(StringUtils.isEmpty(path))
            return IConstants.EMPTY_STRING;
        
        Assert.isTrue(path.startsWith("//"));
        
        String[] segments = path.substring(2).split("/");
        if(sum.getType()==IStreamSummary.Type.MAINLINE){
            Assert.isTrue(segments!=null && segments.length>0);
        }else{
            Assert.isTrue(segments!=null && segments.length>1);
        }
        return "//"+segments[0];
    }
    
    public static String getParentDepot(IStreamSummary sum){
        if(isParentEmpty(sum)){
            return getDepot(sum);
        }

        String path = sum.getParent();
        
        if(StringUtils.isEmpty(path))
            return IConstants.EMPTY_STRING;
        
        Assert.isTrue(path.startsWith("//"));
        
        String[] segments = path.substring(2).split("/");
        Assert.isTrue(segments!=null && segments.length>0);
        
        return "//"+segments[0];
    }

    public static String normalizePath(String streampath) {
        if(StringUtils.isEmpty(streampath))
            return IConstants.EMPTY_STRING;
        
        StringBuilder sb=new StringBuilder();
        
        int index=-1;
        for(int i=0;i<streampath.length();i++){
            if(streampath.charAt(i)!='/'){
                index=i;
                break;
            }
        }
        sb.append("//");
        if(index>=0)
            sb.append(streampath.substring(index));
        
        if(sb.length()>2 && streampath.endsWith("/")){
            for(int i=sb.length()-1;i>0;i--){
                if(sb.charAt(i)=='/'){
                    sb.deleteCharAt(i);
                }else{
                    break;
                }
            }
        }
        
        return sb.toString();
    }

    /**
     *  //depotname/streamname
     */
    public static boolean isStreamPathFormat(String path) {
        if(StringUtils.isEmpty(path))
            return false;
        
        if(path.startsWith("//")){
            String sub = path.substring(2);
            String[] segments=sub.split("/");
            if(segments.length==2)
                return true;
        }
        return false;
    }
    
    public static String getNormalizedPathSegment(String path, int index) {
        Assert.isTrue(isStreamPathFormat(path));
        Assert.isTrue(index<2);
        return path.substring(2).split("/")[index];
    }
    
    public static String normalizeStreamPathForQuery(String parentStreamRoot){
    	return normalizePath(parentStreamRoot);    	
    }

    public static IStream copyStream(IStream s) {
        IStream stream=new Stream();

        stream.setOwnerName(s.getOwnerName());
        stream.setDescription(s.getDescription());
        stream.setName(s.getName());
        stream.setStream(s.getStream());
        stream.setType(s.getType());
        stream.setParent(s.getParent());
        
        IOptions streamOptions = new Options();
        IOptions oldOptions = s.getOptions();
        if (oldOptions != null) {
            streamOptions = new Options();
            streamOptions.setLocked(oldOptions.isLocked());
            streamOptions.setOwnerSubmit(oldOptions.isOwnerSubmit());
            streamOptions.setNoToParent(oldOptions.isNoToParent());
            streamOptions.setNoFromParent(oldOptions.isNoFromParent());
        }
        stream.setOptions(streamOptions);

        ViewMap<IStreamViewMapping> streamView = new ViewMap<IStreamViewMapping>();
        ViewMap<IStreamViewMapping> oldStreamView = s.getStreamView();
        if (oldStreamView != null) {
            for (int i=0;i<oldStreamView.getSize();i++) {
                IStreamViewMapping entry = oldStreamView.getEntry(i);
                StreamViewMapping newEntry = new StreamViewMapping(entry.getOrder(), entry.getPathType(), entry.getViewPath(), entry.getDepotPath());
                newEntry.setPathType(entry.getPathType());
                streamView.addEntry(newEntry);
            }
        } else {
            streamView.addEntry(new StreamViewMapping(0, PathType.SHARE, "...", null));
        }
        stream.setStreamView(streamView);

        ViewMap<IStreamRemappedMapping> remappedView = new ViewMap<IStreamRemappedMapping>();
        ViewMap<IStreamRemappedMapping> oldRemappedView=s.getRemappedView();
        if(oldRemappedView!=null){
            for (int i=0;i<oldRemappedView.getSize();i++) {
                IStreamRemappedMapping entry = oldRemappedView.getEntry(i);
                StreamRemappedMapping newEntry = new StreamRemappedMapping(entry.getOrder(), entry.getLeftRemapPath(), entry.getRightRemapPath());
                remappedView.addEntry(newEntry);
            }
        }
        stream.setRemappedView(remappedView);

        ViewMap<IStreamIgnoredMapping> ignoredMapping = new ViewMap<IStreamIgnoredMapping>();
        ViewMap<IStreamIgnoredMapping> oldIgnoredMapping=s.getIgnoredView();
        if (oldIgnoredMapping != null) {
            for (int i=0;i<oldIgnoredMapping.getSize();i++) {
                IStreamIgnoredMapping entry = oldIgnoredMapping.getEntry(i);
                StreamIgnoredMapping newEntry = new StreamIgnoredMapping(entry.getOrder(), entry.getIgnorePath());
                ignoredMapping.addEntry(newEntry);
            }
        }
        stream.setIgnoredView(ignoredMapping);

        return stream;
    }

    public static void updateStream(IStream src, IStream dst) {
        dst.setOwnerName(src.getOwnerName());
        dst.setDescription(src.getDescription());
        dst.setName(src.getName());
        dst.setStream(src.getStream());
        dst.setType(src.getType());
        dst.setParent(src.getParent());
        
        dst.setOptions(src.getOptions());
        dst.setStreamView(src.getStreamView());
        dst.setIgnoredView(src.getIgnoredView());
        dst.setRemappedView(src.getRemappedView());
    }

    
    public static IStream createDefaultStream(IP4Connection connection) {
        IStream stream=new Stream();
        stream.setOwnerName(connection.getUser());
        stream.setOptions(new Options());
        return stream;
    }    
    
    public static IStream createNewStream(IP4Stream src) {
        IStream stream=new Stream();
        stream.setOptions(new Options());
        if(src!=null){
            stream.setOwnerName(src.getConnection().getUser());
        
            IStream parent=(IStream) src.getAdapter(IStream.class);
            stream.setParent(parent.getStream());
            if(parent.getType()==IStreamSummary.Type.MAINLINE){
                stream.setType(IStreamSummary.Type.DEVELOPMENT);
            }else{
                stream.setType(parent.getType());
            }
        }else{
            stream.setType(IStreamSummary.Type.DEVELOPMENT);
        }
        
        if(stream.getType()==IStreamSummary.Type.DEVELOPMENT){
            stream.getOptions().setNoFromParent(false);
            stream.getOptions().setNoToParent(false);
        }else if(stream.getType()==IStreamSummary.Type.TASK){
            stream.getOptions().setNoFromParent(false);
            stream.getOptions().setNoToParent(false);
        }else if(stream.getType()==IStreamSummary.Type.RELEASE){
            stream.getOptions().setNoFromParent(true);
            stream.getOptions().setNoToParent(false);
        }else if(stream.getType()==IStreamSummary.Type.MAINLINE){
            stream.getOptions().setNoFromParent(true);
            stream.getOptions().setNoToParent(true);
        }else if(stream.getType()==IStreamSummary.Type.VIRTUAL){
            stream.getOptions().setNoFromParent(true);
            stream.getOptions().setNoToParent(true);
        }
        return stream;
    }


    public static String getStreamRoot(IStreamSummary sum) {
        String path = sum.getStream();
        
        if(StringUtils.isEmpty(path))
            return IConstants.UNKNOWN;
        
        Assert.isTrue(path.startsWith("//"));
        
        String[] segments = path.substring(2).split("/");
        Assert.isTrue(segments!=null && segments.length>1);
        
        String root = segments[1];
        int index = root.indexOf("/");
        if(index>0)
            return root.substring(0,index);
        else
            return root;

    }


    public static String print(Object obj){
        if(Platform.inDebugMode()){
            String snapShot = ReflectionToStringBuilder.reflectionToString(obj, new P4LogUtils.RecursiveToStringStyle(-1));
            PerforceProviderPlugin.logInfo(snapShot);
            return snapShot;
        }
        return "";
    }

    public static void updateStreamDepot(IStream stream, String path) {
        if(StringUtils.isEmpty(path)){
            stream.setStream("//"+IConstants.UNKNOWN+"/"+getStreamRoot(stream));
            return;
        }
        
        if(isStreamEmpty(stream)){
            String name = stream.getName();
            if(!StringUtils.isEmpty(name)){
                name=escapeWhiteSpace(name);
            }else{
                name=IConstants.UNKNOWN;
            }

            stream.setStream(path+"/"+name);;
        }else{
            stream.setStream(path+"/"+getStreamRoot(stream));
        }
        
    }
    
    public static void updateStream(IStream stream, String name, String depot) {
        
        if(!StringUtils.isEmpty(name)){
            name=escapeWhiteSpace(name);
        }else{
            name=IConstants.UNKNOWN;
        }

        stream.setStream(depot+"/"+name);
        
    }
    
    public static String escapeWhiteSpace(String src){
        return src.replace(' ', '_');
    }
    
    public static IStream getStream(IP4Connection connection){
        IP4Stream p4stream = connection.getStream(connection.getClient().getStream());
        if(p4stream!=null){
            IStream stream=(IStream) p4stream.getAdapter(IStream.class);
            return stream;
        }
        return null;
    }


    public static IStreamSummary getParentStream(IStreamSummary stream,
            IP4Connection connection) {
        IStreamSummary parentStream=null;
        if(!StreamUtil.isParentEmpty(stream)){
        	parentStream=connection.getStreamSummary(stream.getParent());
        }
        return parentStream;
    }

    public static boolean isFirmerThanParent(IStreamSummary stream) {
        return stream.getType()==IStreamSummary.Type.RELEASE;
    }

    public static boolean isSofterThanParent(IStreamSummary stream) {
        return stream.getType()==IStreamSummary.Type.DEVELOPMENT;
    }

    public static String convertToJavaRegex(String filter) {
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<filter.length();i++){
            if(filter.charAt(i)=='*'){
                if(i==0){
                    sb.append(".");
                }else if(filter.charAt(i-1)!='.'){
                    sb.append(".");
                }
            }
            sb.append(filter.charAt(i));
        }
        return sb.toString();
    }


    public static List<IStreamSummary> getChildStream(IStream parent,
            IP4Connection connection) {
        List<IStreamSummary> list=new ArrayList<IStreamSummary>();
        List<IP4Stream> streams = connection.getFilteredStreams(false, null, "Parent="+parent.getStream(), 0);

        for(IP4Stream s: streams){
            list.add(s.getStreamSummary());
        }
        
        return list;
    }


    /**
     * Get preferred source streams to copy from. 
     * <ul>Case 1: stream type is DEV, and MAIN
     *    <li>for each child c of stream, if c.isSofterThanParent() and c.flowToParent(), add c to list</li>
     * </ul>
     * <ul>Case 2: stream type is REL
     *    <li>for parent p of stream, if !stream.isSofterThanParent() and stream.flowFromParent(), add p to list</li>
     *    <li>for each child c of stream, if c.isSofterThanParent() and c.flowToParent(), add c to list</li>
     * </ul>
     * <ul>Case 3: stream type is VIRTUAL
     *    <li>TODO</li>
     * </ul>
     * 
     * @param stream the target stream to copy to
     * @param connection
     * @return a list of preferred source stream to copy changes from (and to target stream)
     */
    public static List<IStreamSummary> getPreferredCopySources(IStream stream, IP4Connection connection) {
        ArrayList<IStreamSummary> preferredStreams = new ArrayList<IStreamSummary>();
        if(stream!=null){
            if(stream.getType()==IStreamSummary.Type.DEVELOPMENT
                    ||stream.getType()==IStreamSummary.Type.MAINLINE
                    ||stream.getType()==IStreamSummary.Type.VIRTUAL){ // case 1
                preferredStreams.addAll(getChildrenCopyFrom(stream, connection));
            }else if(stream.getType()==IStreamSummary.Type.RELEASE){ // case 2
                preferredStreams.addAll(getChildrenCopyFrom(stream,connection));
                IOptions opt = stream.getOptions();
                if(!opt.isNoFromParent()){
                    IStreamSummary parent = getParentStream(stream, connection);
                    preferredStreams.add(parent);
                }
            }
        }
        return preferredStreams;
    }

    /**
     * Query child streams with "p4 streams -F Parent=this_stream", then filter out the firmer streams.
     * 
     * @param stream
     * @param connection
     * @return a list of child streams which: 
     *         1) if stream.type=DEV, child no firmer than this stream
     *         2) if stream.type=MAINLINE or VIRTUAL, all child
     *         3) Above child should also can flow to this stream
     */
    private static List<IStreamSummary> getChildrenCopyFrom(IStream stream,
            IP4Connection connection) {
        ArrayList<IStreamSummary> list = new ArrayList<IStreamSummary>();
        List<IStreamSummary> children=StreamUtil.getChildStream(stream,connection);
        for(IStreamSummary c: children){
            IOptions opt = c.getOptions();
            if(!StreamUtil.isFirmerThanParent(c) && !opt.isNoToParent()){
                list.add(c);
            }
        }
        return list;
    }


    /**
     * Get preferred streams to merge from. 
     * <ul>Case 1: stream type is REL, and MAIN
     *    <li>for each child c of stream, if c.isFirmerThanParent() and c.flowToParent(), add c to list</li>
     * </ul>
     * <ul>Case 2: stream type is DEV or TASK
     *    <li>for parent p of stream, if stream.flowFromParent(), add p to list</li>
     *    <li>for each child c of stream, if c.isFirmerThanParent() and c.flowToParent(), add c to list</li>
     * </ul>
     * <ul>Case 3: stream type is VIRTUAL
     *    <li>TODO</li>
     * </ul>
     * 
     * @param stream the target stream to merge to
     * @param connection
     * @return a list of preferred streams to merge changes from (and to target stream)
     */
    public static List<IStreamSummary> getPreferredMergeSources(IStream stream, IP4Connection connection) {
        ArrayList<IStreamSummary> preferredStreams = new ArrayList<IStreamSummary>();
        if(stream!=null){
	        if(stream.getType()==IStreamSummary.Type.RELEASE
	                ||stream.getType()==IStreamSummary.Type.MAINLINE
	                ||stream.getType()==IStreamSummary.Type.VIRTUAL){ // case 1
	            preferredStreams.addAll(getChildrenMergeFrom(stream, connection));
	        }else if(stream.getType()==IStreamSummary.Type.DEVELOPMENT || stream.getType()==IStreamSummary.Type.TASK){ // case 2
	            preferredStreams.addAll(getChildrenMergeFrom(stream,connection));
	            IOptions opt = stream.getOptions();
	            if(!opt.isNoFromParent()){
	                IStreamSummary parent = getParentStream(stream, connection);
	                preferredStreams.add(parent);
	            }
	        }
        }
        return preferredStreams;
    }
    
    /**
     * Query child streams with "p4 streams -F Parent=this_stream", then filter out the firmer streams.
     * 
     * @param target
     * @param connection
     * @return a list of child streams which is firmer than this stream, and also can flow to this stream
     */
    private static List<IStreamSummary> getChildrenMergeFrom(IStream target,
            IP4Connection connection) {
        ArrayList<IStreamSummary> list = new ArrayList<IStreamSummary>();
        List<IStreamSummary> children=StreamUtil.getChildStream(target,connection);
        for(IStreamSummary c: children){
            IOptions opt = c.getOptions();
            if(StreamUtil.isFirmerThanParent(c) && !opt.isNoToParent()){
                list.add(c);
            }
        }
        return list;
    }


    public static boolean isValidStreamFormat(IStream stream) {
        if(!isStreamEmpty(stream)){
            String s = stream.getStream();
            if(s.startsWith("//")){
                int count=0;
                int index=-1;
                for(int i=2;i<s.length();i++){
                    if(s.charAt(i)=='/'){
                        count++;
                        index=i;
                    }
                }
                if(count==1 && index!=s.length()-1){
                    return true;
                }
            }
        }

        return false;
    }

    public static IStreamSummary matchStream(String id, List<IStreamSummary> list){
        for(IStreamSummary sum: list){
            if(sum!=null && id.equals(sum.getStream()))
                return sum;
        }
        return null;
    }


    public static String getStreamDisplayText(IStreamSummary sum) {
        int show=PerforceUIPlugin.getPlugin().getPreferenceStore().getInt(IPerforceUIConstants.PREF_STREAM_DISPLAY);
        switch(show){
        case StreamsPreferencesDialog.SHOW_NAME_ONLY:
            return sum.getName();
        case StreamsPreferencesDialog.SHOW_ROOT_ONLY:
            return sum.getStream();
        case StreamsPreferencesDialog.SHOW_NAME_ROOT:
        default:
            return sum.getName()+IConstants.SPACE+IConstants.LPARENTH+sum.getStream()+IConstants.RPARENTH; //$NON-NLS-1$
                
        }
    }
    
    public static IExtraTag getExtraTag(IStream stream, String name){
        List<IExtraTag> extraTags = stream.getExtraTags();
        for(IExtraTag tag: extraTags){
            if(tag.getName().equals(name))
                return tag;
        }
        return null;
    }
    
    public static String getBaseParent(IStreamSummary stream){
        if(stream instanceof IStream){
            IExtraTag tag = getExtraTag((IStream) stream,EXTRA_TAG_BASEPARENT);
            if(tag!=null && !EXTRA_TAG_UNKNOW_VALUE.equals(tag.getValue())){
                return tag.getValue();
            }
        }
        return stream.getParent();
    }
    
    public static IStream getBaseParent(IStreamSummary sum, IP4Connection connection){
        try {
            IStream s=null;
            if(sum instanceof IStream){
                s=(IStream) sum;
            }else{
            	s=(IStream) connection.getStreamSummary(sum.getStream());
            }
            
            IExtraTag tag = getExtraTag(s,EXTRA_TAG_BASEPARENT);
            if(tag!=null && !EXTRA_TAG_UNKNOW_VALUE.equals(tag.getValue())){
                return getBaseParent(connection.getStreamSummary(tag.getValue()),connection);
            }else{
                return (IStream) connection.getStreamSummary(s.getParent());
            }
        } catch (Exception e) {
        }
        return null;
    }

	public static IStreamSummary findStreamSummary(IP4Connection conn,
			String stream) {
		for (IP4Resource r : conn.members()) {
			if (r instanceof P4Depot) {
				List<IStreamSummary> streams = ((P4Depot) r).getStreams();
				if (streams != null && !streams.isEmpty()) {
					for (IStreamSummary s : streams) {
						if (stream.equals(s.getStream()))
							return s;
					}
				}
			}
		}
		return null;
	}

	public static String[] parseStream(String stream){
		if(stream.startsWith("//")){
			stream=stream.substring(2);
		}
		if(stream.endsWith("/")){
			stream = stream.substring(0,stream.length()-1);
		}
		return stream.split("/");
	}
}
