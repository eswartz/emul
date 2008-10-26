package v9t9.tests;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.TestCase;
import v9t9.engine.cpu.Instruction;
import v9t9.engine.memory.Memory;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.MemoryEntry;
import v9t9.engine.memory.StandardConsoleMemoryModel;
import v9t9.engine.memory.WordMemoryArea;
import v9t9.tools.asm.OperandParser;
import v9t9.tools.asm.StandardInstructionParserStage;
import v9t9.tools.llinst.Block;
import v9t9.tools.llinst.LLInstruction;
import v9t9.tools.llinst.ParseException;
import v9t9.tools.llinst.Routine;

public abstract class BaseTest extends TestCase {

	protected MemoryDomain CPU;
	protected Memory memory;

	public BaseTest() {
		super();
	}

	public BaseTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		memory = new Memory();
        CPU = new MemoryDomain();
        memory.addDomain(CPU);
        memory.addAndMap(new MemoryEntry("test ROM",
        		CPU,
        		0,
        		8192,
        		new StandardConsoleMemoryModel.RamArea(8192)));
	}
	

	protected void validateBlock(Block block) {
		assertNotNull(block);
		assertTrue(block.isComplete());
		LLInstruction inst = block.getFirst();
		LLInstruction prev = block.getFirst().getPrev();
		while (inst != null) {
			if (inst == block.getLast() || block.getLast() == null)
				break;
			
			// insts may be broken in the middle, so only check this when insts are in the same block
			if (prev == null || inst.getPrev().getBlock() == block)
				assertEquals(prev, inst.getPrev());
			
			if (inst.getBlock() != block)
				fail("Inst " + inst + " from another block " + inst.getBlock() + " inside block " +block);
			prev = inst;
			inst = inst.getNext();
		}
		// better have hit 
		assertNotNull("Did not hit last " + block.getLast() + " in " + block, inst);
		
	}

	protected void validateBlocks(Collection<Block> blocks) {
		for (Block block : blocks) {
			validateBlock(block);
		}
	}

	protected void validateRoutines(Collection<Routine> routines) {
		Set<Integer> pcSet = new TreeSet<Integer>();
		for (Routine routine : routines) {
			validateRoutine(routine);
			Set<Integer> routinePcSet = getRoutinePcSet(routine);
			if ((routine.flags & Routine.fSubroutine) == 0) {
				if (!Collections.disjoint(pcSet, routinePcSet)) 
					fail("Routine contains PCs already handled elsewhere in code: " + routine);
			}
			pcSet.addAll(routinePcSet);
		}
	}

	private void validateRoutine(Routine routine) {
		int prevFirstPc = -1;
		int prevLastPc = -1;
		for (Block block : routine.getSpannedBlocks()) {
			validateBlock(block);
			assertTrue(block.isComplete());
			if (block.getFirst().pc <= prevLastPc) {
				// might have jump inside instr!
				if (block.getFirst().pc < prevFirstPc)
					fail("blocks out of order at " + block);
				else
					System.out.println("*** block inside another block at " + block);
			}
			prevFirstPc = block.getFirst().pc;
			prevLastPc = block.getLast().pc;
		}
	}

	private Set<Integer> getRoutinePcSet(Routine routine) {
		Set<Integer> pcSet = new TreeSet<Integer>();
		for (Block block : routine.getSpannedBlocks()) {
			Set<Integer> blockPcSet = getBlockPcSet(block);
			if (!Collections.disjoint(pcSet, blockPcSet)) 
				fail("Block contains PCs already handled elsewhere in routine: " + block + " in " + routine);
			pcSet.addAll(blockPcSet);
		}
		return pcSet;
	}

	private Set<Integer> getBlockPcSet(Block block) {
		Set<Integer> pcSet = new TreeSet<Integer>();
		LLInstruction inst = block.getFirst();
		while (inst != null) {
			pcSet.add(inst.pc & 0xffff);
			if (inst == block.getLast())
				break;
			inst = inst.getNext();
		}
		return pcSet;
	}

	StandardInstructionParserStage stage = new StandardInstructionParserStage();

	  protected LLInstruction createLLInstruction(int pc, int wp, String element) throws ParseException {
	    	Instruction[] asminsts = stage.parse(element);
	    	if (asminsts ==  null || asminsts.length != 1)
	    		throw new IllegalArgumentException("Could not uniquely parse " + element);
	    	asminsts[0].pc = pc;
	        LLInstruction inst = new LLInstruction(wp, asminsts[0]);

	        return inst;
		}


}