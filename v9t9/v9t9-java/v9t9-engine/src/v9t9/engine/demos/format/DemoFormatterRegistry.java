/**
 * 
 */
package v9t9.engine.demos.format;

import java.util.HashMap;
import java.util.Map;

import v9t9.common.demos.IDemoEventFormatter;

/**
 * @author ejs
 *
 */
public class DemoFormatterRegistry implements IDemoFormatterRegistry {

	private Map<String, IDemoEventFormatter> formattersByEvent = new HashMap<String, IDemoEventFormatter>();
	private Map<String, IDemoEventFormatter> formattersByBuffer = new HashMap<String, IDemoEventFormatter>();

	/* (non-Javadoc)
	 * @see v9t9.engine.demos.format.IDemoFormatterRegistry#registerDemoEventFormatter(java.lang.String, v9t9.common.demo.IDemoEventFormatter)
	 */
	@Override
	public void registerDemoEventFormatter(IDemoEventFormatter formatter) {
		formattersByEvent.put(formatter.getEventIdentifier(), formatter);
		formattersByBuffer.put(formatter.getBufferIdentifer(), formatter);
	}
	
	
	@Override
	public IDemoEventFormatter findFormatterByEvent(String eventType) {
		return formattersByEvent.get(eventType);
	}
	
	@Override
	public IDemoEventFormatter findFormatterByBuffer(String bufferId) {
		return formattersByBuffer.get(bufferId);
	}
}
