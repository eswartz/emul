/**
 * 
 */
package org.ejs.eulang.test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ejs.eulang.llvm.LLModule;
import org.ejs.eulang.llvm.directives.LLDefineDirective;
import org.ejs.eulang.llvm.tms9900.Block;
import org.ejs.eulang.llvm.tms9900.CodeVisitor;
import org.ejs.eulang.llvm.tms9900.FlowGraphVisitor;
import org.ejs.eulang.llvm.tms9900.LinkedRoutine;
import org.ejs.eulang.llvm.tms9900.Block.Edge;
import org.ejs.eulang.symbols.GlobalScope;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLType;
import org.junit.Before;
import org.junit.Test;

/**
 * @author ejs
 *
 */
public class TestStockFlowGraph extends BaseInstrTest {

	protected Block B0, B1, B2, B3, B4, B5, B6;
	private LLModule mod;
	private LLDefineDirective def;
	private LinkedRoutine rout;
	
	@Before
	public void makeRoutine() {
		mod = new LLModule(typeEngine, v9t9Target, new GlobalScope());
		def = createDefine(mod, "test", typeEngine.VOID, new LLType[0]);
		rout = new LinkedRoutine(def);
		
		B0 = new Block(getSym("B0"));
		B1 = new Block(getSym("B1"));
		B2 = new Block(getSym("B2"));
		B3 = new Block(getSym("B3"));
		B4 = new Block(getSym("B4"));
		B5 = new Block(getSym("B5"));
		B6 = new Block(getSym("B6"));
		
		B0.addSucc(B1);
		B0.addSucc(B5);
		rout.addBlock(B0);
		rout.setEntry(B0);

		B1.addSucc(B2);
		B1.addSucc(B4);
		rout.addBlock(B1);
		
		B2.addSucc(B3);
		B2.addSucc(B6);
		rout.addBlock(B2);
		
		B3.addSucc(B4);
		B3.addSucc(B2);
		rout.addBlock(B3);

		B4.addSucc(B1);
		B4.addSucc(B5);
		rout.addBlock(B4);

		rout.addBlock(B5);
		rout.setExit(B5);
		
		B6.addSucc(B3);
		rout.addBlock(B6);

		FlowGraphVisitor visitor = new FlowGraphVisitor();
		visitor.setupFlow(false);		// no explicit instrs
		rout.accept(visitor);

	}
	/**
	 * @param string
	 * @return
	 */
	private ISymbol getSym(String string) {
		return def.getScope().addTemporary(string);
	}
	/**
	 * Test the basic flowgraph visitor and edge analyzer
	 * @throws Exception
	 */
	@Test
	public void testFlowgraph1() throws Exception {
		
		Map<Block, Edge> edges;
		edges = B0.getEdges();
		assertEquals(B0.getSucc().length, edges.size());
		assertEquals(Edge.TREE, edges.get(B1));
		assertEquals(Edge.FORWARD, edges.get(B5));

		edges = B1.getEdges();
		assertEquals(B1.getSucc().length, edges.size());
		assertEquals(Edge.TREE, edges.get(B2));
		assertEquals(Edge.FORWARD, edges.get(B4));
		
		edges = B2.getEdges();
		assertEquals(B2.getSucc().length, edges.size());
		assertEquals(Edge.TREE, edges.get(B3));
		assertEquals(Edge.TREE, edges.get(B6));
		
		edges = B3.getEdges();
		assertEquals(B3.getSucc().length, edges.size());
		assertEquals(Edge.TREE, edges.get(B4));
		assertEquals(Edge.BACK, edges.get(B2));
		
		edges = B4.getEdges();
		assertEquals(B4.getSucc().length, edges.size());
		assertEquals(Edge.TREE, edges.get(B5));
		assertEquals(Edge.BACK, edges.get(B1));
		
		edges = B5.getEdges();
		assertEquals(B5.getSucc().length, edges.size());
		
		edges = B6.getEdges();
		assertEquals(B6.getSucc().length, edges.size());
		assertEquals(Edge.CROSS, edges.get(B3));
		
	}
	/**
	 * Test the basic flowgraph visitor and edge analyzer
	 * @throws Exception
	 */
	@Test
	public void testDominators1() throws Exception {
		assertEquals(B0, B1.getIdom());
		
		assertEquals(B0, B5.getIdom());
		
		assertEquals(B1, B2.getIdom());
		assertEquals(B1, B4.getIdom());
		
		assertEquals(B2, B3.getIdom());
		assertEquals(B2, B6.getIdom());
		
		List<Block> ch;
		
		ch = B0.getDominatedChildren();
		assertEquals(2, ch.size());
		assertTrue(ch.contains(B5));
		assertTrue(ch.contains(B1));
		
		ch = B1.getDominatedChildren();
		assertEquals(2, ch.size());
		assertTrue(ch.contains(B2));
		assertTrue(ch.contains(B4));
		
		ch = B2.getDominatedChildren();
		assertEquals(2, ch.size());
		assertTrue(ch.contains(B3));
		assertTrue(ch.contains(B6));
		
		assert(B5.getDominatedChildren().isEmpty());
		assert(B4.getDominatedChildren().isEmpty());
		assert(B3.getDominatedChildren().isEmpty());
		assert(B6.getDominatedChildren().isEmpty());
	}
	
	@Test
	public void testDominatorPaths() throws Exception {
		
		final List<List<Block>> paths = new ArrayList<List<Block>>();
		rout.accept(new CodeVisitor() {

			List<Block> path;
			@Override
			public Walk getWalk() {
				return Walk.DOMINATOR_PATHS;
			}
			
			/* (non-Javadoc)
			 * @see org.ejs.eulang.llvm.tms9900.CodeVisitor#enterBlock(org.ejs.eulang.llvm.tms9900.Block)
			 */
			@Override
			public boolean enterBlock(Block block) {
				if (block.getIdom() == null)  {
					System.out.println("new path");
					path = new ArrayList<Block>();
					paths.add(path);
				}
				System.out.println("\t"+ block.getLabel());
				path.add(block);
				return false;
			}
			
		});
		
		assertEquals(4, paths.size());
		List<Block> path;
		path = paths.get(0);
		assertEquals(2, path.size());
		assertTrue(path.contains(B0));
		assertTrue(path.contains(B5));
		
		path = paths.get(1);
		assertEquals(3, path.size());
		assertTrue(path.contains(B0));
		assertTrue(path.contains(B1));
		assertTrue(path.contains(B4));
		
		path = paths.get(2);
		assertEquals(4, path.size());
		assertTrue(path.contains(B0));
		assertTrue(path.contains(B1));
		assertTrue(path.contains(B2));
		assertTrue(path.contains(B3));
		
		path = paths.get(3);
		assertEquals(4, path.size());
		assertTrue(path.contains(B0));
		assertTrue(path.contains(B1));
		assertTrue(path.contains(B2));
		assertTrue(path.contains(B6));
		
		
	}
}

