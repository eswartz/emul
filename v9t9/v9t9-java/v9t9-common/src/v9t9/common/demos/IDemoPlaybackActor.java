/*
  IDemoPlaybackActor.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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
