
package org.ejs.eulang.test;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.ejs.eulang.ast.DumpAST;
import org.ejs.eulang.ast.ExpandAST;
import org.ejs.eulang.ast.IAstAssignStmt;
import org.ejs.eulang.ast.IAstBinExpr;
import org.ejs.eulang.ast.IAstCodeExpr;
import org.ejs.eulang.ast.IAstDefineStmt;
import org.ejs.eulang.ast.IAstIntLitExpr;
import org.ejs.eulang.ast.IAstModule;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstUnaryExpr;
import org.ejs.eulang.ast.IOperation;
import org.ejs.eulang.ast.Message;
import org.junit.Test;

/**
 * Test that we can inject macro calls.
 * 
 */
public class TestMacroCall extends BaseParserTest {
	
	protected IAstNode doExpand(IAstNode node) {
		ExpandAST expand = new ExpandAST();
		
		for (int passes = 1; passes < 256; passes++) {
			List<Message> messages = new ArrayList<Message>();
			boolean changed = expand.expand(messages, node);
			
			if (changed) {
				System.out.println("After expansion pass " + passes + ":");
				DumpAST dump = new DumpAST(System.out);
				node.accept(dump);
				
				for (Message msg : messages)
					System.err.println(msg);
				assertEquals(0, messages.size());
			} else {
				break;
			}
		}
		return node;
	}
	
	@Test
    public void testSimple1() throws Exception {
    	IAstModule mod = treeize(
    			"\n" + 
"if = macro ( macro test : code( => Bool ), macro then : code, macro else : code = code() {} ) {\n" + 
    			"    goto lelse, !test();\n" + 
    			"    then();\n" + 
    			"    goto exit;\n" + 
    			"@lelse:\n" + 
    			"    else();\n" + 
    			"@exit:\n" + 
    			"};\n"+
    			"testSimple1 = code (t, x, y) {\n" +
    			"   return if(t > 10, x + 1, x*t*y);\n"+
    			"};");
    	sanityTest(mod);

    	IAstDefineStmt def = (IAstDefineStmt) mod.getScope().getNode("testSimple1");
    	IAstDefineStmt defPrime = (IAstDefineStmt) doExpand(def);
    	sanityTest(defPrime);
    	
    }
}


