/**
 * 
 */
package v9t9.gui;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.LogManager;

import v9t9.common.client.IClient;
import v9t9.common.cpu.ICpu;
import v9t9.common.machine.TerminatedException;
import v9t9.gui.client.ClientFactory;
import v9t9.gui.client.swt.SwtAwtJavaClient;
import v9t9.gui.client.swt.SwtJavaClient;
import v9t9.gui.client.swt.SwtLwjglJavaClient;
import v9t9.remote.EmulatorRemoteServer;
import v9t9.server.EmulatorLocalServer;
import v9t9.server.client.EmulatorServerBase;

import com.sun.jna.Native;

/**
 * @author Ed
 *
 */
public class Emulator {
	
	private static final boolean sIsWebStarted = System.getProperty("javawebstart.version") != null;
	private static boolean debug;

//	static {
//		String jnaLibraryPath = System.getProperty("jna.library.path");
//		if (sIsWebStarted && jnaLibraryPath == null) {
//			String path = Native.getWebStartLibraryPath("v9t9render");
//			System.out.println("Native libs at " + path);
//			if (path != null)
//				System.setProperty("jna.library.path", path);
//		}		
//		
//	}
	static {
		ClientFactory.register(SwtJavaClient.ID, SwtJavaClient.class);
		ClientFactory.register(SwtAwtJavaClient.ID, SwtAwtJavaClient.class);
		ClientFactory.register(SwtLwjglJavaClient.ID, SwtLwjglJavaClient.class);
	}
	
 	private static boolean findArgument(String[] args, String string) {
    	for (String arg : args)
    		if (arg.equals(string))
    			return true;
		return false;
	}
 	

	static {
		if (System.getProperty("jna.library.path") == null) {
			if (sIsWebStarted) {
				String path = Native.getWebStartLibraryPath("v9t9render");
				System.out.println("Native libs at " + path);
				if (path != null)
					System.setProperty("jna.library.path", path);
			}
			else {
				String path;
				try {
					path = new URL(EmulatorGuiData.sBaseV9t9URL, "libv9t9render").getPath();
					System.out.println("Native libs at " + path);
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
				"r:Cc:ts:",
				new LongOpt[] {
					//new LongOpt("remote", LongOpt.REQUIRED_ARGUMENT, new StringBuffer(), 'r'),
					new LongOpt("clean", LongOpt.NO_ARGUMENT, null, 'C'),
					new LongOpt("configdir", LongOpt.REQUIRED_ARGUMENT, null, 'c'),
					new LongOpt("debug", LongOpt.NO_ARGUMENT, null, 'd'),
					new LongOpt("tcf", LongOpt.NO_ARGUMENT, null, 't'),
					new LongOpt("set", LongOpt.REQUIRED_ARGUMENT, null, 's'),
				}
		);

		Map<String, String> settings = new LinkedHashMap<String, String>();

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
			else if (opt == 't') {
				tcf = true;
			}
			else if (opt == 's') {
				String arg = getopt.getOptarg().trim();
				int idx = arg.indexOf('=');
				if (idx < 0)  {
					System.err.println("expected var=value for -s " + arg);
					continue;
				}
				String var = arg.substring(0, idx-1);
				String val = arg.substring(idx+1);
				settings.put(var, val);
			}
		}
		
		EmulatorServerBase server = remote != null 
			? new EmulatorRemoteServer(remote)
			: new EmulatorLocalServer();
		
		if (tcf)
			server.enableTcf();
		
		if (configdir != null) {
			server.setConfigDir(configdir);
			if (clean) {
				File top = new File(server.getSettingsHandler().getUserSettings().getConfigDirectory());
				deleteDirectory(top);
			}
		}
		
		String modelId = getModelId(server, args);
		String clientId = getClientId(args);
		

		create(server, modelId, clientId);
		
		server.setSettings(settings);
		
		runServer(server);
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
		client = ClientFactory.createClient(clientId, 
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


	/**
	 * @param args
	 * @return
	 */
	private static String getClientId(String[] args) {
		String clientID = SwtLwjglJavaClient.ID;
        /*if (findArgument(args, "--awt")) {
        	clientID = AwtJavaClient.ID;
		} 
        else 
        */
		if (findArgument(args, "--swt")) {
        	clientID = SwtJavaClient.ID;
		} 
        else if (findArgument(args, "--swtawt")) {
        	clientID = SwtAwtJavaClient.ID;
        } 
        else if (findArgument(args, "--swtgl")) {
        	clientID = SwtLwjglJavaClient.ID;
		} 
        return clientID;
	}


	/**
	 * @param server 
	 * @param args
	 * @return
	 */
	private static String getModelId(EmulatorServerBase server, String[] args) {

		Collection<String> models = server.getMachineModelFactory().getRegisteredModels();
		for (String arg : args) {
			if (models.contains(arg))
				return arg;
		}
        
        return server.getMachineModelFactory().getDefaultModel();
	}
}
