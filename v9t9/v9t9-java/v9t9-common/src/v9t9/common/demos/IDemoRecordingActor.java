/**
 * 
 */
package v9t9.common.demos;

import java.io.IOException;

import v9t9.common.machine.IMachine;

/**
 * This interface is implemented by an actor in the emulator
 * that wants to contribute events for a demo.
 * @author ejs
 *
 */
public interface IDemoRecordingActor extends IDemoActor {
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
	
}
