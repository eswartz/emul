/**
 * 
 */
package org.ejs.eulang.test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertSame;

import java.util.List;

import org.ejs.eulang.ast.IAstAssignStmt;
import org.ejs.eulang.ast.IAstBlockStmt;
import org.ejs.eulang.ast.IAstCodeExpr;
import org.ejs.eulang.ast.IAstCondExpr;
import org.ejs.eulang.ast.IAstCondList;
import org.ejs.eulang.ast.IAstDefineStmt;
import org.ejs.eulang.ast.IAstExprStmt;
import org.ejs.eulang.ast.IAstFloatLitExpr;
import org.ejs.eulang.ast.IAstFuncCallExpr;
import org.ejs.eulang.ast.IAstGotoStmt;
import org.ejs.eulang.ast.IAstIntLitExpr;
import org.ejs.eulang.ast.IAstLabelStmt;
import org.ejs.eulang.ast.IAstModule;
import org.ejs.eulang.ast.IAstNodeList;
import org.ejs.eulang.ast.IAstPrototype;
import org.ejs.eulang.ast.IAstSymbolExpr;
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
    
    @SuppressWarnings("unchecked")
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
    	assertTrue(getMainExpr(def) instanceof IAstIntLitExpr);
    	assertEquals("3", ((IAstIntLitExpr)getMainExpr(def)).getLiteral());
    	assertEquals((long) 3, ((IAstIntLitExpr)getMainExpr(def)).getValue());
    	assertTrue(getMainExpr(def) instanceof IAstTypedExpr);
    	assertTrue(((IAstTypedExpr)getMainExpr(def)).getType().equals(typeEngine.INT));
    	
    }

	private IAstTypedExpr getMainExpr(IAstDefineStmt def) {
		return def.getMatchingBodyExpr(null);
	}
    
    @Test
    public void testOneEntryCodeModule0() throws Exception {
    	IAstModule mod = treeize("foo = code (x,y) {\n};");
    	sanityTest(mod);
    	
    	assertEquals(1, mod.getScope().getSymbols().length);
    	assertEquals(1, mod.getChildren().length);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("foo");
    	assertEquals("foo", def.getSymbol().getName());
    	assertTrue(getMainExpr(def) instanceof IAstCodeExpr);
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainExpr(def);
    	assertEquals(mod.getScope(), codeExpr.getScope().getParent());
    	
		IAstPrototype prototype = codeExpr.getPrototype();
		assertEquals(2, prototype.argumentTypes().length);
		assertEquals("x", prototype.argumentTypes()[0].getName());
		assertNull(prototype.argumentTypes()[0].getType());
		assertEquals("y", prototype.argumentTypes()[1].getName());
		assertNull(prototype.argumentTypes()[1].getType());
		
		assertEquals(prototype.argumentTypes()[0].getSymbolExpr().getSymbol().getScope(), codeExpr.getScope());
		assertEquals(prototype.argumentTypes()[1].getSymbolExpr().getSymbol().getScope(), codeExpr.getScope());
		
		assertNotNull(codeExpr.stmts());
		assertTrue(codeExpr.stmts().list().isEmpty());
    }
    @Test
    public void testOneEntryCodeModuleReturnNull() throws Exception {
    	IAstModule mod = treeize("foo = code (x,y) { };");
    	sanityTest(mod);
    	
    }
    @Test
    public void testOneEntryCodeModuleReturnExpr() throws Exception {
    	IAstModule mod = treeize("foo = code (x,y) { x+y; };");
    	sanityTest(mod);
    	
    }
    @Test
    public void testVarDecls() throws Exception {
    	IAstModule mod = treeize("bar := 2; baz : Float ; pp : Float = 3.3; " +
    			"foo = code (x,y) { p : Float = 3.9; x+y*p; };");
    	sanityTest(mod);
    	
    }
    @Test
    public void testVarDeclsRedef1() throws Exception {
    	IAstModule mod = treeizeFail("foo = code (x,y) { p : Float = 3.9; p := 44; x+y*p; };");
    	sanityTest(mod);
    	
    }
    @Test
    public void testVarDeclsRedef2() throws Exception {
    	IAstModule mod = treeizeFail("p : Float = 3.9; p := 44; ");
    	sanityTest(mod);
    	
    }
    @Test
    public void testVarDecls2() throws Exception {
    	IAstModule mod = treeize("foo = code (x,y) { p : Float = 3.9; p = 44; x+y*p; };");
    	sanityTest(mod);
    	
    }
    @Test 
    public void testBinOps() throws Exception {
    	IAstModule mod = treeize("opPrec1 = code { x:=1*2/3%4%%4.5+5-6>>7<<8>>>8.5&9^10|11<12>13<=14>=15==16!=17 and 18 or 19; };");
    	sanityTest(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("opPrec1");
    	assertNotNull(def);
    	assertEquals("opPrec1", def.getSymbol().getName());
    	assertTrue(getMainExpr(def) instanceof IAstCodeExpr);
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainExpr(def);
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
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainExpr(def);
    	IAstExprStmt stmt = (IAstExprStmt) codeExpr.stmts().list().get(0);
    	IAstFuncCallExpr callExpr = (IAstFuncCallExpr) stmt.getExpr();
    	assertEquals(callee.getSymbolExpr(), callExpr.getFunction());
    }
    
    @Test
    public void testGoto1() throws Exception {
    	IAstModule mod = treeize("testGoto = code { @foo: 0; };");
    	sanityTest(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testGoto");
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainExpr(def);
    	IAstLabelStmt lab = (IAstLabelStmt) codeExpr.stmts().list().get(0);
    	assertEquals("foo", lab.getLabel().getSymbol().getName());
    	assertEquals(codeExpr.getScope(), lab.getLabel().getSymbol().getScope());
    }
    
    @Test
    public void testGoto2() throws Exception {
    	IAstModule mod = treeize("testGoto = code { @foo: \n" +
    			"@foo;\n" +
    			"};");
    	sanityTest(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testGoto");
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainExpr(def);
    	IAstLabelStmt lab = (IAstLabelStmt) codeExpr.stmts().list().get(0);
    	assertEquals("foo", lab.getLabel().getSymbol().getName());
    	assertEquals(codeExpr.getScope(), lab.getLabel().getSymbol().getScope());
    	
    	IAstGotoStmt goto1 = (IAstGotoStmt) codeExpr.stmts().list().get(1);
    	assertEquals(lab.getLabel(), goto1.getLabel());
    }
    @Test
    public void testGoto3() throws Exception {
    	IAstModule mod = treeize("testGoto = code { @foo: \n" +
    			"{ @foo: select [ true then 1 else 0 ]; \n"+
    			"@foo;\n" +
    			"@:foo;\n" +
    			"};\n"+
    			"@foo;\n" +
    			"};");
    	sanityTest(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testGoto");
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainExpr(def);
    	IAstLabelStmt lab = (IAstLabelStmt) codeExpr.stmts().list().get(0);
    	assertEquals("foo", lab.getLabel().getSymbol().getName());
    	assertEquals(codeExpr.getScope(), lab.getLabel().getSymbol().getScope());

    	IAstBlockStmt block = (IAstBlockStmt) codeExpr.stmts().list().get(1);
    	
	    	IAstLabelStmt lab2 = (IAstLabelStmt) block.stmts().list().get(0);
	    	assertEquals("foo", lab2.getLabel().getSymbol().getName());
	    	assertEquals(codeExpr.getScope(), lab2.getLabel().getSymbol().getScope().getParent());
	    	IAstGotoStmt goto1 = (IAstGotoStmt) block.stmts().list().get(2);
	    	assertEquals(lab2.getLabel(), goto1.getLabel());
	    	IAstGotoStmt goto2 = (IAstGotoStmt) block.stmts().list().get(3);
	    	assertEquals(lab.getLabel(), goto2.getLabel());
	    	
    	IAstGotoStmt goto3 = (IAstGotoStmt) codeExpr.stmts().list().get(2);
    	assertEquals(lab.getLabel(), goto3.getLabel());
    }
    
    /**
     * if and while are functions which take blocks.  Before type inference, we must
     * be able to handle either expressions, scope blocks, or actual code blocks as
     * parameters. 
     * @throws Exception
     */
    @Test
    public void testImplicitBlocks1() throws Exception {
    	IAstModule mod = treeize(
    			" if = code { };\n"+
    			"testImplicitBlocks1 = code (t, x, y) {\n" +
    			"   if(t, x = 9, y = 7);\n"+
    			"};");
    	sanityTest(mod);
    
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testImplicitBlocks1");
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainExpr(def);
    	IAstExprStmt ret = (IAstExprStmt) codeExpr.stmts().list().get(0);
    	IAstFuncCallExpr funcCall = (IAstFuncCallExpr) ret.getExpr();
    	List<IAstTypedExpr> arglist = funcCall.arguments().list();
		assertEquals(3, arglist.size());
    	assertTrue(arglist.get(0) instanceof IAstSymbolExpr);
    	assertTrue(arglist.get(1) instanceof IAstAssignStmt);
    	assertTrue(arglist.get(2) instanceof IAstAssignStmt);
    }
    /**
     * if and while are functions which take blocks.  Before type inference, we must
     * be able to handle either expressions, scope blocks, or actual code blocks as
     * parameters. 
     * @throws Exception
     */
    @Test
    public void testImplicitBlocks2() throws Exception {
    	IAstModule mod = treeize(
    			" if = code { };\n"+
    			"testImplicitBlocks2 = code (t, x, y) {\n" +
    			"   if(t, { x = x + 9; x; }, { y = y + 7; y; });\n"+
    			"};");
    	sanityTest(mod);
    
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testImplicitBlocks2");
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainExpr(def);
    	IAstExprStmt ret = (IAstExprStmt) codeExpr.stmts().list().get(0);
    	IAstFuncCallExpr funcCall = (IAstFuncCallExpr) ret.getExpr();
    	List<IAstTypedExpr> arglist = funcCall.arguments().list();
		assertEquals(3, arglist.size());
    	assertTrue(arglist.get(0) instanceof IAstSymbolExpr);
    	assertTrue(arglist.get(1) instanceof IAstCodeExpr);
    	assertTrue(arglist.get(2) instanceof IAstCodeExpr);
    }
    /**
     * if and while are functions which take blocks.  Before type inference, we must
     * be able to handle either expressions, scope blocks, or actual code blocks as
     * parameters. 
     * @throws Exception
     */
    @Test
    public void testImplicitBlocks3() throws Exception {
    	IAstModule mod = treeize(
    			" if = code { };\n"+
    			"testImplicitBlocks3 = code (t, x, y) {\n" +
    			"   if(t, code ( => Int) { x = x + 9; x; }, code ( => Int) { y = y + 7;  y; });\n"+
    			"};");
    	sanityTest(mod);
    
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testImplicitBlocks3");
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainExpr(def);
    	IAstExprStmt ret = (IAstExprStmt) codeExpr.stmts().list().get(0);
    	IAstFuncCallExpr funcCall = (IAstFuncCallExpr) ret.getExpr();
    	List<IAstTypedExpr> arglist = funcCall.arguments().list();
		assertEquals(3, arglist.size());
    	assertTrue(arglist.get(0) instanceof IAstSymbolExpr);
    	assertTrue(arglist.get(1) instanceof IAstCodeExpr);
    	assertTrue(arglist.get(2) instanceof IAstCodeExpr);
    }
    
    @Test
    public void testMacroArgs1() throws Exception {
    	IAstModule mod = treeize(
    			" testMacroArgs1 = macro (macro t : code, macro mthen : code, macro melse : code) { };\n");
    	sanityTest(mod);
    
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testMacroArgs1");
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainExpr(def);
    	
    	IAstPrototype proto = codeExpr.getPrototype();
    	assertTrue(proto.argumentTypes()[0].isMacro());
    	assertTrue(proto.argumentTypes()[1].isMacro());
    	assertTrue(proto.argumentTypes()[2].isMacro());
    }
    @Test
    public void testMacroArgs2() throws Exception {
    	treeizeFail(" testMacroArgs1 = code (macro t : code, macro mthen : code, macro melse : code) { };\n");
    }
    
    @Test
    public void testCondStar1() throws Exception {
    	IAstModule mod = treeize(
    		" testCondStar1 = code (t) { select [ 1>t then 1\n" +
    		"		||	t!=2 and t!=1 then { x:= 9+t; -x; }\n" +
    		"		||	else  0.4 \n" +
    		"		]; };\n");
		sanityTest(mod);
		
		IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testCondStar1");
		IAstCodeExpr codeExpr = (IAstCodeExpr)getMainExpr(def);
		IAstExprStmt exprStmt = (IAstExprStmt) codeExpr.stmts().list().get(0);
		IAstCondList condList = (IAstCondList) exprStmt.getExpr();
		assertEquals(3, condList.getCondExprs().nodeCount());
		IAstCondExpr condExpr;
		condExpr = condList.getCondExprs().list().get(0);
		assertTrue(condExpr.getExpr() instanceof IAstIntLitExpr);
		condExpr = condList.getCondExprs().list().get(1);
		assertTrue(condExpr.getExpr() instanceof IAstCodeExpr);
		condExpr = condList.getCondExprs().list().get(2);
		assertTrue(condExpr.getExpr() instanceof IAstFloatLitExpr);
    }
    
    @Test
    public void testPointers1() throws Exception {
    	IAstModule mod = treeize(
        		" badSwap_testPointers1 = code (x : Int&, y : Int& => null) {\n" +
        		"};\n");
    		sanityTest(mod);
    }
    
    
    @Test
    public void testTuples1() throws Exception {
    	IAstModule mod = treeize("tuples1 = code (x,y) { (y,x); };");
    	sanityTest(mod);
    }
    @Test
    public void testTuples2() throws Exception {
    	IAstModule mod = treeize("tuples2 = (7, code (x,y) { (y,x); });");
    	sanityTest(mod);
    }
    @Test
    public void testTuples3() throws Exception {
    	IAstModule mod = treeize("tuples3 = code (x,y => (Int, Int)) { (y,x); };");
    	sanityTest(mod);
    }
    @Test
    public void testTuples4() throws Exception {
    	dumpTreeize = true;
    	IAstModule mod = treeize("swap = code (x,y => (Int, Int)) { (y,x); };\n" +
    			"testTuples4 = code (x,y) { (a, b) = swap(4, 5); }; \n");
    	sanityTest(mod);
    }
    @Test
    public void testTuples4b() throws Exception {
    	dumpTreeize = true;
    	IAstModule mod = treeize(
    			"testTuples4 = code (x,y) { (a, b) := (4, 5); }; \n");
    	sanityTest(mod);
    }
    
   
}


