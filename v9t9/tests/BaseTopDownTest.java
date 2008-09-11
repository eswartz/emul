package v9t9.tests;

import java.util.Collection;

import v9t9.tools.decomp.Block;
import v9t9.tools.decomp.Routine;
import v9t9.tools.decomp.TopDownPhase;

public class BaseTopDownTest extends BaseTopDownPhaseTest {

	protected TopDownPhase phase;

	public BaseTopDownTest() {
		super();
	}

	@Override
	protected void setUp() throws Exception {
	    super.setUp();
	    phase = new TopDownPhase(CPU, decompileInfo);
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
			String[] insts) {
				return parseRoutine(pc, 0, name, routine, insts);
			}

	protected Routine parseRoutine(int pc, int wp, String name,
			Routine routine, String[] insts) {
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