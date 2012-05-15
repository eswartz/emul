/**
 * 
 */
package v9t9.engine.demos.test;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

import v9t9.common.events.NotifyException;
import v9t9.engine.demos.format.DemoInputBuffer;
import v9t9.engine.demos.format.DemoOutputBuffer;

/**
 * @author ejs
 *
 */
public class TestNewDemoFormat {

	private ByteArrayOutputStream bos = new ByteArrayOutputStream(256);
	private DemoOutputBuffer out = new DemoOutputBuffer(bos, "test", 1);
	private ByteArrayInputStream bis;
	private DemoInputBuffer in;

	protected void readIt() throws IOException {
		out.flush();
		bis = new ByteArrayInputStream(bos.toByteArray());
		in = new DemoInputBuffer(bis, 1, "test"); 
		bis.read();
		in.refill();
	}
	@Test
	public void testVarEncodeSmall() throws IOException {
		exercise(new int[] { 0, 1, 127, 128, 128, 255, -1, -19, -128 });
	}
	
	@Test
	public void testVarEncodeBig() throws IOException {
		exercise(new int[] { 0x12345, -0xfeed, 0, 0, -1, 0x12345678, 0xfedbcda1, 0xffffffff, 0x7fffeeee });
	}
	/**
	 * @param is
	 * @throws NotifyException 
	 * @throws IOException 
	 */
	private void exercise(int[] is) throws IOException {
		out.flush();
		for (int idx = 0; idx < is.length; idx++) {
			out.pushVar(is[idx]);
		}
		
		readIt();

		for (int idx = 0; idx < is.length; idx++) {
			String message = "idx:"+idx;
			assertEquals(message, is[idx], in.readVar());
		}		
	}
}
