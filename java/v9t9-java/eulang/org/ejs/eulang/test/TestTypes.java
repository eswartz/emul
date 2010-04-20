/**
 * 
 */
package org.ejs.eulang.test;

import static junit.framework.Assert.*;

import org.ejs.eulang.ast.IAstAllocStmt;
import org.ejs.eulang.ast.IAstCodeExpr;
import org.ejs.eulang.ast.IAstExprStmt;
import org.ejs.eulang.ast.IAstIndexExpr;
import org.ejs.eulang.ast.IAstModule;
import org.ejs.eulang.types.LLArrayType;
import org.ejs.eulang.types.LLCodeType;
import org.junit.Test;

/**
 * @author ejs
 *
 */
public class TestTypes extends BaseParserTest {

    @Test
    public void testArrayDecls() throws Exception {
    	IAstModule mod = doFrontend(
    			"p : Int[10];\n"+
    			"q : Int[];\n");

    	sanityTest(mod);
    	
    	IAstAllocStmt stmt = (IAstAllocStmt) mod.getScope().get("p").getDefinition();
    	assertTrue(stmt.getType() instanceof LLArrayType);
    	assertEquals(10, ((LLArrayType)stmt.getType()).getArrayCount()); 
    	assertEquals(typeEngine.INT, ((LLArrayType)stmt.getType()).getSubType()); 
    	assertTrue(stmt.getType().isCompatibleWith(stmt.getType()));
    	assertNull(((LLArrayType)stmt.getType()).getDynamicSizeExpr());
    	
    	stmt = (IAstAllocStmt) mod.getScope().get("q").getDefinition();
    	assertTrue(stmt.getType() instanceof LLArrayType);
    	assertEquals(0, ((LLArrayType)stmt.getType()).getArrayCount()); 
    	assertEquals(typeEngine.INT, ((LLArrayType)stmt.getType()).getSubType()); 
    	assertTrue(stmt.getType().isCompatibleWith(stmt.getType()));
    	assertNull(((LLArrayType)stmt.getType()).getDynamicSizeExpr());
    }
    @Test
    public void testArrayDecls2() throws Exception {
    	IAstModule mod = doFrontend(
    			"mycode := code(size) {\n"+
    			"	p : Int[size];\n"+	// returns whole array
    			"};\n"+
    			"");

    	sanityTest(mod);
    	
    	IAstAllocStmt astmt = (IAstAllocStmt) mod.getScope().get("mycode").getDefinition();
    	assertTrue(astmt.getType() instanceof LLCodeType);
    	IAstCodeExpr code = (IAstCodeExpr) astmt.getExprs().getFirst();
    	
    	IAstAllocStmt stmt = (IAstAllocStmt) code.stmts().getFirst();
    	LLArrayType arrayType = (LLArrayType)stmt.getType();
    	assertEquals(0, arrayType.getArrayCount());
    	assertNotNull(arrayType.getDynamicSizeExpr());
    	assertEquals(typeEngine.getIntType(16), arrayType.getDynamicSizeExpr().getType());
    	
    	doGenerate(mod);
    	
    	
    }
    
    @Test
    public void testArrayAccess0() throws Exception {
    	IAstModule mod = doFrontend(
    			"mycode := code() {\n"+
    			"   p:Int[10];\n"+		// locally allocated
    			"   p[5];"+
    			"};\n"+
    			"");

    	sanityTest(mod);
    	
    	IAstAllocStmt astmt = (IAstAllocStmt) mod.getScope().get("mycode").getDefinition();
    	assertTrue(astmt.getType() instanceof LLCodeType);
    	IAstCodeExpr code = (IAstCodeExpr) astmt.getExprs().getFirst();
    	
    	IAstExprStmt stmt = (IAstExprStmt) code.stmts().list().get(1);
    	IAstIndexExpr index = (IAstIndexExpr) stmt.getExpr();
    	assertEquals(typeEngine.INT, index.getType());
    	LLArrayType arrayType = (LLArrayType)index.getExpr().getType();
    	assertEquals(10, arrayType.getArrayCount());
    	assertNull(arrayType.getDynamicSizeExpr());
    	assertEquals(typeEngine.INT, index.getIndex().getType());
    	
    	doGenerate(mod);
    	
    	
    }
    
    @Test
    public void testArrayAccess1() throws Exception {
    	IAstModule mod = doFrontend(
    			"mycode := code(p:Int[10]) {\n"+		// passed by reference
    			"   p[5];"+
    			"};\n"+
    			"deref = code () {\n"+
    			" array:Int[10];\n"+
    			" mycode(array);\n"+		// passed by reference
    			"};\n"+
    			"");

    	sanityTest(mod);
    	
    	IAstAllocStmt astmt = (IAstAllocStmt) mod.getScope().get("mycode").getDefinition();
    	assertTrue(astmt.getType() instanceof LLCodeType);
    	IAstCodeExpr code = (IAstCodeExpr) astmt.getExprs().getFirst();
    	
    	IAstExprStmt stmt = (IAstExprStmt) code.stmts().getFirst();
    	IAstIndexExpr index = (IAstIndexExpr) stmt.getExpr();
    	assertEquals(typeEngine.INT, index.getType());
    	LLArrayType arrayType = (LLArrayType)index.getExpr().getType();
    	assertEquals(10, arrayType.getArrayCount());
    	assertNull(arrayType.getDynamicSizeExpr());
    	assertEquals(typeEngine.INT, index.getIndex().getType());
    	
    	doGenerate(mod);
    	
    	
    }
    @Test
    public void testArrayAccess1b() throws Exception {
    	IAstModule mod = doFrontend(
    			"mycode := code(p:Int[10]; i) {\n"+
    			"   p[i];"+
    			"};\n"+
    			"");

    	sanityTest(mod);
    	
    	IAstAllocStmt astmt = (IAstAllocStmt) mod.getScope().get("mycode").getDefinition();
    	assertTrue(astmt.getType() instanceof LLCodeType);
    	IAstCodeExpr code = (IAstCodeExpr) astmt.getExprs().getFirst();
    	
    	IAstExprStmt stmt = (IAstExprStmt) code.stmts().getFirst();
    	IAstIndexExpr index = (IAstIndexExpr) stmt.getExpr();
    	assertEquals(typeEngine.INT, index.getType());
    	LLArrayType arrayType = (LLArrayType)index.getExpr().getType();
    	assertEquals(10, arrayType.getArrayCount());
    	assertNull(arrayType.getDynamicSizeExpr());
    	assertEquals(typeEngine.INT, index.getIndex().getType());
    	
    	doGenerate(mod);
    	
    	
    }
    @Test
    public void testArrayAccess1c() throws Exception {
    	IAstModule mod = doFrontend(
    			"mycode := code(p:Int&[10]; i => nil) {\n"+
    			"   p[i];"+
    			"};\n"+
    			"");

    	sanityTest(mod);
    	
    	IAstAllocStmt astmt = (IAstAllocStmt) mod.getScope().get("mycode").getDefinition();
    	assertTrue(astmt.getType() instanceof LLCodeType);
    	IAstCodeExpr code = (IAstCodeExpr) astmt.getExprs().getFirst();
    	
    	IAstExprStmt stmt = (IAstExprStmt) code.stmts().getFirst();
    	IAstIndexExpr index = (IAstIndexExpr) stmt.getExpr();
    	assertEquals(typeEngine.getRefType(typeEngine.INT), index.getType());
    	LLArrayType arrayType = (LLArrayType)index.getExpr().getType();
    	assertEquals(10, arrayType.getArrayCount());
    	assertNull(arrayType.getDynamicSizeExpr());
    	assertEquals(typeEngine.INT, index.getIndex().getType());
    	
    	doGenerate(mod);
    	
    	
    }
    /*
    @Test
    public void testArrayAccess1d() throws Exception {
    	IAstModule mod = doFrontend(
    			"mycode := code(p:(Int[10])&; i => nil) {\n"+
    			"   p[i];"+
    			"};\n"+
    			"");

    	sanityTest(mod);
    	
    	IAstAllocStmt astmt = (IAstAllocStmt) mod.getScope().get("mycode").getDefinition();
    	assertTrue(astmt.getType() instanceof LLCodeType);
    	IAstCodeExpr code = (IAstCodeExpr) astmt.getExprs().getFirst();
    	
    	IAstExprStmt stmt = (IAstExprStmt) code.stmts().getFirst();
    	IAstIndexExpr index = (IAstIndexExpr) stmt.getExpr();
    	assertEquals(typeEngine.getRefType(typeEngine.INT), index.getType());
    	LLArrayType arrayType = (LLArrayType)index.getExpr().getType();
    	assertEquals(10, arrayType.getArrayCount());
    	assertNull(arrayType.getDynamicSizeExpr());
    	assertEquals(typeEngine.INT, index.getIndex().getType());
    	
    	doGenerate(mod);
    	
    	
    }*/
    
    @Test
    public void testStruct1() throws Exception {
    	IAstModule mod = doFrontend(
    			"Tuple = data {<\n"+
    			"   x:Int=66; y:Float; z; "+
    			">  e:code; f:Byte=9; };\n"+
    			"");

    	sanityTest(mod);
    }
}


