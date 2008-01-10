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

/*
 * Implements input and output stream over TCP/IP transport and UDP based auto discovery.
 */

#ifndef D_channel
#define D_channel

#include "streams.h"

extern OutputStream broadcast_stream;

/*
 * Temporary suspend handling of incoming messages on all channels
 */
extern void channels_suspend(void);

/*
 * Returns 1 if handling of incoming messages is suspended.
 */
extern int are_channels_suspended(void);

/*
 * Resume handling of messages on all channels.
 */
extern void channels_resume(void);
 
/*
 * Return number of pending input messages on all channels.
 */
extern int channels_get_message_count(void);

/*
 * Lock OutputStream to prevent it from being deleted.
 * A stream must be locked to keep a referense to it across event dispatch cycles.
 */
extern void stream_lock(OutputStream * out);
extern void stream_unlock(OutputStream * out);

/*
 * Check if stream is closed. Onlu make sense when the stream is locked.
 * Unlocked stream is deleted when closed.
 */
extern int is_stream_closed(OutputStream * out);

/*
 * Register channel close callback.
 * Service implementation can use the callback to deallocate resources
 * after a client disconnects.
 */
typedef void (*ChannelCloseListener)(InputStream *, OutputStream *);
extern void add_channel_close_listener(ChannelCloseListener listener);
 
/*
 * Initialize channel manager.
 * 'port' - listenig socket port.
 */
extern void ini_channel_manager(int port);

#endif
