/*
  IVdpV9938.java

  (c) 2011-2012 Edward Swartz

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
package v9t9.common.hardware;

/**
 * The V9938 provides a "blink" register (VR13) which controls an alternating
 * pattern that affects the video this way:
 * 
 *  * in text 2 mode, this register tells which characters whose patterns are
 *  marked in the color table will take on the color in VR12 instead of
 *  VR7.
 *  
 *  * in graphics 4-7 modes, this register flips graphics pages.
 * @author ejs
 *
 */
public interface IVdpV9938 extends IVdpTMS9918A {

	/** Get time when blinking/flipping is ON in ms */
	int getBlinkOnPeriod();
	/** Get time when blinking/flipping is OFF in ms */
	int getBlinkOffPeriod();
	/** Get total time for blink on/off period in ms */
	int getBlinkPeriod();
	
	interface IAccelListener {
		void accelCommandStarted();
		void accelCommandWork();
		void accelCommandEnded();
	}
	void addAccelListener(IAccelListener listener);
	void removeAccelListener(IAccelListener listener);
	
	boolean isAccelActive();
}
