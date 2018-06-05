package com.perforce.team.tests.streams;

import java.text.MessageFormat;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.core.runtime.Assert;
import org.junit.Test;

import com.perforce.p4java.impl.generic.core.StreamSummary;
import com.perforce.p4java.impl.generic.core.StreamSummary.Options;
import com.perforce.team.core.p4java.P4Connection;
import com.perforce.team.ui.streams.StreamUtil;

/**
 * Test for general purpose helper functions.
 */
public class UtilTest extends TestCase {
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    /**
     * @throws Exception
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    public void tearDown() throws Exception {
        try {

        } finally {
            super.tearDown();
        }
    }

    @Test
    public void testParserServerVersion(){
        String info="P4D/LINUX26X86_64/2011.1.MAIN-TEST_ONLY/370818 (2011/10/19)";
        int[] ver=getServerVersion(info);
        assertEquals(2011,ver[0]);
        assertEquals(1, ver[1]);

        info="P4D/LINUX26X86_64/2011.1/370818 (2011/10/19)";
        ver=getServerVersion(info);
        assertEquals(2011,ver[0]);
        assertEquals(1, ver[1]);

        info="P4D/LINUX26X86_64/2012.1/370818 (2011/10/19)";
        ver=getServerVersion(info);
        assertEquals(2012,ver[0]);
        assertEquals(1, ver[1]);
    
    }   

    /**
     * P4D/LINUX26X86_64/2011.1.MAIN-TEST_ONLY/370818 (2011/10/19)
     * @return Major and minor array, for example, return  int[]{2011,1} for server info P4D/LINUX26X86_64/2011.1/370818 (2011/10/19).
     */
    public static int[] getServerVersion(String serverInfo){
        int[] result=new int[2];
        Assert.isTrue(StringUtils.isNotEmpty(serverInfo));
        String[] list = serverInfo.split("/",4);
        Pattern reg=Pattern.compile("\\d{4}.\\d.*");
        if(reg.matcher(list[2]).matches()){
            String[] versions = list[2].split("\\.");
            result[0]=Integer.parseInt(versions[0]);
            result[1]=Integer.parseInt(versions[1]);
        }
        return result;
    }
    
    @Test
    public void testClone(){
        Options opt=new StreamSummary.Options(true, true, true, false);
//        Options newOpt = ObjectUtils.clone(opt);
        Options newOpt = new Options(true,true, true,false);
        Options[] a=new Options[]{opt};
        Options[] b=new Options[]{newOpt};
        assertNotSame(opt, newOpt);
        String s = ReflectionToStringBuilder.toString(a,ToStringStyle.SHORT_PREFIX_STYLE);
        String newS = ReflectionToStringBuilder.toString(b,ToStringStyle.SHORT_PREFIX_STYLE);
        System.out.println(s);
        System.out.println(newS);
        assertTrue(s.equals(newS));
    }
    
    @Test
    public void testNormalizePath(){
        String path="//new/mystream";
        
        String p = StreamUtil.normalizePath(path);
        assertEquals(path, p);
        
        path="/////new/mystream";
        p = StreamUtil.normalizePath(path);
        assertEquals("//new/mystream", p);
        
        path="/////new/mystream/";
        p = StreamUtil.normalizePath(path);
        assertEquals("//new/mystream", p);

        path="/////";
        p = StreamUtil.normalizePath(path);
        assertEquals("//", p);

        path="/";
        p = StreamUtil.normalizePath(path);
        assertEquals("//", p);

        path="//new/p////";
        p = StreamUtil.normalizePath(path);
        assertEquals("//new/p", p);

        path="new/p////";
        p = StreamUtil.normalizePath(path);
        assertEquals("//new/p", p);

        path="//new";
        p = StreamUtil.normalizePath(path);
        assertEquals("//new", p);

    }
    
    @Test
    public void testStringFormat(){
        String pattern="Loading Changelist {0,number,#}";
        String msg = MessageFormat.format(pattern, 1011);
        String expected="Loading Changelist 1011";
        assertEquals(expected, msg);
        
        pattern="{0}@{1} - {2,choice,0#default change|0<change {2,number,'#'}}";
        msg = MessageFormat.format(pattern, "ali","perforce",0);
        expected="ali@perforce - default change";
        assertEquals(expected, msg);

        try{
            pattern="{0}@{1} - {2,choice,0#default change|0<change {2,number,#}}";
            msg = MessageFormat.format(pattern, "ali","perforce",0);
            assertTrue(false);
        }catch(Throwable t){
            assertTrue(t instanceof IllegalArgumentException);
        }
        
    }
    
    public void testConvertDiskLabel(){
    	String s="C:\\Abc\\DEF";
    	String cs = P4Connection.convertDiskLabel(s);
    	if(P4Connection.shouldConvertPath(s)){
    		assertEquals("c:\\Abc\\DEF", cs);
    	}else{
    		assertEquals(s, cs);
    	}
    }
}
