/*
  IDemoReversePlaybackActor.java

  (c) 2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
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
