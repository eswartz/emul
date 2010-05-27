/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Uwe Stieber (Wind River) - [271227] Fix compiler warnings in org.eclipse.tm.tcf.rse
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.rse;

import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.AbstractConnectorServiceManager;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;

public class TCFConnectorServiceManager extends AbstractConnectorServiceManager {

    public static int TCF_PORT = 1534;

    private static final TCFConnectorServiceManager manager =
        new TCFConnectorServiceManager();

    @Override
    public IConnectorService createConnectorService(IHost host) {
        return new TCFConnectorService(host, TCF_PORT);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class getSubSystemCommonInterface(ISubSystem subsystem) {
        return ITCFSubSystem.class;
    }

    @Override
    public boolean sharesSystem(ISubSystem otherSubSystem) {
        return otherSubSystem instanceof ITCFSubSystem;
    }

    public static TCFConnectorServiceManager getInstance() {
        return manager;
    }
}
