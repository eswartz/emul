/**
 * 
 */
package v9t9.common.demos;

import java.io.IOException;

import v9t9.common.machine.IMachine;

/**
 * @author ejs
 *
 */
public interface IDemoPlayer {

	IMachine getMachine();
	IDemoInputStream getInputStream();
	void executeEvent(IDemoEvent event) throws IOException;
	
	/** Get the total time for the demo in ms */
	double getTotalTime();
	/** Seek to the given time in ms */
	double seekToTime(double time) throws IOException;
	/** Get the current time in ms */
	double getCurrentTime();
}
