/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
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

#include <framework/channel.h>

/* Time in seconds to keep remote peer data */
#define PEER_DATA_RETENTION_PERIOD 60

/* Peer data polling period in seconds */
#define PEER_DATA_REFRESH_PERIOD (PEER_DATA_RETENTION_PERIOD / 4)

typedef struct Protocol Protocol;
typedef struct ReplyHandlerInfo ReplyHandlerInfo;

typedef void (*ProtocolMessageHandler)(Channel *, char **, int);
typedef void (*ProtocolCommandHandler)(char *, Channel *);
typedef void (*ProtocolEventHandler)(Channel *);

typedef void (*ProtocolMessageHandler2)(Channel *, char **, int, void * client_data);
typedef void (*ProtocolCommandHandler2)(char *, Channel *, void * client_data);
typedef void (*ProtocolEventHandler2)(Channel *, void * client_data);

/*
 * Callback fucntion for replies of commands.  If error is non-zero
 * then no data should be read of the input steam.
 */
/* TODO: need additional argument in ReplyHandlerCB to distinguish R and P responses */
typedef void (*ReplyHandlerCB)(Channel *, void * client_data, int error);

/*
 * Read and dispatch one protocol message.
 * This function is by channel manager when a message is available for handling.
 */
extern void handle_protocol_message(Channel *);

/*
 * Send "Hello" message to host.
 * This function is typically called from the channel callback function "connecting".
 * All message and event handlers must be registered prior to calling this function.
 */
extern void send_hello_message(Channel *);

/*
 * Lookup or allocate service handle for protocol or channel
 */
typedef struct ServiceInfo ServiceInfo;
extern ServiceInfo * protocol_get_service(void * owner, const char * name);

/*
 * Register command message handler.
 * The handler will be called for each incoming command message on the
 * specified protocl, that belongs to 'service' and has name 'name'.
 */
extern void add_command_handler(Protocol *, const char * service, const char * name, ProtocolCommandHandler handler);
extern void add_command_handler2(Protocol *, const char * service, const char * name, ProtocolCommandHandler2 handler, void * client_data);

/*
 * Register event message handler.
 * The handler will be called for each incoming event message on the
 * specified channel, that belongs to 'service' and has name 'name'.
 */
extern void add_event_handler(Channel *, const char * service, const char * name, ProtocolEventHandler handler);
extern void add_event_handler2(Channel *, const char * service, const char * name, ProtocolEventHandler2 handler, void * client_data);

/*
 * Set protocol default message handler.
 * The handler will be called for incoming messages that don't have a specific handler
 * assigned by add_command_handler() or add_event_handler()
 */
extern void set_default_message_handler(Protocol *, ProtocolMessageHandler handler);
extern void set_default_message_handler2(Protocol *, ProtocolMessageHandler2 handler, void * client_data);

/*
 * Send command header and register reply handler and associated client data.
 * The handler will be called when the reply message is received.
 */
extern ReplyHandlerInfo * protocol_send_command(Channel * c,
     const char * service, const char * name, ReplyHandlerCB handler, void * client_data);

/*
 * Cancel pending command.  Returns true if the commands was cancelled
 * before any side effects of the command took place and therefore the
 * reply handler will not be invoked.  Otherwise returns false,
 * indicating that the reply handler will be invoked with the result of
 * the command.
 */
extern int protocol_cancel_command(ReplyHandlerInfo * rh);

/*
 * Send redirect command.
 */
extern ReplyHandlerInfo * send_redirect_command(Channel * c, const char * peerId, ReplyHandlerCB handler, void * client_data);

/*
 * Create protocol instance
 */
extern Protocol * protocol_alloc(void);

/*
 * Record new reference to protocol
 */
extern void protocol_reference(Protocol *);

/*
 * Release protocol reference, protocol if freed when the last
 * reference is released
 */
extern void protocol_release(Protocol *);

#endif /* D_protocol */
