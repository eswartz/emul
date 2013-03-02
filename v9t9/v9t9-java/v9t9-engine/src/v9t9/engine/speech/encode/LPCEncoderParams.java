/*
  LPCEncoderParams.java

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
package v9t9.engine.speech.encode;

/**
 * @author ejs
 *
 */
public class LPCEncoderParams {

	private int framesPerSecond;
	private int hertz;
	private int order;
	
	/**
	 * @param order 
	 * 
	 */
	public LPCEncoderParams(int hertz, int framesPerSecond, int order) {
		this.hertz = hertz;
		this.framesPerSecond = framesPerSecond;
		this.order = order;
	}
	
	public int getFramesPerSecond() {
		return framesPerSecond;
	}
	public void setFramesPerSecond(int framesPerSecond) {
		this.framesPerSecond = framesPerSecond;
	}
	public int getHertz() {
		return hertz;
	}
	public void setHertz(int hertz) {
		this.hertz = hertz;
	}

	/**
	 * @return
	 */
	public int getOrder() {
		return order;
	}
	/**
	 * @param order the order to set
	 */
	public void setOrder(int order) {
		this.order = order;
	}

	/**
	 * @return
	 */
	public int getFrameSize() {
		int frameSize = getHertz() / getFramesPerSecond();
		return frameSize;
	}
	
}
