/**
 * 
 */
package org.ejs.eulang.llvm;

import java.util.ArrayList;
import java.util.List;

import org.ejs.eulang.llvm.directives.LLBaseDirective;
import org.ejs.eulang.llvm.directives.LLDefineDirective;
import org.ejs.eulang.llvm.directives.LLTargetDataTypeDirective;

/**
 * @author ejs
 *
 */
public class LLModule {

	List<LLBaseDirective> directives;
	
	/**
	 * 
	 */
	public LLModule() {
		directives = new ArrayList<LLBaseDirective>();
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (LLBaseDirective d : directives) {
			sb.append(d);
			sb.append('\n');
		}
		return sb.toString();
	}

	/**
	 * @param llTargetDataTypeDirective
	 */
	public void add(LLBaseDirective directive) {
		directives.add(directive);
	};
	
}
