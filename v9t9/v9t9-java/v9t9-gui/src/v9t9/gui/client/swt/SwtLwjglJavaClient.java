/*
 * (c) Ed Swartz, 2011
 *
 */
package v9t9.gui.client.swt;

import v9t9.common.client.IClient;
import v9t9.common.machine.IMachine;

/**
 * @author ejs
 */
public class SwtLwjglJavaClient extends BaseSwtJavaClient implements IClient {
	public static String ID = "SWTLWJGL";
	
    public SwtLwjglJavaClient(IMachine machine) {
    	super(machine);
    }

    @Override
    public String getIdentifier() {
    	return ID;
    }
	/**
	 * 
	 */
	protected void setupRenderer() {
		videoRenderer = new SwtLwjglVideoRenderer(settingsHandler, machine.getVdp());
	
		keyboardHandler = new SwtLwjglKeyboardHandler(
				machine.getKeyboardState(), machine);
	}

}

