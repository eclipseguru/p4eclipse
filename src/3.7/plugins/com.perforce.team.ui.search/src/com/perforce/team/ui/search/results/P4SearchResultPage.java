/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.search.results;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.changelists.Folder.Type;
import com.perforce.team.ui.search.P4UiSearchPlugin;
import com.perforce.team.ui.search.preferences.IPreferenceConstants;
import com.perforce.team.ui.search.results.table.SearchTableContentProvider;
import com.perforce.team.ui.search.results.table.SearchTableLabelProvider;
import com.perforce.team.ui.search.results.table.SearchTableResultsSorter;
import com.perforce.team.ui.search.results.tree.SearchTreeContentProvider;
import com.perforce.team.ui.search.results.tree.SearchTreeLabelProvider;
import com.perforce.team.ui.search.results.tree.SearchTreeResultsSorter;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.search.ui.IContextMenuConstants;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.PartInitException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4SearchResultPage extends AbstractTextSearchViewPage implements
        ISearchResultProvider {

    /**
     * RESULTS_MODE_GROUP
     */
    public static final String RESULTS_MODE_GROUP = "RESULTS_MODE_GROUP"; //$NON-NLS-1$

    private SearchResultsOpenHandler openHandler = new SearchResultsOpenHandler();
    private IAction flatMode;
    private IAction treeMode;
    private IAction compressedMode;

    private SearchTreeContentProvider treeContentProvider = null;
    private SearchTreeLabelProvider labelProvider = null;

    /**
     * Create search result page
     */
    public P4SearchResultPage() {
        super();
        createActions();
    }

    /**
     * Create search result page
     * 
     * @param layouts
     */
    public P4SearchResultPage(int layouts) {
        super(layouts);
        createActions();
    }

    private void createActions() {
        final IPreferenceStore store = P4UiSearchPlugin.getDefault()
                .getPreferenceStore();

        flatMode = new Action(Messages.P4SearchResultPage_FlatMode,
                IAction.AS_RADIO_BUTTON) {

            @Override
            public void run() {
                if (treeContentProvider != null) {
                    boolean checked = isChecked();
                    labelProvider.setShowFileNames(!checked);
                    treeContentProvider.setDisplayType(Type.FLAT);
                    treeContentProvider.refresh();
                    postEnsureSelection();
                    store.setValue(IPreferenceConstants.RESULTS_TREE_MODE,
                            checked);
                }
            }

        };
        flatMode.setEnabled(false);
        flatMode.setImageDescriptor(PerforceUIPlugin
                .getDescriptor(IPerforceUIConstants.IMG_FLAT_LAYOUT));
        flatMode.setChecked(store
                .getBoolean(IPreferenceConstants.RESULTS_FLAT_MODE));

        compressedMode = new Action(Messages.P4SearchResultPage_CompressedMode,
                IAction.AS_RADIO_BUTTON) {

            @Override
            public void run() {
                if (treeContentProvider != null) {
                    boolean checked = isChecked();
                    labelProvider.setShowFileNames(checked);
                    treeContentProvider.setDisplayType(Type.COMPRESSED);
                    treeContentProvider.refresh();
                    postEnsureSelection();
                    store.setValue(
                            IPreferenceConstants.RESULTS_COMPRESSED_MODE,
                            checked);
                }
            }

        };
        compressedMode.setEnabled(false);
        compressedMode.setImageDescriptor(PerforceUIPlugin
                .getDescriptor(IPerforceUIConstants.IMG_COMPRESSED_LAYOUT));
        compressedMode.setChecked(store
                .getBoolean(IPreferenceConstants.RESULTS_COMPRESSED_MODE));

        treeMode = new Action(Messages.P4SearchResultPage_TreeMode,
                IAction.AS_RADIO_BUTTON) {

            @Override
            public void run() {
                if (treeContentProvider != null) {
                    boolean checked = isChecked();
                    labelProvider.setShowFileNames(checked);
                    treeContentProvider.setDisplayType(Type.TREE);
                    treeContentProvider.refresh();
                    postEnsureSelection();
                    store.setValue(IPreferenceConstants.RESULTS_TREE_MODE,
                            checked);
                }
            }

        };
        treeMode.setEnabled(false);
        treeMode.setImageDescriptor(PerforceUIPlugin
                .getDescriptor(IPerforceUIConstants.IMG_TREE_LAYOUT));
        treeMode.setChecked(store
                .getBoolean(IPreferenceConstants.RESULTS_TREE_MODE));

        // Ensure one mode is always checked and use flat mode as the default
        if (!treeMode.isChecked() && !compressedMode.isChecked()) {
            flatMode.setChecked(true);
        }
    }

    /**
     * @see org.eclipse.search.ui.text.AbstractTextSearchViewPage#clear()
     */
    @Override
    protected void clear() {
        getViewer().refresh();
    }

    /**
     * @see org.eclipse.search.ui.text.AbstractTextSearchViewPage#handleOpen(org.eclipse.jface.viewers.OpenEvent)
     */
    @Override
    protected void handleOpen(OpenEvent event) {
        Object firstElement = ((IStructuredSelection) event.getSelection())
                .getFirstElement();
        if (treeContentProvider != null
                && firstElement instanceof RevisionMatch) {
            Match[] matches = getDisplayedMatches(firstElement);
            if (matches != null && matches.length > 0) {
                try {
                    showMatch(matches[0], 0, 0, true);
                } catch (PartInitException e) {
                    PerforceProviderPlugin.logError(e);
                }
            }
        } else {
            super.handleOpen(event);
        }
    }

    /**
     * @see org.eclipse.search.ui.text.AbstractTextSearchViewPage#getDisplayedMatchCount(java.lang.Object)
     */
    @Override
    public int getDisplayedMatchCount(Object element) {
        if (element instanceof FileMatch) {
            return 1;
        }
        return 0;
    }

    /**
     * @see org.eclipse.search.ui.text.AbstractTextSearchViewPage#getDisplayedMatches(java.lang.Object)
     */
    @Override
    public Match[] getDisplayedMatches(Object element) {
        if (element instanceof FileMatch) {
            return new Match[] { (Match) element };
        }
        return super.getDisplayedMatches(element);
    }

    /**
     * @see org.eclipse.search.ui.text.AbstractTextSearchViewPage#configureTableViewer(org.eclipse.jface.viewers.TableViewer)
     */
    @Override
    protected void configureTableViewer(TableViewer viewer) {
        // Clear tree components
        this.treeContentProvider = null;
        this.labelProvider = null;

        viewer.setUseHashlookup(true);
        viewer.setContentProvider(new SearchTableContentProvider(viewer));
        IBaseLabelProvider labelProvider = new DecoratingStyledCellLabelProvider(
                new SearchTableLabelProvider(this, getControl().getDisplay()),
                new SearchLabelDecorator(), null);
        viewer.setLabelProvider(labelProvider);
        viewer.setSorter(new SearchTableResultsSorter());

        treeMode.setEnabled(false);
        flatMode.setEnabled(false);
        compressedMode.setEnabled(false);
    }

    /**
     * @see org.eclipse.search.ui.text.AbstractTextSearchViewPage#configureTreeViewer(org.eclipse.jface.viewers.TreeViewer)
     */
    @Override
    protected void configureTreeViewer(TreeViewer viewer) {
        viewer.setUseHashlookup(true);
        treeContentProvider = new SearchTreeContentProvider(viewer);
        labelProvider = new SearchTreeLabelProvider(this, getControl()
                .getDisplay());
        viewer.setContentProvider(treeContentProvider);
        IBaseLabelProvider decoratedLabelProvider = new DecoratingStyledCellLabelProvider(
                labelProvider, new SearchLabelDecorator(), null);
        viewer.setLabelProvider(decoratedLabelProvider);
        viewer.setSorter(new SearchTreeResultsSorter());

        treeMode.setEnabled(true);
        flatMode.setEnabled(true);
        compressedMode.setEnabled(true);
        if (treeMode.isChecked()) {
            treeContentProvider.setDisplayType(Type.TREE);
            labelProvider.setShowFileNames(true);
        } else if (flatMode.isChecked()) {
            treeContentProvider.setDisplayType(Type.FLAT);
            labelProvider.setShowFileNames(false);
        } else if (compressedMode.isChecked()) {
            treeContentProvider.setDisplayType(Type.COMPRESSED);
            labelProvider.setShowFileNames(true);
        }
    }

    /**
     * @see org.eclipse.search.ui.text.AbstractTextSearchViewPage#elementsChanged(java.lang.Object[])
     */
    @Override
    protected void elementsChanged(Object[] objects) {
        getViewer().refresh();
    }

    /**
     * @see org.eclipse.search.ui.text.AbstractTextSearchViewPage#showMatch(org.eclipse.search.ui.text.Match,
     *      int, int, boolean)
     */
    @Override
    protected void showMatch(Match match, int currentOffset, int currentLength,
            boolean activate) throws PartInitException {
        openHandler.doubleClick(new DoubleClickEvent(getViewer(),
                new StructuredSelection(match)));
    }

    /**
     * @see com.perforce.team.ui.search.results.ISearchResultProvider#getResult()
     */
    public P4SearchResult getResult() {
        return (P4SearchResult) getInput();
    }

    /**
     * @see org.eclipse.search.ui.text.AbstractTextSearchViewPage#fillToolbar(org.eclipse.jface.action.IToolBarManager)
     */
    @Override
    protected void fillToolbar(IToolBarManager tbm) {
        super.fillToolbar(tbm);

        tbm.appendToGroup(IContextMenuConstants.GROUP_VIEWER_SETUP,
                new Separator(RESULTS_MODE_GROUP));
        tbm.appendToGroup(RESULTS_MODE_GROUP, flatMode);
        tbm.appendToGroup(RESULTS_MODE_GROUP, treeMode);
        tbm.appendToGroup(RESULTS_MODE_GROUP, compressedMode);
    }

}
