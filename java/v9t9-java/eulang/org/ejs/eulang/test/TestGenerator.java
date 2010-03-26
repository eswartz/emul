/**
 * 
 */
package org.ejs.eulang.test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.util.Collections;
import java.util.List;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.ParserRuleReturnScope;
import org.antlr.runtime.tree.Tree;
import org.ejs.eulang.ast.DumpAST;
import org.ejs.eulang.ast.GenerateAST;
import org.ejs.eulang.ast.IAstCodeExpression;
import org.ejs.eulang.ast.IAstDefine;
import org.ejs.eulang.ast.IAstIntLitExpr;
import org.ejs.eulang.ast.IAstModule;
import org.ejs.eulang.ast.IAstPrototype;
import org.ejs.eulang.ast.IAstScope;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.TypeEngine;
import org.ejs.eulang.ast.GenerateAST.Error;
import org.junit.Test;

import v9t9.tools.ast.expr.IAstNode;
/**
 * @author ejs
 *
 */
public class TestGenerator extends BaseParserTest {

    private TypeEngine typeEngine;

	protected IAstNode treeize(String method, String pmethod, String str, boolean expectError) throws Exception {
    	ParserRuleReturnScope ret = parse(method, str, expectError);
    	if (ret == null)
    		return null;
    	
    	Tree tree = (Tree) ret.getTree();
    	if (tree == null)
    		return null;
    	
    	GenerateAST gen = new GenerateAST("<string>", Collections.<CharStream, String>emptyMap());
    	
    	typeEngine = gen.getTypeEngine();
    	IAstNode node = null;
    	
    	 if(method == null)
        	node = gen.constructModule(tree);
        else {
    		try {
				node = (IAstNode) gen.getClass().getMethod(method).invoke(tree);
			} catch (Exception e) {
				throw e;
			}
        }
	 
    	 DumpAST dump = new DumpAST(System.out);
     	node.accept(dump);
     	
    	 if (!expectError) {
    		 if (gen.getErrors().size() > 0) {
    			 String msgs = catenate(gen.getErrors());
    			 fail(msgs);
    		 }
    	 } else {
    		 if (gen.getErrors().isEmpty()) {
    			 fail("no errors generated");
    		 }
    	 }
    		 
    	return node;
    }
 
    /**
	 * @param errors
	 * @return
	 */
	private String catenate(List<Error> errors) {
		StringBuilder sb = new StringBuilder();
		for (Error e : errors) {
			sb.append(e.toString());
			sb.append('\n');
		}
		return sb.toString();
	}

	protected IAstModule treeize(String str) throws Exception {
    	return (IAstModule) treeize(null, null, str, false);
    }
    
    
    /**
     * @param mod
     */
    private void sanityTest(IAstNode node) {
    	assertNotNull(node);
    	assertNotNull(node.getChildren());
    	for (IAstNode kid : node.getChildren()) {
    		assertSame(node, kid.getParent());
    		assertEquals(node, kid.getParent());
    		
    		if (node instanceof IAstScope && kid instanceof IAstScope) {
    			assertEquals(((IAstScope)node).getScope(), ((IAstScope) kid).getScope().getOwner());
    		}
    	}
    }
    
    @Test
    public void testEmptyModule() throws Exception {
    	IAstModule mod = treeize("");
    	sanityTest(mod);
    	
    	assertTrue(mod.getScope().getNames().length == 0);
    }
    
    @Test
    public void testOneEntryConstModule() throws Exception {
    	IAstModule mod = treeize("foo = 3;");
    	sanityTest(mod);
    	
    	assertEquals(1, mod.getScope().getNames().length);
    	assertEquals(1, mod.getChildren().length);
    	
    	IAstDefine def = (IAstDefine) mod.getChildren()[0];
    	assertEquals("foo", def.getName().getName());
    	assertTrue(def.getExpression() instanceof IAstIntLitExpr);
    	assertEquals("3", ((IAstIntLitExpr)def.getExpression()).getLiteral());
    	assertEquals((long) 3, ((IAstIntLitExpr)def.getExpression()).getValue());
    	assertTrue(def.getExpression() instanceof IAstTypedExpr);
    	assertTrue(((IAstTypedExpr)def.getExpression()).getType().equals(typeEngine.INT));
    	
    	assertTrue(def instanceof IAstTypedExpr);
    	assertTrue(((IAstTypedExpr)def.getExpression()).getType().equals(typeEngine.INT));
    }
    
    @Test
    public void testOneEntryCodeModule0() throws Exception {
    	IAstModule mod = treeize("foo = code (x,y) { };");
    	sanityTest(mod);
    	
    	assertEquals(1, mod.getScope().getNames().length);
    	assertEquals(1, mod.getChildren().length);
    	
    	IAstDefine def = (IAstDefine) mod.getChildren()[0];
    	assertEquals("foo", def.getName().getName());
    	assertTrue(def.getExpression() instanceof IAstCodeExpression);
    	IAstCodeExpression codeExpression = (IAstCodeExpression)def.getExpression();
    	assertEquals(mod.getScope(), codeExpression.getScope().getParent());
    	
		IAstPrototype prototype = codeExpression.getPrototype();
		assertEquals(2, prototype.argumentTypes().length);
		assertEquals("x", prototype.argumentTypes()[0].getName().getName());
		assertNull(prototype.argumentTypes()[0].getType());
		assertEquals("y", prototype.argumentTypes()[1].getName().getName());
		assertNull(prototype.argumentTypes()[1].getType());
		
		assertEquals(prototype.argumentTypes()[0].getName().getScope(), codeExpression.getScope());
		assertEquals(prototype.argumentTypes()[1].getName().getScope(), codeExpression.getScope());
		
		assertNotNull(codeExpression.getStmts());
		assertTrue(codeExpression.getStmts().list().isEmpty());
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
    public void testOpPrec1b() throws Exception {
    	IAstModule mod = treeize("opPrec1 = code { x:=1*2/3%4%%4.5+5-6>>7<<8>>>8.5&9^10|11<12>13<=14>=15==16!=17&&18||19; };");
    	sanityTest(mod);
    }
}


