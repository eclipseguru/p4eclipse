package com.perforce.team.ui.parts;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.operations.TimeTriggeredProgressMonitorDialog;
import org.eclipse.ui.statushandlers.StatusManager;

import com.perforce.team.ui.Messages;
import com.perforce.team.ui.PerforceUIPlugin;

/**
 * A dialog that is intended for showing feedback about a specific operation.
 */
@SuppressWarnings("restriction")
public class OperationProgressMonitorDialog extends TimeTriggeredProgressMonitorDialog {

	/**
	 * Creates a new instance.
	 *
	 * @param parent the parent shell
	 */
	public OperationProgressMonitorDialog(final Shell parent) {
		this(parent, PlatformUI.getWorkbench().getProgressService().getLongOperationTime());
	}

	/**
	 * Creates a new instance.
	 *
	 * @param parent            the parent shell
	 * @param longOperationTime the operation time when the busy cursor should be
	 *                          replaced with a dialog
	 */
	protected OperationProgressMonitorDialog(final Shell parent, final int longOperationTime) {
		super(parent, longOperationTime);
		setShellStyle(getDefaultOrientation() | SWT.BORDER | SWT.TITLE | SWT.MODELESS);
		setCancelable(true);
	}

	/**
	 * Runs the given {@link IRunnableWithProgress} in the background.
	 *
	 * @param runnableWithProgress
	 */
	public void runInBackground(final IRunnableWithProgress runnableWithProgress) {
		try {
			run(true, true, runnableWithProgress);
		} catch (final OperationCanceledException | InterruptedException e) {
			// cancelled
		} catch (final InvocationTargetException | RuntimeException e) {
			final Throwable targetException = e instanceof InvocationTargetException
					? ((InvocationTargetException) e).getTargetException()
					: e;
			StatusManager.getManager()
					.handle(new Status(IStatus.ERROR, PerforceUIPlugin.ID,
							Messages.OperationProgressMonitorDialog_OperationFailed, targetException),
							StatusManager.LOG | StatusManager.SHOW);
		}
	}

}