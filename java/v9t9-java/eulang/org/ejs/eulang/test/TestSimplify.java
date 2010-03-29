/**
 * Test that we can simplify the AST
 * 
 */
package org.ejs.eulang.test;

import static junit.framework.Assert.*;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.ejs.eulang.ast.DumpAST;
import org.ejs.eulang.ast.IAstAllocStmt;
import org.ejs.eulang.ast.IAstAssignStmt;
import org.ejs.eulang.ast.IAstBinExpr;
import org.ejs.eulang.ast.IAstCodeExpr;
import org.ejs.eulang.ast.IAstDefineStmt;
import org.ejs.eulang.ast.IAstIntLitExpr;
import org.ejs.eulang.ast.IAstModule;
import org.ejs.eulang.ast.IAstPrototype;
import org.ejs.eulang.ast.IAstReturnStmt;
import org.ejs.eulang.ast.IAstUnaryExpr;
import org.ejs.eulang.ast.IOperation;
import org.ejs.eulang.ast.Message;
import org.ejs.eulang.optimize.SimplifyTree;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeInference;
import org.junit.Test;

/**
 * @author ejs
 *
 */
public class TestSimplify extends BaseParserTest {
	protected void doSimplify(IAstModule mod) {
		SimplifyTree simplify = new SimplifyTree(typeEngine);
		
		// must infer types first
		doTypeInfer(mod);
		
		int depth = mod.getDepth();
		
		int passes = 0;
		while (passes++ <= depth) {
			boolean[] changed = { false }; 
			
			simplify.simplify(changed, mod);
			
			if (!changed[0]) 
				break;
			
			System.err.flush();
			System.out.println("After simplification:");
			DumpAST dump = new DumpAST(System.out);
			mod.accept(dump);
			
		}
		System.out.println("Simplification: " + passes + " passes");
	}

	@Test
    public void testPromotedCast2() throws Exception {
    	IAstModule mod = treeize(
    			"testPromotedCast2 = code () {\n" +
    			"   z : Byte;\n" +
    			"	z = z * Byte(100) / 5;\n" +
    			"};");
    	sanityTest(mod);

    	doSimplify(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testPromotedCast2");
    	typeTest(mod, false);
    	
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
    	
    	IAstAssignStmt allocStmt = (IAstAssignStmt) codeExpr.stmts().list().get(0);
		assertEquals(typeEngine.BYTE, allocStmt.getType());
		IAstUnaryExpr castExpr = (IAstUnaryExpr) allocStmt.getExpr();
		assertEquals(typeEngine.INT, castExpr.getExpr().getType());
		IAstBinExpr divExpr = (IAstBinExpr) castExpr.getExpr();
		assertEquals(typeEngine.INT, divExpr.getLeft().getType());
		assertEquals(typeEngine.INT, divExpr.getRight().getType());
		assertTrue(divExpr.getLeft() instanceof IAstUnaryExpr && ((IAstUnaryExpr) divExpr.getLeft()).getOp() == IOperation.CAST);
		IAstBinExpr mulExpr = (IAstBinExpr) ((IAstUnaryExpr) divExpr.getLeft()).getExpr();
		assertEquals(typeEngine.BYTE, mulExpr.getType());
		assertEquals(typeEngine.BYTE, mulExpr.getLeft().getType());
		assertEquals(typeEngine.BYTE, mulExpr.getRight().getType());
		assertEquals(100, ((IAstIntLitExpr) mulExpr.getRight()).getValue());
    }
	
	@Test
    public void testPromotedCond1() throws Exception {
    	IAstModule mod = treeize(
    			"testPromotedCond1 = code () {\n" +
    			"   z : Byte;\n" +
    			"	z = true;\n" +
    			"};");
    	sanityTest(mod);

    	doSimplify(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testPromotedCond1");
    	typeTest(mod, false);
    	
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
    	
    	IAstAssignStmt allocStmt = (IAstAssignStmt) codeExpr.stmts().list().get(0);
		assertEquals(typeEngine.BYTE, allocStmt.getType());

		IAstIntLitExpr litExpr = (IAstIntLitExpr) allocStmt.getExpr();
		assertEquals(1, litExpr.getValue());
		
    }
	
	@Test
    public void testPromotedCond2() throws Exception {
    	IAstModule mod = treeize(
    			"testPromotedCond2 = code () {\n" +
    			"   z : Byte;\n" +
    			"	z = z > Byte(100);\n" +
    			"};");
    	sanityTest(mod);

    	doSimplify(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testPromotedCond2");
    	typeTest(mod, false);
    	
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
    	
    	IAstAssignStmt allocStmt = (IAstAssignStmt) codeExpr.stmts().list().get(0);
		assertEquals(typeEngine.BYTE, allocStmt.getType());
		IAstUnaryExpr castExpr = (IAstUnaryExpr) allocStmt.getExpr();
		assertTrue(castExpr.getOp() == IOperation.CAST);
		IAstBinExpr cmpExpr = (IAstBinExpr)  castExpr.getExpr();
		assertEquals(typeEngine.BOOL, cmpExpr.getType());
		assertEquals(typeEngine.BYTE, cmpExpr.getLeft().getType());
		assertEquals(typeEngine.BYTE, cmpExpr.getRight().getType());
		
		assertEquals(100, ((IAstIntLitExpr) cmpExpr.getRight()).getValue());
		
    }
	
	@Test
    public void testUnaryNot() throws Exception {
    	IAstModule mod = treeize(
    			"testUnaryNot = code () {\n" +
    			"   z : Byte;\n" +
    			"	return !-~z;\n" +
    			"};");
    	sanityTest(mod);

    	doSimplify(mod);
    	
    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testUnaryNot");
    	typeTest(mod, false);
    	
    	IAstCodeExpr codeExpr = (IAstCodeExpr)def.getExpr();
    	
    	IAstReturnStmt allocStmt = (IAstReturnStmt) codeExpr.stmts().list().get(0);
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


