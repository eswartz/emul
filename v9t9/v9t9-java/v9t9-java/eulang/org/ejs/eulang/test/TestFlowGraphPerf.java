/**
 * 
 */
package org.ejs.eulang.test;

import java.util.Random;

import org.ejs.eulang.llvm.LLModule;
import org.ejs.eulang.llvm.directives.LLDefineDirective;
import org.ejs.eulang.llvm.tms9900.Block;
import org.ejs.eulang.llvm.tms9900.FlowGraphVisitor;
import org.ejs.eulang.llvm.tms9900.LinkedRoutine;
import org.ejs.eulang.symbols.GlobalScope;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLType;
import org.junit.Test;

/**
 * Test flowgraph algorithm performance.  I'm not gonna sweat the concrete time too much, but
 * I want to be sure I don't have any realy terrible regressions here.
 * @author ejs
 *
 */
public class TestFlowGraphPerf extends BaseInstrTest {

	private LLModule mod;
	private LLDefineDirective def;
	private LinkedRoutine rout;
	
	protected void makeRoutine(int numBlocks) {
		mod = new LLModule(typeEngine, v9t9Target, new GlobalScope());
		def = createDefine(mod, "test", typeEngine.VOID, new LLType[0]);
		rout = new LinkedRoutine(def);
		
		Random rand = new Random(1234);
		
		Block[] blocks = new Block[numBlocks];
		for (int i = 0; i < numBlocks; i++) {
			String name = "B" + i;
			ISymbol sym = def.getScope().add(name, true);
			blocks[i] = new Block(sym);
			rout.addBlock(blocks[i]);
			if (i == 0)
				rout.setEntry(blocks[i]);
		}
		for (int i = 0; i < numBlocks; i++) {
			for (int j = 0; j < rand.nextInt(4); j++) {
				if (rand.nextInt(numBlocks) < 10)
					blocks[i].addSucc(blocks[rand.nextInt(j+1)]);
				else
					blocks[i].addSucc(blocks[rand.nextInt(numBlocks - j) + j]);
			}
		}
	}
	/**
	 * @param nodes
	 */
	private void timeIt(int nodes) {
		makeRoutine(nodes);
		
		//rout.accept(new RoutineDumper());
		
		long start = System.currentTimeMillis();
		for (int cnt = 0; cnt < 10; cnt++) {
			FlowGraphVisitor visitor = new FlowGraphVisitor();
			visitor.setupFlow(false);
			rout.accept(visitor);
		}
		long end = System.currentTimeMillis();
		System.out.println("Time: " + (end - start) / 10 + " ms each");
	}

	@Test
	public void testFlowgraphSmall() throws Exception {
		timeIt(10);
	}
	@Test
	public void testFlowgraphMedium() throws Exception {
		timeIt(100);
	}
	
	@Test
	public void testFlowgraphBig() throws Exception {
		timeIt(1000);
	}
	
	
	//@Test
	public void testFlowgraphHuge() throws Exception {
		timeIt(10000);
	}
	
	
}

