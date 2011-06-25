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
public class TestLoopGenerator extends BaseTest {

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
    public void testRepeatLoopBreak() throws Exception {
    	IAstModule mod = doFrontend(
    			"testRepeatLoopBreak = code (x) {\n" +
    			"   s := 0;\n"+
    			"   b := 1;\n"+
    			"	repeat x do { s, b = s + b, b + b;\n" +
    			"  		if s > 100 then break s else s }\n"+
    			"};\n");
    	
    	LLVMGenerator gen = doGenerate(mod);
    	assertEquals(1, gen.getModule().getSymbolCount());
    }

    @Test
    public void testRepeatLoopBreak2() throws Exception {
    	dumpLLVMGen = true;
    	IAstModule mod = doFrontend(
    			"testRepeatLoopBreak = code (x) {\n" +
    			"   s := 0;\n"+
    			"   b := 1;\n"+
    			"	repeat x do { s, b += b, b;\n" +
    			"  		if s > 100 then break s else s }\n"+
    			"};\n");
    	
    	LLVMGenerator gen = doGenerate(mod);
    	assertEquals(1, gen.getModule().getSymbolCount());
    }

    @Test
    public void testRepeatLoopBreak3() throws Exception {
    	dumpLLVMGen = true;
    	IAstModule mod = doFrontend(
    			"testRepeatLoopBreak = code (x) {\n" +
    			"   s := 0;\n"+
    			"   b := 1;\n"+
    			"	repeat x do { s, b += b;\n" +
    			"  		if s > 100 then break s else s }\n"+
    			"};\n");
    	
    	LLVMGenerator gen = doGenerate(mod);
    	assertEquals(1, gen.getModule().getSymbolCount());
    }
    @Test
    public void testWhile() throws Exception {
    	IAstModule mod = doFrontend(
    			"testWhile = code (x) {\n" +
    			"   s := 0;\n"+
    			"   b := 1;\n"+
    			"	while b < x do { s, b = s + b, b + 1;  }\n"+
    			"};\n");
    	
    	LLVMGenerator gen = doGenerate(mod);
    	assertEquals(1, gen.getModule().getSymbolCount());
    }
    
    @Test
    public void testDoWhile() throws Exception {
    	IAstModule mod = doFrontend(
    			"testDoWhile = code (x) {\n" +
    			"   s := 0;\n"+
    			"   b := 1;\n"+
    			"	do { s, b = s + b, b + 1; } while b < x\n"+
    			"};\n");
    	
    	LLVMGenerator gen = doGenerate(mod);
    	assertEquals(1, gen.getModule().getSymbolCount());
    }
    
    @Test
    public void testForCount() throws Exception {
    	IAstModule mod = doFrontend(
    			"testForCount = code (cnt) {\n" +
    			"   s := 1.0;\n"+
    			"	for x in cnt do s = s + x;\n"+
    			"};\n");
    	
    	LLVMGenerator gen = doGenerate(mod);
    	assertEquals(1, gen.getModule().getSymbolCount());
    	assertFoundInUnoptimizedText("icmp uge", gen);
    	assertMatchText("add %Int .*, 1", gen.getUnoptimizedText());
    }
    @Test
    public void testForCountDown() throws Exception {
    	IAstModule mod = doFrontend(
    			"testForCountDown = code (cnt) {\n" +
    			"   s := 1.0;\n"+
    			"	for x by -1 in cnt do s = s + x;\n"+
    			"};\n");
    	
    	LLVMGenerator gen = doGenerate(mod);
    	assertEquals(1, gen.getModule().getSymbolCount());
    	assertFoundInUnoptimizedText("icmp slt", gen);
    	assertMatchText("sub %Int .*, 1", gen.getUnoptimizedText());
    }
    @Test
    public void testForCount2() throws Exception {
    	IAstModule mod = doFrontend(
    			"testForCount2 = code (cnt) {\n" +
    			"   x := 1.0;\n"+
    			"	for x and y in cnt do :x = :x + x * y;\n"+
    			"};\n");
    	
    	LLVMGenerator gen = doGenerate(mod);
    	assertEquals(1, gen.getModule().getSymbolCount());
    	assertFoundInUnoptimizedText("icmp uge", gen);
    	assertMatchText("add %Int .*, 2", gen.getUnoptimizedText());
    	assertMatchText("store %Int 0,.*\\.x", gen.getUnoptimizedText());
    	assertMatchText("store %Int 1,.*\\.y", gen.getUnoptimizedText());
    }
    @Test
    public void testForCount3() throws Exception {
    	IAstModule mod = doFrontend(
    			"testForCount2 = code (cnt) {\n" +
    			"   x := 1.0;\n"+
    			"	for x and y by 10 in cnt do :x = :x + x * y;\n"+
    			"};\n");
    	
    	LLVMGenerator gen = doGenerate(mod);
    	assertEquals(1, gen.getModule().getSymbolCount());
    	assertMatchText("store %Int 0,.*\\.x", gen.getUnoptimizedText());
    	assertMatchText("store %Int 1,.*\\.y", gen.getUnoptimizedText());
    	assertFoundInUnoptimizedText("icmp uge", gen);
    	assertMatchText("add %Int .*, 10", gen.getUnoptimizedText());
    }
    @Test
    public void testForCountDown2() throws Exception {
    	IAstModule mod = doFrontend(
    			"testForCountDown2 = code (cnt) {\n" +
    			"   x := 1;\n"+
    			"	for x and y by -1 in cnt do :x = :x + x * y;\n"+
    			"};\n");
    	
    	LLVMGenerator gen = doGenerate(mod);
    	assertEquals(1, gen.getModule().getSymbolCount());
    	assertMatchText("icmp slt .*, 0", gen.getUnoptimizedText());
    	assertMatchText("add %Int %1.*, -1", gen.getUnoptimizedText());	// late temp
    }
    
    @Test
    public void testForCountDown3() throws Exception {
    	dumpLLVMGen = true;
    	IAstModule mod = doFrontend(
    			"testForCountDown3 = code (cnt, step) {\n" +
    			"   s := 1.;\n"+
    			"	for x and y by -step in cnt do s = s + x * y;\n"+
    			"};\n");
    	
    	LLVMGenerator gen = doGenerate(mod);
    	assertEquals(1, gen.getModule().getSymbolCount());
    	assertMatchText("icmp slt .*, 0", gen.getUnoptimizedText());
    	assertMatchText("add %Int %1.*, %2", gen.getUnoptimizedText());		// late temp (not %0!)
    }
}


