/*
  BaseDemoActor.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.demos.actors;

import v9t9.common.demos.IDemoPlaybackActor;
import v9t9.common.demos.IDemoPlayer;
import v9t9.common.demos.IDemoRecordingActor;

/**
 * @author ejs
 *
 */
public abstract class BaseDemoActor implements IDemoRecordingActor, IDemoPlaybackActor {

	/* (non-Javadoc)
	 * @see v9t9.common.demos.IDemoActor#shouldRecordFor(byte[])
	 */
	@Override
	public boolean shouldRecordFor(byte[] header) {
		return true;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#setupPlayback(v9t9.common.demo.IDemoPlayer)
	 */
	@Override
	public void setupPlayback(IDemoPlayer player) {

	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#cleanupPlayback(v9t9.common.demo.IDemoPlayer)
	 */
	@Override
	public void cleanupPlayback(IDemoPlayer player) {

	}
}
