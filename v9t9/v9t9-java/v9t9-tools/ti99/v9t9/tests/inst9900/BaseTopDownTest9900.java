/*
  BaseTopDownTest9900.java

  (c) 2008-2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.tests.inst9900;

import java.util.Collection;

import v9t9.common.asm.Block;
import v9t9.common.asm.Routine;
import v9t9.machine.ti99.asm.TopDownPhase;
import v9t9.tools.asm.assembler.ParseException;

public class BaseTopDownTest9900 extends BaseTopDownPhaseTest9900 {

	protected TopDownPhase phase;

	public BaseTopDownTest9900() {
		super();
	}

	@Override
	protected void setUp() throws Exception {
	    super.setUp();
	    phase = new TopDownPhase(state, decompileInfo);
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