/**
 * 
 */
package v9t9.tools.forthcomp.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;

import v9t9.common.cpu.ICpu;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IMachineModel;
import v9t9.common.memory.IMemoryDomain;
import v9t9.engine.interpreter.IInterpreter;
import v9t9.tools.forthcomp.AbortException;
import v9t9.tools.forthcomp.ForthComp;
import v9t9.tools.forthcomp.HostContext;
import v9t9.tools.forthcomp.ITargetWord;
import v9t9.tools.forthcomp.RelocEntry;
import v9t9.tools.forthcomp.RelocEntry.RelocType;
import v9t9.tools.forthcomp.words.TargetContext;
import v9t9.tools.forthcomp.words.TargetContext.IMemoryReader;

/**
 * @author ejs
 *
 */
public abstract class BaseForthCompTest {

	protected static final int BASE_RP = 0xff00;
	protected static final int BASE_SP = 0xf800;
	protected static final int BASE_UP = 0xfe00;
	
	protected TargetContext targCtx;
	ForthComp comp;
	HostContext hostCtx;
	protected int startDP;
	
	static IMachineModel f99bMachineModel;
	static IMachine f99Machine;
	protected static IInterpreter interp;
	protected static ICpu cpu;

	/**
	 * 
	 */
	public BaseForthCompTest() {
		if (cpu == null) {
			initCpu();
			
			/*
			 			f99bMachineModel = new F99bMachineModel();
			f99Machine = (F99bMachine) f99bMachineModel.createMachine();
			cpu = (CpuF99b) f99Machine.getCpu();
		
			DumpFullReporterF99b dump = new DumpFullReporterF99b(cpu,  new PrintWriter(System.out));
			f99Machine.getExecutor().addInstructionListener(dump);

			 */
		}

	}
	
	/**
	 * 
	 */
	abstract protected void initCpu();

	@Before
	public void setup() throws AbortException {
		interp = createInterpreter();
		
		targCtx = createTargetContext();
		
		hostCtx = new HostContext(targCtx);
		comp = new ForthComp(hostCtx, targCtx);
		
		for (int i = 0; i <65536; i++)
			cpu.getConsole().writeByte(i, (byte) 0);
		
		targCtx.defineBuiltins();
		//comp.parseString("User HERE User Base");

		startDP = targCtx.getDP();
	}
	protected abstract TargetContext createTargetContext();

	protected abstract IInterpreter createInterpreter();

	@After
	public void shutDown() {
		interp.dispose();
	}

	protected void dumpMemory(PrintStream out, int from, int to, final IMemoryDomain domain) {
		System.out.println("raw memory:");
		TargetContext.dumpMemory(out, from, to, new IMemoryReader() {
			
			@Override
			public int readWord(int addr) {
				return domain.readWord(addr);
			}
		});
	}
	
	protected void dumpDict() {
		System.out.println("dictionary cells:");
		targCtx.dumpDict(System.out, startDP & ~1, targCtx.getDP());
	}
	
	protected void parseString(String text) throws AbortException {
		System.out.println(text);
		comp.parseString(text);
		comp.finish();
		assertEquals("errors when compiling", 0, comp.getErrors());
	}
	
	/**
	 * @param name
	 * @throws AbortException 
	 * @throws IOException 
	 */
	protected void interpret(String name) throws AbortException {
		String caller = new Exception().getStackTrace()[1].getMethodName();
		System.out.println("*** interpreting in " + caller);
		
		exportBinary();
		
		ITargetWord word = (ITargetWord) targCtx.require(name);
		
		int pc = word.getEntry().getContentAddr();
		
		doInterpret(pc);
		
		targCtx.importState(hostCtx, f99Machine, BASE_SP, BASE_RP);
		
	}

	protected void exportBinary() throws AbortException {
		targCtx.exportState(hostCtx, f99Machine, BASE_SP, BASE_RP, BASE_UP);

		dumpCompiledMemory();
	}
	protected abstract void doInterpret(int pc);

	protected void dumpCompiledMemory() {
		dumpMemory(System.out, startDP, targCtx.getDP(), f99Machine.getConsole());
	}
	
	protected void assertCall(ITargetWord word, int cell) throws AbortException {
		RelocEntry rel = targCtx.getRelocEntry(cell);
		assertNotNull(rel);
		assertEquals(RelocType.RELOC_CALL_15S1, rel.type);
		assertEquals(word.getEntry().getContentAddr(), rel.target);
	}
	protected void assertCall(String string, int cell) throws AbortException {
		ITargetWord word = (ITargetWord) targCtx.require(string);
		assertCall(word, cell);
	}
	
	

}
