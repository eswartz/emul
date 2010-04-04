/**
 * Test that we can simplify the AST
 * 
 */
package org.ejs.eulang.test;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.ejs.eulang.ast.IAstAllocStmt;
import org.ejs.eulang.ast.IAstAssignStmt;
import org.ejs.eulang.ast.IAstBinExpr;
import org.ejs.eulang.ast.IAstCodeExpr;
import org.ejs.eulang.ast.IAstExprStmt;
import org.ejs.eulang.ast.IAstIntLitExpr;
import org.ejs.eulang.ast.IAstModule;
import org.ejs.eulang.ast.IAstUnaryExpr;
import org.ejs.eulang.ast.IOperation;
import org.junit.Test;

/**
 * @author ejs
 *
 */
public class TestSimplify extends BaseParserTest {
	{
		dumpSimplify = true;
	}
	@Test
    public void testPromotedCast2() throws Exception {
    	IAstModule mod = treeize(
    			"testPromotedCast2 := code () {\n" +
    			"   z : Byte;\n" +
    			"	z = z * Byte(100) / 5;\n" +
    			"};");
    	sanityTest(mod);

    	doSimplify(mod);
    	
    	IAstAllocStmt def = (IAstAllocStmt) mod.getScope().getNode("testPromotedCast2");
    	typeTest(mod, false);
    	
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
    	
    	IAstAssignStmt allocStmt = (IAstAssignStmt) codeExpr.stmts().list().get(1);
		assertEquals(typeEngine.BYTE, allocStmt.getType());
		IAstUnaryExpr castExpr = (IAstUnaryExpr) allocStmt.getExpr();
		assertEquals(typeEngine.INT, castExpr.getExpr().getType());
		IAstBinExpr divExpr = (IAstBinExpr) castExpr.getExpr();
		assertEquals(typeEngine.INT, divExpr.getLeft().getType());
		assertEquals(typeEngine.INT, divExpr.getRight().getType());
		assertTrue(isCastTo(divExpr.getLeft(), typeEngine.INT));
		IAstBinExpr mulExpr = (IAstBinExpr) ((IAstUnaryExpr) divExpr.getLeft()).getExpr();
		assertEquals(typeEngine.BYTE, mulExpr.getType());
		assertEquals(typeEngine.BYTE, mulExpr.getLeft().getType());
		assertEquals(typeEngine.BYTE, mulExpr.getRight().getType());
		assertEquals(100, ((IAstIntLitExpr) mulExpr.getRight()).getValue());
    }
	
	@Test
    public void testPromotedCond1() throws Exception {
    	IAstModule mod = treeize(
    			"testPromotedCond1 := code () {\n" +
    			"   z : Byte;\n" +
    			"	z = true;\n" +
    			"};");
    	sanityTest(mod);

    	doSimplify(mod);
    	
    	IAstAllocStmt def = (IAstAllocStmt) mod.getScope().getNode("testPromotedCond1");
    	typeTest(mod, false);
    	
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
    	
    	IAstAssignStmt allocStmt = (IAstAssignStmt) codeExpr.stmts().list().get(1);
		assertEquals(typeEngine.BYTE, allocStmt.getType());

		IAstIntLitExpr litExpr = (IAstIntLitExpr) allocStmt.getExpr();
		assertEquals(1, litExpr.getValue());
		
    }
	
	@Test
    public void testPromotedCond2() throws Exception {
    	IAstModule mod = treeize(
    			"testPromotedCond2 := code () {\n" +
    			"   z : Byte;\n" +
    			"	z = z > Byte(100);\n" +
    			"};");
    	sanityTest(mod);

    	doSimplify(mod);
    	
    	IAstAllocStmt def = (IAstAllocStmt) mod.getScope().getNode("testPromotedCond2");
    	typeTest(mod, false);
    	
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
    	
    	IAstAssignStmt allocStmt = (IAstAssignStmt) codeExpr.stmts().list().get(1);
		assertEquals(typeEngine.BYTE, allocStmt.getType());
		IAstUnaryExpr castExpr = (IAstUnaryExpr) allocStmt.getExpr();
		assertTrue(isCastTo(allocStmt.getExpr(), typeEngine.BYTE));
		IAstBinExpr cmpExpr = (IAstBinExpr)  castExpr.getExpr();
		assertEquals(typeEngine.BOOL, cmpExpr.getType());
		assertEquals(typeEngine.BYTE, cmpExpr.getLeft().getType());
		assertEquals(typeEngine.BYTE, cmpExpr.getRight().getType());
		
		assertEquals(100, ((IAstIntLitExpr) cmpExpr.getRight()).getValue());
		
    }
	
	@Test
    public void testUnaryNot() throws Exception {
    	IAstModule mod = treeize(
    			"testUnaryNot := code () {\n" +
    			"   z : Byte;\n" +
    			"	 !-~z;\n" +
    			"};");
    	sanityTest(mod);

    	doSimplify(mod);
    	
    	IAstAllocStmt def = (IAstAllocStmt) mod.getScope().getNode("testUnaryNot");
    	typeTest(mod, false);
    	
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
    	
    	//IAstReturnStmt allocStmt = (IAstReturnStmt) codeExpr.stmts().list().get(1);
    	IAstExprStmt allocStmt = (IAstExprStmt) codeExpr.stmts().list().get(1);
		assertEquals(typeEngine.BOOL, allocStmt.getType());
		IAstBinExpr cmpExpr = (IAstBinExpr) allocStmt.getExpr();
		assertTrue(cmpExpr.getOp() == IOperation.COMPNE);
		assertEquals(typeEngine.BOOL, cmpExpr.getType());
		assertEquals(typeEngine.BYTE, cmpExpr.getLeft().getType());
		assertEquals(typeEngine.BYTE, cmpExpr.getRight().getType());
		
		// there will be removed cast here
		assertEquals(0, ((IAstIntLitExpr) cmpExpr.getRight()).getValue());

		IAstUnaryExpr negExpr = (IAstUnaryExpr) cmpExpr.getLeft();
		assertTrue(negExpr.getOp() == IOperation.NEG);
		assertEquals(typeEngine.BYTE, negExpr.getExpr().getType());
		IAstUnaryExpr invExpr = (IAstUnaryExpr) negExpr.getExpr();
		assertTrue(invExpr.getOp() == IOperation.INV);
		assertEquals(typeEngine.BYTE, invExpr.getExpr().getType());
		
    }
	
}


