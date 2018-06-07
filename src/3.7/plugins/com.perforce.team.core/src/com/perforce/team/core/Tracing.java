package com.perforce.team.core;

import java.text.MessageFormat;
import java.util.Calendar;

import org.eclipse.core.runtime.ISafeRunnable;

/**
 * <p>
 * A utility class for printing tracing output to the console, and log the
 * message to eclipse log.
 *
 * @since 2012.2
 */
public final class Tracing {

	/**
	 * The separator to place between the component and the message.
	 */
	public static final String SEPARATOR = " >>> "; //$NON-NLS-1$

	public static String getTimestamp() {
		String timestamp = ""; //$NON-NLS-1$
		Calendar cal = Calendar.getInstance();

		timestamp = String.format("%1$tI:%1$tM:%1$tS:%1$tL %1$tp ", cal).toUpperCase(); //$NON-NLS-1$
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
	 * @param component The component for which this tracing applies; may be
	 *                  <code>null</code>
	 * @param message   The message to print to standard out; may be
	 *                  <code>null</code>.
	 */
	public static final void printTrace(final boolean debug, final String component, final String message) {
		if (debug) {
			String output = constructMessage(component, message);
			System.out.println("TRACE " + output);//$NON-NLS-1$
//			PerforceProviderPlugin.logInfo(getTimestamp()+" "+output);
		}
	}

	public static final void printTrace(final String component, final String message) {
		printTrace(Policy.DEBUG, component, message);
	}

	private static String constructMessage(String component, String messageFormat, Object... messageArguments) {
		StringBuilder buffer = new StringBuilder();
		if (component != null) {
			buffer.append(component);
		}
		if ((component != null) && (messageFormat != null)) {
			buffer.append(SEPARATOR);
		}
		if (messageFormat != null) {
			buffer.append(MessageFormat.format(messageFormat, messageArguments));
		}
		String output = buffer.toString();
		return output;
	}

	public static final void printExecTime(ISafeRunnable r, final String component, final String messageFormat,
			Object... messageArguments) {
		try {
			printExecTime2(r, component, messageFormat, messageArguments);
		} catch (Exception | LinkageError | AssertionError e) {
			PerforceProviderPlugin.logError(e);
		}
	}

	public static final void printExecTime2(ISafeRunnable r, final String component, final String messageFormat,
			Object... messageArguments) throws Exception {
		if (Policy.DEBUG) {
			String msg = constructMessage(component, messageFormat, messageArguments);
			long start = System.nanoTime();
			System.out.println(MessageFormat.format("EXEC  START v {0} [{1}]", start, msg)); //$NON-NLS-1$
			r.run();
			long end = System.nanoTime();
			System.out.println(MessageFormat.format("EXEC  END   ^ {0} [{1}] time={2}ns", start, msg, end - start)); //$NON-NLS-1$
			System.out.println();
		} else {
			r.run();
		}
	}

	/**
	 * This class is not intended to be instantiated.
	 */
	private Tracing() {
		// Do nothing.
	}

	public interface IRunnable {
		public void run() throws Throwable;
	}
}
