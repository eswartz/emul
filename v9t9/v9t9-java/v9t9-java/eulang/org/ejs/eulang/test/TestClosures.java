/**
 * 
 */
package org.ejs.eulang.test;

import org.ejs.eulang.ast.IAstModule;
import org.junit.Test;

/**
 * @author ejs
 *
 */
public class TestClosures extends BaseTest {

	@Test
	public void testExplicitClosure() throws Exception {
		dumpLLVMGen = true;
		doLLVMOptimize = true;
		String text=  "   Closure = data { vars : Byte^; func : code(=>nil); };\n" + 
				"    \n" + 
				"    repeatx = code (x : Int; block : Closure^ => nil) {\n" + 
				"        for i in x do block.func{code(vars:Byte^;x:Int=>nil)^} (block.vars, i);\n" + 
				"    };\n" + 
				"\n" + 
				"    foo = code() {\n" + 
				"        p, s := 10, 0;\n" + 
				"        \n" + 
				"        define MyLocals = data { p : Int; s: Int; };\n" + 
				"        myLocals : MyLocals = [ p ];\n" + 
				"        closure : data { vars : MyLocals^; func : code(vars : MyLocals^ => nil); } = [ \n" + 
				"                &myLocals, code(vars : MyLocals^ => nil) { vars.s += vars.p; vars.p--; } \n" + 
				"        ];\n" + 
				"        repeatx(10, (&closure){Closure^});\n" + 
				"        \n" + 
				"        // maybe\n" + 
				"        p, s = myLocals.p, myLocals.s;\n" +
				"	     s;\n"+
				"    };    ";
		IAstModule mod = doFrontend(text);
		doGenerate(mod);
	}
}
