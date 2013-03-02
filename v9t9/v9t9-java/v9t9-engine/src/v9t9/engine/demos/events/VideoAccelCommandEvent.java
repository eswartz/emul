/*
  VideoAccelCommandEvent.java

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
package v9t9.engine.demos.events;

import v9t9.common.demos.IDemoEvent;

/**
 * @author ejs
 *
 */
public abstract class VideoAccelCommandEvent implements IDemoEvent {

	public static final int ACCEL_STARTED = 1;
	public static final int ACCEL_WORK = 7;
	public static final int ACCEL_ENDED = 15;
	protected final int code;

	/**
	 * @param accelStarted
	 */
	public VideoAccelCommandEvent(int code) {
		this.code = code;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + code;
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VideoAccelCommandEvent other = (VideoAccelCommandEvent) obj;
		if (code != other.code)
			return false;
		return true;
	}


	/**
	 * @return the code
	 */
	public int getCode() {
		return code;
	}


}
