/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.tcf.protocol;

import java.util.ArrayList;
import java.util.TreeSet;

import org.eclipse.tm.internal.tcf.core.LocalPeer;
import org.eclipse.tm.internal.tcf.core.ServiceManager;
import org.eclipse.tm.internal.tcf.core.TransportManager;
import org.eclipse.tm.internal.tcf.services.local.LocatorService;
import org.eclipse.tm.tcf.services.ILocator;


/**
 * Class Protocol provides static methods to access Target Communication Framework root objects:
 * 1. the framework event queue and dispatch thread;
 * 2. local instance of Locator service, which maintains a list of available targets;
 * 3. list of open communication channels.
 * 
 * It also provides utility methods for posting asynchronous events,
 * including delayed events (timers).
 * 
 * Before TCF can be used, it should be given an object implementing IEventQueue interface:
 * @see #setEventQueue
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class Protocol {

    private static IEventQueue event_queue;
    private static ILogger logger;
    private static final TreeSet<Timer> timer_queue = new TreeSet<Timer>();
    private static int timer_cnt;
    
    private static class Timer implements Comparable<Timer>{
        final int id;
        final long time;
        final Runnable run;

        Timer(long time, Runnable run) {
            this.id = timer_cnt++;
            this.time = time;
            this.run = run;
        }

        public int compareTo(Timer x) {
            if (x == this) return 0;
            if (time < x.time) return -1;
            if (time > x.time) return +1;
            if (id < x.id) return -1;
            if (id > x.id) return +1;
            assert false;
            return 0;
        }
    }

    private static final Thread timer_dispatcher = new Thread() {
        public void run() {
            try {
                synchronized (timer_queue) {
                    while (true) {
                        if (timer_queue.isEmpty()) {
                            timer_queue.wait();
                        }
                        else {
                            long time = System.currentTimeMillis();
                            Timer t = timer_queue.first();
                            if (t.time > time) {
                                timer_queue.wait(t.time - time);
                            }
                            else {
                                timer_queue.remove(t);
                                invokeLater(t.run);
                            }
                        }
                    }
                }
            }
            catch (IllegalStateException x) {
                // Dispatch is shut down, exit this thread
            }
            catch (Throwable x) {
                log("Exception in TCF dispatch loop", x);
            }
        }
    };

    private static final ArrayList<CongestionMonitor> congestion_monitors = new ArrayList<CongestionMonitor>();

    /**
     * Before TCF can be used, it should be given an object implementing IEventQueue interface.
     * The implementation maintains a queue of objects implementing Runnable interface and
     * executes <code>run</code> methods of that objects in a sequence by a single thread.
     * The thread in referred as TCF event dispatch thread. Objects in the queue are called TCF events.
     * Executing <code>run</code> method of an event is also called dispatching of the event.
     * 
     * Only few methods in TCF APIs are thread safe - can be invoked from any thread.
     * If a method description does not say "can be invoked from any thread" explicitly -  
     * the method must be invoked from TCF event dispatch thread. All TCF listeners are
     * invoked from the dispatch thread.
     * 
     * @param event_queue - IEventQueue implementation.
     */
    public static void setEventQueue(IEventQueue event_queue) {
        assert Protocol.event_queue == null;
        Protocol.event_queue = event_queue;
        event_queue.invokeLater(new Runnable() {

            public void run() {
                new LocatorService();
                new LocalPeer();
            }
        });
        timer_dispatcher.setName("TCF Timer Dispatcher");
        timer_dispatcher.setDaemon(true);
        timer_dispatcher.start();
    }

    /**
     * @return instance of IEventQueue that is used for TCF events.
     */
    public static IEventQueue getEventQueue() {
        return event_queue;
    }

    /**
     * Returns true if the calling thread is TCF event dispatch thread.
     * Use this call the ensure that a given task is being executed (or not being)
     * on dispatch thread.
     *
     * @return true if running on the dispatch thread.
     */
    public static boolean isDispatchThread() {
        return event_queue != null && event_queue.isDispatchThread();
    }

    /**
     * Causes <code>runnable</code> event to have its <code>run</code>
     * method called in the dispatch thread of the framework.
     * Events are dispatched in same order as queued.
     * If invokeLater is called from the dispatching thread
     * the <i>runnable.run()</i> will still be deferred until
     * all pending events have been processed.
     *
     * This method can be invoked from any thread.
     *
     * @param runnable  the <code>Runnable</code> whose <code>run</code>
     *                  method should be executed asynchronously.
     */
    public static void invokeLater(Runnable runnable) {
        event_queue.invokeLater(runnable);
    }

    /**
     * Causes <code>runnable</code> event to have its <code>run</code>
     * method called in the dispatch thread of the framework.
     * The event is dispatched after given delay.
     *
     * This method can be invoked from any thread.
     *
     * @param delay     milliseconds to delay event dispatch.
     *                  If delay <= 0 the event is posted into the
     *                  "ready" queue without delay.
     * @param runnable  the <code>Runnable</code> whose <code>run</code>
     *                  method should be executed asynchronously.
     */
    public static void invokeLater(long delay, Runnable runnable) {
        if (delay <= 0) {
            event_queue.invokeLater(runnable);
        }
        else {
            synchronized (timer_queue) {
                timer_queue.add(new Timer(System.currentTimeMillis() + delay, runnable));
                timer_queue.notify();
            }
        }
    }
    
    /**
     * Causes <code>runnable</code> to have its <code>run</code>
     * method called in the dispatch thread of the framework.
     * Calling thread is suspended until the method is executed.
     * If invokeAndWait is called from the dispatching thread
     * the <i>runnable.run()</i> is executed immediately.
     *
     * This method can be invoked from any thread.
     *
     * @param runnable  the <code>Runnable</code> whose <code>run</code>
     *                  method should be executed on dispatch thread.
     */
    public static void invokeAndWait(final Runnable runnable) {
        if (event_queue.isDispatchThread()) {
            runnable.run();
        }
        else {
            Runnable r = new Runnable() {
                public void run() {
                    try {
                        runnable.run();
                    }
                    finally {
                        synchronized (this) {
                            notify();
                        }
                    }
                }
            };
            synchronized (r) {
                event_queue.invokeLater(r);
                try {
                    r.wait();
                }
                catch (InterruptedException x) {
                    throw new Error(x);
                }
            }
        }
    }
    
    /**
     * Set framework logger.
     * By default Eclipse logger is used, or System.err if TCF is used stand-alone.
     * 
     * @param logger - an object implementing ILogger interface.
     */
    public static synchronized void setLogger(ILogger logger) {
        Protocol.logger = logger;
    }
    
    /**
     * Logs the given message.
     * @see #setLogger
     * @param msg - log entry text
     * @param x - a Java exception associated with the log entry or null.
     */
    public static synchronized void log(String msg, Throwable x) {
        if (logger == null) {
            System.err.println(msg);
            if (x != null) x.printStackTrace();
        }
        else {
            logger.log(msg, x);
        }
    }

    /**
     * Get instance of the framework locator service.
     * The service can be used to discover available remote peers.
     * 
     * @return instance of ILocator.
     */
    public static ILocator getLocator() {
        return LocatorService.getLocator();
    }
    
    /**
     * Return an array of all open channels.
     * @return an array of IChannel
     */
    public static IChannel[] getOpenChannels() {
        assert isDispatchThread();
        return TransportManager.getOpenChannels();
    }
    
    /**
     * Interface to be implemented by clients willing to be notified when
     * new TCF communication channel is opened.
     * 
     * The interface allows a client to get pointers to channel objects
     * that were opened by somebody else. If a client open a channel itself, it already has
     * the pointer and does not need Protocol.ChannelOpenListener. If a channel is created,
     * for example, by remote peer connecting to the client, the only way to get the pointer
     * is Protocol.ChannelOpenListener.
     */
    public interface ChannelOpenListener {
        public void onChannelOpen(IChannel channel);
    }
    
    /**
     * Add a listener that will be notified when new channel is opened.
     * @param listener
     */
    public static void addChannelOpenListener(ChannelOpenListener listener) {
        assert isDispatchThread();
        TransportManager.addChanelOpenListener(listener);
    }

    /**
     * Remove channel opening listener.
     * @param listener
     */
    public static void removeChannelOpenListener(ChannelOpenListener listener) {
        assert isDispatchThread();
        TransportManager.removeChanelOpenListener(listener);
    }

    /**
     * Transmit TCF event message.
     * The message is sent to all open communication channels – broadcasted.
     */
    public static void sendEvent(String service_name, String event_name, byte[] data) {
        assert isDispatchThread();
        TransportManager.sendEvent(service_name, event_name, data);
    }
    
    /**
     * Call back after all TCF messages sent by this host up to this moment are delivered
     * to their intended target. This method is intended for synchronization of messages
     * across multiple channels.
     * 
     * Note: Cross channel synchronization can reduce performance and throughput.
     * Most clients don't need cross channel synchronization and should not call this method. 
     *  
     * @param done will be executed by dispatch thread after pending communication 
     * messages are delivered to corresponding targets.
     */
    public static void sync(Runnable done) {
        assert isDispatchThread();
        TransportManager.sync(done);
    }
    
    /**
     * Clients implement CongestionMonitor interface to monitor usage of local resources,
     * like, for example, display queue size - if the queue becomes too big, UI response time
     * can become too high, or it can crash all together because of OutOfMemory errors.
     * TCF flow control logic prevents such conditions by throttling traffic coming from remote peers.   
     * Note: Local (in-bound traffic) congestion is detected by framework and reported to
     * remote peer without client needed to be involved. Only clients willing to provide
     * additional data about local congestion should implement CongestionMonitor and
     * register it using Protocol.addCongestionMonitor().
     */
    public interface CongestionMonitor {
        /**
         * Get current level of client resource utilization. 
         * @return integer value in range –100..100, where –100 means all resources are free,
         *         0 means optimal load, and positive numbers indicate level of congestion.
         */
        int getCongestionLevel();
    }
    
    /**
     * Register a congestion monitor.
     * @param monitor - client implementation of CongestionMonitor interface
     */
    public static void addCongestionMonitor(CongestionMonitor monitor) {
        assert monitor != null;
        assert isDispatchThread();
        congestion_monitors.add(monitor);
    }
    
    /**
     * Unregister a congestion monitor.
     * @param monitor - client implementation of CongestionMonitor interface
     */
    public static void removeCongestionMonitor(CongestionMonitor monitor) {
        assert isDispatchThread();
        congestion_monitors.remove(monitor);
    }
    
    /**
     * Get current level of local traffic congestion.
     * 
     * @return integer value in range –100..100, where –100 means no pending
     *         messages (no traffic), 0 means optimal load, and positive numbers
     *         indicate level of congestion.
     */
    public static int getCongestionLevel() {
        assert isDispatchThread();
        int level = -100;
        for (CongestionMonitor m : congestion_monitors) {
            int n = m.getCongestionLevel();
            if (n > level) level = n;
        }
        if (event_queue != null) {
            int n = event_queue.getCongestion();
            if (n > level) level = n;
        }
        if (level > 100) level = 100;
        return level;
    }

    /**
     * Register s service provider. 
     * @param provider - IServiceProvider implementation
     */
    public static void addServiceProvider(IServiceProvider provider){
        ServiceManager.addServiceProvider(provider);
    }
    
    /**
     * Unregister s service provider. 
     * @param provider - IServiceProvider implementation
     */
    public static void removeServiceProvider(IServiceProvider provider){
        ServiceManager.removeServiceProvider(provider);
    }

    /**
     * Register s transport provider. 
     * @param provider - ITransportProvider implementation
     */
    public static void addTransportProvider(ITransportProvider provider){
        assert isDispatchThread();
        TransportManager.addTransportProvider(provider);
    }
    
    /**
     * Unregister s transport provider. 
     * @param provider - ITransportProvider implementation
     */
    public static void removeTransportProvider(ITransportProvider provider){
        assert isDispatchThread();
        TransportManager.removeTransportProvider(provider);
    }
}
