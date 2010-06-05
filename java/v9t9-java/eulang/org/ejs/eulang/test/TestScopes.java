/**
 * 
 */
package org.ejs.eulang.test;


import static junit.framework.Assert.*;

import org.ejs.eulang.ast.IAstAllocStmt;
import org.ejs.eulang.ast.IAstModule;
import org.ejs.eulang.types.LLDataType;
import org.junit.Test;

/**
 * @author ejs
 *
 */
public class TestScopes extends BaseTest {

	@Test 
	public void testScopeAdd1() throws Exception {
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
	@Test 
	public void testScopeExtend1() throws Exception {
		IAstModule mod = doFrontend(
				" Base = data {\n" + 
				"        x:Int;\n" + 
				"        func=code(this:Base^) { this.x; };\n"+
				"};\n" + 
				"\n" + 
				" Base += {\n" +
				"    LIT = 3;\n"+
				"    util=code(this:Base^) { this.LIT; };\n"+
				"};\n" +
				"foo : Base;\n"+
				"main = code() { foo.LIT; };\n"+
				"");
		sanityTest(mod);
		
	}
	@Test 
	public void testScopeExtend2() throws Exception {
		// nope, don't allow adding fields
		treeizeFail(
				" Base = data {\n" + 
				"        x:Int;\n" + 
				"        func=code(this:Base^) { this.x; };\n"+
				"};\n" + 
				"\n" + 
				" Base += {\n" +
				"    newData : Int;\n"+
				"};\n" +
				"foo : Base;\n"+
				"main = code() { foo.LIT; };\n"+
				"");
		
	}
}
