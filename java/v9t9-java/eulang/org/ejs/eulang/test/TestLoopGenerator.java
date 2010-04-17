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
    
    @Test
    public void testWhile() throws Exception {
    	dumpTypeInfer = true;
    	IAstModule mod = doFrontend(
    			"testWhile = code (x) {\n" +
    			"   s := 0;\n"+
    			"   b := 1;\n"+
    			"	while b < x do { s = s + b; b = b + 1; s; }\n"+
    			"};\n");
    	
    	LLVMGenerator gen = doGenerate(mod);
    	assertEquals(1, gen.getModule().getSymbolCount());
    }
    
    @Test
    public void testDoWhile() throws Exception {
    	dumpTypeInfer = true;
    	IAstModule mod = doFrontend(
    			"testDoWhile = code (x) {\n" +
    			"   s := 0;\n"+
    			"   b := 1;\n"+
    			"	do { s = s + b; b = b + 1; s; } while b < x\n"+
    			"};\n");
    	
    	LLVMGenerator gen = doGenerate(mod);
    	assertEquals(1, gen.getModule().getSymbolCount());
    }
}


