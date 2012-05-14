/**
 * 
 */
package v9t9.engine.demos.test;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import v9t9.common.events.NotifyException;
import v9t9.engine.demos.format.BaseReader;
import v9t9.engine.demos.format.DemoOutBuffer;
import v9t9.engine.demos.format.DemoReadBuffer;
import v9t9.engine.demos.format.DemoFormat.BufferType;

/**
 * @author ejs
 *
 */
public class TestNewDemoFormat {
	
	static class ReaderHack  extends BaseReader {

		public ReaderHack(InputStream is) {
			super(is);
		}
		
	}

	private ByteArrayOutputStream bos = new ByteArrayOutputStream(256);
	private DemoOutBuffer out = new DemoOutBuffer(bos, 256);
	private ByteArrayInputStream bis;
	private DemoReadBuffer in;

	protected void readIt() throws IOException, NotifyException {
		out.flush();
		bis = new ByteArrayInputStream(bos.toByteArray());
		in = new DemoReadBuffer(new ReaderHack(bis), 
				"test", BufferType.VIDEO, 256);
		in.refill();
	}
	@Test
	public void testVarEncodeSmall() throws IOException, NotifyException {
		exercise(new int[] { 0, 1, 127, 128, 128, 255, -1, -19, -128 });
	}
	
	@Test
	public void testVarEncodeBig() throws IOException, NotifyException {
		exercise(new int[] { 0x12345, -0xfeed, 0, 0, -1, 0x12345678, 0xfedbcda1, 0xffffffff, 0x7fffeeee });
	}
	/**
	 * @param is
	 * @throws NotifyException 
	 * @throws IOException 
	 */
	private void exercise(int[] is) throws IOException, NotifyException {
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
