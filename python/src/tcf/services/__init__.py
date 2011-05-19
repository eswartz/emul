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

import threading, collections
from tcf import protocol

_providers = []
_lock = threading.RLock()

class ServiceProvider(object):
    """
    Clients can implement this abstract class if they want to provide implementation of a local service or
    remote service proxy.
    """
    def getLocalService(self, channel):
        pass
    def getServiceProxy(self, channel, service_name):
        pass

def addServiceProvider(provider):
    with _lock:
        _providers.append(provider)

def removeServiceProvider(provider):
    with _lock:
        _providers.remove(provider)

def onChannelCreated(channel, services_by_name):
    with _lock:
        # TODO ZeroCopy support is incomplete
#        zero_copy = ZeroCopy()
#        services_by_name[zero_copy.getName()] = zero_copy
        for provider in _providers:
            try:
                arr = provider.getLocalService(channel)
                if not arr: continue
                for service in arr:
                    if services_by_name.has_key(service.getName()): continue
                    services_by_name[service.getName()] = service
            except Exception as x:
                protocol.log("Error calling TCF service provider", x);

def onChannelOpened(channel, service_names, services_by_name):
    with _lock:
        for name in service_names:
            for provider in _providers:
                try:
                    service = provider.getServiceProxy(channel, name)
                    if not service: continue
                    services_by_name[name] = service
                    break
                except Exception as x:
                    protocol.log("Error calling TCF service provider", x)
            if services_by_name.has_key(name): continue
            services_by_name[name] = GenericProxy(channel, name)

def getServiceManagerID():
    # In current implementation ServiceManager is a singleton,
    # so its ID is same as agent ID.
    return protocol.getAgentID()

class GenericCallback(object):
    def __init__(self, callback):
        self.callback = callback
    def __getattr__(self, attr):
        if attr.startswith("done"):
            return self.callback

class Service(object):
    def getName(self):
        raise NotImplementedError("Abstract method")
    def __str__(self):
        return self.getName()
    def _makeCallback(self, done):
        if isinstance(done, collections.Callable):
            return GenericCallback(done)
        return done

class ZeroCopy(Service):
    def getName(self):
        return "ZeroCopy"

class GenericProxy(Service):
    """
     * Objects of GenericProxy class represent remote services, which don't
     * have a proxy class defined for them.
     * Clients still can use such services, but framework will not provide
     * service specific utility methods for message formatting and parsing.
    """
    def __init__(self, channel, name):
        self.__channel = channel
        self.name = name
    def getName(self):
        return self.name
    def getChannel(self):
        return self.__channel

class DefaultServiceProvider(ServiceProvider):
    package_base = "tcf.services.remote"
    def getLocalService(self, channel):
        # TODO DiagnosticsService
        #return [DiagnosticsService(channel)]
        return []
    def getServiceProxy(self, channel, service_name):
        service = None
        try:
            clsName = service_name + "Proxy"
            package = self.package_base + "." + clsName
            clsModule = __import__(package, fromlist=[clsName], globals=globals())
            cls = clsModule.__dict__.get(clsName)
            service = cls(channel)
            assert service_name == service.getName()
        except ImportError:
            pass
        except Exception as x:
            protocol.log("Cannot instantiate service proxy for "+service_name, x)
        return service

addServiceProvider(DefaultServiceProvider())
