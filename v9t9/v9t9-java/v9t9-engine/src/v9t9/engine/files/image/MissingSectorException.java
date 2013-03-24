/**
 * 
 */
package v9t9.engine.files.image;

import java.io.IOException;

/**
 * @author ejs
 *
 */
public class MissingSectorException extends IOException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 973462070227256134L;
	private int secNum;
	private long trackoffset;

	/**
	 * @param trackoffset 
	 * @param message
	 * @param cause
	 */
	public MissingSectorException(int secNum, long trackoffset, String message, Throwable cause) {
		super(message, cause);
		this.secNum = secNum;
		this.trackoffset = trackoffset;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Throwable#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + " (sector " + secNum + "; trackoffset 0x" + Long.toHexString(trackoffset) +")";
	}
	/**
	 * @return the secNum
	 */
	public int getSector() {
		return secNum;
	}

	/**
	 * @return the trackoffset
	 */
	public long getTrackOffset() {
		return trackoffset;
	}
}
