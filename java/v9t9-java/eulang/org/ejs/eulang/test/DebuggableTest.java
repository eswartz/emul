/**
 * 
 */
package org.ejs.eulang.test;

/**
 * @author ejs
 *
 */
public interface DebuggableTest {
	public boolean isSkipping();
	public void setSkipping(boolean skipping);
	public boolean isOnlyTest();
	public void setOnlyTest(boolean only);
}
