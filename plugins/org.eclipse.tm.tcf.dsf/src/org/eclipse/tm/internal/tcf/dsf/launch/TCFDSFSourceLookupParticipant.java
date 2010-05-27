/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.dsf.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.tm.internal.tcf.debug.launch.TCFSourceLookupParticipant;
import org.eclipse.tm.internal.tcf.debug.model.TCFSourceRef;
import org.eclipse.tm.internal.tcf.dsf.services.TCFDSFStack.TCFFrameDMC;
import org.eclipse.tm.tcf.protocol.Protocol;

/**
 * The TCF source lookup participant knows how to translate a TCFFrameData
 * into a source file name
 */
public class TCFDSFSourceLookupParticipant extends TCFSourceLookupParticipant {

    @Override
    public String getSourceName(final Object object) throws CoreException {
        if (object instanceof TCFFrameDMC) {
            final Object[] res = new Object[1];
            synchronized (res) {
                Protocol.invokeLater(new Runnable() {
                    public void run() {
                        TCFFrameDMC dmc = (TCFFrameDMC)object;
                        if (!dmc.context_cache.validate(this)) return;
                        if (!dmc.source_cache.validate(this)) return;
                        synchronized (res) {
                            TCFSourceRef ref = dmc.source_cache.getData();
                            if (ref != null) res[0] = ref.area;
                            res.notify();
                        }
                    }
                });
                try {
                    res.wait();
                }
                catch (InterruptedException e) {
                }
            }
            return super.getSourceName(res[0]);
        }
        return super.getSourceName(object);
    }
}
