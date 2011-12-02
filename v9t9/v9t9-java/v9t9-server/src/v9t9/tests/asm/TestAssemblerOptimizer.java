package v9t9.tests.asm;

import java.util.List;

import v9t9.engine.cpu.IInstruction;
import v9t9.tests.BaseTest;
import v9t9.tools.asm.assembler.ContentEntry;
import v9t9.tools.asm.assembler.Symbol;

public class TestAssemblerOptimizer extends BaseTest {

	public void testAssemblerOptimizerSimplifier() throws Exception {
		String text =
			" aorg >100\n"+
			" li R0, 44\n"+
			" li R1, 0\n"+
			" li R2, -1\n"+
			" ai r4, 1\n"+
			" ai r5, 2\n"+
			" ai r6, -1\n"+
			" ai r7, -2\n"+
			"";
		
		testFileContent(text,
				0x100,
				new String[] { "li r0,44",
				"clr r1",
				"seto r2",
				"inc r4",
				"inct r5",
				"dec r6",
				"dect r7",
				},
				new Symbol[] {  });
		
	}
	
	public void testAssemblerOptimizerSimplifier2() throws Exception {
		String text =
			" aorg >100\n"+
			" mov @>2(R1), @>0(R1)\n"+
			" a @>0(R0), *R5+\n"+
			" mov @>0(R1), @>0(R1)\n"+
			"";
		
		testFileContent(text,
				0x100,
				new String[] { "mov @2(R1), *R1",
				"a *R0, *R5+",
				"mov *R1,*R1"},
				new Symbol[] {  });
		
	}
	
	public void testAssemblerOptimizerPeepholer() throws Exception {
		String text =
			" aorg >100\n"+
			" inc r1\n"+
			" ai R0, 0\n"+
			" inc r2\n"+
			"";
		
		testFileContent(text,
				0x100,
				new String[] {
				"inc r1",
				"inc r2",
				},
				new Symbol[] {  });
		
	}
	
	public void testAssemblerOptimizerPeepholer2() throws Exception {
		String text =
			" aorg >100\n"+
			" dw end\n"+		//100
			" li r0, end\n"+	//102
			" inc r1\n"+		//106
			" ai R0, 0\n"+		//108
			"end: inc r2\n"+	//10C
			"";
		
		testFileContent(text,
				0x100,
				new String[] {
				"dw >108",
				"li r0,>108",
				"inc r1",
				"inc r2",
				},
				new Symbol[] { new Symbol(stdAssembler.getSymbolTable(), "end", 0x108) });
		
	}
	
	private void testFileContent(String text, int pc, String[] stdInsts, Symbol[] symbols) throws Exception {
		String caller = new Exception().fillInStackTrace().getStackTrace()[1].getMethodName();
		stdAssembler.pushContentEntry(new ContentEntry(caller + ".asm", text));
		List<IInstruction> asminsts = stdAssembler.parse();
		List<IInstruction> realinsts = stdAssembler.resolve(asminsts);
		realinsts = stdAssembler.optimize(realinsts);

		testGeneratedContent(stdAssembler, pc, stdInsts, symbols, realinsts);
	}

	
}
