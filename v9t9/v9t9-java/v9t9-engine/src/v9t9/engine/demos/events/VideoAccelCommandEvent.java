/**
 * 
 */
package v9t9.engine.demos.events;

import v9t9.common.demo.IDemoEvent;

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
