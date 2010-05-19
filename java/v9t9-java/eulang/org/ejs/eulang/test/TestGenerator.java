/**
 * 
 */
package org.ejs.eulang.test;

import static junit.framework.Assert.*;

import java.util.List;

import org.ejs.eulang.IOperation;
import org.ejs.eulang.ast.IAstAllocStmt;
import org.ejs.eulang.ast.IAstAssignStmt;
import org.ejs.eulang.ast.IAstBlockStmt;
import org.ejs.eulang.ast.IAstCodeExpr;
import org.ejs.eulang.ast.IAstCondExpr;
import org.ejs.eulang.ast.IAstCondList;
import org.ejs.eulang.ast.IAstDataType;
import org.ejs.eulang.ast.IAstDefineStmt;
import org.ejs.eulang.ast.IAstDerefExpr;
import org.ejs.eulang.ast.IAstExprStmt;
import org.ejs.eulang.ast.IAstFloatLitExpr;
import org.ejs.eulang.ast.IAstFuncCallExpr;
import org.ejs.eulang.ast.IAstGotoStmt;
import org.ejs.eulang.ast.IAstInstanceExpr;
import org.ejs.eulang.ast.IAstIntLitExpr;
import org.ejs.eulang.ast.IAstLabelStmt;
import org.ejs.eulang.ast.IAstModule;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstNodeList;
import org.ejs.eulang.ast.IAstNilLitExpr;
import org.ejs.eulang.ast.IAstPointerType;
import org.ejs.eulang.ast.IAstPrototype;
import org.ejs.eulang.ast.IAstStmtListExpr;
import org.ejs.eulang.ast.IAstSymbolExpr;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.junit.Test;

/**
 * @author ejs
 *
 */
public class TestGenerator extends BaseParserTest {

	{
		//dumpTreeize = true;
	}
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
    	assertEquals(mod.getScope(), codeExpr.getScope().getParent().getParent());
    	
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
    	dumpTreeize = true;
    	IAstModule mod = treeize("opPrec1 = code { x:=1*2/3%4+\\4.5+5-6>>7>>|4<<|7.5<<8+>>8.5&9 ~" +
    			" 10|11+<-11<12+>-12>13+<=-33<=14+>=0>=15==16!=17 and 18 or 19; };");
    	sanityTest(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("opPrec1");
    	assertNotNull(def);
    	assertEquals("opPrec1", def.getSymbol().getName());
    	assertTrue(getMainExpr(def) instanceof IAstCodeExpr);
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainExpr(def);
    	assertEquals(mod.getScope(), codeExpr.getScope().getParent().getParent());
    	
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
    	IAstFuncCallExpr callExpr = (IAstFuncCallExpr) getValue(stmt.getExpr());
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
    			"goto foo;\n" +
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
    			"{ @foo: if true then 1 else 0 ; \n"+
    			"goto foo;\n" +
    			"goto :foo;\n" +
    			"};\n"+
    			"goto foo;\n" +
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
    			" iff = code { };\n"+
    			"testImplicitBlocks1 = code (t, x, y) {\n" +
    			"   iff(t, x = 9, y = 7);\n"+
    			"};");
    	sanityTest(mod);
    
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testImplicitBlocks1");
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainExpr(def);
    	IAstExprStmt ret = (IAstExprStmt) codeExpr.stmts().list().get(0);
    	IAstFuncCallExpr funcCall = (IAstFuncCallExpr) getValue(ret.getExpr());
    	List<IAstTypedExpr> arglist = funcCall.arguments().list();
		assertEquals(3, arglist.size());
    	assertTrue(baseOf(arglist.get(0)) instanceof IAstSymbolExpr);
    	assertTrue(arglist.get(1) instanceof IAstAssignStmt);
    	assertTrue(arglist.get(2) instanceof IAstAssignStmt);
    }
    /**
	 * @param expr
	 * @return
	 */
	private IAstTypedExpr baseOf(IAstTypedExpr expr) {
		if (expr instanceof IAstDerefExpr)
			return ((IAstDerefExpr) expr).getExpr();
		return expr;
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
    			" iff = code { };\n"+
    			"testImplicitBlocks2 = code (t, x, y) {\n" +
    			"   iff(t, { x = x + 9; x; }, { y = y + 7; y; });\n"+
    			"};");
    	sanityTest(mod);
    
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testImplicitBlocks2");
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainExpr(def);
    	IAstExprStmt ret = (IAstExprStmt) codeExpr.stmts().list().get(0);
    	IAstFuncCallExpr funcCall = (IAstFuncCallExpr) getValue(ret.getExpr());
    	List<IAstTypedExpr> arglist = funcCall.arguments().list();
		assertEquals(3, arglist.size());
    	assertTrue(baseOf(arglist.get(0)) instanceof IAstSymbolExpr);
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
    			" iff = code { };\n"+
    			"testImplicitBlocks3 = code (t, x, y) {\n" +
    			"   iff(t, code ( => Int) { x = x + 9; x; }, code ( => Int) { y = y + 7;  y; });\n"+
    			"};");
    	sanityTest(mod);
    
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testImplicitBlocks3");
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainExpr(def);
    	IAstExprStmt ret = (IAstExprStmt) codeExpr.stmts().list().get(0);
    	IAstFuncCallExpr funcCall = (IAstFuncCallExpr) getValue(ret.getExpr());
    	List<IAstTypedExpr> arglist = funcCall.arguments().list();
		assertEquals(3, arglist.size());
    	assertTrue(baseOf(arglist.get(0)) instanceof IAstSymbolExpr);
    	assertTrue(arglist.get(1) instanceof IAstCodeExpr);
    	assertTrue(arglist.get(2) instanceof IAstCodeExpr);
    }

    @Test
    public void testNoMacroAlloc() throws Exception {
    	parseFail(
    			"mycode := macro(p:Int[10]; i) {\n"+
    			"   p[i];"+
    			"};\n"+
    			"");
    }

    
    @Test
    public void testMacroArgs1() throws Exception {
    	IAstModule mod = treeize(
    			" testMacroArgs1 = macro (macro t : code; macro mthen : code; macro melse : code) { };\n");
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
    	treeizeFail(" testMacroArgs1 = code (macro t : code; macro mthen : code; macro melse : code) { };\n");
    }
    
    @Test
    public void testCondStar1() throws Exception {
    	IAstModule mod = treeize(
    		" testCondStar1 = code (t) { if 1>t then 1\n" +
    		"		elif	t!=2 and t!=1 then { x:= 9+t; -x; }\n" +
    		"		else  0.4 \n" +
    		"		; };\n");
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
		assertTrue(condExpr.getExpr() instanceof IAstStmtListExpr);
		condExpr = condList.getCondExprs().list().get(2);
		assertTrue(condExpr.getExpr() instanceof IAstFloatLitExpr);
    }
    @Test
    public void testCondStar2() throws Exception {
    	IAstModule mod = treeize(
    		" testCondStar2 = code (t) { if 1>t then 1\n" +
    		"		elif t!=2 and t!=1 then { x:= 9+t; -x; }\n" +
    		"		else  0.4;" +
    		"11 \n" +
    		"		; };\n");
		sanityTest(mod);
		
		IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testCondStar2");
		IAstCodeExpr codeExpr = (IAstCodeExpr)getMainExpr(def);
		IAstExprStmt exprStmt = (IAstExprStmt) codeExpr.stmts().list().get(0);
		IAstCondList condList = (IAstCondList) exprStmt.getExpr();
		assertEquals(3, condList.getCondExprs().nodeCount());
		IAstCondExpr condExpr;
		condExpr = condList.getCondExprs().list().get(0);
		assertTrue(condExpr.getExpr() instanceof IAstIntLitExpr);
		condExpr = condList.getCondExprs().list().get(1);
		assertTrue(condExpr.getExpr() instanceof IAstStmtListExpr);
		condExpr = condList.getCondExprs().list().get(2);
		assertTrue(condExpr.getExpr() instanceof IAstFloatLitExpr);
    }
    @Test
    public void testCondStar3() throws Exception {
    	// 'fi' means 'else nil'
    	IAstModule mod = treeize(
    		" testCondStar3 = code (t) { \n" +
    		"if 1>t then 1 fi;\n" +
    		"		11;\n"+
    		"		; };\n");
		sanityTest(mod);
		
		IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testCondStar3");
		IAstCodeExpr codeExpr = (IAstCodeExpr)getMainExpr(def);
		IAstExprStmt exprStmt = (IAstExprStmt) codeExpr.stmts().list().get(0);
		IAstCondList condList = (IAstCondList) exprStmt.getExpr();
		assertEquals(2, condList.getCondExprs().nodeCount());
		IAstCondExpr condExpr;
		condExpr = condList.getCondExprs().list().get(0);
		assertTrue(condExpr.getExpr() instanceof IAstIntLitExpr);
		//condExpr = condList.getCondExprs().list().get(1);
		//assertTrue(condExpr.getExpr() instanceof IAstCodeExpr);
		condExpr = condList.getCondExprs().list().get(1);
		assertTrue(condExpr.getExpr() instanceof IAstNilLitExpr);
    }
    /*
    @Test
    public void testPointers1() throws Exception {
    	IAstModule mod = treeize(
        		" badSwap_testPointers1 = code (x : Int&; y : Int& => nil) {\n" +
        		"};\n");
    		sanityTest(mod);
    }
    */
    
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
    	IAstModule mod = treeize("swap = code (x,y => (Int, Int)) { (y,x); };\n" +
    			"testTuples4 = code (x,y) { a : Int; b : Int; (a, b) = swap(4, 5); }; \n");
    	sanityTest(mod);
    }
    @Test
    public void testTuples4b() throws Exception {
    	IAstModule mod = treeize(
    			"testTuples4 = code (x,y) { (a, b) := (4, 5); }; \n");
    	sanityTest(mod);
    }
    
    /*
    @Test
    public void testWith() throws Exception {
    	dumpTreeize = true;
    	IAstModule mod = treeize(
			"testWith = code (x,y) { with ix : x as Int => ix+y else with fx : x as Float => fx-y ; }; \n");
    	sanityTest(mod);
    }
    */
    
    @Test
    public void testBlockScopes() throws Exception {
    	treeize(
    			"testBlockScopes = code (t; x : Int; y : Float) {\n" +
    			"  if t then { z := Float(x); z = z * 8 } else { z := y; }; "+
    			"};");
    	
    	// which 'z' is used at end?  should be unknown
    	IAstModule mod = treeize(
    			"testBlockScopes = code (t; x : Int; y : Float) {\n" +
    			"  if t then { z := Float(x); z = z * 8 } else { z := y; }; z;"+
    			"};");
    	doExpand(mod, true);
    }
    
    @Test
    public void testCodeBlockMultiNamedArgs() throws Exception  {
    	IAstModule mod = treeize("testCodeBlockMultiNamedArgs = code( x , y : Int => Object ) { };");
    	sanityTest(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testCodeBlockMultiNamedArgs");
    	IAstCodeExpr code = (IAstCodeExpr) getMainExpr(def);
    	IAstPrototype proto = code.getPrototype();
    	assertEquals(2, proto.argumentTypes().length);
    	assertNotNull(proto.argumentTypes()[0].getTypeExpr());
    	assertNotNull(proto.argumentTypes()[1].getTypeExpr());
    	if (proto.argumentTypes()[0].getTypeExpr() == proto.argumentTypes()[1].getTypeExpr())
    		fail();
    }
    
    @Test
    public void testCodeBlockMultiNamedArgs2() throws Exception  {
    	IAstModule mod = treeize("testCodeBlockMultiNamedArgs2 = code( a : Float; x , y : Int => Object ) { };");
    	sanityTest(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testCodeBlockMultiNamedArgs2");
    	IAstCodeExpr code = (IAstCodeExpr) getMainExpr(def);
    	IAstPrototype proto = code.getPrototype();
    	assertEquals(3, proto.argumentTypes().length);
    	assertNotNull(proto.argumentTypes()[0].getTypeExpr());
    	assertNotNull(proto.argumentTypes()[1].getTypeExpr());
    	assertNotNull(proto.argumentTypes()[2].getTypeExpr());
    	if (proto.argumentTypes()[1].getTypeExpr() == proto.argumentTypes()[2].getTypeExpr())
    		fail();
    }
    
    @Test
    public void testCodeBlockMultiNamedVars1() throws Exception  {
    	IAstModule mod = treeize("testCodeBlockMultiNamedVars1 = code() { a, b := 4; };");
    	sanityTest(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testCodeBlockMultiNamedVars1");
    	IAstCodeExpr code = (IAstCodeExpr) getMainExpr(def);
    	IAstNode[] kids = code.stmts().getChildren();
    	assertEquals(1, kids.length);
    	assertTrue(kids[0] instanceof IAstAllocStmt);
    	IAstAllocStmt alloc = (IAstAllocStmt) kids[0];
    	assertEquals("a", alloc.getSymbolExprs().list().get(0).getSymbol().getName());
    	assertEquals("b", alloc.getSymbolExprs().list().get(1).getSymbol().getName());
    	assertEquals(1, alloc.getExprs().nodeCount());
    	assertEquals(4, (((IAstIntLitExpr) alloc.getExprs().getFirst()).getValue()));
    }
    @Test
    public void testCodeBlockMultiNamedVars1b() throws Exception  {
    	dumpTreeize = true;
    	treeizeFail("testCodeBlockMultiNamedVars1b = code() { a, b := +9, 8; };");
    }
    @Test
    public void testCodeBlockMultiNamedVars2() throws Exception  {
    	IAstModule mod = treeize("testCodeBlockMultiNamedVars2 = code() { a, b := 4, 9; };");
    	sanityTest(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testCodeBlockMultiNamedVars2");
    	IAstCodeExpr code = (IAstCodeExpr) getMainExpr(def);
    	IAstNode[] kids = code.stmts().getChildren();
    	assertEquals(1, kids.length);
    	assertTrue(kids[0] instanceof IAstAllocStmt);
    	
    	IAstAllocStmt alloc = (IAstAllocStmt) kids[0];
    	assertEquals("a", alloc.getSymbolExprs().list().get(0).getSymbol().getName());
    	assertEquals("b", alloc.getSymbolExprs().list().get(1).getSymbol().getName());
    	assertEquals(2, alloc.getExprs().nodeCount());
    	assertEquals(4, (((IAstIntLitExpr) alloc.getExprs().getFirst()).getValue()));
    	assertEquals(9, (((IAstIntLitExpr) alloc.getExprs().list().get(1)).getValue()));

    }


    @Test
	public void testGenericTypes2() throws Exception {
		dumpTreeize = true;
		IAstModule mod = treeize(
				"List = [T, U] data {\n" +
				"        node:T;\n"+
				"        next:List<U, T>^;\n" +
				"};\n" + 
				"\n" +
				"");
		IAstDefineStmt def = (IAstDefineStmt) mod.getScope().get("List").getDefinition();
		IAstDataType data = (IAstDataType) def.getMatchingBodyExpr(null);
		assertEquals(2, data.getFields().nodeCount());
		IAstAllocStmt alloc = (IAstAllocStmt) data.getFields().list().get(1);
		assertTrue(alloc.getTypeExpr() instanceof IAstPointerType);
		IAstPointerType ptr = (IAstPointerType) alloc.getTypeExpr();
		assertTrue(ptr.getBaseType() instanceof IAstInstanceExpr);
		IAstInstanceExpr instance = (IAstInstanceExpr) ptr.getBaseType();
		assertTrue(instance.getSymbolExpr().getSymbol().getName().equals("List"));
    }
    
    @Test 
    public void testAssignOps() throws Exception {
    	dumpTreeize = true;
    	IAstModule mod = treeize("testAssignOps = code { x:=1;" +
    			//"x+=x-=x*=x/=x+/=x%=x+%=x>>=x<<=x+>>=2;\n"+
    			"x+=(x-=(x*=x/=x+/=x\\=(x+\\=x>>=(x<<=x+>>=(x>>|=x<<|=x%=32)))));\n"+
    			"x|=x~=x&=111;\n"+
    			"};");
    	sanityTest(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testAssignOps");
    	IAstCodeExpr codeExpr = (IAstCodeExpr)getMainExpr(def);

    	IAstAssignStmt stmt;
		stmt = (IAstAssignStmt) codeExpr.stmts().list().get(1);
    	assertEquals(IOperation.ADD, stmt.getOperation());
    	assertTrue(stmt.getExprs().getFirst() instanceof IAstAssignStmt);
    	IAstAssignStmt stmt2 = (IAstAssignStmt) stmt.getExprs().getFirst();
		assertEquals(IOperation.SUB, stmt2.getOperation());
    	stmt = (IAstAssignStmt) codeExpr.stmts().list().get(2);
    	assertEquals(IOperation.BITOR, stmt.getOperation());
    	assertTrue(stmt.getExprs().getFirst() instanceof IAstAssignStmt);
    	stmt2 = (IAstAssignStmt) stmt.getExprs().getFirst();
    	assertEquals(IOperation.BITXOR, stmt2.getOperation());
    	assertEquals(IOperation.BITAND, ((IAstAssignStmt) stmt2.getExprs().getFirst()).getOperation());
    }


    @Test
    public void testInnerData1() throws Exception {
    	dumpTreeize = true;
    	IAstModule mod = treeize(
    			"Complex = data {\n"+
    			"  a,b,c:Byte;\n"+
    			"  Inner = data {\n"+
    			"    d1,d2:Float;\n"+
    			"    p : Complex^;\n"+
    			"  };\n"+
    			"  d : Inner;\n"+
    			" };\n"+
    			"testPtrCalc6 = code() {\n"+
    			"  c : Complex;\n" +
    			"  c.d.p.d.d2;\n"+
    			"};\n"+
    	"");
    	sanityTest(mod);

    	IAstDefineStmt def;
    	def = (IAstDefineStmt) mod.getScope().getNode("Inner");
    	assertNull(def);
		def = (IAstDefineStmt) mod.getScope().getNode("Complex");
		assertNotNull(def);
		
		IAstDataType complex = (IAstDataType) getMainBodyExpr(def);
		IAstDefineStmt inner = (IAstDefineStmt) complex.getScope().getNode("Inner");
		assertNotNull(inner);
		
    	
    	
    }
}


