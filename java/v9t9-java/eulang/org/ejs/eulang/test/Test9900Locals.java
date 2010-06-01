/**
 * 
 */
package org.ejs.eulang.test;

import static junit.framework.Assert.*;

import java.util.Map;

import org.ejs.eulang.ICallingConvention;
import org.ejs.eulang.ICallingConvention.Location;
import org.ejs.eulang.ICallingConvention.StackBarrierLocation;
import org.ejs.eulang.ICallingConvention.StackLocation;
import org.ejs.eulang.llvm.FunctionConvention;
import org.ejs.eulang.llvm.LLModule;
import org.ejs.eulang.llvm.directives.LLBaseDirective;
import org.ejs.eulang.llvm.directives.LLDefineDirective;
import org.ejs.eulang.llvm.tms9900.ILocal;
import org.ejs.eulang.llvm.tms9900.StackFrame;
import org.ejs.eulang.llvm.tms9900.RegisterLocal;
import org.ejs.eulang.llvm.tms9900.StackLocal;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLArrayType;
import org.ejs.eulang.types.LLDataType;
import org.ejs.eulang.types.LLSymbolType;
import org.ejs.eulang.types.LLType;
import org.junit.Test;

/**
 *    
 * @author ejs
 *
 */
public class Test9900Locals extends BaseTest {

	protected boolean forceLocalsToStack = false;

	protected StackFrame doStackFrame(String text) throws Exception {
		LLModule mod = getModule(text);
		for (LLBaseDirective dir : mod.getDirectives()) {
			if (dir instanceof LLDefineDirective) {
				LLDefineDirective def = (LLDefineDirective) dir;
				StackFrame stackFrame = new StackFrame(def.getTarget());
				stackFrame.setForceLocalsToStack(forceLocalsToStack);
				stackFrame.buildLocalTable(def);
				return stackFrame;
				
			}
		}
		fail("no code generated:\n" + mod);
		return null;
	}
	
	private ILocal getLocal(Map<ISymbol, ? extends ILocal> localMap, String string) {
		for (Map.Entry<ISymbol, ? extends ILocal> entry : localMap.entrySet()) {
			if (entry.getKey().getName().equals(string))
				return entry.getValue();
			if (getSimpleName(entry.getKey()).equals(string))
				return entry.getValue();
		}
		for (Map.Entry<ISymbol, ? extends ILocal> entry : localMap.entrySet()) {
			if (entry.getKey().getName().startsWith(string))
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
		StackFrame stackFrame = doStackFrame("foo = code() { };\n");
		assertEquals(0, stackFrame.getStackLocals().size());
	}
	@Test
	public void testOnlyLocals1() throws Exception {
		forceLocalsToStack = true;
		dumpLLVMGen =true;
		StackFrame stackFrame = doStackFrame("foo = code() { x : Int = 10; };\n");
		Map<ISymbol, ? extends ILocal> localMap = stackFrame.getStackLocals();
		assertEquals(1, localMap.size());
		StackLocal l;
		l = (StackLocal) getLocal(localMap, "x");
		assertNotNull(l);
		assertEquals(typeEngine.INT, l.getType());
		assertEquals(0, l.getOffset());
		assertEquals(2, stackFrame.getFrameSize());
	}
	@Test
	public void testOnlyLocals2() throws Exception {
		forceLocalsToStack = true;
		StackFrame stackFrame = doStackFrame("foo = code() { x, y : Int = 10; };\n");
		Map<ISymbol, ? extends ILocal> localMap = stackFrame.getStackLocals();
		assertEquals(2, localMap.size());
		StackLocal l;
		l = (StackLocal) getLocal(localMap, "x");
		assertEquals(typeEngine.INT, l.getType());
		assertEquals(0, l.getOffset());
		l = (StackLocal) getLocal(localMap, "y");
		assertEquals(typeEngine.INT, l.getType());
		assertEquals(-2, l.getOffset());
		assertEquals(4, stackFrame.getFrameSize());
	}
	@Test
	public void testOnlyLocals3() throws Exception {
		
		dumpLLVMGen = true;
		
		forceLocalsToStack = true;
		StackFrame stackFrame = doStackFrame("foo = code( => nil) { x : Int; y : Float; z : Int[10]; };\n");
		Map<ISymbol, ? extends ILocal> localMap = stackFrame.getStackLocals();
		assertEquals(3, localMap.size());
		
		StackLocal l;
		l = (StackLocal) getLocal(localMap, "x");
		assertEquals(typeEngine.INT, l.getType());
		assertEquals(0, l.getOffset());
		
		l = (StackLocal) getLocal(localMap, "y");
		assertEquals(typeEngine.FLOAT, l.getType());
		assertEquals(-2, l.getOffset());
		
		l = (StackLocal) getLocal(localMap, "z");
		assertEquals(typeEngine.getArrayType(typeEngine.INT, 10, null), l.getType());
		assertEquals(-6, l.getOffset());
		
		assertEquals(26, stackFrame.getFrameSize());
	}
	
	@Test
	public void testOnlyLocalsAlign1() throws Exception {
		forceLocalsToStack = true;
		StackFrame stackFrame = doStackFrame("foo = code( => nil) { x := 9 < 8; };\n");
		Map<ISymbol, ? extends ILocal> localMap = stackFrame.getStackLocals();
		assertEquals(1, localMap.size());
		
		StackLocal l;
		l = (StackLocal) getLocal(localMap, "x");
		
		assertEquals(typeEngine.BOOL, l.getType());
		assertEquals(0, l.getOffset());
		
		// aligned
		assertEquals(1, stackFrame.getFrameSize());
	}
	@Test
	public void testOnlyLocalsAlign1b() throws Exception {
		forceLocalsToStack = true;
		StackFrame stackFrame = doStackFrame("foo = code( => nil) { x := 9 < 8; y := x;};\n");
		Map<ISymbol, ? extends ILocal> localMap = stackFrame.getStackLocals();
		assertEquals(2, localMap.size());
		
		StackLocal l;
		l = (StackLocal) getLocal(localMap, "x");
		assertEquals(typeEngine.BOOL, l.getType());
		assertEquals(0, l.getOffset());
		
		l = (StackLocal) getLocal(localMap, "y");
		assertEquals(typeEngine.BOOL, l.getType());
		// conserve space
		assertEquals(-1, l.getOffset());
		
		// aligned
		assertEquals(2, stackFrame.getFrameSize());
	}
	@Test
	public void testOnlyLocalsAlign2() throws Exception {
		forceLocalsToStack = true;
		StackFrame stackFrame = doStackFrame("foo = code( => nil) { x := 9 < 8; y := 9.0; };\n");
		Map<ISymbol, ? extends ILocal> localMap = stackFrame.getStackLocals();
		assertEquals(2, localMap.size());
		
		StackLocal l;
		l = (StackLocal) getLocal(localMap, "x");
		assertEquals(typeEngine.BOOL, l.getType());
		assertEquals(0, l.getOffset());
		
		l = (StackLocal) getLocal(localMap, "y");
		assertEquals(typeEngine.FLOAT, l.getType());
		// aligned 
		assertEquals(-2, l.getOffset());
		
		
		assertEquals(6, stackFrame.getFrameSize());
	}
	
	@Test
	public void testLocalsBlocks1() throws Exception {
		// loops introduce counter and loopvalue 
		forceLocalsToStack = true;
		StackFrame stackFrame = doStackFrame(
				"foo = code() {\n"+
				"x : Int; \n" +
				"repeat 10 do {\n"+
				"	x = x + 1; y := x * x * x;\n"+
				"	x = x + y;\n"+
				"};\n"+
				"};\n");
		Map<ISymbol, ? extends ILocal> localMap = stackFrame.getStackLocals();
		assertEquals(4, localMap.size());
		
		StackLocal l;
		l = (StackLocal) getLocal(localMap, "x");
		assertEquals(typeEngine.INT, l.getType());
		assertEquals(0, l.getOffset());
		
		l = (StackLocal) getLocal(localMap, "loopValue");
		assertEquals(typeEngine.INT, l.getType());
		assertEquals(-2, l.getOffset());
		
		l = (StackLocal) getLocal(localMap, "counter");
		assertEquals(typeEngine.INT, l.getType());
		assertEquals(-4, l.getOffset());
		
		l = (StackLocal) getLocal(localMap, "y");
		assertEquals(typeEngine.INT, l.getType());
		assertEquals(-6, l.getOffset());
		
		assertEquals(8, stackFrame.getFrameSize());
		
	}
	
	@Test
	public void testLocalsRegs() throws Exception {
		forceLocalsToStack = true;
		StackFrame stackFrame = doStackFrame("foo = code( => Int) { x := 10; };\n");	// returns
		Map<ISymbol, ? extends ILocal> localMap = stackFrame.getRegLocals();
		assertEquals(1, localMap.size());
		RegisterLocal l;
		l = (RegisterLocal) getLocal(localMap, "%0");
		assertNotNull(l);
		assertEquals(16, l.getVr());
		assertEquals(typeEngine.INT, l.getType());
	}

	@Test
	public void testLocalsRegsNoFloat() throws Exception {
		// no float regs supported; all should go to stack
		StackFrame stackFrame = doStackFrame("foo = code(=>nil) { y : Float = 10; y = y * 10; };\n");
		Map<ISymbol, ? extends ILocal> localMap = stackFrame.getRegLocals();
		assertEquals(0, localMap.size());
		localMap = stackFrame.getStackLocals();
		assertEquals(3, localMap.size());
	}

	@Test
	public void testArgs1() throws Exception {
		// all go in regs except 'x'
		StackFrame stackFrame = doStackFrame("foo = code(a:Int; b:Int^; x:Int[5]; c:Bool; d:Byte) { };\n");
		Map<ISymbol, ? extends ILocal> stackLocalMap;

		RegisterLocal reg;
		Map<ISymbol, ? extends ILocal> regLocalMap;
		regLocalMap = stackFrame.getRegLocals();
		
		// four enregistered arguments; four mirrors
		assertEquals(8, regLocalMap.size());
		reg = (RegisterLocal) getLocal(regLocalMap, "a");
		assertEquals(0, reg.getVr());
		assertEquals(typeEngine.INT, reg.getType());
		reg = (RegisterLocal) getLocal(regLocalMap, "b");
		assertEquals(1, reg.getVr());
		assertEquals(typeEngine.getPointerType(typeEngine.INT), reg.getType());
		reg = (RegisterLocal) getLocal(regLocalMap, "c");
		assertEquals(2, reg.getVr());
		assertEquals(typeEngine.BOOL, reg.getType());
		reg = (RegisterLocal) getLocal(regLocalMap, "d");
		assertEquals(3, reg.getVr());
		assertEquals(typeEngine.BYTE, reg.getType());
		
		// x and x's actual location
		stackLocalMap = stackFrame.getStackLocals();
		assertEquals(2, stackLocalMap.size());
		
		StackLocal real;
		StackLocal mirror;

		LLArrayType theType = typeEngine.getArrayType(typeEngine.INT, 5, null);

		real = (StackLocal) getLocal(stackLocalMap, "x");
		assertNotNull(real);
		assertEquals(theType, real.getType());
		assertEquals(0, real.getOffset());	// from caller
		
		mirror = (StackLocal) getLocal(stackLocalMap, "_.x");
		assertNotNull(mirror);
		assertEquals(theType, mirror.getType());
		assertEquals(0, mirror.getOffset());	// first local, taking same location as caller's
		
		// not using the space for the array
		assertEquals(0, stackFrame.getFrameSize());
	}
	

	@Test
	public void testArgs2() throws Exception {
		// arguments may have mirror locals, but if we know the argument comes in
		// on the stack, then alloc the argument at that spot
		dumpLLVMGen = true;
		StackFrame stackFrame = doStackFrame("foo = code(a,b,c,d:Int; x:Int; y:Bool; z:Float) { };\n");
		Map<ISymbol, ? extends ILocal> localMap;
		
		RegisterLocal reg;
		localMap = stackFrame.getRegLocals();
		
		// four arguments, enregistered, and mirrors; mirrors of x and y 
		assertEquals(10, localMap.size());
		reg = (RegisterLocal) getLocal(localMap, "a");
		assertEquals(0, reg.getVr());
		reg = (RegisterLocal) getLocal(localMap, "b");
		assertEquals(1, reg.getVr());
		reg = (RegisterLocal) getLocal(localMap, "c");
		assertEquals(2, reg.getVr());
		reg = (RegisterLocal) getLocal(localMap, "d");
		assertEquals(3, reg.getVr());
		assertNotNull(getLocal(localMap, "x"));
		assertNotNull(getLocal(localMap, "y"));
		
		localMap = stackFrame.getStackLocals();
		
		// z & mirrors; x,y  
		assertEquals(4, localMap.size());
		
		StackLocal real;
		StackLocal mirror;
		
		real = (StackLocal) getLocal(localMap, "x");
		assertNotNull(real);
		assertEquals(typeEngine.INT, real.getType());
		assertEquals(6, real.getOffset());	// from caller
		
		mirror = (StackLocal) getLocal(localMap, "_.x");
		assertNull(mirror);
		
		real = (StackLocal) getLocal(localMap, "y");
		assertNotNull(real);
		assertEquals(typeEngine.BOOL, real.getType());
		assertEquals(4, real.getOffset());	// from caller
		
		mirror = (StackLocal) getLocal(localMap, "_.y");
		assertNull(mirror);
		
		real = (StackLocal) getLocal(localMap, "z");
		assertNotNull(real);
		assertEquals(typeEngine.FLOAT, real.getType());
		assertEquals(0, real.getOffset());	// from caller
		
		mirror = (StackLocal) getLocal(localMap, "_.z");
		assertNotNull(mirror);
		assertEquals(typeEngine.FLOAT, mirror.getType());
		assertEquals(0, mirror.getOffset());	// first local, taking same location as caller's
		
		// not using the space for the last
		assertEquals(0, stackFrame.getFrameSize());
	}
	

	@Test
	public void testArgs3() throws Exception {
		// arguments may have mirror locals, but if we know the argument comes in
		// on the stack, then alloc the argument at that spot
		dumpLLVMGen = true;
		StackFrame stackFrame = doStackFrame("foo = code(a:Int => nil) { p:Float; };\n");
		Map<ISymbol, ? extends ILocal> localMap;
		
		RegisterLocal reg;
		localMap = stackFrame.getRegLocals();
		
		// one arguments, enregistered; float arg
		assertEquals(2, localMap.size());
		reg = (RegisterLocal) getLocal(localMap, "a");
		assertEquals(0, reg.getVr());
		
		localMap = stackFrame.getStackLocals();
		
		// one for p's actual location
		assertEquals(1, localMap.size());
		
		StackLocal mirror;
		mirror = (StackLocal) getLocal(localMap, "_.p");
		assertNotNull(mirror);
		assertEquals(typeEngine.FLOAT, mirror.getType());
		assertEquals(0, mirror.getOffset());	
		
		assertEquals(4, stackFrame.getFrameSize());
	}
	

	@Test
	public void testArgs4() throws Exception {
		// arg locs should be in same order even if stack and reg args are interleaved 
		dumpLLVMGen = true;
		StackFrame stackFrame = doStackFrame("foo = code(a,b:Float; x:Int; c,d:Float) { };\n");
		Map<ISymbol, ? extends ILocal> localMap;
		
		localMap = stackFrame.getRegLocals();
		RegisterLocal reg = (RegisterLocal) getLocal(localMap, "x");
		assertEquals(0, reg.getVr());

		localMap = stackFrame.getStackLocals();
		
		// stack and mirrors  
		assertEquals(8, localMap.size());
		
		StackLocal real;
		
		real = (StackLocal) getLocal(localMap, "a");
		assertNotNull(real);
		assertEquals(typeEngine.FLOAT, real.getType());
		assertEquals(12, real.getOffset());	// from caller
		
		real = (StackLocal) getLocal(localMap, "b");
		assertNotNull(real);
		assertEquals(typeEngine.FLOAT, real.getType());
		assertEquals(8, real.getOffset());	// from caller
		
		real = (StackLocal) getLocal(localMap, "c");
		assertNotNull(real);
		assertEquals(typeEngine.FLOAT, real.getType());
		assertEquals(4, real.getOffset());	// from caller
		
		real = (StackLocal) getLocal(localMap, "d");
		assertNotNull(real);
		assertEquals(typeEngine.FLOAT, real.getType());
		assertEquals(0, real.getOffset());	// from caller
		
	}
	
	@Test
	public void testClassArgs() throws Exception {
		dumpLLVMGen = true;
		StackFrame stackFrame = doStackFrame(
				"Class = data {\n"+
				"  draw:code(this:Class; count:Int => nil);\n"+
				"};\n"+
    			"testSelfRef3 = code() {\n"+
    			"  inst : Class;\n"+
    			"  inst.draw(inst, 5);\n"+
    			"};\n"+
    	"");
		Map<ISymbol, ? extends ILocal> localMap;
		
		localMap = stackFrame.getStackLocals();
		StackLocal klass;
		klass = (StackLocal) getLocal(localMap, "inst");
		assertEquals(16, klass.getType().getBits());
		assertEquals(4, stackFrame.getFrameSize());
		
		FunctionConvention fconv = FunctionConvention.create(typeEngine, null, typeEngine.getCodeType(typeEngine.VOID, 
				new LLType[] { new LLSymbolType(((LLDataType)klass.getType()).getSymbol()), typeEngine.INT }));
		ICallingConvention cconv = v9t9Target.getCallingConvention(fconv);
		Location[] argLocs = cconv.getArgumentLocations();
		assertTrue(argLocs[0] instanceof ICallingConvention.StackLocation);
		ICallingConvention.StackLocation sloc = (StackLocation) argLocs[0];
		assertEquals(klass.getType(), sloc.type);
		assertTrue(argLocs[1] instanceof ICallingConvention.RegisterLocation);
		assertTrue(argLocs[2] instanceof ICallingConvention.StackBarrierLocation);
		ICallingConvention.StackBarrierLocation sbloc = (StackBarrierLocation) argLocs[2];
		assertEquals(2, sbloc.getPushedArgumentsSize());
	}
}
