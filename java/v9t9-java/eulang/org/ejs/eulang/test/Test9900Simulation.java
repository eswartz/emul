/**
 * 
 */
package org.ejs.eulang.test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.ejs.coffee.core.utils.HexUtils;
import org.ejs.eulang.llvm.LLModule;
import org.ejs.eulang.llvm.directives.LLBaseDirective;
import org.ejs.eulang.llvm.directives.LLDefineDirective;
import org.ejs.eulang.llvm.directives.LLGlobalDirective;
import org.ejs.eulang.llvm.tms9900.Routine;
import org.ejs.eulang.llvm.tms9900.RoutineDumper;
import org.ejs.eulang.llvm.tms9900.app.Simulator;
import org.ejs.eulang.llvm.tms9900.app.Simulator.InstructionWorkBlock;
import org.junit.Before;
import org.junit.Test;

import v9t9.engine.memory.MemoryEntry;
import v9t9.engine.memory.MemoryDomain.MemoryWriteListener;

/**
 * @author ejs
 *
 */
public class Test9900Simulation extends BaseInstrTest {
	protected boolean doOptimize;
	
	protected Simulator makeSimulate(String string) throws Exception {
		LLModule mod = getModule(string);
		for (LLBaseDirective dir : mod.getDirectives()) {
			if (dir instanceof LLDefineDirective) {
				LLDefineDirective def = (LLDefineDirective) dir;
				
				Routine routine = doIsel(mod, def);
				
				routine.setupForOptimization();
				if (doOptimize)
					doOpt(routine);

			}
			else if (dir instanceof LLGlobalDirective) {
				LLGlobalDirective glob = (LLGlobalDirective) dir;
				
				doData(mod, glob);
			}
			else {
				System.err.println("ignoring " + dir);
			}
		}
		
		final Simulator sim = new Simulator(v9t9Target, buildOutput);
		
		sim.addInstructionListener(sim.new DumpFullReporter());
		final List<Short> changes = new ArrayList<Short>();
		sim.addInstructionListener(new Simulator.InstructionListener() {
			
			@Override
			public void executed(Simulator.InstructionWorkBlock before, Simulator.InstructionWorkBlock after) {
				int wp = sim.getCPU().getWP() & 0xffff;
				for (short addr : changes)
					System.out.println("\t==> " + ((addr >= wp && addr <= wp + 32) ?
							"R" + (addr - wp) / 2 :
								HexUtils.toHex4(addr))
								+ " = " + HexUtils.toHex4(sim.getMemory().readWord(addr)));
				changes.clear();
			}
		});
		sim.getMemory().addWriteListener(new MemoryWriteListener() {
			
			@Override
			public void changed(MemoryEntry entry, int addr) {
				changes.add((short) addr);
			}
		});
		return sim;
	}
	private boolean doOpt(Routine routine) {
		
		boolean anyLowered = false;
		do {
			runPeepholePhase(routine);
			boolean lowered = runLowerPseudoPhase(routine);
			anyLowered |= lowered;
			if (!lowered)
				break;
		} while (true);
		
		if (!anyLowered)
			System.out.println("\n*** No changes");
		else {
			System.out.println("\n*** Done:\n");
			routine.accept(new RoutineDumper());
		}
		
		
		return anyLowered;
	}
	
	protected short doSimulate(Simulator sim, String name, int timeout) {
		Routine routine = buildOutput.lookupRoutine(name);
		assertNotNull(routine);
		
		short pc = sim.getAddress(routine.getName());
		short wp = (short) 0xff80;
		
		sim.getMemory().writeWord(wp + v9t9Target.getSP() * 2, wp);
		
		sim.executeAt(pc, wp, timeout);
		
		// return R0
		return sim.getMemory().readWord(wp);
	}

	@Before
	public void setup() {
		super.setup();
		doOptimize = true;
	}
	
	@Test
	public void testSimple() throws Exception {
		Simulator sim = makeSimulate("x : Int = 5;\n"+
				"getX = code() { x; };\n"+
				"");
		short val = doSimulate(sim, "getX", 100);
		assertEquals(5, val);
	}
	@Test
	public void testShift1() throws Exception {
		Simulator sim = makeSimulate("x : Int = 5;\n"+
				"testShift1 = code() { y := 4; z := ((x << y) << 8); y = 16; z |= x << y; };\n"+
				"");
		short val = doSimulate(sim, "testShift1", 100);
		assertEquals(0x5000, val);
	}
	@Test
	public void testSimpleCall1() throws Exception {
		Simulator sim = makeSimulate(
				"x := 100;\n"+
				"negate = [T] code(x) { -x; };\n"+
				"testSimpleCall1 = code() { y := x*2; z := negate(y);  };\n"+
				"");
		short val = doSimulate(sim, "testSimpleCall1", 100);
		assertEquals(-200, val);
	}
	@Test
	public void testArraySum1() throws Exception {
		Simulator sim = makeSimulate(
				"testArraySum = code() {\n"+
				"  vals : Int[10];\n"+
				"  s := 0;\n"+
				"  for i in 10 do vals[i] = i+1;\n"+
				"  for i in 10 do s += vals[i];\n"+
				"};\n"+
				"");
		short val = doSimulate(sim, "testArraySum", 500);
		assertEquals(55, val);
	}
	@Test
	public void testArraySum2() throws Exception {
		Simulator sim = makeSimulate(
				"vals:Int[10];\n"+
				"testArraySum = code() {\n"+
				"  valp : Int[]^ = &vals;\n"+
				"  s := 0;\n"+
				"  for i in 10 do valp[i] = i+1;\n"+
				"  for i in 10 do s += valp[i];\n"+
				"};\n"+
				"");
		short val = doSimulate(sim, "testArraySum", 500);
		assertEquals(55, val);
	}
	@Test
	public void testArraySum3() throws Exception {
		Simulator sim = makeSimulate(
				"ARRAY =: Int[3,3];\n"+
				"vals:ARRAY;\n"+
				"doSum = code(valp:ARRAY^) {\n"+
				"  s := 0;\n"+
				"  for i in 3 do for j in 3 do valp[i,j] = i*3+j;\n"+		// 0,1,2 | 3,4,5 | 6,7,8
				"  for i in 3 do s += valp[i,i]+3;\n"+		// 0+3 | 4+3 | 8+3
				"};\n"+
				"testArraySum = code() {\n"+
				"  valp := &vals;\n"+
				"  doSum(valp);\n"+
				"};\n"+
				"");
		short val = doSimulate(sim, "testArraySum", 500);
		assertEquals(21, val);
	}

}
