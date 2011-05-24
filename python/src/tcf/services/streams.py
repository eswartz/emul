# *******************************************************************************
# * Copyright (c) 2011 Wind River Systems, Inc. and others.
# * All rights reserved. This program and the accompanying materials
# * are made available under the terms of the Eclipse Public License v1.0
# * which accompanies this distribution, and is available at
# * http://www.eclipse.org/legal/epl-v10.html
# *
# * Contributors:
# *     Wind River Systems - initial API and implementation
# *******************************************************************************

"""
Streams service is a generic interface to support streaming of data between host and remote agents.

The service supports:
 1. Asynchronous overlapped data streaming: multiple 'read' or 'write' command can be issued at same time, both peers
 can continue data processing concurrently with data transmission.
 2. Multicast: multiple clients can receive data from same stream.
 3. Subscription model: clients are required to expressed interest in particular streams by subscribing for the service.
 4. Flow control: peers can throttle data flow of individual streams by delaying 'read' and 'write' commands.
"""

from tcf import services

NAME = "Streams"

class StreamsService(services.Service):
    def getName(self):
        return NAME

    def subscribe(self, stream_type, listener, done):
        """
        Clients must subscribe for one or more stream types to be able to send or receive stream data.
        Subscribers receive notifications when a stream of given type is created or disposed.
        Subscribers are required to respond with 'read' or 'disconnect' commands as necessary.
        @param stream_type - the stream source type.
        @param listener - client implementation of StreamsListener interface.
        @param done - command result call back object.
        @return - pending command handle.
        """
        raise NotImplementedError("Abstract method")

    def unsubscribe(self, stream_type, listener, done):
        """
        Unsubscribe the client from given stream source type.
        @param stream_type - the stream source type.
        @param listener - client implementation of StreamsListener interface.
        @param done - command result call back object.
        @return - pending command handle.
        """
        raise NotImplementedError("Abstract method")

    def read(self, stream_id, size, done):
        """
        Read data from a stream. If stream buffer is empty, the command will wait until data is available.
        Remote peer will continue to process other commands while 'read' command is pending.
        Client can send more 'read' commands without waiting for the first command to complete.
        Doing that improves communication channel bandwidth utilization.
        Pending 'read' commands will be executed in same order as issued.
        Client can delay sending of 'read' command if it is not ready to receive more data,
        however, delaying for too long can cause stream buffer overflow and lost of data.
        @param stream_id - ID of the stream.
        @param size - max number of bytes to read.
        @param done - command result call back object.
        @return - pending command handle.
        """
        raise NotImplementedError("Abstract method")

    def write(self, stream_id, buf, offset, size, done):
        """
        Write data to a stream. If stream buffer is full, the command will wait until space is available.
        Remote peer will continue to process other commands while 'write' command is pending.
        Client can send more 'write' commands without waiting for the first command to complete.
        Doing that improves communication channel bandwidth utilization.
        Pending 'write' commands will be executed in same order as issued.
        @param stream_id - ID of the stream.
        @param buf - buffer that contains stream data.
        @param offset - byte offset in the buffer.
        @param size - number of bytes to write.
        @param done - command result call back object.
        @return - pending command handle.
        """
        raise NotImplementedError("Abstract method")

    def eos(self, stream_id, done):
        """
        Send End Of Stream marker to a stream. No more writing to the stream is allowed after that.
        @param stream_id - ID of the stream.
        @param done - command result call back object.
        @return - pending command handle.
        """
        raise NotImplementedError("Abstract method")

    def connect(self, stream_id, done):
        """
        Connect client to a stream.
        Some data might be dropped from the stream by the time "connect" command is executed.
        Client should be able to re-sync with stream data if it wants to read from such stream.
        If a client wants to read a stream from the beginning it should use "subscribe" command
        instead of "connect".
        @param stream_id - ID of the stream.
        @param done - command result call back object.
        @return - pending command handle.
        """
        raise NotImplementedError("Abstract method")

    def disconnect(self, stream_id, done):
        """
        Disconnect client from a stream.
        @param stream_id - ID of the stream.
        @param done - command result call back object.
        @return - pending command handle.
        """
        raise NotImplementedError("Abstract method")


class StreamsListener(object):
    """
    Clients can implement StreamsListener interface to be notified
    when a stream is created or disposed. The interface is registered with 'subscribe' command.
    
    When new stream is created, client must decide if it is interested in that particular stream instance.
    If not interested, client should send 'disconnect' command to allow remote peer to free resources and bandwidth.
    If not disconnected, client is required to send 'read' commands as necessary to prevent stream buffer overflow.
    """

    def created(self, stream_type, stream_id, context_id):
        """
        Called when a new stream is created.
        @param stream_type - source type of the stream.
        @param stream_id - ID of the stream.
        @param context_id - a context ID that is associated with the stream, or null.
        Exact meaning of the context ID depends on stream type.
        Stream types and context IDs are defined by services that use Streams service to transmit data.
        """
        pass

    def disposed(self, stream_type, stream_id):
        """
        Called when a stream is disposed.
        @param stream_type - source type of the stream.
        @param stream_id - ID of the stream.
        """
        pass

class DoneSubscribe(object):
    """
    Call back interface for 'subscribe' command.
    """
    def doneSubscribe(self, token, error):
        pass

class DoneUnsubscribe(object):
    """
    Call back interface for 'unsubscribe' command.
    """
    def doneUnsubscribe(self, token, error):
        pass

class DoneRead(object):
    """
    Call back interface for 'read' command.
    """
    def doneRead(self, token, error, lost_size, data, eos):
        """
        Called when 'read' command is done.
        @param token - command handle.
        @param error - error object or null.
        @param lost_size - number of bytes that were lost because of buffer overflow.
        'lost_size' -1 means unknown number of bytes were lost.
        if both 'lost_size' and 'data.length' are non-zero then lost bytes are considered
        located right before read bytes.
        @param data - bytes read from the stream.
        @param eos - true if end of stream was reached.
        """
        pass

class DoneWrite(object):
    """
    Call back interface for 'write' command.
    """
    def doneWrite(self, token, error):
        """
        Called when 'write' command is done.
        @param token - command handle.
        @param error - error object or null.
        """
        pass

class DoneEOS(object):
    """
    Call back interface for 'eos' command.
    """
    def doneEOS(self, token, error):
        """
        Called when 'eos' command is done.
        @param token - command handle.
        @param error - error object or null.
        """
        pass

class DoneConnect(object):
    """
    Call back interface for 'connect' command.
    """
    def doneConnect(self, token, error):
        """
        Called when 'connect' command is done.
        @param token - command handle.
        @param error - error object or null.
        """
        pass

class DoneDisconnect(object):
    """
    Call back interface for 'disconnect' command.
    """
    def doneDisconnect(self, token, error):
        """
        Called when 'disconnect' command is done.
        @param token - command handle.
        @param error - error object or null.
        """
        pass
