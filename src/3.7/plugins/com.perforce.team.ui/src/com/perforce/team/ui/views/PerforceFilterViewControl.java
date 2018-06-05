package com.perforce.team.ui.views;

import org.eclipse.jface.action.IContributionManager;

import com.perforce.team.ui.viewer.IFilterViewer;


public abstract class PerforceFilterViewControl extends AbstractPerforceViewControl
        implements IFilterView {

    /**
     * Viewer used by {@link #showFilters(boolean)}
     */
    private IFilterViewer filterViewer = null;

    /**
     * Hide filters action
     */
    private HideFilterAction hideFilters = null;

    public PerforceFilterViewControl(IPerforceView view) {
        super(view);
    }

    /**
     * Create filter action
     * 
     * @param manager
     */
    protected void createFilterAction(IContributionManager manager) {
        this.hideFilters = new HideFilterAction(getFilterPreference(), this);
        if (manager != null) {
            manager.add(this.hideFilters);
        }
    }

    /**
     * Set filter viewer
     * 
     * @param viewer
     */
    protected void setFilterViewer(IFilterViewer viewer) {
        this.filterViewer = viewer;
    }

    /**
     * Get filter preference string
     * 
     * @return - string preference
     */
    protected abstract String getFilterPreference();

    /**
     * @see com.perforce.team.ui.views.PerforceProjectView#showDisplayArea(boolean)
     */
    @Override
    protected void showDisplayArea(boolean layout) {
        super.showDisplayArea(layout);
        if (layout && this.hideFilters != null) {
            showFilters(!this.hideFilters.isChecked());
        }
    }

    /**
     * @see com.perforce.team.ui.views.IFilterView#showFilters(boolean)
     */
    public void showFilters(boolean show) {
        if (filterViewer != null && layout) {
            filterViewer.showFilters(show, true);
        }
    }

}
