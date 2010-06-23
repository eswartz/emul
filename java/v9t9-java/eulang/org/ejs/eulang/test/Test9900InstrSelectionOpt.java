/**
 * 
 */
package org.ejs.eulang.test;

import static junit.framework.Assert.*;

import org.ejs.eulang.llvm.LLModule;
import org.ejs.eulang.llvm.directives.LLBaseDirective;
import org.ejs.eulang.llvm.directives.LLDefineDirective;
import org.ejs.eulang.llvm.tms9900.Routine;
import org.junit.Test;

/**
 * @author ejs
 *
 */
public class Test9900InstrSelectionOpt extends BaseInstrTest {

	protected Routine doIselLLVM(String text) throws Exception {
		String name = new Exception().getStackTrace()[1].getMethodName();
		LLModule mod = doLLVMParse(text);
		LLDefineDirective def = null;
		for (LLBaseDirective dir : mod.getDirectives()) {
			if (dir instanceof LLDefineDirective) {
				def = (LLDefineDirective) dir;
				if (name != null && ((LLDefineDirective) dir).getName().getName().contains(name))
					break;
			}
		}
		if (def != null)
			return doIsel(mod, def);

		fail("no code generated:\n" + mod);
		return null;
	}

	

	/* (non-Javadoc)
	 * @see org.ejs.eulang.test.BaseInstrTest#setup()
	 */
	@Override
	public void setup() {
		super.setup();
		doOptimize = true;
		dumpLLVMGen = true;
	}
	

	@Test
	public void testAddrStore() throws Exception {
		/*
		 * 
@"foo._.Int._.Class$p_$p" = global %"Int._.Class$p_$p" null ; <%"Int._.Class$p_$p"*> [#uses=1]

define i16 @test._.Int._._() optsize {
entry.39:
  %0 = load %"Int._.Class$p_$p"* @"foo._.Int._.Class$p_$p" ; <%"Int._.Class$p_$p"> [#uses=1]
  %1 = tail call i16 %0(%"Class$p" null)          ; <i16> [#uses=1]
  ret i16 %1
}
		 */
		dumpIsel = true;
		doIsel(
				"Class = data {};\n"+
				"Derived = Class + data {};\n"+
				"foo : code(p:Class^ => Int);\n"+
				"test = code() {\n"+
				"y : Class;\n" + 
				"   x : Derived^ = &y;\n"+
				"   foo(x);\n"+
				"};\n"
				);

    	
	}
}
