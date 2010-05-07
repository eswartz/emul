/**
 * 
 */
package org.ejs.eulang.test;

import static junit.framework.Assert.*;

import org.ejs.eulang.ast.IAstAddrOfExpr;
import org.ejs.eulang.ast.IAstAllocStmt;
import org.ejs.eulang.ast.IAstAssignStmt;
import org.ejs.eulang.ast.IAstCodeExpr;
import org.ejs.eulang.ast.IAstDataType;
import org.ejs.eulang.ast.IAstDefineStmt;
import org.ejs.eulang.ast.IAstExprStmt;
import org.ejs.eulang.ast.IAstFieldExpr;
import org.ejs.eulang.ast.IAstIndexExpr;
import org.ejs.eulang.ast.IAstModule;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.IAstDerefExpr;
import org.ejs.eulang.llvm.LLVMGenerator;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLArrayType;
import org.ejs.eulang.types.LLCodeType;
import org.ejs.eulang.types.LLDataType;
import org.ejs.eulang.types.LLInstanceField;
import org.ejs.eulang.types.LLIntType;
import org.ejs.eulang.types.LLPointerType;
import org.ejs.eulang.types.LLType;
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
    			"q : Int[]= [];\n"+
    			"r : Int[]= [0,2];\n"+
    			""
    			);

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
    	
    	stmt = (IAstAllocStmt) mod.getScope().get("r").getDefinition();
    	assertTrue(stmt.getType() instanceof LLArrayType);
    	assertEquals(2, ((LLArrayType)stmt.getType()).getArrayCount()); 
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
    	IAstIndexExpr index = (IAstIndexExpr) getValue(stmt.getExpr());
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
    	IAstIndexExpr index = (IAstIndexExpr) getValue(stmt.getExpr());
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
    	IAstIndexExpr index = (IAstIndexExpr) getValue(stmt.getExpr());
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
    			"mycode := code(p:Int[10]; i => nil) {\n"+
    			"   p[i];"+
    			"};\n"+
    			"");

    	sanityTest(mod);
    	
    	IAstAllocStmt astmt = (IAstAllocStmt) mod.getScope().get("mycode").getDefinition();
    	assertTrue(astmt.getType() instanceof LLCodeType);
    	IAstCodeExpr code = (IAstCodeExpr) astmt.getExprs().getFirst();
    	
    	IAstExprStmt stmt = (IAstExprStmt) code.stmts().getFirst();
    	IAstIndexExpr index = (IAstIndexExpr) getValue(stmt.getExpr());
    	//assertEquals(typeEngine.getRefType(typeEngine.INT), index.getType());
    	assertEquals(typeEngine.INT, index.getType());
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
    			"Tuple = [T] data {\n"+
    			"   x:Int=66; y:Float;\n" +
    			"z:T;\n" +
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
    	IAstFieldExpr field = getField(stmt.getExpr());
    	assertEquals(data.getField("x").getType(), field.getType());
    	assertEquals(data.getField("x").getType(), stmt.getType());
    	
    	doGenerate(mod);
    }
    
    /**
	 * @param expr
	 * @return
	 */
	private IAstFieldExpr getField(IAstTypedExpr expr) {
		while (expr instanceof IAstDerefExpr)
			expr = ((IAstDerefExpr) expr).getExpr();
		return (IAstFieldExpr) expr;
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
    	IAstFieldExpr field = getField(stmt.getSymbolExprs().getFirst());
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
    	IAstFieldExpr field = getField(stmt.getExpr());
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
    			"  foo[7].x = 33;\n"+		// don't deref LHS
    			"  foo[1].x = foo[2].x;\n"+	// do deref RHS
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
    public void testDataDeref6() throws Exception {
    	IAstModule mod = doFrontend(
    			"Tuple = data {\n"+
    			"   next:Tuple^; val:Byte; };\n"+
    			"testDataDeref6 = code() {\n"+
    			"  foo, foo2:Tuple;\n"+
    			"  foo.next = &foo2;\n"+
    			"  foo2.val = 0x55;\n"+
    			"  foo.next.val;\n"+
    			"};\n"+
    	"");
    	LLVMGenerator gen = doGenerate(mod);
    	assertFoundInUnoptimizedText("store %Byte 85, %Byte* ", gen);
    	assertFoundInUnoptimizedText("load %Tuple$p* %", gen);
    	assertFoundInUnoptimizedText("load %Byte* %", gen);
    }
    @Test
    public void testDataDeref7() throws Exception {
    	// should not infer that 'val' is a Double or fail to cast the RHS to Byte
    	IAstModule mod = doFrontend(
    			"Tuple = [T] data {\n"+
    			"   val:T; };\n"+
    			"testDataDeref6 = code(x:Double) {\n"+
    			"  foo:Tuple<Byte>;\n"+
    			"  foo.val = x;\n"+
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
    	LLVMGenerator gen = doGenerate(mod);
    	assertFoundInUnoptimizedText("store %Tuple bitcast (%Tuple$init { %Byte 1, %Bytex1 zeroinitializer, %Float 2.0, %Byte 32, %Byte 16 } to %Tuple), %Tuple*", gen);
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
    	LLVMGenerator gen = doGenerate(mod);
    	assertFoundInUnoptimizedText("store %Tuple bitcast (%Tuple$init { %Bytex2 zeroinitializer, %Float 4.0, %Bytex3 zeroinitializer } to %Tuple), %Tuple*", gen);
    }
    @Test
    public void testDataInit2() throws Exception {
    	IAstModule mod = doFrontend(
    			"testDataInit2 = code() {\n"+
    			"  foo:Int[10] = [ [5] = 55, [1] = 11 ];\n"+
    			"};\n"+
    	"");
    	LLVMGenerator gen = doGenerate(mod);
    	assertFoundInUnoptimizedText("store %Intx10 [ %Int zeroinitializer, %Int 11, %Int zeroinitializer, %Int zeroinitializer, %Int zeroinitializer, %Int 55, %Int zeroinitializer, %Int zeroinitializer, %Int zeroinitializer, %Int zeroinitializer ], %Intx10*", gen);
    }
    
    @Test
    public void testDataInit2b() throws Exception {
    	IAstModule mod = doFrontend(
    			"testDataInit2b = code() {\n"+
    			"  foo:Int[10] = [ 1, [5] = 55, 66, 77, 88, 99, [1] = 11, 22, 33, 44 ];\n"+
    			"};\n"+
    	"");
    	LLVMGenerator generator = doGenerate(mod);
    	assertFoundInUnoptimizedText("store %Intx10 [ %Int 1, %Int 11, %Int 22, %Int 33, %Int 44, %Int 55, %Int 66, %Int 77, %Int 88, %Int 99 ], %Intx10*", generator);
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
    	LLVMGenerator gen = doGenerate(mod);
    	assertFoundInUnoptimizedText("tore %Tuplex10 [ %Tuple zeroinitializer, %Tuple zeroinitializer, %Tuple zeroinitializer, %Tuple zeroinitializer, %Tuple zeroinitializer, %Tuple zeroinitializer, %Tuple zeroinitializer, %Tuple zeroinitializer, %Tuple zeroinitializer, %Tuple zeroinitializer ], %Tuplex10*", gen);
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
    	LLVMGenerator gen = doGenerate(mod);
    	assertFoundInUnoptimizedText("store %Tuplex5 [ %Tuple zeroinitializer, %Tuple bitcast (%Tuple$init { %Bytex2 zeroinitializer, %Float 2.0, %Bytex3 zeroinitializer } to %Tuple), %Tuple zeroinitializer, %Tuple bitcast (%Tuple$init.0 { %Byte 1, %Bytex6 zeroinitializer, %Byte 55 } to %Tuple), %Tuple zeroinitializer ], %Tuplex5*", gen);
    }
    
    @Test
    public void testDataInitVar1() throws Exception {
    	IAstModule mod = doFrontend(
    			"testDataInit2 = code() {\n"+
    			"  val := 10;\n"+
    			"  foo:Int[10] = [ [5] = val, [1] = 11 ];\n"+
    			"};\n"+
    	"");
    	LLVMGenerator gen = doGenerate(mod);
    	assertFoundInUnoptimizedText("insertvalue %Intx10 [ %Int zeroinitializer, %Int 11, %Int zeroinitializer, %Int zeroinitializer, %Int zeroinitializer, %Int zeroinitializer, %Int zeroinitializer, %Int zeroinitializer, %Int zeroinitializer, %Int zeroinitializer ], %Int", gen);
    	assertFoundInUnoptimizedText(", 5", gen);
    }

    @Test
    public void testDataInitVar2() throws Exception {
    	IAstModule mod = doFrontend(
    			"Tuple = data {\n"+
    			"   x:Byte; f:Float; y,z:Byte; };\n"+
    			"testDataInit3b = code() {\n"+
    			"  baz:=55;\n"+
    			"  foo:Tuple[5] = [ [3] = [ baz, .z=55], [1] = [.f=baz] ];\n"+
    			"};\n"+
    	"");
    	// hmm, doing this requires fancy bitcasts and loads/stores to temps,
    	// and I don't really feel like doing that yet
    	doGenerate(mod, true);
    }
    

	@Test
    public void testArrayDecls3Fail() throws Exception {
		// can't have an empty zero dimension unless initializer present
    	treeizeFail(
    			"testDataInit4 = code() {\n"+
    			"  foo:Byte[][3][3];\n"+
    			"};\n"+
    	"");
    	
    }


	@Test
    public void testArrayAccess3() throws Exception {
    	IAstModule mod = doFrontend(
    			"testArrayAccess3 = code(foo:Byte[3][3]) {\n"+
    			"  foo[1][2] + (foo[2])[2];"+
    			"};\n"+
    	"");
    	IAstCodeExpr code = (IAstCodeExpr) getMainExpr((IAstDefineStmt) mod.getScope().get("testArrayAccess3").getDefinition());
    	assertTrue(code.getType().isComplete());

    	ISymbol sym = code.getScope().get("foo");
    	assertEquals(3, ((LLArrayType) sym.getType()).getArrayCount());
    	assertEquals(3, ((LLArrayType)((LLArrayType) sym.getType()).getSubType()).getArrayCount());
    	
    	LLVMGenerator gen = doGenerate(mod);
    	assertFoundInOptimizedText("%foo, i16 0, i16 1, i16 2", gen);
    	assertFoundInOptimizedText("%foo, i16 0, i16 2, i16 2", gen);
    	
    }
	@Test
    public void testDataInit4() throws Exception {
    	IAstModule mod = doFrontend(
    			"testDataInit4 = code() {\n"+
    			"  foo:Byte[][3] = [ [ 1, 2, 3], [4, 5, 6], [7, 8, 9]];\n"+
    			"};\n"+
    	"");
    	IAstCodeExpr code = (IAstCodeExpr) getMainExpr((IAstDefineStmt) mod.getScope().get("testDataInit4").getDefinition());
    	assertTrue(code.getType().isComplete());

    	IAstAllocStmt stmt = (IAstAllocStmt) code.stmts().list().get(0);
    	assertEquals(3, ((LLArrayType) stmt.getType()).getArrayCount()); // fills type from initializer
    	assertEquals(3, ((LLArrayType)((LLArrayType) stmt.getType()).getSubType()).getArrayCount());
    	
    	LLVMGenerator gen = doGenerate(mod);
    	assertFoundInUnoptimizedText("%Bytex3x3 [ %Bytex3 [ %Byte 1, %Byte 2, %Byte 3 ], %Bytex3 [ %Byte 4, %Byte 5, %Byte 6 ], %Bytex3 [ %Byte 7, %Byte 8, %Byte 9 ] ], %Bytex3x3*", gen);
    }
	
	@Test
	public void testPointerDecl1() throws Exception {
    	IAstModule mod = doFrontend(
    			"testPointerDecl1 = code() {\n"+
    			"  foo0:Byte^;\n"+
    			"  foo1:Byte^^;\n"+
    			"  foo2:Byte^[10];\n"+
    			"  foo3:Byte[10]^;\n"+
    			"  foo4:Byte^[10]^;\n"+
    			"};\n"+
    	"");
    	IAstCodeExpr code = (IAstCodeExpr) getMainExpr((IAstDefineStmt) mod.getScope().get("testPointerDecl1").getDefinition());
    	assertTrue(code.getType().isComplete());

    	IAstAllocStmt stmt;
		stmt = (IAstAllocStmt) code.stmts().list().get(0);
    	assertTrue(stmt.getType() instanceof LLPointerType);
    	assertTrue(stmt.getType().getSubType() instanceof LLIntType);
    	
    	stmt = (IAstAllocStmt) code.stmts().list().get(1);
    	assertTrue(stmt.getType() instanceof LLPointerType);
    	assertTrue(stmt.getType().getSubType() instanceof LLPointerType);
    	assertTrue(stmt.getType().getSubType().getSubType() instanceof LLIntType);
		
    	stmt = (IAstAllocStmt) code.stmts().list().get(2);
    	assertTrue(stmt.getType() instanceof LLArrayType);
    	assertTrue(stmt.getType().getSubType() instanceof LLPointerType);
    	assertTrue(stmt.getType().getSubType().getSubType() instanceof LLIntType);
    	
    	stmt = (IAstAllocStmt) code.stmts().list().get(3);
    	assertTrue(stmt.getType() instanceof LLPointerType);
    	assertTrue(stmt.getType().getSubType() instanceof LLArrayType);
    	assertTrue(stmt.getType().getSubType().getSubType() instanceof LLIntType);
    	
    	stmt = (IAstAllocStmt) code.stmts().list().get(4);
    	assertTrue(stmt.getType() instanceof LLPointerType);
    	assertTrue(stmt.getType().getSubType() instanceof LLArrayType);
    	assertTrue(stmt.getType().getSubType().getSubType() instanceof LLPointerType);
    	assertTrue(stmt.getType().getSubType().getSubType().getSubType() instanceof LLIntType);
    	
	}
	
	@Test
	public void testPointerInit1() throws Exception {
    	IAstModule mod = doFrontend(
    			"testPointerInit1 = code() {\n"+
    			"  foo:Byte=10;\n"+
    			"  foo0:Byte^=&foo;\n"+
    			"  foo0^ = 6;\n"+
    			"  foo0 = 0;\n"+
    			"};\n"+
    	"");
    	IAstCodeExpr code = (IAstCodeExpr) getMainExpr((IAstDefineStmt) mod.getScope().get("testPointerInit1").getDefinition());
    	assertTrue(code.getType().isComplete());

    	LLType bytePtr = typeEngine.getPointerType(typeEngine.BYTE);
    	IAstAllocStmt alloc;
    	
    	// setting a pointer to an address
		alloc = (IAstAllocStmt) code.stmts().list().get(1);
    	assertEquals(bytePtr, alloc.getType());
    	assertEquals(bytePtr, alloc.getTypeExpr().getType());
    	assertEquals(bytePtr, alloc.getSymbolExprs().getFirst().getType());
    	assertEquals(bytePtr, alloc.getExprs().getFirst().getType());
    	IAstAddrOfExpr addrOf = (IAstAddrOfExpr) alloc.getExprs().getFirst();
    	assertEquals(typeEngine.BYTE, addrOf.getExpr().getType());
    	
    	IAstAssignStmt assg;
    	// setting the byte value
    	assg = (IAstAssignStmt) code.stmts().list().get(2);
    	assertEquals(typeEngine.BYTE, assg.getType());
    	assertEquals(typeEngine.BYTE, assg.getSymbolExprs().getFirst().getType());
    	assertEquals(typeEngine.BYTE, assg.getExprs().getFirst().getType());
    	
    	// setting the pointer value
    	assg = (IAstAssignStmt) code.stmts().list().get(3);
    	assertEquals(bytePtr, assg.getType());
    	assertEquals(bytePtr, assg.getSymbolExprs().getFirst().getType());
    	assertEquals(bytePtr, assg.getExprs().getFirst().getType());

    	LLVMGenerator gen = doGenerate(mod);
    	assertFoundInOptimizedText("ret i8* null", gen);
	}
	
	@Test
	public void testPointerInit1b() throws Exception {
    	IAstModule mod = doFrontend(
    			"testPointerInit1b = code() {\n"+
    			"  foo:Byte=10;\n"+
    			"  foo0:=&foo;\n"+
    			"};\n"+
    	"");
    	IAstCodeExpr code = (IAstCodeExpr) getMainExpr((IAstDefineStmt) mod.getScope().get("testPointerInit1b").getDefinition());
    	assertTrue(code.getType().isComplete());

    	LLType bytePtr = typeEngine.getPointerType(typeEngine.BYTE);
    	IAstAllocStmt alloc;
    	
    	// setting a pointer to an address
		alloc = (IAstAllocStmt) code.stmts().list().get(1);
    	assertEquals(bytePtr, alloc.getType());
    	assertEquals(bytePtr, alloc.getTypeExpr().getType());
    	assertEquals(bytePtr, alloc.getSymbolExprs().getFirst().getType());
    	assertEquals(bytePtr, alloc.getExprs().getFirst().getType());
    	IAstAddrOfExpr addrOf = (IAstAddrOfExpr) alloc.getExprs().getFirst();
    	assertEquals(typeEngine.BYTE, addrOf.getExpr().getType());
    	
    	// infers a pointer type
    	alloc = (IAstAllocStmt) code.stmts().list().get(1);
    	assertEquals(bytePtr, alloc.getType());
    	assertEquals(bytePtr, alloc.getSymbolExprs().getFirst().getType());
    	assertEquals(bytePtr, alloc.getExprs().getFirst().getType());
    	
    	doGenerate(mod);
    	
	}
	@Test
	public void testPointerInit2() throws Exception {
    	IAstModule mod = doFrontend(
    			"testPointerInit2 = code() {\n"+
    			"  foo:Byte=10;\n"+
    			"  foo0:Byte^=0;\n"+
    			"  foo0 = &foo;\n"+
    			"  foo0^;\n"+
    			"};\n"+
    	"");
    	IAstCodeExpr code = (IAstCodeExpr) getMainExpr((IAstDefineStmt) mod.getScope().get("testPointerInit2").getDefinition());
    	assertTrue(code.getType().isComplete());

    	LLVMGenerator gen = doGenerate(mod);
    	assertFoundInOptimizedText("ret i8 10", gen);
	}
	
	@Test
	public void testPointerMixing1() throws Exception {
    	IAstModule mod = doFrontend(
    			"testPointerMixing1 = code() {\n"+
    			"  foo:Byte=10;\n"+
    			"  foo0:Byte^=&foo;\n"+
    			"  foo0^ = 6;\n"+
    			"  foo0^+foo^;\n"+
    			"};\n"+
    	"");
    	IAstCodeExpr code = (IAstCodeExpr) getMainExpr((IAstDefineStmt) mod.getScope().get("testPointerMixing1").getDefinition());
    	assertTrue(code.getType().isComplete());

    	LLVMGenerator gen = doGenerate(mod);
    	assertFoundInOptimizedText("ret i8 12", gen);		// foo0=6 changed foo
	}
	@Test
	public void testPointerMixing2() throws Exception {
    	IAstModule mod = doFrontend(
    			"testPointerMixing2 = code() {\n"+
    			"  foo:Byte[2]=[10,5];\n"+
    			"  foo0:Byte^=&foo[0];\n"+
    			"  foo0^ = 6;\n"+
    			"  foo0^+foo[1];\n"+
    			"};\n"+
    	"");
    	IAstCodeExpr code = (IAstCodeExpr) getMainExpr((IAstDefineStmt) mod.getScope().get("testPointerMixing2").getDefinition());
    	assertTrue(code.getType().isComplete());

    	LLVMGenerator gen = doGenerate(mod);
    	assertFoundInOptimizedText("ret i8 11", gen);
	}
	@Test
	public void testPointerArrays1() throws Exception {
    	IAstModule mod = doFrontend(
    			"testPointerArrays1 = code() {\n"+
    			"  foo:Byte[2]=[10,5];\n"+
    			"  foo0:Byte^=&foo;\n"+		// array!
    			"  foo0^ = 6;\n"+
    			"  foo0^+foo[1];\n"+
    			"};\n"+
    	"");
    	IAstCodeExpr code = (IAstCodeExpr) getMainExpr((IAstDefineStmt) mod.getScope().get("testPointerArrays1").getDefinition());
    	assertTrue(code.getType().isComplete());

    	LLVMGenerator gen = doGenerate(mod);
    	assertFoundInOptimizedText("ret i8 11", gen);
	}
	@Test
	public void testPointerArrays1Bad() throws Exception {
    	IAstModule mod = doFrontend(
    			"testPointerArrays1Bad = code() {\n"+
    			"  foo:Byte[2]=[10,5];\n"+
    			"  foo0:Byte^=foo;\n"+		// not legal
    			"};\n"+
    	"");
    	doGenerate(mod, true);
	}
	
	@Test
	public void testPointerInit3() throws Exception {
    	IAstModule mod = doFrontend(
    			"testPointerInit3 = code() {\n"+
    			"  foo:Int^=0;\n"+	
    			"  foo^=100;\n" +	// null reference
    			"};\n"+
    	"");
    	LLVMGenerator gen = doGenerate(mod);
    	assertFoundInOptimizedText("unreachable", gen);
	}
	@Test
	public void testPointerMath1() throws Exception {
    	IAstModule mod = doFrontend(
    			"testPointerMath1 = code(foo:Int^) {\n"+
    			"  foo-foo;\n"+
    			"};\n"+
    	"");
    	LLVMGenerator gen = doGenerate(mod);
    	assertFoundInOptimizedText("ret i16 0", gen);
	}
	@Test
	public void testPointerMath2() throws Exception {
    	IAstModule mod = doFrontend(
    			"testPointerMath2 = code(foo:Int^) {\n"+
    			"  foo+4-foo;\n"+
    			"};\n"+
    	"");
    	LLVMGenerator gen = doGenerate(mod);
    	assertFoundInOptimizedText("ashr i16", gen);
	}
	@Test
	public void testPointerMath3() throws Exception {
    	IAstModule mod = doFrontend(
    			"testPointerMath3 = code() {\n"+
    			"  foo:Int[4] = [ 1, 2, 3, 4];\n"+
    			"  foop:Int^=&foo;\n"+
    			"  foo1, foo2 : Int^ = foop+1, foop+3;\n"+
    			"  foo1^ * foo2^;\n"+
    			"};\n"+
    	"");
    	LLVMGenerator gen = doGenerate(mod);
    	assertFoundInOptimizedText("ret i16 8", gen);
	}
	@Test
	public void testPointerMath3b() throws Exception {
    	IAstModule mod = doFrontend(
    			"testPointerMath3b = code() {\n"+
    			"  foo:Int[4] = [ 1, 2, 3, 4];\n"+
    			"  foop:Int^=&foo;\n"+
    			"  (foop+1)^ * (foop+3)^;\n"+
    			"};\n"+
    	"");
    	LLVMGenerator gen = doGenerate(mod);
    	assertFoundInOptimizedText("ret i16 8", gen);
	}
	
	@Test
	public void testPointerInit4() throws Exception {
    	IAstModule mod = doFrontend(
    			"testPointerInit4 = code() {\n"+
    			"  foo:Byte=10;\n"+
    			"  foo0:Int^=&foo;\n"+
    			"};\n"+
    	"");
    	IAstCodeExpr code = (IAstCodeExpr) getMainExpr((IAstDefineStmt) mod.getScope().get("testPointerInit4").getDefinition());
    	assertTrue(code.getType().isComplete());

    	LLVMGenerator gen = doGenerate(mod);
    	assertFoundInOptimizedText("bitcast i8*", gen);
    	assertFoundInOptimizedText("to i16*", gen);
	}
	
	@Test
	public void testCastingBad1() throws Exception {
    	IAstModule mod = doFrontend(
    			"testCastingBad1 = code() {\n"+
    			"  foo:Byte[1];\n"+
    			"  foo0:Byte[2]=foo;\n"+	// cast from array to array
    			"};\n"+
    	"");
    	IAstCodeExpr code = (IAstCodeExpr) getMainExpr((IAstDefineStmt) mod.getScope().get("testCastingBad1").getDefinition());
    	assertTrue(code.getType().isComplete());

    	doGenerate(mod, true);
	}
	
    @Test
    public void testDataFuncPtr0() throws Exception {
    	IAstModule mod = doFrontend(
    			"Class = data {\n"+
    			"  draw:code(this:Class; count:Int => Int);\n"+
    			"};\n"+
    	"");
    	IAstDefineStmt defineStmt = (IAstDefineStmt) mod.getScope().get("Class").getDefinition();
		IAstDataType dataNode = (IAstDataType) getMainExpr(defineStmt);
    	assertTrue(dataNode.getType().isComplete());
    	LLDataType data = (LLDataType) dataNode.getType();
    	LLType drawFieldType = data.getField("draw").getType();
    	assertTrue(drawFieldType instanceof LLPointerType);
		assertTrue(drawFieldType.getSubType() instanceof LLCodeType);
    }
    @Test
    public void testDataFuncPtr1() throws Exception {
    	IAstModule mod = doFrontend(
    			"Class = data {\n"+
    			"  draw:code(this:Class^; count:Int => Int);\n"+
    			"};\n"+
    			"doDraw = code(this:Class^; count:Int) { count*count };\n"+
    			"testDataFuncPtr1 = code() {\n"+
    			"  inst : Class;\n"+
    			"  inst.draw = doDraw;\n"+
    			"  inst.draw(&inst, 5);\n"+
    			"};\n"+
    	"");
    	LLVMGenerator gen = doGenerate(mod);
    	assertFoundInOptimizedText("ret i16 25", gen);
    }
    @Test
    public void testDataFuncPtr2() throws Exception {
    	IAstModule mod = doFrontend(
    			"Class = data {\n"+
    			"  val:Int;\n"+
    			"  draw:code(this:Class^; count:Int=>Int);\n"+
    			"};\n"+
    			"doCall = code(this:Class^; count:Int) { this.draw(this, count); };\n"+
    			"doDraw = code(this:Class^; count:Int) { this.val + count*count };\n"+
    			"testDataFuncPtr2 = code() {\n"+
    			"  inst : Class;\n"+
    			"  inst.val = 10;\n"+
    			"  inst.draw = doDraw;\n"+
    			"  doCall(&inst, 5);\n"+
    			"};\n"+
    	"");
    	LLVMGenerator gen = doGenerate(mod);
    	assertFoundInOptimizedText("ret i16 35", gen); // yay!  (Note: needs -std-link-opts)
    }

	/**
	 * Test that the symbols created for generic types are really generic
	 * and unique 
	 * @throws Exception
	 */
	@Test 
	public void testGenericTypes0() throws Exception {
		dumpTypeInfer = true;
		dumpTreeize = true;
		IAstModule mod = doFrontend(
				"List = [] data {\n" +
				"        next:List^;\n" + 	//no <>
				"};\n" + 
				"\n" +
				"intList = code() {\n"+
				"   list1 : List<>;\n" +
				"   list1.next = &list1;\n"+
				"};\n" +
				"");
		sanityTest(mod);
		IAstCodeExpr code = (IAstCodeExpr) getMainBodyExpr((IAstDefineStmt) mod.getScope().get("intList").getDefinition());
		IAstAllocStmt alloc = (IAstAllocStmt) code.stmts().getFirst();
		LLDataType data = (LLDataType) alloc.getType();
		assertTrue(data.isComplete() && !data.isGeneric());
		assertEquals(1, data.getInstanceFields().length);
		LLPointerType dataPtr = typeEngine.getPointerType(data);
		LLInstanceField nextField = data.getInstanceFields()[0];
		assertTrue(dataPtr.isCompatibleWith(nextField.getType()));	// one is an LLUpType
		
		doGenerate(mod);
	}

	/**
	 * When adding to a generic data, we can instantiate types 
	 * @throws Exception
	 */
	@Test 
	public void testGenericTypes1() throws Exception {
		dumpTypeInfer = true;
		dumpTreeize = true;
		IAstModule mod = doFrontend(
				"List = [T] data {\n" +
				"        node:T;\n"+
				"        next:List^;\n" + 	// no <>
				"};\n" + 
				"\n" +
				"intList = code(x:Int;y:Int=>Int) {\n"+
				"   list1, list2 : List<Int>;\n" +
				"   list1.node = x;\n"+
				"   list2.node = y;\n"+
				"   list1.next = &list2;\n"+
				"   list1.next.node;\n"+
				"};\n" +
				"");
		sanityTest(mod);
		IAstCodeExpr code = (IAstCodeExpr) getMainBodyExpr((IAstDefineStmt) mod.getScope().get("intList").getDefinition());
		IAstAllocStmt alloc = (IAstAllocStmt) code.stmts().getFirst();
		LLDataType data = (LLDataType) alloc.getType();
		assertTrue(data.isComplete() && !data.isGeneric());
		assertEquals(2, data.getInstanceFields().length);
		assertEquals(typeEngine.INT, data.getInstanceFields()[0].getType());
		LLPointerType dataPtr = typeEngine.getPointerType(data);
		LLInstanceField nextField = data.getInstanceFields()[1];
		assertTrue(dataPtr.isCompatibleWith(nextField.getType()));	// one is an LLUpType
		
		doGenerate(mod);
	}

	/**
	 * When adding to a generic data, we can instantiate types 
	 * @throws Exception
	 */
	@Test 
	public void testGenericTypes1a() throws Exception {
		dumpTypeInfer = true;
		dumpTreeize = true;
		IAstModule mod = doFrontend(
				"List = [T] data {\n" +
				"        node:T;\n"+
				"        next:List^;\n" + 	// no <>
				"};\n" + 
				"\n" +
				"intList = code(x:Int;y:Int=>Int) {\n"+
				"   list1 : List<Int>;\n" +
				"   list2 : List<Int>;\n" +
				"   list1.node = x;\n"+
				"   list2.node = y;\n"+
				"   list1.next = &list2;\n"+
				"   list1.next.node;\n"+
				"};\n" +
				"");
		sanityTest(mod);
		IAstCodeExpr code = (IAstCodeExpr) getMainBodyExpr((IAstDefineStmt) mod.getScope().get("intList").getDefinition());
		IAstAllocStmt alloc = (IAstAllocStmt) code.stmts().getFirst();
		LLDataType data = (LLDataType) alloc.getType();
		assertTrue(data.isComplete() && !data.isGeneric());
		assertEquals(2, data.getInstanceFields().length);
		assertEquals(typeEngine.INT, data.getInstanceFields()[0].getType());
		LLPointerType dataPtr = typeEngine.getPointerType(data);
		LLInstanceField nextField = data.getInstanceFields()[1];
		assertTrue(dataPtr.isCompatibleWith(nextField.getType()));	// one is an LLUpType
		
		doGenerate(mod);
	}

	@Test 
	public void testGenericTypes1b() throws Exception {
		dumpTypeInfer = true;
		dumpTreeize = true;
		IAstModule mod = doFrontend(
				"List = [T] data {\n" +
				"        node:T;\n"+
				"        next:List<T>^;\n" + 		// note: indicating type here, tho not necessary
				"};\n" + 
				"\n" +
				"intList = code(x:Int;y:Int=>Int) {\n"+
				"   list1, list2 : List<Int>;\n" +
				"   list1.node = x;\n"+
				"   list2.node = y;\n"+
				"   list1.next = &list2;\n"+
				"   list1.next.node;\n"+
				"};\n" +
				"");
		sanityTest(mod);
		IAstCodeExpr code = (IAstCodeExpr) getMainBodyExpr((IAstDefineStmt) mod.getScope().get("intList").getDefinition());
		IAstAllocStmt alloc = (IAstAllocStmt) code.stmts().getFirst();
		LLDataType data = (LLDataType) alloc.getType();
		assertTrue(data.isComplete() && !data.isGeneric());
		assertEquals(2, data.getInstanceFields().length);
		assertEquals(typeEngine.INT, data.getInstanceFields()[0].getType());
		LLPointerType dataPtr = typeEngine.getPointerType(data);
		LLInstanceField nextField = data.getInstanceFields()[1];
		assertTrue(dataPtr.isCompatibleWith(nextField.getType()));	// one is an LLUpType
		
		doGenerate(mod);
	}
	
	@Test 
	public void testGenericTypes2a() throws Exception {
		dumpTypeInfer = true;
		dumpTreeize = true;
		IAstModule mod = doFrontend(
				"List = [T, U] data {\n" +
				"        node:T;\n"+
				"        next:List<U, T>^;\n" +
				"};\n" + 
				"foo : List<Int, Double>;\n"+
				"");
		sanityTest(mod);
		IAstAllocStmt alloc = (IAstAllocStmt) mod.getScope().get("foo").getDefinition();
		LLDataType data = (LLDataType) alloc.getType();
		assertTrue(data.isComplete() && !data.isGeneric());
		assertEquals(2, data.getInstanceFields().length);
		assertEquals(typeEngine.INT, data.getInstanceFields()[0].getType());
		LLPointerType dataPtr = typeEngine.getPointerType(data);
		LLInstanceField nextField = data.getInstanceFields()[1];
		assertFalse(dataPtr.isCompatibleWith(nextField.getType()));	
		doGenerate(mod);
	}
	
	
	@Test 
	public void testGenericTypes2() throws Exception {
		// there are two expansions here; be sure to name the types properly
		// so they don't get confused in LLVM
		dumpTypeInfer = true;
		dumpTreeize = true;
		IAstModule mod = doFrontend(
				"List = [T, U] data {\n" +
				"        node:T;\n"+
				"        next:List<U, T>^;\n" +
				"};\n" + 
				"\n" +
				"intList = code(x:Int;y:Double) {\n"+
				"   list1 : List<Int, Double>; list2 : List<Double, Int>;\n" +
				"   list1.node = x;\n"+
				"   list2.node = y;\n"+
				"   list1.next = &list2;\n"+
				"   list2.next = &list1;\n"+
				"   list1.next.node;\n"+
				"};\n" +
				"");
		sanityTest(mod);
		IAstCodeExpr code = (IAstCodeExpr) getMainBodyExpr((IAstDefineStmt) mod.getScope().get("intList").getDefinition());
		IAstAllocStmt alloc = (IAstAllocStmt) code.stmts().getFirst();
		LLDataType data = (LLDataType) alloc.getType();
		assertTrue(data.isComplete() && !data.isGeneric());
		assertEquals(2, data.getInstanceFields().length);
		assertEquals(typeEngine.INT, data.getInstanceFields()[0].getType());
		LLPointerType dataPtr = typeEngine.getPointerType(data);
		LLInstanceField nextField = data.getInstanceFields()[1];
		assertFalse(dataPtr.isCompatibleWith(nextField.getType()));	// different type 
		
		IAstAllocStmt alloc2 = (IAstAllocStmt) code.stmts().list().get(1);
		LLDataType data2 = (LLDataType) alloc2.getType();
		assertTrue(data2.isComplete() && !data2.isGeneric());
		assertEquals(2, data2.getInstanceFields().length);
		assertEquals(typeEngine.DOUBLE, data2.getInstanceFields()[0].getType());
		LLPointerType data2Ptr = typeEngine.getPointerType(data2);
		LLInstanceField nextField2 = data2.getInstanceFields()[1];
		
		assertFalse(data2Ptr.isCompatibleWith(nextField2.getType()));	// different type
		
		assertTrue(dataPtr.isCompatibleWith(nextField2.getType()));	
		assertTrue(data2Ptr.isCompatibleWith(nextField.getType()));	
		
		LLCodeType codeType = (LLCodeType) code.getType();
		assertEquals(typeEngine.DOUBLE, codeType.getRetType());
		doGenerate(mod);
	}
	
	@Test
	public void testGenericFuncs1a() throws Exception {
		dumpTypeInfer = true;
		dumpTreeize = true;
		IAstModule mod = doFrontend(
				"neg = [T] code (x:T) { -x };\n"+
				"testGenericFuncs1a = code (x:Int=>Int) {\n"+
				"  neg(x);\n"+	// no explicit <>
				"};\n"+
				"");
		sanityTest(mod);
		doGenerate(mod);
	}
	@Test
	public void testGenericFuncs1b() throws Exception {
		dumpTypeInfer = true;
		dumpTreeize = true;
		IAstModule mod = doFrontend(
				"neg = [T] code (x:T) { -x };\n"+
				"testGenericFuncs1b = code (x:Int=>Int) {\n"+
				"  neg<Int>(x);\n"+	// explicit <>
				"};\n"+
				"");
		sanityTest(mod);
		doGenerate(mod);
	}
	
	@Test 
	public void testGenericFuncs2() throws Exception {
		dumpTypeInfer = true;
		dumpTreeize = true;
		IAstModule mod = doFrontend(
				"List = [T] data {\n" +
				"        node:T;\n"+
				"        next:List<T>^;\n" +	
				"};\n" + 
				"newList = [T] code ( => List<T>) { nil };\n "+
				"listAdd = [T] code (list:List<T>^; x:T) { new:=newList(); list.next=new; list.node=x; list; };\n"+
				"intList = code (x:Int=>Int) {\n"+
				"  a:=newList();\n"+
				"  a= listAdd(a, 10);\n"+
				"};\n"+
	
				"");
		sanityTest(mod);
		IAstCodeExpr code = (IAstCodeExpr) getMainBodyExpr((IAstDefineStmt) mod.getScope().get("intList").getDefinition());
		IAstAllocStmt alloc = (IAstAllocStmt) code.stmts().getFirst();
		LLDataType data = (LLDataType) alloc.getType();
		assertTrue(data.isComplete() && !data.isGeneric());
		assertEquals(2, data.getInstanceFields().length);
		assertEquals(typeEngine.INT, data.getInstanceFields()[0].getType());
		LLPointerType dataPtr = typeEngine.getPointerType(data);
		LLInstanceField nextField = data.getInstanceFields()[1];
		assertTrue(dataPtr.isCompatibleWith(nextField.getType()));	// one is an LLUpType
		
		doGenerate(mod);
	}
	
	@Test 
	public void testGenericTypes3a() throws Exception {
		dumpTypeInfer = true;
		dumpTreeize = true;
		IAstModule mod = doFrontend(
				"List = [T] data {\n" +
				"        node:T;\n"+
				"        next:List<T>^;\n" +	
				"};\n" + 
				"listNextNext = [T] code (list:List<T>) { list.next.next.node };\n"+
				"intList = code (x:Int=>Int) {\n"+
				"  a,b:List<Int>;\n"+
				"  a.node=x;\n"+
				"  a.next=&b;\n"+
				"  b.next=&a;\n"+
				"  listNextNext<Int>(a);\n"+
				"};\n"+
	
				"");
		sanityTest(mod);
		IAstCodeExpr code = (IAstCodeExpr) getMainBodyExpr((IAstDefineStmt) mod.getScope().get("intList").getDefinition());
		IAstAllocStmt alloc = (IAstAllocStmt) code.stmts().getFirst();
		LLDataType data = (LLDataType) alloc.getType();
		assertTrue(data.isComplete() && !data.isGeneric());
		assertEquals(2, data.getInstanceFields().length);
		assertEquals(typeEngine.INT, data.getInstanceFields()[0].getType());
		LLPointerType dataPtr = typeEngine.getPointerType(data);
		LLInstanceField nextField = data.getInstanceFields()[1];
		assertTrue(dataPtr.isCompatibleWith(nextField.getType()));	// one is an LLUpType
		
		doGenerate(mod);
	}
	@Test 
	public void testGenericTypes3b() throws Exception {
		dumpTypeInfer = true;
		dumpTreeize = true;
		IAstModule mod = doFrontend(
				"List = [T, U] data {\n" +
				"        node:T;\n"+
				"        next:List<U,T>^;\n" +		// note: not the same type as parent!
				"};\n" + 
				"\n" +
				"listNextNext = [T,U] code (list:List<T,U>) { list.next.next };\n"+
				"intList = code (x:Int;y:Float=>Float) {\n"+
				"  a:List<Int,Float>;\n"+
				"  b:List<Float,Int>;\n"+
				"  a.next=&b;\n"+
				"  b.next=&a;\n"+
				"  listNextNext(a);\n"+
				"};\n"+
	
				"");
		sanityTest(mod);
		IAstCodeExpr code = (IAstCodeExpr) getMainBodyExpr((IAstDefineStmt) mod.getScope().get("intList").getDefinition());
		IAstAllocStmt alloc = (IAstAllocStmt) code.stmts().getFirst();
		LLDataType data = (LLDataType) alloc.getType();
		assertTrue(data.isComplete() && !data.isGeneric());
		assertEquals(2, data.getInstanceFields().length);
		assertEquals(typeEngine.INT, data.getInstanceFields()[0].getType());
		LLPointerType dataPtr = typeEngine.getPointerType(data);
		LLInstanceField nextField = data.getInstanceFields()[1];
		assertTrue(dataPtr.isCompatibleWith(nextField.getType()));	// one is an LLUpType
		
		doGenerate(mod);
	}
	@Test 
	public void testGenericTypes3c() throws Exception {
		dumpTypeInfer = true;
		dumpTreeize = true;
		IAstModule mod = doFrontend(
				"List = [T, U] data {\n" +
				"        node:T;\n"+
				"        next:List<U>^;\n" +
				"};\n" + 
				"\n" +
				"IntDoubleList = List<Int,Double>;\n"+
				"listNextNext = code (list:IntDoubleList) { list.next.next };\n"+
				"intList = code(x:Int;y:Double) {\n"+
				"   list1 : List<Int, Double>; list2 : List<Double, Int>;\n" +
				"   list1.node = x;\n"+
				"   list2.node = y;\n"+
				"   list1.next = &list2;\n"+
				"   list2.next = &list1;\n"+
				"   listNextNext(list1);\n"+
				"};\n" +
				"");
		sanityTest(mod);
		IAstCodeExpr code = (IAstCodeExpr) getMainBodyExpr((IAstDefineStmt) mod.getScope().get("intList").getDefinition());
		IAstAllocStmt alloc = (IAstAllocStmt) code.stmts().getFirst();
		LLDataType data = (LLDataType) alloc.getType();
		assertTrue(data.isComplete() && !data.isGeneric());
		assertEquals(2, data.getInstanceFields().length);
		assertEquals(typeEngine.INT, data.getInstanceFields()[0].getType());
		LLPointerType dataPtr = typeEngine.getPointerType(data);
		LLInstanceField nextField = data.getInstanceFields()[1];
		assertTrue(dataPtr.isCompatibleWith(nextField.getType()));	// one is an LLUpType
		
		doGenerate(mod);
	}
}


