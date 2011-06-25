/**
 * 
 */
package v9t9.emulator.clients.builtin.awt;

import java.awt.image.BufferStrategy;

/**
 * Interface for AWT-compatible containers
 * @author ejs
 *
 */
public interface IAwtVideoRendererContainer {

	/**
	 * Let the container know the canvas has resized.
	 * @param desiredWidth
	 * @param desiredHeight
	 */
	void setDesiredScreenSize(int desiredWidth, int desiredHeight);

	/**
	 * Get the BufferStrategy for the container.
	 * @return
	 */
	BufferStrategy getBufferStrategy();

}
