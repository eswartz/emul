package v9t9.tests.asm;

import java.util.List;

import v9t9.engine.cpu.IInstruction;
import v9t9.tests.BaseTest;
import v9t9.tools.asm.Assembler;
import v9t9.tools.asm.ContentEntry;
import v9t9.tools.asm.Equate;
import v9t9.tools.asm.Symbol;
import v9t9.tools.llinst.ParseException;

public class TestAssemblerMacros extends BaseTest {

	Assembler assembler = new Assembler();
	

	public void testAssemblerMacroSimple() throws Exception {
		String text =
			"SP equ 10\n"+
			" aorg >100\n"+
			" si SP,10\n"+
			"";
		
		testFileContent(text,
				0x100,
				new String[] { "ai r10,-10"},
				new Symbol[] { new Equate(assembler.getSymbolTable(), "SP", 10) });
		
	}
	public void testAssemblerMacroDefine() throws Exception {
		String text =
			"SP equ 10\n"+
			" aorg >100\n"+
			" define NegVal a,b [\n"+
			" neg ${a}\n"+
			" inv ${b}\n"+
			" a ${a},${b}\n"+	// ignore first 'a'
			" ]\n"+
			" NegVal SP,*5+\n"+
			"";
		
		testFileContent(text,
				0x100,
				new String[] { 
				"neg 10",
				"inv *5+",
				"a 10,*5+"
				},
				new Symbol[] { new Equate(assembler.getSymbolTable(), "SP", 10) });
		
	}
	
	public void testAssemblerMacroDefine2() throws Exception {
		// nesting and argument disambiguation
		String text =
			"SP equ 10\n"+
			" aorg >100\n"+
			" define InvVal a [\n" +
			" inv ${a}\n"+
			" ]\n"+
			
			" define NegVal a,b [\n"+
			" neg ${a}\n"+
			" invval ${b}\n"+	// allow to be expanded with different arg using same name as different arg
			" a ${a},${b}\n"+
			" ]\n"+
			" NegVal SP,*5+\n"+
			"";
		
		testFileContent(text,
				0x100,
				new String[] { 
				"neg 10",
				"inv *5+",
				"a 10,*5+"
				},
				new Symbol[] { new Equate(assembler.getSymbolTable(),  "SP", 10) });
		
	}
	
	public void testAssemblerMacroDefine3() throws Exception {
		String text =
			"SP equ 10\n"+
			" aorg >100\n"+
			" define NegVal a [\n" +
			" inv ${a}\n"+
			" NegVal SP,*5+\n"+
			"";
		
		try {
			testFileContent(text,
					0x100,
					new String[] { 
					},
					new Symbol[] { new Equate(assembler.getSymbolTable(),  "SP", 10) });
			assertEquals(1, assembler.getErrorList().size());
		} catch (ParseException e) {
			
		}
		
	}
	
	public void testAssemblerForEach() throws Exception {
		String text =
			"SP equ 10\n"+
			" aorg >100\n"+
			" foreach REG, IDX ( R1, R5, R11 ){\n"+
			" mov ${REG}, @-${IDX}*2(SP)\n"+
			"}\n"+
			" foreach - REG, IDX ( R1, R5, R11 ){\n"+
			" mov @-${IDX}*2(SP), ${REG}\n"+
			"}\n"+
			"";
		
		testFileContent(text,
				0x100,
				new String[] {
				"mov R1,*10\n",
				"mov R5, @-2(10)\n",
				"mov R11, @-4(10)\n",
				"mov @-4(10), R11\n",
				"mov @-2(10), R5\n",
				"mov *10, R1\n",
				},
				new Symbol[] { new Equate(assembler.getSymbolTable(),  "SP", 10) });
		
	}
	public void testAssemblerMacroLoop() throws Exception {
		String text =
			"SP equ 10\n"+
			" aorg >100\n"+
			" define Push SP, ... [\n" +
			" ai SP, -${#}*2\n"+
			" foreach REG, IDX {\n"+
			" mov ${REG}, @-${IDX}*2(SP)\n"+
			"}\n"+
			"]\n"+
			" define Pop SP, ... [\n" +
			" foreach - REG, IDX {\n"+
			" mov *${SP}+, ${REG}\n"+
			"}\n"+
			"]\n"+
			" Push SP, R1, R5, R11\n"+
			" Pop SP, R1, R5, R11\n"+
			"";
		
		testFileContent(text,
				0x100,
				new String[] {
				"ai 10,-6\n",
				"mov R1,*10\n",
				"mov R5, @-2(10)\n",
				"mov R11, @-4(10)\n",
				"mov *10+, R11\n",
				"mov *10+, R5\n",
				"mov *10+, R1\n",
				},
				new Symbol[] { new Equate(assembler.getSymbolTable(),  "SP", 10) });
		
	}

	public void testAssemblerLocalSymbols() throws Exception {
		String text =
			"SP equ 10\n"+
			" aorg >100\n"+
			" pushscope\n"+
			"SP equ >2222\n"+
			" li R2, SP\n"+
			" popscope\n"+
			" ai SP, 10\n"+
			"";
		
		testFileContent(text,
				0x100,
				new String[] {
				"li R2,>2222\n",
				"ai R10,10\n",
				},
				new Symbol[] { new Equate(assembler.getSymbolTable(),  "SP", 10) });
		
	}

	
	private void testFileContent(String text, int pc, String[] stdInsts, Symbol[] symbols) throws Exception {
		String caller = new Exception().fillInStackTrace().getStackTrace()[1].getMethodName();
		assembler.pushContentEntry(new ContentEntry(caller + ".asm", text));
		List<IInstruction> asminsts = assembler.parse();
		List<IInstruction> realinsts = assembler.resolve(asminsts);
		assembler.optimize(realinsts);

		testGeneratedContent(assembler, pc, stdInsts, symbols, realinsts);
	}
}
