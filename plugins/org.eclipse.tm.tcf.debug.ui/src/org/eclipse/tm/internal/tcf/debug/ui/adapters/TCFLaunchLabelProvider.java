/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.ui.adapters;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.tm.internal.tcf.debug.model.TCFLaunch;
import org.eclipse.tm.internal.tcf.debug.ui.ImageCache;

class TCFLaunchLabelProvider implements IElementLabelProvider {

    public void update(ILabelUpdate[] updates) {
        for (int i = 0; i < updates.length; i++) {
            ILabelUpdate result = updates[i];
            TCFLaunch launch = (TCFLaunch)result.getElement();
            result.setImageDescriptor(ImageCache.getImageDescriptor(ImageCache.IMG_TCF), 0);
            String status = "";
            if (launch.isConnecting()) status = "Connecting";
            else if (launch.isExited()) status = "Exited";
            else if (launch.isDisconnected()) status = "Disconnected";
            Throwable error = launch.getError();
            if (error != null) {
                String msg = error.getLocalizedMessage();
                if (msg == null || msg.length() == 0) msg = error.getClass().getName();
                else msg = msg.replace('\n', '|');
                status += " - " + msg;
                result.setForeground(new RGB(255, 0, 0), 0);
            }
            if (status.length() > 0) status = " (" + status + ")";
            String name = "?";
            ILaunchConfiguration cfg = launch.getLaunchConfiguration();
            if (cfg != null) name = cfg.getName();
            result.setLabel(name + status, 0);
            result.done();
        }
    }
}
