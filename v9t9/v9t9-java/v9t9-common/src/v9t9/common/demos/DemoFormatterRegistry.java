/*
  DemoFormatterRegistry.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.demos;

import java.util.HashMap;
import java.util.Map;

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
