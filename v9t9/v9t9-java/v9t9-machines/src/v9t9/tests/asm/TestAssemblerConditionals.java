package v9t9.tests.asm;

import java.util.List;

import v9t9.engine.cpu.IInstruction;
import v9t9.tests.BaseTest;
import v9t9.tools.asm.assembler.Assembler;
import v9t9.tools.asm.assembler.ContentEntry;
import v9t9.tools.asm.assembler.Equate;
import v9t9.tools.asm.assembler.Symbol;

public class TestAssemblerConditionals extends BaseTest {

	Assembler assembler = new Assembler();
	

	public void testConditionalIf() throws Exception {
		String text =
			"SP equ 10\n"+
			" aorg >100\n"+
			" #if FOO\n" +
			" ai SP, 6\n"+
			" #fi\n"+
			" rt\n"+
			"";
		
		testFileContent(
				new String[] { "FOO" },
				text,
				0x100,
				new String[] { "ai r10, 6", "b *r11" },
				new Symbol[] { new Equate(assembler.getSymbolTable(), "SP", 10) });
		
	}
	public void testConditionalIfElse() throws Exception {
		String text =
			"SP equ 10\n"+
			" aorg >100\n"+
			" #if FOO\n" +
			" ai SP, 6\n"+
			" #else\n"+
			" ai SP, 4\n"+
			" #fi\n"+
			" rt\n"+
			"";
		
		testFileContent(
				new String[] { "FOO=1" },
				text,
				0x100,
				new String[] { "ai r10, 6", "b *r11"},
				new Symbol[] { new Equate(assembler.getSymbolTable(), "SP", 10) });
		
		testFileContent(
				new String[] { "FOO=0" },
				text,
				0x100,
				new String[] { "ai r10, 4", "b *r11"},
				new Symbol[] { new Equate(assembler.getSymbolTable(), "SP", 10) });
		
		
	}
	
	public void testConditionalIfUndefined() throws Exception {
		String text =
			"SP equ 10\n"+
			" aorg >100\n"+
			" #if FOO\n" +
			" ai SP, 6\n"+
			" #else\n"+
			" ai SP, 4\n"+
			" #fi\n"+
			" rt\n"+
			"";
		
		// undefined symbols parse as 0
		testFileContent(
				new String[] { },
				text,
				0x100,
				new String[] { "ai r10, 4", "b *r11"},
				new Symbol[] { new Equate(assembler.getSymbolTable(), "SP", 10) });
	}

	public void testConditionalIfEquates() throws Exception {
		String text =
			" #if FOO\n" +
			"ADDR1 equ 1\n"+
			"ADDR2 equ 2\n"+
			" #else\n"+
			"ADDR1 equ 2\n"+
			"ADDR2 equ 1\n"+
			" #fi\n"+
			"";
		
		testFileContent(
				new String[] { "FOO=1" },
				text,
				0x100,
				new String[] { },
				new Symbol[] { new Equate(assembler.getSymbolTable(), "ADDR1", 1),
						new Equate(assembler.getSymbolTable(), "ADDR2", 2)});
	}
	private void testFileContent(
			String[] equates,
			String text, int pc, String[] stdInsts, Symbol[] symbols) throws Exception {
		String caller = new Exception().fillInStackTrace().getStackTrace()[1].getMethodName();
		assembler.getSymbolTable().clear();

		for (String equ : equates)
			assembler.defineEquate(equ);
		assembler.pushContentEntry(new ContentEntry(caller + ".asm", text));
		List<IInstruction> asminsts = assembler.parse();
		List<IInstruction> realinsts = assembler.resolve(asminsts);
		realinsts = assembler.optimize(realinsts);

		testGeneratedContent(assembler, pc, stdInsts, symbols, realinsts);
	}
}
