/**
 * 
 */
package v9t9.gui;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;

import v9t9.common.client.IClient;
import v9t9.gui.client.ClientFactory;
import v9t9.gui.client.awt.AwtJavaClient;
import v9t9.gui.client.swt.SwtAwtJavaClient;
import v9t9.gui.client.swt.SwtJavaClient;
import v9t9.gui.client.swt.SwtLwjglJavaClient;
import v9t9.server.EmulatorServer;

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
		ClientFactory.register(AwtJavaClient.ID, AwtJavaClient.class);
		ClientFactory.register(SwtLwjglJavaClient.ID, SwtLwjglJavaClient.class);
	}
	
 	private static boolean findArgument(String[] args, String string) {
    	for (String arg : args)
    		if (arg.equals(string))
    			return true;
		return false;
	}
 	
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
					burl = new URL(burlString.substring(0, burlString.indexOf(Emulator.class.getName().replace(".", "/"))));
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
					path = new URL(sBaseV9t9URL, "../libv9t9render").getPath();
					System.out.println("Native libs at " + path);
					if (path != null)
						System.setProperty("jna.library.path", path);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
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
		EmulatorServer server = new EmulatorServer();
		
		String modelId = getModelId(server, args);
		String clientId = getClientId(args);
		

		createAndRun(server, modelId, clientId);
	}


	public static void createAndRun(EmulatorServer server, String modelId, String clientId) {
		try {
			server.init(modelId);
		} catch (IOException e) {
			System.err.println("Failed to contact or create server:" + modelId);
			e.printStackTrace();
			System.exit(23);
			return;
		}
		
		IClient client = null;
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
	 * @param server 
	 * @param args
	 * @return
	 */
	private static String getModelId(EmulatorServer server, String[] args) {

		Collection<String> models = server.getMachineModelFactory().getRegisteredModels();
		for (String arg : args) {
			if (models.contains(arg))
				return arg;
		}
        
        return server.getMachineModelFactory().getDefaultModel();
	}

}
