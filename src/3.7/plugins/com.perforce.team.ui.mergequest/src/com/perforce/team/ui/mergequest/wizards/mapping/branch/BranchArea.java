/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.wizards.mapping.branch;

import com.perforce.p4java.core.IBranchMapping;
import com.perforce.p4java.core.IMapEntry.EntryType;
import com.perforce.p4java.core.ViewMap;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.mergequest.model.BranchSpecMapping;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.core.mergequest.model.Mapping;
import com.perforce.team.core.p4java.IP4Branch;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.branches.NewBranchAction;
import com.perforce.team.ui.branches.SelectBranchDialog;
import com.perforce.team.ui.mergequest.wizards.mapping.MappingArea;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistEvent;
import org.eclipse.jface.text.contentassist.ICompletionListener;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineBackgroundEvent;
import org.eclipse.swt.custom.LineBackgroundListener;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchArea extends MappingArea {

    private Composite branchArea;
    private ITextViewer branchText;
    private BranchAssistant branchNameAssist;
    private ISourceViewer sourceViewer;
    private ISourceViewer targetViewer;

    private String name = null;
    private boolean inSesssion = false;

    private ISchedulingRule loadRule = P4Runner.createRule();

    /**
     * @param graph
     * @param connection
     */
    public BranchArea(IBranchGraph graph, IP4Connection connection) {
        super(graph, connection);
    }

    /**
     * @see com.perforce.team.ui.mergequest.wizards.mapping.MappingArea#validateArea()
     */
    @Override
    public String validateArea() {
        String message = null;
        name = branchText.getDocument().get().trim();
        if (message == null && name.length() == 0) {
            message = Messages.BranchArea_EnterSpecName;
        }
        return message;
    }

    /**
     * Get branch name assistant
     * 
     * @return content assistant
     */
    public BranchAssistant getAssistant() {
        return this.branchNameAssist;
    }

    /**
     * @see com.perforce.team.ui.mergequest.wizards.mapping.IMappingArea#createControl(org.eclipse.swt.widgets.Composite,
     *      org.eclipse.jface.wizard.IWizardContainer)
     */
    public void createControl(Composite parent, IWizardContainer container) {
        branchArea = new Composite(parent, SWT.NONE);
        GridLayout bvaLayout = new GridLayout(4, false);
        bvaLayout.marginHeight = 0;
        bvaLayout.marginWidth = 0;
        branchArea.setLayout(bvaLayout);
        branchArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        branchArea.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                resources.dispose();
            }
        });

        createBranchViewArea(this.branchArea, container);

        this.branchText.getTextWidget().selectAll();
        this.branchText.getTextWidget().setFocus();
    }

    private SourceViewer createPathArea(Composite parent, String label) {
        Composite targetPaths = new Composite(parent, SWT.NONE);
        GridLayout tpLayout = new GridLayout(1, true);
        tpLayout.marginHeight = 0;
        tpLayout.marginWidth = 0;
        tpLayout.verticalSpacing = 0;
        targetPaths.setLayout(tpLayout);
        targetPaths.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Label targetPathLabel = new Label(targetPaths, SWT.NONE);
        targetPathLabel.setText(label);
        targetPathLabel.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true,
                false));

        final Color alternate = new Color(parent.getDisplay(), new RGB(232,
                242, 254));
        P4UIUtils.registerDisposal(parent, alternate);
        final SourceViewer targetViewer = new SourceViewer(targetPaths, null,
                SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.READ_ONLY);
        targetViewer.setDocument(new Document());
        targetViewer.getTextWidget().setFont(
                JFaceResources.getFont(JFaceResources.TEXT_FONT));
        targetViewer.getTextWidget().addLineBackgroundListener(
                new LineBackgroundListener() {

                    public void lineGetBackground(LineBackgroundEvent event) {
                        Color background = null;
                        try {
                            int lineNumber = targetViewer.getDocument()
                                    .getLineOfOffset(event.lineOffset);
                            if (lineNumber % 2 != 0) {
                                background = alternate;
                            }
                        } catch (BadLocationException e) {
                            // Ignore
                        }
                        event.lineBackground = background;
                    }
                });
        targetViewer.getTextWidget().addLineStyleListener(
                new LineStyleListener() {

                    public void lineGetStyle(LineStyleEvent event) {
                        if (event.lineText != null
                                && event.lineText.length() > 0
                                && event.lineText.charAt(0) == '-') {
                            StyleRange range = new StyleRange(
                                    event.lineOffset,
                                    event.lineText.length(),
                                    event.display
                                            .getSystemColor(SWT.COLOR_DARK_RED),
                                    null);
                            event.styles = new StyleRange[] { range };
                        }
                    }
                });
        GridData tvData = new GridData(SWT.FILL, SWT.FILL, true, true);
        tvData.verticalIndent = 2;
        tvData.heightHint = P4UIUtils.computePixelHeight(targetViewer
                .getTextWidget().getFont(), 10);
        targetViewer.getTextWidget().setLayoutData(tvData);

        return targetViewer;
    }

    private void createBranchViewArea(Composite parent,
            final IWizardContainer container) {
        Label nameLabel = new Label(parent, SWT.LEFT);
        nameLabel.setText(Messages.BranchArea_BranchSpecName);

        branchText = new TextViewer(parent, SWT.SINGLE | SWT.BORDER);
        addContentAssistDecoration(branchText.getTextWidget());
        branchText.setDocument(new Document());
        GridData btData = new GridData(SWT.FILL, SWT.FILL, true, false);
        branchText.getTextWidget().setLayoutData(btData);
        branchText.getDocument().addDocumentListener(new IDocumentListener() {

            public void documentChanged(DocumentEvent event) {
                validate();
            }

            public void documentAboutToBeChanged(DocumentEvent event) {

            }
        });
        branchText.getTextWidget().addVerifyKeyListener(
                new VerifyKeyListener() {

                    public void verifyKey(VerifyEvent event) {
                        if (!inSesssion
                                && Character.isWhitespace(event.character)) {
                            event.doit = false;
                        }
                    }
                });
        this.branchNameAssist = new BranchAssistant(this.resources,
                this.connection, new Runnable() {

                    public void run() {
                        loadView(branchText.getDocument().get());
                    }
                });
        this.branchNameAssist.addCompletionListener(new ICompletionListener() {

            public void selectionChanged(ICompletionProposal proposal,
                    boolean smartToggle) {
            }

            public void assistSessionStarted(ContentAssistEvent event) {
                inSesssion = true;
            }

            public void assistSessionEnded(ContentAssistEvent event) {
                inSesssion = false;
            }
        });
        this.branchNameAssist.init();
        this.branchNameAssist.install(branchText);

        ToolBar branchBar = new ToolBar(parent, SWT.FLAT | SWT.WRAP);

        final ToolItem refreshButton = new ToolItem(branchBar, SWT.PUSH);
        refreshButton.setImage(this.resources.getImage(PerforceUIPlugin
                .getDescriptor(IPerforceUIConstants.IMG_REFRESH)));
        refreshButton.setToolTipText(Messages.BranchArea_LoadViewToolTip);

        final ToolItem browseButton = new ToolItem(branchBar, SWT.PUSH);
        browseButton.setImage(this.resources.getImage(PerforceUIPlugin
                .getDescriptor(IPerforceUIConstants.IMG_FIND)));
        browseButton.setToolTipText(Messages.BranchArea_Browse);

        final ToolItem newButton = new ToolItem(branchBar, SWT.PUSH);
        newButton.setImage(this.resources.getImage(PerforceUIPlugin
                .getDescriptor(IPerforceUIConstants.IMG_ADD)));
        newButton.setToolTipText(Messages.BranchArea_New);
        newButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    final IP4Branch[] created = new IP4Branch[] { null };
                    container.run(true, false, new IRunnableWithProgress() {

                        public void run(IProgressMonitor monitor)
                                throws InvocationTargetException,
                                InterruptedException {
                            NewBranchAction newAction = new NewBranchAction();
                            newAction.setAsync(false);
                            newAction.setMonitor(monitor);
                            newAction.selectionChanged(null,
                                    new StructuredSelection(connection));
                            newAction.run(null);
                            created[0] = newAction.getCreatedBranch();

                        }
                    });
                    if (created[0] != null) {
                        branchText.getDocument().set(created[0].getName());
                        setView(created[0]);

                    }
                } catch (InvocationTargetException e1) {
                    PerforceProviderPlugin.logError(e1);
                } catch (InterruptedException e1) {
                    PerforceProviderPlugin.logError(e1);
                }

            }
        });

        Composite paths = new Composite(parent, SWT.NONE);
        GridLayout pLayout = new GridLayout(3, false);
        pLayout.marginWidth = 0;
        pLayout.marginHeight = 0;
        pLayout.horizontalSpacing = 1;
        paths.setLayout(pLayout);
        GridData pData = new GridData(SWT.FILL, SWT.FILL, true, true);
        pData.horizontalSpan = ((GridLayout) parent.getLayout()).numColumns;
        paths.setLayoutData(pData);

        Group sourceArea = new Group(paths, SWT.NONE);
        sourceArea.setText(Messages.BranchArea_Source);
        GridLayout saLayout = new GridLayout(1, true);
        saLayout.marginHeight = 2;
        saLayout.marginWidth = 2;
        sourceArea.setLayout(saLayout);
        sourceArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        this.sourceViewer = createPathArea(sourceArea,
                Messages.BranchArea_DepotPaths);

        createDirectionArea(paths, SWT.VERTICAL);

        Group targetArea = new Group(paths, SWT.BORDER);
        targetArea.setText(Messages.BranchArea_Target);
        GridLayout taLayout = new GridLayout(1, true);
        taLayout.marginHeight = 2;
        taLayout.marginWidth = 2;
        targetArea.setLayout(taLayout);
        targetArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        this.targetViewer = createPathArea(targetArea,
                Messages.BranchArea_DepotPaths);

        createSourceBranchArea(sourceArea, Messages.BranchArea_BranchName);
        createTargetBranchArea(targetArea, Messages.BranchArea_BranchName);

        refreshButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                loadView(branchText.getDocument().get());
            }

        });

        browseButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                SelectBranchDialog dialog = new SelectBranchDialog(browseButton
                        .getParent().getShell(), connection);
                if (SelectBranchDialog.OK == dialog.open()) {
                    IP4Branch selected = dialog.getSelected();
                    if (selected != null && selected.getName() != null) {
                        branchText.getDocument().set(selected.getName());
                        validate();
                        setView(selected);
                    }
                }
            }

        });

        loadCurrentMapping();
    }

    /**
     * Load current mapping into text area and load view
     */
    protected void loadCurrentMapping() {
        if (mapping != null) {
            String name = mapping.getName();
            if (name != null && name.length() > 0) {
                this.branchText.getDocument().set(name);
                loadView(name);
            }
        }
    }

    /**
     * @param string
     */
    private void loadView(final String name) {
        if (name == null || name.length() == 0) {
            return;
        }
        P4Runner.schedule(new P4Runnable() {

            @Override
            public String getTitle() {
                return MessageFormat.format(Messages.BranchArea_LoadingSpec,
                        name);
            }

            @Override
            public void run(IProgressMonitor monitor) {
                final IP4Branch branch = connection.getBranch(name);
                PerforceUIPlugin.syncExec(new Runnable() {

                    public void run() {
                        if (P4UIUtils.okToUse(branchArea)) {
                            setView(branch);
                        }
                    }
                });
            }

        }, loadRule);
    }

    private void setView(IP4Branch branch) {
        if (branch != null && branch.getName() != null
                && branch.getUpdateTime() != null) {
            StringBuilder source = new StringBuilder();
            StringBuilder target = new StringBuilder();
            ViewMap<IBranchMapping> mappings = branch.getView();
			if (mappings != null) {
				for (IBranchMapping mapping : mappings) {
					if (mapping.getType() == EntryType.EXCLUDE) {
						source.append('-');
						target.append('-');
					}
					source.append(mapping.getLeft(true));
					source.append('\n');
					target.append(mapping.getRight(true));
					target.append('\n');
				}
			}
			sourceViewer.getDocument().set(source.toString());
			targetViewer.getDocument().set(target.toString());
        } else {
            sourceViewer.getDocument().set(""); //$NON-NLS-1$
            targetViewer.getDocument().set(""); //$NON-NLS-1$
        }
    }

    /**
     * Get main control
     * 
     * @return - composite
     */
    public Composite getControl() {
        return this.branchArea;
    }

    /**
     * @see com.perforce.team.ui.mergequest.wizards.mapping.MappingArea#createMapping()
     */
    @Override
    public Mapping createMapping() {
        BranchSpecMapping mapping = this.graph.createBranchSpecMapping(null);
        mapping.setName(name);
        mapping.setDirection(getDirection());
        return mapping;
    }

}
