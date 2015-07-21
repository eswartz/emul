/*
  IDeviceSelectorDialog.java

  (c) 2015 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells.disk;

import v9t9.common.machine.IMachine;

/**
 * @author ejs
 *
 */
public interface IDeviceSelectorDialog {
	void warnResetNeeded();
	IMachine getMachine();
	
	EntryHistory getEntryHistory();
}
