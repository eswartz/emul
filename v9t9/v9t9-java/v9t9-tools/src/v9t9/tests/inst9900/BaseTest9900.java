/*
  BaseTest9900.java

  (c) 2008-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tests.inst9900;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.TestCase;
import v9t9.common.asm.Block;
import v9t9.common.asm.IHighLevelInstruction;
import v9t9.common.asm.IInstruction;
import v9t9.common.asm.RawInstruction;
import v9t9.common.asm.ResolveException;
import v9t9.common.asm.Routine;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.files.PathFileLocator;
import v9t9.common.memory.IMemory;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.settings.BasicSettingsHandler;
import v9t9.engine.memory.Memory;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.MemoryEntry;
import v9t9.engine.memory.StockRamArea;
import v9t9.engine.memory.MemoryEntryFactory;
import v9t9.machine.ti99.asm.HighLevelInstruction;
import v9t9.machine.ti99.cpu.InstTable9900;
import v9t9.machine.ti99.cpu.Instruction9900;
import v9t9.tools.asm.Assembler;
import v9t9.tools.asm.AssemblerInstruction;
import v9t9.tools.asm.DirectiveInstructionParserStage;
import v9t9.tools.asm.LLInstruction;
import v9t9.tools.asm.ParseException;
import v9t9.tools.asm.Symbol;
import v9t9.tools.asm.directive.Directive;
import v9t9.tools.asm.inst9900.Assembler9900;
import v9t9.tools.asm.inst9900.StandardInstructionParserStage9900;

public abstract class BaseTest9900 extends TestCase {
	protected ISettingsHandler settings = new BasicSettingsHandler();
	 
	protected IMemoryDomain CPU;
	protected IMemory memory;

	protected StandardInstructionParserStage9900 stdInstStage = new StandardInstructionParserStage9900();
	protected DirectiveInstructionParserStage dtveStage = new DirectiveInstructionParserStage(stdInstStage.getOperandParser());
	protected Assembler9900 stdAssembler = new Assembler9900();

	protected MemoryEntryFactory memoryEntryFactory;

	protected PathFileLocator fileLocator;

	public BaseTest9900() {
		super();
	}

	public BaseTest9900(String name) {
		super(name);
	}
	
	protected void setupAssembler() {
		stdAssembler.setProcessor(Assembler.PROC_9900);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		setupAssembler();
		memory = new Memory();
        CPU = new MemoryDomain(IMemoryDomain.NAME_CPU);
        memory.addDomain(IMemoryDomain.NAME_CPU, CPU);
        memory.addAndMap(new MemoryEntry("test ROM",
        		CPU,
        		0,
        		8192,
        		new StockRamArea(8192)));

        fileLocator = new PathFileLocator();
		memoryEntryFactory = new MemoryEntryFactory(settings, memory, fileLocator	);
	}
	

	protected void validateBlock(Block block) {
		assertNotNull(block);
		assertTrue(block.isComplete());
		IHighLevelInstruction inst = block.getFirst();
		IHighLevelInstruction prev = block.getFirst().getLogicalPrev();
		while (inst != null) {
			if (inst == block.getLast() || block.getLast() == null)
				break;
			
			if (inst.getInst().getPc() > block.getLast().getInst().getPc())
				fail("inst " + inst + " falls outside of logical block " + block);
			
			// insts may be broken in the middle, so only check this when insts are in the same block
			IHighLevelInstruction prevLog = inst.getLogicalPrev();
			if (prev == null || (prevLog != null && prevLog.getBlock() == block))
				assertEquals(prev, prevLog);
			
			if (inst.getBlock() != null && inst.getBlock() != block)
				fail("Inst " + inst + " from another block " + inst.getBlock() + " inside block " +block);
			prev = inst;
			inst = inst.getLogicalNext();
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
			if (block.getFirst().getInst().pc <= prevLastPc) {
				// might have jump inside instr!
				if (block.getFirst().getInst().pc < prevFirstPc)
					fail("blocks out of order at " + block);
				else
					System.out.println("*** block inside another block at " + block);
			}
			prevFirstPc = block.getFirst().getInst().pc;
			prevLastPc = block.getLast().getInst().pc;
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
		IHighLevelInstruction inst = block.getFirst();
		while (inst != null) {
			pcSet.add(inst.getInst().pc & 0xffff);
			if (inst == block.getLast())
				break;
			inst = inst.getLogicalNext();
		}
		return pcSet;
	}

	protected RawInstruction createInstruction(int pc, String element) throws ParseException {
	    IInstruction[] asminsts = stdInstStage.parse("foo", element);
		if (asminsts ==  null || asminsts.length != 1) {
			throw new ParseException("Could not uniquely parse " + element);
		}

		stdAssembler.setPc((short) pc);
		RawInstruction rawInst;
		try {
			rawInst = stdAssembler.getInstructionFactory().createRawInstruction( 
				((LLInstruction)((AssemblerInstruction) asminsts[0]).resolve(
					stdAssembler, null, true)[0]));
		} catch (ResolveException e) {
			throw new IllegalArgumentException(e);
		}
		
		InstTable9900.coerceOperandTypes(rawInst);
		InstTable9900.calculateInstructionSize(rawInst);
		
	    return rawInst;
	}
	protected IHighLevelInstruction createHLInstruction(int pc, int wp, String element) throws ParseException {
		RawInstruction inst = createInstruction(pc, element);
		Instruction9900 inst9900 = new Instruction9900(inst, CPU);
		return new HighLevelInstruction(wp, inst9900, 
				Instruction9900.getInstructionFlags(inst9900));
	}

	class InstFollowInfo {
		public List<IInstruction> realinsts;
		public int idx;
		
		public InstFollowInfo(List<IInstruction> realinsts) {
			this.realinsts = realinsts;
			this.idx = 0;
		}
		public RawInstruction nextRealInst() throws ResolveException {
			IInstruction llInst;
			do {
				llInst = realinsts.get(idx++);
			} while (!(llInst instanceof LLInstruction));
			RawInstruction realInst = stdAssembler.getInstructionFactory().
				createRawInstruction((LLInstruction) llInst);
			return realInst;
		}
		
		public Directive nextDirective(Class<? extends Directive> klass) {
			IInstruction llInst;
			do {
				llInst = realinsts.get(idx++);
			} while (!(llInst instanceof Directive) || !((Directive) llInst).getClass().isAssignableFrom(klass));
			return (Directive) llInst;
		}
	}
	
	protected void testGeneratedContent(Assembler assembler,
			int pc, String[] stdInsts,
			Symbol[] symbols, List<IInstruction> realinsts)
			throws ResolveException {
		testGeneratedSymbols(assembler, symbols);
		
		InstFollowInfo info = new InstFollowInfo(realinsts);
		int targPc = pc;
		for (String stdInstStr : stdInsts) {
			targPc = validateNextInst(stdInstStr, targPc, info);

		}
	}

	private int validateNextInst(String stdInstStr, int targPc,
			InstFollowInfo info) throws ResolveException {
		RawInstruction stdInst = null;
		try {
			stdInst = createInstruction(targPc, stdInstStr);
			IInstruction realInst = info.nextRealInst();
			if (!stdInst.equals(realInst))
				assertEquals(stdInst.toInfoString(), realInst.toInfoString());
			targPc += stdInst.getSize();
		} catch (ParseException e) {
			// try for a directive
			IInstruction[] dirInsts;
			try {
				dirInsts = dtveStage.parse("foo", stdInstStr);
				if (dirInsts == null || dirInsts.length != 1)
					assertNull(stdInstStr, e.toString());

				Directive dirInst = (Directive) dirInsts[0];
				Directive realDirInst = info.nextDirective(dirInst.getClass());
				
				byte[] dirBytes = dirInst.getBytes(stdAssembler.getInstructionFactory());
				byte[] realDirBytes = realDirInst.getBytes(stdAssembler.getInstructionFactory());
				assertTrue(dirInst.toInfoString() + " != " + realDirInst.toInfoString(),
						Arrays.equals(dirBytes, 
								realDirBytes));
				
				targPc += dirBytes.length;
			} catch (ParseException e1) {
				fail(e1.toString());
			}
		}
		return targPc;
	}

	protected void testGeneratedSymbols(Assembler assembler, Symbol[] symbols) {
		for (Symbol stdSymbol : symbols) {
			Symbol symbol = assembler.getSymbolTable().findSymbol(stdSymbol.getName()); 
			assertNotNull(stdSymbol.getName(), symbol);
			assertTrue("missing " + stdSymbol, symbol.isDefined());
			if (stdSymbol.getAddr() != symbol.getAddr())
				assertEquals(stdSymbol.getName(), stdSymbol.getAddr(), symbol.getAddr());
		}
	}
	
	protected void testGeneratedContent(Assembler assembler, List<IInstruction> realinsts,
			Object... pcOrInst) throws ResolveException {
		if (assembler.getErrorList().size() > 0)
			fail("had errors");
		
		for (IInstruction realinst : realinsts)
			System.out.println(realinst);

		InstFollowInfo info = new InstFollowInfo(realinsts);
		int targPc = 0;
		for (Object obj : pcOrInst) {
			if (obj instanceof Integer) {
				targPc = (Integer)obj;
				continue;
			} else if (obj instanceof String) {
				String stdInstStr = (String) obj;
				targPc = validateNextInst(stdInstStr, targPc, info);
				
			} else
				fail("unknown vararg " + obj);
		}
	}

}