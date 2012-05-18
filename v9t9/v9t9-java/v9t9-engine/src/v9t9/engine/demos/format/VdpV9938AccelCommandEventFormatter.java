/**
 * 
 */
package v9t9.engine.demos.format;

import java.io.IOException;

import v9t9.common.demos.IDemoEvent;
import v9t9.common.demos.IDemoInputEventBuffer;
import v9t9.common.demos.IDemoOutputEventBuffer;
import v9t9.engine.demos.events.VdpV9938AccelCommandEvent;

/**
 * @author ejs
 *
 */
public class VdpV9938AccelCommandEventFormatter extends BaseEventFormatter {

	public VdpV9938AccelCommandEventFormatter(String bufferId) {
		super(bufferId, VdpV9938AccelCommandEvent.ID);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoEventFormatter#readEvent(v9t9.common.demo.IDemoInputEventBuffer)
	 */
	@Override
	public IDemoEvent readEvent(IDemoInputEventBuffer buffer)
			throws IOException {
		int code = buffer.read();
		return new VdpV9938AccelCommandEvent(code);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoEventFormatter#writeEvent(v9t9.common.demo.IDemoOutputEventBuffer, v9t9.common.demo.IDemoEvent)
	 */
	@Override
	public void writeEvent(IDemoOutputEventBuffer buffer, IDemoEvent event)
			throws IOException {
		buffer.push((byte) ((VdpV9938AccelCommandEvent) event).getCode());
	}

}
