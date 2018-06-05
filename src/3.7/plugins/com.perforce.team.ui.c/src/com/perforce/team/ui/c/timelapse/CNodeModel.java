/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.c.timelapse;

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

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.core.model.IMember;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.internal.core.model.CElement;
import org.eclipse.cdt.internal.ui.viewsupport.CElementLabels;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorInput;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class CNodeModel extends NodeModel implements IFilterNodeModel {

    /**
     * Get the String handle for a c element
     * 
     * @param element
     * @return - string handle
     */
    public static String getCHandle(Object element) {
        return element instanceof ICElement
                ? generateCHandle((ICElement) element)
                : null;
    }

    private static String generateCHandle(ICElement element) {
        String handle = element.getHandleIdentifier();
        if (handle != null) {
            int sourceElementDelimiter = handle
                    .indexOf(CElement.CEM_SOURCEELEMENT);
            while (sourceElementDelimiter != -1) {
                if (sourceElementDelimiter - 1 > 0
                        && handle.charAt(sourceElementDelimiter - 1) == CElement.CEM_ESCAPE) {
                    sourceElementDelimiter = handle.indexOf(
                            CElement.CEM_SOURCEELEMENT,
                            sourceElementDelimiter + 1);
                } else {
                    handle = handle.substring(sourceElementDelimiter);
                    break;
                }
            }
        }
        return handle;
    }

    private Map<String, String> methodList;

    /**
     * @param revisions
     * @param inputCache
     */
    public CNodeModel(IP4Revision[] revisions, IRevisionInputCache inputCache) {
        super(revisions, inputCache);
        methodList = new TreeMap<String, String>();
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.IFilterNodeModel#getFilterLabels()
     */
    public Collection<String> getFilterLabels() {
        return this.methodList.keySet();
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.IFilterNodeModel#getFilterKey(java.lang.String)
     */
    public String getFilterKey(String filterLabel) {
        return this.methodList.get(filterLabel);
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeModel#findNodes(java.lang.Object,
     *      com.perforce.team.core.p4java.IP4Revision)
     */
    @Override
    public void findNodes(Object nodeInput, IP4Revision revision) {
        walkTree((ICElement) nodeInput, revision);
    }

    private String getMethodDisplay(IFunctionDeclaration method) {
    	try {
			return method.getSignature();
		} catch (CModelException e) {
			e.printStackTrace();
			return method.getElementName();
		}
//        return CElementLabels.getElementLabel(method,
//                CElementLabels.M_PARAMETER_TYPES);
    }

    /**
     * Get the String handle for a c element
     * 
     * @param element
     * @return - string handle
     */
    public String getHandle(Object element) {
        return getCHandle(element);
    }

    private void walkTree(ICElement node, IP4Revision revision) {
        try {
            String key = null;
            if (node instanceof IFunctionDeclaration) {
                IFunctionDeclaration method = (IFunctionDeclaration) node;
                key = generateCHandle(node);
                String display = getMethodDisplay(method);
                methodList.put(display, key);
            }
            if (node instanceof ISourceReference) {
                ISourceReference reference = (ISourceReference) node;
                String content = reference.getSource();
                if (content != null) {
                    // Fix for job036672, trigger visibility changes as a new
                    // node revision even if content of node may not have
                    // changed
                    if (node instanceof IMember) {
                        ASTAccessVisibility visibility = ((IMember) node)
                                .getVisibility();
                        if (visibility != null) {
                            content = visibility.toString() + content;
                        }
                    }
                    int hash = computeHash(content,P4CoreUtils.charsetForName(PerforceCharsets.getJavaCharsetName(revision.getCharset())));
                    String key2 = null;
                    if (key == null) {
                        key = generateCHandle(node);
                    } else {
                        key2 = generateCHandle(node);
                    }
                    addRecord(hash, key, key2, node, revision);
                }
            }
            if (node instanceof IParent) {
                IParent parent = (IParent) node;
                if (parent.hasChildren()) {
                    for (ICElement child : parent.getChildren()) {
                        walkTree(child, revision);
                    }
                }
            }
        } catch (CModelException e) {
            PerforceProviderPlugin.logError(e);
        }
    }

    /**
     * Get c element
     * 
     * @param revision
     * @param id
     * @return - c element
     */
    public ICElement getCElement(IP4Revision revision, String id) {
        return (ICElement) getElement(revision, id);
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeModel#parseInput(org.eclipse.ui.IEditorInput,
     *      com.perforce.team.core.p4java.IP4Revision)
     */
    @Override
    public Object parseInput(IEditorInput input, IP4Revision revision) {
        if (CUtils.getProvider().getDocument(input) == null) {
            try {
                CUtils.getProvider().connect(input);
            } catch (CoreException e) {
                PerforceProviderPlugin.logError(e);
            }
        }
        return CUtils.getWorkingCopy(input);
    }

}
