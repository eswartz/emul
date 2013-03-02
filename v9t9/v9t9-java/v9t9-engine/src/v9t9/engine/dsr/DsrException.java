/*
  DsrException.java

  (c) 2010-2011 Edward Swartz

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
package v9t9.engine.dsr;

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