/**
 * 
 */
package org.ejs.eulang.test;

import static junit.framework.Assert.*;

import java.util.List;

import org.ejs.eulang.ast.IAstAllocStmt;
import org.ejs.eulang.ast.IAstAssignStmt;
import org.ejs.eulang.ast.IAstBlockStmt;
import org.ejs.eulang.ast.IAstCodeExpr;
import org.ejs.eulang.ast.IAstCondExpr;
import org.ejs.eulang.ast.IAstCondList;
import org.ejs.eulang.ast.IAstDefineStmt;
import org.ejs.eulang.ast.IAstDerefExpr;
import org.ejs.eulang.ast.IAstExprStmt;
import org.ejs.eulang.ast.IAstFloatLitExpr;
import org.ejs.eulang.ast.IAstFuncCallExpr;
import org.ejs.eulang.ast.IAstGotoStmt;
import org.ejs.eulang.ast.IAstIntLitExpr;
import org.ejs.eulang.ast.IAstLabelStmt;
import org.ejs.eulang.ast.IAstModule;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstNodeList;
import org.ejs.eulang.ast.IAstNilLitExpr;
import org.ejs.eulang.ast.IAstPrototype;
import org.ejs.eulang.ast.IAstStmtListExpr;
import org.ejs.eulang.ast.IAstSymbolExpr;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.junit.Test;

/**
 * @author ejs
 *
 */
public class TestScopes extends BaseParserTest {

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
		
	}
	@Test 
	public void testScopeDataAdd1() throws Exception {
		IAstModule mod = treeize(
				" Base = data {\n" + 
				"        x:Int;\n" + 
				"};\n" + 
				"\n" + 
				" Added = Base + data {\n" +
				"    y : Float;\n"+
				"};\n" + 
				"");
		
	}
}
