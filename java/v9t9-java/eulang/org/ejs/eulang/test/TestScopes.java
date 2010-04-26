/**
 * 
 */
package org.ejs.eulang.test;


import static junit.framework.Assert.*;

import org.ejs.eulang.ast.IAstAllocStmt;
import org.ejs.eulang.ast.IAstDefineStmt;
import org.ejs.eulang.ast.IAstModule;
import org.ejs.eulang.types.LLDataType;
import org.junit.Test;

/**
 * @author ejs
 *
 */
public class TestScopes extends BaseParserTest {

	@Test 
	public void testScopeAdd1() throws Exception {
		dumpTreeize = true;
		IAstModule mod = treeize(
				" Base = {\n" + 
				"        x:Int;\n" + 
				"};\n" + 
				"\n" + 
				" Added = Base + {\n" +
				"    y : Float;\n"+
				"};\n" + 
				"");
		sanityTest(mod);
		
	}
	@Test 
	public void testScopeDataAdd1() throws Exception {
		dumpTreeize = true;
		IAstModule mod = doFrontend(
				" Base = data {\n" + 
				"        x:Int;\n" + 
				"};\n" + 
				"\n" + 
				" Added = Base + data {\n" +
				"    y : Float;\n"+
				"};\n" +
				"foo : Added;\n"+
				"");
		sanityTest(mod);
		IAstAllocStmt alloc = (IAstAllocStmt) mod.getScope().get("foo").getDefinition();
		LLDataType data = (LLDataType) alloc.getType();
		assertEquals(2, data.getInstanceFields().length);
		
	}
	
}
