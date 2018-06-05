/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.synchronize;

import com.perforce.team.core.p4java.synchronize.PerforceSubscriber;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.ui.synchronize.PerforceSynchronizeParticipant;
import com.perforce.team.ui.synchronize.UpdateModelOperation;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter.SyncInfoDirectionFilter;
import org.eclipse.team.internal.core.subscribers.SubscriberSyncInfoCollector;
import org.eclipse.team.internal.ui.synchronize.SubscriberParticipantPage;
import org.eclipse.team.internal.ui.synchronize.SyncInfoModelElement;
import org.eclipse.team.internal.ui.synchronize.SynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.ISynchronizePageSite;
import org.eclipse.team.ui.synchronize.SynchronizeModelAction;
import org.eclipse.team.ui.synchronize.SynchronizeModelOperation;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class SynchronizeModelOperationTest extends
        ProjectBasedTestCase {

    private ISynchronizePageSite site = new ISynchronizePageSite() {

        public void setSelectionProvider(ISelectionProvider provider) {

        }

        public void setFocus() {

        }

        public boolean isModal() {
            return false;
        }

        public IWorkbenchSite getWorkbenchSite() {
            return null;
        }

        public Shell getShell() {
            return null;
        }

        public ISelectionProvider getSelectionProvider() {
            return new ISelectionProvider() {

                public void setSelection(ISelection selection) {

                }

                public void removeSelectionChangedListener(
                        ISelectionChangedListener listener) {

                }

                public ISelection getSelection() {
                    return StructuredSelection.EMPTY;
                }

                public void addSelectionChangedListener(
                        ISelectionChangedListener listener) {

                }

            };
        }

        public IWorkbenchPart getPart() {
            return null;
        }

        public IDialogSettings getPageSettings() {
            return null;
        }

        public IKeyBindingService getKeyBindingService() {
            return null;
        }

        public IActionBars getActionBars() {
            return null;
        }

    };

    /**
     * Set up sync page configuration
     * 
     * @return - sync page configuration
     */
    protected ISynchronizePageConfiguration setupConfiguration() {
        PerforceSynchronizeParticipant participant = new PerforceSynchronizeParticipant();
        SynchronizePageConfiguration configuration = new SynchronizePageConfiguration(
                participant);
        configuration.setPage(new SubscriberParticipantPage(configuration,
                new SubscriberSyncInfoCollector(PerforceSubscriber
                        .getSubscriber(), new IResource[] { project })));
        configuration.setSite(site);
        new SynchronizeModelAction("Update", configuration) {

            @Override
            protected FastSyncInfoFilter getSyncInfoFilter() {
                return new SyncInfoDirectionFilter(
                        new int[] { SyncInfo.INCOMING });
            }

            @Override
            protected void initialize(
                    final ISynchronizePageConfiguration configuration,
                    final ISelectionProvider selectionProvider) {

            }

            @Override
            protected SynchronizeModelOperation getSubscriberOperation(
                    ISynchronizePageConfiguration configuration,
                    IDiffElement[] elements) {
                UpdateModelOperation operation = new UpdateModelOperation(
                        configuration, elements);
                operation.setAsync(false);
                return operation;
            }
        };
        return configuration;
    }

    /**
     * Set up a model element for a file
     * 
     * @param file
     * @return - sync model element
     */
    protected ISynchronizeModelElement setupModelElement(IFile file) {
        ISynchronizeModelElement element = null;
        try {
            PerforceSubscriber.getSubscriber().refresh(
                    new IResource[] { file }, 0, null);
            SyncInfo info = PerforceSubscriber.getSubscriber()
                    .getSyncInfo(file);
            element = new SyncInfoModelElement(null, info);
        } catch (TeamException e) {
            assertFalse("Team exception thrown", true);
        }

        return element;
    }
}
