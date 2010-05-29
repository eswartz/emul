/**
 * Test that we can simplify the AST
 * 
 */
package org.ejs.eulang.test;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.ejs.eulang.IOperation;
import org.ejs.eulang.ast.DumpAST;
import org.ejs.eulang.ast.IAstAllocStmt;
import org.ejs.eulang.ast.IAstAssignStmt;
import org.ejs.eulang.ast.IAstBinExpr;
import org.ejs.eulang.ast.IAstBoolLitExpr;
import org.ejs.eulang.ast.IAstCodeExpr;
import org.ejs.eulang.ast.IAstExprStmt;
import org.ejs.eulang.ast.IAstFloatLitExpr;
import org.ejs.eulang.ast.IAstIntLitExpr;
import org.ejs.eulang.ast.IAstModule;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.IAstUnaryExpr;
import org.ejs.eulang.types.BasicType;
import org.ejs.eulang.types.LLType;
import org.junit.Test;

/**
 * @author ejs
 *
 */
public class TestSimplify extends BaseTest {
	{
		dumpSimplify = true;
	}
	@Test
    public void testPromotedCast2() throws Exception {
    	IAstModule mod = treeize(
    			"testPromotedCast2 := code () {\n" +
    			"   z : Byte;\n" +
    			"	z = z * Byte(100) / 5;\n" +
    			"};");
    	sanityTest(mod);

    	doSimplify(mod);
    	
    	IAstAllocStmt def = (IAstAllocStmt) mod.getScope().getNode("testPromotedCast2");
    	typeTest(mod, false);
    	
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExprs().getFirst();
    	
    	IAstAssignStmt allocStmt = (IAstAssignStmt) codeExpr.stmts().list().get(1);
		assertEquals(typeEngine.BYTE, allocStmt.getType());
		IAstUnaryExpr castExpr = (IAstUnaryExpr) allocStmt.getExprs().getFirst();
		assertEquals(typeEngine.INT, castExpr.getExpr().getType());
		IAstBinExpr divExpr = (IAstBinExpr) castExpr.getExpr();
		assertEquals(typeEngine.INT, divExpr.getLeft().getType());
		assertEquals(typeEngine.INT, divExpr.getRight().getType());
		assertTrue(isCastTo(divExpr.getLeft(), typeEngine.INT));
		IAstBinExpr mulExpr = (IAstBinExpr) ((IAstUnaryExpr) divExpr.getLeft()).getExpr();
		assertEquals(typeEngine.BYTE, mulExpr.getType());
		assertEquals(typeEngine.BYTE, mulExpr.getLeft().getType());
		assertEquals(typeEngine.BYTE, mulExpr.getRight().getType());
		assertEquals(100, ((IAstIntLitExpr) mulExpr.getRight()).getValue());
    }
	
	@Test
    public void testPromotedCond1() throws Exception {
    	IAstModule mod = treeize(
    			"testPromotedCond1 := code () {\n" +
    			"   z : Byte;\n" +
    			"	z = true;\n" +
    			"};");
    	sanityTest(mod);

    	doSimplify(mod);
    	
    	IAstAllocStmt def = (IAstAllocStmt) mod.getScope().getNode("testPromotedCond1");
    	typeTest(mod, false);
    	
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExprs().getFirst();
    	
    	IAstAssignStmt allocStmt = (IAstAssignStmt) codeExpr.stmts().list().get(1);
		assertEquals(typeEngine.BYTE, allocStmt.getType());

		IAstIntLitExpr litExpr = (IAstIntLitExpr) allocStmt.getExprs().getFirst();
		assertEquals(1, litExpr.getValue());
		
    }
	
	@Test
    public void testPromotedCond2() throws Exception {
    	IAstModule mod = treeize(
    			"testPromotedCond2 := code () {\n" +
    			"   z : Byte;\n" +
    			"	z = z > Byte(100);\n" +
    			"};");
    	sanityTest(mod);

    	doSimplify(mod);
    	
    	IAstAllocStmt def = (IAstAllocStmt) mod.getScope().getNode("testPromotedCond2");
    	typeTest(mod, false);
    	
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExprs().getFirst();
    	
    	IAstAssignStmt allocStmt = (IAstAssignStmt) codeExpr.stmts().list().get(1);
		assertEquals(typeEngine.BYTE, allocStmt.getType());
		IAstUnaryExpr castExpr = (IAstUnaryExpr) allocStmt.getExprs().getFirst();
		assertTrue(isCastTo(allocStmt.getExprs().getFirst(), typeEngine.BYTE));
		IAstBinExpr cmpExpr = (IAstBinExpr)  castExpr.getExpr();
		assertEquals(typeEngine.BOOL, cmpExpr.getType());
		assertEquals(typeEngine.BYTE, cmpExpr.getLeft().getType());
		assertEquals(typeEngine.BYTE, cmpExpr.getRight().getType());
		
		assertEquals(100, ((IAstIntLitExpr) cmpExpr.getRight()).getValue());
		
    }
	
	@Test
    public void testUnaryNot() throws Exception {
    	IAstModule mod = treeize(
    			"testUnaryNot := code () {\n" +
    			"   z : Byte;\n" +
    			"	 not -~z;\n" +
    			"};");
    	sanityTest(mod);

    	doSimplify(mod);
    	
    	IAstAllocStmt def = (IAstAllocStmt) mod.getScope().getNode("testUnaryNot");
    	typeTest(mod, false);
    	
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExprs().getFirst();
    	
    	//IAstReturnStmt allocStmt = (IAstReturnStmt) codeExpr.stmts().list().get(1);
    	IAstExprStmt exprStmt = (IAstExprStmt) codeExpr.stmts().list().get(1);
		assertEquals(typeEngine.BOOL, exprStmt.getType());
		IAstBinExpr cmpExpr = (IAstBinExpr) exprStmt.getExpr();
		assertTrue(cmpExpr.getOp() == IOperation.COMPEQ);
		assertEquals(typeEngine.BOOL, cmpExpr.getType());
		assertEquals(typeEngine.BYTE, cmpExpr.getLeft().getType());
		assertEquals(typeEngine.BYTE, cmpExpr.getRight().getType());
		
		// there will be removed cast here
		assertEquals(0, ((IAstIntLitExpr) cmpExpr.getRight()).getValue());

		IAstUnaryExpr negExpr = (IAstUnaryExpr) cmpExpr.getLeft();
		assertTrue(negExpr.getOp() == IOperation.NEG);
		assertEquals(typeEngine.BYTE, negExpr.getExpr().getType());
		IAstUnaryExpr invExpr = (IAstUnaryExpr) negExpr.getExpr();
		assertTrue(invExpr.getOp() == IOperation.INV);
		assertEquals(typeEngine.BYTE, invExpr.getExpr().getType());
		
    }
	
	protected void noSimplifyTest(String text) throws Exception {
		IAstModule mod = treeize(text);
		sanityTest(mod);
		IAstModule orig = mod.copy(null);
		assertEquals(orig, mod);
		doTypeInfer(orig);
		System.out.println(DumpAST.dumpString(orig));
		doSimplify(mod);
		System.out.println(DumpAST.dumpString(mod));
		assertEquals(orig, mod);
	}

	@Test
	public void testNoSimplify() throws Exception {
		noSimplifyTest("f = code(x) { x%1.0 };\n");
		noSimplifyTest("f = code() { 1.0%0.0 };\n");
		noSimplifyTest("f = code() { 1/0 };\n");
		noSimplifyTest("f = code() { 1+/0 };\n");
	}

	protected void doSimplifyTest(String text, LLType type, int value) throws Exception {
    	IAstModule mod = treeize(
    			"testBinaryArithOps := code () {\n" + text + ";\n};");
    	sanityTest(mod);
    	doSimplify(mod);
    	
    	IAstAllocStmt def = (IAstAllocStmt) mod.getScope().getNode("testBinaryArithOps");
    	typeTest(mod, false);
    	
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExprs().getFirst();
    	IAstExprStmt exprStmt = (IAstExprStmt) codeExpr.stmts().getFirst();
		assertEquals(type, exprStmt.getType());
		
		IAstTypedExpr expr = exprStmt.getExpr();
		assertTrue(type.getBasicType() == BasicType.INTEGRAL ? expr instanceof IAstIntLitExpr
				: type.getBasicType() == BasicType.BOOL ? expr instanceof IAstBoolLitExpr : false);
		if (type.getBasicType() == BasicType.BOOL)
			assertEquals(value, ((IAstBoolLitExpr) expr).getValue() ? 1 : 0);
		else
			assertEquals(value, ((Number)((IAstIntLitExpr) expr).getObject()).intValue());
    }
	

	@Test
    public void testCastOps() throws Exception {
		doSimplifyTest("255{Byte}", typeEngine.BYTE, -1);
		doSimplifyTest("Byte(255)", typeEngine.BYTE, -1);
		doSimplifyTest("0x1001{Byte}", typeEngine.BYTE, 1);
		doSimplifyTest("Byte(0x1001)", typeEngine.BYTE, 1);
		doSimplifyTest("0x1001{Int}", typeEngine.INT, 0x1001);
		doSimplifyTest("Int(0x1001)", typeEngine.INT, 0x1001);
    }
	@Test
	public void testUnaryOps() throws Exception {
		doSimplifyTest("-255{Byte}", typeEngine.BYTE, 1);
		doSimplifyTest("255++", typeEngine.INT, 256);
		doSimplifyTest("255{Byte}++", typeEngine.BYTE, 0);
		doSimplifyTest("~255{Byte}", typeEngine.BYTE, 0);
		doSimplifyTest("~255", typeEngine.INT, (short)0xff00);
	}
	

	@Test
    public void testBinaryArithOps() throws Exception {
		doSimplifyTest("6{Byte} + 65535{Int}", typeEngine.INT, 5);
		doSimplifyTest("6{Byte} - 255{Byte}", typeEngine.BYTE, 7);
		doSimplifyTest("32767 * 2", typeEngine.INT, -2);
		doSimplifyTest("65535 / 2", typeEngine.INT, 0);		//-1 / 2
		doSimplifyTest("65535 +/ 2", typeEngine.INT, 32767);		//-1 / 2
		doSimplifyTest("65534 / -2", typeEngine.INT, 1);		// -2/-2
		doSimplifyTest("-123 / 2", typeEngine.INT, -61);
		doSimplifyTest("-123{Byte} +/ 2{Byte}", typeEngine.BYTE, 66);
		doSimplifyTest("65535 +/ -49152", typeEngine.INT, 3);		// == 65535 / 16384
		doSimplifyTest("127 % 64", typeEngine.INT, 63);
    }
	
	
	@Test
	public void testBinaryShiftOps() throws Exception {
		doSimplifyTest("10 << 2", typeEngine.INT, 40);
		doSimplifyTest("254{Byte} >> 1", typeEngine.BYTE, -1);
		doSimplifyTest("254{Byte} +>> 1", typeEngine.BYTE, 127);
		doSimplifyTest("128{Byte} << 1", typeEngine.BYTE, 0);
		doSimplifyTest("128 << 1", typeEngine.INT, 256);
		doSimplifyTest("16384 << 2", typeEngine.INT, 0);
		doSimplifyTest("16385 <<| 2", typeEngine.INT, 1+4);
		doSimplifyTest("0x9{Byte} >>| 4", typeEngine.BYTE, (byte)0x90);
	}

	@Test
	public void testBinaryCompOps() throws Exception {
		doSimplifyTest("10 > 2", typeEngine.BOOL, 1);
		doSimplifyTest("254{Byte} < 1", typeEngine.BOOL, 1);
		doSimplifyTest("254{Byte} +< 1{Byte}", typeEngine.BOOL, 0);
		doSimplifyTest("254{Byte} +< 1", typeEngine.BOOL, 0);
		doSimplifyTest("65534{Byte} +> 1", typeEngine.BOOL, 1);
		doSimplifyTest("65534 > 1", typeEngine.BOOL, 0);
		doSimplifyTest("254 < 1", typeEngine.BOOL, 0);
	}
	@Test
	public void testBinaryLogCompOps() throws Exception {
		doSimplifyTest("10{Bool} and true", typeEngine.BOOL, 1);
		doSimplifyTest("10{Bool} and (1>2)", typeEngine.BOOL, 0);
		doSimplifyTest("false or true", typeEngine.BOOL, 1);
		dumpTreeize = true;
		doSimplifyTest("(not false) and true", typeEngine.BOOL, 1);
	}
	
	
	
}


