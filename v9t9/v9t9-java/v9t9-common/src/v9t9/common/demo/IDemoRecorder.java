/**
 * 
 */
package v9t9.common.demo;

import java.io.IOException;

import v9t9.common.machine.IMachine;

/**
 * @author ejs
 *
 */
public interface IDemoRecorder {

	IMachine getMachine();
	
	IDemoOutputStream getOutputStream();

	void fail(Throwable e);

	void flushData() throws IOException;
	
}
