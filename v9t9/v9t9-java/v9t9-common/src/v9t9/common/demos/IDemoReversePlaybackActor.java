/**
 * 
 */
package v9t9.common.demos;

import java.io.IOException;

/**
 * This interface is implemented by an actor in the emulator
 * that wants to provide reversed events for playback.
 * @author ejs
 *
 */
public interface IDemoReversePlaybackActor extends IDemoActor {
	/**
	 * Setup for demo playback.
	 */
	void setupReversePlayback(IDemoPlayer player);
	
	/**
	 * Queue an event during demo playback.  Several invocations follow per
	 * frame.  This is called before the given event is executed in the player.
	 */
	void queueEventForReversing(IDemoPlayer player, IDemoEvent event) throws IOException;

	/**
	 * Produce the reversed events from the queue and empty the queue.
	 */
	IDemoEvent[] emitReversedEvents(IDemoPlayer player) throws IOException;

	/**
	 * Clean up after demo playback.
	 */
	void cleanupReversePlayback(IDemoPlayer player);

	
}
