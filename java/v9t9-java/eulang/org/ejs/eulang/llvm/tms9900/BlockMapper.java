/**
 * 
 */
package org.ejs.eulang.llvm.tms9900;

import java.util.LinkedHashMap;
import java.util.Map;

import org.ejs.eulang.llvm.LLBlock;
import org.ejs.eulang.llvm.LLCodeVisitor;
import org.ejs.eulang.llvm.directives.LLDefineDirective;

/**
 * @author ejs
 *
 */
public class BlockMapper  {

	private Map<LLBlock, Integer> blockMap = new LinkedHashMap<LLBlock, Integer>();
	
	public BlockMapper(LLDefineDirective dir) {
		
		dir.accept(new LLCodeVisitor() {
			int index = 0;
			@Override
			public boolean enterBlock(LLBlock block) {
				blockMap.put(block, index++);
				return true;
			}
		});
	}
	
	/**
	 * @return the blockMap
	 */
	public Map<LLBlock, Integer> getBlockMap() {
		return blockMap;
	}
}
