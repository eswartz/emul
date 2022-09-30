/*
  BaseTopDownTest9900.java

  (c) 2008-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tests.inst9900;

import java.util.Collection;

import v9t9.common.asm.Block;
import v9t9.common.asm.Routine;
import v9t9.machine.ti99.asm.TopDownPhase9900;
import v9t9.tools.asm.ParseException;

public class BaseTopDownTest9900 extends BaseTopDownPhaseTest9900 {

	protected TopDownPhase9900 phase;

	public BaseTopDownTest9900() {
		super();
	}

	@Override
	protected void setUp() throws Exception {
	    super.setUp();
	    phase = new TopDownPhase9900(state, decompileInfo);
	    System.out.println("==============");
	}

	protected void checkList(Block[] check, Block[] match) {
	    for (Block element : match) {
	        boolean found = false;
	        for (Block element2 : check) {
	            if (element2 == element) {
	                found = true;
	                break;
	            }
	        }
	        if (!found) {
				fail("did not find " + element);
			}
	    }
	
	    for (Block element : check) {
	        boolean found = false;
	        for (Block element2 : match) {
	            if (element == element2) {
	                found = true;
	                break;
	            }
	        }
	        if (!found) {
				fail("did not expect " + check);
			}
	    }
	}

	protected Routine parseRoutine(int pc, String name, Routine routine,
			String[] insts) throws ParseException {
		return parseRoutine(pc, 0, name, routine, insts);
	}

	protected Routine parseRoutine(int pc, int wp, String name,
			Routine routine, String[] insts) throws ParseException {
	    parse(CPU, pc, wp, insts);
	    return phase.addRoutine(pc, name, routine);
	}

	@Override
	protected void validateBlocks(Collection<Block> blocks) {
		super.validateBlocks(blocks);
		for (Block block : blocks) {
			assertTrue(phase.getBlocks().contains(block));
			for (Block pred : block.pred) {
				validateBlock(pred);
				assertTrue(phase.getBlocks().contains(pred));
			}
			for (Block succ : block.succ) {
				validateBlock(succ);
				assertTrue(phase.getBlocks().contains(succ));
			}
		}
	}
}