/**
 * 
 */
package v9t9.machine.common.tests.bench;

/**
 * @author ejs
 *
 */
public class StdSomething implements ISomething {

	/* (non-Javadoc)
	 * @see v9t9.machine.common.tests.bench.ISomething#run()
	 */
	@Override
	public void run() throws Exception {
		if (System.currentTimeMillis() % 2 == 0)
			throw new Exception();
	}

}
