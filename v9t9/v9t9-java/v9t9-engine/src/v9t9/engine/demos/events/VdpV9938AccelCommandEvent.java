/*
  VdpV9938AccelCommandEvent.java

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
