/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.search.editor;

import com.perforce.p4java.core.IFileLineMatch.MatchType;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4ShelvedChangelist;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.core.p4java.P4ShelveFile;
import com.perforce.team.core.search.query.QueryOptions;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4FormUIUtils;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.search.P4UiSearchPlugin;
import com.perforce.team.ui.search.query.P4SearchQuery;
import com.perforce.team.ui.search.query.SearchOptionsArea;
import com.perforce.team.ui.search.results.FileMatch;
import com.perforce.team.ui.search.results.ISearchResultProvider;
import com.perforce.team.ui.search.results.P4SearchResult;
import com.perforce.team.ui.search.results.RevisionMatch;
import com.perforce.team.ui.search.results.SearchLabelDecorator;
import com.perforce.team.ui.search.results.SearchResultsOpenHandler;
import com.perforce.team.ui.search.results.tree.SearchTreeResultsSorter;
import com.perforce.team.ui.search.results.tree.SearchTreeContentProvider;
import com.perforce.team.ui.search.results.tree.SearchTreeLabelProvider;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.search.ui.text.Match;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.model.WorkbenchAdapter;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ChangelistSearchPage extends FormPage implements
        ISearchResultProvider, IEditorPart {

    private static class NoResults extends WorkbenchAdapter {

        private String text;

        /**
         * Create new no results object for search text
         * 
         * @param searchText
         */
        public NoResults(String searchText) {
            this.text = searchText;
        }

        /**
         * @see org.eclipse.ui.model.WorkbenchAdapter#getLabel(java.lang.Object)
         */
        @Override
        public String getLabel(Object object) {
            return MessageFormat.format(
                    Messages.ChangelistSearchPage_NoSearchResults, this.text);
        }

    }

    private Section searchSection;
    private Text searchText;
    private Button searchButton;

    private SearchOptionsArea optionsArea;
    private Section resultsSection;
    private TreeViewer resultsViewer;

    private P4SearchResult result;
    private int fileCount = 0;
    private int matchCount = 0;
    private ISchedulingRule searchRule = P4Runner.createRule();

    /**
     * ID - editor page id
     */
    public static final String ID = "changelistSearchPage"; //$NON-NLS-1$

    /**
     * @param editor
     */
    public ChangelistSearchPage(FormEditor editor) {
        super(editor, ID, Messages.ChangelistSearchPage_SearchPageTitle);
    }

    /**
     * @see org.eclipse.ui.forms.editor.FormPage#setActive(boolean)
     */
    @Override
    public void setActive(boolean active) {
        super.setActive(active);
        if (active) {
            setFocus();
        }
    }

    /**
     * @see org.eclipse.ui.forms.editor.FormPage#setFocus()
     */
    @Override
    public void setFocus() {
        searchText.setFocus();
    }

    /**
     * @see org.eclipse.ui.forms.editor.FormPage#createFormContent(org.eclipse.ui.forms.IManagedForm)
     */
    @Override
    protected void createFormContent(IManagedForm managedForm) {
        FormToolkit toolkit = managedForm.getToolkit();
        Composite body = managedForm.getForm().getBody();
        body.setLayout(new GridLayout(1, true));

        createSearchArea(body, toolkit);
        createOptionsArea(body, toolkit);
        createResultsArea(body, toolkit);

    }

    private void createOptionsArea(Composite parent, final FormToolkit toolkit) {
        Section optionsSection = toolkit.createSection(parent,
                ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
        optionsSection.setExpanded(false);
        optionsSection
                .setText(Messages.ChangelistSearchPage_SearchOptionsTitle);
        GridLayout osLayout = new GridLayout(1, true);
        osLayout.marginHeight = 0;
        osLayout.marginWidth = 0;
        optionsSection.setLayout(osLayout);
        optionsSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                false));

        Composite area = toolkit.createComposite(optionsSection);
        GridLayout areaLayout = new GridLayout(1, true);
        areaLayout.marginHeight = 0;
        areaLayout.marginWidth = 0;
        area.setLayout(areaLayout);
        area.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        optionsSection.setClient(area);

        this.optionsArea = new SearchOptionsArea() {

            @Override
            protected Button createButton(Composite parent, String text,
                    int style) {
                return toolkit.createButton(parent, text, style);
            }

        };
        this.optionsArea.createControl(area, false);
    }

    private void createSearchArea(Composite parent, FormToolkit toolkit) {
        searchSection = toolkit.createSection(parent,
                ExpandableComposite.TITLE_BAR | Section.DESCRIPTION);
        searchSection
                .setDescription(Messages.ChangelistSearchPage_SearchExpressionDescription);
        searchSection
                .setText(Messages.ChangelistSearchPage_SearchExpressionTitle);
        searchSection.setExpanded(true);
        GridLayout sLayout = new GridLayout(1, true);
        sLayout.marginWidth = 0;
        sLayout.marginHeight = 0;
        searchSection.setLayout(sLayout);
        searchSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                false));

        Composite searchArea = toolkit.createComposite(searchSection);
        GridLayout saLayout = new GridLayout(3, false);
        saLayout.marginHeight = 0;
        saLayout.marginWidth = 0;
        searchArea.setLayout(saLayout);
        searchArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        searchSection.setClient(searchArea);

        toolkit.createLabel(searchArea, Messages.ChangelistSearchPage_Pattern);
        searchText = toolkit.createText(searchArea, "", SWT.SINGLE | SWT.FLAT); //$NON-NLS-1$
        GridData stData = new GridData();
        stData.widthHint = 250;
        searchText.setLayoutData(stData);
        searchText.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.character == '\r' && e.keyCode == '\r') {
                    if (searchButton.isEnabled()) {
                        search();
                    }
                }
            }
        });
        searchText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                searchButton.setEnabled(searchText.getText().length() > 0);
            }
        });

        searchButton = toolkit.createButton(searchArea,
                Messages.ChangelistSearchPage_Search, SWT.PUSH);
        Image searchImage = P4UiSearchPlugin.getDescriptor(
                "icons/depot_search.png").createImage(); //$NON-NLS-1$
        P4UIUtils.registerDisposal(searchButton, searchImage);
        searchButton.setImage(searchImage);
        searchButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                search();
            }

        });
        searchButton.setEnabled(false);
    }

    private void setEnabled(boolean enabled) {
        searchButton.setEnabled(enabled);
    }

    private void createResultsArea(Composite parent, FormToolkit toolkit) {
        resultsSection = toolkit.createSection(parent,
                ExpandableComposite.TITLE_BAR);
        resultsSection.setExpanded(true);

        GridLayout rsLayout = new GridLayout(1, true);
        rsLayout.marginHeight = 0;
        rsLayout.marginWidth = 0;
        resultsSection.setLayout(rsLayout);
        resultsSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                true));

        Composite area = toolkit.createComposite(resultsSection);
        GridLayout areaLayout = new GridLayout(1, true);
        areaLayout.marginHeight = 0;
        areaLayout.marginWidth = 0;
        area.setLayout(areaLayout);
        area.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        resultsSection.setClient(area);

        resultsViewer = new TreeViewer(toolkit.createTree(area, SWT.SINGLE));
        resultsViewer.setAutoExpandLevel(2);

        SearchTreeLabelProvider searchLabelProvider = new SearchTreeLabelProvider(
                this, parent.getDisplay()) {

            @Override
            public String getText(Object element) {
                if (element instanceof RevisionMatch) {
                    return ((RevisionMatch) element).getDepotPath();
                }
                return super.getText(element);
            }

            @Override
            public Image getImage(Object element) {
                if (element instanceof RevisionMatch) {
                    String name = ((RevisionMatch) element).getFile().getName();
                    ImageDescriptor desc = PlatformUI.getWorkbench()
                            .getEditorRegistry().getImageDescriptor(name);
                    if (desc != null) {
                        return (Image) imageManager.get(desc);
                    }
                }
                return super.getImage(element);
            }

        };

        SearchTreeContentProvider searchContentProvider = new SearchTreeContentProvider(
                resultsViewer) {

            @Override
            public Object[] getElements(Object inputElement) {
                if (results.getMatchCount() > 0) {
                    return results.getElements();
                } else {
                    return new Object[] { new NoResults(
                            ((P4SearchQuery) results.getQuery()).getText()) };
                }
            }

        };

        IBaseLabelProvider labelProvider = new DecoratingStyledCellLabelProvider(
                searchLabelProvider, new SearchLabelDecorator(), null);
        resultsViewer.setLabelProvider(labelProvider);
        resultsViewer.setContentProvider(searchContentProvider);
        resultsViewer.getTree().setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, true, true));
        resultsViewer.setSorter(new SearchTreeResultsSorter() {

            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
                if (e1 instanceof RevisionMatch && e2 instanceof RevisionMatch) {
                    return ((RevisionMatch) e1).getDepotPath()
                            .compareToIgnoreCase(
                                    ((RevisionMatch) e2).getDepotPath());
                }
                return super.compare(viewer, e1, e2);
            }

        });
        resultsViewer.addDoubleClickListener(new SearchResultsOpenHandler());

        ToolBar toolbar = P4FormUIUtils.createSectionToolbar(toolkit,
                resultsSection);

        ToolItem collapse = new ToolItem(toolbar, SWT.PUSH);
        collapse.setToolTipText(Messages.ChangelistSearchPage_CollapseAll);
        Image collapseImage = PerforceUIPlugin.getDescriptor(
                IPerforceUIConstants.IMG_COLLAPSE).createImage();
        collapse.setImage(collapseImage);
        P4UIUtils.registerDisposal(collapse, collapseImage);
        collapse.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                resultsViewer.collapseAll();
            }

        });

        updateTitle();
    }

    private void updateTitle() {
        if (result != null) {
            String fileInfo = null;
            String matchInfo = null;
            if (fileCount != 1) {
                fileInfo = MessageFormat.format(
                        Messages.ChangelistSearchPage_MultipleFiles, fileCount);
            } else {
                fileInfo = Messages.ChangelistSearchPage_SingleFile;
            }
            if (matchCount != 1) {
                matchInfo = MessageFormat.format(
                        Messages.ChangelistSearchPage_MultipleMatches,
                        matchCount);
            } else {
                matchInfo = Messages.ChangelistSearchPage_SingleMatch;
            }
            resultsSection
                    .setText(Messages.ChangelistSearchPage_SearchResultsTitle
                            + fileInfo + matchInfo);
        } else {
            resultsSection
                    .setText(Messages.ChangelistSearchPage_SearchResultsTitle);
        }
        resultsSection.layout();
    }

    private IP4Changelist getChangelist() {
        return P4CoreUtils.convert(getEditorInput(), IP4Changelist.class);
    }

    private void search() {
        final IP4Changelist list = getChangelist();
        if (list == null) {
            return;
        }
        String text = this.searchText.getText();
        if (text.length() == 0) {
            return;
        }
        setEnabled(false);
        final QueryOptions options = new QueryOptions(text);
        optionsArea.fillOptions(options);
        P4Runner.schedule(new P4Runnable() {

            @Override
            public String getTitle() {
                return MessageFormat.format(
                        Messages.ChangelistSearchPage_SearchChangelist,
                        Integer.toString(list.getId()));
            }

            @Override
            public void run(IProgressMonitor monitor) {
                String suffix = ""; //$NON-NLS-1$
                if (list instanceof IP4SubmittedChangelist) {
                    suffix = "@" + list.getId(); //$NON-NLS-1$
                } else if (list instanceof IP4ShelvedChangelist) {
                    suffix = P4ShelveFile.SHELVE_SPECIFIER + list.getId();
                }
                for (IP4Resource file : list.getFiles()) {
                    String path = file.getActionPath();
                    if (path != null) {
                        options.addPath(path + suffix);
                    }
                }
                if (options.getPaths().length > 0) {
                    P4SearchQuery query = new P4SearchQuery(list
                            .getConnection(), options);
                    query.run(monitor);
                    result = (P4SearchResult) query.getSearchResult();
                    Object[] elements = result.getElements();
                    fileCount = elements.length;
                    matchCount = 0;
                    for (Object element : elements) {
                        Match[] matches = result.getMatches(element);
                        for (Match match : matches) {
                            if (MatchType.MATCH == ((FileMatch) match)
                                    .getMatch().getType()) {
                                matchCount++;
                            }
                        }
                    }
                    PerforceUIPlugin.syncExec(new Runnable() {

                        public void run() {
                            if (P4UIUtils.okToUse(getPartControl())) {
                                resultsViewer.setInput(result);
                                updateTitle();
                                setEnabled(true);
                            }
                        }
                    });
                } else {
                    PerforceUIPlugin.syncExec(new Runnable() {

                        public void run() {
                            setEnabled(true);
                        }
                    });
                }
            }

        }, searchRule);
    }

    /**
     * @see com.perforce.team.ui.search.results.ISearchResultProvider#getResult()
     */
    public P4SearchResult getResult() {
        return this.result;
    }
}
