/*
  SwtLwjglJavaClient.java

  (c) 2008-2017 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt;

import v9t9.server.client.EmulatorServerBase;

/**
 * @author ejs
 */
public class SwtLwjglJavaClient extends BaseSwtJavaClient {
	public static String ID = "SWTLWJGL";
	
    public SwtLwjglJavaClient(EmulatorServerBase server) {
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
		videoRenderer = new SwtLwjglVideoRenderer(machine);
	
		keyboardHandler = new SwtLwjglKeyboardHandler(machine.getKeyboardState(),
				machine);
	}

}

