/**
 * Test that we can infer the types on an AST
 * 
 */
package org.ejs.eulang.test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import org.ejs.eulang.ast.IAstAllocStmt;
import org.ejs.eulang.ast.IAstAssignStmt;
import org.ejs.eulang.ast.IAstBinExpr;
import org.ejs.eulang.ast.IAstCodeExpr;
import org.ejs.eulang.ast.IAstDefineStmt;
import org.ejs.eulang.ast.IAstExprStmt;
import org.ejs.eulang.ast.IAstIntLitExpr;
import org.ejs.eulang.ast.IAstModule;
import org.ejs.eulang.ast.IAstPrototype;
import org.ejs.eulang.ast.IAstUnaryExpr;
import org.ejs.eulang.ast.IOperation;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLType;
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
    			"   x+10;\n" +
    			"};");
    	sanityTest(mod);

    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testNoChange1");
    	doTypeInfer(def.getExpr());
    	typeTest(mod, false);
    	
    	assertEquals(typeEngine.getCodeType(typeEngine.INT,  new LLType[] {typeEngine.INT, typeEngine.INT}), def.getExpr().getType());
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
		IAstPrototype prototype = codeExpr.getPrototype();
		assertEquals(typeEngine.INT, prototype.returnType().getType());
    	assertEquals(typeEngine.INT, prototype.argumentTypes()[0].getType());
    	assertEquals(typeEngine.INT, prototype.argumentTypes()[1].getType());
    	assertEquals(typeEngine.INT, ((IAstExprStmt) codeExpr.stmts().list().get(0)).getType());
    }

	 @Test 
    public void testBinOps() throws Exception {
    	IAstModule mod = treeize("testBinOps = code { x:=(1*2/3 and 4%%45 or 5<=6>>7<<8>>>85&9^10)or(11<12)>(13<=(14-15)==(16!=17%18+19)); };");
    	sanityTest(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testBinOps");
    	doTypeInfer(def.getExpr());
    	typeTest(mod, false);
    	
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
    	IAstAllocStmt allocStmt = (IAstAllocStmt) codeExpr.stmts().list().get(0);
    	assertEquals(typeEngine.BOOL, allocStmt.getTypeExpr().getType());
    }

    @Test
    public void testInvalidTypes1() throws Exception {
    	IAstModule mod = treeize(
    			"testInvalidTypes1 = code (x : Int, y : Float) {\n" +
    			"   y>>1;\n" +
    			"};");
    	sanityTest(mod);

    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testInvalidTypes1");
    	doTypeInfer(def.getExpr(), true);
    }
    
    @Test
    public void testInvalidTypes2() throws Exception {
    	IAstModule mod = treeize(
    			"testInvalidTypes2 = code (x : Int, y : Float) {\n" +
    			"   z : Int = 2.0;\n" +
    			"   z | y;\n"+
    			"};");
    	sanityTest(mod);

    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testInvalidTypes2");
    	doTypeInfer(def.getExpr(), true);
    }
	    
	@Test
    public void testVoidReturn() throws Exception {
    	IAstModule mod = treeize(
    			"testVoidReturn= code (x : Int, y : Int) { };");
    	sanityTest(mod);

    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testVoidReturn");
    	doTypeInfer(def.getExpr());
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

    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testPromotedCast1");
    	doTypeInfer(def.getExpr());
    	typeTest(mod, false);
    	
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
    	
    	IAstAllocStmt allocStmt = (IAstAllocStmt) codeExpr.stmts().list().get(0);
		assertEquals(typeEngine.FLOAT, allocStmt.getType());
		assertTrue(allocStmt.getExpr() instanceof IAstBinExpr);
		IAstBinExpr binExpr = (IAstBinExpr) allocStmt.getExpr();
		assertEquals(typeEngine.FLOAT, binExpr.getLeft().getType());
		assertTrue(isCastTo(binExpr.getLeft(), typeEngine.FLOAT));
    }
	@Test
    public void testPromotedCast2() throws Exception {
    	IAstModule mod = treeize(
    			"testPromotedCast2 = code () {\n" +
    			"   z : Byte;\n" +
    			"	z = z * Byte(100) / 5;\n" +
    			"};");
    	sanityTest(mod);

    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testPromotedCast2");
    	doTypeInfer(def.getExpr());
    	typeTest(mod, false);
    	
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
    	
    	IAstAssignStmt allocStmt = (IAstAssignStmt) codeExpr.stmts().list().get(1);
		assertEquals(typeEngine.BYTE, allocStmt.getType());
		IAstUnaryExpr castExpr = (IAstUnaryExpr) allocStmt.getExpr();
		assertEquals(typeEngine.INT, castExpr.getExpr().getType());
		IAstBinExpr divExpr = (IAstBinExpr) castExpr.getExpr();
		assertEquals(typeEngine.INT, divExpr.getLeft().getType());
		assertEquals(typeEngine.INT, divExpr.getRight().getType());
		assertTrue(isCastTo(divExpr.getLeft(), typeEngine.INT));
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

    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testPromotedCast3");
    	doTypeInfer(def.getExpr());
    	typeTest(mod, false);
    	
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
    	
    	IAstAssignStmt allocStmt = (IAstAssignStmt) codeExpr.stmts().list().get(1);
		assertEquals(typeEngine.BYTE, allocStmt.getType());
		IAstUnaryExpr castExpr = (IAstUnaryExpr) allocStmt.getExpr();
		assertEquals(typeEngine.INT, castExpr.getExpr().getType());
		IAstBinExpr addExpr = (IAstBinExpr) castExpr.getExpr();
		assertEquals(typeEngine.INT, addExpr.getType());
		assertEquals(typeEngine.INT, addExpr.getLeft().getType());
		assertEquals(typeEngine.INT, addExpr.getRight().getType());
		assertTrue(isCastTo(addExpr.getLeft(), typeEngine.INT));
    }
	@Test
    public void testPromotedCond1() throws Exception {
    	IAstModule mod = treeize(
    			"testPromotedCond1 = code () {\n" +
    			"   z : Byte;\n" +
    			"	z = 99 > 100;\n" +
    			"};");
    	sanityTest(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testPromotedCond1");
    	doTypeInfer(def.getExpr());
    	typeTest(mod, false);
    	
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
    	
    	IAstAssignStmt allocStmt = (IAstAssignStmt) codeExpr.stmts().list().get(1);
		assertTrue(isCastTo(allocStmt.getExpr(), typeEngine.BYTE));
		IAstUnaryExpr castExpr = (IAstUnaryExpr) allocStmt.getExpr();
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

    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testPromotedCond2");
    	doTypeInfer(def.getExpr());
    	
    	typeTest(mod, false);
    	
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
    	
    	IAstAssignStmt allocStmt = (IAstAssignStmt) codeExpr.stmts().list().get(1);
		assertTrue(isCastTo(allocStmt.getExpr(), typeEngine.BYTE));
		IAstUnaryExpr castExpr = (IAstUnaryExpr) allocStmt.getExpr();
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

    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testUnary1");
    	doTypeInfer(def.getExpr());
    	
    	typeTest(mod, false);
    	
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
    	
    	IAstAssignStmt allocStmt = (IAstAssignStmt) codeExpr.stmts().list().get(1);
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
    			"	!-~z;\n" +
    			"};");
    	sanityTest(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testUnaryNot");
    	doTypeInfer(def.getExpr());
    	typeTest(mod, false);
    	
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
    	
    	IAstExprStmt allocStmt = (IAstExprStmt) codeExpr.stmts().list().get(1);
		assertEquals(typeEngine.BOOL, allocStmt.getType());
		IAstBinExpr cmpExpr = (IAstBinExpr) allocStmt.getExpr();
		assertTrue(cmpExpr.getOp() == IOperation.COMPNE);
		assertEquals(typeEngine.BOOL, cmpExpr.getType());
		assertEquals(typeEngine.BYTE, cmpExpr.getLeft().getType());
		
		// there will be a cast here
		assertTrue(isCastTo(cmpExpr.getRight(), typeEngine.BYTE));
		IAstUnaryExpr castExpr = (IAstUnaryExpr) cmpExpr.getRight();
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
    			"   x*1.0;\n" +
    			"};");
    	sanityTest(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testDiscoverReturn1");
    	doTypeInfer(def.getExpr());
    	typeTest(mod, false);
    	
    	assertEquals(typeEngine.getCodeType(typeEngine.FLOAT,  new LLType[] {typeEngine.INT, typeEngine.INT}), def.getExpr().getType());
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
		IAstPrototype prototype = codeExpr.getPrototype();
		assertEquals(typeEngine.FLOAT, prototype.returnType().getType());
    	assertEquals(typeEngine.INT, prototype.argumentTypes()[0].getType());
    	assertEquals(typeEngine.INT, prototype.argumentTypes()[1].getType());
    	
    	IAstExprStmt returnStmt = (IAstExprStmt) codeExpr.stmts().list().get(0);
		assertEquals(typeEngine.FLOAT, returnStmt.getType());
		assertTrue(returnStmt.getExpr() instanceof IAstBinExpr);
		IAstBinExpr binExpr = (IAstBinExpr) returnStmt.getExpr();
		
		assertTrue(isCastTo(binExpr.getLeft(), typeEngine.FLOAT));
		assertEquals(typeEngine.FLOAT, binExpr.getRight().getType());
    }


	@Test
    public void testDiscoverReturn2() throws Exception {
    	IAstModule mod = treeize(
    			"testDiscoverReturn2 = code (x : Int, y : Int) {\n" +
    			"   x+10.0;\n" +
    			"};");
    	sanityTest(mod);

    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testDiscoverReturn2");
    	doTypeInfer(def.getExpr());
    	typeTest(mod, false);
    	
    	assertEquals(typeEngine.getCodeType(typeEngine.FLOAT,  new LLType[] {typeEngine.INT, typeEngine.INT}), def.getExpr().getType());
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
		IAstPrototype prototype = codeExpr.getPrototype();
		assertEquals(typeEngine.FLOAT, prototype.returnType().getType());
    	assertEquals(typeEngine.INT, prototype.argumentTypes()[0].getType());
    	assertEquals(typeEngine.INT, prototype.argumentTypes()[1].getType());
    	IAstExprStmt returnStmt = (IAstExprStmt) codeExpr.stmts().list().get(0);
		assertEquals(typeEngine.FLOAT, returnStmt.getType());
		assertTrue(returnStmt.getExpr() instanceof IAstBinExpr);
		IAstBinExpr binExpr = (IAstBinExpr) returnStmt.getExpr();
		assertTrue(isCastTo(binExpr.getLeft(), typeEngine.FLOAT));
		assertEquals(typeEngine.FLOAT, binExpr.getRight().getType());
    }
	

	@Test
    public void testCast1() throws Exception {
    	IAstModule mod = treeize(
    			"testCast1 = code (x : Int, y : Int) {\n" +
    			"   x+10.0;\n" +
    			"};");
    	sanityTest(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testCast1");
    	doTypeInfer(def.getExpr());
    	typeTest(mod, false);
    	
    	assertEquals(typeEngine.getCodeType(typeEngine.FLOAT,  new LLType[] {typeEngine.INT, typeEngine.INT}), def.getExpr().getType());
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
		IAstPrototype prototype = codeExpr.getPrototype();
		assertEquals(typeEngine.FLOAT, prototype.returnType().getType());
    	assertEquals(typeEngine.INT, prototype.argumentTypes()[0].getType());
    	assertEquals(typeEngine.INT, prototype.argumentTypes()[1].getType());
    	IAstExprStmt returnStmt = (IAstExprStmt) codeExpr.stmts().list().get(0);
		assertEquals(typeEngine.FLOAT, returnStmt.getType());
		assertTrue(returnStmt.getExpr() instanceof IAstBinExpr);
		IAstBinExpr binExpr = (IAstBinExpr) returnStmt.getExpr();
		assertTrue(isCastTo(binExpr.getLeft(), typeEngine.FLOAT));
		assertEquals(typeEngine.FLOAT, binExpr.getRight().getType());
    }
    @Test
    public void testCast2a() throws Exception {
    	IAstModule mod = treeize("global : Int = 3;\n" +
    			"testCast2 = code (x : Int, y : Float) {\n" +
    			"   x+10*y>>global;\n" +
    			"};");
    	sanityTest(mod);

    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testCast2");
    	doTypeInfer(def.getExpr(), true);
    }
    @Test
    public void testCast2b() throws Exception {
    	IAstModule mod = treeize("global : Int = 3;\n" +
    			"testCast2b = code (x : Int, y : Float) {\n" +
    			"   Int(x+10*y)>>global;\n" +
    			"};");
    	sanityTest(mod);

    	
    	assertTrue(mod.getScope().getSymbols().length == 2);
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testCast2b");
    	doTypeInfer(def.getExpr());
    	typeTest(mod, false);
    	

    	assertEquals(typeEngine.getCodeType(typeEngine.INT,  new LLType[] {typeEngine.INT, typeEngine.FLOAT}), def.getExpr().getType());
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
		IAstPrototype prototype = codeExpr.getPrototype();
		assertEquals(typeEngine.INT, prototype.returnType().getType());
    	assertEquals(typeEngine.INT, prototype.argumentTypes()[0].getType());
    	assertEquals(typeEngine.FLOAT, prototype.argumentTypes()[1].getType());
    	
    	IAstExprStmt returnStmt = (IAstExprStmt) codeExpr.stmts().list().get(0);
		assertEquals(typeEngine.INT, returnStmt.getType());
		
		// >>
		IAstBinExpr binExpr = (IAstBinExpr) returnStmt.getExpr();
		assertEquals(typeEngine.INT, binExpr.getLeft().getType());
		assertEquals(typeEngine.INT, binExpr.getRight().getType());
		assertTrue(isCastTo(binExpr.getLeft(), typeEngine.INT));
		assertEquals(typeEngine.INT, binExpr.getRight().getType());
		
		IAstBinExpr mulExpr = (IAstBinExpr) ((IAstUnaryExpr) binExpr.getLeft()).getExpr();
		assertTrue(isCastTo(mulExpr.getLeft(), typeEngine.FLOAT));
		assertEquals(typeEngine.FLOAT, mulExpr.getRight().getType());
    }

	@Test
    public void testDiscoverAssign1() throws Exception {
		// result of 'a' and thus type is promoted
    	IAstModule mod = treeize(
    			"testDiscoverAssign1 = code (x : Int, y : Float) {\n" +
    			"   z := x;\n" +
    			"   a := z + y;\n" +
    			"   a;\n"+
    			"};");
    	sanityTest(mod);

    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testDiscoverAssign1");
    	doTypeInfer(def.getExpr());
    	
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
    			"   a;\n"+
    			"};");
    	sanityTest(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testDiscoverAssign2");
    	doTypeInfer(def.getExpr());
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
    			"   a;\n"+
    			"};");
    	sanityTest(mod);

    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testDiscoverAssign3");
    	doTypeInfer(def.getExpr());
    	
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
    			"   a;\n"+
    			"};");
    	sanityTest(mod);

    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testDiscoverAssign3b");
    	doTypeInfer(def.getExpr());
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
    			"   a;\n"+
    			"};");
    	sanityTest(mod);

    	// module gets allocations, but not defines
    	doTypeInfer(mod);

    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testDiscoverFuncCall2");
    	doTypeInfer(def.getExpr());
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
	
	@Test
    public void testExample1() throws Exception {
    	IAstModule mod = treeize(
    			"testExample1 = code (z, a : Byte) {\n" +
    			"   z = a | Byte(6);\n" +
    			"   z;\n"+
    			"};");
    	sanityTest(mod);

    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testExample1");
    	doTypeInfer(def.getExpr());
    	typeTest(mod, false);
    	
    	assertEquals(typeEngine.getCodeType(typeEngine.BYTE,  new LLType[] {typeEngine.BYTE, typeEngine.BYTE}), def.getExpr().getType());
    	
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
    	IAstAssignStmt assign = (IAstAssignStmt) codeExpr.stmts().list().get(0);
    	assertEquals(typeEngine.BYTE, assign.getType());
    	assertEquals(typeEngine.BYTE, assign.getExpr().getType());
    	assertTrue(assign.getExpr() instanceof IAstBinExpr);
    }
	
 
}


