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
import org.ejs.eulang.ast.IAstBinExpr;
import org.ejs.eulang.ast.IAstCodeExpr;
import org.ejs.eulang.ast.IAstDefine;
import org.ejs.eulang.ast.IAstModule;
import org.ejs.eulang.ast.IAstPrototype;
import org.ejs.eulang.ast.IAstReturnStmt;
import org.ejs.eulang.ast.IAstUnaryExpr;
import org.ejs.eulang.ast.IOperation;
import org.ejs.eulang.ast.Message;
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
    	
    	IAstDefine def = (IAstDefine) mod.getScope().getNode("testNoChange1");
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
    public void testDiscoverReturn1() throws Exception {
    	IAstModule mod = treeize(
    			"testDiscoverReturn1 = code (x : Int, y : Int) {\n" +
    			"   return x*1.0;\n" +
    			"};");
    	sanityTest(mod);

    	doTypeInfer(mod);
    	
    	IAstDefine def = (IAstDefine) mod.getScope().getNode("testDiscoverReturn1");
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
    	
    	IAstDefine def = (IAstDefine) mod.getScope().getNode("testDiscoverReturn2");
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
    	
    	IAstDefine def = (IAstDefine) mod.getScope().getNode("testCast1");
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
    public void testCast2() throws Exception {
    	IAstModule mod = treeize("global : Int = 3;\n" +
    			"testCast2 = code (x : Int, y : Float) {\n" +
    			"   return x+10*y>>global;\n" +
    			"};");
    	sanityTest(mod);

    	doTypeInfer(mod);
    	
    	assertTrue(mod.getScope().getSymbols().length == 2);
    	IAstDefine def = (IAstDefine) mod.getScope().getNode("testCast2");
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

	protected void doTypeInfer(IAstModule mod) {
		List<Message> messages = new ArrayList<Message>();
    	TypeInference infer = new TypeInference();
    	
    	infer.infer(messages, typeEngine, mod);
    	
    	System.out.println("After type inference:");
    	DumpAST dump = new DumpAST(System.out);
    	mod.accept(dump);
    	
    	for (Message msg : messages)
    		System.err.println(msg);
    	assertEquals(0, messages.size());
    	
    	messages.clear();
    	infer.propagateTypes(messages, typeEngine, mod);
    	
    	System.out.println("After type propagation:");
    	mod.accept(dump);
    	
    	for (Message msg : messages)
    		System.err.println(msg);
    	assertEquals(0, messages.size());
	}
}


