/**
 * 
 */
package org.ejs.eulang.app;

import org.ejs.eulang.ITarget;
import org.ejs.eulang.TargetV9t9;

/**
 * Compile and link multiple modules into one 
 * @author ejs
 *
 */
public class CompLink {

	private final ITarget target;

	/**
	 * @param targetV9t9
	 */
	public CompLink(ITarget target) {
		this.target = target;
	}

	public static void main(String[] args) {
		TargetV9t9 target = new TargetV9t9();
		
		Linker linker = new Linker(target);
		String outputName = "a.out";
		
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (arg.equals("-o")) {
				i++;
				outputName = args[i];
				continue;
			}
			Compiler compiler = new Compiler(target);
			CompilerOutput output = compiler.compile(arg);
			linker.add(output);
		}
		LinkerOutput linkOutput = linker.link(outputName);
		
	}

}
