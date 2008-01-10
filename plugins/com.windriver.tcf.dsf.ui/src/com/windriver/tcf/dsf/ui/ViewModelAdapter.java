/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package com.windriver.tcf.dsf.ui;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.dd.dsf.concurrent.ThreadSafe;
import org.eclipse.dd.dsf.debug.ui.viewmodel.expression.ExpressionVMProvider;
import org.eclipse.dd.dsf.debug.ui.viewmodel.register.RegisterVMProvider;
import org.eclipse.dd.dsf.debug.ui.viewmodel.variable.VariableVMProvider;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.viewmodel.dm.AbstractDMVMAdapter;
import org.eclipse.dd.dsf.ui.viewmodel.dm.AbstractDMVMProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.Bundle;

import com.windriver.tcf.api.protocol.Protocol;
import com.windriver.tcf.dsf.core.launch.TCFDSFLaunch;

@ThreadSafe
@SuppressWarnings("restriction")
public class ViewModelAdapter extends AbstractDMVMAdapter implements IElementLabelProvider {
    
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
    protected AbstractDMVMProvider createViewModelProvider(IPresentationContext context) {
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
        return null;
    }

    private static final Map<String,ImageDescriptor> image_cache =
        new HashMap<String,ImageDescriptor>();

    private static ImageDescriptor getImageDescriptor(String name) {
        if (name == null) return null;
        ImageDescriptor descriptor = image_cache.get(name);
        if (descriptor == null) {
            descriptor = ImageDescriptor.getMissingImageDescriptor();
            Bundle bundle = Platform.getBundle("org.eclipse.debug.ui");
            if (bundle != null){
                URL url = FileLocator.find(bundle, new Path(name), null);
                descriptor = ImageDescriptor.createFromURL(url);
            }
            image_cache.put(name, descriptor);
        }
        return descriptor;
    }

    public void update(final ILabelUpdate[] updates) {
        Protocol.invokeLater(new Runnable() {
            public void run() {
                for (ILabelUpdate u : updates) {
                    Object o = u.getElement();
                    if (o instanceof TCFDSFLaunch) {
                        u.setImageDescriptor(getImageDescriptor("icons/full/obj16/ldebug_obj.gif"), 0);
                        TCFDSFLaunch launch = (TCFDSFLaunch)o;
                        String status = "";
                        if (launch.isConnecting()) status = "Connecting";
                        else if (launch.isDisconnected()) status = "Disconnected";
                        else if (launch.isTerminated()) status = "Terminated";
                        Throwable error = launch.getError();
                        if (error != null) {
                            status += " - " + error;
                            u.setForeground(new RGB(255, 0, 0), 0);
                        }
                        if (status.length() > 0) status = " (" + status + ")";
                        u.setLabel(launch.getLaunchConfiguration().getName() + status, 0);
                    }
                    else {
                        u.setForeground(new RGB(255, 0, 0), 0);
                        u.setLabel("Invalid object: " + o.getClass(), 0);
                    }
                }
                Display.getDefault().asyncExec(new Runnable() {
                    public void run() {
                        for (ILabelUpdate u : updates) u.done();
                    }
                });
            }
        });
    }    
}
