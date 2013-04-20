/*
  EmulatorGuiData.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui;

import java.io.IOException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;

import v9t9.common.EmulatorLocations;


/**
 * @author Ed
 *
 */
public class EmulatorGuiData {
	private static final Logger logger = Logger.getLogger(EmulatorGuiData.class);
	
	static final URL sBaseV9t9URL;
	static {
		sBaseV9t9URL = EmulatorLocations.getV9t9BaseURL(EmulatorGuiData.class);
		if (sBaseV9t9URL == null) {
			System.err.println("Could not find base (install) directory");
			System.exit(123);
		}
		logger.info("Base emulator V9t9 data URL: " + sBaseV9t9URL);
		//System.out.println("Base emulator V9t9 data URL: " + sBaseV9t9URL);
	}
	
	public static URL getDataURL(String string) {
		return EmulatorGuiData.class.getClassLoader().getResource(string);
	}
	
	public static Image loadImage(Device device, String path) {
		URL iconFile = getDataURL(path);
		if (iconFile != null) {
			try {
				Image icon = new Image(device, iconFile.openStream());
				return icon;
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (Throwable e1) {
				// try again -- SWT bug
				try {
					Image icon = new Image(device, iconFile.openStream());
					return icon;
				} catch (Throwable e2) {
					e1.printStackTrace();
				}
			}
		}
		return new Image(device, 1, 1);
	}
	
}
