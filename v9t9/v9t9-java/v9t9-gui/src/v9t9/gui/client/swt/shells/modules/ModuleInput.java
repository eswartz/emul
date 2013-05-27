/*
  ModuleInput.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells.modules;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

import v9t9.common.modules.IModule;

/**
 * @author ejs
 *
 */
class ModuleInput {

	/**
	 * @param string
	 * @param moduleMap
	 */
	public ModuleInput(String string, Map<URI, Collection<IModule>> moduleMap) {
		noModule = string;
		modDb = moduleMap;
	}
	public String noModule;
	public Map<URI, Collection<IModule>> modDb;

}