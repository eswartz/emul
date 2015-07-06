/*
  MachineRegistry.java

  (c) 2014 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.machine;

import java.util.ArrayList;
import java.util.List;

import ejs.base.utils.ListenerList;
import ejs.base.utils.ListenerList.IFire;

/**
 * @author ejs
 *
 */
public class MachineRegistry {
	public static MachineRegistry INSTANCE = new MachineRegistry();

	private ListenerList<IMachineRegistryListener> listeners = new ListenerList<IMachineRegistryListener>();
	private List<IMachine> machines = new ArrayList<IMachine>(1);
	
	private MachineRegistry() { }
	
	public void addListener(IMachineRegistryListener listener) {
		listeners.add(listener);
	}
	public void removeListener(IMachineRegistryListener listener) {
		listeners.remove(listener);
	}
	/**
	 * Add a running machine.  (A machine itself should invoke this.)
	 */
	public synchronized void addMachine(final IMachine machine) {
		if (!machines.contains(machine)) {
			machines.add(machine);
			
			listeners.fire(new IFire<IMachineRegistryListener>() {

				@Override
				public void fire(IMachineRegistryListener listener) {
					listener.machineAdded(machine);
				}
			});
		}
	}
	/**
	 * Remove a machine.  (A machine itself should invoke this.)
	 */
	public synchronized void removeMachine(final IMachine machine) {
		if (machines.remove(machine)) {
			
			listeners.fire(new IFire<IMachineRegistryListener>() {

				@Override
				public void fire(IMachineRegistryListener listener) {
					listener.machineRemoved(machine);
				}
			});
		}
	}
}
