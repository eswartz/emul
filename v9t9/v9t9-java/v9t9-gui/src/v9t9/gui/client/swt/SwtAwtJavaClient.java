/*
  SwtAwtJavaClient.java

  (c) 2008-2017 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt;

import v9t9.gui.client.awt.AwtKeyboardHandler;
import v9t9.server.client.EmulatorServerBase;

/**
 * This client does all its own dang work!
 * @author ejs
 */
public class SwtAwtJavaClient extends BaseSwtJavaClient {
	public static String ID = "SWTAWT";
	
    public SwtAwtJavaClient(EmulatorServerBase server) {
    	super(server);
    }

    @Override
    public String getIdentifier() {
    	return ID;
    }
	/**
	 * 
	 */
	protected void setupRenderer() {
		videoRenderer = new SwtAwtVideoRenderer(machine);
	
		keyboardHandler = new AwtKeyboardHandler(
    		machine.getKeyboardState(), machine);
	}
}

