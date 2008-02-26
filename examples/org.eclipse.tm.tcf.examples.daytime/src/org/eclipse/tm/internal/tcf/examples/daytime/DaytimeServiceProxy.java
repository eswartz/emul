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

package org.eclipse.tm.internal.tcf.examples.daytime;

import org.eclipse.tm.tcf.core.Command;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;


public class DaytimeServiceProxy implements IDaytimeService {

        private final IChannel channel;

        DaytimeServiceProxy(IChannel channel) {
                this.channel = channel;
        }

        /**
         * Return service name, as it appears on the wire - a TCF name of the service. 
         */
        public String getName() {
                return NAME;
        }

        /**
         * The method translates arguments to JSON string and sends the command message
         * to remote server. When response arrives, it is translated from JSON to
         * Java object, which are used to call call-back object.
         * 
         * The translation (marshaling) is done by using utility class Command.
         */
        public IToken getTimeOfDay(String tz, final DoneGetTimeOfDay done) {
                return new Command(channel, this, "getTimeOfDay", new Object[]{ tz }) {
                        @Override
                        public void done(Exception error, Object[] args) {
                                String str = null;
                                if (error == null) {
                                        assert args.length == 3;
                                        error = toError(args[0], args[1]);
                                        str = (String)args[2];
                                }
                                done.doneGetTimeOfDay(token, error, str);
                        }
                }.token;
        }

        static {
                /*
                 * Make Daytime Service proxy available to all potential clients by creating
                 * the proxy object every time a TCF communication channel is opened.
                 * Note: extension point "org.eclipse.tm.tcf.startup" is used to load this class
                 * at TCF startup time, so proxy factory is properly activated even if nobody
                 * import directly from this plugin.     
                 */
                Protocol.addChannelOpenListener(new Protocol.ChannelOpenListener() {

                        public void onChannelOpen(IChannel channel) {
                                // Check if remote server provides Daytime service
                                if (channel.getRemoteService(IDaytimeService.NAME) == null) return;
                                // Create service proxy
                                channel.setServiceProxy(IDaytimeService.class, new DaytimeServiceProxy(channel));
                        }
                });
        }
}
