/*******************************************************************************
 * Copyright (c) 2010 Intel Corporation. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Intel Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.tm.internal.tcf.rse;

import org.eclipse.rse.services.files.RemoteFileException;
import org.eclipse.tm.tcf.protocol.IChannel;

public interface ITCFSessionProvider {
    public static final int ERROR_CODE = 100; // filed error code
    public static final int SUCCESS_CODE = 150; // login pass code
    public static final int CONNECT_CLOSED = 200; // code for end of login attempts
    public static final int TCP_CONNECT_TIMEOUT = 10; //seconds - TODO: Make configurable    

    public IChannel getChannel();
    public String getSessionUserId();
    public String getSessionPassword();
    public String getSessionHostName();
    public boolean isSubscribed();
    public void subscribe() throws RemoteFileException;
}
