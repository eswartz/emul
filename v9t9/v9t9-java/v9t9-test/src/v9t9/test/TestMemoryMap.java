/**
 * 
 */
package v9t9.test;

import org.junit.Test;

import v9t9.common.memory.IMemoryArea;
import v9t9.memory.ByteMemoryArea;

/**
 * Test that basic memory mapping operations work and can be
 * performed with minimal dependencies.
 * @author ejs
 *
 */
public class TestMemoryMap {

	@Test
	public void testEmptyMemoryArea() throws Exception {
		IMemoryArea area = new ByteMemoryArea();
		
		
	}
}
