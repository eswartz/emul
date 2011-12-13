/**
 * 
 */
package v9t9.common.hardware;

/**
 * The V9938 provides a "blink" register which controls an alternating
 * pattern that affects the video this way:
 * 
 *  * in text 2 mode, this register tells which characters whose patterns are
 *  marked in the color table will take on the color in register 12 instead of
 *  register 7.
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
	/**
	 * Get the current graphics page offset, if blinking;
	 * only non-zero in graphics modes 4-7.
	 * @return
	 */
	int getGraphicsPageOffset();
	/**
	 * Tell if the blink is "on" -- only true if in text mode 2
	 * @return
	 */
	boolean isBlinkOn();
}
