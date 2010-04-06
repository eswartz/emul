/**
 * 
 */
package org.ejs.eulang.test;

import static junit.framework.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Stack;

import junit.framework.Assert;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.ejs.eulang.ITarget;
import org.ejs.eulang.Message;
import org.ejs.eulang.TargetV9t9;
import org.ejs.eulang.ast.DumpAST;
import org.ejs.eulang.ast.IAstAllocStmt;
import org.ejs.eulang.ast.IAstAssignStmt;
import org.ejs.eulang.ast.IAstCodeExpr;
import org.ejs.eulang.ast.IAstDefineStmt;
import org.ejs.eulang.ast.IAstModule;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ext.CommandLauncher;
import org.ejs.eulang.llvm.LLVMGenerator;
import org.ejs.eulang.types.LLType;
import org.junit.Test;

/**
 * @author ejs
 *
 */
public class TestLLVMGenerator extends BaseParserTest {

	private ITarget v9t9Target = new TargetV9t9();
	
	protected IAstModule doFrontend(String text) throws Exception {
		IAstModule mod = treeize(text);
    	sanityTest(mod);
    	IAstModule expanded = (IAstModule) doExpand(mod);
    	//doTypeInfer(expanded);
    	doSimplify(expanded);
    	
    	System.err.flush();
		System.out.println("After frontend:");
		DumpAST dump = new DumpAST(System.out);
		expanded.accept(dump);
		
    	return expanded;
	}
	
	/**
	 * @param mod
	 */
	private void doGenerate(IAstModule mod) throws Exception {
		doGenerate(mod, false);
	}
	/**
	 * @param mod
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	protected void doGenerate(IAstModule mod, boolean expectErrors) throws Exception {
		doExpand(mod);
		doSimplify(mod);
		
		LLVMGenerator generator = new LLVMGenerator(v9t9Target);
		generator.generate(mod);
		
		String text = generator.getText();
		
		List<Message> messages = generator.getMessages();
		for (Message msg : messages)
			System.err.println(msg);
		if (!expectErrors)
			assertEquals("expected no errors: " + catenate(messages), 0, messages.size());
		else
			assertTrue("expected errors", messages.size() > 0);
		
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
		
		run("llvm-as", llfile.getAbsolutePath(), "-f", "-o", bcFile.getAbsolutePath());
		
		run("opt", bcFile.getAbsolutePath(), "-O2", "-f", "-o", bcOptFile.getAbsolutePath());
		run("llvm-dis", bcOptFile.getAbsolutePath(), "-f", "-o", llOptFile.getAbsolutePath());
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
    public void testPointers4() throws Exception {
		 dumpTypeInfer = true;
    	IAstModule mod = treeize(
    			" genericSwap_testPointers4 := code (@x, @y => null) {\n" +
    			" t : Int = x;\n"+
    			" x = y;\n"+
    			" y = t;\n"+
    	"};\n");
    	doGenerate(mod);

    }
	

}
