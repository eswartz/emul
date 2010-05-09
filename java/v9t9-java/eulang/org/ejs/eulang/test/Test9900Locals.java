/**
 * 
 */
package org.ejs.eulang.test;

import static junit.framework.Assert.*;

import java.util.List;
import java.util.Map;

import org.ejs.eulang.ast.IAstModule;
import org.ejs.eulang.llvm.LLModule;
import org.ejs.eulang.llvm.LLVMGenerator;
import org.ejs.eulang.llvm.directives.LLBaseDirective;
import org.ejs.eulang.llvm.directives.LLDefineDirective;
import org.ejs.eulang.llvm.tms9900.Block;
import org.ejs.eulang.llvm.tms9900.ILocal;
import org.ejs.eulang.llvm.tms9900.Locals;
import org.ejs.eulang.llvm.tms9900.Routine;
import org.ejs.eulang.llvm.tms9900.BackEnd;
import org.ejs.eulang.llvm.tms9900.StackLocal;
import org.ejs.eulang.symbols.ISymbol;
import org.junit.Test;

import v9t9.engine.cpu.InstructionTable;
import v9t9.tools.asm.assembler.HLInstruction;

/**
 * @author ejs
 *
 */
public class Test9900Locals extends BaseParserTest {


	protected Locals doLocals(String text) throws Exception {
		LLModule mod = getModule(text);
		for (LLBaseDirective dir : mod.getDirectives()) {
			if (dir instanceof LLDefineDirective) {
				LLDefineDirective def = (LLDefineDirective) dir;
				Locals locals = new Locals(v9t9Target);
				locals.buildLocalTable(def);
				return locals;
				
			}
		}
		fail("no code generated:\n" + mod);
		return null;
	}
	
	private ILocal getLocal(Map<ISymbol, ILocal> localMap, String string) {
		for (Map.Entry<ISymbol, ILocal> entry : localMap.entrySet()) {
			if (entry.getKey().getName().equals(string))
				return entry.getValue();
			if (getSimpleName(entry.getKey()).equals(string))
				return entry.getValue();
		}
		return null;
	}
	private String getSimpleName(ISymbol sym) {
		String name = sym.getName();
		if (name.startsWith("_."))
			name = name.substring(2);
		int idx = name.indexOf('.');
		if (idx < 0)
			return name;
		return name.substring(0, idx);
	}
	
	@Test
	public void testEmpty() throws Exception {
		Locals locals = doLocals("foo = code() { };\n");
		assertEquals(0, locals.getLocals().size());
	}
	@Test
	public void testOnlyLocals1() throws Exception {
		Locals locals = doLocals("foo = code() { x : Int = 10; };\n");
		Map<ISymbol, ILocal> localMap = locals.getLocals();
		assertEquals(1, localMap.size());
		StackLocal l;
		l = (StackLocal) getLocal(localMap, "x");
		assertNotNull(l);
		assertEquals(2, l.getSize());
		assertEquals(typeEngine.INT, l.getType());
		assertEquals(0, l.getOffset());
		assertEquals(2, locals.getFrameSize());
	}
	@Test
	public void testOnlyLocals2() throws Exception {
		Locals locals = doLocals("foo = code() { x, y : Int = 10; };\n");
		Map<ISymbol, ILocal> localMap = locals.getLocals();
		assertEquals(2, localMap.size());
		StackLocal l;
		l = (StackLocal) getLocal(localMap, "x");
		assertEquals(2, l.getSize());
		assertEquals(typeEngine.INT, l.getType());
		assertEquals(0, l.getOffset());
		l = (StackLocal) getLocal(localMap, "y");
		assertEquals(2, l.getSize());
		assertEquals(typeEngine.INT, l.getType());
		assertEquals(-2, l.getOffset());
		assertEquals(4, locals.getFrameSize());
	}
	@Test
	public void testOnlyLocals3() throws Exception {
		Locals locals = doLocals("foo = code() { x : Int; y : Float; z : Int[10]; };\n");
		Map<ISymbol, ILocal> localMap = locals.getLocals();
		assertEquals(3, localMap.size());
		
		StackLocal l;
		l = (StackLocal) getLocal(localMap, "x");
		assertEquals(2, l.getSize());
		assertEquals(typeEngine.INT, l.getType());
		assertEquals(0, l.getOffset());
		
		l = (StackLocal) getLocal(localMap, "y");
		assertEquals(4, l.getSize());
		assertEquals(typeEngine.FLOAT, l.getType());
		assertEquals(-2, l.getOffset());
		
		l = (StackLocal) getLocal(localMap, "z");
		assertEquals(20, l.getSize());
		assertEquals(typeEngine.getArrayType(typeEngine.INT, 10, null), l.getType());
		assertEquals(-6, l.getOffset());
		
		assertEquals(26, locals.getFrameSize());
	}
	@Test
	public void testLocalsBlocks1() throws Exception {
		// loops introduce counter and loopvalue 
		Locals locals = doLocals(
				"foo = code() {\n"+
				"x : Int; \n" +
				"repeat 10 do {\n"+
				"	x = x + 1; y := x * x * x;\n"+
				"	x = x + y;\n"+
				"};\n"+
				"};\n");
		Map<ISymbol, ILocal> localMap = locals.getLocals();
		assertEquals(4, localMap.size());
		
		StackLocal l;
		l = (StackLocal) getLocal(localMap, "x");
		assertEquals(2, l.getSize());
		assertEquals(typeEngine.INT, l.getType());
		assertEquals(0, l.getOffset());
		
		l = (StackLocal) getLocal(localMap, "loopValue");
		assertEquals(2, l.getSize());
		assertEquals(typeEngine.INT, l.getType());
		assertEquals(-2, l.getOffset());
		
		l = (StackLocal) getLocal(localMap, "counter");
		assertEquals(2, l.getSize());
		assertEquals(typeEngine.INT, l.getType());
		assertEquals(-4, l.getOffset());
		
		l = (StackLocal) getLocal(localMap, "y");
		assertEquals(2, l.getSize());
		assertEquals(typeEngine.INT, l.getType());
		assertEquals(-6, l.getOffset());
		
		assertEquals(8, locals.getFrameSize());
	}
}
