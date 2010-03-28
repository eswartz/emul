/**
 * 
 */
package org.ejs.eulang.test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertSame;

import org.ejs.eulang.ast.IAstCodeExpr;
import org.ejs.eulang.ast.IAstDefineStmt;
import org.ejs.eulang.ast.IAstExprStatement;
import org.ejs.eulang.ast.IAstFuncCallExpr;
import org.ejs.eulang.ast.IAstIntLitExpr;
import org.ejs.eulang.ast.IAstModule;
import org.ejs.eulang.ast.IAstNodeList;
import org.ejs.eulang.ast.IAstPrototype;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.junit.Test;

/**
 * @author ejs
 *
 */
public class TestGenerator extends BaseParserTest {

    @Test
    public void testEmptyModule() throws Exception {
    	IAstModule mod = treeize("");
    	sanityTest(mod);
    	
    	assertTrue(mod.getScope().getSymbols().length == 0);
    }
    
    @Test
    public void testOneEntryConstModule() throws Exception {
    	IAstModule mod = treeize("foo = 3;");
    	sanityTest(mod);
    	
    	assertEquals(1, mod.getScope().getSymbols().length);
    	assertEquals(1, mod.getChildren().length);
    	
    	IAstDefineStmt def0 = (IAstDefineStmt) ((IAstNodeList) mod.getChildren()[0]).list().get(0);
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("foo");
    	assertSame(def0, def);
    	
    	assertEquals("foo", def.getSymbol().getName());
    	assertTrue(def.getExpr() instanceof IAstIntLitExpr);
    	assertEquals("3", ((IAstIntLitExpr)def.getExpr()).getLiteral());
    	assertEquals((long) 3, ((IAstIntLitExpr)def.getExpr()).getValue());
    	assertTrue(def.getExpr() instanceof IAstTypedExpr);
    	assertTrue(((IAstTypedExpr)def.getExpr()).getType().equals(typeEngine.INT));
    	
    }
    
    @Test
    public void testOneEntryCodeModule0() throws Exception {
    	IAstModule mod = treeize("foo = code (x,y) { };");
    	sanityTest(mod);
    	
    	assertEquals(1, mod.getScope().getSymbols().length);
    	assertEquals(1, mod.getChildren().length);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("foo");
    	assertEquals("foo", def.getSymbol().getName());
    	assertTrue(def.getExpr() instanceof IAstCodeExpr);
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
    	assertEquals(mod.getScope(), codeExpr.getScope().getParent());
    	
		IAstPrototype prototype = codeExpr.getPrototype();
		assertEquals(2, prototype.argumentTypes().length);
		assertEquals("x", prototype.argumentTypes()[0].getName());
		assertNull(prototype.argumentTypes()[0].getType());
		assertEquals("y", prototype.argumentTypes()[1].getName());
		assertNull(prototype.argumentTypes()[1].getType());
		
		assertEquals(prototype.argumentTypes()[0].getSymbolExpr().getSymbol().getScope(), codeExpr.getScope());
		assertEquals(prototype.argumentTypes()[1].getSymbolExpr().getSymbol().getScope(), codeExpr.getScope());
		
		assertNotNull(codeExpr.getStmts());
		assertTrue(codeExpr.getStmts().list().isEmpty());
    }
    @Test
    public void testOneEntryCodeModuleReturnNull() throws Exception {
    	IAstModule mod = treeize("foo = code (x,y) { return ; };");
    	sanityTest(mod);
    	
    }
    @Test
    public void testOneEntryCodeModuleReturnExpr() throws Exception {
    	IAstModule mod = treeize("foo = code (x,y) { return x+y; };");
    	sanityTest(mod);
    	
    }
    @Test
    public void testVarDecls() throws Exception {
    	IAstModule mod = treeize("bar := 2; baz : Float ; pp : Float = 3.3; " +
    			"foo = code (x,y) { p : Float = 3.9; return x+y*p; };");
    	sanityTest(mod);
    	
    }
    @Test
    public void testVarDeclsRedef1() throws Exception {
    	IAstModule mod = treeizeFail("foo = code (x,y) { p : Float = 3.9; p := 44; return x+y*p; };");
    	sanityTest(mod);
    	
    }
    @Test
    public void testVarDeclsRedef2() throws Exception {
    	IAstModule mod = treeizeFail("p : Float = 3.9; p := 44; ");
    	sanityTest(mod);
    	
    }
    @Test
    public void testVarDecls2() throws Exception {
    	IAstModule mod = treeize("foo = code (x,y) { p : Float = 3.9; p = 44; return x+y*p; };");
    	sanityTest(mod);
    	
    }
    @Test 
    public void testBinOps() throws Exception {
    	IAstModule mod = treeize("opPrec1 = code { x:=1*2/3%4%%4.5+5-6>>7<<8>>>8.5&9^10|11<12>13<=14>=15==16!=17&&18||19; };");
    	sanityTest(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("opPrec1");
    	assertNotNull(def);
    	assertEquals("opPrec1", def.getSymbol().getName());
    	assertTrue(def.getExpr() instanceof IAstCodeExpr);
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
    	assertEquals(mod.getScope(), codeExpr.getScope().getParent());
    	
		IAstPrototype prototype = codeExpr.getPrototype();
		assertEquals(0, prototype.argumentTypes().length);
		assertNull(prototype.returnType().getType());
    }
    
    @Test
    public void testCalls() throws Exception {
    	IAstModule mod = treeize("callee = code(a,b) {} ;  testCalls = code { callee(7,8); };");
    	sanityTest(mod);
    	
    	IAstDefineStmt callee = (IAstDefineStmt) mod.getScope().getNode("callee");
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testCalls");
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
    	IAstExprStatement stmt = (IAstExprStatement) codeExpr.getStmts().list().get(0);
    	IAstFuncCallExpr callExpr = (IAstFuncCallExpr) stmt.getExpr();
    	assertEquals(callee.getSymbolExpr(), callExpr.getFunction());
    }
}


