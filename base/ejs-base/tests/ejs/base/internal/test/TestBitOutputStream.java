/**
 * 
 */
package ejs.base.internal.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;

import org.junit.Test;

import ejs.base.utils.BitOutputStream;


/**
 * @author ejs
 *
 */
public class TestBitOutputStream {

	final ByteArrayOutputStream stockBis = new ByteArrayOutputStream();
	final ByteArrayOutputStream emptyBis = new ByteArrayOutputStream();
	
	@Test
	public void testEmpty() throws Exception {
		BitOutputStream bs = new BitOutputStream(emptyBis);
		bs.close();
		assertEquals(0, emptyBis.size());
	}
	@Test
	public void testIllegal() throws Exception  {
		BitOutputStream bs = new BitOutputStream(emptyBis);
		try {
			bs.writeBits(0, -1);
			fail();
		} catch (IllegalArgumentException e) {
			
		}
		try {
			bs.writeBits(0, 16);
			fail();
		} catch (UnsupportedOperationException e) {
		} catch (IllegalArgumentException e) {
			
		}
	}
	

	@Test
	public void testSmall() throws Exception {
		BitOutputStream bs = new BitOutputStream(stockBis);
		bs.writeBits(0x55555555, 1);
		
		bs.close();
		
		byte[] bytes = stockBis.toByteArray();
		assertEquals(1, bytes.length);
		assertEquals((byte) 0x80, bytes[0]);
		
	}
	
	@Test
	public void testSmall2() throws Exception {
		BitOutputStream bs = new BitOutputStream(stockBis);
		bs.writeBits(0xffab, 8);
		
		bs.close();
		
		byte[] bytes = stockBis.toByteArray();
		assertEquals(1, bytes.length);
		assertEquals((byte) 0xab, bytes[0]);
		
	}

	@Test
	public void testCross1() throws Exception {
		BitOutputStream bs = new BitOutputStream(stockBis);
		bs.writeBits(0x0, 4);
		bs.writeBits(-1, 8);
		bs.writeBits(0x0, 4);
		
		bs.close();
		
		byte[] bytes = stockBis.toByteArray();
		assertEquals(2, bytes.length);
		assertEquals((byte) 0x0f, bytes[0]);
		assertEquals((byte) 0xf0, bytes[1]);
		
	}
	
	
	@Test
	public void testBig() throws Exception {
		BitOutputStream bs = new BitOutputStream(stockBis);
		bs.writeBits(3, 4);
		bs.writeBits(0, 1);
		bs.writeBits(0, 6);
		bs.writeBits(14, 5);
		bs.writeBits(25, 5);
		bs.writeBits(9, 4);
		bs.writeBits(7, 4);
		
		bs.writeBits(5, 4);
		bs.writeBits(0, 1);
		bs.writeBits(0, 6);
		bs.writeBits(14, 5);
		bs.writeBits(20, 5);
		bs.writeBits(9, 4);
		bs.writeBits(8, 4);
		
		bs.close();
		
		byte[] bytes = stockBis.toByteArray();
		assertEquals(8, bytes.length);
		assertEquals(0x30, bytes[0]);
		assertEquals(0x0e, bytes[1]);
		assertEquals((byte) 0xcc, bytes[2]);
		assertEquals((byte) 0xba, bytes[3]);
		assertEquals((byte) 0x80, bytes[4]);
		assertEquals((byte) 0x75, bytes[5]);
		assertEquals((byte) 0x26, bytes[6]);
		assertEquals((byte) 0x0, bytes[7]);
		
	}
	
}
