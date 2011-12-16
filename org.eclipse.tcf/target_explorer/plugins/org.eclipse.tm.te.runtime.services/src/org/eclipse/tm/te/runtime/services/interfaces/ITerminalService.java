/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.runtime.services.interfaces;

import org.eclipse.tm.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer;

/**
 * Terminals service.
 * <p>
 * Allow to use the embedded terminals view for remote input and output.
 */
public interface ITerminalService extends IService {

	/**
	 * Opens a terminal asynchronously and invokes the given callback if done.
	 *
	 * @param properties The terminal properties. Must be not <code>null</code>.
	 * @param callback The target callback to invoke if finished or <code>null</code>.
	 */
	public void openConsole(IPropertiesContainer properties, ICallback callback);

	/**
	 * Close the terminal asynchronously and invokes the given callback if done.
	 *
	 * @param properties The terminal properties. Must be not <code>null</code>.
	 * @param callback The target callback to invoke if finished or <code>null</code>.
	 */
	public void closeConsole(IPropertiesContainer properties, ICallback callback);
}
