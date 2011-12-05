/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 18, 2004
 *
 */
package v9t9.gui.client.swt;

import v9t9.common.client.IClient;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.machine.IMachine;
import v9t9.gui.client.awt.AwtKeyboardHandler;

/**
 * This client does all its own dang work!
 * @author ejs
 */
public class SwtAwtJavaClient extends BaseSwtJavaClient implements IClient {
	public static String ID = "SWTAWT";
	
    public SwtAwtJavaClient(ISettingsHandler settingsHandler, IMachine machine) {
    	super(settingsHandler, machine);
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
    		((SwtAwtVideoRenderer)videoRenderer).getAwtCanvas(),
    		machine.getKeyboardState(), machine);
	}
    
}

