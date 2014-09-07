/*
  LPCEncoderParams.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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
	private int playbackHz;
	
	/**
	 * @param order 
	 * @param i 
	 * 
	 */
	public LPCEncoderParams(int hertz, int playbackHz, int framesPerSecond, int order) {
		this.hertz = hertz;
		this.playbackHz = playbackHz;
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
	 * @return the playbackHz
	 */
	public int getPlaybackHz() {
		return playbackHz;
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
