/*
  VdpV9938AccelCommandEvent.java

  (c) 2012-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.demos.events;

/**
 * @author ejs
 *
 */
public class VdpV9938AccelCommandEvent extends VideoAccelCommandEvent {

	public static final String ID = "VdpV9938AccelCommand";

	/**
	 * @param code
	 */
	public VdpV9938AccelCommandEvent(int code) {
		super(code);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoEvent#getIdentifier()
	 */
	@Override
	public String getIdentifier() {
		return ID;
	}
}
