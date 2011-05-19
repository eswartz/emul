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

import socket, types
from tcf import protocol
from StreamChannel import StreamChannel

class ChannelTCP(StreamChannel):
    "ChannelTCP is a channel implementation that works on top of TCP sockets as a transport."

    def __init__(self, remote_peer, host, port):
        super(ChannelTCP, self).__init__(remote_peer)
        self.closed = False
        self.started = False
        channel = self
        class CreateSocket(object):
            def __call__(self):
                sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                sock.connect((host, port))
                sock.setsockopt(socket.SOL_TCP, socket.TCP_NODELAY, 1)
                sock.setsockopt(socket.SOL_SOCKET, socket.SO_KEEPALIVE, 1)
                channel.socket = sock
                channel._onSocketConnected(None)
        protocol.invokeLater(CreateSocket())

    def _onSocketConnected(self, x):
        if x:
            self.terminate(x)
            self.closed = True
        if self.closed:
            try:
                if self.socket:
                    self.socket.close()
            except socket.error as y:
                protocol.log("Cannot close socket", y)
        else:
            self.started = True
            self.start()

    def get(self):
        if self.closed: return -1
        try:
            return ord(self.socket.recv(1))
        except socket.error as x:
            if self.closed: return -1
            raise x

    def getBuf(self, buf):
        if self.closed: return -1
        try:
            return self.socket.recv_into(buf)
        except TypeError:
            # see http://bugs.python.org/issue7827
            # use super implementation
            self.getBuf = super(ChannelTCP, self).getBuf
            return self.getBuf(buf)
        except socket.error as x:
            if self.closed: return -1
            raise x

    def put(self, b):
        if self.closed: return
        t = type(b)
        if t is types.StringType:
            s = b
        elif t is types.IntType:
            s = chr(b)
        else:
            raise "Illegal argument type: %s" % t
        self.socket.send(s)

    def putBuf(self, buf):
        if self.closed: return
        t = type(buf)
        if t is types.StringType:
            s = buf
        else:
            s = str(buf)
        self.socket.sendall(s)

    def flush(self):
        pass

    def stop(self):
        self.closed = True
        if self.started:
            self.socket.close()
