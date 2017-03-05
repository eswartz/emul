/*
  DsrException.java

  (c) 2010-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.files;

import java.io.IOException;

public class DsrException extends IOException {

	private static final long serialVersionUID = 2290772739076194246L;
	final  private int err;
	
	public DsrException(int err, Throwable t) {
		super();
		this.err = err;
		initCause(t);
	}

	public DsrException(int err, Throwable t, String message) {
		super(message);
		this.err = err;
		initCause(t);
	}

	public DsrException(int err, String message) {
		super(message);
		this.err = err;
	}

	/* (non-Javadoc)
	 * @see java.lang.Throwable#toString()
	 */
	@Override
	public String toString() {
		return "Error code: " + (err >= 32 ? err >> 5 : err) + "\t" + super.toString();
	}

	/**
	 * Get error code
	 * @return
	 */
	public int getErrorCode() {
		return err;
	}
	
}