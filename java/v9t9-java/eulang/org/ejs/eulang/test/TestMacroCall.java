
package org.ejs.eulang.test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import org.ejs.eulang.ast.IAstCodeExpr;
import org.ejs.eulang.ast.IAstCondExpr;
import org.ejs.eulang.ast.IAstCondList;
import org.ejs.eulang.ast.IAstDefineStmt;
import org.ejs.eulang.ast.IAstExprStmt;
import org.ejs.eulang.ast.IAstModule;
import org.ejs.eulang.ast.IAstStmtListExpr;
import org.ejs.eulang.types.LLType;
import org.junit.Test;

/**
 * Test that we can inject macro calls.
 * 
 */
public class TestMacroCall extends BaseTest {
	@Test
    public void testSimple1() throws Exception {
    	IAstModule mod = treeize(
    			"\n" + 
"iff = code #macro ( macro test : code( => Bool ); macro mthen : code; macro melse : code = code() {} ) {\n" + 
    			"    if  test() then mthen() else melse() ;\n" + 
    			"};\n"+
    			"testSimple1 = code (t, x, y) {\n" +
    			"   iff(t > 10, x + 1, x*t*y);\n"+
    			"};");
    	sanityTest(mod);

    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testSimple1");
    	IAstDefineStmt defPrime = (IAstDefineStmt) doExpand(def);
    	sanityTest(defPrime);
    	
    }
	
	
	@Test
	public void testForCount() throws Exception {
		IAstModule mod = treeize(
				"\n" + 
				"  forCountUntil = code #macro (macro idx; count : Int; macro test; macro body = code { idx }; macro fail = code { -1 }) {\n" + 
				"        idx := 0;\n" + 
				"        @loop: if \n" + 
				"            idx < count then if \n" + 
				"                test then { count = idx; body(); }\n" + 
				"                else { idx = idx + 1; goto loop; }\n" + 
				"            \n" + 
				"            else fail() \n" + 
				"        \n" + 
				"    };\n" + 
				"    \n" + 
				"testForCount = code () { forCountUntil(i, 10, i % 5 == 0, i+10, -123);"+
		"};");
		sanityTest(mod);
		
		IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testForCount");
		IAstDefineStmt defPrime = (IAstDefineStmt) doExpand(def);
		sanityTest(defPrime);
		
		doTypeInfer(mod);
		doGenerate(mod);
		
	}
	
	@Test
	public void testForCountDefaults() throws Exception {
		IAstModule mod = treeize(
				"\n" + 
				"  forCountUntil = code #macro (macro idx; count : Int; macro test; macro body = code { idx }; macro fail = code { -1 }) {\n" + 
				"        idx := 0;\n" + 
				"        @loop: if \n" + 
				"            idx < count then if \n" + 
				"                test then { count = idx; body(); }\n" + 
				"                else { idx = idx + 1; goto loop; }\n" + 
				"            \n" + 
				"            else fail() \n" + 
				"        \n" + 
				"    };\n" + 
				"    \n" + 
				"testForCountDefaults = code () { forCountUntil(i, 10, i % 5 == 0);"+
		"};");
		sanityTest(mod);
		
		IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testForCountDefaults");
		IAstDefineStmt defPrime = (IAstDefineStmt) doExpand(def);
		sanityTest(defPrime);
		
		doTypeInfer(mod);
		doGenerate(mod);
		
	}
	@Test
	public void testForCountBad1() throws Exception {
		IAstModule mod = treeize(
				"\n" + 
				"  forCountUntil = code #macro (macro idx; count : Int; macro test; macro body = code { idx }; macro fail = code { -1 }) {\n" + 
				"        idx := 0;\n" + 
				"        @loop: if \n" + 
				"            idx < count then if \n" + 
				"                test then { count = idx; body; }\n" + 
				"                else { idx = idx + 1; goto loop; }\n" + 
				"            \n" + 
				"            else fail \n" + 
				"        \n" + 
				"    };\n" + 
				"    \n" + 
				"testForCountBad1 = code () { forCountUntil(i % 5 == 0, 10, i, -1);"+
		"};");
		sanityTest(mod);
		
		//bad because we substitute an expression for an identifier
		IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testForCountBad1");
		doExpand(def, true);
	}
	
	@Test
	public void testForCountBad2() throws Exception {
		IAstModule mod = treeize(
				"\n" + 
				"  forCountUntil = code #macro (macro idx; count : Int; macro test; macro body = code { idx }; macro fail = code { -1 }) {\n" + 
				"        idx := 0;\n" + 
				"        @loop: if \n" + 
				"            idx < count then if \n" + 
				"                test then { count = idx; body; }\n" + 
				"                else { idx = idx + 1; goto loop; }\n" + 
				"            \n" + 
				"            else fail \n" + 
				"        \n" + 
				"    };\n" + 
				"    \n" + 
				"testForCountBad2 = code () { forCountUntil(i % 5 == 0, 10, i, 0, -1);"+
		"};");
		sanityTest(mod);
		
		IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testForCountBad2");
		doExpand(def, true);
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
    			"iff = code #macro ( test:Bool; macro mthen: code; macro melse: code) { " +
    			"	 if test then mthen() elif false then false else melse() };\n"+
    			"testImplicitBlocks3 = code (t; x : Int; y : Float) {\n" +
    			"   iff(t, { x = x + 9; x; }, { y = y + 7; y; })\n"+
    			"};");
    	sanityTest(mod);
    	
    	IAstModule expMod = (IAstModule) doExpand(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) expMod.getScope().getNode("testImplicitBlocks3");
    	doTypeInfer(mod);
    	
    	// float because it's the common type
    	assertEquals(typeEngine.getCodeType(typeEngine.FLOAT,  new LLType[] {typeEngine.BOOL, typeEngine.INT, typeEngine.FLOAT}), 
    			getMainBodyExpr(def).getType());
    	
    	IAstCodeExpr defExpr = (IAstCodeExpr) getMainBodyExpr(def);
    	IAstExprStmt stmt1 = (IAstExprStmt) ((IAstStmtListExpr) getValue(((IAstExprStmt) defExpr.stmts().getFirst()).getExpr())).getStmtList().list().get(1);
    	IAstCondList condList = (IAstCondList) stmt1.getExpr();
    	IAstCondExpr condExpr = condList.getCondExprs().list().get(0);
		assertEquals(typeEngine.FLOAT, condExpr.getType());
		assertTrue(isCastTo(condExpr.getExpr(), typeEngine.FLOAT));
		condExpr = condList.getCondExprs().list().get(1);
    	assertEquals(typeEngine.FLOAT, condExpr.getType());
    	assertTrue(isCastTo(condExpr.getExpr(), typeEngine.FLOAT));
    	condExpr = condList.getCondExprs().list().get(2);
    	assertEquals(typeEngine.FLOAT, condExpr.getType());
    	assertFalse(isCastTo(condExpr.getExpr(), typeEngine.FLOAT));
    }

    @Test
    public void testImplicitBlocks4() throws Exception {
    	IAstModule mod = treeize(
    			"wwhile = code #macro ( macro test:code; macro body : code) {\n"+
    			"    @loop: if test() then body() else goto loop;\n"+
    			"};\n"+
    			"testImplicitBlocks4 = code (t; x : Int; y : Float) {\n" +
    			"   wwhile(x > t, { y = y/2; x = x-1; } )\n"+
    			"};");
    	sanityTest(mod);
    	
    	IAstModule expMod = (IAstModule) doExpand(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) expMod.getScope().getNode("testImplicitBlocks4");
    	doTypeInfer(mod);
    	
    	assertEquals(typeEngine.getCodeType(typeEngine.INT,  new LLType[] {typeEngine.INT, typeEngine.INT, typeEngine.FLOAT}), 
    			getMainBodyExpr(def).getType());
    	
    	IAstCodeExpr defExpr = (IAstCodeExpr) getMainBodyExpr(def);
    	IAstExprStmt stmt1 = (IAstExprStmt) ((IAstStmtListExpr) getValue(((IAstExprStmt) defExpr.stmts().getFirst()).getExpr())).getStmtList().list().get(1);
    	IAstCondList condList = (IAstCondList) stmt1.getExpr();
    	IAstCondExpr condExpr = condList.getCondExprs().list().get(0);
		assertEquals(typeEngine.INT, condExpr.getType());
		condExpr = condList.getCondExprs().list().get(1);
    	assertEquals(typeEngine.INT, condExpr.getType());
    }
}


