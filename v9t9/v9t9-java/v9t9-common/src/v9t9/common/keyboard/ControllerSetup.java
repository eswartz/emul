/*
  ControllerSetup.java

  (c) 2017 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.keyboard;

import ejs.base.utils.TextUtils;


/**
 * This represents the configuration of one or more controllers, which
 * were connected at the same time.
 * @author ejs
 *
 */
public class ControllerSetup {

	public String controllerNames = "";
	public ControllerConfig joystick1 = new ControllerConfig();
	public ControllerConfig joystick2 = new ControllerConfig();

	public String toString() {
		return controllerNames 
				+ "||"
				+ joystick1.toString()
				+ "||"
				+ joystick2.toString();
	}
	
	public void fromString(String str) throws ControllerConfig.ParseException {
		controllerNames = "";
		joystick1 = new ControllerConfig();
		joystick2 = new ControllerConfig();

		if (TextUtils.isEmpty(str)) {
			return;
		}
		
		String[] parts = str.split("\\|\\|");
		if (parts.length < 3) {
			throw new ControllerConfig.ParseException(0, "expected at least three segments");
		}
			
		controllerNames = parts[0].trim();
		joystick1.clear();
		joystick2.clear();
		
		joystick1.fromString(parts[1]);
		joystick2.fromString(parts[2]);

	}
}
