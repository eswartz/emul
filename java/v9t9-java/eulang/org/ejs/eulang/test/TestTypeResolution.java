/**
 * 
 */
package org.ejs.eulang.test;

import static junit.framework.Assert.*;

import org.ejs.eulang.ITyped;
import org.ejs.eulang.ast.IAstDefineStmt;
import org.ejs.eulang.ast.IAstModule;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstScope;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.IAstTypedNode;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLType;
import org.junit.Test;

/**
 * @author ejs
 *
 */
public class TestTypeResolution extends BaseParserTest {

	protected IAstModule doResolve(String text, boolean expectErrors) throws Exception {
    	IAstModule mod = treeize(text);
    	
    	sanityTest(mod);
    	
    	validateSymbolTypes(mod);
    	
    	return mod;
	}
	/**
	 * @param mod
	 */
	private void validateSymbolTypes(IAstNode node) {
		if (node instanceof IAstScope) {
			IScope scope = ((IAstScope) node).getScope();
			for (ISymbol symbol : scope) {
				checkType(symbol);
				
				IAstNode def = symbol.getDefinition();
				assertNotNull(symbol+"", def);
				
				if (def instanceof IAstTypedNode) {
					checkType((IAstTypedNode) def);
				}
				else if (def instanceof IAstDefineStmt) {
					IAstDefineStmt define = (IAstDefineStmt) def;
					for (IAstTypedNode body : define.bodyList()) {
						checkType(body);
					}
					for (IAstTypedNode body : define.getConcreteInstances()) {
						checkType(body);
					}
				}
			}
		}
		for (IAstNode kid : node.getChildren()) {
			validateSymbolTypes(kid);
		}
	}
	/**
	 * @param typed
	 */
	private void checkType(ITyped typed) {
		LLType type = typed.getType();
		assertTrue(typed+ " : "+ type, type != null && type.isComplete());
		
	}
	protected IAstModule doResolve(String mod) throws Exception {
		return doResolve(mod, false);
	}
	
	@Test
	public void testEmpty() throws Exception {
		doResolve("");
	}
	
	@Test
	public void testGlobalAlloc1() throws Exception {
		IAstModule mod = doResolve("x : Int = 10;\n");
		assertEquals(1, mod.getScope().getSymbols().length);
	}
	@Test
	public void testGlobalDefine1() throws Exception {
		IAstModule mod = doResolve("x = 10;\n");
		assertEquals(1, mod.getScope().getSymbols().length);
	}
}
