/*
  DemoFormatterRegistry.java

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
