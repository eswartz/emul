/**
 * 
 */
package org.ejs.eulang.test;

import junit.framework.TestSuite;

/**
 * @author ejs
 *
 */
public class DebuggableTestSuite extends TestSuite {

	private boolean onlyOneTest;

	public boolean isOnlyOneTest() {
		return onlyOneTest;
	}

	public void setOnlyOneTest(boolean onlyOneTest) {
		this.onlyOneTest = onlyOneTest;
	}
	
}
