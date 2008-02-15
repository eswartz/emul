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
 * TCF communication protocol.
 * This module handles registration of command and event handlers.
 * It is called when new messages are received and will dispatch
 * messages to the appropriate handler. It has no knowledge of what transport
 * protocol is used and what services do.
 */

#ifndef D_protocol
#define D_protocol

#include "channel.h"

typedef struct Protocol Protocol;
typedef struct ReplyHandlerInfo ReplyHandlerInfo;


typedef void (*MessageHandler)(char *, Channel *);
typedef void (*EventHandler)(Channel *);

/*
 * Callback fucntion for replies of commands.  If error is non-zero
 * then no data should be read of the input steam.
 */
typedef void (*ReplyHandlerCB)(Channel *, void *client_data, int error);

/*
 * Read and dispatch one protocol message.
 * This function is by channel manager when a message is available for handling.
 */
extern void handle_protocol_message(Protocol *, Channel *);

/*
 * Send "Hello" message to host.
 * This function is typically called from the channel callback function "connecting".
 * All message and event handlers must be registered prior to calling this function.
 */
extern void send_hello_message(Protocol *, Channel *);

extern void event_locator_peer_added(Channel * c);
extern void event_locator_peer_changed(Channel * c);
extern void event_locator_peer_removed(Channel * c);

/*
 * Register command message handler.
 * The handler will be called for each incoming command message on the
 * specified protocl, that belongs to 'service' and has name 'name'.
 */
extern void add_command_handler(Protocol *, const char * service, const char * name, MessageHandler handler);

/*
 * Register event message handler.
 * The handler will be called for each incoming event message on the
 * specified channel, that belongs to 'service' and has name 'name'.
 */
extern void add_event_handler(Channel *, const char * service, const char * name, EventHandler handler);

/*
 * Send command header and register reply handler and associated client data.
 * The handler will be called when the reply message is received.
 */
extern ReplyHandlerInfo * protocol_send_command(Protocol * p, Channel * c,
     const char * service, const char * name, ReplyHandlerCB handler, void * client_data);

/*
 * Cancel pending command.  Returns true if the commands was cancelled
 * before any side effects of the command took place and therefore the
 * reply handler will not be invoked.  Otherwise returns false,
 * indicating that the reply handler will be invoke with the result of
 * the command.
 */
extern int protocol_cancel_command(Protocol * p, ReplyHandlerInfo * rh);

/*
 * Indicates that specified channel is opened.
 */
extern void protocol_channel_opened(Protocol * p, Channel * c);

/*
 * Indicates that specified channel is closed.
 */
extern void protocol_channel_closed(Protocol * p, Channel * c);

/*
 * Create protocol instance
 */
extern Protocol * protocol_alloc(void);

/*
 * Release protocol instance
 */
extern void protocol_free(Protocol *);

#endif
