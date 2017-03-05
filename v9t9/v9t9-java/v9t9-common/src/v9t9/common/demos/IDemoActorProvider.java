/*
  IDemoActorProvider.java

  (c) 2012-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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