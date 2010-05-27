/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
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
 * TCF Streams - generic streams service.
 */

#ifndef D_streamsservice
#define D_streamsservice

#include <framework/protocol.h>

typedef struct VirtualStream VirtualStream;

typedef void VirtualStreamCallBack(VirtualStream * /* stream */, int /* event */, void * /* args */);

#define VS_EVENT_SPACE_AVAILABLE    1
#define VS_EVENT_DATA_AVAILABLE     2

#define VS_ENABLE_REMOTE_READ       0x01
#define VS_ENABLE_REMOTE_WRITE      0x02

extern void virtual_stream_create(const char * type, const char * context_id, size_t buf_len, unsigned access,
    VirtualStreamCallBack * callback, void * callback_args, VirtualStream ** res);
extern void virtual_stream_delete(VirtualStream * stream);

extern void virtual_stream_get_id(VirtualStream * stream, char * id_buf, size_t buf_size);
extern VirtualStream * virtual_stream_find(char * id);

extern int virtual_stream_add_data(VirtualStream * stream, char * buf, size_t buf_size, size_t * data_size, int eos);
extern int virtual_stream_get_data(VirtualStream * stream, char * buf, size_t buf_size, size_t * data_size, int * eos);
extern int virtual_stream_is_empty(VirtualStream * stream);

/*
 * Initialize streams service.
 */
extern void ini_streams_service(Protocol * proto);


#endif /* D_streamsservice */
