/*
  TestBlocks9900.java

  (c) 2008-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tests.inst9900;

import junit.framework.AssertionFailedError;
import v9t9.common.asm.Block;
import v9t9.common.asm.IHighLevelInstruction;

/**
 * @author ejs
 *
 */
public class TestBlocks9900 extends BaseTopDownPhaseTest9900 {

	private IHighLevelInstruction inst0,inst1,inst2,inst3,inst4,inst5;
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		inst0 = createHLInstruction(0x0, 0x0, "LI R1,1");
		inst1 = createHLInstruction(0x4, 0x0, "LI R2,1");
		inst2 = createHLInstruction(0x8, 0x0, "LI R3,1");
		inst3 = createHLInstruction(0xC, 0x0, "LI R4,1");
		inst4 = createHLInstruction(0x10, 0x0, "LI R5,1");
		inst5 = createHLInstruction(0x14, 0x0, "LI R6,1");
		inst0.setPhysicalNext(inst1);
		inst1.setPhysicalNext(inst2);
		inst2.setPhysicalNext(inst3);
		inst3.setPhysicalNext(inst4);
		inst4.setPhysicalNext(inst5);
	}
	
	public void testBasic() {
		Block block = new Block(inst0);
		assertTrue(block.isComplete());
		block.setLast(inst4);
		assertTrue(block.isComplete());
		validateBlock(block);
		assertEquals(block, inst0.getBlock());
		assertEquals(block, inst1.getBlock());
		assertEquals(block, inst2.getBlock());
		assertEquals(block, inst3.getBlock());
		assertEquals(block, inst4.getBlock());
	}
	public void testBasic2() {
		Block block = new Block(inst0, inst4);
		assertTrue(block.isComplete());
		validateBlock(block);
		assertEquals(block, inst0.getBlock());
		assertEquals(block, inst1.getBlock());
		assertEquals(block, inst2.getBlock());
		assertEquals(block, inst3.getBlock());
		assertEquals(block, inst4.getBlock());
		
	}
	public void testChangeLastNull() {
		Block block = new Block(inst0);
		assertTrue(block.isComplete());
		block.setLast(inst1);
		validateBlock(block);
		assertEquals(block, inst0.getBlock());
		assertEquals(block, inst1.getBlock());
		assertNull(inst2.getBlock());
		block.setLast(null);
		try {
			validateBlock(block);
			fail("block is incomplete, should not validate");
		} catch (AssertionFailedError e) {
			
		}
		assertEquals(block, inst0.getBlock());
		assertEquals(null, inst1.getBlock());
		assertNull(inst2.getBlock());

	}
	public void testChangeLastLower() {
		Block block = new Block(inst0);
		assertTrue(block.isComplete());
		block.setLast(inst3);
		validateBlock(block);
		assertEquals(block, inst0.getBlock());
		assertEquals(block, inst1.getBlock());
		assertEquals(block, inst2.getBlock());
		assertEquals(block, inst3.getBlock());
		assertNull(inst4.getBlock());
		
		block.setLast(inst1);
		validateBlock(block);
		assertEquals(block, inst0.getBlock());
		assertEquals(block, inst1.getBlock());
		assertEquals(null, inst2.getBlock());
		assertEquals(null, inst3.getBlock());
		assertNull(inst2.getBlock());

	}
	public void testChangeLastHigher() {
		Block block = new Block(inst0);
		assertTrue(block.isComplete());
		block.setLast(inst1);
		validateBlock(block);
		assertEquals(block, inst0.getBlock());
		assertEquals(block, inst1.getBlock());
		block.setLast(inst3);
		validateBlock(block);
		assertEquals(block, inst0.getBlock());
		assertEquals(block, inst1.getBlock());
		assertEquals(block, inst2.getBlock());
		assertEquals(block, inst3.getBlock());
		assertNull(inst4.getBlock());

	}
	public void testSplit() {
		Block block = new Block(inst0);
		assertTrue(block.isComplete());
		block.setLast(inst3);
		
		Block newBlock = block.split(inst2);
		validateBlock(block);
		validateBlock(newBlock);
		
		assertEquals(block, inst0.getBlock());
		assertEquals(block, inst1.getBlock());
		assertEquals(inst1, block.getLast());
		assertEquals(newBlock, inst2.getBlock());
		assertEquals(newBlock, inst3.getBlock());
		assertNull(inst4.getBlock());

	}
	public void testSplitIllegal() {
		Block block = new Block(inst0);
		assertTrue(block.isComplete());
		assertNull(inst1.getBlock());
		
		assertNull(block.split(inst2));
	}
	
	public void testCreateIllegal() {
		Block block = new Block(inst0);
		
		// fine: two unbounded blocks
		new Block(inst1);
		
		// now block is closed
		block.setLast(inst3);
		
		try {
			// cannot make a new block inside; must split
			new Block(inst2);
			fail();
		} catch (Exception e) {
			
		}
		
	}
}
