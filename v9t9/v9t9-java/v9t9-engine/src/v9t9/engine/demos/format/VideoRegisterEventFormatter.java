/*
  VideoRegisterEventFormatter.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.demos.format;

import v9t9.engine.demos.events.VideoWriteRegisterEvent;

/**
 * @author ejs
 *
 */
public class VideoRegisterEventFormatter extends RegisterEventFormatter {

	/**
	 * @param bufferId
	 */
	public VideoRegisterEventFormatter(String bufferId) {
		super(bufferId, VideoWriteRegisterEvent.ID);
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.demos.format.RegisterEventFormatter#createRegisterEvent(int, int)
	 */
	@Override
	protected VideoWriteRegisterEvent createRegisterEvent(int reg, int val) {
		return new VideoWriteRegisterEvent(reg, val);
	}

}
