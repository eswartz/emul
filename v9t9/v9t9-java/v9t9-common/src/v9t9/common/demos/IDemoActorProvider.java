/**
 * 
 */
package v9t9.common.demos;

/**
 * Create a demo actor
 * @author ejs
 *
 */
public interface IDemoActorProvider {
	boolean FORWARD = true;
	boolean BACKWARD = false;

	/**
	 * Create the actor for playback  
	 */
	IDemoPlaybackActor createForPlayback();
	
	/**
	 * Create the actor for recording  
	 */
	IDemoRecordingActor createForRecording();
	
	/**
	 * Create the actor for reverse playback -- which is invoked
	 * as a recorder that emits events 
	 */
	IDemoReversePlaybackActor createForReversePlayback();
	
	/**
	 * Get the identifier for the {@link IDemoEvent#getIdentifier()}
	 * created and consumed by this actor.
	 * @return
	 */
	String getEventIdentifier();
}