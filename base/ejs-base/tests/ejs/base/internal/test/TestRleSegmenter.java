/*
  TestRleSegmenter.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.internal.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ejs.base.utils.RleSegmenter;
import ejs.base.utils.RleSegmenter.Segment;


/**
 * @author ejs
 *
 */
public class TestRleSegmenter {
	
	private RleSegmenter segmenter;
	
	private void encode(int threshold, byte[] content) {
		segmenter = new RleSegmenter(threshold, content, 0, content.length);
		
	}
	private void encode(int threshold, byte[] content, int offset, int length) {
		segmenter = new RleSegmenter(threshold, content, offset, length);
	}
	
	private void validateFrom(int addr, int... ranges) {
		int idx = 0;
		for (Segment seg : segmenter) {
			int expLength = ranges[idx++];
			assertEquals("at " + seg.getOffset(), Math.abs(expLength), seg.getLength());
			assertEquals(addr, seg.getOffset());
			if (expLength < 0) {
				assertTrue(seg.isRepeat());
			}
			addr += Math.abs(expLength);
		}
	}
	private void validate(int... ranges) {
		validateFrom(0, ranges);
	}
	@Test
	public void testNonRle() {
		encode(4, new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 });
		validate(16);
	}
	@Test
	public void testShortRle() {
		encode(4, new byte[] { 0, 1, 2, 3, 4, 4, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 });
		validate(16);
	}
	@Test
	public void testRle1() {
		encode(4, new byte[] { 0, 1, 2, 3, 4, 4, 4, 4, 8, 9, 10, 11, 12, 13, 14, 15 });
		validate(4, -4, 8);
	}
	@Test
	public void testRle2() {
		byte[] content = new byte[] { 0, 1, 2, 3, 4, 4, 4, 4, 8, 8, 8, 8, 12, 13, 14, 15 };
		encode(4, content);
		validate(4, -4, -4, 4);
	}
	@Test
	public void testRle2Inner() {
		byte[] content = new byte[] { 0, 1, 2, 3, 4, 4, 4, 4, 8, 8, 8, 8, 12, 13, 14, 15 };
		encode(4, content, 3, 9);
		validateFrom(3, 1, -4, -4);
	}
	@Test
	public void testRle2b() {
		encode(4, new byte[] { 0, 1, 2, 3, 4, 4, 4, 4, 4, 4, 4, 4, 12, 13, 14, 15 });
		validate(4, -8, 4);
	}
	@Test
	public void testRle3() {
		encode(4, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 });
		validate(-8);
	}
	@Test
	public void testRle4() {
		encode(4, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 1 });
		validate(-8, 1);
	}
	@Test
	public void testRle5() {
		encode(4, new byte[] { 0, 0, 0 });
		validate(3);
	}
	@Test
	public void testRle6() {
		encode(4, new byte[] { 0, 0, 0, 0 });
		validate(4);
	}
}
