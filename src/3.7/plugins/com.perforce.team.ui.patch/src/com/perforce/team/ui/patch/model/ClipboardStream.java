/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.patch.model;

import com.perforce.p4java.CharsetDefs;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ClipboardStream implements IPatchStream {

    private ByteArrayOutputStream stream = null;

    /**
     * @see com.perforce.team.ui.patch.model.IPatchStream#initialize(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void initialize(IProgressMonitor monitor) {
        stream = new ByteArrayOutputStream();
    }

    /**
     * @see com.perforce.team.ui.patch.model.IPatchStream#getStream()
     */
    public OutputStream getStream() {
        return stream;
    }

    /**
     * @see com.perforce.team.ui.patch.model.IPatchStream#finish(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void finish(IProgressMonitor monitor) {
        if (stream != null) {
			try {
				final String clipboardContents = stream.toString(CharsetDefs.DEFAULT_NAME);
				Runnable runnable = new Runnable() {
					
					public void run() {
						P4UIUtils.copyToClipboard(clipboardContents);
					}
				};
				if (PerforceUIPlugin.isUIThread()) {
					runnable.run();
				} else {
					PerforceUIPlugin.asyncExec(runnable);
				}
			} catch (UnsupportedEncodingException e) {
				PerforceProviderPlugin.logError(e);
			}
            stream = null;
        }
    }

    /**
     * @see com.perforce.team.ui.patch.model.IPatchStream#getCharset()
     */
    public Charset getCharset() {
        return CharsetDefs.LOCAL;
    }
}
