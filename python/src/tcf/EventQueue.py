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

import threading
import protocol

class EventQueue(object):

    def __init__(self):
        self.__thread = threading.Thread(target=self, name="TCF Event Dispatcher")
        self.__thread.daemon = True
        self.__is_waiting = False
        self.__is_shutdown = False
        self.__lock = threading.Condition()
        self.__queue = []

    def start(self):
        self.__thread.start()

    def shutdown(self):
        try:
            with self.__lock:
                self.__is_shutdown = True
                if self.__is_waiting:
                    self.__is_waiting = False
                    self.__lock.notifyAll()
            self.__thread.join()
        except BaseException as e:
            protocol.log("Failed to shutdown TCF event dispatch thread", e)

    def isShutdown(self):
        with self._lock:
            return self.__is_shutdown

    def __error(self, x):
        protocol.log("Unhandled exception in TCF event dispatch", x)

    def __call__(self):
        while True:
            try:
                with self.__lock:
                    while not self.__queue:
                        if self.__is_shutdown: return
                        self.__is_waiting = True
                        self.__lock.wait()
                    r, args, kwargs = self.__queue.pop(0)
                r(*args, **kwargs)
            except BaseException as x:
                self.__error(x)

    def invokeLater(self, r, *args, **kwargs):
        assert r
        with self.__lock:
            if self.__is_shutdown: raise RuntimeError("TCF event dispatcher has shut down")
            self.__queue.append((r, args, kwargs))
            if self.__is_waiting:
                self.__is_waiting = False
                self.__lock.notifyAll()

    def isDispatchThread(self):
        return threading.currentThread() is self.__thread

    def getCongestion(self):
        with self.__lock:
            job_cnt = 0
            l0 = job_cnt / 10 - 100
            l1 = len(self.__queue) / 10 - 100
            if l1 > l0: l0 = l1
            if l0 > 100: l0 = 100
            return l0
