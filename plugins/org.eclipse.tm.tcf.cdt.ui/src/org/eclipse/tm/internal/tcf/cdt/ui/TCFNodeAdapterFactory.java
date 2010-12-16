/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.cdt.ui;

import org.eclipse.cdt.debug.core.model.ISteppingModeTarget;
import org.eclipse.cdt.ui.text.c.hover.ICEditorTextHover;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.tm.internal.tcf.cdt.ui.hover.TCFDebugTextHover;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFModel;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNode;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeExecContext;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeStackFrame;
import org.eclipse.tm.tcf.util.TCFTask;

@SuppressWarnings("rawtypes")
public class TCFNodeAdapterFactory implements IAdapterFactory {

    private static final Class<?>[] CLASSES = { 
        ISteppingModeTarget.class,
        ISuspendResume.class,
        ICEditorTextHover.class
    };

    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (adaptableObject instanceof TCFNode) {
            final TCFNode node = (TCFNode) adaptableObject;
            TCFModel model = node.getModel();
            if (ISteppingModeTarget.class == adapterType) {
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
            }
        }
        return null;
    }

    public Class[] getAdapterList() {
        return CLASSES;
    }

}
