/*
  EmulatorTCFServer.java

  (c) 2011-2012 Edward Swartz

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

import java.io.IOException;
import java.io.PrintWriter;

import org.eclipse.tm.tcf.core.ServerTCP;
import org.eclipse.tm.tcf.protocol.ILogger;
import org.eclipse.tm.tcf.protocol.Protocol;

import ejs.base.properties.IProperty;
import ejs.base.settings.Logging;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.SettingSchema;
import v9t9.common.settings.Settings;

/**
 * This class manages TCF servers for remote access and control of a running
 * emulator.
 * @author ejs
 *
 */
public class EmulatorTCFServer {
	private static boolean DEBUG = true;
	
	public static final SettingSchema settingTCFLog = new SettingSchema(
			ISettingsHandler.TRANSIENT,
			"TCFLog",
			Boolean.FALSE);
	
	private volatile boolean isRunning;

	private IProperty tcfLog;

	private EmulatorTCFQueue queue;

	private EmulatorTCFServiceProvider serviceProvider;

	private final IMachine machine;

	private ServerTCP server;

	/**
	 * @param machine 
	 * 
	 */
	public EmulatorTCFServer(IMachine machine) {

		this.machine = machine;
		setupLogging();

		queue = (EmulatorTCFQueue) Protocol.getEventQueue();
		if (queue == null) {
			queue = new EmulatorTCFQueue();
			Protocol.setEventQueue(queue);
		}
        
        serviceProvider = new EmulatorTCFServiceProvider(machine);
	}
	
	private void setupLogging() {
		tcfLog = Settings.get(machine, settingTCFLog);
		Logging.registerLog(tcfLog, "/tmp/emul_tcf.txt");
		
		Protocol.setLogger(new ILogger() {

            public void log(String msg, Throwable x) {
            	if (DEBUG) {
	                System.err.println(msg);
	                if (x != null) x.printStackTrace();
            	}
                
                PrintWriter pw = Logging.getLog(tcfLog);
                if (pw != null) {
                	pw.println(msg);
                	if (x != null)
                		x.printStackTrace(pw);
                }
            }
        });		
	}

	/**
	 * @return the isRunning
	 */
	public boolean isRunning() {
		return isRunning;
	}
	
	public synchronized void run() {
		if (isRunning)
			return;

		queue.start();
		
		Protocol.addServiceProvider(serviceProvider);
		
		Protocol.invokeAndWait(new Runnable() {
			public void run() {
				try {
					server = new ServerTCP(machine.getModel().getIdentifier(), 9900);
					isRunning = true;
				} catch (IOException e) {
					Protocol.log("Failed to start TCF server", e);
				}
			}
		});
		
	}
	
	public synchronized void stop() {
		if (!isRunning)
			return;
		
		try {
			server.close();
		} catch (IOException e) {
			Protocol.log("Failed to stop server", e);
		}
		
		Protocol.removeServiceProvider(serviceProvider);
		queue.shutdown();
		
		isRunning = false;
	}
}
