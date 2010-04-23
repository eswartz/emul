/**
 * 
 */
package org.ejs.eulang.test;

import static junit.framework.Assert.*;

import org.ejs.eulang.ast.IAstAllocStmt;
import org.ejs.eulang.ast.IAstAssignStmt;
import org.ejs.eulang.ast.IAstCodeExpr;
import org.ejs.eulang.ast.IAstDataType;
import org.ejs.eulang.ast.IAstDefineStmt;
import org.ejs.eulang.ast.IAstExprStmt;
import org.ejs.eulang.ast.IAstFieldExpr;
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
    public void testData0() throws Exception {
    	IAstModule mod = doFrontend(
    			"Tuple = data {\n"+
    			"   x:Int=66; y:Float;\n" +
    			" static f,g,h:Byte=9; };\n"+
    			"");

    	sanityTest(mod);
    	IAstDataType type = (IAstDataType) getMainExpr((IAstDefineStmt) mod.getScope().get("Tuple").getDefinition());
    	assertTrue(type.getType() instanceof LLDataType);
    	LLDataType data = (LLDataType) type.getType();
    	assertTrue(data.isComplete());
    	assertEquals(5, data.getTypes().length);
    	assertEquals(2, data.getInstanceFields().length);
    	assertEquals(3, data.getStaticFields().length);
    	
    	assertEquals((2 + 4) * 8, data.getSizeof());
    }
    
    @Test
    public void testData1() throws Exception {
    	IAstModule mod = doFrontend(
    			"Tuple = data {\n"+
    			"   x:Int=66; y:Float;\n" +
    			"z;\n" +
    			" static f,g,h:Byte=9; };\n"+
    	"");
    	
    	sanityTest(mod);
    	IAstDataType type = (IAstDataType) getMainExpr((IAstDefineStmt) mod.getScope().get("Tuple").getDefinition());
    	assertTrue(type.getType() instanceof LLDataType);
    	LLDataType data = (LLDataType) type.getType();
    	assertTrue(data.isGeneric());
    	assertEquals(6, data.getTypes().length);
    	assertEquals(3, data.getInstanceFields().length);
    	assertEquals(3, data.getStaticFields().length);
    	
    	assertEquals((2 + 4) * 8, data.getSizeof());		// z is generic and contributes nothing
    }
    
    @Test
    public void testDataAlign1() throws Exception {
    	IAstModule mod = doFrontend(
    			"Tuple = data {\n"+
    			"   x,y,z:Byte; f:Float; };\n"+
    	"");
    	
    	sanityTest(mod);
    	IAstDataType type = (IAstDataType) getMainExpr((IAstDefineStmt) mod.getScope().get("Tuple").getDefinition());
    	assertTrue(type.getType() instanceof LLDataType);
    	LLDataType data = (LLDataType) type.getType();
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
    public void testDataAlign2() throws Exception {
    	IAstModule mod = doFrontend(
    			"Tuple = data {\n"+
    			"   x:Byte; f:Float; y,z:Byte; };\n"+
    	"");
    	
    	sanityTest(mod);
    	IAstDataType type = (IAstDataType) getMainExpr((IAstDefineStmt) mod.getScope().get("Tuple").getDefinition());
    	assertTrue(type.getType() instanceof LLDataType);
    	LLDataType data = (LLDataType) type.getType();
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
    public void testDataAlloc1() throws Exception {
    	IAstModule mod = doFrontend(
    			"Tuple = data {\n"+
    			"   x:Byte; f:Float; y,z:Byte; };\n"+
    			"testDataAlloc1 = code() {\n"+
    			"  foo:Tuple;\n"+
    			"};\n"+
    	"");
    	
    	sanityTest(mod);
    	IAstDataType type = (IAstDataType) getMainExpr((IAstDefineStmt) mod.getScope().get("Tuple").getDefinition());
    	assertTrue(type.getType() instanceof LLDataType);
    	LLDataType data = (LLDataType) type.getType();
    	
    	IAstCodeExpr code = (IAstCodeExpr) getMainExpr((IAstDefineStmt) mod.getScope().get("testDataAlloc1").getDefinition());
    	assertTrue(code.getType().isComplete());

    	IAstAllocStmt stmt = (IAstAllocStmt) code.stmts().getFirst();
    	assertEquals(data, stmt.getType());
    	
    	doGenerate(mod);
    }
    
    @Test
    public void testDataDeref1() throws Exception {
    	IAstModule mod = doFrontend(
    			"Tuple = data {\n"+
    			"   x:Byte; f:Float; y,z:Byte; };\n"+
    			"testDataDeref1 = code() {\n"+
    			"  foo:Tuple;\n"+
    			"  foo.x;\n"+
    			"};\n"+
    	"");
    	
    	sanityTest(mod);
    	IAstDataType type = (IAstDataType) getMainExpr((IAstDefineStmt) mod.getScope().get("Tuple").getDefinition());
    	assertTrue(type.getType() instanceof LLDataType);
    	LLDataType data = (LLDataType) type.getType();
    	
    	IAstCodeExpr code = (IAstCodeExpr) getMainExpr((IAstDefineStmt) mod.getScope().get("testDataDeref1").getDefinition());
    	assertTrue(code.getType().isComplete());

    	IAstExprStmt stmt = (IAstExprStmt) code.stmts().list().get(1);
    	IAstFieldExpr field = (IAstFieldExpr) stmt.getExpr();
    	assertEquals(data.getField("x").getType(), field.getType());
    	assertEquals(data.getField("x").getType(), stmt.getType());
    	
    	doGenerate(mod);
    }
    
    @Test
    public void testDataDeref2() throws Exception {
    	IAstModule mod = doFrontend(
    			"Tuple = data {\n"+
    			"   x:Byte; f:Float; y,z:Byte; };\n"+
    			"testDataDeref2 = code() {\n"+
    			"  foo:Tuple;\n"+
    			"  (foo.x, foo.y, foo.z) = (1, 2, 3);\n"+
    			"  foo.x = foo.y + foo.z;\n"+
    			"  foo.x;\n"+
    			"};\n"+
    	"");
    	
    	sanityTest(mod);
    	IAstDataType type = (IAstDataType) getMainExpr((IAstDefineStmt) mod.getScope().get("Tuple").getDefinition());
    	assertTrue(type.getType() instanceof LLDataType);
    	LLDataType data = (LLDataType) type.getType();
    	
    	IAstCodeExpr code = (IAstCodeExpr) getMainExpr((IAstDefineStmt) mod.getScope().get("testDataDeref2").getDefinition());
    	assertTrue(code.getType().isComplete());

    	IAstAssignStmt stmt = (IAstAssignStmt) code.stmts().list().get(2);
    	IAstFieldExpr field = (IAstFieldExpr) stmt.getSymbolExprs().getFirst();
    	assertEquals(data.getField("x").getType(), field.getType());
    	assertEquals(data.getField("x").getType(), stmt.getType());
    	
    	doGenerate(mod);
    }
    
    
    @Test
    public void testDataDeref3() throws Exception {
    	IAstModule mod = doFrontend(
    			"Tuple = data {\n"+
    			"   x:Byte; f:Float; y,z:Byte; };\n"+
    			"getData = code() {\n"+
    			"  foo:Tuple;\n"+
    			"  (foo.x, foo.y, foo.z) = (1, 2, 3);\n"+
    			"  foo;\n"+
    			"};\n"+
    			"testDataDeref3 = code() {\n"+
    			"  foo:= getData();\n"+
    			"  foo.x;\n"+
    			"};\n"+
    	"");
    	
    	sanityTest(mod);
    	IAstDataType type = (IAstDataType) getMainExpr((IAstDefineStmt) mod.getScope().get("Tuple").getDefinition());
    	assertTrue(type.getType() instanceof LLDataType);
    	LLDataType data = (LLDataType) type.getType();
    	
    	IAstCodeExpr code = (IAstCodeExpr) getMainExpr((IAstDefineStmt) mod.getScope().get("testDataDeref3").getDefinition());
    	assertTrue(code.getType().isComplete());

    	IAstExprStmt stmt = (IAstExprStmt) code.stmts().list().get(1);
    	IAstFieldExpr field = (IAstFieldExpr) stmt.getExpr();
    	assertEquals(data.getField("x").getType(), field.getType());
    	assertEquals(data.getField("x").getType(), stmt.getType());
    	
    	doGenerate(mod);
    }
    

    @Test
    public void testDataDerefFail1() throws Exception {
    	IAstModule mod = treeize(
    			"Tuple = data {\n"+
    			"   x:Byte; f:Float; y,z:Byte; };\n"+
    			"getData = code() {\n"+
    			"  foo:Tuple;\n"+
    			"  foo.x.y = 4;\n"+
    			"};\n"+
    	"");
    	doTypeInfer(mod, true);
    }

    @Test
    public void testDataDerefFail2() throws Exception {
    	IAstModule mod = treeize(
    			"Tuple = data {\n"+
    			"   x:Byte; f:Float; y,z:Byte; };\n"+
    			"getData = code() {\n"+
    			"  foo:Tuple;\n"+
    			"  foo.pp = 4;\n"+
    			"};\n"+
    	"");
    	doTypeInfer(mod, true);
    }
    
    @Test
    public void testDataDeref4() throws Exception {
    	dumpTypeInfer = true;
    	IAstModule mod = doFrontend(
    			"Tuple = data {\n"+
    			"   x:Byte; f:Float; y,z:Byte; };\n"+
    			"testDataDeref4 = code() {\n"+
    			"  foo:Tuple[10];\n"+
    			"  foo[7].x = 33;\n"+
    			"};\n"+
    	"");
    	doGenerate(mod);
    }
    
    @Test
    public void testDataDeref5() throws Exception {
    	IAstModule mod = doFrontend(
    			"Tuple = data {\n"+
    			"   x:Byte; f:Float; y,z:Byte; };\n"+
    			"testDataDeref5 = code() {\n"+
    			"  foo:Tuple[10];\n"+
    			"  foo[7].x = foo[foo[0].z].x;\n"+
    			"};\n"+
    	"");
    	doGenerate(mod);
    }
    
    @Test
    public void testDataInit1() throws Exception {
    	IAstModule mod = doFrontend(
    			"Tuple = data {\n"+
    			"   x:Byte; f:Float; y,z:Byte; };\n"+
    			"testDataInit1 = code() {\n"+
    			"  foo:Tuple = [ 1, 2, .z=0x10, .y=0x20 ];\n"+
    			"};\n"+
    	"");
    	doGenerate(mod);
    }

    @Test
    public void testDataInit1b() throws Exception {
    	IAstModule mod = doFrontend(
    			"Tuple = data {\n"+
    			"   x:Byte; f:Float; y,z:Byte; };\n"+
    			"testDataInit1b = code() {\n"+
    			"  foo:Tuple = [ .f=4 ];\n"+
    			"};\n"+
    	"");
    	doGenerate(mod);
    }
    @Test
    public void testDataInit2() throws Exception {
    	IAstModule mod = doFrontend(
    			"testDataInit2 = code() {\n"+
    			"  foo:Int[10] = [ [5] = 55, [1] = 11 ];\n"+
    			"};\n"+
    	"");
    	doGenerate(mod);
    }
    
    
    @Test
    public void testDataInit3a() throws Exception {
    	IAstModule mod = doFrontend(
    			"Tuple = data {\n"+
    			"   x:Byte; f:Float; y,z:Byte; };\n"+
    			"testDataInit3a = code() {\n"+
    			"  foo:Tuple[10] = [];\n"+
    			"};\n"+
    	"");
    	doGenerate(mod);
    }
    
    @Test
    public void testDataInit3b() throws Exception {
    	IAstModule mod = doFrontend(
    			"Tuple = data {\n"+
    			"   x:Byte; f:Float; y,z:Byte; };\n"+
    			"testDataInit3b = code() {\n"+
    			"  foo:Tuple[5] = [ [3] = [ 1, .z=55], [1] = [.f=2] ];\n"+
    			"};\n"+
    	"");
    	doGenerate(mod);
    }
}


