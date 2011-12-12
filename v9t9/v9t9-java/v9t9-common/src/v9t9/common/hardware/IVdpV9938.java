/**
 * 
 */
package v9t9.common.hardware;

/**
 * @author ejs
 *
 */
public interface IVdpV9938 extends IVdpTMS9918A {

	/** Get time when blinking is ON in ms */
	int getBlinkOnPeriod();
	/** Get time when blinking is OFF in ms */
	int getBlinkOffPeriod();
	/** Get total time for blink on/off period in ms */
	int getBlinkPeriod();
}
