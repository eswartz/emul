/**
 * 
 */
package org.ejs.eulang.test;

import static junit.framework.Assert.*;

import java.lang.reflect.InvocationTargetException;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.ParserRuleReturnScope;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.Tree;
import org.ejs.eulang.EulangLexer;
import org.ejs.eulang.EulangParser;
import org.junit.Test;
/**
 * @author ejs
 *
 */
public class TestParser  {

    protected ParserRuleReturnScope run(String method, String str, boolean expectError) throws RecognitionException {
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
    
    protected void run(String str) throws Exception {
    	run(null, str, false);
    }
    
    protected void runFail(String str) throws Exception {
    	run(null, str, true);
    }
    
    protected void runAt(String method, String str) throws Exception {
    	run(method, str, false);
    }
    
    @Test
    public void testEmpty() throws Exception {
    	run("  \n\n");
    }
    @Test
    public void testNumber1() throws Exception {
    	runAt("atom", "3192");
    }
    @Test
    public void testNumber2() throws Exception {
    	runAt("atom", "0x100FFFp100");
    }
    @Test
    public void testNumber3() throws Exception {
    	runAt("atom", "0x1.00FFFp100");
    }
    @Test
    public void testNumber4() throws Exception {
    	runAt("atom", "129.39281e203");
    }
    @Test
    public void testChar1() throws Exception {
    	runAt("atom", "'9'");
    }
    @Test
    public void testChar2() throws Exception {
    	runAt("atom", "'\\u0004'");
    }
    @Test
    public void testString1() throws Exception {
    	runAt("atom", "\"\"");
    }
    @Test
    public void testString2() throws Exception {
    	runAt("atom", "\"There is stuff in here\"");
    }
    @Test
    public void testId1() throws Exception {
    	runAt("atom", "myName");
    }
    @Test
    public void testId2() throws Exception {
    	runAt("atom", "m");
    }
    @Test
    public void testExpr() throws Exception {
    	run("3+6;");
    }
    @Test
    public void testTopLevelAssign() throws Exception  {
    	run("i = 3 + 6 ;");
    }
    @Test
    public void testTopLevelStmtList() throws Exception  {
    	run("i = 3 + 6 ; j = 8 + 9;");
    }
    @Test
    public void testEmptyCodeBlock() throws Exception  {
    	run("myCode = {()};");
    }
    @Test
    public void testExprCodeBlock() throws Exception  {
    	run("myCode = {() 3+6 };");
    }
    @Test
    public void testStmtCodeBlock() throws Exception  {
    	run("myCode = {() i=3+6; j=i*i; };");
    }
    @Test
    public void testSelector1() throws Exception  {
    	run("myCode = [ {() i=3+6; j=i*i; } ];");
    }
    @Test
    public void testSelector2() throws Exception  {
    	run("myCode = [ " +
    			"{() i=3+6; j=i*333; }, " +
    			"{()  }, " +
    			"{(a:Int,b)  }, " +	// (1) not a scope ref, (2) allow trailing comma
    			"]" +
    			";");
    }
    @Test
    public void testSelector2b() throws Exception  {
    	run("myCode = [  ]" +
    			";");
    }
    @Test
    public void testSelector2c() throws Exception  {
    	runFail("myCode = [ , ]" +
    			";");
    }
    @Test
    public void testCodeBlockArgs1() throws Exception  {
    	run("sqrAdd = {( x, y) x*x+y };");
    }
    @Test
    public void testCodeBlockArgs1b() throws Exception  {
    	run("sqrAdd = {( x, y,) x*x+y };");
    }
    @Test
    public void testCodeBlockArgs1c() throws Exception  {
    	runFail("sqrAdd = {( ,) return x*x+y; };");
    }
    @Test
    public void testCodeBlockArgs2() throws Exception  {
    	run("myCode = {( a : Int = 10 , b= 10 , c: Float)  } ;");
    }
    @Test
    public void testCodeBlockReturns1() throws Exception  {
    	run("sqrAdd = {( => Object )  };");
    }
    @Test
    public void testCodeBlockReturns2() throws Exception  {
    	run("sqrAdd = {( x , y => Object )  };");
    }
    @Test
    public void testCodeBlockFuncCall1a() throws Exception  {
    	run("sqrAdd = {( a,b ) a*a+b } ;"
    			+"myCode = {( a ) sqrAdd(a*10, a-10) ; } ;");
    }
    @Test
    public void testCodeBlockFuncCall1b() throws Exception  {
    	runAt("codestmtlist", "sqrAdd(a*10, a-10);");
    }
    @Test
    public void testCodeBlockFuncCall1c() throws Exception  {
    	runAt("codestmtlist", "sqrAdd(10, -10);");
    }
    @Test
    public void testCodeBlockFuncCall1d() throws Exception  {
    	runAt("codestmtlist", "a = 1; sqrAdd(a, a=2);");
    }
    @Test
    public void testCodeBlockFuncCall2a() throws Exception  {
    	runAt("codestmtlist", "sqrAdd() + sqrAdd();");
    }
    @Test
    public void testCodeBlockFuncCall2b() throws Exception  {
    	runAt("codestmtlist", "sqrAdd(a*10, a-10) + sqrAdd(10,20);");
    }
    @Test
    public void testCodeBlockFuncCall2c() throws Exception  {
    	runAt("codestmtlist", "a = 1 / sqrAdd(-1, -2);");
    }
    @Test
    public void testExpr1() throws Exception  {
    	runAt("rhsExpr", "-1 - -1");
    }
    @Test
    public void testExpr2() throws Exception  {
    	runAt("rhsExpr", "(y & 0xff) << 8 + 5");
    }
    @Test
    public void testExpr2b() throws Exception  {
    	runAt("rhsExpr", "y * z + a / c + d & 11");
    }
    @Test
    public void testExpr2c() throws Exception  {
    	runAt("rhsExpr", "y & z ^ d");
    }
    @Test
    public void testExpr2d() throws Exception  {
    	runAt("rhsExpr", "y | z >>> a \\ c ^ d & 11");
    }
    @Test
    public void testCondExpr2b() throws Exception  {
    	runAt("rhsExpr", "(y*2 > 0 && x < 10) ? true : false");
    }
    @Test
    public void testCondExpr2c() throws Exception  {
    	runAt("rhsExpr", "a ? (b ? 1 : 2) : 3");
    }
    @Test
    public void testCondExpr2d() throws Exception  {
    	runAt("rhsExpr", "a==4 ? ((y*2 > 0 && x < 10) ? a<<9!=0 : a!=4) : rout(a)");
    }
    @Test
    public void testProto() throws Exception  {
    	run("run = (x,y);");
    }
    @Test
    public void testNoTopLevelStmts() throws Exception  {
    	runFail("return 3;");
    }
    @Test
    public void testListCompr0() throws Exception  {
    	runAt("listCompr", "for T in [ Int, Float ] : {( x : T, y : T => T )  x*x+y }");
    }
    @Test
    public void testListCompr1() throws Exception  {
    	run("sqrAdd = [ for T in [ Int, Float ] : {( x : T, y : T => T )  x*x+y } ] ; ");
    }
    @Test
    public void testListCompr2() throws Exception  {
    	run("sqrAdd = [ for T, U in [ Int, Float ] : {( x : T, y : U )  x*x+y } ] ; ");
    }
    @Test
    public void testListCompr3() throws Exception  {
    	run("sqrAdd = [ for T in [ Int, Float ] for U in [ Byte, Double ] : {( x : T, y : U )  x*x+y } ];");
    }
    @Test
    public void testListCompr4() throws Exception  {
    	// TODO: be sure the outer list has three items
    	run("sqrAdd = [ {( x, y, z )} , for T in [ Byte, Double ] : {( x : T, y : U )  x*x+y }, {( a, b, c )} ];");
    }
    @Test
    public void testScope1() throws Exception  {
    	run("{ sqrAdd = (); }");
    }
    @Test
    public void testScope2() throws Exception  {
    	run("{ sqrAdd = (); inner = { foo = 4; }; }");
    }
    @Test
    public void testType1() throws Exception  {
    	runAt("type", "Int&");
    }
    @Test
    public void testType1b() throws Exception  {
    	runAt("varDecl", "x : Int&");
    }
    @Test
    public void testType1c() throws Exception  {
    	runAt("codeStmt", "x : Int&");
    }
    @Test
    public void testType1d() throws Exception  {
    	run("foo = {() x : Int& = 0; };");
    }
    @Test
    public void testCodeExpr1() throws Exception  {
    	run("codeExpr1 = {()  x; } ; ");
    }
    @Test
    public void testCodeExpr2a() throws Exception  {
    	run("codeExpr2 = {()  x.y.z; } ; ");
    }
    @Test
    public void testCodeExpr2b() throws Exception  {
    	run("codeExpr2 = {()  x.y.z(); } ; ");
    }
    
    @Test
    public void testScopeRef1() throws Exception  {
    	run("foo = {() Ref.at; Ref.at(0x8370); };");
    }
    @Test
    public void testRef1() throws Exception  {
    	run("foo = {() x : Int& = Ref.at(0x8370); };");
    }
    @Test
    public void testEmptyICodeBlock() throws Exception  {
    	run("myICode = {*()};");
    }
    @Test
    public void testCallICodeBlock() throws Exception  {
    	run("myICode = {(x,y)}; foo = {() a=*myICode(1,2); };");
    }
    @Test
    public void testAmbiguousProtoOrExpr() throws Exception  {
    	// TODO: test
    	run("myProto = (x,y); myExpr = (10);  myProtoNotExpr = (x);");
    }
    @Test
    public void testScopeOrFloat() throws Exception  {
    	run("foo = {() x.y.z0 + 0x.e0; };");
    }
    @Test
    public void testScopeRefs() throws Exception  {
    	run("scopeRef = {()  :x = x; } ; ");
    }
    @Test
    public void testScopeRefs2() throws Exception  {
    	run("scopeRef = {()  a.b.c = x; } ; ");
    }
    @Test
    public void testScopeRefs3a() throws Exception  {
    	run("scopeRef = {()  :a.b.c = x; } ; ");
    }
    @Test
    public void testScopeRefs3b() throws Exception  {
    	run("scopeRef = {()  ::a.b.c = x; } ; ");
    }
    @Test
    public void testScopeRefs4() throws Exception  {
    	run("scopeRef = {()  :x = x; ::a.b.c = r; } ; ");
    }
}


