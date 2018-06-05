package com.perforce.team.core;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.perforce.p4java.core.file.FileSpecOpStatus;
import com.perforce.p4java.core.file.IFileSpec;

/**
 * Logging utils.
 * 
 * @author ali
 * 
 */
public class P4LogUtils {

    public static String getError(IFileSpec[] specs) {
        StringBuffer buffer = new StringBuffer();
        for (IFileSpec spec : specs) {
            if (FileSpecOpStatus.ERROR == spec.getOpStatus()) {
                String message = spec.getStatusMessage();
                if (message != null) {
                    buffer.append(message).append("\n"); //$NON-NLS-1$
                }
            }

        }
        return buffer.toString();
    }

    public static void logError(IFileSpec[] specs) {
        PerforceProviderPlugin.logError(getError(specs));
    }

    public static boolean reflectiveEquals(Object a, Object b){
    	if(a==b)
    		return true;
    	if(a==null || b==null)
    		return false;
    	
    	String astring = ReflectionToStringBuilder.reflectionToString(a, new P4LogUtils.RecursiveToStringStyle(-1));
    	String bstring = ReflectionToStringBuilder.reflectionToString(b, new P4LogUtils.RecursiveToStringStyle(-1));
    	return astring.endsWith(bstring);
    }

    public static boolean testEquals(Object a, Object b){
    	if(a instanceof String || b instanceof String){
    		if(StringUtils.isEmpty((String) a) && StringUtils.isEmpty((String)b)){
    			return true;
    		}
    	}
    	
    	if(a==b)
    		return true;
    	if(a==null || b==null)
    		return false;
    	
    	return a.equals(b);
    }

    /**
     * Usage:
     *   ReflectionToStringBuilder.reflectionToString(stream,new P4LogUtils.RecursiveToStringStyle());
     *   
     * @author ali
     *
     */
    public static class RecursiveToStringStyle extends ToStringStyle {

        private static final long serialVersionUID = 2509154210044460206L;

        private static final int INFINITE_DEPTH = -1;

        /**
         * Setting {@link #maxDepth} to 0 will have the same effect as using
         * original {@link #ToStringStyle}: it will print all 1st level values
         * without traversing into them. Setting to 1 will traverse up to 2nd
         * level and so on.
         */
        private int maxDepth;

        private int depth;

        public RecursiveToStringStyle() {
            this(INFINITE_DEPTH);
        }

        public RecursiveToStringStyle(int maxDepth) {
            String indent=SystemUtils.LINE_SEPARATOR;
            setContentStart(indent+"{");
            setFieldSeparator(indent + "  ");
            setFieldSeparatorAtStart(true);
            setContentEnd(indent + "}");

            setUseShortClassName(true);
            setUseIdentityHashCode(false);

            this.maxDepth = maxDepth;
        }

        private void updateDepth(int delta){
            String indent;
            if(delta>0){
                indent=SystemUtils.LINE_SEPARATOR+getIndent(depth);
                setContentStart(indent + "{");
            }else{
                indent=SystemUtils.LINE_SEPARATOR+getIndent(depth+delta);
                setContentStart(indent + "{");
            }
            
            depth+=delta;
            indent=SystemUtils.LINE_SEPARATOR+getIndent(depth);
            setFieldSeparator(indent);
            
            if(delta>0){
                indent=SystemUtils.LINE_SEPARATOR+getIndent(depth-delta);
                setContentEnd(indent + "}");
            }else{
                indent=SystemUtils.LINE_SEPARATOR+getIndent(depth+delta);
                setContentEnd(indent + "}");
            }

        }
        @Override
        protected void appendDetail(StringBuffer buffer, String fieldName,
                Object value) {
            if (value.getClass().getName().startsWith("java.lang.")
                    || (maxDepth != INFINITE_DEPTH && depth >= maxDepth)) {
                buffer.append(value);
            } else {
                updateDepth(+1);
                buffer.append(ReflectionToStringBuilder.toString(value, this));
                updateDepth(-1);
            }
        }

        // another helpful method
        @Override
        protected void appendDetail(StringBuffer buffer, String fieldName,
                Collection<?> coll) {
            updateDepth(+1);
            buffer.append(ReflectionToStringBuilder.toString(coll.toArray(),
                    this, true, true));
            updateDepth(-1);
        }
        
        protected String getIndent(int depth){
            StringBuilder sb=new StringBuilder();
            for(int i=-1;i<depth;i++){
                sb.append("  ");
            }
            return sb.toString();
        }
    }

}
