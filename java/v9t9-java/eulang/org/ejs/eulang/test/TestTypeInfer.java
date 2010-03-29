/**
 * Test that we can infer the types on an AST
 * 
 */
package org.ejs.eulang.test;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.ejs.eulang.ast.DumpAST;
import org.ejs.eulang.ast.IAstAllocStmt;
import org.ejs.eulang.ast.IAstAssignStmt;
import org.ejs.eulang.ast.IAstBinExpr;
import org.ejs.eulang.ast.IAstCodeExpr;
import org.ejs.eulang.ast.IAstDefineStmt;
import org.ejs.eulang.ast.IAstIntLitExpr;
import org.ejs.eulang.ast.IAstModule;
import org.ejs.eulang.ast.IAstPrototype;
import org.ejs.eulang.ast.IAstReturnStmt;
import org.ejs.eulang.ast.IAstUnaryExpr;
import org.ejs.eulang.ast.IOperation;
import org.ejs.eulang.ast.Message;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeInference;
import org.junit.Test;

/**
 * @author ejs
 *
 */
public class TestTypeInfer extends BaseParserTest {
	

	@Test
    public void testNoChange1() throws Exception {
    	IAstModule mod = treeize(
    			"testNoChange1 = code (x : Int, y : Int => Int) {\n" +
    			"   return x+10;\n" +
    			"};");
    	sanityTest(mod);

    	doTypeInfer(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testNoChange1");
    	typeTest(mod, false);
    	
    	assertEquals(typeEngine.getCodeType(typeEngine.INT,  new LLType[] {typeEngine.INT, typeEngine.INT}), def.getExpr().getType());
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
		IAstPrototype prototype = codeExpr.getPrototype();
		assertEquals(typeEngine.INT, prototype.returnType().getType());
    	assertEquals(typeEngine.INT, prototype.argumentTypes()[0].getType());
    	assertEquals(typeEngine.INT, prototype.argumentTypes()[1].getType());
    	assertEquals(typeEngine.INT, ((IAstReturnStmt) codeExpr.stmts().list().get(0)).getType());
    }

	 @Test 
    public void testBinOps() throws Exception {
    	IAstModule mod = treeize("testBinOps = code { x:=(1*2/3&&4%%45||5<=6>>7<<8>>>85&9^10)||(11<12)>(13<=(14-15)==(16!=17%18+19)); };");
    	sanityTest(mod);
    	
    	doTypeInfer(mod);
    	

    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testBinOps");
    	typeTest(mod, false);
    	
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
    	IAstAllocStmt allocStmt = (IAstAllocStmt) codeExpr.stmts().list().get(0);
    	assertEquals(typeEngine.BOOL, allocStmt.getTypeExpr().getType());
    }

    @Test
    public void testInvalidTypes1() throws Exception {
    	IAstModule mod = treeize(
    			"testInvalidTypes1 = code (x : Int, y : Float) {\n" +
    			"   return y>>1;\n" +
    			"};");
    	sanityTest(mod);

    	doTypeInfer(mod, true);
    }
    
    @Test
    public void testInvalidTypes2() throws Exception {
    	IAstModule mod = treeize(
    			"testInvalidTypes2 = code (x : Int, y : Float) {\n" +
    			"   z : Int = 2.0;\n" +
    			"   return z | y;\n"+
    			"};");
    	sanityTest(mod);

    	doTypeInfer(mod, true);
    }
	    
	@Test
    public void testVoidReturn() throws Exception {
    	IAstModule mod = treeize(
    			"testVoidReturn = code (x : Int, y : Int) {\n" +
    			"};");
    	sanityTest(mod);

    	doTypeInfer(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testVoidReturn");
    	typeTest(mod, false);
    	
    	assertEquals(typeEngine.getCodeType(typeEngine.VOID,  new LLType[] {typeEngine.INT, typeEngine.INT}), def.getExpr().getType());
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
		IAstPrototype prototype = codeExpr.getPrototype();
		assertEquals(typeEngine.VOID, prototype.returnType().getType());
    	assertEquals(typeEngine.INT, prototype.argumentTypes()[0].getType());
    	assertEquals(typeEngine.INT, prototype.argumentTypes()[1].getType());
    	
    }
	@Test
    public void testPromotedCast1() throws Exception {
    	IAstModule mod = treeize(
    			"testPromotedCast1 = code (x : Int, y : Int) {\n" +
    			"   p := x*1.0;\n" +
    			"};");
    	sanityTest(mod);

    	doTypeInfer(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testPromotedCast1");
    	typeTest(mod, false);
    	
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
    	
    	IAstAllocStmt allocStmt = (IAstAllocStmt) codeExpr.stmts().list().get(0);
		assertEquals(typeEngine.FLOAT, allocStmt.getType());
		assertTrue(allocStmt.getExpr() instanceof IAstBinExpr);
		IAstBinExpr binExpr = (IAstBinExpr) allocStmt.getExpr();
		assertEquals(typeEngine.FLOAT, binExpr.getLeft().getType());
		assertTrue(binExpr.getLeft() instanceof IAstUnaryExpr && ((IAstUnaryExpr) binExpr.getLeft()).getOp() == IOperation.CAST);
		assertEquals(typeEngine.FLOAT, binExpr.getRight().getType());
    }
	@Test
    public void testPromotedCast2() throws Exception {
    	IAstModule mod = treeize(
    			"testPromotedCast2 = code () {\n" +
    			"   z : Byte;\n" +
    			"	z = z * Byte(100) / 5;\n" +
    			"};");
    	sanityTest(mod);

    	doTypeInfer(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testPromotedCast2");
    	typeTest(mod, false);
    	
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
    	
    	IAstAssignStmt allocStmt = (IAstAssignStmt) codeExpr.stmts().list().get(0);
		assertEquals(typeEngine.BYTE, allocStmt.getType());
		IAstUnaryExpr castExpr = (IAstUnaryExpr) allocStmt.getExpr();
		assertEquals(typeEngine.INT, castExpr.getExpr().getType());
		IAstBinExpr divExpr = (IAstBinExpr) castExpr.getExpr();
		assertEquals(typeEngine.INT, divExpr.getLeft().getType());
		assertEquals(typeEngine.INT, divExpr.getRight().getType());
		assertTrue(divExpr.getLeft() instanceof IAstUnaryExpr && ((IAstUnaryExpr) divExpr.getLeft()).getOp() == IOperation.CAST);
		IAstBinExpr mulExpr = (IAstBinExpr) ((IAstUnaryExpr) divExpr.getLeft()).getExpr();
		assertEquals(typeEngine.BYTE, mulExpr.getType());
		assertEquals(typeEngine.BYTE, mulExpr.getLeft().getType());
		assertEquals(typeEngine.BYTE, mulExpr.getRight().getType());
    }
	@Test
    public void testPromotedCast3() throws Exception {
    	IAstModule mod = treeize(
    			"testPromotedCast3 = code () {\n" +
    			"   z : Byte;\n" +
    			"	z = z + 1000;\n" +
    			"};");
    	sanityTest(mod);

    	doTypeInfer(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testPromotedCast3");
    	typeTest(mod, false);
    	
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
    	
    	IAstAssignStmt allocStmt = (IAstAssignStmt) codeExpr.stmts().list().get(0);
		assertEquals(typeEngine.BYTE, allocStmt.getType());
		IAstUnaryExpr castExpr = (IAstUnaryExpr) allocStmt.getExpr();
		assertEquals(typeEngine.INT, castExpr.getExpr().getType());
		IAstBinExpr addExpr = (IAstBinExpr) castExpr.getExpr();
		assertEquals(typeEngine.INT, addExpr.getType());
		assertEquals(typeEngine.INT, addExpr.getLeft().getType());
		assertEquals(typeEngine.INT, addExpr.getRight().getType());
		castExpr = (IAstUnaryExpr) addExpr.getLeft();
		assertTrue(castExpr.getOp() == IOperation.CAST);
		assertEquals(typeEngine.BYTE, castExpr.getExpr().getType());
    }
	@Test
    public void testPromotedCond1() throws Exception {
    	IAstModule mod = treeize(
    			"testPromotedCond1 = code () {\n" +
    			"   z : Byte;\n" +
    			"	z = 99 > 100;\n" +
    			"};");
    	sanityTest(mod);

    	doTypeInfer(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testPromotedCond1");
    	typeTest(mod, false);
    	
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
    	
    	IAstAssignStmt allocStmt = (IAstAssignStmt) codeExpr.stmts().list().get(0);
		assertEquals(typeEngine.BYTE, allocStmt.getType());
		IAstUnaryExpr castExpr = (IAstUnaryExpr) allocStmt.getExpr();
		assertTrue(castExpr.getOp() == IOperation.CAST);
		IAstBinExpr cmpExpr = (IAstBinExpr)  castExpr.getExpr();
		assertEquals(typeEngine.BOOL, cmpExpr.getType());
		assertEquals(typeEngine.INT, cmpExpr.getLeft().getType());
		assertEquals(typeEngine.INT, cmpExpr.getRight().getType());
		
    }
	@Test
    public void testPromotedCond2() throws Exception {
    	IAstModule mod = treeize(
    			"testPromotedCond2 = code () {\n" +
    			"   z : Byte;\n" +
    			"	z = z > Byte(100);\n" +
    			"};");
    	sanityTest(mod);

    	doTypeInfer(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testPromotedCond2");
    	typeTest(mod, false);
    	
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
    	
    	IAstAssignStmt allocStmt = (IAstAssignStmt) codeExpr.stmts().list().get(0);
		assertEquals(typeEngine.BYTE, allocStmt.getType());
		IAstUnaryExpr castExpr = (IAstUnaryExpr) allocStmt.getExpr();
		assertTrue(castExpr.getOp() == IOperation.CAST);
		IAstBinExpr cmpExpr = (IAstBinExpr)  castExpr.getExpr();
		assertEquals(typeEngine.BOOL, cmpExpr.getType());
		assertEquals(typeEngine.BYTE, cmpExpr.getLeft().getType());
		assertEquals(typeEngine.BYTE, cmpExpr.getRight().getType());
		
    }
	
	@Test
    public void testUnary1() throws Exception {
    	IAstModule mod = treeize(
    			"testUnary1 = code () {\n" +
    			"   z : Byte;\n" +
    			"	z = -~z;\n" +
    			"};");
    	sanityTest(mod);

    	doTypeInfer(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testUnary1");
    	typeTest(mod, false);
    	
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
    	
    	IAstAssignStmt allocStmt = (IAstAssignStmt) codeExpr.stmts().list().get(0);
		assertEquals(typeEngine.BYTE, allocStmt.getType());

		IAstUnaryExpr negExpr = (IAstUnaryExpr) allocStmt.getExpr();
		assertTrue(negExpr.getOp() == IOperation.NEG);
		assertEquals(typeEngine.BYTE, negExpr.getExpr().getType());
		IAstUnaryExpr invExpr = (IAstUnaryExpr) negExpr.getExpr();
		assertTrue(invExpr.getOp() == IOperation.INV);
		assertEquals(typeEngine.BYTE, invExpr.getExpr().getType());
		
    }
	
	@Test
    public void testUnaryNot() throws Exception {
    	IAstModule mod = treeize(
    			"testUnaryNot = code () {\n" +
    			"   z : Byte;\n" +
    			"	return !-~z;\n" +
    			"};");
    	sanityTest(mod);

    	doTypeInfer(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testUnaryNot");
    	typeTest(mod, false);
    	
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
    	
    	IAstReturnStmt allocStmt = (IAstReturnStmt) codeExpr.stmts().list().get(0);
		assertEquals(typeEngine.BOOL, allocStmt.getType());
		IAstBinExpr cmpExpr = (IAstBinExpr) allocStmt.getExpr();
		assertTrue(cmpExpr.getOp() == IOperation.COMPNE);
		assertEquals(typeEngine.BOOL, cmpExpr.getType());
		assertEquals(typeEngine.BYTE, cmpExpr.getLeft().getType());
		assertEquals(typeEngine.BYTE, cmpExpr.getRight().getType());
		
		// there will be a cast here
		IAstUnaryExpr castExpr = (IAstUnaryExpr) cmpExpr.getRight();
		assertEquals(IOperation.CAST, castExpr.getOp());
		assertEquals(0, ((IAstIntLitExpr) castExpr.getExpr()).getValue());

		IAstUnaryExpr negExpr = (IAstUnaryExpr) cmpExpr.getLeft();
		assertTrue(negExpr.getOp() == IOperation.NEG);
		assertEquals(typeEngine.BYTE, negExpr.getExpr().getType());
		IAstUnaryExpr invExpr = (IAstUnaryExpr) negExpr.getExpr();
		assertTrue(invExpr.getOp() == IOperation.INV);
		assertEquals(typeEngine.BYTE, invExpr.getExpr().getType());
		
    }
	@Test
    public void testDiscoverReturn1() throws Exception {
    	IAstModule mod = treeize(
    			"testDiscoverReturn1 = code (x : Int, y : Int) {\n" +
    			"   return x*1.0;\n" +
    			"};");
    	sanityTest(mod);

    	doTypeInfer(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testDiscoverReturn1");
    	typeTest(mod, false);
    	
    	assertEquals(typeEngine.getCodeType(typeEngine.FLOAT,  new LLType[] {typeEngine.INT, typeEngine.INT}), def.getExpr().getType());
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
		IAstPrototype prototype = codeExpr.getPrototype();
		assertEquals(typeEngine.FLOAT, prototype.returnType().getType());
    	assertEquals(typeEngine.INT, prototype.argumentTypes()[0].getType());
    	assertEquals(typeEngine.INT, prototype.argumentTypes()[1].getType());
    	
    	IAstReturnStmt returnStmt = (IAstReturnStmt) codeExpr.stmts().list().get(0);
		assertEquals(typeEngine.FLOAT, returnStmt.getType());
		assertTrue(returnStmt.getExpr() instanceof IAstBinExpr);
		IAstBinExpr binExpr = (IAstBinExpr) returnStmt.getExpr();
		assertEquals(typeEngine.FLOAT, binExpr.getLeft().getType());
		assertTrue(binExpr.getLeft() instanceof IAstUnaryExpr && ((IAstUnaryExpr) binExpr.getLeft()).getOp() == IOperation.CAST);
		assertEquals(typeEngine.FLOAT, binExpr.getRight().getType());
    }


	@Test
    public void testDiscoverReturn2() throws Exception {
    	IAstModule mod = treeize(
    			"testDiscoverReturn2 = code (x : Int, y : Int) {\n" +
    			"   return x+10.0;\n" +
    			"};");
    	sanityTest(mod);

    	doTypeInfer(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testDiscoverReturn2");
    	typeTest(mod, false);
    	
    	assertEquals(typeEngine.getCodeType(typeEngine.FLOAT,  new LLType[] {typeEngine.INT, typeEngine.INT}), def.getExpr().getType());
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
		IAstPrototype prototype = codeExpr.getPrototype();
		assertEquals(typeEngine.FLOAT, prototype.returnType().getType());
    	assertEquals(typeEngine.INT, prototype.argumentTypes()[0].getType());
    	assertEquals(typeEngine.INT, prototype.argumentTypes()[1].getType());
    	IAstReturnStmt returnStmt = (IAstReturnStmt) codeExpr.stmts().list().get(0);
		assertEquals(typeEngine.FLOAT, returnStmt.getType());
		assertTrue(returnStmt.getExpr() instanceof IAstBinExpr);
		IAstBinExpr binExpr = (IAstBinExpr) returnStmt.getExpr();
		assertEquals(typeEngine.FLOAT, binExpr.getLeft().getType());
		assertTrue(binExpr.getLeft() instanceof IAstUnaryExpr && ((IAstUnaryExpr) binExpr.getLeft()).getOp() == IOperation.CAST);
		assertEquals(typeEngine.FLOAT, binExpr.getRight().getType());
    }
	

	@Test
    public void testCast1() throws Exception {
    	IAstModule mod = treeize(
    			"testCast1 = code (x : Int, y : Int) {\n" +
    			"   return x+10.0;\n" +
    			"};");
    	sanityTest(mod);

    	doTypeInfer(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testCast1");
    	typeTest(mod, false);
    	
    	assertEquals(typeEngine.getCodeType(typeEngine.FLOAT,  new LLType[] {typeEngine.INT, typeEngine.INT}), def.getExpr().getType());
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
		IAstPrototype prototype = codeExpr.getPrototype();
		assertEquals(typeEngine.FLOAT, prototype.returnType().getType());
    	assertEquals(typeEngine.INT, prototype.argumentTypes()[0].getType());
    	assertEquals(typeEngine.INT, prototype.argumentTypes()[1].getType());
    	IAstReturnStmt returnStmt = (IAstReturnStmt) codeExpr.stmts().list().get(0);
		assertEquals(typeEngine.FLOAT, returnStmt.getType());
		assertTrue(returnStmt.getExpr() instanceof IAstBinExpr);
		IAstBinExpr binExpr = (IAstBinExpr) returnStmt.getExpr();
		assertEquals(typeEngine.FLOAT, binExpr.getLeft().getType());
		assertTrue(binExpr.getLeft() instanceof IAstUnaryExpr && ((IAstUnaryExpr) binExpr.getLeft()).getOp() == IOperation.CAST);
		assertEquals(typeEngine.FLOAT, binExpr.getRight().getType());
    }
    @Test
    public void testCast2a() throws Exception {
    	IAstModule mod = treeize("global : Int = 3;\n" +
    			"testCast2 = code (x : Int, y : Float) {\n" +
    			"   return x+10*y>>global;\n" +
    			"};");
    	sanityTest(mod);

    	doTypeInfer(mod, true);
    }
    @Test
    public void testCast2b() throws Exception {
    	IAstModule mod = treeize("global : Int = 3;\n" +
    			"testCast2b = code (x : Int, y : Float) {\n" +
    			"   return Int(x+10*y)>>global;\n" +
    			"};");
    	sanityTest(mod);

    	doTypeInfer(mod);
    	
    	assertTrue(mod.getScope().getSymbols().length == 2);
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testCast2b");
    	typeTest(mod, false);
    	

    	assertEquals(typeEngine.getCodeType(typeEngine.INT,  new LLType[] {typeEngine.INT, typeEngine.FLOAT}), def.getExpr().getType());
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
		IAstPrototype prototype = codeExpr.getPrototype();
		assertEquals(typeEngine.INT, prototype.returnType().getType());
    	assertEquals(typeEngine.INT, prototype.argumentTypes()[0].getType());
    	assertEquals(typeEngine.FLOAT, prototype.argumentTypes()[1].getType());
    	
    	IAstReturnStmt returnStmt = (IAstReturnStmt) codeExpr.stmts().list().get(0);
		assertEquals(typeEngine.INT, returnStmt.getType());
		
		// >>
		IAstBinExpr binExpr = (IAstBinExpr) returnStmt.getExpr();
		assertEquals(typeEngine.INT, binExpr.getLeft().getType());
		assertEquals(typeEngine.INT, binExpr.getRight().getType());
		assertTrue(binExpr.getLeft() instanceof IAstUnaryExpr && ((IAstUnaryExpr) binExpr.getLeft()).getOp() == IOperation.CAST);
		assertEquals(typeEngine.INT, binExpr.getRight().getType());
		
		IAstBinExpr mulExpr = (IAstBinExpr) ((IAstUnaryExpr) binExpr.getLeft()).getExpr();
		assertTrue(mulExpr.getLeft() instanceof IAstUnaryExpr && ((IAstUnaryExpr) mulExpr.getLeft()).getOp() == IOperation.CAST);
		assertEquals(typeEngine.FLOAT, mulExpr.getLeft().getType());
		assertEquals(typeEngine.FLOAT, mulExpr.getRight().getType());
    }

	@Test
    public void testDiscoverAssign1() throws Exception {
		// result of 'a' and thus return type is promoted
    	IAstModule mod = treeize(
    			"testDiscoverAssign1 = code (x : Int, y : Float) {\n" +
    			"   z := x;\n" +
    			"   a := z + y;\n" +
    			"   return a;\n"+
    			"};");
    	sanityTest(mod);

    	doTypeInfer(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testDiscoverAssign1");
    	typeTest(mod, false);
    	
    	assertEquals(typeEngine.getCodeType(typeEngine.FLOAT,  new LLType[] {typeEngine.INT, typeEngine.FLOAT}), def.getExpr().getType());
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
		IAstPrototype prototype = codeExpr.getPrototype();
		assertEquals(typeEngine.FLOAT, prototype.returnType().getType());
    	assertEquals(typeEngine.INT, prototype.argumentTypes()[0].getType());
    	assertEquals(typeEngine.FLOAT, prototype.argumentTypes()[1].getType());
    	
    }
	@Test
    public void testDiscoverAssign2() throws Exception {
		// 'a' should not change 'y'
    	IAstModule mod = treeize(
    			"testDiscoverAssign2 = code (x : Int, y : Float) {\n" +
    			"   z := x;\n" +
    			"   a : Int = y;\n"+
    			"   a = z + y;\n" +
    			"   return a;\n"+
    			"};");
    	sanityTest(mod);

    	doTypeInfer(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testDiscoverAssign2");
    	typeTest(mod, false);
    	
    	assertEquals(typeEngine.getCodeType(typeEngine.INT,  new LLType[] {typeEngine.INT, typeEngine.FLOAT}), def.getExpr().getType());
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
		IAstPrototype prototype = codeExpr.getPrototype();
		assertEquals(typeEngine.INT, prototype.returnType().getType());
    	assertEquals(typeEngine.INT, prototype.argumentTypes()[0].getType());
    	assertEquals(typeEngine.FLOAT, prototype.argumentTypes()[1].getType());
    	
    	ISymbol a = codeExpr.getScope().get("a");
    	assertEquals(typeEngine.INT, a.getType());
    	ISymbol x = codeExpr.getScope().get("x");
    	assertEquals(typeEngine.INT, x.getType());
    }
	
	@Test
    public void testDiscoverAssign3() throws Exception {
		// 'a' drives everything
    	IAstModule mod = treeize(
    			"testDiscoverAssign3 = code (x, y) {\n" +
    			"   z := x;\n" +
    			"   a : Int = y;\n"+
    			"   a = z + y;\n" +
    			"   return a;\n"+
    			"};");
    	sanityTest(mod);

    	doTypeInfer(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testDiscoverAssign3");
    	typeTest(mod, false);
    	
    	assertEquals(typeEngine.getCodeType(typeEngine.INT,  new LLType[] {typeEngine.INT, typeEngine.INT}), def.getExpr().getType());
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
		IAstPrototype prototype = codeExpr.getPrototype();
		assertEquals(typeEngine.INT, prototype.returnType().getType());
    	assertEquals(typeEngine.INT, prototype.argumentTypes()[0].getType());
    	assertEquals(typeEngine.INT, prototype.argumentTypes()[1].getType());
    	
    	ISymbol a = codeExpr.getScope().get("a");
    	assertEquals(typeEngine.INT, a.getType());
    	ISymbol x = codeExpr.getScope().get("x");
    	assertEquals(typeEngine.INT, x.getType());
    }
	@Test
    public void testDiscoverAssign3b() throws Exception {
		// infer entirely from that shift
    	IAstModule mod = treeize(
    			"testDiscoverAssign3b = code (x, y) {\n" +
    			"   z := x;\n" +
    			"   a := z + (y >> 1);\n" +
    			"   return a;\n"+
    			"};");
    	sanityTest(mod);

    	doTypeInfer(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testDiscoverAssign3b");
    	typeTest(mod, false);
    	
    	assertEquals(typeEngine.getCodeType(typeEngine.INT,  new LLType[] {typeEngine.INT, typeEngine.INT}), def.getExpr().getType());
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
		IAstPrototype prototype = codeExpr.getPrototype();
		assertEquals(typeEngine.INT, prototype.returnType().getType());
    	assertEquals(typeEngine.INT, prototype.argumentTypes()[0].getType());
    	assertEquals(typeEngine.INT, prototype.argumentTypes()[1].getType());
    	
    	ISymbol a = codeExpr.getScope().get("a");
    	assertEquals(typeEngine.INT, a.getType());
    	ISymbol x = codeExpr.getScope().get("x");
    	assertEquals(typeEngine.INT, x.getType());
    }
	
	@Test
    public void testDiscoverFuncCall2() throws Exception {
    	IAstModule mod = treeize(
    			"floatfunc := code (=>Float) { };\n"+
    			"testDiscoverFuncCall2 = code (x, y) {\n" +
    			"   z := x;\n" +
    			"   a := z + y > floatfunc();\n" +
    			"   return a;\n"+
    			"};");
    	sanityTest(mod);

    	doTypeInfer(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testDiscoverFuncCall2");
    	typeTest(mod, false);
    	
    	assertEquals(typeEngine.getCodeType(typeEngine.BOOL,  new LLType[] {typeEngine.FLOAT, typeEngine.FLOAT}), def.getExpr().getType());
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
		IAstPrototype prototype = codeExpr.getPrototype();
		assertEquals(typeEngine.BOOL, prototype.returnType().getType());
    	assertEquals(typeEngine.FLOAT, prototype.argumentTypes()[0].getType());
    	assertEquals(typeEngine.FLOAT, prototype.argumentTypes()[1].getType());
    	
    	ISymbol a = codeExpr.getScope().get("a");
    	assertEquals(typeEngine.BOOL, a.getType());
    	ISymbol x = codeExpr.getScope().get("x");
    	assertEquals(typeEngine.FLOAT, x.getType());
    }
}


