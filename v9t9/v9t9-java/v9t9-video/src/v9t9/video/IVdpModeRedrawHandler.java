/*
  IVdpModeRedrawHandler.java

  (c) 2011-2014 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.video;

/**
 * @author ejs
 *
 */
public interface IVdpModeRedrawHandler {

	int getCharsPerRow();
	/**
	 * Record that the VDP memory at addr was changed.
	 * @param addr
	 * @return true if change will be visible on-screen
	 */
	boolean touch(int addr);
	/**
	 * Update the changed blocks (on the screen) according to relationships
	 * between the various update areas.
	 */
	void prepareUpdate();

}