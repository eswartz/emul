/**
 * 
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
