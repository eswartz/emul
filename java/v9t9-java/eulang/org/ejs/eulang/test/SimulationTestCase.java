/**
 * 
 */
package org.ejs.eulang.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.BitSet;

import junit.framework.Protectable;
import junit.framework.Test;
import junit.framework.TestResult;

import org.ejs.coffee.core.utils.HexUtils;
import org.ejs.eulang.llvm.LLModule;
import org.ejs.eulang.llvm.directives.LLBaseDirective;
import org.ejs.eulang.llvm.directives.LLDefineDirective;
import org.ejs.eulang.llvm.directives.LLGlobalDirective;
import org.ejs.eulang.llvm.tms9900.Routine;
import org.ejs.eulang.llvm.tms9900.RoutineDumper;
import org.ejs.eulang.llvm.tms9900.app.Simulator;
import org.junit.Before;

import v9t9.engine.memory.MemoryEntry;
import v9t9.engine.memory.MemoryDomain.MemoryWriteListener;

/**
 * @author ejs
 *
 */
public class SimulationTestCase extends BaseInstrTest implements Test, DebuggableTest {
	protected Simulator simulator;
	
	private final String TMP = File.separatorChar == '\\' ? "c:/temp/" : "/tmp/";
	
	private final String program;
	private final SimulationRunnable[] actions;
	private final String testName;
	private final String comment;
	
	private boolean skipping;
	private boolean only;

	public interface SimulationRunnable {
		void run(Simulator sim) throws Exception;
	}
	
	public boolean isSkipping() {
		return skipping;
	}
	public void setSkipping(boolean skipping) {
		this.skipping = skipping;
	}
	
	public boolean isOnlyTest() {
		return only;
	}
	public void setOnlyTest(boolean only) {
		this.only = only;
	}
	/**
	 * @param comment 
	 * @param skipping 
	 * @param only2 
	 * 
	 */
	public SimulationTestCase(String testName, String comment,  String program, boolean skipping, 
			boolean only, SimulationRunnable[] actions) {
		this.testName = testName;
		this.program = program;
		this.comment = comment;
		this.skipping = skipping;
		this.only = only;
		this.actions = actions;
	}
	
	protected Simulator makeSimulator(String string) throws Exception {
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
		
		final BitSet globalChanges = new BitSet();
		MemoryWriteListener globalMemoryListener = new MemoryWriteListener() {
			
			@Override
			public void changed(MemoryEntry entry, int addr, boolean isByte) {
				if (isByte) {
					globalChanges.set(addr);
				} else {
					globalChanges.set((addr & 0xfffe));
					globalChanges.set((addr & 0xfffe) + 1);
				}
			}
		};
		
		sim.getMemory().addWriteListener(globalMemoryListener);
		
		sim.init();
		
		if (!globalChanges.isEmpty()) {
			System.out.println("Global memory:");
			for (int addr = globalChanges.nextSetBit(0); addr >= 0; ) {
				StringBuilder sb = new StringBuilder();
				sb.append("\t==> ").append(HexUtils.toHex4(addr)).append(" = ");
				int endAddr = addr + 1;
				while (globalChanges.nextSetBit(endAddr) == endAddr) {
					endAddr++;
					if (endAddr % 16 == 0)
						break;
				}
				
				if ((addr & 1) == 1) {
					sb.append(HexUtils.toHex2(sim.getMemory().readByte(addr)));
					sb.append(' ');
					addr++;
				}
				while (addr + 2 <= endAddr) {
					sb.append(HexUtils.toHex4(sim.getMemory().readWord(addr)));
					sb.append(' ');
					addr += 2;
				}
				if (addr < endAddr) {
					sb.append(HexUtils.toHex2(sim.getMemory().readByte(addr)));
					sb.append(' ');
					addr++;
				}
				System.out.println(sb);
				
				if (!globalChanges.get(addr))
					addr = globalChanges.nextSetBit(addr + 1);
			}
			System.out.println();
		}
		sim.getMemory().removeWriteListener(globalMemoryListener);
		
		sim.addInstructionListener(sim.new DumpFullReporter());
		final BitSet changes = new BitSet();
		sim.addInstructionListener(new Simulator.InstructionListener() {
			
			@Override
			public void executed(Simulator.InstructionWorkBlock before, Simulator.InstructionWorkBlock after) {
				int wp = sim.getCPU().getWP() & 0xffff;
				for (int addr = changes.nextSetBit(0); addr != -1; addr = changes.nextSetBit(addr+1)) {
					String loc = ((addr >= wp && addr <= wp + 32) ?
							"R" + (addr - wp) / 2 :
								HexUtils.toHex4(addr));
					if ((addr & 1) == 0 && changes.nextSetBit(addr + 1) == addr + 1) {
						// word
						System.out.println("\t==> " + loc
									+ " = " + HexUtils.toHex4(sim.getMemory().readWord(addr)));
						addr++;
					} else {
						System.out.println("\t==> " + loc
									+ " = " + HexUtils.toHex2(sim.getMemory().readByte(addr)));
					}
				}
				changes.clear();
			}
		});
		sim.getMemory().addWriteListener(new MemoryWriteListener() {
			
			@Override
			public void changed(MemoryEntry entry, int addr, boolean isByte) {
				if (isByte) {
					changes.set(addr);
				} else {
					changes.set((addr & 0xfffe));
					changes.set((addr & 0xfffe) + 1);
				}
			}
		});
		return sim;
	}
	protected boolean doOpt(Routine routine) {
		
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
	
	@Before
	public void setup() {
		super.setup();
		doOptimize = true;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return (comment.isEmpty() ? "" : comment + ": ") + testName ;
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.Test#countTestCases()
	 */
	@Override
	public int countTestCases() {
		return 1;
	}
	
	static class MirrorPrintStream extends PrintStream {

		private final PrintStream mirror;

		/**
		 * @param out
		 */
		public MirrorPrintStream(OutputStream out, PrintStream mirror) {
			super(out);
			this.mirror = mirror;
		}
		
		/* (non-Javadoc)
		 * @see java.io.PrintStream#write(byte[], int, int)
		 */
		@Override
		public void write(byte[] buf, int off, int len) {
			super.write(buf, off, len);
			mirror.write(buf, off, len);
		}
		
		/* (non-Javadoc)
		 * @see java.io.PrintStream#write(int)
		 */
		@Override
		public void write(int b) {
			super.write(b);
			mirror.write(b);
		}
		
		
	}
	/* (non-Javadoc)
	 * @see junit.framework.Test#run(junit.framework.TestResult)
	 */
	@Override
	public void run(TestResult result) {
		if (skipping) {
			return;
		}
		
		result.startTest(this);
		
		PrintStream oldOut = System.out;
		PrintStream oldErr = System.err;
		
		final ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
		final ByteArrayOutputStream errBuf = new ByteArrayOutputStream();
		final PrintStream outStr;
		final PrintStream errStr;
		
		if (isOnlyTest()) {
			// special
			outStr = new MirrorPrintStream(outBuf, oldOut);
			errStr = new MirrorPrintStream(errBuf, oldErr);
			dumpTreeize = true;
			dumpLLVMGen = true;
			dumpIsel = true;
		} else {
			outStr = new PrintStream(outBuf);
			errStr = new PrintStream(errBuf);
		}
			
		
		result.runProtected(this, new Protectable() {
			
			@Override
			public void protect() throws Throwable {
				System.setOut(outStr);
				System.setErr(errStr);

				System.out.println("#### " + SimulationTestCase.this.toString());

				setup();
				
				short wp = (short) 0xff80;

				Simulator sim = makeSimulator(program);
				sim.getCPU().setWP(wp);
				
				for (SimulationRunnable r : actions) {
					r.run(sim);
				}
			}
		});
		
		System.setOut(oldOut);
		System.setErr(oldErr);
		
		FileOutputStream fos;
		
		String ftestName = this.getClass().getSimpleName() + "_" + (comment.isEmpty() ? testName : comment);
		ftestName = ftestName.replaceAll("[:_\\. /]", "_");
		try {
			File outFile = new File(TMP + ftestName + ".out");
			fos = new FileOutputStream(outFile);
			fos.write(outBuf.toByteArray());
			fos.close();
			
			File errFile = new File(TMP + ftestName + ".err");
			fos = new FileOutputStream(errFile);
			fos.write(errBuf.toByteArray());
			fos.close();
			
			System.out.println(toString() + ": wrote\n\t" + outFile + "\n\tand\n\t" + errFile);
		} catch (IOException e) {
			result.addError(this, e);
		}
		
		result.endTest(this);
		
		System.out.println();
	}
}
