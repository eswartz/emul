/**
 * 
 */
package v9t9.common.demos;

import java.io.IOException;

import v9t9.common.machine.IMachine;

/**
 * This interface is implemented by an actor in the emulator
 * that wants to contribute and consume events for a demo.
 * @author ejs
 *
 */
public interface IDemoActor {
	/**
	 * Get the identifier for the {@link IDemoEvent#getIdentifier()}
	 * created and consumed by this actor.
	 * @return
	 */
	String getEventIdentifier();
	
	/**
	 * Set up the actor for this machine
	 */
	void setup(IMachine machine);

	/**
	 * Tell whether this actor should be enabled for recording.
	 */
	boolean shouldRecordFor(byte[] header);
	
	/**
	 * Connect to a machine to record the events relevant to this actor.
	 * Also, send any initialization events.
	 * @param machine
	 * @throws IOException 
	 */
	void connectForRecording(IDemoRecorder recorder) throws IOException;
	
	/**
	 * Flush recorded events to buffer (called periodically)
	 */
	void flushRecording(IDemoRecorder recorder) throws IOException;
	
	/**
	 * Disconnect from a machine (undo effects of {@link #connectForRecording(IMachine)})
	 * @param machine
	 */
	void disconnectFromRecording(IDemoRecorder recorder);

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
