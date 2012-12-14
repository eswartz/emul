/**
 * 
 */
package v9t9.machine.common.tests.bench;

/**
 * @author ejs
 *
 */
public class TestExcSpeed {
	public static void main(String[] args) throws Exception {
		ISomething something;
		if (args.length > 0) {
			something = (ISomething) Class.forName(args[0]).newInstance();
		} else {
			something = new StdSomething();
		}
		long start = System.currentTimeMillis();
		
		int excs = 0;
		final int COUNT = 10000000;
		for (int i = 0; i < COUNT; i++) {
			try {
				something.run();
			} catch (Throwable t) {
				excs++;
			}
		}
		long end = System.currentTimeMillis();
		
		System.out.println(excs + " exceptions in " + (end-start) + " ms: " + excs * 1000 / (end-start ) + " per sec");
	}
}
