/*
  IDsrHandler.java

  (c) 2008-2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.dsr;

import java.util.List;

import ejs.base.properties.IPersistable;



/**
 * Java code that handles the work of a device service handler.
 * @author ejs
 *
 */
public interface IDsrHandler extends IPersistable, IDeviceSettings {

	String GROUP_DSR_SELECTION = "Device Selection";
	String GROUP_REAL_DISK_CONFIGURATION = "Disk Image Configuration";
	String GROUP_EMU_DISK_CONFIGURATION = "Disk Directory Configuration";
	String GROUP_RS232_CONFIGURATION = "RS232 (Serial) Configuration";
	String GROUP_PIO_CONFIGURATION = "PIO (Parallel Port) Configuration";

	void init();
	void dispose();

	String getName();
	
	List<IDeviceIndicatorProvider> getDeviceIndicatorProviders();
}
