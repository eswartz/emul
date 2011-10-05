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
import org.eclipse.cdt.debug.ui.IPinProvider;
import org.eclipse.cdt.ui.text.c.hover.ICEditorTextHover;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.tm.internal.tcf.cdt.ui.breakpoints.TCFWatchpointTarget;
import org.eclipse.tm.internal.tcf.cdt.ui.commands.TCFPinViewCommand;
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
        IPinProvider.class,
        ICWatchpointTarget.class,
        ISourceNotFoundPresentation.class
    };

    private static final TCFSourceNotFoundPresentation fgSourceNotFoundPresentation = new TCFSourceNotFoundPresentation();

    public Object getAdapter(Object obj, Class type) {
        if (obj instanceof TCFNode) {
            final TCFNode node = (TCFNode)obj;
            TCFModel model = node.getModel();
            if (IDisassemblyBackend.class == type) {
                TCFDisassemblyBackend backend = new TCFDisassemblyBackend();
                if (backend.supportsDebugContext((TCFNode)obj)) return backend;
            }
            else if (ISteppingModeTarget.class == type) {
                ISteppingModeTarget target = (ISteppingModeTarget)model.getAdapter(type, node);
                if (target == null) model.setAdapter(type, target = new TCFSteppingModeTarget(model));
                return target;
            }
            else if (ISuspendResume.class == type) {
                TCFNodeExecContext exec = null;
                if (node instanceof TCFNodeExecContext) {
                    exec = (TCFNodeExecContext)node;
                }
                else if (node instanceof TCFNodeStackFrame) {
                    exec = (TCFNodeExecContext)node.getParent();
                }
                if (exec != null) {
                    return new TCFSuspendResumeAdapter(exec);
                }
            }
            else if (ICEditorTextHover.class == type) {
                ICEditorTextHover hover = (ICEditorTextHover)model.getAdapter(type, node);
                if (hover == null) model.setAdapter(type, hover = new TCFDebugTextHover());
                return hover;
            }
            else if (IReverseToggleHandler.class == type) {
                IReverseToggleHandler handler = (IReverseToggleHandler)model.getAdapter(type, node);
                if (handler == null) model.setAdapter(type, handler = new TCFReverseToggleCommand());
                return handler;
            }
            else if (IReverseStepIntoHandler.class == type) {
                IReverseStepIntoHandler handler = (IReverseStepIntoHandler)model.getAdapter(type, node);
                if (handler == null) model.setAdapter(type, handler = new TCFReverseStepIntoCommand(model));
                return handler;
            }
            else if (IReverseStepOverHandler.class == type) {
                IReverseStepOverHandler handler = (IReverseStepOverHandler)model.getAdapter(type, node);
                if (handler == null) model.setAdapter(type, handler = new TCFReverseStepOverCommand(model));
                return handler;
            }
            else if (IUncallHandler.class == type) {
                IUncallHandler handler = (IUncallHandler)model.getAdapter(type, node);
                if (handler == null) model.setAdapter(type, handler = new TCFReverseStepReturnCommand(model));
                return handler;
            }
            else if (IReverseResumeHandler.class == type) {
                IReverseResumeHandler handler = (IReverseResumeHandler)model.getAdapter(type, node);
                if (handler == null) model.setAdapter(type, handler = new TCFReverseResumeCommand(model));
                return handler;
            }
            else if (IPinProvider.class == type) {
                IPinProvider handler = (IPinProvider)model.getAdapter(type, node);
                if (handler == null) model.setAdapter(type, handler = new TCFPinViewCommand(model));
                return handler;
            }
            else if (ICWatchpointTarget.class == type) {
                if (node instanceof TCFNodeExpression) return new TCFWatchpointTarget((TCFNodeExpression)node);
            }
            else if (ISourceNotFoundPresentation.class == type) {
                return fgSourceNotFoundPresentation;
            }
        }
        return null;
    }

    public Class[] getAdapterList() {
        return CLASSES;
    }
}
