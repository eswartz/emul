/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 18, 2004
 *
 */
package v9t9.emulator.clients.builtin.swt;

import v9t9.emulator.clients.builtin.awt.AwtKeyboardHandler;
import v9t9.emulator.common.Machine;
import v9t9.engine.Client;

/**
 * This client does all its own dang work!
 * @author ejs
 */
public class SwtAwtJavaClient extends BaseSwtJavaClient implements Client {
	public static String ID = "SWTAWT";
	
    public SwtAwtJavaClient(final Machine machine) {
    	super(machine);
    }

    /* (non-Javadoc)
     * @see v9t9.engine.Client#getIdentifier()
     */
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

