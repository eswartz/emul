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
}
