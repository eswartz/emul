/*
 * (c) Ed Swartz, 2011
 *
 */
package v9t9.emulator.clients.builtin.swt;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;

import v9t9.emulator.clients.builtin.video.ImageDataCanvas;
import v9t9.emulator.common.Machine;
import v9t9.engine.Client;

/**
 * @author ejs
 */
public class SwtLwjglJavaClient extends BaseSwtJavaClient implements Client {
	public static String ID = "SWTLWJGL";
	
    public SwtLwjglJavaClient(final Machine machine) {
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
		videoRenderer = new SwtLwjglVideoRenderer();
	
		keyboardHandler = new SwtKeyboardHandler(
				machine.getKeyboardState(), machine);
	}

}

