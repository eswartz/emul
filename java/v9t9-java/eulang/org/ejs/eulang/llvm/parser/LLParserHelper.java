/**
 * 
 */
package org.ejs.eulang.llvm.parser;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.llvm.ILLCodeTarget;
import org.ejs.eulang.llvm.LLModule;
import org.ejs.eulang.llvm.directives.LLTargetDataTypeDirective;

/**
 * @author ejs
 *
 */
public class LLParserHelper {

	public final LLModule module;
	public ILLCodeTarget currentTarget;
	private TypeEngine typeEngine;

	/**
	 * 
	 */
	public LLParserHelper(LLModule module) {
		this.module = module;
		this.typeEngine = module.getTarget().getTypeEngine();
	}
	
	public void addTargetDataLayoutDirective(String desc) {
		module.addModuleDirective(new LLTargetDataTypeDirective(typeEngine));
	}
}
