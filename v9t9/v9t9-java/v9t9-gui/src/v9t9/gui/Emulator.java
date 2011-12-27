/**
 * 
 */
package v9t9.gui;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;


import v9t9.common.client.IClient;
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

	static {
		if (sIsWebStarted && System.getProperty("jna.library.path") == null) {
			String path = Native.getWebStartLibraryPath("v9t9render");
			System.out.println("Native libs at " + path);
			if (path != null)
				System.setProperty("jna.library.path", path);
		}		
		
	}
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
					path = new URL(EmulatorGuiData.sBaseV9t9URL, "../libv9t9render").getPath();
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
		
		Getopt getopt = new Getopt(Emulator.class.getName(), args, 
				"r:",
				new LongOpt[] {
					new LongOpt("remote", LongOpt.REQUIRED_ARGUMENT, new StringBuffer(), 'r')
				}
		);
		
		int opt;
		while ((opt = getopt.getopt()) != -1)
		{
			if (opt == 0) {
				if (getopt.getLongind() == 0) {
					opt = 'r';
				}
			}
			
			if (opt == 'r') {
				remote = getopt.getOptarg();
			}
		}
		
		EmulatorServerBase server = remote != null 
			? new EmulatorRemoteServer(remote)
			: new EmulatorLocalServer();
		
		String modelId = getModelId(server, args);
		String clientId = getClientId(args);
		

		createAndRun(server, modelId, clientId);
	}


	public static void createAndRun(EmulatorServerBase server, String modelId, String clientId) {
		try {
			server.init(modelId);
		} catch (IOException e) {
			System.err.println("Failed to contact or create server:" + modelId);
			e.printStackTrace();
			System.exit(23);
			return;
		}
		
		IClient client = null;
		client = ClientFactory.createClient(clientId, 
				server.getMachine());

		if (client == null) {
			System.err.println("Failed to contact or create client: " + clientId);
			System.exit(23);
			return;
		}
		server.setClient(client);
		
		try {
			server.run();
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
