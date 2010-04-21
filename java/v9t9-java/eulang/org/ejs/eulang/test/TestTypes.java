/**
 * 
 */
package org.ejs.eulang.test;

import static junit.framework.Assert.*;

import org.ejs.eulang.ast.IAstAllocStmt;
import org.ejs.eulang.ast.IAstCodeExpr;
import org.ejs.eulang.ast.IAstDataDecl;
import org.ejs.eulang.ast.IAstDefineStmt;
import org.ejs.eulang.ast.IAstExprStmt;
import org.ejs.eulang.ast.IAstIndexExpr;
import org.ejs.eulang.ast.IAstModule;
import org.ejs.eulang.types.LLArrayType;
import org.ejs.eulang.types.LLCodeType;
import org.ejs.eulang.types.LLDataType;
import org.ejs.eulang.types.LLInstanceField;
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
    public void testStruct0() throws Exception {
    	IAstModule mod = doFrontend(
    			"Tuple = data {\n"+
    			"   x:Int=66; y:Float;\n" +
    			" static f,g,h:Byte=9; };\n"+
    			"");

    	sanityTest(mod);
    	IAstDataDecl decl = (IAstDataDecl) getMainExpr((IAstDefineStmt) mod.getScope().get("Tuple").getDefinition());
    	assertTrue(decl.getType() instanceof LLDataType);
    	LLDataType data = (LLDataType) decl.getType();
    	assertTrue(data.isComplete());
    	assertEquals(5, data.getTypes().length);
    	assertEquals(2, data.getInstanceFields().length);
    	assertEquals(3, data.getStaticFields().length);
    	
    	assertEquals((2 + 4) * 8, data.getSizeof());
    }
    
    @Test
    public void testStruct1() throws Exception {
    	IAstModule mod = doFrontend(
    			"Tuple = data {\n"+
    			"   x:Int=66; y:Float;\n" +
    			"z;\n" +
    			" static f,g,h:Byte=9; };\n"+
    	"");
    	
    	sanityTest(mod);
    	IAstDataDecl decl = (IAstDataDecl) getMainExpr((IAstDefineStmt) mod.getScope().get("Tuple").getDefinition());
    	assertTrue(decl.getType() instanceof LLDataType);
    	LLDataType data = (LLDataType) decl.getType();
    	assertTrue(data.isGeneric());
    	assertEquals(6, data.getTypes().length);
    	assertEquals(3, data.getInstanceFields().length);
    	assertEquals(3, data.getStaticFields().length);
    	
    	assertEquals((2 + 4) * 8, data.getSizeof());		// z is generic and contributes nothing
    }
    
    @Test
    public void testStructAlign1() throws Exception {
    	IAstModule mod = doFrontend(
    			"Tuple = data {\n"+
    			"   x,y,z:Byte; f:Float; };\n"+
    	"");
    	
    	sanityTest(mod);
    	IAstDataDecl decl = (IAstDataDecl) getMainExpr((IAstDefineStmt) mod.getScope().get("Tuple").getDefinition());
    	assertTrue(decl.getType() instanceof LLDataType);
    	LLDataType data = (LLDataType) decl.getType();
    	assertTrue(data.isComplete());
    	assertTrue(data.isCompatibleWith(data));
    	
    	assertEquals(4, data.getTypes().length);
    	LLInstanceField[] fields = data.getInstanceFields();
		assertEquals(4, fields.length);
		assertEquals("x", fields[0].getName());
		assertEquals(0, fields[0].getOffset());
		assertEquals("y", fields[1].getName());
		assertEquals(8, fields[1].getOffset());
		assertEquals("z", fields[2].getName());
		assertEquals(16, fields[2].getOffset());
		assertEquals("f", fields[3].getName());
		assertEquals(32, fields[3].getOffset());
    	
    	assertEquals(32 + 4 * 8, data.getSizeof());
    }

    @Test
    public void testStructAlign2() throws Exception {
    	IAstModule mod = doFrontend(
    			"Tuple = data {\n"+
    			"   x:Byte; f:Float; y,z:Byte; };\n"+
    	"");
    	
    	sanityTest(mod);
    	IAstDataDecl decl = (IAstDataDecl) getMainExpr((IAstDefineStmt) mod.getScope().get("Tuple").getDefinition());
    	assertTrue(decl.getType() instanceof LLDataType);
    	LLDataType data = (LLDataType) decl.getType();
    	assertTrue(data.isComplete());
    	assertTrue(data.isCompatibleWith(data));
    	
    	assertEquals(4, data.getTypes().length);
    	LLInstanceField[] fields = data.getInstanceFields();
		assertEquals(4, fields.length);
		assertEquals("x", fields[0].getName());
		assertEquals(0, fields[0].getOffset());
		assertEquals("f", fields[1].getName());
		assertEquals(16, fields[1].getOffset());
		assertEquals("y", fields[2].getName());
		assertEquals(48, fields[2].getOffset());
		assertEquals("z", fields[3].getName());
		assertEquals(56, fields[3].getOffset());
    	
    	assertEquals(64, data.getSizeof());
    }
    
    @Test
    public void testStructAlloc1() throws Exception {
    	IAstModule mod = doFrontend(
    			"Tuple = data {\n"+
    			"   x:Byte; f:Float; y,z:Byte; };\n"+
    			"testStructAlloc1 = code() {\n"+
    			"  foo:Tuple;\n"+
    			"};\n"+
    	"");
    	
    	sanityTest(mod);
    	IAstDataDecl decl = (IAstDataDecl) getMainExpr((IAstDefineStmt) mod.getScope().get("Tuple").getDefinition());
    	assertTrue(decl.getType() instanceof LLDataType);
    	LLDataType data = (LLDataType) decl.getType();
    	
    	IAstCodeExpr code = (IAstCodeExpr) getMainExpr((IAstDefineStmt) mod.getScope().get("testStructAlloc1").getDefinition());
    	assertTrue(code.getType().isComplete());

    	IAstAllocStmt stmt = (IAstAllocStmt) code.stmts().getFirst();
    	assertEquals(data, stmt.getType());
    	
    	doGenerate(mod);
    }
}


