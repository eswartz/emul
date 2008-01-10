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

#include "streams.h"

typedef void (*MessageHandler)(char * /* Token or NULL */, InputStream *, OutputStream *);

/*
 * Current communication channels congestion level.
 * Can be used by message handlers to manage congestion.
 */
extern int congestion_level;

/*
 * Read and dispatch one protocol message.
 * This function is by channel manager when a message is available for handling.
 */
extern void handle_protocol_message(InputStream * inp, OutputStream * out);

/*
 * Send "Hello" message to host.
 * This function is called by channel manager when new channel is opened.
 */
extern void send_hello_message(OutputStream * out);

/*
 * Register command message handler.
 * The handler will be called for each incoming command message,
 * that belongs to 'service' and has name 'name'.
 */
extern void add_command_handler(const char * service, const char * name, MessageHandler handler);

/*
 * Register event message handler.
 * The handler will be called for each incoming event message,
 * that belongs to 'service' and has name 'name'.
 */
extern void add_event_handler(const char * service, const char * name, MessageHandler handler);

extern void ini_protocol(void);

#endif
