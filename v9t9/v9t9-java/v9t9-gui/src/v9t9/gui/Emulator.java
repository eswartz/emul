/**
 * 
 */
package v9t9.gui;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import v9t9.emulator.EmulatorServer;
import v9t9.emulator.clients.builtin.ClientFactory;
import v9t9.emulator.clients.builtin.awt.AwtJavaClient;
import v9t9.emulator.clients.builtin.swt.SwtAwtJavaClient;
import v9t9.emulator.clients.builtin.swt.SwtJavaClient;
import v9t9.emulator.clients.builtin.swt.SwtLwjglJavaClient;
import v9t9.emulator.hardware.EnhancedMachineModel;
import v9t9.emulator.hardware.F99bMachineModel;
import v9t9.emulator.hardware.StandardMachineModel;
import v9t9.emulator.hardware.StandardMachineV9938Model;
import v9t9.engine.Client;

import com.sun.jna.Native;

/**
 * @author Ed
 *
 */
public class Emulator {


	static {
		ClientFactory.register(SwtJavaClient.ID, SwtJavaClient.class);
		ClientFactory.register(SwtAwtJavaClient.ID, SwtAwtJavaClient.class);
		ClientFactory.register(AwtJavaClient.ID, AwtJavaClient.class);
		ClientFactory.register(SwtLwjglJavaClient.ID, SwtLwjglJavaClient.class);
	}
	
 	private static boolean findArgument(String[] args, String string) {
    	for (String arg : args)
    		if (arg.equals(string))
    			return true;
		return false;
	}
 	

	private static final boolean sIsWebStarted = System.getProperty("javawebstart.version") != null;
	static final boolean sIsDevBuild;
	
	private static final URL sBaseDataURL;
	private static final URL sBaseV9t9URL;
	static {
		URL url = Emulator.class.getClassLoader().getResource(".");
		URL burl = Emulator.class.getClassLoader().getResource(
				Emulator.class.getName().replace(".", "/") + ".class");
		System.out.println("\n\n\n\n");
		System.out.println("/ URL = " + url);
		System.out.println("Emulator.class URL = " + burl);
		System.out.flush();
		if (url != null) {
			// "." will be under "bin", go to parent of tree
			try {
				url = new URL(url, "..");
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			
		}
		else {
			try {
				// get out of sources to build dir
				File cwdParentParent = new File(System.getProperty("user.dir"), "/../..");
				url = new URL("file", null, cwdParentParent.getAbsolutePath());
			} catch (MalformedURLException e) {
				e.printStackTrace();
				try {
					url = URI.create(".").toURL();
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
					System.exit(123);
				}
			}
		}
		
		if (burl != null) {
			// "." will be under "bin", go to parent of tree
			try {
				String burlString = burl.toString();
				if (!burlString.contains("!/")) {
					burl = new URL(burlString.substring(0, burlString.indexOf("bin/v9t9")));
					burl = new URL(burl, "data/");
				} else {
					burl = new URL(burlString.substring(0, burlString.indexOf(EmulatorServer.class.getName().replace(".", "/"))));
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			
		}
		sBaseV9t9URL = url;
		sBaseDataURL = burl;
		System.out.println("sBaseV9t9URL = " + sBaseV9t9URL);
		System.out.println("sBaseBuildURL = " + sBaseDataURL);
		
		sIsDevBuild = sBaseV9t9URL != null && sBaseV9t9URL.getProtocol().equals("file");
	}
	
	static {
		if (sIsWebStarted && System.getProperty("jna.library.path") == null) {
			String path = Native.getWebStartLibraryPath("v9t9render");
			System.out.println("Native libs at " + path);
			if (path != null)
				System.setProperty("jna.library.path", path);
		}		
	}
	
	public static URL getDataURL(String string) {
		try {
			return new URL(sBaseDataURL, string);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String modelId = getModelId(args);
		String clientId = getClientId(args);
		
		createAndRun(modelId, clientId);
	}


	public static void createAndRun(String modelId, String clientId) {
		EmulatorServer server = null;
		try {
			server = findOrCreateServer(modelId);
		} catch (IOException e) {
			System.err.println("Failed to contact or create server:" + modelId);
			e.printStackTrace();
			System.exit(23);
			return;
		}
		
		Client client = null;
		client = ClientFactory.createClient(
    			clientId, server.getMachine());

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
		String clientID;
        if (findArgument(args, "--awt")) {
        	clientID = AwtJavaClient.ID;
		} 
        else if (findArgument(args, "--swtgl")) {
        	clientID = SwtLwjglJavaClient.ID;
		} 
        else /*if (findArgument(args, "--swtawt"))*/ {
        	boolean awtRenderer = !findArgument(args, "--swt");
        	clientID = awtRenderer? SwtAwtJavaClient.ID : SwtJavaClient.ID;
		} 
        return clientID;
	}


	/**
	 * @param args
	 * @return
	 */
	private static String getModelId(String[] args) {

        String modelId = StandardMachineModel.ID;
        if (findArgument(args, "--f99b")) {
        	modelId = F99bMachineModel.ID;
        } else if (findArgument(args, "--enhanced")) {
        	modelId = EnhancedMachineModel.ID;
        } else if (findArgument(args, "--v9938")) {
        	modelId = StandardMachineV9938Model.ID;
        } else {
        	modelId = StandardMachineModel.ID;
        }
        
        return modelId;
	}


	/**
	 * @param args
	 * @return
	 * @throws IOException 
	 */
	public static EmulatorServer findOrCreateServer(String modelId) throws IOException {

		EmulatorServer server = new EmulatorServer(modelId);
		return server;
	}


}
