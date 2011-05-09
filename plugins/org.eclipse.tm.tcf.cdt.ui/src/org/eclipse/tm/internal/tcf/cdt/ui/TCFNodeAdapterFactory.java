/*******************************************************************************
 * Copyright (c) 2010, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.cdt.ui;

import org.eclipse.cdt.debug.core.model.IReverseResumeHandler;
import org.eclipse.cdt.debug.core.model.IReverseStepIntoHandler;
import org.eclipse.cdt.debug.core.model.IReverseStepOverHandler;
import org.eclipse.cdt.debug.core.model.IReverseToggleHandler;
import org.eclipse.cdt.debug.core.model.ISteppingModeTarget;
import org.eclipse.cdt.debug.core.model.IUncallHandler;
import org.eclipse.cdt.debug.internal.core.ICWatchpointTarget;
import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyBackend;
import org.eclipse.cdt.ui.text.c.hover.ICEditorTextHover;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.tm.internal.tcf.cdt.ui.breakpoints.TCFWatchpointTarget;
import org.eclipse.tm.internal.tcf.cdt.ui.commands.TCFReverseResumeCommand;
import org.eclipse.tm.internal.tcf.cdt.ui.commands.TCFReverseStepIntoCommand;
import org.eclipse.tm.internal.tcf.cdt.ui.commands.TCFReverseStepOverCommand;
import org.eclipse.tm.internal.tcf.cdt.ui.commands.TCFReverseStepReturnCommand;
import org.eclipse.tm.internal.tcf.cdt.ui.commands.TCFReverseToggleCommand;
import org.eclipse.tm.internal.tcf.cdt.ui.disassembly.TCFDisassemblyBackend;
import org.eclipse.tm.internal.tcf.cdt.ui.hover.TCFDebugTextHover;
import org.eclipse.tm.internal.tcf.cdt.ui.sourcelookup.TCFSourceNotFoundPresentation;
import org.eclipse.tm.internal.tcf.debug.ui.model.ISourceNotFoundPresentation;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFModel;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNode;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeExecContext;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeExpression;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeStackFrame;
import org.eclipse.tm.tcf.util.TCFTask;

@SuppressWarnings({ "rawtypes", "restriction" })
public class TCFNodeAdapterFactory implements IAdapterFactory {

    private static final Class<?>[] CLASSES = {
        IDisassemblyBackend.class,
        ISteppingModeTarget.class,
        ISuspendResume.class,
        ICEditorTextHover.class,
        IReverseToggleHandler.class,
        IReverseStepIntoHandler.class,
        IReverseStepOverHandler.class,
        IReverseResumeHandler.class,
        IUncallHandler.class,
        ICWatchpointTarget.class
    };
    
    private static final TCFSourceNotFoundPresentation fgSourceNotFoundPresentation = new TCFSourceNotFoundPresentation();

    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (adaptableObject instanceof TCFNode) {
            final TCFNode node = (TCFNode) adaptableObject;
            TCFModel model = node.getModel();
            if (IDisassemblyBackend.class == adapterType) {
                TCFDisassemblyBackend backend = new TCFDisassemblyBackend();
                if (backend.supportsDebugContext((TCFNode) adaptableObject)) {
                    return backend;
                }
            } else if (ISteppingModeTarget.class == adapterType) {
                ISteppingModeTarget target = (ISteppingModeTarget) model.getAdapter(adapterType, node);
                if (target == null) {
                    model.setAdapter(adapterType, target = new TCFSteppingModeTarget(model));
                }
                return target;
            } else if (ISuspendResume.class == adapterType) {
                TCFNodeExecContext execCtx = null;
                if (node instanceof TCFNodeExecContext) {
                    execCtx = (TCFNodeExecContext) node;
                } else if (node instanceof TCFNodeStackFrame) {
                    execCtx = new TCFTask<TCFNodeExecContext>() {
                        public void run() {
                            if (node.getParent() instanceof TCFNodeExecContext) {
                                done((TCFNodeExecContext) node.getParent());
                            } else {
                                done(null);
                            }
                        }
                    }.getE();
                }
                if (execCtx != null) {
                    return new TCFSuspendResumeAdapter(execCtx);
                }
            } else if (ICEditorTextHover.class == adapterType) {
                ICEditorTextHover hover = (ICEditorTextHover) model.getAdapter(adapterType, node);
                if (hover == null) {
                    model.setAdapter(adapterType, hover = new TCFDebugTextHover());
                }
                return hover;
            } else if (IReverseToggleHandler.class == adapterType) {
                IReverseToggleHandler handler = (IReverseToggleHandler) model.getAdapter(adapterType, node);
                if (handler == null) {
                    model.setAdapter(adapterType, handler = new TCFReverseToggleCommand());
                }
                return handler;
            } else if (IReverseStepIntoHandler.class == adapterType) {
                IReverseStepIntoHandler handler = (IReverseStepIntoHandler) model.getAdapter(adapterType, node);
                if (handler == null) {
                    model.setAdapter(adapterType, handler = new TCFReverseStepIntoCommand(model));
                }
                return handler;
            } else if (IReverseStepOverHandler.class == adapterType) {
                IReverseStepOverHandler handler = (IReverseStepOverHandler) model.getAdapter(adapterType, node);
                if (handler == null) {
                    model.setAdapter(adapterType, handler = new TCFReverseStepOverCommand(model));
                }
                return handler;
            } else if (IUncallHandler.class == adapterType) {
                IUncallHandler handler = (IUncallHandler) model.getAdapter(adapterType, node);
                if (handler == null) {
                    model.setAdapter(adapterType, handler = new TCFReverseStepReturnCommand(model));
                }
                return handler;
            } else if (IReverseResumeHandler.class == adapterType) {
                IReverseResumeHandler handler = (IReverseResumeHandler) model.getAdapter(adapterType, node);
                if (handler == null) {
                    model.setAdapter(adapterType, handler = new TCFReverseResumeCommand(model));
                }
                return handler;
            } else if (ICWatchpointTarget.class == adapterType) {
                if (node instanceof TCFNodeExpression) {
                    return new TCFWatchpointTarget((TCFNodeExpression) node);
                }
            } else if (ISourceNotFoundPresentation.class == adapterType) {
                return fgSourceNotFoundPresentation;
            }
        }
        return null;
    }

    public Class[] getAdapterList() {
        return CLASSES;
    }

}
