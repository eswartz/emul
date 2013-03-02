/*
  EmulatorTCFQueue.java

  (c) 2011 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.server.tcf;

import java.util.LinkedList;

import org.eclipse.tm.tcf.protocol.IEventQueue;
import org.eclipse.tm.tcf.protocol.Protocol;


/**
 * Implementation of Target Communication Framework event queue.
 */
public class EmulatorTCFQueue implements IEventQueue, Runnable {

    private final LinkedList<Runnable> queue = new LinkedList<Runnable>();
    private final Thread thread;
    private boolean waiting;
    private boolean shutdown;
    private int job_cnt;

    public EmulatorTCFQueue() {
        thread = new Thread(this);
        thread.setDaemon(true);
        thread.setName("TCF Event Dispatcher"); //$NON-NLS-1$
    }

    public void start() {
        thread.start();
    }

    public void shutdown() {
        try {
            synchronized (this) {
                shutdown = true;
                if (waiting) {
                    waiting = false;
                    notifyAll();
                }
            }
            thread.join();
        }
        catch (Exception e) {
            Protocol.log("Failed to shutdown TCF event dispatch thread", e); //$NON-NLS-1$
        }
    }

    private void error(Throwable x) {
        Protocol.log("Unhandled exception in TCF event dispatch", x); //$NON-NLS-1$
    }

    public void run() {
        for (;;) {
            try {
                Runnable r = null;
                synchronized (this) {
                    while (queue.size() == 0) {
                        if (shutdown) return;
                        waiting = true;
                        wait();
                    }
                    r = queue.removeFirst();
                }
                r.run();
            }
            catch (Throwable x) {
                error(x);
            }
        }
    }

    public synchronized void invokeLater(final Runnable r) {
        assert r != null;
        if (shutdown) throw new IllegalStateException("TCF event dispatcher has shut down"); //$NON-NLS-1$
        queue.add(r);
        if (waiting) {
            waiting = false;
            notifyAll();
        }
    }

    public boolean isDispatchThread() {
        return Thread.currentThread() == thread;
    }

    public synchronized int getCongestion() {
        int l0 = job_cnt / 10 - 100;
        int l1 = queue.size() / 10 - 100;
        if (l1 > l0) l0 = l1;
        if (l0 > 100) l0 = 100;
        return l0;
    }
}
