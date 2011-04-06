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

import cStringIO, json, binascii, types, exceptions

# channel states
STATE_OPENING = 0
STATE_OPEN = 1
STATE_CLOSED = 2

class TraceListener(object):
    def onMessageReceived(self, type, token, service, name, data):
        pass

    def onMessageSent(self, type, token, service, name, data):
        pass

    def onChannelClosed(self, error):
        pass

def Proxy(object):
    def onCommand(self, token, service, name, data):
        pass

    def onEvent(self, service, name, data):
        pass

    def onChannelClosed(self, error):
        pass

_token_cnt = 0
class Token(object):
    def __init__(self, id=None, listener=None):
        if id is None:
            global _token_cnt
            id = str(_token_cnt)
            _token_cnt += 1
        else:
            id = str(id)
        self.id = id
        self.listener = listener
    def getID(self):
        return self.id
    def getListener(self):
        return self.listener
    def cancel(self):
        return False

class ChannelListener(object):
    """
    Channel listener interface.
    """

    def onChannelOpened(self):
        """
        Called when a channel is opened or redirected.
        """
        pass

    def onChannelClosed(self, error):
        """
        Called when channel closed. If it is closed because of an error,
        'error' parameter will describe the error. 'error' is null if channel
        is closed normally by calling Channel.close().
        @param error - channel exception or null
        """
        pass

    def congestionLevel(self, level):
        """
        Notifies listeners about channel out-bound traffic congestion level changes.
        When level > 0 client should delay sending more messages.
        @param level - current congestion level
        """
        pass

class EventListener(object):
    """
    A generic interface for service event listener.
    Services usually define a service specific event listener interface,
    which is implemented using this generic listener.
    Clients should user service specific listener interface,
    unless no such interface is defined.
    """
    svc_name = "<unknown>"
    def event(self, name, data):
        """
        Called when service event message is received
        @param name - event name
        @param data - event arguments encoded as bytearray
        """
        pass

class CommandServer(object):
    """
    Command server interface.
    This interface is to be implemented by service providers.
    """
    def command(self, token, name, data):
        """
        Called every time a command is received from remote peer.
        @param token - command handle
        @param name - command name
        @param data - command arguments encoded into array of bytes
        """
        pass

def toJSONSequence(args):
    if args is None:
        return None
    buf = cStringIO.StringIO()
    for arg in args:
        json.dump(arg, buf, separators=(',', ':'), cls=TCFJSONEncoder)
        buf.write('\0')
    return buf.getvalue()

def fromJSONSequence(bytes):
    if bytes[-1] == 0:
        del bytes[-1]
    str = bytes.decode("UTF-8")
    parts = str.split('\0')
    objects = []
    for part in parts:
        if part:
            objects.append(json.loads(part))
        else:
            objects.append(None)
    return objects

def dumpJSONObject(object, buf):
        json.dump(object, buf, separators=(',', ':'), cls=TCFJSONEncoder)
    
def toByteArray(data):
    if data is None: return None
    t = type(data)
    if t is bytearray: return data
    elif t is str:
        return binascii.a2b_base64(data)
    elif t is unicode:
        return binascii.a2b_base64(str(data))
    raise exceptions.TypeError(str(t))

class TCFJSONEncoder(json.JSONEncoder):
    def default(self, o):
        if isinstance(o, bytearray):
            return binascii.b2a_base64(o)[:-1]
        elif hasattr(o, '__iter__'):
            return tuple(o)
        else:
            json.JSONEncoder.default(self, o)
