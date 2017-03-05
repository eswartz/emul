/*
  TestMD5SumFilters.java

  (c) 2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.common.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;

import org.junit.Test;

import v9t9.common.files.IMD5SumFilter;
import v9t9.common.files.MD5FilterAlgorithms;
import v9t9.common.files.MD5SumEngine;

/**
 * @author ejs
 *
 */
public class TestMD5SumFilters {

	private static final byte[] content1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
	
	@Test
	public void testEquivalentSums() {
		String md5a = MD5SumEngine.createMD5(
				MD5FilterAlgorithms.FullContentFilter.INSTANCE, 
				content1);
		String md5b = MD5SumEngine.createMD5(
				MD5FilterAlgorithms.create(MD5FilterAlgorithms.ALGORITHM_SEGMENT
						+":0000+000A"),
				content1);
		assertEquals(md5a, md5b);
	}


	@Test
	public void testSegments() {
		byte[] content = { 1, 2, 3, 4 };
		IMD5SumFilter filter = MD5FilterAlgorithms.create(MD5FilterAlgorithms.ALGORITHM_SEGMENT
				+":0001+0001:0003+0001");
		String baseMd5 = MD5SumEngine.createMD5(filter, content);
		
		// should not affect since outside segments
		content[0] = (byte) 0x90;
		String md5x = MD5SumEngine.createMD5(filter, content);
		assertEquals(baseMd5, md5x);
		
		// should not affect since outside segments
		content[2] = (byte) 0x90;
		md5x = MD5SumEngine.createMD5(filter, content);
		assertEquals(baseMd5, md5x);

		// *should* affect
		content[1] = (byte) 0x0;
		md5x = MD5SumEngine.createMD5(filter, content);
		assertFalse(baseMd5.equals(md5x));
		
		
	}
	
	@Test
	public void testGrom() {
		String baseMd5 = null;
		byte[] content = new byte[40960];
		Arrays.fill(content, (byte) 0xaa);
		baseMd5 = MD5SumEngine.createMD5(MD5FilterAlgorithms.GromFilter.INSTANCE, content);
		
		// test with random garbage i each last 2k of 8k
		for (int i = 0; i < 4; i++) {
			Arrays.fill(content, 0x2000 * i + 0x1800, 0x2000 * i + 0x1fff, (byte) i);
			String md5x = MD5SumEngine.createMD5(MD5FilterAlgorithms.GromFilter.INSTANCE, content);
			assertEquals("i="+i, baseMd5, md5x);
		}
	}
}
