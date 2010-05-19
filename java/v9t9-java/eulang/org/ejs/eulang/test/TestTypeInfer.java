/**
 * Test that we can infer the types on an AST
 * 
 */
package org.ejs.eulang.test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.util.Collection;

import junit.framework.AssertionFailedError;

import org.ejs.eulang.IOperation;
import org.ejs.eulang.ast.IAstAllocStmt;
import org.ejs.eulang.ast.IAstAssignStmt;
import org.ejs.eulang.ast.IAstBinExpr;
import org.ejs.eulang.ast.IAstCodeExpr;
import org.ejs.eulang.ast.IAstCondList;
import org.ejs.eulang.ast.IAstDataType;
import org.ejs.eulang.ast.IAstDefineStmt;
import org.ejs.eulang.ast.IAstExprStmt;
import org.ejs.eulang.ast.IAstFuncCallExpr;
import org.ejs.eulang.ast.IAstIndexExpr;
import org.ejs.eulang.ast.IAstIntLitExpr;
import org.ejs.eulang.ast.IAstModule;
import org.ejs.eulang.ast.IAstPrototype;
import org.ejs.eulang.ast.IAstSymbolDefiner;
import org.ejs.eulang.ast.IAstSymbolExpr;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.IAstUnaryExpr;
import org.ejs.eulang.ast.impl.AstTypedNode;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLArrayType;
import org.ejs.eulang.types.LLCodeType;
import org.ejs.eulang.types.LLDataType;
import org.ejs.eulang.types.LLInstanceField;
import org.ejs.eulang.types.LLPointerType;
import org.ejs.eulang.types.LLSymbolType;
import org.ejs.eulang.types.LLTupleType;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeInference;
import org.junit.Test;

/**
 * @author ejs
 *
 */
public class TestTypeInfer extends BaseParserTest {
	{
		dumpTypeInfer = true;
	}

	@Test
    public void testNoChange1() throws Exception {
    	IAstModule mod = treeize(
    			"testNoChange1 = code (x : Int; y : Int => Int) {\n" +
    			"   x+10;\n" +
    			"};");
    	sanityTest(mod);

    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testNoChange1");
    	doTypeInfer(mod);
    	typeTest(mod, false);
    	
    	assertEquals(typeEngine.getCodeType(typeEngine.INT,  new LLType[] {typeEngine.INT, typeEngine.INT}), getMainBodyExpr(def).getType());
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainBodyExpr(def);
		IAstPrototype prototype = codeExpr.getPrototype();
		assertEquals(typeEngine.INT, prototype.returnType().getType());
    	assertEquals(typeEngine.INT, prototype.argumentTypes()[0].getType());
    	assertEquals(typeEngine.INT, prototype.argumentTypes()[1].getType());
    	assertEquals(typeEngine.INT, ((IAstExprStmt) codeExpr.stmts().list().get(0)).getType());
    }

	 @Test 
    public void testBinOps() throws Exception {
    	IAstModule mod = treeize("testBinOps = code { x:=(Bool(1*2/3) and Bool(4+\\45 )or 5<=6>>7<<8+>>85&9 ~ 10)or(11<12)>(13<=(14-15)==(16!=17%18+19)); };");
    	sanityTest(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testBinOps");
    	doTypeInfer(mod);
    	typeTest(mod, false);
    	
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainBodyExpr(def);
    	IAstAllocStmt allocStmt = (IAstAllocStmt) codeExpr.stmts().list().get(0);
    	assertEquals(typeEngine.BOOL, allocStmt.getTypeExpr().getType());
    }

    @Test
    public void testInvalidTypes1() throws Exception {
    	IAstModule mod = treeize(
    			"testInvalidTypes1 = code (x : Int; y : Float => nil) {\n" +
    			"   y>>1;\n" +
    			"};");
    	sanityTest(mod);

    	doTypeInfer(mod, true);
    }
    
    @Test
    public void testInvalidTypes2() throws Exception {
    	IAstModule mod = treeize(
    			"testInvalidTypes2 = code (x : Int; y : Float => nil) {\n" +
    			"   z : Int = 2.0;\n" +
    			"   z | y;\n"+
    			"};");
    	sanityTest(mod);

    	doTypeInfer(mod, true);
    }
	    
	@Test
    public void testVoidReturn() throws Exception {
    	IAstModule mod = treeize(
    			"testVoidReturn= code (x : Int; y : Int) { };");
    	sanityTest(mod);

    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testVoidReturn");
    	doTypeInfer(mod);
    	typeTest(mod, false);
    	
    	assertEquals(typeEngine.getCodeType(typeEngine.VOID,  new LLType[] {typeEngine.INT, typeEngine.INT}), getMainBodyExpr(def).getType());
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainBodyExpr(def);
		IAstPrototype prototype = codeExpr.getPrototype();
		assertEquals(typeEngine.VOID, prototype.returnType().getType());
    	assertEquals(typeEngine.INT, prototype.argumentTypes()[0].getType());
    	assertEquals(typeEngine.INT, prototype.argumentTypes()[1].getType());
    	
    }
	@Test
    public void testVoidReturn2() throws Exception {
    	IAstModule mod = treeize(
    			"testVoidReturn2= code ( => nil) { y : Int[10]; };");
    	sanityTest(mod);

    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testVoidReturn2");
    	doTypeInfer(mod);
    	typeTest(mod, false);
    	
    	assertEquals(typeEngine.getCodeType(typeEngine.VOID, 
    			new LLType[] {}), getMainBodyExpr(def).getType());
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainBodyExpr(def);
		IAstPrototype prototype = codeExpr.getPrototype();
		assertEquals(typeEngine.VOID, prototype.returnType().getType());
    	
    }
	@Test
    public void testPromotedCast1() throws Exception {
    	IAstModule mod = treeize(
    			"testPromotedCast1 = code (x : Int; y : Int) {\n" +
    			"   p := x*1.0;\n" +
    			"};");
    	sanityTest(mod);

    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testPromotedCast1");
    	doTypeInfer(mod);
    	typeTest(mod, false);
    	
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainBodyExpr(def);
    	
    	IAstAllocStmt allocStmt = (IAstAllocStmt) codeExpr.stmts().list().get(0);
		assertEquals(typeEngine.FLOAT, allocStmt.getType());
		assertTrue(allocStmt.getExprs().getFirst() instanceof IAstBinExpr);
		IAstBinExpr binExpr = (IAstBinExpr) allocStmt.getExprs().getFirst();
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
    	doTypeInfer(mod);
    	typeTest(mod, false);
    	
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainBodyExpr(def);
    	
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
    	doTypeInfer(mod);
    	typeTest(mod, false);
    	
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainBodyExpr(def);
    	
    	IAstAssignStmt allocStmt = (IAstAssignStmt) codeExpr.stmts().list().get(1);
		assertEquals(typeEngine.BYTE, allocStmt.getType());
		IAstBinExpr addExpr = (IAstBinExpr) allocStmt.getExprs().getFirst();
		assertEquals(typeEngine.BYTE, addExpr.getType());
		assertEquals(typeEngine.BYTE, addExpr.getLeft().getType());
		assertEquals(typeEngine.BYTE, addExpr.getRight().getType());
		assertTrue(isCastTo(addExpr.getRight(), typeEngine.BYTE));
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
    	doTypeInfer(mod);
    	typeTest(mod, false);
    	
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainBodyExpr(def);
    	
    	IAstAssignStmt allocStmt = (IAstAssignStmt) codeExpr.stmts().list().get(1);
		assertTrue(isCastTo(allocStmt.getExprs().getFirst(), typeEngine.BYTE));
		IAstUnaryExpr castExpr = (IAstUnaryExpr) allocStmt.getExprs().getFirst();
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
    	doTypeInfer(mod);
    	
    	typeTest(mod, false);
    	
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainBodyExpr(def);
    	
    	IAstAssignStmt allocStmt = (IAstAssignStmt) codeExpr.stmts().list().get(1);
		assertTrue(isCastTo(allocStmt.getExprs().getFirst(), typeEngine.BYTE));
		IAstUnaryExpr castExpr = (IAstUnaryExpr) allocStmt.getExprs().getFirst();
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
    	doTypeInfer(mod);
    	
    	typeTest(mod, false);
    	
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainBodyExpr(def);
    	
    	IAstAssignStmt allocStmt = (IAstAssignStmt) codeExpr.stmts().list().get(1);
		assertEquals(typeEngine.BYTE, allocStmt.getType());

		IAstUnaryExpr negExpr = (IAstUnaryExpr) allocStmt.getExprs().getFirst();
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
    			"	not -~z;\n" +
    			"};");
    	sanityTest(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testUnaryNot");
    	doTypeInfer(mod);
    	typeTest(mod, false);
    	
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainBodyExpr(def);
    	
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
    			"testDiscoverReturn1 = code (x : Int; y : Int) {\n" +
    			"   x*1.0;\n" +
    			"};");
    	sanityTest(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testDiscoverReturn1");
    	doTypeInfer(mod);
    	typeTest(mod, false);
    	
    	assertEquals(typeEngine.getCodeType(typeEngine.FLOAT,  new LLType[] {typeEngine.INT, typeEngine.INT}), getMainBodyExpr(def).getType());
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainBodyExpr(def);
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
    			"testDiscoverReturn2 = code (x : Int; y : Int) {\n" +
    			"   x+10.0;\n" +
    			"};");
    	sanityTest(mod);

    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testDiscoverReturn2");
    	doTypeInfer(mod);
    	typeTest(mod, false);
    	
    	assertEquals(typeEngine.getCodeType(typeEngine.FLOAT,  new LLType[] {typeEngine.INT, typeEngine.INT}), getMainBodyExpr(def).getType());
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainBodyExpr(def);
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
    			"testCast1 = code (x : Int; y : Int) {\n" +
    			"   x+10.0;\n" +
    			"};");
    	sanityTest(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testCast1");
    	doTypeInfer(mod);
    	typeTest(mod, false);
    	
    	assertEquals(typeEngine.getCodeType(typeEngine.FLOAT,  new LLType[] {typeEngine.INT, typeEngine.INT}), getMainBodyExpr(def).getType());
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainBodyExpr(def);
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
    			"testCast2 = code (x : Int; y : Float => nil) {\n" +
    			"   x+10*y>>global;\n" +
    			"};");
    	sanityTest(mod);

    	doTypeInfer(mod, true);
    }
    @Test
    public void testCast2b() throws Exception {
    	IAstModule mod = treeize("global : Int = 3;\n" +
    			"testCast2b = code (x : Int; y : Float) {\n" +
    			"   Int(x+10*y)>>global;\n" +
    			"};");
    	sanityTest(mod);

    	doTypeInfer(mod);
    	
    //	assertEquals(2, mod.getScope().getSymbols().length);
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testCast2b");
    	typeTest(mod, false);
    	

    	assertEquals(typeEngine.getCodeType(typeEngine.INT,  new LLType[] {typeEngine.INT, typeEngine.FLOAT}), getMainBodyExpr(def).getType());
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainBodyExpr(def);
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
    public void testCast3() throws Exception {
    	// we have one cast of the int to the byte, and another cast of the assignment to an int for the return
    	dumpLLVMGen = true;
    	IAstModule mod = treeize("x : Byte; testCast3 = code( => Int ) { x = 0x1234; };\n");
    	sanityTest(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testCast3");
    	doTypeInfer(mod);
    	
    	typeTest(mod, false);
    	
    	assertEquals(typeEngine.getCodeType(typeEngine.INT, new LLType[0]), getMainBodyExpr(def).getType());
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainBodyExpr(def);
    	IAstExprStmt exprStmt = (IAstExprStmt) codeExpr.stmts().getFirst();
    	assertTrue(isCastTo(exprStmt.getExpr(), typeEngine.INT));
    	IAstAssignStmt assn = (IAstAssignStmt) ((IAstUnaryExpr) exprStmt.getExpr()).getExpr();
    	assertTrue(isCastTo(assn.getExprs().getFirst(), typeEngine.BYTE));
    }

	@Test
    public void testDiscoverAssign1() throws Exception {
		// result of 'a' and thus type is promoted
    	IAstModule mod = treeize(
    			"testDiscoverAssign1 = code (x : Int; y : Float) {\n" +
    			"   z := x;\n" +
    			"   a := z + y;\n" +
    			"   a;\n"+
    			"};");
    	sanityTest(mod);

    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testDiscoverAssign1");
    	doTypeInfer(mod);
    	
    	typeTest(mod, false);
    	
    	assertEquals(typeEngine.getCodeType(typeEngine.FLOAT,  new LLType[] {typeEngine.INT, typeEngine.FLOAT}), getMainBodyExpr(def).getType());
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainBodyExpr(def);
		IAstPrototype prototype = codeExpr.getPrototype();
		assertEquals(typeEngine.FLOAT, prototype.returnType().getType());
    	assertEquals(typeEngine.INT, prototype.argumentTypes()[0].getType());
    	assertEquals(typeEngine.FLOAT, prototype.argumentTypes()[1].getType());
    	
    }
	@Test
    public void testDiscoverAssign2() throws Exception {
		// 'a' should not change 'y'
    	IAstModule mod = treeize(
    			"testDiscoverAssign2 = code (x : Int; y : Float) {\n" +
    			"   z := x;\n" +
    			"   a : Int = y;\n"+
    			"   a = z + y;\n" +
    			"   a;\n"+
    			"};");
    	sanityTest(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testDiscoverAssign2");
    	doTypeInfer(mod);
    	typeTest(mod, false);
    	
    	assertEquals(typeEngine.getCodeType(typeEngine.INT,  new LLType[] {typeEngine.INT, typeEngine.FLOAT}), getMainBodyExpr(def).getType());
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainBodyExpr(def);
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
    	doTypeInfer(mod);
    	
    	typeTest(mod, false);
    	
    	assertEquals(typeEngine.getCodeType(typeEngine.INT,  new LLType[] {typeEngine.INT, typeEngine.INT}), getMainBodyExpr(def).getType());
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainBodyExpr(def);
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
    	doTypeInfer(mod);
    	typeTest(mod, false);
    	
    	assertEquals(typeEngine.getCodeType(typeEngine.INT,  new LLType[] {typeEngine.INT, typeEngine.INT}), getMainBodyExpr(def).getType());
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainBodyExpr(def);
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
    	//doTypeInfer(getMainBodyExpr(def));
    	typeTest(mod, false);
    	
    	assertEquals(typeEngine.getCodeType(typeEngine.BOOL,  new LLType[] {typeEngine.FLOAT, typeEngine.FLOAT}), getMainBodyExpr(def).getType());
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainBodyExpr(def);
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
    public void testExample1Fail() throws Exception {
		// can't allow this right now... if we infer the LHS type this way, it messes up "real" types
		// which may dereference through pointers or fields
    	IAstModule mod = treeize(
    			"testExample1 = code (z; a : Byte) {\n" +
    			"   z = a | Byte(6);\n" +
    			"   z;\n"+
    			"};");
    	sanityTest(mod);

    	//IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testExample1");
    	doTypeInfer(mod);
    	try {
    		typeTest(mod, false);
    		fail();
    	} catch (AssertionFailedError e) {
    		
    	}
    	
    	/*
    	assertEquals(typeEngine.getCodeType(typeEngine.BYTE,  new LLType[] {typeEngine.BYTE, typeEngine.BYTE}), getMainBodyExpr(def).getType());
    	
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainBodyExpr(def);
    	IAstAssignStmt assign = (IAstAssignStmt) codeExpr.stmts().list().get(0);
    	assertEquals(typeEngine.BYTE, assign.getType());
    	assertEquals(typeEngine.BYTE, assign.getExprs().getFirst().getType());
    	assertTrue(assign.getExprs().getFirst() instanceof IAstBinExpr);
    	*/
    }
	
/*
    @Test
    public void testPointers1() throws Exception {
    	IAstModule mod = treeize(
        		" badSwap_testPointers1 = code (x : Int&; y : Int& => nil) {\n" +
        		" t := x;\n"+
    			" x = y;\n"+
    			" y = t;\n"+
        		"};\n");
		sanityTest(mod);
		
		IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("badSwap_testPointers1");
    	doTypeInfer(mod);
    	typeTest(mod, false);

		LLType intRef = typeEngine.getRefType(typeEngine.INT);
	   	assertEquals(typeEngine.getCodeType(typeEngine.VOID,  
	   			new LLType[] { intRef, intRef }), 
	   			getMainBodyExpr(def).getType());
	   	
	   	IAstAssignStmt assn = (IAstAssignStmt) ((IAstCodeExpr) getMainBodyExpr(def)).stmts().getLast();
	   	//assertEquals(intRef, assn.getType());
	   	assertEquals(typeEngine.VOID, assn.getType());
	   	assertEquals(intRef, assn.getSymbolExprs().getFirst().getType());
    }
*/
	
    /*
    @Test
    public void testPointers2() throws Exception {
    	IAstModule mod = treeize(
        		" refOnlySwap_testPointers2 = code (@x : Int&; @y : Int& => null) {\n" +
        		" t := x;\n"+
    			" x = y;\n"+
    			" y = t;\n"+
        		"};\n");
		sanityTest(mod);
		
		IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("refOnlySwap_testPointers2");
    	    	doTypeInfer(mod);
    	typeTest(mod, false);

		LLType intRef = typeEngine.getRefType(typeEngine.INT);
	   	assertEquals(typeEngine.getCodeType(typeEngine.VOID,  
	   			new LLType[] { intRef, intRef }), 
	   			def.getExpr().getType());
	   	
	   	IAstAssignStmt assn = (IAstAssignStmt) ((IAstCodeExpr) def.getExpr()).stmts().getLast();
	   	assertEquals(intRef, assn.getType());
		
    }
    
    @Test
    public void testPointers3() throws Exception {
    	IAstModule mod = treeize(
        		" intOnlySwap_testPointers3 = code (@x : Int; @y : Int => null) {\n" +
        		" t := x;\n"+
    			" x = y;\n"+
    			" y = t;\n"+
        		"};\n");
		sanityTest(mod);


		IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("intOnlySwap_testPointers3");
    	    	doTypeInfer(mod);
    	typeTest(mod, false);

	   	assertEquals(typeEngine.getCodeType(typeEngine.VOID,  
	   			new LLType[] { typeEngine.INT, typeEngine.INT }), 
	   			def.getExpr().getType());
	   	
	   	IAstAssignStmt assn = (IAstAssignStmt) ((IAstCodeExpr) def.getExpr()).stmts().getLast();
	   	assertEquals(typeEngine.INT, assn.getType());

    }
    @Test
    public void testPointers4() throws Exception {
    	IAstModule mod = treeize(
    			" genericSwap_testPointers4 = code (@x, @y => null) {\n" +
    			" t : Int = x;\n"+
    			" x = y;\n"+
    			" y = t;\n"+
    	"};\n");
    	sanityTest(mod);
    	
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("genericSwap_testPointers4");
    	    	doTypeInfer(mod);
    	typeTest(mod, false);
    	
    	assertEquals(typeEngine.getCodeType(typeEngine.VOID,  
    			new LLType[] { typeEngine.INT, typeEngine.INT }), 
    			def.getExpr().getType());
    	
	   	IAstAssignStmt assn = (IAstAssignStmt) ((IAstCodeExpr) def.getExpr()).stmts().getLast();
	   	assertEquals(typeEngine.INT, assn.getType());

    }*/
    


	@Test
	public void testShortCircuitAndOrRet() throws Exception {
		// awesomeness: return propagates down
		IAstModule mod = treeize("testShortCircuitAndOr = code (x,y,z => Int ){\n" +
				"if x > y and y > z then y " +
				"elif x > z and z > y then z " +
				"elif y > x and x > z then x " +
				"elif x == y or z == x then x+y+z " +
				"else x-y-z  };");
		sanityTest(mod);
    	
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testShortCircuitAndOr");
    	doTypeInfer(mod);
    	typeTest(mod, false);
    	
    	assertEquals(typeEngine.getCodeType(typeEngine.INT,  
    			new LLType[] { typeEngine.INT, typeEngine.INT, typeEngine.INT }), 
    			getMainBodyExpr(def).getType());
    	
    	IAstExprStmt stmt1 = (IAstExprStmt)((IAstCodeExpr)getMainBodyExpr(def)).stmts().getFirst();
		assertEquals(typeEngine.INT, stmt1.getType());
		assertEquals(typeEngine.INT, ((IAstCondList)stmt1.getExpr()).getType());
	}
	

	@Test
	public void testShortCircuitAndOrConst() throws Exception {
		// awesomeness: lone int constant propagates up
		IAstModule mod = treeize("testShortCircuitAndOr = code (x,y,z ){\n" +
				"if  x > y and y > z then y " +
				"elif x > z and z > y then z " +
				"elif y > x and x > z then x " +
				"elif x == y or z == x then x+y+z " +
				"else x-y-z+0 };");
		sanityTest(mod);
    	
    	
    	doTypeInfer(mod);
    	typeTest(mod, false);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testShortCircuitAndOr");
    	assertEquals(typeEngine.getCodeType(typeEngine.INT,  
    			new LLType[] { typeEngine.INT, typeEngine.INT, typeEngine.INT }), 
    			getMainBodyExpr(def).getType());
    	
    	IAstExprStmt stmt1 = (IAstExprStmt)((IAstCodeExpr)getMainBodyExpr(def)).stmts().getFirst();
		assertEquals(typeEngine.INT, stmt1.getType());
		assertEquals(typeEngine.INT, ((IAstCondList)stmt1.getExpr()).getType());
	}

	/*
	@Test
	public void testShortCircuitAndOrRef() throws Exception {
		// be sure stray 'Int&' doesn't make everything a reference
		IAstModule mod = treeize("testShortCircuitAndOrRef = code (x;y:Int&;z => Int){\n" +
				"if x > y and y > z then y " +
				"elif x > z and z > y then z " +
				"elif y > x and x > z then x " +
				"elif x == y or z == x then x+y+z " +
				"else x-y-z };");
		sanityTest(mod);
    	
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testShortCircuitAndOrRef");
    	doTypeInfer(mod);
    	typeTest(mod, false);
    	
    	LLType intRef = typeEngine.getRefType(typeEngine.INT);
    	assertEquals(typeEngine.getCodeType(typeEngine.INT,  
    			new LLType[] { typeEngine.INT, intRef, typeEngine.INT }), 
    			getMainBodyExpr(def).getType());
    	
    	IAstExprStmt stmt1 = (IAstExprStmt)((IAstCodeExpr)getMainBodyExpr(def)).stmts().getFirst();
		assertEquals(typeEngine.INT, stmt1.getType());
		assertEquals(typeEngine.INT, ((IAstCondList)stmt1.getExpr()).getType());
	}
	*/

	 
    @Test
    public void testTuples1() throws Exception {
    	IAstModule mod = treeize("testTuples1 = code (x,y) { (y+0,x+0); };");
    	sanityTest(mod);
    	

    	doTypeInfer(mod);
    	typeTest(mod, false);
    	
    }
    
    /*
    @Test
    public void testTuples2() throws Exception {
    	IAstModule mod = treeize("testTuples2 = (7, code (x,y) { (0,1); });");
    	sanityTest(mod);
    	
    	doTypeInfer(mod);

    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testTuples2");
    	doTypeInfer(def.getExpr());
    	typeTest(mod, false);
    	
    }
    */
    @Test
    public void testTuples3() throws Exception {
    	IAstModule mod = treeize("testTuples3 = code (x,y => (Int, Int)) { (y,x); };");
    	sanityTest(mod);

    	doTypeInfer(mod);
    	typeTest(mod, false);
    	
    }
    @Test
    public void testTuples4() throws Exception {
    	IAstModule mod = treeize("swap = code (x,y => (Int, Int)) { (y,x); };\n" +
    			"testTuples4 = code (a,b) { (a, b) = swap(4, 5); }; \n");
    	sanityTest(mod);
    	
    	// module gets allocations, but not defines
    	doTypeInfer(mod);
    	
    }
    @Test
    public void testTuples4Fail() throws Exception {
    	IAstModule mod = treeize("swap = code (x,y,z => (Int, Int, Int)) { (y,x); };\n" +
    			"testTuples4b = code (a,b) { (x, o, y) := swap(a+b, a-b, b); (a*x, y*b); }; \n");
    	sanityTest(mod);
    	doTypeInfer(mod, true);
    }
   
    /**
	 * @param i
	 * @param def
	 * @param j
	 */
	private void assertInstanceCount(int bodyCount, int totalInstanceCount, IAstDefineStmt def) {
    	assertEquals(bodyCount, def.bodyList().size());
    	assertEquals(totalInstanceCount, def.getConcreteInstances().size());
		
	}
	
	@Test
    public void testTuples5() throws Exception {
    	// In this case, we need to infer through the 'bop' call.
    	// This means detecting that 'bop' is generic and making a new instance.
    	IAstModule mod = treeize("swap = code (x,y => (Int, Int)) { (y,x); };\n" +
    			"bop = [T,U] code(x:T;y:U) { y };\n"+
    			"testTuples5 = code (a,b) { (a, b) = bop(5, swap(4, 5)); }; \n");
    	sanityTest(mod);
    	
    	doTypeInfer(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("bop");
    	assertInstanceCount(1, 1, def);
    	
    	LLTupleType tupleType = typeEngine.getTupleType(  
    			new LLType[] { typeEngine.INT, typeEngine.INT });
    	
    	LLType expType = typeEngine.getCodeType(tupleType, new LLType[] { typeEngine.INT, tupleType });
    	IAstTypedExpr main = getMainBodyExpr(def);
    	assertNotNull(def.getMatchingInstance(main.getType(), expType));
    	

    }
    
	@Test
    public void testGenerics0() throws Exception {
    	IAstModule mod = treeize("add = [] code (x,y) { x+y };\n" +
    			"testGenerics0 = code (a:Int;b:Int) { add(a,b);  }; \n");
    	sanityTest(mod);
    	
    	mod = (IAstModule) doExpand(mod);
    	doTypeInfer(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("add");
    	assertInstanceCount(1, 1, def);
    	
    	def = (IAstDefineStmt) mod.getScope().getNode("testGenerics0");
    	assertFalse(getMainBodyExpr(def).getType().isGeneric());

    }
    
    @Test
    public void testGenerics0b() throws Exception {
    	IAstModule mod = treeize("add = [T,U] code  (x:T;y:U) { x+y };\n" +
    			"testGenerics0b = code (a:Int;b:Int) { add(a,b) + add(10.0,b);  }; \n");
    	sanityTest(mod);
    	
    	mod = (IAstModule) doExpand(mod);
    	doTypeInfer(mod);
    	
    	IAstDefineStmt addDef = (IAstDefineStmt) mod.getScope().getNode("add");
    	assertInstanceCount(1, 2, addDef);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testGenerics0b");
    	assertFalse(getMainBodyExpr(def).getType().isGeneric());
    	
    	IAstTypedExpr addBody = addDef.getMatchingBodyExpr(null);
    	assertNotNull(addBody);
    	Collection<ISymbol> exps = addDef.bodyToInstanceMap().get(addBody.getType());
    	assertNotNull(exps);
    	assertEquals(2, exps.size());
    	ISymbol[] symbols = (ISymbol[]) exps.toArray(new ISymbol[exps.size()]);
    	LLCodeType expType1 = typeEngine.getCodeType(typeEngine.INT, new LLType[] { typeEngine.INT, typeEngine.INT });
    	assertTrue(expType1+"", symbols[0].getType().equals(expType1) || symbols[1].getType().equals(expType1));
    	LLCodeType expType2 = typeEngine.getCodeType(typeEngine.FLOAT, new LLType[] { typeEngine.FLOAT, typeEngine.INT });
    	assertTrue(expType2+"", symbols[0].getType().equals(expType2) || symbols[1].getType().equals(expType2));

    	// make sure the casting worked properly
    	symbols[0].getDefinition().validateType(typeEngine);
    	symbols[1].getDefinition().validateType(typeEngine);
    }
    @Test
    public void testGenerics1() throws Exception {
    	IAstModule mod = treeize("swap = [] code (x,y) { (y,x); };\n" +
    			"testGenerics1 = code (a,b) { (a, b) = swap(4, 5);  }; \n");
    	sanityTest(mod);
    	
    	mod = (IAstModule) doExpand(mod);
    	doTypeInfer(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("swap");
    	assertInstanceCount(1, 1, def);
    	
    	def = (IAstDefineStmt) mod.getScope().getNode("testGenerics1");
    	assertFalse(getMainBodyExpr(def).getType().isGeneric());

    }
    @Test
    public void testGenerics2() throws Exception {
    	IAstModule mod = treeize("swap = [T,U] code (x:T;y:U) { (y,x); };\n" +
    			"testGenerics2 = code (a,b) { (a, b) = swap(4, 5); (x, y) := swap(1.0, 9); }; \n");
    	sanityTest(mod);
    	
    	mod = (IAstModule) doExpand(mod);
    	doTypeInfer(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("swap");
    	assertInstanceCount(1, 2, def);
    	
    	def = (IAstDefineStmt) mod.getScope().getNode("testGenerics2");
    	assertFalse(getMainBodyExpr(def).getType().isGeneric());

    }
    
    @Test
    public void testTypeList1() throws Exception {
    	IAstModule mod = treeize("floor = [\n"+
    			"	code (x:Float) { x - x%1.0 },\n" +
    			"   code (x:Double) { x - x%1.0 }\n " +
    			"];\n"+
			"testTypeList1 = code (a:Float;b:Double) { floor(a)+floor(b) }; \n");
		sanityTest(mod);
		
		doTypeInfer(mod);

		IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("floor");
		assertInstanceCount(2, 2, def);
    	
		// be sure we selected the right one
		def = (IAstDefineStmt) mod.getScope().getNode("testTypeList1");
		IAstCodeExpr code = (IAstCodeExpr) def.getMatchingBodyExpr(null);
		IAstBinExpr add = (IAstBinExpr) ((IAstExprStmt) code.stmts().list().get(0)).getExpr();
		assertEquals(typeEngine.DOUBLE, add.getType());
		assertTrue(isCastTo(add.getLeft(), typeEngine.DOUBLE)); 
		assertFalse(isCastTo(add.getRight(), typeEngine.DOUBLE)); 
    }
    
    @Test
    public void testTypeList2() throws Exception {
 		 // make sure we don't generate more than one instance per type (even if we waste temp symbols)
    	IAstModule mod = treeize("floor = [\n"+
    			"	code (x:Float) { x - x%1.0 },\n" +
    			"   code (x:Double) { x - x%1.0 }\n " +
    			"];\n"+
			"testTypeList1 = code (a:Float;b:Double) { floor(a)+floor(b)*floor(a)*floor(b) }; \n");
		sanityTest(mod);
		
		doTypeInfer(mod);

		IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("floor");
		assertInstanceCount(2, 2, def);
    	
		// be sure we selected the right one
		def = (IAstDefineStmt) mod.getScope().getNode("testTypeList1");
		IAstCodeExpr code = (IAstCodeExpr) def.getMatchingBodyExpr(null);
		IAstBinExpr add = (IAstBinExpr) ((IAstExprStmt) code.stmts().list().get(0)).getExpr();
		assertEquals(typeEngine.DOUBLE, add.getType());
		assertTrue(isCastTo(add.getLeft(), typeEngine.DOUBLE)); 
		assertFalse(isCastTo(add.getRight(), typeEngine.DOUBLE)); 
    }
    
    // be sure we can do this style of overloading too:
    // 1) don't replace macro in call spot, but only as a function call
    // (or else you get wrong arguments on injected code)
    // 2) avoid recursion in expanding
    // 3) be sure generic types are not inferred so tightly that the code is broken (add casts)
    @Test
	public void testOverloadingMacro0() throws Exception {
		IAstModule mod = doFrontend("    util = [\n"
				+ "             macro (x, y) { util(x, y, 0) },\n"
				+ "				code(x:Int; y:Int; z:Int => Int ) { x*y-z }\n"
				+ "            ];\n"
				+ "func = code(x:Int;y:Int => Int) { util(x,y) };\n");
		sanityTest(mod);

		doTypeInfer(mod);

	}
    
	@Test
	public void testOverloadingMacro1a() throws Exception {
		IAstModule mod = doFrontend("    util = [T, U] [ code(x:T; y:U; z:T ) { x*y-z },\n"
				+ "             macro (x:T; y:T) { util(x, y, 0) }\n"
				+ "            ];\n"
				+ "func = code(x:Int;y:Int => Int) { util(x,y) };\n");
		sanityTest(mod);

		doTypeInfer(mod);

	}
	@Test
	public void testOverloadingMacro1b() throws Exception {
		IAstModule mod = doFrontend("    util = [] [ code(x; y; z ) { x*y-z },\n"
				+ "             macro (x; y) { util(x, y, 0) }\n"
				+ "            ];\n"
				+ "func = code(x:Int;y:Int => Int) { util(x,y) };\n");
		sanityTest(mod);
		
		doTypeInfer(mod);
		
	}
	
	@Test
	public void testOverloadingMacro2() throws Exception {
		IAstModule mod = doFrontend("    util = [] [ code(x, y, z ) { x*y-z },\n"
				+ "             macro (x, y) { util(x, y, 0) }\n"
				+ "            ];\n"
				+ "func = code(x:Int;y:Float => Float) { util(x,y) };\n");
		sanityTest(mod);
		
		doTypeInfer(mod);
		
	}
	
	@Test
	public void testFunctionTypes() throws Exception {
		IAstModule mod = doFrontend("" +
				" funcptr : code;\n"+     
				"func = code(x:Int;y:Float => Float) { funcptr(x,y) };\n");
		sanityTest(mod);
		
		doTypeInfer(mod);
		
	}
	
	@Test
    public void testWhileLoop() throws Exception {
    	dumpTypeInfer = true;
    	IAstModule mod = treeize(
    			"testWhileLoop := code (t; x : Int; y : Float) {\n" +
    			"    @loop: if x > t then { y = y / 2; x = x - 1; goto loop } else goto loop;\n"+
    			 "	y;\n"+
    			"};");
    	doTypeInfer(mod);
    }
	
	@Test
    public void testArrayAccess1() throws Exception {
    	IAstModule mod = doFrontend(
    			"mycode := code(p:Int[10]) {\n"+
    			"   p[5];"+
    			"};\n"+
    			"");

    	sanityTest(mod);
    	
    	IAstAllocStmt astmt = (IAstAllocStmt) mod.getScope().get("mycode").getDefinition();
    	assertTrue(astmt.getType() instanceof LLCodeType);
    	IAstCodeExpr code = (IAstCodeExpr) astmt.getExprs().getFirst();
    	
    	IAstExprStmt stmt = (IAstExprStmt) code.stmts().getFirst();
    	IAstIndexExpr index = (IAstIndexExpr) getValue(stmt.getExpr());
    	assertEquals(typeEngine.INT, index.getType());
    	LLArrayType arrayType = (LLArrayType)index.getExpr().getType();
    	assertEquals(10, arrayType.getArrayCount());
    	assertNull(arrayType.getDynamicSizeExpr());
    	assertEquals(typeEngine.INT, index.getIndex().getType());
    	
    }
    @Test
    public void testArrayAccess1b() throws Exception {
    	IAstModule mod = doFrontend(
    			"mycode := code(p:Int[10]; i) {\n"+
    			"   p[i];"+
    			"};\n"+
    			"");

    	sanityTest(mod);
    	
    	IAstAllocStmt astmt = (IAstAllocStmt) mod.getScope().get("mycode").getDefinition();
    	assertTrue(astmt.getType() instanceof LLCodeType);
    	IAstCodeExpr code = (IAstCodeExpr) astmt.getExprs().getFirst();
    	
    	IAstExprStmt stmt = (IAstExprStmt) code.stmts().getFirst();
    	IAstIndexExpr index = (IAstIndexExpr) getValue(stmt.getExpr());
    	assertEquals(typeEngine.INT, index.getType());
    	LLArrayType arrayType = (LLArrayType)index.getExpr().getType();
    	assertEquals(10, arrayType.getArrayCount());
    	assertNull(arrayType.getDynamicSizeExpr());
    	assertEquals(typeEngine.INT, index.getIndex().getType());
    	
    	
    }
    
    
    @Test
    public void testForwardReferences() throws Exception {
    	dumpTypeInfer = true;
    	IAstModule mod = treeize("forward bar;\n"+
    			"foo = code(p) { bar(p-1, p+1); };\n"+
    			"bar = code(a:Int; b:Int => Int ) { a*b };\n");
    	sanityTest(mod);
    	doTypeInfer(mod);
    	
    }
    
    // base case
    @Test
    public void testSelfRef0() throws Exception {
    	dumpTypeInfer = true;
    	IAstModule mod = treeize(
    			"Class = data {\n"+
    			"  next:Int^;\n"+
    			"};\n"+
    			"testSelfRef0 = code() {\n"+
    			"  inst : Class;\n"+
    			"};\n"+
    	"");
    	sanityTest(mod);
    	doTypeInfer(mod);
    }
    @Test
    public void testSelfRef1() throws Exception {
    	dumpTypeInfer = true;
    	IAstModule mod = treeize(
    			"Class = data {\n"+
    			"  next:Class^;\n"+
    			"};\n"+
    			"testSelfRef1 = code() {\n"+
    			"  inst : Class;\n"+
    			"};\n"+
    	"");
    	sanityTest(mod);
    	mod = (IAstModule) doExpand(mod);
    	doTypeInfer(mod);
    	ISymbol classSym = mod.getScope().get("Class");
    	IAstDataType dataNode = (IAstDataType) ((IAstDefineStmt) classSym.getDefinition()).getMatchingBodyExpr(null);
		//IAstDataType dataNode = (IAstDataType) ((IAstDefineStmt) classSym.getDefinition()).getConcreteSymbols().iterator().next().getDefinition();
    	assertTrue(dataNode.getType().isComplete());
    	System.out.println(dataNode.getType());
    	
    	LLDataType data = (LLDataType) dataNode.getType();
    	assertEquals(1, data.getInstanceFields().length);
    	LLInstanceField field = data.getInstanceFields()[0];
    	LLPointerType ptr = (LLPointerType) field.getType();
    	assertFalse(ptr.getSubType()== data);
    	
    }
    @Test 
    public void testSelfRef2() throws Exception {
    	dumpTypeInfer = true;
    	IAstModule mod = treeize(
    			"Class = data {\n"+
    			"  draw:code(this:Class; count:Int => nil);\n"+
    			"};\n"+
    	"");
    	sanityTest(mod);
    	doTypeInfer(mod);
    	ISymbol classSym = mod.getScope().get("Class");
		IAstDataType dataNode = (IAstDataType) ((IAstDefineStmt) classSym.getDefinition()).getMatchingBodyExpr(null);
    	assertTrue(dataNode.getType().isComplete());
    	System.out.println(dataNode.getType());
    	
    	LLDataType data = (LLDataType) dataNode.getType();
    	assertEquals(1, data.getInstanceFields().length);
    	LLInstanceField field = data.getInstanceFields()[0];
    	LLPointerType funcPtr = (LLPointerType) field.getType();
    	LLCodeType code = (LLCodeType) funcPtr.getSubType();
    	assertEquals(typeEngine.INT, code.getArgTypes()[1]);
    	assertEquals(new LLSymbolType(classSym), code.getArgTypes()[0]);
    }

    @Test
    public void testSelfRef3() throws Exception {
    	dumpTypeInfer = true;
    	IAstModule mod = treeize(
    			"Class = data {\n"+
    			"  draw:code(this:Class; count:Int => nil);\n"+
    			"};\n"+
    			//"doDraw = code(this:Class; count:Int) { count*count };\n"+
    			"testSelfRef3 = code() {\n"+
    			"  inst : Class;\n"+
    			//"  inst.draw = doDraw;\n"+
    			"  inst.draw(inst, 5);\n"+
    			"};\n"+
    	"");
    	sanityTest(mod);
    	doTypeInfer(mod);
    	
    	IAstCodeExpr code = (IAstCodeExpr) ((IAstDefineStmt) mod.getScope().get("testSelfRef3").getDefinition()).getMatchingBodyExpr(null);
    	assertTrue(code.getType().isComplete());
    	System.out.println(code.getType());
    	
    	IAstDataType dataType = (IAstDataType) ((IAstDefineStmt) mod.getScope().get("Class").getDefinition()).getMatchingBodyExpr(null);
    	assertEquals(16, dataType.getType().getBits());
    }
    
    @Test
	public void testLogicalOpsByte1() throws Exception {
    	dumpTypeInfer = true;
    	dumpTreeize = true;
    	IAstModule mod = treeize("testLogicalOpsByte1 = code(x, y:Byte => Byte) { " +
    			"(x|15)" +
    			//" + (x|y) + (x&41) + (x&y) + (x~9) + (x~y) " +
    			"};\n");
    	sanityTest(mod);
    	doTypeInfer(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testLogicalOpsByte1");
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainBodyExpr(def);
    	assertTrue(codeExpr.getType().isComplete());

    	IAstExprStmt exprStmt = (IAstExprStmt) codeExpr.stmts().getFirst();
    	assertEquals(typeEngine.BYTE, exprStmt.getType());
    	IAstBinExpr orExpr = (IAstBinExpr) exprStmt.getExpr();
    	assertEquals(typeEngine.BYTE, orExpr.getType());
    	assertEquals(typeEngine.BYTE, orExpr.getLeft().getType());
    	assertEquals(typeEngine.BYTE, orExpr.getRight().getType());
    }
    @Test
    public void testLogicalOpsByte2() throws Exception {
    	dumpTypeInfer = true;
    	dumpTreeize = true;
    	IAstModule mod = treeize("testLogicalOpsByte2 = code(x, y:Byte ) { " +
    			"(x|15) + (x|y) + (x&41) + (x&y) + (x~9) + (x~y) " +
    	"};\n");
    	sanityTest(mod);
    	doTypeInfer(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testLogicalOpsByte2");
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainBodyExpr(def);
    	assertTrue(codeExpr.getType().isComplete());
    	
    	IAstExprStmt exprStmt = (IAstExprStmt) codeExpr.stmts().getFirst();
    	assertEquals(typeEngine.BYTE, exprStmt.getType());
    	IAstBinExpr orExpr = (IAstBinExpr) exprStmt.getExpr();
    	assertEquals(typeEngine.BYTE, orExpr.getType());
    	assertEquals(typeEngine.BYTE, orExpr.getLeft().getType());
    	assertEquals(typeEngine.BYTE, orExpr.getRight().getType());
    	assertEquals(typeEngine.BYTE, ((IAstBinExpr) orExpr.getLeft()).getLeft().getType());
    	assertEquals(typeEngine.BYTE, ((IAstBinExpr) ((IAstBinExpr) orExpr.getLeft()).getLeft()).getLeft().getType());
    	assertEquals(typeEngine.BYTE, ((IAstBinExpr) ((IAstBinExpr) orExpr.getLeft()).getLeft()).getRight().getType());
    	assertEquals(typeEngine.BYTE, ((IAstBinExpr) ((IAstBinExpr) orExpr.getLeft()).getRight()).getRight().getType());
    }
    @Test
    public void testArithOpsByte1() throws Exception {
    	dumpTypeInfer = true;
    	dumpTreeize = true;
    	IAstModule mod = treeize("testArithOpsByte1 = code(x, y:Byte ) { " +
    			"(x-15) + (x+y) + (x*41) + (x/y) + (x%9) + (x+\\y) " +
    	"};\n");
    	sanityTest(mod);
    	doTypeInfer(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testArithOpsByte1");
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainBodyExpr(def);
    	assertTrue(codeExpr.getType().isComplete());
    	
    	IAstExprStmt exprStmt = (IAstExprStmt) codeExpr.stmts().getFirst();
    	assertEquals(typeEngine.BYTE, exprStmt.getType());
    	IAstBinExpr orExpr = (IAstBinExpr) exprStmt.getExpr();
    	assertEquals(typeEngine.BYTE, orExpr.getType());
    	assertEquals(typeEngine.BYTE, orExpr.getLeft().getType());
    	assertEquals(typeEngine.BYTE, orExpr.getRight().getType());
    	assertEquals(typeEngine.BYTE, ((IAstBinExpr) orExpr.getLeft()).getLeft().getType());
    	assertEquals(typeEngine.BYTE, ((IAstBinExpr) ((IAstBinExpr) orExpr.getLeft()).getLeft()).getLeft().getType());
    	assertEquals(typeEngine.BYTE, ((IAstBinExpr) ((IAstBinExpr) orExpr.getLeft()).getLeft()).getRight().getType());
    	assertEquals(typeEngine.BYTE, ((IAstBinExpr) ((IAstBinExpr) orExpr.getLeft()).getRight()).getRight().getType());
    }
    @Test
    public void testCompareOpsByte1() throws Exception {
    	dumpTypeInfer = true;
    	dumpTreeize = true;
    	IAstModule mod = treeize("testCompareOpsByte1 = code(x, y:Byte ) { " +
    			"(x<15) and(x>=y) " +
    	"};\n");
    	sanityTest(mod);
    	doTypeInfer(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testCompareOpsByte1");
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainBodyExpr(def);
    	assertTrue(codeExpr.getType().isComplete());
    	
    	IAstExprStmt exprStmt = (IAstExprStmt) codeExpr.stmts().getFirst();
    	assertEquals(typeEngine.BOOL, exprStmt.getType());
    	IAstBinExpr andExpr = (IAstBinExpr) exprStmt.getExpr();
    	assertEquals(typeEngine.BOOL, andExpr.getType());
    	IAstTypedExpr ltExpr = andExpr.getLeft();
		assertEquals(typeEngine.BOOL, ltExpr.getType());
		assertEquals(typeEngine.BYTE, ((IAstBinExpr) ltExpr).getLeft().getType());
		assertEquals(typeEngine.BYTE, ((IAstBinExpr) ltExpr).getRight().getType());
    	IAstTypedExpr geExpr = andExpr.getRight();
		assertEquals(typeEngine.BOOL, geExpr.getType());
    	assertEquals(typeEngine.BYTE, ((IAstBinExpr) geExpr).getLeft().getType());
    	assertEquals(typeEngine.BYTE, ((IAstBinExpr) geExpr).getRight().getType());
    }
    
    @Test
    public void testTwoData1() throws Exception {
    	dumpTypeInfer = true;
    	doFrontend(
    			"forward Complex;\n"+
    			"Inner = data {\n"+
    			"  d1,d2:Float;\n"+
    			"  p : Inner^;\n"+
    			"};\n"+
    			"Complex = data {\n"+
    			"  a,b,c:Byte;\n"+
    			"  d : Inner;\n"+
    			" };\n"+
    			"testPtrCalc6 = code() {\n"+
    			"  c : Complex;\n" +
    			"  c.d.p.d2;\n"+
    			"};\n"+
    	"");
    }
    @Test
    public void testTwoData2() throws Exception {
    	IAstModule mod = doFrontend(
    			"forward Complex;\n"+
    			"Inner = data {\n"+
    			"  p : Complex^;\n"+
    			"};\n"+
    			"Complex = data {\n"+
    			"  d : Inner;\n"+
    			" };\n"+
    	"");
    	IAstDefineStmt def;
    	IAstDataType inner;
    	
		def = (IAstDefineStmt) mod.getScope().getNode("Inner");
		inner = (IAstDataType)getMainBodyExpr(def);
    	assertTrue(inner.getType().isComplete());
    	
		def = (IAstDefineStmt) mod.getScope().getNode("Complex");
		inner = (IAstDataType)getMainBodyExpr(def);
    	assertTrue(inner.getType().isComplete());

    }
    @Test
    public void testTwoData3() throws Exception {
    	AstTypedNode.DUMP = true;
    	TypeInference.DUMP = true;
    	dumpTypeInfer = true;
    	IAstModule mod = doFrontend(
    			"forward Complex;\n"+
    			"Inner = data {\n"+
    			"  d1,d2:Float;\n"+
    			"  p : Complex^;\n"+
    			"};\n"+
    			"Complex = data {\n"+
    			"  a,b,c:Byte;\n"+
    			"  d : Inner;\n"+
    			" };\n"+
    	"");
    	IAstDefineStmt def;
    	IAstDataType inner;
    	
		def = (IAstDefineStmt) mod.getScope().getNode("Inner");
		inner = (IAstDataType)getMainBodyExpr(def);
    	assertTrue(inner.getType().isComplete());
    	
		def = (IAstDefineStmt) mod.getScope().getNode("Complex");
		inner = (IAstDataType)getMainBodyExpr(def);
    	assertTrue(inner.getType().isComplete());

    }
    

    @Test
    public void testInnerData1() throws Exception {
    	IAstModule mod = doFrontend(
    			"Complex = data {\n"+
    			"  a,b,c:Byte;\n"+
    			"  Inner = data {\n"+
    			"    d1,d2:Float;\n"+
    			"    p : Complex^;\n"+
    			"  };\n"+
    			"  Inner2 = data {\n"+
    			"    parent : Inner^;\n"+
    			"    next : Inner2^;\n"+
    			"  };\n"+
    			"  d : Inner^;\n"+
    			"  e : Inner2^;\n"+
    			" };\n"+
    			"testInnerData1 = code() {\n"+
    			"  c : Complex;\n" +
    			"  c.e.next.parent.p.b;\n"+
    			"};\n"+
    	"");

    	IAstDefineStmt def;
    	def = (IAstDefineStmt) mod.getScope().getNode("Inner");
    	assertNull(def);
		def = (IAstDefineStmt) mod.getScope().getNode("Complex");
		assertNotNull(def);
		
		IAstDataType complex = (IAstDataType) getMainBodyExpr(def);
		IAstDefineStmt inner = (IAstDefineStmt) complex.getScope().getNode("Inner");
		assertNotNull(inner);
		
		IAstDataType innerData = (IAstDataType) getMainBodyExpr(inner);
		assertTrue(innerData.getType().isComplete());
    	
    	
    }
    


    /** Normal old-style method call */
    @Test
    public void testInnerCode0() throws Exception {
    	IAstModule mod = doFrontend(
    			"Complex = data {\n"+
    			"  a,b,c:Byte;\n"+
    			"  Inner = data {\n"+
    			"    d1,d2:Float;\n"+
    			"    p : Complex^;\n"+
    			"  };\n"+
    			"  summer = code(@this:Complex) {\n"+
    			"    this.d.d1 + this.d.p.c;\n"+
    			"  };\n"+
    			"  d : Inner^;\n"+
    			" };\n"+
    			"testInnerData1 = code() {\n"+
    			"  c : Complex;\n" +
    			"  Complex.summer(c);\n" +
    			"};\n"+
    	"");

    	IAstDefineStmt def;
    	def = (IAstDefineStmt) mod.getScope().getNode("Inner");
    	assertNull(def);
		def = (IAstDefineStmt) mod.getScope().getNode("Complex");
		assertNotNull(def);
		
		IAstDataType complex = (IAstDataType) getMainBodyExpr(def);
		IAstDefineStmt inner = (IAstDefineStmt) complex.getScope().getNode("Inner");
		assertNotNull(inner);
		
		IAstDataType innerData = (IAstDataType) getMainBodyExpr(inner);
		assertTrue(innerData.getType().isComplete());
    	
		// the call to summer as a field ref should be replaced with a direct symbol ref 
		def = (IAstDefineStmt) mod.getScope().getNode("testInnerData1");
		IAstCodeExpr codeExpr = (IAstCodeExpr)getMainBodyExpr(def);
    	assertTrue(codeExpr.getType().isComplete());
    	
    	IAstExprStmt exprStmt = (IAstExprStmt) codeExpr.stmts().list().get(1);
    	IAstFuncCallExpr funcCall = (IAstFuncCallExpr) exprStmt.getExpr();
    	assertTrue(funcCall.getFunction() instanceof IAstSymbolExpr);
    	IAstSymbolExpr symExpr = (IAstSymbolExpr) funcCall.getFunction();
    	assertEquals("summer", symExpr.getSymbol().getName());
    	assertEquals(complex.getScope(), symExpr.getSymbol().getScope());
    	
    }
    
    /** Non-canonical method call, through instance */
    @Test
    public void testInnerCode1() throws Exception {
    	IAstModule mod = doFrontend(
    			"Complex = data {\n"+
    			"  a,b,c:Byte;\n"+
    			"  Inner = data {\n"+
    			"    d1,d2:Float;\n"+
    			"    p : Complex^;\n"+
    			"  };\n"+
    			"  summer = code(@this:Complex) {\n"+
    			"    this.d.d1 + this.d.p.c;\n"+
    			"  };\n"+
    			"  d : Inner^;\n"+
    			" };\n"+
    			"testInnerData1 = code() {\n"+
    			"  c : Complex;\n" +
    			"  c.summer(c);\n" +
    			"};\n"+
    	"");

    	IAstDefineStmt def;
    	def = (IAstDefineStmt) mod.getScope().getNode("Inner");
    	assertNull(def);
		def = (IAstDefineStmt) mod.getScope().getNode("Complex");
		assertNotNull(def);
		
		IAstDataType complex = (IAstDataType) getMainBodyExpr(def);
		IAstDefineStmt inner = (IAstDefineStmt) complex.getScope().getNode("Inner");
		assertNotNull(inner);
		
		IAstDataType innerData = (IAstDataType) getMainBodyExpr(inner);
		assertTrue(innerData.getType().isComplete());
    	
		// the call to summer as a field ref should be replaced with a direct symbol ref 
		def = (IAstDefineStmt) mod.getScope().getNode("testInnerData1");
		IAstCodeExpr codeExpr = (IAstCodeExpr)getMainBodyExpr(def);
    	assertTrue(codeExpr.getType().isComplete());
    	
    	IAstExprStmt exprStmt = (IAstExprStmt) codeExpr.stmts().list().get(1);
    	IAstFuncCallExpr funcCall = (IAstFuncCallExpr) exprStmt.getExpr();
    	assertTrue(funcCall.getFunction() instanceof IAstSymbolExpr);
    	IAstSymbolExpr symExpr = (IAstSymbolExpr) funcCall.getFunction();
    	assertEquals("summer", symExpr.getSymbol().getName());
    	assertEquals(complex.getScope(), symExpr.getSymbol().getScope());
    	
    }
    

    /** Non-canonical method call, through instance */
    @Test
    public void testInnerCode2() throws Exception {
    	dumpTreeize = true;
    	IAstModule mod = doFrontend(
    			"Complex = data {\n"+
    			"  a,b,c:Byte;\n"+
    			"  Inner = data {\n"+
    			"    d1,d2:Float;\n"+
    			"    p : Complex^;\n"+
    			"  };\n"+
    			"  summer = code(@this:Complex) {\n"+
    			"    this.d.d1 + this.d.p.c;\n"+
    			"  };\n"+
    			"  d : Inner^;\n"+
    			" };\n"+
    			"testInnerData1 = code() {\n"+
    			"  c : Complex;\n" +
    			"  c.d.p.summer(c);\n" +
    			"};\n"+
    	"");

    	IAstDefineStmt def;
    	def = (IAstDefineStmt) mod.getScope().getNode("Inner");
    	assertNull(def);
		def = (IAstDefineStmt) mod.getScope().getNode("Complex");
		assertNotNull(def);
		
		IAstDataType complex = (IAstDataType) getMainBodyExpr(def);
		IAstDefineStmt inner = (IAstDefineStmt) complex.getScope().getNode("Inner");
		assertNotNull(inner);
		
		IAstDataType innerData = (IAstDataType) getMainBodyExpr(inner);
		assertTrue(innerData.getType().isComplete());
    	
		// the call to summer as a field ref should be replaced with a direct symbol ref 
		def = (IAstDefineStmt) mod.getScope().getNode("testInnerData1");
		IAstCodeExpr codeExpr = (IAstCodeExpr)getMainBodyExpr(def);
    	assertTrue(codeExpr.getType().isComplete());
    	
    	IAstExprStmt exprStmt = (IAstExprStmt) codeExpr.stmts().list().get(1);
    	IAstFuncCallExpr funcCall = (IAstFuncCallExpr) exprStmt.getExpr();
    	assertTrue(funcCall.getFunction() instanceof IAstSymbolExpr);
    	IAstSymbolExpr symExpr = (IAstSymbolExpr) funcCall.getFunction();
    	assertEquals("summer", symExpr.getSymbol().getName());
    	assertEquals(complex.getScope(), symExpr.getSymbol().getScope());
    	
    }
}


