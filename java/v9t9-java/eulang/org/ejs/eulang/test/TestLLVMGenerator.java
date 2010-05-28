/**
 * 
 */
package org.ejs.eulang.test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import org.ejs.eulang.ast.IAstModule;
import org.ejs.eulang.llvm.LLVMGenerator;
import org.ejs.eulang.llvm.ops.LLSymbolOp;
import org.ejs.eulang.symbols.ISymbol;
import org.junit.Test;

/**
 * @author ejs
 *
 */
public class TestLLVMGenerator extends BaseTest {


	@Test
	public void testConsts() throws Exception {
		dumpLLVMGen = true;
		IAstModule mod = doFrontend(
				"main := code () { x:=123; x=456; };\n");
		
		LLVMGenerator gen  = doGenerate(mod);
		assertFoundInUnoptimizedText("123",gen);
		assertFoundInUnoptimizedText("456",gen);
	}
	
	@Test
	public void testSimple() throws Exception {
		dumpLLVMGen = true;
		IAstModule mod = doFrontend("FOO = 3;\n"+
				"helper = code (x : Int => Int) { -x; };\n"+
				"main := code (p, q) {\n" +
				"	x := helper(10 * q);\n"+
				"   x = x + x;\n"+
				"   if x > q then -FOO else 1+p ;\n"+
				"};\n");
		
		doGenerate(mod);
	}
	
	/*
	
	@Test
    public void testPointers3() throws Exception {
		 dumpTypeInfer = true;
    	IAstModule mod = doFrontend(
    			" refSwap_testPointers3 := code (x : Int&; y : Int& => nil) {\n" +
    			" t : Int = x;\n"+
    			" x = y;\n"+
    			" y = t;\n"+
    	"};\n");
    	doGenerate(mod);

    }
	*/
	
	@Test
    public void testPointers4() throws Exception {
    	IAstModule mod = doFrontend(
    			" genericSwap_testPointers4 := code (@x, y : Int => nil) {\n" +
    			//" x = x + 1; y = y + 1; x = x + 2; y = y - 4; x = x - 4;\n" +
    			" t : Int = x;\n"+
    			" x = y;\n"+
    			" y = t;\n"+
    	"};\n");
    	doGenerate(mod);

    }
	
	/*

	@Test
    public void testPointers2() throws Exception {
		 dumpTypeInfer = true;
    	IAstModule mod = doFrontend(
    			" swap_testPointers2 := code (x : Int&; @y : Int => null) {\n" +
    			" t : Int = x;\n"+
    			" x = y;\n"+
    			" y = t;\n"+
    	"};\n");
    	doGenerate(mod);

    }


	@Test
    public void testPointers2b() throws Exception {
		 dumpTypeInfer = true;
    	IAstModule mod = doFrontend(
    			" swap_testPointers2b := code (x : Int&; @y : Int& => null) {\n" +
    			" t : Int = x;\n"+
    			" x = y;\n"+
    			" y = t;\n"+
    	"};\n");
    	doGenerate(mod);

    }
    */
	
	@Test
	public void testBinOps() throws Exception {
		IAstModule mod = doFrontend("testBinOps = code { x:=1*2/3%4\\99+\\45+5-6>>7>>|4<<|75<<8+>>85&9 ~" + 
				"10|11+<-11<12+>-12>13+<=-33<=14+>=0>=15==16!=17 and Bool(18) or Bool(19); };");
		doGenerate(mod);
	}
	

	@Test
	public void testShortCircuitAndOr() throws Exception {
		IAstModule mod = doFrontend("testShortCircuitAndOr = code (x;y:Int;z => Int){\n" +
				"if  x > y and y > z then y " +
				"elif x > z and z > y then z " +
				"elif y > x and x > z then x " +
				"elif x == y or z == x then x+y+z " +
				"else x-y-z };");
		doGenerate(mod);
	}
	
	@Test
    public void testTuples4() throws Exception {
    	IAstModule mod = doFrontend("swap = [] code (x,y) { (y,x); };\n" +
    			"testTuples4 = code (a,b) { (a, b) = swap(4, 5); }; \n");
    	LLVMGenerator g = doGenerate(mod);
    	assertEquals(2, g.getModule().getSymbolCount());
    }
	@Test
    public void testTuples4b() throws Exception {
    	IAstModule mod = doFrontend("swap = code (x,y,z => (Int, Int, Int)) { (y,z,x); };\n" +
    			"testTuples4b = code (a,b) { (x, o, y) := swap(a+b, a-b, b); (a*x, y*b); }; \n");
    	LLVMGenerator g = doGenerate(mod);
    	assertEquals(2, g.getModule().getSymbolCount());
    }
	
	@Test
	public void testGenerics0b() throws Exception {
		IAstModule mod = doFrontend("add = [] code (x,y) { x+y };\n" +
		"testGenerics0b = code (a:Int;b:Int) { add(a,b) + add(10.0,b);  }; \n");
		LLVMGenerator g = doGenerate(mod);
		assertEquals(3, g.getModule().getSymbolCount());
	}
  
	@Test
  	public void testCasting1() throws Exception {
  		IAstModule mod = doFrontend(
			"testCasting1 = code (f:Float; d:Double; i:Int; b:Byte) {\n"+
			"f = d; f = i; f = b;\n"+
			"d = f; d = i; d = b;\n" +
			"i = f; i = d; i = b;\n" +
			"b = f; b = d; b = i;\n" +
			"};\n");
  		LLVMGenerator g = doGenerate(mod);
    	assertEquals(1, g.getModule().getSymbolCount());
  }
	
  	@Test
  	public void testTypeList1() throws Exception {
  		// be sure the right symbol is used for each reference
  		IAstModule mod = doFrontend("floor = [\n"+
  			"	code (x:Float) { x - x%1.0 },\n" +
  			"   code (x:Double) { x - x%1.0 }\n " +
  			"];\n"+
			"testTypeList1 = code (a:Float;b:Double) { floor(a)+floor(b) }; \n");
  		LLVMGenerator g = doGenerate(mod);
    	assertEquals(3, g.getModule().getSymbolCount());
  }
  	
  	 @Test
     public void testTypeList2() throws Exception {
  		 // make sure we don't generate more than one instance per type
     	IAstModule mod = doFrontend("floor = [\n"+
     			"	code (x:Float) { x - x%1.0 },\n" +
     			"   code (x:Double) { x - x%1.0 }\n " +
     			"];\n"+
 			"testTypeList1 = code (a:Float;b:Double) { floor(a)+floor(b)*floor(a)*floor(b) }; \n");
     	LLVMGenerator g = doGenerate(mod);
    	assertEquals(3, g.getModule().getSymbolCount());
     }

  	 @Test
     public void testRecursion() throws Exception {
     	IAstModule mod = doFrontend(
     	"   factorial = code (x) { if x > 1 then x * factorial(x-1) else 1 };\n" + 
     	"  ");
     	LLVMGenerator g = doGenerate(mod);
    	assertEquals(1, g.getModule().getSymbolCount());
     	
     }
  	 
  	 @Test
  	 public void testOverloading() throws Exception {
  		 // util(x,y) should be Int,Int=>Int too
  		 IAstModule mod = doFrontend(
  				 "    util = [ code(x:Int; y:Int; z:Int) { x*y-z },\n" + 
  				 "             code(x, y) { util(x, y, 0) }\n" + 
  				 "            ];\n" +
  				 "func = code(x:Int;y:Float => Float) { util(x,y) };\n");
  		 LLVMGenerator g = doGenerate(mod);
  		 assertEquals(3, g.getModule().getSymbolCount());
  		 
  		 
  	 }
  	 
  	 @Test
     public void testOverloadingMacro() throws Exception {
  		 IAstModule mod = doFrontend(
  				 "    util = [] [ code(x, y, z ) { x*y-z },\n" + 
  				 "             macro (x, y) { util(x, y, 0) }\n" + 
  				 "            ];\n" +
  				 "func = code(x:Int;y:Float => Float) { util(x,y) };\n");
  		 LLVMGenerator g = doGenerate(mod);
  		 assertEquals(2, g.getModule().getSymbolCount());
  		 
  	 }
  	 
  	@Test
    public void testCondStar3() throws Exception {
  		// nil counts as 0 in the inferred type
    	IAstModule mod = doFrontend(
    		" testCondStar3 = code (t) { \n" +
    		"if 1>t then 15 else nil;\n" +
    		"};\n");
    	LLVMGenerator g = doGenerate(mod);
 		 assertEquals(1, g.getModule().getSymbolCount());
 		 
    }
  	
    @Test
    public void testWhileLoop() throws Exception {
    	IAstModule mod = doFrontend(
    			"wwhile = macro ( macro test:code; macro body : code) {\n"+
    			"    @loop: if test() then { body(); goto loop } fi;\n"+
    			"};\n"+
    			"testWhileLoop = code (t; x : Int; y : Float => Void) {\n" +
    			"   wwhile(x > t, { y = y/2; x = x-1; } );\n"+
    			"};");
    	LLVMGenerator g = doGenerate(mod);
		 assertEquals(1, g.getModule().getSymbolCount());
    }
    @Test
    public void testDoWhile() throws Exception {
    	IAstModule mod = doFrontend(
    			"doWhile = macro ( macro body : code; macro test:code) {\n"+
    			"    @loop: body(); goto loop if (not test()) ;\n"+
    			"};\n"+
    			"testDoWhile = code (t; x : Int; y : Float) {\n" +
    			"   doWhile(y = y/2, { x = x - 1; x > t }); y ; \n"+
    			"};");
    	LLVMGenerator g = doGenerate(mod);
		 assertEquals(1, g.getModule().getSymbolCount());
    }
    
    @Test
    public void testBlockScopes() throws Exception {
    	IAstModule mod = doFrontend(
    			"testBlockScopes = code (t; x : Int; y : Float) {\n" +
    			"  if t then { z := Float(x); z = z * 8 } else { z := y; };"+
    			"};");
    	mod = mod.copy(null);
    	LLVMGenerator g = doGenerate(mod);
    	assertEquals(1, g.getModule().getSymbolCount());
    	
    }
    
    
    @Test
    public void testCodeBlockMultiNamedVars1() throws Exception  {
    	IAstModule mod = doFrontend("maker = code(u:Int) { u*u*u*u }; " +
    			"\n" +
    			"testCodeBlockMultiNamedVars1 = code() {\n" +
    			"	a, b := maker(4);\n" +
    			"	c, d := +maker(4);\n" +
    			" };");
    	LLVMGenerator g = doGenerate(mod);
    	assertEquals(2, g.getModule().getSymbolCount());
    }
    @Test
    public void testCodeBlockMultiNamedVars2() throws Exception  {
    	IAstModule mod = doFrontend("maker = code(u:Int) { u*u*u*u }; " +
    			"\n" +"testCodeBlockMultiNamedVars2 = code() {\n" +
    					" a, b := maker(4), maker(9);\n" +
    					" };");
    	LLVMGenerator g = doGenerate(mod);
    	assertEquals(2, g.getModule().getSymbolCount());
    }
    @Test
    public void testCodeBlockMultiAssigns1() throws Exception  {
    	IAstModule mod = doFrontend("maker = code(u:Int) { -u }; " +
    			"\n" +
    			"testCodeBlockMultiAssigns1 = code(a, b) {\n" +
    			"	a, b = maker(a+b);\n" +
    			"	a, b = +maker(a+b);\n" +
    			" };");
    	LLVMGenerator g = doGenerate(mod);
    	assertEquals(2, g.getModule().getSymbolCount());
    }
    @Test
    public void testCodeBlockMultiAssigns2() throws Exception  {
    	IAstModule mod = doFrontend("maker = code(u:Int) { u*u*u*u }; " +
    			"\n" +
    			"testCodeBlockMultiAssigns2 = code(a, b) {\n" +
    			"	a, b = maker(b), maker(a);\n" +
    			"   a, b = b, a;\n"+
    			" };");
    	LLVMGenerator g = doGenerate(mod);
    	assertEquals(2, g.getModule().getSymbolCount());
    }
    @Test
    public void testIncsDecs() throws Exception  {
    	dumpLLVMGen = true;
    	IAstModule mod = doFrontend("maker1 = code(u,v:Int) { u++ * ++v }; " +
    			"maker2 = code(u,v:Int) { u-- * --v }; " +
    			"\n" +
    			"testCodeBlockMultiAssigns2 = code(a, b) {\n" +
    			"	a, b = maker1(b,a), maker2(a,b);\n" +
    			"   a/b;\n"+
    			" };");
    	LLVMGenerator g = doGenerate(mod);
    	assertEquals(3, g.getModule().getSymbolCount());
    }
    
    @Test
    public void testGlobal1() throws Exception {
    	IAstModule mod = doFrontend("bat := 10;\n" +
    			"foo = code(x,y:Int => nil) { bat = x+y };\n"+
    			"");
    	
    	LLVMGenerator g = doGenerate(mod);
    	ISymbol batSym = g.getModule().getGlobalScope().get("bat");
		assertNotNull(batSym);
		
		LLSymbolOp op = new LLSymbolOp(batSym);
		assertEquals("@bat", op.toString());
		
    }
    

	@Test
	public void testTrunc16_to_8_1_Mem() throws Exception {
		dumpLLVMGen = true;
		IAstModule mod = doFrontend("x := 11; foo = code( ) { Byte(x) };\n");
    	doGenerate(mod);
	}
	
	@Test
	public void testTrunc16_to_8_1_Mem_to_Mem() throws Exception {
		IAstModule mod = doFrontend("x := 11; foo = code( ) { x = Byte(x) };\n");
		LLVMGenerator g = doGenerate(mod);
		assertFoundInUnoptimizedText("trunc", g);
	}

	@Test
    public void testAssignOps() throws Exception {
    	IAstModule mod = doFrontend("testAssignOps = code { x:=1;" +
    			//"x+=x-=x*=x/=x+/=x%=x+%=x>>=x<<=x+>>=2;\n"+
    			"x+=(x-=(x*=x/=x+/=x%=(x\\=x>>=(x<<=x+>>=x\\=2))));\n"+
    			"x|=x~=x&=111;\n"+
    			"};");
    	doGenerate(mod);
    }
	

    @Test
    public void testRetAddr2() throws Exception {
    	dumpLLVMGen = true;
    	// make sure global var is addressed with the right type
    	IAstModule mod = doFrontend(
    			"x : Byte;\n"+
    			"testRetAddr2 = code() {\n"+
    			" y : Byte^;\n"+
    			" y = &x;\n"+
    			"};\n"+
    	"");
    	doGenerate(mod);
    }
    

    /** Non-canonical method call, through instance */
    /** Test that we export defines inside data */
    @Test
    public void testInnerCode2() throws Exception {
    	dumpLLVMGen = true;
    	IAstModule mod = doFrontend(
    			"Complex = data {\n"+
    			"  a,b,c:Byte;\n"+
    			"  Inner = data {\n"+
    			"    d1,d2:Float;\n"+
    			"    p : Complex^;\n"+
    			"  };\n"+
    			"  summer = code(this:Complex^) {\n"+
    			"    this.d.d1 + this.d.p.c;\n"+
    			"  };\n"+
    			"  d : Inner^;\n"+
    			" };\n"+
    			"testInnerData1 = code() {\n"+
    			"  c : Complex;\n" +
    			"  c.d.p.summer(&c);\n" +
    			"};\n"+
    	"");
    	doGenerate(mod);
    	
    }

    /** Non-canonical symbol references */
    /** Test that we export defines inside data */
    /** Test that we export nested scopes */
    /** Test that we give unique symbols */
    @Test
    public void testInnerCode3() throws Exception {
    	dumpLLVMGen = true;
    	IAstModule mod = doFrontend(
    			"Nest1 = {\n"+
    			"  a=code(=>nil) { };\n"+
    			"  Nest2 = data {\n"+
    			"    a=code(=>nil) { :a() };\n"+
    			"    Inner = data {\n"+
    			"	   p : Nest2^;\n"+
    			"      a=code(=>nil) { ::a(); :a(); };\n"+
    			"      Nest3 = {\n"+
    			"        a=code(=>nil) { :::a(); ::a(); :a(); };\n"+
    			"      };\n"+
    			"    };\n"+
    			"  };\n"+
    			" };\n"+
    			"testInnerData1 = code() {\n"+
    			"  c : Nest1.Nest2.Inner;\n" +
    			"  c.a();\n"+
    			"  c.Nest3.a();\n"+
    			"};\n"+
    	"");
    	doGenerate(mod);
    	
    }
    
    /** Test lots of uniquifying */
    @Test
    public void testInnerTypes1() throws Exception {
    	dumpLLVMGen = true;
    	IAstModule mod = doFrontend(
    			"Nest1 = data {\n"+
    			"  x:Int;\n"+
    			"  Nest1 = data {\n"+
    			"    y:Byte;\n"+
    			"    Inner = data {\n"+
    			"      Nest1 = data {\n"+
    			"		 z:Float;\n"+
    			"      };\n"+
    			"    };\n"+
    			"  };\n"+
    			" };\n"+
    			"func1 = [ code(a:Nest1 => nil) {},\n"+
    			"	code(a:Nest1.Nest1 => nil) {},\n"+
    			"   code(a:Nest1.Nest1.Inner.Nest1 => nil) {} ];\n"+
    			"testInnerTypes1 = code() {\n"+
    			"  c : Nest1;\n" +
    			"  d : Nest1.Nest1;\n" +
    			"  e : Nest1.Nest1.Inner.Nest1;\n" +
    			"  func1(c);\n"+
    			"  func1(d);\n"+
    			"  func1(e);\n"+
    			"};\n"+
    	"");
    	LLVMGenerator gen = doGenerate(mod);
    	
    	assertMatchText("call.*Nest1_", gen.getUnoptimizedText());  
    	assertMatchText("call.*Nest1.Nest1_", gen.getUnoptimizedText());  
    	assertMatchText("call.*Nest1.Nest1.Inner.Nest1_", gen.getUnoptimizedText());  
    }

    @Test
    public void testArrayAccess2() throws Exception {
    	dumpTreeize = true;
		dumpLLVMGen = true;
		IAstModule mod = doFrontend("foo = code(x:Int[10]^) { (x-1)[2] };\n");
		LLVMGenerator gen = doGenerate(mod);
		assertMatchText("getelementptr.*, %Int -1", gen.getUnoptimizedText());

    }
}
