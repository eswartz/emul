/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 25, 2006
 *
 */
package v9t9.tests;

import java.util.Set;

import v9t9.engine.cpu.InstructionTable;
import v9t9.engine.cpu.MachineOperand;
import v9t9.engine.memory.DiskMemoryEntry;
import v9t9.engine.memory.MemoryArea;
import v9t9.engine.memory.WordMemoryArea;
import v9t9.tools.llinst.Block;
import v9t9.tools.llinst.ContextSwitchRoutine;
import v9t9.tools.llinst.HighLevelInstruction;
import v9t9.tools.llinst.LabelListOperand;
import v9t9.tools.llinst.LinkedRoutine;
import v9t9.tools.llinst.ParseException;
import v9t9.tools.llinst.Routine;
import v9t9.tools.llinst.RoutineOperand;


public class TestTopDown1 extends BaseTopDownTest
{
    
    public void testDanglingBlock() throws Exception {
        // no return
        Routine routine = parseRoutine(0x100, "ENTRY", new LinkedRoutine(), new String[] {
           "li r1,100"     
        });
        
        phase.run();
        phase.dumpBlocks();
        
        
        assertTrue(routine.getSpannedBlocks().size() == 1);
        Block block = getSingleEntry(routine);
        assertNotNull(block);
        assertTrue(block.getPred().length == 0);
        assertTrue(block.getSucc().length == 0);
        assertTrue((block.getLast().flags & HighLevelInstruction.fIsReturn) == 0);
        validateBlocks(routine.getSpannedBlocks());
    }

    public void testOneBlock() throws Exception {
        Routine routine = parseRoutine(0x100, "ENTRY", new LinkedRoutine(),
                new String[] {
           "li r1,100",
           "rt"
        });
        
        phase.run();
        assertTrue(routine.getSpannedBlocks().size() == 1);
        assertTrue((routine.flags & Routine.fUnknownExit) == 0);
        Block block = getSingleEntry(routine);
        assertNotNull(block);
        assertTrue(block.getPred().length == 0);
        assertTrue(block.getSucc().length == 0);
        assertTrue(block.getFirst() != block.getLast());
        assertTrue(!block.getFirst().isReturn());
        assertTrue(block.getLast().isReturn());
        validateBlocks(routine.getSpannedBlocks());
    }

    public void testThreeBlock() throws Exception {
        Routine routine = parseRoutine(0x100, "ENTRY", new LinkedRoutine(),
                new String[] {
           "li r1,100", // 100
           "jne >10A",  //104
           ///
           "li r1, 10",//106
           //
           "rt"//10A
        });
        
        phase.run();
        assertTrue(routine.getSpannedBlocks().size() == 3);
        assertTrue((routine.flags & Routine.fUnknownExit) == 0);
        
        Block block1 = getSingleEntry(routine);
        Block block2 = block1.getSucc()[1];
        Block block3 = block1.getSucc()[0];

        checkList(block1.getPred(), new Block[] { });
        checkList(block1.getSucc(), new Block[] { block2, block3 });

        assertTrue(block1.getFirst() != block1.getLast());
        assertTrue(!block1.getLast().isReturn());


        checkList(block2.getPred(), new Block[] { block1 });
        checkList(block2.getSucc(), new Block[] { block3 });

        assertTrue(block2.getFirst() == block2.getLast());
        assertTrue(!block2.getFirst().isReturn());
        assertTrue(!block2.getLast().isBranch());

        checkList(block3.getPred(), new Block[] { block1, block2 });
        checkList(block3.getSucc(), new Block[] {} );

        assertTrue(block3.getFirst() == block3.getLast());
        assertTrue((block3.getLast().flags & HighLevelInstruction.fIsReturn + HighLevelInstruction.fIsBranch) != 0);
        validateBlocks(routine.getSpannedBlocks());

    }

    public void testOuterCall() throws Exception {
        Routine routine = parseRoutine(0x100, "ENTRY", new LinkedRoutine(),
                new String[] {
           "li r1,100", // 100
           "blwp @>2300",  
           "rt"
        });
        
        phase.run();
        phase.dumpBlocks();
        assertTrue((routine.flags & Routine.fUnknownExit) == 0);
        assertTrue(routine.getSpannedBlocks().size() == 2);
        Block block1 = getSingleEntry(routine);
        assertTrue(block1.getFirst().inst == InstructionTable.Ili);
        assertTrue(block1.getLast().inst == InstructionTable.Iblwp);
        Block block2 = block1.succ.get(0);
        assertTrue(block2.getFirst().inst == InstructionTable.Ib);
        assertTrue(block2.getLast().inst == InstructionTable.Ib);
        validateBlocks(routine.getSpannedBlocks());
    }

    public void testInnerCall() throws Exception {
    	// this forms another routine
        Routine routine = parseRoutine(0x100, "ENTRY", new ContextSwitchRoutine(0x8300),
                new String[] {
           "li r1,100", // 100
           "bl @>110", // 104
           //
           "rtwp", // 108
           //
           "nop", // 10A
           //
           "nop", // 10C
           //
           "nop", // 10E
           //
           "mov r5, 11", // 110
           "rt", // 114
           
        });
        
        phase.run();
        phase.dumpBlocks();
        
        assertTrue(routine.getSpannedBlocks().size() == 2);
        assertTrue((routine.flags & Routine.fUnknownExit) == 0);
        Block block1 = getSingleEntry(routine);
        assertTrue(block1.getFirst().inst == InstructionTable.Ili);
        assertTrue(block1.getLast().inst == InstructionTable.Ibl);
        Block block2 = block1.succ.get(0);
        assertTrue(block2.getFirst().inst == InstructionTable.Irtwp);
        assertTrue(block2.getLast().inst == InstructionTable.Irtwp);
        
        assertTrue(block1.getFirst().getNext().op1 instanceof RoutineOperand);
        Routine routine2 = ((RoutineOperand) block1.getFirst().getNext().op1).routine;  
        assertNotSame(routine, routine2);
        assertTrue(routine2.getSpannedBlocks().size() == 1);
        
        // the NOPs are jumps so they produce a new routine
        assertEquals(2, phase.getRoutines().size());
        Block r2block = getSingleEntry(routine2);
        assertEquals(1, r2block.getPred().length);
        checkList(r2block.getSucc(), new Block[] {});
        validateBlocks(routine.getSpannedBlocks());
    }

    public void testInnerCall2() throws Exception {
    	// this forms another routine
        Routine routine = parseRoutine(0x100, "ENTRY", new ContextSwitchRoutine(0x8300),
                new String[] {
           "li r1,100", // 100
           "bl @>110", // 104
           //
           "bl @>110", // 108
           //
           "rtwp", // 10C
           //
           "nop", // 10E
           "li r5, 11", // 110
           "rt", // 114
           
        });
        
        phase.run();
        assertTrue(routine.getSpannedBlocks().size() == 3);
        assertTrue((routine.flags & Routine.fUnknownExit) == 0);
        Block block1 = getSingleEntry(routine);
        assertTrue(block1.getFirst().inst == InstructionTable.Ili);
        assertTrue(block1.getFirst().getNext().op1 instanceof RoutineOperand);
        assertTrue(block1.getFirst().getNext().getNext().op1 instanceof RoutineOperand);
        assertTrue(block1.getLast().inst == InstructionTable.Ibl);
        
        Routine routine2 = ((RoutineOperand) block1.getFirst().getNext().op1).routine;  
        Routine routine2p = ((RoutineOperand) block1.getFirst().getNext().getNext().op1).routine;
        // don't duplicate
        assertTrue(routine2 == routine2p);
        validateBlocks(routine.getSpannedBlocks());
        
    }

    public void testSavedBL() throws Exception {
        Routine routine = parseRoutine(0x100, "ENTRY", new LinkedRoutine(),
                new String[] {
           "mov r11,r6", // 100
           "bl @>0110", // 102
           //
           "movb r2,@>fffe(r15)", // 106
           "b *r6", // 10A
           //
           "nop", // 10C
           "nop", // 10E
           "rt", //110
        });
        
        phase.run();
        assertTrue(routine.getSpannedBlocks().size() == 2);
        assertTrue((routine.flags & Routine.fUnknownExit) == 0);
        Block block1 = getSingleEntry(routine);
        assertTrue(block1.getFirst().inst == InstructionTable.Imov);
        assertTrue(block1.getFirst().getNext().op1 instanceof RoutineOperand);
        assertTrue(block1.getLast().inst == InstructionTable.Ibl);
        Block block2 = block1.succ.get(0);
        assertTrue(block2.getFirst().inst == InstructionTable.Imovb);
        assertTrue(block2.getLast().isReturn());
        
        validateBlocks(routine.getSpannedBlocks());
    }

    /** THIS WILL FAIL until we properly implement a compiler (or pass opcodes some other way) */
    /*
    public void testJumpTable1() throws Exception {
        // jumping directly to code (unlikely)
        Routine routine = parseRoutine(0x100, "ENTRY", new LinkedRoutine(),
                new String[] {
           "li r1,4", // 100
           "b @>110(r1)", // 104    
           "nop", // 108
           "nop", // 10A
           "nop", // 10C
           "rt", // 10E
           "li r2,10", //110
           "ai r2, 5", //114
           "jmp >10E", //118
           "data >4444"
        });
        
        phase.run();
        assertTrue(routine.blocks.size() == 5);
        
        LabelListOperand op = (LabelListOperand) routine.blocks.get(0).last.op1;
        assertTrue(op.operands.size() == 3);
        assertTrue(op.operands.get(0).label.addr == 0x118);
        assertTrue(op.operands.get(1).label.addr == 0x11C);
        assertTrue(op.operands.get(2).label.addr == 0x120);
    }*/

    /** If we know the WP, we can optimize this */
    public void testJumpReg1() throws Exception {
        // Jump to register.  This runs in the workspace.
        Routine routine = parseRoutine(0x100, 0x83E0, "ENTRY", new LinkedRoutine(),
                new String[] {
           "li r1,>0300", // 100
           "li r2,>0002", // 104
           "li r3,>0300", // 108
           "li r4,>0000", // 10C
           "li r5,>045b", // 110
           "bl r1", // 114
           "rt" // 116
        });
        
        phase.run();
        assertTrue(routine.getSpannedBlocks().size() == 2);
        
        Block block = getSingleEntry(routine);
        MachineOperand op = (MachineOperand) block.getLast().op1;
        assertTrue(op.type == MachineOperand.OP_ADDR);
        assertEquals(op.val, 0);
        assertEquals(op.immed, (short) 0x83E2);
        validateBlocks(routine.getSpannedBlocks());
    }
    
    /** If we know the WP, we can optimize this */
    public void testJumpReg2() throws Exception {
        // Jump to register.  We don't know the WP.
        Routine routine = parseRoutine(0x100, 0, "ENTRY", new LinkedRoutine(),
                new String[] {
           "bl r1",
           //
           "rt"
        });
        
        phase.run();
        assertTrue(routine.getSpannedBlocks().size() == 2);
        
        Block block = getSingleEntry(routine);
        MachineOperand op = (MachineOperand) block.getLast().op1;
        assertTrue(op.type == MachineOperand.OP_REG);
        assertEquals(op.val, 1);
        assertEquals(op.immed, 0);
        validateBlocks(routine.getSpannedBlocks());
    }

    public void testJumpTable0() throws Exception {
        // Not a jump table!
        Routine routine = parseRoutine(0x100, "ENTRY", new ContextSwitchRoutine(0x83e0),
                new String[] {
        	"mov @>83d8,R11", //100
           "b *R11", // 104
        });

        highLevel.getLLInstructions().put(0xfe, createHLInstruction(0xfe, 0, "B *R10"));

        phase.run();
        assertEquals(1, routine.getSpannedBlocks().size());
        
        assertTrue(getSingleEntry(routine).getLast().op1 instanceof MachineOperand);
        validateBlocks(routine.getSpannedBlocks());
    }
    public void testJumpTable0b() throws Exception {
        // Single-entry
        Routine routine = parseRoutine(0x4c0, "ENTRY", new ContextSwitchRoutine(0x83e0),
                new String[] {
        	"mov @>4c6,R11", //4c0
           "b *R11", // 4c4
           "data >4c8", //4c6
           "clr r10", // 4ca == 4c8 but not a jump
           "clr r12", //4ca
        });

        highLevel.getLLInstructions().put(0xfe, createHLInstruction(0xfe, 0, "B *R10"));

        phase.run();
        assertEquals(2, routine.getSpannedBlocks().size());
        
        LabelListOperand op = (LabelListOperand) getSingleEntry(routine).getLast().op1;
        assertEquals(1, op.operands.size());
        assertEquals(0x4c8, op.operands.get(0).label.getAddr());
        
        
        validateBlocks(routine.getSpannedBlocks());
    }

	private Block getSingleEntry(Routine routine) {
		assertEquals(1, routine.getEntries().size());
		return routine.getMainLabel().getBlock();
	}
    public void testJumpTable1() throws Exception {
        // indirect jump to code.  This requires two instructions -- one to read an address
    	// and another to jump through that register
        Routine routine = parseRoutine(0x100, "ENTRY", new LinkedRoutine(),
                new String[] {
           "mov r1,r1", // 100
           "inct r1", // 102
           "mov @>110(r1), r2", // 104
           "b *r2", // 108
           "nop", // 10A
           "nop", // 10C
           "rt", // 10E
           "data >118", //110
           "data >11C", //112
           "data >11C", //114
           "data >120", //116
           "li r2,10", //118
           "ai r2, 5", //11C
           "jmp >10E", //120
           "data >4444"
        });

        highLevel.getLLInstructions().put(0xfe, createHLInstruction(0xfe, 0, "RT"));

        phase.run();
        assertEquals(5, routine.getSpannedBlocks().size());
        
        LabelListOperand op = (LabelListOperand) getSingleEntry(routine).getLast().op1;
        assertEquals(4, op.operands.size());
        assertEquals(0x118, op.operands.get(0).label.getAddr());
        assertEquals(0x11c, op.operands.get(1).label.getAddr());
        assertEquals(0x11C, op.operands.get(2).label.getAddr());
        assertEquals(0x120, op.operands.get(3).label.getAddr());
        validateBlocks(routine.getSpannedBlocks());
    }

    public void testJumpTable2() throws Exception {
        // indirect jump to code.  This requires two instructions -- one to read an address
    	// and another to jump through that register.  This one restricts the range of the index.
        Routine routine = parseRoutine(0x100, "ENTRY", new LinkedRoutine(),
                new String[] {
           "andi r1,6", // 100
           "mov @>110(r1), r2", // 104
           "b *r2", // 108
           ///
           "nop", // 10A
           ///
           "nop", // 10C
           ///
           "rt", // 10E
           ///
           "data >118", //110
           "data >11C", //112
           "data >11C", //114
           "data >120", //116	// not a target
           ///
           "li r2,10", //118
           ///
           "ai r2, 5", //11C
           "jmp >10E", //120
           "data >4444"
        });
        
        highLevel.getLLInstructions().put(0xfe, createHLInstruction(0xfe, 0, "RT"));
        
        phase.run();
        assertEquals(4, routine.getSpannedBlocks().size());
        
        LabelListOperand op = (LabelListOperand) getSingleEntry(routine).getLast().op1;
        assertEquals(3, op.operands.size());
        assertEquals(0x118, op.operands.get(0).label.getAddr());
        assertEquals(0x11c, op.operands.get(1).label.getAddr());
        assertEquals(0x11C, op.operands.get(2).label.getAddr());
        validateBlocks(routine.getSpannedBlocks());
    }

    public void testJumpTable3() throws Exception {
        // indirect jump to code.  This requires two instructions -- one to read an address
    	// and another to jump through that register.  This one restricts the range of the index.
        Routine routine = parseRoutine(0x100, "ENTRY", new LinkedRoutine(),
                new String[] {
           "mov r1,r1", // 100
           "srl r1,>f", // 102
           "mov @>110(r1), r2", // 104
           "b *r2", // 108
           "nop", // 10A
           "nop", // 10C
           "rt", // 10E
           "data >118", //110
           "data >11C", //112	// not
           "data >11C", //114	// not
           "data >120", //116	// not a target
           "li r2,10", //118
           "ai r2, 5", //11C
           "jmp >10E", //120
           "data >4444"
        });

        highLevel.getLLInstructions().put(0xfe, createHLInstruction(0xfe, 0, "RT"));

        phase.run();
        assertEquals(3, routine.getSpannedBlocks().size());
        
        LabelListOperand op = (LabelListOperand) getSingleEntry(routine).getLast().op1;
        assertEquals(1, op.operands.size());
        assertEquals(0x118, op.operands.get(0).label.getAddr());
        validateBlocks(routine.getSpannedBlocks());
    }

    public void testOneBlockBLWP() throws Exception {
        Routine routine = parseRoutine(0x100, "ENTRY", new ContextSwitchRoutine(0x8300),
                new String[] {
           "li r1,100",
           "rtwp"
        });
        
        phase.run();
        assertTrue(routine.getSpannedBlocks().size() == 1);
        Block block = getSingleEntry(routine);
        assertNotNull(block);
        assertTrue(block.getPred().length == 0);
        assertTrue(block.getSucc().length == 0);
        assertTrue(block.getFirst() != block.getLast());
        assertTrue((block.getFirst().flags & HighLevelInstruction.fIsReturn + HighLevelInstruction.fIsBranch) == 0);
        assertTrue((block.getLast().flags & HighLevelInstruction.fIsReturn) != 0);
        validateBlocks(routine.getSpannedBlocks());
    }

    public void testCrossRoutineJumps() throws ParseException {
    	Routine routine1 = parseRoutine(0x200, "ENTRY", new LinkedRoutine(),
                new String[] {
    		"CLR R5", //200
    		"RT", //202
    	});
    	Routine routine2 = parseRoutine(0x104, "ENTRY2", new LinkedRoutine(),
                new String[] {
    		"SETO R5", //104
    		"MOV @>10E(R4),R4",//106
    		"B *R4", //10A
    		"RT", //10C
    		"DATA >10C",
    		"DATA >200"
    	});
    	
        phase.run();
        validateBlocks(routine1.getSpannedBlocks());
        validateBlocks(routine2.getSpannedBlocks());
        validateRoutines(phase.getRoutines());
        assertEquals(1, routine1.getSpannedBlocks().size());
        
        assertEquals(3, routine2.getSpannedBlocks().size());
        assertEquals(2, routine2.getEntries().size());
        assertTrue((routine1.flags & Routine.fSubroutine) != 0);
        assertTrue((routine2.flags & Routine.fSubroutine) == 0);
        assertTrue(routine2.getEntries().contains(routine1.getMainLabel().getBlock()));
        
    }
    /** Make sure blocks do not contain the same instruction twice 
     * @throws ParseException */
    public void testBlockGeneration() throws ParseException {
        Routine routine = parseRoutine(0x100, "ENTRY", new LinkedRoutine(),
                new String[] {
    		"LI R1,1", //100
    		"LI R2,1", //104
    		"LI R3,1", //108
    		"LI R4,1", //10c
    		"JNE >138", //110
    		"JGT >134", //112
    		"CLR R5", //114
    		"JH >100", //116
    		"LI R6,1", //118
    		"LI R7,1", //11c
    		"NEG R8", //120
    		"JGT >12C",//122
    		"LI R9,1", //124
    		"LI R10,1", //128
    		"LI R11,1", //12c
    		"LI R12,1", //130
    		"MOV @>13A(R13),R13", //134
    		"B *R13", //138
    		"data >100",//13A
    		"data >104",
    		"data >108",
    		"data >10c",
    		"data >110",
    		"data >114",
    		"data >118",
    		"data >11c",
    		"data >120",
    		"data >124",
    		"data >128",
    		"data >12c",
    		"data >130",
    	});
        phase.run();
        phase.dumpBlocks();
        assertEquals(16, routine.getSpannedBlocks().size());
        
        validateBlocks(routine.getSpannedBlocks());
        
    }
    
    public void testJumpTable4() throws ParseException {
    	// don't get confusing flowgraph when jumps inside instructions
        Routine routine = parseRoutine(0x100, "ENTRY", new LinkedRoutine(),
                new String[] {
    		"LI R1,>4c1", //100
    		"MOV @>10C(R2),R2", //104
    		"B *R2", //108
    		"rt", //10A
    		"data >10A",//10C
    		"data >102",//stop here
    		"data >ffff",
    	});
        highLevel.getLLInstructions().put(0x102, createHLInstruction(0x102, 0, "CLR R1"));
        
        phase.run();
        assertEquals(2, routine.getSpannedBlocks().size());

        LabelListOperand op = (LabelListOperand) getSingleEntry(routine).getFirst().getNext().getNext().op1;
        assertEquals(1, op.operands.size());
        assertEquals(0x10A, op.operands.get(0).label.getAddr());
        validateBlocks(routine.getSpannedBlocks());
        
    }

    public void testDataWords1() throws ParseException {
    	Routine routine = parseRoutine(0x100, "ENTRY", new LinkedRoutine(),
                new String[] {
    		"MOV *R11+,R1", //100
    		"CLR R2", //100
    		"MOVB *R11+,R2", //100
    		"SWPB R2",
    		"MOVB *R11+,R2", //100
    		"SWPB R2",
    		"rt", //10A
    	});
        
        phase.run();
        assertEquals(1, routine.getSpannedBlocks().size());
        assertEquals(2, routine.getDataWords());
    }
    public void testDataWords2() throws ParseException {
    	Routine routine = parseRoutine(0x100, "ENTRY", new LinkedRoutine(),
                new String[] {
    		"MOV *R11+,R1", //100
    		"MOV R11,R10",
    		"CLR R2", //100
    		"MOVB *R10+,R2", //100
    		"SWPB R2",
    		"MOVB *R10+,R2", //100
    		"SWPB R2",
    		"B *R10", //10A
    	});
        
        phase.run();
        assertEquals(1, routine.getSpannedBlocks().size());
        assertEquals(2, routine.getDataWords());
    }
    
    public void testDataWords3() throws ParseException {
    	Routine routine = parseRoutine(0x100, "ENTRY", new ContextSwitchRoutine(0x83e0),
                new String[] {
    		"MOV *R14+,R1", //100
    		"CLR R2", //100
    		"MOVB *R14+,R2", //100
    		"SWPB R2",
    		"MOVB *R14+,R2", //100
    		"SWPB R2",
    		"rtwp", //10A
    	});
        
        phase.run();
        assertEquals(1, routine.getSpannedBlocks().size());
        assertEquals(2, routine.getDataWords());
    }
    public void testDataWords4() throws ParseException {
    	Routine routine = parseRoutine(0x100, "ENTRY", new ContextSwitchRoutine(0x8300),
                new String[] {
    		"MOV *R14+,R1", //100
    		// don't follow moves here, because it's more likely we'll return via R14
    		"MOV R14,R11",
    		"CLR R2", //100
    		"MOVB *R11+,R2", //100
    		"SWPB R2",
    		"MOVB *R11+,R2", //100
    		"SWPB R2",
    		"RTWP", //10A
    	});
        
        phase.run();
        assertEquals(1, routine.getSpannedBlocks().size());
        assertEquals(1, routine.getDataWords());
    }
    
    /**
     * A routine may consume words which appear to be jumps or something
     * else which will mess up the flowgraph.  Be sure the routine's data word
     * detection forces a re-scan to fix up such things.
     * @throws ParseException 
     */
    public void testFixupDataWordConfusion() throws ParseException {
    	Routine routine1 = parseRoutine(0x100, "CALLER", new LinkedRoutine(),
                new String[] {
    		"MOV R11,R10",//100
    		"BL @>400", //102
    		//
    		"JMP $+>40",// 106  <-- looks like jumping to >146, but doesn't.
    		"B *R10", //108
    	});
    	Routine routine2 = parseRoutine(0x140, "RANDOM", new LinkedRoutine(),
                new String[] {
    		"CLR R1", // 140
    		"CLR R2", // 142
    		"CLR R3", // 144
    		"CLR R4", // 146
    		"RT",     // 148
    	});
    	Routine routine3 = parseRoutine(0x400, "ROUTINE", new LinkedRoutine(),
                new String[] {
    		"MOV R11,R10",
    		"MOV *R10+, R1",
    		"B *R10",
    	});
        
        phase.run();
        phase.dumpRoutines();
        
        validateRoutines(getRoutines());
        assertEquals(0, routine1.flags & Routine.fSubroutine);
        assertEquals(0, routine2.flags & Routine.fSubroutine);
        assertEquals(0, routine3.flags & Routine.fSubroutine);
        
        assertEquals(2, routine1.getSpannedBlocks().size());
        assertEquals(0, routine1.getDataWords());

        assertEquals(1, routine2.getSpannedBlocks().size());
        assertEquals(0, routine2.getDataWords());

        assertEquals(1, routine3.getSpannedBlocks().size());
        assertEquals(1, routine3.getDataWords());
    }
    
    
    public void testBlockBreaks() throws ParseException {
		Routine routine1 = parseRoutine(0x100, "ENTRY", new LinkedRoutine(),
	            new String[] {
			"JMP $+>2", //100
			"JNE $+>2",	//102
			"LIMI >2",	//104
			"B @>10C",	//108
			"NOP"		//10C
		});
	    
	    phase.run();
	    phase.dumpRoutines();
	    
	    validateRoutines(getRoutines());
	    
	    assertEquals(5, routine1.getSpannedBlocks().size());
	}

	public void test994ARom_0() {
    	MemoryArea area = new WordMemoryArea();
    	String path = "/usr/local/src/v9t9-data/roms/994arom.bin";
    	this.memory.addAndMap(DiskMemoryEntry.newFromFile(area, 0x0, 0x2000, "CPU ROM", CPU, path, 0, false));
    	phase.disassemble();
    	phase.addStandardROMRoutines();
    	phase.run();
    	phase.dumpBlocks();
    	validateBlocks(phase.getBlocks());
    	validateRoutines(phase.getRoutines());
    	Set<Integer> spannedPcs = phase.getBlockSpannedPcs();
    	System.out.println("spanned PCs: " + spannedPcs.size());
		assertTrue(spannedPcs.size() > 0x1e00 / 2);
    	assertTrue(phase.getRoutines().size() > 50);
    }
    
    public void test994ARom_BlockCrazy() {
    	MemoryArea area = new WordMemoryArea();
    	String path = "/usr/local/src/v9t9-data/roms/994arom.bin";
    	this.memory.addAndMap(DiskMemoryEntry.newFromFile(area, 0x0, 0x2000, "CPU ROM", CPU, path, 0, false));
    	phase.disassemble();
    	phase.addStandardROMRoutines();
    	// add label at every instruction just to be sure it doesn't explode
    	for (HighLevelInstruction inst : phase.decompileInfo.getLLInstructions().values()) {
    		if (inst.getBlock() == null)
    			phase.addBlock(new Block(inst));
    	}
    	phase.run();
    	phase.dumpBlocks();
    	
    	System.out.println(phase.getBlocks().size());
    	assertTrue(phase.getBlocks().size() < 4222);
    	validateBlocks(phase.getBlocks());
    	validateRoutines(phase.getRoutines());
    	Set<Integer> spannedPcs = phase.getBlockSpannedPcs();
    	System.out.println("spanned PCs: " + spannedPcs.size());
		assertTrue(spannedPcs.size() > 0x1e00 / 2);
    	assertTrue(phase.getRoutines().size() > 100);
    }
    
    public void test994ARom_1() {
    	MemoryArea area = new WordMemoryArea();
    	String path = "/usr/local/src/v9t9-data/roms/994arom.bin";
    	this.memory.addAndMap(DiskMemoryEntry.newFromFile(area, 0x800, 0x800, "CPU ROM", CPU, path, 0x800, false));
    	phase.decompileInfo.getMemoryRanges().clear();
    	phase.decompileInfo.getMemoryRanges().addRange(0x800, 0x800, true);
    	phase.disassemble();
    	HighLevelInstruction inst = phase.decompileInfo.getLLInstructions().get(0x800);
    	while (inst != null) {
    		if (inst.pc >= 0x800 && inst.pc < 0x1000 && inst.getBlock() == null)
    			phase.addBlock(new Block(inst));
    		inst = inst.getNext();
    	}
    	phase.run();
    	phase.dumpBlocks();
    	System.out.println(phase.getBlocks().size());
    	
    	assertTrue(phase.getBlocks().size() > 240);
    	validateBlocks(phase.getBlocks());
    	
    	System.out.println(phase.getRoutines().size());
    	assertTrue(phase.getRoutines().size() > 25);
    	validateRoutines(phase.getRoutines());
    	
    	Set<Integer> spannedPcs = phase.getBlockSpannedPcs();
    	System.out.println("spanned PCs: " + spannedPcs.size());
		assertTrue(spannedPcs.size() > 0x700 / 2);
    }

}
