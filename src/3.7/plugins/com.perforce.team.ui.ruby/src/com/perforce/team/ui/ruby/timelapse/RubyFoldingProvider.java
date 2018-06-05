/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.perforce.team.ui.ruby.timelapse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.eclipse.core.runtime.ILog;
import org.eclipse.dltk.ast.ASTNode;
import org.eclipse.dltk.ast.declarations.Declaration;
import org.eclipse.dltk.ast.declarations.MethodDeclaration;
import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
import org.eclipse.dltk.ast.declarations.TypeDeclaration;
import org.eclipse.dltk.ast.expressions.CallExpression;
import org.eclipse.dltk.ruby.ast.FakeModuleDeclaration;
import org.eclipse.dltk.ruby.core.RubyConstants;
import org.eclipse.dltk.ruby.core.RubyNature;
import org.eclipse.dltk.ruby.internal.ui.RubyPreferenceConstants;
import org.eclipse.dltk.ruby.internal.ui.RubyUI;
import org.eclipse.dltk.ruby.internal.ui.text.IRubyPartitions;
import org.eclipse.dltk.ruby.internal.ui.text.RubyPartitionScanner;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;

/**
 * Ruby folding provider
 */
public class RubyFoldingProvider extends ASTFoldingProvider {

    /**
     * @param viewer
     * @param support
     */
    public RubyFoldingProvider(ProjectionViewer viewer,
            ProjectionSupport support) {
        super(viewer, support);
    }

    @Override
    protected String getCommentPartition() {
        return IRubyPartitions.RUBY_COMMENT;
    }

    @Override
    protected String getDocPartition() {
        return IRubyPartitions.RUBY_DOC;
    }

    @Override
    protected String getPartition() {
        return IRubyPartitions.RUBY_PARTITIONING;
    }

    @Override
    protected IPartitionTokenScanner getPartitionScanner() {
        return new RubyPartitionScanner();
    }

    @Override
    protected String[] getPartitionTypes() {
        return IRubyPartitions.RUBY_PARTITION_TYPES;
    }

    @Override
    protected String getNatureId() {
        return RubyNature.NATURE_ID;
    }

    @Override
    protected ILog getLog() {
        return RubyUI.getDefault().getLog();
    }

    @Override
    protected CodeBlock[] getCodeBlocks(String code, int offset) {
        ModuleDeclaration decl = parse(code, offset);
        if (decl==null || decl instanceof FakeModuleDeclaration) {
            return null;
        }
        return buildCodeBlocks(decl, offset);
    }

    @Override
    protected boolean mayCollapse(ASTNode s,
            FoldingStructureComputationContext ctx) {
        return super.mayCollapse(s, ctx) || s instanceof CallExpression;
    }

    @Override
    protected boolean initiallyCollapse(ASTNode s) {
        return super.initiallyCollapse(s)
                || (s instanceof CallExpression && fInitCollapseRequires);
    }

    private boolean fInitCollapseRequires;

    @Override
    protected void initializePreferences() {
        super.initializePreferences();
        fInitCollapseRequires = RubyUI
                .getDefault()
                .getPreferenceStore()
                .getBoolean(
                        RubyPreferenceConstants.EDITOR_FOLDING_INIT_REQUIRES);
    }

    /**
     * This folding visitor implementation intentionally does not fold top level
     * classes, but methods and inner classes are folded. This behavior is
     * similar to the JDT.
     */
    protected static class RubyFoldingASTVisitor extends FoldingASTVisitor {

        static class DeclarationContainer {

            final List<Object> children = new ArrayList<Object>();
            final Declaration declaration;
            final boolean foldAlways;

            public DeclarationContainer(Declaration declaration,
                    boolean foldAlways) {
                this.declaration = declaration;
                this.foldAlways = foldAlways;
            }

            void addChild(DeclarationContainer child) {
                children.add(child);
            }

            int countChildren() {
                return children.size();
            }

            @Override
            public String toString() {
                return declaration != null ? declaration.toString() : "(TOP)"; //$NON-NLS-1$
            }
        }

        static class ModuleDeclarationContainer extends DeclarationContainer {

            final List<CallExpression> requires = new ArrayList<CallExpression>();

            public ModuleDeclarationContainer() {
                super(null, false);
            }

            public void addChild(CodeBlock block) {
                children.add(block);
            }
        }

        private final Stack<DeclarationContainer> declarations = new Stack<DeclarationContainer>();

        private DeclarationContainer peekDeclaration() {
            return declarations.peek();
        }

        private DeclarationContainer popDeclaration() {
            return declarations.pop();
        }

        private ModuleDeclarationContainer peekModuleDeclaration() {
            if (declarations.size() == 1) {
                DeclarationContainer container = peekDeclaration();
                if (container instanceof ModuleDeclarationContainer) {
                    return (ModuleDeclarationContainer) container;
                }
            }
            return null;
        }

        protected RubyFoldingASTVisitor(int offset) {
            super(offset);
        }

        @Override
        public boolean visit(ModuleDeclaration s) throws Exception {
            declarations.push(new ModuleDeclarationContainer());
            return visitGeneral(s);
        }

        @Override
        public boolean visit(TypeDeclaration s) throws Exception {
            handleRequireStatements();
            final DeclarationContainer child = new DeclarationContainer(s,
                    false);
            peekDeclaration().addChild(child);
            declarations.push(child);
            return visitGeneral(s);
        }

        @Override
        public boolean endvisit(TypeDeclaration s) throws Exception {
            declarations.pop();
            return super.endvisit(s);
        }

        @Override
        public boolean visit(MethodDeclaration s) throws Exception {
            handleRequireStatements();
            final DeclarationContainer child = new DeclarationContainer(s, true);
            peekDeclaration().addChild(child);
            declarations.push(child);
            return visitGeneral(s);
        }

        @Override
        public boolean endvisit(MethodDeclaration s) throws Exception {
            declarations.pop();
            return super.endvisit(s);
        }

        private void processDeclarations(DeclarationContainer container,
                int level, boolean collapsible) {
            if (container.declaration != null
                    && (collapsible || container.foldAlways)) {
                add(container.declaration);
            }
            final boolean nextCollabsible = collapsible
                    || (level > 0 && container.countChildren() > 1);
            for (Iterator<Object> i = container.children.iterator(); i.hasNext();) {
                final Object child = i.next();
                if (child instanceof DeclarationContainer) {
                    processDeclarations((DeclarationContainer) child,
                            level + 1, nextCollabsible);
                } else if (child instanceof CodeBlock) {
                    add((CodeBlock) child);
                }
            }
        }

        @Override
        public boolean endvisit(ModuleDeclaration s) throws Exception {
            handleRequireStatements();
            final DeclarationContainer container = popDeclaration();
            processDeclarations(container, 0, false);
            return super.endvisit(s);
        }

        @Override
        public boolean visitGeneral(ASTNode node) throws Exception {
            if (declarations.size() == 1) {
                if (node instanceof CallExpression) {
                    final CallExpression call = (CallExpression) node;
                    if (RubyConstants.REQUIRE.equals(call.getName())) {
                        final ModuleDeclarationContainer container = peekModuleDeclaration();
                        if (container != null) {
                            container.requires.add(call);
                        }
                        return false;
                    }
                } else {
                    handleRequireStatements();
                }
            }
            return super.visitGeneral(node);
        }

        private void handleRequireStatements() {
            final ModuleDeclarationContainer container = peekModuleDeclaration();
            if (container != null && !container.requires.isEmpty()) {
                final CallExpression firstRequire = container.requires.get(0);
                final CallExpression lastRequire = container.requires
                        .get(container.requires.size() - 1);
                container.addChild(new CodeBlock(firstRequire, new Region(
                        firstRequire.sourceStart(), lastRequire.sourceEnd()
                                - firstRequire.sourceStart())));
                container.requires.clear();
            }
        }

    }

    @Override
    protected FoldingASTVisitor getFoldingVisitor(int offset) {
        return new RubyFoldingASTVisitor(offset);
    }

    /**
     * @see com.perforce.team.ui.ruby.timelapse.ASTFoldingProvider#getHandle(com.perforce.team.ui.ruby.timelapse.ASTFoldingProvider.ScriptProjectionAnnotation)
     */
    @Override
    protected String getHandle(ScriptProjectionAnnotation annotation) {
        return annotation != null ? RubyNodeModel.getRubyHandle(annotation
                .getElement()) : null;
    }

}
