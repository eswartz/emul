/**
 * Test that we can infer the types on an AST
 * 
 */
package org.ejs.eulang.test;

import static junit.framework.Assert.*;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.ejs.eulang.ast.DumpAST;
import org.ejs.eulang.ast.IAstAllocStmt;
import org.ejs.eulang.ast.IAstAssignStmt;
import org.ejs.eulang.ast.IAstBinExpr;
import org.ejs.eulang.ast.IAstCodeExpr;
import org.ejs.eulang.ast.IAstDefineStmt;
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
	protected void doTypeInfer(IAstModule mod) {
		doTypeInfer(mod, false);
	}
	protected void doTypeInfer(IAstModule mod, boolean expectErrors) {
		List<Message> messages = new ArrayList<Message>();
		TypeInference infer = new TypeInference();
		
		int depth = mod.getDepth();
		
		int passes = 0;
		while (passes++ <= depth) {
			boolean changed = false;
			
			changed = infer.infer(messages, typeEngine, mod);
			
			if (!changed) 
				break;
			
			System.err.flush();
			System.out.println("After type inference:");
			DumpAST dump = new DumpAST(System.out);
			mod.accept(dump);
			
		}
		System.out.println("Inference: " + passes + " passes");
		for (Message msg : messages)
			System.err.println(msg);
		if (!expectErrors)
			assertEquals("expected no errors: " + catenate(messages), 0, messages.size());
		else
			assertTrue("expected errors", messages.size() > 0);
	}

	@Test
    public void testNoChange1() throws Exception {
    	IAstModule mod = treeize(
    			"testNoChange1 = code (x : Int, y : Int => Int) {\n" +
    			"   return x+10;\n" +
    			"};");
    	sanityTest(mod);

    	doTypeInfer(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testNoChange1");
    	typeTest(def.getExpr(), false);
    	
    	assertEquals(typeEngine.getCodeType(typeEngine.INT,  new LLType[] {typeEngine.INT, typeEngine.INT}), def.getExpr().getType());
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
		IAstPrototype prototype = codeExpr.getPrototype();
		assertEquals(typeEngine.INT, prototype.returnType().getType());
    	assertEquals(typeEngine.INT, prototype.argumentTypes()[0].getType());
    	assertEquals(typeEngine.INT, prototype.argumentTypes()[1].getType());
    	assertEquals(typeEngine.INT, ((IAstReturnStmt) codeExpr.getStmts().list().get(0)).getType());
    }

	 @Test 
    public void testBinOps() throws Exception {
    	IAstModule mod = treeize("testBinOps = code { x:=(1*2/3&&4%%45||5<=6>>7<<8>>>85&9^10)||(11<12)>(13<=(14-15)==(16!=17%18+19)); };");
    	sanityTest(mod);
    	
    	doTypeInfer(mod);
    	

    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testBinOps");
    	typeTest(def.getExpr(), false);
    	
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
    	IAstAllocStmt allocStmt = (IAstAllocStmt) codeExpr.getStmts().list().get(0);
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
    	typeTest(def.getExpr(), false);
    	
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
    	typeTest(def.getExpr(), false);
    	
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
    	
    	IAstAllocStmt allocStmt = (IAstAllocStmt) codeExpr.getStmts().list().get(0);
		assertEquals(typeEngine.FLOAT, allocStmt.getType());
		assertTrue(allocStmt.getExpr() instanceof IAstBinExpr);
		IAstBinExpr binExpr = (IAstBinExpr) allocStmt.getExpr();
		assertEquals(typeEngine.FLOAT, binExpr.getLeft().getType());
		assertTrue(binExpr.getLeft() instanceof IAstUnaryExpr && ((IAstUnaryExpr) binExpr.getLeft()).getOp() == IOperation.CAST);
		assertEquals(typeEngine.FLOAT, binExpr.getRight().getType());
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
    	typeTest(def.getExpr(), false);
    	
    	assertEquals(typeEngine.getCodeType(typeEngine.FLOAT,  new LLType[] {typeEngine.INT, typeEngine.INT}), def.getExpr().getType());
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
		IAstPrototype prototype = codeExpr.getPrototype();
		assertEquals(typeEngine.FLOAT, prototype.returnType().getType());
    	assertEquals(typeEngine.INT, prototype.argumentTypes()[0].getType());
    	assertEquals(typeEngine.INT, prototype.argumentTypes()[1].getType());
    	
    	IAstReturnStmt returnStmt = (IAstReturnStmt) codeExpr.getStmts().list().get(0);
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
    	typeTest(def.getExpr(), false);
    	
    	assertEquals(typeEngine.getCodeType(typeEngine.FLOAT,  new LLType[] {typeEngine.INT, typeEngine.INT}), def.getExpr().getType());
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
		IAstPrototype prototype = codeExpr.getPrototype();
		assertEquals(typeEngine.FLOAT, prototype.returnType().getType());
    	assertEquals(typeEngine.INT, prototype.argumentTypes()[0].getType());
    	assertEquals(typeEngine.INT, prototype.argumentTypes()[1].getType());
    	IAstReturnStmt returnStmt = (IAstReturnStmt) codeExpr.getStmts().list().get(0);
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
    	typeTest(def.getExpr(), false);
    	
    	assertEquals(typeEngine.getCodeType(typeEngine.FLOAT,  new LLType[] {typeEngine.INT, typeEngine.INT}), def.getExpr().getType());
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
		IAstPrototype prototype = codeExpr.getPrototype();
		assertEquals(typeEngine.FLOAT, prototype.returnType().getType());
    	assertEquals(typeEngine.INT, prototype.argumentTypes()[0].getType());
    	assertEquals(typeEngine.INT, prototype.argumentTypes()[1].getType());
    	IAstReturnStmt returnStmt = (IAstReturnStmt) codeExpr.getStmts().list().get(0);
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
    	typeTest(def.getExpr(), false);
    	

    	assertEquals(typeEngine.getCodeType(typeEngine.INT,  new LLType[] {typeEngine.INT, typeEngine.FLOAT}), def.getExpr().getType());
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
		IAstPrototype prototype = codeExpr.getPrototype();
		assertEquals(typeEngine.INT, prototype.returnType().getType());
    	assertEquals(typeEngine.INT, prototype.argumentTypes()[0].getType());
    	assertEquals(typeEngine.FLOAT, prototype.argumentTypes()[1].getType());
    	
    	IAstReturnStmt returnStmt = (IAstReturnStmt) codeExpr.getStmts().list().get(0);
		assertEquals(typeEngine.INT, returnStmt.getType());
		
		// >>
		IAstBinExpr binExpr = (IAstBinExpr) returnStmt.getExpr();
		assertEquals(typeEngine.INT, binExpr.getLeft().getType());
		assertEquals(typeEngine.INT, binExpr.getRight().getType());
		assertTrue(binExpr.getLeft() instanceof IAstUnaryExpr && ((IAstUnaryExpr) binExpr.getLeft()).getOp() == IOperation.CAST);
		assertEquals(typeEngine.INT, binExpr.getRight().getType());
		
		IAstBinExpr mulExpr = (IAstBinExpr) ((IAstUnaryExpr) binExpr.getLeft()).getOperand();
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
    	typeTest(def.getExpr(), false);
    	
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
    	typeTest(def.getExpr(), false);
    	
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
    	typeTest(def.getExpr(), false);
    	
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
    	typeTest(def.getExpr(), false);
    	
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
    	typeTest(def.getExpr(), false);
    	
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


