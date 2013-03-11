/*
  IFileExecutor.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.files;

import v9t9.common.events.NotifyException;
import v9t9.common.machine.IMachine;

/**
 * @author ejs
 *
 */
public interface IFileExecutor {

	/**
	 * @return
	 */
	String getLabel();

	String getDescription();
	
	void run(IMachine machine) throws NotifyException;
}
