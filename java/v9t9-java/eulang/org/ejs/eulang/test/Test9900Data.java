/**
 * 
 */
package org.ejs.eulang.test;

import static org.junit.Assert.*;

import org.ejs.eulang.llvm.tms9900.DataBlock;
import org.ejs.eulang.llvm.tms9900.asm.NumOperand;
import org.ejs.eulang.llvm.tms9900.asm.SymbolOperand;
import org.ejs.eulang.llvm.tms9900.asm.TupleTempOperand;
import org.ejs.eulang.types.LLArrayType;
import org.ejs.eulang.types.LLCodeType;
import org.ejs.eulang.types.LLIntType;
import org.ejs.eulang.types.LLTupleType;
import org.ejs.eulang.types.LLType;
import org.junit.Test;

import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;

/**
 * @author ejs
 *
 */
public class Test9900Data extends BaseInstrTest {

	@Test
	public void testByte() throws Exception {
		DataBlock data = doData("foo := 5{Byte};\n");
		assertNotNull(data);
		assertEquals(typeEngine.BYTE, data.getValue().getType());
		assertEquals(5, ((NumOperand)data.getValue()).getValue());
	}

	@Test
	public void testTuple1() throws Exception {
		DataBlock data = doData("foo := (5, 100{Byte});\n");
		assertNotNull(data);
		LLTupleType tupleType = typeEngine.getTupleType(new LLType[] { typeEngine.INT, typeEngine.BYTE });
		assertEquals(tupleType, data.getValue().getType());
		assertEquals(new TupleTempOperand(tupleType,
				new AssemblerOperand[] { new NumOperand(typeEngine.BYTE, 5), new NumOperand(typeEngine.BYTE, 100) }),
				 data.getValue());
	}

	@Test
	public void testArray1() throws Exception {
		DataBlock data = doData("foo : Byte[] = [5, 100{Byte}];\n");
		assertNotNull(data);
		LLArrayType arrayType = typeEngine.getArrayType(typeEngine.BYTE, 2, null);
		assertEquals(arrayType, data.getValue().getType());
		assertEquals(new TupleTempOperand(arrayType,
				new AssemblerOperand[] { new NumOperand(typeEngine.BYTE, 5), new NumOperand(typeEngine.BYTE, 100) }),
				data.getValue());
	}
	
	@Test
	public void testArray1b() throws Exception {
		DataBlock data = doData("foo : Byte[5] = [5, 100{Byte}];\n");
		assertNotNull(data);
		LLIntType B = typeEngine.BYTE;
		LLArrayType arrayType = typeEngine.getArrayType(B,5, null);
		assertEquals(arrayType, data.getValue().getType());
		assertEquals(new TupleTempOperand(arrayType,
				new AssemblerOperand[] { new NumOperand(B, 5), new NumOperand(B, 100),
				new NumOperand(B, 0), new NumOperand(B, 0), new NumOperand(B, 0),
				}),
				 data.getValue());
	}
	
	@Test
	public void testFuncPtr1() throws Exception {
		DataBlock data = doData(
				"defaultNew = code(x:Int=>Int^) { nil };\n"+
				"new : code(x:Int=>Int^) = defaultNew;\n");
		assertNotNull(data);
		LLCodeType code = typeEngine.getCodeType(typeEngine.getPointerType(typeEngine.INT), new LLType[] { typeEngine.INT });
		assertEquals(typeEngine.getPointerType(code), data.getValue().getType());
		assertTrue(data.getValue() instanceof SymbolOperand);
		assertSameSymbol(((SymbolOperand) data.getValue()).getSymbol(), "defaultNew");
	}
	

	@Test
	public void testConstExpr1() throws Exception {
		DataBlock data = doData(
				"defaultNew = code(x:Int=>Int^) { nil };\n"+
				"otherNew = code(x:Int=>Int^) { nil };\n"+
				"FLAG1=1;\n"+
				"FLAG2=false;\n"+
				"FLAG3=0;\n"+
				// main simplification tests in TestSimplify
				"new : code(x:Int=>Int^) = if (FLAG2 and FLAG1+3>FLAG3 or FLAG3 == 9 or not (FLAG1<<2)&2){Bool} then defaultNew else otherNew;\n"+
				"");
		assertNotNull(data);
		LLCodeType code = typeEngine.getCodeType(typeEngine.getPointerType(typeEngine.INT), new LLType[] { typeEngine.INT });
		assertEquals(typeEngine.getPointerType(code), data.getValue().getType());
		assertTrue(data.getValue() instanceof SymbolOperand);
		assertSameSymbol(((SymbolOperand) data.getValue()).getSymbol(), "defaultNew");
	}
}
