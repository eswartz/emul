/**
 * 
 */
package org.ejs.eulang.test;

import static junit.framework.Assert.assertEquals;

import org.ejs.eulang.ast.IAstModule;
import org.ejs.eulang.llvm.LLVMGenerator;
import org.junit.Test;

/**
 * @author ejs
 *
 */
public class TestLoopGenerator extends BaseParserTest {

    @Test
    public void testRepeatLoop() throws Exception {
    	IAstModule mod = doFrontend(
    			"testRepeatLoop = code (x) {\n" +
    			"   s := 0;\n"+
    			"   b := 1;\n"+
    			"	repeat x do { s = s + b; b = b + b; s; }\n"+
    			"};\n");
    	
    	LLVMGenerator gen = doGenerate(mod);
    	assertEquals(1, gen.getModule().getSymbolCount());
    }
}


