/**
 * 
 */
package org.ejs.eulang.test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import junit.framework.AssertionFailedError;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.ejs.eulang.ITarget;
import org.ejs.eulang.Message;
import org.ejs.eulang.TargetV9t9;
import org.ejs.eulang.ast.IAstCodeExpr;
import org.ejs.eulang.ast.IAstCondExpr;
import org.ejs.eulang.ast.IAstCondList;
import org.ejs.eulang.ast.IAstDefineStmt;
import org.ejs.eulang.ast.IAstExprStmt;
import org.ejs.eulang.ast.IAstModule;
import org.ejs.eulang.ast.IAstStmtListExpr;
import org.ejs.eulang.ext.CommandLauncher;
import org.ejs.eulang.llvm.LLVMGenerator;
import org.ejs.eulang.types.LLType;
import org.junit.Test;

/**
 * @author ejs
 *
 */
public class TestLLVMGenerator extends BaseParserTest {

	private ITarget v9t9Target = new TargetV9t9(typeEngine);
	/**
	 * Generate the module, expecting no errors.
	 * @param mod
	 * @return 
	 */
	private LLVMGenerator doGenerate(IAstModule mod) throws Exception {
		return doGenerate(mod, false);
	}
	/**
	 * @param mod
	 * @return 
	 * @throws Exception 
	 */
	protected LLVMGenerator doGenerate(IAstModule mod, boolean expectErrors) throws Exception {
		//doExpand(mod);
		//doSimplify(mod);
		
		LLVMGenerator generator = new LLVMGenerator(v9t9Target);
		generator.generate(mod);
		
		String text = generator.getText();
		
		List<Message> messages = generator.getMessages();
		for (Message msg : messages)
			System.err.println(msg);
		if (!expectErrors)
			assertEquals("expected no errors: " + catenate(messages), 0, messages.size());
		
		File file = getTempFile("");
		File llfile = new File(file.getAbsolutePath() + ".ll");
		FileOutputStream os = new FileOutputStream(llfile);
		os.write(text.getBytes());
		os.close();
		
		File bcFile = new File(file.getAbsolutePath() + ".bc");
		bcFile.delete();

		File bcOptFile = new File(file.getAbsolutePath() + ".opt.bc");
		bcOptFile.delete();

		File llOptFile = new File(file.getAbsolutePath() + ".opt.ll");
		llOptFile.delete();

		System.out.println(text);
		
		try {
			run("llvm-as", llfile.getAbsolutePath(), "-f", "-o", bcFile.getAbsolutePath());
			run("opt", bcFile.getAbsolutePath(), "-O2", "-f", "-o", bcOptFile.getAbsolutePath());
			run("llvm-dis", bcOptFile.getAbsolutePath(), "-f", "-o", llOptFile.getAbsolutePath());
		} catch (AssertionFailedError e) {
			if (expectErrors)
				return generator;
			else
				throw e;
		}
		
		if (expectErrors)
			assertTrue("expected errors", messages.size() > 0);
		
		return generator;
	}
	/**
	 * @param string
	 * @param absolutePath
	 * @param string2
	 * @param string3
	 * @param absolutePath2
	 * @throws CoreException 
	 */
	private void run(String prog, String... args) throws CoreException {
		CommandLauncher launcher = new CommandLauncher();
		launcher.showCommand(true);
		launcher.execute(new Path(prog), 
				args,
				null,
				null,
				null);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream err = new ByteArrayOutputStream();
		int exit = launcher.waitAndRead(out, err);
		
		System.out.print(out.toString());
		System.err.print(err.toString());
		assertEquals(out.toString() + err.toString(), 0, exit);
	}

	/**
	 * @return
	 * @throws IOException 
	 */
	private File getTempFile(String ext) throws IOException {
		String name = "test";
		StackTraceElement[] stackTrace = new Exception().getStackTrace();
		for (StackTraceElement e : stackTrace) {
			if (e.getMethodName().startsWith("test")) {
				name = e.getMethodName();
				break;
			}
		}
		return new File("/tmp/" + name + ext);
	}
	
	@Test
	public void testSimple() throws Exception {
		IAstModule mod = doFrontend("FOO = 3;\n"+
				"helper = code (x : Int => Int) { -x; };\n"+
				"main := code (p, q) {\n" +
				"	x := helper(10 * q);\n"+
				"   x = x + x;\n"+
				"   select [ x > q then -FOO else 1+p ];\n"+
				"};\n");
		
		doGenerate(mod);
	}
	

	
	@Test
    public void testPointers3() throws Exception {
		 dumpTypeInfer = true;
    	IAstModule mod = doFrontend(
    			" refSwap_testPointers3 := code (x : Int&, y : Int& => null) {\n" +
    			" t : Int = x;\n"+
    			" x = y;\n"+
    			" y = t;\n"+
    	"};\n");
    	doGenerate(mod);

    }
	
	/*
	
	@Test
    public void testPointers4() throws Exception {
		 dumpTypeInfer = true;
    	IAstModule mod = doFrontend(
    			" genericSwap_testPointers4 := code (@x, @y => null) {\n" +
    			//" x = x + 1; y = y + 1; x = x + 2; y = y - 4; x = x - 4;\n" +
    			" t : Int = x;\n"+
    			" x = y;\n"+
    			" y = t;\n"+
    	"};\n");
    	doGenerate(mod);

    }

	@Test
    public void testPointers2() throws Exception {
		 dumpTypeInfer = true;
    	IAstModule mod = doFrontend(
    			" swap_testPointers2 := code (x : Int&, @y : Int => null) {\n" +
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
    			" swap_testPointers2b := code (x : Int&, @y : Int& => null) {\n" +
    			" t : Int = x;\n"+
    			" x = y;\n"+
    			" y = t;\n"+
    	"};\n");
    	doGenerate(mod);

    }
    */
	
	@Test
	public void testBinOps() throws Exception {
		dumpTypeInfer = true;
		IAstModule mod = doFrontend("testBinOps = code { x:=1*2/3%4%%45+5-6>>7<<8>>>85&9^10|11<12>13<=14>=15==16!=17 and Bool(18) or Bool(19); };");
		doGenerate(mod);
	}
	

	@Test
	public void testShortCircuitAndOr() throws Exception {
		dumpTypeInfer = true;
		IAstModule mod = doFrontend("testShortCircuitAndOr = code (x,y:Int&,z => Int){\n" +
				"select [ x > y and y > z then y " +
				"|| x > z and z > y then z" +
				"|| y > x and x > z then x " +
				"|| x == y or z == x then x+y+z " +
				"|| else x-y-z ] };");
		doGenerate(mod);
	}
	
	@Test
    public void testTuples4() throws Exception {
    	IAstModule mod = doFrontend("swap = code (x,y => (Int, Int)) { (y,x); };\n" +
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
    	IAstModule mod = doFrontend("add = code (x,y) { x+y };\n" +
    			"testGenerics0b = code (a:Int,b:Int) { add(a,b) + add(10.0,b);  }; \n");
    	LLVMGenerator g = doGenerate(mod);
    	assertEquals(3, g.getModule().getSymbolCount());
    }
  
	@Test
  	public void testCasting1() throws Exception {
  		IAstModule mod = doFrontend(
			"testCasting1 = code (f:Float, d:Double, i:Int, b:Byte) {\n"+
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
			"testTypeList1 = code (a:Float,b:Double) { floor(a)+floor(b) }; \n");
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
 			"testTypeList1 = code (a:Float,b:Double) { floor(a)+floor(b)*floor(a)*floor(b) }; \n");
     	LLVMGenerator g = doGenerate(mod);
    	assertEquals(3, g.getModule().getSymbolCount());
     }

  	 @Test
     public void testRecursion() throws Exception {
     	IAstModule mod = doFrontend(
     	"   factorial = code (x) { select [ x > 1 then x * factorial(x-1) else 1 ] };\n" + 
     	"  ");
     	LLVMGenerator g = doGenerate(mod);
    	assertEquals(1, g.getModule().getSymbolCount());
     	
     }
  	 
  	 @Test
  	 public void testOverloading() throws Exception {
  		 IAstModule mod = doFrontend(
  				 "    util = [ code(x:Int, y:Int, z:Int) { x*y-z },\n" + 
  				 "             code(x, y) { util(x, y, 0) }\n" + 
  				 "            ];\n" +
  				 "func = code(x:Int,y:Float => Float) { util(x,y) };\n");
  		 LLVMGenerator g = doGenerate(mod);
  		 assertEquals(3, g.getModule().getSymbolCount());
  		 
  		 
  	 }
  	 
  	 @Test
     public void testOverloadingMacro() throws Exception {
  		 IAstModule mod = doFrontend(
  				 "    util = [ code(x, y, z ) { x*y-z },\n" + 
  				 "             macro (x, y) { util(x, y, 0) }\n" + 
  				 "            ];\n" +
  				 "func = code(x:Int,y:Float => Float) { util(x,y) };\n");
  		 LLVMGenerator g = doGenerate(mod);
  		 assertEquals(2, g.getModule().getSymbolCount());
  		 
  	 }
  	 
  	@Test
    public void testCondStar3() throws Exception {
  		// implicit else counts as 0 in the inferred type
    	IAstModule mod = doFrontend(
    		" testCondStar3 = code (t) { \n" +
    		"select [ 1>t then 15 ];\n" +
    		"		; };\n");
    	LLVMGenerator g = doGenerate(mod);
 		 assertEquals(1, g.getModule().getSymbolCount());
 		 
    }
  	
    @Test
    public void testWhileLoop() throws Exception {
    	dumpTypeInfer = true;
    	IAstModule mod = doFrontend(
    			"if = macro ( test:Bool, macro mthen: code, macro melse: code) { select [\n" +
    			"	 test then mthen() || false then false || else melse() ] };\n"+
    			"while = macro ( macro test:code, macro body : code) {\n"+
    			"    @loop: select test() then { body(); @loop } else @loop;\n"+
    			"};\n"+
    			// TODO: forcing ':=' to avoid situation where the code block is marked generic
    			// before we have expanded (suppressing genericization before expansion
    			// causes other failures)
    			"testWhileLoop := code (t, x : Int, y : Float) {\n" +
    			"   while(x > t, { y = y/2; x = x-1; } );\n"+
    			 "	y;\n"+
    			"};");
    	LLVMGenerator g = doGenerate(mod);
		 assertEquals(1, g.getModule().getSymbolCount());
    }

}
