/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.python.timelapse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.dltk.core.Flags;
import org.eclipse.dltk.core.IBuffer;
import org.eclipse.dltk.core.IMethod;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.IParent;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.ISourceReference;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.internal.core.SourceRefElement;
import org.eclipse.dltk.ui.ScriptElementLabels;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IStorageEditorInput;

import com.perforce.p4java.server.PerforceCharsets;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.ui.text.timelapse.IFilterNodeModel;
import com.perforce.team.ui.text.timelapse.NodeModel;
import com.perforce.team.ui.timelapse.IRevisionInputCache;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class PythonNodeModel extends NodeModel implements IFilterNodeModel {

    /**
     * PYTHON_SUFFIX
     */
    public static final String PYTHON_SUFFIX = ".py"; //$NON-NLS-1$

    /**
     * PYTHON_SUFFIX_LENGTH
     */
    public static final int PYTHON_SUFFIX_LENGTH = PYTHON_SUFFIX.length();

    /**
     * PYTHON_PREFIX
     */
    public static final String PYTHON_PREFIX = "P4_PYTHON_PREFIX"; //$NON-NLS-1$

    /**
     * PYTHON_PREFIX_REGEX
     */
    public static final String PYTHON_PREFIX_REGEX = PYTHON_PREFIX + "\\d+"; //$NON-NLS-1$

    /**
     * PUBLIC
     */
    public static final String PUBLIC = "PUBLIC"; //$NON-NLS-1$

    /**
     * PROTECTED
     */
    public static final String PROTECTED = "PROTECTED"; //$NON-NLS-1$

    /**
     * PRIVATE
     */
    public static final String PRIVATE = "PRIVATE"; //$NON-NLS-1$

    private Map<String, String> methodList;
    private List<IEditorInput> converted;

    /**
     * @param revisions
     * @param inputCache
     */
    public PythonNodeModel(IP4Revision[] revisions,
            IRevisionInputCache inputCache) {
        super(revisions, inputCache);
        methodList = new TreeMap<String, String>();
        converted = new ArrayList<IEditorInput>();
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeModel#clear()
     */
    @Override
    public void clear() {
        super.clear();
        converted.clear();
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
     * Get python element
     * 
     * @param revision
     * @param id
     * @return - python element
     */
    public IModelElement getPythonElement(IP4Revision revision, String id) {
        return (IModelElement) getElement(revision, id);
    }

    private static String getFileClassHandle(IModelElement element) {
        String handle = element.getHandleIdentifier();
        if (handle != null && element instanceof SourceRefElement) {
            SourceRefElement ref = (SourceRefElement) element;
            if (ref.occurrenceCount > 1) {
                handle += "#" + ref.occurrenceCount; //$NON-NLS-1$
            }
        }
        if (handle != null && handle.indexOf(PYTHON_PREFIX) != -1) {
            handle = handle.replaceAll(PYTHON_PREFIX_REGEX, ""); //$NON-NLS-1$
        }
        return handle;
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeModel#findNodes(java.lang.Object,
     *      com.perforce.team.core.p4java.IP4Revision)
     */
    @Override
    public void findNodes(Object nodeInput, IP4Revision revision) {
        walkTree((IModelElement) nodeInput, revision);
    }

    private void walkTree(IModelElement node, IP4Revision revision) {
        try {
            String key = null;
            if (node != null) {
                key = getFileClassHandle(node);
            }
            if (node instanceof IMethod) {
                IMethod method = (IMethod) node;
                String display = getMethodDisplay(method);
                methodList.put(display, key);
            }
            if (node instanceof ISourceReference) {
                ISourceReference reference = (ISourceReference) node;
                String content = reference.getSource();
                if (content != null) {
                    String visibility = ""; //$NON-NLS-1$
                    if (node instanceof IMethod) {
                        int flags = ((IMethod) node).getFlags();
                        if (Flags.isPrivate(flags)) {
                            visibility = PRIVATE;
                        } else if (Flags.isProtected(flags)) {
                            visibility = PROTECTED;
                        } else if (Flags.isPublic(flags)) {
                            visibility = PUBLIC;
                        }
                    }
                    int hash = computeHash(visibility + content,P4CoreUtils.charsetForName(PerforceCharsets.getJavaCharsetName(revision.getCharset())));
                    String key2 = null;
                    if (key != null) {
                        key2 = node.getHandleIdentifier();
                    }
                    addRecord(hash, key, key2, node, revision);
                }
            }
            if (node instanceof IParent) {
                IParent parent = (IParent) node;
                if (parent.hasChildren()) {
                    for (IModelElement child : parent.getChildren()) {
                        walkTree(child, revision);
                    }
                }
            }
        } catch (ModelException e) {
            PerforceProviderPlugin.logError(e);
        }
    }

    /**
     * @param method
     * @return
     */
    private String getMethodDisplay(IMethod method) {
        StringBuffer buffer = new StringBuffer();
        ScriptElementLabels.getDefault().getElementLabel(
                method,
                ScriptElementLabels.M_PARAMETER_TYPES
                        | ScriptElementLabels.M_POST_QUALIFIED, buffer);
        if (method instanceof SourceRefElement) {
            SourceRefElement element = (SourceRefElement) method;
            if (element.occurrenceCount > 1) {
                buffer.append(' ');
                buffer.append('#');
                buffer.append(element.occurrenceCount);
            }
        }
        return buffer.toString();
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeModel#parseInput(org.eclipse.ui.IEditorInput,
     *      com.perforce.team.core.p4java.IP4Revision)
     */
    @Override
    public Object parseInput(IEditorInput input, IP4Revision revision) {
        if (PythonUtils.getProvider().getDocument(input) == null) {
            try {
                PythonUtils.getProvider().connect(input);
            } catch (CoreException e) {
                PerforceProviderPlugin.logError(e);
            }
        }
        ISourceModule module = PythonUtils.getWorkingCopy(input);
        if (input instanceof IStorageEditorInput && !converted.contains(input)) {
            BufferedReader reader = null;
            try {
                IStorage storage = ((IStorageEditorInput) input).getStorage();
                reader = new BufferedReader(new InputStreamReader(
                        storage.getContents(),P4CoreUtils.charsetForName(PerforceCharsets.getJavaCharsetName(revision.getCharset()))));
                char[] buffer = new char[4096];
                StringBuilder content = new StringBuilder();
                int read = reader.read(buffer);
                while (read > 0) {
                    content.append(buffer, 0, read);
                    read = reader.read(buffer);
                }
                IBuffer moduleBuffer = module.getBuffer();
                if (moduleBuffer != null) {
                    moduleBuffer.setContents(content.toString());
                }
                module.makeConsistent(new NullProgressMonitor());
                converted.add(input);
            } catch (CoreException e) {
                PerforceProviderPlugin.logError(e);
            } catch (IOException e) {
                PerforceProviderPlugin.logError(e);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        // Do nothing
                    }
                }
            }

        }
        return module;
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.INodeModel#getHandle(java.lang.Object)
     */
    public String getHandle(Object element) {
        return getPythonHandle(element);
    }

    /**
     * Get python handle
     * 
     * @param element
     * @return - handle
     */
    public static String getPythonHandle(Object element) {
        return element instanceof IModelElement
                ? getFileClassHandle((IModelElement) element)
                : null;
    }

}
