/**
 * 
 */
package v9t9.common.demos;

import java.io.IOException;

/**
 * This interface is implemented by an actor in the emulator
 * that wants to reproduce events for a demo.
 * @author ejs
 *
 */
public interface IDemoPlaybackActor extends IDemoActor {
	/**
	 * Setup for demo playback.
	 */
	void setupPlayback(IDemoPlayer player);
	
	/**
	 * Execute an event during demo playback.
	 */
	void executeEvent(IDemoPlayer player, IDemoEvent event) throws IOException;

	/**
	 * Clean up after demo playback.
	 */
	void cleanupPlayback(IDemoPlayer player);

	
}
