/*
  SwtLwjglKeyboardHandler.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.java.games.input.Component;
import net.java.games.input.Component.Identifier;
import net.java.games.input.Component.Identifier.Button;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.ControllerEvent;
import net.java.games.input.ControllerListener;

import org.apache.log4j.Logger;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.keyboard.ControllerConfig;
import v9t9.common.keyboard.ControllerIdentifier;
import v9t9.common.keyboard.IKeyboardState;
import v9t9.common.keyboard.JoystickRole;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.SettingSchema;
import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;

/**
 * @author ejs
 *
 */
public class SwtLwjglKeyboardHandler extends SwtKeyboardHandler {
	private static final Logger logger = Logger.getLogger(SwtLwjglKeyboardHandler.class);

	static public final SettingSchema settingControllerConfig = new SettingSchema(
			ISettingsHandler.USER,
			"ControllerConfig", "");
	
	static public final SettingSchema settingJoystick1Config = new SettingSchema(
			ISettingsHandler.USER,
			"Joystick1Config", "");
	
	static public final SettingSchema settingJoystick2Config = new SettingSchema(
			ISettingsHandler.USER,
			"Joystick2Config", "");
	
	static public final SettingSchema settingJoystickRescan = new SettingSchema(
			ISettingsHandler.TRANSIENT,
			"JoystickRescan", false);
	
	static boolean DEBUG = false;
	
	private Controller[] controllers; 
	private Map<String, Controller> controllerMap = new HashMap<String, Controller>();
	
	private IProperty controllerConfig, joystick1Config, joystick2Config, joystickRescan;
	
	private List<IControllerHandler> joystick1Handlers = new ArrayList<IControllerHandler>();
	private List<IControllerHandler> joystick2Handlers = new ArrayList<IControllerHandler>();
	
	private Runnable scanTask;
	
	public SwtLwjglKeyboardHandler(final IKeyboardState keyboardState, IMachine machine) {
		super(keyboardState, machine);
		
		controllerConfig = machine.getSettings().get(settingControllerConfig);
		joystick1Config = machine.getSettings().get(settingJoystick1Config);
		joystick2Config = machine.getSettings().get(settingJoystick2Config);
		joystickRescan = machine.getSettings().get(settingJoystickRescan);
		
		updateControllers();
		
		joystick1Config.addListener(new IPropertyListener() {
			
			@Override
			public void propertyChanged(IProperty property) {
				restoreHandlers(joystick1Handlers, property.getString());
			}
		});
		
		joystick2Config.addListener(new IPropertyListener() {
			
			@Override
			public void propertyChanged(IProperty property) {
				restoreHandlers(joystick2Handlers, property.getString());
			}
		});
		
		joystickRescan.addListener(new IPropertyListener() {
			
			@Override
			public void propertyChanged(IProperty property) {
				if (property.getBoolean()) {
					joystick1Config.setString("");
					joystick2Config.setString("");
					controllerConfig.setString("");
					updateControllers();
					property.setBoolean(false);
				}
			}
		});
		
		scanTask = new Runnable() {
			@Override
			public void run() {
				for (Controller controller : controllers) {
					controller.poll();
				}
				scanJoystick(keyboardState, joystick1Handlers, 1);
				scanJoystick(keyboardState, joystick2Handlers, 2);
			}
		};
		machine.getFastMachineTimer().scheduleTask(scanTask, 10);
		
		ControllerEnvironment.getDefaultEnvironment().addControllerListener(new ControllerListener() {
			
			@Override
			public void controllerRemoved(ControllerEvent arg0) {
			}
			
			@Override
			public void controllerAdded(ControllerEvent arg0) {
			}
		});
	}
	
	public static Map<Controller, Map<ControllerIdentifier, Component>> getSupportedControllerComponents() {
		Map<Controller, Map<ControllerIdentifier, Component>> cmap = new HashMap<Controller, Map<ControllerIdentifier, Component>>();
		 
		for (Controller controller : ControllerEnvironment.getDefaultEnvironment().getControllers()) {
			if (controller.getComponent(Identifier.Axis.X) == null || 
					controller.getComponent(Identifier.Axis.Y) == null ||
					controller.getType() == Controller.Type.MOUSE)
				continue;

			cmap.put(controller, fetchComponents(controller));
		}
		return cmap;
	}


	/**
	 * Get a mapping of available components
	 * @param controller
	 */
	private static Map<ControllerIdentifier, Component> fetchComponents(Controller controller) {
		Map<ControllerIdentifier, Component> map = new LinkedHashMap<ControllerIdentifier, Component>();
		
		String controllerName = controller.getName();
		int index = 0;
		for (Component c : controller.getComponents()) {
			Identifier cid = c.getIdentifier();
			boolean willUse = false; 
			if (cid == Identifier.Axis.X || cid == Identifier.Axis.RX || cid == Identifier.Axis.Z) {
				willUse = true;
			} 
			else if (cid == Identifier.Axis.Y || cid == Identifier.Axis.RY || cid == Identifier.Axis.RZ) {
				willUse = true;
			}
			else if (cid == Identifier.Axis.POV) {
				willUse = true;
			}
			else if (cid instanceof Button) {
				willUse = true;
			}

			if (willUse) {
				String name = c.getName();
				
				map.put(new ControllerIdentifier(controllerName, index, name),
						c);
			}
			
			index++;
		}
		
		return map;
	}

	
	/**
	 * Rescan the connected controllers, and reestablish mappings
	 * from user selections or pick mappings from scratch.
	 */
	private synchronized void updateControllers() {
		Map<Controller, Map<ControllerIdentifier, Component>> cmap = getSupportedControllerComponents();
		
		controllers = cmap.keySet().toArray(new Controller[cmap.size()]);
		controllerMap.clear();
		
		ControllerConfig jsconfig1 = null;
		Map<ControllerIdentifier, Component> js1Unused = null;
		ControllerConfig jsconfig2 = null;
		Map<ControllerIdentifier, Component> js2Unused = null;
		
		// find valid controllers
		StringBuilder sb = new StringBuilder();
		
		for (Controller controller : controllers) {
			String name = controller.getName();
			System.out.println("Using controller: " + name);
			controllerMap.put(name, controller);
			
			if (jsconfig1 == null) {
				// try to use what's available in the first controller for joystick #1
				Map<ControllerIdentifier, Component> comps = new HashMap<ControllerIdentifier, Component>(cmap.get(controller));

				jsconfig1 = initializeConfig(1, comps);
				
				js1Unused = comps;
				
			} else if (jsconfig2 == null) {
				// for the second joystick, try to use the new controller
				Map<ControllerIdentifier, Component> comps = new HashMap<ControllerIdentifier, Component>(cmap.get(controller));
				
				jsconfig2 = initializeConfig(2, comps);
				
				js2Unused = comps;
			}
			
			sb.append(name).append('\n');
		}
		
		if (jsconfig1 != null && jsconfig2 == null) {
			// nothing worked for joystick #2, use what's left over from the first controller
			jsconfig2 = initializeConfig(2, js1Unused);
		}
		
		if (jsconfig1 != null) {
			// record all the remaining buttons to joy #1
			for (Entry<ControllerIdentifier, Component> ent : js1Unused.entrySet()) {
				if (ent.getValue().getIdentifier() instanceof Identifier.Button) {
					jsconfig1.map(ent.getKey(), JoystickRole.BUTTON);
				}
			}
		}
		if (jsconfig2 != null) {
			// record all the remaining buttons to joy #1
			for (Entry<ControllerIdentifier, Component> ent : js2Unused.entrySet()) {
				if (ent.getValue().getIdentifier() instanceof Identifier.Button) {
					jsconfig2.map(ent.getKey(), JoystickRole.BUTTON);
				}
			}
		}

		// all that scanning is used only if the previously saved
		// configurations no longer apply
		String currentConfig = sb.toString().trim();
		String oldConfig = controllerConfig.getString().trim(); 
		
		if (!currentConfig.equals(oldConfig) || !restoreHandlers(joystick1Handlers, joystick1Config.getString())) {
			if (jsconfig1 != null) {
				joystick1Config.setString(jsconfig1.toString());
				restoreHandlers(joystick1Handlers, joystick1Config.getString());
			}
		}
		
		if (!currentConfig.equals(oldConfig) || !restoreHandlers(joystick2Handlers, joystick2Config.getString())) {
			if (jsconfig2 != null) {
				joystick2Config.setString(jsconfig2.toString());
				restoreHandlers(joystick2Handlers, joystick2Config.getString());
			}
		}
		
		controllerConfig.setString(currentConfig);
	}

	/**
	 * Initialize configurations for joysticks by picking and removing likely
	 * candidates from the unusedComponents.
	 */
	private ControllerConfig initializeConfig(int joy,
			Map<ControllerIdentifier, Component> unusedComponents) {

		ControllerConfig config = new ControllerConfig();
		
		boolean foundX = false;
		boolean foundY = false;
		boolean foundButton = false;
		
		for (Iterator<Map.Entry<ControllerIdentifier, Component>> it = unusedComponents.entrySet().iterator();
				it.hasNext(); ) {
			Map.Entry<ControllerIdentifier, Component> ent = it.next();
			ControllerIdentifier id = ent.getKey();
			Identifier cid = ent.getValue().getIdentifier();
			
			if (cid == Identifier.Axis.X
					|| (joy > 1 && (cid == Identifier.Axis.RX || cid == Identifier.Axis.Z))) {
				config.map(id, JoystickRole.X_AXIS);
				foundX = true;
				it.remove();
			}
			else if (cid == Identifier.Axis.Y 
					|| (joy > 1 && (cid == Identifier.Axis.RY || cid == Identifier.Axis.RZ))) {
				config.map(id, JoystickRole.Y_AXIS);
				foundY = true;
				it.remove();
			}
			else if (cid == Identifier.Axis.POV) {
				config.map(id, JoystickRole.DIRECTIONAL);
				foundX = true;
				foundY = true;
				it.remove();
			}
			else if (ent.getValue().getIdentifier() instanceof Button) {
				if (isButtonFor(ent.getValue(), joy) || !foundButton) {
					config.map(id, JoystickRole.BUTTON);
					foundButton = true;
					it.remove();
				}
			}
		}
		
		if (!foundX || !foundY || !foundButton) {
			logger.warn("Failed to discover X, Y, and button for joystick #" + joy);
			return null;
		}
		
		return config;
	
	}

	protected boolean isButtonFor(Component button, int joy) {
		String name = button.getIdentifier().getName();
		return name.toLowerCase().matches(".*(trigger|thumb|" + (joy == 1 ? "left" : "right|2") + ").*");
	}
	
	/**
	 * Try to restore controller handlers from a config 
	 */
	private boolean restoreHandlers(List<IControllerHandler> handlers, String text) {
		
		if (DEBUG)
			System.out.println("Restoring handlers from:\n\n" + text);
		
		ControllerConfig config = new ControllerConfig();
		try {
			config.fromString(text);
			
			if (config.getMap().isEmpty()) {
				// new config
				return false;
			}
			
			createHandlers(handlers, config);
			
			return !handlers.isEmpty();
			
		} catch (ControllerConfig.ParseException e) {
			// ignore
			logger.error(e.getMessage());
			return false;
		}
	}
	
	/**
	 * Create handlers from the configuration
	 * @param handlers
	 * @param config
	 */
	private void createHandlers(List<IControllerHandler> handlers, ControllerConfig config) {

		handlers.clear();
		
		// map the configuration to the components desired
		for (Map.Entry<ControllerIdentifier, JoystickRole> ent : config.getMap().entrySet()) {
			ControllerIdentifier id = ent.getKey();
			Controller controller = controllerMap.get(id.controllerName);
			if (controller == null) {
				logger.warn("did not find controller for incoming " + id.controllerName);
				continue;
			}
			
			Component[] components = controller.getComponents();
			
			Component component = null;
			
			// find the matching name, and hopefully the index
			Component cand = null;
			int index = 0;
			for (Component c : components) {
				if (c.getName().equals(id.name)) { 
					cand = c;
					if (index == id.index) {
						component = cand;
						break;
					}
				}
				index++;
			}
			if (cand != null && component == null) {
				component = cand;
				logger.warn("found component but not at expected index for '" + id + "' in " + id.controllerName);
			}
			else if (component == null) {
				logger.warn("did not find for '" + id + "' in " + id.controllerName);
				continue;
			}
			
			handlers.add(new SimpleControllerHandler(controller, component, ent.getValue()));
		}
	}
	
	/**
	 * @param joystickHandler
	 * @param i
	 */
	private void scanJoystick(IKeyboardState state, List<IControllerHandler> joystickHandlers, int joy) {
		IControllerHandler.State cstate = new IControllerHandler.State();
		
		for (IControllerHandler joystickHandler : joystickHandlers) {
			
			joystickHandler.setFailedLast(false);
			joystickHandler.setJoystick(joy, cstate);
		}
		
		state.setJoystick(joy, IKeyboardState.JOY_X + IKeyboardState.JOY_Y + IKeyboardState.JOY_B, 
				cstate.x, cstate.y, cstate.button);
	}

}
