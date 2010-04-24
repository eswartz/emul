/**
 * Test that we can infer the types on an AST
 * 
 */
package org.ejs.eulang.test;

import static junit.framework.Assert.*;

import java.util.List;

import org.ejs.eulang.IOperation;
import org.ejs.eulang.ast.IAstAllocStmt;
import org.ejs.eulang.ast.IAstAssignStmt;
import org.ejs.eulang.ast.IAstBinExpr;
import org.ejs.eulang.ast.IAstCodeExpr;
import org.ejs.eulang.ast.IAstCondList;
import org.ejs.eulang.ast.IAstDefineStmt;
import org.ejs.eulang.ast.IAstExprStmt;
import org.ejs.eulang.ast.IAstIndexExpr;
import org.ejs.eulang.ast.IAstIntLitExpr;
import org.ejs.eulang.ast.IAstModule;
import org.ejs.eulang.ast.IAstPrototype;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.IAstUnaryExpr;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLArrayType;
import org.ejs.eulang.types.LLCodeType;
import org.ejs.eulang.types.LLType;
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
    	doTypeInfer(getMainBodyExpr(def));
    	typeTest(mod, false);
    	
    	assertEquals(typeEngine.getCodeType(typeEngine.INT,  new LLType[] {typeEngine.INT, typeEngine.INT}), getMainBodyExpr(def).getType());
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainBodyExpr(def);
		IAstPrototype prototype = codeExpr.getPrototype();
		assertEquals(typeEngine.INT, prototype.returnType().getType());
    	assertEquals(typeEngine.INT, prototype.argumentTypes()[0].getType());
    	assertEquals(typeEngine.INT, prototype.argumentTypes()[1].getType());
    	assertEquals(typeEngine.INT, ((IAstExprStmt) codeExpr.stmts().list().get(0)).getType());
    }

	private IAstTypedExpr getMainBodyExpr(IAstDefineStmt def) {
		return def.getMatchingBodyExpr(null);
	}

	 @Test 
    public void testBinOps() throws Exception {
    	IAstModule mod = treeize("testBinOps = code { x:=(Bool(1*2/3) and Bool(4%%45 )or 5<=6>>7<<8>>>85&9 xor 10)or(11<12)>(13<=(14-15)==(16!=17%18+19)); };");
    	sanityTest(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testBinOps");
    	doTypeInfer(getMainBodyExpr(def));
    	typeTest(mod, false);
    	
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainBodyExpr(def);
    	IAstAllocStmt allocStmt = (IAstAllocStmt) codeExpr.stmts().list().get(0);
    	assertEquals(typeEngine.BOOL, allocStmt.getTypeExpr().getType());
    }

    @Test
    public void testInvalidTypes1() throws Exception {
    	IAstModule mod = treeize(
    			"testInvalidTypes1 = code (x : Int; y : Float) {\n" +
    			"   y>>1;\n" +
    			"};");
    	sanityTest(mod);

    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testInvalidTypes1");
    	doTypeInfer(getMainBodyExpr(def), true);
    }
    
    @Test
    public void testInvalidTypes2() throws Exception {
    	IAstModule mod = treeize(
    			"testInvalidTypes2 = code (x : Int; y : Float) {\n" +
    			"   z : Int = 2.0;\n" +
    			"   z | y;\n"+
    			"};");
    	sanityTest(mod);

    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testInvalidTypes2");
    	doTypeInfer(getMainBodyExpr(def), true);
    }
	    
	@Test
    public void testVoidReturn() throws Exception {
    	IAstModule mod = treeize(
    			"testVoidReturn= code (x : Int; y : Int) { };");
    	sanityTest(mod);

    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testVoidReturn");
    	doTypeInfer(getMainBodyExpr(def));
    	typeTest(mod, false);
    	
    	assertEquals(typeEngine.getCodeType(typeEngine.VOID,  new LLType[] {typeEngine.INT, typeEngine.INT}), getMainBodyExpr(def).getType());
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainBodyExpr(def);
		IAstPrototype prototype = codeExpr.getPrototype();
		assertEquals(typeEngine.VOID, prototype.returnType().getType());
    	assertEquals(typeEngine.INT, prototype.argumentTypes()[0].getType());
    	assertEquals(typeEngine.INT, prototype.argumentTypes()[1].getType());
    	
    }
	@Test
    public void testPromotedCast1() throws Exception {
    	IAstModule mod = treeize(
    			"testPromotedCast1 = code (x : Int; y : Int) {\n" +
    			"   p := x*1.0;\n" +
    			"};");
    	sanityTest(mod);

    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testPromotedCast1");
    	doTypeInfer(getMainBodyExpr(def));
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
    	doTypeInfer(getMainBodyExpr(def));
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
    	doTypeInfer(getMainBodyExpr(def));
    	typeTest(mod, false);
    	
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainBodyExpr(def);
    	
    	IAstAssignStmt allocStmt = (IAstAssignStmt) codeExpr.stmts().list().get(1);
		assertEquals(typeEngine.BYTE, allocStmt.getType());
		IAstUnaryExpr castExpr = (IAstUnaryExpr) allocStmt.getExprs().getFirst();
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
    	doTypeInfer(getMainBodyExpr(def));
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
    	doTypeInfer(getMainBodyExpr(def));
    	
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
    	doTypeInfer(getMainBodyExpr(def));
    	
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
    	doTypeInfer(getMainBodyExpr(def));
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
    	doTypeInfer(getMainBodyExpr(def));
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
    	doTypeInfer(getMainBodyExpr(def));
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
    	doTypeInfer(getMainBodyExpr(def));
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
    			"testCast2 = code (x : Int; y : Float) {\n" +
    			"   x+10*y>>global;\n" +
    			"};");
    	sanityTest(mod);

    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testCast2");
    	doTypeInfer(getMainBodyExpr(def), true);
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
    	doTypeInfer(getMainBodyExpr(def));
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
    	doTypeInfer(getMainBodyExpr(def));
    	
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
    	doTypeInfer(getMainBodyExpr(def));
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
    	doTypeInfer(getMainBodyExpr(def));
    	
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
    	doTypeInfer(getMainBodyExpr(def));
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
    	doTypeInfer(getMainBodyExpr(def));
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
    public void testExample1() throws Exception {
    	IAstModule mod = treeize(
    			"testExample1 = code (z; a : Byte) {\n" +
    			"   z = a | Byte(6);\n" +
    			"   z;\n"+
    			"};");
    	sanityTest(mod);

    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testExample1");
    	doTypeInfer(getMainBodyExpr(def));
    	typeTest(mod, false);
    	
    	assertEquals(typeEngine.getCodeType(typeEngine.BYTE,  new LLType[] {typeEngine.BYTE, typeEngine.BYTE}), getMainBodyExpr(def).getType());
    	
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainBodyExpr(def);
    	IAstAssignStmt assign = (IAstAssignStmt) codeExpr.stmts().list().get(0);
    	assertEquals(typeEngine.BYTE, assign.getType());
    	assertEquals(typeEngine.BYTE, assign.getExprs().getFirst().getType());
    	assertTrue(assign.getExprs().getFirst() instanceof IAstBinExpr);
    }
	

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
    	doTypeInfer(getMainBodyExpr(def));
    	typeTest(mod, false);

		LLType intRef = typeEngine.getRefType(typeEngine.INT);
	   	assertEquals(typeEngine.getCodeType(typeEngine.VOID,  
	   			new LLType[] { intRef, intRef }), 
	   			getMainBodyExpr(def).getType());
	   	
	   	IAstAssignStmt assn = (IAstAssignStmt) ((IAstCodeExpr) getMainBodyExpr(def)).stmts().getLast();
	   	assertEquals(intRef, assn.getType());
    }

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
    	doTypeInfer(def.getExpr());
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
    	doTypeInfer(def.getExpr());
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
    	doTypeInfer(def.getExpr());
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
    	doTypeInfer(getMainBodyExpr(def));
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
    	
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testShortCircuitAndOr");
    	doTypeInfer(getMainBodyExpr(def));
    	typeTest(mod, false);
    	
    	assertEquals(typeEngine.getCodeType(typeEngine.INT,  
    			new LLType[] { typeEngine.INT, typeEngine.INT, typeEngine.INT }), 
    			getMainBodyExpr(def).getType());
    	
    	IAstExprStmt stmt1 = (IAstExprStmt)((IAstCodeExpr)getMainBodyExpr(def)).stmts().getFirst();
		assertEquals(typeEngine.INT, stmt1.getType());
		assertEquals(typeEngine.INT, ((IAstCondList)stmt1.getExpr()).getType());
	}

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
    	doTypeInfer(getMainBodyExpr(def));
    	typeTest(mod, false);
    	
    	LLType intRef = typeEngine.getRefType(typeEngine.INT);
    	assertEquals(typeEngine.getCodeType(typeEngine.INT,  
    			new LLType[] { typeEngine.INT, intRef, typeEngine.INT }), 
    			getMainBodyExpr(def).getType());
    	
    	IAstExprStmt stmt1 = (IAstExprStmt)((IAstCodeExpr)getMainBodyExpr(def)).stmts().getFirst();
		assertEquals(typeEngine.INT, stmt1.getType());
		assertEquals(typeEngine.INT, ((IAstCondList)stmt1.getExpr()).getType());
	}
	

	 
    @Test
    public void testTuples1() throws Exception {
    	IAstModule mod = treeize("testTuples1 = code (x,y) { (y+0,x+0); };");
    	sanityTest(mod);
    	

    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testTuples1");
    	doTypeInfer(getMainBodyExpr(def));
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

    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testTuples3");
    	doTypeInfer(getMainBodyExpr(def));
    	typeTest(mod, false);
    	
    }
    @Test
    public void testTuples4() throws Exception {
    	IAstModule mod = treeize("swap = code (x,y => (Int, Int)) { (y,x); };\n" +
    			"testTuples4 = code (a,b) { (a, b) = swap(4, 5); }; \n");
    	sanityTest(mod);
    	
    	// module gets allocations, but not defines
    	doTypeInfer(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testTuples4");
    	doTypeInfer(getMainBodyExpr(def));
    	
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
    			"bop = code(x,y) { y };\n"+
    			"testTuples5 = code (a,b) { (a, b) = bop(5, swap(4, 5)); }; \n");
    	sanityTest(mod);
    	
    	doTypeInfer(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("bop");
    	assertInstanceCount(1, 1, def);
    }
    
	@Test
    public void testGenerics0() throws Exception {
    	IAstModule mod = treeize("add = code (x,y) { x+y };\n" +
    			"testGenerics0 = code (a:Int;b:Int) { add(a,b);  }; \n");
    	sanityTest(mod);
    	
    	doTypeInfer(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("add");
    	assertInstanceCount(1, 1, def);
    	
    	def = (IAstDefineStmt) mod.getScope().getNode("testGenerics0");
    	assertFalse(getMainBodyExpr(def).getType().isGeneric());

    }
    
    @Test
    public void testGenerics0b() throws Exception {
    	IAstModule mod = treeize("add = code (x,y) { x+y };\n" +
    			"testGenerics0b = code (a:Int;b:Int) { add(a,b) + add(10.0,b);  }; \n");
    	sanityTest(mod);
    	
    	doTypeInfer(mod);
    	
    	IAstDefineStmt addDef = (IAstDefineStmt) mod.getScope().getNode("add");
    	assertInstanceCount(1, 2, addDef);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testGenerics0b");
    	assertFalse(getMainBodyExpr(def).getType().isGeneric());
    	
    	IAstTypedExpr addBody = addDef.getMatchingBodyExpr(null);
    	assertNotNull(addBody);
    	List<IAstTypedExpr> exps = addDef.bodyToInstanceMap().get(addBody.getType());
    	assertNotNull(exps);
    	assertEquals(2, exps.size());
    	assertEquals(typeEngine.getCodeType(typeEngine.INT, new LLType[] { typeEngine.INT, typeEngine.INT }), exps.get(0).getType());
    	assertEquals(typeEngine.getCodeType(typeEngine.FLOAT, new LLType[] { typeEngine.FLOAT, typeEngine.INT }), exps.get(1).getType());

    	// make sure the casting worked properly
    	exps.get(1).validateType(typeEngine);
    }
    @Test
    public void testGenerics1() throws Exception {
    	IAstModule mod = treeize("swap = code (x,y) { (y,x); };\n" +
    			"testGenerics1 = code (a,b) { (a, b) = swap(4, 5);  }; \n");
    	sanityTest(mod);
    	
    	doTypeInfer(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("swap");
    	assertInstanceCount(1, 1, def);
    	
    	def = (IAstDefineStmt) mod.getScope().getNode("testGenerics1");
    	assertFalse(getMainBodyExpr(def).getType().isGeneric());

    }
    @Test
    public void testGenerics2() throws Exception {
    	IAstModule mod = treeize("swap = code (x,y) { (y,x); };\n" +
    			"testGenerics2 = code (a,b) { (a, b) = swap(4, 5); (x, y) := swap(1.0, 9); }; \n");
    	sanityTest(mod);
    	
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
	public void testOverloadingMacro1() throws Exception {
		IAstModule mod = doFrontend("    util = [ code(x, y, z ) { x*y-z },\n"
				+ "             macro (x, y) { util(x, y, 0) }\n"
				+ "            ];\n"
				+ "func = code(x:Int;y:Int => Int) { util(x,y) };\n");
		sanityTest(mod);

		doTypeInfer(mod);

	}
	@Test
	public void testOverloadingMacro2() throws Exception {
		IAstModule mod = doFrontend("    util = [ code(x, y, z ) { x*y-z },\n"
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
    	IAstIndexExpr index = (IAstIndexExpr) stmt.getExpr();
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
    	IAstIndexExpr index = (IAstIndexExpr) stmt.getExpr();
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
}


