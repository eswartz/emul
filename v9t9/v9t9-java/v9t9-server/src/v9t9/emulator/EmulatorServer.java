/*
 * (c) Ed Swartz, 2008
 * 
 * Created on Feb 19, 2006
 *
 */
package v9t9.emulator;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.ejs.coffee.core.properties.IProperty;
import org.ejs.coffee.core.properties.IPropertyListener;
import org.ejs.coffee.core.utils.Check;

import v9t9.emulator.runtime.compiler.CompilerBase;
import v9t9.emulator.clients.builtin.NotifyException;
import v9t9.emulator.common.EmulatorSettings;
import v9t9.emulator.common.IEventNotifier;
import v9t9.emulator.common.IMachine;
import v9t9.emulator.common.ModuleManager;
import v9t9.emulator.common.WorkspaceSettings;
import v9t9.emulator.hardware.MachineModel;
import v9t9.emulator.hardware.MachineModelFactory;
import v9t9.emulator.runtime.cpu.Cpu;
import v9t9.emulator.runtime.cpu.Executor;
import v9t9.engine.Client;
import v9t9.engine.files.DataFiles;
import v9t9.engine.memory.Memory;
import v9t9.engine.memory.MemoryModel;

public class EmulatorServer {

	private static final boolean sIsDevBuild;
	
	private static final URL sBaseV9t9URL;
	private static final URL sBaseDataURL;
	static {
		URL url = EmulatorServer.class.getClassLoader().getResource(".");
		URL burl = EmulatorServer.class.getClassLoader().getResource(
				EmulatorServer.class.getName().replace(".", "/") + ".class");
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
		DataFiles.settingBootRomsPath.addListener(new IPropertyListener() {
			
			@Override
			public void propertyChanged(IProperty setting) {
				if (setting.getList().isEmpty())
					addDefaultPaths();
			}

		});
		
		addDefaultPaths();
	}
	
	private static void addDefaultPaths() {
		if (sIsDevBuild) {
			DataFiles.addSearchPath("../../build/roms");
		}
	}


	private Memory memory;
	private IMachine machine;
	private MemoryModel memoryModel;
	private Client client;
	private boolean inited;

    public static URL getDataURL(String string) {
		try {
			return new URL(sBaseDataURL, string);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public EmulatorServer(String modelId) throws IOException {
    	Check.checkArg(modelId);
    	init(modelId);
    	this.memory = machine.getMemory();
    	this.memoryModel = memory.getModel();
    }
    
    /**
	 * @return the machine
	 */
	public IMachine getMachine() {
		return machine;
	}
	protected void setupDefaults() {
    	WorkspaceSettings.CURRENT.register(IMachine.settingModuleList);
		WorkspaceSettings.CURRENT.register(Cpu.settingCyclesPerSecond);
		WorkspaceSettings.CURRENT.register(Cpu.settingRealTime);
		
		try {
			WorkspaceSettings.CURRENT.setDirty(false);
			WorkspaceSettings.loadFrom("workspace." + machine.getModel().getIdentifier());
		} catch (IOException e) {
		}
		
    	Cpu.settingRealTime.setBoolean(true);
    	
    	// compile?
    	if (true) {
    		//Cpu.settingRealTime.setBoolean(false);
	    	Executor.settingCompile.setBoolean(true);
	    	//Compiler.settingDebugInstructions.setBoolean(true);
	    	//Compiler.settingOptimize.setBoolean(true);
	        CompilerBase.settingOptimizeRegAccess.setBoolean(true);
	        CompilerBase.settingOptimizeStatus.setBoolean(true);
	        //Compiler.settingCompileOptimizeCallsWithData.setBoolean(true);
	        //Compiler.settingCompileFunctions.setBoolean(true);
    	}
    }
    
	protected void loadState() {
		int barrier = client.getEventNotifier().getErrorCount();
		memoryModel.loadMemory(client.getEventNotifier());
		
		if (machine.getModuleManager() != null) {
			try {
	    		String dbNameList = IMachine.settingModuleList.getString();
	    		if (dbNameList.length() > 0) {
	    			String[] dbNames = dbNameList.split(";");
	    			machine.getModuleManager().loadModules(dbNames, client.getEventNotifier());
	    		}
	        	if (ModuleManager.settingLastLoadedModule.getString().length() > 0)
	        		machine.getModuleManager().switchModule(ModuleManager.settingLastLoadedModule.getString());
	        	
	        } catch (NotifyException e) {
	        	machine.notifyEvent(e.getEvent());
	        }
	        
		}
		
		if (client.getEventNotifier().getErrorCount() > barrier) {
			machine.notifyEvent(IEventNotifier.Level.ERROR,
					"Failed to load startup ROMs; please edit your " + DataFiles.settingBootRomsPath.getName() + " in the file "
					+ WorkspaceSettings.CURRENT.getConfigFilePath());
			//EmulatorSettings.INSTANCE.save();
		}
	}
	
    public void init(String modelId) throws IOException {
    	
    	try {
    		EmulatorSettings.INSTANCE.load();
    	} catch (IOException e) {
    		System.err.println("Setting up new instance");
    	}

		WorkspaceSettings.CURRENT.register(ModuleManager.settingLastLoadedModule);

		EmulatorSettings.INSTANCE.register(DataFiles.settingBootRomsPath,
				EmulatorSettings.INSTANCE.getConfigDirectory() + "roms");
		EmulatorSettings.INSTANCE.register(DataFiles.settingStoredRamPath,
				EmulatorSettings.INSTANCE.getConfigDirectory() + "module_ram");
		DataFiles.addSearchPath(DataFiles.settingStoredRamPath.getString());
    	
    	try {
    		WorkspaceSettings.loadFrom(WorkspaceSettings.currentWorkspace.getString());
    	} catch (IOException e) {
    		System.err.println("Setting up new configuration");
    	}
    	
        MachineModel model = MachineModelFactory.createModel(modelId);
        assert (model != null);
        
        machine = model.createMachine();
    }

	public void setClient(Client client) {
		this.client = client;
		machine.setClient(client);
		
	}

	/**
	 * @throws IOException 
	 * 
	 */
	public void dispose() throws IOException {
		machine.getMemory().save();
		
		WorkspaceSettings.CURRENT.save();        	
    	EmulatorSettings.INSTANCE.save();   
	}

	public void run() {
		
		if (!inited) {
			inited = true;
	        setupDefaults();
	        loadState();
	        
		}
		machine.getCpu().reset();
        machine.start();
        
        while (client.isAlive()) {
        	client.handleEvents();
	    	
	    	if (!machine.isAlive())
	    		break;
	    	
	    	// don't eat up CPU
	    	try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
        }
	}

}

