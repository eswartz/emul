/**
 * 
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
