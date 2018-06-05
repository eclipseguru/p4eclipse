/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.diff;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.editor.HaveQuickDiffProvider;
import com.perforce.team.ui.editor.HeadQuickDiffProvider;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class QuickDiffTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        addFile(project.getFile("plugin.xml"), new ByteArrayInputStream(
                "<plugin></plugin>".getBytes()));
        addFile(project.getFile("plugin.xml"), new ByteArrayInputStream(
                "<plugin id=\"test\"></plugin>".getBytes()));
    }

    /**
     * Test quick diff against have revision
     */
    public void testHave() {
        HaveQuickDiffProvider provider = new HaveQuickDiffProvider();
        assertFalse(provider.isEnabled());
        final IFile file = project.getFile("plugin.xml");
        assertTrue(file.exists());
        try {
        	final boolean[] activated=new boolean[]{false};
        	PerforceUIPlugin.getActivePage().addPartListener(new IPartListener() {
				
				@Override
				public void partOpened(IWorkbenchPart part) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void partDeactivated(IWorkbenchPart part) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void partClosed(IWorkbenchPart part) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void partBroughtToTop(IWorkbenchPart part) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void partActivated(IWorkbenchPart part) {
					if(part instanceof IEditorPart){
						IEditorInput input = ((IEditorPart) part).getEditorInput();
						if(input instanceof IFileEditorInput){
							if(((IFileEditorInput) input).getFile()==file){
								activated[0]=true;
							}
						}
					}
				}
			});
            IEditorPart part = IDE.openEditor(PerforceUIPlugin.getActivePage(),
                    file, "org.eclipse.ui.DefaultTextEditor");
            int total=0;
            while(!activated[0] && total<100){
            	Utils.sleep(0.1);// wait for editor activate.
            	total++;
            }
            ITextEditor editor = (ITextEditor) part;
            provider.setActiveEditor(editor);
            assertTrue(provider.isEnabled());
            assertNull(provider.getId());
            provider.setId("test");
            assertNotNull(provider.getId());
            IDocument document = provider
                    .getReference(new NullProgressMonitor());

            // wait for opening file.
            total=0;
            while(document.get().length()==0 && total<100){
            	Utils.sleep(0.1);
            	document=provider
                        .getReference(new NullProgressMonitor());
            	total++;
            }

            assertNotNull(document);
            assertTrue(document.get().length() > 0);
            assertEquals(document.get(), Utils.getContent(file));
        } catch (Exception e) {
            handle(e);
            provider.dispose();
        }
    }

    /**
     * Test quick diff against head revision
     */
    public void testHead() {
        HeadQuickDiffProvider provider = new HeadQuickDiffProvider();
        assertFalse(provider.isEnabled());
        IFile file = project.getFile("plugin.xml");
        assertTrue(file.exists());
        try {
            IEditorPart part = IDE.openEditor(PerforceUIPlugin.getActivePage(),
                    file, "org.eclipse.ui.DefaultTextEditor");
            Utils.sleep(2);// wait for loading.
            ITextEditor editor = (ITextEditor) part;
            provider.setActiveEditor(editor);
            assertTrue(provider.isEnabled());
            assertNull(provider.getId());
            provider.setId("test");
            assertNotNull(provider.getId());
            IDocument document = provider
                    .getReference(new NullProgressMonitor());
            
            // wait for opening file.
            int total=0;
            while(document.get().length()==0 && total<100){
            	Utils.sleep(0.1);
            	document=provider
                        .getReference(new NullProgressMonitor());
            	total++;
            }

            assertNotNull(document);
            assertTrue(document.get().length() > 0);
            assertEquals(document.get(), Utils.getContent(file));
        } catch (Exception e) {
            handle(e);
            provider.dispose();
        }
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.plugin";
    }

}
