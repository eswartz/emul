/*
  PIOHandler.java

  (c) 2014 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.dsr.rs232;

import v9t9.common.dsr.IOBuffer;
import v9t9.common.dsr.IPIOHandler;
import v9t9.common.dsr.IPIOListener;
import ejs.base.utils.ListenerList;
import ejs.base.utils.ListenerList.IFire;

/**
 * This handles 
 * @author ejs
 *
 */
public class PIOHandler implements IPIOHandler {

	private ListenerList<IPIOListener> listeners = new ListenerList<IPIOListener>();

	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IRS232Handler#addListener(v9t9.common.dsr.IRS232Handler.IPIOListener)
	 */
	@Override
	public void addListener(IPIOListener listener) {
		listeners.add(listener);
	}
	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IRS232Handler#removeListener(v9t9.common.dsr.IRS232Handler.IPIOListener)
	 */
	@Override
	public void removeListener(IPIOListener listener) {
		listeners.remove(listener);		
	}

	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IRS232Handler#transmitChars(v9t9.common.dsr.IRS232Handler.Buffer)
	 */
	@Override
	public void transmitChars(final IOBuffer buf) {
		final byte[] buffer = buf.takeAll();
		listeners.fire(new IFire<IPIOListener>() {

			@Override
			public void fire(IPIOListener listener) {
				listener.charsTransmitted(buffer);
			}
		});
	}

}
