/*
  Emulator.java

  (c) 2011-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import v9t9.common.client.IClient;
import v9t9.common.cpu.ICpu;
import v9t9.common.machine.TerminatedException;
import v9t9.gui.client.ClientFactory;
import v9t9.gui.client.ConsoleOnlyClient;
import v9t9.gui.client.swt.SwtAwtJavaClient;
import v9t9.gui.client.swt.SwtJavaClient;
import v9t9.gui.client.swt.SwtLwjglJavaClient;
import v9t9.remote.EmulatorRemoteServer;
import v9t9.server.EmulatorLocalServer;
import v9t9.server.MachineModelFactory;
import v9t9.server.client.EmulatorServerBase;

/**
 * @author Ed
 *
 */
public class Emulator {
	private static final Logger logger = Logger.getLogger(Emulator.class);
	
	private static final boolean sIsWebStarted = System.getProperty("javawebstart.version") != null;
	private static boolean debug;

	static {
		ClientFactory.INSTANCE.register(SwtJavaClient.ID, SwtJavaClient.class);
		ClientFactory.INSTANCE.register(SwtAwtJavaClient.ID, SwtAwtJavaClient.class);
		ClientFactory.INSTANCE.register(SwtLwjglJavaClient.ID, SwtLwjglJavaClient.class);
		ClientFactory.INSTANCE.register(ConsoleOnlyClient.ID, ConsoleOnlyClient.class);
		
		ClientFactory.INSTANCE.setDefault(SwtLwjglJavaClient.ID);
	}
	
	static {
		if (System.getProperty("jna.library.path") == null) {
			if (sIsWebStarted) {
				/* not shipping
				String path = Native.getWebStartLibraryPath("v9t9render");
				System.out.println("Native libs at " + path);
				if (path != null)
					System.setProperty("jna.library.path", path);
				*/
			}
			else {
				String path;
				try {
					path = new URL(EmulatorGuiData.sBaseV9t9URL, "libv9t9render").getPath();
					logger.info("Native libs at " + path);
					if (path != null)
						System.setProperty("jna.library.path", path);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String remote = null;
		String configdir = null;
		boolean clean = false;
		boolean tcf = false;
		
		Getopt getopt = new Getopt(Emulator.class.getName(), args, 
				"r:Cc:tTs:d",
				new LongOpt[] {
					//new LongOpt("remote", LongOpt.REQUIRED_ARGUMENT, new StringBuffer(), 'r'),
					new LongOpt("clean", LongOpt.NO_ARGUMENT, null, 'C'),
					new LongOpt("configdir", LongOpt.REQUIRED_ARGUMENT, null, 'c'),
					new LongOpt("debug", LongOpt.NO_ARGUMENT, null, 'd'),
					new LongOpt("tcf", LongOpt.NO_ARGUMENT, null, 'T'),
					new LongOpt("test", LongOpt.NO_ARGUMENT, null, 't'),
					new LongOpt("set", LongOpt.REQUIRED_ARGUMENT, null, 's'),
					new LongOpt("list-machines", LongOpt.NO_ARGUMENT, null, 0x101),
					new LongOpt("list-clients", LongOpt.NO_ARGUMENT, null, 0x102),
					new LongOpt("machine", LongOpt.REQUIRED_ARGUMENT, null, 0x103),
					new LongOpt("client", LongOpt.REQUIRED_ARGUMENT, null, 0x104),
				}
		);
		
		EmulatorServerBase server = remote != null 
				? new EmulatorRemoteServer(remote)
				: new EmulatorLocalServer();

		Map<String, String> settings = new LinkedHashMap<String, String>();

		String modelId = MachineModelFactory.INSTANCE.getDefaultModel();
		String clientId = ClientFactory.INSTANCE.getDefaultClient();
				
		int opt;
		while ((opt = getopt.getopt()) != -1)
		{
			if (opt == 'C') {
				clean = true;
			}
			else if (opt == 'c') {
				configdir = getopt.getOptarg().trim();
			}
			else if (opt == 'd') {
				debug = true;
			}
			else if (opt == 'T') {
				tcf = true;
			}
			else if (opt == 't') {
				settings.put(ICpu.settingTestSuccessSymbol.getName(), "~SUCCESS~");
				settings.put(ICpu.settingTestFailureSymbol.getName(), "~FAILURE~");
				//settings.put(ICpu.settingRunForCount.getName(), "30000000");
				settings.put(ICpu.settingDetectCrash.getName(), "true");
				debug = true;
				
			}
			else if (opt == 's') {
				String arg = getopt.getOptarg().trim();
				int idx = arg.indexOf('=');
				if (idx < 0)  {
					System.err.println("expected var=value for -s " + arg);
					continue;
				}
				String var = arg.substring(0, idx);
				String val = arg.substring(idx+1);
				settings.put(var, val);
			}
			else if (opt == 0x101) {
				for (String model : MachineModelFactory.INSTANCE.getRegisteredModels()) {
					System.out.println(model);
				}
				return;
			}
			else if (opt == 0x102) {
				for (String client : ClientFactory.INSTANCE.getRegisteredClients()) {
					System.out.println(client);
				}
				return;
			}
			else if (opt == 0x103) {
				modelId = getopt.getOptarg();
				if (!MachineModelFactory.INSTANCE.getRegisteredModels().contains(modelId)) {
					System.err.println("No such registered machine: " + modelId);
					return;
				}
			}
			else if (opt == 0x104) {
				clientId = getopt.getOptarg();
				if (!ClientFactory.INSTANCE.getRegisteredClients().contains(clientId)) {
					System.err.println("No such registered client: " + clientId);
					return;
				}
			}
		}
		if (tcf)
			server.enableTcf();
		
		if (configdir != null) {
			server.setConfigDir(configdir);
			if (clean) {
				File top = new File(server.getSettingsHandler().getUserSettings().getConfigDirectory());
				deleteDirectory(top);
			}
		}

		create(server, modelId, clientId);
		
		server.setSettings(settings);
		
		runServer(server);
		
		// in some OSes, AWT does not want to die
		System.exit(0);
	}


	/**
	 * @param top
	 */
	private static void deleteDirectory(File top) {
		File[] entries = top.listFiles();
		if (entries != null) {
			for (File entry : entries) {
				deleteDirectory(entry);
			}
		} 
		top.delete();
	}


	public static IClient create(EmulatorServerBase server, String modelId, String clientId) {
		try {
			server.init(modelId);
		} catch (IOException e) {
			System.err.println("Failed to contact or create server:" + modelId);
			e.printStackTrace();
			System.exit(23);
			return null;
		}
		
		IClient client = null;
		client = ClientFactory.INSTANCE.createClient(clientId, 
				server.getMachine());


		for (@SuppressWarnings("unchecked")
		Enumeration<Appender> e = LogManager.getRootLogger().getAllAppenders(); e.hasMoreElements(); ) {
			Appender a = e.nextElement();
			if (a instanceof FileAppender) {
				System.out.println("Log file: " + (((FileAppender) a).getFile()) );
			}
		}
		
		if (debug) {
			server.getMachine().getSettings().get(ICpu.settingDumpFullInstructions).setBoolean(true);
		}
		
		if (client == null) {
			System.err.println("Failed to contact or create client: " + clientId);
			System.exit(23);
			return null;
		}
		server.setClient(client);
		
		return client;
		
	}
	
	public static void runServer(EmulatorServerBase server) {
		try {
			server.run();
		} catch (TerminatedException e) {
			// good
		} finally {
			try {
				server.dispose();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
