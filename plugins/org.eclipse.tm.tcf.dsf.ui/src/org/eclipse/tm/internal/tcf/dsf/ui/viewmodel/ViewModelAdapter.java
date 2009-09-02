/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.dsf.ui.viewmodel;

import org.eclipse.dd.dsf.concurrent.ThreadSafe;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.expression.ExpressionVMProvider;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.modules.ModulesVMProvider;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.variable.VariableVMProvider;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.viewmodel.IVMProvider;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMAdapter;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.tm.internal.tcf.dsf.launch.TCFDSFLaunch;


@ThreadSafe
@SuppressWarnings("restriction")
public class ViewModelAdapter extends AbstractDMVMAdapter {

    private final TCFDSFLaunch launch;

    public ViewModelAdapter(DsfSession session, TCFDSFLaunch launch) {
        super(session);
        this.launch = launch;
        getSession().registerModelAdapter(IColumnPresentationFactory.class, this);
    }

    @Override
    public void dispose() {
        getSession().unregisterModelAdapter(IColumnPresentationFactory.class);
        super.dispose();
    }

    @Override
    protected IVMProvider createViewModelProvider(IPresentationContext context) {
        if (IDebugUIConstants.ID_DEBUG_VIEW.equals(context.getId()) ) {
            return new LaunchVMProvider(this, context, getSession(), launch);
        }
        if (IDebugUIConstants.ID_VARIABLE_VIEW.equals(context.getId()) ) {
            return new VariableVMProvider(this, context, getSession());
        }
        if (IDebugUIConstants.ID_REGISTER_VIEW.equals(context.getId()) ) {
            return new RegisterVMProvider(this, context, getSession());
        }
        if (IDebugUIConstants.ID_EXPRESSION_VIEW.equals(context.getId()) ) {
            return new ExpressionVMProvider(this, context, getSession());
        }
        if (IDebugUIConstants.ID_MODULE_VIEW.equals(context.getId()) ) {
            return new ModulesVMProvider(this, context, getSession());
        }
        return null;
    }
}
