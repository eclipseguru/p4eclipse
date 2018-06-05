/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.synchronize;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.internal.ui.synchronize.ChangeSetCapability;
import org.eclipse.team.internal.ui.synchronize.IChangeSetProvider;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.ISynchronizePage;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipantDescriptor;
import org.eclipse.team.ui.synchronize.ISynchronizeScope;
import org.eclipse.team.ui.synchronize.SubscriberParticipant;
import org.eclipse.team.ui.synchronize.SynchronizePageActionGroup;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PartInitException;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.synchronize.PerforceSubscriber;
import com.perforce.team.ui.decorator.PerforceDecorator;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class PerforceSynchronizeParticipant extends SubscriberParticipant
        implements IPropertyChangeListener, IChangeSetProvider {

    /**
     * ID
     */
    public static final String ID = "com.perforce.synchronize.participant"; //$NON-NLS-1$

    /**
     * Creates and adds a sync participant to the sync manager
     * 
     * @param scope
     * @return - the created and added participant
     */
    public static PerforceSynchronizeParticipant addParticipant(
            ISynchronizeScope scope) {
        PerforceSynchronizeParticipant addedParticipant = new PerforceSynchronizeParticipant(
                scope);
        TeamUI.getSynchronizeManager().addSynchronizeParticipants(
                new ISynchronizeParticipant[] { addedParticipant });
        return addedParticipant;
    }

    private ChangeSetCapability changeSetCapability;

    /**
     * Creates a new perforce sync participant
     */
    public PerforceSynchronizeParticipant() {
        setSubscriber(PerforceSubscriber.getSubscriber());
    }

    /**
     * Creates a new perforce sync participant with a sync scope
     * 
     * @param scope
     */
    public PerforceSynchronizeParticipant(ISynchronizeScope scope) {
        super(scope);
        setSubscriber(PerforceSubscriber.getSubscriber());
    }

    /**
     * @see org.eclipse.team.ui.synchronize.SubscriberParticipant#initializeConfiguration(org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration)
     */
    @Override
    protected void initializeConfiguration(
            final ISynchronizePageConfiguration configuration) {
        configuration.addActionContribution(new RemoveActionGroup());
        configuration.addActionContribution(new PerforceSyncActionGroup());

        // Add contributed action groups
        for (SynchronizePageActionGroup group : SyncActionGroupRegistry
                .getRegistry().generateGroups()) {
            configuration.addActionContribution(group);
        }

        super.initializeConfiguration(configuration);
        PerforceDecorator decorator = new PerforceDecorator(true){
            @Override
            public String getName() {
            	return PerforceSynchronizeParticipant.class.getSimpleName()+":"+super.getName();
            }
        };

        // Register a label provider listener so that changes to decoration
        // preferences are picked up by in the Synchronize view
        ILabelProviderListener listener = new ILabelProviderListener() {

            public void labelProviderChanged(LabelProviderChangedEvent event) {
                if (event.getElements() == null) {
                    ISynchronizePage page = configuration.getPage();
                    if (page != null) {
                        Viewer viewer = page.getViewer();
                        if (viewer != null) {
                            viewer.refresh();
                        }
                    }
                }
            }
        };
        decorator.addListener(listener);
        configuration.addLabelDecorator(decorator);

        configuration
                .setSupportedModes(ISynchronizePageConfiguration.ALL_MODES);
        configuration.setMode(ISynchronizePageConfiguration.BOTH_MODE);

        P4ChangeSetCapability capability = (P4ChangeSetCapability) getChangeSetCapability();
        capability.addChangeSetFilter(configuration);
    }

    /**
     * @see org.eclipse.team.ui.synchronize.SubscriberParticipant#setSubscriber(org.eclipse.team.core.subscribers.Subscriber)
     */
    @Override
    protected void setSubscriber(Subscriber subscriber) {
        super.setSubscriber(subscriber);
        try {
            ISynchronizeParticipantDescriptor descriptor = getDescriptor();
            setInitializationData(descriptor);
        } catch (CoreException e) {
            PerforceProviderPlugin.logError(e);
        }
        if (getSecondaryId() == null) {
            setSecondaryId(Long.toString(System.currentTimeMillis()));
        }
    }

    /**
     * Return the descriptor for this participant
     * 
     * @return the descriptor for this participant
     */
    protected ISynchronizeParticipantDescriptor getDescriptor() {
        return TeamUI.getSynchronizeManager().getParticipantDescriptor(ID);
    }

    /**
     * @see org.eclipse.team.ui.synchronize.SubscriberParticipant#init(java.lang.String,
     *      org.eclipse.ui.IMemento)
     */
    @Override
    public void init(String secondaryId, IMemento memento)
            throws PartInitException {
        super.init(secondaryId, memento);
        setSubscriber(PerforceSubscriber.getSubscriber());
    }

    /**
     * @see org.eclipse.team.internal.ui.synchronize.IChangeSetProvider#getChangeSetCapability()
     */
    public ChangeSetCapability getChangeSetCapability() {
        if (this.changeSetCapability == null) {
            this.changeSetCapability = new P4ChangeSetCapability();
        }
        return this.changeSetCapability;
    }

    /**
     * @see org.eclipse.team.ui.synchronize.AbstractSynchronizeParticipant#isViewerContributionsSupported()
     */
    @Override
    protected boolean isViewerContributionsSupported() {
        return true;
    }

}
