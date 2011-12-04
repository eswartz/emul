/*
 * (c) Ed Swartz, 2011
 *
 */
package v9t9.gui.client.swt;

import v9t9.common.client.IClient;
import v9t9.engine.machine.MachineBase;

/**
 * @author ejs
 */
public class SwtLwjglJavaClient extends BaseSwtJavaClient implements IClient {
	public static String ID = "SWTLWJGL";
	
    public SwtLwjglJavaClient(final MachineBase machine) {
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
		videoRenderer = new SwtLwjglVideoRenderer(machine.getVdp());
	
		keyboardHandler = new SwtLwjglKeyboardHandler(
				machine.getKeyboardState(), machine);
	}

}

