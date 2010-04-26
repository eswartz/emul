
package org.ejs.eulang.test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import org.ejs.eulang.ast.IAstBinExpr;
import org.ejs.eulang.ast.IAstCodeExpr;
import org.ejs.eulang.ast.IAstCondExpr;
import org.ejs.eulang.ast.IAstCondList;
import org.ejs.eulang.ast.IAstDataType;
import org.ejs.eulang.ast.IAstDefineStmt;
import org.ejs.eulang.ast.IAstDerefExpr;
import org.ejs.eulang.ast.IAstExprStmt;
import org.ejs.eulang.ast.IAstIntLitExpr;
import org.ejs.eulang.ast.IAstModule;
import org.ejs.eulang.ast.IAstStmtListExpr;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.types.LLType;
import org.junit.Test;

/**
 * Test that we can inject macro calls.
 * 
 */
public class TestGenerics extends BaseParserTest {
	{
		dumpExpand = true;
	}
	@Test
    public void testSimple1() throws Exception {
		dumpTreeize = true;
    	IAstModule mod = treeize(
    			"foo = [T,U] T+U;\n"+
    			"bar = foo<10,20>;\n"+
    			"");
    	sanityTest(mod);

    	mod = (IAstModule) doExpand(mod);
    	
    	sanityTest(mod);
    	
    	IAstTypedExpr body = getMainBodyExpr((IAstDefineStmt) mod.getScope().get("bar").getDefinition());
    	assertTrue(body instanceof IAstBinExpr);
    	IAstBinExpr bin = (IAstBinExpr) body;
    	assertTrue(bin.getLeft() instanceof IAstDerefExpr);
    	assertTrue(((IAstDerefExpr)bin.getLeft()).getExpr()  instanceof IAstIntLitExpr);
    	assertTrue(bin.getRight() instanceof IAstDerefExpr);
    	assertTrue(((IAstDerefExpr)bin.getRight()).getExpr()  instanceof IAstIntLitExpr);
    }
	@Test
    public void testSimple2() throws Exception {
		dumpTreeize = true;
    	IAstModule mod = treeize(
    			"foo = [T,U] data { node:T; next:U^; };\n"+
    			"bar = foo<Int, Float>;\n"+
    			"");
    	sanityTest(mod);

    	mod = (IAstModule) doExpand(mod);
    	
    	sanityTest(mod);
    	
    	IAstTypedExpr body = getMainBodyExpr((IAstDefineStmt) mod.getScope().get("bar").getDefinition());
    	assertTrue(body instanceof IAstDataType);
    	
    }

}


