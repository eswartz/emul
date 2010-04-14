/**
 * 
 */
package org.ejs.eulang.test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.ParserRuleReturnScope;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.Tree;
import org.ejs.eulang.IOperation;
import org.ejs.eulang.Message;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.DumpAST;
import org.ejs.eulang.ast.ExpandAST;
import org.ejs.eulang.ast.GenerateAST;
import org.ejs.eulang.ast.IAstDefineStmt;
import org.ejs.eulang.ast.IAstModule;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstScope;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.IAstTypedNode;
import org.ejs.eulang.ast.IAstUnaryExpr;
import org.ejs.eulang.optimize.SimplifyTree;
import org.ejs.eulang.parser.EulangLexer;
import org.ejs.eulang.parser.EulangParser;
import org.ejs.eulang.symbols.GlobalScope;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeInference;

/**
 * @author ejs
 *
 */
public class BaseParserTest {

	/**
	 * 
	 */
	public BaseParserTest() {
		super();
	}
	
	protected ParserRuleReturnScope parse(String method, String str, boolean expectError)
			throws RecognitionException {
		System.err.flush();
		System.out.flush();
		final StringBuilder errors = new StringBuilder();
		try {
	    	// create a CharStream that reads from standard input
	        EulangLexer lexer = new EulangLexer(new ANTLRStringStream(str)) {
	        	/* (non-Javadoc)
	        	 * @see org.antlr.runtime.BaseRecognizer#emitErrorMessage(java.lang.String)
	        	 */
	        	@Override
	        	public void emitErrorMessage(String msg) {
	        		errors.append( msg +"\n");
	        	}
	        };
	        
	        // create a buffer of tokens pulled from the lexer
	        CommonTokenStream tokens = new CommonTokenStream(lexer);
	        // create a parser that feeds off the tokens buffer
	        EulangParser parser = new EulangParser(tokens);
	        // begin parsing at rule
	        ParserRuleReturnScope prog = null;
	        if(method == null)
	        	prog = parser.prog();
	        else {
	    		try {
					prog = (ParserRuleReturnScope) parser.getClass().getMethod(method).invoke(parser);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				}
	        		
	        }
	        System.out.println("\n"+str);
	        if (!expectError) {
				if (parser.getNumberOfSyntaxErrors() > 0 || lexer.getNumberOfSyntaxErrors() > 0) {
					System.err.println(errors);
					fail(errors.toString());
				}
			} else {
				assertTrue(parser.getNumberOfSyntaxErrors() > 0 || lexer.getNumberOfSyntaxErrors() > 0);
			}
	        
	        if (prog != null && prog.getTree() != null)
	        	System.out.println(((Tree) prog.getTree()).toStringTree());
	
	        if (!expectError)
	        	assertTrue("did not consume all input", tokens.index() >= tokens.size());
	
	        return prog;
		} finally {
			System.err.flush();
			System.out.flush();
			
		}
	}

	protected void parse(String str) throws Exception {
		parse(null, str, false);
	}

	protected void parseFail(String str) throws Exception {
		parse(null, str, true);
	}

	protected void parseAt(String method, String str) throws Exception {
		parse(method, str, false);
	}

	protected TypeEngine typeEngine = new TypeEngine();
	protected boolean dumpSimplify;
	protected boolean dumpTreeize;
	protected boolean dumpTypeInfer;
	protected boolean dumpExpand;

	protected IAstNode treeize(String method, String pmethod, String str, boolean expectError) throws Exception {
    	ParserRuleReturnScope ret = parse(method, str, false);
    	if (ret == null)
    		return null;
    	
    	Tree tree = (Tree) ret.getTree();
    	if (tree == null)
    		return null;
    	
    	
    	Map<CharStream, String> fileMap = new HashMap<CharStream, String>();
		String mainFileName = "<string>";
		GenerateAST gen = new GenerateAST(typeEngine, mainFileName, fileMap);
    	
    	IAstNode node = null;
    		
    	if(method == null) {
        	node = gen.constructModule(tree);
        	((IAstModule) node).getNonFileText().put(mainFileName, str);
    	} else {
    		try {
				node = (IAstNode) gen.getClass().getMethod(method).invoke(tree);
			} catch (Exception e) {
				throw e;
			}
    	}
	 
    	if (dumpTreeize || (!expectError && gen.getErrors().size() > 0)) {
	    	DumpAST dump = new DumpAST(System.out);
	    	node.accept(dump);
    	}
     	
    	if (!expectError) {
    		 if (gen.getErrors().size() > 0) {
    			 String msgs = catenate(gen.getErrors());
    			 fail(msgs);
    		 }
    	} else {
    		 if (gen.getErrors().isEmpty()) {
    			 fail("no errors generated");
    		 }
    	}
    		 
    	return node;
    }
 
    /**
	 * @param errors
	 * @return
	 */
	protected String catenate(List<? extends Message> errors) {
		StringBuilder sb = new StringBuilder();
		for (Message e : errors) {
			sb.append(e.toString());
			sb.append('\n');
		}
		return sb.toString();
	}

	protected IAstModule treeize(String str) throws Exception {
    	return (IAstModule) treeize(null, null, str, false);
    }
	protected IAstModule treeizeFail(String str) throws Exception {
    	return (IAstModule) treeize(null, null, str, true);
    }
    
    
    /**
     * @param symIds 
     * @param nodeIds 
     * @param mod
     */
    protected void doSanityTest(IAstNode node, Set<Integer> nodeIds) {
    	assertFalse(nodeIds.contains(node.getId()));
    	nodeIds.add(node.getId());
    	if (node instanceof IAstScope)
    		scopeTest(((IAstScope) node).getScope());
    	assertNotNull(node+"", node);
    	assertNotNull(node+"", node.getChildren());
    	assertNotNull("source for " + node.getClass()+"", node.getSourceRef());
    	for (IAstNode kid : node.getChildren()) {
    		assertSame(kid+"",  node, kid.getParent());
    		assertEquals(node, kid.getParent());
    		
    		if (node instanceof IAstScope && kid instanceof IAstScope) {
    			assertEquals(((IAstScope)node).getScope(), ((IAstScope) kid).getScope().getOwner());
    		}
    		doSanityTest(kid, nodeIds);
    		
    		//assertTrue("source containment " + kid+"", node.getSourceRef().contains(kid.getSourceRef()));
    	}
    	
    }
    protected void sanityTest(IAstNode node) {
    	Set<Integer> nodeIds = new HashSet<Integer>();
    	doSanityTest(node, nodeIds);
    	
    	IAstNode copy = node.copy(null);
    	
    	//DumpAST dump = new DumpAST(System.out);
    	//copy.accept(dump);
    	checkCopy(node, copy);
    }

	/**
	 * Make sure a full copy works
	 * @param node
	 * @param copy
	 */
	private void checkCopy(IAstNode node, IAstNode copy) {
		assertEquals(node.getClass(), copy.getClass());
		assertFalse(node.toString(), node == copy);
		if (node.getParent() != null)
			assertFalse(node.toString(), node.getParent() == copy.getParent());
		IAstNode[] kids = node.getChildren();
		IAstNode[] copyKids = copy.getChildren();
		assertEquals(node.toString() +  ": children count differ", kids.length, copyKids.length);
		if (node instanceof IAstTypedNode)
			assertEquals(node.toString() + ": types differ", ((IAstTypedNode) node).getType(), ((IAstTypedNode) copy).getType());
		
		if (node instanceof IAstScope) {
			IScope scope = ((IAstScope) node).getScope();
			IScope copyScope = ((IAstScope) copy).getScope();
			assertFalse(node.toString(), scope == copyScope);
			assertEquals(node.toString() + ": scope count", scope.getSymbols().length, copyScope.getSymbols().length);
			
			for (ISymbol symbol : scope) {
				ISymbol copySym = copyScope.get(symbol.getUniqueName());
				assertSame(copyScope, copySym.getScope());
				assertEquals(symbol+"", symbol, copySym);
				// a symbol may refer to dead code
				if (symbol.getDefinition() == null || symbol.getDefinition().getParent() != null)
					assertEquals(symbol+"", symbol.getDefinition(), copySym.getDefinition());
				assertFalse(symbol+"", symbol == copySym);
				assertFalse(symbol+"", symbol.getDefinition() != null && symbol.getDefinition() == copySym.getDefinition());
				if (symbol.getDefinition() != null) {
					assertFalse(symbol+"", symbol.getDefinition() == copySym.getDefinition());
				}
			}
			
			assertEquals(node.toString() + ": scopes differ", scope, copyScope);
		}
		
		for (int  i = 0; i < kids.length; i++) {
			assertSame(copy+"", copy, copyKids[i].getParent());
			// look out for dead definitions
			if (kids[i].getParent() == copy)
				checkCopy(kids[i], copyKids[i]);
		}
		
		// now that a rough scan worked, check harder
		assertEquals(node.toString() + ": #equals()", node, copy);
		
		assertEquals(node.toString() + ": #hashCode()", node.hashCode(), copy.hashCode());
	}

	/**
	 * @param scope
	 */
	protected void scopeTest(IScope scope) {
		if (!(scope instanceof GlobalScope))
			assertNotNull(scope.getOwner());
		Set<Integer> seenIds = new HashSet<Integer>();
		Set<String> seen = new HashSet<String>();
		for (ISymbol sym : scope) {
			assertNotNull(sym);
			assertNotNull(sym.getName());
			assertNotNull(sym.getUniqueName());
			assertFalse(sym.getUniqueName(), seen.contains(sym.getUniqueName()));
			seen.add(sym.getUniqueName());
			assertSame(scope, sym.getScope());
			assertFalse(sym.getUniqueName(), seenIds.contains(sym.getNumber()));
			seenIds.add(sym.getNumber());
		}
	}
	
	protected void typeTest(IAstNode node, boolean allowUnknown) {
		if (!allowUnknown && node instanceof IAstTypedNode) {
			if (!(node instanceof IAstDefineStmt) && !(node.getParent() instanceof IAstDefineStmt)) {
				assertNotNull("No type: " +node.toString(), ((IAstTypedNode) node).getType());
				assertTrue("Unresolved type: " +node.toString(), ((IAstTypedNode) node).getType().isComplete());
			}
		}
		for (IAstNode kid : node.getChildren()) {
			assertNotNull(node.toString(), kid);
			typeTest(kid, allowUnknown);
		}
	}
 
	protected void doTypeInfer(IAstNode mod) {
		doTypeInfer(mod, false);
	}
	protected void doTypeInfer(IAstNode mod, boolean expectErrors) {
		TypeInference infer = new TypeInference(typeEngine);
		List<Message> messages = infer.getMessages();
		
		
		infer.infer(mod, true);
		if (dumpTypeInfer || (!expectErrors && messages.size() > 0)) {
			DumpAST dump = new DumpAST(System.out);
			mod.accept(dump);
		}
		//System.out.println("Inference: " + passes + " passes");
		for (Message msg : messages)
			System.err.println(msg);
		if (!expectErrors)
			assertEquals("expected no errors: " + catenate(messages), 0, messages.size());
		else
			assertTrue("expected errors", messages.size() > 0);
	}
	
	protected void doSimplify(IAstNode mod) {
		SimplifyTree simplify = new SimplifyTree(typeEngine);
		
		// must infer types first
		doTypeInfer(mod);
		
		int depth = mod.getDepth();
		
		int passes = 0;
		while (passes++ <= depth) {
			boolean changed = simplify.simplify(mod);
			
			if (!changed) 
				break;
			
			if (dumpSimplify) {
				System.err.flush();
				System.out.println("After simplification:");
				DumpAST dump = new DumpAST(System.out);
				mod.accept(dump);
			}
			
		}
		System.out.println("Simplification: " + passes + " passes");
	}
	
	

	protected IAstNode doExpand(IAstNode node) {
		ExpandAST expand = new ExpandAST();
		
		for (int passes = 1; passes < 256; passes++) {
			List<Message> messages = new ArrayList<Message>();
			boolean changed = expand.expand(messages, node);
			
			if (changed) {
				if (dumpExpand || messages.size() > 0) {
					System.out.println("After expansion pass " + passes + ":");
					DumpAST dump = new DumpAST(System.out);
					node.accept(dump);
				}
				
				for (Message msg : messages)
					System.err.println(msg);
				assertEquals(catenate(messages), 0, messages.size());
			} else {
				break;
			}
		}
		return node;
	}

	protected boolean isCastTo(IAstTypedExpr expr, LLType type) {
		return (expr instanceof IAstUnaryExpr && ((IAstUnaryExpr) expr).getOp() == IOperation.CAST)
		&& expr.getType().equals(type);
	}
	

	protected IAstModule doFrontend(String text) throws Exception {
		IAstModule mod = treeize(text);
    	sanityTest(mod);
    	
    	TypeInference infer = new TypeInference(typeEngine);
    	infer.infer(mod, false);
    	sanityTest(mod);
    	
    	
    	IAstModule expanded = (IAstModule) doExpand(mod);
    	sanityTest(expanded);
    	
    	System.err.flush();
		System.out.println("After doExpand:");
		DumpAST dump = new DumpAST(System.out);
		expanded.accept(dump);
		
    	doTypeInfer(expanded);
    	doSimplify(expanded);
    	
    	System.err.flush();
		System.out.println("After frontend:");
		dump = new DumpAST(System.out);
		expanded.accept(dump);
		
    	return expanded;
	}
	

}