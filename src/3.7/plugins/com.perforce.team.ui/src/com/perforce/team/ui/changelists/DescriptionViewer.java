/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.changelists;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.editor.ContentAssistRegistry;
import com.perforce.team.ui.editor.IContentAssistProvider;
import com.perforce.team.ui.p4java.dialogs.ChangeSpecDialog;
import com.perforce.team.ui.p4java.dialogs.ChangelistDescriptionAssistant;
import com.perforce.team.ui.p4java.dialogs.WrappingPainter;
import com.perforce.team.ui.preferences.IPreferenceConstants;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.progress.UIJob;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class DescriptionViewer {

    /**
     * Update the styled text widget with the correct description editor font
     * 
     * @param text
     */
    public static void updateFont(StyledText text) {
        if (P4UIUtils.okToUse(text)) {
            if (PerforceUIPlugin.getPlugin().getPreferenceStore()
                    .getBoolean(IPreferenceConstants.DESCRIPTION_EDITOR_FONT)) {
                text.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));
            } else {
                text.setFont(JFaceResources.getFont(JFaceResources.DIALOG_FONT));
            }
        }
    }

    private IP4Resource resource;
    private IP4File[] files = new IP4File[0];
    private IP4Job[] jobs = new IP4Job[0];
    private String context = null;
    private TextViewer viewer;
    private StyledText textWidget = null;
    private ChangelistDescriptionAssistant assistant;

    /**
     * Create a description text viewer
     */
    public DescriptionViewer() {
        this(null);
    }

    /**
     * Create a description text viewer
     * 
     * @param resource
     */
    public DescriptionViewer(IP4Resource resource) {
        this(ChangeSpecDialog.PENDING_CONTEXT, resource, null, null);
    }

    /**
     * Create a description text viewer
     * 
     * @param context
     * @param resource
     * @param files
     * @param jobs
     */
    public DescriptionViewer(String context, IP4Resource resource,
            IP4File[] files, IP4Job[] jobs) {
        this.context = context;
        this.resource = resource;
        if (jobs != null) {
            this.jobs = jobs;
        }
        if (files != null) {
            this.files = files;
        }
    }

    private void configureFont(final StyledText text) {
        IPreferenceStore store = PerforceUIPlugin.getPlugin()
                .getPreferenceStore();
        updateFont(text);
        final IPropertyChangeListener listener = new IPropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent event) {
                if (IPreferenceConstants.DESCRIPTION_EDITOR_FONT.equals(event
                        .getProperty())) {
                    UIJob updateFont = new UIJob(
                            Messages.DescriptionViewer_UpdateEditorFont) {

                        @Override
                        public IStatus runInUIThread(IProgressMonitor monitor) {
                            updateFont(text);
                            return Status.OK_STATUS;
                        }
                    };
                    updateFont.schedule();
                }
            }
        };
        store.addPropertyChangeListener(listener);
        text.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                PerforceUIPlugin.getPlugin().getPreferenceStore()
                        .removePropertyChangeListener(listener);
            }
        });
    }

    private void installContentAssist(final TextViewer viewer,
            final IP4File[] files, final IP4Job[] jobs) {
        this.assistant = new ChangelistDescriptionAssistant();
        P4Runner.schedule(new P4Runnable() {

            @Override
            public String getTitle() {
                return Messages.DescriptionViewer_GeneratingContentAssist;
            }

            @Override
            public void run(IProgressMonitor monitor) {
                IContentAssistProvider[] providers = ContentAssistRegistry
                        .getRegistry().getProviders(context);
                for (IContentAssistProvider provider : providers) {
                    assistant.addProposals(provider.getProposals(resource,
                            DescriptionViewer.this));
                }
                List<DescriptionTemplate> descriptions = DescriptionTemplate
                        .getHistory();
                for (DescriptionTemplate tpl : DescriptionTemplate
                        .getTemplates()) {
                    descriptions.add(tpl);
                }
                assistant.init(files, jobs, descriptions
                        .toArray(new DescriptionTemplate[descriptions.size()]));
                PerforceUIPlugin.syncExec(new Runnable() {

                    public void run() {
                        if (P4UIUtils.okToUse(viewer.getTextWidget())) {
                            assistant.install(viewer);
                        }
                    }
                });
            }
        });

    }

    /**
     * Create viewer control for specified parent and initial text content
     * 
     * @param parent
     * @param text
     */
    public void createControl(Composite parent, String text) {
        viewer = new TextViewer(parent, SWT.BORDER | SWT.MULTI | SWT.WRAP
                | SWT.V_SCROLL);
        textWidget = viewer.getTextWidget();
        configureFont(textWidget);
        viewer.setDocument(new Document());

        GridData deData = new GridData(SWT.FILL, SWT.FILL, true, false);
        deData.heightHint = Math.max(80,
                P4UIUtils.computePixelHeight(textWidget.getFont(), 8));
        textWidget.setLayoutData(deData);

        new WrappingPainter(viewer);

        if (text == null) {
            text = ""; //$NON-NLS-1$
        }
        viewer.getDocument().set(text);
        textWidget.selectAll();

        installContentAssist(viewer, files, jobs);
    }

    /**
     * Get underlying text viewer
     * 
     * @return - text viewer
     */
    public TextViewer getViewer() {
        return this.viewer;
    }

    /**
     * Set text of text widget if text is non-null
     * 
     * @param text
     */
    public void setText(String text) {
        if (text != null && P4UIUtils.okToUse(textWidget)) {
            textWidget.setText(text);
        }
    }

    /**
     * Set focus to text widget
     */
    public void setFocus() {
        if (P4UIUtils.okToUse(textWidget)) {
            textWidget.setFocus();
        }
    }

    /**
     * Get viewer description
     * 
     * @return = description
     */
    public String getDescription() {
        if (viewer != null) {
            return viewer.getDocument().get();
        } else {
            return ""; //$NON-NLS-1$
        }
    }

    /**
     * Get viewer document
     * 
     * @return - document
     */
    public IDocument getDocument() {
        return this.viewer.getDocument();
    }

    /**
     * Get control of viewer
     * 
     * @return - control
     */
    public Control getControl() {
        return viewer.getControl();
    }

    /**
     * Get changelist description assistant
     * 
     * @return - assistant
     */
    public ChangelistDescriptionAssistant getAssistant() {
        return this.assistant;
    }

    /**
     * Update templates present in content assist
     * 
     * @param templates
     */
    public void updateTemplates(Collection<DescriptionTemplate> templates) {
        if (templates != null) {
            updateTemplates(templates.toArray(new DescriptionTemplate[templates
                    .size()]));
        }
    }

    /**
     * Update templates present in content assist
     * 
     * @param templates
     */
    public void updateTemplates(DescriptionTemplate[] templates) {
        if (this.assistant != null && templates != null) {
            this.assistant.updateTemplates(templates);
        }
    }

    /**
     * Update jobs present in content assist
     * 
     * @param jobs
     */
    public void updateJobs(Collection<IP4Job> jobs) {
        if (jobs != null) {
            updateJobs(jobs.toArray(new IP4Job[jobs.size()]));
        }
    }

    /**
     * Update jobs present in content assist
     * 
     * @param jobs
     */
    public void updateJobs(IP4Job[] jobs) {
        if (this.assistant != null && jobs != null) {
            this.assistant.updateJobs(jobs);
        }
    }

}
