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
package org.eclipse.tm.tcf.protocol;

/**
 * IToken is created by the framework for each command sent to a remote peer.
 * It is used to match results to commands and to cancel pending commands.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IToken {

    /**
     * Try to cancel a command associated with given token. A command can be
     * canceled by this method only if it was not transmitted yet to remote peer
     * for execution. Successfully canceled command does not produce any result
     * messages.
     * 
     * @return true if successful.
     */
    boolean cancel();

}
