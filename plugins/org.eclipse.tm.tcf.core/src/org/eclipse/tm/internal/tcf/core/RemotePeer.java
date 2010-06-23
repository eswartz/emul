/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.core;

import java.util.Map;

import org.eclipse.tm.tcf.core.AbstractPeer;

/**
 * RemotePeer objects represent TCF agents that Locator service discovered on local network.
 * This includes both local host agents and remote host agents.
 * Note that "remote peer" means any peer accessible over network,
 * it does not imply the agent is running on a "remote host".
 * If an agent binds multiple network interfaces or multiple ports, it can be represented by
 * multiple RemotePeer objects - one per each network address/port combination.
 * RemotePeer objects life cycle is managed by Locator service.
 */
public class RemotePeer extends AbstractPeer {

    private long last_update_time;

    public RemotePeer(Map<String,String> attrs) {
        super(attrs);
        last_update_time = System.currentTimeMillis();
    }

    @Override
    public void updateAttributes(Map<String,String> attrs) {
        super.updateAttributes(attrs);
        last_update_time = System.currentTimeMillis();
    }

    public long getLastUpdateTime() {
        return last_update_time;
    }
}
