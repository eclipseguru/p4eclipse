/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.text.timelapse.form;

import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceContentProvider;
import com.perforce.team.ui.PerforceLabelProvider;
import com.perforce.team.ui.text.PerforceUiTextPlugin;
import com.perforce.team.ui.text.timelapse.INodeModel;
import com.perforce.team.ui.text.timelapse.NodeModelTimeLapseEditor;
import com.perforce.team.ui.text.timelapse.NodeOutlinePage;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.ui.IActionBars;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class FormOutlinePage extends NodeOutlinePage {

    /**
     * SORT
     */
    public static final String SORT = "com.perforce.team.ui.text.timelapse.form.SORT"; //$NON-NLS-1$

    /**
     * Simple node label provider that implements {@link IStyledLabelProvider}
     */
    protected class NodeLabelProvider implements IStyledLabelProvider {

        private ILabelProvider provider;

        /**
         * Create a node label provider
         * 
         * @param provider
         */
        public NodeLabelProvider(ILabelProvider provider) {
            this.provider = provider;
        }

        /**
         * @see org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider#getImage(java.lang.Object)
         */
        public Image getImage(Object element) {
            return provider.getImage(element);
        }

        /**
         * @see org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider#getStyledText(java.lang.Object)
         */
        public StyledString getStyledText(Object element) {
            String text = provider.getText(element);
            Styler styler = null;
            if (model != null && revision != null && model.isComplete()) {
                String id = model.getHandle(element);
                if (id != null && model.isChanged(id, revision)) {
                    styler = changeStyler;
                }
            }
            return new StyledString(text, styler);
        }

        /**
         * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
         */
        public void addListener(ILabelProviderListener listener) {

        }

        /**
         * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
         */
        public void dispose() {
            provider.dispose();
        }

        /**
         * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object,
         *      java.lang.String)
         */
        public boolean isLabelProperty(Object element, String property) {
            return false;
        }

        /**
         * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
         */
        public void removeListener(ILabelProviderListener listener) {

        }

    }

    private Styler changeStyler;

    /**
     * @param root
     * @param model
     */
    public FormOutlinePage(Form root, INodeModel model) {
        super(root, model);
    }

    private void configureToolbar(final TreeViewer viewer,
            IActionBars actionBars) {
        IToolBarManager manager = actionBars.getToolBarManager();
        final ViewerComparator comparator = new ViewerComparator();
        Action sort = new Action(Messages.FormOutlinePage_Sort,
                PerforceUiTextPlugin
                        .getImageDescriptor(PerforceUiTextPlugin.IMG_SORT)) {

            @Override
            public int getStyle() {
                return Action.AS_CHECK_BOX;
            }

            @Override
            public void run() {
                if (isChecked()) {
                    viewer.setComparator(comparator);
                } else {
                    viewer.setComparator(null);
                }
                PerforceUiTextPlugin.getDefault().getPreferenceStore()
                        .setValue(SORT, isChecked());
            }
        };
        sort.setToolTipText(Messages.FormOutlinePage_Sort);
        manager.add(sort);
        sort.setChecked(PerforceUiTextPlugin.getDefault().getPreferenceStore()
                .getBoolean(SORT));
        sort.run();
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeOutlinePage#configureViewer(org.eclipse.jface.viewers.TreeViewer)
     */
    @Override
    protected void configureViewer(TreeViewer viewer) {
        final Color changeColor = new Color(viewer.getTree().getDisplay(),
                PreferenceConverter.getColor(PerforceUiTextPlugin.getDefault()
                        .getPreferenceStore(),
                        NodeModelTimeLapseEditor.TICK_CHANGE_COLOR));
        P4UIUtils.registerDisposal(viewer.getTree(), changeColor);
        changeStyler = new Styler() {

            @Override
            public void applyStyles(TextStyle textStyle) {
                textStyle.foreground = changeColor;
            }
        };
        changeStyler = new Styler() {

            @Override
            public void applyStyles(TextStyle textStyle) {
                textStyle.foreground = changeColor;
            }
        };
        configureToolbar(viewer, getSite().getActionBars());
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeOutlinePage#expand(java.lang.Object)
     */
    @Override
    protected boolean expand(Object element) {
        return false;
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeOutlinePage#getContentProvider()
     */
    @Override
    protected ITreeContentProvider getContentProvider() {
        return new ITreeContentProvider() {

            public void inputChanged(Viewer viewer, Object oldInput,
                    Object newInput) {

            }

            public void dispose() {

            }

            public Object[] getElements(Object inputElement) {
                if (inputElement instanceof Form) {
                    return ((Form) inputElement).getFields();
                }
                return PerforceContentProvider.EMPTY;
            }

            public boolean hasChildren(Object element) {
                return false;
            }

            public Object getParent(Object element) {
                return null;
            }

            public Object[] getChildren(Object parentElement) {
                return PerforceContentProvider.EMPTY;
            }
        };
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeOutlinePage#getLabelProvider()
     */
    @Override
    protected IBaseLabelProvider getLabelProvider() {
        return new DecoratingStyledCellLabelProvider(new NodeLabelProvider(
                new PerforceLabelProvider()), new ILabelDecorator() {

            public void removeListener(ILabelProviderListener listener) {

            }

            public boolean isLabelProperty(Object element, String property) {
                return false;
            }

            public void dispose() {

            }

            public void addListener(ILabelProviderListener listener) {

            }

            public String decorateText(String text, Object element) {
                if (element instanceof FormField) {
                    return decorateRevisions(text, element);
                }
                return null;
            }

            public Image decorateImage(Image image, Object element) {
                return null;
            }
        }, null);
    }

}
