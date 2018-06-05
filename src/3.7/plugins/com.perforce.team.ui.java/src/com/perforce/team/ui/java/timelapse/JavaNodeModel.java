/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.java.timelapse;

import com.perforce.p4java.server.PerforceCharsets;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.ui.text.timelapse.IFilterNodeModel;
import com.perforce.team.ui.text.timelapse.NodeModel;
import com.perforce.team.ui.timelapse.IRevisionInputCache;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.ui.IEditorInput;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class JavaNodeModel extends NodeModel implements IFilterNodeModel {

    /**
     * JAVA_SUFFIX
     */
    public static final String JAVA_SUFFIX = ".java"; //$NON-NLS-1$

    /**
     * JAVA_SUFFIX_LENGTH
     */
    public static final int JAVA_SUFFIX_LENGTH = JAVA_SUFFIX.length();

    private Map<String, String> methodList;

    /**
     * @param revisions
     * @param inputCache
     */
    public JavaNodeModel(IP4Revision[] revisions, IRevisionInputCache inputCache) {
        super(revisions, inputCache);
        methodList = new TreeMap<String, String>();
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.IFilterNodeModel#getFilterKey(java.lang.String)
     */
    public String getFilterKey(String filterLabel) {
        return this.methodList.get(filterLabel);

    }

    /**
     * @see com.perforce.team.ui.text.timelapse.IFilterNodeModel#getFilterLabels()
     */
    public Collection<String> getFilterLabels() {
        return this.methodList.keySet();
    }

    /**
     * Get java element
     * 
     * @param revision
     * @param id
     * @return - java element
     */
    public IJavaElement getJavaElement(IP4Revision revision, String id) {
        return (IJavaElement) getElement(revision, id);
    }

    private String getMethodDisplay(IMethod method) {
        StringBuffer buffer = new StringBuffer();
        JavaElementLabels.getElementLabel(method,
                JavaElementLabels.M_PARAMETER_TYPES
                        | JavaElementLabels.M_POST_QUALIFIED, buffer);

        // Handle possible collisision created by anonymous inner classes
        if (method.getParent() instanceof IMember) {
            IMember member = (IMember) method.getParent();
            if (member.getElementName() == null
                    || member.getElementName().length() == 0) {
                buffer.append('$');
                buffer.append(member.getOccurrenceCount());
            }
        }
        return buffer.toString();
    }

    private String getFileClassHandle(IJavaElement element) {
        String handle = element.getHandleIdentifier();
        if (element instanceof IMethod) {
            // Support refactored classes where methods are in a class that is
            // in the same name as the source file.
            //
            // For example if method update() is in class Cache in file
            // Cache.java, the key will just be update(). And if
            // Cache.java is later refactored/renamed to FileCache.java and
            // there is a class FileCache with an update method, the history
            // from Cache.update() will be linked with FileCache.update() since
            // they will share the same id since they are in a class-file match
            //
            // This seems to be a very common case when renaming files and
            // classes at the same time.
            ICompilationUnit unit = ((IMethod) element).getCompilationUnit();
            if (unit != null) {
                String fileName = unit.getElementName();
                if (fileName.endsWith(JAVA_SUFFIX)) {
                    String className = fileName.substring(0, fileName.length()
                            - JAVA_SUFFIX_LENGTH);
                    String fileClassMatch = fileName + JavaElement.JEM_TYPE
                            + className;
                    if (handle.contains(fileClassMatch)) {
                        handle = handle.replace(fileClassMatch, ""); //$NON-NLS-1$
                    }
                }
            }
        }
        return handle;
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeModel#findNodes(java.lang.Object,
     *      com.perforce.team.core.p4java.IP4Revision)
     */
    @Override
    public void findNodes(Object nodeInput, IP4Revision revision) {
        walkTree((IJavaElement) nodeInput, revision);
    }

    private void walkTree(IJavaElement node, IP4Revision revision) {
        try {
            String key = null;
            if (node instanceof IMethod) {
                IMethod method = (IMethod) node;
                key = getFileClassHandle(method);
                String display = getMethodDisplay(method);
                methodList.put(display, key);
            }
            if (node instanceof ISourceReference) {
                ISourceReference reference = (ISourceReference) node;
                String content = reference.getSource();
                if (content != null) {
                    int hash = computeHash(content,P4CoreUtils.charsetForName(PerforceCharsets.getJavaCharsetName(revision.getCharset())));
                    String key2 = null;
                    if (key == null) {
                        key = node.getHandleIdentifier();
                    } else {
                        key2 = node.getHandleIdentifier();
                    }
                    addRecord(hash, key, key2, node, revision);
                }
            }
            if (node instanceof IParent) {
                IParent parent = (IParent) node;
                if (parent.hasChildren()) {
                    for (IJavaElement child : parent.getChildren()) {
                        walkTree(child, revision);
                    }
                }
            }
        } catch (JavaModelException e) {
            PerforceProviderPlugin.logError(e);
        }
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeModel#parseInput(org.eclipse.ui.IEditorInput,
     *      com.perforce.team.core.p4java.IP4Revision)
     */
    @Override
    public Object parseInput(IEditorInput input, IP4Revision revision) {
        if (JavaUtils.getProvider().getDocument(input) == null) {
            try {
                JavaUtils.getProvider().connect(input);
            } catch (CoreException e) {
                PerforceProviderPlugin.logError(e);
            }
        }
        return JavaUtils.getWorkingCopy(input);
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.INodeModel#getHandle(java.lang.Object)
     */
    public String getHandle(Object element) {
        return element instanceof IJavaElement
                ? getFileClassHandle((IJavaElement) element)
                : null;
    }

}
