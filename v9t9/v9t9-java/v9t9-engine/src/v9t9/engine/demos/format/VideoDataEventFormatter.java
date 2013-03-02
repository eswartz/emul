/*
  VideoDataEventFormatter.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.demos.format;

import v9t9.common.demos.IDemoEvent;
import v9t9.engine.demos.events.VideoWriteDataEvent;

/**
 * @author ejs
 *
 */
public class VideoDataEventFormatter extends DataEventFormatter {

	/**
	 * @param bufferId
	 * @param eventId
	 */
	public VideoDataEventFormatter(String bufferId) {
		super(bufferId, VideoWriteDataEvent.ID);
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.demos.format.DataEventFormatter#createWriteDataEvent(int, byte[])
	 */
	@Override
	protected IDemoEvent createWriteDataEvent(int addr, byte[] data) {
		return new VideoWriteDataEvent(addr, data);
	}

}
