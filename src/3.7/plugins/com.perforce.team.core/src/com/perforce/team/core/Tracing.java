package com.perforce.team.core;

import java.text.MessageFormat;
import java.util.Calendar;


/**
 * <p>
 * A utility class for printing tracing output to the console, and log the message to eclipse log.
 * 
 * @since 2012.2
 */
public final class Tracing {

	/**
	 * The separator to place between the component and the message.
	 */
	public static final String SEPARATOR = " >>> "; //$NON-NLS-1$

	public static String getTimestamp(){
        String timestamp = ""; //$NON-NLS-1$
        Calendar cal = Calendar.getInstance();
        
        timestamp = String
                .format("%1$tI:%1$tM:%1$tS:%1$tL %1$tp ", cal).toUpperCase(); //$NON-NLS-1$
        return timestamp;
	}
	/**
	 * <p>
	 * Prints a tracing message to standard out. The message is prefixed by a
	 * component identifier and some separator. See the example below.
	 * </p>
	 * 
	 * <pre>
	 *        BINDINGS &gt;&gt; There are 4 deletion markers
	 * </pre>
	 * 
	 * @param component
	 *            The component for which this tracing applies; may be
	 *            <code>null</code>
	 * @param message
	 *            The message to print to standard out; may be <code>null</code>.
	 */
	public static final void printTrace(final boolean debug, final String component,
			final String message) {
		if(debug){
			String output = constructMessage(component,message);
			System.out.println("TRACE "+output);//$NON-NLS-1$
//			PerforceProviderPlugin.logInfo(getTimestamp()+" "+output);
		}
	}

	public static final void printTrace(final String component,
			final String message) {
		printTrace(Policy.DEBUG,component,message);
	}

	private static String constructMessage(String component, String message) {
		StringBuffer buffer = new StringBuffer();
		if (component != null) {
			buffer.append(component);
		}
		if ((component != null) && (message != null)) {
			buffer.append(SEPARATOR);
		}
		if (message != null) {
			buffer.append(message);
		}
		String output = buffer.toString();
		return output;
	}

	public static final void printExecTime(final boolean debug, final String component, final String message, Runnable runnable){
		if(debug){
			String msg = constructMessage(component, message);
			long start = System.nanoTime();
//			System.out.println(MessageFormat.format("EXEC  {0} START [{1}]",msg,start)); //$NON-NLS-1$
			System.out.println(MessageFormat.format("EXEC  START v {0} [{1}]",start,msg)); //$NON-NLS-1$
			runnable.run();
			long end = System.nanoTime();
			System.out.println(MessageFormat.format("EXEC  END   ^ {0} [{1}] time={2}ns",start,msg,end-start)); //$NON-NLS-1$
//			System.out.println(MessageFormat.format("EXEC  {0} END [{1}] time={2}ns",msg,start,end-start)); //$NON-NLS-1$
			System.out.println();
		}else
			runnable.run();
	}

	public static final void printExecTime2(final boolean debug, final String component, final String message, IRunnable runnable) throws Throwable {
		if(debug){
			String msg = constructMessage(component, message);
			long start = System.nanoTime();
//			System.out.println(MessageFormat.format("EXEC  {0} START [{1}]",msg,start)); //$NON-NLS-1$
			System.out.println(MessageFormat.format("EXEC  START v {0} [{1}]",start,msg)); //$NON-NLS-1$
			runnable.run();
			long end = System.nanoTime();
			System.out.println(MessageFormat.format("EXEC  END   ^ {0} [{1}] time={2}ns",start,msg,end-start)); //$NON-NLS-1$
//			System.out.println(MessageFormat.format("EXEC  {0} END [{1}] time={2}ns",msg,start,end-start)); //$NON-NLS-1$
			System.out.println();
		}else
			runnable.run();
	}
	
	public static final void printExecTime3(final boolean debug, final String component, final String message, IRunnable runnable) {
		try {
			printExecTime2(debug, component, message, runnable);
		} catch (Throwable t) {
		}
	}
	
	/**
	 * This class is not intended to be instantiated.
	 */
	private Tracing() {
		// Do nothing.
	}
	
	public interface IRunnable{
		public void run() throws Throwable;
	}
}
