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
Module protocol provides static methods to access Target Communication Framework root objects:
1. the framework event queue and dispatch thread
2. local instance of Locator service, which maintains a list of available targets
3. list of open communication channels.

It also provides utility methods for posting asynchronous events,
including delayed events (timers).
"""

import sys, uuid, threading
from EventQueue import EventQueue

_event_queue = None
def startEventQueue():
    global _event_queue
    if _event_queue and not _event_queue.isShutdown(): return
    _event_queue = EventQueue()
    _event_queue.start()
    # initialize LocatorService
    from services.local.LocatorService import LocatorService
    _event_queue.invokeLater(LocatorService)

def getEventQueue():
    """
    @return instance of event queue that is used for TCF events.
    """
    return _event_queue

def isDispatchThread():
    """
    Returns true if the calling thread is the TCF event dispatch thread.
    Use this call to ensure that a given task is being executed (or not being)
    on dispatch thread.

    @return true if running on the dispatch thread.
    """
    return _event_queue is not None and _event_queue.isDispatchThread()

def invokeLater(callable, *args, **kwargs):
    """
    Causes callable to be called with given arguments
    in the dispatch thread of the framework.
    Events are dispatched in same order as queued.
    If invokeLater is called from the dispatching thread
    the callable will still be deferred until
    all pending events have been processed.

    This method can be invoked from any thread.

    @param callable the callable to be executed asynchronously
    """
    _event_queue.invokeLater(callable, *args, **kwargs)

def invokeLaterWithDelay(delay, callable, *args, **kwargs):
    """
    Causes callable event to called in the dispatch thread of the framework.
    The event is dispatched after given delay.

    This method can be invoked from any thread.

    @param delay     milliseconds to delay event dispatch.
                     If delay <= 0 the event is posted into the
                     "ready" queue without delay.
    @param callable  the callable to be executed asynchronously.
    """
    if delay <= 0:
        _event_queue.invokeLater(callable, *args, **kwargs)
    else:
        # TODO timer_queue
        raise NotImplementedError("Implement invokeLaterWithDelay")
#        synchronized (timer_queue) {
#            timer_queue.add(new Timer(System.currentTimeMillis() + delay, runnable))
#            timer_queue.notify()

def invokeAndWait(callable, *args, **kwargs):
    """
    Causes callable to be called in the dispatch thread of the framework.
    Calling thread is suspended until the method is executed.
    If invokeAndWait is called from the dispatching thread
    the callable is executed immediately.

    This method can be invoked from any thread.

    @param runnable  the callable to be executed on dispatch thread.
    """
    if _event_queue.isDispatchThread():
        return callable(*args, **kwargs)
    else:
        class DoRun():
            result = None
            def __call__(self):
                try:
                    self.result = callable(*args, **kwargs)
                finally:
                    with runLock:
                        runLock.notify()
        doRun = DoRun()
        runLock = threading.Condition()
        with runLock:
            _event_queue.invokeLater(doRun)
            runLock.wait()
            return doRun.result

_agentID = str(uuid.uuid4())
def getAgentID():
    return _agentID

_logger = None
def setLogger(logger):
    """
    Set framework logger.
    By default sys.stderr is used.

    @param logger - an object implementing Logger interface.
    """
    global _logger
    _logger = logger

def log(msg, x=None):
    """
    Logs the given message.
    @see #setLogger
    This method can be invoked from any thread.
    @param msg - log entry text
    @param x - an exception associated with the log entry or null.
    """
    if not _logger:
        print>>sys.stderr, msg
        while x:
            import traceback
            print>>sys.stderr, "%s: %s" % (type(x).__name__, x)
            tb = getattr(x, "tb", None) or sys.exc_info()[2]
            if tb:
                traceback.print_tb(tb)
            caused_by = getattr(x, "caused_by", None)
            if caused_by:
                print>>sys.stderr, "Caused by:"
                x = caused_by
            else:
                break
    else:
        _logger.log(msg, x)

def getLocator():
    """
    Get instance of the framework locator service.
    The service can be used to discover available remote peers.
    @return instance of LocatorService.
    """
    from tcf.services.local.LocatorService import LocatorService
    return LocatorService.locator

def getOpenChannels():
    """
    Return an array of all open channels.
    @return an array of IChannel
    """
    assert isDispatchThread()
    import transport
    return transport.getOpenChannels()

class ChannelOpenListener(object):
    """
    Interface to be implemented by clients willing to be notified when
    new TCF communication channel is opened.

    The interface allows a client to get pointers to channel objects
    that were opened by somebody else. If a client open a channel itself, it already has
    the pointer and does not need Protocol.ChannelOpenListener. If a channel is created,
    for example, by remote peer connecting to the client, the only way to get the pointer
    is Protocol.ChannelOpenListener.
    """
    def onChannelOpen(self, channel):
        pass

def addChannelOpenListener(listener):
    """
    Add a listener that will be notified when new channel is opened.
    @param listener
    """
    assert isDispatchThread()
    import transport
    transport.addChannelOpenListener(listener)

def removeChannelOpenListener(listener):
    """
    Remove channel opening listener.
    @param listener
    """
    assert isDispatchThread()
    import transport
    transport.removeChannelOpenListener(listener)

def sendEvent(service_name, event_name, data):
    """
    Transmit TCF event message.
    The message is sent to all open communication channels - broadcasted.
    """
    assert isDispatchThread()
    import transport
    transport.sendEvent(service_name, event_name, data)

def sync(done):
    """
    Call back after all TCF messages sent by this host up to this moment are delivered
    to their intended target. This method is intended for synchronization of messages
    across multiple channels.

    Note: Cross channel synchronization can reduce performance and throughput.
    Most clients don't need cross channel synchronization and should not call this method.

    @param done will be executed by dispatch thread after pending communication
    messages are delivered to corresponding targets.
    """
    assert isDispatchThread()
    import transport
    transport.sync(done)

class CongestionMonitor(object):
    """
    Clients implement CongestionMonitor interface to monitor usage of local resources,
    like, for example, display queue size - if the queue becomes too big, UI response time
    can become too high, or it can crash all together because of OutOfMemory errors.
    TCF flow control logic prevents such conditions by throttling traffic coming from remote peers.
    Note: Local (in-bound traffic) congestion is detected by framework and reported to
    remote peer without client needed to be involved. Only clients willing to provide
    additional data about local congestion should implement CongestionMonitor and
    register it using Protocol.addCongestionMonitor().
    """
    def getCongestionLevel(self):
        """
        Get current level of client resource utilization.
        @return integer value in range -100..100, where -100 means all resources are free,
                0 means optimal load, and positive numbers indicate level of congestion.
        """
        raise NotImplementedError("Abstract method")

_congestion_monitors = []
def addCongestionMonitor(monitor):
    """
    Register a congestion monitor.
    @param monitor - client implementation of CongestionMonitor interface
    """
    assert isDispatchThread()
    _congestion_monitors.add(monitor)

def removeCongestionMonitor(monitor):
    """
    Unregister a congestion monitor.
    @param monitor - client implementation of CongestionMonitor interface
    """
    assert isDispatchThread()
    _congestion_monitors.remove(monitor)

def getCongestionLevel():
    """
    Get current level of local traffic congestion.

    @return integer value in range -100..100, where -100 means no pending
          messages (no traffic), 0 means optimal load, and positive numbers
          indicate level of congestion.
    """
    assert isDispatchThread()
    level = -100
    for m in _congestion_monitors:
        n = m.getCongestionLevel()
        if n > level: level = n
    if _event_queue:
        n = _event_queue.getCongestion()
        if n > level: level = n
    if level > 100: level = 100
    return level

def addServiceProvider(provider):
    """
    Register service provider.
    This method can be invoked from any thread.
    @param provider - ServiceProvider implementation
    """
    import services
    services.addServiceProvider(provider)

def removeServiceProvider(provider):
    """
    Unregister service provider.
    This method can be invoked from any thread.
    @param provider - ServiceProvider implementation
    """
    import services
    services.removeServiceProvider(provider)

def addTransportProvider(provider):
    """
    Register transport provider.
    This method can be invoked from any thread.
    @param provider - TransportProvider implementation
    """
    import transport
    transport.addTransportProvider(provider)

def removeTransportProvider(provider):
    """
    Unregister transport provider.
    This method can be invoked from any thread.
    @param provider - TransportProvider implementation
    """
    import transport
    transport.removeTransportProvider(provider)
