/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.preferences.decorators;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE.SharedImages;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.perforce.p4java.core.file.FileAction;
import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.decorator.IconCache;
import com.perforce.team.ui.decorator.PerforceDecorator;
import com.perforce.team.ui.preferences.IPreferenceConstants;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class LabelPreviewPreferencePage extends PreferencePage implements
        IWorkbenchPreferencePage {

    /**
     * ID
     */
    public static final String ID = "com.perforce.team.ui.dialogs.DecorationsPreferencesDialog"; //$NON-NLS-1$

    private static final String[] ICON_POSITIONS = {
            Messages.LabelPreviewPreferencePage_None,
            Messages.LabelPreviewPreferencePage_TopLeft,
            Messages.LabelPreviewPreferencePage_TopRight,
            Messages.LabelPreviewPreferencePage_BottomLeft,
            Messages.LabelPreviewPreferencePage_BottomRight, };

    /**
     * Project class
     */
    private static class Project {

        String name;
        IP4Connection connection;

        Project(String name, IP4Connection connection) {
            this.name = name;
            this.connection = connection;
        }

    }

    /**
     * Folder class
     */
    private static class Folder {

        String name;

        Folder(String name, IP4Connection connection) {
            this.name = name;
        }
    }

    private Composite displayArea;

    private Link labelDecoratorsLink;

    private TabFolder labelTabs;

    private TabItem generalTab;
    private Composite generalArea;
    private Button showChangelistInSyncButton;

    private TabItem iconTab;
    private Composite iconArea;
    private Combo sharedProjectsCombo;
    private Combo openFilesCombo;
    private Combo syncedFilesCombo;
    private Combo notSyncedFilesCombo;
    private Combo lockedFilesCombo;
    private Combo unresolvedFilesCombo;
    private Combo openElsewhereFilesCombo;
    private Combo ignoredFilesCombo;
    private Combo localFilesCombo;
    private Combo streamSandboxCombo;
    private Combo streamSandboxProjectCombo;
    private Button unmanagedFilesButton;

    private TabItem textTab;
    private Composite textArea;
    private Button ignoreButton;
    private Text ignoreText;
    private Text projectText;
    private Button projectVariablesButton;
    private Text connectText;
    private Button connectVariablesButton;
    private Text fileText;
    private Button fileVariablesButton;
    private Text outgoingChangeText;
    private Text unaddedChangeText;

    private TreeViewer previewTree;
    private IP4Connection onlineConnection;
    private IP4Connection offlineConnection;
    private IP4Connection onlineSandboxConnection;
    private IP4Connection offlineSandboxConnection;
    private Project onlineProject;
    private Project offlineProject;
    private Project sandboxProject;
	private Project offlineSandboxProject;
    private Folder ignoredFolder;
    private PreviewP4File addedFile;
    private PreviewP4File editedFile;
    private PreviewP4File deletedFile;
    private PreviewP4File unresolvedFile;
    private PreviewP4File lockedFile;
    private PreviewP4File syncedFile;
    private PreviewP4File notSyncedFile;
    private PreviewP4File addedElsewhereFile;
    private PreviewP4File editedElsewhereFile;
    private PreviewP4File deletedElsewhereFile;
    private PreviewP4File ignoredFile;
    private PreviewP4File unmanagedFile;
    private PreviewP4File localOnly;
    private PreviewP4File branchFile;
    private PreviewP4File branchElsewhereFile;
    private PreviewP4File integrateFile;
    private PreviewP4File integrateElsewhereFile;

    private SelectionAdapter comboAdapter = new SelectionAdapter() {

        @Override
        public void widgetSelected(SelectionEvent e) {
            cache.clear();
            cache.setLockLocation(getPosition(lockedFilesCombo));
            cache.setMarkUnmanaged(unmanagedFilesButton.getSelection());
            cache.setNotSyncLocation(getPosition(notSyncedFilesCombo));
            cache.setOpenLocation(getPosition(openFilesCombo));
            cache.setOtherLocation(getPosition(openElsewhereFilesCombo));
            cache.setSyncLocation(getPosition(syncedFilesCombo));
            cache.setUnresolvedLocation(getPosition(unresolvedFilesCombo));
            cache.setProjectLocation(getPosition(sharedProjectsCombo));
            cache.setIgnoredLocation(getPosition(ignoredFilesCombo));
            cache.setLocalLocation(getPosition(localFilesCombo));
            cache.setStreamAndSandboxLocation(getPosition(streamSandboxCombo));
            cache.setStreamAndSandboxProjectLocation(getPosition(streamSandboxProjectCombo));
            decorator.decorateIgnored(ignoreButton.getSelection());
            previewTree.refresh();
        }

    };

    private ModifyListener textListener = new ModifyListener() {

        public void modifyText(ModifyEvent e) {
        	decorator.setConnectionDecoration(connectText.getText());
            decorator.setProjectDecoration(projectText.getText());
            decorator.setFileDecoration(fileText.getText());
            decorator.setOutgoingDecoration(outgoingChangeText.getText());
            decorator.setUnaddedDecoration(unaddedChangeText.getText());
            decorator.setIgnoredDecoration(ignoreText.getText());
            decorator.decorateIgnored(ignoreButton.getSelection());
            previewTree.refresh();
        }
    };

    private ITreeContentProvider provider = new ITreeContentProvider() {

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

        }

        public void dispose() {

        }

        public Object[] getElements(Object inputElement) {
            return new Object[] { unmanagedFile, onlineProject, offlineProject, sandboxProject, onlineConnection, offlineConnection, onlineSandboxConnection, offlineSandboxConnection};
        }

        public boolean hasChildren(Object element) {
            return element == onlineProject;
        }

        public Object getParent(Object element) {
            if (element instanceof IProject) {
                return null;
            } else {
                return onlineProject;
            }
        }

        public Object[] getChildren(Object parentElement) {
            if (parentElement == onlineProject) {
                return new Object[] { addedFile, editedFile, deletedFile,
                        branchFile, integrateFile, unresolvedFile, lockedFile,
                        syncedFile, notSyncedFile, addedElsewhereFile,
                        editedElsewhereFile, deletedElsewhereFile,
                        branchElsewhereFile, integrateElsewhereFile,
                        ignoredFolder, ignoredFile, localOnly, };
            }
            return new Object[0];
        }
    };

    /**
     * Decorator class wrapper
     */
    private class PageDecorator extends PerforceDecorator {

        protected Map<ImageDescriptor, Image> images = new HashMap<ImageDescriptor, Image>();

        @Override
        public void dispose() {
            super.dispose();
            for (Image img : images.values()) {
                if (img != null && !img.isDisposed()) {
                    img.dispose();
                }
            }
            images.clear();
            if (iconCache != null) {
                iconCache.dispose();
            }
        }

        public PageDecorator(IconCache cache) {
            super(true);
            if (iconCache != null) {
                iconCache.clear();
            }
            iconCache = cache;
        }

        void setFileDecoration(String decoration) {
            this.fileDecoration = decoration;
        }

        void setProjectDecoration(String decoration) {
            this.projectDecoration = decoration;
        }

        void setConnectionDecoration(String decoration) {
            this.connectionDecoration = decoration;
        }

        void setOutgoingDecoration(String decoration) {
            this.outgoingChangeFlag = decoration;
        }

        void setUnaddedDecoration(String decoration) {
            this.unaddedChangeFlag = decoration;
        }

        void setIgnoredDecoration(String decoration) {
            this.ignoredText = decoration;
        }

        void decorateIgnored(boolean decorate) {
            this.decorateIgnored = decorate;
        }

        @Override
        public String decorateText(String text, Object o) {
            if (text != null) {
                if (o instanceof Project) {
                    return super.decorateProjectWithConnection(((Project) o).name,
                            ((Project) o).connection);
                } else if (o == ignoredFile && this.decorateIgnored) {
                    return super.decorateIgnoredResource(ignoredFile.getName());
                } else if (o == ignoredFolder) {
                    if (this.decorateIgnored) {
                        return super
                                .decorateIgnoredResource(ignoredFolder.name);
                    } else {
                        return ignoredFolder.name;
                    }
                } else if (o instanceof IP4File) {
                    IP4File file = (IP4File) o;
                    return super.getFileText(file.getName(), file,
                            o == ignoredFile);
                } else if (o instanceof IP4Connection){
                	IP4Connection conn=(IP4Connection) o;
                	return super.decorateText(conn.getName(), conn);
                }
            }
            return super.decorateText(text, o);
        }

        @Override
        public Image decorateImage(Image image, Object o) {
            if (o == ignoredFile) {
                String text = o.toString();
                ImageDescriptor desc = PlatformUI.getWorkbench()
                        .getEditorRegistry().getImageDescriptor(text);
                if (desc != null) {
                    image = images.get(desc);
                    if (image == null) {
                        image = desc.createImage();
                        images.put(desc, image);
                    }
                    return iconCache.getIgnoredImage(image);
                }
            } else if (o instanceof IP4Connection) {
            	IP4Connection conn=(IP4Connection) o;
				ImageDescriptor desc = PerforceUIPlugin
						.getPlugin()
						.getImageDescriptor(
								conn.isOffline() ? IPerforceUIConstants.IMG_DEPOT_CONNECTION_OFFLINE
										: IPerforceUIConstants.IMG_DEPOT_CONNECTION);
            	if(desc!=null){
            		image=images.get(desc);
            		if(image==null){
                        image = desc.createImage();
                        images.put(desc, image);
            		}
            	}
                return iconCache.getImage(image, conn);
            } else if (o instanceof Project) {
                return iconCache.decorateProjectImageBasedonConnection(
                        PlatformUI.getWorkbench().getSharedImages()
                                .getImage(SharedImages.IMG_OBJ_PROJECT),
                        ((Project) o).connection);
            } else if (o instanceof Folder) {
                return PlatformUI.getWorkbench().getSharedImages()
                        .getImage(ISharedImages.IMG_OBJ_FOLDER);
            } else if (o instanceof IP4File) {
                String text = o.toString();
                ImageDescriptor desc = PlatformUI.getWorkbench()
                        .getEditorRegistry().getImageDescriptor(text);
                if (desc != null) {
                    image = images.get(desc);
                    if (image == null) {
                        image = desc.createImage();
                        images.put(desc, image);
                    }
                    return super.decorateImage(image, o);
                }
            }
            return super.decorateImage(image, o);
        }
        @Override
        public String getName() {
        	return LabelPreviewPreferencePage.class.getSimpleName()+":"+super.getName(); //$NON-NLS-1$
        }

    };

    /**
     * Cache wrapper class
     */
    private static class PageCache extends IconCache {

        void setOpenLocation(int location) {
            this.openLocation = location;
        }

        void setSyncLocation(int location) {
            this.syncLocation = location;
        }

        void setNotSyncLocation(int location) {
            this.notSyncLocation = location;
        }

        void setUnresolvedLocation(int location) {
            this.unresolvedLocation = location;
        }

        void setLockLocation(int location) {
            this.lockLocation = location;
        }

        void setOtherLocation(int location) {
            this.otherLocation = location;
        }

        void setMarkUnmanaged(boolean mark) {
            this.markUnmanaged = mark;
        }

        void setProjectLocation(int location) {
            this.projectLocation = location;
        }

        void setIgnoredLocation(int location) {
            this.ignoredLocation = location;
        }

        void setLocalLocation(int location) {
            this.localOnlyLocation = location;
        }

        void setStreamAndSandboxLocation(int location) {
            this.streamAndSandboxLocation = location;
        }

        void setStreamAndSandboxProjectLocation(int location) {
            this.streamAndSandboxProjectLocation = location;
        }
    };

    private PageDecorator decorator = null;
    private PageCache cache = new PageCache();

    /**
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        displayArea = new Composite(parent, SWT.NONE);
        GridLayout daLayout = new GridLayout(1, true);
        displayArea.setLayout(daLayout);
        GridData daData = new GridData(SWT.FILL, SWT.FILL, true, true);
        displayArea.setLayoutData(daData);

        labelDecoratorsLink = new Link(displayArea, SWT.NONE);
        labelDecoratorsLink
                .setText(Messages.LabelPreviewPreferencePage_ToEnablePerforceDecoration);
        labelDecoratorsLink.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                P4UIUtils
                        .openPreferencePage("org.eclipse.ui.preferencePages.Decorators"); //$NON-NLS-1$
            }

        });

        labelTabs = new TabFolder(displayArea, SWT.TOP);
        labelTabs.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        createIconTab(labelTabs);
        createTextTab(labelTabs);
        createGeneralTab(labelTabs);

        Label previewLabel = new Label(displayArea, SWT.LEFT);
        previewLabel.setText(Messages.LabelPreviewPreferencePage_Preview);

        previewTree = new TreeViewer(displayArea, SWT.SINGLE
                | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        previewTree.getTree().setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, true, true));
        previewTree.setContentProvider(provider);
        decorator = new PageDecorator(cache);
        previewTree.setLabelProvider(new DecoratingLabelProvider(
                new WorkbenchLabelProvider(), decorator));
        previewTree.setInput(new Object[] { onlineProject, offlineProject, sandboxProject });
        previewTree.expandAll();

        return displayArea;
    }

    private void createGeneralTab(TabFolder parent) {
        generalTab = new TabItem(parent, SWT.NONE);
        generalTab.setText(Messages.LabelPreviewPreferencePage_General);
        generalArea = new Composite(parent, SWT.NONE);
        generalTab.setControl(generalArea);
        GridLayout gaLayout = new GridLayout(1, true);
        generalArea.setLayout(gaLayout);
        GridData gaData = new GridData(SWT.FILL, SWT.FILL, true, true);
        generalArea.setLayoutData(gaData);

        showChangelistInSyncButton = new Button(generalArea, SWT.CHECK);
        showChangelistInSyncButton
                .setText(Messages.LabelPreviewPreferencePage_ShowCurrentChangelistInSyncView);
        showChangelistInSyncButton.setSelection(getPreferenceStore()
                .getBoolean(IPreferenceConstants.SHOW_CHANGELIST_IN_SYNC_VIEW));
    }

    private void createIconTab(TabFolder parent) {
        iconTab = new TabItem(parent, SWT.NONE);
        iconTab.setText(Messages.LabelPreviewPreferencePage_IconDecorations);
        iconArea = new Composite(parent, SWT.NONE);
        iconTab.setControl(iconArea);
        GridLayout iaLayout = new GridLayout(2, false);
        iconArea.setLayout(iaLayout);
        GridData iaData = new GridData(SWT.FILL, SWT.FILL, true, true);
        iconArea.setLayoutData(iaData);

        Label sharedProjectsLabel = new Label(iconArea, SWT.CHECK);
        sharedProjectsLabel
                .setText(Messages.LabelPreviewPreferencePage_IndicatorForSharedProjects);
        sharedProjectsCombo = new Combo(iconArea, SWT.READ_ONLY | SWT.DROP_DOWN);
        addComboOptions(sharedProjectsCombo);

        Label openFilesLabel = new Label(iconArea, SWT.CHECK);
        openFilesLabel
                .setText(Messages.LabelPreviewPreferencePage_IndicatorForOpenForAddEditDelete);
        openFilesCombo = new Combo(iconArea, SWT.READ_ONLY | SWT.DROP_DOWN);
        addComboOptions(openFilesCombo);

        Label syncedFilesLabel = new Label(iconArea, SWT.CHECK);
        syncedFilesLabel
                .setText(Messages.LabelPreviewPreferencePage_IndicatorForSyncedToHead);
        syncedFilesCombo = new Combo(iconArea, SWT.READ_ONLY | SWT.DROP_DOWN);
        addComboOptions(syncedFilesCombo);

        Label notSyncedFilesLabel = new Label(iconArea, SWT.CHECK);
        notSyncedFilesLabel
                .setText(Messages.LabelPreviewPreferencePage_IndicatorForNotSyncedToHead);
        notSyncedFilesCombo = new Combo(iconArea, SWT.READ_ONLY | SWT.DROP_DOWN);
        addComboOptions(notSyncedFilesCombo);

        Label lockedFilesLabel = new Label(iconArea, SWT.CHECK);
        lockedFilesLabel
                .setText(Messages.LabelPreviewPreferencePage_IndicatorForLocked);
        lockedFilesCombo = new Combo(iconArea, SWT.READ_ONLY | SWT.DROP_DOWN);
        addComboOptions(lockedFilesCombo);

        Label unresolvedFilesLabel = new Label(iconArea, SWT.CHECK);
        unresolvedFilesLabel
                .setText(Messages.LabelPreviewPreferencePage_IndicatorForUnresolved);
        unresolvedFilesCombo = new Combo(iconArea, SWT.READ_ONLY
                | SWT.DROP_DOWN);
        addComboOptions(unresolvedFilesCombo);

        Label openElsewhereFilesLabel = new Label(iconArea, SWT.CHECK);
        openElsewhereFilesLabel
                .setText(Messages.LabelPreviewPreferencePage_IndicatorForOpenedByOther);
        openElsewhereFilesCombo = new Combo(iconArea, SWT.READ_ONLY
                | SWT.DROP_DOWN);
        addComboOptions(openElsewhereFilesCombo);

        Label ignoredFilesLabel = new Label(iconArea, SWT.CHECK);
        ignoredFilesLabel
                .setText(Messages.LabelPreviewPreferencePage_IndicatorForIgnoredResources);
        ignoredFilesCombo = new Combo(iconArea, SWT.READ_ONLY | SWT.DROP_DOWN);
        addComboOptions(ignoredFilesCombo);

        Label localFilesLabel = new Label(iconArea, SWT.CHECK);
        localFilesLabel
                .setText(Messages.LabelPreviewPreferencePage_IndicatorForNotUnderVersionControl);
        localFilesCombo = new Combo(iconArea, SWT.READ_ONLY | SWT.DROP_DOWN);
        addComboOptions(localFilesCombo);

        Label streamSandBoxLabel = new Label(iconArea, SWT.CHECK);
        streamSandBoxLabel
                .setText(Messages.LabelPreviewPreferencePage_IndicatorForStreamAndSandboxConnection);
        streamSandboxCombo = new Combo(iconArea, SWT.READ_ONLY | SWT.DROP_DOWN);
        addComboOptions(streamSandboxCombo);

        Label streamSandBoxProjectLabel = new Label(iconArea, SWT.CHECK);
        streamSandBoxProjectLabel
                .setText(Messages.LabelPreviewPreferencePage_IndicatorForStreamAndSandboxProject);
        streamSandboxProjectCombo = new Combo(iconArea, SWT.READ_ONLY | SWT.DROP_DOWN);
        addComboOptions(streamSandboxProjectCombo);
        
        unmanagedFilesButton = new Button(iconArea, SWT.CHECK);
        unmanagedFilesButton
                .setText(Messages.LabelPreviewPreferencePage_DisplayIconForFilesNotManagedByEclipse);
        GridData ufbData = new GridData(SWT.FILL, SWT.FILL, true, false);
        ufbData.horizontalSpan = 2;
        unmanagedFilesButton.setLayoutData(ufbData);
        unmanagedFilesButton.setSelection(getPreferenceStore().getBoolean(
                IPerforceUIConstants.PREF_MARK_UNMANAGED_FILES));
        unmanagedFilesButton.addSelectionListener(comboAdapter);

        setPosition(
                openFilesCombo,
                getPreferenceStore().getInt(
                        IPerforceUIConstants.PREF_FILE_OPEN_ICON));
        setPosition(
                notSyncedFilesCombo,
                getPreferenceStore().getInt(
                        IPerforceUIConstants.PREF_FILE_SYNC_ICON));
        setPosition(
                syncedFilesCombo,
                getPreferenceStore().getInt(
                        IPerforceUIConstants.PREF_FILE_SYNC2_ICON));
        setPosition(
                unresolvedFilesCombo,
                getPreferenceStore().getInt(
                        IPerforceUIConstants.PREF_FILE_UNRESOLVED_ICON));
        setPosition(
                lockedFilesCombo,
                getPreferenceStore().getInt(
                        IPerforceUIConstants.PREF_FILE_LOCK_ICON));
        setPosition(
                openElsewhereFilesCombo,
                getPreferenceStore().getInt(
                        IPerforceUIConstants.PREF_FILE_OTHER_ICON));
        setPosition(
                sharedProjectsCombo,
                getPreferenceStore().getInt(
                        IPerforceUIConstants.PREF_PROJECT_ICON));
        setPosition(
                ignoredFilesCombo,
                getPreferenceStore().getInt(
                        IPerforceUIConstants.PREF_IGNORED_ICON));
        setPosition(
                localFilesCombo,
                getPreferenceStore().getInt(
                        IPerforceUIConstants.PREF_LOCAL_ONLY_ICON));
        setPosition(
                streamSandboxCombo,
                getPreferenceStore().getInt(
                        IPerforceUIConstants.PREF_STREAM_SANDBOX_ICON));
        setPosition(
                streamSandboxProjectCombo,
                getPreferenceStore().getInt(
                        IPerforceUIConstants.PREF_STREAM_SANDBOX_PROJECT_ICON));
    }

    private void createTextTab(TabFolder parent) {
        textTab = new TabItem(parent, SWT.NONE);
        textTab.setText(Messages.LabelPreviewPreferencePage_TextDecorations);
        textArea = new Composite(parent, SWT.NONE);
        textTab.setControl(textArea);
        GridLayout iaLayout = new GridLayout(3, false);
        textArea.setLayout(iaLayout);
        GridData iaData = new GridData(SWT.FILL, SWT.FILL, true, true);
        textArea.setLayoutData(iaData);

        Label fileLabel = new Label(textArea, SWT.LEFT);
        fileLabel.setText(Messages.LabelPreviewPreferencePage_FileDecoration);
        fileText = new Text(textArea, SWT.SINGLE | SWT.BORDER);
        fileText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        fileText.setText(getPreferenceStore().getString(
                IPreferenceConstants.FILE_DECORATION_TEXT));
        fileText.addModifyListener(textListener);
        fileVariablesButton = new Button(textArea, SWT.PUSH);
        fileVariablesButton
                .setText(Messages.LabelPreviewPreferencePage_AddVariables);
        fileVariablesButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                String[] vars = new String[] {
                        IPreferenceConstants.ACTION_VARIABLE,
                        IPreferenceConstants.HAVE_VARIABLE,
                        IPreferenceConstants.HEAD_VARIABLE,
                        IPreferenceConstants.HEAD_CHANGE_VARIABLE,
                        IPreferenceConstants.NAME_VARIABLE,
                        IPreferenceConstants.UNADDED_CHANGE_VARIABLE,
                        IPreferenceConstants.OUTGOING_CHANGE_VARIABLE,
                        IPreferenceConstants.TYPE_VARIABLE, };
                String[] descs = new String[] {
                        IPreferenceConstants.ACTION_DESCRIPTION,
                        IPreferenceConstants.HAVE_DESCRIPTION,
                        IPreferenceConstants.HEAD_DESCRIPTION,
                        IPreferenceConstants.HEAD_CHANGE_DESCRIPTION,
                        IPreferenceConstants.NAME_DESCRIPTION,
                        IPreferenceConstants.UNADDED_CHANGE_DESCRIPTION,
                        IPreferenceConstants.OUTGOING_CHANGE_DESCRIPTION,
                        IPreferenceConstants.TYPE_DESCRIPTION, };
                VariablesDialog dialog = new VariablesDialog(
                        fileVariablesButton.getShell(), vars, descs);
                if (dialog.open() == VariablesDialog.OK) {
                    String[] selected = dialog.getSelectedVariables();
                    StringBuffer newVariables = new StringBuffer();
                    for (String var : selected) {
                        newVariables.append(var);
                    }
                    fileText.insert(newVariables.toString());
                }
            }

        });

        Label projectLabel = new Label(textArea, SWT.LEFT);
        projectLabel
                .setText(Messages.LabelPreviewPreferencePage_ProjectDecoration);
        projectText = new Text(textArea, SWT.SINGLE | SWT.BORDER);
        projectText
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        projectText.setText(getPreferenceStore().getString(
                IPreferenceConstants.PROJECT_DECORATION_TEXT));
        projectText.addModifyListener(textListener);
        projectVariablesButton = new Button(textArea, SWT.PUSH);
        projectVariablesButton
                .setText(Messages.LabelPreviewPreferencePage_AddVariables);
        projectVariablesButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                String[] vars = new String[] {
                        IPreferenceConstants.CHARSET_VARIABLE,
                        IPreferenceConstants.CLIENT_VARIABLE,
                        IPreferenceConstants.NAME_VARIABLE,
                        IPreferenceConstants.OFFLINE_VARIABLE,
                        IPreferenceConstants.SANDBOX_VARIABLE,
                        IPreferenceConstants.STREAM_NAME_VARIABLE,
                        IPreferenceConstants.STREAM_ROOT_VARIABLE,
                        IPreferenceConstants.SERVER_VARIABLE,
                        IPreferenceConstants.USER_VARIABLE, };
                String[] descs = new String[] {
                        IPreferenceConstants.CHARSET_DESCRIPTION,
                        IPreferenceConstants.CLIENT_DESCRIPTION,
                        IPreferenceConstants.NAME_DESCRIPTION,
                        IPreferenceConstants.OFFLINE_DESCRIPTION,
                        IPreferenceConstants.SANDBOX_DESCRIPTION,
                        IPreferenceConstants.STREAM_NAME_DESCRIPTION,
                        IPreferenceConstants.STREAM_ROOT_DESCRIPTION,
                        IPreferenceConstants.SERVER_DESCRIPTION,
                        IPreferenceConstants.USER_DESCRIPTION, };
                VariablesDialog dialog = new VariablesDialog(
                        projectVariablesButton.getShell(), vars, descs);
                if (dialog.open() == VariablesDialog.OK) {
                    String[] selected = dialog.getSelectedVariables();
                    StringBuffer newVariables = new StringBuffer();
                    for (String var : selected) {
                        newVariables.append(var);
                    }
                    projectText.insert(newVariables.toString());
                }
            }

        });

        Label connectLabel = new Label(textArea, SWT.LEFT);
        connectLabel
                .setText(Messages.LabelPreviewPreferencePage_ConnectionDecoration);
        connectText = new Text(textArea, SWT.SINGLE | SWT.BORDER);
        connectText
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        connectText.setText(getPreferenceStore().getString(
                IPreferenceConstants.CONNECTION_DECORATION_TEXT));
        connectText.addModifyListener(textListener);
        connectVariablesButton = new Button(textArea, SWT.PUSH);
        connectVariablesButton
                .setText(Messages.LabelPreviewPreferencePage_AddVariables);
        connectVariablesButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                String[] vars = new String[] {
                        IPreferenceConstants.OFFLINE_VARIABLE,
                        IPreferenceConstants.STREAM_NAME_VARIABLE,
                        IPreferenceConstants.STREAM_ROOT_VARIABLE,
                        IPreferenceConstants.SANDBOX_VARIABLE,
                };
                String[] descs = new String[] {
                        IPreferenceConstants.OFFLINE_DESCRIPTION,
                        IPreferenceConstants.STREAM_NAME_DESCRIPTION,
                        IPreferenceConstants.STREAM_ROOT_DESCRIPTION,
                        IPreferenceConstants.SANDBOX_DESCRIPTION,
                };
                VariablesDialog dialog = new VariablesDialog(
                        connectVariablesButton.getShell(), vars, descs);
                if (dialog.open() == VariablesDialog.OK) {
                    String[] selected = dialog.getSelectedVariables();
                    StringBuffer newVariables = new StringBuffer();
                    for (String var : selected) {
                        newVariables.append(var);
                    }
                    connectText.insert(newVariables.toString());
                }
            }

        });

        Composite bottom = new Composite(textArea, SWT.NONE);
        GridData bData = new GridData(SWT.FILL, SWT.FILL, true, true);
        bData.horizontalSpan = 3;
        bottom.setLayoutData(bData);
        GridLayout bLayout = new GridLayout(2, false);
        bLayout.marginTop = 15;
        bLayout.marginWidth = 0;
        bLayout.marginHeight = 0;
        bottom.setLayout(bLayout);

        ignoreButton = new Button(bottom, SWT.CHECK);
        ignoreButton
                .setText(Messages.LabelPreviewPreferencePage_DecorateIgnoredResources);
        ignoreButton.setSelection(getPreferenceStore().getBoolean(
                IPerforceUIConstants.PREF_IGNORED_TEXT));
        ignoreButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                ignoreText.setEnabled(ignoreButton.getSelection());
            }

        });
        ignoreButton.addSelectionListener(comboAdapter);
        ignoreText = new Text(bottom, SWT.SINGLE | SWT.BORDER);
        ignoreText.setEnabled(ignoreButton.getSelection());
        ignoreText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        ignoreText.setText(getPreferenceStore().getString(
                IPreferenceConstants.IGNORED_DECORATION));
        ignoreText.addModifyListener(textListener);

        Label outgoingChangeLabel = new Label(bottom, SWT.LEFT);
        outgoingChangeLabel
                .setText(Messages.LabelPreviewPreferencePage_OutgoingChangeFlag);
        outgoingChangeText = new Text(bottom, SWT.SINGLE | SWT.BORDER);
        outgoingChangeText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                false));
        outgoingChangeText.setText(getPreferenceStore().getString(
                IPreferenceConstants.OUTGOING_CHANGE_DECORATION));
        outgoingChangeText.addModifyListener(textListener);

        Label unaddedChangeLabel = new Label(bottom, SWT.LEFT);
        unaddedChangeLabel
                .setText(Messages.LabelPreviewPreferencePage_NotUnderVersionControlDecoration);
        unaddedChangeText = new Text(bottom, SWT.SINGLE | SWT.BORDER);
        unaddedChangeText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                false));
        unaddedChangeText.setText(getPreferenceStore().getString(
                IPreferenceConstants.UNADDED_CHANGE_DECORATION));
        unaddedChangeText.addModifyListener(textListener);
    }

    private void addComboOptions(Combo combo) {
        for (String option : ICON_POSITIONS) {
            combo.add(option);
        }
        combo.addSelectionListener(comboAdapter);
    }

    /**
     * Set combo selection from preference
     */
    private void setPosition(Combo list, int position) {
        if ((position & IPerforceUIConstants.ICON_TOP_LEFT) != 0) {
            list.select(1);
        } else if ((position & IPerforceUIConstants.ICON_TOP_RIGHT) != 0) {
            list.select(2);
        } else if ((position & IPerforceUIConstants.ICON_BOTTOM_LEFT) != 0) {
            list.select(3);
        } else if ((position & IPerforceUIConstants.ICON_BOTTOM_RIGHT) != 0) {
            list.select(4);
        } else {
            list.select(0);
        }
    }

    /**
     * Get preference from combo selection
     */
    private int getPosition(Combo list) {
        int idx = list.getSelectionIndex();
        if (idx == 1) {
            return IPerforceUIConstants.ICON_TOP_LEFT;
        } else if (idx == 2) {
            return IPerforceUIConstants.ICON_TOP_RIGHT;
        } else if (idx == 3) {
            return IPerforceUIConstants.ICON_BOTTOM_LEFT;
        } else if (idx == 4) {
            return IPerforceUIConstants.ICON_BOTTOM_RIGHT;
        } else {
            return 0;
        }
    }

    private void setupResources() {
        ConnectionParameters onlineParams = new ConnectionParameters();
        onlineParams.setUser("user1"); //$NON-NLS-1$
        onlineParams.setClient("client1"); //$NON-NLS-1$
        onlineParams.setPort("localhost:1666"); //$NON-NLS-1$
        onlineParams.setCharset("utf8"); //$NON-NLS-1$
        onlineConnection = new PreviewConnection(onlineParams,
                false, false,Messages.PreviewConnection_StreamRoot,Messages.PreviewConnection_StreamName);
        onlineProject = new Project(
                Messages.LabelPreviewPreferencePage_OnlineProject,
                onlineConnection);

        ConnectionParameters offlineParams = new ConnectionParameters();
        offlineParams.setUser("user2"); //$NON-NLS-1$
        offlineParams.setClient("client2"); //$NON-NLS-1$
        offlineParams.setPort("server:1666"); //$NON-NLS-1$
        offlineConnection = new PreviewConnection(offlineParams,
                true, false,Messages.PreviewConnection_StreamRoot,Messages.PreviewConnection_StreamName);
        offlineProject = new Project(
                Messages.LabelPreviewPreferencePage_OfflineProject,
                offlineConnection);

        ConnectionParameters sandboxParams = new ConnectionParameters();
        sandboxParams.setUser("user3"); //$NON-NLS-1$
        sandboxParams.setClient("sandbox_client"); //$NON-NLS-1$
        sandboxParams.setPort("server:1666"); //$NON-NLS-1$
        onlineSandboxConnection = new PreviewConnection(sandboxParams,
                false, true,Messages.LabelPreviewPreferencePage_SandboxStreamRoot,Messages.LabelPreviewPreferencePage_SandboxStreamName);
        sandboxProject = new Project(
                Messages.LabelPreviewPreferencePage_SandboxProject,
                onlineSandboxConnection);
        ConnectionParameters offlineSandboxParams = new ConnectionParameters();
        offlineSandboxParams.setUser("user4"); //$NON-NLS-1$
        offlineSandboxParams.setClient("sandbox_client"); //$NON-NLS-1$
        offlineSandboxParams.setPort("server:1666"); //$NON-NLS-1$
        offlineSandboxConnection = new PreviewConnection(offlineSandboxParams,
                true, true,Messages.LabelPreviewPreferencePage_SandboxStreamRoot,Messages.LabelPreviewPreferencePage_SandboxStreamName);
        offlineSandboxProject = new Project(
                Messages.LabelPreviewPreferencePage_SandboxProject,
                offlineSandboxConnection);
        
        ignoredFolder = new Folder(
                Messages.LabelPreviewPreferencePage_IgnoredFolder,
                onlineConnection);

        addedFile = new PreviewP4File();
        addedFile.setAction(FileAction.ADD);
        addedFile.setName("added.txt"); //$NON-NLS-1$
        addedFile.setHeadType("text"); //$NON-NLS-1$
        addedFile.setOpenedByOwner(true);
        editedFile = new PreviewP4File();
        editedFile.setSynced(true);
        editedFile.setAction(FileAction.EDIT);
        editedFile.setName("edited.h"); //$NON-NLS-1$
        editedFile.setHeadType("text"); //$NON-NLS-1$
        editedFile.setHaveRevision(3);
        editedFile.setHeadRevision(3);
        editedFile.setOpenedByOwner(true);
        deletedFile = new PreviewP4File();
        deletedFile.setSynced(true);
        deletedFile.setName("deleted.png"); //$NON-NLS-1$
        deletedFile.setHeadType("ubinary"); //$NON-NLS-1$
        deletedFile.setAction(FileAction.DELETE);
        deletedFile.setHaveRevision(2);
        deletedFile.setHeadRevision(2);
        deletedFile.setOpenedByOwner(true);
        unresolvedFile = new PreviewP4File();
        unresolvedFile.setName("unresolved.zip"); //$NON-NLS-1$
        unresolvedFile.setAction(FileAction.EDIT);
        unresolvedFile.setHeadRevision(2);
        unresolvedFile.setHaveRevision(2);
        unresolvedFile.setSynced(true);
        unresolvedFile.setUnresolved(true);
        unresolvedFile.setHeadType("binary"); //$NON-NLS-1$
        lockedFile = new PreviewP4File();
        lockedFile.setName("locked.txt"); //$NON-NLS-1$
        lockedFile.setLocked(true);
        lockedFile.setSynced(true);
        lockedFile.setHaveRevision(1);
        lockedFile.setHeadRevision(1);
        lockedFile.setHeadType("text"); //$NON-NLS-1$
        lockedFile.setAction(FileAction.EDIT);
        syncedFile = new PreviewP4File();
        syncedFile.setName("Synced.java"); //$NON-NLS-1$
        syncedFile.setHeadType("text"); //$NON-NLS-1$
        syncedFile.setSynced(true);
        syncedFile.setHaveRevision(18);
        syncedFile.setHeadRevision(18);
        notSyncedFile = new PreviewP4File();
        notSyncedFile.setName("NotSynced.java"); //$NON-NLS-1$
        notSyncedFile.setHeadType("text"); //$NON-NLS-1$
        notSyncedFile.setSynced(false);
        notSyncedFile.setHaveRevision(5);
        notSyncedFile.setHeadRevision(8);
        addedElsewhereFile = new PreviewP4File();
        addedElsewhereFile.setName("added_elsewhere.xml"); //$NON-NLS-1$
        addedElsewhereFile.setAction(FileAction.ADD);
        addedElsewhereFile.setOpenedByOwner(false);
        addedElsewhereFile.setHeadType("text"); //$NON-NLS-1$
        editedElsewhereFile = new PreviewP4File();
        editedElsewhereFile.setName("edited_elsewhere.xml"); //$NON-NLS-1$
        editedElsewhereFile.setAction(FileAction.EDIT);
        editedElsewhereFile.setOpenedByOwner(false);
        editedElsewhereFile.setHeadType("text"); //$NON-NLS-1$
        deletedElsewhereFile = new PreviewP4File();
        deletedElsewhereFile.setName("deleted_elsewhere.xml"); //$NON-NLS-1$
        deletedElsewhereFile.setAction(FileAction.DELETE);
        deletedElsewhereFile.setOpenedByOwner(false);
        deletedElsewhereFile.setHeadType("text"); //$NON-NLS-1$
        branchFile = new PreviewP4File();
        branchFile.setName("branch.txt"); //$NON-NLS-1$
        branchFile.setHeadType("text"); //$NON-NLS-1$
        branchFile.setOpenedByOwner(true);
        branchFile.setAction(FileAction.BRANCH);
        branchElsewhereFile = new PreviewP4File();
        branchElsewhereFile.setName("branched_elsewhere.txt"); //$NON-NLS-1$
        branchElsewhereFile.setHeadType("text"); //$NON-NLS-1$
        branchElsewhereFile.setOpenedByOwner(false);
        branchElsewhereFile.setAction(FileAction.BRANCH);
        integrateFile = new PreviewP4File();
        integrateFile.setName("integrate.properties"); //$NON-NLS-1$
        integrateFile.setHeadType("text"); //$NON-NLS-1$
        integrateFile.setOpenedByOwner(true);
        integrateFile.setAction(FileAction.INTEGRATE);
        integrateElsewhereFile = new PreviewP4File();
        integrateElsewhereFile.setName("integrated_elsewhere.properties"); //$NON-NLS-1$
        integrateElsewhereFile.setHeadType("text"); //$NON-NLS-1$
        integrateElsewhereFile.setOpenedByOwner(false);
        integrateElsewhereFile.setAction(FileAction.INTEGRATE);
        ignoredFile = new PreviewP4File();
        ignoredFile.setName("ignored.txt"); //$NON-NLS-1$
        unmanagedFile = new PreviewP4File();
        unmanagedFile.setName("not_in_Eclipse.txt"); //$NON-NLS-1$
        unmanagedFile.setUnmanaged(true);
        unmanagedFile.setHaveRevision(1);
        unmanagedFile.setHeadRevision(1);
        unmanagedFile.setAction(FileAction.EDIT);
        unmanagedFile.setSynced(true);
        unmanagedFile.setOpenedByOwner(true);
        localOnly = new PreviewP4File();
        localOnly.setName("not_in_depot.log"); //$NON-NLS-1$
        localOnly.setFileSpec(null);
    }

    /**
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench) {
        setPreferenceStore(PerforceUIPlugin.getPlugin().getPreferenceStore());
        setupResources();
    }

    /**
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        super.performDefaults();
        IPreferenceStore store = getPreferenceStore();
        setPosition(openFilesCombo,
                store.getDefaultInt(IPerforceUIConstants.PREF_FILE_OPEN_ICON));
        setPosition(notSyncedFilesCombo,
                store.getDefaultInt(IPerforceUIConstants.PREF_FILE_SYNC_ICON));
        setPosition(syncedFilesCombo,
                store.getDefaultInt(IPerforceUIConstants.PREF_FILE_SYNC2_ICON));
        setPosition(
                unresolvedFilesCombo,
                store.getDefaultInt(IPerforceUIConstants.PREF_FILE_UNRESOLVED_ICON));
        setPosition(lockedFilesCombo,
                store.getDefaultInt(IPerforceUIConstants.PREF_FILE_LOCK_ICON));
        setPosition(openElsewhereFilesCombo,
                store.getDefaultInt(IPerforceUIConstants.PREF_FILE_OTHER_ICON));
        setPosition(sharedProjectsCombo,
                store.getDefaultInt(IPerforceUIConstants.PREF_PROJECT_ICON));
        setPosition(ignoredFilesCombo,
                store.getDefaultInt(IPerforceUIConstants.PREF_IGNORED_ICON));
        setPosition(localFilesCombo,
                store.getDefaultInt(IPerforceUIConstants.PREF_LOCAL_ONLY_ICON));
        setPosition(streamSandboxCombo,
                store.getDefaultInt(IPerforceUIConstants.PREF_STREAM_SANDBOX_ICON));
        setPosition(streamSandboxProjectCombo,
                store.getDefaultInt(IPerforceUIConstants.PREF_STREAM_SANDBOX_PROJECT_ICON));

        ignoreButton.setSelection(store
                .getDefaultBoolean(IPerforceUIConstants.PREF_IGNORED_TEXT));
        ignoreText.setText(store
                .getDefaultString(IPreferenceConstants.IGNORED_DECORATION));
        outgoingChangeText
                .setText(store
                        .getDefaultString(IPreferenceConstants.OUTGOING_CHANGE_DECORATION));
        unaddedChangeText
                .setText(store
                        .getDefaultString(IPreferenceConstants.UNADDED_CHANGE_DECORATION));

        connectText
        .setText(store
                .getDefaultString(IPreferenceConstants.CONNECTION_DECORATION_TEXT));
        projectText
        .setText(store
                .getDefaultString(IPreferenceConstants.PROJECT_DECORATION_TEXT));
        fileText.setText(store
                .getDefaultString(IPreferenceConstants.FILE_DECORATION_TEXT));

        showChangelistInSyncButton
                .setSelection(store
                        .getDefaultBoolean(IPreferenceConstants.SHOW_CHANGELIST_IN_SYNC_VIEW));

        unmanagedFilesButton
                .setSelection(store
                        .getDefaultBoolean(IPerforceUIConstants.PREF_MARK_UNMANAGED_FILES));

        comboAdapter.widgetSelected(new SelectionEvent(new Event()));
        textListener.modifyText(new ModifyEvent(new Event()));
    }

    /**
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        IPreferenceStore store = getPreferenceStore();
        store.setValue(IPerforceUIConstants.PREF_FILE_OPEN_ICON,
                getPosition(openFilesCombo));
        store.setValue(IPerforceUIConstants.PREF_FILE_SYNC_ICON,
                getPosition(notSyncedFilesCombo));
        store.setValue(IPerforceUIConstants.PREF_FILE_SYNC2_ICON,
                getPosition(syncedFilesCombo));
        store.setValue(IPerforceUIConstants.PREF_FILE_UNRESOLVED_ICON,
                getPosition(unresolvedFilesCombo));
        store.setValue(IPerforceUIConstants.PREF_FILE_LOCK_ICON,
                getPosition(lockedFilesCombo));
        store.setValue(IPerforceUIConstants.PREF_FILE_OTHER_ICON,
                getPosition(openElsewhereFilesCombo));
        store.setValue(IPerforceUIConstants.PREF_PROJECT_ICON,
                getPosition(sharedProjectsCombo));
        store.setValue(IPerforceUIConstants.PREF_IGNORED_ICON,
                getPosition(ignoredFilesCombo));
        store.setValue(IPerforceUIConstants.PREF_STREAM_SANDBOX_ICON,
                getPosition(streamSandboxCombo));
        store.setValue(IPerforceUIConstants.PREF_STREAM_SANDBOX_PROJECT_ICON,
                getPosition(streamSandboxProjectCombo));
        store.setValue(IPreferenceConstants.IGNORED_DECORATION,
                ignoreText.getText());
        store.setValue(IPreferenceConstants.OUTGOING_CHANGE_DECORATION,
                outgoingChangeText.getText());
        store.setValue(IPreferenceConstants.UNADDED_CHANGE_DECORATION,
                unaddedChangeText.getText());
        store.setValue(IPreferenceConstants.PROJECT_DECORATION_TEXT,
                projectText.getText());
        store.setValue(IPreferenceConstants.CONNECTION_DECORATION_TEXT,
                connectText.getText());
        store.setValue(IPreferenceConstants.FILE_DECORATION_TEXT,
                fileText.getText());
        store.setValue(IPreferenceConstants.SHOW_CHANGELIST_IN_SYNC_VIEW,
                showChangelistInSyncButton.getSelection());
        store.setValue(IPerforceUIConstants.PREF_IGNORED_TEXT,
                ignoreButton.getSelection());
        store.setValue(IPerforceUIConstants.PREF_LOCAL_ONLY_ICON,
                getPosition(localFilesCombo));
        store.setValue(IPerforceUIConstants.PREF_MARK_UNMANAGED_FILES,
                unmanagedFilesButton.getSelection());
        return super.performOk();
    }

    /**
     * @return - show changelist decoration in synchronize selection
     */
    public boolean getChangelistInSyncSelection() {
        return this.showChangelistInSyncButton.getSelection();
    }

    /**
     * @return - ignored button selection
     */
    public boolean getIgnoredTextSelection() {
        return this.ignoreButton.getSelection();
    }

    /**
     * @return - ignored text
     */
    public String getIgnoredText() {
        return this.ignoreText.getText();
    }

    /**
     * @return - outgoing text
     */
    public String getOutgoingText() {
        return this.outgoingChangeText.getText();
    }

    /**
     * @return - unadded text
     */
    public String getUnaddedText() {
        return this.unaddedChangeText.getText();
    }

    /**
     * @return - connect text
     */
    public String getConnectionText() {
        return this.connectText.getText();
    }

    /**
     * @return - project text
     */
    public String getProjectText() {
        return this.projectText.getText();
    }

    /**
     * @return - file text
     */
    public String getFileText() {
        return this.fileText.getText();
    }

    /**
     * @return - opened icon text
     */
    public String getOpenIconText() {
        return this.openFilesCombo.getText();
    }

    /**
     * @return - synced icon text
     */
    public String getSyncIconText() {
        return this.syncedFilesCombo.getText();
    }

    /**
     * @return - not synced icon text
     */
    public String getNotSyncIconText() {
        return this.notSyncedFilesCombo.getText();
    }

    /**
     * @return - unresolved icon text
     */
    public String getUnresolvedIconText() {
        return this.unresolvedFilesCombo.getText();
    }

    /**
     * @return - locked icon text
     */
    public String getLockedIconText() {
        return this.lockedFilesCombo.getText();
    }

    /**
     * @return - opened elsewhere icon text
     */
    public String getOpenedElsewhereIconText() {
        return this.openElsewhereFilesCombo.getText();
    }

    /**
     * @return - ignored icon text
     */
    public String getIgnoredIconText() {
        return this.ignoredFilesCombo.getText();
    }

    /**
     * @return - project icon text
     */
    public String getProjectIconText() {
        return this.sharedProjectsCombo.getText();
    }

}
